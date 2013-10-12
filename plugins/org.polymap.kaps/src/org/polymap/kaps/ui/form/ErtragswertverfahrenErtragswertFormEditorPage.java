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

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.entity.Entity;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.form.FormEditor;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.KapsPlugin;
import org.polymap.kaps.model.data.ErmittlungModernisierungsgradComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.model.data.VertragsdatenBaulandComposite;
import org.polymap.kaps.model.data.VertragsdatenErweitertComposite;
import org.polymap.kaps.ui.ActionButton;
import org.polymap.kaps.ui.FieldCalculation;
import org.polymap.kaps.ui.FieldListener;
import org.polymap.kaps.ui.FieldMultiplication;
import org.polymap.kaps.ui.FieldSummation;
import org.polymap.kaps.ui.InterEditorListener;
import org.polymap.kaps.ui.InterEditorPropertyChangeEvent;
import org.polymap.kaps.ui.NumberFormatter;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class ErtragswertverfahrenErtragswertFormEditorPage
        extends ErtragswertverfahrenFormEditorPage {

    private static Log             log         = LogFactory
                                                       .getLog( ErtragswertverfahrenErtragswertFormEditorPage.class );

    private boolean                initialized = false;

    private FieldCalculation       bwanteilbetragListener;

    private FieldCalculation       bwanteilReinertragListener;

    private FieldListener          fieldListener;

    private FieldSummation         ertragsWert;

    private FieldCalculation       bwAbzglFreilegung;

    private FieldSummation         ewbazListener;

    private FieldCalculation       vervievaeltigerListener;

    private FieldCalculation       rndListener;

    private FieldMultiplication    ertragsWertBauListener;

    private ActionButton           baujahrBerechneAction;

    private NonFiringFieldListener gndbjListener;

    private final FormEditor       formEditor;

    private InterEditorListener    editorListener;

    private NonFiringFieldListener ertragsWertListener;


    // private IFormFieldListener gemeindeListener;

    public ErtragswertverfahrenErtragswertFormEditorPage( FormEditor formEditor, Feature feature,
            FeatureStore featureStore ) {
        super( ErtragswertverfahrenErtragswertFormEditorPage.class.getName(), "Ertragswert", feature, featureStore );
        this.formEditor = formEditor;

        EventManager.instance().subscribe(
                fieldListener = new FieldListener( vb.jahresBetriebskosten(), vb.jahresReinErtrag() ),
                new FieldListener.EventFilter( formEditor ) );

        EventManager.instance().subscribe(
                editorListener = new InterEditorListener( vb.bereinigtesBaujahr(), vb.bodenwertAnteil(),
                        vb.bereinigterKaufpreis() ) {

                    @Override
                    protected void onChangedValue( IFormEditorPageSite site, Entity entity, String fieldName,
                            Object value ) {
                        if (fieldName.equals( vb.bereinigtesBaujahr().qualifiedName().name() )) {
                            site.setFieldValue( fieldName, value != null ? NumberFormatter.getFormatter( 0, false )
                                    .format( value ) : null );
                        }
                        else if (fieldName.equals( vb.bodenwertAnteil().qualifiedName().name() )) {
                            site.setFieldValue( fieldName,
                                    value != null ? NumberFormatter.getFormatter( 2 ).format( value ) : null );
                        }
                        else if (fieldName.equals( vb.bereinigterKaufpreis().qualifiedName().name() )) {
                            site.setFieldValue( fieldName,
                                    value != null ? NumberFormatter.getFormatter( 2 ).format( value ) : null );
                        }
                    }
                }, new InterEditorListener.EventFilter( vb ) );
    }


    @Override
    public void dispose() {
        super.dispose();
        EventManager.instance().unsubscribe( editorListener );
        EventManager.instance().unsubscribe( fieldListener );
    }


    @Override
    public void afterDoLoad( IProgressMonitor monitor )
            throws Exception {
        // bereinigten Kaufpreis laden
        VertragComposite vertrag = vb.vertrag().get();
        VertragsdatenErweitertComposite ev = vertrag.erweiterteVertragsdaten().get();
        if (ev != null) {
            Double preis = ev.bereinigterVollpreis().get();
            pageSite.setFieldValue( vb.bereinigterKaufpreis().qualifiedName().name(), preis != null ? NumberFormatter
                    .getFormatter( 2 ).format( preis ) : null );
        }
        fieldListener.flush( pageSite );
        editorListener.flush( pageSite );
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
        createPreisField( vb.bereinigterKaufpreis(), three().top( lastLine, 20 ), client, false );

        lastLine = newLine;
        newLine = createLabel( client, "Bodenwertanteil", one().top( lastLine ), SWT.RIGHT );
        createPreisField( vb.bodenwertAnteil(), three().top( lastLine ), client, false );

        lastLine = newLine;
        newLine = createLabel( client, "Freilegungskosten", one().top( lastLine ), SWT.RIGHT );
        createPreisField( vb.freilegung(), three().top( lastLine ), client, true );

        lastLine = newLine;
        newLine = createLabel( client, "Liegenschaftszins", one().top( lastLine ), SWT.RIGHT );
        createPreisField( vb.bodenwertAnteilLiegenschaftsZins(), two().top( lastLine ), client, true );
        createPreisField( IFormFieldLabel.NO_LABEL, "Liegenschaftszins in % von (Bodenwertanteil - Freilegung)",
                vb.bodenwertAnteilLiegenschaftsZinsBetrag(), three().top( lastLine ), client, false );
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
        createPreisField( vb.anteilDerBaulichenAnlagenAmJahresreinertrag(), three().top( lastLine ), client, true );
        site.addFieldListener( bwanteilReinertragListener = new FieldCalculation( site, 2, vb
                .anteilDerBaulichenAnlagenAmJahresreinertrag(), vb.bodenwertAnteilLiegenschaftsZinsBetrag(), vb
                .jahresReinErtrag() ) {

            @Override
            protected Double calculate( ValueProvider values ) {
                Double bwant = values.get( vb.bodenwertAnteilLiegenschaftsZinsBetrag() );
                Double jahresReinErtrag = values.get( vb.jahresReinErtrag() );
                if (jahresReinErtrag != null && bwant != null) {
                    return jahresReinErtrag - bwant;
                }
                return jahresReinErtrag;
            }
        } );

        lastLine = newLine;
        newLine = createLabel( client, "Gesamtnutzungsdauer", one().top( lastLine, 30 ), SWT.RIGHT );
        createGrouplessField( vb.gesamtNutzungsDauer(), three().top( lastLine, 30 ), client, true );

        lastLine = newLine;
        newLine = createLabel( client, "Baujahr tatsächlich/bereinigt", one().top( lastLine ), SWT.RIGHT );
        createGrouplessField( IFormFieldLabel.NO_LABEL, "tatsächliches Baujahr", vb.tatsaechlichesBaujahr(),
                two().top( lastLine ), client, true );
        createGrouplessField( IFormFieldLabel.NO_LABEL, "bereinigtes Baujahr", vb.bereinigtesBaujahr(),
                three().top( lastLine ), client, true );

        site.addFieldListener( gndbjListener = new NonFiringFieldListener( vb.gesamtNutzungsDauer(), vb
                .tatsaechlichesBaujahr() ) );
        baujahrBerechneAction = new ActionButton( parent, new Action( "Berechnen" ) {

            @Override
            public void run() {
                if (gndbjListener.get( vb.gesamtNutzungsDauer() ) == null
                        || gndbjListener.get( vb.tatsaechlichesBaujahr() ) == null) {
                    MessageDialog.openError( PolymapWorkbench.getShellToParentOn(), "Fehlende Daten",
                            "Bitte geben Sie Gesamtnutzungsdauer und das tatsächliche Baujahr ein, bevor Sie diese Berechnung starten." );
                }
                else {
                    ErmittlungModernisierungsgradComposite ermittlung = ErmittlungModernisierungsgradComposite.Mixin
                            .forErtragswertverfahren( vb );
                    if (ermittlung == null) {
                        ermittlung = repository.newEntity( ErmittlungModernisierungsgradComposite.class, null );
                        ermittlung.vertrag().set( vb.vertrag().get() );
                        ermittlung.ertragswertVerfahren().set( vb );
                        ermittlung.alterObergrenzeZeile1().set( 40.0d );
                        ermittlung.alterObergrenzeZeile2().set( 20.0d );
                        ermittlung.alterObergrenzeZeile3().set( 20.0d );
                        ermittlung.alterObergrenzeZeile4().set( 15.0d );
                        ermittlung.alterObergrenzeZeile5().set( 30.0d );
                        ermittlung.alterObergrenzeZeile6().set( 15.0d );
                        ermittlung.alterObergrenzeZeile7().set( 15.0d );
                        ermittlung.alterObergrenzeZeile8().set( 30.0d );
                    }
                    // ermittlung.gesamtNutzungsDauer().set( gnd );
                    // ermittlung.tatsaechlichesBaujahr().set( baujahr );
                    FormEditor targetEditor = KapsPlugin.openEditor( fs, ErmittlungModernisierungsgradComposite.NAME,
                            ermittlung );
                    EventManager.instance().publish(
                            new InterEditorPropertyChangeEvent( formEditor, targetEditor, ermittlung, ermittlung
                                    .gesamtNutzungsDauer().qualifiedName().name(), ermittlung.gesamtNutzungsDauer()
                                    .get(), gndbjListener.get( vb.gesamtNutzungsDauer() ) ) );
                    EventManager.instance().publish(
                            new InterEditorPropertyChangeEvent( formEditor, targetEditor, ermittlung, ermittlung
                                    .tatsaechlichesBaujahr().qualifiedName().name(), ermittlung.tatsaechlichesBaujahr()
                                    .get(), gndbjListener.get( vb.tatsaechlichesBaujahr() ) ) );
                }
            }

        } );
        baujahrBerechneAction.setLayoutData( four().width( 40 ).top( lastLine ).height( 25 ).create() );
        baujahrBerechneAction.setEnabled( true );

        lastLine = newLine;
        newLine = createLabel( client, "Restnutzungsdauer", one().top( lastLine ), SWT.RIGHT );
        createGrouplessField( vb.restnutzungsDauer(), three().top( lastLine ), client, false );
        site.addFieldListener( rndListener = new FieldCalculation( site, 0, vb.restnutzungsDauer(), vb
                .bereinigtesBaujahr(), vb.gesamtNutzungsDauer() ) {

            @Override
            protected Double calculate( ValueProvider values ) {
                Double gnd = values.get( vb.gesamtNutzungsDauer() );
                Double baujahr = values.get( vb.bereinigtesBaujahr() );
                if (gnd != null && baujahr != null) {
                    final Calendar cal = new GregorianCalendar();
                    Double heute = new Integer( cal.get( Calendar.YEAR ) ).doubleValue();
                    return Math.max( 0.0d, gnd - (heute - baujahr) );
                }
                return null;
            }
        } );

        lastLine = newLine;
        newLine = createLabel( client, "Vervielfältiger", one().top( lastLine ), SWT.RIGHT );
        createNumberField( IFormFieldLabel.NO_LABEL, null, vb.vervielvaeltiger(), three().top( lastLine ), client,
                false, 4 );
        site.addFieldListener( vervievaeltigerListener = new FieldCalculation( site, 4, vb.vervielvaeltiger(), vb
                .restnutzungsDauer(), vb.bodenwertAnteilLiegenschaftsZins() ) {

            @Override
            protected Double calculate( ValueProvider values ) {
                Double LiZins = values.get( vb.bodenwertAnteilLiegenschaftsZins() );
                Double RND = values.get( vb.restnutzungsDauer() );
                if (LiZins != null && RND != null) {
                    return (Math.pow( 1 + LiZins / 100, RND ) - 1) / (Math.pow( 1 + LiZins / 100, RND ) * LiZins / 100);
                }
                return null;
            }
        } );

        lastLine = newLine;
        newLine = createLabel( client, "Ertragswert der baulichen Anlagen", one().top( lastLine, 30 ), SWT.RIGHT );
        createPreisField( vb.ertragswertDerBaulichenAnlagen(), three().top( lastLine, 30 ), client, false );
        site.addFieldListener( ertragsWertBauListener = new FieldMultiplication( site, 2, vb.vervielvaeltiger(), vb
                .anteilDerBaulichenAnlagenAmJahresreinertrag(), vb.ertragswertDerBaulichenAnlagen() ) );

        lastLine = newLine;
        newLine = createLabel( client, "sonstige wertbeeinflussende Umstände", one().top( lastLine ), SWT.RIGHT );
        createTextField( vb.wertbeeinflussendeUmstaendeText(), two().top( lastLine ), client );
        createPreisField( vb.wertbeeinflussendeUmstaende(), three().top( lastLine ), client, true );

        lastLine = newLine;
        newLine = createLabel( client, "Zwischensumme", one().top( lastLine ), SWT.RIGHT );
        createPreisField( vb.ertragswertDerBaulichenAnlagenZwischensumme(), three().top( lastLine ), client, false );
        site.addFieldListener( ewbazListener = new FieldSummation( site, 2, vb
                .ertragswertDerBaulichenAnlagenZwischensumme(), vb.ertragswertDerBaulichenAnlagen(), vb
                .wertbeeinflussendeUmstaende() ) );

        lastLine = newLine;
        newLine = createLabel( client, "Bodenwert abzüglich Freilegung", one().top( lastLine ), SWT.RIGHT );
        createPreisField( vb.bodenwertAbzglFreilegung(), three().top( lastLine ), client, false );
        site.addFieldListener( bwAbzglFreilegung = new FieldCalculation( site, 2, vb.bodenwertAbzglFreilegung(), vb
                .bodenwertAnteil(), vb.freilegung() ) {

            @Override
            protected Double calculate( ValueProvider values ) {
                Double bw = values.get( vb.bodenwertAnteil() );
                Double freilegung = values.get( vb.freilegung() );
                return bw != null ? (freilegung != null ? bw - freilegung : bw) : null;
            }
        } );

        lastLine = newLine;
        newLine = createLabel( client, "Ertragswert", one().top( lastLine, 30 ), SWT.RIGHT );
        createPreisField( vb.ertragswert(), three().top( lastLine, 30 ).bottom( 100 ), client, false );
        site.addFieldListener( ertragsWert = new FieldSummation( site, 2, vb.ertragswert(), vb
                .bodenwertAbzglFreilegung(), vb.ertragswertDerBaulichenAnlagenZwischensumme() ) );

        site.addFieldListener( ertragsWertListener = new NonFiringFieldListener( vb.ertragswert() ) );

        final VertragComposite vertrag = vb.vertrag().get();
        String label = vertrag == null ? "kein Vertrag" : "Übernehmen";
        ActionButton openErweiterteDaten = new ActionButton( parent, new Action( label ) {

            @Override
            public void run() {
                // Iterable<FlurstueckComposite> flurstuecke =
                // FlurstueckComposite.Mixin.forEntity( vertrag );
                // int count = 0;
                // for (FlurstueckComposite flurstueck : flurstuecke) {
                VertragsdatenBaulandComposite erweitert = VertragsdatenBaulandComposite.Mixin.forVertrag( vertrag );
                if (erweitert != null) {
                    Double newValue = ertragsWertListener.get( vb.ertragswert() );
                    if (newValue != null) { // && !newValue.equals(
                                            // erweitert.wertDerBaulichenAnlagen() ))
                                            // {
                                            // count++;
                        FormEditor editor = KapsPlugin.openEditor( fs, VertragsdatenBaulandComposite.NAME, erweitert );
                        editor.setActivePage( VertragsdatenBaulandBodenwertFormEditorPage.class.getName() );
                        EventManager.instance().publish(
                                new InterEditorPropertyChangeEvent( formEditor, editor, erweitert, erweitert
                                        .wertDerBaulichenAnlagen().qualifiedName().name(), erweitert
                                        .wertDerBaulichenAnlagen().get(), newValue ) );
                        EventManager.instance().publish(
                                new InterEditorPropertyChangeEvent( formEditor, editor, erweitert, erweitert
                                        .bewertungsMethode().qualifiedName().name(), erweitert.bewertungsMethode()
                                        .get(), "Ertragswert-normal" ) );
                    }
                    MessageDialog.openInformation(
                            PolymapWorkbench.getShellToParentOn(),
                            "Wert übernommen",
                            "Der Gesamtwert der baulichen Anlagen wurde in \"Wert der baulichen Anlagen\" im Reiter \"Boden- und Gebäudewert \" in "
                                    + VertragsdatenBaulandComposite.NAME
                                    + " übernommen. Die Formulare werden entsprechend angezeigt." );
                }
            }
        } );
        openErweiterteDaten.setToolTipText( vertrag == null ? "Kein Vertrag zugewiesen" : "In Vertrag "
                + EingangsNummerFormatter.format( vertrag.eingangsNr().get() ) + " übernehmen" );
        openErweiterteDaten.setLayoutData( four().height( 25 ).width( 40 ).top( lastLine, 30 ).create() );
        openErweiterteDaten.setEnabled( vertrag != null );
    }


    @Override
    protected SimpleFormData one() {
        return new SimpleFormData( SPACING ).left( 0 ).right( 45 );
    }


    @Override
    protected SimpleFormData two() {
        return new SimpleFormData( SPACING ).left( 45 ).right( 60 );
    }


    @Override
    protected SimpleFormData three() {
        return new SimpleFormData( SPACING ).left( 60 ).right( 75 );
    }


    @Override
    protected SimpleFormData four() {
        return new SimpleFormData( SPACING ).left( 75 ).right( 90 );
    }
}
