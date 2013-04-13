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

import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.qi4j.QiModule;
import org.polymap.core.qi4j.QiModule.EntityCreator;

import org.polymap.rhei.data.entityfeature.EntityProvider2;
import org.polymap.rhei.data.entityfeature.EntitySourceProcessor;

import org.polymap.kaps.model.data.KaufvertragComposite;
import org.polymap.kaps.ui.form.EingangsNummerFormatter;

/**
 * Minimale Implementation für einen EntityProvider. Alle einfachen Properties der
 * Entity werden 1:1 umgesetzt. Properties, die Collections oder komplexe Typen
 * enthalten, werden ausgelassen.
 * <p/>
 * Bei Bedarf kann der FeatureType und die Features noch nachbearbeitet werden. Das
 * ist anders als beim {@link EntityProvider2}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class KaufvertragEntityProvider
        extends KapsEntityProvider<KaufvertragComposite> {

    private static final Log log = LogFactory.getLog( EntitySourceProcessor.class );


    public KaufvertragEntityProvider( QiModule repo ) {
        super( repo, KaufvertragComposite.class, new NameImpl( KapsRepository.NAMESPACE,
                "Kaufvertrag" ) );
    }


    public KaufvertragComposite newEntity( final EntityCreator<KaufvertragComposite> creator )
            throws Exception {
        return ((KapsRepository)repo).newKaufvertrag( creator );
    }


    @Override
    public FeatureType buildFeatureType( FeatureType schema ) {
        FeatureType type = super.buildFeatureType( schema );

        // Spaltentyp ändern
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.init( (SimpleFeatureType)type );
        builder.remove( "eingangsNr" );
        builder.add( "eingangsNr", String.class );
        type = builder.buildFeatureType();

        // aussortieren für die Tabelle
        SimpleFeatureType filtered = SimpleFeatureTypeBuilder.retype( (SimpleFeatureType)type,
                new String[] { "eingangsNr", "vertragsDatum", "vertragsArt", "eingangsDatum",
                        "kaufpreis" } );
        return filtered;
    }


    @Override
    public Feature buildFeature( KaufvertragComposite entity, Feature feature, FeatureType schema ) {
        super.buildFeature( entity, feature, schema );

        // formatieren
        // eingangsnummer
        if (entity.eingangsNr().get() != null) {
            feature.getProperty( "eingangsNr" ).setValue(
                    EingangsNummerFormatter.format( entity.eingangsNr().get().toString() ) );
        }
        return feature;
    }
}
