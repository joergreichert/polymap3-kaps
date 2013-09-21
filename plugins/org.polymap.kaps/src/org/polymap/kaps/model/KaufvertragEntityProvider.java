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

import org.polymap.kaps.model.data.VertragComposite;
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
        extends KapsEntityProvider<VertragComposite> {

    private static final Log log = LogFactory.getLog( EntitySourceProcessor.class );


    public KaufvertragEntityProvider( QiModule repo ) {
        super( repo, VertragComposite.class, new NameImpl( KapsRepository.NAMESPACE, VertragComposite.NAME ) );
    }


    public VertragComposite newEntity( final EntityCreator<VertragComposite> creator )
            throws Exception {
        return ((KapsRepository)repo).newEntity( VertragComposite.class, null, new EntityCreator<VertragComposite>() {

            public void create( VertragComposite prototype )
                    throws Exception {
                prototype.eingangsDatum().set( new Date() );
                // prototype.kaufpreis().set( new Double(0.0d) );
                prototype.kaufpreisAnteilZaehler().set( new Double( 1.0 ) );
                prototype.kaufpreisAnteilNenner().set( new Double( 1.0 ) );
                prototype.fuerGewosGeeignet().set( Boolean.TRUE );
                prototype.fuerAuswertungGeeignet().set( Boolean.TRUE );
                // VertragsdatenErweitertComposite vdec = newEntity(
                // VertragsdatenErweitertComposite.class, null );
                // prototype.erweiterteVertragsdaten().set( vdec );
                // vdec.basispreis().set( prototype.kaufpreis().get() );
                // eingangsnummer erst beim Speichern setzen!

                if (creator != null) {
                    creator.create( prototype );
                }
            }
        } );
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
        SimpleFeatureType filtered = SimpleFeatureTypeBuilder.retype( (SimpleFeatureType)type, new String[] {
                "eingangsNr", "vertragsDatum", "vertragsArt", "eingangsDatum", "kaufpreis" } );
        return filtered;
    }


    @Override
    public Feature buildFeature( VertragComposite entity, Feature feature, FeatureType schema ) {
        super.buildFeature( entity, feature, schema );

        // formatieren
        // eingangsnummer
        if (entity.eingangsNr().get() != null) {
            feature.getProperty( "eingangsNr" ).setValue(
                    EingangsNummerFormatter.format( entity.eingangsNr().get().toString() ) );
        }
        return feature;
    }


    @Override
    public boolean modifyFeature( VertragComposite entity, String propName, Object value )
            throws Exception {
        // set defaults
        if (value == null) {
            if (entity.kaufpreis().qualifiedName().name().equals( propName )) {
                entity.kaufpreis().set( new Double( 0.0 ) );
                return true;
            }
            if (entity.vertragsDatum().qualifiedName().name().equals( propName )) {
                entity.vertragsDatum().set( new Date() );
                return true;
            }
            if (entity.eingangsDatum().qualifiedName().name().equals( propName )) {
                entity.eingangsDatum().set( new Date() );
                return true;
            }
        }
        return super.modifyFeature( entity, propName, value );
    }
}
