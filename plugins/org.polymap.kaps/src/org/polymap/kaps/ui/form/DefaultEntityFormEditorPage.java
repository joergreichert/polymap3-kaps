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
package org.polymap.kaps.ui.form;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import java.lang.reflect.Method;

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;

import org.polymap.core.model.Entity;
import org.polymap.core.model.EntityType;
import org.polymap.core.model.EntityType.Property;
import org.polymap.core.qi4j.QiModule;
import org.polymap.core.runtime.Polymap;

import org.polymap.rhei.data.entityfeature.AssociationAdapter;
import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.CheckboxFormField;
import org.polymap.rhei.field.DateTimeFormField;
import org.polymap.rhei.field.NumberValidator;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.Named;
import org.polymap.kaps.model.SchlNamed;
import org.polymap.kaps.ui.KapsDefaultFormEditorPage;
import org.polymap.kaps.ui.NotNullValidator;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class DefaultEntityFormEditorPage
        extends KapsDefaultFormEditorPage {

    private static Log         log = LogFactory.getLog( DefaultEntityFormEditorPage.class );

    protected Entity           composite;

    private String             editorTitle;

    private final List<String> propertyNames;


    // private final Class<? extends Entity> type;

    public <T extends Entity> DefaultEntityFormEditorPage( Feature feature, FeatureStore featureStore, Class<T> type,
            QiModule module, String editorTitle, String... properties ) {
        super( DefaultEntityFormEditorPage.class.getName(), "Grunddaten", feature, featureStore );

        composite = repository.findEntity( type, feature.getIdentifier().getID() );
        this.editorTitle = editorTitle;
        // this.type = type;
        this.propertyNames = new ArrayList<String>();

        if (properties.length == 0) {
            EntityType<?> entityType = composite.getEntityType();
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


    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );
        String objectName = "";
        if (composite instanceof SchlNamed) {
            objectName = ((SchlNamed)composite).schl().get();
        }
        site.setEditorTitle( formattedTitle( editorTitle, objectName, null ) );
        site.setFormTitle( formattedTitle( editorTitle, objectName, getTitle() ) );

        Composite parent = site.getPageBody();
        Composite lastLine = null;

        EntityType<?> entityType = composite.getEntityType();
        // sort after labeling
        Map<String, String> labels = new TreeMap<String, String>();
        for (String propertyName : propertyNames) {
            labels.put( labelFor( propertyName ), propertyName );
        }

        // special handling for the key schl
        if (composite instanceof SchlNamed && labels.containsValue( "schl" )) {
            lastLine = newFormField( labelFor( "schl" ) ).setEnabled( ((SchlNamed)composite).schl().get() == null )
                    .setProperty( new PropertyAdapter( ((SchlNamed)composite).schl() ) )
                    .setField( new StringFormField() ).setValidator( new NotNullValidator() )
                    .setLayoutData( left().top( lastLine ).create() ).create();
        }

        for (String label : labels.keySet()) {
            String propertyName = labels.get( label );
            if ("schl".equals( propertyName )) {
                continue;
            }
            Property property = entityType.getProperty( propertyName );
            // if (!(property instanceof EntityType.ManyAssociation)) {
            Class propertyType = property.getType();

            Object delegate = null;
            try {
                Method m = composite.getClass().getMethod( property.getName(), new Class[0] );
                delegate = m.invoke( composite, new Object[0] );
            }
            catch (Exception e) {
                throw new IllegalStateException( "this must never be thrown", e );
            }

            // lastLine = newFormField( "Schlüssel" ).setEnabled(
            // composite.schl().get() == null )
            // .setProperty( new PropertyAdapter( composite.schl() ) ).setField( new
            // StringFormField() )
            // .setValidator( new NotNullValidator() ).setLayoutData( left().top(
            // lastLine ).create() ).create();
            //
            //
            // lastLine = newFormField( "Bezeichung" ).setProperty( new
            // PropertyAdapter( composite.name() ) )
            // .setValidator( new NotNullValidator() ).setField( new
            // StringFormField() )
            // .setLayoutData( left().top( lastLine ).create() ).create();
            //
            // lastLine = lastLine;
            // lastLine = newFormField( "Gebäudeart" ).setToolTipText(
            // "Gebäudeart entsprechend Statistischem Bundesamt" )
            // .setProperty( new AssociationAdapter<GebaeudeArtStaBuComposite>(
            // composite.gebaeudeArtStabu() ) )
            // .setField( namedAssocationsPicklist( GebaeudeArtStaBuComposite.class )
            // )
            // .setLayoutData( left().top( lastLine ).create() ).create();
            FormFieldBuilder fieldBuilder = newFormField( label ).setToolTipText( tooltipFor( propertyName ) ).setLayoutData( left().top( lastLine ).create() );
            if (String.class.isAssignableFrom( propertyType )) {
                fieldBuilder.setProperty( new PropertyAdapter( (org.qi4j.api.property.Property)delegate ) )
                        .setField( new StringFormField() );
            }
            else if (Integer.class.isAssignableFrom( propertyType )) {
                fieldBuilder.setProperty( new PropertyAdapter( (org.qi4j.api.property.Property)delegate ) )
                        .setField( new StringFormField() )
                        .setValidator( new NumberValidator( Integer.class, Polymap.getSessionLocale() ) );
            }
            else if (Double.class.isAssignableFrom( propertyType )) {
                fieldBuilder.setProperty( new PropertyAdapter( (org.qi4j.api.property.Property)delegate ) )
                        .setField( new StringFormField() )
                        .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 2, 1, 2 ) );
            }
            else if (Date.class.isAssignableFrom( propertyType )) {
                fieldBuilder.setProperty( new PropertyAdapter( (org.qi4j.api.property.Property)delegate ) )
                        .setField( new DateTimeFormField() );
            }
            else if (Boolean.class.isAssignableFrom( propertyType )) {
                fieldBuilder.setProperty( new PropertyAdapter( (org.qi4j.api.property.Property)delegate ) )
                        .setField( new CheckboxFormField() );
            }
            else if (Named.class.isAssignableFrom( propertyType )) {
                fieldBuilder.setProperty( new AssociationAdapter( (org.qi4j.api.entity.association.Association)delegate ) )
                        .setField( namedAssocationsPicklist( propertyType ) );
            }
            lastLine = fieldBuilder.create();
        }
    }


    private Map<String, ? extends Object> valuesFor( Class propertyType ) {
        return ((KapsRepository)repository).entitiesWithNames( propertyType );
    }


    public DefaultEntityFormEditorPage exclude( String... names ) {
        for (String name : names) {
            this.propertyNames.remove( name );
        }
        return this;
    }


    protected String labelFor( String name ) {
        if ("schl".equals( name )) {
            return "Schlüssel";
        }
        return name.substring( 0, 1 ).toUpperCase() + name.substring( 1 );
    }


    protected String tooltipFor( String name ) {
        return labelFor(name);
    }
}
