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
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Computed;
import org.qi4j.api.property.ComputedPropertyInstance;
import org.qi4j.api.property.GenericPropertyInfo;
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
@Mixins({ WohnungComposite.Mixin.class, PropertyChangeSupport.Mixin.class, ModelChangeSupport.Mixin.class,
        QiEntity.Mixin.class
// JsonState.Mixin.class
})
@ImportTable("K_EWOHN")
public interface WohnungComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite {

    String NAME = "Wohnung";


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


    // WENR - String
    @Optional
    @ImportColumn("WENR")
    Property<String> wohnungseigentumsNummer();


    // EIGENTART - String
    @Optional
    Association<EigentumsartComposite> eigentumsArt();


    // WOHNFL - Double
    @Optional
    @ImportColumn("WOHNFL")
    Property<Double> wohnflaeche();


    // ZAHLZI - Double
    @Optional
    @ImportColumn("ZAHLZI")
    Property<Double> anzahlZimmer();


    // GESCHOSS - String
    @Optional
    Association<EtageComposite> etage();


    // TODO GJAHR - INT
    @Optional
    @ImportColumn("GJAHR")
    Property<Integer> GJAHR();


    // GESCHBESCH - String
    @Optional
    @ImportColumn("GESCHBESCH")
    Property<String> etageBeschreibung();


    // AUFZUG - String
    @Optional
    @ImportColumn("AUFZUG")
    Property<String> aufzug();


    // HIMMELSRI - String
    @Optional
    Association<HimmelsrichtungComposite> himmelsrichtung();


    // TODO HJAHR - INT
    @Optional
    @ImportColumn("HJAHR")
    Property<Integer> HJAHR();


    // BAUJAHR - Long
    @Optional
    // @ImportColumn("BAUJAHR")
    Property<Double> baujahr();


    // UMBAU - Long
    @Optional
    @ImportColumn("UMBAU")
    Property<Integer> umbau();


    // BERBAUJ - INT
    @Optional
    // @ImportColumn("BERBAUJ")
    Property<Double> bereinigtesBaujahr();


    // GLDAUER - Double
    @Optional
    @ImportColumn("GLDAUER")
    Property<Double> gesamtNutzungsDauer();


    // TODO GAANZAHL - Double
    @Optional
    @ImportColumn("GAANZAHL")
    Property<Double> GAANZAHL();


    // TODO GAART1 - String
    @Optional
    @ImportColumn("GAART1")
    Property<String> GAART1();


    // TODO GAART2 - String
    @Optional
    @ImportColumn("GAART2")
    Property<String> GAART2();


    // TODO STANZAHL - Double
    @Optional
    @ImportColumn("STANZAHL")
    Property<Double> STANZAHL();


    // TODO START1 - String
    @Optional
    @ImportColumn("START1")
    Property<String> START1();


    // TODO START2 - String
    @Optional
    @ImportColumn("START2")
    Property<String> START2();


    // BEWSCHL - String
    @Optional
    Association<AusstattungComposite> ausstattungSchluessel();


    // BEWPUNKTE - Double
    @Optional
    @ImportColumn("BEWPUNKTE")
    Property<Double> bewertungsPunkte();


    // BALKON - String
    @Optional
    @ImportColumn("BALKON")
    Property<String> balkon();


    // BEMERKUNG - String
    @Optional
    Property<String> bemerkung();


    // EINGANGSNR - Double
    @Optional
    // @ImportColumn("EINGANGSNR")
    // Association<VertragComposite> vertrag();
    Association<FlurstueckComposite> flurstueck();


    //
    //
    // // TODO VERTDATUM - SHORT_DATE_TIME
    // @Optional
    // @ImportColumn("VERTDATUM")
    // Property<SHORT_DATE_TIME> VERTDATUM();
    //
    //
    // // TODO VERKZEITP - String
    // @Optional
    // @ImportColumn("VERKZEITP")
    // Property<String> VERKZEITP();
    //
    //
    // // TODO VERTRAGART - String
    // @Optional
    // @ImportColumn("VERTRAGART")
    // Property<String> VERTRAGART();
    //
    //
    // // TODO VKREIS - String
    // @Optional
    // @ImportColumn("VKREIS")
    // Property<String> VKREIS();
    //
    //
    // // TODO KKREIS - String
    // @Optional
    // @ImportColumn("KKREIS")
    // Property<String> KKREIS();
    //
    //
    // KAUFPREIS - Double
    @Optional
    @ImportColumn("KAUFPREIS")
    Property<Double> kaufpreis();


