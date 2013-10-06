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
import java.util.HashSet;
import java.util.Set;

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
import org.polymap.rhei.field.BetweenValidator;
import org.polymap.rhei.field.DateTimeFormField;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.filter.IFilterEditorSite;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.data.FlurstueckComposite;
import org.polymap.kaps.model.data.FlurstuecksdatenBaulandComposite;
import org.polymap.kaps.model.data.GebaeudeArtComposite;
import org.polymap.kaps.model.data.GemarkungComposite;
import org.polymap.kaps.model.data.GemeindeComposite;
import org.polymap.kaps.model.data.NutzungComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.ui.MyNumberValidator;

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class VertraegeFuerBaujahrUndGebaeudeartFilter
        extends AbstractEntityFilter {

    private static Log log = LogFactory.getLog( VertraegeFuerBaujahrUndGebaeudeartFilter.class );


    public VertraegeFuerBaujahrUndGebaeudeartFilter( ILayer layer ) {
        super( "__kaps--", layer, "Verträge nach Nutzung, Art, Gemeinde und Baujahr...", null, 10000,
                VertragComposite.class );
    }


    public boolean hasControl() {
        return true;
    }


    public Composite createControl( Composite parent, IFilterEditorSite site ) {
        Composite result = site.createStandardLayout( parent );

        site.addStandardLayout( site.newFormField( result, "datum", Date.class, new BetweenFormField(
                new DateTimeFormField(), new DateTimeFormField() ), null, "Vertragsdatum" ) );

        site.addStandardLayout( site.newFormField( result, "gemeinde", GemeindeComposite.class, new PicklistFormField(
                KapsRepository.instance().entitiesWithNames( GemeindeComposite.class ) ), null, "Gemeinde" ) );

        site.addStandardLayout( site.newFormField( result, "nutzung", NutzungComposite.class, new PicklistFormField(
                KapsRepository.instance().entitiesWithNames( NutzungComposite.class ) ), null, "Nutzung" ) );

        site.addStandardLayout( site.newFormField( result, "gebart", GebaeudeArtComposite.class, new PicklistFormField(
                KapsRepository.instance().entitiesWithNames( GebaeudeArtComposite.class ) ), null, "Gebäudeart" ) );

        site.addStandardLayout( site.newFormField( result, "baujahr", Integer.class, new BetweenFormField(
                new StringFormField(), new StringFormField() ), new BetweenValidator( new MyNumberValidator(
                Integer.class ) ), "Baujahr" ) );

        return result;
    }


    protected Query<? extends Entity> createQuery( IFilterEditorSite site ) {

        GebaeudeArtComposite gebaeude = (GebaeudeArtComposite)site.getFieldValue( "gebart" );
        GemeindeComposite gemeinde = (GemeindeComposite)site.getFieldValue( "gemeinde" );
        NutzungComposite nutzung = (NutzungComposite)site.getFieldValue( "nutzung" );

        BooleanExpression fExpr = null;
        VertragComposite template = QueryExpressions.templateFor( VertragComposite.class );

        if (gebaeude != null || nutzung != null || gemeinde != null) {
            FlurstueckComposite flurTemplate = QueryExpressions.templateFor( FlurstueckComposite.class );

            // gemeinde
            BooleanExpression gExpr = null;
            if (gemeinde != null) {
                GemarkungComposite gemarkungTemplate = QueryExpressions.templateFor( GemarkungComposite.class );
                Query<GemarkungComposite> gemarkungen = KapsRepository.instance().findEntities(
                        GemarkungComposite.class, QueryExpressions.eq( gemarkungTemplate.gemeinde(), gemeinde ), 0, -1 );
                for (GemarkungComposite gemarkung : gemarkungen) {
                    BooleanExpression newExpr = QueryExpressions.eq( flurTemplate.gemarkung(), gemarkung );
                    if (gExpr == null) {
                        gExpr = newExpr;
                    }
                    else {
                        gExpr = QueryExpressions.or( gExpr, newExpr );
                    }
                }
            }

            BooleanExpression qExpr = gebaeude != null ? QueryExpressions.eq( flurTemplate.gebaeudeArt(), gebaeude )
                    : null;
            BooleanExpression nExpr = nutzung != null ? QueryExpressions.eq( flurTemplate.nutzung(), nutzung ) : null;

            if (qExpr != null) {
                if (gExpr != null) {
                    qExpr = QueryExpressions.and( qExpr, gExpr );
                }
            }
            else {
                qExpr = gExpr;
            }
            if (qExpr != null) {
                if (nExpr != null) {
                    qExpr = QueryExpressions.and( qExpr, nExpr );
                }
            }
            else {
                qExpr = nExpr;
            }

            Query<FlurstueckComposite> flurstuecke = KapsRepository.instance().findEntities( FlurstueckComposite.class,
                    qExpr, 0, -1 );
            Set<Integer> eingangsNummern = new HashSet<Integer>();
            for (FlurstueckComposite fc : flurstuecke) {
                // mehrere Flurstücke können einem Vertrag angehören
                if (fc.vertrag().get() != null) {
                    Integer eingangsNummer = fc.vertrag().get().eingangsNr().get();
                    if (!eingangsNummern.contains( eingangsNummer )) {
                        BooleanExpression newExpr = QueryExpressions.eq( template.eingangsNr(), eingangsNummer );
                        if (fExpr == null) {
                            fExpr = newExpr;
                        }
                        else {
                            fExpr = QueryExpressions.or( fExpr, newExpr );
                        }
                        eingangsNummern.add( eingangsNummer );
                    }
                }
            }
            // wenn keine gefunden, ungültige Query erzeugen, damit auch keine
            // Verträge gefunden werden
            if (fExpr == null) {
                fExpr = QueryExpressions.eq( template.identity(), "unknown" );
            }
        }

        Object[] jahre = (Object[])site.getFieldValue( "baujahr" );
        BooleanExpression baujahrExpr = null;
        if (jahre != null) {
            FlurstuecksdatenBaulandComposite dateTemplate = QueryExpressions
                    .templateFor( FlurstuecksdatenBaulandComposite.class );
            BooleanExpression expr2 = null;

            BooleanExpression ge = jahre[0] != null ? QueryExpressions.ge( dateTemplate.baujahr(),
                    Integer.parseInt( (String)jahre[0] ) ) : null;

            BooleanExpression le = jahre[1] != null ? QueryExpressions.le( dateTemplate.baujahr(),
                    Integer.parseInt( (String)jahre[1] ) ) : null;

            if (ge != null) {
                expr2 = ge;
            }
            if (le != null) {
                expr2 = expr2 == null ? le : QueryExpressions.and( ge, le );
            }
            if (expr2 != null) {
                Query<FlurstuecksdatenBaulandComposite> daten = KapsRepository.instance().findEntities(
                        FlurstuecksdatenBaulandComposite.class, expr2, 0, -1 );
                Set<Integer> eingangsNummern = new HashSet<Integer>();
                for (FlurstuecksdatenBaulandComposite kv : daten) {
                    Integer eingangsNummer = kv.vertrag().get().eingangsNr().get();
                    if (!eingangsNummern.contains( eingangsNummer )) {
                        eingangsNummern.add( eingangsNummer );
                        BooleanExpression newExpr = QueryExpressions.eq( template.eingangsNr(), eingangsNummer );
                        if (baujahrExpr == null) {
                            baujahrExpr = newExpr;
                        }
                        else {
                            baujahrExpr = QueryExpressions.or( baujahrExpr, newExpr );
                        }
                    }
                }
                if (baujahrExpr == null) {
                    baujahrExpr = QueryExpressions.eq( template.identity(), "unknown" );
                }
            }
        }

        Object[] vertragsDatum = (Object[])site.getFieldValue( "datum" );
        BooleanExpression vertragsDatumExpr = null;
        if (vertragsDatum != null) {
            VertragComposite dateTemplate = QueryExpressions.templateFor( VertragComposite.class );
            BooleanExpression ge = vertragsDatum[0] != null ? QueryExpressions.ge( dateTemplate.vertragsDatum(),
                    (Date)vertragsDatum[0] ) : null;

            BooleanExpression le = vertragsDatum[1] != null ? QueryExpressions.le( dateTemplate.vertragsDatum(),
                    (Date)vertragsDatum[1] ) : null;

            if (ge != null) {
                vertragsDatumExpr = ge;
            }
            if (le != null) {
                vertragsDatumExpr = vertragsDatumExpr == null ? le : QueryExpressions.and( ge, le );
            }
        }

        BooleanExpression allExpr = vertragsDatumExpr;
        if (allExpr != null) {
            if (baujahrExpr != null) {
                allExpr = QueryExpressions.and( allExpr, baujahrExpr );
            }
        }
        else {
            allExpr = baujahrExpr;
        }

        if (allExpr != null) {
            if (fExpr != null) {
                allExpr = QueryExpressions.and( allExpr, fExpr );
            }
        }
        else {
            allExpr = fExpr;
        }

        return KapsRepository.instance().findEntities( VertragComposite.class, allExpr, 0, getMaxResults() );
    }
}
