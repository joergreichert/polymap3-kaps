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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;

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
import org.polymap.kaps.model.data.ArtDesBaugebietsComposite;
import org.polymap.kaps.model.data.FlurstueckComposite;
import org.polymap.kaps.model.data.GemeindeComposite;
import org.polymap.kaps.model.data.GrundstuecksArtBaulandStalaComposite;
import org.polymap.kaps.model.data.KaeuferKreisComposite;
import org.polymap.kaps.model.data.NutzungComposite;
import org.polymap.kaps.model.data.RichtwertzoneComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.model.data.VertragsArtComposite;
import org.polymap.kaps.model.data.VertragsdatenBaulandComposite;
import org.polymap.kaps.model.data.VertragsdatenErweitertComposite;
import org.polymap.kaps.ui.form.EingangsNummerFormatter;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class VertragStaLaBaulandExporter
        extends DefaultFeatureOperation
        implements IFeatureOperation {

    // Struktur
    // 052200000000000201300023110120130000100000006290145226201610101
    //
    // 1-4 Melder 0522
    // 5-24 Eingangsnummer 00000000000201300023
    // 25-32 Vertragsdatum TTMMJJJJ 11012013
    // 33-41 Kaufpreis 000010000
    // 42-48 Flaeche 0006290
    // 49-56 Gemeindeschlüssel 14522620
    // 57 Art des Grundstücks STALA bei Nutzung 1
    // 58 Art des Baugebietes 6
    // 59-60 Veräusserer STALA 10
    // 61-62 Erwerber STALA 10
    // 63 Verwandschaftsverhältnis 1
    // 64-80 leer
    private static Log          log        = LogFactory.getLog( VertragStaLaBaulandExporter.class );

    protected static DateFormat fileFormat = new SimpleDateFormat( "yyyy_MM_dd_HH_mm_ss" );

    private final static String DELIMITER  = "";


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

        final Date now = new Date();

        final File f = File.createTempFile( "StatLandesamt_Bauland", ".txt" );
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
                    out.write( line );
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
                allErrors.append( "\n" ).append( "Es wurden " + lines.size() + " Verträge exportiert." );

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
                                "Es wurden " + lines.size() + " Verträge exportiert." );
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
                            return "text/plain; charset=ISO-8859-1";
                        }


                        public String getFilename() {
                            return "StatLandesamt_Bauland_" + fileFormat.format( new Date() ) + ".txt";
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
            while (it.hasNext()) {
                if (monitor.isCanceled()) {
                    throw new OperationCanceledException();
                }
                if ((++count % 100) == 0) {
                    monitor.subTask( "Objekte: " + count++ );
                    monitor.worked( 100 );
                }
                Feature feature = it.next();

                VertragComposite vdc = repo.findEntity( VertragComposite.class, feature.getIdentifier().getID() );
                // all properties
                export( lines, vdc, errors );

            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
    }


    private void export( List<String> lines, VertragComposite vertrag, List<String> errors ) {

        VertragsdatenBaulandComposite vdc = VertragsdatenBaulandComposite.Mixin.forVertrag( vertrag );
        if (vdc == null) {
            errors.add( error( vertrag, "Erweiterte Vertragsdaten Bauland nicht gefunden" ) );
            return;
        }
        FlurstueckComposite flurstueck = null;
        for (FlurstueckComposite fs : FlurstueckComposite.Mixin.forEntity( vertrag )) {
            NutzungComposite nutzung = fs.nutzung().get();
            if (nutzung != null && (nutzung.isAgrar().get() == null || nutzung.isAgrar().get() == Boolean.FALSE)) {
                flurstueck = fs;
                break;
            }
        }
        if (flurstueck == null) {
            errors.add( error( vertrag, "Flurstück mit Nutzung Bauland nicht gefunden" ) );
            return;
        }
        RichtwertzoneComposite richtwertzone = vdc.richtwertZone().get();
        if (richtwertzone == null) {
            errors.add( error( vertrag, "Richtwertzone nicht gefunden" ) );
            return;
        }
        GemeindeComposite gemeinde = richtwertzone.gemeinde().get();
        if (gemeinde == null) {
            errors.add( error( vertrag, "Gemeinde nicht gefunden" ) );
            return;
        }
        VertragsdatenErweitertComposite vertragErweitert = vertrag.erweiterteVertragsdaten().get();
        if (vertragErweitert == null) {
            errors.add( error( vertrag, "Erweiterte Vertragsdaten nicht gefunden" ) );
            return;
        }

        List<String> contents = new ArrayList<String>();
        // data
        // bezirk
        contents.add( "05" );
        // String melder =
        contents.add( "22" );
        // String kennnummer =
        contents.add( String.format( "%020d", vertrag.eingangsNr().get() ) );

        Calendar cal = new GregorianCalendar();
        cal.setTime( vertrag.vertragsDatum().get() );

        // String tag =
        contents.add( String.format( "%02d", cal.get( Calendar.DAY_OF_MONTH ) ) );
        // String monat =
        contents.add( String.format( "%02d", cal.get( Calendar.MONTH ) + 1 ) );
        // String jahr =
        contents.add( String.format( "%04d", cal.get( Calendar.YEAR ) ) );

        if (vertragErweitert.bereinigterVollpreis().get() == null) {
            errors.add( error( vertrag, "Bereinigter Vollpreis nicht gefunden" ) );
            return;
        }
        // String kaufpreis =
        contents.add( String.format( "%09d", vertragErweitert.bereinigterVollpreis().get().intValue() ) );
        Double v = vdc.verkaufteFlaecheGesamt().get();
        if (v == null) {
            v = vdc.verkaufteFlaeche().get();
        }
        if (v == null) {
            errors.add( error( vertrag, "Verkaufte Fläche Gesamt nicht gefunden" ) );
            return;
        }
        // String flaeche =
        contents.add( String.format( "%07d", v.intValue() ) );

        // String land =
        contents.add( "14" );
        // String kreisGemeinde =
        contents.add( gemeinde.schl().get().substring( 0, 3 ) + DELIMITER + gemeinde.schl().get().substring( 3 ) );

        GrundstuecksArtBaulandStalaComposite artStala = flurstueck.nutzung().get().stala().get();
        if (artStala == null) {
            errors.add( error( vertrag, "Art des Grundstücks nicht gefunden" ) );
            return;
        }
        // String artDesGrundstuecks =
        contents.add( artStala.schl().get() );

        ArtDesBaugebietsComposite artDesBaugebietsC = flurstueck.artDesBaugebiets().get();
        if (artDesBaugebietsC == null) {
            errors.add( error( vertrag, "Art des Baugebiets nicht gefunden" ) );
            return;
        }
        // String artDesBaugebiets =
        contents.add( artDesBaugebietsC.schl().get() );

        KaeuferKreisComposite verkaeuferKreisComposite = vertrag.verkaeuferKreis().get();
        if (verkaeuferKreisComposite == null) {
            errors.add( error( vertrag, "Verkäufer nicht gefunden" ) );
            return;
        }
        if (verkaeuferKreisComposite.stala().get() == null) {
            errors.add( error( vertrag, "Verkäufer - StaLa Zuordnung nicht gefunden" ) );
            return;
        }
        // String veräußerer - 2
        contents.add( verkaeuferKreisComposite.stala().get().schl().get() );

        KaeuferKreisComposite kaeuferKreisComposite = vertrag.kaeuferKreis().get();
        if (kaeuferKreisComposite == null) {
            errors.add( error( vertrag, "Erwerber nicht gefunden" ) );
            return;
        }
        if (kaeuferKreisComposite.stala().get() == null) {
            errors.add( error( vertrag, "Erwerber - StaLa Zuordnung nicht gefunden" ) );
            return;
        }
        // String Erwerber - 2
        contents.add( kaeuferKreisComposite.stala().get().schl().get() );

        VertragsArtComposite vertragsArtComposite = vertrag.vertragsArt().get();
        if (vertragsArtComposite == null) {
            errors.add( error( vertrag, "Vertragsart nicht gefunden" ) );
            return;
        }
        if (vertragsArtComposite.stala().get() == null) {
            errors.add( error( vertrag, "Vertragsart - StaLa Verwandschaftsverhältnis Zuordnung nicht gefunden" ) );
            return;
        }
        // String Verwandschaftsverhältnis - 2
        contents.add( vertragsArtComposite.stala().get().schl().get() );

        StringBuilder ret = new StringBuilder();
        for (String s : contents) {
            ret.append( s ).append( DELIMITER );
        }
        lines.add( ret.toString() );
    }


    private String error( VertragComposite vertrag, String msg ) {
        return EingangsNummerFormatter.format( vertrag.eingangsNr().get() ) + ": " + msg;
    }
}
