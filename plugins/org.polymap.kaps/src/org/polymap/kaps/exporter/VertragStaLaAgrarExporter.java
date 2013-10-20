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

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
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
import org.polymap.kaps.model.data.FlurComposite;
import org.polymap.kaps.model.data.FlurstueckComposite;
import org.polymap.kaps.model.data.GemeindeComposite;
import org.polymap.kaps.model.data.KaeuferKreisComposite;
import org.polymap.kaps.model.data.NutzungComposite;
import org.polymap.kaps.model.data.RichtwertzoneComposite;
import org.polymap.kaps.model.data.RichtwertzoneZeitraumComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.model.data.VertragsdatenAgrarComposite;
import org.polymap.kaps.model.data.VertragsdatenErweitertComposite;
import org.polymap.kaps.ui.form.EingangsNummerFormatter;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class VertragStaLaAgrarExporter
        extends DefaultFeatureOperation
        implements IFeatureOperation {

    // Struktur
    // 000000000201300016;Mittelsachsen;Lichtenau;4535;000;00016;01012013;2;4;9;00250329;00250329;00310000;;;;A-164072m²-1,64/
    // GR-40597m²-0,49/ VS-960m²-1,66/ Vertrag von 26.09.2012;
    // ;A-164072m²-1,64/ GR-40597m²-0,49/ VS-960m²-1,66/ Vertrag von 26.09.2012;
    //
    // 18 Eingangsnummer 000000000201300016
    // Landkreis Mittelsachsen
    // Gemeinde Lichtenau
    // Gemarkung SCHL 4535
    // Flur 000
    // Flurstück Hauptnummer 5 Stellen 00016
    // Vertragsdatum TTMMJJJJ
    // Veräußerer Stala AGRAR 2
    // Erwerber Stala AGRAR 4
    // Art des Baugebietes SCHL 9
    // Gesamtfläche über alle Grundstücke
    // Fläche der landwirtschaftlichen Nutzung = Gesamtfläche 8 stellig siehe Reiter
    // Weiteres
    // Geldleistung bereinigter Vollpreis 8 stelling
    // Hypothek leer alles aus FLURZWI_AGRAR
    // Tauschgrundstück leer
    // sonstige Leistung leer
    // leer
    // Bemerkung StaLa siehe Reiter Weiteres

    private static Log          log       = LogFactory.getLog( VertragStaLaAgrarExporter.class );

    private final static String DELIMITER = ";";


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

        final File f = File.createTempFile( "StatLandesamt_Agrar", ".csv" );
        f.deleteOnExit();
        BufferedWriter out = new BufferedWriter( new FileWriter( f ) );

        final List<String> lines = new ArrayList<String>();
        final List<String> errors = new ArrayList<String>();

        try {
            export( context.features(), lines, monitor, errors );
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
                            return "text/csv; charset=ISO-8859-1";
                        }


                        public String getFilename() {
                            return "StatLandesamt_Agrar.csv";
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

                    log.info( "CSV: download URL: " + url );

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

        VertragsdatenAgrarComposite vdc = VertragsdatenAgrarComposite.Mixin.forVertrag( vertrag );
        if (vdc == null) {
            errors.add( error( vertrag, "Erweiterte Vertragsdaten Agrar nicht gefunden" ) );
            return;
        }
        FlurstueckComposite flurstueck = null;
        for (FlurstueckComposite fs : FlurstueckComposite.Mixin.forEntity( vertrag )) {
            NutzungComposite nutzung = fs.nutzung().get();
            if (nutzung != null && nutzung.isAgrar().get() != null && nutzung.isAgrar().get() == Boolean.TRUE) {
                flurstueck = fs;
                break;
            }
        }
        if (flurstueck == null) {
            errors.add( error( vertrag, "Flurstück mit Nutzung Agrar nicht gefunden" ) );
            return;
        }
        RichtwertzoneZeitraumComposite richtwertzoneZ = vdc.richtwertZone1().get();
        if (richtwertzoneZ == null) {
            errors.add( error( vertrag, "Richtwertzone nicht gefunden" ) );
            return;
        }
        RichtwertzoneComposite richtwertzone = richtwertzoneZ.zone().get();
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
            errors.add( error( vertrag, "Erweiterte Vertragsdaten fehlen" ) );
            return;
        }

        List<String> contents = new ArrayList<String>();
        // data

        // String kennnummer = 18
        contents.add( String.format( "%018d", vertrag.eingangsNr().get() ) );

        // String land =
        contents.add( "14" );
        // String kreisGemeinde =
        contents.add( gemeinde.schl().get().substring( 0, 3 ) + DELIMITER + gemeinde.schl().get().substring( 3 ) );

        FlurComposite flur = flurstueck.flur().get();
        // gibts in Sachsen nicht
        contents.add( flur == null ? "000" : flur.schl().get() );

        // flurstueck1
        contents.add( String.format( "%05d", flurstueck.hauptNummer().get() ) );
        // flurstueck2
        contents.add( StringUtils.leftPad( flurstueck.unterNummer().get(), 5, "0" ) );

        // Datum
        Calendar cal = new GregorianCalendar();
        cal.setTime( vertrag.vertragsDatum().get() );

        // String tag =
        contents.add( String.format( "%02d", cal.get( Calendar.DAY_OF_MONTH ) ) );
        // String monat =
        contents.add( String.format( "%02d", cal.get( Calendar.MONTH ) + 1 ) );
        // String jahr =
        contents.add( String.format( "%04d", cal.get( Calendar.YEAR ) ) );

        KaeuferKreisComposite verkaeuferKreisComposite = vertrag.verkaeuferKreis().get();
        if (verkaeuferKreisComposite == null) {
            errors.add( error( vertrag, "Verkäufer nicht gefunden" ) );
            return;
        }
        if (verkaeuferKreisComposite.stalaAgrar().get() == null) {
            errors.add( error( vertrag, "Verkäufer - StaLa Zuordnung nicht gefunden" ) );
            return;
        }
        // String veräußerer - 2
        contents.add( verkaeuferKreisComposite.stalaAgrar().get().schl().get() );

        KaeuferKreisComposite kaeuferKreisComposite = vertrag.kaeuferKreis().get();
        if (kaeuferKreisComposite == null) {
            errors.add( error( vertrag, "Erwerber nicht gefunden" ) );
            return;
        }
        if (kaeuferKreisComposite.stalaAgrar().get() == null) {
            errors.add( error( vertrag, "Erwerber - StaLa Zuordnung nicht gefunden" ) );
            return;
        }
        // String Erwerber - 2
        contents.add( kaeuferKreisComposite.stalaAgrar().get().schl().get() );

        ArtDesBaugebietsComposite artDesBaugebietsC = flurstueck.artDesBaugebiets().get();
        if (artDesBaugebietsC == null) {
            errors.add( error( vertrag, "Art des Grundstücks nicht gefunden" ) );
            return;
        }
        // String artDesBaugebiets =
        contents.add( artDesBaugebietsC.schl().get() );

        // String flaeche =
        if (vdc.verkaufteFlaeche().get() == null) {
            errors.add( error( vertrag, "Verkaufte Fläche gesamt nicht gefunden" ) );
            return;
        }
        contents.add( String.format( "%08d", vdc.verkaufteFlaeche().get().intValue() ) );

        // String flaeche =
        Double flaecheLandwirtschaftStala = vdc.flaecheLandwirtschaftStala().get();
        if (flaecheLandwirtschaftStala == null) {
            errors.add( error( vertrag, "Fläche der landwirtschaftlichen Nutzung nicht gefunden, nutze 0" ) );
        }
        contents.add( String.format( "%08d", flaecheLandwirtschaftStala != null ? flaecheLandwirtschaftStala.intValue()
                : 0 ) );

        if (vertragErweitert.bereinigterVollpreis().get() == null) {
            errors.add( error( vertrag, "Bereinigter Vollpreis nicht gefunden" ) );
            return;
        }
        // String geldwert =
        contents.add( String.format( "%08d", vertragErweitert.bereinigterVollpreis().get().intValue() ) );

        // String hypotheken =
        Double h = vdc.hypothekStala().get();
        if (h == null) {
            errors.add( error( vertrag, "Hypotheken nicht gefunden, nutze 0" ) );
        }
        contents.add( String.format( "%08d", h != null ? h.intValue() : 0 ) );

        // String tauschgrundstück =
        h = vdc.wertTauschStala().get();
        if (h == null) {
            errors.add( error( vertrag, "Wert des Tauschgrundstückes nicht gefunden, nutze 0" ) );
        }
        contents.add( String.format( "%08d", h != null ? h.intValue() : 0 ) );

        // String sonstiges =
        h = vdc.wertSonstigesStala().get();
        if (h == null) {
            errors.add( error( vertrag, "Wert sonstige Leistungen nicht gefunden, nutze 0" ) );
        }
        contents.add( String.format( "%08d", h != null ? h.intValue() : 0 ) );

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