    //
    //
    // ABGA - Double
    @Optional
    @ImportColumn("ABGA")
    Property<Double> abschlagGarage();


    //
    //
    // // TODO ABBEM1 - String
    // @Optional
    // @ImportColumn("ABBEM1")
    // Property<String> ABBEM1();
    //
    //
    // ABNEB - Double
    @Optional
    @ImportColumn("ABNEB")
    Property<Double> abschlagAnderes();


    //
    //
    // // TODO ABBEM2 - String
    // @Optional
    // @ImportColumn("ABBEM2")
    // Property<String> ABBEM2();
    //
    //
    // BERKPREIS - Double
    @Optional
    @ImportColumn("BERKPREIS")
    Property<Double> bereinigterVollpreis();


    // DMQM - Double
    @Optional
    @ImportColumn("DMQM")
    Property<Double> vollpreisWohnflaeche();


    // VERMIETET - String
    @Optional
    @ImportColumn("VERMIETET")
    Property<String> vermietet();


    // GEWICHTUNG - Double
    @Optional
    @ImportColumn("GEWICHTUNG")
    Property<Double> gewichtung();


    // Boolean VERARBKZ - String
    @Optional
    // @ImportColumn("VERARBKZ")
    Property<Boolean> zurAuswertungGeeignet();


    // BEMERKUNG2 - String
    @Optional
    @ImportColumn("BEMERKUNG2")
    Property<String> bemerkungVertragsdaten();


    // MONROHERTR - Double
    @Optional
    @ImportColumn("MONROHERTR")
    Property<Double> monatlicherRohertrag();


    @Optional
    Property<Double> jahresRohertrag();


    @Optional
    Property<Double> monatlicherRohertragJeQm();


    // MIETFESEIT - SHORT_DATE_TIME
    @Optional
    @ImportColumn("MIETFESEIT")
    Property<Date> mietfestsetzungSeit();


    // BODENUNB - Double
    @Optional
    @ImportColumn("BODENUNB")
    Property<Double> bodenpreis();


    // BEBAB - String
    @Optional
    Property<Boolean> mitBebauungsabschlag();


    // BODENBEB - Double
    @Optional
    @ImportColumn("BODENBEB")
    Property<Double> bebauungsabschlag();


    // BEBABSCHL - Long
    @Optional
    // @ImportColumn("BEBABSCHL")
    Property<Double> bebauungsabschlagInProzent();


    // TODO GEBBOD - Double
    @Optional
    @ImportColumn("GEBBOD")
    Property<Double> GEBBOD();


    // TODO BODENWE - Double
    @Optional
    @ImportColumn("BODENWE")
    Property<Double> BODENWE();


    // SACHWERT - Double
    @Optional
    @ImportColumn("SACHWERT")
    Property<Double> sachwertDerWohnung();


    // TODO KAUFSACH - Double
    @Optional
    @ImportColumn("KAUFSACH")
    Property<Double> KAUFSACH();


    // LIEZINS - Double
    @Optional
    @ImportColumn("LIEZINS")
    Property<Double> liegenschaftsZins();


    // TODO FEUEBSCHR - Double
    @Optional
    @ImportColumn("FEUEBSCHR")
    Property<Double> FEUEBSCHR();


    // VERWERTEN - String
    @Optional
    @ImportColumn("VERWERTEN")
    Property<Boolean> geeignet();


    //
    // // TODO HINWEIS - String
    // @Optional
    // @ImportColumn("HINWEIS")
    // Property<String> HINWEIS();

    //
    // // TODO DMEURO - String
    // @Optional
    // @ImportColumn("DMEURO")
    // Property<String> DMEURO();

    // GEBARTG - String
    @Optional
    Association<GebaeudeArtComposite> gebaeudeArtGarage();


    // GEBARTS - String
    @Optional
    Association<GebaeudeArtComposite> gebaeudeArtStellplatz();


    // GEBARTN - String
    @Optional
    Association<GebaeudeArtComposite> gebaeudeArtAnderes();


    // ABST - Double
    @Optional
    @ImportColumn("ABST")
    Property<Double> abschlagStellplatz();


    // TODO ABBEMST - String
    @Optional
    @ImportColumn("ABBEMST")
    Property<String> ABBEMST();


    // TERRASSE - String
    @Optional
    @ImportColumn("TERRASSE")
    Property<String> terrasse();


    // SCHAETZGA - String
    @Optional
    @ImportColumn("SCHAETZGA")
    Property<Boolean> schaetzungGarage();


    // SCHAETZST - String
    @Optional
    @ImportColumn("SCHAETZST")
    Property<Boolean> schaetzungStellplatz();


