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

import java.util.Collection;

import java.text.NumberFormat;

import javax.swing.text.NumberFormatter;

import org.geotools.data.Query;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model.Composite;
import org.polymap.core.model.EntityType;
import org.polymap.core.model.EntityType.Association;
import org.polymap.core.model.EntityType.Property;
import org.polymap.core.qi4j.QiModule;
import org.polymap.core.qi4j.QiModule.EntityCreator;

import org.polymap.rhei.data.entityfeature.EntityProvider2;
import org.polymap.rhei.data.entityfeature.EntityProvider3;
import org.polymap.rhei.data.entityfeature.EntitySourceProcessor;

import org.polymap.kaps.form.EingangsNummerFormatter;

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

    private static final Log log = LogFactory.getLog( EntitySourceProcessor.class );


    public KaufvertragEntityProvider( QiModule repo, FidsQueryProvider queryProvider ) {
        super( repo, KaufvertragComposite.class, new NameImpl( KapsRepository.NAMESPACE,
                "Kaufvertrag" ), queryProvider );
    }


    public KaufvertragComposite newEntity( final EntityCreator<KaufvertragComposite> creator )
            throws Exception {
        return ((KapsRepository)repo).newKaufvertrag( creator );
    }


    @Override
    public FeatureType buildFeatureType( FeatureType schema ) {
        // filter properties
        // SimpleFeatureType filtered = SimpleFeatureTypeBuilder.retype(
        // (SimpleFeatureType)schema,
        // new String[] {"eingangsDatum", "eingangsNr"} );

        // VertragsArt
        // ACHTUNG! : der name darf kein existierender Name einer
        // Property oder Assoziation der Entity sein!
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.init( (SimpleFeatureType)schema );

        // assoziationen ergänzen
        // alle mit einem Type der ein Property Name hat
        EntityType entityType = getEntityType();
        Collection<EntityType.Property> p = entityType.getProperties();
        for (EntityType.Property prop : p) {
            Class propType = prop.getType();
            if (prop instanceof Association) {
                log.debug( "    adding association: " + prop.getName() + " / " + propType );
                Association association = (Association)prop;
                EntityType associationType = repo.entityType( association.getType() );
                if (associationType.getProperty( "name" ) != null) {
                    builder.add( association.getName(), String.class );
                }
            }
        }

        builder.remove( "eingangsNr" );
        builder.add( "eingangsNr", String.class );

        // aussortieren für die Tabelle
        SimpleFeatureType filtered = SimpleFeatureTypeBuilder.retype( builder.buildFeatureType(),
                new String[] { "eingangsNr", "vertragsDatum", "vertragsArt", "eingangsDatum", "vollPreis" } );
        return filtered;
    }


    @Override
    public Feature buildFeature( KaufvertragComposite entity, Feature feature, FeatureType schema ) {
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
                    log.debug( "    adding association: " + prop.getName() + " / " + propType );
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
                                    associatedCompositeName.append( schl ).append( "  -  " );
                                }
                                if (name != null) {
                                    associatedCompositeName.append( name );
                                }
                            }
                            property.setValue( associatedCompositeName );
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            throw new IllegalStateException( e );
        }
        // formatieren
        // eingangsnummer
        if (entity.eingangsNr().get() != null) {
            feature.getProperty( "eingangsNr" ).setValue(
                    EingangsNummerFormatter.format( entity.eingangsNr().get().toString() ) );
        }
        return feature;
    }


    @Override
    public boolean modifyFeature( KaufvertragComposite entity, String propName, Object value )
            throws Exception {
        // apply default method
        return false;
    }


    @Override
    public Query transformQuery( Query query ) {
        return query;
    }

}
