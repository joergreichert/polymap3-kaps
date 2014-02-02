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
public class FlurstueckeFuerVertragGemeindeFilter
        extends AbstractEntityFilter {

    private static Log         log = LogFactory.getLog( FlurstueckeFuerVertragGemeindeFilter.class );

    private IFormFieldListener gemeindeListener;


    public FlurstueckeFuerVertragGemeindeFilter( ILayer layer ) {
        super( FlurstueckeFuerVertragGemeindeFilter.class.getName(), layer,
                "nach Vertrag, Gemeinde, Nutzung, Nummern...", null, 10000, FlurstueckComposite.class );
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

        site.addStandardLayout( site.newFormField( result, "nummer", Integer.class, new StringFormField(),
                new MyNumberValidator( Integer.class ), "Flurstücksnummer" ) );

        site.addStandardLayout( site.newFormField( result, "unternummer", Integer.class, new StringFormField(), null,
                "Unternummer" ) );

        return result;
    }


    protected Query<? extends Entity> createQuery( IFilterEditorSite site ) {

        GemeindeComposite gemeinde = (GemeindeComposite)site.getFieldValue( "gemeinde" );
        GemarkungComposite gemarkung = (GemarkungComposite)site.getFieldValue( "gemarkung" );
        NutzungComposite nutzung = (NutzungComposite)site.getFieldValue( "nutzung" );
        Integer nummer = (Integer)site.getFieldValue( "nummer" );
        String unternummer = (String)site.getFieldValue( "unternummer" );

        FlurstueckComposite flurTemplate = QueryExpressions.templateFor( FlurstueckComposite.class );

        BooleanExpression hExpr = nummer != null ? QueryExpressions.eq( flurTemplate.hauptNummer(), nummer ) : null;
        BooleanExpression uExpr = unternummer != null && !unternummer.isEmpty() ? QueryExpressions.eq(
                flurTemplate.unterNummer(), unternummer ) : null;
        BooleanExpression nExpr = nutzung != null ? QueryExpressions.eq( flurTemplate.nutzung(), nutzung ) : null;

        BooleanExpression gExpr = null;
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
            // gemeinde gewählt, aber keine gemeinden gefunden
            if (gExpr == null) {
                gExpr = QueryExpressions.eq( flurTemplate.identity(), "unknown" );
            }
        }

        // expressions sammeln
        BooleanExpression qExpr = and( hExpr, uExpr );
        qExpr = and( qExpr, nExpr );
        qExpr = and( qExpr, gExpr );

        return KapsRepository.instance().findEntities( FlurstueckComposite.class, qExpr, 0, getMaxResults() );
    }
}
