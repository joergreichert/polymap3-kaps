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

import java.util.Collections;
import java.util.SortedMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import org.polymap.rhei.form.IFormEditorPageSite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public abstract class SimplePickList<T> {

    private Combo                combo;

    private SortedMap<String, T> values;

    private T                    lastSelection;

    private static Log           log = LogFactory.getLog( SimplePickList.class );


    public SimplePickList( Composite parent, IFormEditorPageSite pageSite ) {
        combo = pageSite.getToolkit().createCombo( parent, Collections.EMPTY_SET, SWT.READ_ONLY | SWT.SINGLE );
        combo.addSelectionListener( new SelectionListener() {

            @Override
            public void widgetSelected( SelectionEvent e ) {
                String selected = combo.getText();
                if (values != null) {
                    onSelectionInternal( values.get( selected ) );
                }
            }


            @Override
            public void widgetDefaultSelected( SelectionEvent e ) {
            }
        } );
    }


    public void setEnabled( boolean enabled ) {
        onSelectionInternal( null );
        combo.deselectAll();
        combo.removeAll();

        if (enabled) {
            values = getValues();
            for (String label : values.keySet()) {
                combo.add( label );
            }
            if (!values.keySet().isEmpty()) {
                combo.select( 0 );
                onSelectionInternal( values.get( combo.getText()) );
            }
        }
        combo.setEnabled( enabled );
    }


    private void onSelectionInternal( T object ) {
        lastSelection = object;
        onSelection( object );
    }


    protected abstract SortedMap<String, T> getValues();


    protected abstract void onSelection( T select );


    public SimplePickList setLayoutData( FormData data ) {
        combo.setLayoutData( data );
        return this;
    }


    public T getSelection() {
        return lastSelection;
    }
}
