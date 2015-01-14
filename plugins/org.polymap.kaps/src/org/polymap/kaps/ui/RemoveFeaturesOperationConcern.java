package org.polymap.kaps.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.data.operation.FeatureOperationContainer;
import org.polymap.core.data.operation.IFeatureOperationContext;
import org.polymap.core.data.operations.feature.RemoveFeaturesOperation;
import org.polymap.core.operation.IOperationConcernFactory;
import org.polymap.core.operation.OperationConcernAdapter;
import org.polymap.core.operation.OperationInfo;

public class RemoveFeaturesOperationConcern
        extends IOperationConcernFactory {

    private static Log log = LogFactory.getLog( RemoveFeaturesOperationConcern.class );


    // public static IMessages i18n = Messages.forPrefix(
    // "NewFeatureOperationConcern" );

    public static class BooleanStore {
        boolean value = false;
    }


    public IUndoableOperation newInstance( final IUndoableOperation op, final OperationInfo info ) {
        if (op instanceof FeatureOperationContainer
                && ((FeatureOperationContainer)op).getDelegate() instanceof RemoveFeaturesOperation) {

            return new OperationConcernAdapter() {

                public IStatus execute( IProgressMonitor monitor, IAdaptable _info )
                        throws ExecutionException {

                    IFeatureOperationContext context = ((RemoveFeaturesOperation)((FeatureOperationContainer)op)
                            .getDelegate()).getContext();
                    final BooleanStore cancelled = new BooleanStore();
                    try {
                        if (context.features().size() == 1) {
                            // nur nachfragen bei Einzelauswahl
                            final Display display = (Display)_info.getAdapter( Display.class );
                            display.syncExec( new Runnable() {

                                @Override
                                public void run() {
                                    if (!MessageDialog.openQuestion( display.getActiveShell(), "Löschen",
                                            "Sind Sie sicher, dass Sie dieses Objekt komplett löschen möchten?" )) {
                                        cancelled.value = true;
                                    }
                                }
                            } );
                        }
                    }
                    catch (Exception e) {
                        throw new ExecutionException( "exception during check for deletion", e );
                    }

                    return cancelled.value ? Status.CANCEL_STATUS : info.next().execute( monitor, _info );
                }


                public IStatus redo( IProgressMonitor monitor, IAdaptable _info )
                        throws ExecutionException {
                    return info.next().redo( monitor, info );
                }


                public IStatus undo( IProgressMonitor monitor, IAdaptable _info )
                        throws ExecutionException {
                    return info.next().undo( monitor, info );
                }


                protected OperationInfo getInfo() {
                    return info;
                }

            };
        }
        return null;
    }
}
