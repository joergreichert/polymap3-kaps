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
import org.qi4j.api.entity.association.kaps.ComputedAssociationInstance;
import org.qi4j.api.entity.association.kaps.GenericAssociationInfo;
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
@Mixins({ FlurstuecksdatenBaulandComposite.Mixin.class, PropertyChangeSupport.Mixin.class,
        ModelChangeSupport.Mixin.class, QiEntity.Mixin.class,
// JsonState.Mixin.class
})
@ImportTable("K_BEVERW")
public interface FlurstuecksdatenBaulandComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite {

    String NAME = "Erweiterte Flurstücksdaten Bauland";


    // 16.000 Objekte
    // CREATE TABLE K_BEVERW (
    // LAGEKL DOUBLE,
    // TODO <= 9.9 in ui
    @Optional
    @ImportColumn("LAGEKL")
    Property<Double> lageklasse();


    // GFZ DOUBLE,
    // TODO <= 9,9
    @Optional
    @ImportColumn("GFZ")
    Property<Double> gfz();


    // TODO VERARBKZ VARCHAR(1),
    @Optional
    @ImportColumn("VERARBKZ")
    Property<String> VERARBKZ();


    // RIZUSCHL DOUBLE,
    @Optional
    @ImportColumn("RIZUSCHL")
    Property<Double> richtwertZuschlagProzent();


    // RIZUBET DOUBLE,
    @Optional
    @ImportColumn("RIZUBET")
    Property<Double> richtwertZuschlagBerechnet();


    // RIZUBEM VARCHAR(25),
    @Optional
    @ImportColumn("RIZUBEM")
    Property<String> richtwertZuschlagBemerkung();


    // RIABSCHL DOUBLE,
    @Optional
    @ImportColumn("RIABSCHL")
    Property<Double> richtwertAbschlagProzent();


    // RIABBET DOUBLE,
    @Optional
    @ImportColumn("RIABBET")
    Property<Double> richtwertAbschlagBerechnet();


    // RIABBEM VARCHAR(25),
    @Optional
    @ImportColumn("RIABBEM")
    Property<String> richtwertAbschlagBemerkung();


    // ERSCHLKO DOUBLE,
    @Optional
    @ImportColumn("ERSCHLKO")
    Property<Double> erschliessungsKosten();


    // BERWEUNB DOUBLE,
    @Optional
    @ImportColumn("BERWEUNB")
    Property<Double> bodenpreisUnbebaut();


    // BERBOFAKT DOUBLE,
    @Optional
    @ImportColumn("BERBOFAKT")
    Property<Double> bebAbschlag();


    @Optional
    Property<Double> bebAbschlagBerechnet();


    // BERWEBEB DOUBLE,
    @Optional
    @ImportColumn("BERWEBEB")
    Property<Double> bodenpreisBebaut();


    // TODO GESBAUWE DOUBLE,
    @Optional
    @ImportColumn("GESBAUWE")
    Property<Double> GESBAUWE();


    // TODO VERFAHREN VARCHAR(36),
    @Optional
    @ImportColumn("VERFAHREN")
    Property<String> VERFAHREN();


    // FL1 DOUBLE,
    @Optional
    @ImportColumn("FL1")
    Property<Double> flaeche1();


    // DMQM1 DOUBLE,
//    @Optional
//    @ImportColumn("DMQM1")
//    Property<Double> bodenpreisQm1();
    // deprecated entspricht bodenpreisBebaut()  



    // DM1 DOUBLE,
    @Optional
    @ImportColumn("DM1")
    Property<Double> bodenwert1();


    // QMBER1 DOUBLE,
    @Optional
    @ImportColumn("QMBER1")
    Property<Double> bodenwertBereinigt1();


    // FL2 DOUBLE,
    @Optional
    @ImportColumn("FL2")
    Property<Double> flaeche2();


    // DMQM2 DOUBLE,
    @Optional
    @ImportColumn("DMQM2")
    Property<Double> bodenpreisQm2();


