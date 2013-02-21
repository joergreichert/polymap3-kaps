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

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.DefaultQuery;
import org.geotools.data.Query;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.visitor.DuplicatingFilterVisitor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.PropertyName;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.polymap.core.model.Entity;
import org.polymap.core.model.EntityType;
import org.polymap.core.qi4j.QiModule;
import org.polymap.core.qi4j.QiModule.EntityCreator;
import org.polymap.rhei.data.entityfeature.DefaultEntityProvider;
import org.polymap.rhei.data.entityfeature.EntityProvider2;

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class KaufvertragsEntityProvider
        extends DefaultEntityProvider<KaufvertragComposite>
        implements EntityProvider2<KaufvertragComposite> {

    private static Log log = LogFactory.getLog( KaufvertragsEntityProvider.class );

    /** 
     * The properties (name/type) of the feature type provided. 
     */
    private enum PROP {
        eingangsDatum( Date.class ), 
//        SBK( String.class, "SBK/TK25/UNr." ), 
        eingangsNr( String.class ), 
//        Beschreibung( String.class ), 
        vertragArtNr( String.class );
//        Geprueft( Boolean.class, "Gepr�ft" ), 
//        Wert( String.class ), 
//        Archiv( Integer.class );
        
        private Class       type;
        
        private String      name = name();
        
        PROP( Class type ) {
            this.type = type;
        }
        PROP( Class type, String name ) {
            this.type = type;
            this.name = name;
        }
        public Class type() {
            return type;
        }
        public <T> T cast( Object value ) {
            return (T)type.cast( value );
        }
        public String toString() {
            return name;
        }
    }

    
    public KaufvertragsEntityProvider( QiModule repo, FidsQueryProvider queryProvider ) {
        super( repo, KaufvertragComposite.class, new NameImpl( KapsRepository.NAMESPACE, "Kaufvertrag" ), queryProvider );
    }


    public KaufvertragComposite newEntity( final EntityCreator<KaufvertragComposite> creator )
    throws Exception {
        return ((KapsRepository)repo).newKaufvertrag( creator );
    }


    public FeatureType buildFeatureType() {
        EntityType entityType = getEntityType();

        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName( getEntityName() );
//
//        CoordinateReferenceSystem crs = getCoordinateReferenceSystem( getDefaultGeometry() );
//        builder.add( getDefaultGeometry(), MultiPolygon.class, crs );
//        builder.setDefaultGeometry( getDefaultGeometry() );
        // WORKAROUND use kaufvertrag.eingangsNr.qualifiedName + kaufvertrag.eingangsNr.type
        for (PROP prop : PROP.values()) {
            builder.add( prop.toString(), prop.type() );            
        }
        return builder.buildFeatureType();
    }


    // ben�tigt f�r Suche in Tabellen
    public Query transformQuery( Query query ) {
        Filter filter = query.getFilter();
//        if (filter == null) {
//            log.warn( "Filter is NULL!" );
//            filter = Filter.INCLUDE;
//        }
        Filter dublicate = filter == null ? null : (Filter)filter.accept( new DuplicatingFilterVisitor() {
            
            public Object visit( PropertyName input, Object data ) {
//                if (input.getPropertyName().equals( PROP.Wert.toString() )) {
//                    return getFactory( data ).property( "wert" );
//                }
//                else if (input.getPropertyName().equals( PROP.Biotopnummer.toString() )) {
//                    return getFactory( data ).property( "objnr" );
//                }
//                else if (input.getPropertyName().equals( PROP.Beschreibung.toString() )) {
//                    return getFactory( data ).property( "beschreibung" );
//                }
//                else 
                	if (input.getPropertyName().equals( PROP.eingangsNr.toString() )) {
                    return getFactory( data ).property( PROP.eingangsNr.toString() );
                }
//                else if (input.getPropertyName().equals( PROP.SBK.toString() )) {
//                    throw new RuntimeException( "Das Feld ist errechnet und kann nicht durchsucht werden: " + PROP.SBK.toString() );
//                }
                else if (input.getPropertyName().equals( PROP.vertragArtNr.toString() )) {
                    throw new RuntimeException( "Das Feld ist errechnet und kann nicht durchsucht werden: " + PROP.vertragArtNr.toString() );
                }
//                else if (input.getPropertyName().equals( PROP.Geprueft.toString() )) {
//                    return getFactory( data ).property( "geprueft" );
//                }
//                else if (input.getPropertyName().equals( PROP.Archiv.toString() )) {
//                    return getFactory( data ).property( "status" );
//                }
                return input;
            }
        }, null );
        DefaultQuery result = new DefaultQuery( query );
        result.setFilter( dublicate );
        return result;
    }


    // benötigt für Tabellendarstellung
    public Feature buildFeature( Entity entity, FeatureType schema ) {
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder( (SimpleFeatureType)schema );
        KaufvertragComposite biotop = (KaufvertragComposite)entity;
        try {
//            fb.set( getDefaultGeometry(), biotop.geom().get() );
            fb.set( PROP.eingangsDatum.toString(), biotop.eingangsDatum().get() );
//            fb.set( PROP.SBK.toString(), Joiner.on( "/" ).useForNull( "-" )
//                    .join( biotop.objnr_sbk().get(), biotop.tk25().get(), biotop.unr().get() ) );
            fb.set( PROP.eingangsNr.toString(), biotop.eingangsNr().get() );
//            fb.set( PROP.Beschreibung.toString(), biotop.beschreibung().get() );
            fb.set( PROP.vertragArtNr.toString(), biotop.vertragArtNr().get() );
//            fb.set( PROP.Wert.toString(), biotop.wert().get() );
//            fb.set( PROP.Geprueft.toString(), biotop.geprueft().get() /*.booleanValue() ? "ja" : "nein"*/ );
//            fb.set( PROP.Archiv.toString(), biotop.status().get() /*== Status.nicht_aktuell.id ? "ja" : "nein"*/ );
            
//            String nummer = biotop.biotoptypArtNr().get();
//            BiotoptypArtComposite biotoptyp = ((BiotopRepository)repo).btForNummer( nummer );
//            fb.set( PROP.Biotoptyp.toString(), biotoptyp != null ? biotoptyp.name().get() : null );
        }
        catch (Exception e) {
            log.warn( "", e );
        }
        return fb.buildFeature( biotop.id() );
    }


    public void modifyFeature( Entity entity, String propName, Object value )
    throws Exception {
    	KaufvertragComposite biotop = (KaufvertragComposite)entity;
////        if (propName.equals( getDefaultGeometry() )) {
////            biotop.geom().set( (MultiPolygon)value );
////        }
//        else if (propName.equals( PROP.Eingangsnummer.toString() )) {
//            biotop.eingangsNr().set( (String)value );
//        }
//        else if (propName.equals( PROP.Beschreibung.toString() )) {
//            biotop.eingangsdatum().set( (String)value );
//        }
//        else if (propName.equals( PROP.Biotoptyp.toString() )) {
//            biotop.biotoptypArtNr().set( (String)value );
//        }
////        else if (propName.equals( PROP.Geprueft.toString() )) {
////            biotop.geprueft().set( value.equals( "ja" ) );
////        }
////        else if (propName.equals( PROP.Archiv.toString() )) {
////            biotop.status().set( value.equals( "ja" ) ? Status.nicht_aktuell.id : Status.aktuell.id );
////        }
//        else {
//            throw new RuntimeException( "Unhandled property: " + propName );
//        }
    }


    public CoordinateReferenceSystem getCoordinateReferenceSystem( String propName ) {
        try {
            return CRS.decode( "EPSG:31468" );
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
