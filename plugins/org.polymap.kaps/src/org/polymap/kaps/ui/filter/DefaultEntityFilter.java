/*
 * polymap.org Copyright 2013 Polymap GmbH. All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.kaps.ui.filter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;

import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;

import org.polymap.core.model.Entity;
import org.polymap.core.model.EntityType;
import org.polymap.core.model.EntityType.Property;
import org.polymap.core.project.ILayer;
import org.polymap.core.qi4j.QiModule;

import org.polymap.rhei.field.BetweenFormField;
import org.polymap.rhei.field.BetweenValidator;
import org.polymap.rhei.field.DateTimeFormField;
import org.polymap.rhei.field.SelectlistFormField;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.filter.IFilterEditorSite;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.Named;
import org.polymap.kaps.ui.MyNumberValidator;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class DefaultEntityFilter<T extends Entity>
        extends KapsEntityFilter<T> {

    public static abstract class PropertyFilter<T> {

        @SuppressWarnings("rawtypes")
        public abstract Iterable<org.qi4j.api.property.Property> getVisibleProperties( T template );
    }

    private static Log         log = LogFactory.getLog( DefaultEntityFilter.class );

    private QiModule           module;

    private final List<String> propertyNames;

    private final Class<T>     type;


    // public <T extends Entity> DefaultEntityFilter( ILayer layer, Class<T> type,
    // QiModule module ) {
    // super( "__kaps--", layer, "Standard...", null, 10000, type );
    // this.module = module;
    // this.propertyNames = new ArrayList();
    // EntityType<?> entityType = module.entityType( entityClass );
    // for (Property property : entityType.getProperties()) {
    // propertyNames.add( property.getName() );
    // }
    // // Collections.sort( this.propertyNames );
    // }

    public DefaultEntityFilter( ILayer layer, Class<T> type, QiModule module, String... properties ) {
        super( DefaultEntityFilter.class.getName(), layer, "Standard...", null, 10000, type );
        this.type = type;
        this.module = module;

        this.propertyNames = new ArrayList<String>();

        if (properties.length == 0) {
            EntityType<?> entityType = module.entityType( type );
            for (Property property : entityType.getProperties()) {
                propertyNames.add( property.getName() );
            }
        }
        else {
            for (String name : properties) {
                this.propertyNames.add( name );
            }
        }
    }


    public DefaultEntityFilter( ILayer layer, Class<T> type, QiModule module, PropertyFilter filter ) {
        super( "__kaps--", layer, "Standard...", null, 10000, type );
        this.type = type;
        this.module = module;

        this.propertyNames = new ArrayList<String>();

        if (filter == null) {
            EntityType<?> entityType = module.entityType( type );
            for (Property property : entityType.getProperties()) {
                propertyNames.add( property.getName() );
            }
        }
        else {
            for (org.qi4j.api.property.Property prop : (Iterable<org.qi4j.api.property.Property>)filter
                    .getVisibleProperties( QueryExpressions.templateFor( type ) )) {
                this.propertyNames.add( prop.qualifiedName().name() );
            }
        }
    }


    public boolean hasControl() {
        return true;
    }


    public Composite createControl( Composite parent, IFilterEditorSite site ) {
        Composite result = site.createStandardLayout( parent );

        EntityType<?> entityType = module.entityType( entityClass );
        // sort after labeling
        Map<String, String> labels = new TreeMap<String, String>();
        for (String propertyName : propertyNames) {
            labels.put( labelFor( propertyName ), propertyName );
        }

        for (String label : labels.keySet()) {
            String propertyName = labels.get( label );
            Property property = entityType.getProperty( propertyName );
            // if (!(property instanceof EntityType.ManyAssociation)) {
            Class propertyType = property.getType();
            if (String.class.isAssignableFrom( propertyType )) {
                site.addStandardLayout( site.newFormField( result, property.getName(), String.class,
                        new StringFormField(), null, label ) );
            }
            else if (Integer.class.isAssignableFrom( propertyType )) {
                site.addStandardLayout( site.newFormField( result, property.getName(), Integer.class,
                        new BetweenFormField( new StringFormField(), new StringFormField() ), new BetweenValidator(
                                new MyNumberValidator( Integer.class ) ), label ) );
            }
            else if (Double.class.isAssignableFrom( propertyType )) {
                site.addStandardLayout( site.newFormField( result, property.getName(), Double.class,
                        new BetweenFormField( new StringFormField(), new StringFormField() ), new BetweenValidator(
                                new MyNumberValidator( Double.class, 2 ) ), label ) );
            }
            else if (Date.class.isAssignableFrom( propertyType )) {
                site.addStandardLayout( site.newFormField( result, property.getName(), Date.class,
                        new BetweenFormField( new DateTimeFormField(), new DateTimeFormField() ), null, label ) );
            }
            else if (Named.class.isAssignableFrom( propertyType )) {
                SelectlistFormField field = new SelectlistFormField( valuesFor( propertyType ) );
                field.setIsMultiple( true );
                Composite formField = site.newFormField( result, property.getName(), propertyType, field, null, label );
                site.addStandardLayout( formField );
                ((FormData)formField.getLayoutData()).height = 100;
                ((FormData)formField.getLayoutData()).width = 100;
            }
            // }
        }

        return result;
    }


    protected Map<String, ? extends Object> valuesFor( Class propertyType ) {
    	// TODO: when testing with an attribute having 4500 values, the filter form never gets build,
    	// as this seems to be too many entries for SelectlistFormField
    	// using plain strings instead of real Vertrag objects doesn't help as there are still
    	// many entries in the list
    	// only proper solution could be something like pagination - this solution here
    	// only restricts the result to max 200 values
        return ((KapsRepository)module).entitiesWithNames( propertyType, 200 );
    }


    public DefaultEntityFilter exclude( String... names ) {
        for (String name : names) {
            this.propertyNames.remove( name );
        }
        return this;
    }


    protected String labelFor( String name ) {
        return name.substring( 0, 1 ).toUpperCase() + name.substring( 1 );
    }


    @Override
    protected Query<T> createQuery( IFilterEditorSite site ) {
        try {

            BooleanExpression expr = null;

            EntityType<?> entityType = module.entityType( entityClass );
            Entity template = QueryExpressions.templateFor( entityClass );

            for (Property property : entityType.getProperties()) {
                Object value = site.getFieldValue( property.getName() );
                if (value != null) {
                    BooleanExpression currentExpression = null;
                    Class propertyType = property.getType();
                    Method propertyMethod = entityClass.getDeclaredMethod( property.getName(), new Class[0] );
                    if (String.class.isAssignableFrom( propertyType )) {
                        currentExpression = createStringExpression( template, value, propertyMethod );
                    }
                    else if (Boolean.class.isAssignableFrom( propertyType )) {
                        currentExpression = createBooleanExpression( template, value, propertyMethod );
                    }
                    else if (Integer.class.isAssignableFrom( propertyType )) {
                        currentExpression = createIntegerExpression( template, value, propertyMethod );
                    }
                    else if (Double.class.isAssignableFrom( propertyType )) {
                        currentExpression = createDoubleExpression( template, value, propertyMethod );
                    }
                    else if (Date.class.isAssignableFrom( propertyType )) {
                        currentExpression = createDateExpression( template, value, propertyMethod );
                    }
                    else if (Named.class.isAssignableFrom( propertyType )) {
                        currentExpression = createNamedExpression( template, value, propertyMethod );
                    }
                    if (currentExpression != null) {
                        expr = (expr == null) ? currentExpression : QueryExpressions.and( expr, currentExpression );
                    }
                }
            }
            return module.findEntities( type, expr, 0, getMaxResults() );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }


    private BooleanExpression createBooleanExpression( Entity template, Object value, Method propertyMethod )
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        return QueryExpressions.eq(
                (org.qi4j.api.property.Property<Boolean>)propertyMethod.invoke( template, new Object[0] ),
                (Boolean)value );
    }


    private BooleanExpression createStringExpression( Entity template, Object value, Method propertyMethod )
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        String match = (String)value;
        if (!match.isEmpty()) {
            if (match.indexOf( '*' ) == -1 && match.indexOf( '?' ) == -1) {
                match = '*' + match + '*';
            }
            return QueryExpressions.matches(
                    (org.qi4j.api.property.Property<String>)propertyMethod.invoke( template, new Object[0] ), match );
        }
        return null;
    }


    private BooleanExpression createNamedExpression( Entity template, Object value, Method propertyMethod )
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException,
            NoSuchMethodException {
        List<Named> values = (List<Named>)value;
        BooleanExpression expr = null;
        for (Named named : values) {
            Object p = propertyMethod.invoke( template, new Object[0] );
            BooleanExpression current = null;
            if (p instanceof Association) {
                current = QueryExpressions.eq( (Association<Named>)p, named );
            }
            else {
                // must be manyassociation
                Method identity = entityClass.getMethod( "identity", new Class[0] );
                for (Object entity : module.findEntities( entityClass, null, 0, 10000 )) {
                    if (((ManyAssociation<Named>)propertyMethod.invoke( entity, new Object[0] )).contains( named )) {
                        BooleanExpression newExpr = QueryExpressions.eq(
                                (org.qi4j.api.property.Property<String>)identity.invoke( template, new Object[0] ),
                                ((Entity)entity).id() );
                        if (current == null) {
                            current = newExpr;
                        }
                        else {
                            current = QueryExpressions.or( current, newExpr );
                        }
                    }
                }
            }
            if (expr == null) {
                expr = current;
            }
            else {
                expr = QueryExpressions.or( expr, current );
            }
        }
        return expr;
    }


    private BooleanExpression createIntegerExpression( Entity template, Object value, Method propertyMethod )
            throws IllegalAccessException, InvocationTargetException {
        BooleanExpression currentExpression;
        Object[] betweenValues = (Object[])value;
        BooleanExpression ge = betweenValues[0] != null ? QueryExpressions.ge(
                (org.qi4j.api.property.Property<Integer>)propertyMethod.invoke( template, new Object[0] ),
                Integer.parseInt( (String)betweenValues[0] ) ) : null;

        BooleanExpression le = betweenValues[1] != null ? QueryExpressions.le(
                (org.qi4j.api.property.Property<Integer>)propertyMethod.invoke( template, new Object[0] ),
                Integer.parseInt( (String)betweenValues[1] ) ) : null;

        currentExpression = ge;
        if (le != null) {
            currentExpression = currentExpression == null ? le : QueryExpressions.and( ge, le );
        }
        return currentExpression;
    }


    private BooleanExpression createDoubleExpression( Entity template, Object value, Method propertyMethod )
            throws IllegalAccessException, InvocationTargetException {
        BooleanExpression currentExpression;
        Object[] betweenValues = (Object[])value;
        BooleanExpression ge = betweenValues[0] != null ? QueryExpressions.ge(
                (org.qi4j.api.property.Property<Double>)propertyMethod.invoke( template, new Object[0] ),
                Double.parseDouble( (String)betweenValues[0] ) ) : null;

        BooleanExpression le = betweenValues[1] != null ? QueryExpressions.le(
                (org.qi4j.api.property.Property<Double>)propertyMethod.invoke( template, new Object[0] ),
                Double.parseDouble( (String)betweenValues[1] ) ) : null;

        currentExpression = ge;
        if (le != null) {
            currentExpression = currentExpression == null ? le : QueryExpressions.and( ge, le );
        }
        return currentExpression;
    }


    private BooleanExpression createDateExpression( Entity template, Object value, Method propertyMethod )
            throws IllegalAccessException, InvocationTargetException {
        BooleanExpression currentExpression;
        Object[] betweenValues = (Object[])value;
        BooleanExpression ge = betweenValues[0] != null ? QueryExpressions.ge(
                (org.qi4j.api.property.Property<Date>)propertyMethod.invoke( template, new Object[0] ),
                dayStart((Date)betweenValues[0] )) : null;

        BooleanExpression le = betweenValues[1] != null ? QueryExpressions.le(
                (org.qi4j.api.property.Property<Date>)propertyMethod.invoke( template, new Object[0] ),
                dayEnd((Date)betweenValues[1] )) : null;

        currentExpression = ge;
        if (le != null) {
            currentExpression = currentExpression == null ? le : QueryExpressions.and( ge, le );
        }
        return currentExpression;
    }
}
