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
@Mixins({ VertragsdatenAgrarComposite.Mixin.class, PropertyChangeSupport.Mixin.class, ModelChangeSupport.Mixin.class,
        QiEntity.Mixin.class,
// JsonState.Mixin.class
})
@ImportTable("K_BEVERL")
public interface VertragsdatenAgrarComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite {

    String NAME = "Erweiterte Vertragsdaten Agrar";


    @Optional
    // @ImportColumn("EINGANGSNR")
    // Property<Double> EINGANGSNR();
    Association<VertragComposite> vertrag();


    @Optional
    @ImportColumn("LAGEKL")
    Property<Double> lageklasse();


    // VERARBKZ - String
    @Optional
    // @ImportColumn("VERARBKZ")
    Property<Boolean> fuerStatistikGeeignet();


    @Optional
    @ImportColumn("GESBAUWE")
    Property<Double> gesamtBauWert();


    // VERFAHREN - String
    @Optional
    @ImportColumn("VERFAHREN")
    Property<String> gesamtBauWertNach();


    // BONU1 - String
    @Optional
    Association<BodennutzungComposite> bodennutzung1();


    // FL1 - Double
    @Optional
    @ImportColumn("FL1")
    Property<Double> flaechenAnteil1();


    // ZUAB1 - Double
    @Optional
    @ImportColumn("ZUAB1")
    Property<Double> bodenrichtwert1();


    // ABGL1 - Double
    @Optional
    @ImportColumn("ABGL1")
    Property<Double> abgleichAufKaufpreis1();


    // WERT1 - Double
    @Optional
    @ImportColumn("WERT1")
    Property<Double> bodenwert1();


    // BONU2 - String
    @Optional
    Association<BodennutzungComposite> bodennutzung2();


    // FL2 - Double
    @Optional
    @ImportColumn("FL2")
    Property<Double> flaechenAnteil2();


    // ZUAB2 - Double
    @Optional
    @ImportColumn("ZUAB2")
    Property<Double> bodenrichtwert2();


    // ABGL2 - Double
    @Optional
    @ImportColumn("ABGL2")
    Property<Double> abgleichAufKaufpreis2();


    // WERT2 - Double
    @Optional
    @ImportColumn("WERT2")
    Property<Double> bodenwert2();


    // BONU3 - String
    @Optional
    Association<BodennutzungComposite> bodennutzung3();


    // FL3 - Double
    @Optional
    @ImportColumn("FL3")
    Property<Double> flaechenAnteil3();


    // ZUAB3 - Double
    @Optional
    @ImportColumn("ZUAB3")
    Property<Double> bodenrichtwert3();


    // ABGL3 - Double
    @Optional
    @ImportColumn("ABGL3")
    Property<Double> abgleichAufKaufpreis3();


    // WERT3 - Double
    @Optional
    @ImportColumn("WERT3")
    Property<Double> bodenwert3();


    // BONU4 - String
    @Optional
    Association<BodennutzungComposite> bodennutzung4();


    // FL4 - Double
    @Optional
    @ImportColumn("FL4")
    Property<Double> flaechenAnteil4();


    // ZUAB4 - Double
    @Optional
    @ImportColumn("ZUAB4")
    Property<Double> bodenrichtwert4();


    // ABGL4 - Double
    @Optional
    @ImportColumn("ABGL4")
    Property<Double> abgleichAufKaufpreis4();


    // WERT4 - Double
    @Optional
    @ImportColumn("WERT4")
    Property<Double> bodenwert4();


    // BONU5 - String
    @Optional
    Association<BodennutzungComposite> bodennutzung5();


    // FL5 - Double
    @Optional
    @ImportColumn("FL5")
    Property<Double> flaechenAnteil5();


    // ZUAB5 - Double
    @Optional
    @ImportColumn("ZUAB5")
    Property<Double> bodenrichtwert5();


    // ABGL5 - Double
    @Optional
    @ImportColumn("ABGL5")
    Property<Double> abgleichAufKaufpreis5();


    // WERT5 - Double
    @Optional
    @ImportColumn("WERT5")
    Property<Double> bodenwert5();


    // BONU6 - String
    @Optional
    Association<BodennutzungComposite> bodennutzung6();


    // FL6 - Double
    @Optional
    @ImportColumn("FL6")
    Property<Double> flaechenAnteil6();