    // DM2 DOUBLE,
    @Optional
    @ImportColumn("DM2")
    Property<Double> bodenwert2();


    // QMBER2 DOUBLE,
    @Optional
    @ImportColumn("QMBER2")
    Property<Double> bodenwertBereinigt2();


    // FL3 DOUBLE,
    @Optional
    @ImportColumn("FL3")
    Property<Double> flaeche3();


    // DMQM3 DOUBLE,
    @Optional
    @ImportColumn("DMQM3")
    Property<Double> bodenpreisQm3();


    // DM3 DOUBLE,
    @Optional
    @ImportColumn("DM3")
    Property<Double> bodenwert3();


    // QMBER3 DOUBLE,
    @Optional
    @ImportColumn("QMBER3")
    Property<Double> bodenwertBereinigt3();


    // FL4 DOUBLE,
    @Optional
    @ImportColumn("FL4")
    Property<Double> flaeche4();


    // DMQM4 DOUBLE,
    @Optional
    @ImportColumn("DMQM4")
    Property<Double> bodenpreisQm4();


    // DM4 DOUBLE,
    @Optional
    @ImportColumn("DM4")
    Property<Double> bodenwert4();


    // QMBER4 DOUBLE,
    @Optional
    @ImportColumn("QMBER4")
    Property<Double> bodenwertBereinigt4();


    // TODO SONST1 VARCHAR(18),
    @Optional
    @ImportColumn("SONST1")
    Property<String> SONST1();


    // FL5 DOUBLE,
    @Optional
    @ImportColumn("FL5")
    Property<Double> flaeche5();


    // DMQM5 DOUBLE,
    @Optional
    @ImportColumn("DMQM5")
    Property<Double> bodenpreisQm5();


    // DM5 DOUBLE,
    @Optional
    @ImportColumn("DM5")
    Property<Double> bodenwert5();


    // QMBER5 DOUBLE,
    @Optional
    @ImportColumn("QMBER5")
    Property<Double> bodenwertBereinigt5();


    // SONST2 VARCHAR(18),
    @Optional
    @ImportColumn("SONST2")
    Property<String> bodenwertAufteilungText6();


    // FL6 DOUBLE,
    @Optional
    @ImportColumn("FL6")
    Property<Double> flaeche6();


    // DMQM6 DOUBLE,
    @Optional
    @ImportColumn("DMQM6")
    Property<Double> bodenpreisQm6();


    // DM6 DOUBLE,
    @Optional
    @ImportColumn("DM6")
    Property<Double> bodenwert6();


    // QMBER6 DOUBLE,
    @Optional
    @ImportColumn("QMBER6")
    Property<Double> bodenwertBereinigt6();


    // FLGE DOUBLE,
    // Verkaufsfläche aus Vertrag
    @Optional
    @ImportColumn("FLGE")
    Property<Double> verkaufteFlaecheGesamt();


    // FLGE2 DOUBLE, Gesamtfläche aus Vertrag
    @Optional
    @ImportColumn("FLGE2")
    Property<Double> flaecheGesamt();


    // SACHWERT1 DOUBLE,
    @Optional
    @ImportColumn("SACHWERT1")
    Property<Double> sachwert();


    // FAKTOR DOUBLE,
    // TODO default 1
    @Optional
    @ImportColumn("FAKTOR")
    Property<Double> faktorBereinigterKaufpreis();


    // FAKTOR2 DOUBLE,
    // TODO default 1
    @Optional
    @ImportColumn("FAKTOR2")
    Property<Double> faktorOhneStrassenplatz();


    // KPANTGEB DOUBLE,
    @Optional
    @ImportColumn("KPANTGEB")
    Property<Double> kaufpreisAnteilDerBaulichenAnlagen();


    // KPANTGRU DOUBLE,
    @Optional
    @ImportColumn("KPANTGRU")
    Property<Double> kaufpreisAnteilBodenwert();


    // BPAKV DOUBLE,
    @Optional
    @ImportColumn("BPAKV")
    Property<Double> bodenpreisAbgleichAufBaupreisBebaut();


