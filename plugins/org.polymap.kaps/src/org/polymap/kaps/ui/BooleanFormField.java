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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.IFormFieldSite;
import org.polymap.rhei.form.IFormEditorToolkit;
import org.polymap.rhei.internal.form.FormEditorToolkit;

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class BooleanFormField
        implements IFormField {

    private static Log           log             = LogFactory.getLog( BooleanFormField.class );

    public static final int      FORCE_MATCH     = 1;

    public static final int      TEXT_EDITABLE   = 2;

    private IFormFieldSite       site;

    private Combo                combo;

    private Object               loadedValue;

    private List<ModifyListener> modifyListeners = new ArrayList<ModifyListener>();

    private Map<String, String>  values          = new HashMap<String, String>();


    public BooleanFormField() {
        values.put( "ja", "J" );
        values.put( "nein", "N" );
        values.put( "unbekannt", "U" );
    }


    public void init( IFormFieldSite _site ) {
        this.site = _site;
    }


    public void dispose() {
        combo.dispose();
    }


    /**
     * Add a raw {@link ModifyListener} to the combo of this picklist. This listener
     * is called when the user types into the textfield of the combo.
     */
    public void addModifyListener( ModifyListener l ) {
        modifyListeners.add( l );
        if (combo != null) {
            combo.addModifyListener( l );
        }
    }


    public Control createControl( Composite parent, IFormEditorToolkit toolkit ) {
        int comboStyle = SWT.DROP_DOWN | SWT.READ_ONLY;
        combo = toolkit.createCombo( parent, Collections.EMPTY_SET, comboStyle | SWT.MULTI );

        for (ModifyListener l : modifyListeners) {
            combo.addModifyListener( l );
        }

        // add values
        fillCombo();
        // selection listener
        combo.addSelectionListener( new SelectionListener() {

            public void widgetSelected( SelectionEvent ev ) {
                log.debug( "widgetSelected(): selectionIndex= " + combo.getSelectionIndex() );

                int i = 0;

                // for (String label : values.keySet()) {
                // if (i++ == combo.getSelectionIndex()) {
                // combo.setText( label );
                // break;
                // }
                // }
                Object value = values.get( combo.getText() );
                site.fireEvent( BooleanFormField.this, IFormFieldListener.VALUE_CHANGE, value );
            }


            public void widgetDefaultSelected( SelectionEvent ev ) {
            }
        } );

        // focus listener
        combo.addFocusListener( new FocusListener() {

            public void focusLost( FocusEvent event ) {
                combo.setBackground( FormEditorToolkit.textBackground );
                site.fireEvent( this, IFormFieldListener.FOCUS_LOST, combo.getText() );
            }


            public void focusGained( FocusEvent event ) {
                combo.setBackground( FormEditorToolkit.textBackgroundFocused );
                site.fireEvent( this, IFormFieldListener.FOCUS_GAINED, combo.getText() );
            }
        } );

        return combo;
    }


    private void fillCombo() {
        for (String entry : values.keySet()) {
            combo.add( entry );
        }
    }


    public IFormField setEnabled( boolean enabled ) {
        combo.setEnabled( enabled );
        return this;
    }


    public IFormField setValue( Object value ) {
        // combo.setText( "" );
        combo.deselectAll();

        // find label for given value
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if ((value == null && entry.getValue() != null && entry.getValue().equals( "U" ))
                    || (value != null && value.equals( entry.getValue() ))) {
                combo.setText( entry.getKey() );
                break;
            }
        }
        return this;
    }


    /**
     * Returns the current value depending on the {@link #forceTextMatch} flag. If
     * true, then the current text of the {@link #combo} is returned only if it
     * matches one of the labels. Otherwise the text is returned as is.
     * 
     * @return
     */
    protected Object getValue() {
        String text = combo.getText();
        return values.get( text );
    }


    public void load()
            throws Exception {
        assert combo != null : "Control is null, call createControl() first.";

        loadedValue = site.getFieldValue();
        setValue( loadedValue );

        // int i = 0;
        // for (Iterator it=values.get().values().iterator(); it.hasNext(); i++) {
        // if (it.next().equals( loadedValue )) {
        // combo.select( i );
        // return;
        // }
        // }
    }


    public void store()
            throws Exception {
        site.setFieldValue( getValue() );
    }


    public void reloadValues() {
        if (combo == null) {
            throw new IllegalStateException( "createControl must be called before" );
        }
        combo.removeAll();
        fillCombo();
        // combo.layout( true, true );
        // combo.redraw();
    }
}