    // SCHAETZNE - String
    @Optional
    @ImportColumn("SCHAETZNE")
    Property<Boolean> schaetzungAnderes();


    //
    // // TODO GEM - String
    // @Optional
    // @ImportColumn("GEM")
    // Property<String> GEM();
    //
    //
    // // TODO FLUR - String
    // @Optional
    // @ImportColumn("FLUR")
    // Property<String> FLUR();
    //
    //
    // // TODO GEMEINDE - Long
    // @Optional
    // @ImportColumn("GEMEINDE")
    // Property<Integer> GEMEINDE();
    //
    //
    // // TODO STRNR - String
    // @Optional
    // @ImportColumn("STRNR")
    // Property<String> STRNR();
    //
    //
    // // TODO HAUSNR - String
    // @Optional
    // @ImportColumn("HAUSNR")
    // Property<String> HAUSNR();
    //
    //
    // // TODO HZUSNR - String
    // @Optional
    // @ImportColumn("HZUSNR")
    // Property<String> HZUSNR();
    //
    //
    // // TODO FLSTNR - Long
    // @Optional
    // @ImportColumn("FLSTNR")
    // Property<Integer> FLSTNR();
    //
    //
    // // TODO FLSTNRU - String
    // @Optional
    // @ImportColumn("FLSTNRU")
    // Property<String> FLSTNRU();
    //

    // Zusatz - String
    @Optional
    @ImportColumn("Zusatz")
    Property<String> abschlagBemerkung();


    // ANzan - INT
    @Optional
    @ImportColumn("ANzan")
    Property<Integer> anzahlAnderes();


    // Anzga - INT
    @Optional
    @ImportColumn("Anzga")
    Property<Integer> anzahlGaragen();


    // anzst - INT
    @Optional
    @ImportColumn("anzst")
    Property<Integer> anzahlStellplatz();


    // BERBODPREIS - Double
    @Optional
    @ImportColumn("BERBODPREIS")
    Property<Double> bereinigterBodenpreis();


    // TODO BEBABBETR - Double
    @Optional
    @ImportColumn("BEBABBETR")
    Property<Double> BEBABBETR();


    // BEWIKODM - Double
    @Optional
    @ImportColumn("BEWIKODM")
    Property<Double> bewirtschaftungsKosten();


    // BEWIKOPROZ - Double
    @Optional
    @ImportColumn("BEWIKOPROZ")
    Property<Double> bewirtschaftungsKostenInProzent();


    // JREINERTR - Double
    @Optional
    @ImportColumn("JREINERTR")
    Property<Double> jahresReinertrag();


    // BODWERTANT - Double
    @Optional
    @ImportColumn("BODWERTANT")
    Property<Double> bodenwertAnteil();


    // BODWERTANTW - Double
    @Optional
    @ImportColumn("BODWERTANTW")
    Property<Double> bodenwertAnteilDerWohnung();


    // GEBWERTANT - Double
    @Optional
    @ImportColumn("GEBWERTANT")
    Property<Double> gebaeudewertAnteil();


    // GEBWERTANTW - Double
    @Optional
    @ImportColumn("GEBWERTANTW")
    Property<Double> gebaeudewertAnteilDerWohnung();


    // JREINKP - Double
    @Optional
    @ImportColumn("JREINKP")
    Property<Double> jahresReinErtragZuKaufpreis();


    // GEBWERTKP - Double
    @Optional
    @ImportColumn("GEBWERTKP")
    Property<Double> gebaeudewertAnteilZuKaufpreis();


    // KORRFAKTOR - Double
    @Optional
    @ImportColumn("KORRFAKTOR")
    Property<Double> korrekturFaktor();


    // TODO NHK - Long
    @Optional
    @ImportColumn("NHK")
    Property<Integer> NHK();


    // GEWICHT - Long
    @Optional
    @ImportColumn("GEWICHT")
    Property<Integer> gewichtungFehlabweichung();


    // G1 - String
    @Optional
    @ImportColumn("G1")
    Property<String> etage1();


    // FL1 - Long
    @Optional
    @ImportColumn("FL1")
    Property<Integer> flaeche1();


    // M1 - Double
    @Optional
    @ImportColumn("M1")
    Property<Double> miete1();


    // G2 - String
    @Optional
    @ImportColumn("G2")
    Property<String> etage2();


    // FL2 - Long
    @Optional
    @ImportColumn("FL2")
    Property<Integer> flaeche2();


    // M2 - Double
    @Optional
    @ImportColumn("M2")
    Property<Double> miete2();


