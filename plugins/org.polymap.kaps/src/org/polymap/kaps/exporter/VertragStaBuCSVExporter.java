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
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import org.polymap.kaps.model.data.ArtDerBauflaecheStaBuComposite;
import org.polymap.kaps.model.data.FlurstueckComposite;
import org.polymap.kaps.model.data.GebaeudeArtStaBuComposite;
import org.polymap.kaps.model.data.GebaeudeComposite;
import org.polymap.kaps.model.data.GebaeudeTypStaBuComposite;
import org.polymap.kaps.model.data.GemarkungComposite;
import org.polymap.kaps.model.data.GemeindeComposite;
import org.polymap.kaps.model.data.KaeuferKreisComposite;
import org.polymap.kaps.model.data.KaeuferKreisStaBuComposite;
import org.polymap.kaps.model.data.KellerComposite;
import org.polymap.kaps.model.data.NutzungComposite;
import org.polymap.kaps.model.data.RichtwertzoneComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.model.data.VertragsdatenBaulandComposite;
import org.polymap.kaps.model.data.VertragsdatenErweitertComposite;
import org.polymap.kaps.model.data.WohnlageStaBuComposite;
import org.polymap.kaps.model.data.WohnungComposite;
import org.polymap.kaps.ui.form.EingangsNummerFormatter;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class VertragStaBuCSVExporter
        extends AbstractExcelExporter<VertragComposite> {

    public VertragStaBuCSVExporter() {
        super( VertragComposite.class, VertragComposite.NAME, "StatBundesamt_PreisindizesWohnimmobilien", "Verträge" );
    }


    @Override
    protected boolean headersEnabled() {
        return false;
    }

    private boolean isFirstLine = true;


    @Override
    protected List<List<Value>> createMultiRowValues( VertragComposite vertrag, List<String> errors ) {
        List<List<Value>> values = Lists.newArrayList();
        if (isFirstLine) {
            List<Value> firstLine = Lists.newArrayList();
            firstLine.add( new Value( "BerichtseinheitID", "1410522" ) );
            values.add( firstLine );
            isFirstLine = false;
        }

        values.add( createValues( vertrag, errors ) );
        return values;
    }


    protected List<Value> createValues( VertragComposite vertrag, List<String> errors ) {

        List<Value> result = Lists.newArrayList();

        Double bodenrichtwert = null;
        Integer baujahr = null;
        Double wohnflaeche = null;
        GebaeudeTypStaBuComposite gebaeudeTyp = null;
        GebaeudeArtStaBuComposite gebaeudeArt = null;
        WohnlageStaBuComposite wohnlage = null;
        String stellplatz = "";

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
            String stellplatzB = baulandComposite.stellplaetze().get();
            if (stellplatzB != null) { 
                stellplatz = "J".equalsIgnoreCase( stellplatzB ) ? "1" : "0";
            }
            String carport = baulandComposite.carport().get();
            if (carport != null && "J".equalsIgnoreCase( carport )) {
                stellplatz = "2";
            }
            String garage = baulandComposite.garage().get();
            if (garage != null && "J".equalsIgnoreCase( garage )) {
                stellplatz = "2";
            }
            grundstuecksflaeche = baulandComposite.verkaufteFlaeche().get();
        }
        FlurstueckComposite flurstueck = null;
        // double verkaufteFlaeche = 0.0d;
        for (FlurstueckComposite fs : FlurstueckComposite.Mixin.forEntity( vertrag )) {
            NutzungComposite nutzung = fs.nutzung().get();
            if (nutzung != null && nutzung.isAgrar().get() != null && !nutzung.isAgrar().get().booleanValue()) {
                flurstueck = fs;
                // Double flaeche = flurstueck.verkaufteFlaeche().get();
                // if (flaeche != null) {
                // verkaufteFlaeche += flaeche.doubleValue();
                // }
            }
        }
        if (flurstueck == null) {
            errors.add( error( vertrag, "Kein Flurstück gefunden!" ) );
            return result;
        }
        NutzungComposite nutzung = flurstueck.nutzung().get();
        Calendar cal = new GregorianCalendar();
        cal.setTime( vertrag.vertragsDatum().get() );

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

        KaeuferKreisComposite verkaeufer = vertrag.verkaeuferKreis().get();
        KaeuferKreisStaBuComposite veraeusserer = verkaeufer != null ? verkaeufer.kaeuferKreisStabu().get() : null;

        KaeuferKreisComposite kaeufer = vertrag.kaeuferKreis().get();
        KaeuferKreisStaBuComposite erwerber = kaeufer != null ? kaeufer.kaeuferKreisStabu().get() : null;

        ArtDerBauflaecheStaBuComposite artDerBauflaeche = nutzung.artDerBauflaeche().get();

        // kann null sein
        VertragsdatenErweitertComposite ew = vertrag.erweiterteVertragsdaten().get();
        Double kaufpreis = vertrag.vollpreis().get();
        if (ew != null && ew.bereinigterVollpreis().get() != null) {
            kaufpreis = ew.bereinigterVollpreis().get();
        }

        if (nutzung.isWohneigentum().get().booleanValue()) {
            result.add( new Value( "Satzart", 2 ) );
        }
        else {
            result.add( new Value( "Satzart", "1" ) );
        }
        result.add( new Value( "Kennummer_Kauffall", EingangsNummerFormatter.format( vertrag.eingangsNr().get() ) ) );
        result.add( new Value( "Kaufdatum_TT", twoCol( cal.get( Calendar.DAY_OF_MONTH ) ) ) );
        result.add( new Value( "Kaufdatum_MM", twoCol( cal.get( Calendar.MONTH ) + 1 ) ) );
        result.add( new Value( "Kaufdatum_JJJJ", String.valueOf( cal.get( Calendar.YEAR ) ) ) );
        result.add( new Value( "Gemeindeschluessel", "14" + gemeinde.schl().get() ) );
        result.add( new Value( "Gemarkungsschluessel", gemarkung.schl().get() ) );
        result.add( new Value( "Veraeusserer", veraeusserer != null ? veraeusserer.schl().get() : "" ) );
        result.add( new Value( "Erwerber", erwerber != null ? erwerber.schl().get() : "" ) );
        result.add( new Value( "Kaufpreis", kaufpreis != null ? String.valueOf( kaufpreis.intValue() ) : "" ) );
        result.add( new Value( "Bodenrichtwert",
                bodenrichtwert != null ? String.valueOf( bodenrichtwert.intValue() ) : "" ) );
        // schlüssel stabu in winakps und stabexport weichen voneinander ab
        result.add( new Value( "Flaechenart",
                artDerBauflaeche != null && artDerBauflaeche.schl().get() == "1" ? "10" : "21" ) );
        result.add( new Value( "Baujahr", baujahr != null ? String.valueOf( baujahr.intValue() ) : "" ) );
        result.add( new Value( "Wohnflaeche", wohnflaeche != null ? String.valueOf( wohnflaeche.intValue() ) : "" ) );
        // TODO Stellplatz
        result.add( new Value( "Stellplatz", stellplatz) );
        result.add( new Value( "Lagequalitaet", wohnlage != null ? wohnlage.schl().get() : "" ) );

        if (nutzung.isWohneigentum().get().booleanValue()) {
            Iterator<WohnungComposite> wohnungen = WohnungComposite.Mixin.findWohnungenFor( vertrag ).iterator();
            WohnungComposite wohnung = wohnungen.hasNext() ? wohnungen.next() : null;
            // nur wenn EINE wohnung vorhanden
            if (wohnung != null && !wohnungen.hasNext()) {
                GebaeudeComposite gebaeude = GebaeudeComposite.Mixin.forKeys( wohnung.objektNummer().get(),
                        wohnung.gebaeudeNummer().get() );
                result.add( new Value( "Vertragsart", wohnung.immobilienArtStaBu().get() != null ? wohnung.immobilienArtStaBu().get().schl().get() : "" ) );
                result.add(
                        new Value( "Anzahl_Wohneinheiten", gebaeude != null ? gebaeude.wohnEinheiten().get() : null ) );
                result.add(
                        new Value( "Anzahl_Geschosse", gebaeude != null ? gebaeude.anzahlGeschosse().get() : null ) );
                result.add( new Value( "Stockwerk", wohnung.stockwerkStaBu().get() != null ? wohnung.stockwerkStaBu().get().schl().get() : "" ) );
                result.add( new Value( "Anzahl_Zimmer",
                        wohnung.anzahlZimmer().get() != null ? String.valueOf( wohnung.anzahlZimmer().get() ) : "" ) );
                result.add( new Value( "Fahrstuhl", wohnung.aufzug().get() != null
                        ? (wohnung.aufzug().get().equalsIgnoreCase( "J" ) ? "1" : "0") : "" ) );
                result.add( new Value( "Vermietung", wohnung.vermietet().get() != null
                        ? (wohnung.vermietet().get().equalsIgnoreCase( "J" ) ? "1" : "0") : "" ) );
            }
        }
        else {
            // bebaute Grundstücke
            result.add( new Value( "Grundstuecksflaeche", grundstuecksflaeche != null && grundstuecksflaeche != 0.0d
                    ? String.valueOf( new Double( grundstuecksflaeche ).intValue() ) : "" ) );
            result.add( new Value( "Gebaeudeart", gebaeudeArt != null ? gebaeudeArt.schl().get() : "" ) );
            result.add( new Value( "Gebaeudetyp", gebaeudeTyp != null ? gebaeudeTyp.schl().get() : "" ) );
            if (keller != null && !keller.schl().get().endsWith( "1" )) {
                result.add( new Value( "Unterkellerung", keller.schl().get().equals( "2" ) ? "0" : "1" ) );
            }
            else {
                result.add( new Value( "Unterkellerung", "" ) );
            }
        }
        return result;
    }


    private String twoCol( int value ) {
        return (value < 10) ? "0" + value : "" + value;
    }


    @Override
    protected String getSortString( VertragComposite vertrag ) {
        return EingangsNummerFormatter.format( vertrag.eingangsNr().get() );
    }

}
