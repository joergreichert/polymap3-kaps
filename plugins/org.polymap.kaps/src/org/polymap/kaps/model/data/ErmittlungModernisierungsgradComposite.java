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
@Mixins({ ErmittlungModernisierungsgradComposite.Mixin.class, PropertyChangeSupport.Mixin.class,
        ModelChangeSupport.Mixin.class, QiEntity.Mixin.class
// JsonState.Mixin.class
})
@ImportTable("K_BAUJAHRRECH")
public interface ErmittlungModernisierungsgradComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite {

    final static String NAME = "Ermittlung Modernisierungsgrad";


    @Optional
    Association<VertragComposite> vertrag();


    //
    // // BEREICH - String
    // @Optional
    // @ImportColumn("BEREICH")
    // Property<String> BEREICH();

    //
    // // UNTERBER - String
    // @Optional
    // @ImportColumn("UNTERBER")
    // Property<String> UNTERBER();

    // OBJEKTNR - Long
    @Optional
    @ImportColumn("OBJEKTNR")
    Property<Integer> objektNummer();


    // OBJEKTNRFORTF - Long
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


    //
    // // PUNKTE01 - Double
    // @Optional
    // @ImportColumn("PUNKTE01")
    // Property<Double> PUNKTE01();

    // PUNKTE02 - Double
    @Optional
    @ImportColumn("PUNKTE02")
    Property<Double> punkteZeile1();


    // PUNKTE03 - Double
    @Optional
    @ImportColumn("PUNKTE03")
    Property<Double> punkteZeile2();


    // PUNKTE04 - Double
    @Optional
    @ImportColumn("PUNKTE04")
    Property<Double> punkteZeile3();


    // PUNKTE05 - Double
    @Optional
    @ImportColumn("PUNKTE05")
    Property<Double> punkteZeile4();


    // PUNKTE06 - Double
    @Optional
    @ImportColumn("PUNKTE06")
    Property<Double> punkteZeile5();


    // PUNKTE07 - Double
    @Optional
    @ImportColumn("PUNKTE07")
    Property<Double> punkteZeile6();


    // PUNKTE08 - Double
    @Optional
    @ImportColumn("PUNKTE08")
    Property<Double> punkteZeile7();


    // PUNKTE09 - Double
    @Optional
    @ImportColumn("PUNKTE09")
    Property<Double> punkteZeile8();


    //
    // // PUNKTE10 - Double
    // @Optional
    // @ImportColumn("PUNKTE10")
    // Property<Double> PUNKTE10();
    //

    // BERBAUJ - Long
    @Optional
    // @ImportColumn("BERBAUJ")
    Property<Double> bereinigtesBaujahr();


    @Optional
    Property<Double> tatsaechlichesBaujahr();


    @Optional
    Property<Double> restNutzungsDauer();


    @Optional
    Property<Double> auswirkungZeile1();


    @Optional
    Property<Double> auswirkungZeile2();


    @Optional
    Property<Double> auswirkungZeile3();


    @Optional
    Property<Double> auswirkungZeile4();


    @Optional
    Property<Double> auswirkungZeile5();


    @Optional
    Property<Double> auswirkungZeile6();


    @Optional
    Property<Double> auswirkungZeile7();


    @Optional
    Property<Double> auswirkungZeile8();


    @Optional
    Property<Double> modernisierungsGrad();


    // GND - Long
    @Optional
    // @ImportColumn("GND")
    Property<Double> gesamtNutzungsDauer();


    @Optional
    Property<Double> neueRestNutzungsDauer();


    //
    //
    // // METHODE - String
    // @Optional
    // @ImportColumn("METHODE")
    // Property<String> METHODE();

    // ALTER2 - Double
    @Optional
    @ImportColumn("ALTER2")
    Property<Double> alterZeile1();


    // ALTER3 - Double
    @Optional
    @ImportColumn("ALTER3")
    Property<Double> alterZeile2();


    // ALTER4 - Double
    @Optional
    @ImportColumn("ALTER4")
    Property<Double> alterZeile3();


    // ALTER5 - Double
    @Optional
    @ImportColumn("ALTER5")
    Property<Double> alterZeile4();


