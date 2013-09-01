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
package org.polymap.kaps.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.data.ui.featuretable.FeatureTableViewer;
import org.polymap.core.model.Entity;
import org.polymap.core.model.EntityType;
import org.polymap.core.project.ui.util.SimpleFormData;

import org.polymap.rhei.data.entityfeature.ReloadablePropertyAdapter.CompositeProvider;
import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.ui.NamedCompositesFeatureContentProvider.FeatureTableElement;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public abstract class KapsDefaultFormEditorPageWithFeatureTable<T extends Entity>
        extends KapsDefaultFormEditorPage {

    
    public class LastNameInvocationHandler
            implements InvocationHandler {

        private String lastCall;

        @Override
        public Object invoke( Object proxy, Method method, Object[] args )
                throws Throwable {
            // do nothing
            if ("toString".equals( method.getName() )) {
                return lastCall;
            }
            lastCall = method.getName();
            return null;
        }

    }


    protected FeatureStore         featureStore;

    private FeatureTableViewer     viewer;

    private boolean                dirty;

    private List<T>                model             = new ArrayList<T>();

    protected CompositeProvider<T> selectedComposite = new CompositeProvider<T>();

    private List<IFormField>       reloadables       = new ArrayList<IFormField>();

    private boolean                updatingElements;
    
    protected final T nameTemplate;

    public KapsDefaultFormEditorPageWithFeatureTable( Class<T> type, String id, String title, Feature feature,
            FeatureStore featureStore ) {
        super( id, title, feature, featureStore );
        nameTemplate = (T)Proxy.newProxyInstance( Thread.currentThread().getContextClassLoader(), new Class[]{type}, new LastNameInvocationHandler() );
    }


    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );
    }


    protected abstract EntityType addViewerColumns( FeatureTableViewer viewer );


    protected void refreshReloadables()
            throws Exception {
        boolean enabled = selectedComposite.get() != null;
        for (IFormField field : reloadables) {
            field.setEnabled( enabled );
            field.load();
        }
        dirty = false;
    }


    protected IFormField reloadable( IFormField formField ) {
        reloadables.add( formField );
        return formField;
    }


    /**
     * 
     * @param parent
     */
    protected Composite createTableForm( Composite parent, Control top ) {
        return createTableForm( parent, top, false );
    }


    protected Composite createTableForm( Composite parent, Control top, boolean addAllowed ) {

        int TOPSPACING = 20;
        viewer = new FeatureTableViewer( parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL );
        viewer.getTable().setLayoutData(
                new SimpleFormData().fill().left( 0 ).height( 100 ).right( 85 ).top( top, TOPSPACING ).bottom( 100 )
                        .create() );

        // columns
        EntityType<T> type = addViewerColumns( viewer );

        // model/content
        viewer.setContent( new NamedCompositesFeatureContentProvider( null, type ) );
        try {
            doLoad( new NullProgressMonitor() );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }

        ActionButton addBtn = null;
        if (addAllowed) {
            AddCompositeAction<T> addAction = new AddCompositeAction<T>() {

                protected void execute()
                        throws Exception {

                    dirty = true;
                    T newComposite = createNewComposite();
                    selectedComposite.set( newComposite );
                    model.add( 0, newComposite );

                    doLoad( new NullProgressMonitor() );
                    viewer.getTable().deselectAll();
                    viewer.getTable().select(
                            ((NamedCompositesFeatureContentProvider)viewer.getContentProvider())
                                    .getIndicesForElements( newComposite ) );
                    // refreshReloadables();
                    // refreshReloadables is triggered bei selection event();
                }
            };
            addBtn = new ActionButton( parent, addAction );
            addBtn.setLayoutData( new SimpleFormData().left( viewer.getTable(), 6 ).top( top, TOPSPACING ).right( 100 )
                    .height( 30 ).create() );
        }

        DeleteCompositeAction<T> deleteAction = new DeleteCompositeAction<T>() {

            protected void execute()
                    throws Exception {

                if (selectedComposite.get() != null) {
                    T toSelect = selectedComposite.get();
                    model.remove( toSelect );
                    if (viewer != null) {
                        Collection<T> viewerInput = (Collection<T>)viewer.getInput();
                        viewerInput.remove( toSelect );
                    }
                    repository.removeEntity( toSelect );
                    selectedComposite.set( null );

                    // pageSite.reloadEditor();
                    doLoad( new NullProgressMonitor() );
                    refreshReloadables();

                    dirty = true;
                    // pageSite.fireEvent( this, id,
                    // IFormFieldListener.VALUE_CHANGE, null );
                    // viewer.refresh( true );

                }
            }
        };
        ActionButton delBtn = new ActionButton( parent, deleteAction );
        delBtn.setLayoutData( new SimpleFormData().left( viewer.getTable(), 6 )
                .top( addBtn != null ? addBtn : top, addBtn != null ? SPACING : TOPSPACING ).right( 100 ).height( 30 )
                .create() );

        parent.layout( true );

        viewer.addSelectionChangedListener( new ISelectionChangedListener() {

            @Override
            public void selectionChanged( SelectionChangedEvent event ) {
                if (!updatingElements) {
                    // Felder für ausgewählten Eintrag in UI wurden geändert
                    // keine Selektion hier
                    StructuredSelection selection = (StructuredSelection)event.getSelection();
                    FeatureTableElement tableRow = (FeatureTableElement)selection.getFirstElement();
                    if (tableRow != null) {
                        selectedComposite.set( (T)tableRow.getComposite() );
                        try {
                            refreshReloadables();
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } );
        return viewer.getTable();
    }


    /**
     * 
     * @return
     */
    protected T createNewComposite()
            throws Exception {
        // must only be implemented if add-action on table is enabled
        throw new RuntimeException( "not yet implemented." );
    }


    public void updateElements( Collection<T> coll ) {
        if (viewer != null && !viewer.isBusy()) {
            updatingElements = true;
            viewer.refresh( true );
            updatingElements = false;
        }
    }


    protected abstract Iterable<T> getElements();


    public void doLoad( IProgressMonitor monitor )
            throws Exception {
        if (viewer != null && !viewer.isBusy()) {
            // model = new HashMap();
            for (T elm : getElements()) {
                if (!model.contains( elm )) {
                    // TODO wie wird der EventHandler registriert?
                    // elm.addPropertyChangeListener( this );
                    model.add( elm );
                }
            }
            // transform to list

            viewer.setInput( model );
            // viewer.refresh( true );
        }

        if (pageSite != null) {
            refreshReloadables();
        }
        super.doLoad( monitor );
        dirty = false;
    }


    public void doSubmit( IProgressMonitor monitor )
            throws Exception {
        if (model != null) {
            updateElements( model );
        }
        super.doSubmit( monitor );
        dirty = false;
    }


    /**
     * Handles Value property changes.
     */
    public void propertyChange( PropertyChangeEvent evt ) {
        try {
            dirty = true;
            // update dirty/valid flags of the editor
            pageSite.fireEvent( this, id, IFormFieldListener.VALUE_CHANGE, null );
            if (!viewer.isBusy()) {
                viewer.refresh( true );
            }
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }

}