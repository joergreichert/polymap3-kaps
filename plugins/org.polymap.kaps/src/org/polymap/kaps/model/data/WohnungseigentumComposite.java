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

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.common.Optional;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Computed;
import org.qi4j.api.property.ComputedPropertyInstance;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;

import org.polymap.core.qi4j.QiEntity;
import org.polymap.core.qi4j.event.ModelChangeSupport;
import org.polymap.core.qi4j.event.PropertyChangeSupport;

import org.polymap.kaps.importer.ImportColumn;
import org.polymap.kaps.importer.ImportTable;
import org.polymap.kaps.model.KapsRepository;

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
@Concerns({ PropertyChangeSupport.Concern.class })
@Mixins({ WohnungseigentumComposite.Mixin.class, PropertyChangeSupport.Mixin.class, ModelChangeSupport.Mixin.class,
        QiEntity.Mixin.class
// JsonState.Mixin.class
})
@ImportTable("K_EOBJ")
public interface WohnungseigentumComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite {

    String NAME = "Wohnungseigentum";


    // OBJEKTNR - Long
    @Optional
    @ImportColumn("OBJEKTNR")
    Property<Integer> objektNummer();


    // TEDATUM - SHORT_DATE_TIME
    @Optional
    @ImportColumn("TEDATUM")
    Property<Date> datumTeilungserklerung();


    // BEMERKUNG - String
    @Optional
    // @ImportColumn("BEMERKUNG")
    Property<String> bemerkungen();


    // GFLAECHE_AKT - Long
    @Optional
    // @ImportColumn("GFLAECHE_AKT")
    Property<Double> gesamtFlaeche();


    // TEURNR - String
    @Optional
    @ImportColumn("TEURNR")
    Property<String> urkundenNummerDerTeilungserklaerung();


    @Optional
    @Computed
    Property<String> schl();


    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements WohnungseigentumComposite {

        private static Log log = LogFactory.getLog( Mixin.class );


        @Override
        public void beforeCompletion()
                throws UnitOfWorkCompletionException {
            if (objektNummer().get() == null) {
                objektNummer().set( KapsRepository.instance().objektnummern.get().generate() );
            }
        }


        @Override
        public Property<String> schl() {
            return new ComputedPropertyInstance<String>( new GenericPropertyInfo( WohnungseigentumComposite.class,
                    "schl" ) ) {

                @Override
                public String get() {
                    return String.valueOf( objektNummer().get() );
                }


                @Override
                public void set( String anIgnoredValue )
                        throws IllegalArgumentException, IllegalStateException {
                    // IGNORE
                }
            };
        }
    }

}
