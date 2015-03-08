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
import org.polymap.kaps.model.data.GebaeudeArtComposite;
import org.polymap.kaps.model.data.NutzungComposite;
import org.polymap.kaps.model.data.RichtwertzoneComposite;
import org.polymap.kaps.model.data.StrasseComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.model.data.VertragsdatenBaulandComposite;
import org.polymap.kaps.model.data.VertragsdatenErweitertComposite;
import org.polymap.kaps.ui.form.EingangsNummerFormatter;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class VertragsdatenBaulandAlsBRLWExporter
        extends AbstractSingleRowExcelExporter<VertragsdatenBaulandComposite> {

    public VertragsdatenBaulandAlsBRLWExporter() {
        super( VertragsdatenBaulandComposite.class, VertragsdatenBaulandComposite.NAME, "statistik_brlw", "Verträge" );
    }


    protected List<Value> createValues( VertragsdatenBaulandComposite vdb, List<String> errors ) {

        List<Value> result = new ArrayList<Value>();
        VertragComposite vertrag = vdb.vertrag().get();
        if (vertrag == null) {
            errors.add( "Keinen Vertrag gefunden!" );
            return result;
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
            errors.add( error( vertrag, "Kein Flurstück gefunden!" ) );
            return result;
        }
        RichtwertzoneComposite zone = flurstueck.richtwertZone().get();
        if (zone == null) {
            errors.add( error( vertrag, "Keine Richtwertzone gefunden!" ) );
            return result;
        }
        VertragsdatenErweitertComposite ew = vertrag.erweiterteVertragsdaten().get();
        Double preis = vertrag.vollpreis().get();
        if (ew != null && ew.bereinigterVollpreis().get() != null) {
            preis = ew.bereinigterVollpreis().get();
        }

        result.add( new Value( "Eingangsnummer", EingangsNummerFormatter.format( vertrag.eingangsNr().get() ) ) );
        result.add( new Value( "Flst.-zaehler", flurstueck.flaechenAnteilZaehler().get(), 2 ) );
        result.add( new Value( "Flst.-nenner", flurstueck.flaechenAnteilZaehler().get(), 2 ) );

        StrasseComposite strasseComposite = flurstueck.strasse().get();
        result.add( new Value( "Strasse", strasseComposite != null ? strasseComposite.name().get() : "" ) );
        result.add( new Value( "Hausnummer", flurstueck.hausnummer().get() ) );

        NutzungComposite nutzungComposite = flurstueck.nutzung().get();
        result.add( new Value( "Nutzung", nutzungComposite != null ? nutzungComposite.name().get() : "" ) );

        result.add( new Value( "Richtwertzone", zone.schl().get() ) );
        result.add( new Value( "Richtwertzone Name", zone.name().get() ) );

        GebaeudeArtComposite gebaeudeArtComposite = flurstueck.gebaeudeArt().get();
        result.add( new Value( "Gebaeudeart", gebaeudeArtComposite != null ? gebaeudeArtComposite.name().get() : "" ) );

        result.add( new Value( "Fläche 1", vdb.flaeche1().get(), 0 ) );
        result.add( new Value( "Abgleich 1", vdb.bodenwertBereinigt1().get(), 2 ) );
        result.add( new Value( "ber. Vollpreis", preis, 2 ) );

        result.add( new Value( "zur Richtwertermittlung geeignet", vdb.zurRichtwertermittlungGeeignet().get() ) );

        return result;
    }


    @Override
    protected String getSortString( VertragsdatenBaulandComposite entity ) {
        VertragComposite vertrag = entity.vertrag().get();
        if (vertrag == null) {
            return entity.toString();
        }
        return EingangsNummerFormatter.format( vertrag.eingangsNr().get() );
    }
}