    // ALTER6 - Double
    @Optional
    @ImportColumn("ALTER6")
    Property<Double> alterZeile5();


    // ALTER7 - Double
    @Optional
    @ImportColumn("ALTER7")
    Property<Double> alterZeile6();


    // ALTER8 - Double
    @Optional
    @ImportColumn("ALTER8")
    Property<Double> alterZeile7();


    // ALTER9 - Double
    @Optional
    @ImportColumn("ALTER9")
    Property<Double> alterZeile8();


    //
    // // ALTER10 - Double
    // @Optional
    // @ImportColumn("ALTER10")
    // Property<Double> alterZeile10();
    //
    //
    // // ANTRAGNR_GUTACHTEN - String
    // @Optional
    // @ImportColumn("ANTRAGNR_GUTACHTEN")
    // Property<String> ANTRAGNR_GUTACHTEN();
    //
    //
    // // GUTACHTNR_GUTACHTEN - String
    // @Optional
    // @ImportColumn("GUTACHTNR_GUTACHTEN")
    // Property<String> GUTACHTNR_GUTACHTEN();
    //
    //
    // // ALTER_OBERGRENZE - Long
    // @Optional
    // @ImportColumn("ALTER_OBERGRENZE")
    // Property<Long> ALTER_OBERGRENZE();
    //

    // ALTER_OBERGRENZE2 - Long
    @Optional
    // @ImportColumn("ALTER_OBERGRENZE2")
    Property<Double> alterObergrenzeZeile1();


    // ALTER_OBERGRENZE3 - Long
    @Optional
    // @ImportColumn("ALTER_OBERGRENZE3")
    Property<Double> alterObergrenzeZeile2();


    // ALTER_OBERGRENZE4 - Long
    @Optional
    // @ImportColumn("ALTER_OBERGRENZE4")
    Property<Double> alterObergrenzeZeile3();


    // ALTER_OBERGRENZE5 - Long
    @Optional
    // @ImportColumn("ALTER_OBERGRENZE5")
    Property<Double> alterObergrenzeZeile4();


    // ALTER_OBERGRENZE6 - Long
    @Optional
    // @ImportColumn("ALTER_OBERGRENZE6")
    Property<Double> alterObergrenzeZeile5();


    // ALTER_OBERGRENZE7 - Long
    @Optional
    // @ImportColumn("ALTER_OBERGRENZE7")
    Property<Double> alterObergrenzeZeile6();


    // ALTER_OBERGRENZE8 - Long
    @Optional
    // @ImportColumn("ALTER_OBERGRENZE8")
    Property<Double> alterObergrenzeZeile7();


    // ALTER_OBERGRENZE9 - Long
    @Optional
    // @ImportColumn("ALTER_OBERGRENZE9")
    Property<Double> alterObergrenzeZeile8();


