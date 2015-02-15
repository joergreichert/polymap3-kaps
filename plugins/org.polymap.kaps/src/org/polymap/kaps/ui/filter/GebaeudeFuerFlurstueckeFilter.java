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

import org.polymap.core.model.Entity;
import org.polymap.core.project.ILayer;

import org.polymap.rhei.data.entityfeature.AbstractEntityFilter;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.filter.FilterEditor;
import org.polymap.rhei.filter.IFilterEditorSite;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.data.FlurstueckComposite;
import org.polymap.kaps.model.data.GebaeudeComposite;
import org.polymap.kaps.model.data.GemarkungComposite;
import org.polymap.kaps.model.data.GemeindeComposite;
import org.polymap.kaps.model.data.StrasseComposite;
import org.polymap.kaps.ui.MyNumberValidator;

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class GebaeudeFuerFlurstueckeFilter
        extends AbstractEntityFilter {

    private static Log         log = LogFactory.getLog( GebaeudeFuerFlurstueckeFilter.class );

    private IFormFieldListener gemeindeListener;


    public GebaeudeFuerFlurstueckeFilter( ILayer layer ) {
        super( GebaeudeFuerFlurstueckeFilter.class.getName(), layer, "nach Gemeinde, Flurstück, Strasse...", null,
                10000, GebaeudeComposite.class );
    }


    public boolean hasControl() {
        return true;
    }


    public Composite createControl( Composite parent, final IFilterEditorSite site ) {
        Composite result = site.createStandardLayout( parent );

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

        final PicklistFormField strassen = new PicklistFormField( new PicklistFormField.ValueProvider() {

            @Override
            public SortedMap<String, Object> get() {
                SortedMap<String, Object> strassen = new TreeMap<String, Object>();
                GemeindeComposite gemeinde = (GemeindeComposite)site.getFieldValue( "gemeinde" );
                if (gemeinde != null) {
                    for (StrasseComposite strasse : StrasseComposite.Mixin.findStrasseIn( gemeinde )) {
                        strassen.put( strasse.name().get(), strasse );
                    }
                }
                return strassen;
            }
        } );

        editor.addFieldListener( gemeindeListener = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if ("gemeinde".equals( ev.getFieldName() )) {
                    gemarkungen.reloadValues();
                    strassen.reloadValues();
                }
            }
        } );

        site.addStandardLayout( site.newFormField( result, "gemarkung", GemarkungComposite.class, gemarkungen, null,
                "Gemarkung" ) );

        site.addStandardLayout( site
                .newFormField( result, "strasse", StrasseComposite.class, strassen, null, "Strasse" ) );

        site.addStandardLayout( site.newFormField( result, "nummer", Integer.class, new StringFormField(),
                new MyNumberValidator( Integer.class ), "Flurstücksnummer" ) );

        site.addStandardLayout( site.newFormField( result, "unternummer", Integer.class, new StringFormField(), null,
                "Unternummer" ) );

        return result;
    }


    protected Query<? extends Entity> createQuery( IFilterEditorSite site ) {

        GemeindeComposite gemeinde = (GemeindeComposite)site.getFieldValue( "gemeinde" );
        GemarkungComposite gemarkung = (GemarkungComposite)site.getFieldValue( "gemarkung" );
        StrasseComposite strasse = (StrasseComposite)site.getFieldValue( "strasse" );
        Integer nummer = (Integer)site.getFieldValue( "nummer" );
        String unternummer = (String)site.getFieldValue( "unternummer" );

        FlurstueckComposite flurTemplate = QueryExpressions.templateFor( FlurstueckComposite.class );

        BooleanExpression hExpr = nummer != null ? QueryExpressions.eq( flurTemplate.hauptNummer(), nummer ) : null;
        BooleanExpression uExpr = unternummer != null && !unternummer.isEmpty() ? QueryExpressions.eq(
                flurTemplate.unterNummer(), unternummer ) : null;

        BooleanExpression gExpr = null;
        if (strasse != null) {
            gExpr = QueryExpressions.eq( flurTemplate.strasse(), strasse );
        }
        if (gemarkung != null) {
            BooleanExpression gemExpr = QueryExpressions.eq( flurTemplate.gemarkung(), gemarkung );
            gExpr = gExpr == null ? gemExpr : and( gExpr, gemExpr );
        }
        if (gExpr == null && gemeinde != null) {
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
            // gemeinde gewählt, aber keine GemarkungComposite gefunden
            if (gExpr == null) {
                gExpr = QueryExpressions.eq( flurTemplate.identity(), "unknown" );
            }
        }

        // expressions sammeln
        BooleanExpression qExpr = hExpr == null ? uExpr : and( hExpr, uExpr );
        qExpr = qExpr == null ? gExpr : and( qExpr, gExpr );

        Set<String> gebaeudeIds = null;
        if (qExpr != null) {
            gebaeudeIds = Sets.newHashSet();

            // flurstücke eingeschränkt, falls keine gefunden werden ist das set leer
            Query<FlurstueckComposite> flurstuecke = KapsRepository.instance().findEntities( FlurstueckComposite.class,
                    qExpr, 0, -1 );
            for (FlurstueckComposite fc : flurstuecke) {
                // contains predicate geht bei ManyAssociaiton nicht, deshalb hier je
                // Flustück checken, in welchen Gebaeuden das drin ist
                for (GebaeudeComposite gebaeude : KapsRepository.instance().findEntities( GebaeudeComposite.class,
                        null, 0, -1 )) {
                    if (gebaeude.flurstuecke().contains( fc )) {
                        gebaeudeIds.add( gebaeude.id() );
                    }
                }
            }
        }
        BooleanExpression gebExpr = null;
        GebaeudeComposite gebaeudeTemplate = QueryExpressions.templateFor( GebaeudeComposite.class );
        if (gebaeudeIds == null) {
            // keine Einschränkung, also alle finden
        }
        else if (gebaeudeIds.isEmpty()) {
            // eingeschränkt nach Flurstücken, aber keine gefunden, also auch keine
            // Gebäude finden
            gebExpr = QueryExpressions.eq( gebaeudeTemplate.identity(), "unknown" );
        }
        else {
            // filtern nach allen ids
            for (String id : gebaeudeIds) {
                BooleanExpression newExpr = QueryExpressions.eq( gebaeudeTemplate.identity(), id );
                if (gebExpr == null) {
                    gebExpr = newExpr;
                }
                else {
                    gebExpr = QueryExpressions.or( gebExpr, newExpr );
                }

            }
        }

        return KapsRepository.instance().findEntities( GebaeudeComposite.class, gebExpr, 0, getMaxResults() );
    }
}
