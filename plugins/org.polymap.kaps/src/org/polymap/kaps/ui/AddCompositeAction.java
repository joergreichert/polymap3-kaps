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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import org.polymap.core.model.Entity;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.kaps.KapsPlugin;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public abstract class AddCompositeAction<A extends Entity>
        extends Action
        implements ISelectionChangedListener {

    private static Log  log = LogFactory.getLog( AddCompositeAction.class );

    public AddCompositeAction() {
        super( "Neu" );
        setToolTipText( "Eintrag erzeugen" );
        setImageDescriptor( KapsPlugin.imageDescriptorFromPlugin( KapsPlugin.PLUGIN_ID,
                "icons/add.gif" ) );
        setEnabled( true );
    }


    public void selectionChanged( SelectionChangedEvent ev ) {
    }


    protected abstract void execute()
            throws Exception;


    public void run() {
        try {
            execute();
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( KapsPlugin.PLUGIN_ID, this, "Fehler beim Anlegen.", e );
        }
    }
}
