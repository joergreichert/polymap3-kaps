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

import org.polymap.kaps.model.data.RichtwertzoneComposite;

/**
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RichtwertzoneEntityProvider
        extends KapsEntityProvider<RichtwertzoneComposite> {

    private static final Log log = LogFactory.getLog( EntitySourceProcessor.class );


    public RichtwertzoneEntityProvider( QiModule repo ) {
        super( repo, RichtwertzoneComposite.class, new NameImpl( KapsRepository.NAMESPACE,
                "Richtwertzone" ) );
    }


    @Override
    public FeatureType buildFeatureType( FeatureType schema ) {
        FeatureType type = super.buildFeatureType( schema );

        // aussortieren
        SimpleFeatureType filtered = SimpleFeatureTypeBuilder.retype( (SimpleFeatureType)type,
                new String[] { "gemeinde", "zone", "name", "gueltigAb", "stichtag", "euroQm",
                        "gfzBereich" } );
        return filtered;
    }
}
