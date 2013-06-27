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
import org.polymap.kaps.model.SchlNamed;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
@Concerns({ PropertyChangeSupport.Concern.class })
@Mixins({ StalaComposite.Mixin.class, PropertyChangeSupport.Mixin.class,
        ModelChangeSupport.Mixin.class, QiEntity.Mixin.class
// JsonState.Mixin.class
})
@ImportTable("K_STALA")
public interface StalaComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite, SchlNamed {

    // CREATE TABLE ".".K_STALA (
    // ART VARCHAR(1),
    // SCHL VARCHAR(2),
    // BEZ VARCHAR(70)
    // );

    // Arten: Handbuch Seite 40
    // Bauland
    // 1 = Grundstücksart
    // 2 = Art des Baugebietes
    // 3 = Veräußerer/Erwerber
    // 4 = Verwandschaftsverhältnis
    // Agrarland
    // 5 = Veräußerer 
    // 6 = Art des Grundstücks
    // 7 = Erwerber

    static String GRUNDSTUECKSART = "1";
    static String ARTDESBAUGEBIETES = "2";
    static String VERAEUSSERER_BAULAND = "3";
    static String VERWANDSCHAFTSVERHAELTNIS = "4";
    static String VERAEUSSERER_AGRARLAND = "5";
    static String ARTDESGRUNDSTUECKES = "6";
    static String ERWERBER = "7";


    @Optional
    @ImportColumn("ART")
    Property<String> art();

    @Optional
    @ImportColumn("SCHL")
    Property<String> schl();

    @Optional
    @ImportColumn("BEZ")
    Property<String> name();


    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements StalaComposite {

        private static Log log = LogFactory.getLog( Mixin.class );

    }
}