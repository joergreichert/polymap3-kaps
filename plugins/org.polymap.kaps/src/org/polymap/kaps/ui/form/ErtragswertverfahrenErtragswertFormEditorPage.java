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
package org.polymap.kaps.ui.form;

import java.util.List;

import java.beans.PropertyChangeEvent;

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.model.data.ErtragswertverfahrenComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.model.data.VertragsdatenErweitertComposite;
import org.polymap.kaps.ui.FieldCalculation;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class ErtragswertverfahrenErtragswertFormEditorPage
        extends ErtragswertverfahrenFormEditorPage {

    private static Log       log         = LogFactory.getLog( ErtragswertverfahrenErtragswertFormEditorPage.class );

    private boolean          initialized = false;

    private Double           jahresReinErtrag;

    private FieldCalculation bwanteilbetragListener;

    private FieldCalculation bwanteilReinertragListener;


    // private IFormFieldListener gemeindeListener;

    public ErtragswertverfahrenErtragswertFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( ErtragswertverfahrenErtragswertFormEditorPage.class.getName(), "Ertragswert", feature, featureStore );

        jahresReinErtrag = vb.jahresReinErtrag().get();

        EventManager.instance().subscribe( this, new EventFilter<PropertyChangeEvent>() {

            public boolean apply( PropertyChangeEvent ev ) {
                Object source = ev.getSource();
                return source != null && source instanceof ErtragswertverfahrenComposite && source.equals( vb );
            }
        } );
    }


    @EventHandler(display = true, delay = 1)
    public void handleExternalGebaeudeSelection( List<PropertyChangeEvent> events )
            throws Exception {
        for (PropertyChangeEvent ev : events) {
            if (ev.getPropertyName().equals( vb.jahresReinErtrag().qualifiedName().name() )) {
                jahresReinErtrag = (Double)ev.getNewValue();
                if (bwanteilbetragListener != null) {
                    bwanteilbetragListener.refreshResult();
                }
                System.out.println( ev );
            }
        }
    }


    @Override
    public void dispose() {
        super.dispose();
        EventManager.instance().unsubscribe( this );
        // EventManager.instance().unsubscribe( fieldListener );
    }
    
    @Override
    public void afterDoLoad( IProgressMonitor monitor )
            throws Exception {
        // bereinigten Kaufpreis laden
        VertragComposite vertrag = vb.vertrag().get();
        VertragsdatenErweitertComposite ev = vertrag.erweiterteVertragsdaten().get();
        if (ev != null) {
            Double preis = ev.bereinigterVollpreis().get();
            pageSite.setFieldValue( vb.bereinigterKaufpreis().qualifiedName().name(), preis != null ? getFormatter( 2 )
                    .format( preis ) : null );
        }
        //
        //
        // pageSite.setFieldValue(
        // vb.anteiligeBetriebskosten().qualifiedName().name(),
        // anteiligeBetriebskosten != null ? getFormatter( 2 ).format(
        // anteiligeBetriebskosten ) : null );
        bwanteilbetragListener.refreshResult();
    }




    @SuppressWarnings("unchecked")
    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );

        initialized = true;

        Control newLine, lastLine = null;
        Composite parent = pageSite.getPageBody();

        // Section section = newSection( parent, "Bodenwertaufteilung" );
        // Composite client = (Composite)section.getClient();
        Composite client = parent;

        newLine = createLabel( client, "bereinigter Kaufpreis", one().top( lastLine, 20 ), SWT.RIGHT );
        createPreisField( vb.bereinigterKaufpreis(), two().top( lastLine, 20 ), client, false );

        lastLine = newLine;
        newLine = createLabel( client, "Bodenwertanteil", one().top( lastLine ), SWT.RIGHT );
        createPreisField( vb.bodenwertAnteil(), two().top( lastLine ), client, false );
        // TODO berechnen beim afterDoLoad

        lastLine = newLine;
        newLine = createLabel( client, "Freilegungskosten", one().top( lastLine ), SWT.RIGHT );
        createPreisField( vb.freilegung(), one().left( newLine ).top( lastLine ), client, true );

        lastLine = newLine;
        newLine = createLabel( client, "Liegenschaftszins", one().top( lastLine ), SWT.RIGHT );
        createPreisField( vb.bodenwertAnteilLiegenschaftsZins(), one().left( newLine ).top( lastLine ), client, true );
        createPreisField( IFormFieldLabel.NO_LABEL, "Liegenschaftszins in % von (Bodenwertanteil - Freilegung)",
                vb.bodenwertAnteilLiegenschaftsZinsBetrag(), two().top( lastLine ), client, false );
        site.addFieldListener( bwanteilbetragListener = new FieldCalculation( site, 2, vb
                .bodenwertAnteilLiegenschaftsZinsBetrag(), vb.bodenwertAnteilLiegenschaftsZins(), vb.freilegung(), vb
                .bodenwertAnteil() ) {

            @Override
            protected Double calculate( ValueProvider values ) {
                Double zins = values.get( vb.bodenwertAnteilLiegenschaftsZins() );
                Double anteil = values.get( vb.bodenwertAnteil() );
                Double freilegung = values.get( vb.freilegung() );
                Double result = null;
                if (zins != null && anteil != null) {
                    if (freilegung != null) {
                        anteil = anteil - freilegung;
                    }
                    result = anteil / 100 * zins;
                }
                return result;
            }
        } );

        lastLine = newLine;
        newLine = createLabel( client, "Anteil der baulichen Anlage am Reinertrag", one().top( lastLine ), SWT.RIGHT );
        createPreisField( vb.anteilDerBaulichenAnlagenAmJahresreinertrag(), two().top( lastLine ), client, true );
        site.addFieldListener( bwanteilReinertragListener = new FieldCalculation( site, 2, vb
                .anteilDerBaulichenAnlagenAmJahresreinertrag(), vb.bodenwertAnteilLiegenschaftsZinsBetrag() ) {

            @Override
            protected Double calculate( ValueProvider values ) {
                Double bwant = values.get( vb.bodenwertAnteilLiegenschaftsZinsBetrag() );
                Double result = null;
                if (jahresReinErtrag != null && bwant != null) {
                    result = jahresReinErtrag - bwant;
                }
                return result;
            }
        } );

        lastLine = newLine;
        newLine = createLabel( client, "Gesamtnutzungsdauer", one().top( lastLine, 30 ), SWT.RIGHT );
        createFlaecheField( vb.gesamtNutzungsDauer(), two().top( lastLine, 30 ), client, true );

        lastLine = newLine;
        newLine = createLabel( client, "tatsächliches Baujahr", one().top( lastLine ), SWT.RIGHT );
        createFlaecheField( vb.tatsaechlichesBaujahr(), two().top( lastLine ), client, true );

        lastLine = newLine;
        newLine = createLabel( client, "bereinigtes Baujahr", one().top( lastLine ), SWT.RIGHT );
        createFlaecheField( vb.bereinigtesBaujahr(), two().top( lastLine ), client, true );
        // TODO berechnen
        
        lastLine = newLine;
        newLine = createLabel( client, "Restnutzungsdauer", one().top( lastLine ), SWT.RIGHT );
        createFlaecheField( vb.restnutzungsDauer(), two().top( lastLine ), client, false );
        // TODO berechnen oder übernehmen
        
        lastLine = newLine;
        newLine = createLabel( client, "Vervielfältiger", one().top( lastLine ), SWT.RIGHT );
        createFlaecheField( vb.vervielvaeltiger(), two().top( lastLine ), client, false );
        // TODO berechnen
        
    }


    @Override
    protected SimpleFormData one() {
        return new SimpleFormData( SPACING ).left( 0 ).right( 45 );
    }


    @Override
    protected SimpleFormData two() {
        return new SimpleFormData( SPACING ).left( 45 ).right( 60 );
    }

}
