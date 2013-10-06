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
@Mixins({ WohnlageStaBuComposite.Mixin.class, PropertyChangeSupport.Mixin.class, ModelChangeSupport.Mixin.class,
        QiEntity.Mixin.class
// JsonState.Mixin.class
})
public interface WohnlageStaBuComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite, SchlNamed {

    public static String NAME = "Stadträumliche Wohnlage";


    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements WohnlageStaBuComposite {

        private static Log log = LogFactory.getLog( Mixin.class );


        public static void createInitData( SchlNamedCreatorCallback cb ) {
            cb.create( WohnlageStaBuComposite.class, "1", "einfache Lage" );
            cb.create( WohnlageStaBuComposite.class, "2", "mittlere Lage" );
            cb.create( WohnlageStaBuComposite.class, "3", "gute Lage" );
            cb.create( WohnlageStaBuComposite.class, "4", "Spitzenlage" );
        }
    }

}
