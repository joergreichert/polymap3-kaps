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
    extends AbstractExcelExporter<VertragsdatenBaulandComposite> {

	
    public VertragsdatenBaulandAlsBRLWExporter() {
        super( VertragsdatenBaulandComposite.class, VertragsdatenBaulandComposite.NAME, "statistik_brlw", "Verträge" );
    }


	@Override
	protected List<List<Value>> createMultiRowValues( VertragsdatenBaulandComposite vdc, List<String> errors ) {
	    List<List<Value>> result = new ArrayList<List<Value>>();
	
	    VertragComposite vertrag = vdc.vertrag().get();
	    if (vertrag == null) {
	        errors.add( "Keinen Vertrag gefunden!" );
	        return result;
	    }
	    Iterable<FlurstueckComposite> fses = FlurstueckComposite.Mixin.forEntity( vertrag );
	    if (!fses.iterator().hasNext()) {
	        errors.add( error( vertrag, "Kein Flurstück gefunden!" ) );
	        return result;
	    }
	    boolean firstRow = true;
	    for(FlurstueckComposite flurstueck : fses) {
	        List<Value> createValues = createValues( vdc, flurstueck, errors, firstRow );
	        firstRow = false;
	        if (!createValues.isEmpty()) {
	        	result.add( createValues );
	        }
	    }
	    return result;
	}	


    protected List<Value> createValues( VertragsdatenBaulandComposite vdb, FlurstueckComposite flurstueck, List<String> errors, boolean firstRow ) {

        List<Value> result = new ArrayList<Value>();
        VertragComposite vertrag = vdb.vertrag().get();
        if (vertrag == null) {
            errors.add( "Keinen Vertrag gefunden!" );
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

        result.add( new Value( "Eingangsnummer", firstRow ? EingangsNummerFormatter.format( vertrag.eingangsNr().get() ) : "") );
        result.add( new Value( "Flst.-zaehler", flurstueck.hauptNummer().get() ) );
        result.add( new Value( "Flst.-nenner", flurstueck.unterNummer().get() ) );

        StrasseComposite strasseComposite = flurstueck.strasse().get();
        result.add( new Value( "Strasse", strasseComposite != null ? strasseComposite.name().get() : "" ) );
        result.add( new Value( "Hausnummer", flurstueck.hausnummer().get() ) );

        NutzungComposite nutzungComposite = flurstueck.nutzung().get();
        result.add( new Value( "Nutzung", nutzungComposite != null ? nutzungComposite.name().get() : "" ) );

        result.add( new Value( "Richtwertzone", zone.schl().get() ) );
        result.add( new Value( "Richtwertzone Name", zone.name().get() ) );

        GebaeudeArtComposite gebaeudeArtComposite = flurstueck.gebaeudeArt().get();
        result.add( new Value( "Gebaeudeart", gebaeudeArtComposite != null ? gebaeudeArtComposite.name().get() : "" ) );

        result.add( new Value( "Fläche 1", firstRow ? vdb.flaeche1().get() : null, 0 ) );
        result.add( new Value( "Abgleich 1", firstRow ? vdb.normierterGfzBereinigterBodenpreis().get() : null, 2 ) );
        result.add( new Value( "ber. Vollpreis", firstRow ? preis : null, 2 ) );

        result.add( new Value( "zur Richtwertermittlung geeignet", firstRow ? vdb.zurRichtwertermittlungGeeignet().get() : null ) );

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
