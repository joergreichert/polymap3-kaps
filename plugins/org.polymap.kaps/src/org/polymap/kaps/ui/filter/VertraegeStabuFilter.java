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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
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
import org.polymap.kaps.model.data.FlurstueckComposite;
import org.polymap.kaps.model.data.GemarkungComposite;
import org.polymap.kaps.model.data.GemeindeComposite;
import org.polymap.kaps.model.data.NutzungComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.ui.NotNullValidator;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class VertraegeStabuFilter
        extends AbstractEntityFilter {

    private static Log log = LogFactory.getLog( VertraegeStabuFilter.class );


    public VertraegeStabuFilter( ILayer layer ) {
        super( "__kaps--", layer, "für StaBu...", null, 15000, VertragComposite.class );
    }


    public boolean hasControl() {
        return true;
    }


    public Composite createControl( Composite parent, IFilterEditorSite site ) {
        Composite result = site.createStandardLayout( parent );

        site.addStandardLayout( site.newFormField( result, "Quartal", String.class, new PicklistFormField(
                new String[] { "1", "2", "3", "4" } ), new NotNullValidator(), "Quartal" ) );

        Calendar cal = new GregorianCalendar();
        int thisYear = cal.get( Calendar.YEAR ) - 2;
        List<String> jahre = new ArrayList<String>();
        for (int i = 0; i < 5; i++) {
            jahre.add( String.valueOf( thisYear + i ) );
        }

        site.addStandardLayout( site.newFormField( result, "Jahr", String.class, new PicklistFormField( jahre ),
                new NotNullValidator(), "Jahr" ) );

        Map<String, GemeindeComposite> typen = KapsRepository.instance().entitiesWithNames( GemeindeComposite.class );

        site.addStandardLayout( site.newFormField( result, "gemeinde", GemeindeComposite.class, new PicklistFormField(
                typen ), null, "Gemeinde" ) );

        return result;
    }


    protected Query<? extends Entity> createQuery( IFilterEditorSite site ) {

        Integer quartal = Integer.parseInt( (String)site.getFieldValue( "Quartal" ) );
        Integer jahr = Integer.parseInt( (String)site.getFieldValue( "Jahr" ) );

        Calendar lowerCal = new GregorianCalendar();
        lowerCal.set( Calendar.YEAR, jahr );
        lowerCal.set( Calendar.DAY_OF_MONTH, 1 );
        lowerCal.set( Calendar.HOUR_OF_DAY, 0 );
        lowerCal.set( Calendar.MINUTE, 0 );
        lowerCal.set( Calendar.SECOND, 0 );
        lowerCal.set( Calendar.MILLISECOND, 0 );

        Calendar upperCal = new GregorianCalendar();
        upperCal.setTime( lowerCal.getTime() );

        switch (quartal) {
            case 1:
                lowerCal.set( Calendar.MONTH, Calendar.JANUARY );
                upperCal.set( Calendar.MONTH, Calendar.APRIL );
                break;
            case 2:
                lowerCal.set( Calendar.MONTH, Calendar.APRIL );
                upperCal.set( Calendar.MONTH, Calendar.JULY );
                break;
            case 3:
                lowerCal.set( Calendar.MONTH, Calendar.JULY );
                upperCal.set( Calendar.MONTH, Calendar.OCTOBER );
                break;
            case 4:
                lowerCal.set( Calendar.MONTH, Calendar.OCTOBER );
                upperCal.roll( Calendar.YEAR, true );
                upperCal.set( Calendar.MONTH, Calendar.JANUARY );
                break;
            default:
                throw new IllegalStateException( "Quartal " + quartal + " unbekannt!" );
        }

        FlurstueckComposite flurstueckTemplate = QueryExpressions.templateFor( FlurstueckComposite.class );
        VertragComposite vertragTemplate = QueryExpressions.templateFor( VertragComposite.class );

        BooleanExpression dExpr = null;
        BooleanExpression vExpr = null;
        {
            Query<VertragComposite> vertraege = KapsRepository.instance().findEntities(
                    VertragComposite.class,
                    QueryExpressions.and( QueryExpressions.ge( vertragTemplate.vertragsDatum(), lowerCal.getTime() ),
                            QueryExpressions.lt( vertragTemplate.vertragsDatum(), upperCal.getTime() ) ), 0, -1 );

            for (VertragComposite kv : vertraege) {
                BooleanExpression newExpr = QueryExpressions.eq( flurstueckTemplate.vertrag(), kv );
                if (vExpr == null) {
                    vExpr = newExpr;
                }
                else {
                    vExpr = QueryExpressions.or( vExpr, newExpr );
                }
            }

        }
        // falls keine Verträge gefunden werden, würden später alle Flurstücke
        // selektiert -> StackOverFlow
        if (vExpr != null) {
            BooleanExpression nExpr = null;
            {
                NutzungComposite nutzungTemplate = QueryExpressions.templateFor( NutzungComposite.class );
                Query<NutzungComposite> nutzungen = KapsRepository.instance().findEntities( NutzungComposite.class,
                        QueryExpressions.eq( nutzungTemplate.isAgrar(), Boolean.FALSE ), 0, -1 );
                for (NutzungComposite nutzung : nutzungen) {
                    BooleanExpression newExpr = QueryExpressions.eq( flurstueckTemplate.nutzung(), nutzung );
                    if (nExpr == null) {
                        nExpr = newExpr;
                    }
                    else {
                        nExpr = QueryExpressions.or( nExpr, newExpr );
                    }
                }
            }

            GemeindeComposite gemeinde = (GemeindeComposite)site.getFieldValue( "gemeinde" );
            BooleanExpression gExpr = null;
            if (gemeinde != null) {
                GemarkungComposite gemarkungTemplate = QueryExpressions.templateFor( GemarkungComposite.class );
                Query<GemarkungComposite> gemarkungen = KapsRepository.instance().findEntities(
                        GemarkungComposite.class, QueryExpressions.eq( gemarkungTemplate.gemeinde(), gemeinde ), 0, -1 );
                for (GemarkungComposite gemarkung : gemarkungen) {
                    BooleanExpression newExpr = QueryExpressions.eq( flurstueckTemplate.gemarkung(), gemarkung );
                    if (gExpr == null) {
                        gExpr = newExpr;
                    }
                    else {
                        gExpr = QueryExpressions.or( gExpr, newExpr );
                    }
                }
            }

            BooleanExpression expr = vExpr;
            if (nExpr != null) {
                expr = expr == null ? nExpr : QueryExpressions.and( expr, nExpr );
            }
            if (gExpr != null) {
                expr = expr == null ? gExpr : QueryExpressions.and( expr, gExpr );
            }

            Query<FlurstueckComposite> allFlurstuecke = KapsRepository.instance().findEntities(
                    FlurstueckComposite.class, expr, 0, getMaxResults() );

            for (FlurstueckComposite flurstueck : allFlurstuecke) {
                BooleanExpression newExpr = QueryExpressions.eq( vertragTemplate.identity(), flurstueck.vertrag().get()
                        .identity().get() );
                if (dExpr == null) {
                    dExpr = newExpr;
                }
                else {
                    dExpr = QueryExpressions.or( dExpr, newExpr );
                }
            }
            // flurstücksfilter angegeben und keine flurstücke gefunden
            if (dExpr == null) {
                dExpr = QueryExpressions.eq( vertragTemplate.identity(), "nothing" );
            }
        }
        else {
            dExpr = QueryExpressions.eq( vertragTemplate.identity(), "nothing" );
        }

        BooleanExpression geeignetExpr = QueryExpressions.eq( vertragTemplate.fuerAuswertungGeeignet(), Boolean.TRUE );

        if (dExpr != null) {
            dExpr = QueryExpressions.and( dExpr, geeignetExpr );
        } else {
            dExpr = geeignetExpr;
        }

        return KapsRepository.instance().findEntities( VertragComposite.class, dExpr, 0, getMaxResults() );
    }
}
