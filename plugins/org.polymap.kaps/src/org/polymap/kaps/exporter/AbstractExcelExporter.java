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
import java.util.Locale;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.entity.association.Association;
import org.qi4j.api.property.Property;

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
import org.polymap.core.model.Entity;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.LabelSupport;
import org.polymap.kaps.model.SchlNamed;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.ui.NumberFormatter;
import org.polymap.kaps.ui.form.EingangsNummerFormatter;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public abstract class AbstractExcelExporter<T extends Entity>
        extends DefaultFeatureOperation
        implements IFeatureOperation {

    public static class Value {

        private String key;

        private String value;


        public Value( String key, String value ) {
            this.key = key;
            this.value = value;
        }


        public Value( String key, Date value ) {
            this( key, value != null ? dateFormat.format( value ) : "" );
        }


        public Value( String key, Double value, int fractionDigits ) {
            this( key, value, fractionDigits, true );
        }


        public Value( String key, Double value, int fractionDigits, boolean useGrouping ) {
            this( key, value != null ? NumberFormatter.getFormatter( fractionDigits, useGrouping ).format( value ) : "" );
        }


        public Value( String key, Long value ) {
            this( key, value != null ? NumberFormatter.getFormatter( 0 ).format( value ) : "" );
        }


        public Value( String key, Integer value ) {
            this( key, value, false );
        }


        public Value( String key, Integer value, boolean useGrouping ) {
            this( key, value != null ? NumberFormatter.getFormatter( 0, useGrouping ).format( value ) : "" );
        }


        public Value( String key, Boolean value ) {
            this( key, value == null ? "" : (value.equals( Boolean.TRUE ) ? "ja" : "nein") );
        }


        public Value( LabelSupport entity, Property<Integer> property, boolean useGrouping ) {
            this( entity.getLabel( property ), property.get(), useGrouping );
        }


        public Value( LabelSupport entity, Property<String> property ) {
            this( entity.getLabel( property ), property.get() );
        }


        public Value( Property<Boolean> property, LabelSupport entity ) {
            this( entity.getLabel( property ), property.get() );
        }


        public Value( LabelSupport entity, Property<Double> property, int fractionDigits ) {
            this( entity, property, fractionDigits, true );
        }


        public Value( LabelSupport entity, Property<Double> property, int fractionDigits, boolean useGrouping ) {
            this( entity.getLabel( property ), property.get(), fractionDigits, useGrouping );
        }


        public Value( LabelSupport entity, Association<? extends SchlNamed> assoc ) {
            this( entity.getLabel( assoc ), assoc.get() != null ? assoc.get().name().get() : null );
        }


        public String getKey() {
            return key;
        }


        public String getValue() {
            return value == null ? "" : value;
        }
    }

    private static Log            log             = LogFactory.getLog( AbstractExcelExporter.class );

    // Locale wegen dem . in 0.00
    protected static NumberFormat euroFormat      = NumberFormat.getNumberInstance( Locale.ENGLISH );

    protected static NumberFormat euroShortFormat = NumberFormat.getNumberInstance( Locale.ENGLISH );
    static {
        euroFormat.setMaximumFractionDigits( 2 );
        euroFormat.setMinimumFractionDigits( 2 );
        euroFormat.setMinimumIntegerDigits( 1 );
        euroShortFormat.setMaximumFractionDigits( 0 );
        euroShortFormat.setMinimumFractionDigits( 0 );
        euroFormat.setMinimumIntegerDigits( 1 );
    }

    protected static DateFormat   dateFormat      = new SimpleDateFormat( "dd.MM.yyyy" );

    protected static DateFormat   fileFormat      = new SimpleDateFormat( "yyyy_MM_dd_HH_mm_ss" );

    private final String          typename;

    private final Class<T>        type;

    private final String          filename;

    private final String          messagename;


    protected AbstractExcelExporter( Class<T> type, String typename, String filename, String messagename ) {
        this.type = type;
        this.typename = typename;
        this.filename = filename + "_" + fileFormat.format( new Date() );
        this.messagename = messagename;
    }


    @Override
    public boolean init( IFeatureOperationContext ctx ) {
        super.init( ctx );
        try {
            return ctx.featureSource().getSchema().getName().getLocalPart().equals( typename );
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

        final File f = File.createTempFile( filename, ".csv" );
        f.deleteOnExit();

        OutputStream out = new BufferedOutputStream( new FileOutputStream( f ) );
        List<String> errors = new ArrayList<String>();
        try {
            final int size = context.features().size();
            write( context.features(), out, monitor, errors );
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
                allErrors.append( "\n" ).append(
                        "Es wurden " + (size - errors.size()) + " " + messagename + " exportiert." );

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
                                "Es wurden " + size + " " + messagename + " exportiert." );
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
        Polymap.getSessionDisplay().asyncExec( new Runnable() {

            public void run() {
                String url = DownloadServiceHandler.registerContent( new ContentProvider() {

                    public String getContentType() {
                        return "text/csv; charset=ISO-8859-1";
                    }


                    public String getFilename() {
                        return filename + ".csv";
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

                ExternalBrowser.open( "download_window", url, ExternalBrowser.NAVIGATION_BAR | ExternalBrowser.STATUS );
            }
        } );

        monitor.done();
        return Status.OK;
    }


    private void write( FeatureCollection features, OutputStream out, IProgressMonitor monitor, List<String> errors )
            throws IOException {
        FeatureIterator it = null;
        Writer writer = null;
        CsvListWriter csvWriter = null;
        try {
            // TODO Trennzeichen?
            CsvPreference prefs = new CsvPreference( '"', ';', "\r\n" );

            writer = new OutputStreamWriter( out, "ISO-8859-1" );

            csvWriter = new CsvListWriter( writer, prefs );

            it = features.features();
            int count = 0;
            boolean noHeaderYet = true;
            final KapsRepository repo = KapsRepository.instance();

            while (it.hasNext()) {
                if (monitor.isCanceled()) {
                    throw new OperationCanceledException();
                }
                if ((++count % 100) == 0) {
                    monitor.subTask( "Objekte: " + count++ );
                    monitor.worked( 100 );
                }
                Feature feature = it.next();

                T entity = repo.findEntity( type, feature.getIdentifier().getID() );

                List<Value> values = createValues( entity, errors );
                if (values != null && !values.isEmpty()) {
                    // header
                    if (noHeaderYet) {
                        csvWriter.writeHeader( getHeaders( values ) );
                        noHeaderYet = false;
                    }

                    // all properties
                    csvWriter.write( getValues( values ) );
                }
            }

        }
        finally {
            if (csvWriter != null) {
                csvWriter.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (it != null) {
                it.close();
            }
        }
    }


    private String[] getHeaders( List<Value> values ) {
        List<String> headers = new ArrayList<String>();
        for (Value entry : values) {
            headers.add( entry.getKey() );
        }
        return headers.toArray( new String[0] );
    }


    private String[] getValues( List<Value> values ) {
        List<String> headers = new ArrayList<String>();
        for (Value entry : values) {
            headers.add( entry.getValue() );
        }
        return headers.toArray( new String[0] );
    }


    protected abstract List<Value> createValues( T entity, List<String> errors );


    protected String error( VertragComposite vertrag, String msg ) {
        return EingangsNummerFormatter.format( vertrag.eingangsNr().get() ) + ": " + msg;
    }


    public static void main( String[] args ) {
        System.out.println( fileFormat.format( new Date() ) );
    }
}
