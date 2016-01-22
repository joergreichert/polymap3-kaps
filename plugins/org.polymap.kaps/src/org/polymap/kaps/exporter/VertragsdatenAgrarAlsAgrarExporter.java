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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
        extends AbstractExcelExporter<VertragsdatenAgrarComposite> {

    public VertragsdatenAgrarAlsAgrarExporter() {
        super( VertragsdatenAgrarComposite.class, VertragsdatenAgrarComposite.NAME, "statistik_agrar", "Verträge" );
    }

    
    @Override
    protected List<List<Value>> createMultiRowValues( VertragsdatenAgrarComposite vdc, List<String> errors ) {
        List<List<Value>> result = new ArrayList<List<Value>>();

        VertragComposite vertrag = vdc.vertrag().get();
        if (vertrag == null) {
            errors.add( "Keinen Vertrag gefunden!" );
            return result;
        }
        boolean firstRow = true;
        Iterable<FlurstueckComposite> fses = FlurstueckComposite.Mixin.forEntity( vertrag );
        if (!fses.iterator().hasNext()) {
            errors.add( error( vertrag, "Kein Flurstück gefunden!" ) );
            return result;
        }
        Map<GemarkungComposite, List<FlurstueckComposite>> gemarkungFlurstuecke = new HashMap<GemarkungComposite, List<FlurstueckComposite>>(); 
        Map<GemeindeComposite, List<GemarkungComposite>> gemeindeGemarkung = new HashMap<GemeindeComposite, List<GemarkungComposite>>(); 
        for(FlurstueckComposite flurstueck : fses) {
        	GemarkungComposite gemarkung = flurstueck.gemarkung().get();
        	if (gemarkung == null) {
        		errors.add( error( vertrag, String.format("Keine Gemarkung für Flurstück %s gefunden!", flurstueck.name().get()) ) );
        		return result;
        	} else {
        		if(!gemarkungFlurstuecke.containsKey(gemarkung)) {
        			gemarkungFlurstuecke.put(gemarkung, new ArrayList<FlurstueckComposite>());
        		}
        		gemarkungFlurstuecke.get(gemarkung).add(flurstueck);
                GemeindeComposite gemeinde = gemarkung.gemeinde().get();
                if (gemeinde == null) {
                    errors.add( error( vertrag, "Keine Gemeinde gefunden!" ) );
                    return result;
                } else {
                	if(!gemeindeGemarkung.containsKey(gemeinde)) {
                		gemeindeGemarkung.put(gemeinde, new ArrayList<GemarkungComposite>());
                	}
            		List<GemarkungComposite> gemarkungen = gemeindeGemarkung.get(gemeinde);
            		if(!gemarkungen.contains(gemarkung)) {
            			gemarkungen.add(gemarkung);
            		}
                }
        	}
        }
        boolean firstFlurstueckOfGemarkung = true;
        for(Entry<GemeindeComposite, List<GemarkungComposite>> gemeindeGemarkungEntry : gemeindeGemarkung.entrySet()) {
        	for(GemarkungComposite gemarkung : gemeindeGemarkungEntry.getValue()) {
        		firstFlurstueckOfGemarkung = true;
            	for(FlurstueckComposite fs : gemarkungFlurstuecke.get(gemarkung)) {
                    NutzungComposite nutzung = fs.nutzung().get();
                    if (nutzung != null && nutzung.isAgrar().get() != null && nutzung.isAgrar().get() == Boolean.TRUE) {
                        Double flaeche = fs.verkaufteFlaeche().get();
                        if (flaeche != null) {
                            List<Value> createValues = createValues( vertrag, vdc, fs, gemarkung, gemeindeGemarkungEntry.getKey(), errors, firstRow, firstFlurstueckOfGemarkung );
                            if (!createValues.isEmpty()) {
                            	result.add( createValues );
                            	firstRow = false;
                            	firstFlurstueckOfGemarkung = false;
                            }
                        }
                    }
            	}
        	}
        }
        return result;
    }
    

    protected List<Value> createValues( VertragComposite vertrag, VertragsdatenAgrarComposite vdb, FlurstueckComposite fs, GemarkungComposite gemarkung, GemeindeComposite gemeinde, 
    		List<String> errors, boolean firstRow, boolean firstFlurstueckOfGemarkung ) {
        List<Value> result = new ArrayList<Value>();
        RichtwertzoneComposite zone = fs.richtwertZone().get();
        if (zone == null) {
            errors.add( error( vertrag, "Keine Richtwertzone gefunden!" ) );
            return result;
        }
        if(vdb.kaufpreisAnteilBodenwert().get() == null) {
            errors.add( error( vertrag, "Kaufpreis Anteil Bodenwert (KPANTGRU) ist nicht gesetzt." ) );
            return result;
        }
        if(vdb.bodenwertGesamt().get() == null) {
            errors.add( error( vertrag, "Bodenwert Gesamt (GESBOWERT) ist nicht gesetzt." ) );
            return result;
        }
        double faktorBereinigterKaufpreis = vdb.kaufpreisAnteilBodenwert().get() / vdb.bodenwertGesamt().get(); 
        
        result.add( new Value( "Eingangsnummer", EingangsNummerFormatter.format( vertrag.eingangsNr().get() ) ) );
        result.add( new Value( "Vertragsdatum", vertrag.vertragsDatum().get() ) );
        result.add( new Value( "Gemeinde", gemeinde.name().get() ) );
        result.add( new Value( "Gemarkung", gemarkung.name().get() ) );
        result.add( new Value( "Flurstücksnummer", fs.hauptNummer().get() ) );
        result.add( new Value( "Unternummer", fs.unterNummer().get() ) );
        result.add( new Value( "Vertragsfläche", fs.flaeche().get(), 0 ) );
        result.add( new Value( "Bebaut", vdb.istBebaut().get() ) );

        
        boolean matches1 = firstFlurstueckOfGemarkung && vdb.richtwertZone1().get() != null && vdb.richtwertZone1().get().name().get().equals(zone.name().get());
    	result.add( new Value( "Bodennutzung 1", matches1 && vdb.bodennutzung1().get() != null ? vdb.bodennutzung1().get().name()
    			.get() : "" ) );
    	result.add( new Value( "Fläche 1", matches1 ? vdb.ackerzahl1().get() : null ) );
    	result.add( new Value( "Flächeanteil 1", matches1 ? vdb.flaechenAnteil1().get() : null, 2 ) );
    	result.add( new Value( "Abgleich 1", matches1 ? vdb.abgleichAufKaufpreis1().get() : null, 2 ) );
    	result.add( new Value( "Preis 1", matches1 ? vdb.bodenwert1().get() * faktorBereinigterKaufpreis : null, 2 ) );
        
        boolean matches2 = firstFlurstueckOfGemarkung && vdb.richtwertZone2().get() != null && vdb.richtwertZone2().get().name().get().equals(zone.name().get());
        result.add( new Value( "Bodennutzung 2", matches2 && vdb.bodennutzung2().get() != null ? vdb.bodennutzung2().get().name()
                .get() : "" ) );
        result.add( new Value( "Fläche 2", matches2 ? vdb.ackerzahl2().get() : null ) );
        result.add( new Value( "Flächeanteil 2", matches2 ? vdb.flaechenAnteil2().get() : null, 2 ) );
        result.add( new Value( "Abgleich 2", matches2 ? vdb.abgleichAufKaufpreis2().get() : null, 2 ) );
    	result.add( new Value( "Preis 2", matches2 ? vdb.bodenwert2().get() * faktorBereinigterKaufpreis : null, 2 ) );

        boolean matches3 = firstFlurstueckOfGemarkung && vdb.richtwertZone3().get() != null && vdb.richtwertZone3().get().name().get().equals(zone.name().get());
        result.add( new Value( "Bodennutzung 3", matches3 && vdb.bodennutzung3().get() != null ? vdb.bodennutzung3().get().name()
                .get() : "" ) );
        result.add( new Value( "Fläche 3", matches3 ? vdb.ackerzahl3().get() : null ) );
        result.add( new Value( "Flächeanteil 3", matches3 ? vdb.flaechenAnteil3().get() : null, 2 ) );
        result.add( new Value( "Abgleich 3", matches3 ? vdb.abgleichAufKaufpreis3().get() : null, 2 ) );
    	result.add( new Value( "Preis 3", matches3 ? vdb.bodenwert3().get() * faktorBereinigterKaufpreis : null, 2 ) );
        
        boolean matches4 = firstFlurstueckOfGemarkung && vdb.richtwertZone4().get() != null && vdb.richtwertZone4().get().name().get().equals(zone.name().get());
        result.add( new Value( "Bodennutzung 4", matches4 && vdb.bodennutzung4().get() != null ? vdb.bodennutzung4().get().name()
                .get() : "" ) );
        result.add( new Value( "Fläche 4", matches4 ? vdb.ackerzahl4().get() : null ) );
        result.add( new Value( "Flächeanteil 4", matches4 ? vdb.flaechenAnteil4().get() : null, 2 ) );
        result.add( new Value( "Abgleich 4", matches4 ? vdb.abgleichAufKaufpreis4().get() : null, 2 ) );
    	result.add( new Value( "Preis 4", matches4 ? vdb.bodenwert4().get() * faktorBereinigterKaufpreis : null, 2 ) );

        boolean matches5 = firstFlurstueckOfGemarkung && vdb.richtwertZone5().get() != null && vdb.richtwertZone5().get().name().get().equals(zone.name().get());
        result.add( new Value( "Bodennutzung 5", matches5 && vdb.bodennutzung5().get() != null ? vdb.bodennutzung5().get().name()
                .get() : "" ) );
        result.add( new Value( "Fläche 5", matches5 ? vdb.ackerzahl5().get() : null ) );
        result.add( new Value( "Flächeanteil 5", matches5 ? vdb.flaechenAnteil5().get() : null, 2 ) );
        result.add( new Value( "Abgleich 5", matches5 ? vdb.abgleichAufKaufpreis5().get() : null, 2 ) );
    	result.add( new Value( "Preis 5", matches5 ? vdb.bodenwert5().get() * faktorBereinigterKaufpreis : null, 2 ) );

        boolean matches6 = firstFlurstueckOfGemarkung && vdb.richtwertZone6().get() != null && vdb.richtwertZone6().get().name().get().equals(zone.name().get());
        result.add( new Value( "Bodennutzung 6", matches6 && vdb.bodennutzung6().get() != null ? vdb.bodennutzung6().get().name()
                .get() : "" ) );
        result.add( new Value( "Fläche 6", matches6 ? vdb.ackerzahl6().get() : null ) );
        result.add( new Value( "Flächeanteil 6", matches6 ? vdb.flaechenAnteil6().get() : null, 2 ) );
        result.add( new Value( "Abgleich 6", matches6 ? vdb.abgleichAufKaufpreis6().get() : null, 2 ) );
    	result.add( new Value( "Preis 6", matches6 ? vdb.bodenwert6().get() * faktorBereinigterKaufpreis : null, 2 ) );

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
