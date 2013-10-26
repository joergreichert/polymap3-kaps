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

import java.util.TreeMap;

import java.beans.PropertyChangeListener;

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.entity.Entity;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.ui.forms.widgets.Section;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.data.entityfeature.AssociationAdapter;
import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.form.FormEditor;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.KapsPlugin;
import org.polymap.kaps.model.data.AusstattungBewertungComposite;
import org.polymap.kaps.model.data.AusstattungComposite;
import org.polymap.kaps.model.data.EigentumsartComposite;
import org.polymap.kaps.model.data.ErmittlungModernisierungsgradComposite;
import org.polymap.kaps.model.data.EtageComposite;
import org.polymap.kaps.model.data.FlurstueckComposite;
import org.polymap.kaps.model.data.HimmelsrichtungComposite;
import org.polymap.kaps.model.data.ImmobilienArtStaBuComposite;
import org.polymap.kaps.model.data.RichtwertzoneComposite;
import org.polymap.kaps.model.data.RichtwertzoneZeitraumComposite;
import org.polymap.kaps.model.data.StockwerkStaBuComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.model.data.VertragsdatenErweitertComposite;
import org.polymap.kaps.model.data.WohnungComposite;
import org.polymap.kaps.ui.ActionButton;
import org.polymap.kaps.ui.BooleanFormField;
import org.polymap.kaps.ui.FieldCalculation;
import org.polymap.kaps.ui.FieldListener;
import org.polymap.kaps.ui.InterEditorListener;
import org.polymap.kaps.ui.InterEditorPropertyChangeEvent;
import org.polymap.kaps.ui.MyNumberValidator;
import org.polymap.kaps.ui.NotNullMyNumberValidator;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class WohnungGrunddatenFormEditorPage
        extends WohnungFormEditorPage {

    private static Log                log = LogFactory.getLog( WohnungGrunddatenFormEditorPage.class );

    private PropertyChangeListener    compositeListener;

    private IFormFieldListener        publisher;

    private FieldCalculation          riwezuschlag;

    private FieldCalculation          riweabschlag;

    private FieldCalculation          preisunbebaut;

    private FieldCalculation          bebabschlag;

    private FieldCalculation          bodenpreisbebaut;

    private IFormFieldListener        richtwertzone;

    private ActionButton              baujahrBerechneAction;

    private FieldListener             gndbjListener;

    private FormEditor                formEditor;

    private final InterEditorListener editorListener;

    private ActionButton              bewertungAction;

    private IFormFieldListener        ausstattungListener;

    private IFormFieldListener        flurstueckListener;


    public WohnungGrunddatenFormEditorPage( FormEditor formEditor, Feature feature, FeatureStore featureStore ) {
        super( WohnungGrunddatenFormEditorPage.class.getName(), "Grunddaten", feature, featureStore );
        this.formEditor = formEditor;
        EventManager.instance().subscribe(
                editorListener = new InterEditorListener( wohnung.bereinigtesBaujahr(), wohnung.bewertungsPunkte() ) {

                    @Override
                    protected void onChangedValue( IFormEditorPageSite site, Entity entity, String fieldName,
                            Object value ) {
                        if (fieldName.equals( wohnung.bereinigtesBaujahr().qualifiedName().name() )) {
                            pageSite.setFieldValue( fieldName, value != null ? getFormatter( 0, false ).format( value )
                                    : null );
                        }
                        else if (fieldName.equals( wohnung.bewertungsPunkte().qualifiedName().name() )) {
                            pageSite.setFieldValue( fieldName, value != null ? getFormatter( 0, false ).format( value )
                                    : null );
                        }
                    }

                }, new InterEditorListener.EventFilter( wohnung ) );
    }


    @Override
    public void dispose() {
        super.dispose();
        EventManager.instance().unsubscribe( editorListener );
    }


    @Override
    public void afterDoLoad( IProgressMonitor monitor )
            throws Exception {
        editorListener.flush( pageSite );
        pageSite.addFieldListener( flurstueckListener = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == IFormFieldListener.VALUE_CHANGE
                        && ev.getFieldName().endsWith( wohnung.flurstueck().qualifiedName().name() )) {
                    updateBodenPreis( (FlurstueckComposite)ev.getNewValue() );
                }
            }
        } );
        updateBodenPreis( wohnung.flurstueck().get() );
        updateKaufpreis();
    }


    private void updateKaufpreis() {
        VertragComposite vertrag = wohnung.vertrag().get();
        Double kaufpreis = null;
        if (vertrag != null) {
            if (vertrag.erweiterteVertragsdaten().get() != null) {
                VertragsdatenErweitertComposite vertragsdatenErweitertComposite = vertrag.erweiterteVertragsdaten()
                        .get();
                kaufpreis = vertragsdatenErweitertComposite.bereinigterVollpreis().get();
            }
            // erweiterte Daten kann leer sein
            if (kaufpreis == null || kaufpreis == 0.0d) {
                kaufpreis = vertrag.vollpreis().get();
            }
        }
        if (kaufpreis != null && !kaufpreis.equals( wohnung.kaufpreis().get() )) {
            pageSite.fireEvent( this, wohnung.kaufpreis().qualifiedName().name(), IFormFieldListener.VALUE_CHANGE,
                    kaufpreis );
        }
    }


    private void updateBodenPreis( FlurstueckComposite flurstueck ) {
        // refresh bodenpreis aus richtwertzone vom flurstück
        if (flurstueck != null) {
            RichtwertzoneComposite richtwertzoneComposite = flurstueck.richtwertZone().get();
            VertragComposite vertrag = flurstueck.vertrag().get();
            if (vertrag != null && richtwertzoneComposite != null) {
                RichtwertzoneZeitraumComposite zeitraum = RichtwertzoneZeitraumComposite.Mixin.findZeitraumFor(
                        richtwertzoneComposite, vertrag.vertragsDatum().get() );
                if (zeitraum != null) {
                    pageSite.fireEvent( this, wohnung.bodenrichtwert().qualifiedName().name(),
                            IFormFieldListener.VALUE_CHANGE, zeitraum.euroQm().get() );
                    if (wohnung.bodenpreis().get() == null) {
                        // set the bodenpreis also to this default value
                        pageSite.fireEvent( this, wohnung.bodenpreis().qualifiedName().name(),
                                IFormFieldListener.VALUE_CHANGE, zeitraum.euroQm().get() );
                    }
                }
            }
        }
    }


    @SuppressWarnings("unchecked")
    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );

        Composite newLine, lastLine = null;
        Composite parent = pageSite.getPageBody();

        newLine = newFormField( IFormFieldLabel.NO_LABEL ).setToolTipText( "Objektnummer" )
                .setProperty( new PropertyAdapter( wohnung.objektNummer() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NotNullMyNumberValidator( Integer.class ) )
                .setEnabled( wohnung.objektNummer().get() == null )
                .setLayoutData( left().left( 0 ).right( 15 ).create() ).create();

        newFormField( IFormFieldLabel.NO_LABEL ).setToolTipText( "Fortführung" )
                .setProperty( new PropertyAdapter( wohnung.objektFortfuehrung() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NotNullMyNumberValidator( Integer.class ) )
                .setEnabled( wohnung.objektNummer().get() == null )
                .setLayoutData( left().left( 16 ).right( 31 ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( IFormFieldLabel.NO_LABEL ).setToolTipText( "Gebäudenummer" )
                .setProperty( new PropertyAdapter( wohnung.gebaeudeNummer() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NotNullMyNumberValidator( Integer.class ) )
                .setEnabled( wohnung.objektNummer().get() == null )
                .setLayoutData( left().left( 34 ).right( 49 ).create() ).create();

        newFormField( IFormFieldLabel.NO_LABEL ).setToolTipText( "Fortführung" )
                .setProperty( new PropertyAdapter( wohnung.gebaeudeFortfuehrung() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NotNullMyNumberValidator( Integer.class ) )
                .setEnabled( wohnung.objektNummer().get() == null )
                .setLayoutData( left().left( 50 ).right( 65 ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( IFormFieldLabel.NO_LABEL ).setToolTipText( "Wohnungsnummer" )
                .setProperty( new PropertyAdapter( wohnung.wohnungsNummer() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NotNullMyNumberValidator( Integer.class ) )
                // .setEnabled( wohnung.wohnungsNummer().get() == null )
                .setLayoutData( left().left( 69 ).right( 83 ).create() ).create();

        newFormField( IFormFieldLabel.NO_LABEL ).setToolTipText( "Fortführung" )
                .setProperty( new PropertyAdapter( wohnung.wohnungsFortfuehrung() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NotNullMyNumberValidator( Integer.class ) )
                .setEnabled( wohnung.wohnungsNummer().get() == null )
                .setLayoutData( left().left( 84 ).right( 100 ).create() ).create();

        // flurstücke
        lastLine = newLine;
        TreeMap<String, FlurstueckComposite> flurstuecke = new TreeMap<String, FlurstueckComposite>();

        if (wohnung.gebaeudeNummer().get() != null) {
            // its not a new wohnung without a gebaeude
            Iterable<FlurstueckComposite> iterable = WohnungComposite.Mixin.findFlurstueckeFor( wohnung );
            for (FlurstueckComposite flurstueck : iterable) {
                flurstuecke.put( flurstueck.name().get(), flurstueck );
            }
        }
        if (wohnung.flurstueck().get() != null) {
            flurstuecke.put( wohnung.flurstueck().get().name().get(), wohnung.flurstueck().get() );
        }
        newLine = newFormField( "Lage" )
                .setProperty( new AssociationAdapter<FlurstueckComposite>( wohnung.flurstueck() ) )
                .setField( new PicklistFormField( flurstuecke.descendingMap() ) )
                .setLayoutData( left().top( lastLine ).right( RIGHT ).create() ).setParent( parent ).create();

        lastLine = newLine;
        newLine = newFormField( "Eigentumsnr." ).setToolTipText( "Wohnungseigentumsnummer" )
                .setProperty( new PropertyAdapter( wohnung.wohnungseigentumsNummer() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( parent ).create();

        lastLine = newLine;
        newLine = newFormField( "Eigentum" ).setToolTipText( "Eigentum am Grundstück" )
                .setProperty( new AssociationAdapter<EigentumsartComposite>( wohnung.eigentumsArt() ) )
                .setField( namedAssocationsPicklist( EigentumsartComposite.class ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( parent ).create();
        newFormField( "Anzahl Zimmer" ).setProperty( new PropertyAdapter( wohnung.anzahlZimmer() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new MyNumberValidator( Double.class ) )
                .setLayoutData( right().right( 75 ).top( lastLine ).create() ).setParent( parent ).create();

        lastLine = newLine;
        newLine = createFlaecheField( "Wohn-/Nutzfläche", "Woh-/Nutzfläche in m²", wohnung.wohnflaeche(),
                left().top( lastLine ), parent, true );

        lastLine = newLine;
        newLine = newFormField( "Gesamtnutzungsdauer" ).setToolTipText( "Gesamtnutzungsdauer in Jahren" )
                .setProperty( new PropertyAdapter( wohnung.gesamtNutzungsDauer() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new MyNumberValidator( Double.class ) ).setLayoutData( left().top( lastLine ).create() )
                .setParent( parent ).create();

        lastLine = newLine;
        newLine = newFormField( "Baujahr tatsächlich" ).setToolTipText( "Baujahr tatsächlich" )
                .setProperty( new PropertyAdapter( wohnung.baujahr() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new MyNumberValidator( Double.class ) ).setLayoutData( left().top( lastLine ).create() )
                .setParent( parent ).create();
        Composite baujahrField = newFormField( "Baujahr bereinigt" ).setToolTipText( "Baujahr bereinigt" )
                .setProperty( new PropertyAdapter( wohnung.bereinigtesBaujahr() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new MyNumberValidator( Double.class ) )
                .setLayoutData( right().right( 75 ).top( lastLine ).create() ).setParent( parent ).create();

        site.addFieldListener( gndbjListener = new NonFiringFieldListener( wohnung.gesamtNutzungsDauer(), wohnung
                .baujahr() ) );

        // berechnen knopf
        baujahrBerechneAction = new ActionButton( parent, new Action( "Berechnen" ) {

            @Override
            public void run() {
                if (gndbjListener.get( wohnung.gesamtNutzungsDauer() ) == null
                        || gndbjListener.get( wohnung.baujahr() ) == null) {
                    MessageDialog.openError( PolymapWorkbench.getShellToParentOn(), "Fehlende Daten",
                            "Bitte geben Sie Gesamtnutzungsdauer und das tatsächliche Baujahr ein, bevor Sie diese Berechnung starten." );
                }
                else {
                    ErmittlungModernisierungsgradComposite ermittlung = ErmittlungModernisierungsgradComposite.Mixin
                            .forWohnung( wohnung );
                    if (ermittlung == null) {
                        ermittlung = repository.newEntity( ErmittlungModernisierungsgradComposite.class, null );
                        ermittlung.wohnung().set( wohnung );
                        // ermittlung.vertrag().set( bewertung.vertrag().get() );
                        ermittlung.objektNummer().set( wohnung.objektNummer().get() );
                        ermittlung.objektFortfuehrung().set( wohnung.objektFortfuehrung().get() );
                        ermittlung.gebaeudeNummer().set( wohnung.gebaeudeNummer().get() );
                        ermittlung.gebaeudeFortfuehrung().set( wohnung.gebaeudeFortfuehrung().get() );
                        ermittlung.wohnungsNummer().set( wohnung.wohnungsNummer().get() );
                        ermittlung.wohnungsFortfuehrung().set( wohnung.wohnungsFortfuehrung().get() );
                        ermittlung.alterObergrenzeZeile1().set( 20.0d );
                        ermittlung.alterObergrenzeZeile2().set( 20.0d );
                        ermittlung.alterObergrenzeZeile3().set( 20.0d );
                        ermittlung.alterObergrenzeZeile4().set( 20.0d );
                        ermittlung.alterObergrenzeZeile5().set( 20.0d );
                        ermittlung.alterObergrenzeZeile6().set( 20.0d );
                        ermittlung.alterObergrenzeZeile7().set( 20.0d );
                        ermittlung.alterObergrenzeZeile8().set( 20.0d );
                    }
                    // ermittlung.gesamtNutzungsDauer().set( gndbjListener.get(
                    // wohnung.gesamtNutzungsDauer() ) );
                    // ermittlung.tatsaechlichesBaujahr().set( gndbjListener.get(
                    // wohnung.baujahr() ) );
                    FormEditor targetEditor = KapsPlugin.openEditor( fs, ErmittlungModernisierungsgradComposite.NAME,
                            ermittlung );
                    EventManager.instance().publish(
                            new InterEditorPropertyChangeEvent( formEditor, targetEditor, ermittlung, ermittlung
                                    .gesamtNutzungsDauer().qualifiedName().name(), ermittlung.gesamtNutzungsDauer()
                                    .get(), gndbjListener.get( wohnung.gesamtNutzungsDauer() ) ) );
                    EventManager.instance().publish(
                            new InterEditorPropertyChangeEvent( formEditor, targetEditor, ermittlung, ermittlung
                                    .tatsaechlichesBaujahr().qualifiedName().name(), ermittlung.tatsaechlichesBaujahr()
                                    .get(), gndbjListener.get( wohnung.baujahr() ) ) );
                }
            }
        } );
        baujahrBerechneAction.setLayoutData( right().left( baujahrField ).top( lastLine ).height( 25 ).create() );
        baujahrBerechneAction.setEnabled( true );

        lastLine = newLine;
        newLine = newFormField( "Etage" ).setProperty( new AssociationAdapter<EtageComposite>( wohnung.etage() ) )
                .setField( namedAssocationsPicklist( EtageComposite.class ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( parent ).create();
        newFormField( "Bemerkung" ).setToolTipText( "Bemerkung zur Etage" )
                .setProperty( new PropertyAdapter( wohnung.etageBeschreibung() ) ).setField( new StringFormField() )
                .setLayoutData( right().top( lastLine ).create() ).setParent( parent ).create();

        lastLine = newLine;
        newLine = newFormField( "Stockwerk" ).setToolTipText( "Stockwerk entsprechend Statistischem Bundesamt" )
                .setProperty( new AssociationAdapter<StockwerkStaBuComposite>( wohnung.stockwerkStaBu() ) )
                .setField( namedAssocationsPicklist( StockwerkStaBuComposite.class ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( parent ).create();

        newLine = newFormField( "Art" ).setToolTipText( "Art entsprechend Statistischem Bundesamt" )
                .setProperty( new AssociationAdapter<ImmobilienArtStaBuComposite>( wohnung.immobilienArtStaBu() ) )
                .setField( namedAssocationsPicklist( ImmobilienArtStaBuComposite.class ) )
                .setLayoutData( right().top( lastLine ).create() ).setParent( parent ).create();

        lastLine = newLine;
        newLine = newFormField( "Himmelsrichtung" )
                .setProperty( new AssociationAdapter<HimmelsrichtungComposite>( wohnung.himmelsrichtung() ) )
                .setField( namedAssocationsPicklist( HimmelsrichtungComposite.class ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( parent ).create();
        newFormField( "Umbau" ).setToolTipText( "Jahr des letzen Umbaus" )
                .setProperty( new PropertyAdapter( wohnung.umbau() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new MyNumberValidator( Integer.class ) )
                .setLayoutData( right().top( lastLine ).create() ).setParent( parent ).create();

        lastLine = newLine;
        newLine = newFormField( "Bemerkung" ).setProperty( new PropertyAdapter( wohnung.bemerkung() ) )
                .setField( new TextFormField() )
                .setLayoutData( left().right( RIGHT ).height( 50 ).top( lastLine ).create() ).setParent( parent )
                .create();

        Section section = newSection( newLine, "Ausstattung" );
        Composite client = (Composite)section.getClient();

        lastLine = newLine;
        newLine = newFormField( "Balkon" ).setToolTipText( "wertrelevanter Balkon?" )
                .setProperty( new PropertyAdapter( wohnung.balkon() ) ).setField( new BooleanFormField() )
                .setLayoutData( left().top( lastLine ).create() ).setParent( client ).create();
        newFormField( "Terrasse" ).setToolTipText( "wertrelevante Terrasse?" )
                .setProperty( new PropertyAdapter( wohnung.terrasse() ) ).setField( new BooleanFormField() )
                .setLayoutData( right().top( lastLine ).create() ).setParent( client ).create();

        lastLine = newLine;
        newLine = newFormField( "Punkte" ).setToolTipText( "Bewertungspunkte der Ausstattung" )
                .setProperty( new PropertyAdapter( wohnung.bewertungsPunkte() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new MyNumberValidator( Double.class ) )
                .setLayoutData( left().right( 40 ).top( lastLine ).bottom( 100 ).create() ).setParent( client )
                .create();

        Composite schluessel = newFormField( "Schlüssel" )
                .setProperty( new AssociationAdapter<AusstattungComposite>( wohnung.ausstattungSchluessel() ) )
                .setField( namedAssocationsPicklist( AusstattungComposite.class ) )
                .setLayoutData( right().left( newLine ).right( 75 ).top( lastLine ).create() ).setParent( client )
                .create();
        site.addFieldListener( ausstattungListener = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == IFormFieldListener.VALUE_CHANGE
                        && ev.getFieldName().equals( wohnung.bewertungsPunkte().qualifiedName().name() )) {
                    site.setFieldValue( wohnung.ausstattungSchluessel().qualifiedName().name(),
                            AusstattungComposite.Mixin.forWert( (Double)ev.getNewValue() ) );
                }
            }
        } );

        // berechnen knopf
        bewertungAction = new ActionButton( client, new Action( "Ermitteln" ) {

            @Override
            public void run() {
                AusstattungBewertungComposite aust = AusstattungBewertungComposite.Mixin.forWohnung( wohnung );

                if (aust == null) {
                    aust = repository.newEntity( AusstattungBewertungComposite.class, null );
                    aust.wohnung().set( wohnung );
                }
                FormEditor targetEditor = KapsPlugin.openEditor( fs, AusstattungBewertungComposite.NAME, aust );
            }

        } );
        bewertungAction.setLayoutData( right().left( schluessel ).top( lastLine ).height( 25 ).create() );
        bewertungAction.setEnabled( true );
    }
}
