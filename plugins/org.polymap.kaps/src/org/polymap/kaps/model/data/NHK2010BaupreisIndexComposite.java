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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.common.Optional;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.ComputedPropertyInstance;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.QueryExpressions;

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
@Mixins({ NHK2010BaupreisIndexComposite.Mixin.class, PropertyChangeSupport.Mixin.class, ModelChangeSupport.Mixin.class,
        QiEntity.Mixin.class
// JsonState.Mixin.class
})
@ImportTable("K_EBKIND10")
public interface NHK2010BaupreisIndexComposite
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


    @Optional
    Property<String> schl();


    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements NHK2010BaupreisIndexComposite {

        private static Log log = LogFactory.getLog( Mixin.class );


        @Override
        public Property<String> schl() {
            return new ComputedPropertyInstance<String>( new GenericPropertyInfo( NHK2010BaupreisIndexComposite.class,
                    "schl" ) ) {

                @Override
                public String get() {
                    return jahr().get() + " " + monatVon().get() + "/" + monatBis().get();
                }


                @Override
                public void set( String anIgnoredValue )
                        throws IllegalArgumentException, IllegalStateException {
                    // really ignore
                }
            };
        }


        public final static Values indexFor( String indexType, Date wertErmittlungsStichtag ) {
            // durchschnitt für 2010 für den Typ berechnen
            Values result = new Values();
            NHK2010BaupreisIndexComposite template = QueryExpressions.templateFor( NHK2010BaupreisIndexComposite.class );
            KapsRepository repo = KapsRepository.instance();
            Double summe = 0.0d;
            Integer count = 0;
            for (NHK2010BaupreisIndexComposite index : repo.findEntities( NHK2010BaupreisIndexComposite.class,
                    QueryExpressions.eq( template.jahr(), 2010 ), 0, 100 )) {
                count++;
                summe += indexFor( indexType, index );
            }
            result.durchschnitt = summe / count;

            // danach wert für datum und typ finden
            Calendar cal = GregorianCalendar.getInstance();
            cal.setTime( wertErmittlungsStichtag );
            int year = cal.get( Calendar.YEAR );
            int month = cal.get( Calendar.MONTH ) + 1;
            Double indexValue = 0.0d;
            Integer lastMonatBis = -1;
            for (NHK2010BaupreisIndexComposite index : repo.findEntities( NHK2010BaupreisIndexComposite.class,
                    QueryExpressions.eq( template.jahr(), year ), 0, 100 )) {
                Double currentIndex = indexFor( indexType, index );

                Integer monatBis = index.monatBis().get();
                Integer monatVon = index.monatVon().get();
                if (monatVon <= month && monatBis >= month) {
                    indexValue = currentIndex;
                    // jahr noch nicht komplett so den höchsten Wert nehmen
                }
                else if (monatBis > lastMonatBis) {
                    indexValue = currentIndex;
                    lastMonatBis = monatBis;
                }
            }
            result.index = indexValue;
            result.result = result.index / result.durchschnitt;
            return result;
        }


        public final static Double indexFor( String indexType, NHK2010BaupreisIndexComposite index ) {
            if ("E".equals( indexType )) {
                return index.einfamilienGebaeude().get();
            }
            else if ("M".equals( indexType )) {
                return index.mehrfamilienGebaeude().get();
            }
            else if ("B".equals( indexType )) {
                return index.bueroGebaeude().get();
            }
            else if ("G".equals( indexType )) {
                return index.gewerbeBetrieb().get();
            }
            else {
                throw new IllegalStateException( "Unknown indexType " + indexType );
            }
        }
    }


    public static class Values {

        public Double durchschnitt;

        public Double index;

        public Double result;
    }

}
