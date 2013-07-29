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

import java.util.SortedMap;
import java.util.TreeMap;

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;
import org.opengis.feature.type.PropertyDescriptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.entity.association.Association;
import org.qi4j.api.property.Property;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.action.Action;

import org.eclipse.ui.forms.widgets.Section;

import org.polymap.core.data.ui.featuretable.DefaultFeatureTableColumn;
import org.polymap.core.data.ui.featuretable.FeatureTableViewer;
import org.polymap.core.model.EntityType;
import org.polymap.core.qi4j.QiModule.EntityCreator;
import org.polymap.core.runtime.Polymap;

import org.polymap.rhei.data.entityfeature.PropertyDescriptorAdapter;
import org.polymap.rhei.data.entityfeature.ReloadablePropertyAdapter;
import org.polymap.rhei.data.entityfeature.ReloadablePropertyAdapter.AssociationCallback;
import org.polymap.rhei.data.entityfeature.ReloadablePropertyAdapter.PropertyCallback;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.NumberValidator;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.KapsPlugin;
import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.data.ArtDesBaugebietsComposite;
import org.polymap.kaps.model.data.BelastungComposite;
import org.polymap.kaps.model.data.FlurComposite;
import org.polymap.kaps.model.data.FlurstueckComposite;
import org.polymap.kaps.model.data.FlurstuecksdatenAgrarComposite;
import org.polymap.kaps.model.data.FlurstuecksdatenBaulandComposite;
import org.polymap.kaps.model.data.GebaeudeArtComposite;
import org.polymap.kaps.model.data.GebaeudeComposite;
import org.polymap.kaps.model.data.GemarkungComposite;
import org.polymap.kaps.model.data.GemeindeComposite;
import org.polymap.kaps.model.data.NutzungComposite;
import org.polymap.kaps.model.data.RichtwertzoneComposite;
import org.polymap.kaps.model.data.StrasseComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.model.data.WohnungComposite;
import org.polymap.kaps.ui.ActionButton;
import org.polymap.kaps.ui.BooleanFormField;
import org.polymap.kaps.ui.KapsDefaultFormEditorPageWithFeatureTable;
import org.polymap.kaps.ui.SimplePickList;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class KaufvertragFlurstueckeFormEditorPage
        extends KapsDefaultFormEditorPageWithFeatureTable<FlurstueckComposite> {

    private static Log                log    = LogFactory.getLog( KaufvertragFlurstueckeFormEditorPage.class );

    private VertragComposite          kaufvertrag;

    private final static String       prefix = KaufvertragFlurstueckeFormEditorPage.class.getSimpleName();

    private FlurstueckSearcher        sfAction;

    private VerkaufteFlaecheRefresher verkaufteFlaecheRefresher;

    private GemarkungComposite        selectedGemarkung;

    private NutzungComposite          selectedNutzung;

    private IFormFieldListener        gemarkungListener;

    private IFormFieldListener        nutzungListener;

    private ActionButton              searchFlurstueckeButton;

    private StrasseComposite          selectedStrasse;

    private RichtwertzoneComposite    selectedRichtwertzone;

    private ArtDesBaugebietsComposite selectedArtDesBaugebietes;


    public KaufvertragFlurstueckeFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( KaufvertragFlurstueckeFormEditorPage.class.getName(), "Flurstücke", feature, featureStore );

        kaufvertrag = repository.findEntity( VertragComposite.class, feature.getIdentifier().getID() );
    }


    @Override
    public void createFormContent( IFormEditorPageSite site ) {
        super.createFormContent( site );

        site.setEditorTitle( formattedTitle( "Kaufvertrag", kaufvertrag.eingangsNr().get(), null ) );
        site.setFormTitle( formattedTitle( "Kaufvertrag", kaufvertrag.eingangsNr().get(), getTitle() ) );

        Composite parent = site.getPageBody();
        Composite schildForm = createFlurstueckForm( parent );

        Composite extendedForm = createErweiterteDatenForm( schildForm );

        Section tableSection = newSection( extendedForm, "Auswahl" );
        createTableForm( (Composite)tableSection.getClient(), extendedForm, true );
    }


    protected void refreshReloadables()
            throws Exception {
        FlurstueckComposite composite = selectedComposite.get();
        selectedGemarkung = composite != null ? composite.gemarkung().get() : null;
        selectedStrasse = composite != null ? composite.strasse().get() : null;
        selectedRichtwertzone = composite != null ? composite.richtwertZone().get() : null;
        selectedNutzung = composite != null ? composite.nutzung().get() : null;
        selectedArtDesBaugebietes = composite != null ? composite.artDesBaugebiets().get() : null;

        super.refreshReloadables();
        if (sfAction != null) {
            sfAction.refresh();
            searchFlurstueckeButton.setEnabled( composite != null );
            openErweiterteDatenAgrar.setEnabled( composite != null );
            openErweiterteDatenBauland.setEnabled( composite != null );
            wohnungPicklist.setEnabled( composite != null );
            createWohnung.setEnabled( composite != null );
            searchWohnung.setEnabled( composite != null );
        }
        // das muss disabled bleiben
        pageSite.setFieldEnabled( prefix + "verkaufteFlaeche", false );
    }


    public Composite createFlurstueckForm( Composite parent ) {

        Section formSection = newSection( parent, "Flurstücksdaten" );
        parent = (Composite)formSection.getClient();

        Composite line0 = newFormField( "Gemarkung" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite, prefix + "gemarkung",
                                new AssociationCallback<FlurstueckComposite>() {

                                    public Association get( FlurstueckComposite entity ) {
                                        return entity.gemarkung();
                                    }
                                } ) )
                .setField( reloadable( namedAssocationsPicklist( GemarkungComposite.class, true ) ) )
                .setLayoutData( left().create() ).create();

        newFormField( "Flur" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite, prefix + "flur",
                                new AssociationCallback<FlurstueckComposite>() {

                                    public Association get( FlurstueckComposite entity ) {
                                        return entity.flur();
                                    }
                                } ) ).setField( reloadable( namedAssocationsPicklist( FlurComposite.class, false ) ) )
                .setLayoutData( right().create() ).create();

        Control line1 = newFormField( "Flurstücksnummer" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite, prefix + "nummer",
                                new PropertyCallback<FlurstueckComposite>() {

                                    public Property get( FlurstueckComposite entity ) {
                                        return entity.nummer();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setLayoutData( left().right( 30 ).top( line0 ).create() )
                .setValidator( new NumberValidator( Integer.class, locale ) ).create();

        newFormField( "/" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite, prefix + "unterNummer",
                                new PropertyCallback<FlurstueckComposite>() {

                                    public Property get( FlurstueckComposite entity ) {
                                        return entity.unterNummer();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setLayoutData( left().left( 30 ).right( 50 ).top( line0 ).create() ).create();

        // newFormField( "Hauptflurstück" )
        // .setParent( parent )
        // .setProperty(
        // new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite,
        // prefix + "hauptFlurstueck",
        // new PropertyCallback<FlurstueckComposite>() {
        //
        // public Property get( FlurstueckComposite entity ) {
        // return entity.hauptFlurstueck();
        // }
        // } ) ).setField( reloadable( new CheckboxFormField() ) )
        // .setLayoutData( right().top( line0 ).create() ).create();

        // BUTTON zur Datenabfrage
        sfAction = new FlurstueckSearcher( prefix ) {

            protected void adopt( FlurstueckComposite toAdopt )
                    throws Exception {
                assert toAdopt != null;
                FlurstueckComposite current = selectedComposite.get();
                // separate behandeln, da die 3 felder von zusätzlichen reloads von
                // gemakrung und nutzung
                // wieder zurückgesetzt werden
                selectedStrasse = toAdopt.strasse().get();
                selectedRichtwertzone = toAdopt.richtwertZone().get();
                selectedArtDesBaugebietes = toAdopt.artDesBaugebiets().get();

                pageSite.setFieldValue( prefix + "gemarkung", toAdopt.gemarkung().get() );
                // current.gemarkung().set( toAdopt.gemarkung().get() );
                pageSite.setFieldValue( prefix + "flur", toAdopt.flur().get() );
                pageSite.setFieldValue( prefix + "nummer", String.valueOf( toAdopt.nummer().get() ) );
                pageSite.setFieldValue( prefix + "unterNummer", toAdopt.unterNummer().get() );
                pageSite.setFieldValue( prefix + "strasse", toAdopt.strasse().get() );
                pageSite.setFieldValue( prefix + "hausnummer", toAdopt.hausnummer().get() );
                pageSite.setFieldValue( prefix + "hausnummerZusatz", toAdopt.hausnummerZusatz().get() );
                pageSite.setFieldValue( prefix + "richtwertZone", toAdopt.richtwertZone().get() );
                // pageSite.setFieldValue( prefix + "kartenBlatt",
                // toAdopt.kartenBlatt().get() );
                // pageSite.setFieldValue( prefix + "baublock",
                // toAdopt.baublock().get() );
                pageSite.setFieldValue( prefix + "nutzung", toAdopt.nutzung().get() );
                pageSite.setFieldValue( prefix + "gebaeudeArt", toAdopt.gebaeudeArt().get() );
                pageSite.setFieldValue( prefix + "artDesBaugebiets", toAdopt.artDesBaugebiets().get() );
                pageSite.setFieldValue( prefix + "flaeche", String.valueOf( toAdopt.flaeche().get() ) );
                pageSite.setFieldValue( prefix + "flaechenAnteilZaehler",
                        String.valueOf( toAdopt.flaechenAnteilZaehler().get() ) );
                pageSite.setFieldValue( prefix + "flaechenAnteilNenner",
                        String.valueOf( toAdopt.flaechenAnteilNenner().get() ) );
                pageSite.setFieldValue( prefix + "verkaufteFlaeche", String.valueOf( toAdopt.verkaufteFlaeche().get() ) );
                pageSite.setFieldValue( prefix + "erbbaurecht", toAdopt.erbbaurecht().get() );
                pageSite.setFieldValue( prefix + "belastung", toAdopt.belastung().get() );

                // refreshReloadables();
            }
        };
        sfAction.setEnabled( false );
        searchFlurstueckeButton = new ActionButton( parent, sfAction );
        searchFlurstueckeButton.setLayoutData( right().right( 70 ).height( 25 ).top( line0 ).create() );
        searchFlurstueckeButton.setEnabled( false );
        line1 = searchFlurstueckeButton;
        pageSite.addFieldListener( sfAction );

        final PicklistFormField strassePickList = new PicklistFormField( new PicklistFormField.ValueProvider() {

            @Override
            public SortedMap<String, Object> get() {
                SortedMap<String, Object> strassen = new TreeMap<String, Object>();

                if (selectedGemarkung != null) {
                    GemeindeComposite gemeinde = selectedGemarkung.gemeinde().get();
                    Iterable<StrasseComposite> iterable = StrasseComposite.Mixin.findStrasseIn( gemeinde );
                    for (StrasseComposite strasse : iterable) {
                        strassen.put( strasse.name().get(), strasse );
                    }
                }

                return strassen;
            }
        } );

        Composite line2 = newFormField( "Straße/Gewann" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite, prefix + "strasse",
                                new AssociationCallback<FlurstueckComposite>() {

                                    public Association get( FlurstueckComposite entity ) {
                                        return entity.strasse();
                                    }
                                } ) ).setField( reloadable( strassePickList ) )
                .setLayoutData( left().top( line1 ).create() ).create();

        newFormField( "Hausnummer" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite, prefix + "hausnummer",
                                new PropertyCallback<FlurstueckComposite>() {

                                    public Property<String> get( FlurstueckComposite entity ) {
                                        return entity.hausnummer();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setLayoutData( right().right( 75 ).top( line1 ).create() ).create();

        newFormField( "Zusatz" )
                .setToolTipText( "Hausnummernzusatz" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite, prefix
                                + "hausnummerZusatz", new PropertyCallback<FlurstueckComposite>() {

                            public Property<String> get( FlurstueckComposite entity ) {
                                return entity.hausnummerZusatz();
                            }
                        } ) ).setField( reloadable( new StringFormField() ) )
                .setLayoutData( right().left( 75 ).top( line1 ).create() ).create();

        final PicklistFormField richtwertZonePickList = new PicklistFormField( new PicklistFormField.ValueProvider() {

            @Override
            public SortedMap<String, Object> get() {
                TreeMap<String, Object> zonen = new TreeMap<String, Object>();
                if (selectedGemarkung != null) {
                    GemeindeComposite gemeinde = selectedGemarkung.gemeinde().get();
                    Iterable<RichtwertzoneComposite> iterable = RichtwertzoneComposite.Mixin.findZoneIn( gemeinde );
                    for (RichtwertzoneComposite zone : iterable) {
                        String prefix = zone.schl().get();
                        if (prefix.startsWith( "00" )) {
                            prefix = "*" + prefix;
                        }
                        zonen.put( prefix + " - " + zone.name().get(), zone );
                    }
                }
                return zonen.descendingMap();
            }
        } );
        Composite line3 = newFormField( "Richtwertzone" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite,
                                prefix + "richtwertZone", new AssociationCallback<FlurstueckComposite>() {

                                    public Association<RichtwertzoneComposite> get( FlurstueckComposite entity ) {
                                        return entity.richtwertZone();
                                    }
                                } ) ).setField( reloadable( richtwertZonePickList ) )
                .setLayoutData( left().top( line2 ).create() ).create();

        pageSite.addFieldListener( gemarkungListener = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == VALUE_CHANGE && ev.getFieldName().equalsIgnoreCase( prefix + "gemarkung" )) {
                    if ((ev.getNewValue() == null && selectedGemarkung != null)
                            || (ev.getNewValue() != null && !ev.getNewValue().equals( selectedGemarkung ))) {
                        selectedGemarkung = ev.getNewValue();
                        // strassePickList.
                        strassePickList.reloadValues();
                        pageSite.setFieldValue( prefix + "strasse", selectedStrasse );
                        richtwertZonePickList.reloadValues();
                        pageSite.setFieldValue( prefix + "richtwertZone", selectedRichtwertzone );
                        // selectedComposite.get() != null ?
                        // selectedComposite.get().richtwertZone().get() : null );
                    }
                }
            }
        } );

        // newFormField( "Kartenblatt" )
        // .setParent( parent )
        // .setProperty(
        // new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite,
        // prefix + "kartenBlatt",
        // new PropertyCallback<FlurstueckComposite>() {
        //
        // public Property get( FlurstueckComposite entity ) {
        // return entity.kartenBlatt();
        // }
        // } ) ).setField( reloadable( new StringFormField() ) )
        // .setLayoutData( right().right( 75 ).top( line2 ).create() ).create();
        //
        // newFormField( "Baublock" )
        // .setParent( parent )
        // .setProperty(
        // new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite,
        // prefix + "baublock", new PropertyCallback<FlurstueckComposite>() {
        //
        // public Property get( FlurstueckComposite entity ) {
        // return entity.baublock();
        // }
        // } ) ).setField( reloadable( new StringFormField() ) )
        // .setLayoutData( right().left( 75 ).top( line2 ).create() ).create();

        Composite line4 = newFormField( "Nutzung" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite, prefix + "nutzung",
                                new AssociationCallback<FlurstueckComposite>() {

                                    public Association get( FlurstueckComposite entity ) {
                                        return entity.nutzung();
                                    }
                                } ) ).setField( reloadable( namedAssocationsPicklist( NutzungComposite.class ) ) )
                .setLayoutData( left().top( line3 ).create() ).create();

        newFormField( "Gebäudeart" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite, prefix + "gebaeudeArt",
                                new AssociationCallback<FlurstueckComposite>() {

                                    public Association get( FlurstueckComposite entity ) {
                                        return entity.gebaeudeArt();
                                    }
                                } ) ).setField( reloadable( namedAssocationsPicklist( GebaeudeArtComposite.class ) ) )
                .setLayoutData( right().top( line3 ).create() ).create();

        final PicklistFormField artPicklist = new PicklistFormField( new PicklistFormField.ValueProvider() {

            @Override
            public SortedMap<String, Object> get() {
                SortedMap<String, Object> values = new TreeMap<String, Object>();
                if (selectedNutzung != null) {
                    Iterable<ArtDesBaugebietsComposite> iterable = ArtDesBaugebietsComposite.Mixin
                            .findByAgrar( selectedNutzung.isAgrar().get() );
                    for (ArtDesBaugebietsComposite zone : iterable) {
                        values.put( zone.schl().get() + " - " + zone.name().get(), zone );
                    }
                }
                return values;
            }
        } );
        pageSite.addFieldListener( nutzungListener = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == VALUE_CHANGE && ev.getFieldName().equalsIgnoreCase( prefix + "nutzung" )) {
                    if ((ev.getNewValue() == null && selectedNutzung != null)
                            || (ev.getNewValue() != null && !ev.getNewValue().equals( selectedNutzung ))) {
                        selectedNutzung = ev.getNewValue();
                        artPicklist.reloadValues();
                        pageSite.setFieldValue( prefix + "artDesBaugebiets", selectedArtDesBaugebietes );
                    }
                }
            }
        } );

        Composite line5 = newFormField( "Art" )
                .setToolTipText( "Art des Grundstücks bei Agrarland, Art des Baugebietes sonst" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite, prefix
                                + "artDesBaugebiets", new AssociationCallback<FlurstueckComposite>() {

                            public Association get( FlurstueckComposite entity ) {
                                return entity.artDesBaugebiets();
                            }
                        } ) ).setField( reloadable( artPicklist ) ).setLayoutData( left().top( line4 ).create() )
                .create();

        Composite line6 = newFormField( "Fläche in m²" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite, prefix + "flaeche",
                                new PropertyCallback<FlurstueckComposite>() {

                                    public Property get( FlurstueckComposite entity ) {
                                        return entity.flaeche();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 2, 1, 2 ) )
                .setLayoutData( left().right( 25 ).top( line5 ).create() ).create();

        newFormField( "Anteil" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite, prefix
                                + "flaechenAnteilZaehler", new PropertyCallback<FlurstueckComposite>() {

                            public Property get( FlurstueckComposite entity ) {
                                return entity.flaechenAnteilZaehler();
                            }
                        } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 2, 1, 2 ) )
                .setLayoutData( left().left( 25 ).right( 50 ).top( line5 ).create() ).create();

        newFormField( "/" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite, prefix
                                + "flaechenAnteilNenner", new PropertyCallback<FlurstueckComposite>() {

                            public Property get( FlurstueckComposite entity ) {
                                return entity.flaechenAnteilNenner();
                            }
                        } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 2, 1, 2 ) )
                .setLayoutData( left().left( 50 ).right( 75 ).top( line5 ).create() ).create();

        newFormField( "verkaufte Fläche in m²" )
                .setParent( parent )
                .setEnabled( false )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite, prefix
                                + "verkaufteFlaeche", new PropertyCallback<FlurstueckComposite>() {

                            public Property get( FlurstueckComposite entity ) {
                                return entity.verkaufteFlaeche();
                            }
                        } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 2, 1, 2 ) )
                .setLayoutData( left().left( 75 ).right( 100 ).top( line5 ).create() ).create();

        pageSite.addFieldListener( verkaufteFlaecheRefresher = new VerkaufteFlaecheRefresher( pageSite,
                selectedComposite, prefix ) );

        Composite line7 = newFormField( "Erbbaurecht" )
                .setEnabled( true )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite, prefix + "erbbaurecht",
                                new PropertyCallback<FlurstueckComposite>() {

                                    public Property get( FlurstueckComposite entity ) {
                                        return entity.erbbaurecht();
                                    }
                                } ) ).setField( reloadable( new BooleanFormField() ) )
                .setLayoutData( left().top( line6 ).bottom( 100 ).create() ).create();

        newFormField( "Belastung" )
                // .setToolTipText(
                // "Art des Grundstücks bei Agrarland, Art des Baugebietes sonst" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite, prefix + "belastung",
                                new AssociationCallback<FlurstueckComposite>() {

                                    public Association get( FlurstueckComposite entity ) {
                                        return entity.belastung();
                                    }
                                } ) ).setField( namedAssocationsPicklist( BelastungComposite.class ) )
                .setLayoutData( right().top( line6 ).create() ).create();

        // return the last line
        return formSection;
    }

    private ActionButton                     openErweiterteDatenAgrar;

    private ActionButton                     openErweiterteDatenBauland;

    private ActionButton                     createWohnung;

    private ActionButton                     searchWohnung;

    private SimplePickList<WohnungComposite> wohnungPicklist;


    public Section createErweiterteDatenForm( Composite top ) {

        Section formSection = newSection( top, "Erweiterte Daten" );
        formSection.setExpanded( false );
        Composite parent = (Composite)formSection.getClient();

        openErweiterteDatenAgrar = new ActionButton( parent, new Action( "Daten Agrarland anlegen" ) {

            @Override
            public void run() {
                FlurstueckComposite flurstueck = selectedComposite.get();
                if (flurstueck != null) {
                    FlurstuecksdatenAgrarComposite agrar = FlurstuecksdatenAgrarComposite.Mixin
                            .forFlurstueck( flurstueck );
                    if (agrar == null) {
                        agrar = repository.newEntity( FlurstuecksdatenAgrarComposite.class, null );
                        agrar.flurstueck().set( flurstueck );
                        agrar.vertrag().set( flurstueck.vertrag().get() );
                        // agrar.richtwertZone1().set(
                        // flurstueck.richtwertZone().get() );
                    }
                    KapsPlugin.openEditor( fs, FlurstuecksdatenAgrarComposite.NAME, agrar );
                }
            }

        } ) {

            @Override
            public void setEnabled( boolean enabled ) {
                if (enabled) {
                    FlurstueckComposite flurstueck = selectedComposite.get();
                    if (flurstueck != null && FlurstuecksdatenAgrarComposite.Mixin.forFlurstueck( flurstueck ) != null) {
                        setText( "Daten Agrarland bearbeiten" );
                    }
                    else {
                        setText( "Daten Agrarland anlegen" );
                    }
                }
                super.setEnabled( enabled );
            };
        };
        openErweiterteDatenAgrar.setLayoutData( left().right( 15 ).height( 25 ).top( null ).bottom( 100 ).create() );
        openErweiterteDatenAgrar.setEnabled( false );

        openErweiterteDatenBauland = new ActionButton( parent, new Action( "Daten Bauland anlegen" ) {

            @Override
            public void run() {
                FlurstueckComposite flurstueck = selectedComposite.get();
                if (flurstueck != null) {
                    FlurstuecksdatenBaulandComposite bauland = FlurstuecksdatenBaulandComposite.Mixin
                            .forFlurstueck( flurstueck );
                    if (bauland == null) {
                        bauland = repository.newEntity( FlurstuecksdatenBaulandComposite.class, null );
                        bauland.flurstueck().set( flurstueck );
                        bauland.vertrag().set( flurstueck.vertrag().get() );
                        bauland.richtwertZone().set( flurstueck.richtwertZone().get() );
                    }
                    KapsPlugin.openEditor( fs, FlurstuecksdatenBaulandComposite.NAME, bauland );
                }
            }

        } ) {

            @Override
            public void setEnabled( boolean enabled ) {
                if (enabled) {
                    FlurstueckComposite flurstueck = selectedComposite.get();
                    if (flurstueck != null
                            && FlurstuecksdatenBaulandComposite.Mixin.forFlurstueck( flurstueck ) != null) {
                        setText( "Daten Bauland bearbeiten" );
                    }
                    else {
                        setText( "Daten Bauland anlegen" );
                    }
                }
                super.setEnabled( enabled );
            };
        };
        openErweiterteDatenBauland.setLayoutData( left().left( 17 ).right( 32 ).height( 25 ).top( null ).create() );
        openErweiterteDatenBauland.setEnabled( false );

        final ActionButton openWohnung = new ActionButton( parent, new Action( "Wohnung bearbeiten" ) {

            @Override
            public void run() {
                WohnungComposite wohnung = wohnungPicklist.getSelection();
                if (wohnung != null) {
                    KapsPlugin.openEditor( fs, WohnungComposite.NAME, wohnung );
                }
            }

        } );
        openWohnung.setLayoutData( left().left( 55 ).right( 68 ).height( 25 ).top( null ).create() );
        openWohnung.setEnabled( false );

        // Liste mit Wohnung + Auswählen daneben
        wohnungPicklist = new SimplePickList<WohnungComposite>( parent, pageSite ) {

            @Override
            public SortedMap<String, WohnungComposite> getValues() {
                SortedMap<String, WohnungComposite> values = new TreeMap<String, WohnungComposite>();
                if (selectedComposite != null) {
                    Iterable<WohnungComposite> iterable = WohnungComposite.Mixin.findWohnungenFor( selectedComposite
                            .get() );
                    for (WohnungComposite zone : iterable) {
                        values.put( zone.schl().get(), zone );
                    }
                }
                return values;
            }


            @Override
            public void onSelection( WohnungComposite selectedObject ) {
                if (openWohnung != null) {
                    openWohnung.setEnabled( selectedObject != null );
                }
            }
        };
        wohnungPicklist.setLayoutData( right().left( 40 ).right( 55 ).height( 25 ).top( null ).create() );

        createWohnung = new ActionButton( parent, new Action( "Wohnung anlegen" ) {

            @Override
            public void run() {
                final FlurstueckComposite flurstueck = selectedComposite.get();
                if (flurstueck != null) {
                    WohnungComposite wohnung = repository.newEntity( WohnungComposite.class, null );
                    wohnung.flurstueck().set( flurstueck );
                    // wohnung.vertrag().set( flurstueck.vertrag().get() );
                    KapsPlugin.openEditor( fs, WohnungComposite.NAME, wohnung );
                }
            }

        } );
        createWohnung.setLayoutData( left().left( 71 ).right( 85 ).height( 25 ).top( null ).create() );
        createWohnung.setEnabled( false );

        searchWohnung = new ActionButton( parent, new WohnungSearcher() {

            @Override
            protected void adopt( final WohnungComposite wohnung )
                    throws Exception {
                if (wohnung != null) {
                    final FlurstueckComposite flurstueckComposite = selectedComposite.get();
                    queue( new UpdateCommand() {
                        @Override
                        public void execute() {
                            // Adapter der erst beim speichern der
                            // Seite ausgeführt wird
                            wohnung.flurstueck().set( flurstueckComposite );
                            if (wohnung.gebaeudeNummer().get() != null) {
                                GebaeudeComposite gebaeude = GebaeudeComposite.Mixin.forKeys( wohnung.objektNummer()
                                        .get(), wohnung.objektFortfuehrung().get(), wohnung.gebaeudeNummer().get(),
                                        wohnung.gebaeudeFortfuehrung().get() );
                                if (gebaeude != null && !gebaeude.flurstuecke().contains( flurstueckComposite )) {
                                    gebaeude.flurstuecke().add( flurstueckComposite );
                                }
                            }
                        }
                    } );
                }
            }
        } );
        searchWohnung.setLayoutData( left().left( 87 ).right( 100 ).height( 25 ).top( null ).create() );
        searchWohnung.setEnabled( false );
        //
        // wohnungPicklist.addSelectionListener( new Selectio )
        // wohnungPicklist.setLayoutData( right().right( 70 ).height( 25 ).top( null
        // ).create() );

        // und daneben Knopf zum Wohnung anlegen

        return formSection;
    }


    protected EntityType<FlurstueckComposite> addViewerColumns( FeatureTableViewer viewer ) {
        // entity types
        final KapsRepository repo = KapsRepository.instance();
        final EntityType<FlurstueckComposite> type = repo.entityType( FlurstueckComposite.class );

        PropertyDescriptor prop = null;
        prop = new PropertyDescriptorAdapter( type.getProperty( "gemarkung" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Gemarkung" ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "flur" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Flur" ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "nummer" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Flurstück" ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "unterNummer" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Unternummer" ) );
        // prop = new PropertyDescriptorAdapter( type.getProperty( "hauptFlurstueck"
        // ) );
        // viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader(
        // "Hauptflurstück" ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "strasse" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Straße" ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "hausnummer" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Hausnummer" ) );

        return type;
    }


    public Iterable<FlurstueckComposite> getElements() {
        return FlurstueckComposite.Mixin.forEntity( kaufvertrag );
    }


    @Override
    protected FlurstueckComposite createNewComposite()
            throws Exception {
        return repository.newEntity( FlurstueckComposite.class, null, new EntityCreator<FlurstueckComposite>() {

            public void create( FlurstueckComposite prototype )
                    throws Exception {
                prototype.vertrag().set( kaufvertrag );
                prototype.flaechenAnteilZaehler().set( 1.0d );
                prototype.flaechenAnteilNenner().set( 1.0d );
            }
        } );
    }
}