    //
    // // ALTER_OBERGRENZE10 - Long
    // @Optional
    // @ImportColumn("ALTER_OBERGRENZE10")
    // Property<Double> alterObergrenzeZeile10();

    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements ErmittlungModernisierungsgradComposite {

        private static Log log = LogFactory.getLog( Mixin.class );


        public final static ErmittlungModernisierungsgradComposite forVertrag( VertragComposite vertrag ) {
            ErmittlungModernisierungsgradComposite template = QueryExpressions
                    .templateFor( ErmittlungModernisierungsgradComposite.class );
            BooleanExpression expr = QueryExpressions.eq( template.vertrag(), vertrag );
            return KapsRepository.instance().findEntities( ErmittlungModernisierungsgradComposite.class, expr, 0, 1 )
                    .find();
        }


        public static ErmittlungModernisierungsgradComposite forNHK2010( NHK2010BewertungGebaeudeComposite gebaeude ) {

            ErmittlungModernisierungsgradComposite template = QueryExpressions
                    .templateFor( ErmittlungModernisierungsgradComposite.class );
            BooleanExpression expr = QueryExpressions.and(
                    QueryExpressions.eq( template.gebaeudeNummer(), gebaeude.laufendeNummer().get() ),
                    QueryExpressions.eq( template.vertrag(), gebaeude.bewertung().get().vertrag().get() ) );

            return KapsRepository.instance().findEntities( ErmittlungModernisierungsgradComposite.class, expr, 0, 1 )
                    .find();
        }


        public static Double berechneRND( double modernisierungsgrad, double alter, double gnd ) {
            double relativesAlter = (alter / gnd) * 100;
            if (alter > gnd) {
                alter = gnd;
            }
            double rnd = Math.max( gnd - alter, 0 );
            Double v2 = null;
            Double v3 = null;
            Double v4 = null;
            // Double result = null;
            if (modernisierungsgrad <= 1.0d) {
                if (relativesAlter > 60.0d) {
                    return berechne( 0.0125d, 2.625d, 152.5d, alter, gnd );
                }
                else {
                    return rnd;
                }
            }
            if (modernisierungsgrad > 1.0d && modernisierungsgrad < 8.0d) {
                if (relativesAlter >= 40.0d) {
                    v2 = berechne( 0.0073d, 1.577d, 111.33d, alter, gnd );
                }
                else {
                    v2 = rnd;
                }
            }
            if (modernisierungsgrad > 4.0d && modernisierungsgrad < 13.0d) {
                if (relativesAlter >= 20) {
                    v3 = berechne( 0.0050d, 1.100d, 100.00d, alter, gnd );
                }
                else {
                    v3 = rnd;
                }
            }
            if (modernisierungsgrad > 8.0d && modernisierungsgrad < 18.0d) {
                if (relativesAlter >= 15) {
                    v4 = berechne( 0.0033d, 0.735d, 95.28d, alter, gnd );
                }
                else {
                    v4 = rnd;
                }
            }
            if (modernisierungsgrad >= 18.0d) {
                if (relativesAlter >= 10) {
                    return berechne( 0.0020d, 0.440d, 94.20d, alter, gnd );
                }
                else {
                    return rnd;
                }
            }
            // 2, 3 und 4 überschneiden sich
            if (v2 != null) {
                if (v3 != null) {
                    double diff = (v3 - v2) / (8 - 4);
                    double factor = modernisierungsgrad - 4;
                    return factor * diff + v2;
                }
                else {
                    return v2;
                }
            }
            else if (v3 != null) {
                if (v4 != null) {
                    double diff = (v4 - v3) / (13 - 8);
                    double factor = modernisierungsgrad - 8;
                    return factor * diff + v3;
                }
                else {
                    return v3;
                }
            }
            else if (v4 != null) {
                return v4;
            }
            return 0.0d;
        }


        private final static Double berechne( double a, double b, double c, Double alter, Double gnd ) {
            Double result = (a * (100 / gnd) * alter * alter) - (b * alter) + (c * (gnd / 100));
            if (result < 0.0d) {
                return 0.0d;
            }
            if (result > gnd) {
                return gnd;
            }
            return result;
        }


        public static ErmittlungModernisierungsgradComposite forWohnung( WohnungComposite wohnung ) {
            ErmittlungModernisierungsgradComposite template = QueryExpressions
                    .templateFor( ErmittlungModernisierungsgradComposite.class );
            BooleanExpression expr = QueryExpressions.and(
                    QueryExpressions.eq( template.objektNummer(), wohnung.objektNummer().get() ),
                    QueryExpressions.eq( template.objektFortfuehrung(), wohnung.objektFortfuehrung().get() ),
                    QueryExpressions.eq( template.gebaeudeNummer(), wohnung.gebaeudeNummer().get() ),
                    QueryExpressions.eq( template.gebaeudeFortfuehrung(), wohnung.gebaeudeFortfuehrung().get() ),
                    QueryExpressions.eq( template.wohnungsNummer(), wohnung.wohnungsNummer().get() ),
                    QueryExpressions.eq( template.wohnungsFortfuehrung(), wohnung.wohnungsFortfuehrung().get() ) );
            Query<ErmittlungModernisierungsgradComposite> matches = KapsRepository.instance().findEntities(
                    ErmittlungModernisierungsgradComposite.class, expr, 0, 1 );
            return matches.find();
        }
    }
}
