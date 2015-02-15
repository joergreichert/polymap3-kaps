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

import org.polymap.kaps.model.data.BodennutzungComposite;
import org.polymap.kaps.model.data.BodenwertAufteilungTextComposite;
import org.polymap.kaps.model.data.FlurstueckComposite;
import org.polymap.kaps.model.data.GemarkungComposite;
import org.polymap.kaps.model.data.GemeindeComposite;
import org.polymap.kaps.model.data.NutzungComposite;
import org.polymap.kaps.model.data.RichtwertzoneComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.model.data.VertragsdatenBaulandComposite;
import org.polymap.kaps.model.data.VertragsdatenErweitertComposite;
import org.polymap.kaps.ui.form.EingangsNummerFormatter;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class VertragsdatenBaulandAlsBRLExporter
        extends AbstractSingleRowExcelExporter<VertragsdatenBaulandComposite> {

    public VertragsdatenBaulandAlsBRLExporter() {
        super( VertragsdatenBaulandComposite.class, VertragsdatenBaulandComposite.NAME, "statistik_brl", "Verträge" );
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
        String boden = null;
        BodennutzungComposite bn = vdb.bodennutzung().get();
        if (bn != null) {
            boden = bn.name().get();
        }
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
        result.add( new Value( "Bodennutzung", boden ) );
        result.add( new Value( "Richtwertzone", zone.schl().get() ) );
        result.add( new Value( "Fläche 1", vdb.flaeche1().get(), 0 ) );
        result.add( new Value( "Abgleich 1", vdb.bodenwertBereinigt1().get(), 2 ) );
        result.add( new Value( "Fläche 2", vdb.flaeche2().get(), 0 ) );
        result.add( new Value( "Abgleich 2", vdb.bodenwertBereinigt2().get(), 2 ) );
        BodenwertAufteilungTextComposite auf = vdb.bodenwertAufteilung1().get();
        result.add( new Value( "Art 3", auf != null ? auf.name().get() : "" ) );
        result.add( new Value( "Fläche 3", vdb.flaeche3().get(), 0 ) );
        result.add( new Value( "Abgleich 3", vdb.bodenwertBereinigt3().get(), 2 ) );
        auf = vdb.bodenwertAufteilung2().get();
        result.add( new Value( "Art 4", auf != null ? auf.name().get() : "" ) );
        result.add( new Value( "Fläche 4", vdb.flaeche4().get(), 0 ) );
        result.add( new Value( "Abgleich 4", vdb.bodenwertBereinigt4().get(), 2 ) );
        auf = vdb.bodenwertAufteilung3().get();
        result.add( new Value( "Art 5", auf != null ? auf.name().get() : "" ) );
        result.add( new Value( "Fläche 5", vdb.flaeche5().get(), 0 ) );
        result.add( new Value( "Abgleich 5", vdb.bodenwertBereinigt5().get(), 2 ) );
        result.add( new Value( "Art 6", vdb.bodenwertAufteilungText6().get() ) );
        result.add( new Value( "Fläche 6", vdb.flaeche6().get(), 0 ) );
        result.add( new Value( "Abgleich 6", vdb.bodenwertBereinigt6().get(), 2 ) );

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
