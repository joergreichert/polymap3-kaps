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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;

import org.eclipse.swt.widgets.Composite;

import org.polymap.core.model.Entity;
import org.polymap.core.project.ILayer;

import org.polymap.rhei.data.entityfeature.AbstractEntityFilter;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.filter.IFilterEditorSite;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.ui.MyNumberValidator;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class EinzelnerVertragFilter
        extends AbstractEntityFilter {

    private static Log log = LogFactory.getLog( EinzelnerVertragFilter.class );


    public EinzelnerVertragFilter( ILayer layer ) {
        super( "__kaps--", layer, "einzelner Vertrag...", null, 10000, VertragComposite.class );
    }


    public boolean hasControl() {
        return true;
    }


    public Composite createControl( Composite parent, IFilterEditorSite site ) {
        Composite result = site.createStandardLayout( parent );

        site.addStandardLayout( site.newFormField( result, "eingangsNr", Integer.class,
                new StringFormField(),
                new MyNumberValidator( Integer.class ), "Eingangsnummer" ) );

        return result;
    }


    protected Query<? extends Entity> createQuery( IFilterEditorSite site ) {
        VertragComposite template = QueryExpressions
                .templateFor( VertragComposite.class );

        Integer nummer = (Integer)site.getFieldValue( "eingangsNr" );
        BooleanExpression expr = nummer != null ? QueryExpressions.eq(
                template.eingangsNr(), nummer ) : null;

        return KapsRepository.instance().findEntities( VertragComposite.class, expr, 0,
                getMaxResults() );
    }
}
