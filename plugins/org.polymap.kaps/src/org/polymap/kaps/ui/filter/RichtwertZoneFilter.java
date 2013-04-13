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

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;

import org.eclipse.swt.widgets.Composite;

import org.polymap.core.model.Entity;
import org.polymap.core.project.ILayer;

import org.polymap.rhei.data.entityfeature.AbstractEntityFilter;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.filter.IFilterEditorSite;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.data.GemeindeComposite;
import org.polymap.kaps.model.data.RichtwertzoneComposite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class RichtwertZoneFilter
        extends AbstractEntityFilter {

    private static Log log = LogFactory.getLog( RichtwertZoneFilter.class );


    public RichtwertZoneFilter( ILayer layer ) {
        super( "__kaps--", layer, "nach Gemeinde...", null, 15000, RichtwertzoneComposite.class );
    }


    public boolean hasControl() {
        return true;
    }


    public Composite createControl( Composite parent, IFilterEditorSite site ) {
        Composite result = site.createStandardLayout( parent );

        RichtwertzoneComposite template = QueryExpressions
                .templateFor( RichtwertzoneComposite.class );

        Map<String, GemeindeComposite> typen = KapsRepository.instance().entitiesWithNames(
                GemeindeComposite.class );

        site.addStandardLayout( site.newFormField( result, "gemeinde", GemeindeComposite.class,
                new PicklistFormField( typen ), null, "Gemeinde" ) );

        return result;
    }


    protected Query<? extends Entity> createQuery( IFilterEditorSite site ) {
        RichtwertzoneComposite template = QueryExpressions
                .templateFor( RichtwertzoneComposite.class );

        GemeindeComposite gemeinde = (GemeindeComposite)site.getFieldValue( "gemeinde" );
        BooleanExpression expr = gemeinde != null ? QueryExpressions.eq( template.gemeinde(),
                gemeinde ) : null;

        return KapsRepository.instance().findEntities( RichtwertzoneComposite.class, expr, 0,
                getMaxResults() );
    }
}
