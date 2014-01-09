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

import org.polymap.rhei.data.entityfeature.PropertyDescriptorAdapter;
import org.polymap.rhei.data.entityfeature.ReloadablePropertyAdapter;
import org.polymap.rhei.data.entityfeature.ReloadablePropertyAdapter.AssociationCallback;
import org.polymap.rhei.data.entityfeature.ReloadablePropertyAdapter.PropertyCallback;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.data.BelastungComposite;
import org.polymap.kaps.model.data.FlurComposite;
import org.polymap.kaps.model.data.FlurstueckComposite;
import org.polymap.kaps.model.data.GebaeudeComposite;
import org.polymap.kaps.model.data.GemarkungComposite;
import org.polymap.kaps.model.data.GemeindeComposite;
import org.polymap.kaps.model.data.NutzungComposite;
import org.polymap.kaps.model.data.RichtwertzoneComposite;
import org.polymap.kaps.model.data.StrasseComposite;
import org.polymap.kaps.ui.ActionButton;
import org.polymap.kaps.ui.BooleanFormField;
import org.polymap.kaps.ui.KapsDefaultFormEditorPageWithFeatureTable;
import org.polymap.kaps.ui.MyNumberValidator;
import org.polymap.kaps.ui.NotNullMyNumberValidator;
import org.polymap.kaps.ui.NotNullValidator;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class GebaeudeFlurstueckeFormEditorPage
        extends KapsDefaultFormEditorPageWithFeatureTable<FlurstueckComposite> {

    private static Log          log    = LogFactory.getLog( GebaeudeFlurstueckeFormEditorPage.class );

    private GebaeudeComposite   gebaeude;

    private FlurstueckSearcher  sfAction;

    private ActionButton        searchFlurstueckeButton;

    private GemarkungComposite  selectedGemarkung;

    private IFormFieldListener  gemarkungListener;

    private Object              selectedStrasse;

    private Object              selectedRichtwertzone;

    // private Object selectedNutzung;

    // private Object selectedArtDesBaugebietes;

    private final static String prefix = GebaeudeFlurstueckeFormEditorPage.class.getSimpleName();


    public GebaeudeFlurstueckeFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( FlurstueckComposite.class, GebaeudeFlurstueckeFormEditorPage.class.getName(), "Flurstücke", feature,
                featureStore );

        gebaeude = repository.findEntity( GebaeudeComposite.class, feature.getIdentifier().getID() );
    }


    @Override
    public void createFormContent( IFormEditorPageSite site ) {
        super.createFormContent( site );

        String nummer = gebaeude.objektNummer().get() != null ? gebaeude.schl().get() : "neu";

        site.setEditorTitle( formattedTitle( "Gebäude", nummer, null ) );
        site.setFormTitle( formattedTitle( "Gebäude", nummer, getTitle() ) );

        Composite parent = site.getPageBody();
        Control form = createFlurstueckForm( parent );
        createTableForm( parent, form, true, false );
    }


    protected void refreshReloadables()
            throws Exception {
        FlurstueckComposite composite = selectedComposite.get();
        boolean compositeSelected = composite != null;
        selectedGemarkung = compositeSelected ? composite.gemarkung().get() : null;
        selectedStrasse = composite != null ? composite.strasse().get() : null;
        selectedRichtwertzone = composite != null ? composite.richtwertZone().get() : null;
        // selectedNutzung = composite != null ? composite.nutzung().get() : null;
        // selectedArtDesBaugebietes = composite != null ?
        // composite.artDesBaugebiets().get() : null;

        super.refreshReloadables();
        // if (openErweiterteDaten != null) {
        // sfAction.refresh();
        // searchFlurstueckeButton.setEnabled( compositeSelected );
        // openErweiterteDaten.setEnabled( compositeSelected );
        // }
        // das muss disabled bleiben
        // // pageSite.setFieldEnabled( prefix + "verkaufteFlaeche", false );
        // if (selectedComposite.get() != null &&
        // selectedComposite.get().gebaeudeNummer().get() != null) {
        // pageSite.setFieldEnabled( prefix + "gebaeudeNummer", false );
        // pageSite.setFieldEnabled( prefix + "gebaeudeFortfuehrung", false );
        // }
    }


    public boolean isValid() {
        return true;
    }


    public Control createFlurstueckForm( Composite parent ) {

        Control lastLine, newLine = null;
        //
        // lastLine = newLine;
        // newLine = newFormField( "Gebäudenummer" )
        // .setProperty(
        // new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite,
        // prefix
        // + "gebaeudeNummer", new PropertyCallback<FlurstueckComposite>() {
        //
        // public Property<Integer> get( FlurstueckComposite entity ) {
        // return entity.gebaeudeNummer();
        // }
        // } ) ).setField( reloadable( new StringFormField(
        // StringFormField.Style.ALIGN_RIGHT ) ) )
        // .setValidator( new MyNumberValidator( Integer.class ) )
        // .setLayoutData( left().top( lastLine ).create() ).create();
        //
        // newFormField( "Fortführung" )
        // .setProperty(
        // new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite,
        // prefix
        // + "gebaeudeFortfuehrung", new PropertyCallback<FlurstueckComposite>() {
        //
        // public Property<Integer> get( FlurstueckComposite entity ) {
        // return entity.gebaeudeFortfuehrung();
        // }
        // } ) ).setField( reloadable( new StringFormField(
        // StringFormField.Style.ALIGN_RIGHT ) ) )
        // .setValidator( new MyNumberValidator( Integer.class ) )
        // .setLayoutData( right().top( lastLine ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( "Gemarkung" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite, prefix + "gemarkung",
                                new AssociationCallback<FlurstueckComposite>() {

                                    public Association<GemarkungComposite> get( FlurstueckComposite entity ) {
                                        return entity.gemarkung();
                                    }
                                } ) )
                .setField( reloadable( namedAssocationsPicklist( GemarkungComposite.class, true ) ) )
                .setValidator( new NotNullValidator() ).setLayoutData( left().top( lastLine ).create() ).create();

        // newFormField( "Flur" )
        // .setParent( parent )
        // .setProperty(
        // new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite,
        // prefix + "flur",
        // new AssociationCallback<FlurstueckComposite>() {
        //
        // public Association<FlurComposite> get( FlurstueckComposite entity ) {
        // return entity.flur();
        // }
        // } ) ).setField( reloadable( namedAssocationsPicklist( FlurComposite.class,
        // false ) ) )
        // .setLayoutData( right().top( lastLine ).create() ).create();
        newFormField( "Flurstücksnummer" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite, prefix + "hauptNummer",
                                new PropertyCallback<FlurstueckComposite>() {

                                    public Property<Integer> get( FlurstueckComposite entity ) {
                                        return entity.hauptNummer();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setLayoutData( right().right( 75 ).top( lastLine ).create() )
                .setValidator( new NotNullMyNumberValidator( Integer.class ) ).create();

        newFormField( "Unternummer" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite, prefix + "unterNummer",
                                new PropertyCallback<FlurstueckComposite>() {

                                    public Property<String> get( FlurstueckComposite entity ) {
                                        return entity.unterNummer();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new NotNullValidator() )
                .setLayoutData( right().left( 75 ).top( lastLine ).create() ).create();

        lastLine = newLine;
        // BUTTON zur Datenabfrage
        sfAction = new FlurstueckSearcher( prefix ) {

            protected void adopt( FlurstueckComposite toAdopt )
                    throws Exception {
                assert toAdopt != null;
                FlurstueckComposite current = selectedComposite.get();
                // separate behandeln, da die 3 felder von zusätzlichen reloads von
                // gemarkung und nutzung
                // wieder zurückgesetzt werden
                selectedStrasse = toAdopt.strasse().get();
                selectedRichtwertzone = toAdopt.richtwertZone().get();
                // selectedArtDesBaugebietes = toAdopt.artDesBaugebiets().get();

                pageSite.setFieldValue( prefix + "gemarkung", toAdopt.gemarkung().get() );
                // current.gemarkung().set( toAdopt.gemarkung().get() );
                // pageSite.setFieldValue( prefix + "flur", toAdopt.flur().get() );
                pageSite.setFieldValue( prefix + "hauptNummer", String.valueOf( toAdopt.hauptNummer().get() ) );
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
                // pageSite.setFieldValue( prefix + "gebaeudeArt",
                // toAdopt.gebaeudeArt().get() );
                // pageSite.setFieldValue( prefix + "artDesBaugebiets",
                // toAdopt.artDesBaugebiets().get() );
                pageSite.setFieldValue( prefix + "flaeche", String.valueOf( toAdopt.flaeche().get() ) );
                // pageSite.setFieldValue( prefix + "flaechenAnteilZaehler",
                // String.valueOf( toAdopt.flaechenAnteilZaehler().get() ) );
                // pageSite.setFieldValue( prefix + "flaechenAnteilNenner",
                // String.valueOf( toAdopt.flaechenAnteilNenner().get() ) );
                // pageSite.setFieldValue( prefix + "verkaufteFlaeche",
                // String.valueOf( toAdopt.verkaufteFlaeche().get() ) );
                pageSite.setFieldValue( prefix + "erbbaurecht", toAdopt.erbbaurecht().get() );
                pageSite.setFieldValue( prefix + "belastung", toAdopt.belastung().get() );

                // refreshReloadables();
            }
        };
        sfAction.setEnabled( false );
        searchFlurstueckeButton = new ActionButton( parent, sfAction );
        searchFlurstueckeButton.setLayoutData( right().right( 75 ).height( 25 ).top( lastLine ).create() );
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
                        new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite, prefix + "strasse",
                                new AssociationCallback<FlurstueckComposite>() {

                                    public Association<StrasseComposite> get( FlurstueckComposite entity ) {
                                        return entity.strasse();
                                    }
                                } ) ).setField( reloadable( strassePickList ) ).setValidator( new NotNullValidator() )
                .setLayoutData( left().top( lastLine ).create() ).create();

        newFormField( "Hausnummer" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite, prefix + "hausnummer",
                                new PropertyCallback<FlurstueckComposite>() {

                                    public Property<String> get( FlurstueckComposite entity ) {
                                        return entity.hausnummer();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new NotNullValidator() ).setLayoutData( right().right( 75 ).top( lastLine ).create() )
                .create();

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
                        new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite,
                                prefix + "richtwertZone", new AssociationCallback<FlurstueckComposite>() {

                                    public Association<RichtwertzoneComposite> get( FlurstueckComposite entity ) {
                                        return entity.richtwertZone();
                                    }
                                } ) ).setField( reloadable( richtwertZonePickList ) )
                .setValidator( new NotNullValidator() ).setLayoutData( left().top( lastLine ).create() ).create();

        pageSite.addFieldListener( gemarkungListener = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == VALUE_CHANGE && ev.getFieldName().equalsIgnoreCase( prefix + "gemarkung" )) {
                    if ((ev.getNewValue() == null && selectedGemarkung != null)
                            || (ev.getNewValue() != null && !ev.getNewValue().equals( selectedGemarkung ))) {
                        selectedGemarkung = ev.getNewValue();
                        strassePickList.reloadValues();
                        pageSite.setFieldValue( prefix + "strasse", selectedStrasse );
                        richtwertZonePickList.reloadValues();
                        pageSite.setFieldValue( prefix + "richtwertZone", selectedRichtwertzone );
                    }
                }
            }
        } );

        lastLine = newLine;
        newLine = newFormField( "Nutzung" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite, prefix + "nutzung",
                                new AssociationCallback<FlurstueckComposite>() {

                                    public Association<NutzungComposite> get( FlurstueckComposite entity ) {
                                        return entity.nutzung();
                                    }
                                } ) ).setField( reloadable( namedAssocationsPicklist( NutzungComposite.class ) ) )
                .setValidator( new NotNullValidator() ).setLayoutData( left().top( lastLine ).create() ).create();

        newFormField( "Belastung" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite, prefix + "belastung",
                                new AssociationCallback<FlurstueckComposite>() {

                                    public Association<BelastungComposite> get( FlurstueckComposite entity ) {
                                        return entity.belastung();
                                    }
                                } ) ).setField( reloadable( namedAssocationsPicklist( BelastungComposite.class ) ) )
                .setValidator( new NotNullValidator() ).setLayoutData( right().top( lastLine ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( "Fläche in m²" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite, prefix + "flaeche",
                                new PropertyCallback<FlurstueckComposite>() {

                                    public Property<Double> get( FlurstueckComposite entity ) {
                                        return entity.flaeche();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new NotNullMyNumberValidator( Double.class, 2 ) )
                .setLayoutData( left().top( lastLine ).create() ).create();

        newFormField( "Erbbaurecht" )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite, prefix + "erbbaurecht",
                                new PropertyCallback<FlurstueckComposite>() {

                                    public Property<String> get( FlurstueckComposite entity ) {
                                        return entity.erbbaurecht();
                                    }
                                } ) ).setField( reloadable( new BooleanFormField() ) )
                .setValidator( new NotNullValidator() ).setLayoutData( right().top( lastLine ).create() )
                .setParent( parent ).create();

        // openErweiterteDaten = new ActionButton( parent, new Action(
        // "Flurstück bewerten" ) {
        //
        // @Override
        // public void run() {
        // FlurstueckComposite flurstueck = selectedComposite.get();
        // if (flurstueck != null) {
        // NutzungComposite nutzung = flurstueck.nutzung().get();
        // if (nutzung.isAgrar().get()) {
        // VertragsdatenAgrarComposite agrar =
        // VertragsdatenAgrarComposite.Mixin
        // .forFlurstueck( flurstueck );
        // if (agrar == null) {
        // agrar = repository.newEntity( VertragsdatenAgrarComposite.class, null
        // );
        // agrar.flurstueck().set( flurstueck );
        // agrar.vertrag().set( flurstueck.vertrag().get() );
        // // agrar.richtwertZone1().set(
        // // flurstueck.richtwertZone().get() );
        // }
        // KapsPlugin.openEditor( fs, VertragsdatenAgrarComposite.NAME, agrar );
        // }
        // else {
        // VertragsdatenBaulandComposite bauland =
        // VertragsdatenBaulandComposite.Mixin
        // .forFlurstueck( flurstueck );
        // if (bauland == null) {
        // bauland = repository.newEntity( VertragsdatenBaulandComposite.class,
        // null );
        // bauland.flurstueck().set( flurstueck );
        // bauland.vertrag().set( flurstueck.vertrag().get() );
        // // bauland.richtwertZone().set(
        // // flurstueck.richtwertZone().get() );
        // }
        // KapsPlugin.openEditor( fs, VertragsdatenBaulandComposite.NAME, bauland
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


    protected EntityType<FlurstueckComposite> addViewerColumns( FeatureTableViewer viewer ) {
        // entity types
        final KapsRepository repo = KapsRepository.instance();
        final EntityType<FlurstueckComposite> type = repo.entityType( FlurstueckComposite.class );

        PropertyDescriptor prop = null;
        prop = new PropertyDescriptorAdapter( type.getProperty( "vertragsNummer" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Vertragsnummer" ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "gemarkung" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Gemarkung" ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "hauptNummer" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Flurstück" ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "unterNummer" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Unternummer" ) );

        return type;
    }


    public Iterable<FlurstueckComposite> getElements() {
        return gebaeude.flurstuecke().toList();
    }


    @Override
    protected FlurstueckComposite createNewComposite()
            throws Exception {

        final FlurstueckComposite flurstueck = repository.newEntity( FlurstueckComposite.class, null );
        flurstueck.flur().set( repository.findSchlNamed( FlurComposite.class, "000" ) );

        // queue( new UpdateCommand() {
        //
        // @Override
        // public void execute() {
        gebaeude.flurstuecke().add( flurstueck );
        // }
        // } );
        return flurstueck;
    }


    @Override
    protected void deleteComposite( FlurstueckComposite flurstueck ) {
        if (gebaeude.flurstuecke().contains( flurstueck )) {
            gebaeude.flurstuecke().remove( flurstueck );
        }
    }
}