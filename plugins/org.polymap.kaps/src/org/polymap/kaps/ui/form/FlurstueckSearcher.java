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

import java.util.ArrayList;
import java.util.List;

import org.opengis.feature.type.PropertyDescriptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;

import org.eclipse.ui.PlatformUI;

import org.polymap.core.data.ui.featuretable.DefaultFeatureTableColumn;
import org.polymap.core.data.ui.featuretable.FeatureTableViewer;
import org.polymap.core.data.ui.featuretable.IFeatureTableElement;
import org.polymap.core.model.EntityType;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.data.entityfeature.PropertyDescriptorAdapter;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;

import org.polymap.kaps.KapsPlugin;
import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.data.FlurComposite;
import org.polymap.kaps.model.data.FlurstueckComposite;
import org.polymap.kaps.model.data.FlurstueckVerkaufComposite;
import org.polymap.kaps.model.data.GemarkungComposite;
import org.polymap.kaps.ui.NamedCompositesFeatureContentProvider;

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public abstract class FlurstueckSearcher
        extends Action
        implements IFormFieldListener {

    private static Log                                   log = LogFactory.getLog( FlurstueckSearcher.class );

    private List<FlurstueckVerkaufComposite>                    content;

    private final String                                 prefix;

    private GemarkungComposite                           gemarkung;

    private FlurComposite                                flur;

    private Integer                                      nummer;

    private String                                       unterNummer;


    public FlurstueckSearcher( String prefix) {
        super( "Flurstück suchen" );
        this.prefix = prefix;

        setToolTipText( "Flurstück suchen" );
        // setImageDescriptor( BiotopPlugin.imageDescriptorFromPlugin(
        // BiotopPlugin.PLUGIN_ID, "icons/add.gif" ) );
        setEnabled( false );
    }


    protected abstract void adopt( FlurstueckVerkaufComposite element )
            throws Exception;


    public void run() {
        try {

            // FlurstueckComposite template = provider.get();
            // if (template != null) {
            // // set the search fields to the default values from the selected
            // // template
            // if (gemarkung == null) {
            // gemarkung = template.gemarkung().get();
            // }
            // if (flur == null) {
            // flur = template.flur().get();
            // }
            // if (nummer == null) {
            // nummer = template.nummer().get();
            // }
            // if (unterNummer == null) {
            // unterNummer = template.unterNummer().get();
            // }
            // }
            content = new ArrayList();
            for (FlurstueckVerkaufComposite fc : KapsRepository.instance().findFlurstuecke( gemarkung, flur, nummer,
                    unterNummer )) {
                content.add( fc );
            }

            FlurstueckTableDialog dialog = new FlurstueckTableDialog();
            dialog.setBlockOnOpen( true );

            if (dialog.open() == Window.OK) {
                assert dialog.sel.length == 1 : "Selected: " + dialog.sel.length;
                final IFeatureTableElement sel = dialog.sel[0];
                adopt( Iterables.find( content, new Predicate<FlurstueckVerkaufComposite>() {

                    public boolean apply( FlurstueckVerkaufComposite input ) {
                        return input.id().equals( sel.fid() );
                    }
                } ) );
            }
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( KapsPlugin.PLUGIN_ID, this, "Fehler beim Öffnen der Flurstückstabelle.", e );
        }
    }


    /**
     * 
     */
    class FlurstueckTableDialog
            extends TitleAreaDialog {

        private FeatureTableViewer     viewer;

        private IFeatureTableElement[] sel;


        public FlurstueckTableDialog() {
            super( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() );
            setShellStyle( getShellStyle() | SWT.RESIZE );
        }


        protected Image getImage() {
            return getShell().getDisplay().getSystemImage( SWT.ICON_QUESTION );
        }


        protected Point getInitialSize() {
            return new Point( 800, 600 );
            // return super.getInitialSize();
        }


        protected Control createDialogArea( Composite parent ) {
            Composite area = (Composite)super.createDialogArea( parent );

            setTitle( "Gefundene Flurstücke" );
            setMessage( "Wählen Sie das Flurstück." );

            viewer = new FeatureTableViewer( area, SWT.V_SCROLL | SWT.H_SCROLL );
            viewer.getTable().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

            // entity types
            final KapsRepository repo = KapsRepository.instance();
            final EntityType<FlurstueckComposite> type = repo.entityType( FlurstueckComposite.class );

            // columns
            PropertyDescriptor prop = null;
            prop = new PropertyDescriptorAdapter( type.getProperty( "name" ) );
            viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Vertragsnummer" ) );
            prop = new PropertyDescriptorAdapter( type.getProperty( "gemarkung" ) );
            viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Gemarkung" ) );
            prop = new PropertyDescriptorAdapter( type.getProperty( "flur" ) );
            viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Flur" ) );
            prop = new PropertyDescriptorAdapter( type.getProperty( "nummer" ) );
            viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Flurstück" ) );
            prop = new PropertyDescriptorAdapter( type.getProperty( "unterNummer" ) );
            viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Unternummer" ) );
            // prop = new PropertyDescriptorAdapter( type.getProperty(
            // "hauptFlurstueck" ) );
            // viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader(
            // "Hauptflurstück" ) );
            prop = new PropertyDescriptorAdapter( type.getProperty( "strasse" ) );
            viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Straße" ) );
            prop = new PropertyDescriptorAdapter( type.getProperty( "hausnummer" ) );
            viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Hausnummer" ) );

            // model/content
            viewer.setContent( new NamedCompositesFeatureContentProvider( content, type ) );
            viewer.setInput( content );

            // selection
            viewer.addSelectionChangedListener( new ISelectionChangedListener() {

                public void selectionChanged( SelectionChangedEvent ev ) {
                    sel = viewer.getSelectedElements();
                    getButton( IDialogConstants.OK_ID ).setEnabled( sel.length > 0 );
                }
            } );

            area.pack();
            return area;
        }


        protected void createButtonsForButtonBar( Composite parent ) {
            super.createButtonsForButtonBar( parent );
            // createButton( parent, RESET_BUTTON, "Zurücksetzen", false );

            getButton( IDialogConstants.OK_ID ).setEnabled( false );
        }

    }


    public void fieldChange( FormFieldEvent ev ) {
        if (ev.getEventCode() == IFormFieldListener.VALUE_CHANGE) {
            String fieldName = ev.getFieldName();
            if (fieldName.equalsIgnoreCase( prefix + "gemarkung" )) {
                gemarkung = (GemarkungComposite)ev.getNewValue();
                setEnabled( true );
            }
            else if (fieldName.equalsIgnoreCase( prefix + "flur" )) {
                flur = (FlurComposite)ev.getNewValue();
                setEnabled( true );
            }
            else if (fieldName.equalsIgnoreCase( prefix + "nummer" )) {
                nummer = (Integer)ev.getNewValue();
                setEnabled( true );
            }
            else if (fieldName.equalsIgnoreCase( prefix + "unterNummer" )) {
                unterNummer = (String)ev.getNewValue();
                setEnabled( true );
            }
        }
    }


    /**
     *
     */
    public void refresh() {
        setEnabled( false );
        gemarkung = null;
        flur = null;
        nummer = null;
        unterNummer = null;
    }
}
