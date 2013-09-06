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

import java.util.Locale;

import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import org.polymap.core.data.operation.DefaultFeatureOperation;
import org.polymap.core.data.operation.FeatureOperationExtension;
import org.polymap.core.data.operation.IFeatureOperation;
import org.polymap.core.data.operation.IFeatureOperationContext;
import org.polymap.core.model.CompletionException;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.entity.ConcurrentModificationException;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.data.RichtwertzoneComposite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class RichtwertzoneKoordinateImporter
        extends DefaultFeatureOperation
        implements IFeatureOperation {

    private static Log          log        = LogFactory.getLog( RichtwertzoneKoordinateImporter.class );

    // Locale wegen dem . in 0.00
    private static NumberFormat euroFormat = NumberFormat.getNumberInstance( Locale.ENGLISH );
    static {
        euroFormat.setMaximumFractionDigits( 2 );
        euroFormat.setMinimumFractionDigits( 2 );
        euroFormat.setMinimumIntegerDigits( 1 );
    }

    private static DateFormat   dateFormat = new SimpleDateFormat( "dd.MM.yyyy" );


    @Override
    public boolean init( IFeatureOperationContext ctx ) {
        super.init( ctx );
        try {
            return context.featureSource().getSchema().getName().getLocalPart().startsWith( "Bodenrichtwertzonen" );
        }
        catch (Exception e) {
            log.warn( "", e );
            return false;
        }
    }


    @Override
    public Status execute( IProgressMonitor monitor )
            throws Exception {
        monitor.beginTask( context.adapt( FeatureOperationExtension.class ).getLabel(), context.features().size() );

        String errors = "";
        try {
            errors = importFeature( context.features(), monitor );
        }
        catch (OperationCanceledException e) {
            return Status.Cancel;
        }

        monitor.done();
        if (errors.length() != 0) {
            final String errors2 = errors;
            Polymap.getSessionDisplay().asyncExec( new Runnable() {
                public void run() {
                    MessageDialog.openError( PolymapWorkbench.getShellToParentOn(), "Fehler beim Import", errors2 );
                }
            });
            return Status.Error;
        }
        return Status.OK;
    }


    private String importFeature( final FeatureCollection features, final IProgressMonitor monitor )
            throws IOException, CompletionException, ConcurrentModificationException {
        FeatureIterator it = null;
        try {

            it = features.features();
            int count = 0;
            final KapsRepository repo = KapsRepository.instance();
            StringBuffer errors = new StringBuffer();
            while (it.hasNext()) {
                if (monitor.isCanceled()) {
                    throw new OperationCanceledException();
                }
                if ((++count % 100) == 0) {
                    monitor.subTask( "Objekte importiert: " + count++ );
                    monitor.worked( 100 );
                }
                Feature feature = it.next();
                String number = (String)feature.getProperty( "BTS" ).getValue();
                if (number != null && !number.isEmpty()) {
                    RichtwertzoneComposite richtwertzone = repo.findSchlNamed( RichtwertzoneComposite.class, number );
                    if (richtwertzone == null) {
                        errors.append( "- Richtwertzone für BTS '" + number + "' wurde nicht gefunden\n" );
                    }
                    else {
                        MultiPolygon mp = (MultiPolygon)feature.getDefaultGeometryProperty().getValue();
                        if (mp != null) {
                            if (mp.getNumGeometries() == 1) {
                                richtwertzone.geom().set( (Polygon)mp.getGeometryN( 0 ) );
                            }
                            else if (mp.getNumGeometries() > 1) {
                                errors.append( "- Richtwertzone für BTS '" + number + "' enthält mehr als 1 Polygon\n" );
                            }
                        }
                    }
                }
            }
            repo.commitChanges();
            return errors.toString();
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
    }
}