    // BPAKVUNB DOUBLE,
    @Optional
    @ImportColumn("BPAKVUNB")
    Property<Double> bodenpreisAbgleichAufKaufpreisUnbebaut();


    // VKWABFAKT DOUBLE,
    // TODO default 1
    @Optional
    @ImportColumn("VKWABFAKT")
    Property<Double> verkehrswertFaktor();


    // ZWSUEK DOUBLE,
    @Optional
    @ImportColumn("ZWSUEK")
    Property<Double> zwischensummeEK();


    // ZWRIZU DOUBLE,
    @Optional
    @ImportColumn("ZWRIZU")
    Property<Double> zwischensummeRichtwertZuschlag();


    // ZWRIAB DOUBLE,
    @Optional
    @ImportColumn("ZWRIAB")
    Property<Double> zwischensummeRichtwertAbschlag();


    // RIWE DOUBLE,
    @Optional
    @ImportColumn("RIWE")
    Property<Double> vorlaeufigerBodenpreis();


    // GEBART VARCHAR(3),
    @Optional
    @Computed
    Association<GebaeudeArtComposite> gebaeudeArt();


    // BERBAUJ INTEGER,
    @Optional
    @ImportColumn("BERBAUJ")
    Property<Integer> baujahrBereinigt();


    // GESCHFL DOUBLE,
    @Optional
    @ImportColumn("GESCHFL")
    Property<Double> geschossFlaeche();


    // UMBRAUM DOUBLE,
    @Optional
    @ImportColumn("UMBRAUM")
    Property<Double> bruttoRaumInhalt();


    // BM VARCHAR(25),
    @Optional
    @ImportColumn("BM")
    Property<String> bewertungsMethode();


    // BTEXT VARCHAR(36),
    @Optional
    @ImportColumn("BTEXT")
    Property<String> bText();


    // ETEXT VARCHAR(36),
    @Optional
    @ImportColumn("ETEXT")
    Property<String> eText();


    // EW1 VARCHAR(100),
    @Optional
    @ImportColumn("EW1")
    Property<String> ergebnisWuerdigung();


    // EINGANGSNR DOUBLE,
    @Optional
    Association<VertragComposite> kaufvertrag();


    // TODO GRART DOUBLE,
    @Optional
    @ImportColumn("GRART")
    Property<Double> grart();


    // TODO RND DOUBLE,
    @Optional
    @ImportColumn("RND")
    Property<Double> rnd();


    // TODO JRE DOUBLE,
    @Optional
    @ImportColumn("JRE")
    Property<Double> jre();


    // TODO JSB DOUBLE,
    @Optional
    @ImportColumn("JSB")
    Property<Double> jsb();


    // TODO GEW1 DOUBLE,
    @Optional
    @ImportColumn("GEW1")
    Property<Double> gew1();


    // TODO GEW2 DOUBLE,
    @Optional
    @ImportColumn("GEW2")
    Property<Double> gew2();


    // TODO GEW3 DOUBLE,
    @Optional
    @ImportColumn("GEW3")
    Property<Double> gew3();


    // TODO LIEZINS DOUBLE,
    @Optional
    @ImportColumn("LIEZINS")
    Property<Double> liezins();


    // GESBOWERT DOUBLE,
    @Optional
    @ImportColumn("GESBOWERT")
    Property<Double> bodenwertGesamt();


    // TODO GEWANT DOUBLE,
    @Optional
    @ImportColumn("GEWANT")
    Property<Double> gewant();


    // TODO INNEN VARCHAR(1),
    @Optional
    @ImportColumn("INNEN")
    Property<String> innen();


    // TODO GEWART VARCHAR(50),
    @Optional
    @ImportColumn("GEWART")
    Property<String> gewart();


    // ERBBAU VARCHAR(1),
    @Optional
    Property<Boolean> erbbauRecht();


    // TODO WERT1 DOUBLE,
    @Optional
    @ImportColumn("WERT1")
    Property<Double> wert1();


