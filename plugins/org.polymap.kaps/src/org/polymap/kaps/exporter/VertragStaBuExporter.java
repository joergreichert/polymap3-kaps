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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
import org.polymap.kaps.model.data.ArtDerBauflaecheStaBuComposite;
import org.polymap.kaps.model.data.FlurstueckComposite;
import org.polymap.kaps.model.data.GebaeudeArtStaBuComposite;
import org.polymap.kaps.model.data.GebaeudeTypStaBuComposite;
import org.polymap.kaps.model.data.GemarkungComposite;
import org.polymap.kaps.model.data.GemeindeComposite;
import org.polymap.kaps.model.data.KaeuferKreisComposite;
import org.polymap.kaps.model.data.KaeuferKreisStaBuComposite;
import org.polymap.kaps.model.data.KellerComposite;
import org.polymap.kaps.model.data.NutzungComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.model.data.VertragsdatenBaulandComposite;
import org.polymap.kaps.model.data.VertragsdatenErweitertComposite;
import org.polymap.kaps.model.data.WohnlageStaBuComposite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class VertragStaBuExporter
        extends DefaultFeatureOperation
        implements IFeatureOperation {

    private static Log          log             = LogFactory.getLog( VertragStaBuExporter.class );

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

    private static DateFormat   dateFormat      = new SimpleDateFormat( "yyyyMMdd" );

    private static DateFormat   timeFormat      = new SimpleDateFormat( "h24ssmm" );


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

        final File f = File.createTempFile( "StatBundesamt_PreisindizesWohnimmobilien", ".xml" );
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
                        return "text/xml; charset=ISO-8859-1";
                    }


                    public String getFilename() {
                        return "StatBundesamt_PreisindizesWohnimmobilien.xml";
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

                log.info( "XML: download URL: " + url );

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

        final KapsRepository repo = KapsRepository.instance();

        FeatureIterator it = null;

        StreamResult result = new StreamResult( out );

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElementNS( "http://www.destatis.de/schema/datml-raw/2.0/de", "DatML-RAW-D" );
            rootElement.setAttribute( "version", "2.0" );
            doc.appendChild( rootElement );

            // optionen elements
            Element optionen = doc.createElement( "optionen" );
            rootElement.appendChild( optionen );

            rootElement.appendChild( createProtokoll( doc ) );

            rootElement.appendChild( createAbsender( doc ) );

            rootElement.appendChild( createEmpfaenger( doc ) );

            Element nachricht = doc.createElement( "nachricht" );
            rootElement.appendChild( nachricht );

            it = features.features();
            Calendar cal = new GregorianCalendar();
            // quartal zurückrollen
            cal.roll( Calendar.MONTH, -3 );
            if (it.hasNext()) {
                VertragComposite vertrag = repo.findEntity( VertragComposite.class, it.next().getIdentifier().getID() );
                cal.setTime( vertrag.vertragsDatum().get() );
            }
            Element datenSegment = createDatenSegment( nachricht, cal.get( Calendar.YEAR ),
                    cal.get( Calendar.MONTH ) / 3 );

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

                VertragComposite vertrag = repo.findEntity( VertragComposite.class, feature.getIdentifier().getID() );
                // all properties
                createSegment( datenSegment, vertrag );

            }

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource( doc );

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);

            transformer.transform( source, result );
        }
        catch (TransformerConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            if (out != null) {
                out.flush();
                out.close();
            }
            if (it != null) {
                it.close();
            }
        }
    }


    private void createSegment( Element datenSegment, VertragComposite vertrag ) {

        // Iterable<FlurstueckComposite> flurstuecke =
        // FlurstueckComposite.Mixin.forEntity( vertrag );
        // FlurstueckComposite flurstueck = null;
        Double bodenrichtwert = null;
        Integer baujahr = null;
        Double wohnflaeche = null;
        GebaeudeTypStaBuComposite gebaeudeTyp = null;
        GebaeudeArtStaBuComposite gebaeudeArt = null;
        WohnlageStaBuComposite wohnlage = null;
        String stellplatz = null;
        String garage = null;
        String carport = null;

        KellerComposite keller = null;
        Double grundstuecksflaeche = null;
        VertragsdatenBaulandComposite baulandComposite = VertragsdatenBaulandComposite.Mixin.forVertrag( vertrag );
        if (baulandComposite != null) {
            bodenrichtwert = baulandComposite.richtwert().get();
            baujahr = baulandComposite.baujahr().get();
            wohnflaeche = baulandComposite.wohnflaeche().get();
            gebaeudeTyp = baulandComposite.gebaeudeTypStaBu().get();
            gebaeudeArt = baulandComposite.gebaeudeArtStaBu().get();
            keller = baulandComposite.keller().get();
            wohnlage = baulandComposite.wohnlageStaBu().get();
            stellplatz = baulandComposite.stellplaetze().get();
            carport = baulandComposite.carport().get();
            garage = baulandComposite.garage().get();
            grundstuecksflaeche = baulandComposite.verkaufteFlaeche().get();
        }
        FlurstueckComposite flurstueck = null;
        for (FlurstueckComposite f : FlurstueckComposite.Mixin.forEntity( vertrag )) {
            NutzungComposite nutzung = f.nutzung().get();
            if (nutzung.isAgrar().get() != null && !nutzung.isAgrar().get().booleanValue()) {
                flurstueck = f;
                break;
            }
        }

        NutzungComposite nutzung = flurstueck.nutzung().get();
        Calendar cal = new GregorianCalendar();
        cal.setTime( vertrag.vertragsDatum().get() );

        GemarkungComposite gemarkung = flurstueck.gemarkung().get();
        GemeindeComposite gemeinde = gemarkung != null ? gemarkung.gemeinde().get() : null;

        KaeuferKreisComposite verkaeufer = vertrag.verkaeuferKreis().get();
        KaeuferKreisStaBuComposite veraeusserer = verkaeufer != null ? verkaeufer.kaeuferKreisStabu().get() : null;

        KaeuferKreisComposite kaeufer = vertrag.kaeuferKreis().get();
        KaeuferKreisStaBuComposite erwerber = kaeufer != null ? kaeufer.kaeuferKreisStabu().get() : null;

        ArtDerBauflaecheStaBuComposite artDerBauflaeche = nutzung.artDerBauflaeche().get();

        VertragsdatenErweitertComposite vertragsdatenErweitertComposite = vertrag.erweiterteVertragsdaten().get();
        Double kaufpreis = vertragsdatenErweitertComposite != null
                && vertragsdatenErweitertComposite.bereinigterVollpreis().get() != null ? vertragsdatenErweitertComposite
                .bereinigterVollpreis().get() : vertrag.vollpreis().get();

        // TODO Wohnfläche, Baujahr
        // Liste
        // WohnungComposite wohnung = WohnungComposite.Mixin.findWohnungenFor(
        // flurstueck );

        // grundstücksfläche

        Element satz = addElement( datenSegment, "satz" );
        String name;
        // TODO bebaut = 1, wohneigentum = 2
        // TODO sollte hier nicht auch mal Wohneigentum auftauchen?
        // if (nutzung.isWohneigentum().get()) {
        // satz.setAttribute( "kennung", "SA-Wohneigentum" );
        // addMM( satz, "Satzart", "2" );
        // name = "Wohnungseigentum";
        // }
        // else {
        satz.setAttribute( "kennung", "SA-Bebaute-Grundstücke" );
        addMM( satz, "Satzart", "1" );
        name = "Bebaute_Grundstuecke";
        // }

        Element mmgrOuter = addElement( satz, "mmgr" );
        mmgrOuter.setAttribute( "name", name );

        Element pwi = addElement( mmgrOuter, "mmgr" );
        pwi.setAttribute( "name", "PWI" );

        addMM( pwi, "Kennummer_Kauffall", String.valueOf( vertrag.eingangsNr().get() ) );
        addMM( pwi, "Kaufdatum_TT", twoCol( cal.get( Calendar.DAY_OF_MONTH ) ) );
        addMM( pwi, "Kaufdatum_MM", twoCol( cal.get( Calendar.MONTH ) + 1 ) );
        addMM( pwi, "Kaufdatum_JJJJ", String.valueOf( cal.get( Calendar.YEAR ) ) );
        // TODO Landkreiskennung in Properties erfassen
        if (gemeinde != null) {
            addMM( pwi, "Gemeindeschluessel", "14" + gemeinde.schl().get() );
        }
        if (gemarkung != null) {
            addMM( pwi, "Gemarkungsschluessel", gemarkung.schl().get() );
        }
        if (veraeusserer != null) {
            addMM( pwi, "Veraeusserer", veraeusserer.schl().get() );
        }
        if (erwerber != null) {
            addMM( pwi, "Erwerber", erwerber.schl().get() );
        }
        if (kaufpreis != null) {
            addMM( pwi, "Kaufpreis", String.valueOf( kaufpreis.intValue() ) );
        }
        if (bodenrichtwert != null) {
            addMM( pwi, "Bodenrichtwert", String.valueOf( bodenrichtwert.intValue() ) );
        }
        if (artDerBauflaeche != null) {
            // schlüssel stabu in winakps und stabexport weichen voneinander ab
            addMM( pwi, "Flaechenart", artDerBauflaeche.schl().get() == "1" ? "10" : "21" );
        }
        if (baujahr != null) {
            addMM( pwi, "Baujahr", String.valueOf( baujahr.intValue() ) );
        }
        if (wohnflaeche != null) {
            addMM( pwi, "Wohnflaeche", String.valueOf( wohnflaeche.intValue() ) );
        }
        // TODO Stellplatz
        // if (carport != null && carport.equals( "J" ) || )

        if (wohnlage != null) {
            addMM( pwi, "Lagequalitaet", wohnlage.schl().get() );
        }

        // TODO Unterscheidung Wohnungseigentum vs. Grundstuecke
        // if (nutzung.isWohneigentum().get()) {
        // Element wohnungseigentum = addElement( mmgrOuter, "Wohnungseigentum" );
        // }
        // else {
        Element grundstuecke = addElement( mmgrOuter, "Grundstuecke" );
        if (grundstuecksflaeche != 0.0d) {
            addMM( grundstuecke, "Grundstuecksflaeche", String.valueOf( new Double( grundstuecksflaeche ).intValue() ) );
        }

        if (gebaeudeArt != null) {
            addMM( grundstuecke, "Gebaeudeart", gebaeudeArt.schl().get() );
        }
        if (gebaeudeTyp != null) {
            addMM( grundstuecke, "Gebaeudetyp", gebaeudeTyp.schl().get() );
        }
        if (keller != null && !keller.schl().get().endsWith( "1" )) {
            if (keller.schl().get().equals( "2" )) {
                addMM( grundstuecke, "Unterkellerung", "0" );
            }
            else {
                addMM( grundstuecke, "Unterkellerung", "1" );
            }
        }

    }


    private String twoCol( int value ) {
        return (value < 10) ? "0" + value : "" + value;
    }


    private void addMM( Element parent, String mmName, String wert ) {
        Element mm = addElement( parent, "mm" );
        mm.setAttribute( "name", mmName );
        addTextElement( mm, "wert", wert );
    }


    private Element createDatenSegment( Element nachricht, int jahr, int quartal ) {
        addTextElement( nachricht, "nachrichtenID", "1" );

        Element erhebung = addElement( nachricht, "erhebung" );
        addTextElement( erhebung, "kennung", "0296", "klasse", "ERHID" );

        Element berichtszeitraum = addElement( nachricht, "berichtszeitraum" );
        addTextElement( berichtszeitraum, "jahr", String.valueOf( jahr ) );
        addTextElement( berichtszeitraum, "quartal", String.valueOf( quartal ) );

        // optional
        // createBerichtsPflichtiger( nachricht );

        createBerichtsEmpfaenger( nachricht );

        Element segment = addElement( nachricht, "segment" );
        Element hmm = addElement( segment, "hmm" );
        hmm.setAttribute( "name", "BerichtseinheitID" );
        // TODO Mittelsachsen konfigurierbar
        addTextElement( hmm, "wert", "1410522" );

        Element datensegment = addElement( segment, "datensegment" );
        addTextElement( datensegment, "meldungsID", "1" );

        return datensegment;
    }


    private void createBerichtsEmpfaenger( Element nachricht ) {
        Element berichtsempfaenger = addElement( nachricht, "berichtsempfaenger" );
        addTextElement( berichtsempfaenger, "kennung", "00", "klasse", "STAID" );

        Element identifikation = addElement( berichtsempfaenger, "identifikation" );
        Element identitaet = addElement( identifikation, "identitaet" );
        Element organisation = addElement( identitaet, "organisation" );
        addTextElement( organisation, "name", "Statistisches Bundesamt" );
    }


    private Node createEmpfaenger( Document doc ) {
        // empfaenger elements
        Element empfaenger = doc.createElement( "empfaenger" );
        addTextElement( empfaenger, "kennung", "99", "klasse", "STAID" );
        return empfaenger;
    }


    private Element createAbsender( Document doc ) {
        // absender elements
        Element absender = doc.createElement( "absender" );
        addTextElement( absender, "kennung", "9900019233", "klasse", "MELDID" );

        Element identifikation = addElement( absender, "identifikation" );
        Element identitaet = addElement( identifikation, "identitaet" );
        Element organisation = addElement( identitaet, "organisation" );
        addTextElement( organisation, "name", "Landratsamt Mittelsachsen" );

        Element adresse = addElement( identifikation, "adresse" );
        addTextElement( adresse, "strasse", "Frauensteiner Straße" );
        addTextElement( adresse, "hausnummer", "43" );
        addTextElement( adresse, "postleitzahl", "09599" );
        addTextElement( adresse, "ort", "Freiberg" );

        Element kontakt = addElement( absender, "kontakt" );
        Element identitaet2 = addElement( kontakt, "identitaet" );
        Element person = addElement( identitaet2, "person" );
        addTextElement( person, "vorname", "Birgit" );
        addTextElement( person, "nachname", "Schellenberg" );

        return absender;
    }


    private Element createProtokoll( Document doc ) {

        // protokoll elements
        Element protokoll = doc.createElement( "protokoll" );
        Element instanz = addElement( protokoll, "dokumentinstanz" );

        addTextElement( instanz, "datum", dateFormat.format( new Date() ) );
        addTextElement( instanz, "uhrzeit", timeFormat.format( new Date() ) );

        Element anwendung = addElement( instanz, "anwendung" );

        addTextElement( anwendung, "anwendungsname", "KAPS" );
        addTextElement( anwendung, "version", "1.0.0" );
        addTextElement( anwendung, "hersteller", "Polymap GmbH, Leipzig, http://polymap.de" );

        return protokoll;
    }


    private Element addElement( Element parent, String elementName ) {
        Element e = parent.getOwnerDocument().createElement( elementName );
        parent.appendChild( e );
        return e;
    }


    private Element addTextElement( Element parent, String elementName, String text ) {
        Element e = addElement( parent, elementName );
        e.appendChild( parent.getOwnerDocument().createTextNode( text ) );
        return e;
    }


    private Element addTextElement( Element parent, String elementName, String text, String attrName, String attrValue ) {
        Element e = addTextElement( parent, elementName, text );
        e.setAttribute( attrName, attrValue );
        return e;
    }
}
