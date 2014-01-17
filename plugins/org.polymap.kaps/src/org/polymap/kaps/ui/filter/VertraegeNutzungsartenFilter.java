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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;

import com.google.common.collect.Sets;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.dialogs.MessageDialog;

import org.polymap.core.model.Entity;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.data.entityfeature.AbstractEntityFilter;
import org.polymap.rhei.field.BetweenFormField;
import org.polymap.rhei.field.BetweenValidator;
import org.polymap.rhei.field.DateTimeFormField;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.field.StringFormField;
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
import org.polymap.kaps.ui.NotNullValidator;

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class VertraegeNutzungsartenFilter
        extends AbstractEntityFilter {

    private static Log log = LogFactory.getLog( VertraegeNutzungsartenFilter.class );


    public VertraegeNutzungsartenFilter( ILayer layer ) {
        super( VertraegeNutzungsartenFilter.class.getName(), layer, "mit versch. Nutzungsarten...", null, 10000,
                VertragComposite.class );
    }


    public boolean hasControl() {
        return true;
    }


    public Composite createControl( Composite parent, IFilterEditorSite site ) {
        Composite result = site.createStandardLayout( parent );

        Calendar cal = new GregorianCalendar();
        int thisYear = cal.get( Calendar.YEAR );
        List<String> jahre = new ArrayList<String>();
        for (int i = 0; i < 10; i++) {
            jahre.add( String.valueOf( thisYear - i ) );
        }

        site.addStandardLayout( site.newFormField( result, "Jahr", String.class, new PicklistFormField( jahre ),
                new NotNullValidator(), "Jahr" ) );

        return result;
    }


    protected Query<? extends Entity> createQuery( IFilterEditorSite site ) {

        BooleanExpression jExpr = null;
        VertragComposite template = QueryExpressions.templateFor( VertragComposite.class );
        Integer jahr = Integer.parseInt( (String)site.getFieldValue( "Jahr" ) );
        if (jahr != null) {
            Calendar lowerCal = new GregorianCalendar();
            lowerCal.set( Calendar.YEAR, jahr );
            lowerCal.set( Calendar.DAY_OF_MONTH, 1 );
            lowerCal.set( Calendar.HOUR_OF_DAY, 0 );
            lowerCal.set( Calendar.MINUTE, 0 );
            lowerCal.set( Calendar.SECOND, 0 );
            lowerCal.set( Calendar.MILLISECOND, 0 );

            Calendar upperCal = new GregorianCalendar();
            upperCal.setTime( lowerCal.getTime() );

            lowerCal.set( Calendar.MONTH, Calendar.JANUARY );
            upperCal.roll( Calendar.YEAR, true );
            upperCal.set( Calendar.MONTH, Calendar.JANUARY );

            jExpr = QueryExpressions.and(
                    QueryExpressions.ge( template.vertragsDatum(), lowerCal.getTime() ),
                    QueryExpressions.lt( template.vertragsDatum(), upperCal.getTime() ) );
        }
        Set<Integer> nummern = Sets.newHashSet();

        for (VertragComposite vertrag : KapsRepository.instance().findEntities( VertragComposite.class, jExpr, 0, -1 )) {
            NutzungComposite nutzung = null;
            for (FlurstueckComposite flurstueck : FlurstueckComposite.Mixin.forEntity( vertrag )) {
                if (nutzung == null) {
                    nutzung = flurstueck.nutzung().get();
                }
                else if (!nutzung.equals( flurstueck.nutzung().get() )) {
                    log.info( "Found difference in " + vertrag.eingangsNr().get() );
                    nummern.add( vertrag.eingangsNr().get() );
                    continue;
                }
            }
        }

        BooleanExpression fExpr = null;
        for (Integer nummer : nummern) {
            BooleanExpression newExpr = QueryExpressions.eq( template.eingangsNr(), nummer );
            if (fExpr == null) {
                fExpr = newExpr;
            }
            else {
                fExpr = QueryExpressions.or( fExpr, newExpr );
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
