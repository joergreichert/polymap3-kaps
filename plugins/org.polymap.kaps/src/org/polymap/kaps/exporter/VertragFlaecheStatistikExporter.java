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
package org.polymap.kaps.exporter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Charsets;

import org.eclipse.rwt.widgets.ExternalBrowser;

import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import org.polymap.core.data.operation.DefaultFeatureOperation;
import org.polymap.core.data.operation.DownloadServiceHandler;
import org.polymap.core.data.operation.DownloadServiceHandler.ContentProvider;
import org.polymap.core.data.operation.FeatureOperationExtension;
import org.polymap.core.data.operation.IFeatureOperation;
import org.polymap.core.data.operation.IFeatureOperationContext;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.data.FlurstueckComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.ui.NumberFormatter;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class VertragFlaecheStatistikExporter
        extends DefaultFeatureOperation
        implements IFeatureOperation {

    private static Log          log        = LogFactory.getLog( VertragFlaecheStatistikExporter.class );

    protected static DateFormat fileFormat = new SimpleDateFormat( "yyyy_MM_dd_HH_mm_ss" );

    private final static String DELIMITER  = ";";


    @Override
    public boolean init( IFeatureOperationContext ctx ) {
        super.init( ctx );
        try {
            return context.featureSource().getSchema().getName().getLocalPart().equals( VertragComposite.NAME );
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

        // final Date now = new Date();

        final File f = File.createTempFile( "Statistik_Flaeche", ".csv" );
        f.deleteOnExit();
        BufferedWriter out = new BufferedWriter( new FileWriter( f ) );

        final List<String> lines = new ArrayList<String>();
        final List<String> errors = new ArrayList<String>();

        try {
            export( context.features(), lines, monitor, errors );
            // TODO Fehler in Messagebox
            // write to f
            if (!lines.isEmpty()) {
                for (String line : lines) {
                    out.write( new String( line.getBytes(), Charsets.ISO_8859_1 ) );
                    out.write( "\n" );
                }
                out.flush();
            }
            if (!errors.isEmpty()) {
                final StringBuilder allErrors = new StringBuilder();
                int count = 0;
                for (String line : errors) {
                    allErrors.append( line ).append( "\n" );
                    count++;
                    if (count > 15) {
                        break;
                    }
                }
                if (count < errors.size()) {
                    allErrors.append( "\n" ).append( "Weitere " + (errors.size() - count) + " Fehler entfernt..." );
                }
                allErrors.append( "\n" ).append( "Es wurden " + lines.size() + " Zeilen exportiert." );

                Polymap.getSessionDisplay().asyncExec( new Runnable() {

                    public void run() {
                        MessageDialog.openError( PolymapWorkbench.getShellToParentOn(), "Fehler beim Export",
                                allErrors.toString() );
                    }
                } );
            }
            else {
                Polymap.getSessionDisplay().asyncExec( new Runnable() {

                    public void run() {
                        MessageDialog.openInformation( PolymapWorkbench.getShellToParentOn(), "Export durchgeführt",
                                "Es wurden " + lines.size() + " Zeilen exportiert." );
                    }
                } );
            }
        }
        catch (OperationCanceledException e) {
            return Status.Cancel;
        }
        finally {
            IOUtils.closeQuietly( out );
        }

        // open download
        if (!lines.isEmpty()) {
            Polymap.getSessionDisplay().asyncExec( new Runnable() {

                public void run() {

                    String url = DownloadServiceHandler.registerContent( new ContentProvider() {

                        public String getContentType() {
                            return "text/csv; charset=" + Charsets.ISO_8859_1.name();
                        }


                        public String getFilename() {
                            return "Statistik_Flaeche_" + fileFormat.format( new Date() ) + ".csv";
                        }


                        public InputStream getInputStream()
                                throws Exception {
                            return new BufferedInputStream( new FileInputStream( f ) );
                        }


                        public boolean done( boolean success ) {
                            f.delete();
                            return true;
                        }

                    } );

                    log.info( "TXT: download URL: " + url );

                    ExternalBrowser.open( "download_window", url, ExternalBrowser.NAVIGATION_BAR
                            | ExternalBrowser.STATUS );
                }
            } );
        }

        monitor.done();
        return Status.OK;
    }


    private void export( FeatureCollection features, List<String> lines, IProgressMonitor monitor, List<String> errors )
            throws IOException {

        final KapsRepository repo = KapsRepository.instance();

        FeatureIterator it = null;

        try {
            // reload the iterator
            it = features.features();
            int count = 0;

            double gesamteFlaeche = 0;
            double groessteFlaeche = 0;
            double kleinsteFlaeche = Double.MAX_VALUE;
            double gesamterKaufpreis = 0;
            double niedrigsterKaufpreis = Double.MAX_VALUE;
            double hoechsterKaufpreis = 0;

            while (it.hasNext()) {
                if (monitor.isCanceled()) {
                    throw new OperationCanceledException();
                }
                if ((++count % 100) == 0) {
                    monitor.subTask( "Objekte: " + count++ );
                    monitor.worked( 100 );
                }
                Feature feature = it.next();

                VertragComposite vertrag = repo.findEntity( VertragComposite.class, feature.getIdentifier().getID() );
                // all properties
                Double kaufpreis = vertrag.kaufpreis().get();
                if (kaufpreis != null) {
                    gesamterKaufpreis += kaufpreis;
                    if (kaufpreis > hoechsterKaufpreis) {
                        hoechsterKaufpreis = kaufpreis;
                    }
                    if (kaufpreis < niedrigsterKaufpreis) {
                        niedrigsterKaufpreis = kaufpreis;
                    }
                }

                Double flaeche = calculateVerkaufteFlaeche( vertrag );
                if (flaeche != null) {
                    gesamteFlaeche += flaeche;
                    if (flaeche > groessteFlaeche) {
                        groessteFlaeche = flaeche;
                    }
                    if (flaeche < kleinsteFlaeche) {
                        kleinsteFlaeche = flaeche;
                    }
                }
            }
            lines.add( new StringBuilder().append( "Gesamtzahl Kaufvertraege" ).append( DELIMITER ).append( count )
                    .toString() );
            NumberFormat numberFormat = NumberFormatter.getFormatter( 0, true );
            lines.add( new StringBuilder().append( "Gesamte verkaufte Flaeche" ).append( DELIMITER )
                    .append( numberFormat.format( gesamteFlaeche ) ).append( DELIMITER ).append( "qm" ).toString() );
            lines.add( new StringBuilder().append( "Groesste verkaufte Flaeche" ).append( DELIMITER )
                    .append( numberFormat.format( groessteFlaeche ) ).append( DELIMITER ).append( "qm" ).toString() );
            lines.add( new StringBuilder().append( "Kleinste verkaufte Flaeche" ).append( DELIMITER )
                    .append( numberFormat.format( kleinsteFlaeche ) ).append( DELIMITER ).append( "qm" ).toString() );
            lines.add( new StringBuilder().append( "Durchschnittlich verkaufte Flaeche" ).append( DELIMITER )
                    .append( numberFormat.format( gesamteFlaeche / count ) ).append( DELIMITER ).append( "qm" )
                    .toString() );
            lines.add( new StringBuilder().append( "Gesamter Kaufpreis" ).append( DELIMITER )
                    .append( numberFormat.format( gesamterKaufpreis ) ).append( DELIMITER ).append( "EUR" ).toString() );
            lines.add( new StringBuilder().append( "Groesster Kaufpreis" ).append( DELIMITER )
                    .append( numberFormat.format( hoechsterKaufpreis ) ).append( DELIMITER ).append( "EUR" ).toString() );
            lines.add( new StringBuilder().append( "Kleinster Kaufpreis" ).append( DELIMITER )
                    .append( numberFormat.format( niedrigsterKaufpreis ) ).append( DELIMITER ).append( "EUR" )
                    .toString() );
            lines.add( new StringBuilder().append( "Durchschnittlicher Kaufpreis" ).append( DELIMITER )
                    .append( numberFormat.format( gesamterKaufpreis / count ) ).append( DELIMITER ).append( "EUR" )
                    .toString() );
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
    }


    private double calculateVerkaufteFlaeche( VertragComposite vertrag ) {
        double verkaufteFlaeche = 0;

        for (FlurstueckComposite flurstueck : FlurstueckComposite.Mixin.forEntity( vertrag )) {
            if (flurstueck != null) {
                Double flaeche = flurstueck.flaeche().get();
                Double n = flurstueck.flaechenAnteilNenner().get();
                Double z = flurstueck.flaechenAnteilZaehler().get();

                if (flaeche != null && n != null && z != null && z != 0) {
                    verkaufteFlaeche += flaeche * z / n;
                }
            }
        }
        return verkaufteFlaeche;
    }
}
