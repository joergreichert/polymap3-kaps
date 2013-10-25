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
import org.polymap.kaps.model.data.GemarkungComposite;
import org.polymap.kaps.model.data.GemeindeComposite;
import org.polymap.kaps.model.data.NutzungComposite;
import org.polymap.kaps.model.data.RichtwertzoneComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.model.data.VertragsdatenAgrarComposite;
import org.polymap.kaps.model.data.VertragsdatenErweitertComposite;
import org.polymap.kaps.ui.NumberFormatter;
import org.polymap.kaps.ui.form.EingangsNummerFormatter;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class VertragsdatenAgrarAlsAgrarExporter
        extends DefaultFeatureOperation
        implements IFeatureOperation {

    public class Value {

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
            this( key, value != null ? NumberFormatter.getFormatter( fractionDigits ).format( value ) : "" );
        }


        public Value( String key, Long value ) {
            this( key, value != null ? NumberFormatter.getFormatter( 0 ).format( value ) : "" );
        }


        public Value( String key, Boolean value ) {
            this( key, value == null ? "" : (value.equals( Boolean.TRUE ) ? "ja" : "nein") );
        }


        public String getKey() {
            return key;
        }


        public String getValue() {
            return value == null ? "" : value;
        }
    }

    private static Log          log             = LogFactory.getLog( VertragsdatenAgrarAlsAgrarExporter.class );

    // Locale wegen dem . in 0.00
    private static NumberFormat euroFormat      = NumberFormat.getNumberInstance( Locale.ENGLISH );

    private static NumberFormat euroShortFormat = NumberFormat.getNumberInstance( Locale.ENGLISH );
    static {
        euroFormat.setMaximumFractionDigits( 2 );
        euroFormat.setMinimumFractionDigits( 2 );
        euroFormat.setMinimumIntegerDigits( 1 );
        euroShortFormat.setMaximumFractionDigits( 0 );
        euroShortFormat.setMinimumFractionDigits( 0 );
        euroFormat.setMinimumIntegerDigits( 1 );
    }

    private static DateFormat   dateFormat      = new SimpleDateFormat( "dd.MM.yyyy" );


    @Override
    public boolean init( IFeatureOperationContext ctx ) {
        super.init( ctx );
        try {
            return context.featureSource().getSchema().getName().getLocalPart()
                    .equals( VertragsdatenAgrarComposite.NAME );
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

        final Date now = new Date();

        final File f = File.createTempFile( "statistik-agrar", ".csv" );
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
                allErrors.append( "\n" ).append( "Es wurden " + (size - errors.size()) + " Verträge exportiert." );

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
                                "Es wurden " + size + " Verträge exportiert." );
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
                        return "statistik-agrar.csv";
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

                VertragsdatenAgrarComposite richtwertzone = repo.findEntity( VertragsdatenAgrarComposite.class, feature
                        .getIdentifier().getID() );

                List<Value> values = createValues( richtwertzone, errors );
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


    private List<Value> createValues( VertragsdatenAgrarComposite vdb, List<String> errors ) {

        List<Value> result = new ArrayList<Value>();
        VertragComposite vertrag = vdb.vertrag().get();
        if (vertrag == null) {
            errors.add( "Keinen Vertrag gefunden!" );
            return result;
        }
        FlurstueckComposite flurstueck = null;
        double verkaufteFlaeche = 0.0d;
        for (FlurstueckComposite fs : FlurstueckComposite.Mixin.forEntity( vertrag )) {
            NutzungComposite nutzung = fs.nutzung().get();
            if (nutzung != null && nutzung.isAgrar().get() != null && nutzung.isAgrar().get() == Boolean.TRUE) {
                flurstueck = fs;
                Double flaeche = flurstueck.verkaufteFlaeche().get();
                if (flaeche != null) {
                    verkaufteFlaeche += flaeche.doubleValue();
                }
            }
        }
        if (flurstueck == null) {
            errors.add( error( vertrag, "Kein Flurstück gefunden!" ) );
            return result;
        }
        GemarkungComposite gemarkung = flurstueck.gemarkung().get();
        if (gemarkung == null) {
            errors.add( error( vertrag, "Keine Gemarkung gefunden!" ) );
            return result;
        }
        GemeindeComposite gemeinde = gemarkung.gemeinde().get();
        if (gemeinde == null) {
            errors.add( error( vertrag, "Keine Gemeinde gefunden!" ) );
            return result;
        }
        RichtwertzoneComposite zone = flurstueck.richtwertZone().get();
        if (zone == null) {
            errors.add( error( vertrag, "Keine Richtwertzone gefunden!" ) );
            return result;
        }
        // kann null sein
        VertragsdatenErweitertComposite ew = vertrag.erweiterteVertragsdaten().get();
        Double preis = vertrag.vollpreis().get();
        if (ew != null && ew.bereinigterVollpreis().get() != null) {
            preis = ew.bereinigterVollpreis().get();
        }

        result.add( new Value( "Eingangsnummer", EingangsNummerFormatter.format( vertrag.eingangsNr().get() ) ) );
        result.add( new Value( "Vertragsdatum", vertrag.vertragsDatum().get() ) );
        result.add( new Value( "Gemeinde", gemeinde.name().get() ) );
        result.add( new Value( "Gemarkung", gemarkung.name().get() ) );
        result.add( new Value( "Flurstücksnummer", "" + flurstueck.hauptNummer().get() ) );
        result.add( new Value( "Unternummer", flurstueck.unterNummer().get() ) );
        result.add( new Value( "Vollpreis", preis, 2 ) );
        result.add( new Value( "Vertragsfläche gesamt", verkaufteFlaeche, 0 ) );
        result.add( new Value( "Bebaut", vdb.istBebaut().get() ) );

        result.add( new Value( "Bodennutzung 1", vdb.bodennutzung1().get() != null ? vdb.bodennutzung1().get().name()
                .get() : "" ) );
        result.add( new Value( "Fläche 1", vdb.ackerzahl1().get() ) );
        result.add( new Value( "Flächeanteil 1", vdb.flaechenAnteil1().get(), 2 ) );
        result.add( new Value( "Abgleich 1", vdb.abgleichAufKaufpreis1().get(), 2 ) );

        result.add( new Value( "Bodennutzung 2", vdb.bodennutzung2().get() != null ? vdb.bodennutzung2().get().name()
                .get() : "" ) );
        result.add( new Value( "Fläche 2", vdb.ackerzahl2().get() ) );
        result.add( new Value( "Flächeanteil 2", vdb.flaechenAnteil2().get(), 2 ) );
        result.add( new Value( "Abgleich 2", vdb.abgleichAufKaufpreis2().get(), 2 ) );

        result.add( new Value( "Bodennutzung 3", vdb.bodennutzung3().get() != null ? vdb.bodennutzung3().get().name()
                .get() : "" ) );
        result.add( new Value( "Fläche 3", vdb.ackerzahl3().get() ) );
        result.add( new Value( "Flächeanteil 3", vdb.flaechenAnteil3().get(), 2 ) );
        result.add( new Value( "Abgleich 3", vdb.abgleichAufKaufpreis3().get(), 2 ) );

        result.add( new Value( "Bodennutzung 4", vdb.bodennutzung4().get() != null ? vdb.bodennutzung4().get().name()
                .get() : "" ) );
        result.add( new Value( "Fläche 4", vdb.ackerzahl4().get() ) );
        result.add( new Value( "Flächeanteil 4", vdb.flaechenAnteil4().get(), 2 ) );
        result.add( new Value( "Abgleich 4", vdb.abgleichAufKaufpreis4().get(), 2 ) );

        result.add( new Value( "Bodennutzung 5", vdb.bodennutzung5().get() != null ? vdb.bodennutzung5().get().name()
                .get() : "" ) );
        result.add( new Value( "Fläche 5", vdb.ackerzahl5().get() ) );
        result.add( new Value( "Flächeanteil 5", vdb.flaechenAnteil5().get(), 2 ) );
        result.add( new Value( "Abgleich 5", vdb.abgleichAufKaufpreis5().get(), 2 ) );

        result.add( new Value( "Bodennutzung 6", vdb.bodennutzung6().get() != null ? vdb.bodennutzung6().get().name()
                .get() : "" ) );
        result.add( new Value( "Fläche 6", vdb.ackerzahl6().get() ) );
        result.add( new Value( "Flächeanteil 6", vdb.flaechenAnteil6().get(), 2 ) );
        result.add( new Value( "Abgleich 6", vdb.abgleichAufKaufpreis6().get(), 2 ) );

        result.add( new Value( "zur Statistik geeignet", vdb.fuerStatistikGeeignet().get() ) );
        result.add( new Value( "zur Richtwertermittlung geeignet", vdb.zurRichtwertermittlungGeeignet().get() ) );

        return result;
    }


    private String error( VertragComposite vertrag, String msg ) {
        return EingangsNummerFormatter.format( vertrag.eingangsNr().get() ) + ": " + msg;
    }
}