    // TODO WERT2 DOUBLE,
    @Optional
    @ImportColumn("WERT2")
    Property<Double> wert2();


    // TODO WERT3 DOUBLE,
    @Optional
    @ImportColumn("WERT3")
    Property<Double> wert3();


    // TODO WERT4 DOUBLE,
    @Optional
    @ImportColumn("WERT4")
    Property<Double> wert4();


    // TODO WERT5 DOUBLE,
    @Optional
    @ImportColumn("WERT5")
    Property<Double> wert5();


    // TODO WERT6 DOUBLE,
    @Optional
    @ImportColumn("WERT6")
    Property<Double> wert6();


    // WOHNFLAECHE DOUBLE DEFAULT 0,
    @Optional
    @ImportColumn("WOHNFLAECHE")
    Property<Double> wohnflaeche();


    // GEWERBEFLAECHE DOUBLE DEFAULT 0,
    @Optional
    @ImportColumn("GEWERBEFLAECHE")
    Property<Double> gewerbeflaeche();


    // GRUNDSTUECKSTIEFE DOUBLE DEFAULT 0,
    @Optional
    @ImportColumn("GRUNDSTUECKSTIEFE")
    Property<Double> grundstuecksTiefe();


    // GRUNDSTUECKSBREITE DOUBLE DEFAULT 0,
    @Optional
    @ImportColumn("GRUNDSTUECKSBREITE")
    Property<Double> grundstuecksBreite();


    // AUSSTATTUNG SMALLINT DEFAULT 0,
    // 1 = stark gehoben
    // 2 = gehoben
    // 3 = mittel
    // 4 = einfach
    // 5 = einfach
    @Optional
    @ImportColumn("AUSSTATTUNG")
    Property<Integer> ausstattung();


    // TODO BHKZ VARCHAR(1),
    // geht eventuell auf K_BEW
    // C, G, E
    @Optional
    Property<String> bhkz();


    // NUTZUNG VARCHAR(2),
    @Optional
    @Computed
    Association<NutzungComposite> nutzung();


    // GEMEINDE INTEGER,
    @Optional
    @Computed
    Association<GemeindeComposite> gemeinde();


    // BRUTTOGRUNDFLAECHE DOUBLE DEFAULT 0,
    @Optional
    @ImportColumn("BRUTTOGRUNDFLAECHE")
    Property<Double> bruttoGrundflaeche();


    // MARKTANP VARCHAR(1),
    @Optional
    Property<Boolean> faktorFuerMarktanpassungGeeignet();


    // TODO LIZI VARCHAR(1),
    @Optional
    @ImportColumn("LIZI")
    Property<String> lizi();


    // TODO VOLLGESCH DOUBLE,
    @Optional
    @ImportColumn("VOLLGESCH")
    Property<Double> vollgesch();


    // RIWEBEREIN DOUBLE,
    @Optional
    @ImportColumn("RIWEBEREIN")
    Property<Double> richtwertBereinigung();


    // BAUJAHR INTEGER,
    @Optional
    @ImportColumn("BAUJAHR")
    Property<Integer> baujahr();


    // TODO GND DOUBLE,
    @Optional
    @ImportColumn("GND")
    Property<Double> gnd();


    // GFZBERBODPREIS DOUBLE DEFAULT 0,
    @Optional
    @ImportColumn("GFZBERBODPREIS")
    Property<Double> gfzBereinigterBodenpreis();


    // GFZVERWENDEN VARCHAR(1),
    @Optional
    Property<Boolean> gfzBereinigtenBodenpreisVerwenden();


    // KVBODPREISBER DOUBLE DEFAULT 0,
    @Optional
    @ImportColumn("KVBODPREISBER")
    Property<Double> normierterGfzBereinigterBodenpreis();


    // Denkmalschutz VARCHAR(1),
    @Optional
    Property<Boolean> denkmalschutz();


