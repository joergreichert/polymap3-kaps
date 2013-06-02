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
@Mixins({ WohnungsTeileigentumComposite.Mixin.class, PropertyChangeSupport.Mixin.class, ModelChangeSupport.Mixin.class,
        QiEntity.Mixin.class
// JsonState.Mixin.class
})
@ImportTable("K_EWOHNT")
public interface WohnungsTeileigentumComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite {

    String NAME = "Wohnung - Teileigentum";


    // OBJEKTNR - Long
    @Optional
    @ImportColumn("OBJEKTNR")
    Property<Long> objektNummer();


    // OBJEKTNRFORTF - Long
    @Optional
    @ImportColumn("OBJEKTNRFORTF")
    Property<Long> objektFortfuehrung();


    // GEBNR - Long
    @Optional
    @ImportColumn("GEBNR")
    Property<Long> gebaeudeNummer();


    // GEBFORTF - Long
    @Optional
    @ImportColumn("GEBFORTF")
    Property<Long> gebaeudeFortfuehrung();


    // WOHNUNGSNR - Long
    @Optional
    @ImportColumn("WOHNUNGSNR")
    Property<Long> wohnungsNummer();


    // FORTF - Long
    @Optional
    @ImportColumn("FORTF")
    Property<Long> wohnungsFortfuehrung();


    // TENR - String
    @Optional
    @ImportColumn("TENR")
    Property<String> teileigentumNummer();


    // BRZAEHLER - Double
    @Optional
    @ImportColumn("BRZAEHLER")
    Property<Double> bruchteilZaehler();


    // BRNENNER - Double
    @Optional
    @ImportColumn("BRNENNER")
    Property<Double> bruchteilNenner();


    // BEMERKUNG - String
    @Optional
    @ImportColumn("BEMERKUNG")
    Property<String> bemerkung();


    // TEBEZ - String
    @Optional
    // @ImportColumn("TEBEZ")
    Association<GebaeudeArtComposite> gebaeudeArt();


    @Optional
    @Computed
    Property<String> schl();


    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements WohnungsTeileigentumComposite {

        private static Log log = LogFactory.getLog( Mixin.class );


        @Override
        public Property<String> schl() {
            return new ComputedPropertyInstance<String>( new GenericPropertyInfo( WohnungsTeileigentumComposite.class,
                    "schl" ) ) {

                @Override
                public String get() {
                    return objektNummer().get() + "/" + objektFortfuehrung().get() + "/" + gebaeudeNummer().get() + "/"
                            + gebaeudeFortfuehrung().get() + "/" + wohnungsNummer().get() + "/"
                            + wohnungsFortfuehrung().get();
                }
            };
        }
    }

}
