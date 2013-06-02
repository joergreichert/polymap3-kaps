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
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Computed;
import org.qi4j.api.property.ComputedPropertyInstance;
import org.qi4j.api.property.GenericPropertyInfo;
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
@Mixins({ FlurstueckWohneigentumComposite.Mixin.class, PropertyChangeSupport.Mixin.class,
        ModelChangeSupport.Mixin.class, QiEntity.Mixin.class
// JsonState.Mixin.class
})
@ImportTable("K_EOBJF")
public interface FlurstueckWohneigentumComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite {

    String NAME = "Flurstück Wohneigentum";


    // OBJEKTNR - Long
    @Optional
    @ImportColumn("OBJEKTNR")
    Property<Long> objektNummer();


    // FORTF - Long
    @Optional
    @ImportColumn("FORTF")
    Property<Long> objektFortfuehrung();


    // GEM - String
    @Optional
    Association<GemarkungComposite> gemarkung();


    // FLUR - String
    @Optional
    Association<FlurComposite> flur();


    // FLSTNR - Long
    @Optional
    @ImportColumn("FLSTNR")
    Property<Long> nummer();


    // FLSTNRU - String
    @Optional
    @ImportColumn("FLSTNRU")
    Property<String> unterNummer();


    // GFLAECHE - Double
    @Optional
    @ImportColumn("GFLAECHE")
    Property<Double> flaeche();


    // ERBBAUR - String
    @Optional
    @ImportColumn("ERBBAUR")
    Property<String> erbbaurecht();


    // BELASTUNG - String
    @Optional
    Association<BelastungComposite> belastung();


    // NUTZUNG - String
    @Optional
    Association<NutzungComposite> nutzung();


    // BAUBLOCK - String
    @Optional
    @ImportColumn("BAUBLOCK")
    Property<String> baublock();


    // STRNR - String
    @Optional
    Association<StrasseComposite> strasse();


    // KARTBLATT - String
    @Optional
    @ImportColumn("KARTBLATT")
    Property<String> kartenBlatt();


    // GEBNR - Long
    @Optional
    @ImportColumn("GEBNR")
    Property<Long> gebaeudeNummer();


    // GEBNRFORTF - Long
    @Optional
    @ImportColumn("GEBNRFORTF")
    Property<Long> gebaeudeFortfuehrung();


    // HAUSNR - String
    @Optional
    @ImportColumn("HAUSNR")
    Property<String> hausnummer();


    // KARTBLATTN - String
    @Optional
    @ImportColumn("KARTBLATTN")
    Property<String> kartenBlattNummer();


    // HZUSNR - String
    @Optional
    @ImportColumn("HZUSNR")
    Property<String> hausnummerZusatz();


    // RIZONE - String
    @Optional
    Association<RichtwertzoneComposite> richtwertZone();


    @Optional
    @Computed
    Property<String> schl();


    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements FlurstueckWohneigentumComposite {

        private static Log log = LogFactory.getLog( Mixin.class );


        // String id = entity.objektNummer() + "." + entity.objektFortfuehrung() +
        // "." + entity.gebaeudeNummer() + "." +
        @Override
        public Property<String> schl() {
            return new ComputedPropertyInstance<String>( new GenericPropertyInfo( WohnungComposite.class, "schl" ) ) {

                @Override
                public String get() {
                    return objektNummer().get() + "/" + objektFortfuehrung().get() + "/" + gebaeudeNummer().get() + "/"
                            + gebaeudeFortfuehrung().get() + "/" + nummer().get() + "/" + unterNummer().get();
                }
            };
        }
    }
}