    // BEREINBEM VARCHAR(40),
    @Optional
    @ImportColumn("BEREINBEM")
    Property<String> richtwertBereinigungBemerkung();


    // BODWTEXT1 VARCHAR(2),
    @Optional
    Association<BodenwertAufteilungTextComposite> bodenwertAufteilung1();


    // BODWTEXT2 VARCHAR(2),
    @Optional
    Association<BodenwertAufteilungTextComposite> bodenwertAufteilung2();


    // BODWTEXT3 VARCHAR(2),
    @Optional
    Association<BodenwertAufteilungTextComposite> bodenwertAufteilung3();


    // EURO_UMSTELL VARCHAR(1) ignored

    // VOLLGESCHX VARCHAR(6),
    @Optional
    @ImportColumn("VOLLGESCHX")
    Property<String> zulaessigeVollgeschosse();


    // BONUTZ VARCHAR(2),
    @Optional
    // @Computed
    Association<BodennutzungComposite> bodennutzung();


    // EB VARCHAR(1),
    @Optional
    // @Computed
    Association<ErschliessungsBeitragComposite> erschliessungsBeitrag();


    // TODO BODPREISPROZ VARCHAR(1),
    @Optional
    @ImportColumn("BODPREISPROZ")
    Property<String> bodpreisproz();


    // TODO DMQM2PROZ DOUBLE,
    @Optional
    @ImportColumn("DMQM2PROZ")
    Property<Double> dmqm2proz();


    // TODO DMQM3PROZ DOUBLE,
    @Optional
    @ImportColumn("DMQM3PROZ")
    Property<Double> dmqm3proz();


    // TODO DMQM4PROZ DOUBLE,
    @Optional
    @ImportColumn("DMQM4PROZ")
    Property<Double> dmqm4proz();


    // TODO DMQM5PROZ DOUBLE,
    @Optional
    @ImportColumn("DMQM5PROZ")
    Property<Double> dmqm5proz();


    // TODO DMQM6PROZ DOUBLE,
    @Optional
    @ImportColumn("DMQM6PROZ")
    Property<Double> dmqm6proz();


    // RICHTWERT DOUBLE,
    @Optional
    // @ImportColumn("RICHTWERT")
    @Computed
    Property<Double> richtwert();


    // GFZBER VARCHAR(20),
    @Optional
    // @ImportColumn("GFZBER")
    @Computed
    Property<String> gfzBereich();


    // TODO BEBABBETR DOUBLE,
    @Optional
    @ImportColumn("BEBABBETR")
    Property<Double> bebabbetr();


    // GFZKOMMA VARCHAR(1),
    @Optional
    Property<Boolean> bereinigterBodenpreisMitNachkommastellen();


    // BODWNICHT VARCHAR(1),
    @Optional
    Property<Boolean> fuerBodenwertaufteilungNichtGeeignet();


    // TODO BERECHNUNG_NEU VARCHAR(1),
    @Optional
    @ImportColumn("BERECHNUNG_NEU")
    Property<String> berechnungNeu();


    // RIZONE VARCHAR(7),
    // @Optional
    // @Computed
    // Association<RichtwertzoneComposite> richtwertZone();

    @Optional
    // @Computed
    Association<RichtwertzoneZeitraumComposite> richtwertZoneG();


    // SAN VARCHAR(1),
    @Optional
    Property<Boolean> sanierung();


    // SANANFEND VARCHAR(1),
    @Optional
    // A oder E
    Property<Boolean> sanierungAnfangswert();


    // TODO VKWABFAKT_GB DOUBLE,
    @Optional
    @ImportColumn("VKWABFAKT_GB")
    Property<Double> VKWABFAKT_GB();


    // TODO VKWABFAKT_UMB DOUBLE,
    @Optional
    @ImportColumn("VKWABFAKT_UMB")
    Property<Double> VKWABFAKT_UMB();


    // TODO VKWABFAKT_BGF95 DOUBLE,
    @Optional
    @ImportColumn("VKWABFAKT_BGF95")
    Property<Double> VKWABFAKT_BGF95();


