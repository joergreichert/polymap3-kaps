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

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;

import org.eclipse.swt.widgets.Composite;

import org.polymap.core.project.ILayer;

import org.polymap.rhei.filter.IFilterEditorSite;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.data.RichtwertzoneZeitraumComposite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class BrokenRichtwertZoneZeitraumFilter
        extends KapsEntityFilter<RichtwertzoneZeitraumComposite> {

    private static Log log = LogFactory.getLog( BrokenRichtwertZoneZeitraumFilter.class );


    public BrokenRichtwertZoneZeitraumFilter( ILayer layer ) {
        super( BrokenRichtwertZoneZeitraumFilter.class.getName(), layer, "mit fehlenden Angaben...", null, 15000,
                RichtwertzoneZeitraumComposite.class );
    }


    public boolean hasControl() {
        return true;
    }


    public Composite createControl( Composite parent, IFilterEditorSite site ) {
        Composite result = site.createStandardLayout( parent );

        // RichtwertzoneComposite template = QueryExpressions
        // .templateFor( RichtwertzoneComposite.class );
        //
        // Map<String, GemeindeComposite> typen =
        // KapsRepository.instance().entitiesWithNames(
        // GemeindeComposite.class );
        //
        // site.addStandardLayout( site.newFormField( result, "gemeinde",
        // GemeindeComposite.class,
        // new PicklistFormField( typen ), null, "Gemeinde" ) );

        // site.addStandardLayout( site.newFormField( result, "date", Date.class, new
        // BetweenFormField(
        // new DateTimeFormField(), new DateTimeFormField() ), null, "Gültig ab" ) );

        return result;
    }


    protected Query<RichtwertzoneZeitraumComposite> createQuery( IFilterEditorSite site ) {
        RichtwertzoneZeitraumComposite template = QueryExpressions.templateFor( RichtwertzoneZeitraumComposite.class );

        BooleanExpression expr = QueryExpressions.or( QueryExpressions.eq( template.schl(), "--- Zone fehlt ---" ),
                QueryExpressions.eq( template.gueltigAb(), new Date(0) ));

        return KapsRepository.instance().findEntities( RichtwertzoneZeitraumComposite.class, expr, 0, getMaxResults() );
    }
}
