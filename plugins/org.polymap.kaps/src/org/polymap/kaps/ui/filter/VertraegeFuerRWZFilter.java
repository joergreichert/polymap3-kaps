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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.workbench.PolymapWorkbench;
import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.data.FlurstueckComposite;
import org.polymap.kaps.model.data.RichtwertzoneComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.filter.IFilterEditorSite;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class VertraegeFuerRWZFilter
        extends KapsEntityFilter<VertragComposite> {

    private static Log log = LogFactory.getLog( VertraegeFuerRWZFilter.class );
	private Button schlSel;
	private Button nameSel;


    public VertraegeFuerRWZFilter( ILayer layer ) {
        super( VertraegeFuerRWZFilter.class.getName(), layer, "nach Richtwertzonen...", null, 10000,
                VertragComposite.class );
    }


    public boolean hasControl() {
        return true;
    }


    public Composite createControl( Composite parent, IFilterEditorSite site ) {
        Composite result = site.createStandardLayout( parent );

        site.addStandardLayout( site.newFormField( result, "rwz", String.class, new StringFormField(), null, "RWZ" ) );

        return result;
    }


    protected Query<VertragComposite> createQuery( IFilterEditorSite site ) {
    	
        String wert = (String) site.getFieldValue( "rwz" );
        
        VertragComposite template = QueryExpressions.templateFor( VertragComposite.class );
        BooleanExpression fExpr = null;

        if(wert == null || wert.length() == 0) {
            fExpr = QueryExpressions.eq( template.identity(), "unknown" );
        } else {
        	RichtwertzoneComposite rwz = null;
    		RichtwertzoneComposite rwzTemplate = QueryExpressions.templateFor(RichtwertzoneComposite.class);
    		BooleanExpression schlExpression = QueryExpressions.eq(rwzTemplate.schl(), wert);
    		BooleanExpression nameExpression = QueryExpressions.eq(rwzTemplate.name(), wert);
    		BooleanExpression searchExpression = QueryExpressions.or(schlExpression, nameExpression);
    		Query<RichtwertzoneComposite> rwzs = KapsRepository.instance().findEntities(RichtwertzoneComposite.class, searchExpression, 0, 1);
    		if(rwzs.count() > 1) {
	            Polymap.getSessionDisplay().asyncExec( new Runnable() {
	            	
	                public void run() {
	                	MessageDialog.openError( PolymapWorkbench.getShellToParentOn(), "Zu viele RWZ",
	                			"Es wurde mehr als eine RWZ gefunden, die den gegebenen Wert als Schlüssel oder Name verwendet." );
	                }
	            } );
	            return KapsRepository.instance().findEntities( VertragComposite.class,
	                    QueryExpressions.eq( template.identity(), "unknown" ), 0, -1 );
    		} else if(rwzs.count() == 1) {
    			rwz = rwzs.iterator().next();
    		}
        	if(rwz == null) {
	            return KapsRepository.instance().findEntities( VertragComposite.class,
	                    QueryExpressions.eq( template.identity(), "unknown" ), 0, -1 );
        	}
	
	        FlurstueckComposite fstTemplate = QueryExpressions.templateFor( FlurstueckComposite.class );
	        BooleanExpression rwzFstExpr = QueryExpressions.eq(fstTemplate.richtwertZone(), rwz);
	        Query<FlurstueckComposite> fsts = KapsRepository.instance().findEntities( FlurstueckComposite.class, rwzFstExpr, 0, getMaxResults() );
	        
	        if (fsts.count() > 5000) {
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
	        for (FlurstueckComposite fc : fsts) {
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

        return KapsRepository.instance().findEntities( VertragComposite.class, fExpr, 0, getMaxResults() );
    }
}
