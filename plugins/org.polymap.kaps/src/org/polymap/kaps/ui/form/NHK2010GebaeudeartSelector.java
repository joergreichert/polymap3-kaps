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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;

import org.eclipse.ui.PlatformUI;

import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.form.IFormEditorToolkit;

import org.polymap.kaps.KapsPlugin;
import org.polymap.kaps.model.NHK2010GebaeudeartenProvider;
import org.polymap.kaps.model.data.NHK2010Gebaeudeart;

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public abstract class NHK2010GebaeudeartSelector
        extends Action {

    private static Log               log = LogFactory.getLog( NHK2010GebaeudeartSelector.class );

    private final IFormEditorToolkit toolkit;


    public NHK2010GebaeudeartSelector( IFormEditorToolkit iFormEditorToolkit ) {
        super( "Auswahl" );
        this.toolkit = iFormEditorToolkit;

        setToolTipText( "Auswahl der NHK 2010 - Gebäudeart" );
        // setImageDescriptor( BiotopPlugin.imageDescriptorFromPlugin(
        // BiotopPlugin.PLUGIN_ID, "icons/add.gif" ) );
        setEnabled( false );
    }


    protected abstract void adopt( NHK2010Gebaeudeart element )
            throws Exception;


    public void run() {
        try {

            TreeDialog dialog = new TreeDialog();
            dialog.setBlockOnOpen( true );

            if (dialog.open() == Window.OK) {
                assert dialog.result != null : "Selected: " + dialog.result;

                adopt( dialog.result );
            }
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( KapsPlugin.PLUGIN_ID, this, "Fehler beim Öffnen der Flurstückstabelle.", e );
        }
    }


    /**
     * 
     */
    class TreeDialog
            extends TitleAreaDialog {

        private TreeViewer         viewer;

        private NHK2010Gebaeudeart result;


        public TreeDialog() {
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

            setTitle( "Gebäudearten nach NHK 2010" );
            setMessage( "Wählen Sie die Gebäudeart." );

            // toolkit.createTree( parent, style );
            viewer = new TreeViewer( area, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER );

            Tree tree = viewer.getTree();
            tree.setLinesVisible( true );
            tree.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
//            tree.setLayoutData( new SimpleFormData().fill().create()  );
            tree.setHeaderVisible( false );

            viewer.setContentProvider( NHK2010GebaeudeartenProvider.instance() );
            viewer.setInput( "_" );
            // viewer
            viewer.setLabelProvider( new LabelProvider() {
                @Override
                public String getText( Object element ) {
                    return ((NHK2010Gebaeudeart)element).getName();
                }
            } );

            viewer.addSelectionChangedListener( new ISelectionChangedListener() {

                @Override
                public void selectionChanged( SelectionChangedEvent event ) {
                    ISelection sel = event.getSelection();
                    if (sel != null && sel instanceof IStructuredSelection) {
                        Object elm = ((IStructuredSelection)sel).getFirstElement();
                        if (elm != null && elm instanceof NHK2010Gebaeudeart) {
                            result = (NHK2010Gebaeudeart)elm;
                        }
                    }
                    getButton( IDialogConstants.OK_ID ).setEnabled( result != null && result.isSelectable() );
                }
            } );

            area.pack();
            return area;
        }


        protected void createButtonsForButtonBar( Composite parent ) {
            super.createButtonsForButtonBar( parent );

            getButton( IDialogConstants.OK_ID ).setEnabled( false );
        }

    }
}