    // G3 - String
    @Optional
    @ImportColumn("G3")
    Property<String> etage3();


    // FL3 - Long
    @Optional
    @ImportColumn("FL3")
    Property<Integer> flaeche3();


    // M3 - Double
    @Optional
    @ImportColumn("M3")
    Property<Double> miete3();


    // G4 - String
    @Optional
    @ImportColumn("G4")
    Property<String> etage4();


    // FL4 - Long
    @Optional
    @ImportColumn("FL4")
    Property<Integer> flaeche4();


    // M4 - Double
    @Optional
    @ImportColumn("M4")
    Property<Double> miete4();


    // G5 - String
    @Optional
    @ImportColumn("G5")
    Property<String> etage5();


    // FL5 - Long
    @Optional
    @ImportColumn("FL5")
    Property<Integer> flaeche5();


    // M5 - Double
    @Optional
    @ImportColumn("M5")
    Property<Double> miete5();


    // G6 - String
    @Optional
    @ImportColumn("G6")
    Property<String> etage6();


    // FL6 - Long
    @Optional
    @ImportColumn("FL6")
    Property<Integer> flaeche6();


    // M6 - Double
    @Optional
    @ImportColumn("M6")
    Property<Double> miete6();


    // BETRKOST - Long
    @Optional
    @ImportColumn("BETRKOST")
    Property<Integer> betriebskostenInProzent();


    // LIZI_GARAGE - String
    @Optional
    @ImportColumn("LIZI_GARAGE")
    Property<Boolean> garagenBeiLiegenschaftszinsBeruecksichtigen();


    // GESMIETE_EING - String
    @Optional
    Property<Boolean> eingabeGesamtMiete();


    // GM1 - Double
    @Optional
    @ImportColumn("GM1")
    Property<Double> gesamtMiete1();


    // GM2 - Double
    @Optional
    @ImportColumn("GM2")
    Property<Double> gesamtMiete2();


    // GM3 - Double
    @Optional
    @ImportColumn("GM3")
    Property<Double> gesamtMiete3();


    // GM4 - Double
    @Optional
    @ImportColumn("GM4")
    Property<Double> gesamtMiete4();


    // GM5 - Double
    @Optional
    @ImportColumn("GM5")
    Property<Double> gesamtMiete5();


    // GM6 - Double
    @Optional
    @ImportColumn("GM6")
    Property<Double> gesamtMiete6();


    // MIETE_TATS - String
    @Optional
    Property<Boolean> tatsaechlicheMieteVerwenden();


    // TODO GUTACHTNR1 - String
    @Optional
    @ImportColumn("GUTACHTNR1")
    Property<String> GUTACHTNR1();


    // TODO WE_WOHNEINH_ZAEHLER - String
    @Optional
    @ImportColumn("WE_WOHNEINH_ZAEHLER")
    Property<String> WE_WOHNEINH_ZAEHLER();


    // ANZAHL1 - Long
    @Optional
    @ImportColumn("ANZAHL1")
    Property<Integer> anzahl1();


    // ANZAHL2 - Long
    @Optional
    @ImportColumn("ANZAHL2")
    Property<Integer> anzahl2();


    // ANZAHL3 - Long
    @Optional
    @ImportColumn("ANZAHL3")
    Property<Integer> anzahl3();


    // ANZAHL4 - Long
    @Optional
    @ImportColumn("ANZAHL4")
    Property<Integer> anzahl4();


    // ANZAHL5 - Long
    @Optional
    @ImportColumn("ANZAHL5")
    Property<Integer> anzahl5();


    // ANZAHL6 - Long
    @Optional
    @ImportColumn("ANZAHL6")
    Property<Integer> anzahl6();


    @Optional
    @Computed
    Property<String> schl();


