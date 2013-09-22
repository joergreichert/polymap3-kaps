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
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.qi4j.QiModule;

import org.polymap.rhei.data.entityfeature.EntitySourceProcessor;

import org.polymap.kaps.model.data.FlurstueckComposite;

public class FlurstueckEntityProvider
        extends KapsEntityProvider<FlurstueckComposite> {

    private static final Log log = LogFactory.getLog( EntitySourceProcessor.class );


    public FlurstueckEntityProvider( QiModule repo ) {
        super( repo, FlurstueckComposite.class, new NameImpl( KapsRepository.NAMESPACE, FlurstueckComposite.NAME ) );
    }


    @Override
    public FeatureType buildFeatureType( FeatureType schema ) {
        FeatureType type = super.buildFeatureType( schema );

        // aussortieren
        SimpleFeatureType filtered = SimpleFeatureTypeBuilder.retype( (SimpleFeatureType)type, new String[] {
                "vertragsNummer", "gemarkung", "hauptNummer", "unterNummer", "flaeche", "nutzung", "richtwertZone" } );
        return filtered;
    }
    //
    // @Override
    // public Feature buildFeature( FlurstueckComposite entity, Feature feature,
    // FeatureType schema ) {
    // Feature ret = super.buildFeature( entity, feature, schema );
    // // FIXME
    // ret.getProperty( "geom" ).setValue( entity.geom().get() );
    // return ret;
    // }
}
