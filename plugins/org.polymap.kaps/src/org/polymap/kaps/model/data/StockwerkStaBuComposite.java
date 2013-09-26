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
@Mixins({ StockwerkStaBuComposite.Mixin.class, PropertyChangeSupport.Mixin.class,
        ModelChangeSupport.Mixin.class, QiEntity.Mixin.class
// JsonState.Mixin.class
})
public interface StockwerkStaBuComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite, SchlNamed {

//    Property<String> schl();

//    Property<String> name();
    
    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements StockwerkStaBuComposite {

        private static Log log = LogFactory.getLog( Mixin.class );

        public static void createInitData(SchlNamedCreatorCallback cb) {
            cb.create(StockwerkStaBuComposite.class, "E", "Erdgeschoß" );
            cb.create(StockwerkStaBuComposite.class, "K", "Kellergeschoß/Souterrain" );
            cb.create(StockwerkStaBuComposite.class, "D", "Dachgeschoß/Penthouse" );
            cb.create(StockwerkStaBuComposite.class, "1", "1. Stock" );
            cb.create(StockwerkStaBuComposite.class, "2", "2. Stock" );
            cb.create(StockwerkStaBuComposite.class, "3", "3. Stock" );
            cb.create(StockwerkStaBuComposite.class, "4", "4. Stock" );
            cb.create(StockwerkStaBuComposite.class, "5", "5. Stock" );
            cb.create(StockwerkStaBuComposite.class, "6", "6. Stock" );
            cb.create(StockwerkStaBuComposite.class, "7", "7. Stock" );
            cb.create(StockwerkStaBuComposite.class, "8", "8. Stock" );
            cb.create(StockwerkStaBuComposite.class, "9", "9. Stock" );
            cb.create(StockwerkStaBuComposite.class, "10", "10. Stock" );
            cb.create(StockwerkStaBuComposite.class, "11", "11. Stock" );
            cb.create(StockwerkStaBuComposite.class, "12", "12. Stock" );
            cb.create(StockwerkStaBuComposite.class, "13", "13. Stock" );
            cb.create(StockwerkStaBuComposite.class, "14", "14. Stock" );
        }
    }

}
