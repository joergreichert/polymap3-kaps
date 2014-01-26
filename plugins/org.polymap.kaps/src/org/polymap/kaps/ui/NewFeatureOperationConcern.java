package org.polymap.kaps.ui;

import java.util.ArrayList;
import java.util.List;

import net.refractions.udig.catalog.IGeoResource;

import org.opengis.feature.Feature;

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

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.operations.NewFeatureOperation;
import org.polymap.core.operation.IOperationConcernFactory;
import org.polymap.core.operation.OperationConcernAdapter;
import org.polymap.core.operation.OperationInfo;
import org.polymap.core.project.ILayer;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.data.entityfeature.EntityProvider;
import org.polymap.rhei.data.entityfeature.catalog.EntityGeoResourceImpl;
import org.polymap.rhei.filter.IFilter;

import org.polymap.kaps.model.KapsEntityProvider;
import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.data.GebaeudeComposite;
import org.polymap.kaps.model.data.RichtwertzoneComposite;
import org.polymap.kaps.model.data.WohnungComposite;

public class NewFeatureOperationConcern
        extends IOperationConcernFactory {

    private static Log log = LogFactory.getLog( NewFeatureOperationConcern.class );


    // public static IMessages i18n = Messages.forPrefix(
    // "NewFeatureOperationConcern" );

    public IUndoableOperation newInstance( final IUndoableOperation op, final OperationInfo info ) {
        if (op instanceof NewFeatureOperation) {

            return new OperationConcernAdapter() {

                public IStatus execute( IProgressMonitor monitor, IAdaptable _info )
                        throws ExecutionException {

                    try {

                        final KapsRepository repo = KapsRepository.instance();
                        ILayer layer = ((NewFeatureOperation)op).getLayer();
                        IGeoResource geores = layer.getGeoResource();

                        String message = null;
                        if (geores instanceof EntityGeoResourceImpl) {
                            EntityProvider provider = geores.resolve( EntityProvider.class, null );
                            if (provider != null && provider instanceof KapsEntityProvider) {
                                Class type = provider.getEntityType().getType();
                                if (type.isAssignableFrom( GebaeudeComposite.class )) {
                                    message = "Legen Sie neue Gebäude bitte im Formular für Wohneigentum an.";
                                } else if (type.isAssignableFrom( WohnungComposite.class )) {
                                    message = "Legen Sie neue Wohnungen bitte im Formular für Gebäude an.";
                                }
                            }
                        }
                        if (message != null) {
                            final String fMessage = message;
                            Display display = (Display)_info.getAdapter( Display.class );
                            display.asyncExec( new Runnable() {

                                public void run() {
                                    MessageDialog.openWarning( PolymapWorkbench.getShellToParentOn(),
                                            "Anlegen nicht möglich", fMessage );
                                }
                            } );
                            return Status.CANCEL_STATUS;
                        }
                        else {
                            return info.next().execute( monitor, _info );
                        }

                    }
                    catch (Exception e) {
                        PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this,
                                "Das Formular zum Bearbeiten des neuen Objektes konnte nicht geöffnet werden.", e );
                    }
                    return Status.CANCEL_STATUS;
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