    // ZUAB6 - Double
    @Optional
    @ImportColumn("ZUAB6")
    Property<Double> bodenrichtwert6();


    // ABGL6 - Double
    @Optional
    @ImportColumn("ABGL6")
    Property<Double> abgleichAufKaufpreis6();


    // WERT6 - Double
    @Optional
    @ImportColumn("WERT6")
    Property<Double> bodenwert6();


    // GESBOWERT - Double
    @Optional
    @ImportColumn("GESBOWERT")
    Property<Double> bodenwertGesamt();


    // GESSAWERT - Double
    @Optional
    @ImportColumn("GESSAWERT")
    Property<Double> sachwertGesamt();


    // FAKTOR - Double
    @Optional
    @ImportColumn("FAKTOR")
    Property<Double> faktorKaufpreisZuSachwert();


    // KPANTGEB - Double
    @Optional
    @ImportColumn("KPANTGEB")
    Property<Double> kaufpreisAnteilBaulicheAnlagen();


    // KPANTGRU - Double
    @Optional
    @ImportColumn("KPANTGRU")
    Property<Double> kaufpreisAnteilBodenwert();


    @Optional
    @ImportColumn("BERBAUJ")
    Property<Integer> bereinigtesBaujahr();


    // WZ11 - Long
    @Optional
    @ImportColumn("WZ11")
    Property<Long> ackerzahl1();


    // TODO WZ21 - Long
    @Optional
    @ImportColumn("WZ21")
    Property<Long> WZ21();


    // WZ12 - Long
    @Optional
    @ImportColumn("WZ12")
    Property<Long> ackerzahl2();


    // TODO WZ22 - Long
    @Optional
    @ImportColumn("WZ22")
    Property<Long> WZ22();


    // WZ13 - Long
    @Optional
    @ImportColumn("WZ13")
    Property<Long> ackerzahl3();


    // TODO WZ23 - Long
    @Optional
    @ImportColumn("WZ23")
    Property<Long> WZ23();


    // WZ14 - Long
    @Optional
    @ImportColumn("WZ14")
    Property<Long> ackerzahl4();


    // TODO WZ24 - Long
    @Optional
    @ImportColumn("WZ24")
    Property<Long> WZ24();


    // WZ15 - Long
    @Optional
    @ImportColumn("WZ15")
    Property<Long> ackerzahl5();


    // TODO WZ25 - Long
    @Optional
    @ImportColumn("WZ25")
    Property<Long> WZ25();


    // WZ16 - Long
    @Optional
    @ImportColumn("WZ16")
    Property<Long> ackerzahl6();


    // TODO WZ26 - Long
    @Optional
    @ImportColumn("WZ26")
    Property<Long> WZ26();


    @Optional
    // @ImportColumn("BEM2") + BEM1
    Property<String> bemerkungen();


    // FLGE2 - Double
    @Optional
    @ImportColumn("FLGE2")
    Property<Double> flaechenAnteilGesamt();


    // TODO RESD1 - Double
    @Optional
    @ImportColumn("RESD1")
    Property<Double> RESD1();


    //
    // // TODO NUTZUNG - String
    // @Optional
    // @ImportColumn("NUTZUNG")
    // Property<String> NUTZUNG();

    // TODO BHKZ - String
    @Optional
    @ImportColumn("BHKZ")
    Property<String> BHKZ();


    //
    // // TODO GEMEINDE - Long
    // @Optional
    // @ImportColumn("GEMEINDE")
    // Property<Long> GEMEINDE();

    @Optional
    // @ImportColumn("GEBART")
    // Property<String> GEBART();
    Association<GebaeudeArtComposite> gebaeudeArt();


    @Optional
    @ImportColumn("BAUJAHR")
    Property<Integer> baujahr();


    // bebaut - String
    @Optional
    // @ImportColumn("bebaut")
    Property<Boolean> istBebaut();


    // RIWEGEEIGNET - String
    @Optional
    // @ImportColumn("RIWEGEEIGNET")
    Property<Boolean> zurRichtwertermittlungGeeignet();


    // TODO ZUAB1X - String
    @Optional
    @ImportColumn("ZUAB1X")
    Property<String> ZUAB1X();


    // TODO ABGL1X - String
    @Optional
    @ImportColumn("ABGL1X")
    Property<String> ABGL1X();