    // TODO VKWABFAKT_BRI95 DOUBLE,
    @Optional
    @ImportColumn("VKWABFAKT_BRI95")
    Property<Double> VKWABFAKT_BRI95();


    // TODO VKWABFAKT_2000 DOUBLE,
    @Optional
    @ImportColumn("VKWABFAKT_2000")
    Property<Double> VKWABFAKT_2000();


    // TODO VKWABFAKT_2005 DOUBLE,
    @Optional
    @ImportColumn("VKWABFAKT_2005")
    Property<Double> VKWABFAKT_2005();


    // TODO VKWABFAKT_EWN DOUBLE,
    @Optional
    @ImportColumn("VKWABFAKT_EWN")
    Property<Double> VKWABFAKT_EWN();


    // TODO VKWABFAKT_EWL DOUBLE,
    @Optional
    @ImportColumn("VKWABFAKT_EWL")
    Property<Double> VKWABFAKT_EWL();


    // TODO VKWABFAKT_UNB DOUBLE,
    @Optional
    @ImportColumn("VKWABFAKT_UNB")
    Property<Double> VKWABFAKT_UNB();


    // RIJAHR TIMESTAMP,
    @Optional
    @ImportColumn("RIJAHR")
    Property<Date> richtwertzoneJahr();


    // TODO ABSCHLAG_NHK95BGF DOUBLE,
    @Optional
    @ImportColumn("ABSCHLAG_NHK95BGF")
    Property<Double> ABSCHLAG_NHK95BGF();


    // TODO ABSCHLAG_NHK95BRI DOUBLE,
    @Optional
    @ImportColumn("ABSCHLAG_NHK95BRI")
    Property<Double> ABSCHLAG_NHK95BRI();


    // TODO ZUSCHLAG_NHK95BGF DOUBLE,
    @Optional
    @ImportColumn("ZUSCHLAG_NHK95BGF")
    Property<Double> ZUSCHLAG_NHK95BGF();


    // TODO ZUSCHLAG_NHK95BRI DOUBLE,
    @Optional
    @ImportColumn("ZUSCHLAG_NHK95BRI")
    Property<Double> ZUSCHLAG_NHK95BRI();


    // TODO ABSCHLAG_NHK2000 DOUBLE,
    @Optional
    @ImportColumn("ABSCHLAG_NHK2000")
    Property<Double> ABSCHLAG_NHK2000();


    // TODO ZUSCHLAG_NHK2000 DOUBLE,
    @Optional
    @ImportColumn("ZUSCHLAG_NHK2000")
    Property<Double> ZUSCHLAG_NHK2000();


    // KELLER VARCHAR(1),
    @Optional
    Association<KellerComposite> keller();


    // TODO VKWABFAKT_EWD DOUBLE,
    @Optional
    @ImportColumn("VKWABFAKT_EWD")
    Property<Double> VKWABFAKT_EWD();


    // TODO VKWABFAKT_EWP DOUBLE,
    @Optional
    @ImportColumn("VKWABFAKT_EWP")
    Property<Double> VKWABFAKT_EWP();


    // TODO VKWABFAKT_EWI DOUBLE,
    @Optional
    @ImportColumn("VKWABFAKT_EWI")
    Property<Double> VKWABFAKT_EWI();


    // TODO ABSCHLAG_EWN DOUBLE,
    @Optional
    @ImportColumn("ABSCHLAG_EWN")
    Property<Double> ABSCHLAG_EWN();


    // TODO ZUSCHLAG_EWN DOUBLE,
    @Optional
    @ImportColumn("ZUSCHLAG_EWN")
    Property<Double> ZUSCHLAG_EWN();


    // TODO ABSCHLAG_EWL DOUBLE,
    @Optional
    @ImportColumn("ABSCHLAG_EWL")
    Property<Double> ABSCHLAG_EWL();


    // TODO ZUSCHLAG_EWL DOUBLE,
    @Optional
    @ImportColumn("ZUSCHLAG_EWL")
    Property<Double> ZUSCHLAG_EWL();


