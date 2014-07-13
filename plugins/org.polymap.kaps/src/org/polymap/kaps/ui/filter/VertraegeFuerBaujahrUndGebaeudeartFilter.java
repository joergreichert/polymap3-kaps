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
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;

import com.google.common.collect.Sets;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.dialogs.MessageDialog;

import org.polymap.core.model.Entity;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.data.entityfeature.AbstractEntityFilter;
import org.polymap.rhei.field.BetweenFormField;
import org.polymap.rhei.field.BetweenValidator;
import org.polymap.rhei.field.DateTimeFormField;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.filter.FilterEditor;
import org.polymap.rhei.filter.IFilterEditorSite;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.data.FlurstueckComposite;
import org.polymap.kaps.model.data.GebaeudeArtComposite;
import org.polymap.kaps.model.data.GemarkungComposite;
import org.polymap.kaps.model.data.GemeindeComposite;
import org.polymap.kaps.model.data.NutzungComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.model.data.VertragsdatenBaulandComposite;
import org.polymap.kaps.ui.MyNumberValidator;

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class VertraegeFuerBaujahrUndGebaeudeartFilter
        extends AbstractEntityFilter {

    private static Log         log = LogFactory.getLog( VertraegeFuerBaujahrUndGebaeudeartFilter.class );

    private IFormFieldListener gemeindeListener;


    public VertraegeFuerBaujahrUndGebaeudeartFilter( ILayer layer ) {
        super( VertraegeFuerBaujahrUndGebaeudeartFilter.class.getName(), layer,
                "nach Nutzung, Art, Gemeinde und Baujahr...", null, 10000, VertragComposite.class );
    }


    public boolean hasControl() {
        return true;
    }


    public Composite createControl( Composite parent, final IFilterEditorSite site ) {
        Composite result = site.createStandardLayout( parent );

        site.addStandardLayout( site.newFormField( result, "datum", Date.class, new BetweenFormField(
                new DateTimeFormField(), new DateTimeFormField() ), null, "Vertragsdatum" ) );

        site.addStandardLayout( site.newFormField( result, "gemeinde", GemeindeComposite.class, new PicklistFormField(
                KapsRepository.instance().entitiesWithNames( GemeindeComposite.class ) ), null, "Gemeinde" ) );

        FilterEditor editor = (FilterEditor)site;

        final PicklistFormField gemarkungen = new PicklistFormField( new PicklistFormField.ValueProvider() {

            @Override
            public SortedMap<String, Object> get() {
                SortedMap<String, Object> gemarkungen = new TreeMap<String, Object>();
                GemeindeComposite gemeinde = (GemeindeComposite)site.getFieldValue( "gemeinde" );
                if (gemeinde != null) {
                    for (GemarkungComposite gemarkung : GemarkungComposite.Mixin.forGemeinde( gemeinde )) {
                        gemarkungen.put( gemarkung.schl().get() + "  -  " + gemarkung.name().get(), gemarkung );
                    }
                }
                return gemarkungen;
            }
        } );

        editor.addFieldListener( gemeindeListener = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if ("gemeinde".equals( ev.getFieldName() )) {
                    gemarkungen.reloadValues();
                }
            }
        } );

        site.addStandardLayout( site.newFormField( result, "gemarkung", GemarkungComposite.class, gemarkungen, null,
                "Gemarkung" ) );

        site.addStandardLayout( site.newFormField( result, "nutzung", NutzungComposite.class, new PicklistFormField(
                KapsRepository.instance().entitiesWithNames( NutzungComposite.class ) ), null, "Nutzung" ) );

        site.addStandardLayout( site.newFormField( result, "gebart", GebaeudeArtComposite.class, new PicklistFormField(
                KapsRepository.instance().entitiesWithNames( GebaeudeArtComposite.class ) ), null, "Gebäudeart" ) );

        site.addStandardLayout( site.newFormField( result, "baujahr", Integer.class, new BetweenFormField(
                new StringFormField(), new StringFormField() ), new BetweenValidator( new MyNumberValidator(
                Integer.class ) ), "Baujahr" ) );

        site.addStandardLayout( site.newFormField( result, "urkunde", String.class, new StringFormField(), null,
                "Urkunde" ) );
        site.addStandardLayout( site.newFormField( result, "notariat", String.class, new StringFormField(), null,
                "Notariat" ) );

        return result;
    }


    protected Query<? extends Entity> createQuery( IFilterEditorSite site ) {

        GebaeudeArtComposite gebaeude = (GebaeudeArtComposite)site.getFieldValue( "gebart" );
        NutzungComposite nutzung = (NutzungComposite)site.getFieldValue( "nutzung" );

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

        // nach Vertragsdatum vorsortieren führt zu StackOverflow
        Set<VertragComposite> vertraegeNachDatum = null;
        if (vertragsDatumExpr != null) {
            vertraegeNachDatum = Sets.newHashSet();
            Query<VertragComposite> vertraege = KapsRepository.instance().findEntities( VertragComposite.class,
                    vertragsDatumExpr, 0, -1 );
            for (VertragComposite vertrag : vertraege) {
                vertraegeNachDatum.add( vertrag );
            }
        }

        BooleanExpression fExpr = null;
        VertragComposite template = QueryExpressions.templateFor( VertragComposite.class );

        // if (gebaeude != null || nutzung != null || gemeinde != null) {
        FlurstueckComposite flurTemplate = QueryExpressions.templateFor( FlurstueckComposite.class );

        // gemeinde
        BooleanExpression gExpr = null;
        GemeindeComposite gemeinde = (GemeindeComposite)site.getFieldValue( "gemeinde" );
        GemarkungComposite gemarkung = (GemarkungComposite)site.getFieldValue( "gemarkung" );
        if (gemarkung != null) {
            gExpr = QueryExpressions.eq( flurTemplate.gemarkung(), gemarkung );
        }
        else if (gemeinde != null) {
            GemarkungComposite gemarkungTemplate = QueryExpressions.templateFor( GemarkungComposite.class );
            Query<GemarkungComposite> gemarkungen = KapsRepository.instance().findEntities( GemarkungComposite.class,
                    QueryExpressions.eq( gemarkungTemplate.gemeinde(), gemeinde ), 0, -1 );
            for (GemarkungComposite gemarkungg : gemarkungen) {
                BooleanExpression newExpr = QueryExpressions.eq( flurTemplate.gemarkung(), gemarkungg );
                if (gExpr == null) {
                    gExpr = newExpr;
                }
                else {
                    gExpr = QueryExpressions.or( gExpr, newExpr );
                }
            }
        }

        BooleanExpression qExpr = gebaeude != null ? QueryExpressions.eq( flurTemplate.gebaeudeArt(), gebaeude ) : null;
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

        Set<VertragComposite> vertraegeNachDatumUndFlurstueck = null;
        if (qExpr != null) {
            // flurstücke eingeschränkt, falls keine gefunden werden ist das set leer
            vertraegeNachDatumUndFlurstueck = new HashSet<VertragComposite>();
            Query<FlurstueckComposite> flurstuecke = KapsRepository.instance().findEntities( FlurstueckComposite.class,
                    qExpr, 0, -1 );
            for (FlurstueckComposite fc : flurstuecke) {
                // mehrere Flurstücke können einem Vertrag angehören
                VertragComposite vertrag = fc.vertrag().get();
                if (vertrag != null) {
                    if (vertraegeNachDatum == null || vertraegeNachDatum.contains( vertrag )) {
                        vertraegeNachDatumUndFlurstueck.add( vertrag );
                    }
                }
            }
        }
        else {
            // ansonsten flurstücke nicht weiter eingeschränkt, nimm alle nach Datum
            vertraegeNachDatumUndFlurstueck = vertraegeNachDatum;
        }

        Object[] jahre = (Object[])site.getFieldValue( "baujahr" );
        Set<VertragComposite> vertraegeNachDatumUndFlurstueckUndBaujahr = null;
        if (jahre != null) {
            vertraegeNachDatumUndFlurstueckUndBaujahr = new HashSet<VertragComposite>();
            VertragsdatenBaulandComposite dateTemplate = QueryExpressions
                    .templateFor( VertragsdatenBaulandComposite.class );
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
                Query<VertragsdatenBaulandComposite> daten = KapsRepository.instance().findEntities(
                        VertragsdatenBaulandComposite.class, expr2, 0, -1 );
                for (VertragsdatenBaulandComposite kv : daten) {
                    VertragComposite v = kv.vertrag().get();
                    if (vertraegeNachDatumUndFlurstueck == null || vertraegeNachDatumUndFlurstueck.contains( v )) {
                        // ist schon in Menge vorhanden, also auch hier rein
                        vertraegeNachDatumUndFlurstueckUndBaujahr.add( v );
                    }
                }
            }
        }
        else {
            vertraegeNachDatumUndFlurstueckUndBaujahr = vertraegeNachDatumUndFlurstueck;
        }

        if (vertraegeNachDatumUndFlurstueckUndBaujahr != null) {
            if (vertraegeNachDatumUndFlurstueckUndBaujahr.size() > 5000) {

                Polymap.getSessionDisplay().asyncExec( new Runnable() {

                    public void run() {
                        MessageDialog.openError( PolymapWorkbench.getShellToParentOn(), "Zu viele Ergebnisse",
                                "Es wurden über 5000 Ergebnisse gefunden. Bitte schränken Sie die Suche weiter ein." );
                    }
                } );
                return KapsRepository.instance().findEntities( VertragComposite.class,
                        QueryExpressions.eq( template.identity(), "unknown" ), 0, -1 );
            }
            for (VertragComposite vertrag : vertraegeNachDatumUndFlurstueckUndBaujahr) {
                BooleanExpression newExpr = QueryExpressions.eq( template.identity(), vertrag.id() );
                if (fExpr == null) {
                    fExpr = newExpr;
                }
                else {
                    fExpr = QueryExpressions.or( fExpr, newExpr );
                }
            }
        }

        String urkunde = (String)site.getFieldValue( "urkunde" );
        if (urkunde != null && !urkunde.trim().isEmpty()) {
            BooleanExpression newExpr = QueryExpressions.eq( template.urkundenNummer(), urkunde.trim() );
            if (fExpr == null) {
                fExpr = newExpr;
            }
            else {
                fExpr = QueryExpressions.and( fExpr, newExpr );
            }
        }
        String notariat = (String)site.getFieldValue( "notariat" );
        if (notariat != null && !notariat.trim().isEmpty()) {
            BooleanExpression newExpr = QueryExpressions.eq( template.notariat(), notariat.trim() );
            if (fExpr == null) {
                fExpr = newExpr;
            }
            else {
                fExpr = QueryExpressions.and( fExpr, newExpr );
            }
        }
        // wenn keine gefunden, ungültige Query erzeugen, damit auch keine
        // Verträge gefunden werden
        if (fExpr == null) {
            fExpr = QueryExpressions.eq( template.identity(), "unknown" );
        }

        return KapsRepository.instance().findEntities( VertragComposite.class, fExpr, 0, getMaxResults() );
    }
}
