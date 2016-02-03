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
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.BooleanQuery;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.query.grammar.GreaterOrEqualPredicate;
import org.qi4j.api.query.grammar.LessOrEqualPredicate;

import com.google.common.collect.Sets;

import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.jface.dialogs.MessageDialog;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;
import org.polymap.rhei.field.BetweenFormField;
import org.polymap.rhei.field.BetweenValidator;
import org.polymap.rhei.field.DateTimeFormField;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.SelectlistFormField;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.filter.FilterEditor;
import org.polymap.rhei.filter.IFilterEditorSite;
import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.data.BodennutzungComposite;
import org.polymap.kaps.model.data.FlurstueckComposite;
import org.polymap.kaps.model.data.GemarkungComposite;
import org.polymap.kaps.model.data.GemeindeComposite;
import org.polymap.kaps.model.data.NutzungComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.model.data.VertragsdatenAgrarComposite;
import org.polymap.kaps.ui.MyNumberValidator;
import org.polymap.kaps.ui.NotNullValidator;

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class VertragsdatenAgrarAgrarFilter
        extends KapsEntityFilter<VertragsdatenAgrarComposite> {

    private static Log         log = LogFactory.getLog( VertragsdatenAgrarAgrarFilter.class );

    private IFormFieldListener gemeindeListener;


    public VertragsdatenAgrarAgrarFilter( ILayer layer ) {
        super( VertragsdatenAgrarAgrarFilter.class.getName(), layer, "nach Nutzung, Datum, Gemeinde...", null, 10000,
                VertragsdatenAgrarComposite.class );
    }


    @Override
    public boolean hasControl() {
        return true;
    }


    @Override
    public Composite createControl( Composite parent, final IFilterEditorSite site ) {
        Composite result = site.createStandardLayout( parent );

        site.addStandardLayout( site.newFormField( result, "datum", Date.class, new BetweenFormField(
                new DateTimeFormField(), new DateTimeFormField() ), new BetweenValidator( new NotNullValidator() ),
                "Vertragsdatum" ) );
        SelectlistFormField gemeinden = new SelectlistFormField( KapsRepository.instance().entitiesWithNames( GemeindeComposite.class ) );
        gemeinden.setIsMultiple( true );
        Composite formField = site.newFormField( result, "gemeinde", GemeindeComposite.class,
                gemeinden,
                null, "Gemeinde" ) ;
        site.addStandardLayout( formField );
        ((FormData)formField.getLayoutData()).height = 200;
        ((FormData)formField.getLayoutData()).width = 100;
        
        FilterEditor editor = (FilterEditor)site;
        final SelectlistFormField gemarkungen = new SelectlistFormField( new SelectlistFormField.ValueProvider() {

            @Override
            public SortedMap<String, Object> get() {
                SortedMap<String, Object> gemarkungen = new TreeMap<String, Object>();
                List<GemeindeComposite> gemeinden = (List<GemeindeComposite>)site.getFieldValue( "gemeinde" );
                if (gemeinden != null) {
                    for (GemeindeComposite gemeinde : gemeinden) {
                        for (GemarkungComposite gemarkung : GemarkungComposite.Mixin.forGemeinde( gemeinde )) {
                            gemarkungen.put( gemarkung.schl().get() + "  -  " + gemarkung.name().get(), gemarkung );
                        }
                    }
                }
                return gemarkungen;
            }
        } );
        gemarkungen.setIsMultiple( true );
        editor.addFieldListener( gemeindeListener = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if ("gemeinde".equals( ev.getFieldName() )) {
                    gemarkungen.reloadValues();
                }
            }
        } );

        formField = site.newFormField( result, "gemarkung", GemarkungComposite.class, gemarkungen, null,
                "Gemarkung" );
        site.addStandardLayout( formField );
        ((FormData)formField.getLayoutData()).height = 200;
        ((FormData)formField.getLayoutData()).width = 100;
        
        SelectlistFormField field = new SelectlistFormField( KapsRepository.instance().entitiesWithNames(
                NutzungComposite.class ) );
        field.setIsMultiple( true );
        formField = site.newFormField( result, "nutzung", NutzungComposite.class, field,
                new NotNullValidator(), "Nutzung" );
        site.addStandardLayout( formField );
        ((FormData)formField.getLayoutData()).height = 200;
        ((FormData)formField.getLayoutData()).width = 100;

        field = new SelectlistFormField( KapsRepository.instance().entitiesWithNames( BodennutzungComposite.class ) );
        field.setIsMultiple( true );
        formField = site
                .newFormField( result, "bodennutzung", BodennutzungComposite.class, field, null, "Bodennutzung" );
        site.addStandardLayout( formField );
        ((FormData)formField.getLayoutData()).height = 200;
        ((FormData)formField.getLayoutData()).width = 100;
        
        StringFormField from = new StringFormField();
        Composite fromField = site.newFormField( result, "nutzungsGroesseMin", Integer.class, 
        		from, new MyNumberValidator( Integer.class ), "Min Fläche m²" );
        site.addStandardLayout( fromField );

        StringFormField to = new StringFormField();
        Composite toField = site.newFormField( result, "nutzungsGroesseMax", Integer.class, 
        		to, new MyNumberValidator( Integer.class ), "Max Fläche m²" );
        site.addStandardLayout( toField );

        return result;
    }


    @Override
    protected Query<VertragsdatenAgrarComposite> createQuery( IFilterEditorSite site ) {

        List<NutzungComposite> nutzungen = (List<NutzungComposite>)site.getFieldValue( "nutzung" );
        List<BodennutzungComposite> bodennutzungen = (List<BodennutzungComposite>)site.getFieldValue( "bodennutzung" );
        List<GemeindeComposite> gemeinden = (List<GemeindeComposite>)site.getFieldValue( "gemeinde" );
        Integer nutzungsGroesseMin = (Integer) site.getFieldValue( "nutzungsGroesseMin" );
        Integer nutzungsGroesseMax = (Integer) site.getFieldValue( "nutzungsGroesseMax" );

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
        VertragsdatenAgrarComposite template = QueryExpressions.templateFor( VertragsdatenAgrarComposite.class );

        FlurstueckComposite flurTemplate = QueryExpressions.templateFor( FlurstueckComposite.class );

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

        // gemeinde
        BooleanExpression gExpr = null;
        List<GemarkungComposite> gemarkungen = (List<GemarkungComposite>)site.getFieldValue( "gemarkung" );
        if (gemarkungen != null) {
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
        else if (gemeinden != null) {
            GemarkungComposite gemarkungTemplate = QueryExpressions.templateFor( GemarkungComposite.class );
            BooleanExpression gSubExpr = null;
            for (GemeindeComposite gemeinde : gemeinden) {
                BooleanExpression newExpr = QueryExpressions.eq( gemarkungTemplate.gemeinde(), gemeinde );
                if (gSubExpr == null) {
                    gSubExpr = newExpr;
                }
                else {
                    gSubExpr = QueryExpressions.or( gSubExpr, newExpr );
                }
            }
            if (gSubExpr == null) {
                gSubExpr = QueryExpressions.eq( gemarkungTemplate.identity(), "unknown" );
            }
            Query<GemarkungComposite> subGemarkungen = KapsRepository.instance().findEntities(
                    GemarkungComposite.class, gSubExpr, 0, -1 );
            for (GemarkungComposite gemarkungg : subGemarkungen) {
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

        if (nExpr != null) {
            if (gExpr != null) {
                nExpr = QueryExpressions.and( nExpr, gExpr );
            }
        }
        else {
            nExpr = gExpr;
        }
        
        Set<VertragComposite> vertraegeNachDatumUndFlurstueck = new HashSet<VertragComposite>();

        if (nExpr != null) {
            Query<FlurstueckComposite> flurstuecke = KapsRepository.instance().findEntities( FlurstueckComposite.class,
                    nExpr, 0, -1 );

            for (FlurstueckComposite fc : flurstuecke) {
                // mehrere Flurstücke können einem Vertrag angehören
                VertragComposite vertrag = fc.vertrag().get();
                if (vertrag != null) {
                    if ((vertraegeNachDatum == null || vertraegeNachDatum.contains( vertrag ))) {
                        vertraegeNachDatumUndFlurstueck.add( vertrag );
                    }
                }
            }
        }
        if (vertraegeNachDatumUndFlurstueck.size() > 5000) {
            Polymap.getSessionDisplay().asyncExec( new Runnable() {

                public void run() {
                    MessageDialog.openError( PolymapWorkbench.getShellToParentOn(), "Zu viele Ergebnisse",
                            "Es wurden über 5000 Ergebnisse gefunden. Bitte schränken Sie die Suche weiter ein." );
                }
            } );
            return KapsRepository.instance().findEntities( VertragsdatenAgrarComposite.class,
                    QueryExpressions.eq( template.identity(), "unknown" ), 0, -1 );
        }
        for (VertragComposite vertrag : vertraegeNachDatumUndFlurstueck) {
            BooleanExpression newExpr = QueryExpressions.eq( template.vertrag(), vertrag );
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
        BooleanExpression bExpr = null;
        if (bodennutzungen != null) {
            for (BodennutzungComposite nutzung : bodennutzungen) {
                BooleanExpression newExpr = QueryExpressions.or(
                        QueryExpressions.eq( template.bodennutzung1(), nutzung ),
                        QueryExpressions.eq( template.bodennutzung2(), nutzung ),
                        QueryExpressions.eq( template.bodennutzung3(), nutzung ),
                        QueryExpressions.eq( template.bodennutzung4(), nutzung ),
                        QueryExpressions.eq( template.bodennutzung5(), nutzung ),
                        QueryExpressions.eq( template.bodennutzung6(), nutzung ) );
                if (bExpr == null) {
                    bExpr = newExpr;
                }
                else {
                    bExpr = QueryExpressions.or( bExpr, newExpr );
                }
            }
        }
        if (bExpr != null) {
            fExpr = QueryExpressions.and( bExpr, fExpr );
        }
        
        BooleanExpression bodenExp1 = getBodennutzungExpression(template.bodennutzung1(), bodennutzungen);
        BooleanExpression bodenExp2 = getBodennutzungExpression(template.bodennutzung2(), bodennutzungen);
        BooleanExpression bodenExp3 = getBodennutzungExpression(template.bodennutzung3(), bodennutzungen);
        BooleanExpression bodenExp4 = getBodennutzungExpression(template.bodennutzung4(), bodennutzungen);
        BooleanExpression bodenExp5 = getBodennutzungExpression(template.bodennutzung5(), bodennutzungen);
        BooleanExpression bodenExp6 = getBodennutzungExpression(template.bodennutzung6(), bodennutzungen);

        BooleanExpression greaterEquals = null;
        if(nutzungsGroesseMin != null) {
        	BooleanExpression nutzungExp1 = QueryExpressions.ge(template.flaechenAnteil1(), Double.valueOf(nutzungsGroesseMin));
        	BooleanExpression nutzungExp2 = QueryExpressions.ge(template.flaechenAnteil2(), Double.valueOf(nutzungsGroesseMin));
        	BooleanExpression nutzungExp3 = QueryExpressions.ge(template.flaechenAnteil3(), Double.valueOf(nutzungsGroesseMin));
        	BooleanExpression nutzungExp4 = QueryExpressions.ge(template.flaechenAnteil4(), Double.valueOf(nutzungsGroesseMin));
        	BooleanExpression nutzungExp5 = QueryExpressions.ge(template.flaechenAnteil5(), Double.valueOf(nutzungsGroesseMin));
        	BooleanExpression nutzungExp6 = QueryExpressions.ge(template.flaechenAnteil6(), Double.valueOf(nutzungsGroesseMin));
        	
        	greaterEquals = QueryExpressions.or(
        		(bodenExp1 != null ? QueryExpressions.and(nutzungExp1, bodenExp1) : nutzungExp1),
       			(bodenExp2 != null ? QueryExpressions.and(nutzungExp2, bodenExp2) : nutzungExp2),
       			(bodenExp3 != null ? QueryExpressions.and(nutzungExp3, bodenExp3) : nutzungExp3),
       			(bodenExp4 != null ? QueryExpressions.and(nutzungExp4, bodenExp4) : nutzungExp4),
       			(bodenExp5 != null ? QueryExpressions.and(nutzungExp5, bodenExp5) : nutzungExp5),
       			(bodenExp6 != null ? QueryExpressions.and(nutzungExp6, bodenExp6) : nutzungExp6)
        	);
        }
        
        BooleanExpression flaecheExpression = null;
        BooleanExpression lowerEquals = null;
        if(nutzungsGroesseMax != null) {
        	BooleanExpression nutzungExp1 = QueryExpressions.le(template.flaechenAnteil1(), Double.valueOf(nutzungsGroesseMax));
        	BooleanExpression nutzungExp2 = QueryExpressions.le(template.flaechenAnteil2(), Double.valueOf(nutzungsGroesseMax));
        	BooleanExpression nutzungExp3 = QueryExpressions.le(template.flaechenAnteil3(), Double.valueOf(nutzungsGroesseMax));
        	BooleanExpression nutzungExp4 = QueryExpressions.le(template.flaechenAnteil4(), Double.valueOf(nutzungsGroesseMax));
        	BooleanExpression nutzungExp5 = QueryExpressions.le(template.flaechenAnteil5(), Double.valueOf(nutzungsGroesseMax));
        	BooleanExpression nutzungExp6 = QueryExpressions.le(template.flaechenAnteil6(), Double.valueOf(nutzungsGroesseMax));
        	
        	lowerEquals = QueryExpressions.or(
       			(bodenExp1 != null ? QueryExpressions.and(nutzungExp1, bodenExp1) : nutzungExp1),
       			(bodenExp2 != null ? QueryExpressions.and(nutzungExp2, bodenExp2) : nutzungExp2),
       			(bodenExp3 != null ? QueryExpressions.and(nutzungExp3, bodenExp3) : nutzungExp3),
       			(bodenExp4 != null ? QueryExpressions.and(nutzungExp4, bodenExp4) : nutzungExp4),
       			(bodenExp5 != null ? QueryExpressions.and(nutzungExp5, bodenExp5) : nutzungExp5),
       			(bodenExp6 != null ? QueryExpressions.and(nutzungExp6, bodenExp6) : nutzungExp6)
        	);
        }
        if(greaterEquals != null) {
        	if(lowerEquals != null) {
        		flaecheExpression = QueryExpressions.and(greaterEquals, lowerEquals);
        	} else {
        		flaecheExpression = greaterEquals;
        	}
        } else {
    		flaecheExpression = lowerEquals;
        }
        
        if (flaecheExpression != null) {
            fExpr = QueryExpressions.and( flaecheExpression, fExpr );
        }
        
        int oldMax = BooleanQuery.getMaxClauseCount();
        try {
        	BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
            return KapsRepository.instance().findEntities( VertragsdatenAgrarComposite.class, fExpr, 0, getMaxResults() );
        } finally {
        	BooleanQuery.setMaxClauseCount(oldMax);
        }
    }


	private BooleanExpression getBodennutzungExpression(
			Association<BodennutzungComposite> bodennutzung,
			List<BodennutzungComposite> bodennutzungen) {
		BooleanExpression newExpr = null;
		if(bodennutzungen != null) {
			for (BodennutzungComposite nutzung : bodennutzungen) {
				newExpr = QueryExpressions.eq( bodennutzung, nutzung );
			}
		}
		return newExpr;
	}
}
