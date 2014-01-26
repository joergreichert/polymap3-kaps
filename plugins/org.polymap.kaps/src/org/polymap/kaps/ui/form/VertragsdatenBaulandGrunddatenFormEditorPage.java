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

import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.ui.forms.widgets.Section;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.data.entityfeature.AssociationAdapter;
import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.CheckboxFormField;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.form.FormEditor;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.KapsPlugin;
import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.data.BodennutzungComposite;
import org.polymap.kaps.model.data.ErschliessungsBeitragComposite;
import org.polymap.kaps.model.data.ErtragswertverfahrenComposite;
import org.polymap.kaps.model.data.NHK2010BewertungComposite;
import org.polymap.kaps.model.data.RichtwertzoneComposite;
import org.polymap.kaps.model.data.RichtwertzoneZeitraumComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.model.data.VertragsdatenBaulandComposite;
import org.polymap.kaps.ui.ActionButton;
import org.polymap.kaps.ui.FieldCalculation;
import org.polymap.kaps.ui.InterEditorPropertyChangeEvent;
import org.polymap.kaps.ui.MyNumberValidator;
import org.polymap.kaps.ui.NumberFormatter;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class VertragsdatenBaulandGrunddatenFormEditorPage
        extends VertragsdatenBaulandFormEditorPage {

    private static Log             log = LogFactory.getLog( VertragsdatenBaulandGrunddatenFormEditorPage.class );

    private PropertyChangeListener compositeListener;

    private IFormFieldListener     publisher;

    private FieldCalculation       riwezuschlag;

    private FieldCalculation       riweabschlag;

    // private FieldCalculation preisunbebaut;

    private FieldCalculation       bebabschlag;

    private FieldCalculation       bodenpreisbebaut;

    private IFormFieldListener     richtwertzone;

    private ActionButton           openBewertungen;

    private ActionButton           openErtragswert;

    private FormEditor             formEditor;


    // private IFormFieldListener gemeindeListener;

    public VertragsdatenBaulandGrunddatenFormEditorPage( FormEditor formEditor, Feature feature,
            FeatureStore featureStore ) {
        super( VertragsdatenBaulandGrunddatenFormEditorPage.class.getName(), "Grunddaten", feature, featureStore );
        this.formEditor = formEditor;
        //
        // EventManager.instance().subscribe(
        // fieldListener = new FieldListener( kaufvertrag.vollpreis(),
        // erweitert.bereinigterVollpreis() ),
        // new FieldListener.EventFilter( formEditor ) );
    }


    @SuppressWarnings("unchecked")
    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );

        Composite newLine, lastLine = null;
        Composite parent = pageSite.getPageBody();

        final VertragComposite kaufvertrag = vb.vertrag().get();
        String nummer = EingangsNummerFormatter.format( kaufvertrag.eingangsNr().get() );
        String label = kaufvertrag == null ? "Kein Vertrag zugewiesen" : "Vertrag " + nummer + " öffnen";
        ActionButton openVertrag = new ActionButton( parent, new Action( label ) {

            @Override
            public void run() {
                KapsPlugin.openEditor( fs, VertragComposite.NAME, kaufvertrag );
            }
        } );
        openVertrag.setLayoutData( left().height( 25 ).create() );
        openVertrag.setEnabled( kaufvertrag != null );

        Section section = newSection( parent, "Richtwertberechnung" );
        section.setLayoutData( new SimpleFormData( SECTION_SPACING ).left( 0 ).right( 100 ).top( openVertrag ).create() );
        Composite client = (Composite)section.getClient();

        newLine = newFormField( "Lageklasse" ).setProperty( new PropertyAdapter( vb.lageklasse() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) )
                .setLayoutData( left().right( 33 ).top( lastLine ).create() ).setParent( client ).create();
        newFormField( "zul. GFZ" ).setProperty( new PropertyAdapter( vb.gfz() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) )
                .setLayoutData( left().left( 33 ).right( 66 ).top( lastLine ).create() ).setParent( client ).create();
        newFormField( "zul. Vollgeschosse" ).setProperty( new PropertyAdapter( vb.zulaessigeVollgeschosse() ) )
                .setField( new StringFormField() ).setLayoutData( right().left( 66 ).top( lastLine ).create() )
                .setParent( client ).create();

        // lastLine = newLine;
        // newLine = newFormField( "Gebäudeart" ).setEnabled( false )
        // .setProperty( new AssociationAdapter<GebaeudeArtComposite>(
        // vb.gebaeudeArt() ) )
        // .setField( namedAssocationsPicklist( GebaeudeArtComposite.class ) )
        // .setLayoutData( left().top( lastLine ).create() ).setParent( client
        // ).create();

        lastLine = newLine;
        newLine = newFormField( "Bodennutzung" )
                .setProperty( new AssociationAdapter<BodennutzungComposite>( vb.bodennutzung() ) )
                .setField( namedAssocationsPicklist( BodennutzungComposite.class ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( client ).create();

        newFormField( "Erschließungsbeitrag" )
                .setProperty( new AssociationAdapter<ErschliessungsBeitragComposite>( vb.erschliessungsBeitrag() ) )
                .setField( namedAssocationsPicklist( ErschliessungsBeitragComposite.class ) )
                .setLayoutData( right().top( lastLine ).create() ).setParent( client ).create();

        lastLine = newLine;
        newLine = newFormField( "Baujahr tatsächlich" ).setProperty( new PropertyAdapter( vb.baujahr() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new MyNumberValidator( Integer.class ) ).setLayoutData( left().top( lastLine ).create() )
                .setParent( client ).create();
        newFormField( "Baujahr bereinigt" ).setProperty( new PropertyAdapter( vb.baujahrBereinigt() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new MyNumberValidator( Integer.class ) )
                .setLayoutData( right().top( lastLine ).create() ).setParent( client ).create();

        lastLine = newLine;
        TreeMap<String, Object> zonen = new TreeMap<String, Object>();
        TreeMap<String, Object> zeitraeume = new TreeMap<String, Object>();
        // FlurstueckComposite flurstueck = vb.flurstueck().get();
        RichtwertzoneComposite richtwertZone = vb.vertrag().get().richtwertZoneBauland().get();
        if (richtwertZone != null) {
            // GemeindeComposite gemeinde = richtwertZone.gemeinde().get();
            // Iterable<RichtwertzoneComposite> iterable =
            // RichtwertzoneComposite.Mixin.findZoneIn( gemeinde );
            // for (RichtwertzoneComposite zone : iterable) {
            String prefix = richtwertZone.schl().get();
            if (prefix.startsWith( "00" )) {
                prefix = "*" + prefix;
            }
            zonen.put( prefix + " - " + richtwertZone.name().get(), richtwertZone );
            // }

            // richtwertZone = flurstueck.richtwertZone().get();
            if (richtwertZone != null) {
                for (RichtwertzoneZeitraumComposite zeitraum : RichtwertzoneZeitraumComposite.Mixin
                        .forZone( richtwertZone )) {
                    zeitraeume.put( KapsRepository.SHORT_DATE.format( zeitraum.gueltigAb().get() ), zeitraum );
                }
            }
        }

        newLine = newFormField( "Richtwertzone" ).setEnabled( false )
                .setProperty( new AssociationAdapter<RichtwertzoneComposite>( vb.richtwertZone() ) )
                .setField( new PicklistFormField( zonen.descendingMap() ) )
                .setLayoutData( left().top( lastLine ).bottom( 100 ).create() ).setParent( client ).create();

        newFormField( "Gültig ab" )
                .setProperty( new AssociationAdapter<RichtwertzoneZeitraumComposite>( vb.richtwertZoneG() ) )
                .setField( new PicklistFormField( zeitraeume.descendingMap() ) ).setEnabled( false )
                .setLayoutData( right().top( lastLine ).create() ).setParent( client ).create();
        pageSite.addFieldListener( richtwertzone = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == IFormFieldListener.VALUE_CHANGE
                        && ev.getFieldName().equals( vb.richtwertZoneG().qualifiedName().name() )) {
                    RichtwertzoneZeitraumComposite rzc = (RichtwertzoneZeitraumComposite)ev.getNewValue();
                    pageSite.setFieldValue(
                            vb.richtwert().qualifiedName().name(),
                            rzc != null && rzc.euroQm().get() != null ? NumberFormatter.getFormatter( 2 ).format(
                                    rzc.euroQm().get() ) : "0" );
                    pageSite.setFieldValue( vb.erschliessungsBeitrag().qualifiedName().name(), rzc != null ? rzc
                            .erschliessungsBeitrag().get() : null );
                }
            }
        } );

        section = newSection( section, "Bodenpreisberechnung" );
        client = (Composite)section.getClient();

        lastLine = newLine;
        newLine = newFormField( "Richtwert" ).setToolTipText( "Richtwert aus Richtwertzone" ).setEnabled( false )
                .setProperty( new PropertyAdapter( vb.richtwert() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) )
                .setLayoutData( left().left( 25 ).top( lastLine ).create() ).setParent( client ).create();

        lastLine = newLine;
        newLine = newFormField( "GFZ-bereinigt" ).setToolTipText( "GFZ bereinigter Bodenpreis" ).setEnabled( false )
                .setProperty( new PropertyAdapter( vb.gfzBereinigterBodenpreis() ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setLayoutData( left().left( 25 ).top( lastLine ).create() ).setParent( client ).create();
        newFormField( "verwenden?" ).setToolTipText( "GFZ bereinigten Bodenpreis verwenden?" )
                .setProperty( new PropertyAdapter( vb.gfzBereinigtenBodenpreisVerwenden() ) )
                .setField( new CheckboxFormField() ).setLayoutData( right().right( 75 ).top( lastLine ).create() )
                .setParent( client ).create();
        newFormField( "GFZ-Bereich" ).setEnabled( false ).setProperty( new PropertyAdapter( vb.gfzBereich() ) )
                .setField( new StringFormField() ).setLayoutData( right().left( 75 ).top( lastLine ).create() )
                .setParent( client ).create();

        lastLine = newLine;
        newLine = newFormField( "Bereinigung in €/m²" ).setToolTipText( "Richtwertbereinigung in €/m² (+/-)" )
                .setProperty( new PropertyAdapter( vb.richtwertBereinigung() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) )
                .setLayoutData( left().left( 25 ).top( lastLine ).create() ).setParent( client ).create();
        newFormField( "Bemerkung" ).setToolTipText( "Bemerkung zur Richwertbereinigung" )
                .setProperty( new PropertyAdapter( vb.richtwertBereinigungBemerkung() ) )
                .setField( new StringFormField() ).setLayoutData( right().top( lastLine ).create() ).setParent( client )
                .create();

        lastLine = newLine;
        newLine = newFormField( "Zuschlag in %" ).setToolTipText( "Richtwertzuschlag in %" )
                .setProperty( new PropertyAdapter( vb.richtwertZuschlagProzent() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) )
                .setLayoutData( left().right( 25 ).top( lastLine ).create() ).setParent( client ).create();
        newLine = newFormField( "in €/m²" ).setEnabled( false )
                .setProperty( new PropertyAdapter( vb.richtwertZuschlagBerechnet() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) )
                .setLayoutData( left().left( 25 ).top( lastLine ).create() ).setParent( client ).create();
        site.addFieldListener( riwezuschlag = new FieldCalculation( site, 2, vb.richtwertZuschlagBerechnet(), vb
                .richtwert(), vb.richtwertBereinigung(), vb.richtwertZuschlagProzent() ) {

            @Override
            protected Double calculate( org.polymap.kaps.ui.FieldCalculation.ValueProvider values ) {
                Double zuschlag = values.get( vb.richtwertZuschlagProzent() );
                if (zuschlag != null) {
                    Double richtwert = values.get( vb.richtwert() );
                    if (richtwert != null) {
                        Double richtwertB = values.get( vb.richtwertBereinigung() );
                        if (richtwertB != null) {
                            richtwert += richtwertB;
                        }
                        return richtwert * zuschlag / 100;
                    }
                }
                return null;
            }
        } );

        newFormField( "Bemerkung" ).setToolTipText( "Bemerkung zum Richtwertzuschlag" )
                .setProperty( new PropertyAdapter( vb.richtwertZuschlagBemerkung() ) ).setField( new StringFormField() )
                .setLayoutData( right().top( lastLine ).create() ).setParent( client ).create();

        lastLine = newLine;
        newLine = newFormField( "Abschlag in %" ).setToolTipText( "Richtwertabschlag in %" )
                .setProperty( new PropertyAdapter( vb.richtwertAbschlagProzent() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) )
                .setLayoutData( left().right( 25 ).top( lastLine ).create() ).setParent( client ).create();
        newLine = newFormField( "in €/m²" ).setEnabled( false )
                .setProperty( new PropertyAdapter( vb.richtwertAbschlagBerechnet() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) )
                .setLayoutData( left().left( 25 ).top( lastLine ).create() ).setParent( client ).create();
        pageSite.addFieldListener( riweabschlag = new FieldCalculation( site, 2, vb.richtwertAbschlagBerechnet(), vb
                .richtwert(), vb.richtwertBereinigung(), vb.richtwertAbschlagProzent() ) {

            @Override
            protected Double calculate( org.polymap.kaps.ui.FieldCalculation.ValueProvider values ) {
                Double abschlag = values.get( vb.richtwertAbschlagProzent() );
                if (abschlag != null) {
                    Double richtwert = values.get( vb.richtwert() );
                    if (richtwert != null) {
                        Double richtwertB = values.get( vb.richtwertBereinigung() );
                        if (richtwertB != null) {
                            richtwert += richtwertB;
                        }
                        return richtwert * abschlag / 100;
                    }
                }
                return null;
            }
        } );

        newFormField( "Bemerkung" ).setToolTipText( "Bemerkung zum Richtwertabschlag" )
                .setProperty( new PropertyAdapter( vb.richtwertAbschlagBemerkung() ) ).setField( new StringFormField() )
                .setLayoutData( right().top( lastLine ).create() ).setParent( client ).create();

        lastLine = newLine;
        newLine = newFormField( "Erschließung in €/m²" )
                .setToolTipText( "Erschließungskosten in €/m² anrechenbarer Grundstücksgröße" )
                .setProperty( new PropertyAdapter( vb.erschliessungsKosten() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) )
                .setLayoutData( left().left( 25 ).top( lastLine ).create() ).setParent( client ).create();

        newFormField( "Bemerkung" ).setToolTipText( "Bemerkung gesamt" )
                .setProperty( new PropertyAdapter( vb.bemerkungen() ) ).setField( new TextFormField() )
                .setLayoutData( right().top( lastLine ).height( 100 ).bottom( 100 ).create() ).setParent( client ).create();
        // lastLine = newLine;
        // newLine = newFormField( "Preis unbebaut in €/m²" ).setToolTipText(
        // "Bodenpreis unbebaut in €/m²" )
        // .setEnabled( false ).setProperty( new PropertyAdapter(
        // vb.bodenpreisUnbebaut() ) )
        // .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
        // .setValidator( new MyNumberValidator( Double.class, 2 ) )
        // .setLayoutData( left().left( 25 ).top( lastLine ).create() ).setParent(
        // client ).create();
        // pageSite.addFieldListener( preisunbebaut = new FieldCalculation( pageSite,
        // 2, vb.bodenpreisUnbebaut(), vb
        // .richtwert(), vb.richtwertBereinigung(), vb.richtwertZuschlagBerechnet(),
        // vb
        // .richtwertAbschlagBerechnet(), vb.erschliessungsKosten() ) {
        //
        // @Override
        // protected Double calculate(
        // org.polymap.kaps.ui.FieldCalculation.ValueProvider values ) {
        // Double richtwert = values.get( vb.richtwert() );
        // if (richtwert != null) {
        // Double richtwertB = values.get( vb.richtwertBereinigung() );
        // if (richtwertB != null) {
        // richtwert += richtwertB;
        // }
        // richtwertB = values.get( vb.richtwertZuschlagBerechnet() );
        // if (richtwertB != null) {
        // richtwert += richtwertB;
        // }
        // richtwertB = values.get( vb.richtwertAbschlagBerechnet() );
        // if (richtwertB != null) {
        // richtwert -= richtwertB;
        // }
        // richtwertB = values.get( vb.erschliessungsKosten() );
        // if (richtwertB != null) {
        // richtwert -= richtwertB;
        // }
        // return richtwert;
        // }
        // return null;
        // }
        // } );

        // lastLine = newLine;
        // newLine = newFormField( "Beb.-abschlag in %" ).setProperty( new
        // PropertyAdapter( vb.bebAbschlag() ) )
        // .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
        // .setValidator( new MyNumberValidator( Double.class, 2 ) )
        // .setLayoutData( left().right( 25 ).top( lastLine ).create() ).setParent(
        // client ).create();
        // newLine = newFormField( "in €/m²" ).setEnabled( false )
        // .setProperty( new PropertyAdapter( vb.bebAbschlagBerechnet() ) )
        // .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
        // .setValidator( new MyNumberValidator( Double.class, 2 ) )
        // .setLayoutData( left().left( 25 ).top( lastLine ).create() ).setParent(
        // client ).create();
        // pageSite.addFieldListener( bebabschlag = new FieldCalculation( pageSite,
        // 2, vb.bebAbschlagBerechnet(), vb
        // .bodenpreisBebaut(), vb.bebAbschlag() ) {
        //
        // @Override
        // protected Double calculate(
        // org.polymap.kaps.ui.FieldCalculation.ValueProvider values ) {
        // Double abschlag = values.get( vb.bebAbschlag() );
        // if (abschlag != null) {
        // Double bodenpreis = values.get( vb.bodenpreisBebaut() );
        // if (bodenpreis != null) {
        // return bodenpreis * abschlag / 100;
        // }
        // }
        // return null;
        // }
        // } );

        lastLine = newLine;
        newLine = newFormField( "Richtwert in €/m²" ).setEnabled( false )
                .setToolTipText( "Bereinigter Richtwert/Bodenwert in €/m²" )
                .setProperty( new PropertyAdapter( vb.bodenpreisBebaut() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) )
                .setLayoutData( left().left( 25 ).top( lastLine ).bottom( 100 ).create() ).setParent( client ).create();
        pageSite.addFieldListener( bodenpreisbebaut = new FieldCalculation( pageSite, 2, vb.bodenpreisBebaut(), vb
                .richtwert(), vb.richtwertBereinigung(), vb.richtwertZuschlagBerechnet(), vb
                .richtwertAbschlagBerechnet(), vb.erschliessungsKosten() ) {

            @Override
            protected Double calculate( org.polymap.kaps.ui.FieldCalculation.ValueProvider values ) {
                Double richtwert = values.get( vb.richtwert() );
                if (richtwert != null) {
                    Double richtwertB = values.get( vb.richtwertBereinigung() );
                    if (richtwertB != null) {
                        richtwert += richtwertB;
                    }
                    richtwertB = values.get( vb.richtwertZuschlagBerechnet() );
                    if (richtwertB != null) {
                        richtwert += richtwertB;
                    }
                    richtwertB = values.get( vb.richtwertAbschlagBerechnet() );
                    if (richtwertB != null) {
                        richtwert -= richtwertB;
                    }
                    richtwertB = values.get( vb.erschliessungsKosten() );
                    if (richtwertB != null) {
                        richtwert -= richtwertB;
                    }
                    return richtwert;
                }
                return null;
            }
        } );

        section = createBewertungenForm( section );
    }


    private Section createBewertungenForm( Composite top ) {

        Section formSection = newSection( top, "Bewertungen" );
        formSection.setExpanded( true );
        Composite parent = (Composite)formSection.getClient();

        openBewertungen = new ActionButton( parent, new Action( "nach NHK 2010 bewerten" ) {

            @Override
            public void run() {
                NHK2010BewertungComposite bewertungComposite = NHK2010BewertungComposite.Mixin.forVertrag( vb.vertrag()
                        .get() );
                if (bewertungComposite == null) {
                    bewertungComposite = repository.newEntity( NHK2010BewertungComposite.class, null );
                    bewertungComposite.vertrag().set( vb.vertrag().get() );
                }
                KapsPlugin.openEditor( fs, NHK2010BewertungComposite.NAME, bewertungComposite );
            }
        } ) {

            @Override
            public void setEnabled( boolean enabled ) {
                if (enabled) {
                    if (NHK2010BewertungComposite.Mixin.forVertrag( vb.vertrag().get() ) != null) {
                        setText( "Bewertung nach NHK 2010 anpassen" );
                    }
                    else {
                        setText( "nach NHK 2010 bewerten" );
                    }
                }
                super.setEnabled( enabled );
            };
        };
        openBewertungen.setLayoutData( left().right( 25 ).height( 25 ).top( null ).bottom( 100 ).create() );
        openBewertungen.setEnabled( true );

        openErtragswert = new ActionButton( parent, new Action( "nach Ertragswertverfahren - normal bewerten" ) {

            @Override
            public void run() {
                Double kaufpreis = vb.vertrag().get().erweiterteVertragsdaten().get().bereinigterVollpreis().get();
                if (kaufpreis == null) {
                    kaufpreis = vb.vertrag().get().kaufpreis().get();
                }
                if (kaufpreis == null || isDirty()) {
                    MessageDialog.openError( PolymapWorkbench.getShellToParentOn(), "Fehlende Daten",
                            "Bitte geben Sie den Kaufpreis ein und speichern Sie den Vertrag, bevor Sie diese Berechnung starten." );
                }
                else {
                    ErtragswertverfahrenComposite bewertungComposite = ErtragswertverfahrenComposite.Mixin
                            .forVertrag( vb.vertrag().get() );
                    if (bewertungComposite == null) {
                        bewertungComposite = repository.newEntity( ErtragswertverfahrenComposite.class, null );
                        bewertungComposite.vertrag().set( vb.vertrag().get() );
                    }
                    FormEditor targetEditor = KapsPlugin.openEditor( fs, ErtragswertverfahrenComposite.NAME,
                            bewertungComposite );
                    EventManager.instance().publish(
                            new InterEditorPropertyChangeEvent( formEditor, targetEditor, bewertungComposite,
                                    bewertungComposite.bereinigterKaufpreis().qualifiedName().name(),
                                    bewertungComposite.bereinigterKaufpreis().get(), kaufpreis ) );
                    // Bodenwertanteil übergeben, sind gespeichert also keine
                    // FieldListener einsetzen
                    Double bodenwertAnteil = 0.0d;
                    // for (FlurstueckComposite flurstueck :
                    // FlurstueckComposite.Mixin.forEntity( kaufvertrag )) {
                    VertragsdatenBaulandComposite bauland = VertragsdatenBaulandComposite.Mixin.forVertrag( vb
                            .vertrag().get() );
                    if (bauland != null && bauland.bodenwertGesamt().get() != null) {
                        bodenwertAnteil += bauland.bodenwertGesamt().get();
                    }
                    // }
                    EventManager.instance().publish(
                            new InterEditorPropertyChangeEvent( formEditor, targetEditor, bewertungComposite,
                                    bewertungComposite.bodenwertAnteil().qualifiedName().name(), bewertungComposite
                                            .bodenwertAnteil().get(), bodenwertAnteil ) );
                }
            }
        } ) {

            @Override
            public void setEnabled( boolean enabled ) {
                if (enabled) {
                    if (ErtragswertverfahrenComposite.Mixin.forVertrag( vb.vertrag().get() ) != null) {
                        setText( "Bewertung nach Ertragswertverfahren - normal anpassen" );
                    }
                    else {
                        setText( "nach Ertragswertverfahren - normal bewerten" );
                    }
                }
                super.setEnabled( enabled );
            };
        };
        openErtragswert.setLayoutData( left().left( openBewertungen, 5 ).width( 25 ).height( 25 ).top( null )
                .bottom( 100 ).create() );
        openErtragswert.setEnabled( true );
        return formSection;
    }


    @Override
    public void afterDoLoad( IProgressMonitor monitor )
            throws Exception {
        super.afterDoLoad( monitor );

        if (vb.richtwertZoneG().get() == null) {
            // set the default zone from flurstueck
            RichtwertzoneComposite zone = vb.vertrag().get().richtwertZoneBauland().get();
            RichtwertzoneZeitraumComposite zeitraum = RichtwertzoneZeitraumComposite.Mixin.findZeitraumFor( zone, vb
                    .vertrag().get().vertragsDatum().get() );
            // vb.richtwertZone().set( zone );
            // vb.richtwertZoneG().set( zeitraum );
            // pageSite.fireEvent( this, vb.richtwertZoneG().qualifiedName().name(),
            // IFormFieldListener.VALUE_CHANGE,
            // zeitraum );
            pageSite.setFieldValue( vb.richtwertZone().qualifiedName().name(), zone );
            if (zeitraum != null) {
                pageSite.setFieldValue( vb.richtwertZoneG().qualifiedName().name(), zeitraum );
            }
        }
        else {
            // trigger recalculate
            pageSite.fireEvent( this, vb.richtwertZoneG().qualifiedName().name(), IFormFieldListener.VALUE_CHANGE, vb
                    .richtwertZoneG().get() );
        }
    }
}
