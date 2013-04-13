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
@Mixins({ BauweiseComposite.Mixin.class, PropertyChangeSupport.Mixin.class,
        ModelChangeSupport.Mixin.class, QiEntity.Mixin.class
// JsonState.Mixin.class
})
// FIXME in aktueller kaufdat.mdb nicht referenziert
public interface BauweiseComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite, SchlNamed {


    public static abstract class Mixin
            implements BauweiseComposite {

        private static Log log = LogFactory.getLog( Mixin.class );

        public static void createInitData(SchlNamedCreatorCallback cb) {
            cb.create(BauweiseComposite.class, "o", "offene Bauweise" );
            cb.create(BauweiseComposite.class, "g", "geschlossene Bauweise" );
            cb.create(BauweiseComposite.class, "a", "abweichende Bauweise" );
            cb.create(BauweiseComposite.class, "eh", "Einzelhäuser" );
            cb.create(BauweiseComposite.class, "ed", "Einzel- doer Doppelhäuser" );
            cb.create(BauweiseComposite.class, "dh", "Doppelhaushälften" );
            cb.create(BauweiseComposite.class, "rh", "Reihenhäuser" );
            cb.create(BauweiseComposite.class, "rm", "Reihenmittelhäuser" );
            cb.create(BauweiseComposite.class, "er", "Reihenendhäuser" );
        }
    }

}
