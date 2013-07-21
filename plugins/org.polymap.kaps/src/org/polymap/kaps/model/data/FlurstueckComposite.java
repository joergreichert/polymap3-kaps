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
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.ComputedPropertyInstance;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Property;

import org.polymap.core.qi4j.QiEntity;
import org.polymap.core.qi4j.event.ModelChangeSupport;
import org.polymap.core.qi4j.event.PropertyChangeSupport;

import org.polymap.kaps.importer.ImportColumn;
import org.polymap.kaps.importer.ImportTable;
import org.polymap.kaps.model.Named;

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
@Concerns({ PropertyChangeSupport.Concern.class })
@Mixins({ FlurstueckComposite.Mixin.class, PropertyChangeSupport.Mixin.class, ModelChangeSupport.Mixin.class,
        QiEntity.Mixin.class
// JsonState.Mixin.class
})
@ImportTable("FLURZWI")
public interface FlurstueckComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite, Named {

    // GEMARKUNG VARCHAR(4),
    @Optional
    Association<GemarkungComposite> gemarkung();


    // FLUR VARCHAR(3),
    @Optional
    Association<FlurComposite> flur();


    // FLSTNR1 INTEGER DEFAULT 0,
    @Optional
    @ImportColumn("FLSTNR1")
    Property<Integer> nummer();


    // FLSTNR1U VARCHAR(3),
    @Optional
    @ImportColumn("FLSTNR1U")
    Property<String> unterNummer();



    // FLAECHE1 DOUBLE,
    @Optional
    @UseDefaults
    @ImportColumn("FLAECHE1")
    Property<Double> flaeche();


    // KARTBLATT VARCHAR(6),
    @Optional
    @ImportColumn("KARTBLATT")
    Property<String> kartenBlatt();


    // XGK DOUBLE, IGNORE, eventuell später mal als geom()
    // YGK DOUBLE,

    // KARTBLATTN VARCHAR(10),
    @Optional
    @ImportColumn("KARTBLATTN")
    Property<String> kartenBlattNummer();


    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements FlurstueckComposite {

        private static Log log = LogFactory.getLog( Mixin.class );


        @Override
        public Property<String> name() {
            return new ComputedPropertyInstance<String>( new GenericPropertyInfo( FlurstueckComposite.class, "name" ) ) {

                public String get() {
                    StringBuffer label = new StringBuffer();
//                    if (strasse().get() != null) {
//                        label.append( strasse().get().name().get() ).append( " - " );
//                    }
//                    if (hausnummer().get() != null) {
//                        label.append( hausnummer().get() );
//
//                        if (hausnummerZusatz().get() != null) {
//                            label.append( hausnummerZusatz().get() );
//                        }
//                        label.append( " - " );
//                    }
                    if (gemarkung().get() != null) {
                        label.append( gemarkung().get().name().get() ).append( " - " );
                    }
                    label.append( nummer().get() ).append( "/" ).append( unterNummer().get() );
                    return label.toString();
                }
            };
        }
    }
}
