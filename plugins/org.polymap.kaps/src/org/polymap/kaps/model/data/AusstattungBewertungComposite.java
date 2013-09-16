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
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;

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
@Mixins({ AusstattungBewertungComposite.Mixin.class, PropertyChangeSupport.Mixin.class, ModelChangeSupport.Mixin.class,
        QiEntity.Mixin.class
// JsonState.Mixin.class
})
@ImportTable("K_EAUSX")
public interface AusstattungBewertungComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite {

    public final String NAME = "Ausstattungsmerkmale - Bewertung";
    
    // OBJEKTNR - Long
    @Optional
    @ImportColumn("OBJEKTNR")
    Property<Integer> objektNummer();


    // OBJEKTNRFORTF - Integer
    @Optional
    @ImportColumn("OBJEKTNRFORTF")
    Property<Integer> objektFortfuehrung();


    // GEBNR - Long
    @Optional
    @ImportColumn("GEBNR")
    Property<Integer> gebaeudeNummer();


    // GEBFORTF - Long
    @Optional
    @ImportColumn("GEBFORTF")
    Property<Integer> gebaeudeFortfuehrung();


    // WOHNUNGSNR - Long
    @Optional
    @ImportColumn("WOHNUNGSNR")
    Property<Integer> wohnungsNummer();


    // FORTF - Long
    @Optional
    @ImportColumn("FORTF")
    Property<Integer> wohnungsFortfuehrung();


    // ME1P - Double
    @Optional
    @ImportColumn("ME1P")
    Property<Double> ME1P();


    // ME11 - String
    @Optional
    @ImportColumn("ME11")
    Property<Boolean> ME11();


    // ME12 - String
    @Optional
    @ImportColumn("ME12")
    Property<Boolean> ME12();


    // ME13 - String
    @Optional
    @ImportColumn("ME13")
    Property<Boolean> ME13();


    // ME14 - String
    @Optional
    @ImportColumn("ME14")
    Property<Boolean> ME14();


    // ME2P - Double
    @Optional
    @ImportColumn("ME2P")
    Property<Double> ME2P();


    // ME21 - String
    @Optional
    @ImportColumn("ME21")
    Property<Boolean> ME21();


    // ME22 - String
    @Optional
    @ImportColumn("ME22")
    Property<Boolean> ME22();


    // ME23 - String
    @Optional
    @ImportColumn("ME23")
    Property<Boolean> ME23();


    // ME3P - Double
    @Optional
    @ImportColumn("ME3P")
    Property<Double> ME3P();


    // ME31 - String
    @Optional
    @ImportColumn("ME31")
    Property<Boolean> ME31();


    // ME32 - String
    @Optional
    @ImportColumn("ME32")
    Property<Boolean> ME32();


    // ME33 - String
    @Optional
    @ImportColumn("ME33")
    Property<Boolean> ME33();


    // ME34 - String
    @Optional
    @ImportColumn("ME34")
    Property<Boolean> ME34();


    // ME35 - String
    @Optional
    @ImportColumn("ME35")
    Property<Boolean> ME35();


    // ME36 - String
    @Optional
    @ImportColumn("ME36")
    Property<Boolean> ME36();


    // ME37 - String
    @Optional
    @ImportColumn("ME37")
    Property<Boolean> ME37();


    // ME372 - String
    @Optional
    @ImportColumn("ME372")
    Property<Boolean> ME372();


    // ME38 - String
    @Optional
    @ImportColumn("ME38")
    Property<Boolean> ME38();


    // ME39 - String
    @Optional
    @ImportColumn("ME39")
    Property<Boolean> ME39();


    // ME4P - Double
    @Optional
    @ImportColumn("ME4P")
    Property<Double> ME4P();


    // ME41 - String
    @Optional
    @ImportColumn("ME41")
    Property<Boolean> ME41();


    // ME42 - String
    @Optional
    @ImportColumn("ME42")
    Property<Boolean> ME42();


    // ME43 - String
    @Optional
    @ImportColumn("ME43")
    Property<Boolean> ME43();


    // ME5P - Double
    @Optional
    @ImportColumn("ME5P")
    Property<Double> ME5P();


    // ME51 - String
    @Optional
    @ImportColumn("ME51")
    Property<Boolean> ME51();


    // ME52 - String
    @Optional
    @ImportColumn("ME52")
    Property<Boolean> ME52();


    // ME53 - String
    @Optional
    @ImportColumn("ME53")
    Property<Boolean> ME53();


    // ME15 - String
    @Optional
    @ImportColumn("ME15")
    Property<Boolean> ME15();


    // ME24 - String
    @Optional
    @ImportColumn("ME24")
    Property<Boolean> ME24();


    // ME44 - String
    @Optional
    @ImportColumn("ME44")
    Property<Boolean> ME44();


    // ME45 - String
    @Optional
    @ImportColumn("ME45")
    Property<Boolean> ME45();


    // ME46 - String
    @Optional
    @ImportColumn("ME46")
    Property<Boolean> ME46();


    // ME32A - String
    @Optional
    @ImportColumn("ME32A")
    Property<Boolean> ME32A();


    // ME33A - String
    @Optional
    @ImportColumn("ME33A")
    Property<Boolean> ME33A();


    // ME54 - String
    @Optional
    @ImportColumn("ME54")
    Property<Boolean> ME54();


    // ME61 - String
    @Optional
    @ImportColumn("ME61")
    Property<Boolean> ME61();


    // ME62 - String
    @Optional
    @ImportColumn("ME62")
    Property<Boolean> ME62();


    // ME63 - String
    @Optional
    @ImportColumn("ME63")
    Property<Boolean> ME63();


    // ME64 - String
    @Optional
    @ImportColumn("ME64")
    Property<Boolean> ME64();


    // ME71 - String
    @Optional
    @ImportColumn("ME71")
    Property<Boolean> ME71();


    // ME72 - String
    @Optional
    @ImportColumn("ME72")
    Property<Boolean> ME72();


    // ME73 - String
    @Optional
    @ImportColumn("ME73")
    Property<Boolean> ME73();


    // ME6P - Long
    @Optional
//    @ImportColumn("ME6P")
    Property<Double> ME6P();


    // ME7P - Long
    @Optional
//    @ImportColumn("ME7P")
    Property<Double> ME7P();
//
//
//    // NEU - String
//    @Optional
//    @ImportColumn("NEU")
//    Property<Boolean> NEU();


    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements AusstattungBewertungComposite {

        private static Log log = LogFactory.getLog( Mixin.class );

        public static AusstattungBewertungComposite forWohnung( WohnungComposite wohnung ) {
            AusstattungBewertungComposite template = QueryExpressions
                    .templateFor( AusstattungBewertungComposite.class );
            BooleanExpression expr = QueryExpressions.eq( template.wohnung(), wohnung );
            Query<AusstattungBewertungComposite> matches = KapsRepository.instance().findEntities(
                    AusstattungBewertungComposite.class, expr, 0, 1 );
            return matches.find();
        }
    }


    @Optional
    Association<WohnungComposite> wohnung();


    @Optional
    Property<Double> gesamtSumme();

}
