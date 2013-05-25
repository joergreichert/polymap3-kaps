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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.common.Optional;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.unitofwork.UnitOfWork;

import org.polymap.core.qi4j.QiEntity;
import org.polymap.core.qi4j.event.ModelChangeSupport;
import org.polymap.core.qi4j.event.PropertyChangeSupport;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.SchlNamed;

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
@Concerns({ PropertyChangeSupport.Concern.class })
@Mixins({ ArtDesBaugebietsComposite.Mixin.class, PropertyChangeSupport.Mixin.class,
        ModelChangeSupport.Mixin.class, QiEntity.Mixin.class
// JsonState.Mixin.class
})
public interface ArtDesBaugebietsComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite, SchlNamed {

    // Property<String> schl();

    // Property<String> name();

    @Optional
    Property<Boolean> isAgrar();


    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements ArtDesBaugebietsComposite {

        private static Log log = LogFactory.getLog( Mixin.class );


        public static void createInitData( UnitOfWork uow ) {
            create( uow, "1", "Geschäftsgebiet", Boolean.FALSE );
            create( uow, "2", "Geschäfts- und Wohngebiet gemischt", Boolean.FALSE );
            create( uow, "3", "Wohngebiet in geschlossener Bauweise", Boolean.FALSE );
            create( uow, "4", "Wohngebiet in offener Bauweise", Boolean.FALSE );
            create( uow, "5", "Industriegebiet", Boolean.FALSE );
            create( uow, "6", "Dorfgebiet", Boolean.FALSE );
            create( uow, "7", "mit Gebäude und Inventar", Boolean.TRUE );
            create( uow, "8", "mit Gebäude und ohne Inventar", Boolean.TRUE );
            create( uow, "9", "ohne Gebäude und Inventar", Boolean.TRUE );
        }


        private static ArtDesBaugebietsComposite create(UnitOfWork uow, String schl, String name, Boolean isAgrar ) {
            EntityBuilder<ArtDesBaugebietsComposite> builder = uow.newEntityBuilder( ArtDesBaugebietsComposite.class );
            builder.instance().name().set( name );
            builder.instance().schl().set( schl );
            builder.instance().isAgrar().set( isAgrar );
            return builder.newInstance();
        }


        public static Iterable<ArtDesBaugebietsComposite> findByAgrar( Boolean agrar ) {
            ArtDesBaugebietsComposite template = QueryExpressions
                    .templateFor( ArtDesBaugebietsComposite.class );
            BooleanExpression expr = QueryExpressions.eq( template.isAgrar(), agrar );
            Query<ArtDesBaugebietsComposite> matches = KapsRepository.instance().findEntities(
                    ArtDesBaugebietsComposite.class, expr, 0, -1 );
            return matches;
        }

    }

}
