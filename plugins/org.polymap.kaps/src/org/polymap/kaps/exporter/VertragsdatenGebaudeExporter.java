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
import org.polymap.kaps.model.data.StrasseComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.model.data.VertragsdatenBaulandComposite;
import org.polymap.kaps.model.data.VertragsdatenErweitertComposite;
import org.polymap.kaps.ui.form.EingangsNummerFormatter;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class VertragsdatenGebaudeExporter
        extends AbstractExcelExporter<VertragComposite> {

    public VertragsdatenGebaudeExporter() {
        super( VertragComposite.class, VertragComposite.NAME, "gebaeude", "Gebäude" );
    }


    @Override
    protected List<List<Value>> createMultiRowValues( VertragComposite vertrag, List<String> errors ) {
        List<List<Value>> result = new ArrayList<List<Value>>();

        VertragsdatenBaulandComposite vdc = VertragsdatenBaulandComposite.Mixin.forVertrag( vertrag );
        if (vdc == null) {
            errors.add( error( vertrag, "Keine VertragsdatenBauland gefunden!" ) );
            return result;
        }
        boolean firstRow = true;
        for (FlurstueckComposite fs : FlurstueckComposite.Mixin.forEntity( vertrag )) {
            List<Value> createValues = createValues( vertrag, vdc, fs, errors, firstRow );
            if (!createValues.isEmpty()) {
                result.add( createValues );
                firstRow = false;
            }
        }
        return result;
    }


    private List<Value> createValues( VertragComposite vertrag, VertragsdatenBaulandComposite vdc,
            FlurstueckComposite flurstueck, List<String> errors, boolean firstRow ) {

        List<Value> result = new ArrayList<Value>();
        GemarkungComposite gemarkung = flurstueck.gemarkung().get();
        if (gemarkung == null) {
            errors.add( error( flurstueck, "Keine Gemarkung gefunden!" ) );
            return result;
        }
        GemeindeComposite gemeinde = gemarkung.gemeinde().get();
        if (gemeinde == null) {
            errors.add( error( flurstueck, "Keine Gemeinde gefunden!" ) );
            return result;
        }
        StrasseComposite strasse = flurstueck.strasse().get();
        if (strasse == null) {
            errors.add( error( flurstueck, "Keine Strasse gefunden!" ) );
            return result;
        }
        VertragsdatenErweitertComposite ew = vertrag.erweiterteVertragsdaten().get();
        Double preis = vertrag.vollpreis().get();
        if (ew != null && ew.bereinigterVollpreis().get() != null) {
            preis = ew.bereinigterVollpreis().get();
        }

        result.add( new Value( "Eingangsnummer", firstRow ? EingangsNummerFormatter.format( vertrag.eingangsNr().get() )
                : null ) );
        result.add( new Value( "Vertragsdatum", firstRow ? vertrag.vertragsDatum().get() : null ) );
        result.add( new Value( "Vertragsart", firstRow ? vertrag.vertragsArt().get() : null ) );
        result.add( new Value( "Kaufpreis", firstRow ? preis : null, 2 ) );
        result.add( new Value( "Gemeinde", gemeinde ) );
        result.add( new Value( "Gemarkung", gemarkung ) );
        result.add( new Value( "Strasse", strasse ) );
        result.add( new Value( "Flurstücksnummer", flurstueck.hauptNummer().get() + "/"
                + flurstueck.unterNummer().get() ) );
        result.add( new Value( "Gebäudeart", firstRow ? vdc.gebaeudeArtStaBu().get() : null ) );
        result.add( new Value( "anrechenbare Baulandfläche", firstRow ? vdc.flaeche1().get() : null, 1 ) );
        result.add( new Value( "ausgewertete Fläche", firstRow ? vdc.verkaufteFlaecheGesamt().get() : null, 1 ) );
        result.add( new Value( "Bodenpreis normiert", firstRow ? vdc.normierterGfzBereinigterBodenpreis().get() : null,
                2 ) );
        result.add( new Value( "Wohnfläche", firstRow ? vdc.wohnflaeche().get() : null, 2 ) );
        result.add( new Value( "Denkmal", firstRow ? vdc.denkmalschutz().get() : null ) );
        result.add( new Value( "Baujahr", firstRow ? vdc.baujahr().get() : null ) );

        return result;
    }
    

    @Override
    protected String getSortString( VertragComposite vertrag ) {
        return EingangsNummerFormatter.format( vertrag.eingangsNr().get() );
    }
}
