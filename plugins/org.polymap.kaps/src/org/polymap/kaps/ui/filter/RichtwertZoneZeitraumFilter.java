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

import org.polymap.core.model.Entity;
import org.polymap.core.project.ILayer;

import org.polymap.rhei.data.entityfeature.AbstractEntityFilter;
import org.polymap.rhei.field.BetweenFormField;
import org.polymap.rhei.field.DateTimeFormField;
import org.polymap.rhei.filter.IFilterEditorSite;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.data.RichtwertzoneComposite;
import org.polymap.kaps.model.data.RichtwertzoneZeitraumComposite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class RichtwertZoneZeitraumFilter
        extends AbstractEntityFilter {

    private static Log log = LogFactory.getLog( RichtwertZoneZeitraumFilter.class );


    public RichtwertZoneZeitraumFilter( ILayer layer ) {
        super( "__kaps--", layer, "nach Zeitraum...", null, 15000, RichtwertzoneComposite.class );
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

        site.addStandardLayout( site.newFormField( result, "date", Date.class, new BetweenFormField(
                new DateTimeFormField(), new DateTimeFormField() ), null, "Gültig ab" ) );

        return result;
    }


    protected Query<? extends Entity> createQuery( IFilterEditorSite site ) {
        RichtwertzoneComposite template = QueryExpressions.templateFor( RichtwertzoneComposite.class );

        Object[] jahre = (Object[])site.getFieldValue( "date" );
        BooleanExpression dExpr = null;
        if (jahre != null) {
            RichtwertzoneZeitraumComposite dateTemplate = QueryExpressions
                    .templateFor( RichtwertzoneZeitraumComposite.class );
            BooleanExpression expr2 = null;

            BooleanExpression ge = jahre[0] != null ? QueryExpressions.ge( dateTemplate.gueltigAb(), (Date)jahre[0] )
                    : null;

            BooleanExpression le = jahre[1] != null ? QueryExpressions.le( dateTemplate.gueltigAb(), (Date)jahre[1] )
                    : null;

            if (ge != null) {
                expr2 = ge;
            }
            if (le != null) {
                expr2 = expr2 == null ? le : QueryExpressions.and( ge, le );
            }
            if (expr2 != null) {
                Query<RichtwertzoneZeitraumComposite> daten = KapsRepository.instance().findEntities(
                        RichtwertzoneZeitraumComposite.class, expr2, 0, -1 );

                for (RichtwertzoneZeitraumComposite kv : daten) {
                    BooleanExpression newExpr = QueryExpressions.eq( template.identity(), kv.zone().get()
                            .identity().get() );
                    if (dExpr == null) {
                        dExpr = newExpr;
                    }
                    else {
                        dExpr = QueryExpressions.or( dExpr, newExpr );
                    }
                }
            }
        }

        return KapsRepository.instance().findEntities( RichtwertzoneComposite.class, dExpr, 0, getMaxResults() );
    }
}