    // TODO ABSCHLAG_EWI DOUBLE,
    @Optional
    @ImportColumn("ABSCHLAG_EWI")
    Property<Double> ABSCHLAG_EWI();


    // TODO ZUSCHLAG_EWI DOUBLE,
    @Optional
    @ImportColumn("ZUSCHLAG_EWI")
    Property<Double> ZUSCHLAG_EWI();


    // TODO ABSCHLAG_EWP DOUBLE,
    @Optional
    @ImportColumn("ABSCHLAG_EWP")
    Property<Double> ABSCHLAG_EWP();


    // TODO ZUSCHLAG_EWP DOUBLE,
    @Optional
    @ImportColumn("ZUSCHLAG_EWP")
    Property<Double> ZUSCHLAG_EWP();


    // TODO ABSCHLAG_EWD DOUBLE,
    @Optional
    @ImportColumn("ABSCHLAG_EWD")
    Property<Double> ABSCHLAG_EWD();


    // TODO ZUSCHLAG_EWD DOUBLE
    @Optional
    @ImportColumn("ZUSCHLAG_EWD")
    Property<Double> ZUSCHLAG_EWD();


    @Optional
    // @Computed
    Association<FlurstueckComposite> flurstueck();


    @Optional
    // @Computed
    Association<RichtwertzoneComposite> richtwertZone();


