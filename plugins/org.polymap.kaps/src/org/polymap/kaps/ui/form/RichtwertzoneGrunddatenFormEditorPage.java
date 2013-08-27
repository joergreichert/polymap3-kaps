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
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.Feature;
import org.opengis.feature.type.PropertyDescriptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.entity.association.Association;
import org.qi4j.api.property.Property;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.data.operations.ModifyFeaturesOperation;
import org.polymap.core.data.ui.featuretable.DefaultFeatureTableColumn;
import org.polymap.core.data.ui.featuretable.FeatureTableViewer;
import org.polymap.core.mapeditor.MapEditor;
import org.polymap.core.model.EntityType;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.qi4j.QiModule.EntityCreator;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.data.entityfeature.AssociationAdapter;
import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.data.entityfeature.PropertyDescriptorAdapter;
import org.polymap.rhei.data.entityfeature.ReloadablePropertyAdapter;
import org.polymap.rhei.data.entityfeature.ReloadablePropertyAdapter.AssociationCallback;
import org.polymap.rhei.data.entityfeature.ReloadablePropertyAdapter.PropertyCallback;
import org.polymap.rhei.field.DateTimeFormField;
import org.polymap.rhei.field.NumberValidator;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.KapsPlugin;
import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.data.BodennutzungComposite;
import org.polymap.kaps.model.data.ErschliessungsBeitragComposite;
import org.polymap.kaps.model.data.GemeindeComposite;
import org.polymap.kaps.model.data.NutzungComposite;
import org.polymap.kaps.model.data.RichtwertZoneLageComposite;
import org.polymap.kaps.model.data.RichtwertzoneComposite;
import org.polymap.kaps.model.data.RichtwertzoneZeitraumComposite;
import org.polymap.kaps.ui.KapsDefaultFormEditorPageWithFeatureTable;
import org.polymap.kaps.ui.NotNullValidator;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class RichtwertzoneGrunddatenFormEditorPage
        extends KapsDefaultFormEditorPageWithFeatureTable<RichtwertzoneZeitraumComposite> {

    private static Log               log    = LogFactory.getLog( RichtwertzoneGrunddatenFormEditorPage.class );

    private final static String      prefix = RichtwertzoneGrunddatenFormEditorPage.class.getSimpleName();

    protected RichtwertzoneComposite richtwertzone;


    public RichtwertzoneGrunddatenFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( RichtwertzoneGrunddatenFormEditorPage.class.getName(), "Grunddaten", feature, featureStore );

        richtwertzone = lookupRichtwertzoneComposite();
    }


    protected RichtwertzoneComposite lookupRichtwertzoneComposite() {
        return repository.findEntity( RichtwertzoneComposite.class, feature.getIdentifier().getID() );
    }


    @Override
    public Action[] getEditorActions() {
        if (true /*richtwertzone.geom().get() == null*/) {
            Action action = new Action( "Geometrie anlegen" ) {
                public void run() {
                    try {
                        ILayer layer = ((PipelineFeatureSource)fs).getLayer();
                        IMap map = layer.getMap();
                        MapEditor mapEditor = MapEditor.openMap( map, true );
                        
                        boolean yes = MessageDialog.openConfirm( PolymapWorkbench.getShellToParentOn(), "Geometrie anlegen", 
                                "Die Geometrie für die Richtwertzone wird im aktuellen Kartenausschnitt angelegt. " + 
                                "Diese Standardgeometrie dient als Basis und muss nachbearbeitet werden.\n\n" +
                                "Ist der aktuelle Kartenausschnitt (etwa) richtig für die Richtwertzone?" );
                        if (yes) {
                            ReferencedEnvelope mapExtent = map.getMaxExtent();
                            GeometryFactory gf = new GeometryFactory();
                            Polygon polygon = (Polygon)gf.toGeometry( mapExtent );
                            polygon.buffer( mapExtent.getWidth()/20 );
                            MultiPolygon geom = gf.createMultiPolygon( new Polygon[] { polygon } );
                            
                            ModifyFeaturesOperation op = new ModifyFeaturesOperation( layer, fs, 
                                    feature.getIdentifier().getID(), "geom", geom );
                            OperationSupport.instance().execute( op, true, false );

//                            richtwertzone.geom().set( geom );
//                            WMSLayer ollayer = ((WMSLayer)mapEditor.findLayer( layer ));
//                            if (ollayer == null) {
//                                layer.setVisible( true );
//                            }
//                            else {
//                                ollayer.redraw( true );
//                            }
                        }
                    }
                    catch (Exception e) {
                        PolymapWorkbench.handleError( KapsPlugin.PLUGIN_ID, RichtwertzoneGrunddatenFormEditorPage.this, "Die Geometrie konnte nicht angelegt werden.", e );
                    }
                };
            };
            action.setToolTipText( "Für importierte Richtwertzonen wird eine Geometrie im aktuellen Kartenausschnitt angelegt" );
            return new Action[] { action };
        }
        return null;
    }


    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );
        site.setEditorTitle( formattedTitle( "Richtwertzone", richtwertzone.schl().get(), null ) );
        site.setFormTitle( formattedTitle( "Richtwertzone", richtwertzone.schl().get(), getTitle() ) );

        Composite parent = site.getPageBody();
        Control top = createGrunddatenForm( parent );
        top = createZeitraumForm( parent, top );
        createTableForm( parent, top, true );
    }


    private Control createGrunddatenForm( Composite parent ) {
        GemeindeComposite gemeinde = richtwertzone.gemeinde().get();

        Composite newLine, lastLine = null;

        newLine = newFormField( "Gemeinde" ).setEnabled( richtwertzone.gemeinde().get() == null )
                .setProperty( new AssociationAdapter<GemeindeComposite>( richtwertzone.gemeinde() ) )
                .setField( namedAssocationsPicklist( GemeindeComposite.class ) ).setValidator( new NotNullValidator() )
                .setLayoutData( left().top( lastLine ).create() ).create();

        newFormField( "Bezeichnung" ).setProperty( new PropertyAdapter( richtwertzone.name() ) )
                .setField( new StringFormField() ).setLayoutData( right().top( lastLine ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( "Zone" ).setEnabled( richtwertzone.schl().get() == null )
                .setProperty( new PropertyAdapter( richtwertzone.schl() ) ).setValidator( new NotNullValidator() )
                .setField( new StringFormField() ).setLayoutData( left().top( lastLine ).create() ).create();
        // TODO einfach immer anlassen ist wohl am einfachsten oder?
        // boolean lageEnabled = (gemeinde != null && gemeinde.einwohner().get() >
        // 50000);
        final Composite lage = newFormField( "Lage (STALA)" )
        /* .setEnabled( lageEnabled ) */
        .setProperty( new AssociationAdapter<RichtwertZoneLageComposite>( richtwertzone.lage() ) )
                .setField( namedAssocationsPicklist( RichtwertZoneLageComposite.class ) )
                .setLayoutData( right().top( lastLine ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( "GFZ-Bereich" ).setProperty( new PropertyAdapter( richtwertzone.gfzBereich() ) )
                .setField( new StringFormField() ).setLayoutData( right().top( lastLine ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( "Nutzung" )
                .setProperty( new AssociationAdapter<NutzungComposite>( richtwertzone.nutzung() ) )
                .setField( namedAssocationsPicklist( NutzungComposite.class ) )
                .setLayoutData( left().top( lastLine ).create() ).create();

        newFormField( "Bodennutzung" )
                .setProperty( new AssociationAdapter<BodennutzungComposite>( richtwertzone.bodenNutzung() ) )
                .setField( namedAssocationsPicklist( BodennutzungComposite.class ) )
                .setLayoutData( right().top( lastLine ).create() ).create();

        // site.addFieldListener( gemeindeListener = new IFormFieldListener() {
        //
        // @Override
        // public void fieldChange( FormFieldEvent ev ) {
        // if (ev.getFieldName().equals( "gemeinde" )) {
        // GemeindeComposite gemeinde = (GemeindeComposite)ev.getNewValue();
        // lage.setEnabled( gemeinde != null && gemeinde.einwohner().get() > 50000 );
        // }
        // }
        // } );

        lastLine = newLine;
        return newLine;
    }


    private Control createZeitraumForm( Composite parent, Control top ) {
        Composite newLine, lastLine = null;

        newLine = newFormField( "Bezeichnung" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<RichtwertzoneZeitraumComposite>( selectedComposite, prefix
                                + "name", new PropertyCallback<RichtwertzoneZeitraumComposite>() {

                            @Override
                            public Property get( RichtwertzoneZeitraumComposite entity ) {
                                return entity.name();
                            }

                        } ) ).setField( reloadable( new StringFormField() ) )
                .setLayoutData( left().top( top ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( "Gültig ab" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<RichtwertzoneZeitraumComposite>( selectedComposite, prefix
                                + "gueltigAb", new PropertyCallback<RichtwertzoneZeitraumComposite>() {

                            @Override
                            public Property get( RichtwertzoneZeitraumComposite entity ) {
                                return entity.gueltigAb();
                            }

                        } ) ).setField( reloadable( new DateTimeFormField() ) )// .setValidator(
                                                                               // new
                                                                               // NotNullValidator()
                                                                               // )
                .setLayoutData( left().top( lastLine ).create() ).create();
        newFormField( "Stichtag" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<RichtwertzoneZeitraumComposite>( selectedComposite, prefix
                                + "stichtag", new PropertyCallback<RichtwertzoneZeitraumComposite>() {

                            @Override
                            public Property get( RichtwertzoneZeitraumComposite entity ) {
                                return entity.stichtag();
                            }

                        } ) ).setField( reloadable( new DateTimeFormField() ) )// .setValidator(
                                                                               // new
                                                                               // NotNullValidator()
                                                                               // )
                .setLayoutData( right().top( lastLine ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( "€ pro m²" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<RichtwertzoneZeitraumComposite>( selectedComposite, prefix
                                + "euroQm", new PropertyCallback<RichtwertzoneZeitraumComposite>() {

                            @Override
                            public Property get( RichtwertzoneZeitraumComposite entity ) {
                                return entity.euroQm();
                            }

                        } ) ).setField( reloadable( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ) )
                .setValidator( new NumberValidator( Double.class, locale, 12, 2, 1, 2 ) )
                .setLayoutData( left().top( lastLine ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( "EB" )
                .setParent( parent )
                .setProperty(
                        new ReloadablePropertyAdapter<RichtwertzoneZeitraumComposite>( selectedComposite, prefix
                                + "erschliessungsBeitrag", new AssociationCallback<RichtwertzoneZeitraumComposite>() {

                            @Override
                            public Association get( RichtwertzoneZeitraumComposite entity ) {
                                return entity.erschliessungsBeitrag();
                            }

                        } ) ).setField( reloadable( namedAssocationsPicklist( ErschliessungsBeitragComposite.class ) ) )
                .setLayoutData( left().top( lastLine ).create() ).create();

        return newLine;
    }


    @Override
    protected EntityType addViewerColumns( FeatureTableViewer viewer ) {
        final KapsRepository repo = KapsRepository.instance();
        final EntityType<RichtwertzoneZeitraumComposite> type = repo.entityType( RichtwertzoneZeitraumComposite.class );

        PropertyDescriptor prop = null;
        prop = new PropertyDescriptorAdapter( type.getProperty( "name" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Bezeichnung" ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "gueltigAb" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Gültig ab" ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "stichtag" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Stichtag" ) );
        prop = new PropertyDescriptorAdapter( type.getProperty( "euroQm" ) );
        viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "€ pro m²" ) );

        return type;
    }


    @Override
    protected Iterable<RichtwertzoneZeitraumComposite> getElements() {
        return RichtwertzoneZeitraumComposite.Mixin.forZone( richtwertzone );
    }


    @Override
    protected RichtwertzoneZeitraumComposite createNewComposite()
            throws Exception {
        return repository.newEntity( RichtwertzoneZeitraumComposite.class, null,
                new EntityCreator<RichtwertzoneZeitraumComposite>() {

                    public void create( RichtwertzoneZeitraumComposite prototype )
                            throws Exception {
                        prototype.zone().set( richtwertzone );
                        prototype.schl().set( richtwertzone.schl().get() );
                        prototype.name().set( richtwertzone.name().get() );
                    }
                } );
    }
}
