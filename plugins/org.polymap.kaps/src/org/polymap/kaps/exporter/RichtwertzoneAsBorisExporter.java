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

import com.vividsolutions.jts.geom.Polygon;

import org.eclipse.rwt.widgets.ExternalBrowser;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import org.polymap.core.data.operation.DefaultFeatureOperation;
import org.polymap.core.data.operation.DownloadServiceHandler;
import org.polymap.core.data.operation.DownloadServiceHandler.ContentProvider;
import org.polymap.core.data.operation.FeatureOperationExtension;
import org.polymap.core.data.operation.IFeatureOperation;
import org.polymap.core.data.operation.IFeatureOperationContext;
import org.polymap.core.runtime.Polymap;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.data.BauweiseComposite;
import org.polymap.kaps.model.data.BodenRichtwertKennungComposite;
import org.polymap.kaps.model.data.BodenRichtwertRichtlinieArtDerNutzungComposite;
import org.polymap.kaps.model.data.BodenRichtwertRichtlinieErgaenzungComposite;
import org.polymap.kaps.model.data.EntwicklungsZusatzComposite;
import org.polymap.kaps.model.data.EntwicklungsZustandComposite;
import org.polymap.kaps.model.data.ErschliessungsBeitragComposite;
import org.polymap.kaps.model.data.GemeindeComposite;
import org.polymap.kaps.model.data.RichtwertzoneComposite;
import org.polymap.kaps.model.data.RichtwertzoneZeitraumComposite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class RichtwertzoneAsBorisExporter
        extends DefaultFeatureOperation
        implements IFeatureOperation {

    private static Log          log             = LogFactory.getLog( RichtwertzoneAsBorisExporter.class );

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

    protected static DateFormat fileFormat      = new SimpleDateFormat( "yyyy_MM_dd_HH_mm_ss" );


    @Override
    public boolean init( IFeatureOperationContext ctx ) {
        super.init( ctx );
        try {
            return context.featureSource().getSchema().getName().getLocalPart()
                    .equals( RichtwertzoneZeitraumComposite.NAME );
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

        final File f = File.createTempFile( "polymap_boris_export_", ".csv" );
        f.deleteOnExit();

        OutputStream out = new BufferedOutputStream( new FileOutputStream( f ) );

        try {
            write( context.features(), out, monitor );
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
                        return "boris_export_" + fileFormat.format( new Date() ) + ".csv";
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

                // String filename = view.getLayer() != null
                // ? view.getLayer().getLabel() + "_export.csv" :
                // "polymap3_export.csv";
                // String linkTarget = "../csv/" + id + "/" + filename;
                // String htmlTarget = "../csv/download.html?id=" + id + "&filename="
                // + filename;

                ExternalBrowser.open( "download_window", url, ExternalBrowser.NAVIGATION_BAR | ExternalBrowser.STATUS );
            }
        } );

        monitor.done();
        return Status.OK;
    }


    private void write( FeatureCollection features, OutputStream out, IProgressMonitor monitor )
            throws IOException {
        FeatureIterator it = null;
        Writer writer = null;
        CsvListWriter csvWriter = null;
        try {
            // TODO Trennzeichen?
            CsvPreference prefs = new CsvPreference( '"', '|', "\r\n" );

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

                // header
                if (noHeaderYet) {
                    csvWriter.writeHeader( getHeaders() );
                    noHeaderYet = false;
                }

                RichtwertzoneZeitraumComposite richtwertzone = repo.findEntity( RichtwertzoneZeitraumComposite.class,
                        feature.getIdentifier().getID() );
                // all properties
                csvWriter.write( getRow( richtwertzone ) );

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


    private String[] getHeaders() {
        return new String[] { "GESL", "GENA", "GASL", "GABE", "GENU", "GEMA", "ORTST", "WNUM", "BRW", "STAG", "BRKE",
                "BEDW", "PLZ", "BASBE", "BASMA", "YWERT", "XWERT", "BEZUG", "ENTW", "BEIT", "NUTA", "ERGNUTA", "BAUW",
                "GEZ", "WGFZ", "GRZ", "BMZ", "FLAE", "GTIE", "GBREI", "ERVE", "VERG", "VERF", "YVERG", "XVERG", "BOD",
                "ACZA", "GRZA", "AUFW", "WEER", "KOORWERT", "KOORVERF", "BEM", "FREI", "BRZNAME", "UMART", "LUMNUM" };
    }


    private List getRow( RichtwertzoneZeitraumComposite richtwertzoneG ) {

        RichtwertzoneComposite richtwertzone = richtwertzoneG.zone().get();
        Polygon geom = richtwertzone.geom().get();

        List result = new ArrayList( 41 ) {

            @Override
            public boolean add( Object e ) {
                if (e == null) {
                    return super.add( "" );
                }
                else {
                    return super.add( e );
                }
            }
        };
        // 1
        // Gemeindekennzeichen
        // gemeindekennzeichen
        // GESL
        // Zeichenkette
        // 12
        // -
        // amtlicher Gemeindeschlüssel des statistischen Bundesam-tes im Format
        // LLRKKGGGTTTT
        // @TODO das muss noch konfigurierbar werden
        // LL = Land = 14 Sachsen
        // R = Regierungsbezirk = 5
        // KK = Kreis = 22 Mittelsachsen
        // GGG = Gemeinde = 522520 = schl, enthält bereits R un KK
        // TTTT = Gemeindeteil nach landesspezifischem Schlüssel = gibts nicht
        // Pflicht
        GemeindeComposite gemeinde = richtwertzone.gemeinde().get();
        result.add( "14" + gemeinde.schl().get() + "0000" );
        // 2
        // Gemeinde
        // gemeindename
        // GENA
        // Zeichenkette
        // 30
        // -
        // Name der Gemeinde als Text
        // freiwillig
        result.add( gemeinde.name().get() );

        // 3
        // Gutachterausschuss-kennziffer
        // gutachterausschusskennziffer
        // GASL
        // Integer
        // 5
        // -
        // Nummer des zuständigen Gutachterausschusses gemäß
        // Landesschlüssel
        // Pflicht
        result.add( "00522" );

        // 4
        // Bezeichnung des Gut-achterausschusses
        // gutachterausschussbezeichnung
        // GABE
        // Zeichenkette
        // 140
        // -
        // Name des zuständigen Gutachterausschusses
        // freiwillig
        // TODO konfigurierbar
        result.add( "Gutachterausschuss für Grundstückswerte im Landkreis Mittelsachsen" );

        // 5
        // Gemarkungsnummer
        // gemarkungsnummer
        // GENU
        // Integer
        // 4
        // -
        // Gemarkungsnummer
        // freiwillig
        result.add( "" );
        // 6
        // Gemarkungsname
        // gemarkungsname
        // GEMA
        // Zeichenkette
        // 60
        // -
        // Name der Gemarkung als Text
        // freiwillig
        result.add( "" );
        // 7
        // Ortsteil
        // ortsteilName
        // ORTST
        // Zeichenkette
        // 60
        // -
        // Name des Ortsteils bzw. Stadtteils als Text
        // freiwillig
        result.add( "" );
        // 8
        // Bodenrichtwertnummer
        // bodenrichtwertNummer
        // WNUM
        // Integer
        // 7
        // -
        // Nummer des Bodenrichtwerts gemäß Landesschlüssel
        // Pflicht
        result.add( "2" + richtwertzone.schl().get() );
        // 9
        // Bodenrichtwert
        // bodenrichtwert
        // BRW
        // Dezimal
        // 5.2
        // ja
        // Bodenrichtwertangabe in €/m², auch bei Stichtagen vor 2002
        // Pflicht
        Double euro = richtwertzoneG.euroQm().get();
        String euroStr = "";
        if (euro != null && euro >= 10.0d) {
            euroStr = euroShortFormat.format( richtwertzoneG.euroQm().get() );
        }
        else {
            euroStr = euroFormat.format( richtwertzoneG.euroQm().get() );
        }
        result.add( euroStr );
        // 10
        // Stichtag des Bodenrichtwerts
        // stichtag
        // STAG
        // Datum
        // TT.MM.JJJJ
        // 10
        // ja
        // Stichtag des Bodenrichtwerts
        // Pflicht
        result.add( dateFormat.format( richtwertzoneG.stichtag().get() ) );
        // 11
        // Bodenrichtwertkennung
        // bodenrichtwertArt
        // BRKE
        // Integer
        // 1
        // -
        // 1 = zonal
        // 2 = lagetypisch (historisch)
        // Pflicht
        BodenRichtwertKennungComposite richtwertKennung = richtwertzone.bodenrichtwertKennung().get();
        result.add( richtwertKennung != null ? richtwertKennung.schl().get() : null );
        // 12
        // Bedarfswert
        // bedarfswert
        // BEDW
        // Dezimal
        // 5.2
        // -
        // Bedarfswertangabe (zum 1.1.1996) in €/m², historisch
        // freiwillig
        result.add( "" );
        // 13
        // Postleitzahl
        // postleitzahl
        // PLZ
        // Zeichenkette
        // 5
        // -
        // Nummer des Postleitzahlbezirks, in dem die Bodenricht-wertzone
        // überwiegend liegt
        // freiwillig
        result.add( "" );

        // 14
        // Basiskarten-Bezeichnung
        // basiskartenbezeichnung
        // BASBE
        // Zeichenkette
        // 20
        // -
        // Angabe zur Kartengrundlage, auf welcher der
        // Bodenrichtwert beschlossen wurde (Basiskarte)
        // freiwillig
        result.add( "TOP.sachsen" );

        // 15
        // Basiskarten-Maßstabszahl
        // basiskartenmassstabszahl
        // BASMA
        // Integer
        // 6
        // -
        // Maßstabszahl der Basiskarte
        // Pflicht
        result.add( "24000" );

        // 16
        // Rechtswert/Ostwert
        // ostwertBRW
        // YWERT
        // Integer
        // 8
        // -
        // Georeferenz der Bodenrichtwertangabe
        // (Präsentationskoordinate)
        // freiwillig
        result.add( geom != null ? Double.valueOf( geom.getCentroid().getX() ).intValue() : "" );

        // 17
        // Hochwert/Nordwert
        // nordwertBRW
        // XWERT
        // Integer
        // 7
        // -
        // Georeferenz der Bodenrichtwertangabe
        // (Präsentationskoordinate)
        // freiwillig
        result.add( geom != null ? Double.valueOf( geom.getCentroid().getY() ).intValue() : "" );

        // 18
        // Bezugssystem
        // bezugssystemBRW
        // BEZUG
        // Zeichenkette
        // 12
        // -
        // Bezugssystem der angegebenen Koordinaten gemäß
        // AdV-Schlüssel
        // DE_DHDN_3GK2
        // DE_DHDN_3GK3
        // DE_DHDN_3GK4
        // ETRS89_UTM32
        // ETRS89_UTM33
        // Pflicht
        result.add( "DE_DHDN_3GK4" );

        // 19
        // Entwicklungszustand
        // entwicklungszustand
        // ENTW
        // Zeichenkette
        // 2
        // ja
        // Entwicklungszustand nach § 5 ImmoWertV:
        // B = Baureifes Land
        // R = Rohbauland
        // E = Bauerwartungsland
        // L = Fläche der Land- oder Forstwirtschaft
        // SF = Sonstige Fläche
        // Pflicht
        EntwicklungsZustandComposite entwicklungsZustand = richtwertzone.entwicklungsZustand().get();
        result.add( entwicklungsZustand != null ? entwicklungsZustand.schl().get() : null );

        // 20
        // Beitrags- und abgaben-rechtlicher Zustand
        // beitrags_abgabenrechtlZustand
        // BEIT
        // Zeichenkette
        // 1
        // ja
        // Beitrags- und abgabenrechtlicher Zustand
        // 1 = erschließungsbeitrags- und kostenerstattungs-betragsfrei
        // 2 =erschließungsbeitrags-/kostenerstattungs-betragsfrei und
        // abgabenpflichtig nach Kom-munalabgabengesetz
        // 3 = erschließungsbeitrags-/kostenerstattungs-betragspflichtig und
        // abgabenpflichtig nach Kommunalabgabengesetz
        // Pflicht falls
        // 19=B
        ErschliessungsBeitragComposite erschliessungsBeitrag = richtwertzoneG.erschliessungsBeitrag().get();
        result.add( erschliessungsBeitrag != null ? erschliessungsBeitrag.schl().get() : null );

        // 21
        // Art der Nutzung
        // ArtNutzung
        // NUTA
        // Zeichenkette
        // 8
        // ja
        // Art der Nutzung gemäß Anlage 1 zur BRW-RL
        // Pflicht
        BodenRichtwertRichtlinieArtDerNutzungComposite artDerNutzung = richtwertzone.brwrlArt().get();
        result.add( artDerNutzung != null ? artDerNutzung.schl().get() : null );

        // 22
        // Ergänzung zur Art der Nutzung
        // ErgaenzungArtNutzung
        // ERGNUTA
        // Zeichenkette
        // 30
        // ja
        // Ergänzung zur Art der Nutzung gemäß Anlage 1 zur BRW-RL
        // Pflicht soweit wertrelevant
        BodenRichtwertRichtlinieErgaenzungComposite ergaenzung = richtwertzone.brwrlErgaenzung().get();
        result.add( ergaenzung != null ? ergaenzung.schl().get() : null );

        // 23
        // Bauweise
        // bauweise
        // BAUW
        // Zeichenkette
        // 2
        // ja
        // Bauweise des Bodenrichtwertgrundstücks:
        // o = offene Bauweise
        // g = geschlossene Bauweise
        // a = abweichende Bauweise
        // eh = Einzelhäuser
        // ed = Einzel- und Doppelhäuser
        // dh = Doppelhaushälften
        // rh = Reihenhäuser
        // rm = Reihenmittelhäuser
        // re = Reihenendhäuser
        // Pflicht soweit wertrelevant
        BauweiseComposite bauweise = richtwertzone.bauweise().get();
        result.add( bauweise != null ? bauweise.schl().get() : null );

        // 24
        // Geschosszahl
        // geschosszahl
        // GEZ
        // Zeichenkette
        // 9
        // ja
        // Geschosszahl des Bodenrichtwertgrundstücks
        // Pflicht soweit wertrelevant
        result.add( richtwertzone.geschossZahl().get() );
        // 25
        // Wertrelevante Ge-schossflächenzahl
        // wertrelevantegeschossflaechenzahl
        // WGFZ
        // Zeichenkette
        // 11
        // ja
        // wertrelevante Geschossflächenzahl des Bodenrichtwert-grundstücks gemäß
        // Nummer 6 Absatz 6 BRW-RL
        // Pflicht soweit wertrelevant
        // TODO berechnen
        result.add( "" );
        // 26
        // Grundflächenzahl
        // grundflaechenzahl
        // GRZ
        // Zeichenkette
        // 7
        // ja
        // Grundflächenzahl des Bodenrichtwertgrundstücks
        // Pflicht soweit wertrelevant
        result.add( richtwertzone.grundflaechenZahl().get() );
        // 27
        // Baumassenzahl
        // baumassenzahl
        // BMZ
        // Zeichenkette
        // 9
        // ja
        // Baumassenzahl des Bodenrichtwertgrundstücks
        // Pflicht soweit wertrelevant
        result.add( richtwertzone.baumassenZahl().get() );
        // 28
        // Fläche
        // flaeche
        // FLAE
        // Zeichenkette
        // 12
        // ja
        // Fläche des Bodenrichtwertgrundstücks in m²
        // Pflicht soweit wertrelevant
        Double groesse = richtwertzone.grundstuecksGroesse().get();
        result.add( groesse != null ? groesse.intValue() : "" );

        // 29
        // Tiefe
        // tiefe
        // GTIE
        // Zeichenkette
        // 8
        // ja
        // Tiefe des Bodenrichtwertgrundstücks in m
        // Pflicht soweit wertrelevant
        result.add( richtwertzone.grundstuecksTiefe().get() );
        // 30
        // Breite
        // breite
        // GBREI
        // Zeichenkette
        // 8
        // ja
        // Breite des Bodenrichtwertgrundstücks in m
        // Pflicht soweit wertrelevant
        result.add( richtwertzone.grundstuecksBreite().get() );
        // 31
        // Erschließungs-verhältnisse
        // erschliessungsverhaeltnisse
        // ERVE
        // Integer
        // 1
        // -
        // Qualität der Erschließungsanlagen, historisch
        // 1 = sehr gute Erschließungsverhältnisse
        // 2 = schlechte Erschließungsverhältnisse
        // freiwillig
        // TODO fehlt
        result.add( "" );

        // 32
        // Verfahrensgrund
        // verfahrensart
        // VERG
        // Zeichenkette
        // 4
        // -
        // Maßnahmen nach BauGB: Angabe des
        // Verfahrensgrundes, historisch:
        // San = Sanierungsgebiet
        // Entw = Entwicklungsbereich
        // freiwillig
        EntwicklungsZusatzComposite entwicklungsZusatz = richtwertzone.entwicklungsZusatz().get();
        String verg = "";
        if (entwicklungsZusatz != null) {
            if (entwicklungsZusatz.schl().get().startsWith( "S" )) {
                verg = "San";
            }
            else {
                verg = "Entw";
            }
        }
        result.add( verg );

        // 33
        // Entwicklungs-/Sanierungszusatz
        // EntwicklungsSanierungszusatz
        // VERF
        // Zeichenkette
        // 2
        // ja
        // Angabe des verfahrensrechtlichen Zustandes
        // (Maßnahmen nach BauGB):
        // SU = sanierungsunbeeinflusster Bodenrichtwert, ohne Berücksichtigung der
        // rechtlichen und tatsächlichen Neuordnung
        // SB = sanierungsbeeinflusster Bodenrichtwert, unter Berücksichtigung der
        // rechtlichen und tatsäch-lichen Neuordnung
        // EU = entwicklungsunbeeinflusster Bodenrichtwert, ohne Berücksichtigung
        // der rechtlichen und tatsächlichen Neuordnung
        // EB = entwicklungsbeeinflusster Bodenrichtwert, unter Berücksichtigung der
        // rechtlichen und tatsächlichen Neuordnung
        // Pflicht falls Sanierungsgebiet oder Entwicklungsbereich
        result.add( entwicklungsZusatz != null ? entwicklungsZusatz.schl().get() : null );

        // 34
        // Rechtswert/Ostwert der
        // Maßnahme
        // ostwertVerf
        // YVERG
        // Integer
        // 8
        // -
        // Georeferenz der Beschriftung zur städtebaulichen
        // Maßnahme (Präsentationskoordinate), historisch
        // freiwillig
        result.add( entwicklungsZusatz != null && geom != null ? Double.valueOf( geom.getCentroid().getX() )
                .intValue() : "" );

        // 35
        // Hochwert/Nordwert der Maßnahme
        // nordwertVerf
        // XVERG
        // Integer
        // 7
        // -
        // Georeferenz der Beschriftung zur städtebaulichen Maß-nahme
        // (Präsentationskoordinate), historisch
        // freiwillig
        // TODO ei jedem BRW im San- Gebiet sind zudem die Koordinaten (Feld 34/35)
        // für die Visualisierung der Beschriftung "San" anzugeben, möglichst nicht
        // im Mittelpunkt des Polygons, da dort der BRW platziert wird
        result.add( entwicklungsZusatz != null && geom != null ? Double.valueOf( geom.getCentroid().getY() )
                .intValue() : "" );
        // 36
        // Bodenart
        // bodenart
        // BOD
        // Zeichenkette
        // 6
        // -
        // Bodenart gemäß Bodenschätzungsgesetz
        // Pflicht soweit wertrelevant
        result.add( "" );
        // 37
        // Ackerzahl
        // ackerzahl
        // ACZA
        // Zeichenkette
        // 7
        // ja
        // Ackerzahl des Bodenrichtwertgrundstücks
        // Pflicht soweit wertrelevant
        result.add( richtwertzone.ackerZahl().get() );
        // 38
        // Grünlandzahl
        // gruenlandzahl
        // GRZA
        // Zeichenkette
        // 7
        // ja
        // Grünlandzahl des Bodenrichtwertgrundstücks
        // Pflicht soweit wertrelevant
        result.add( richtwertzone.gruenLandZahl().get() );
        // 39
        // Aufwuchs
        // aufwuchs
        // AUFW
        // Zeichenkette
        // 2
        // -
        // land- und forstwirtschaftliche Richtwerte mit Berücksich-tigung des
        // Aufwuchses, historisch:
        // mA = mit Aufwuchs
        // freiwillig
        result.add( "" );
        // 40
        // Wegeerschließung
        // wegeerschliessung
        // WEER
        // Zeichenkette
        // 1
        // -
        // Wegeerschließung für land-/forstwirtschaftliche Flächen:
        // 1 = erschlossen
        // 0 = nicht erschlossen
        // freiwillig
        result.add( "" );
        // 41
        // Koordinatenliste
        // Bodenrichtwertzone
        // koordlisteBRW
        // KOORWERT
        // Zeichenkette
        // 500000
        // -
        // Umgrenzung der Bodenrichtwertzone; Angabe im
        // Format WKT; Koordinaten als Rechts- bzw. Ostwert
        // und Hoch- bzw. Nordwert jeweils im Format 7.3 oder
        // 8.3
        // Pflicht, falls
        // 11=1
        result.add( geom != null ? new WKTWriter().writeFormatted( geom ) : null );

        // 42
        // Koordinatenliste
        // Verfahren
        // umring
        // KOORVERF
        // Zeichenkette
        // 120000
        // -
        // Umgrenzung der städtebaulichen Maßnahme; Angabe im Format WKT; Koordinaten
        // als Rechts- bzw. Ostwert und Hoch- bzw. Nordwert jeweils im Format 7.3
        // oder 8.3, historisch
        // freiwillig
        String row42 = "";
        if (entwicklungsZusatz != null && geom != null && entwicklungsZusatz.schl().get().startsWith( "S" )) {
            row42 = new WKTWriter().writeFormatted( geom );
        }
        result.add( row42 );

        // 43
        // Bemerkungen
        // bemerkungen
        // BEM
        // Zeichenkette
        // 255
        // -
        // Sonstige Hinweise
        // freiwillig
        result.add( "" );

        // 44
        // Freies Feld
        // landesspezifischeAngaben
        // FREI
        // Zeichenkette
        // 255
        // -
        // Merkmal von den Ländern frei belegbar; Zuordnung zum Bundesland über das
        // Gemeindekennzeichen
        // freiwillig
        result.add( "" );
        // 45
        // Bodenrichtwertzonen-name
        // bodenrichtwertzonenname
        // BRZNAME
        // Zeichenkette
        // 255
        // -
        // Bezeichnung der Bodenrichtwertzone
        // freiwillig
        result.add( richtwertzone.name().get() );
        // 46
        // Art der Umrechnungs-faktorendokumentation
        // umrechnungdokart
        // UMART
        // Zeichenkette
        // 1
        // -
        // 0 = Dateidokumente
        // 1 = Datensatz
        // freiwillig
        result.add( "" );
        // 47
        // Umrechnungstabellen
        // liste_umrechnungstabellen
        // LUMNUM
        // Zeichenkette
        // 255
        // ja
        // Liste der Nummern der Umrechnungstabellen
        // (Datensatz) oder Liste der Namen der Dateidokumente
        // Pflicht soweit wertrelevant
        result.add( "" );

        return result;
    }
}
