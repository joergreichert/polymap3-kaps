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
import java.util.List;

import org.polymap.kaps.model.data.FlurstueckComposite;
import org.polymap.kaps.model.data.GemarkungComposite;
import org.polymap.kaps.model.data.GemeindeComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.model.data.VertragsdatenErweitertComposite;
import org.polymap.kaps.model.data.WohnungComposite;
import org.polymap.kaps.ui.form.EingangsNummerFormatter;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class WohnungAlsETWExporter
        extends AbstractSingleRowExcelExporter<WohnungComposite> {

    public WohnungAlsETWExporter() {
        super( WohnungComposite.class, WohnungComposite.NAME, "statistik_etw", "Verträge" );
    }


    protected List<Value> createValues( WohnungComposite wohnung, List<String> errors ) {

        List<Value> result = new ArrayList<Value>();
        VertragComposite vertrag = wohnung.vertrag().get();
        if (vertrag == null) {
            errors.add( "Keinen Vertrag gefunden!" );
            return result;
        }
        FlurstueckComposite flurstueck = wohnung.flurstueck().get();
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
        // RichtwertzoneComposite zone = flurstueck.richtwertZone().get();
        // if (zone == null) {
        // errors.add( error( vertrag, "Keine Richtwertzone gefunden!" ) );
        // return result;
        // }
        Double preis = wohnung.bereinigterVollpreis().get();
        if (preis == null) {
            preis = vertrag.vollpreis().get();
            VertragsdatenErweitertComposite ew = vertrag.erweiterteVertragsdaten().get();
            if (ew != null && ew.bereinigterVollpreis().get() != null) {
                preis = ew.bereinigterVollpreis().get();
            }
        }

        result.add( new Value( "Eingangsnummer", EingangsNummerFormatter.format( vertrag.eingangsNr().get() ) ) );
        result.add( new Value( "Vertragsdatum", vertrag.vertragsDatum().get() ) );
        result.add( new Value( "Gemeinde", gemeinde.name().get() ) );
        result.add( new Value( "Gemarkung", gemarkung.name().get() ) );
        result.add( new Value( "Strasse", flurstueck.strasse().get() != null ? flurstueck.strasse().get().name().get()
                : "" ) );
        result.add( new Value( "Hausnummer", flurstueck.hausnummer().get() ) );
        result.add( new Value( "Zusatz", flurstueck.hausnummerZusatz().get() ) );
        result.add( new Value( "Flurstücksnummer", flurstueck.hauptNummer().get() ) );
        result.add( new Value( "Unternummer", flurstueck.unterNummer().get() ) );

        result.add( new Value( wohnung, wohnung.objektNummer(), false ) );
        result.add( new Value( wohnung, wohnung.gebaeudeNummer(), false ) );
        result.add( new Value( wohnung, wohnung.wohnungsNummer(), false ) );
        result.add( new Value( wohnung, wohnung.wohnungsFortfuehrung(), false ) );

        result.add( new Value( "Gebäudeart", flurstueck.gebaeudeArt().get() != null ? flurstueck.gebaeudeArt().get()
                .name().get() : null ) );
        result.add( new Value( wohnung, wohnung.baujahr(), 0, false ) );
        result.add( new Value( wohnung, wohnung.umbau(), false ) );
        result.add( new Value( wohnung, wohnung.anzahlZimmer(), 0, false ) );
        result.add( new Value( wohnung, wohnung.wohnflaeche(), 0, true ) );
        result.add( new Value( wohnung, wohnung.etage() ) );
        result.add( new Value( wohnung, wohnung.ausstattungSchluessel() ) );
        result.add( new Value( "Balkon", wohnung.balkon().get() != null
                && wohnung.balkon().get().equalsIgnoreCase( "J" ) ) );
        result.add( new Value( "Terrasse", wohnung.terrasse().get() != null
                && wohnung.terrasse().get().equalsIgnoreCase( "J" ) ) );
        result.add( new Value( wohnung, wohnung.anzahlGaragen(), true ) );
        result.add( new Value( wohnung, wohnung.abschlagGarage(), 2, true ) );
        result.add( new Value( wohnung, wohnung.anzahlStellplatz(), true ) );
        result.add( new Value( wohnung, wohnung.abschlagStellplatz(), 2, true ) );
        result.add( new Value( wohnung, wohnung.anzahlAnderes(), true ) );
        result.add( new Value( wohnung, wohnung.abschlagAnderes(), 2, true ) );

        result.add( new Value( "Bereinigter Vollpreis", preis, 2, true ) );
        result.add( new Value( wohnung, wohnung.vollpreisWohnflaeche(), 2, true ) );

        result.add( new Value( wohnung.zurAuswertungGeeignet(), wohnung ) );
        result.add( new Value( "Vermietet", wohnung.vermietet().get() != null
                && wohnung.vermietet().get().equalsIgnoreCase( "J" ) ) );

        result.add( new Value( wohnung, wohnung.liegenschaftsZins(), 2, true ) );
        // result.add(new Value(wohnung, wohnung.))
        return result;
    }
    

    @Override
    protected String getSortString( WohnungComposite entity ) {
        VertragComposite vertrag = entity.vertrag().get();
        if (vertrag == null) {
            return entity.schl().get();
        }
        return EingangsNummerFormatter.format( vertrag.eingangsNr().get() ) + "/" + entity.schl().get();
    }
}
