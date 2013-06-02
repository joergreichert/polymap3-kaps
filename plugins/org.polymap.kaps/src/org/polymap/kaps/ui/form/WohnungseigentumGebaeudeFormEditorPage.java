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
import org.polymap.rhei.field.NumberValidator;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.data.GebaeudeArtComposite;
import org.polymap.kaps.model.data.GebaeudeComposite;
import org.polymap.kaps.model.data.WohnungseigentumComposite;
import org.polymap.kaps.ui.BooleanFormField;
import org.polymap.kaps.ui.KapsDefaultFormEditorPageWithFeatureTable;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class WohnungseigentumGebaeudeFormEditorPage
        extends KapsDefaultFormEditorPageWithFeatureTable<GebaeudeComposite> {

    private static Log                log    = LogFactory.getLog( WohnungseigentumGebaeudeFormEditorPage.class );

    private WohnungseigentumComposite eigentum;

    private final static String       prefix = WohnungseigentumGebaeudeFormEditorPage.class.getSimpleName();

    // private FlurstueckSearcher sfAction;

    // private GemarkungComposite selectedGemarkung;

    // private NutzungComposite selectedNutzung;

    // private IFormFieldListener gemarkungListener;

    // private IFormFieldListener nutzungListener;

//    private ActionButton              openErweiterteDaten;


    public WohnungseigentumGebaeudeFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( WohnungseigentumGebaeudeFormEditorPage.class.getName(), "Gebäudedaten", feature, featureStore );

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
//        boolean compositeSelected = selectedComposite.get() != null;
        // selectedGemarkung = compositeSelected ?
        // selectedComposite.get().gemarkung().get() : null;
        //
        // selectedNutzung = compositeSelected ?
        // selectedComposite.get().nutzung().get() : null;

        super.refreshReloadables();
//        if (openErweiterteDaten != null) {
            // sfAction.refresh();
            // searchFlurstueckeButton.setEnabled( compositeSelected );
//            openErweiterteDaten.setEnabled( compositeSelected );
//        }
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

        Composite lastLine, newLine = null;
        newLine = newFormField( "Gebäudenummer" )
                .setProperty(
                        new ReloadablePropertyAdapter<GebaeudeComposite>( selectedComposite, prefix + "gebaeudeNummer",
                                new PropertyCallback<GebaeudeComposite>() {

                                    public Property get( GebaeudeComposite entity ) {
                                        return entity.gebaeudeNummer();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new NumberValidator( Integer.class, Polymap.getSessionLocale() ) )
                .setLayoutData( left().create() ).create();

        newFormField( "Fortführung" )
                .setProperty(
                        new ReloadablePropertyAdapter<GebaeudeComposite>( selectedComposite, prefix
                                + "gebaeudeFortfuehrung", new PropertyCallback<GebaeudeComposite>() {

                            public Property get( GebaeudeComposite entity ) {
                                return entity.gebaeudeFortfuehrung();
                            }
                        } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new NumberValidator( Integer.class, Polymap.getSessionLocale() ) )
                .setLayoutData( right().create() ).create();

        lastLine = newLine;
        newLine = newFormField( "Gebäudeart" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<GebaeudeComposite>( selectedComposite, prefix + "gebaeudeArt",
                                new AssociationCallback<GebaeudeComposite>() {

                                    public Association get( GebaeudeComposite entity ) {
                                        return entity.gebaeudeArt();
                                    }
                                } ) )
                .setField( reloadable( namedAssocationsPicklist( GebaeudeArtComposite.class, true ) ) )
                .setLayoutData( left().top( lastLine ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( "Baujahr tatsächlich" )
                .setToolTipText( "tatsächliches Baujahr" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<GebaeudeComposite>( selectedComposite, prefix
                                + "baujahrTatsaechlich", new PropertyCallback<GebaeudeComposite>() {

                            public Property get( GebaeudeComposite entity ) {
                                return entity.baujahrTatsaechlich();
                            }
                        } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setLayoutData( left().top( lastLine ).create() )
                .setValidator( new NumberValidator( Integer.class, locale ) ).create();

        newFormField( "Baujahr bereinigt" )
                .setToolTipText( "bereinigtes Baujahr" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<GebaeudeComposite>( selectedComposite, prefix + "baujahr",
                                new PropertyCallback<GebaeudeComposite>() {

                                    public Property get( GebaeudeComposite entity ) {
                                        return entity.baujahr();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setLayoutData( right().top( lastLine ).create() )
                .setValidator( new NumberValidator( Integer.class, locale ) ).create();

        lastLine = newLine;
        newLine = newFormField( "Lageklasse" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<GebaeudeComposite>( selectedComposite, prefix + "lageklasse",
                                new PropertyCallback<GebaeudeComposite>() {

                                    public Property get( GebaeudeComposite entity ) {
                                        return entity.lageklasse();
                                    }
                                } ) ).setField( reloadable( new StringFormField() ) )
                .setLayoutData( left().top( lastLine ).create() )
                .setValidator( new NumberValidator( Double.class, locale, 12, 0, 0, 0 ) ).create();

        newFormField( "Wohn-/Gewerbeeinheiten" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<GebaeudeComposite>( selectedComposite, prefix + "wohnEinheiten",
                                new PropertyCallback<GebaeudeComposite>() {

                                    public Property get( GebaeudeComposite entity ) {
                                        return entity.wohnEinheiten();
                                    }
                                } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setLayoutData( right().top( lastLine ).create() )
                .setValidator( new NumberValidator( Integer.class, locale ) ).create();

        lastLine = newLine;
        newLine = newFormField( "Aufzug" )

                .setProperty(
                        new ReloadablePropertyAdapter<GebaeudeComposite>( selectedComposite, prefix + "aufzug",
                                new PropertyCallback<GebaeudeComposite>() {

                                    public Property get( GebaeudeComposite entity ) {
                                        return entity.aufzug();
                                    }
                                } ) ).setField( reloadable( new BooleanFormField() ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( parent ).create();
        newFormField( "Denkmalschutz" )

                .setProperty(
                        new ReloadablePropertyAdapter<GebaeudeComposite>( selectedComposite, prefix + "denkmalschutz",
                                new PropertyCallback<GebaeudeComposite>() {

                                    public Property get( GebaeudeComposite entity ) {
                                        return entity.denkmalschutz();
                                    }
                                } ) ).setField( reloadable( new BooleanFormField() ) )
                .setLayoutData( right().top( lastLine ).create() ).setParent( parent ).create();

        lastLine = newLine;
        newLine = newFormField( "Sanierung" )

                .setProperty(
                        new ReloadablePropertyAdapter<GebaeudeComposite>( selectedComposite, prefix + "sanierung",
                                new PropertyCallback<GebaeudeComposite>() {

                                    public Property get( GebaeudeComposite entity ) {
                                        return entity.sanierung();
                                    }
                                } ) ).setField( reloadable( new BooleanFormField() ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( parent ).create();

        Map<String, Object> werte = new HashMap<String, Object>();
        werte.put( "Anfangswert", "A" );
        werte.put( "Endwert", "E" );
        werte.put( "unbekannt", "U" );
        newFormField( "Sanierungswert" )
                .setEnabled( true )
                .setProperty(
                        new ReloadablePropertyAdapter<GebaeudeComposite>( selectedComposite, prefix + "sanierungswert",
                                new PropertyCallback<GebaeudeComposite>() {

                                    public Property get( GebaeudeComposite entity ) {
                                        return entity.sanierungswert();
                                    }
                                } ) ).setField( reloadable( new PicklistFormField( werte ) ) )
                .setLayoutData( right().top( lastLine ).create() ).setParent( parent ).create();

        lastLine = newLine;
        newLine = newFormField( "Bemerkung" )
                .setProperty(
                        new ReloadablePropertyAdapter<GebaeudeComposite>( selectedComposite, prefix + "bemerkung",
                                new PropertyCallback<GebaeudeComposite>() {

                                    public Property get( GebaeudeComposite entity ) {
                                        return entity.bemerkung();
                                    }
                                } ) ).setField( reloadable(new TextFormField()) )
                .setLayoutData( left().top( lastLine ).right( RIGHT ).height( 100 ).create() ).setParent( parent )
                .create();
        return newLine;
    }


    protected EntityType<GebaeudeComposite> addViewerColumns( FeatureTableViewer viewer ) {
        // entity types
        final KapsRepository repo = KapsRepository.instance();
        final EntityType<GebaeudeComposite> type = repo.entityType( GebaeudeComposite.class );

        PropertyDescriptor prop = null;
        prop = new PropertyDescriptorAdapter( type.getProperty( "gebaeudeNummer" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Gebaeudenummer" ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "gebaeudeFortfuehrung" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Gebaeudefortfuehrung" ) );

        return type;
    }


    public Iterable<GebaeudeComposite> getElements() {
        return GebaeudeComposite.Mixin.forEntity( eigentum );
    }


    @Override
    protected GebaeudeComposite createNewComposite()
            throws Exception {
        return repository.newEntity( GebaeudeComposite.class, null, new EntityCreator<GebaeudeComposite>() {

            public void create( GebaeudeComposite prototype )
                    throws Exception {
                prototype.objektNummer().set( eigentum.objektNummer().get() );
                prototype.objektFortfuehrung().set( eigentum.objektFortfuehrung().get() );
            }
        } );
    }
}