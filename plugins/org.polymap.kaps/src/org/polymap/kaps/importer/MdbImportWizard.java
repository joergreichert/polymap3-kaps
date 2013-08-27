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
package org.polymap.kaps.importer;

import java.beans.PropertyChangeEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;

import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.operation.OperationSupport;
import org.polymap.core.qi4j.event.AbstractModelChangeOperation;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.kaps.KapsPlugin;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class MdbImportWizard
        extends Wizard
        implements IImportWizard {

    private static Log    log = LogFactory.getLog( MdbImportWizard.class );

    private MdbImportPage importPage;


    public MdbImportWizard() {
    }


    public void init( IWorkbench workbench, IStructuredSelection selection ) {
        addPage( importPage = new MdbImportPage() );
    }


    public void dispose() {
    }


    public boolean canFinish() {
        return true;
    }


    public boolean performFinish() {
        try {
            // MdbImportOperation op =
            AbstractModelChangeOperation op = new AbstractModelChangeOperation( "WinAKPS importieren" ) {

                @Override
                protected IStatus doExecute( IProgressMonitor monitor, IAdaptable info )
                        throws Exception {
                    new MdbImportOperation( importPage.dbFile, importPage.tableNames ).doExecute( monitor, info );
                    new MdbImportWohneigentumOperation( importPage.dbFile, importPage.tableNames ).doExecute( monitor,
                          info );
                    new MdbImportBewertungenOperation( importPage.dbFile, importPage.tableNames ).doExecute( monitor,
                            info );
                    return Status.OK_STATUS;
                }
                
                @Override
                public void propertyChange( PropertyChangeEvent ev ) {
                    // do nothing
                    // undo not supported
                }
            };
            OperationSupport.instance().execute( op, true, true );

            return true;
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( KapsPlugin.PLUGIN_ID, this, "Fehler beim importieren.", e );
            return false;
        }
    }

}
