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
package org.polymap.kaps.model.data;

import static org.qi4j.api.query.QueryExpressions.orderBy;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.common.Optional;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.query.grammar.OrderBy;

import org.polymap.core.qi4j.QiEntity;
import org.polymap.core.qi4j.event.ModelChangeSupport;
import org.polymap.core.qi4j.event.PropertyChangeSupport;

import org.polymap.kaps.importer.ImportColumn;
import org.polymap.kaps.importer.ImportTable;
import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.SchlNamed;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
@Concerns({ PropertyChangeSupport.Concern.class })
@Mixins({ RichtwertzoneZeitraumComposite.Mixin.class, PropertyChangeSupport.Mixin.class,
        ModelChangeSupport.Mixin.class, QiEntity.Mixin.class
// JsonState.Mixin.class
})
@ImportTable("K_RIWE")
public interface RichtwertzoneZeitraumComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite, SchlNamed {

    final String NAME = "Richtwertzone Gültigkeit";


    // CREATE TABLE K_RIWE (
    //
    // DMQM DOUBLE, Richtwert
    @Optional
    @ImportColumn("DMQM")
    Property<Double> euroQm();


    @Optional
    Association<RichtwertzoneComposite> zone();


    // RIZONE VARCHAR(7), Nummer PK, scheint keine Referenz zu sein
    @Optional
    @ImportColumn("RIZONE")
    Property<String> schl();


    // BEZ VARCHAR(40), Name
    @Optional
    @ImportColumn("BEZ")
    Property<String> name();


    // JAHR TIMESTAMP, Gültig ab, immer Jahresbeginn
    @Optional
    @ImportColumn("JAHR")
    Property<Date> gueltigAb();


    // STICHTAG TIMESTAMP, 1 Tag vor JAHR
    @Optional
    @ImportColumn("STICHTAG")
    Property<Date> stichtag();


    // EB VARCHAR(1), ist O,1,2,3,Null
    // Erschließungsbeitrag/Erschließungszustand nach BauGB 1,2 oder 3 ->
    @Optional
    Association<ErschliessungsBeitragComposite> erschliessungsBeitrag();


    // Handbuch Seite 23, 24

    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements RichtwertzoneZeitraumComposite {

        private static Log log = LogFactory.getLog( Mixin.class );


        // FIXME uncomment on import
//        @Override
//        public void afterCompletion( UnitOfWorkStatus status ) {
//            RichtwertzoneComposite zone = zone().get();
//            Iterator<RichtwertzoneZeitraumComposite> iterator = forZone( zone ).iterator();
//            if (iterator != null && iterator.hasNext()) {
//                zone.latestZone().set( iterator.next() );
//            }
//        }


        public static Iterable<RichtwertzoneZeitraumComposite> forZone( RichtwertzoneComposite zone ) {
            RichtwertzoneZeitraumComposite template = QueryExpressions
                    .templateFor( RichtwertzoneZeitraumComposite.class );
            BooleanExpression expr = QueryExpressions.eq( template.zone(), zone );
            Query<RichtwertzoneZeitraumComposite> matches = KapsRepository.instance().findEntities(
                    RichtwertzoneZeitraumComposite.class, expr, 0, -1 );
            matches.orderBy( orderBy( template.gueltigAb(), OrderBy.Order.DESCENDING ) );
            return matches;
        }


        // private PropertyInfo schlProperty = new GenericPropertyInfo(
        // RichtwertzoneZeitraumComposite.class, "schl" );
        //
        // @Override
        // public Property<String> schl() {
        // return new ComputedPropertyInstance<String>( schlProperty ) {
        //
        // public String get() {
        // return zone().get();
        // }
        //
        // @Override
        // public void set( String newValue )
        // throws IllegalArgumentException, IllegalStateException {
        // zone().set( newValue );
        // }
        // };
        // }

        public static RichtwertzoneZeitraumComposite findZeitraumFor( RichtwertzoneComposite zone, Date date ) {
            for (RichtwertzoneZeitraumComposite zeitraum : forZone( zone )) {
                // zeiträume sind nach Datum sortiert, neueste zu erst,
                // deshalb ersten nehmen, dessen gültigkeitsbeginn vor dem zieldatum
                // liegt
                if (zeitraum.gueltigAb().get() != null && zeitraum.gueltigAb().get().compareTo( date ) <= 0) {
                    return zeitraum;
                }
            }
            return null;
        }

        // public static Iterable<RichtwertzoneZeitraumComposite> findZoneIn(
        // GemeindeComposite gemeinde ) {
        // RichtwertzoneZeitraumComposite template = QueryExpressions
        // .templateFor( RichtwertzoneZeitraumComposite.class );
        // BooleanExpression expr = QueryExpressions.eq( template.gemeinde(),
        // gemeinde );
        // Query<RichtwertzoneZeitraumComposite> matches =
        // KapsRepository.instance().findEntities(
        // RichtwertzoneZeitraumComposite.class, expr, 0, -1 );
        // // filter auf letzte aktuelle Zone, darf nicht da ja auch ältere Zonen
        // auswählbar sind
        // return matches;
        // }

        // @Override
        // public boolean equals( Object arg0 ) {
        // if (arg0 != null && arg0 instanceof RichtwertzoneZeitraumComposite) {
        // return id().equals( ((RichtwertzoneZeitraumComposite)arg0).id() );
        // }
        // return false;
        // }
    }
}