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

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;
import org.opengis.feature.type.PropertyDescriptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.property.Property;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.eclipse.ui.forms.widgets.Section;

import org.polymap.core.data.ui.featuretable.DefaultFeatureTableColumn;
import org.polymap.core.data.ui.featuretable.FeatureTableViewer;
import org.polymap.core.model.EntityType;
import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.qi4j.QiModule.EntityCreator;
import org.polymap.core.runtime.Polymap;

import org.polymap.rhei.data.entityfeature.PropertyDescriptorAdapter;
import org.polymap.rhei.data.entityfeature.ReloadablePropertyAdapter;
import org.polymap.rhei.data.entityfeature.ReloadablePropertyAdapter.PropertyCallback;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.NumberValidator;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.NHK2010GebaeudeartenProvider;
import org.polymap.kaps.model.data.NHK2010BewertungComposite;
import org.polymap.kaps.model.data.NHK2010BewertungGebaeudeComposite;
import org.polymap.kaps.model.data.NHK2010Gebaeudeart;
import org.polymap.kaps.ui.ActionButton;
import org.polymap.kaps.ui.FieldSummation;
import org.polymap.kaps.ui.KapsDefaultFormEditorPageWithFeatureTable;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class NHK2010BewertungFormEditorPage
        extends KapsDefaultFormEditorPageWithFeatureTable<NHK2010BewertungGebaeudeComposite> {

    private static Log                  log         = LogFactory.getLog( NHK2010BewertungFormEditorPage.class );

    private final static String         prefix      = NHK2010BewertungFormEditorPage.class.getSimpleName();

    protected NHK2010BewertungComposite bewertung;

    private FieldSummation              gesamtWert;

    private boolean                     formCreated = false;

    private NHK2010GebaeudeartSelector  gebaeudeArtAction;

    private Label gebaeudeArtLabel;


    public NHK2010BewertungFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( NHK2010BewertungFormEditorPage.class.getName(), "NHK 2010", feature, featureStore );

        bewertung = repository.findEntity( NHK2010BewertungComposite.class, feature.getIdentifier().getID() );
    }


    protected void refreshReloadables()
            throws Exception {
        NHK2010BewertungGebaeudeComposite composite = selectedComposite.get();

        super.refreshReloadables();

        // diese felder müssen disabled bleiben
        if (formCreated) {
            gebaeudeArtAction.setEnabled( composite != null );

            pageSite.setFieldEnabled( prefix + "laufendeNummer", false );
            pageSite.setFieldEnabled( prefix + "gebaeudeArtId", false );
            
            if (composite != null) {
                NHK2010Gebaeudeart art = NHK2010GebaeudeartenProvider.instance().gebaeudeForId( composite.gebaeudeArtId().get() );
                gebaeudeArtLabel.setText( art != null ? art.getQualifiedName() : "" );                 
            }
        }
    }


    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );
        site.setEditorTitle( formattedTitle( "Bewertung nach NHK 2010", null, null ) );
        site.setFormTitle( formattedTitle( "Bewertung nach NHK 2010", null, getTitle() ) );

        Composite parent = site.getPageBody();

        Section tableSection = newSection( parent, "Auswahl Gebäude" );
        tableSection.setLayoutData( new SimpleFormData( SECTION_SPACING ).left( 0 ).right( 50 ).create() );
        createTableForm( (Composite)tableSection.getClient(), parent, true );

        Section sumSection = newSection( parent, "Summen" );
        sumSection.setLayoutData( new SimpleFormData( SECTION_SPACING ).left( tableSection, 0 ).right( 100 ).create() );
        createSumForm( site, sumSection );

        Section formSection = newSection( parent, "Gebäudedaten" );
        formSection.setLayoutData( new SimpleFormData( SECTION_SPACING ).left( 0 ).right( 100 ).top( tableSection )
                .create() );
        createGebaeudeForm( formSection );

        formCreated = true;
    }


    private Composite createSumForm( final IFormEditorPageSite site, final Section section ) {
        Composite parent = (Composite)section.getClient();

        Control newLine, lastLine = null;
        newLine = createPreisField( "Zeitwerte", "Summe der Gebäudezeitwerte", bewertung.summeZeitwerte(), left()
                .right( 100 ).top( lastLine ), parent, false );

        lastLine = newLine;
        newLine = createPreisField( "Bauteile", "+/- nicht erfasste Bauteile", bewertung.nichtErfassteBauteile(),
                left().right( 100 ).top( lastLine ), parent, true );

        lastLine = newLine;
        newLine = createPreisField( "Außenanlagen", "Wert der Außenanlagen", bewertung.wertDerAussenanlagen(), left()
                .right( 100 ).top( lastLine ), parent, true );

        lastLine = newLine;
        newLine = createPreisField( "Gesamtwert", "Gesamtwert der baulichen und sonstigen Anlagen",
                bewertung.gesamtWert(), left().right( 100 ).bottom( 100 ).top( lastLine ), parent, false );
        site.addFieldListener( gesamtWert = new FieldSummation( site, 2, bewertung.gesamtWert(), bewertung
                .summeZeitwerte(), bewertung.nichtErfassteBauteile(), bewertung.wertDerAussenanlagen() ) );
        return section;
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
                            public Property get( NHK2010BewertungGebaeudeComposite entity ) {
                                return entity.laufendeNummer();
                            }
                        } ) ).setField( reloadable( new StringFormField() ) )
                .setValidator( new NumberValidator( Long.class, Polymap.getSessionLocale(), 2, 0 ) ).setEnabled( false )
                .setLayoutData( two().top( lastLine ).create() ).create();

        // gebäudeart mit selektor
        lastLine = newLine;
        newLine = createLabel( parent, "Gebäudeart", one().right( 10 ).top( lastLine ) );
        // gebäudeselectionaction
        gebaeudeArtAction = new NHK2010GebaeudeartSelector( pageSite.getToolkit() ) {

            protected void adopt( NHK2010Gebaeudeart toAdopt )
                    throws Exception {
                assert toAdopt != null;
                pageSite.setFieldValue( prefix + "gebaeudeArtId", toAdopt.getId() );
                gebaeudeArtLabel.setText( toAdopt.getQualifiedName() );
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
                            public Property get( NHK2010BewertungGebaeudeComposite entity ) {
                                return entity.gebaeudeArtId();
                            }
                        } ) ).setField( reloadable( new StringFormField() ) ).setEnabled( false )
                .setLayoutData( two().top( lastLine ).create() ).create();
