/*
 * polymap.org Copyright 2013, Falko Bräutigam. All rights reserved.
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
package org.polymap.kaps.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.polymap.core.qi4j.QiModule;
import org.polymap.kaps.model.data.GemeindeComposite;
import org.polymap.kaps.model.data.RichtwertzoneComposite;
import org.polymap.kaps.model.data.RichtwertzoneZeitraumComposite;
import org.polymap.kaps.model.data.VertragsdatenAgrarComposite;
import org.polymap.kaps.ui.form.EingangsNummerFormatter;
import org.polymap.kaps.ui.form.RichtwertzoneProvider;
import org.polymap.rhei.data.entityfeature.EntitySourceProcessor;

public class VertragsdatenAgrarEntityProvider
        extends KapsEntityProvider<VertragsdatenAgrarComposite> {

    private static final Log log = LogFactory.getLog( EntitySourceProcessor.class );


    public VertragsdatenAgrarEntityProvider( QiModule repo ) {
        super( repo, VertragsdatenAgrarComposite.class, new NameImpl( KapsRepository.NAMESPACE,
                VertragsdatenAgrarComposite.NAME ) );
    }


    @Override
    public FeatureType buildFeatureType( FeatureType schema ) {
        FeatureType type = super.buildFeatureType( schema );

        // Spaltentyp ändern
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.init( (SimpleFeatureType)type );
        // builder.remove( "vertrag" );
        builder.add( "vertragsDatum", Date.class );
        builder.add( "eingangsNr", String.class );
        builder.add( "leereRWT", String.class );
        type = builder.buildFeatureType();

        // aussortieren für die Tabelle
        SimpleFeatureType filtered = SimpleFeatureTypeBuilder.retype( (SimpleFeatureType)type,
                new String[] { "vertragsDatum","eingangsNr","leereRWT" } );
        return filtered;
    }


    @Override
    public Feature buildFeature( VertragsdatenAgrarComposite entity, Feature feature, FeatureType schema ) {
        super.buildFeature( entity, feature, schema );

        // formatieren
        // eingangsnummer
        if (entity.vertrag().get() != null && entity.vertrag().get().eingangsNr().get() != null) {
            feature.getProperty( "eingangsNr" ).setValue(
                    EingangsNummerFormatter.format( entity.vertrag().get().eingangsNr().get().toString() ) );
        }
        if (entity.vertrag().get() != null && entity.vertrag().get().vertragsDatum().get() != null) {
            feature.getProperty( "vertragsDatum" ).setValue( entity.vertrag().get().vertragsDatum().get() );
        }
        
        if (entity.vertrag().get() != null) {
            feature.getProperty( "leereRWT" ).setValue( existsEntryWithEmptyRichtwertzone(entity) ? "ja" : "nein" );
        }

        return feature;
    }
    
    private boolean existsEntryWithEmptyRichtwertzone(VertragsdatenAgrarComposite entity) {
    	return !isInValidRWTs(entity, entity.richtwertZone1().get(), entity.richtwertZone2().get(), entity.richtwertZone3().get(), 
    			entity.richtwertZone4().get(), entity.richtwertZone5().get(), entity.richtwertZone6().get());
    }
    
    private boolean isInValidRWTs(VertragsdatenAgrarComposite vb, RichtwertzoneZeitraumComposite... aktuelle) {
        if (!vb.vertrag().get().richtwertZonenAgrar().isEmpty()) {
            Set<GemeindeComposite> gemeinden = new HashSet<GemeindeComposite>();
            for (RichtwertzoneComposite zone : vb.vertrag().get().richtwertZonenAgrar()) {
                gemeinden.add( zone.gemeinde().get() );
            }
            boolean exists = false;
            for (GemeindeComposite gemeinde : gemeinden) {
            	exists = exists | RichtwertzoneProvider.exists( gemeinde, vb.vertrag().get().vertragsDatum().get(), aktuelle);
            }
            return exists;
        }
		return false;
    }    
}
