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
import org.polymap.kaps.model.data.FlurComposite;
import org.polymap.kaps.model.data.FlurstueckComposite;
import org.polymap.kaps.model.data.FlurstueckVerkaufComposite;
import org.polymap.kaps.model.data.FlurstuecksdatenAgrarComposite;
import org.polymap.kaps.model.data.FlurstuecksdatenBaulandComposite;
import org.polymap.kaps.model.data.GebaeudeArtComposite;
import org.polymap.kaps.model.data.GemarkungComposite;
import org.polymap.kaps.model.data.GemeindeComposite;
import org.polymap.kaps.model.data.NutzungComposite;
import org.polymap.kaps.model.data.RichtwertzoneComposite;
import org.polymap.kaps.model.data.StrasseComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.ui.ActionButton;
import org.polymap.kaps.ui.BooleanFormField;
import org.polymap.kaps.ui.KapsDefaultFormEditorPageWithFeatureTable;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class KaufvertragFlurstueckeFormEditorPage
        extends KapsDefaultFormEditorPageWithFeatureTable<FlurstueckVerkaufComposite> {

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

    private ActionButton              openErweiterteDaten;


    public KaufvertragFlurstueckeFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( KaufvertragFlurstueckeFormEditorPage.class.getName(), "Flurstücksdaten", feature, featureStore );

        kaufvertrag = repository.findEntity( VertragComposite.class, feature.getIdentifier().getID() );
    }


    @Override
    public void createFormContent( IFormEditorPageSite site ) {
        super.createFormContent( site );

        site.setEditorTitle( formattedTitle( "Kaufvertrag", kaufvertrag.eingangsNr().get(), null ) );
        site.setFormTitle( formattedTitle( "Kaufvertrag", kaufvertrag.eingangsNr().get(), getTitle() ) );

        Composite parent = site.getPageBody();
        Control schildForm = createFlurstueckForm( parent );
        createTableForm( parent, schildForm, true );
    }


    protected void refreshReloadables()
            throws Exception {
        FlurstueckVerkaufComposite composite = selectedComposite.get();
        selectedGemarkung = composite != null ? composite.flurstueck().get().gemarkung().get() : null;

        selectedNutzung = composite != null ? composite.nutzung().get() : null;

        super.refreshReloadables();
        if (sfAction != null) {
            sfAction.refresh();
            searchFlurstueckeButton.setEnabled( composite != null );
            openErweiterteDaten.setEnabled( composite != null );
        }
        // das muss disabled bleiben
        pageSite.setFieldEnabled( prefix + "verkaufteFlaeche", false );
    }


    public boolean isValid() {
        return true;
    }


    public Control createFlurstueckForm( Composite parent ) {

        Composite line0 = newFormField( "Gemarkung" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckVerkaufComposite>( selectedComposite, prefix
                                + "gemarkung", new AssociationCallback<FlurstueckVerkaufComposite>() {

                            public Association get( FlurstueckVerkaufComposite entity ) {
                                return entity.flurstueck().get().gemarkung();
                            }
                        } ) ).setField( reloadable( namedAssocationsPicklist( GemarkungComposite.class, true ) ) )
                .setLayoutData( left().create() ).create();

        newFormField( "Flur" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckVerkaufComposite>( selectedComposite, prefix + "flur",
                                new AssociationCallback<FlurstueckVerkaufComposite>() {

                                    public Association get( FlurstueckVerkaufComposite entity ) {
                                        return entity.flurstueck().get().flur();
                                    }
                                } ) ).setField( reloadable( namedAssocationsPicklist( FlurComposite.class, false ) ) )
                .setLayoutData( right().create() ).create();

        Control line1 = newFormField( "Flurstücksnummer" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckVerkaufComposite>( selectedComposite,
                                prefix + "nummer", new PropertyCallback<FlurstueckVerkaufComposite>() {

                                    public Property get( FlurstueckVerkaufComposite entity ) {
                                        return entity.flurstueck().get().nummer();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setLayoutData( left().right( 30 ).top( line0 ).create() )
                .setValidator( new NumberValidator( Integer.class, locale ) ).create();

        newFormField( "/" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckVerkaufComposite>( selectedComposite, prefix
                                + "unterNummer", new PropertyCallback<FlurstueckVerkaufComposite>() {

                            public Property get( FlurstueckVerkaufComposite entity ) {
                                return entity.flurstueck().get().unterNummer();
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

            protected void adopt( FlurstueckVerkaufComposite toAdopt )
                    throws Exception {
                assert toAdopt != null;
                FlurstueckVerkaufComposite current = selectedComposite.get();
//                current.gemarkung().set( toAdopt.gemarkung().get() );
//                current.flur().set( toAdopt.flur().get() );
//                current.nummer().set( toAdopt.nummer().get() );
//                current.unterNummer().set( toAdopt.unterNummer().get() );
                current.strasse().set( toAdopt.strasse().get() );
                current.hausnummer().set( toAdopt.hausnummer().get() );
                current.hausnummerZusatz().set( toAdopt.hausnummerZusatz().get() );
                current.richtwertZone().set( toAdopt.richtwertZone().get() );
//                current.kartenBlatt().set( toAdopt.kartenBlatt().get() );
                current.baublock().set( toAdopt.baublock().get() );
                current.nutzung().set( toAdopt.nutzung().get() );
                current.gebaeudeArt().set( toAdopt.gebaeudeArt().get() );
                current.artDesBaugebiets().set( toAdopt.artDesBaugebiets().get() );
//                current.flaeche().set( toAdopt.flaeche().get() );
                current.flaechenAnteilZaehler().set( toAdopt.flaechenAnteilZaehler().get() );
                current.flaechenAnteilNenner().set( toAdopt.flaechenAnteilNenner().get() );
                current.verkaufteFlaeche().set( toAdopt.verkaufteFlaeche().get() );
                current.erbbaurecht().set( toAdopt.erbbaurecht().get() );
                current.belastung().set( toAdopt.belastung().get() );
                current.flurstueck().set( toAdopt.flurstueck().get() );
                delete old flurstück wenn noch nicht persistent 

                
                refreshReloadables();
            }
        };
        sfAction.setEnabled( false );
        searchFlurstueckeButton = new ActionButton( parent, sfAction );
        searchFlurstueckeButton.setLayoutData( left().right( 20 ).height( 25 ).top( line1 ).create() );
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
                        new ReloadablePropertyAdapter<FlurstueckVerkaufComposite>( selectedComposite, prefix
                                + "strasse", new AssociationCallback<FlurstueckVerkaufComposite>() {

                            public Association get( FlurstueckVerkaufComposite entity ) {
                                return entity.strasse();
                            }
                        } ) ).setField( reloadable( strassePickList ) ).setLayoutData( left().top( line1 ).create() )
                .create();

        newFormField( "Hausnummer" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckVerkaufComposite>( selectedComposite, prefix
                                + "hausnummer", new PropertyCallback<FlurstueckVerkaufComposite>() {

                            public Property<String> get( FlurstueckVerkaufComposite entity ) {
                                return entity.hausnummer();
                            }
                        } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setLayoutData( right().right( 75 ).top( line1 ).create() ).create();

        newFormField( "Zusatz" )
                .setToolTipText( "Hausnummernzusatz" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckVerkaufComposite>( selectedComposite, prefix
                                + "hausnummerZusatz", new PropertyCallback<FlurstueckVerkaufComposite>() {

                            public Property<String> get( FlurstueckVerkaufComposite entity ) {
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
                        new ReloadablePropertyAdapter<FlurstueckVerkaufComposite>( selectedComposite, prefix
                                + "richtwertZone", new AssociationCallback<FlurstueckVerkaufComposite>() {

                            public Association<RichtwertzoneComposite> get( FlurstueckVerkaufComposite entity ) {
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
                        strassePickList.reloadValues();
                        strassePickList.setValue( selectedComposite.get() != null ? selectedComposite.get().strasse()
                                .get() : null );
                        richtwertZonePickList.reloadValues();
                        richtwertZonePickList.setValue( selectedComposite.get() != null ? selectedComposite.get()
                                .richtwertZone().get() : null );
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
                        new ReloadablePropertyAdapter<FlurstueckVerkaufComposite>( selectedComposite, prefix
                                + "nutzung", new AssociationCallback<FlurstueckVerkaufComposite>() {

                            public Association get( FlurstueckVerkaufComposite entity ) {
                                return entity.nutzung();
                            }
                        } ) ).setField( reloadable( namedAssocationsPicklist( NutzungComposite.class ) ) )
                .setLayoutData( left().top( line3 ).create() ).create();

        newFormField( "Gebäudeart" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckVerkaufComposite>( selectedComposite, prefix
                                + "gebaeudeArt", new AssociationCallback<FlurstueckVerkaufComposite>() {

                            public Association get( FlurstueckVerkaufComposite entity ) {
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
                        artPicklist.setValue( selectedComposite.get() != null ? selectedComposite.get()
                                .artDesBaugebiets().get() : null );
                    }
                }
            }
        } );

        Composite line5 = newFormField( "Art" )
                .setToolTipText( "Art des Grundstücks bei Agrarland, Art des Baugebietes sonst" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckVerkaufComposite>( selectedComposite, prefix
                                + "artDesBaugebiets", new AssociationCallback<FlurstueckVerkaufComposite>() {

                            public Association get( FlurstueckVerkaufComposite entity ) {
                                return entity.artDesBaugebiets();
                            }
                        } ) ).setField( reloadable( artPicklist ) ).setLayoutData( left().top( line4 ).create() )
                .create();

        Composite line6 = newFormField( "Fläche in m²" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckVerkaufComposite>( selectedComposite, prefix
                                + "flaeche", new PropertyCallback<FlurstueckVerkaufComposite>() {

                            public Property get( FlurstueckVerkaufComposite entity ) {
                                return entity.flurstueck().get().flaeche();
                            }
                        } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 2, 1, 2 ) )
                .setLayoutData( left().right( 25 ).top( line5 ).create() ).create();

        newFormField( "Anteil" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckVerkaufComposite>( selectedComposite, prefix
                                + "flaecheAnteilZaehler", new PropertyCallback<FlurstueckVerkaufComposite>() {

                            public Property get( FlurstueckVerkaufComposite entity ) {
                                return entity.flaechenAnteilZaehler();
                            }
                        } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 2, 1, 2 ) )
                .setLayoutData( left().left( 25 ).right( 50 ).top( line5 ).create() ).create();

        newFormField( "/" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckVerkaufComposite>( selectedComposite, prefix
                                + "flaechenAnteilNenner", new PropertyCallback<FlurstueckVerkaufComposite>() {

                            public Property get( FlurstueckVerkaufComposite entity ) {
                                return entity.flaechenAnteilNenner();
                            }
                        } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 2, 1, 2 ) )
                .setLayoutData( left().left( 50 ).right( 75 ).top( line5 ).create() ).create();

        newFormField( "verkaufte Fläche in m²" )
                .setParent( parent )
                .setEnabled( false )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckVerkaufComposite>( selectedComposite, prefix
                                + "verkaufteFlaeche", new PropertyCallback<FlurstueckVerkaufComposite>() {

                            public Property get( FlurstueckVerkaufComposite entity ) {
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
                        new ReloadablePropertyAdapter<FlurstueckVerkaufComposite>( selectedComposite, prefix
                                + "erbbaurecht", new PropertyCallback<FlurstueckVerkaufComposite>() {

                            public Property get( FlurstueckVerkaufComposite entity ) {
                                return entity.erbbaurecht();
                            }
                        } ) ).setField( reloadable( new BooleanFormField() ) )
                .setLayoutData( left().top( line6 ).create() ).create();

        openErweiterteDaten = new ActionButton( parent, new Action( "Flurstück bewerten" ) {

            @Override
            public void run() {
                FlurstueckVerkaufComposite flurstueck = selectedComposite.get();
                if (flurstueck != null) {
                    NutzungComposite nutzung = flurstueck.nutzung().get();
                    if (nutzung.isAgrar().get()) {
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
                    else {
                        FlurstuecksdatenBaulandComposite bauland = FlurstuecksdatenBaulandComposite.Mixin
                                .forFlurstueck( flurstueck );
                        if (bauland == null) {
                            bauland = repository.newEntity( FlurstuecksdatenBaulandComposite.class, null );
                            bauland.flurstueck().set( flurstueck );
                            bauland.vertrag().set( flurstueck.vertrag().get() );
                            // bauland.richtwertZone().set(
                            // flurstueck.richtwertZone().get() );
                        }
                        KapsPlugin.openEditor( fs, FlurstuecksdatenBaulandComposite.NAME, bauland );
                    }
                }
            }
        } );
        openErweiterteDaten.setLayoutData( left().right( 20 ).height( 25 ).top( line6 ).create() );
        openErweiterteDaten.setEnabled( false );

        Control line8 = openErweiterteDaten;
        // return the last line
        return line8;
    }


    protected EntityType<FlurstueckVerkaufComposite> addViewerColumns( FeatureTableViewer viewer ) {
        // entity types
        final KapsRepository repo = KapsRepository.instance();
        final EntityType<FlurstueckVerkaufComposite> type = repo.entityType( FlurstueckVerkaufComposite.class );

        PropertyDescriptor prop = null;
        prop = new PropertyDescriptorAdapter( type.getProperty( "flurstueck.gemarkung" ) );
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


    public Iterable<FlurstueckVerkaufComposite> getElements() {
        return FlurstueckVerkaufComposite.Mixin.forEntity( kaufvertrag );
    }


    @Override
    protected FlurstueckVerkaufComposite createNewComposite()
            throws Exception {
        final FlurstueckComposite flurstueckComposite = repository.newEntity( FlurstueckComposite.class, null);
        
        return repository.newEntity( FlurstueckVerkaufComposite.class, null,
                new EntityCreator<FlurstueckVerkaufComposite>() {

                    public void create( FlurstueckVerkaufComposite prototype )
                            throws Exception {
                        prototype.vertrag().set( kaufvertrag );
                        prototype.flaechenAnteilZaehler().set( 1.0d );
                        prototype.flaechenAnteilNenner().set( 1.0d );
                        prototype.flurstueck().set( flurstueckComposite );
                    }
                } );
    }
}