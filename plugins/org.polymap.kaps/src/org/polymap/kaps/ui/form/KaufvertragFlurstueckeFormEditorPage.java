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
import org.polymap.rhei.field.CheckboxFormField;
import org.polymap.rhei.field.NumberValidator;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.data.FlurComposite;
import org.polymap.kaps.model.data.FlurstueckComposite;
import org.polymap.kaps.model.data.GemarkungComposite;
import org.polymap.kaps.model.data.KaufvertragComposite;
import org.polymap.kaps.ui.ActionButton;
import org.polymap.kaps.ui.KapsDefaultFormEditorPageWithFeatureTable;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class KaufvertragFlurstueckeFormEditorPage
        extends KapsDefaultFormEditorPageWithFeatureTable<FlurstueckComposite> {

    private KaufvertragComposite   kaufvertrag;

    private final static String    prefix = KaufvertragFlurstueckeFormEditorPage.class
                                                  .getSimpleName();

    private FlurstueckSearcher sfAction;


    public KaufvertragFlurstueckeFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( KaufvertragFlurstueckeFormEditorPage.class.getName(), "Flurstücksdaten", feature,
                featureStore );

        kaufvertrag = repository.findEntity( KaufvertragComposite.class, feature.getIdentifier()
                .getID() );
    }


    @Override
    public void createFormContent( IFormEditorPageSite site ) {
        super.createFormContent( site );

        site.setEditorTitle( formattedTitle( "Kaufvertrag", kaufvertrag.eingangsNr().get(), null ) );
        site.setFormTitle( formattedTitle( "Kaufvertrag", kaufvertrag.eingangsNr().get(),
                getTitle() ) );

        Composite parent = site.getPageBody();
        Control schildForm = createFlurstueckForm( parent );
        createTableForm( parent, schildForm );
    }


    protected void refreshReloadables()
            throws Exception {
        super.refreshReloadables();
        if (sfAction != null) {
            sfAction.refresh();
        }
    }


    public boolean isValid() {
        return true;
    }


    public Control createFlurstueckForm( Composite parent ) {

        Composite line0 = newFormField( "Gemarkung" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite,
                                prefix + "gemarkung",
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
                        new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite,
                                prefix + "flur", new AssociationCallback<FlurstueckComposite>() {

                                    public Association get( FlurstueckComposite entity ) {
                                        return entity.flur();
                                    }
                                } ) )
                .setField( namedAssocationsPicklist( FlurComposite.class, true ) )
                .setLayoutData( right().create() ).create();

        Composite line1 = newFormField( "Flurstücksnummer" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite,
                                prefix + "nummer", new PropertyCallback<FlurstueckComposite>() {

                                    public Property get( FlurstueckComposite entity ) {
                                        return entity.nummer();
                                    }
                                } ) ).setField( reloadable( new StringFormField() ) )
                .setLayoutData( left().right( 25 ).top( line0 ).create() )
                .setValidator( new NumberValidator( Integer.class, locale ) ).create();

        newFormField( "/" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite,
                                prefix + "unterNummer",
                                new PropertyCallback<FlurstueckComposite>() {

                                    public Property get( FlurstueckComposite entity ) {
                                        return entity.unterNummer();
                                    }
                                } ) ).setField( reloadable( new StringFormField() ) )
                .setLayoutData( left().left( 25 ).right( 50 ).top( line0 ).create() ).create();

        newFormField( "Hauptflurstück" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite,
                                prefix + "hauptFlurstueck",
                                new PropertyCallback<FlurstueckComposite>() {

                                    public Property get( FlurstueckComposite entity ) {
                                        return entity.hauptFlurstueck();
                                    }
                                } ) ).setField( reloadable( new CheckboxFormField() ) )
                .setLayoutData( right().top( line0 ).create() ).create();

        // BUTTON zur Datenabfrage
        sfAction = new FlurstueckSearcher( prefix, selectedComposite ) {

            protected void adopt( FlurstueckComposite toAdopt )
                    throws Exception {
                assert toAdopt != null;
                FlurstueckComposite current = selectedComposite.get();
                current.gemarkung().set( toAdopt.gemarkung().get() );
                current.flur().set( toAdopt.flur().get() );
                current.nummer().set( toAdopt.nummer().get() );
                current.unterNummer().set( toAdopt.unterNummer().get() );
                // TODO continue

                refreshReloadables();
            }
        };
        ActionButton addBtn = new ActionButton( parent, sfAction );
        addBtn.setLayoutData( left().right( 10 ).top( line1 ).create() );

        pageSite.addFieldListener( sfAction );
        // Composite line2 = newFormField( "Pfeilrichtung" )
        // .setParent( parent )
        // .setProperty(
        // new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite,
        // prefix + "pfeilrichtung",
        // new AssociationCallback<FlurstueckComposite>() {
        //
        // public Association get( FlurstueckComposite entity ) {
        // return entity.pfeilrichtung();
        // }
        // } ) )
        // .setField( namedAssocationsPicklist( PfeilrichtungComposite.class ) )
        // .setLayoutData( left().top( line1 ).create() ).create();
        //
        // newFormField( "Material" )
        // .setParent( parent )
        // .setProperty(
        // new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite,
        // prefix + "material",
        // new AssociationCallback<FlurstueckComposite>() {
        //
        // public Association get( FlurstueckComposite entity ) {
        // return entity.material();
        // }
        // } ) )
        // .setField( namedAssocationsPicklist( SchildmaterialComposite.class ) )
        // .setLayoutData( right().top( line1 ).create() ).create();
        //
        // Composite line3 = newFormField( "Beschriftung" )
        // .setParent( parent )
        // .setProperty(
        // new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite,
        // prefix + "beschriftung",
        // new PropertyCallback<FlurstueckComposite>() {
        //
        // public Property get( FlurstueckComposite entity ) {
        // return entity.beschriftung();
        // }
        // } ) ).setField( new TextFormField() )
        // .setLayoutData( left().top( line2 ).height( 50 ).right( RIGHT ).create() )
        // .setToolTipText( "Schildbeschriftung mit Entfernungsangabe und Zusatzinfo"
        // )
        // .create();
        //
        // Composite line4 = newFormField( "Befestigung" )
        // .setParent( parent )
        // .setProperty(
        // new ReloadablePropertyAdapter<FlurstueckComposite>( selectedComposite,
        // prefix + "befestigung",
        // new PropertyCallback<FlurstueckComposite>() {
        //
        // public Property get( FlurstueckComposite entity ) {
        // return entity.befestigung();
        // }
        // } ) ).setField( new TextFormField() )
        // .setLayoutData( left().top( line3 ).height( 50 ).create() ).create();
        //
        // return imagePreview.getControl();
        return addBtn;
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
        prop = new PropertyDescriptorAdapter( type.getProperty( "hauptFlurstueck" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Hauptflurstück" ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "strasse" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Straße" ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "hausnummer" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Hausnummer" ) );

        return type;
    }


    public Iterable<FlurstueckComposite> getElements() {
        return FlurstueckComposite.Mixin.forEntity( kaufvertrag );
    }
}