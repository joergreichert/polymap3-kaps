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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SortedMap;
import java.util.TreeMap;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;

import org.geotools.data.FeatureStore;
import org.h2.util.MathUtils;
import org.opengis.feature.Feature;
import org.opengis.feature.type.PropertyDescriptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.entity.Entity;
import org.qi4j.api.property.Property;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.ui.forms.widgets.Section;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.data.ui.featuretable.DefaultFeatureTableColumn;
import org.polymap.core.data.ui.featuretable.FeatureTableViewer;
import org.polymap.core.model.EntityType;
import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.qi4j.QiModule.EntityCreator;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.data.entityfeature.PropertyDescriptorAdapter;
import org.polymap.rhei.data.entityfeature.ReloadablePropertyAdapter;
import org.polymap.rhei.data.entityfeature.ReloadablePropertyAdapter.PropertyCallback;
import org.polymap.rhei.field.CheckboxFormField;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.form.FormEditor;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.KapsPlugin;
import org.polymap.kaps.MathUtil;
import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.NHK2010GebaeudeArtProvider;
import org.polymap.kaps.model.data.ErmittlungModernisierungsgradComposite;
import org.polymap.kaps.model.data.NHK2010BaupreisIndexComposite;
import org.polymap.kaps.model.data.NHK2010BaupreisIndexComposite.Values;
import org.polymap.kaps.model.data.NHK2010BewertungComposite;
import org.polymap.kaps.model.data.NHK2010BewertungGebaeudeComposite;
import org.polymap.kaps.model.data.NHK2010GebaeudeArtComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.model.data.VertragsdatenBaulandComposite;
import org.polymap.kaps.ui.ActionButton;
import org.polymap.kaps.ui.FieldSummation;
import org.polymap.kaps.ui.FieldCalculation;
import org.polymap.kaps.ui.InterEditorListener;
import org.polymap.kaps.ui.InterEditorPropertyChangeEvent;
import org.polymap.kaps.ui.KapsDefaultFormEditorPageWithFeatureTable;
import org.polymap.kaps.ui.MyNumberValidator;
import org.polymap.kaps.ui.NumberFormatter;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class NHK2010BewertungFormEditorPage
        extends KapsDefaultFormEditorPageWithFeatureTable<NHK2010BewertungGebaeudeComposite> {

    private static Log                  log         = LogFactory.getLog( NHK2010BewertungFormEditorPage.class );

    private final static String         prefix      = NHK2010BewertungFormEditorPage.class.getSimpleName();

    protected NHK2010BewertungComposite bewertung;

    private FieldCalculation            gesamtWert;

    private boolean                     formCreated = false;

    private NHK2010GebaeudeartSelector  gebaeudeArtAction;

    private Action                      gebaeudeStandardAction;

    private NHK2010GebaeudeArtComposite selectedGebaeudeArt;

    private IFormFieldListener          gebaeudeArtListener;

    private IFormFieldListener          gebaeudeStandardListener;

    private String                      selectedGebaeudeStandard;

    private IFormFieldListener          zfhListener;

    private IFormFieldListener          grundrissartListener;

    private IFormFieldListener          wohnungsgroesseListener;

    private IFormFieldListener          korrekturfaktorListener;

    private IFormFieldListener          normalHerstellungsWertListener;

    private IFormFieldListener          gebaeudeZeitWertListener;

    private IFormFieldListener          baukostenIndexWertListener;

    private IFormFieldListener          neuWertListener;

    private IFormFieldListener          gndListener;

    private ActionButton                baujahrBerechneAction;

    private IFormFieldListener          rndListener;

    private IFormFieldListener          altersWertMinderungListener;

    private IFormFieldListener          zeitwertRndListener;

    private IFormFieldListener          gesamtSumme;

    private IFormFieldListener          gndbjListener;

    protected Double                    baujahr;

    protected Double                    gnd;

    private InterEditorListener         fieldListener;

    private final FormEditor            formEditor;

    private IFormFieldListener          gebaeudeStandardGNDListener;

    private IFormFieldListener          aussenAnlagenListener;

    private boolean                     aussenanlageProzent;


    public NHK2010BewertungFormEditorPage( final FormEditor formEditor, Feature feature, FeatureStore featureStore ) {
        super( NHK2010BewertungGebaeudeComposite.class, NHK2010BewertungFormEditorPage.class.getName(), "NHK 2010",
                feature, featureStore );
        this.formEditor = formEditor;

        bewertung = repository.findEntity( NHK2010BewertungComposite.class, feature.getIdentifier().getID() );

        EventManager.instance().subscribe( fieldListener = new InterEditorListener() {

            @Override
            protected void onChangedValue( IFormEditorPageSite site, Entity entity, String fieldName, Object value ) {
                NHK2010BewertungGebaeudeComposite gebaeude = (NHK2010BewertungGebaeudeComposite)entity;
                if (selectedComposite.get() == null || !selectedComposite.get().equals( gebaeude )) {
                    selectedComposite.set( gebaeude );
                    try {
                        refreshReloadables();
                    }
                    catch (Exception e) {
                        throw new RuntimeException( "Fehler beim Setzen der neuen Parameter" );
                    }
                }
                // GND und Baujahr
                // check for gnd and baujahr, alle anderen Properties werden beim
                // Speichern gefeuert
                if (fieldName.equals( selectedComposite.get().gesamtNutzungsDauer().qualifiedName().name() )
                        || fieldName.equals( selectedComposite.get().bereinigtesBaujahr().qualifiedName().name() )) {
                    // System.out.println( ev );
                    pageSite.setFieldValue( prefix + fieldName, value != null ? getFormatter( 0, false ).format( value )
                            : null );
                }
            }
        }, new EventFilter<InterEditorPropertyChangeEvent>() {

            public boolean apply( InterEditorPropertyChangeEvent ev ) {
                Object source = ev.getEntity();
                if (source != null && source instanceof NHK2010BewertungGebaeudeComposite) {
                    NHK2010BewertungGebaeudeComposite gebaeude = (NHK2010BewertungGebaeudeComposite)source;
                    if (gebaeude.bewertung().get().equals( bewertung )) {
                        // gebaeude wurde extern geändert
                        return true;
                    }
                }
                return false;
            }
        } );
    }


    @Override
    public void dispose() {
        super.dispose();
        EventManager.instance().unsubscribe( fieldListener );
    }


    @Override
    public void afterDoLoad( IProgressMonitor monitor )
            throws Exception {
        fieldListener.flush( pageSite );
    }


    protected void refreshReloadables()
            throws Exception {
        NHK2010BewertungGebaeudeComposite composite = selectedComposite.get();
        selectedGebaeudeArt = NHK2010GebaeudeArtProvider.instance().gebaeudeForId(
                composite != null ? composite.gebaeudeArtId().get() : null );
        selectedGebaeudeStandard = composite != null ? composite.gebaeudeStandard().get() : null;

        super.refreshReloadables();

        // diese felder müssen disabled bleiben
        if (formCreated) {
            gebaeudeArtAction.setEnabled( composite != null );
            // TODO
            gebaeudeStandardAction.setEnabled( false );
            pageSite.setFieldEnabled( prefix + "laufendeNummer", false );
            pageSite.setFieldEnabled( prefix + "gebaeudeArtId", false );
            pageSite.setFieldEnabled( prefix + "nhk", false );
            pageSite.setFieldEnabled( prefix + "faktorZweifamilienhaus", false );
            pageSite.setFieldEnabled( prefix + "faktorWohnungsgroesse", false );
            pageSite.setFieldEnabled( prefix + "faktorGrundrissart", false );
            pageSite.setFieldEnabled( getPropertyName( nameTemplate.baukostenIndexWert() ), false );

            baujahrBerechneAction.setEnabled( composite != null );
            pageSite.setFieldEnabled( getPropertyName( nameTemplate.restNutzungsDauer() ), false );
            pageSite.setFieldEnabled( getPropertyName( nameTemplate.altersWertMinderung() ), false );
            pageSite.setFieldEnabled( getPropertyName( nameTemplate.zeitwertRnd() ), false );

            // if (composite != null) {
            // // NHK2010GebaeudeArtComposite art =
            // NHK2010GebaeudeArtProvider.instance().gebaeudeForId(
            // composite.gebaeudeArtId().get() );
            // // gebaeudeArtLabel.setText( art != null ? art.getQualifiedName() : ""
            // );
            //
            // // selectedGebaeudeArt = art;
            // } else {
            // selectedGebaeudeArt = null;
            // }
            // gebaeudeStandardField.setEnabled( selectedGebaeudeArt != null );
            postProcessGebaeudeArtSelection();
            if (composite != null) {
                // if (composite.zweifamilienHaus().get()) {
                pageSite.setFieldValue( prefix + "zweifamilienHaus", composite.zweifamilienHaus().get() );
                // called by listenerpageSite.setFieldValue( prefix +
                // "faktorZweifamilienhaus", null );
            }
            else {
                pageSite.setFieldValue( prefix + "zweifamilienHaus", Boolean.FALSE );
                pageSite.setFieldValue( prefix + "faktorZweifamilienhaus", null );
            }
        }
    }


    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );

        final VertragComposite vertragComposite = bewertung.vertrag().get();
        String nummer = vertragComposite != null ? EingangsNummerFormatter.format( vertragComposite.eingangsNr().get() )
                : null;
        site.setEditorTitle( formattedTitle( "NHK 2010", nummer, null ) );
        site.setFormTitle( formattedTitle( "Bewertung nach NHK 2010", nummer, getTitle() ) );

        Composite parent = site.getPageBody();
        String label = vertragComposite == null ? "Kein Vertrag zugewiesen" : "Vertrag " + nummer + " öffnen";
        ActionButton openVertrag = new ActionButton( parent, new Action( label ) {

            @Override
            public void run() {
                KapsPlugin.openEditor( fs, VertragComposite.NAME, vertragComposite );
            }
        } );
        openVertrag.setLayoutData( left().height( 25 ).create() );
        openVertrag.setEnabled( vertragComposite != null );

        Section tableSection = newSection( parent, "Auswahl Gebäude" );
        tableSection.setLayoutData( new SimpleFormData( SECTION_SPACING ).left( 0 ).right( 50 ).top( openVertrag )
                .create() );
        createTableForm( (Composite)tableSection.getClient(), parent, true );

        Section sumSection = newSection( parent, "Summen" );
        sumSection.setLayoutData( new SimpleFormData( SECTION_SPACING ).left( tableSection, 0 ).top( openVertrag )
                .right( 100 ).create() );
        createSumForm( site, sumSection );

        Section formSection = newSection( parent, "Gebäudedaten" );
        formSection.setLayoutData( new SimpleFormData( SECTION_SPACING ).left( 0 ).right( 100 ).top( sumSection )
                .create() );
        createGebaeudeForm( formSection );

        formCreated = true;
    }


    @SuppressWarnings("unchecked")
    private Composite createSumForm( final IFormEditorPageSite site, final Section section ) {
        Composite parent = (Composite)section.getClient();

        int col1 = 45;
        int col2 = 65;

        Control newLine, lastLine = null;
        newLine = createLabel( parent, "Zeitwerte", "Summe der Gebäudezeitwerte", left().right( col1 ).top( lastLine ),
                SWT.RIGHT );
        createPreisField( bewertung.summeZeitwerte(), left().left( col2 ).right( 100 ).top( lastLine ), parent, false );
        site.addFieldListener( gesamtSumme = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == VALUE_CHANGE) {
                    if (ev.getFieldName().equalsIgnoreCase( getPropertyName( nameTemplate.gebaeudeZeitWert() ) )) {
                        // zeitwert am aktuellen gebäude hat sich geändert
                        Double zeitWert = (Double)ev.getNewValue();
                        Double result = 0.0d;
                        for (NHK2010BewertungGebaeudeComposite gebaeude : getElements()) {
                            if (zeitWert != null
                                    && selectedComposite.get() != null
                                    && selectedComposite.get().laufendeNummer().get() == gebaeude.laufendeNummer()
                                            .get()) {
                                // gewähltes gebäude gefunden
                                // property nehmen
                                result += zeitWert;
                            }
                            else {
                                result += gebaeude.gebaeudeZeitWert().get() != null ? gebaeude.gebaeudeZeitWert().get()
                                        : 0.0d;
                            }
                        }
                        pageSite.setFieldValue( bewertung.summeZeitwerte().qualifiedName().name(),
                                result != null ? NumberFormatter.getFormatter( 2 ).format( result ) : null );
                    }
                }
            }
        } );

        lastLine = newLine;
        newLine = createLabel( parent, "Bauteile", "+/- nicht erfasste Bauteile", left().right( col1 ).top( lastLine ),
                SWT.RIGHT );
        createPreisField( bewertung.nichtErfassteBauteile(), left().left( col2 ).right( 100 ).top( lastLine ), parent,
                true );

        lastLine = newLine;
        newLine = createLabel( parent, "Außenanlagen", "Wert der Außenanlagen", left().right( col1 ).top( lastLine ),
                SWT.RIGHT );
        createPreisField( bewertung.wertDerAussenanlagen(), left().left( col2 ).right( 100 ).top( lastLine ), parent,
                true );

        lastLine = newLine;
        newLine = createLabel( parent, "Außenanlagen in %", "Wert der Außenanlagen in % vom Gebäudezeitwert", left()
                .right( col1 ).top( lastLine ), SWT.RIGHT );
        createBooleanField( bewertung.aussenAnlagenInProzent(), left().left( col1 ).right( col2 ).top( lastLine ),
                parent );
        createFlaecheField( bewertung.prozentwertDerAussenanlagen(), left().left( col2 ).right( 100 ).top( lastLine ),
                parent, true );
        site.addFieldListener( aussenAnlagenListener = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == IFormFieldListener.VALUE_CHANGE) {
                    if (ev.getFieldName().equals( bewertung.aussenAnlagenInProzent().qualifiedName().name() )) {
                        Boolean value = (Boolean)ev.getNewValue();
                        enableAussenanlageProzent( site, value );
                    }
                }
            }
        } );

        lastLine = newLine;
        newLine = createLabel( parent, "Gesamtwert", "Gesamtwert der baulichen und sonstigen Anlagen",
                left().right( col1 ).top( lastLine ), SWT.RIGHT );
        createPreisField( bewertung.gesamtWert(), left().left( col2 ).right( 100 ).top( lastLine ), parent, false );
        site.addFieldListener( gesamtWert = new FieldCalculation( site, 2, bewertung.gesamtWert(), bewertung
                .summeZeitwerte(), bewertung.nichtErfassteBauteile(), bewertung.wertDerAussenanlagen(), bewertung
                .prozentwertDerAussenanlagen() ) {

            @Override
            protected Double calculate( ValueProvider values ) {
                // summe über alles
                Double result = new Double( 0.0d );
                if (values.get( bewertung.summeZeitwerte() ) != null) {
                    result += values.get( bewertung.summeZeitwerte() );
                }
                if (values.get( bewertung.nichtErfassteBauteile() ) != null) {
                    result += values.get( bewertung.nichtErfassteBauteile() );
                }
                if (aussenanlageProzent) {
                    if (values.get( bewertung.prozentwertDerAussenanlagen() ) != null) {
                        Double prozent = values.get( bewertung.prozentwertDerAussenanlagen() );
                        result += result / 100 * prozent;
                    }                  
                }
                else {
                    if (values.get( bewertung.wertDerAussenanlagen() ) != null) {
                        result += values.get( bewertung.wertDerAussenanlagen() );
                    }
                }
                return result;
            }
        } );

        lastLine = newLine;
        final VertragComposite vertrag = bewertung.vertrag().get();
        String label = vertrag == null ? "Kein Vertrag zugewiesen" : "In Vertrag "
                + EingangsNummerFormatter.format( vertrag.eingangsNr().get() ) + " übernehmen";
        ActionButton openErweiterteDaten = new ActionButton( parent, new Action( label ) {

            @Override
            public void run() {
                VertragsdatenBaulandComposite erweitert = VertragsdatenBaulandComposite.Mixin.forVertrag( vertrag );
                if (erweitert != null) {
                    Double newValue = gesamtWert.getLastResultValue() == null ? bewertung.gesamtWert().get()
                            : gesamtWert.getLastResultValue();
                    if (newValue != null && !newValue.equals( erweitert.wertDerBaulichenAnlagen() )) {
                        FormEditor editor = KapsPlugin.openEditor( fs, VertragsdatenBaulandComposite.NAME, erweitert );
                        editor.setActivePage( VertragsdatenBaulandBodenwertFormEditorPage.class.getName() );
                        EventManager.instance().publish(
                                new InterEditorPropertyChangeEvent( formEditor, editor, erweitert, erweitert
                                        .wertDerBaulichenAnlagen().qualifiedName().name(), erweitert
                                        .wertDerBaulichenAnlagen().get(), newValue ) );
                        EventManager.instance().publish(
                                new InterEditorPropertyChangeEvent( formEditor, editor, erweitert, erweitert
                                        .bewertungsMethode().qualifiedName().name(), erweitert.bewertungsMethode()
                                        .get(), "NHK2010" ) );
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
        openErweiterteDaten.setLayoutData( left().height( 25 ).top( lastLine ).bottom( 100 ).create() );
        openErweiterteDaten.setEnabled( vertrag != null );
        newLine = openErweiterteDaten;

        enableAussenanlageProzent( site, bewertung.aussenAnlagenInProzent().get() );
        return section;
    }


    private void enableAussenanlageProzent( IFormEditorPageSite site, Boolean prozent ) {
        aussenanlageProzent = prozent == null ? false : prozent.booleanValue();
        site.setFieldEnabled( bewertung.prozentwertDerAussenanlagen().qualifiedName().name(), aussenanlageProzent );
        site.setFieldEnabled( bewertung.wertDerAussenanlagen().qualifiedName().name(), !aussenanlageProzent );
        gesamtWert.refreshResult();
    }


    private Control createGebaeudeForm( Section section ) {

        Composite parent = (Composite)section.getClient();
        Control newLine, lastLine = null;

        newLine = createLabel( parent, "Gebäudenummer", one().top( lastLine ) );
        newFormField( IFormFieldLabel.NO_LABEL )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2010BewertungGebaeudeComposite>( selectedComposite, prefix
                                + "laufendeNummer", new PropertyCallback<NHK2010BewertungGebaeudeComposite>() {

                            @Override
                            public Property<Integer> get( NHK2010BewertungGebaeudeComposite entity ) {
                                return entity.laufendeNummer();
                            }
                        } ) ).setField( reloadable( new StringFormField() ) )
                .setValidator( new MyNumberValidator( Integer.class, 2, 0 ) ).setEnabled( false )
                .setLayoutData( two().top( lastLine ).create() ).create();

        // gebäudeart mit selektor
        lastLine = newLine;
        newLine = createLabel( parent, "Gebäudeart", one().right( 10 ).top( lastLine ) );
        // gebäudeselectionaction
        gebaeudeArtAction = new NHK2010GebaeudeartSelector( pageSite.getToolkit() ) {

            protected void adopt( NHK2010GebaeudeArtComposite toAdopt )
                    throws Exception {
                assert toAdopt != null;
                pageSite.setFieldValue( prefix + "gebaeudeArtId", toAdopt.getId() );
            }
        };
        gebaeudeArtAction.setEnabled( false );
        ActionButton gebaeudeArtActionButton = new ActionButton( parent, gebaeudeArtAction );
        gebaeudeArtActionButton.setLayoutData( one().left( newLine, 0 ).top( lastLine ).height( 16 ).create() );
        gebaeudeArtActionButton.setEnabled( false );
        // pageSite.addFieldListener( gebaeudeArtAction );
        newFormField( IFormFieldLabel.NO_LABEL )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2010BewertungGebaeudeComposite>( selectedComposite, prefix
                                + "gebaeudeArtId", new PropertyCallback<NHK2010BewertungGebaeudeComposite>() {

                            @Override
                            public Property<String> get( NHK2010BewertungGebaeudeComposite entity ) {
                                return entity.gebaeudeArtId();
                            }
                        } ) ).setField( reloadable( new StringFormField() ) ).setEnabled( false )
                .setLayoutData( two().top( lastLine ).create() ).create();
        // lastLine = newLine;
        final Label gebaeudeArtLabel = (Label)createLabel( parent, "", three().right( 100 ).top( lastLine ), SWT.LEFT );
        lastLine = gebaeudeArtLabel;

        // gebäudestandard
        newLine = createLabel( parent, "Standard", one().right( 10 ).top( lastLine ) );
        // TODO
        gebaeudeStandardAction = new Action( "Ermitteln" ) {
        };
        // gebaeudeStandardAction = new NHK2010GebaeudeStandardSelector(
        // pageSite.getToolkit() ) {
        //
        // protected void adopt( Double gebaeudeStandard )
        // throws Exception {
        // assert gebaeudeStandard != null;
        // pageSite.setFieldValue( prefix + "gebaeudeArtId", toAdopt.getId() );
        // gebaeudeArtLabel.setText( toAdopt.getQualifiedName() );
        // }
        // };
        // gebaeudeStandardAction.setEnabled( false );
        ActionButton gebaeudeStandardActionButton = new ActionButton( parent, gebaeudeStandardAction );
        gebaeudeStandardActionButton.setLayoutData( one().left( newLine, 0 ).top( lastLine ).height( 25 ).create() );
        gebaeudeStandardActionButton.setEnabled( false );

        final PicklistFormField gebaeudeStandardPickList = new PicklistFormField(
                new PicklistFormField.ValueProvider() {

                    @Override
                    public SortedMap<String, Object> get() {
                        TreeMap<String, Object> zonen = new TreeMap<String, Object>();
                        if (selectedGebaeudeArt != null) {
                            if (selectedGebaeudeArt.getStufe1() != null) {
                                zonen.put( "1.0 Einfachst", "1" );
                                zonen.put( "1.5 Einfachst - Einfach", "1.5" );
                                zonen.put( "2.0 Einfach", "2" );
                                zonen.put( "2.5 Einfach - Mittel", "2.5" );
                            }
                            zonen.put( "3.0 Mittel", "3" );
                            zonen.put( "3.5 Mittel - Gehoben", "3.5" );
                            zonen.put( "4.0 Gehoben", "4" );
                            zonen.put( "4.5 Gehoben - Stark gehoben", "4.5" );
                            zonen.put( "5.0 Stark gehoben", "5" );
                        }
                        return zonen;
                    }
                } );

        newFormField( IFormFieldLabel.NO_LABEL )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2010BewertungGebaeudeComposite>( selectedComposite, prefix
                                + "gebaeudeStandard", new PropertyCallback<NHK2010BewertungGebaeudeComposite>() {

                            @Override
                            public Property<String> get( NHK2010BewertungGebaeudeComposite entity ) {
                                return entity.gebaeudeStandard();
                            }
                        } ) ).setField( reloadable( gebaeudeStandardPickList ) ).setEnabled( false )
                .setLayoutData( two().top( lastLine ).create() ).create();
        final Label gebaeudeBnkLabel = (Label)createLabel( parent, "", three().right( 100 ).top( lastLine ), SWT.LEFT );

        // Gebäudestandard - Zusatzdaten
        lastLine = newLine;
        // grundrissart
        newLine = createLabel( parent, "Grundrissart", one().top( lastLine ) );
        final PicklistFormField grundrissArtPickList = new PicklistFormField( new PicklistFormField.ValueProvider() {

            @Override
            public SortedMap<String, Object> get() {
                TreeMap<String, Object> zonen = new TreeMap<String, Object>();
                zonen.put( "1. Einspänner", "Einspänner" );
                zonen.put( "2. Zweispänner", "Zweispänner" );
                zonen.put( "3. Dreispänner", "Dreispänner" );
                zonen.put( "4. Vierspänner", "Vierspänner" );
                return zonen;
            }
        } );

        newFormField( IFormFieldLabel.NO_LABEL )
                .setToolTipText( "Grundrissart (nur bei Mehrfamilienhäusern)" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2010BewertungGebaeudeComposite>( selectedComposite, prefix
                                + "grundrissArt", new PropertyCallback<NHK2010BewertungGebaeudeComposite>() {

                            @Override
                            public Property get( NHK2010BewertungGebaeudeComposite entity ) {
                                return entity.grundrissArt();
                            }
                        } ) ).setField( reloadable( grundrissArtPickList ) ).setEnabled( false )
                .setLayoutData( two().top( lastLine ).create() ).create();

        newFormField( "Wohnungen" )
                .setToolTipText( "Anzahl Wohnungen (nur bei Mehrfamilienhäusern)" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2010BewertungGebaeudeComposite>( selectedComposite, prefix
                                + "anzahlWohnungen", new PropertyCallback<NHK2010BewertungGebaeudeComposite>() {

                            @Override
                            public Property get( NHK2010BewertungGebaeudeComposite entity ) {
                                return entity.anzahlWohnungen();
                            }
                        } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new MyNumberValidator( Double.class, 10, 0 ) ).setEnabled( false )
                .setLayoutData( three().top( lastLine ).create() ).create();

        newFormField( "Zweifamilienhaus" )
                .setToolTipText( "Zweifamilienhaus (nur bei Auswahl von Einfamilienhäusern)" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2010BewertungGebaeudeComposite>( selectedComposite, prefix
                                + "zweifamilienHaus", new PropertyCallback<NHK2010BewertungGebaeudeComposite>() {

                            @Override
                            public Property get( NHK2010BewertungGebaeudeComposite entity ) {
                                return entity.zweifamilienHaus();
                            }
                        } ) ).setField( reloadable( new CheckboxFormField() ) ).setEnabled( false )
                .setLayoutData( four().top( lastLine ).create() ).create();

        lastLine = newLine;

        pageSite.addFieldListener( gebaeudeArtListener = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == VALUE_CHANGE && ev.getFieldName().equalsIgnoreCase( prefix + "gebaeudeArtId" )) {
                    // if ((ev.getNewValue() == null && selectedGebaeudeArt != null)
                    // || (ev.getNewValue() != null && !ev.getNewValue().equals(
                    // selectedGebaeudeArt.getId() ))) {
                    // selectedGebaeudeArt = toAdopt;

                    selectedGebaeudeArt = NHK2010GebaeudeArtProvider.instance()
                            .gebaeudeForId( (String)ev.getNewValue() );
                    gebaeudeArtLabel.setText( selectedGebaeudeArt != null ? selectedGebaeudeArt.getQualifiedName() : "" );
                    gebaeudeBnkLabel.setText( selectedGebaeudeArt != null && selectedGebaeudeArt.getBnk() != null ? "inkl. Baunebenkosten von "
                            + selectedGebaeudeArt.getBnk() + " %"
                            : "" );

                    gebaeudeStandardPickList.reloadValues();
                    pageSite.setFieldValue( prefix + "gebaeudeStandard",
                            selectedComposite.get() != null ? selectedComposite.get().gebaeudeStandard().get() : "3" );
                    postProcessGebaeudeArtSelection();
                    // }
                }
            }
        } );

        // wird nicht gespeichert und ausgewertet, also vllt. als computed wenn
        // nachgefragt
        // newFormField( "Wohnungsgröße" )
        // .setToolTipText(
        // "Wohnungsgröße = BGF / Anzahl der Wohnungen bei Mehrfamilienhaus)" )
        // .setParent( parent )
        // .setProperty(
        // new ReloadablePropertyAdapter<NHK2010BewertungGebaeudeComposite>(
        // selectedComposite, prefix
        // + "zweifamilienHaus", new
        // PropertyCallback<NHK2010BewertungGebaeudeComposite>() {
        //
        // @Override
        // public Property get( NHK2010BewertungGebaeudeComposite entity ) {
        // return entity.zweifamilienHaus();
        // }
        // } ) ).setField( reloadable( new CheckboxFormField() ) ).setEnabled( false
        // )
        // .setLayoutData( five().top( lastLine ).create() ).create();

        lastLine = newLine;

        createLabel( parent, "Korrekturfaktoren", "Korrekturfaktoren entsprechend Gebäudeart und Gebäudedaten", one()
                .top( lastLine ), SWT.LEFT );

        newLine = newFormField( "Grundrissart" )
                .setToolTipText( "Korrekturfaktor Grundrissart" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2010BewertungGebaeudeComposite>( selectedComposite, prefix
                                + "faktorGrundrissart", new PropertyCallback<NHK2010BewertungGebaeudeComposite>() {

                            @Override
                            public Property<Double> get( NHK2010BewertungGebaeudeComposite entity ) {
                                return entity.faktorGrundrissart();
                            }
                        } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new MyNumberValidator( Double.class, 12, 2 ) ).setEnabled( false )
                .setLayoutData( two().top( lastLine ).create() ).create();

        pageSite.addFieldListener( grundrissartListener = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == VALUE_CHANGE && ev.getFieldName().equalsIgnoreCase( prefix + "grundrissArt" )) {
                    String value = ev.getNewValue();
                    String faktor = null;
                    if ("Einspänner".equals( value )) {
                        faktor = "1,05";
                    }
                    else if ("Zweispänner".equals( value )) {
                        faktor = "1,00";
                    }
                    else if ("Dreispänner".equals( value )) {
                        faktor = "0,97";
                    }
                    else if ("Vierspänner".equals( value )) {
                        faktor = "0,95";
                    }
                    pageSite.setFieldValue( prefix + "faktorGrundrissart", faktor );
                }
            }
        } );

        newFormField( "Wohnungsgröße" )
                .setToolTipText( "Korrekturfaktor Wohnungsgröße" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2010BewertungGebaeudeComposite>( selectedComposite, prefix
                                + "faktorWohnungsgroesse", new PropertyCallback<NHK2010BewertungGebaeudeComposite>() {

                            @Override
                            public Property<Double> get( NHK2010BewertungGebaeudeComposite entity ) {
                                return entity.faktorWohnungsgroesse();
                            }
                        } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new MyNumberValidator( Double.class, 12, 2 ) ).setEnabled( false )
                .setLayoutData( three().top( lastLine ).create() ).create();

        pageSite.addFieldListener( wohnungsgroesseListener = new IFormFieldListener() {

            private Double bruttoGrundFlaeche;

            private Double anzahlWohnungen;


            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == VALUE_CHANGE) {
                    if (ev.getFieldName().equalsIgnoreCase( prefix + "bruttoGrundFlaeche" )) {
                        this.bruttoGrundFlaeche = (Double)ev.getNewValue();
                        calculateFaktor();
                    }
                    else if (ev.getFieldName().equalsIgnoreCase( prefix + "anzahlWohnungen" )) {
                        this.anzahlWohnungen = (Double)ev.getNewValue();
                        calculateFaktor();
                    }
                }
            }


            private void calculateFaktor() {
                Double faktor = null;
                if (bruttoGrundFlaeche != null && anzahlWohnungen != null && anzahlWohnungen != 0.0d) {
                    Double wohnungsflaeche = MathUtil.round( bruttoGrundFlaeche / anzahlWohnungen );
                    if (wohnungsflaeche <= 35.0d) {
                        faktor = 1.1d;
                    }
                    else if (wohnungsflaeche >= 135.0d) {
                        faktor = 0.85d;
                    }
                    else if (wohnungsflaeche == 50.0d) {
                        faktor = 1.0d;
                    }
                    else if (wohnungsflaeche < 50.0d) {
                        faktor = 1.1d - (wohnungsflaeche - 35.0d) * (1.1d - 1.0d) / (50.0d - 35.0d);
                    }
                    else if (wohnungsflaeche < 135.0d) {
                        faktor = 1.0d - (wohnungsflaeche - 50.0d) * (1.0d - 0.85d) / (135.0d - 50.0d);
                    }
                }
                pageSite.setFieldValue( prefix + "faktorWohnungsgroesse", faktor != null ? NumberFormatter
                        .getFormatter( 2 ).format( faktor ) : null );
            }
        } );

        newLine = newFormField( "Zweifamilienhaus" )
                .setToolTipText( "Korrekturfaktor Zweifamilienhaus" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2010BewertungGebaeudeComposite>( selectedComposite, prefix
                                + "faktorZweifamilienhaus", new PropertyCallback<NHK2010BewertungGebaeudeComposite>() {

                            @Override
                            public Property<Double> get( NHK2010BewertungGebaeudeComposite entity ) {
                                // nur berechnet, gibts in WinAKPS nicht
                                if (entity.zweifamilienHaus().get() != null
                                        && entity.zweifamilienHaus().get().booleanValue()) {
                                    entity.faktorZweifamilienhaus().set( new Double( 1.05d ) );
                                }
                                return entity.faktorZweifamilienhaus();
                            }
                        } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new MyNumberValidator( Double.class, 12, 2 ) ).setEnabled( false )
                .setLayoutData( four().top( lastLine ).create() ).create();

        pageSite.addFieldListener( zfhListener = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == VALUE_CHANGE
                        && ev.getFieldName().equalsIgnoreCase( prefix + "zweifamilienHaus" )) {
                    Boolean value = ev.getNewValue();
                    if (value != null && value.booleanValue()) {
                        pageSite.setFieldValue( prefix + "faktorZweifamilienhaus", "1,05" );
                    }
                    else {
                        pageSite.setFieldValue( prefix + "faktorZweifamilienhaus", null );
                    }
                }
            }
        } );

        // NHK BGF Wohnungsgröße
        lastLine = newLine;
        // newLine = createLabel( parent, , "NHK 2010 in €/m² Bruttogrundfläche",
        // one().top( lastLine ),
        // SWT.LEFT );
        newLine = newFormField( "NHK 2010" )
                .setToolTipText( "NHK 2010 in €/m² Bruttogrundfläche" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2010BewertungGebaeudeComposite>( selectedComposite, prefix
                                + "nhk", new PropertyCallback<NHK2010BewertungGebaeudeComposite>() {

                            @Override
                            public Property get( NHK2010BewertungGebaeudeComposite entity ) {
                                return entity.nhk();
                            }
                        } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) ).setEnabled( false )
                .setLayoutData( two().top( lastLine, 30 ).create() ).create();

        pageSite.addFieldListener( gebaeudeStandardListener = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == VALUE_CHANGE) {
                    if (ev.getFieldName().equalsIgnoreCase( prefix + "gebaeudeStandard" )) {
                        if ((ev.getNewValue() == null && selectedGebaeudeStandard != null)
                                || (ev.getNewValue() != null && !ev.getNewValue().equals( selectedGebaeudeStandard ))) {
                            selectedGebaeudeStandard = ev.getNewValue();
                            if (selectedGebaeudeStandard != null) {
                                Double value = MathUtil.round( selectedGebaeudeArt
                                        .calculateNHKFor( selectedGebaeudeStandard ) );
                                pageSite.setFieldValue( prefix + "nhk", NumberFormatter.getFormatter( 2 )
                                        .format( value ) );
                            }
                            else {
                                pageSite.setFieldValue( prefix + "nhk", null );
                            }
                        }
                    }
                }
            }

        } );

        newFormField( "BGF" )
                .setToolTipText( "Bruttogrundfläche" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2010BewertungGebaeudeComposite>( selectedComposite, prefix
                                + "bruttoGrundFlaeche", new PropertyCallback<NHK2010BewertungGebaeudeComposite>() {

                            @Override
                            public Property<Double> get( NHK2010BewertungGebaeudeComposite entity ) {
                                return entity.bruttoGrundFlaeche();
                            }
                        } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new MyNumberValidator( Double.class ) ).setEnabled( true )
                .setLayoutData( three().top( lastLine, 30 ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( "NHK 2010 korrigiert" )
                .setToolTipText(
                        "NHK 2010 korrigiert in €/m² Bruttogrundfläche korrigiert entsprechend der Korrekturfaktoren" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2010BewertungGebaeudeComposite>( selectedComposite, prefix
                                + "nhkKorrigiert", new PropertyCallback<NHK2010BewertungGebaeudeComposite>() {

                            @Override
                            public Property get( NHK2010BewertungGebaeudeComposite entity ) {
                                return entity.nhkKorrigiert();
                            }
                        } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) ).setEnabled( false )
                .setLayoutData( two().top( lastLine ).create() ).create();

        pageSite.addFieldListener( korrekturfaktorListener = new IFormFieldListener() {

            private Double faktorGrundrissart;

            private Double faktorWohnungsgroesse;

            private Double faktorZweifamilienhaus;

            private Double nhk;


            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == VALUE_CHANGE) {
                    if (ev.getFieldName().equalsIgnoreCase( prefix + "faktorGrundrissart" )) {
                        faktorGrundrissart = (Double)ev.getNewValue();
                        updateNhk();
                    }
                    else if (ev.getFieldName().equalsIgnoreCase( prefix + "faktorWohnungsgroesse" )) {
                        faktorWohnungsgroesse = (Double)ev.getNewValue();
                        updateNhk();
                    }
                    else if (ev.getFieldName().equalsIgnoreCase( prefix + "faktorZweifamilienhaus" )) {
                        faktorZweifamilienhaus = (Double)ev.getNewValue();
                        updateNhk();
                    }
                    else if (ev.getFieldName().equalsIgnoreCase( prefix + "nhk" )) {
                        nhk = (Double)ev.getNewValue();
                        updateNhk();
                    }
                }
            }


            private void updateNhk() {
                Double nhkKorrigiert = nhk;
                if (nhkKorrigiert != null && faktorGrundrissart != null) {
                    nhkKorrigiert *= faktorGrundrissart;
                }
                if (nhkKorrigiert != null && faktorWohnungsgroesse != null) {
                    nhkKorrigiert *= faktorWohnungsgroesse;
                }
                if (nhkKorrigiert != null && faktorZweifamilienhaus != null) {
                    nhkKorrigiert *= faktorZweifamilienhaus;
                }
                pageSite.setFieldValue(
                        getPropertyName( nameTemplate.nhkKorrigiert() ),
                        nhkKorrigiert != null ? NumberFormatter.getFormatter( 2 ).format(
                                MathUtil.round( nhkKorrigiert ) ) : null );
            }
        } );

        normalherstellungswert( parent, lastLine );

        lastLine = newLine;
        newLine = baukostenIndex( parent, lastLine );

        lastLine = newLine;
        newLine = baujahr( parent, lastLine );

        lastLine = newLine;
        newLine = alterswertMinderung( parent, lastLine );

        lastLine = newLine;
        newLine = createLabel( parent, "Zu-Abschläge nach §8 Absatz 3 ImmoWertV", one().top( lastLine ) );
        lastLine = newLine;
        newLine = immoWertV( parent, lastLine );

        lastLine = newLine;
        newLine = gebaeudeZeitWert( parent, lastLine );

        return newLine;
    }


    private Control immoWertV( Composite parent, Control lastLine ) {
        TreeMap<String, Object> zonen = new TreeMap<String, Object>();
        zonen.put( "Modernisierungsrückstau", "Modernisierungsrückstau" );
        zonen.put( "Wirtschaftliche Überalterung", "Wirtschaftliche Überalterung" );
        zonen.put( "Besondere Ertragsverhältnisse", "Besondere Ertragsverhältnisse" );
        zonen.put( "Überdurchschnittlicher Erhaltungszustand", "Überdurchschnittlicher Erhaltungszustand" );
        zonen.put( "Freilegungskosten", "Freilegungskosten" );
        zonen.put( "Bodenverunreinigungen", "Bodenverunreinigungen" );
        zonen.put( "Grundstücksbezogene Rechte/Lasten", "Grundstücksbezogene Rechte/Lasten" );

        Control newLine = createLabel( parent, "Baumängel/schädel", two().top( lastLine ) );
        newFormField( IFormFieldLabel.NO_LABEL )
                .setToolTipText( "Zu-/Abschlag in €" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2010BewertungGebaeudeComposite>( selectedComposite,
                                getPropertyName( nameTemplate.abschlagBaumaengelBetrag() ),
                                new PropertyCallback<NHK2010BewertungGebaeudeComposite>() {

                                    @Override
                                    public Property<Double> get( NHK2010BewertungGebaeudeComposite entity ) {
                                        return entity.abschlagBaumaengelBetrag();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) ).setEnabled( false )
                .setLayoutData( three().top( lastLine ).create() ).create();
        lastLine = newLine;
        newLine = createLabel( parent, "Unterhaltungsrückstau", two().top( lastLine ) );
        newFormField( IFormFieldLabel.NO_LABEL )
                .setToolTipText( "Zu-/Abschlag in €" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2010BewertungGebaeudeComposite>( selectedComposite,
                                getPropertyName( nameTemplate.abschlagRueckstauBetrag() ),
                                new PropertyCallback<NHK2010BewertungGebaeudeComposite>() {

                                    @Override
                                    public Property<Double> get( NHK2010BewertungGebaeudeComposite entity ) {
                                        return entity.abschlagRueckstauBetrag();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) )
                .setLayoutData( three().top( lastLine ).create() ).create();
        lastLine = newLine;
        newLine = newFormField( IFormFieldLabel.NO_LABEL )
                .setToolTipText( "Zu-/Abschlag in €" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2010BewertungGebaeudeComposite>( selectedComposite,
                                getPropertyName( nameTemplate.zuschlagZeile3Bezeichnung() ),
                                new PropertyCallback<NHK2010BewertungGebaeudeComposite>() {

                                    @Override
                                    public Property<String> get( NHK2010BewertungGebaeudeComposite entity ) {
                                        return entity.zuschlagZeile3Bezeichnung();
                                    }
                                } ) ).setField( reloadable( new PicklistFormField( zonen ) ) )
                .setLayoutData( two().top( lastLine ).create() ).create();
        newFormField( IFormFieldLabel.NO_LABEL )
                .setToolTipText( "Zu-/Abschlag in €" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2010BewertungGebaeudeComposite>( selectedComposite,
                                getPropertyName( nameTemplate.zuschlagZeile3Betrag() ),
                                new PropertyCallback<NHK2010BewertungGebaeudeComposite>() {

                                    @Override
                                    public Property<Double> get( NHK2010BewertungGebaeudeComposite entity ) {
                                        return entity.zuschlagZeile3Betrag();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) )
                .setLayoutData( three().top( lastLine ).create() ).create();
        lastLine = newLine;
        newLine = newFormField( IFormFieldLabel.NO_LABEL )
                .setToolTipText( "Zu-/Abschlag in €" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2010BewertungGebaeudeComposite>( selectedComposite,
                                getPropertyName( nameTemplate.zuschlagZeile4Bezeichnung() ),
                                new PropertyCallback<NHK2010BewertungGebaeudeComposite>() {

                                    @Override
                                    public Property<String> get( NHK2010BewertungGebaeudeComposite entity ) {
                                        return entity.zuschlagZeile4Bezeichnung();
                                    }
                                } ) ).setField( reloadable( new PicklistFormField( zonen ) ) )
                .setLayoutData( two().top( lastLine ).create() ).create();
        newFormField( IFormFieldLabel.NO_LABEL )
                .setToolTipText( "Zu-/Abschlag in €" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2010BewertungGebaeudeComposite>( selectedComposite,
                                getPropertyName( nameTemplate.zuschlagZeile4Betrag() ),
                                new PropertyCallback<NHK2010BewertungGebaeudeComposite>() {

                                    @Override
                                    public Property<Double> get( NHK2010BewertungGebaeudeComposite entity ) {
                                        return entity.zuschlagZeile4Betrag();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) )
                .setLayoutData( three().top( lastLine ).create() ).create();

        return newLine;
    }


    private Control baukostenIndex( Composite parent, Control lastLine ) {
        final PicklistFormField baukostenIndexList = new PicklistFormField( new PicklistFormField.ValueProvider() {

            private TreeMap<String, Object> zonen;


            @Override
            public SortedMap<String, Object> get() {
                if (zonen == null) {
                    zonen = new TreeMap<String, Object>();
                    zonen.put( "Wohngebäude", "W" );
                    zonen.put( "Bürogebäude", "B" );
                    zonen.put( "Gewerblicher Betrieb", "G" );
                }
                return zonen;
            }
        } );
        Control field = newFormField( "Baukostenindex" )
                .setToolTipText( "Baukostenindex auf Basis 2010" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2010BewertungGebaeudeComposite>( selectedComposite,
                                getPropertyName( nameTemplate.baukostenIndexTyp() ),
                                new PropertyCallback<NHK2010BewertungGebaeudeComposite>() {

                                    @Override
                                    public Property<String> get( NHK2010BewertungGebaeudeComposite entity ) {
                                        return entity.baukostenIndexTyp();
                                    }
                                } ) ).setField( reloadable( baukostenIndexList ) )
                .setLayoutData( two().top( lastLine ).create() ).create();

        final Composite indexField = newFormField( "" )
                .setToolTipText( "Baukostenindex auf Basis 2010" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2010BewertungGebaeudeComposite>( selectedComposite,
                                getPropertyName( nameTemplate.baukostenIndexWert() ),
                                new PropertyCallback<NHK2010BewertungGebaeudeComposite>() {

                                    @Override
                                    public Property<Double> get( NHK2010BewertungGebaeudeComposite entity ) {
                                        return entity.baukostenIndexWert();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) ).setEnabled( false )
                .setLayoutData( three().top( lastLine ).create() ).create();

        pageSite.addFieldListener( baukostenIndexWertListener = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == VALUE_CHANGE) {
                    if (ev.getFieldName().equalsIgnoreCase( getPropertyName( nameTemplate.baukostenIndexTyp() ) )) {
                        String indexType = (String)ev.getNewValue();
                        Values result = null;

                        // durchschnitt berechnen, nur wenn verändert
                        if (indexType != null
                                && !indexType.isEmpty()
                                && (selectedComposite.get() != null && (!indexType.equals( selectedComposite.get()
                                        .baukostenIndexTyp().get() ) || selectedComposite.get().baukostenIndexWert()
                                        .get() == null))) {
                            // Wertermittlungsstichtag = Vertragsdatum
                            result = NHK2010BaupreisIndexComposite.Mixin.indexFor( indexType, bewertung.vertrag().get()
                                    .vertragsDatum().get() );

                            pageSite.setFieldValue(
                                    getPropertyName( nameTemplate.baukostenIndexWert() ),
                                    result != null && result.result != null ? NumberFormatter.getFormatter( 2 ).format(
                                            result.result ) : null );
                            indexField.setToolTipText( result != null && result.result != null ? "Index: "
                                    + NumberFormatter.getFormatter( 2 ).format( result.index )
                                    + " / Durchschnitt 2010: "
                                    + NumberFormatter.getFormatter( 2 ).format( result.durchschnitt )
                                    : "Baukostenindex auf Basis 2010" );
                        }
                    }
                }
            }
        } );

        newFormField( "Neuwert" )
                .setToolTipText( "Normalherstellungswert * Baukostenindex auf Basis 2010" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2010BewertungGebaeudeComposite>( selectedComposite,
                                getPropertyName( nameTemplate.neuWert() ),
                                new PropertyCallback<NHK2010BewertungGebaeudeComposite>() {

                                    @Override
                                    public Property<Double> get( NHK2010BewertungGebaeudeComposite entity ) {
                                        return entity.neuWert();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) ).setEnabled( false )
                .setLayoutData( four().top( lastLine ).create() ).create();
        pageSite.addFieldListener( neuWertListener = new IFormFieldListener() {

            private Double normalHerstellungsWert;

            private Double baukostenIndexWert;


            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == VALUE_CHANGE) {
                    if (ev.getFieldName().equalsIgnoreCase( getPropertyName( nameTemplate.normalHerstellungsWert() ) )) {
                        normalHerstellungsWert = (Double)ev.getNewValue();
                        updateNhk();
                    }
                    else if (ev.getFieldName().equalsIgnoreCase( getPropertyName( nameTemplate.baukostenIndexWert() ) )) {
                        baukostenIndexWert = (Double)ev.getNewValue();
                        updateNhk();
                    }
                }
            }


            private void updateNhk() {
                Double neuwert = normalHerstellungsWert;
                if (neuwert != null && baukostenIndexWert != null) {
                    neuwert *= baukostenIndexWert;
                }
                if (neuwert != null) {
                    pageSite.setFieldValue( getPropertyName( nameTemplate.neuWert() ),
                            neuwert != null ? NumberFormatter.getFormatter( 2 ).format( neuwert ) : null );
                }
            }

        } );

        return field;
    }


    private Control baujahr( Composite parent, Control lastLine ) {
        final Control field = newFormField( "GND" )
                .setToolTipText( "Gesamtnutzungsdauer" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2010BewertungGebaeudeComposite>( selectedComposite,
                                getPropertyName( nameTemplate.gesamtNutzungsDauer() ),
                                new PropertyCallback<NHK2010BewertungGebaeudeComposite>() {

                                    @Override
                                    public Property<Double> get( NHK2010BewertungGebaeudeComposite entity ) {
                                        return entity.gesamtNutzungsDauer();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new MyNumberValidator( Double.class ) ).setLayoutData( two().top( lastLine ).create() )
                .create();

        pageSite.addFieldListener( gebaeudeStandardGNDListener = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == VALUE_CHANGE
                        && ev.getFieldName().equalsIgnoreCase( prefix + "gebaeudeStandard" )) {
                    String value = ev.getNewValue();
                    String faktor = null;
                    if (value != null) {
                        if (value.startsWith( "1" )) {
                            faktor = "60";
                        }
                        else if (value.startsWith( "2" )) {
                            faktor = "65";
                        }
                        else if (value.startsWith( "3" )) {
                            faktor = "70";
                        }
                        else if (value.startsWith( "4" )) {
                            faktor = "75";
                        }
                        else if (value.startsWith( "5" )) {
                            faktor = "80";
                        }
                    }
                    pageSite.setFieldValue( getPropertyName( nameTemplate.gesamtNutzungsDauer() ), faktor );
                }
            }
        } );

        pageSite.addFieldListener( gndListener = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == VALUE_CHANGE) {
                    if (ev.getFieldName().equalsIgnoreCase( getPropertyName( nameTemplate.gebaeudeArtId() ) )) {
                        String id = (String)ev.getNewValue();
                        StringBuffer tooltip = new StringBuffer( "Gesamtnutzungsdauer" );
                        if (id != null) {
                            NHK2010GebaeudeArtComposite art = NHK2010GebaeudeArtProvider.instance().gebaeudeForId( id );
                            Integer bis = art.getGndBis();
                            Integer von = art.getGndVon();
                            if (von != null && bis != null) {
                                tooltip.append( ": " ).append( von ).append( " - " ).append( bis ).append( " Jahre" );
                            }

                            if (selectedComposite.get().gesamtNutzungsDauer().get() == null) {
                                Integer gnd = 80;
                                if (bis != null) {
                                    gnd = bis;
                                }
                                pageSite.setFieldValue( getPropertyName( nameTemplate.gesamtNutzungsDauer() ), "" + gnd );
                            }
                        }
                        field.setToolTipText( tooltip.toString() );
                    }
                }
            }

        } );

        newFormField( "Baujahr" )
                .setToolTipText( "Tatsächliches Baujahr" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2010BewertungGebaeudeComposite>( selectedComposite,
                                getPropertyName( nameTemplate.tatsaechlichesBaujahr() ),
                                new PropertyCallback<NHK2010BewertungGebaeudeComposite>() {

                                    @Override
                                    public Property<Double> get( NHK2010BewertungGebaeudeComposite entity ) {
                                        return entity.tatsaechlichesBaujahr();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new MyNumberValidator( Double.class ) ).setLayoutData( three().top( lastLine ).create() )
                .create();

        Control bb = newFormField( "Baujahr bereinigt" )
                .setToolTipText( "Bereinigtes Baujahr" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2010BewertungGebaeudeComposite>( selectedComposite,
                                getPropertyName( nameTemplate.bereinigtesBaujahr() ),
                                new PropertyCallback<NHK2010BewertungGebaeudeComposite>() {

                                    @Override
                                    public Property<Double> get( NHK2010BewertungGebaeudeComposite entity ) {
                                        return entity.bereinigtesBaujahr();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new MyNumberValidator( Double.class ) ).setLayoutData( four().top( lastLine ).create() )
                .create();

        pageSite.addFieldListener( gndbjListener = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == VALUE_CHANGE) {
                    if (ev.getFieldName().equalsIgnoreCase( getPropertyName( nameTemplate.tatsaechlichesBaujahr() ) )) {
                        baujahr = (Double)ev.getNewValue();
                    }
                    else if (ev.getFieldName().equalsIgnoreCase( getPropertyName( nameTemplate.gesamtNutzungsDauer() ) )) {
                        gnd = (Double)ev.getNewValue();
                    }
                }
            }
        } );

        baujahrBerechneAction = new ActionButton( parent, new Action( "Berechnen" ) {

            @Override
            public void run() {
                NHK2010BewertungGebaeudeComposite gebaeude = selectedComposite.get();
                if (gebaeude != null) {
                    if (gnd == null || baujahr == null) {
                        MessageDialog.openError( PolymapWorkbench.getShellToParentOn(), "Fehlende Daten",
                                "Bitte geben Sie Gesamtnutzungsdauer und das tatsächliche Baujahr ein, bevor Sie diese Berechnung starten." );
                    }
                    else {
                        ErmittlungModernisierungsgradComposite ermittlung = ErmittlungModernisierungsgradComposite.Mixin
                                .forNHK2010( gebaeude );
                        if (ermittlung == null) {
                            ermittlung = repository.newEntity( ErmittlungModernisierungsgradComposite.class, null );
                            ermittlung.vertrag().set( bewertung.vertrag().get() );
                            ermittlung.nhk2010().set( selectedComposite.get() );
                            ermittlung.gebaeudeNummer().set( gebaeude.laufendeNummer().get() );
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
                        FormEditor targetEditor = KapsPlugin.openEditor( fs,
                                ErmittlungModernisierungsgradComposite.NAME, ermittlung );
                        EventManager.instance().publish(
                                new InterEditorPropertyChangeEvent( formEditor, targetEditor, ermittlung, ermittlung
                                        .gesamtNutzungsDauer().qualifiedName().name(), ermittlung.gesamtNutzungsDauer()
                                        .get(), gnd ) );
                        EventManager.instance().publish(
                                new InterEditorPropertyChangeEvent( formEditor, targetEditor, ermittlung, ermittlung
                                        .tatsaechlichesBaujahr().qualifiedName().name(), ermittlung
                                        .tatsaechlichesBaujahr().get(), baujahr ) );
                    }
                }
            }
        } );
        baujahrBerechneAction.setLayoutData( five().left( bb, 0 ).width( 40 ).top( lastLine ).height( 25 ).create() );
        baujahrBerechneAction.setEnabled( false );

        return field;
    }


    private Control alterswertMinderung( Composite parent, Control lastLine ) {
        final Control field = newFormField( "RND" )
                .setToolTipText( "Restnutzungsdauer" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2010BewertungGebaeudeComposite>( selectedComposite,
                                getPropertyName( nameTemplate.restNutzungsDauer() ),
                                new PropertyCallback<NHK2010BewertungGebaeudeComposite>() {

                                    @Override
                                    public Property<Double> get( NHK2010BewertungGebaeudeComposite entity ) {
                                        return entity.restNutzungsDauer();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new MyNumberValidator( Double.class ) ).setEnabled( false )
                .setLayoutData( two().top( lastLine ).create() ).create();

        pageSite.addFieldListener( rndListener = new IFormFieldListener() {

            private Double bereinigtesBaujahr;

            private Double tatsaechlichesBaujahr;

            private Double gesamtNutzungsDauer;


            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == VALUE_CHANGE) {
                    if (ev.getFieldName().equalsIgnoreCase( getPropertyName( nameTemplate.bereinigtesBaujahr() ) )) {
                        bereinigtesBaujahr = (Double)ev.getNewValue();
                        update();
                    }
                    else if (ev.getFieldName()
                            .equalsIgnoreCase( getPropertyName( nameTemplate.tatsaechlichesBaujahr() ) )) {
                        tatsaechlichesBaujahr = (Double)ev.getNewValue();
                        update();
                    }
                    if (ev.getFieldName().equalsIgnoreCase( getPropertyName( nameTemplate.gesamtNutzungsDauer() ) )) {
                        gesamtNutzungsDauer = (Double)ev.getNewValue();
                        update();
                    }
                }
            }


            private void update() {
                Double result = bereinigtesBaujahr;
                if (result == null) {
                    result = tatsaechlichesBaujahr;
                }
                if (result != null && gesamtNutzungsDauer != null) {
                    // alter berechnen
                    Calendar cal = new GregorianCalendar();
                    cal.setTime( bewertung.vertrag().get().vertragsDatum().get() );
                    Double alter = cal.get( Calendar.YEAR ) - result;
                    // rnd = gnd - alter
                    result = Math.max( gesamtNutzungsDauer - alter, 0 );

                    pageSite.setFieldValue( getPropertyName( nameTemplate.restNutzungsDauer() ),
                            result != null ? getFormatter( 0, false ).format( result ) : null );
                }
                else {
                    // nicht das Jahr als RND setzen
                    pageSite.setFieldValue( getPropertyName( nameTemplate.restNutzungsDauer() ), null );
                }
            }

        } );

        newFormField( "Alterswertminderung" )
                .setToolTipText( "Alterswertminderung linear: (GND - RND) / GND * 100" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2010BewertungGebaeudeComposite>( selectedComposite,
                                getPropertyName( nameTemplate.altersWertMinderung() ),
                                new PropertyCallback<NHK2010BewertungGebaeudeComposite>() {

                                    @Override
                                    public Property<Double> get( NHK2010BewertungGebaeudeComposite entity ) {
                                        return entity.altersWertMinderung();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) )
                .setLayoutData( three().top( lastLine ).create() ).create();

        pageSite.addFieldListener( altersWertMinderungListener = new IFormFieldListener() {

            private Double restNutzungsDauer;

            private Double gesamtNutzungsDauer;


            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == VALUE_CHANGE) {
                    if (ev.getFieldName().equalsIgnoreCase( getPropertyName( nameTemplate.restNutzungsDauer() ) )) {
                        restNutzungsDauer = (Double)ev.getNewValue();
                        update();
                    }
                    else if (ev.getFieldName().equalsIgnoreCase( getPropertyName( nameTemplate.gesamtNutzungsDauer() ) )) {
                        gesamtNutzungsDauer = (Double)ev.getNewValue();
                        update();
                    }
                }
            }


            private void update() {
                Double result = null;

                if (restNutzungsDauer != null && gesamtNutzungsDauer != null && gesamtNutzungsDauer != 0
                        && gesamtNutzungsDauer > restNutzungsDauer) {
                    result = ((gesamtNutzungsDauer.doubleValue() - restNutzungsDauer.doubleValue()) / gesamtNutzungsDauer
                            .doubleValue()) * 100;
                }
                pageSite.setFieldValue( getPropertyName( nameTemplate.altersWertMinderung() ),
                        result != null ? NumberFormatter.getFormatter( 2 ).format( result ) : null );
            }

        } );

        newFormField( "Zeitwert" )
                .setToolTipText( "Gebäudezeitwert ohne Zu-/Abschläge ImmoWertV" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2010BewertungGebaeudeComposite>( selectedComposite,
                                getPropertyName( nameTemplate.zeitwertRnd() ),
                                new PropertyCallback<NHK2010BewertungGebaeudeComposite>() {

                                    @Override
                                    public Property<Double> get( NHK2010BewertungGebaeudeComposite entity ) {
                                        return entity.zeitwertRnd();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) )
                .setLayoutData( four().top( lastLine ).create() ).create();

        pageSite.addFieldListener( zeitwertRndListener = new IFormFieldListener() {

            private Double altersWertMinderung;

            private Double neuWert;


            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == VALUE_CHANGE) {
                    if (ev.getFieldName().equalsIgnoreCase( getPropertyName( nameTemplate.altersWertMinderung() ) )) {
                        altersWertMinderung = (Double)ev.getNewValue();
                        update();
                    }
                    else if (ev.getFieldName().equalsIgnoreCase( getPropertyName( nameTemplate.neuWert() ) )) {
                        neuWert = (Double)ev.getNewValue();
                        update();
                    }
                }
            }


            private void update() {
                Double result = null;

                if (neuWert != null && altersWertMinderung != null) {
                    result = neuWert / 100 * (100 - altersWertMinderung);
                }
                pageSite.setFieldValue( getPropertyName( nameTemplate.zeitwertRnd() ), result != null ? NumberFormatter
                        .getFormatter( 2 ).format( MathUtil.round( result ) ) : null );
            }

        } );

        return field;
    }


    private Control normalherstellungswert( Composite parent, Control lastLine ) {
        Control field = newFormField( "Normalherstellwert" )
                .setToolTipText( "Normalherstellungswert nach NHK 2010 korrigiert in €" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2010BewertungGebaeudeComposite>( selectedComposite, prefix
                                + "normalHerstellungsWert", new PropertyCallback<NHK2010BewertungGebaeudeComposite>() {

                            @Override
                            public Property<Double> get( NHK2010BewertungGebaeudeComposite entity ) {
                                return entity.normalHerstellungsWert();
                            }
                        } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) ).setEnabled( false )
                .setLayoutData( four().top( lastLine ).create() ).create();

        pageSite.addFieldListener( normalHerstellungsWertListener = new IFormFieldListener() {

            private Double nhkKorrigiert;

            private Double bruttoGrundFlaeche;


            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == VALUE_CHANGE) {
                    if (ev.getFieldName().equalsIgnoreCase( getPropertyName( nameTemplate.nhkKorrigiert() ) )) {
                        nhkKorrigiert = (Double)ev.getNewValue();
                        update();
                    }
                    else if (ev.getFieldName().equalsIgnoreCase( getPropertyName( nameTemplate.bruttoGrundFlaeche() ) )) {
                        bruttoGrundFlaeche = (Double)ev.getNewValue();
                        update();
                    }
                }
            }


            private void update() {
                Double result = null;
                if (nhkKorrigiert != null && bruttoGrundFlaeche != null) {
                    result = nhkKorrigiert * bruttoGrundFlaeche;
                }
                if (result != null) {
                    pageSite.setFieldValue( getPropertyName( nameTemplate.normalHerstellungsWert() ),
                            result != null ? NumberFormatter.getFormatter( 2 ).format( MathUtil.round( result ) )
                                    : null );
                }
            }

        } );
        return field;
    }


    private Control gebaeudeZeitWert( Composite parent, Control lastLine ) {
        Control field = newFormField( "Gebäudezeitwert" )
                .setToolTipText( "Gebäudezeitwert in €" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2010BewertungGebaeudeComposite>( selectedComposite,
                                getPropertyName( nameTemplate.gebaeudeZeitWert() ),
                                new PropertyCallback<NHK2010BewertungGebaeudeComposite>() {

                                    @Override
                                    public Property<Double> get( NHK2010BewertungGebaeudeComposite entity ) {
                                        return entity.gebaeudeZeitWert();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) ).setEnabled( false )
                .setLayoutData( four().top( lastLine ).bottom( 100 ).create() ).create();

        pageSite.addFieldListener( gebaeudeZeitWertListener = new IFormFieldListener() {

            private Double zeitwertRnd;

            private Double abschlagRueckstauBetrag;

            private Double abschlagBaumaengelBetrag;

            private Double zuschlagZeile3Betrag;

            private Double zuschlagZeile4Betrag;


            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == VALUE_CHANGE) {
                    if (ev.getFieldName().equalsIgnoreCase( getPropertyName( nameTemplate.zeitwertRnd() ) )) {
                        zeitwertRnd = (Double)ev.getNewValue();
                        update();
                    }
                    else if (ev.getFieldName().equalsIgnoreCase(
                            getPropertyName( nameTemplate.abschlagBaumaengelBetrag() ) )) {
                        abschlagBaumaengelBetrag = (Double)ev.getNewValue();
                        update();
                    }
                    else if (ev.getFieldName().equalsIgnoreCase(
                            getPropertyName( nameTemplate.abschlagRueckstauBetrag() ) )) {
                        abschlagRueckstauBetrag = (Double)ev.getNewValue();
                        update();
                    }
                    else if (ev.getFieldName()
                            .equalsIgnoreCase( getPropertyName( nameTemplate.zuschlagZeile3Betrag() ) )) {
                        zuschlagZeile3Betrag = (Double)ev.getNewValue();
                        update();
                    }
                    else if (ev.getFieldName()
                            .equalsIgnoreCase( getPropertyName( nameTemplate.zuschlagZeile4Betrag() ) )) {
                        zuschlagZeile4Betrag = (Double)ev.getNewValue();
                        update();
                    }
                }
            }


            private void update() {
                Double result = zeitwertRnd;
                // Zu/-Abschläge nach ImmoWertV
                if (result != null && abschlagBaumaengelBetrag != null) {
                    result -= abschlagBaumaengelBetrag;
                }
                if (result != null && abschlagRueckstauBetrag != null) {
                    result -= abschlagRueckstauBetrag;
                }
                if (result != null && zuschlagZeile3Betrag != null) {
                    result += zuschlagZeile3Betrag;
                }
                if (result != null && zuschlagZeile4Betrag != null) {
                    result += zuschlagZeile4Betrag;
                }
                if (result != null) {
                    pageSite.setFieldValue( getPropertyName( nameTemplate.gebaeudeZeitWert() ),
                            result != null ? NumberFormatter.getFormatter( 2 ).format( MathUtil.round( result ) )
                                    : null );
                }
            }

        } );

        field = createLabel( parent, "", one().top( field ) );
        return field;
    }


    protected void postProcessGebaeudeArtSelection() {
        pageSite.setFieldEnabled( prefix + "gebaeudeStandard", selectedGebaeudeArt != null );
        pageSite.setFieldEnabled( prefix + "grundrissArt", selectedGebaeudeArt != null
                && (selectedGebaeudeArt.getId().startsWith( "4" ) || selectedGebaeudeArt.getId().startsWith( "5.1" )) );
        pageSite.setFieldEnabled( prefix + "anzahlWohnungen", selectedGebaeudeArt != null
                && (selectedGebaeudeArt.getId().startsWith( "4" ) || selectedGebaeudeArt.getId().startsWith( "5.1" )) );
        pageSite.setFieldEnabled( prefix + "zweifamilienHaus", selectedGebaeudeArt != null
                && selectedGebaeudeArt.getId().startsWith( "1" ) );
        // reset von zfh, anzahlzimmer und grundrissart und
        // korrfaktoren
        pageSite.setFieldValue( prefix + "grundrissArt", null );
        pageSite.setFieldValue( prefix + "anzahlWohnungen", null );
    }


    @Override
    protected EntityType addViewerColumns( FeatureTableViewer viewer ) {
        final KapsRepository repo = KapsRepository.instance();
        final EntityType<NHK2010BewertungGebaeudeComposite> type = repo
                .entityType( NHK2010BewertungGebaeudeComposite.class );

        PropertyDescriptor prop = null;
        prop = new PropertyDescriptorAdapter( type.getProperty( "laufendeNummer" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Lfd. Nr." ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "gebaeudeArtId" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Art" ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "gebaeudeZeitWert" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Gebäudezeitwert" ) );

        return type;
    }


    @Override
    protected Iterable<NHK2010BewertungGebaeudeComposite> getElements() {
        return NHK2010BewertungGebaeudeComposite.Mixin.forBewertung( bewertung );
    }


    @Override
    protected NHK2010BewertungGebaeudeComposite createNewComposite()
            throws Exception {
        return repository.newEntity( NHK2010BewertungGebaeudeComposite.class, null,
                new EntityCreator<NHK2010BewertungGebaeudeComposite>() {

                    public void create( NHK2010BewertungGebaeudeComposite prototype )
                            throws Exception {
                        prototype.bewertung().set( bewertung );
                        int nextNumber = 1;
                        for (Object o : getElements()) {
                            nextNumber++;
                        }
                        prototype.laufendeNummer().set( nextNumber );
                    }
                } );
    }


    private String getPropertyName( Property<?> property ) {
        return prefix + nameTemplate.toString();
    }


    public static void main( String[] args ) {
        System.out.println( Math.floor( 9.5d + 0.5d ) );
        System.out.println( Math.floor( 9.4d + 0.55d ) );
        System.out.println( Math.floor( 9.49d + 0.55d ) );
        System.out.println( Math.floor( 9.8d + 0.5d ) );
        System.out.println( Math.round( 9.5d ) );
        System.out.println( Math.round( 9.4d ) );
        System.out.println( Math.round( (9.49d * 100) / 100 ) );
        System.out.println( Math.round( 9.8d ) );
        System.out.println( new DecimalFormat( "###" ).format( 9.49d ) );
        BigDecimal bd = new BigDecimal( 9.49d );
        bd = bd.round( MathContext.UNLIMITED );
        System.out.println( bd.toString() );

    }
}