    // @Optional
    // TODO auch beim Create einer Wohnung beachten
    // Association<GebaeudeComposite> gebaeude();

    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements WohnungComposite {

        private static Log log = LogFactory.getLog( Mixin.class );


        // @Override
        // public void beforeCompletion()
        // throws UnitOfWorkCompletionException {
        // if (objektNummer().get() == null || objektFortfuehrung().get() == null ||
        // gebaeudeNummer().get() == null
        // || gebaeudeFortfuehrung().get() == null || wohnungsNummer().get() == null
        // || wohnungsFortfuehrung().get() == null) {
        // throw new UnitOfWorkCompletionException(
        // "Alle Nummern und Fortführungen müssen ausgefüllt sein!" );
        // }
        // }

        @Override
        public Property<String> schl() {
            return new ComputedPropertyInstance<String>( new GenericPropertyInfo( WohnungComposite.class, "schl" ) ) {

                @Override
                public String get() {
                    return objektNummer().get() + "/" + objektFortfuehrung().get() + "/" + gebaeudeNummer().get() + "/"
                            + gebaeudeFortfuehrung().get() + "/" + wohnungsNummer().get() + "/"
                            + wohnungsFortfuehrung().get();
                }


                @Override
                public void set( String anIgnoredValue )
                        throws IllegalArgumentException, IllegalStateException {
                    // really ignore
                }
            };
        }


        public static Iterable<FlurstueckComposite> findFlurstueckeFor( WohnungComposite wohnung ) {
            GebaeudeComposite gebaeudeTemplate = QueryExpressions.templateFor( GebaeudeComposite.class );
            BooleanExpression expr3 = QueryExpressions
                    .and( QueryExpressions.eq( gebaeudeTemplate.objektNummer(), wohnung.objektNummer().get() ),
                            QueryExpressions.eq( gebaeudeTemplate.objektFortfuehrung(), wohnung.objektFortfuehrung()
                                    .get() ), QueryExpressions.eq( gebaeudeTemplate.gebaeudeNummer(), wohnung
                                    .gebaeudeNummer().get() ), QueryExpressions.eq(
                                    gebaeudeTemplate.gebaeudeFortfuehrung(), wohnung.gebaeudeFortfuehrung().get() ) );
            GebaeudeComposite gebaeude = KapsRepository.instance().findEntities( GebaeudeComposite.class, expr3, 0, 1 )
                    .find();
            return gebaeude.flurstuecke().toList();
        }


        public static Iterable<WohnungComposite> findWohnungenFor( GebaeudeComposite gebaeude ) {
            WohnungComposite wohnungTemplate = QueryExpressions.templateFor( WohnungComposite.class );
            BooleanExpression expr3 = QueryExpressions
                    .and( QueryExpressions.eq( wohnungTemplate.objektNummer(), gebaeude.objektNummer().get() ),
                            QueryExpressions.eq( wohnungTemplate.objektFortfuehrung(), gebaeude.objektFortfuehrung()
                                    .get() ), QueryExpressions.eq( wohnungTemplate.gebaeudeNummer(), gebaeude
                                    .gebaeudeNummer().get() ), QueryExpressions.eq(
                                    wohnungTemplate.gebaeudeFortfuehrung(), gebaeude.gebaeudeFortfuehrung().get() ) );
            return KapsRepository.instance().findEntities( WohnungComposite.class, expr3, 0, -1 );
        }


        public static VertragComposite vertragFor( WohnungComposite wohnung ) {
            return wohnung.flurstueck().get() != null ? wohnung.flurstueck().get().vertrag().get() : null;
        }


        public static WohnungComposite forKeys( final Integer objektNummer, final Integer objektFortfuehrung,
                final Integer gebaeudeNummer, final Integer gebaeudeFortfuehrung, final Integer wohnungsNummer,
                final Integer wohnungsFortfuehrung ) {
            WohnungComposite template = QueryExpressions.templateFor( WohnungComposite.class );
            BooleanExpression expr = QueryExpressions.and(
                    QueryExpressions.eq( template.objektNummer(), objektNummer ),
                    QueryExpressions.eq( template.objektFortfuehrung(), objektFortfuehrung ),
                    QueryExpressions.eq( template.gebaeudeNummer(), gebaeudeNummer ),
                    QueryExpressions.eq( template.gebaeudeFortfuehrung(), gebaeudeFortfuehrung ),
                    QueryExpressions.eq( template.wohnungsNummer(), wohnungsNummer ),
                    QueryExpressions.eq( template.wohnungsFortfuehrung(), wohnungsFortfuehrung ) );
            Query<WohnungComposite> matches = KapsRepository.instance().findEntities( WohnungComposite.class, expr, 0,
                    1 );
            return matches.find();
        }


        public static Iterable<WohnungComposite> findWohnungenFor( FlurstueckComposite flurstueck ) {
            WohnungComposite template = QueryExpressions.templateFor( WohnungComposite.class );
            BooleanExpression expr = QueryExpressions.eq( template.flurstueck(), flurstueck );
            return KapsRepository.instance().findEntities( WohnungComposite.class, expr, 0, -1 );
        }

    }


    @Optional
    Association<StockwerkStaBuComposite> stockwerkStaBu();
    
    @Optional
    Association<ImmobilienArtStaBuComposite> immobilienArtStaBu();
}
