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

import org.qi4j.api.query.QueryExpressions;

import org.polymap.core.qi4j.QiModule;

import org.polymap.rhei.data.entityfeature.EntitySourceProcessor;

import org.polymap.kaps.model.data.WohnungComposite;
import org.polymap.kaps.ui.form.EingangsNummerFormatter;

public class WohnungEntityProvider
        extends KapsEntityProvider<WohnungComposite> {

    private static final Log log = LogFactory.getLog( EntitySourceProcessor.class );


    public WohnungEntityProvider( QiModule repo ) {
        super( repo, WohnungComposite.class, new NameImpl( KapsRepository.NAMESPACE, WohnungComposite.NAME ) );
    }


    @Override
    public FeatureType buildFeatureType( FeatureType schema ) {
        FeatureType type = super.buildFeatureType( schema );

        // Spaltentyp ändern
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.init( (SimpleFeatureType)type );
        builder.add( "eingangsNr", String.class );
        type = builder.buildFeatureType();

        WohnungComposite template = QueryExpressions.templateFor( WohnungComposite.class );
        // aussortieren für die Tabelle
        SimpleFeatureType filtered = SimpleFeatureTypeBuilder.retype( (SimpleFeatureType)type, new String[] {
                "eingangsNr", template.objektNummer().qualifiedName().name(),
                template.gebaeudeNummer().qualifiedName().name(), template.wohnungsNummer().qualifiedName().name(),
                template.wohnungsFortfuehrung().qualifiedName().name() } );
        return filtered;
    }


    @Override
    public Feature buildFeature( WohnungComposite entity, Feature feature, FeatureType schema ) {
        super.buildFeature( entity, feature, schema );

        // formatieren
        // eingangsnummer
        if (entity.vertrag().get() != null && entity.vertrag().get().eingangsNr().get() != null) {
            feature.getProperty( "eingangsNr" ).setValue(
                    EingangsNummerFormatter.format( entity.vertrag().get().eingangsNr().get().toString() ) );
        }
        return feature;
    }
}