//        lastLine = newLine;
        gebaeudeArtLabel = (Label)createLabel( parent, "", three().right( 100 ).top( lastLine ), SWT.LEFT );
        lastLine = gebaeudeArtLabel;
        // newLine = newFormField( "Zone" ).setEnabled( bewertung.schl().get() ==
        // null )
        // .setProperty( new PropertyAdapter( bewertung.schl() ) ).setValidator( new
        // NotNullValidator() )
        // .setField( new StringFormField() ).setLayoutData( left().top( lastLine
        // ).create() ).create();
        // // TODO einfach immer anlassen ist wohl am einfachsten oder?
        // // boolean lageEnabled = (gemeinde != null && gemeinde.einwohner().get() >
        // // 50000);
        // final Composite lage = newFormField( "Lage (STALA)" )
        // /* .setEnabled( lageEnabled ) */
        // .setProperty( new AssociationAdapter<RichtwertZoneLageComposite>(
        // bewertung.lage() ) )
        // .setField( namedAssocationsPicklist( RichtwertZoneLageComposite.class ) )
        // .setLayoutData( right().top( lastLine ).create() ).create();
        //
        // lastLine = newLine;
        // newLine = newFormField( "GFZ-Bereich" ).setProperty( new PropertyAdapter(
        // bewertung.gfzBereich() ) )
        // .setField( new StringFormField() ).setLayoutData( right().top( lastLine
        // ).create() ).create();
        //
        // lastLine = newLine;
        // newLine = newFormField( "Nutzung" )
        // .setProperty( new AssociationAdapter<NutzungComposite>(
        // bewertung.nutzung() ) )
        // .setField( namedAssocationsPicklist( NutzungComposite.class ) )
        // .setLayoutData( left().top( lastLine ).create() ).create();
        //
        // newFormField( "Bodennutzung" )
        // .setProperty( new AssociationAdapter<BodennutzungComposite>(
        // bewertung.bodenNutzung() ) )
        // .setField( namedAssocationsPicklist( BodennutzungComposite.class ) )
        // .setLayoutData( right().top( lastLine ).create() ).create();
        //
        // // site.addFieldListener( gemeindeListener = new IFormFieldListener() {
        // //
        // // @Override
        // // public void fieldChange( FormFieldEvent ev ) {
        // // if (ev.getFieldName().equals( "gemeinde" )) {
        // // GemeindeComposite gemeinde = (GemeindeComposite)ev.getNewValue();
        // // lage.setEnabled( gemeinde != null && gemeinde.einwohner().get() > 50000
        // );
        // // }
        // // }
        // // } );
        //
        lastLine = newLine;
        return newLine;
    }


    //
    //
    // private Control createZeitraumForm( Composite parent, Control top ) {
    // Composite newLine, lastLine = null;
    //
    // newLine = newFormField( "Bezeichnung" )
    // .setParent( parent )
    // .setProperty(
    // new ReloadablePropertyAdapter<RichtwertzoneZeitraumComposite>(
    // selectedComposite, prefix
    // + "name", new PropertyCallback<RichtwertzoneZeitraumComposite>() {
    //
    // @Override
    // public Property get( RichtwertzoneZeitraumComposite entity ) {
    // return entity.name();
    // }
    //
    // } ) ).setField( reloadable( new StringFormField() ) )
    // .setLayoutData( left().top( top ).create() ).create();
    //
    // lastLine = newLine;
    // newLine = newFormField( "Gültig ab" )
    // .setParent( parent )
    // .setProperty(
    // new ReloadablePropertyAdapter<RichtwertzoneZeitraumComposite>(
    // selectedComposite, prefix
    // + "gueltigAb", new PropertyCallback<RichtwertzoneZeitraumComposite>() {
    //
    // @Override
    // public Property get( RichtwertzoneZeitraumComposite entity ) {
    // return entity.gueltigAb();
    // }
    //
    // } ) ).setField( reloadable( new DateTimeFormField() ) )// .setValidator(
    // // new
    // // NotNullValidator()
    // // )
    // .setLayoutData( left().top( lastLine ).create() ).create();
    // newFormField( "Stichtag" )
    // .setParent( parent )
    // .setProperty(
    // new ReloadablePropertyAdapter<RichtwertzoneZeitraumComposite>(
    // selectedComposite, prefix
    // + "stichtag", new PropertyCallback<RichtwertzoneZeitraumComposite>() {
    //
    // @Override
    // public Property get( RichtwertzoneZeitraumComposite entity ) {
    // return entity.stichtag();
    // }
    //
    // } ) ).setField( reloadable( new DateTimeFormField() ) )// .setValidator(
    // // new
    // // NotNullValidator()
    // // )
    // .setLayoutData( right().top( lastLine ).create() ).create();
    //
    // lastLine = newLine;
    // newLine = newFormField( "€ pro m²" )
    // .setParent( parent )
    // .setProperty(
    // new ReloadablePropertyAdapter<RichtwertzoneZeitraumComposite>(
    // selectedComposite, prefix
    // + "euroQm", new PropertyCallback<RichtwertzoneZeitraumComposite>() {
    //
    // @Override
    // public Property get( RichtwertzoneZeitraumComposite entity ) {
    // return entity.euroQm();
    // }
    //
    // } ) ).setField( reloadable( new StringFormField(
    // StringFormField.Style.ALIGN_RIGHT ) ) )
    // .setValidator( new NumberValidator( Double.class, locale, 12, 2, 1, 2 ) )
    // .setLayoutData( left().top( lastLine ).create() ).create();
    //
    // lastLine = newLine;
    // newLine = newFormField( "EB" )
    // .setParent( parent )
    // .setProperty(
    // new ReloadablePropertyAdapter<RichtwertzoneZeitraumComposite>(
    // selectedComposite, prefix
    // + "erschliessungsBeitrag", new
    // AssociationCallback<RichtwertzoneZeitraumComposite>() {
    //
    // @Override
    // public Association get( RichtwertzoneZeitraumComposite entity ) {
    // return entity.erschliessungsBeitrag();
    // }
    //
    // } ) ).setField( reloadable( namedAssocationsPicklist(
    // ErschliessungsBeitragComposite.class ) ) )
    // .setLayoutData( left().top( lastLine ).create() ).create();
    //
    // return newLine;
    // }
    //

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
                        long nextNumber = 1;
                        for (Object o : getElements()) {
                            nextNumber++;
                        }
                        prototype.laufendeNummer().set( nextNumber );
                    }
                } );
    }
}
