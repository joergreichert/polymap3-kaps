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
import org.polymap.kaps.model.data.NutzungComposite;
import org.polymap.kaps.model.data.RichtwertzoneComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.model.data.VertragsdatenAgrarComposite;
import org.polymap.kaps.model.data.VertragsdatenErweitertComposite;
import org.polymap.kaps.ui.form.EingangsNummerFormatter;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class VertragsdatenAgrarAlsAgrarExporter
        extends AbstractSingleRowExcelExporter<VertragsdatenAgrarComposite> {

    public VertragsdatenAgrarAlsAgrarExporter() {
        super( VertragsdatenAgrarComposite.class, VertragsdatenAgrarComposite.NAME, "statistik_agrar", "Verträge" );
    }


    protected List<Value> createValues( VertragsdatenAgrarComposite vdb, List<String> errors ) {

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
        result.add( new Value( "Flurstücksnummer", flurstueck.hauptNummer().get() ) );
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


    @Override
    protected String getSortString( VertragsdatenAgrarComposite entity ) {
        VertragComposite vertrag = entity.vertrag().get();
        if (vertrag == null) {
            return entity.toString();
        }
        return EingangsNummerFormatter.format( vertrag.eingangsNr().get() );
    }
}
