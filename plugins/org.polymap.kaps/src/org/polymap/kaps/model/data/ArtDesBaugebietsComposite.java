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

import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.mixin.Mixins;

import org.polymap.core.qi4j.QiEntity;
import org.polymap.core.qi4j.event.ModelChangeSupport;
import org.polymap.core.qi4j.event.PropertyChangeSupport;

import org.polymap.kaps.model.SchlNamed;
import org.polymap.kaps.model.SchlNamedCreatorCallback;

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

//    Property<String> schl();

//    Property<String> name();
    
    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements ArtDesBaugebietsComposite {

        private static Log log = LogFactory.getLog( Mixin.class );

        public static void createInitData(SchlNamedCreatorCallback cb) {
            cb.create(ArtDesBaugebietsComposite.class, "1", "Geschäftsgebiet" );
            cb.create(ArtDesBaugebietsComposite.class, "2", "Geschäfts- und Wohngebiet gemischt" );
            cb.create(ArtDesBaugebietsComposite.class, "3", "Wohngebiet in geschlossener Bauweise" );
            cb.create(ArtDesBaugebietsComposite.class, "4", "Wohngebiet in offener Bauweise" );
            cb.create(ArtDesBaugebietsComposite.class, "5", "Industriegebiet" );
            cb.create(ArtDesBaugebietsComposite.class, "6", "Dorfgebiet" );
            cb.create(ArtDesBaugebietsComposite.class, "7", "mit Gebäude und Inventar" );
            cb.create(ArtDesBaugebietsComposite.class, "8", "mit Gebäude und ohne Inventar" );
            cb.create(ArtDesBaugebietsComposite.class, "9", "ohne Gebäude und Inventar" );
        }
    }

}
