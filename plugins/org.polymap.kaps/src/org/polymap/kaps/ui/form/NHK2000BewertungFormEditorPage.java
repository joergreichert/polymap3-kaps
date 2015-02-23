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

import org.eclipse.jface.action.Action;

import org.eclipse.ui.forms.widgets.Section;

import org.polymap.core.data.ui.featuretable.DefaultFeatureTableColumn;
import org.polymap.core.data.ui.featuretable.FeatureTableViewer;
import org.polymap.core.model.EntityType;
import org.polymap.core.project.ui.util.SimpleFormData;

import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.data.entityfeature.PropertyDescriptorAdapter;
import org.polymap.rhei.data.entityfeature.ReloadablePropertyAdapter;
import org.polymap.rhei.data.entityfeature.ReloadablePropertyAdapter.PropertyCallback;
import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.form.FormEditor;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.KapsPlugin;
import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.NHK2000GebaeudeArtLabelProvider;
import org.polymap.kaps.model.data.NHK2000BewertungComposite;
import org.polymap.kaps.model.data.NHK2000BewertungGebaeudeComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.model.data.WohnungComposite;
import org.polymap.kaps.ui.ActionButton;
import org.polymap.kaps.ui.KapsDefaultFormEditorPageWithFeatureTable;
import org.polymap.kaps.ui.MyNumberValidator;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class NHK2000BewertungFormEditorPage
        extends KapsDefaultFormEditorPageWithFeatureTable<NHK2000BewertungGebaeudeComposite> {

    private static Log                  log    = LogFactory.getLog( NHK2000BewertungFormEditorPage.class );

    private final static String         prefix = NHK2000BewertungFormEditorPage.class.getSimpleName();

    protected NHK2000BewertungComposite bewertung;

    private final FormEditor            formEditor;

    private Label                       gebaeudeArtLabel;


    public NHK2000BewertungFormEditorPage( final FormEditor formEditor, Feature feature, FeatureStore featureStore ) {
        super( NHK2000BewertungGebaeudeComposite.class, NHK2000BewertungFormEditorPage.class.getName(), "NHK 2000",
                feature, featureStore );
        this.formEditor = formEditor;

        bewertung = repository.findEntity( NHK2000BewertungComposite.class, feature.getIdentifier().getID() );
    }


    protected void refreshReloadables()
            throws Exception {
        NHK2000BewertungGebaeudeComposite composite = selectedComposite.get();
        super.refreshReloadables();
        gebaeudeArtLabel
                .setText( composite != null && composite.GEBNR1().get() != null ? NHK2000GebaeudeArtLabelProvider
                        .instance().labelFor( composite.GEBNR1().get() ) : "" );
        for (IFormField field : reloadables) {
            field.setEnabled( false );
        }

    }


    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );

        final VertragComposite vertragComposite = bewertung.vertrag().get();
        final WohnungComposite wohnung = bewertung.wohnung().get();
        Composite parent = site.getPageBody();
        ActionButton openVertragOderWohnung = null;
        if (vertragComposite != null) {
            String nummer = vertragComposite != null ? EingangsNummerFormatter.format( vertragComposite.eingangsNr()
                    .get() ) : null;
            site.setEditorTitle( formattedTitle( "NHK 2000", nummer, null ) );
            site.setFormTitle( formattedTitle( "Bewertung nach NHK 2000", nummer, getTitle() ) );

            openVertragOderWohnung = new ActionButton( parent, new Action( "Vertrag " + nummer + " öffnen" ) {

                @Override
                public void run() {
                    KapsPlugin.openEditor( fs, VertragComposite.NAME, vertragComposite );
                }
            } );
            openVertragOderWohnung.setLayoutData( left().height( 25 ).create() );
            openVertragOderWohnung.setEnabled( true );
        }
        else if (wohnung != null) {
            String nummer = wohnung.objektNummer().get() + "/" + wohnung.gebaeudeNummer().get() + "/"
                    + wohnung.wohnungsNummer().get() + "/" + wohnung.wohnungsFortfuehrung().get();
            site.setEditorTitle( formattedTitle( "NHK 2000", nummer, null ) );
            site.setFormTitle( formattedTitle( "Bewertung nach NHK 2000", nummer, getTitle() ) );

            openVertragOderWohnung = new ActionButton( parent, new Action( "Wohnung " + nummer + " öffnen" ) {

                @Override
                public void run() {
                    KapsPlugin.openEditor( fs, WohnungComposite.NAME, wohnung );
                }
            } );
            openVertragOderWohnung.setLayoutData( left().height( 25 ).create() );
            openVertragOderWohnung.setEnabled( true );
        }

        Section tableSection = newSection( parent, "Auswahl Gebäude" );
        tableSection.setLayoutData( new SimpleFormData( SECTION_SPACING ).left( 0 ).right( 50 ).top( openVertragOderWohnung )
                .create() );
        createTableForm( (Composite)tableSection.getClient(), parent, false, false, false );

        Section sumSection = newSection( parent, "Summen" );
        sumSection.setLayoutData( new SimpleFormData( SECTION_SPACING ).left( tableSection, 0 ).top( openVertragOderWohnung )
                .right( 100 ).create() );
        createSumForm( site, sumSection );

        Section formSection = newSection( parent, "Gebäudedaten" );
        formSection.setLayoutData( new SimpleFormData( SECTION_SPACING ).left( 0 ).right( 100 ).top( sumSection )
                .create() );
        createGebaeudeForm( formSection );
    }


    @SuppressWarnings("unchecked")
    private Composite createSumForm( final IFormEditorPageSite site, final Section section ) {
        Composite parent = (Composite)section.getClient();

        int col1 = 45;
        int col2 = 65;

        Control newLine, lastLine = null;
        newLine = createLabel( parent, "Zeitwerte", "Summe der Gebäudezeitwerte", left().right( col1 ).top( lastLine ),
                SWT.RIGHT );
        createPreisField( bewertung.BERZEITW1(), left().left( col2 ).right( 100 ).top( lastLine ), parent, false );

        lastLine = newLine;
        newLine = createLabel( parent, "Sonstige Werte", "+/- sonstige Zeitwerte",
                left().right( col1 ).top( lastLine ), SWT.RIGHT );
        createPreisField( bewertung.ZWSUMSONST(), left().left( col2 ).right( 100 ).top( lastLine ), parent, false );

        lastLine = newLine;
        newLine = createLabel( parent, "Anbauten", "Anbauten", left().right( col1 ).top( lastLine ), SWT.RIGHT );
        newFormField( IFormFieldLabel.NO_LABEL ).setEnabled( false )
                .setProperty( new PropertyAdapter( bewertung.ZEITWTEXT() ) ).setField( new StringFormField() )
                .setLayoutData( left().left( col2 ).right( 100 ).top( lastLine ).create() ).setParent( parent )
                .create();

        lastLine = newLine;
        newLine = createLabel( parent, "Außenanlagen", "Wert der Außenanlagen", left().right( col1 ).top( lastLine ),
                SWT.RIGHT );
        createPreisField( bewertung.AUSVHBETR(), left().left( col2 ).right( 100 ).top( lastLine ), parent, false );

        lastLine = newLine;
        newLine = createLabel( parent, "Gesamtwert", "Gesamtwert der baulichen und sonstigen Anlagen",
                left().right( col1 ).top( lastLine ), SWT.RIGHT );
        createPreisField( bewertung.GESBAUWERT(), left().left( col2 ).bottom( 100 ).right( 100 ).top( lastLine ),
                parent, false );

        lastLine = newLine;
        return section;
    }


    private Control createGebaeudeForm( Section section ) {

        Composite parent = (Composite)section.getClient();
        Control newLine, lastLine = null;

        newLine = createLabel( parent, "Gebäudenummer", one().top( lastLine ) );
        newFormField( IFormFieldLabel.NO_LABEL )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2000BewertungGebaeudeComposite>( selectedComposite,
                                getPropertyName( nameTemplate.LFDNR() ),
                                new PropertyCallback<NHK2000BewertungGebaeudeComposite>() {

                                    @Override
                                    public Property<Integer> get( NHK2000BewertungGebaeudeComposite entity ) {
                                        return entity.LFDNR();
                                    }
                                } ) ).setField( reloadable( new StringFormField() ) )
                .setValidator( new MyNumberValidator( Integer.class, 2, 0 ) ).setEnabled( false )
                .setLayoutData( two().top( lastLine ).create() ).create();

        // gebäudeart mit selektor
        lastLine = newLine;
        newLine = createLabel( parent, "Gebäudeart", one().right( 10 ).top( lastLine ) );
        newFormField( IFormFieldLabel.NO_LABEL )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2000BewertungGebaeudeComposite>( selectedComposite,
                                getPropertyName( nameTemplate.GEBNR1() ),
                                new PropertyCallback<NHK2000BewertungGebaeudeComposite>() {

                                    @Override
                                    public Property<String> get( NHK2000BewertungGebaeudeComposite entity ) {
                                        return entity.GEBNR1();
                                    }
                                } ) ).setField( reloadable( new StringFormField() ) ).setEnabled( false )
                .setLayoutData( two().top( lastLine ).create() ).create();
        // lastLine = newLine;
        gebaeudeArtLabel = (Label)createLabel( parent, "", three().right( 100 ).top( lastLine ), SWT.LEFT );
        lastLine = gebaeudeArtLabel;

        // gebäudestandard
        newLine = createLabel( parent, "Ausstattung", one().right( 10 ).top( lastLine ) );
        newFormField( IFormFieldLabel.NO_LABEL )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2000BewertungGebaeudeComposite>( selectedComposite,
                                getPropertyName( nameTemplate.AUSSTATT1() ),
                                new PropertyCallback<NHK2000BewertungGebaeudeComposite>() {

                                    @Override
                                    public Property<String> get( NHK2000BewertungGebaeudeComposite entity ) {
                                        return entity.AUSSTATT1();
                                    }
                                } ) ).setField( reloadable( new StringFormField() ) ).setEnabled( false )
                .setLayoutData( two().top( lastLine ).create() ).create();
        // gebaeudeBnkLabel = (Label)createLabel( parent, "", three().right( 100
        // ).top( lastLine ), SWT.LEFT );

        // Gebäudestandard - Zusatzdaten
        lastLine = newLine;
        // grundrissart
        newLine = createLabel( parent, "Grundrissart", one().top( lastLine ) );

        newFormField( IFormFieldLabel.NO_LABEL )
                .setToolTipText( "Grundrissart (nur bei Mehrfamilienhäusern)" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2000BewertungGebaeudeComposite>( selectedComposite,
                                getPropertyName( nameTemplate.GRDRISS1() ),
                                new PropertyCallback<NHK2000BewertungGebaeudeComposite>() {

                                    @Override
                                    public Property get( NHK2000BewertungGebaeudeComposite entity ) {
                                        return entity.GRDRISS1();
                                    }
                                } ) ).setField( reloadable( new StringFormField() ) ).setEnabled( false )
                .setLayoutData( two().top( lastLine ).create() ).create();
        //
        // newFormField( "Wohnungen" )
        // .setToolTipText( "Anzahl Wohnungen (nur bei Mehrfamilienhäusern)" )
        // .setParent( parent )
        // .setProperty(
        // new ReloadablePropertyAdapter<NHK2000BewertungGebaeudeComposite>(
        // selectedComposite, prefix
        // + "anzahlWohnungen", new
        // PropertyCallback<NHK2000BewertungGebaeudeComposite>() {
        //
        // @Override
        // public Property get( NHK2000BewertungGebaeudeComposite entity ) {
        // return entity.anzahlWohnungen();
        // }
        // } ) ).setField( reloadable( new StringFormField(
        // StringFormField.Style.ALIGN_RIGHT ) ) )
        // .setValidator( new MyNumberValidator( Double.class, 10, 0 ) ).setEnabled(
        // false )
        // .setLayoutData( three().top( lastLine ).create() ).create();
        //
        // newFormField( "Zweifamilienhaus" )
        // .setToolTipText(
        // "Zweifamilienhaus (nur bei Auswahl von Einfamilienhäusern)" )
        // .setParent( parent )
        // .setProperty(
        // new ReloadablePropertyAdapter<NHK2000BewertungGebaeudeComposite>(
        // selectedComposite, prefix
        // + "zweifamilienHaus", new
        // PropertyCallback<NHK2000BewertungGebaeudeComposite>() {
        //
        // @Override
        // public Property get( NHK2000BewertungGebaeudeComposite entity ) {
        // return entity.zweifamilienHaus();
        // }
        // } ) ).setField( reloadable( new CheckboxFormField() ) ).setEnabled( false
        // )
        // .setLayoutData( four().top( lastLine ).create() ).create();

        lastLine = newLine;

        // wird nicht gespeichert und ausgewertet, also vllt. als computed wenn
        // nachgefragt
        // newFormField( "Wohnungsgröße" )
        // .setToolTipText(
        // "Wohnungsgröße = BGF / Anzahl der Wohnungen bei Mehrfamilienhaus)" )
        // .setParent( parent )
        // .setProperty(
        // new ReloadablePropertyAdapter<NHK2000BewertungGebaeudeComposite>(
        // selectedComposite, prefix
        // + "zweifamilienHaus", new
        // PropertyCallback<NHK2000BewertungGebaeudeComposite>() {
        //
        // @Override
        // public Property get( NHK2000BewertungGebaeudeComposite entity ) {
        // return entity.zweifamilienHaus();
        // }
        // } ) ).setField( reloadable( new CheckboxFormField() ) ).setEnabled( false
        // )
        // .setLayoutData( five().top( lastLine ).create() ).create();

        lastLine = newLine;
        //
        // newFormField( "Wohnungsgröße" )
        // .setToolTipText( "Korrekturfaktor Wohnungsgröße" )
        // .setParent( parent )
        // .setProperty(
        // new ReloadablePropertyAdapter<NHK2000BewertungGebaeudeComposite>(
        // selectedComposite, prefix
        // + "faktorWohnungsgroesse", new
        // PropertyCallback<NHK2000BewertungGebaeudeComposite>() {
        //
        // @Override
        // public Property<Double> get( NHK2000BewertungGebaeudeComposite entity ) {
        // return entity.faktorWohnungsgroesse();
        // }
        // } ) ).setField( reloadable( new StringFormField(
        // StringFormField.Style.ALIGN_RIGHT ) ) )
        // .setValidator( new MyNumberValidator( Double.class, 12, 2 ) ).setEnabled(
        // false )
        // .setLayoutData( three().top( lastLine ).create() ).create();

        // NHK BGF Wohnungsgröße
        lastLine = newLine;
        // newLine = createLabel( parent, , "NHK 2010 in €/m² Bruttogrundfläche",
        // one().top( lastLine ),
        // SWT.LEFT );
        newLine = newFormField( "NHK 2000" )
                .setToolTipText( "NHK 2000 in €/m² Bruttogrundfläche" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2000BewertungGebaeudeComposite>( selectedComposite,
                                getPropertyName( nameTemplate.NHK1() ),
                                new PropertyCallback<NHK2000BewertungGebaeudeComposite>() {

                                    @Override
                                    public Property get( NHK2000BewertungGebaeudeComposite entity ) {
                                        return entity.NHK1();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) ).setEnabled( false )
                .setLayoutData( two().top( lastLine, 30 ).create() ).create();

        newFormField( "BGF" )
                .setToolTipText( "Bruttogrundfläche" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2000BewertungGebaeudeComposite>( selectedComposite,
                                getPropertyName( nameTemplate.BGF1() ),
                                new PropertyCallback<NHK2000BewertungGebaeudeComposite>() {

                                    @Override
                                    public Property<Double> get( NHK2000BewertungGebaeudeComposite entity ) {
                                        return entity.BGF1();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new MyNumberValidator( Double.class ) ).setEnabled( true )
                .setLayoutData( three().top( lastLine, 30 ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( "NHK 2000 inkl. BNK" )
                .setToolTipText( "NHK 2000 korrigiert in €/m² inklusive Baunebenkosten" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2000BewertungGebaeudeComposite>( selectedComposite,
                                getPropertyName( nameTemplate.NHKBNK1() ),
                                new PropertyCallback<NHK2000BewertungGebaeudeComposite>() {

                                    @Override
                                    public Property get( NHK2000BewertungGebaeudeComposite entity ) {
                                        return entity.NHKBNK1();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) ).setEnabled( false )
                .setLayoutData( two().top( lastLine ).create() ).create();

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
        //
        // lastLine = newLine;
        // newLine = gebaeudeZeitWert( parent, lastLine );

        return newLine;
    }


    private Control immoWertV( Composite parent, Control lastLine ) {

        Control newLine = createLabel( parent, "Baumängel/schädel", two().top( lastLine ) );
        newFormField( IFormFieldLabel.NO_LABEL )
                .setToolTipText( "Zu-/Abschlag in €" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2000BewertungGebaeudeComposite>( selectedComposite,
                                getPropertyName( nameTemplate.ABSCHLBM_BETRAG() ),
                                new PropertyCallback<NHK2000BewertungGebaeudeComposite>() {

                                    @Override
                                    public Property<Double> get( NHK2000BewertungGebaeudeComposite entity ) {
                                        return entity.ABSCHLBM_BETRAG();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) ).setEnabled( false )
                .setLayoutData( three().top( lastLine ).create() ).create();
        lastLine = newLine;
        newLine = createLabel( parent, "sonst. Zu-/Abschläge", two().top( lastLine ) );
        newFormField( IFormFieldLabel.NO_LABEL )
                .setToolTipText( "Zu-/Abschlag in €" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2000BewertungGebaeudeComposite>( selectedComposite,
                                getPropertyName( nameTemplate.ABSCHLSO_BETRAG() ),
                                new PropertyCallback<NHK2000BewertungGebaeudeComposite>() {

                                    @Override
                                    public Property<Double> get( NHK2000BewertungGebaeudeComposite entity ) {
                                        return entity.ABSCHLSO_BETRAG();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) )
                .setLayoutData( three().bottom( 100 ).top( lastLine ).create() ).create();
        lastLine = newLine;

        return newLine;
    }


    private Control baukostenIndex( Composite parent, Control lastLine ) {

        newFormField( "Baukostenindex" )
                .setToolTipText( "Baukostenindex auf Basis 2000" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2000BewertungGebaeudeComposite>( selectedComposite,
                                getPropertyName( nameTemplate.BKIND() ),
                                new PropertyCallback<NHK2000BewertungGebaeudeComposite>() {

                                    @Override
                                    public Property<Double> get( NHK2000BewertungGebaeudeComposite entity ) {
                                        return entity.BKIND();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) ).setEnabled( false )
                .setLayoutData( three().top( lastLine ).create() ).create();

        return newFormField( "Neuwert" )
                .setToolTipText( "Normalherstellungswert * Baukostenindex auf Basis 2010" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2000BewertungGebaeudeComposite>( selectedComposite,
                                getPropertyName( nameTemplate.NEUWERT1() ),
                                new PropertyCallback<NHK2000BewertungGebaeudeComposite>() {

                                    @Override
                                    public Property<Double> get( NHK2000BewertungGebaeudeComposite entity ) {
                                        return entity.NEUWERT1();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) ).setEnabled( false )
                .setLayoutData( four().top( lastLine ).create() ).create();

        // return field;
    }


    private Control baujahr( Composite parent, Control lastLine ) {
        final Control field = newFormField( "GND" )
                .setToolTipText( "Gesamtnutzungsdauer" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2000BewertungGebaeudeComposite>( selectedComposite,
                                getPropertyName( nameTemplate.GND1() ),
                                new PropertyCallback<NHK2000BewertungGebaeudeComposite>() {

                                    @Override
                                    public Property<Double> get( NHK2000BewertungGebaeudeComposite entity ) {
                                        return entity.GND1();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new MyNumberValidator( Double.class ) ).setLayoutData( two().top( lastLine ).create() )
                .create();

        newFormField( "Baujahr" )
                .setToolTipText( "Tatsächliches Baujahr" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2000BewertungGebaeudeComposite>( selectedComposite,
                                getPropertyName( nameTemplate.BAUJ1() ),
                                new PropertyCallback<NHK2000BewertungGebaeudeComposite>() {

                                    @Override
                                    public Property<Double> get( NHK2000BewertungGebaeudeComposite entity ) {
                                        return entity.BAUJ1();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new MyNumberValidator( Double.class ) ).setLayoutData( three().top( lastLine ).create() )
                .create();

        Control bb = newFormField( "Baujahr bereinigt" )
                .setToolTipText( "Bereinigtes Baujahr" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2000BewertungGebaeudeComposite>( selectedComposite,
                                getPropertyName( nameTemplate.BERBAUJ1() ),
                                new PropertyCallback<NHK2000BewertungGebaeudeComposite>() {

                                    @Override
                                    public Property<Double> get( NHK2000BewertungGebaeudeComposite entity ) {
                                        return entity.BERBAUJ1();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new MyNumberValidator( Double.class ) ).setLayoutData( four().top( lastLine ).create() )
                .create();

        return field;
    }


    private Control alterswertMinderung( Composite parent, Control lastLine ) {
        final Control field = newFormField( "RND" )
                .setToolTipText( "Restnutzungsdauer" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2000BewertungGebaeudeComposite>( selectedComposite,
                                getPropertyName( nameTemplate.RND1() ),
                                new PropertyCallback<NHK2000BewertungGebaeudeComposite>() {

                                    @Override
                                    public Property<Double> get( NHK2000BewertungGebaeudeComposite entity ) {
                                        return entity.RND1();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new MyNumberValidator( Double.class ) ).setEnabled( false )
                .setLayoutData( two().top( lastLine ).create() ).create();

        newFormField( "Alterswertminderung" )
                .setToolTipText( "Alterswertminderung" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2000BewertungGebaeudeComposite>( selectedComposite,
                                getPropertyName( nameTemplate.ALTERSWERTMINDERUNG() ),
                                new PropertyCallback<NHK2000BewertungGebaeudeComposite>() {

                                    @Override
                                    public Property<Double> get( NHK2000BewertungGebaeudeComposite entity ) {
                                        return entity.ALTERSWERTMINDERUNG();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) )
                .setLayoutData( three().top( lastLine ).create() ).create();

        newFormField( "Zeitwert" )
                .setToolTipText( "Gebäudezeitwert ohne Zu-/Abschläge ImmoWertV" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2000BewertungGebaeudeComposite>( selectedComposite,
                                getPropertyName( nameTemplate.ZEITWERT1() ),
                                new PropertyCallback<NHK2000BewertungGebaeudeComposite>() {

                                    @Override
                                    public Property<Double> get( NHK2000BewertungGebaeudeComposite entity ) {
                                        return entity.ZEITWERT1();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) )
                .setLayoutData( four().top( lastLine ).create() ).create();

        return field;
    }


    private Control normalherstellungswert( Composite parent, Control lastLine ) {
        Control field = newFormField( "Normalherstellwert" )
                .setToolTipText( "Normalherstellungswert nach NHK 2000 korrigiert in €" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<NHK2000BewertungGebaeudeComposite>( selectedComposite,
                                getPropertyName( nameTemplate.NHWERT1() ),
                                new PropertyCallback<NHK2000BewertungGebaeudeComposite>() {

                                    @Override
                                    public Property<Double> get( NHK2000BewertungGebaeudeComposite entity ) {
                                        return entity.NHWERT1();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) ).setEnabled( false )
                .setLayoutData( four().top( lastLine ).create() ).create();

        return field;
    }


    //
    // private Control gebaeudeZeitWert( Composite parent, Control lastLine ) {
    // Control field = newFormField( "Gebäudezeitwert" )
    // .setToolTipText( "Gebäudezeitwert in €" )
    // .setParent( parent )
    // .setProperty(
    // new ReloadablePropertyAdapter<NHK2000BewertungGebaeudeComposite>(
    // selectedComposite,
    // getPropertyName( nameTemplate.ZEITWERT1() ),
    // new PropertyCallback<NHK2000BewertungGebaeudeComposite>() {
    //
    // @Override
    // public Property<Double> get( NHK2000BewertungGebaeudeComposite entity ) {
    // return entity.ZEITWERT1();
    // }
    // } ) ).setField( reloadable( new StringFormField(
    // StringFormField.Style.ALIGN_RIGHT ) ) )
    // .setValidator( new MyNumberValidator( Double.class, 2 ) ).setEnabled( false )
    // .setLayoutData( four().top( lastLine ).bottom( 100 ).create() ).create();
    //
    // field = createLabel( parent, "", one().top( field ) );
    // return field;
    // }

    @Override
    protected EntityType addViewerColumns( FeatureTableViewer viewer ) {
        final KapsRepository repo = KapsRepository.instance();
        final EntityType<NHK2000BewertungGebaeudeComposite> type = repo
                .entityType( NHK2000BewertungGebaeudeComposite.class );

        PropertyDescriptor prop = null;
        prop = new PropertyDescriptorAdapter( type.getProperty( "LFDNR" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Lfd. Nr." ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "GEBNR1" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Art" ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "ZEITWERT1" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Zeitwert" ) );

        return type;
    }


    @Override
    protected Iterable<NHK2000BewertungGebaeudeComposite> getElements() {
        return NHK2000BewertungGebaeudeComposite.Mixin.forBewertung( bewertung );
    }


    private String getPropertyName( Property<?> property ) {
        return prefix + nameTemplate.toString();
    }
}
