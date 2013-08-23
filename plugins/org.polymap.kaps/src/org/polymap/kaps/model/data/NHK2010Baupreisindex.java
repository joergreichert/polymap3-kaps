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
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

import org.polymap.core.qi4j.QiEntity;
import org.polymap.core.qi4j.event.ModelChangeSupport;
import org.polymap.core.qi4j.event.PropertyChangeSupport;

import org.polymap.kaps.importer.ImportColumn;
import org.polymap.kaps.importer.ImportTable;

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
@Concerns({ PropertyChangeSupport.Concern.class })
@Mixins({ NHK2010Baupreisindex.Mixin.class, PropertyChangeSupport.Mixin.class, ModelChangeSupport.Mixin.class,
        QiEntity.Mixin.class
// JsonState.Mixin.class
})
@ImportTable("K_EBKIND10")
public interface NHK2010Baupreisindex
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite {

    final static String NAME = "NHK 2010 - Baupreisindex";


    // JAHR - Double
    @Optional
    // @ImportyColumn("JAHR")
    Property<Integer> jahr();


    // MONVON - Double
    @Optional
    // @ImportColumn("MONVON")
    Property<Integer> monatVon();


    // MONBIS - Double
    @Optional
    // @ImportColumn("MONBIS")
    Property<Integer> monatBis();


    // INDEX_WG - Double
    @Optional
    @ImportColumn("INDEX_WG")
    Property<Double> wohneigentum();


    // INDEX_EG - Double
    @Optional
    @ImportColumn("INDEX_EG")
    Property<Double> einfamilienGebaeude();


    // INDEX_MG - Double
    @Optional
    @ImportColumn("INDEX_MG")
    Property<Double> mehrfamilienGebaeude();


    // INDEX_BG - Double
    @Optional
    @ImportColumn("INDEX_BG")
    Property<Double> bueroGebaeude();


    // INDEX_GW - Double
    @Optional
    @ImportColumn("INDEX_GW")
    Property<Double> gewerbeBetrieb();


    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements NHK2010Baupreisindex {

        private static Log log = LogFactory.getLog( Mixin.class );
    }

}
