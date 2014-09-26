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

import org.eclipse.jface.dialogs.MessageDialog;

import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.field.BetweenFormField;
import org.polymap.rhei.field.BetweenValidator;
import org.polymap.rhei.field.DateTimeFormField;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.filter.IFilterEditorSite;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.data.FlurstueckComposite;
import org.polymap.kaps.model.data.GemarkungComposite;
import org.polymap.kaps.model.data.GemeindeComposite;
import org.polymap.kaps.model.data.NutzungComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.ui.MyNumberValidator;
import org.polymap.kaps.ui.NotNullValidator;

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class VertraegeFuerFlurstueckeFilter
        extends KapsEntityFilter<VertragComposite> {

    private static Log log = LogFactory.getLog( VertraegeFuerFlurstueckeFilter.class );


    public VertraegeFuerFlurstueckeFilter( ILayer layer ) {
        super( VertraegeFuerFlurstueckeFilter.class.getName(), layer, "nach Flurstücken...", null, 10000,
                VertragComposite.class );
    }


    public boolean hasControl() {
        return true;
    }


    public Composite createControl( Composite parent, IFilterEditorSite site ) {
        Composite result = site.createStandardLayout( parent );

        site.addStandardLayout( site.newFormField( result, "datum", Date.class, new BetweenFormField(
                new DateTimeFormField(), new DateTimeFormField() ), new BetweenValidator( new NotNullValidator() ), "Vertragsdatum" ) );

        site.addStandardLayout( site.newFormField( result, "gemeinde", GemeindeComposite.class, new PicklistFormField(
                KapsRepository.instance().entitiesWithNames( GemeindeComposite.class ) ), null, "Gemeinde" ) );

        site.addStandardLayout( site.newFormField( result, "nutzung", NutzungComposite.class, new PicklistFormField(
                KapsRepository.instance().entitiesWithNames( NutzungComposite.class ) ), null, "Nutzung" ) );

        site.addStandardLayout( site.newFormField( result, "nummer", Integer.class, new StringFormField(),
                new MyNumberValidator( Integer.class ), "Flurstücksnummer" ) );

        site.addStandardLayout( site.newFormField( result, "unternummer", Integer.class, new StringFormField(), null,
                "Unternummer" ) );

        return result;
    }


    protected Query<VertragComposite> createQuery( IFilterEditorSite site ) {

        GemeindeComposite gemeinde = (GemeindeComposite)site.getFieldValue( "gemeinde" );
        NutzungComposite nutzung = (NutzungComposite)site.getFieldValue( "nutzung" );
        Integer nummer = (Integer)site.getFieldValue( "nummer" );
        String unternummer = (String)site.getFieldValue( "unternummer" );

        Object[] vertragsDatum = (Object[])site.getFieldValue( "datum" );
        BooleanExpression vertragsDatumExpr = null;
        if (vertragsDatum != null) {
            VertragComposite dateTemplate = QueryExpressions.templateFor( VertragComposite.class );
            BooleanExpression ge = vertragsDatum[0] != null ? QueryExpressions.ge( dateTemplate.vertragsDatum(),
                    dayStart( (Date)vertragsDatum[0] ) ) : null;

            BooleanExpression le = vertragsDatum[1] != null ? QueryExpressions.le( dateTemplate.vertragsDatum(),
                    dayEnd( (Date)vertragsDatum[1] ) ) : null;

            if (ge != null) {
                vertragsDatumExpr = ge;
            }
            if (le != null) {
                vertragsDatumExpr = vertragsDatumExpr == null ? le : QueryExpressions.and( ge, le );
            }
        }

        BooleanExpression fExpr = null;
        VertragComposite template = QueryExpressions.templateFor( VertragComposite.class );

        if (nutzung != null || gemeinde != null || nummer != null || (unternummer != null && !unternummer.isEmpty())) {
            FlurstueckComposite flurTemplate = QueryExpressions.templateFor( FlurstueckComposite.class );

            // nach Vertragsdatum vorsortieren
            BooleanExpression vExpr = null;
            if (vertragsDatumExpr != null) {
                Query<VertragComposite> vertraege = KapsRepository.instance().findEntities( VertragComposite.class,
                        vertragsDatumExpr, 0, -1 );
                for (VertragComposite vertrag : vertraege) {
                    BooleanExpression newExpr = QueryExpressions.eq( flurTemplate.vertrag(), vertrag );
                    if (vExpr == null) {
                        vExpr = newExpr;
                    }
                    else {
                        vExpr = QueryExpressions.or( vExpr, newExpr );
                    }
                }
            }

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

            BooleanExpression hExpr = nummer != null ? QueryExpressions.eq( flurTemplate.hauptNummer(), nummer ) : null;
            BooleanExpression uExpr = unternummer != null && !unternummer.isEmpty() ? QueryExpressions.eq(
                    flurTemplate.unterNummer(), unternummer ) : null;
            BooleanExpression nExpr = nutzung != null ? QueryExpressions.eq( flurTemplate.nutzung(), nutzung ) : null;

            // expressions sammeln
            BooleanExpression qExpr = vExpr;

            if (qExpr != null) {
                if (gExpr != null) {
                    qExpr = QueryExpressions.and( qExpr, gExpr );
                }
            }
            else {
                qExpr = gExpr;
            }
            if (qExpr != null) {
                if (hExpr != null) {
                    qExpr = QueryExpressions.and( qExpr, hExpr );
                }
            }
            else {
                qExpr = hExpr;
            }
            if (qExpr != null) {
                if (uExpr != null) {
                    qExpr = QueryExpressions.and( qExpr, uExpr );
                }
            }
            else {
                qExpr = uExpr;
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
            if (flurstuecke.count() > 5000) {
                Polymap.getSessionDisplay().asyncExec( new Runnable() {

                    public void run() {
                        MessageDialog.openError( PolymapWorkbench.getShellToParentOn(), "Zu viele Ergebnisse",
                                "Es wurden zu viele Ergebnisse gefunden. Bitte schränken Sie die Suche weiter ein." );
                    }
                } );
                return KapsRepository.instance().findEntities( VertragComposite.class,
                        QueryExpressions.eq( template.identity(), "unknown" ), 0, -1 );
            }
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

        BooleanExpression allExpr = vertragsDatumExpr;
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
