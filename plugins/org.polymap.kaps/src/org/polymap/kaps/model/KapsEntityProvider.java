/* 
 * polymap.org
 * Copyright 2013 Polymap GmbH. All rights reserved.
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

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.type.Name;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.polymap.core.data.util.Geometries;
import org.polymap.core.model.Entity;
import org.polymap.core.qi4j.QiModule;

import org.polymap.rhei.data.entityfeature.DefaultEntityProvider;
import org.polymap.rhei.data.entityfeature.EntityProvider;

/**
 * Basisklasse für alle KAPS {@link EntityProvider}. Die Klasse liefert einfache
 * Implementationen für Methoden. Die Geometrie muss immer im Property "geom" liegen.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
abstract class KapsEntityProvider<T extends Entity>
        extends DefaultEntityProvider<T>
        implements EntityProvider<T> {

    public KapsEntityProvider( QiModule repo, Class<T> entityClass, Name entityName, FidsQueryProvider queryProvider ) {
        super( repo, entityClass, entityName, queryProvider );
    }

    public CoordinateReferenceSystem getCoordinateReferenceSystem( String propName ) {
        try {
            return Geometries.crs( "EPSG:31468" );
        } 
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }

    public String getDefaultGeometry() {
        return "geom";
    }

    public ReferencedEnvelope getBounds() {
        return new ReferencedEnvelope( 4000000, 5000000, 5000000, 6000000, getCoordinateReferenceSystem( null ) );
    }
    
}
