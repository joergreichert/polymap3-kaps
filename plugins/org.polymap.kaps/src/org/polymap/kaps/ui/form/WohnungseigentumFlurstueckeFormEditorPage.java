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

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.data.BelastungComposite;
import org.polymap.kaps.model.data.FlurComposite;
import org.polymap.kaps.model.data.FlurstueckComposite;
import org.polymap.kaps.model.data.FlurstueckWohneigentumComposite;
import org.polymap.kaps.model.data.GemarkungComposite;
import org.polymap.kaps.model.data.GemeindeComposite;
import org.polymap.kaps.model.data.NutzungComposite;
import org.polymap.kaps.model.data.RichtwertzoneComposite;
import org.polymap.kaps.model.data.StrasseComposite;
import org.polymap.kaps.model.data.WohnungseigentumComposite;
import org.polymap.kaps.ui.ActionButton;
import org.polymap.kaps.ui.BooleanFormField;
import org.polymap.kaps.ui.KapsDefaultFormEditorPageWithFeatureTable;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class WohnungseigentumFlurstueckeFormEditorPage
        extends KapsDefaultFormEditorPageWithFeatureTable<FlurstueckWohneigentumComposite> {

    private static Log                log    = LogFactory.getLog( WohnungseigentumFlurstueckeFormEditorPage.class );

    private WohnungseigentumComposite eigentum;

    private FlurstueckSearcher        sfAction;

    private ActionButton              searchFlurstueckeButton;

    private GemarkungComposite        selectedGemarkung;

    private IFormFieldListener        gemarkungListener;

    private final static String       prefix = WohnungseigentumFlurstueckeFormEditorPage.class.getSimpleName();

    public WohnungseigentumFlurstueckeFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( WohnungseigentumFlurstueckeFormEditorPage.class.getName(), "Flurstücke", feature, featureStore );

        eigentum = repository.findEntity( WohnungseigentumComposite.class, feature.getIdentifier().getID() );
    }


    @Override
    public void createFormContent( IFormEditorPageSite site ) {
        super.createFormContent( site );

        String nummer = eigentum.objektNummer().get() != null ? eigentum.schl().get() : "neu";

        site.setEditorTitle( formattedTitle( "Wohnungseigentum", nummer, null ) );
        site.setFormTitle( formattedTitle( "Wohnungseigentum", nummer, getTitle() ) );

        Composite parent = site.getPageBody();
        Control form = createFlurstueckForm( parent );
        createTableForm( parent, form, true );
    }


    protected void refreshReloadables()
            throws Exception {
        boolean compositeSelected = selectedComposite.get() != null;
        selectedGemarkung = compositeSelected ? selectedComposite.get().gemarkung().get() : null;
        //
        // selectedNutzung = compositeSelected ?
        // selectedComposite.get().nutzung().get() : null;

        super.refreshReloadables();
        // if (openErweiterteDaten != null) {
        // sfAction.refresh();
        // searchFlurstueckeButton.setEnabled( compositeSelected );
        // openErweiterteDaten.setEnabled( compositeSelected );
        // }
        // das muss disabled bleiben
        // pageSite.setFieldEnabled( prefix + "verkaufteFlaeche", false );
        if (selectedComposite.get() != null && selectedComposite.get().gebaeudeNummer().get() != null) {
            pageSite.setFieldEnabled( prefix + "gebaeudeNummer", false );
            pageSite.setFieldEnabled( prefix + "gebaeudeFortfuehrung", false );
        }
    }


    public boolean isValid() {
        return true;
    }


    public Control createFlurstueckForm( Composite parent ) {

        Control lastLine, newLine = null;
        
        lastLine = newLine;
        newLine = newFormField( "Gebäudenummer" )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckWohneigentumComposite>( selectedComposite, prefix
                                + "gebaeudeNummer", new PropertyCallback<FlurstueckWohneigentumComposite>() {

                            public Property<Integer> get( FlurstueckWohneigentumComposite entity ) {
                                return entity.gebaeudeNummer();
                            }
                        } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new NumberValidator( Integer.class, Polymap.getSessionLocale() ) )
                .setLayoutData( left().top( lastLine ).create() ).create();

        newFormField( "Fortführung" )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckWohneigentumComposite>( selectedComposite, prefix
                                + "gebaeudeFortfuehrung", new PropertyCallback<FlurstueckWohneigentumComposite>() {

                            public Property<Integer> get( FlurstueckWohneigentumComposite entity ) {
                                return entity.gebaeudeFortfuehrung();
                            }
                        } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new NumberValidator( Integer.class, Polymap.getSessionLocale() ) )
                .setLayoutData( right().top( lastLine ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( "Gemarkung" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckWohneigentumComposite>( selectedComposite, prefix
                                + "gemarkung", new AssociationCallback<FlurstueckWohneigentumComposite>() {

                            public Association<GemarkungComposite> get( FlurstueckWohneigentumComposite entity ) {
                                return entity.gemarkung();
                            }
                        } ) ).setField( reloadable( namedAssocationsPicklist( GemarkungComposite.class, true ) ) )
                .setLayoutData( left().top( lastLine ).create() ).create();

        newFormField( "Flur" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckWohneigentumComposite>( selectedComposite, prefix
                                + "flur", new AssociationCallback<FlurstueckWohneigentumComposite>() {

                            public Association<FlurComposite> get( FlurstueckWohneigentumComposite entity ) {
                                return entity.flur();
                            }
                        } ) ).setField( reloadable( namedAssocationsPicklist( FlurComposite.class, false ) ) )
                .setLayoutData( right().top( lastLine ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( "Flurstücksnummer" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckWohneigentumComposite>( selectedComposite, prefix
                                + "nummer", new PropertyCallback<FlurstueckWohneigentumComposite>() {

                            public Property<Integer> get( FlurstueckWohneigentumComposite entity ) {
                                return entity.nummer();
                            }
                        } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setLayoutData( left().right( 30 ).top( lastLine ).create() )
                .setValidator( new NumberValidator( Integer.class, locale ) ).create();

        newFormField( "/" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckWohneigentumComposite>( selectedComposite, prefix
                                + "unterNummer", new PropertyCallback<FlurstueckWohneigentumComposite>() {

                            public Property<String> get( FlurstueckWohneigentumComposite entity ) {
                                return entity.unterNummer();
                            }
                        } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setLayoutData( left().left( 30 ).right( 50 ).top( lastLine ).create() ).create();

        lastLine = newLine;
        // BUTTON zur Datenabfrage
        sfAction = new FlurstueckSearcher( prefix ) {

            protected void adopt( FlurstueckComposite toAdopt )
                    throws Exception {
                assert toAdopt != null;
                FlurstueckWohneigentumComposite current = selectedComposite.get();
                current.gemarkung().set( toAdopt.gemarkung().get() );
                current.flur().set( toAdopt.flur().get() );
                current.nummer().set( toAdopt.nummer().get() );
                current.unterNummer().set( toAdopt.unterNummer().get() );
                current.strasse().set( toAdopt.strasse().get() );
                current.hausnummer().set( toAdopt.hausnummer().get() );
                current.hausnummerZusatz().set( toAdopt.hausnummerZusatz().get() );
                current.richtwertZone().set( toAdopt.richtwertZone().get() );
                current.kartenBlatt().set( toAdopt.kartenBlatt().get() );
                current.baublock().set( toAdopt.baublock().get() );
                current.nutzung().set( toAdopt.nutzung().get() );
                current.flaeche().set( toAdopt.flaeche().get() );

                refreshReloadables();
            }
        };
        sfAction.setEnabled( false );
        searchFlurstueckeButton = new ActionButton( parent, sfAction );
        searchFlurstueckeButton.setLayoutData( left().right( 20 ).height( 25 ).top( lastLine ).create() );
        searchFlurstueckeButton.setEnabled( false );
        pageSite.addFieldListener( sfAction );

        lastLine = searchFlurstueckeButton;
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

        newLine = newFormField( "Straße/Gewann" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckWohneigentumComposite>( selectedComposite, prefix
                                + "strasse", new AssociationCallback<FlurstueckWohneigentumComposite>() {

                            public Association<StrasseComposite> get( FlurstueckWohneigentumComposite entity ) {
                                return entity.strasse();
                            }
                        } ) ).setField( reloadable( strassePickList ) ).setLayoutData( left().top( lastLine ).create() )
                .create();

        newFormField( "Hausnummer" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckWohneigentumComposite>( selectedComposite, prefix
                                + "hausnummer", new PropertyCallback<FlurstueckWohneigentumComposite>() {

                            public Property<String> get( FlurstueckWohneigentumComposite entity ) {
                                return entity.hausnummer();
                            }
                        } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setLayoutData( right().right( 75 ).top( lastLine ).create() ).create();

        newFormField( "Zusatz" )
                .setToolTipText( "Hausnummernzusatz" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckWohneigentumComposite>( selectedComposite, prefix
                                + "hausnummerZusatz", new PropertyCallback<FlurstueckWohneigentumComposite>() {

                            public Property<String> get( FlurstueckWohneigentumComposite entity ) {
                                return entity.hausnummerZusatz();
                            }
                        } ) ).setField( reloadable( new StringFormField() ) )
                .setLayoutData( right().left( 75 ).top( lastLine ).create() ).create();

        lastLine = newLine;
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

        newLine = newFormField( "Richtwertzone" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckWohneigentumComposite>( selectedComposite, prefix
                                + "richtwertZone", new AssociationCallback<FlurstueckWohneigentumComposite>() {

                            public Association<RichtwertzoneComposite> get( FlurstueckWohneigentumComposite entity ) {
                                return entity.richtwertZone();
                            }
                        } ) ).setField( reloadable( richtwertZonePickList ) )
                .setLayoutData( left().top( lastLine ).create() ).create();

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

        lastLine = newLine;
        newLine = newFormField( "Nutzung" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckWohneigentumComposite>( selectedComposite, prefix
                                + "nutzung", new AssociationCallback<FlurstueckWohneigentumComposite>() {

                            public Association<NutzungComposite> get( FlurstueckWohneigentumComposite entity ) {
                                return entity.nutzung();
                            }
                        } ) ).setField( reloadable( namedAssocationsPicklist( NutzungComposite.class ) ) )
                .setLayoutData( left().top( lastLine ).create() ).create();

        newFormField( "Belastung" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckWohneigentumComposite>( selectedComposite, prefix
                                + "belastung", new AssociationCallback<FlurstueckWohneigentumComposite>() {

                            public Association<BelastungComposite> get( FlurstueckWohneigentumComposite entity ) {
                                return entity.belastung();
                            }
                        } ) ).setField( reloadable( namedAssocationsPicklist( BelastungComposite.class ) ) )
                .setLayoutData( right().top( lastLine ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( "Fläche in m²" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckWohneigentumComposite>( selectedComposite, prefix
                                + "flaeche", new PropertyCallback<FlurstueckWohneigentumComposite>() {

                            public Property<Double> get( FlurstueckWohneigentumComposite entity ) {
                                return entity.flaeche();
                            }
                        } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 2, 1, 2 ) )
                .setLayoutData( left().top( lastLine ).create() ).create();

        newFormField( "Erbbaurecht" )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckWohneigentumComposite>( selectedComposite, prefix
                                + "erbbaurecht", new PropertyCallback<FlurstueckWohneigentumComposite>() {

                            public Property<String> get( FlurstueckWohneigentumComposite entity ) {
                                return entity.erbbaurecht();
                            }
                        } ) ).setField( reloadable( new BooleanFormField() ) )
                .setLayoutData( right().top( lastLine ).create() ).setParent( parent ).create();
        
        // openErweiterteDaten = new ActionButton( parent, new Action(
        // "Flurstück bewerten" ) {
        //
        // @Override
        // public void run() {
        // FlurstueckComposite flurstueck = selectedComposite.get();
        // if (flurstueck != null) {
        // NutzungComposite nutzung = flurstueck.nutzung().get();
        // if (nutzung.isAgrar().get()) {
        // FlurstuecksdatenAgrarComposite agrar =
        // FlurstuecksdatenAgrarComposite.Mixin
        // .forFlurstueck( flurstueck );
        // if (agrar == null) {
        // agrar = repository.newEntity( FlurstuecksdatenAgrarComposite.class, null
        // );
        // agrar.flurstueck().set( flurstueck );
        // agrar.vertrag().set( flurstueck.vertrag().get() );
        // // agrar.richtwertZone1().set(
        // // flurstueck.richtwertZone().get() );
        // }
        // KapsPlugin.openEditor( fs, FlurstuecksdatenAgrarComposite.NAME, agrar );
        // }
        // else {
        // FlurstuecksdatenBaulandComposite bauland =
        // FlurstuecksdatenBaulandComposite.Mixin
        // .forFlurstueck( flurstueck );
        // if (bauland == null) {
        // bauland = repository.newEntity( FlurstuecksdatenBaulandComposite.class,
        // null );
        // bauland.flurstueck().set( flurstueck );
        // bauland.vertrag().set( flurstueck.vertrag().get() );
        // // bauland.richtwertZone().set(
        // // flurstueck.richtwertZone().get() );
        // }
        // KapsPlugin.openEditor( fs, FlurstuecksdatenBaulandComposite.NAME, bauland
        // );
        // }
        // }
        // }
        // } );
        // openErweiterteDaten.setLayoutData( left().right( 20 ).height( 25 ).top(
        // line6 ).create() );
        // openErweiterteDaten.setEnabled( false );
        //

        return newLine;
    }


    protected EntityType<FlurstueckWohneigentumComposite> addViewerColumns( FeatureTableViewer viewer ) {
        // entity types
        final KapsRepository repo = KapsRepository.instance();
        final EntityType<FlurstueckWohneigentumComposite> type = repo
                .entityType( FlurstueckWohneigentumComposite.class );

        PropertyDescriptor prop = null;
        prop = new PropertyDescriptorAdapter( type.getProperty( "gebaeudeNummer" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Gebaeudenummer" ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "gebaeudeFortfuehrung" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Gebaeudefortfuehrung" ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "gemarkung" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Gemarkung" ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "nummer" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Flurstück" ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "unterNummer" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Unternummer" ) );

        return type;
    }


    public Iterable<FlurstueckWohneigentumComposite> getElements() {
        return FlurstueckWohneigentumComposite.Mixin.forEntity( eigentum );
    }


    @Override
    protected FlurstueckWohneigentumComposite createNewComposite()
            throws Exception {
        return repository.newEntity( FlurstueckWohneigentumComposite.class, null,
                new EntityCreator<FlurstueckWohneigentumComposite>() {

                    public void create( FlurstueckWohneigentumComposite prototype )
                            throws Exception {
                        prototype.objektNummer().set( eigentum.objektNummer().get() );
                        prototype.objektFortfuehrung().set( eigentum.objektFortfuehrung().get() );
                    }
                } );
    }
}