/* 
 * polymap.org
 * Copyright 2013, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.kaps.model;

import org.geotools.data.Query;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;

import org.polymap.core.model.Entity;
import org.polymap.core.qi4j.QiModule;
import org.polymap.core.qi4j.QiModule.EntityCreator;

import org.polymap.rhei.data.entityfeature.EntityProvider2;
import org.polymap.rhei.data.entityfeature.EntityProvider3;

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
        extends KapsEntityProvider<KaufvertragComposite>
        implements EntityProvider3<KaufvertragComposite> {

    public KaufvertragEntityProvider( QiModule repo, FidsQueryProvider queryProvider ) {
        super( repo, KaufvertragComposite.class, new NameImpl( KapsRepository.NAMESPACE, "Kaufvertrag2" ), queryProvider );
    }

    public KaufvertragComposite newEntity( final EntityCreator<KaufvertragComposite> creator )
            throws Exception {
        return ((KapsRepository)repo).newKaufvertrag( creator );
    }

    @Override
    public FeatureType buildFeatureType( FeatureType schema ) {
        // filter properties
        return SimpleFeatureTypeBuilder.retype( (SimpleFeatureType)schema, 
                new String[] {"eingangsDatum", "eingangsNr"} );
    }

    @Override
    public Feature buildFeature( Entity entity, Feature feature, FeatureType schema ) {
        return feature;
    }

    @Override
    public boolean modifyFeature( Entity entity, String propName, Object value ) throws Exception {
        // apply default method
        return false;
    }

    @Override
    public Query transformQuery( Query query ) {
        return query;
    }
    
}
