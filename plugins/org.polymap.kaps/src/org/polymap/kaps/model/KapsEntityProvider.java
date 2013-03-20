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
package org.polymap.kaps.model;

import java.util.Collection;

import org.geotools.data.Query;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.polymap.core.data.util.Geometries;
import org.polymap.core.model.Composite;
import org.polymap.core.model.Entity;
import org.polymap.core.model.EntityType;
import org.polymap.core.model.EntityType.Association;
import org.polymap.core.model.EntityType.Property;
import org.polymap.core.qi4j.QiModule;

import org.polymap.rhei.data.entityfeature.DefaultEntityProvider;
import org.polymap.rhei.data.entityfeature.EntityProvider;
import org.polymap.rhei.data.entityfeature.EntityProvider3;

import org.polymap.kaps.form.EingangsNummerFormatter;

/**
 * Basisklasse für alle KAPS {@link EntityProvider}. Die Klasse liefert einfache
 * Implementationen für Methoden. Die Geometrie muss immer im Property "geom" liegen.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
abstract class KapsEntityProvider<T extends Entity>
        extends DefaultEntityProvider<T>
        implements EntityProvider<T>, EntityProvider3<T> {

    public KapsEntityProvider( QiModule repo, Class<T> entityClass, Name entityName,
            FidsQueryProvider queryProvider ) {
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
        return new ReferencedEnvelope( 4000000, 5000000, 5000000, 6000000,
                getCoordinateReferenceSystem( null ) );
    }


    @Override
    public FeatureType buildFeatureType( FeatureType schema ) {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.init( (SimpleFeatureType)schema );

        // assoziationen ergänzen
        // alle mit einem Type der ein Property Name hat
        EntityType entityType = getEntityType();
        Collection<EntityType.Property> p = entityType.getProperties();
        for (EntityType.Property prop : p) {
            Class propType = prop.getType();
            if (prop instanceof Association) {
                Association association = (Association)prop;
                EntityType associationType = repo.entityType( association.getType() );
                if (associationType.getProperty( "name" ) != null) {
                    builder.add( association.getName(), String.class );
                }
            }
        }

        return builder.buildFeatureType();
    }


    @Override
    public Feature buildFeature( T entity, Feature feature, FeatureType schema ) {
        // VertragsArtComposite vertragsArt = entity.vertragsArt().get();
        // feature.getProperty("Vertragsart").setValue(
        // vertragsArt != null ? vertragsArt.name().get() : "");
        // assoziationen ergänzen, alle mit Name Property
        try {
            EntityType entityType = getEntityType();
            Collection<EntityType.Property> p = entityType.getProperties();
            for (EntityType.Property prop : p) {
                Class propType = prop.getType();
                if (prop instanceof Association) {
                    Association association = (Association)prop;

                    org.opengis.feature.Property property = feature.getProperty( association
                            .getName() );
                    if (property != null) {
                        EntityType associationType = repo.entityType( association.getType() );
                        Property nameProperty = associationType.getProperty( "name" );
                        Property schlProperty = associationType.getProperty( "schl" );
                        if (nameProperty != null) {
                            Object associationValue = association.getValue( entity );
                            StringBuffer associatedCompositeName = new StringBuffer( "" );
                            if (associationValue != null) {
                                String name = (String)nameProperty
                                        .getValue( (Composite)associationValue );
                                String schl = (String)schlProperty
                                        .getValue( (Composite)associationValue );
                                if (schl != null) {
                                    associatedCompositeName.append( schl );
                                }
                                if (name != null && !name.isEmpty()) {
                                    if (associatedCompositeName.length() > 0) {
                                        associatedCompositeName.append( "  -  " );
                                    }
                                    associatedCompositeName.append( name );
                                }
                            }
                            property.setValue( associatedCompositeName.toString() );
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            throw new IllegalStateException( e );
        }
        return feature;
    }


    @Override
    public boolean modifyFeature( T entity, String propName, Object value )
            throws Exception {
        // apply default method
        return false;
    }


    @Override
    public Query transformQuery( Query query ) {
        return query;
    }
}
