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

import java.util.HashMap;
import java.util.Map;

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;

import org.qi4j.api.property.Property;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.ui.forms.widgets.Section;

import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.CheckboxFormField;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.form.FormEditor;
import org.polymap.rhei.form.IFormEditorPage;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.KapsPlugin;
import org.polymap.kaps.model.data.AusstattungBewertungComposite;
import org.polymap.kaps.model.data.WohnungComposite;
import org.polymap.kaps.ui.ActionButton;
import org.polymap.kaps.ui.FieldSummation;
import org.polymap.kaps.ui.InterEditorPropertyChangeEvent;
import org.polymap.kaps.ui.KapsDefaultFormEditorPage;
import org.polymap.kaps.ui.NumberFormatter;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class BewertungAnhandVonAustattungsmerkmalenFormEditorPage
        extends KapsDefaultFormEditorPage
        implements IFormEditorPage {

    public class BooleanListener
            implements IFormFieldListener {

        private final Map<String, Boolean> values = new HashMap<String, Boolean>();


        public void put( String fieldName, Boolean value ) {
            values.put( fieldName, value );
        }

        private final Map<String, Integer> terms;

        private IFormEditorPageSite        site;

        private Property<Double>           result;

        private Integer                    maxValue;


        public BooleanListener( IFormEditorPageSite site, Integer maxValue, Property<Double> result,
                Map<Property<Boolean>, Integer> operators ) {
            this.site = site;
            this.result = result;
            this.maxValue = maxValue;

            terms = new HashMap<String, Integer>();

            for (Property<Boolean> term : operators.keySet()) {
                terms.put( term.qualifiedName().name(), operators.get( term ) );
                put( term.qualifiedName().name(), term.get() );
            }
        }


        @Override
        public void fieldChange( FormFieldEvent ev ) {
            if (ev.getEventCode() != IFormFieldListener.VALUE_CHANGE) {
                return;
            }
            String fieldName = ev.getFieldName();
            if (terms.keySet().contains( fieldName )) {
                put( fieldName, (Boolean)ev.getNewValue() );
                //
                refreshResult();
            }
        }


        public final void refreshResult() {
            Double result = 0.0d;
            for (String fieldName : values.keySet()) {
                Boolean isSet = values.get( fieldName );

                if (isSet != null && isSet.booleanValue()) {
                    result += terms.get( fieldName );
                }
            }
            site.setFieldValue( this.result.qualifiedName().name(), NumberFormatter.getFormatter(0)
                    .format( Math.min( maxValue, result ) ) );
        }
    }

    protected AusstattungBewertungComposite vb;

    private BooleanListener                 listener1;

    private FormEditor                      formEditor;

    private BooleanListener                 listener2;

    private BooleanListener                 listener4;

    private BooleanListener                 listener5;

    private BooleanListener                 listener3;

    private BooleanListener                 listener7;

    private BooleanListener                 listener6;

    private FieldSummation                  summeListener;

    private NonFiringFieldListener          resultListener;


    public BewertungAnhandVonAustattungsmerkmalenFormEditorPage( FormEditor editor, Feature feature,
            FeatureStore featureStore ) {
        super( BewertungAnhandVonAustattungsmerkmalenFormEditorPage.class.getName(), "Bewertung", feature, featureStore );
        this.formEditor = editor;

        vb = repository.findEntity( AusstattungBewertungComposite.class, feature.getIdentifier().getID() );

        if (vb.wohnung().get() == null) {
            throw new IllegalStateException(
                    "Bitte nutzen Sie ausschließlich den Knopf 'Bewerten' bei Wohnung, um eine solche Bewertung zu erstellen." );
        }
    }


    @Override
    public void createFormContent( IFormEditorPageSite site ) {
        super.createFormContent( site );

        final WohnungComposite wohnung = vb.wohnung().get();
        site.setEditorTitle( formattedTitle( "Bewertung für Wohnung", wohnung != null ? wohnung.schl().get() : null,
                null ) );
        site.setFormTitle( formattedTitle( "Bewertung anhang von Ausstattungsmerkmalen für Wohnung",
                wohnung != null ? wohnung.schl().get() : null, getTitle() ) );

        Control newLine = null;
        Control lastLine = null;
        Composite parent = pageSite.getPageBody();

        lastLine = createFlaecheField( "Gesamtsumme", vb.gesamtSumme(), one().right( 25 ), parent, false );
        site.addFieldListener( summeListener = new FieldSummation( site, 0, vb.gesamtSumme(), vb.ME1P(), vb.ME2P(), vb
                .ME3P(), vb.ME4P(), vb.ME5P(), vb.ME6P(), vb.ME7P() ) );
        site.addFieldListener( resultListener = new NonFiringFieldListener( vb.gesamtSumme() ) );

        String label = wohnung == null ? "keine Wohnung" : "Übernehmen";
        ActionButton openErweiterteDaten = new ActionButton( parent, new Action( label ) {

            @Override
            public void run() {
                if (wohnung != null) {
                    Double newValue = resultListener.get( vb.gesamtSumme() );
                    if (newValue != null) {
                        FormEditor editor = KapsPlugin.openEditor( fs, WohnungComposite.NAME, wohnung );
                        editor.setActivePage( VertragsdatenBaulandBodenwertFormEditorPage.class.getName() );
                        EventManager.instance().publish(
                                new InterEditorPropertyChangeEvent( formEditor, editor, wohnung, wohnung
                                        .bewertungsPunkte().qualifiedName().name(), wohnung.bewertungsPunkte().get(),
                                        newValue ) );
                        // EventManager.instance().publish(
                        // new InterEditorPropertyChangeEvent( formEditor, editor,
                        // wohnung, wohnung
                        // .ausstattung().qualifiedName().name(),
                        // wohnung.ausstattung()
                        // .get(), AusstattungComposite.Mixin.forWert(newValue)) );
                    }
                    MessageDialog.openInformation( PolymapWorkbench.getShellToParentOn(), "Wert übernommen",
                            "Der Wert der Ausstattung wurde in den Reiter \"Grunddaten \" in " + WohnungComposite.NAME
                                    + " übernommen." );
                }

            }
        } );
        openErweiterteDaten.setToolTipText( wohnung == null ? "Keine Wohnung zugewiesen" : "In Wohnung "
                + wohnung.schl().get() + " übernehmen" );
        openErweiterteDaten.setLayoutData( one().left( lastLine ).height( 25 ).right(50).create() );
        openErweiterteDaten.setEnabled( wohnung != null );

        Section section1 = newSection( parent, "Heizung (maximal 12 Punkte)" );
        section1.setLayoutData( new SimpleFormData( SECTION_SPACING ).left( 0 ).right( 50 ).top( openErweiterteDaten )
                .create() );
        Composite client1 = (Composite)section1.getClient();

        Map<Property<Boolean>, Integer> values1 = new HashMap<Property<Boolean>, Integer>();
        newLine = createCheckboxField( "Zentralheizung", values1, 10, vb.ME11(), client1, newLine );
        newLine = createCheckboxField( "Eletrospeicheröfen/Einzelofen", values1, 2, vb.ME12(), client1, newLine );
        newLine = createCheckboxField( "Kachelofen mit automatische Befeuerung", values1, 4, vb.ME13(), client1,
                newLine );
        newLine = createCheckboxField( "Kachelofen mit manueller Befeuerung", values1, 2, vb.ME14(), client1, newLine );
        newLine = createCheckboxField( "Fußbodenheizung", values1, 4, vb.ME15(), client1, newLine );

        lastLine = newLine;
        newLine = createLabel( client1, "Summe", one().top( lastLine ), SWT.RIGHT );
        createFlaecheField( vb.ME1P(), two().bottom( 100 ).top( lastLine ), client1, false );
        site.addFieldListener( listener1 = new BooleanListener( site, 12, vb.ME1P(), values1 ) );

        // Section 2
        Section section2 = newSection( parent, "WC - separat (maximal 6 Punkte)" );
        section2.setLayoutData( new SimpleFormData( SECTION_SPACING ).left( 0 ).right( 50 ).top( section1 ).create() );
        Composite client2 = (Composite)section2.getClient();

        Map<Property<Boolean>, Integer> values2 = new HashMap<Property<Boolean>, Integer>();
        newLine = createCheckboxField( "innerhalb der Wohnung", values2, 1, vb.ME21(), client2, newLine );
        newLine = createCheckboxField( "Wände deckenhoch gefliest", values2, 2, vb.ME22(), client2, newLine );
        newLine = createCheckboxField( "1 Waschbecken", values2, 2, vb.ME23(), client2, newLine );
        newLine = createCheckboxField( "2 Waschbecken", values2, 2, vb.ME24(), client2, newLine );

        lastLine = newLine;
        newLine = createLabel( client2, "Summe", one().top( lastLine ), SWT.RIGHT );
        createFlaecheField( vb.ME2P(), two().bottom( 100 ).top( lastLine ), client2, false );
        site.addFieldListener( listener2 = new BooleanListener( site, 6, vb.ME2P(), values2 ) );

        // Section 4
        Section section4 = newSection( parent, "Fenster (maximal 14 Punkte)" );
        section4.setLayoutData( new SimpleFormData( SECTION_SPACING ).left( 0 ).right( 50 ).top( section2 ).create() );
        Composite client4 = (Composite)section4.getClient();

        Map<Property<Boolean>, Integer> values4 = new HashMap<Property<Boolean>, Integer>();
        newLine = createCheckboxField( "Isolierverglaste Fenster", values4, 10, vb.ME45(), client4, newLine );
        newLine = createCheckboxField( "", values4, 8, vb.ME46(), client4, newLine );
        newLine = createCheckboxField( "Doppelverglaste Fenster", values4, 4, vb.ME44(), client4, newLine );
        newLine = createCheckboxField( "Rollläden manuell", values4, 2, vb.ME42(), client4, newLine );
        newLine = createCheckboxField( "Rollläden elektrisch", values4, 4, vb.ME43(), client4, newLine );
        newLine = createCheckboxField( "Klappladen oder Jalousetten", values4, 2, vb.ME41(), client4, newLine );

        lastLine = newLine;
        newLine = createLabel( client4, "Summe", one().top( lastLine ), SWT.RIGHT );
        createFlaecheField( vb.ME4P(), two().bottom( 100 ).top( lastLine ), client4, false );
        site.addFieldListener( listener4 = new BooleanListener( site, 14, vb.ME4P(), values4 ) );

        // Section 5
        Section section5 = newSection( parent, "Sonstiges (maximal 11 Punkte)" );
        section5.setLayoutData( new SimpleFormData( SECTION_SPACING ).left( 0 ).right( 50 ).top( section4 ).create() );
        Composite client5 = (Composite)section5.getClient();

        Map<Property<Boolean>, Integer> values5 = new HashMap<Property<Boolean>, Integer>();
        newLine = createCheckboxField( "Balkon-/Terrassentiefe größer als 2m", values5, 3, vb.ME51(), client5, newLine );
        newLine = createCheckboxField( "Keller größer als 6m²", values5, 1, vb.ME52(), client5, newLine );
        newLine = createCheckboxField( "separate Bühne größer als 6m²", values5, 2, vb.ME53(), client5, newLine );
        newLine = createCheckboxField( "zusätzlich 1 Garage oder Stellplatz", values5, 5, vb.ME54(), client5, newLine );

        lastLine = newLine;
        newLine = createLabel( client5, "Summe", one().top( lastLine ), SWT.RIGHT );
        createFlaecheField( vb.ME5P(), two().bottom( 100 ).top( lastLine ), client5, false );
        site.addFieldListener( listener5 = new BooleanListener( site, 11, vb.ME5P(), values5 ) );

        // Section3
        Section section3 = newSection( parent, "Badezimmer/Duschraum (maximal 21 Punkte)" );
        section3.setLayoutData( new SimpleFormData( SECTION_SPACING ).left( 50 ).right( 100 ).top( openErweiterteDaten )
                .create() );
        Composite client3 = (Composite)section3.getClient();

        Map<Property<Boolean>, Integer> values3 = new HashMap<Property<Boolean>, Integer>();
        newLine = createCheckboxField( "Innerhalb der Wohnung", values3, 3, vb.ME31(), client3, newLine );
        newLine = createCheckboxField( "WC im Bad", values3, 4, vb.ME32(), client3, newLine );
        newLine = createCheckboxField( "WC zusätzlich im Bad", values3, 6, vb.ME32A(), client3, newLine );
        newLine = createCheckboxField( "Wände deckenhoch gefliest", values3, 2, vb.ME33(), client3, newLine );
        newLine = createCheckboxField( "", values3, 3, vb.ME33A(), client3, newLine );
        newLine = createCheckboxField( "Badewanne freistehend", values3, 2, vb.ME34(), client3, newLine );
        newLine = createCheckboxField( "Badewanne eingebaut", values3, 4, vb.ME35(), client3, newLine );
        newLine = createCheckboxField( "Duschkabine", values3, 3, vb.ME36(), client3, newLine );
        newLine = createCheckboxField( "Waschbecken", values3, 1, vb.ME37(), client3, newLine );
        newLine = createCheckboxField( "2. Waschbecken", values3, 1, vb.ME372(), client3, newLine );
        newLine = createCheckboxField( "Duschkabine außerhalb des Bades", values3, 1, vb.ME38(), client3, newLine );
        newLine = createCheckboxField( "Fußboden gefliest", values3, 1, vb.ME39(), client3, newLine );

        lastLine = newLine;
        newLine = createLabel( client3, "Summe", one().top( lastLine ), SWT.RIGHT );
        createFlaecheField( vb.ME3P(), two().bottom( 100 ).top( lastLine ), client3, false );
        site.addFieldListener( listener3 = new BooleanListener( site, 21, vb.ME3P(), values3 ) );

        // Section 7
        Section section7 = newSection( parent, "Außenwärmedämmung (maximal 6 Punkte)" );
        section7.setLayoutData( new SimpleFormData( SECTION_SPACING ).left( 50 ).right( 100 ).top( section3 ).create() );
        Composite client7 = (Composite)section7.getClient();

        Map<Property<Boolean>, Integer> values7 = new HashMap<Property<Boolean>, Integer>();
        newLine = createCheckboxField( "", values7, 6, vb.ME71(), client7, newLine );
        newLine = createCheckboxField( "", values7, 4, vb.ME72(), client7, newLine );
        newLine = createCheckboxField( "", values7, 2, vb.ME73(), client7, newLine );

        lastLine = newLine;
        newLine = createLabel( client7, "Summe", one().top( lastLine ), SWT.RIGHT );
        createFlaecheField( vb.ME7P(), two().bottom( 100 ).top( lastLine ), client7, false );
        site.addFieldListener( listener7 = new BooleanListener( site, 6, vb.ME7P(), values7 ) );

        // Section 6
        Section section6 = newSection( parent, "Fußboden/Bodenbelag (maximal 6 Punkte)" );
        section6.setLayoutData( new SimpleFormData( SECTION_SPACING ).left( 50 ).right( 100 ).top( section7 ).create() );
        Composite client6 = (Composite)section6.getClient();

        Map<Property<Boolean>, Integer> values6 = new HashMap<Property<Boolean>, Integer>();
        newLine = createCheckboxField( "vorwiegend Laminat", values6, 2, vb.ME61(), client6, newLine );
        newLine = createCheckboxField( "vorwiegend Linoleum", values6, 4, vb.ME62(), client6, newLine );
        newLine = createCheckboxField( "vorwiegend Parkett", values6, 5, vb.ME63(), client6, newLine );
        newLine = createCheckboxField( "vorwiegend Fliesen", values6, 6, vb.ME64(), client6, newLine );

        lastLine = newLine;
        newLine = createLabel( client6, "Summe", one().top( lastLine ), SWT.RIGHT );
        createFlaecheField( vb.ME6P(), two().bottom( 100 ).top( lastLine ), client6, false );
        site.addFieldListener( listener6 = new BooleanListener( site, 6, vb.ME6P(), values6 ) );
    }


    private Control createCheckboxField( String label, Map<Property<Boolean>, Integer> values, Integer count,
            Property<Boolean> property, Composite client, Control top ) {
        values.put( property, count );
        label = label + " (" + count + ")";

        Control newLine = createLabel( client, label, one().top( top ), SWT.RIGHT );

        newFormField( IFormFieldLabel.NO_LABEL ).setProperty( new PropertyAdapter( property ) ).setToolTipText( label )
                .setField( new CheckboxFormField() ).setEnabled( true ).setLayoutData( two().top( top ).create() )
                .setParent( client ).create();

        return newLine;
    }


    @Override
    protected SimpleFormData one() {
        return new SimpleFormData( SPACING ).left( 0 ).right( 80 );
    }


    protected SimpleFormData two() {
        return new SimpleFormData( SPACING ).left( 80 ).right( 100 );
    }

}