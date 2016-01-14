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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.polymap.kaps.model.data.GebaeudeArtComposite;
import org.polymap.kaps.model.data.NutzungComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.model.data.VertragsArtComposite;
import org.polymap.kaps.model.data.VertragsdatenBaulandComposite;
import org.polymap.kaps.model.data.VertragsdatenErweitertComposite;
import org.polymap.kaps.ui.NumberFormatter;
import org.polymap.kaps.ui.form.EingangsNummerFormatter;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class VertragGewosExporter
        extends DefaultFeatureOperation
        implements IFeatureOperation {

    public class Output {

        double flaeche = 0;

        int    anzahl  = 0;

        int    umsatz  = 0;


        public Output add( Output output ) {
            flaeche += output.flaeche;
            anzahl += output.anzahl;
            umsatz += output.umsatz;
            return this;
        }
    }

    private static Log          log        = LogFactory.getLog( VertragGewosExporter.class );

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

        final File f = File.createTempFile( "GEWOS", ".csv" );
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
                            return "GEWOS_" + fileFormat.format( new Date() ) + ".csv";
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

                    ExternalBrowser.open( "download_window2", url, ExternalBrowser.NAVIGATION_BAR
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
            int berichtsjahr = -1;

            int vertraege1 = 0;
            int vertraegeMitKz2 = 0;
            Map<String, Output> zeilen = new HashMap<String, Output>();
            for (int i = 3; i <= 21; i++) {
                zeilen.put( String.valueOf( i ), new Output() );
            }
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
                vertraege1++;
                if (vertrag.fuerAuswertungGeeignet().get()) {
                    vertraegeMitKz2++;
                }
                if (berichtsjahr == -1) {
                    Date vertragsDatum = vertrag.vertragsDatum().get();
                    if (vertragsDatum != null) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime( vertragsDatum );
                        berichtsjahr = calendar.get( Calendar.YEAR );
                    }
                }

                VertragsdatenErweitertComposite ew = vertrag.erweiterteVertragsdaten().get();
                Double preis = vertrag.vollpreis().get();
                if (ew != null && ew.bereinigterVollpreis().get() != null) {
                    preis = ew.bereinigterVollpreis().get();
                }

                Output current = new Output();
                current.umsatz = preis != null ? preis.intValue() : 0;
                current.anzahl = 1;

                VertragsArtComposite vertragsArtC = vertrag.vertragsArt().get();
                if (vertragsArtC != null) {
                    int vertragsArt = Integer.parseInt( vertragsArtC.schl().get() );
                    if (vertragsArt == 13) {
                        // erbbaurecht
                        current.flaeche = calculateVerkaufteFlaeche( vertrag );
                        zeilen.get( "10" ).add( current );
                    }
                    else if (vertragsArt == 2 || vertragsArt == 3) {
                        Set<String> addTo = new HashSet<String>();
                        // mehrere flurstuecke mussen summiert werden
                        current.anzahl = 0;
                        for (FlurstueckComposite flurstueck : FlurstueckComposite.Mixin.forEntity( vertrag )) {
                            if (flurstueck != null) {
                                current.anzahl++;
                                NutzungComposite nutzungC = flurstueck.nutzung().get();
                                int nutzung = nutzungC != null ? Integer.parseInt( nutzungC.schl().get() ) : -1;
                                GebaeudeArtComposite gebartC = flurstueck.gebaeudeArt().get();
                                int gebArt = gebartC != null ? Integer.parseInt( gebartC.schl().get() ) : -1;
                                double flaeche = calculateVerkaufteFlaeche( flurstueck );
                                current.flaeche += flaeche;
                                if (gebArt == 0 && nutzung == 16) {
                                    addTo.add( "3" );
                                }
                                if (gebArt == 0 && nutzung == 17) {
                                    addTo.add( "4" );
                                }
                                if (gebArt == 0 && (nutzung == 2 || nutzung == 6 || nutzung == 11)) {
                                    addTo.add( "5" );
                                }
                                if (gebArt == 0 && (nutzung >= 12 && nutzung <= 15)) {
                                    addTo.add( "6" );
                                }
                                if (gebArt == 0 && (nutzung == 7)) {
                                    addTo.add( "7" );
                                }
                                if (gebArt == 0 && (nutzung >= 8 && nutzung <= 10)) {
                                    addTo.add( "8" );
                                }
                                if (gebArt == 0 && (nutzung == 5)) {
                                    addTo.add( "9" );
                                }
                                if ((gebArt == 70 || gebArt == 80) && (nutzung == 1 || nutzung == 16)) {
                                    addTo.add( "11" );
                                }
                                if (((gebArt >= 71 && gebArt <= 73) || (gebArt >= 81 && gebArt <= 83))
                                        && (nutzung == 1 || nutzung == 16)) {
                                    addTo.add( "12" );
                                }
                                if (gebArt >= 70 && gebArt <= 83 && nutzung == 1) {
                                    // baujahr check
                                    VertragsdatenBaulandComposite vdc = VertragsdatenBaulandComposite.Mixin
                                            .forVertrag( vertrag );
                                    if (vdc != null) {
                                        Integer baujahr = vdc.baujahr().get();
                                        if (baujahr != null && baujahr == berichtsjahr) {
                                            addTo.add( "13" );
                                        }
                                    }
                                }
                                if (((gebArt >= 120 && gebArt <= 143) || (gebArt >= 200 && gebArt <= 203))
                                        && (nutzung == 1)) {
                                    addTo.add( "14" );
                                }
                                if ((gebArt == 190 || gebArt == 210) && (nutzung == 3 || nutzung == 4)) {
                                    addTo.add( "15" );
                                }
                                if ((gebArt == 240 || gebArt == 250 || gebArt == 260 || gebArt == 270
                                        || gebArt == 300 || gebArt == 400 || gebArt == 410 || gebArt == 420)
                                        && (nutzung == 3 || nutzung == 4)) {
                                    addTo.add( "16" );
                                }
                                if ((gebArt >= 330 && gebArt <= 370) || (gebArt >= 450 && gebArt <= 890)) {
                                    addTo.add( "17" );
                                }
                                if ((gebArt >= 70 && gebArt <= 203) && nutzung == 29) {
                                    addTo.add( "18" );
                                }
                                if ((gebArt >= 70 && gebArt <= 203) && (nutzung >= 30 && nutzung <= 32)) {
                                    addTo.add( "19" );
                                }
                                if (vertragsArt == 2 && (gebArt >= 70 && gebArt <= 203) && nutzung == 29) {
                                    // baujahr check
                                    VertragsdatenBaulandComposite vdc = VertragsdatenBaulandComposite.Mixin
                                            .forVertrag( vertrag );
                                    if (vdc != null) {
                                        Integer baujahr = vdc.baujahr().get();
                                        if (baujahr != null && baujahr == berichtsjahr) {
                                            addTo.add( "20" );
                                        }
                                    }
                                }
                                if (vertragsArt == 3 && (gebArt >= 70 && gebArt <= 203) && nutzung == 29) {
                                    addTo.add( "21" );
                                }
                            }
                        }
//                        if (addTo.isEmpty()) {
//                            errors.add( "Vertrag " + EingangsNummerFormatter.format( vertrag.eingangsNr().get() )
//                                    + " konnte nicht berücksichtigt werden." );
//                        }
                        Set<String> reduced = new HashSet<String>(addTo);
                        // die einzigen 3 die doppelt sein duerfen
                        reduced.remove( "13" );
                        reduced.remove( "20" );
                        reduced.remove( "21" );
                        if (reduced.size() > 1) {
                            errors.add( "Vertrag " + EingangsNummerFormatter.format( vertrag.eingangsNr().get() )
                                    + " enthält verschiedene Nutzungs- oder Gebäudearten." );
                        }
                        // direkt an den 1. setzen
                        for (String key : addTo) {
                            zeilen.get( key ).add( current );
                        }
                    }
                }
            }

            lines.add( new StringBuilder().append( "Geschaeftsjahr" ).append( DELIMITER ).append( berichtsjahr )
                    .toString() );
            lines.add( new StringBuilder().append( "Anzahl Grundstuecksvertraege" ).append( DELIMITER )
                    .append( vertraege1 ).toString() );
            lines.add( new StringBuilder().append( "Vert. gewoehnlicher Geschaeftsverkehr" ).append( DELIMITER )
                    .append( vertraegeMitKz2 ).toString() );
            lines.add( new StringBuilder().append( DELIMITER ).append( DELIMITER ).append( DELIMITER ).toString() );
            lines.add( new StringBuilder().append( DELIMITER ).append( "Anzahl" ).append( DELIMITER )
                    .append( "Flaeche in qm" ).append( DELIMITER ).append( "Geldumsatz in EUR" ).toString() );
            lines.add( createLine( "1.1 Baureifes Wohnbauland", zeilen, true, true, DELIMITER, 3 ) );
            lines.add( createLine( "1.2 Industrie- und Gewerbeland", zeilen, true, true, DELIMITER, 4 ) );
            lines.add( createLine( "1.3 Sonstiges Bauland", zeilen, true, true, DELIMITER, 5 ) );
            lines.add( createLine( "                Zwischensumme Bauland", zeilen, true, true, DELIMITER, 3, 4, 5 ) );
            lines.add( createLine( "1.4 Uebrige Flaeche", zeilen, true, true, DELIMITER, 6, 7, 8, 9 ) );
            lines.add( createLine( "        davon Gemeinbedarfsflaechen", zeilen, true, true, DELIMITER, 7 ) );
            lines.add( createLine( "        davon Verkehrsflaechen", zeilen, true, true, DELIMITER, 8 ) );
            lines.add( createLine( "        davon Agrarland", zeilen, true, true, DELIMITER, 9 ) );
            lines.add( createLine( "        davon BWL + RBL", zeilen, true, true, DELIMITER, 6 ) );
            lines.add( createLine( "                Summe unbebaute Grundstuecke", zeilen, true, true, DELIMITER, 3, 4, 5, 6, 7, 8, 9 ) );
            lines.add( createLine( "Erbbaurechtsvertraege", zeilen, true, false, DELIMITER, 10 ) );
            lines.add( createLine( "2.1 EFH + ZFH", zeilen, false, true, DELIMITER, 11, 12 ) );
            lines.add( createLine( "        davon Neubau", zeilen, false, true, DELIMITER, 13 ) );
            lines.add( createLine( "        davon RH und DHH", zeilen, false, true, DELIMITER, 12 ) );
            lines.add( createLine( "2.2 MFH", zeilen, false, true, DELIMITER, 14 ) );
            lines.add( createLine( "2.3 Buero-, Verw.-,GH", zeilen, false, true, DELIMITER, 15 ) );
            lines.add( createLine( "2.4 Ind.-, Gewerbeobjekte", zeilen, false, true, DELIMITER, 16 ) );
            lines.add( createLine( "2.5 Sonstiges Objekte", zeilen, false, true, DELIMITER, 17 ) );
            lines.add( createLine( "                Summe bebaute Grundstuecke", zeilen, false, true, DELIMITER, 11, 12, 14, 15, 16, 17 ) );
            lines.add( createLine( "3. ETW/TE", zeilen, false, true, DELIMITER, 18, 19 ) );
            lines.add( createLine( "        davon ETW", zeilen, false, true, DELIMITER, 18 ) );
            lines.add( createLine( "Erstverkaeufe aus Neubauten", zeilen, false, true, DELIMITER, 20 ) );
            lines.add( createLine( "Wiederverkauf", zeilen, false, true, DELIMITER, 21 ) );
            lines.add( new StringBuilder().append( DELIMITER ).append( DELIMITER ).append( DELIMITER ).toString() );

            // TODO
            Output dpw = zeilen.get( "20" );
             NumberFormat numberFormat = NumberFormatter.getFormatter( 2, true );
             lines.add( new StringBuilder().append( "Durchschnittspreis in Euro/qm (Erstverkaeufe aus Neubauten)"
             ).append( DELIMITER )
             .append( dpw.flaeche > 0.0d ? numberFormat.format( dpw.umsatz / dpw.flaeche ) : "" ).toString() );
             lines.add( new StringBuilder().append(
             "Durchschnittsgroesse der WE in qm (Erstverkaeufe aus Neubauten)" ).append( DELIMITER )
             .append( dpw.anzahl > 0.0d ? numberFormat.format( dpw.flaeche / dpw.anzahl ) : ""  ).toString() );

        }
        finally {
            if (it != null) {
                it.close();
            }
        }
    }


    private String createLine( String label, Map<String, Output> zeilen, boolean withFlaeche, boolean withPreis,
            String delimiter, int... lines ) {
        NumberFormat numberFormat = NumberFormatter.getFormatter( 0, true );
        Output sum = new Output();
        for (int line : lines) {
            sum.add( zeilen.get( String.valueOf( line ) ) );
        }
        return new StringBuilder().append( label ).append( DELIMITER ).append( numberFormat.format( sum.anzahl ) )
                .append( DELIMITER ).append( withFlaeche ? numberFormat.format( sum.flaeche ) : "" ).append( DELIMITER )
                .append( withPreis ? numberFormat.format( sum.umsatz ) : "" ).append( DELIMITER ).toString();

    }


    private double calculateVerkaufteFlaeche( VertragComposite vertrag ) {
        double verkaufteFlaeche = 0;

        for (FlurstueckComposite flurstueck : FlurstueckComposite.Mixin.forEntity( vertrag )) {
            verkaufteFlaeche += calculateVerkaufteFlaeche( flurstueck );
        }
        return verkaufteFlaeche;
    }


    private double calculateVerkaufteFlaeche( FlurstueckComposite flurstueck ) {
        if (flurstueck != null) {
            Double flaeche = flurstueck.flaeche().get();
            Double n = flurstueck.flaechenAnteilNenner().get();
            Double z = flurstueck.flaechenAnteilZaehler().get();

            if (flaeche != null && n != null && z != null && z != 0) {
                return flaeche * z / n;
            }
        }
        return 0.0;
    }


    public static void main( String[] args ) {
        System.out.println( new Double( 1234.56d ).intValue() );
        System.out.println( Integer.parseInt( "000" ) );
    }
}
