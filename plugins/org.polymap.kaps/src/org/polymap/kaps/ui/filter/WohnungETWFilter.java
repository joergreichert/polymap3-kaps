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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;

import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.dialogs.MessageDialog;

import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.field.BetweenFormField;
import org.polymap.rhei.field.BetweenValidator;
import org.polymap.rhei.field.DateTimeFormField;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.field.SelectlistFormField;
import org.polymap.rhei.filter.IFilterEditorSite;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.data.BodennutzungComposite;
import org.polymap.kaps.model.data.FlurstueckComposite;
import org.polymap.kaps.model.data.GebaeudeArtComposite;
import org.polymap.kaps.model.data.GemarkungComposite;
import org.polymap.kaps.model.data.GemeindeComposite;
import org.polymap.kaps.model.data.NutzungComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.model.data.WohnungComposite;
import org.polymap.kaps.ui.NotNullValidator;

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class WohnungETWFilter
        extends KapsEntityFilter<WohnungComposite> {

    private static Log log = LogFactory.getLog( WohnungETWFilter.class );


    public WohnungETWFilter( ILayer layer ) {
        super( WohnungETWFilter.class.getName(), layer, "nach Nutzung, Datum, Gemeinde...", null, 10000, WohnungComposite.class );
    }


    @Override
    public boolean hasControl() {
        return true;
    }


    @Override
    public Composite createControl( Composite parent, IFilterEditorSite site ) {
        Composite result = site.createStandardLayout( parent );

        site.addStandardLayout( site.newFormField( result, "datum", Date.class, new BetweenFormField(
                new DateTimeFormField(), new DateTimeFormField() ), new BetweenValidator( new NotNullValidator() ),
                "Vertragsdatum" ) );

        site.addStandardLayout( site.newFormField( result, "gemeinde", GemeindeComposite.class, new PicklistFormField(
                KapsRepository.instance().entitiesWithNames( GemeindeComposite.class ) ), null, "Gemeinde" ) );

        SelectlistFormField field = new SelectlistFormField( KapsRepository.instance().entitiesWithNames(
                NutzungComposite.class ) );
        field.setIsMultiple( true );
        Composite formField = site.newFormField( result, "nutzung", NutzungComposite.class, field,
                new NotNullValidator(), "Nutzung" );
        site.addStandardLayout( formField );
        ((FormData)formField.getLayoutData()).height = 200;
        ((FormData)formField.getLayoutData()).width = 100;

        field = new SelectlistFormField( KapsRepository.instance().entitiesWithNames( GebaeudeArtComposite.class ) );
        field.setIsMultiple( true );
        formField = site.newFormField( result, "gebaeudeArt", BodennutzungComposite.class, field, null, "Gebäudeart" );
        site.addStandardLayout( formField );
        ((FormData)formField.getLayoutData()).height = 200;
        ((FormData)formField.getLayoutData()).width = 100;

        return result;
    }


    @Override
    protected Query<WohnungComposite> createQuery( IFilterEditorSite site ) {

        List<NutzungComposite> nutzungen = (List<NutzungComposite>)site.getFieldValue( "nutzung" );
        List<GebaeudeArtComposite> gebaeudearten = (List<GebaeudeArtComposite>)site.getFieldValue( "gebaeudeArt" );
        GemeindeComposite gemeinde = (GemeindeComposite)site.getFieldValue( "gemeinde" );

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
        WohnungComposite template = QueryExpressions.templateFor( WohnungComposite.class );

        // if (nutzungen != null || gemeinde != null) {
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
            // vertragsdatum wurde gesetzt, aber keine Verträge gefunden, also auch
            // keine Flurstücke finden
            if (vExpr == null) {
                vExpr = QueryExpressions.eq( flurTemplate.identity(), "unknown" );
            }
        }

        // gemeinde
        BooleanExpression gExpr = null;
        if (gemeinde != null) {
            GemarkungComposite gemarkungTemplate = QueryExpressions.templateFor( GemarkungComposite.class );
            Query<GemarkungComposite> gemarkungen = KapsRepository.instance().findEntities( GemarkungComposite.class,
                    QueryExpressions.eq( gemarkungTemplate.gemeinde(), gemeinde ), 0, -1 );
            for (GemarkungComposite gemarkung : gemarkungen) {
                BooleanExpression newExpr = QueryExpressions.eq( flurTemplate.gemarkung(), gemarkung );
                if (gExpr == null) {
                    gExpr = newExpr;
                }
                else {
                    gExpr = QueryExpressions.or( gExpr, newExpr );
                }
            }
            // gemeinde gewählt, aber keine gemeinden gefunden
            if (gExpr == null) {
                gExpr = QueryExpressions.eq( flurTemplate.identity(), "unknown" );
            }
        }

        // nutzungen
        BooleanExpression nExpr = null;
        if (nutzungen != null) {
            for (NutzungComposite nutzung : nutzungen) {
                BooleanExpression newExpr = QueryExpressions.eq( flurTemplate.nutzung(), nutzung );
                if (nExpr == null) {
                    nExpr = newExpr;
                }
                else {
                    nExpr = QueryExpressions.or( nExpr, newExpr );
                }
            }
        }

        BooleanExpression gaExpr = null;
        if (gebaeudearten != null) {
            for (GebaeudeArtComposite art : gebaeudearten) {
                BooleanExpression newExpr = QueryExpressions.eq( flurTemplate.gebaeudeArt(), art );
                if (gaExpr == null) {
                    gaExpr = newExpr;
                }
                else {
                    gaExpr = QueryExpressions.or( gaExpr, newExpr );
                }
            }
        }

        if (nExpr != null) {
            if (gaExpr != null) {
                nExpr = QueryExpressions.and( nExpr, gaExpr );
            }
        }
        else {
            nExpr = gaExpr;
        }

        if (nExpr != null) {
            if (vExpr != null) {
                nExpr = QueryExpressions.and( nExpr, vExpr );
            }
        }
        else {
            nExpr = vExpr;
        }
        if (nExpr != null) {
            if (gExpr != null) {
                nExpr = QueryExpressions.and( nExpr, gExpr );
            }
        }
        else {
            nExpr = gExpr;
        }

        Query<FlurstueckComposite> flurstuecke = KapsRepository.instance().findEntities( FlurstueckComposite.class,
                nExpr, 0, -1 );
        if (flurstuecke.count() > 5000) {
            Polymap.getSessionDisplay().asyncExec( new Runnable() {

                public void run() {
                    MessageDialog.openError( PolymapWorkbench.getShellToParentOn(), "Zu viele Ergebnisse",
                            "Es wurden zu viele Ergebnisse gefunden. Bitte schränken Sie die Suche weiter ein." );
                }
            } );
            return KapsRepository.instance().findEntities( WohnungComposite.class,
                    QueryExpressions.eq( template.identity(), "unknown" ), 0, -1 );
        }
        for (FlurstueckComposite fc : flurstuecke) {
            // mehrere Flurstücke können einem Vertrag angehören
            BooleanExpression newExpr = QueryExpressions.eq( template.flurstueck(), fc );
            if (fExpr == null) {
                fExpr = newExpr;
            }
            else {
                fExpr = QueryExpressions.or( fExpr, newExpr );
            }
        }
        // wenn keine verträge gefunden, ungültige Query erzeugen, damit auch keine
        // Verträge gefunden werden
        if (fExpr == null) {
            fExpr = QueryExpressions.eq( template.identity(), "unknown" );
        }
        // }

        return KapsRepository.instance().findEntities( WohnungComposite.class, fExpr, 0, getMaxResults() );
    }
}
