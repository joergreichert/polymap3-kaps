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
import org.polymap.core.runtime.Polymap;

import org.polymap.rhei.data.entityfeature.AbstractEntityFilter;
import org.polymap.rhei.field.BetweenFormField;
import org.polymap.rhei.field.BetweenValidator;
import org.polymap.rhei.field.NumberValidator;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.filter.IFilterEditorSite;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.data.FlurstueckComposite;
import org.polymap.kaps.model.data.GebaeudeArtComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.model.data.VertragsdatenBaulandComposite;

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class VertraegeFuerBaujahrUndGebaeudeartFilter
        extends AbstractEntityFilter {

    private static Log log = LogFactory.getLog( VertraegeFuerBaujahrUndGebaeudeartFilter.class );


    public VertraegeFuerBaujahrUndGebaeudeartFilter( ILayer layer ) {
        super( "__kaps--", layer, "Verträge nach Gebäudeart und Baujahr...", null, 10000,
                VertragComposite.class );
    }


    public boolean hasControl() {
        return true;
    }


    public Composite createControl( Composite parent, IFilterEditorSite site ) {
        Composite result = site.createStandardLayout( parent );

        Map<String, GebaeudeArtComposite> typen = KapsRepository.instance().entitiesWithNames(
                GebaeudeArtComposite.class );

        site.addStandardLayout( site.newFormField( result, "gebart", GebaeudeArtComposite.class,
                new PicklistFormField( typen ), null, "Gebäudeart" ) );

        site.addStandardLayout( site.newFormField(
                result,
                "date",
                Integer.class,
                new BetweenFormField( new StringFormField(), new StringFormField() ),
                new BetweenValidator( new NumberValidator( Integer.class, Polymap
                        .getSessionLocale() ) ), "Baujahr" ) );

        return result;
    }


    protected Query<? extends Entity> createQuery( IFilterEditorSite site ) {

        GebaeudeArtComposite gebaeude = (GebaeudeArtComposite)site.getFieldValue( "gebart" );
        FlurstueckComposite flurTemplate = QueryExpressions.templateFor( FlurstueckComposite.class );
        BooleanExpression expr = gebaeude != null ? QueryExpressions.eq(
                flurTemplate.gebaeudeArt(), gebaeude ) : null;

        Query<FlurstueckComposite> flurstuecke = KapsRepository.instance().findEntities(
                FlurstueckComposite.class, expr, 0, -1 );

        Object[] jahre = (Object[])site.getFieldValue( "date" );
        VertragsdatenBaulandComposite dateTemplate = QueryExpressions
                .templateFor( VertragsdatenBaulandComposite.class );
        BooleanExpression expr2 = null;
        if (jahre != null) {
            expr2 = QueryExpressions.and(
                    QueryExpressions.ge( dateTemplate.baujahr(),
                            Integer.parseInt( (String)jahre[0] ) ),
                    QueryExpressions.le( dateTemplate.baujahr(),
                            Integer.parseInt( (String)jahre[1] ) ) );
        }
        Query<VertragsdatenBaulandComposite> daten = KapsRepository.instance().findEntities(
                VertragsdatenBaulandComposite.class, expr2, 0, -1 );

        VertragComposite template = QueryExpressions.templateFor( VertragComposite.class );
        BooleanExpression dExpr = null;
        for (VertragsdatenBaulandComposite kv : daten) {
            BooleanExpression newExpr = QueryExpressions.eq( template.eingangsNr(), kv
                    .kaufvertrag().get().eingangsNr().get() );
            if (dExpr == null) {
                dExpr = newExpr;
            }
            else {
                dExpr = QueryExpressions.or( dExpr, newExpr );
            }
        }
        BooleanExpression fExpr = null;
        for (FlurstueckComposite fc : flurstuecke) {
            BooleanExpression newExpr = QueryExpressions.eq( template.eingangsNr(), fc.vertrag()
                    .get().eingangsNr().get() );
            if (fExpr == null) {
                fExpr = newExpr;
            }
            else {
                fExpr = QueryExpressions.or( fExpr, newExpr );
            }
        }

        BooleanExpression allExpr = dExpr;
        if (allExpr != null) {
            if (dExpr != null) {
                allExpr = QueryExpressions.and( allExpr, dExpr );
            }
        }
        else {
            allExpr = fExpr;
        }

        return KapsRepository.instance().findEntities( VertragComposite.class, allExpr, 0,
                getMaxResults() );

    }
}