    // FLST_AUSWERTUNG - String
    // ist immer flurstücksbezogen
    @Optional
    @ImportColumn("FLST_AUSWERTUNG")
    Property<String> FLST_AUSWERTUNG();


    // //
    // //
    // @Optional
    // Association<FlurstueckComposite> flurstueck();

    // TODO KEYGES - String
    @Optional
    @ImportColumn("KEYGES")
    Property<String> KEYGES();


    // RIZO1 - String
    @Optional
    Association<RichtwertzoneZeitraumComposite> richtwertZone1();


    // RIZO2 - String
    @Optional
    Association<RichtwertzoneZeitraumComposite> richtwertZone2();


    // RIZO3 - String
    @Optional
    Association<RichtwertzoneZeitraumComposite> richtwertZone3();


    // RIZO4 - String
    @Optional
    Association<RichtwertzoneZeitraumComposite> richtwertZone4();


    // RIZO5 - String
    @Optional
    Association<RichtwertzoneZeitraumComposite> richtwertZone5();


    // RIZO6 - String
    @Optional
    Association<RichtwertzoneZeitraumComposite> richtwertZone6();


    @Optional
    Property<Double> flaecheLandwirtschaftStala();


    @Optional
    Property<Double> hypothekStala();


    @Optional
    Property<Double> wertTauschStala();


    @Optional
    Property<Double> wertSonstigesStala();


    @Optional
    Property<String> bemerkungStala();


    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements VertragsdatenAgrarComposite {

        private static Log log = LogFactory.getLog( Mixin.class );


        // public static VertragsdatenAgrarComposite forFlurstueck(
        // FlurstueckComposite flurstueck ) {
        // VertragsdatenAgrarComposite template = QueryExpressions.templateFor(
        // VertragsdatenAgrarComposite.class );
        // BooleanExpression expr = QueryExpressions.eq( template.flurstueck(),
        // flurstueck );
        // return KapsRepository.instance().findEntities(
        // VertragsdatenAgrarComposite.class, expr, 0, 1 ).find();
        // }

        public static VertragsdatenAgrarComposite forVertrag( VertragComposite vertrag ) {
            VertragsdatenAgrarComposite template = QueryExpressions.templateFor( VertragsdatenAgrarComposite.class );
            BooleanExpression expr = QueryExpressions.eq( template.vertrag(), vertrag );
            return KapsRepository.instance().findEntities( VertragsdatenAgrarComposite.class, expr, 0, -1 ).find();
        }


        public static Iterable<VertragsdatenAgrarComposite> forRWZ( RichtwertzoneZeitraumComposite zone ) {
            VertragsdatenAgrarComposite template = QueryExpressions.templateFor( VertragsdatenAgrarComposite.class );
            BooleanExpression expr = QueryExpressions.or( QueryExpressions.eq( template.richtwertZone1(), zone ),
                    QueryExpressions.eq( template.richtwertZone2(), zone ),
                    QueryExpressions.eq( template.richtwertZone3(), zone ),
                    QueryExpressions.eq( template.richtwertZone4(), zone ),
                    QueryExpressions.eq( template.richtwertZone5(), zone ),
                    QueryExpressions.eq( template.richtwertZone6(), zone ) );
            return KapsRepository.instance().findEntities( VertragsdatenAgrarComposite.class, expr, 0, -1 );
        }


        @Override
        public Property<Double> verkaufteFlaeche() {
            return new ComputedPropertyInstance<Double>( new GenericPropertyInfo( VertragsdatenAgrarComposite.class,
                    "verkaufteFlaeche" ) ) {

                @Override
                public Double get() {
                    double ret = 0.0d;
                    for (FlurstueckComposite flurstueck : FlurstueckComposite.Mixin.forEntity( vertrag().get() )) {
                        NutzungComposite nutzung = flurstueck.nutzung().get();
                        if (nutzung != null && nutzung.isAgrar().get() != null
                                && nutzung.isAgrar().get() == Boolean.TRUE) {
                            Double flaeche = flurstueck.verkaufteFlaeche().get();
                            if (flaeche != null) {
                                ret += flaeche.doubleValue();
                            }
                        }
                    }
                    return ret;
                }


                @Override
                public void set( Double anIgnoredValue )
                        throws IllegalArgumentException, IllegalStateException {
                    // ignored
                }
            };
        }
    }


    @Optional
    @Computed
    Property<Double> verkaufteFlaeche();
}
