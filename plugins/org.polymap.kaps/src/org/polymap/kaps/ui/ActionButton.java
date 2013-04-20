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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class ActionButton
        extends Button {

    private static Log log = LogFactory.getLog( ActionButton.class );
    
    private IAction         delegate;
    
    
    public ActionButton( Composite parent, IAction delegate ) {
        super( parent, SWT.PUSH );
        this.delegate = delegate;
        setText( delegate.getText() );
        setToolTipText( delegate.getToolTipText() );
//        setImage( TwvPlugin.getDefault().imageForDescriptor( 
//                delegate.getImageDescriptor(), delegate.getText() ) );

        setEnabled( delegate.isEnabled() );
        
        delegate.addPropertyChangeListener( new IPropertyChangeListener() {
            public void propertyChange( PropertyChangeEvent ev ) {
                log.info( "prop change: " + ev );
                if (ev.getProperty().equalsIgnoreCase( "enabled" )) {
                    setEnabled( (Boolean)ev.getNewValue() );
                }
            }
        });

        addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev ) {
                ActionButton.this.delegate.run();
            }
        });
    }
    
}