    // );
    //
    // CREATE INDEX K_BEVERW_RIWE ON K_BEVERW (GEMEINDE ASC, RIZONE ASC, RIJAHR ASC);
    //
    // CREATE INDEX Reference10 ON K_BEVERW (GEBART ASC);
    //
    // CREATE UNIQUE INDEX Reference3 ON K_BEVERW (EINGANGSNR ASC);
    //
    // CREATE INDEX K_NUTZK_BEVERW ON K_BEVERW (NUTZUNG ASC);
    //
    // ALTER TABLE K_BEVERW ADD CONSTRAINT PK_K_BEVERW PRIMARY KEY (EINGANGSNR);

    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements FlurstuecksdatenBaulandComposite {

        private static Log log = LogFactory.getLog( Mixin.class );


        public static FlurstuecksdatenBaulandComposite forFlurstueck( FlurstueckComposite flurstueck ) {
            FlurstuecksdatenBaulandComposite template = QueryExpressions
                    .templateFor( FlurstuecksdatenBaulandComposite.class );
            BooleanExpression expr = QueryExpressions.eq( template.flurstueck(), flurstueck );
            return KapsRepository.instance().findEntities( FlurstuecksdatenBaulandComposite.class, expr, 0, 1 ).find();
        }


        // @Override
        // public Association<FlurstueckComposite> flurstueck() {
        // return new ComputedAssociationInstance<FlurstueckComposite>(
        // new GenericAssociationInfo( FlurstuecksdatenBaulandComposite.class,
        // "flurstueck" ) ) {
        //
        // public FlurstueckComposite get() {
        // VertragComposite kaufvertrag = getKaufvertrag();
        // return kaufvertrag != null ? kaufvertrag.hauptFlurstueck().get() : null;
        // }
        //
        //
        // @Override
        // public void set( FlurstueckComposite anIgnoredValue )
        // throws IllegalArgumentException, IllegalStateException {
        // // ignored
        // }
        // };
        // }

        // private VertragComposite kaufvertrag = null;
        //
        //
        // // AssociationCaching
        // protected VertragComposite getKaufvertrag() {
        // if (kaufvertrag == null) {
        // kaufvertrag = kaufvertrag().get();
        // }
        // return kaufvertrag;
        // }

        @Override
        public Association<NutzungComposite> nutzung() {
            return new ComputedAssociationInstance<NutzungComposite>( new GenericAssociationInfo(
                    FlurstuecksdatenBaulandComposite.class, "nutzung" ) ) {

                public NutzungComposite get() {
                    FlurstueckComposite flurstueck = flurstueck().get();
                    return flurstueck != null ? flurstueck.nutzung().get() : null;
                }


                @Override
                public void set( NutzungComposite anIgnoredValue )
                        throws IllegalArgumentException, IllegalStateException {
                    // ignored
                }
            };
        }


        //
        // @Override
        // public Association<GemeindeComposite> gemeinde() {
        // return new ComputedAssociationInstance<GemeindeComposite>( new
        // GenericAssociationInfo(
        // FlurstuecksdatenBaulandComposite.class, "gemeinde" ) ) {
        //
        // public GemeindeComposite get() {
        // RichtwertzoneComposite rz = richtwertZone().get();
        // return rz != null ? rz.gemeinde().get() : null;
        // }
        //
        //
        // @Override
        // public void set( GemeindeComposite anIgnoredValue )
        // throws IllegalArgumentException, IllegalStateException {
        // // ignored
        // }
        // };
        // }

        @Override
        public Association<RichtwertzoneComposite> richtwertZone() {
            return new ComputedAssociationInstance<RichtwertzoneComposite>( new GenericAssociationInfo(
                    FlurstuecksdatenBaulandComposite.class, "richtwertZone" ) ) {

                public RichtwertzoneComposite get() {
                    FlurstueckComposite flurstueck = flurstueck().get();
                    return flurstueck != null ? flurstueck.richtwertZone().get() : null;
                }


                @Override
                public void set( RichtwertzoneComposite anIgnoredValue )
                        throws IllegalArgumentException, IllegalStateException {
                    // ignored
                }
            };
        }


        @Override
        public Association<GebaeudeArtComposite> gebaeudeArt() {
            return new ComputedAssociationInstance<GebaeudeArtComposite>( new GenericAssociationInfo(
                    FlurstuecksdatenBaulandComposite.class, "gebaeudeArt" ) ) {

                public GebaeudeArtComposite get() {
                    FlurstueckComposite flurstueck = flurstueck().get();
                    return flurstueck != null ? flurstueck.gebaeudeArt().get() : null;
                }


                @Override
                public void set( GebaeudeArtComposite anIgnoredValue )
                        throws IllegalArgumentException, IllegalStateException {
                    // ignored
                }
            };
        }


        @Override
        public Property<Double> richtwert() {
            return new ComputedPropertyInstance<Double>( new GenericPropertyInfo(
                    FlurstuecksdatenBaulandComposite.class, "richtwert" ) ) {

                @Override
                public Double get() {
                    RichtwertzoneZeitraumComposite rz = richtwertZoneG().get();
                    return rz != null ? rz.euroQm().get() : null;
                }


                @Override
                public void set( Double anIgnoredValue )
                        throws IllegalArgumentException, IllegalStateException {
                    // ignored
                }
            };
        }


        @Override
        public Property<String> gfzBereich() {
            return new ComputedPropertyInstance<String>( new GenericPropertyInfo(
                    FlurstuecksdatenBaulandComposite.class, "gfzBereich" ) ) {

                @Override
                public String get() {
                    RichtwertzoneComposite rz = flurstueck().get().richtwertZone().get();
                    return rz != null ? rz.gfzBereich().get() : null;
                }


                @Override
                public void set( String anIgnoredValue )
                        throws IllegalArgumentException, IllegalStateException {
                    // ignored
                }
            };
        }
        // @Override
        // public Association<BodennutzungComposite> bodennutzung() {
        // return new ComputedAssociationInstance<BodennutzungComposite>(
        // new GenericAssociationInfo( FlurstuecksdatenBaulandComposite.class,
        // "bodennutzung" ) ) {
        //
        // public BodennutzungComposite get() {
        // RichtwertzoneComposite rz = richtwertZone().get();
        // return rz != null ? rz.bodenNutzung().get() : null;
        // }
        //
        //
        // @Override
        // public void set( BodennutzungComposite anIgnoredValue )
        // throws IllegalArgumentException, IllegalStateException {
        // // ignored
        // }
        // };
        // }
    }

}
