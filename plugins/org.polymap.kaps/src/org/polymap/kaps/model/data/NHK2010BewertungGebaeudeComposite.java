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
import org.polymap.kaps.model.SchlNamed;

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
@Concerns({ PropertyChangeSupport.Concern.class })
@Mixins({ NHK2010BewertungGebaeudeComposite.Mixin.class, PropertyChangeSupport.Mixin.class,
        ModelChangeSupport.Mixin.class, QiEntity.Mixin.class
// JsonState.Mixin.class
})
@ImportTable("K_BEWERTBGF10")
public interface NHK2010BewertungGebaeudeComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite, SchlNamed {

    @Optional
    Association<NHK2010BewertungComposite> bewertung();


    @Optional
    @ImportColumn("LFDNR")
    Property<Long> laufendeNummer();


    @Optional
    @ImportColumn("OBJEKTNR")
    Property<Integer> objektNummer();


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


    // // TODO ANTRAGNR_GUTACHTEN - String
    // @Optional
    // @ImportColumn("ANTRAGNR_GUTACHTEN")
    // Property<String> ANTRAGNR_GUTACHTEN();
    //
    //
    // // TODO GUTACHTNR_GUTACHTEN - String
    // @Optional
    // @ImportColumn("GUTACHTNR_GUTACHTEN")
    // Property<String> GUTACHTNR_GUTACHTEN();
    //
    //
    // // TODO VERKJAHR - Long
    // @Optional
    // @ImportColumn("VERKJAHR")
    // Property<Long> VERKJAHR();

    // // TODO HAUPTNR - Long
    // // TODO beim Import zusammenlegen
    // @Optional
    // @ImportColumn("HAUPTNR")
    // Property<Long> HAUPTNR();
    //
    //
    // // TODO NR - Long
    // @Optional
    // @ImportColumn("NR")
    // Property<Long> NR();
    //
    //
    // // TODO UNTERNR - Long
    // @Optional
    // @ImportColumn("UNTERNR")
    // Property<Long> UNTERNR();
    //
    // @Optional
    // Property<String> gebaeudeArt();

    //
    // // TODO BERUECK - String
    // @Optional
    // @ImportColumn("BERUECK")
    // Property<String> BERUECK();
    //
    //
    // // TODO BGF_WOHNFL_NHK - Double
    // @Optional
    // @ImportColumn("BGF_WOHNFL_NHK")
    // Property<Double> BGF_WOHNFL_NHK();
    //
    //
    // // TODO BGF_WOHNFL_TATS - Double
    // @Optional
    // @ImportColumn("BGF_WOHNFL_TATS")
    // Property<Double> BGF_WOHNFL_TATS();
    //
    //
    // // TODO BRI_BGF_NHK - Double
    // @Optional
    // @ImportColumn("BRI_BGF_NHK")
    // Property<Double> BRI_BGF_NHK();
    //
    //
    // // TODO BRI_BGF_TATS - Double
    // @Optional
    // @ImportColumn("BRI_BGF_TATS")
    // Property<Double> BRI_BGF_TATS();
    //
    //
    // // TODO BGF_NUTZ_NHK - Double
    // @Optional
    // @ImportColumn("BGF_NUTZ_NHK")
    // Property<Double> BGF_NUTZ_NHK();
    //
    //
    // // TODO BGF_NUTZ_TATS - Double
    // @Optional
    // @ImportColumn("BGF_NUTZ_TATS")
    // Property<Double> BGF_NUTZ_TATS();
    //
    //
    // // TODO DREMPEL - Double
    // @Optional
    // @ImportColumn("DREMPEL")
    // Property<Double> DREMPEL();
    //
    //
    // // TODO SPITZBODEN - Double
    // @Optional
    // @ImportColumn("SPITZBODEN")
    // Property<Double> SPITZBODEN();
    //
    //
    // // TODO BKIND - Double
    // @Optional
    // @ImportColumn("BKIND")
    // Property<Double> BKIND();
    //
    //
    // // TODO AUSSTATT1 - String
    // @Optional
    // @ImportColumn("AUSSTATT1")
    // Property<String> AUSSTATT1();

    // BERBAUJ1 - Long
    @Optional
    @ImportColumn("BERBAUJ1")
    Property<Long> bereinigtesBaujahr();


    // BAUJ1 - Long
    @Optional
    @ImportColumn("BAUJ1")
    Property<Long> tatsaechlichesBaujahr();


    // BGF1 - Double
    @Optional
    @ImportColumn("BGF1")
    Property<Double> bruttoGrundFlaeche();


    // NHK1_NHK - Double
    @Optional
    @ImportColumn("NHK1_NHK")
    Property<Double> nhk();


    // NHK1_KORR - Double
    @Optional
    @ImportColumn("NHK1_KORR")
    Property<Double> nhkKorrigiert();


    // NHWERT - Double
    @Optional
    @ImportColumn("NHWERT")
    Property<Double> normalHerstellungsWert();


    // NEUWERT1 - Double
    @Optional
    @ImportColumn("NEUWERT1")
    Property<Double> neuWert();


    // ZEITWERT1 - Double
    @Optional
    @ImportColumn("ZEITWERT1")
    Property<Double> gebaeudeZeitWert();


    //
    // // TODO BERZEITW1 - Double
    // @Optional
    // @ImportColumn("BERZEITW1")
    // Property<Double> BERZEITW1();

    // GND - Long
    @Optional
    @ImportColumn("GND")
    Property<Long> gesamtNutzungsDauer();


    // RND - Long
    @Optional
    @ImportColumn("RND")
    Property<Long> restNutzungsDauer();


    // ALTER1 - Long
    @Optional
    @ImportColumn("ALTER1")
    Property<Long> alter();


    // WOHNGEB - String
    @Optional
    @ImportColumn("WOHNGEB")
    Property<String> baukostenIndexTyp();


    @Optional
    Property<Double> baukostenIndexWert();


    // GEBNR1 - String
    @Optional
    // @ImportColumn("GEBNR1") wird aus Nummer/Unternummer berechnet
    Property<String> gebaeudeArtId();


    // ABSCHL_BM_PROZ - Double
    @Optional
    @ImportColumn("ABSCHL_BM_PROZ")
    Property<Double> abschlagBaumaengelProzent();


    // ABSCHL_BM_BETR - Double
    @Optional
    @ImportColumn("ABSCHL_BM_BETR")
    Property<Double> abschlagBaumaengelBetrag();


    // TODO BOOL ABSCHL_BM_KZ - String
    @Optional
    @ImportColumn("ABSCHL_BM_KZ")
    Property<String> abschlagBaumaengelInProzent();


    // ZUABSCHL_PROZ - Double
    @Optional
    @ImportColumn("ZUABSCHL_PROZ")
    Property<Double> abschlagRueckstauProzent();


    // ZUABSCHL_BETR - Double
    @Optional
    @ImportColumn("ZUABSCHL_BETR")
    Property<Double> abschlagRueckstauBetrag();


    // TODO BOOL ZUABSCHL_KZ - String
    @Optional
    @ImportColumn("ZUABSCHL_KZ")
    Property<String> abschlagRueckstauInProzent();


    // FAMHAUS2 - String
    @Optional
    // @ImportColumn("FAMHAUS2")
    Property<Boolean> zweifamilienHaus();


    // GRUNDRISSART - String
    @Optional
    @ImportColumn("GRUNDRISSART")
    Property<String> grundrissArt();


    // ANZZIMMER - Long
    @Optional
    // @ImportColumn("ANZZIMMER")
    Property<Double> anzahlWohnungen();


    @Optional
    Property<Double> faktorGrundrissart();


    @Optional
    Property<Double> faktorWohnungsgroesse();


    @Optional
    Property<Double> faktorZweifamilienhaus();


    //
    // // TODO DACHGEOMETRIE - Double
    // @Optional
    // @ImportColumn("DACHGEOMETRIE")
    // Property<Double> DACHGEOMETRIE();

    // ZUABSCHL2_PROZ - Double
    @Optional
    @ImportColumn("ZUABSCHL2_PROZ")
    Property<Double> zuschlagZeile3Prozent();


    // ZUABSCHL2_BETR - Double
    @Optional
    @ImportColumn("ZUABSCHL2_BETR")
    Property<Double> zuschlagZeile3Betrag();


    // TODO BOOL ZUABSCHL2_KZ - String
    @Optional
    @ImportColumn("ZUABSCHL2_KZ")
    Property<String> zuschlagZeile3InProzent();


    // ZUABSCHL2_BEZ - String
    @Optional
    @ImportColumn("ZUABSCHL2_BEZ")
    Property<String> zuschlagZeile3Bezeichnung();


    // ZUABSCHL3_PROZ - Double
    @Optional
    @ImportColumn("ZUABSCHL3_PROZ")
    Property<Double> zuschlagZeile4Prozent();


    // ZUABSCHL3_BETR - Double
    @Optional
    @ImportColumn("ZUABSCHL3_BETR")
    Property<Double> zuschlagZeile4Betrag();


    // TODO BOOL ZUABSCHL3_KZ - String
    @Optional
    @ImportColumn("ZUABSCHL3_KZ")
    Property<String> zuschlagZeile4InProzent();


    // ZUABSCHL3_BEZ - String
    @Optional
    @ImportColumn("ZUABSCHL3_BEZ")
    Property<String> zuschlagZeile4Bezeichnung();


    //
    //
    // // GND_BERECHNET - Double
    // @Optional
    // @ImportColumn("GND_BERECHNET")
    // Property<Double> GND_BERECHNET();

    // AUSSTATT2 - String
    @Optional
    @ImportColumn("AUSSTATT2")
    Property<String> gebaeudeStandard();



    @Optional
    Property<Double> altersWertMinderung();


    @Optional
    Property<Double> zeitwertRnd();


    //
    // // TODO GFAKTOR1 - Double
    // @Optional
    // @ImportColumn("GFAKTOR1")
    // Property<Double> GFAKTOR1();
    //
    //
    // // TODO GWERT1 - Double
    // @Optional
    // @ImportColumn("GWERT1")
    // Property<Double> GWERT1();
    //
    //
    // // TODO GFAKTOR2 - Double
    // @Optional
    // @ImportColumn("GFAKTOR2")
    // Property<Double> GFAKTOR2();
    //
    //
    // // TODO GWERT2 - Double
    // @Optional
    // @ImportColumn("GWERT2")
    // Property<Double> GWERT2();

    //
    // // TODO SUMBGF - Double
    // @Optional
    // @ImportColumn("SUMBGF")
    // Property<Double> SUMBGF();
    //
    //
    // // TODO SUMWOHNFL - Double
    // @Optional
    // @ImportColumn("SUMWOHNFL")
    // Property<Double> SUMWOHNFL();
    //
    //
    // // TODO MAKLERBW - String
    // @Optional
    // @ImportColumn("MAKLERBW")
    // Property<String> MAKLERBW();
    //
    //
    // // TODO GEBNR1_2000 - String
    // @Optional
    // @ImportColumn("GEBNR1_2000")
    // Property<String> GEBNR1_2000();

    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements NHK2010BewertungGebaeudeComposite {

        private static Log log = LogFactory.getLog( Mixin.class );


        public static Iterable<NHK2010BewertungGebaeudeComposite> forBewertung( NHK2010BewertungComposite bewertung ) {
            NHK2010BewertungGebaeudeComposite template = QueryExpressions
                    .templateFor( NHK2010BewertungGebaeudeComposite.class );
            BooleanExpression expr = QueryExpressions.eq( template.bewertung(), bewertung );
            Query<NHK2010BewertungGebaeudeComposite> matches = KapsRepository.instance().findEntities(
                    NHK2010BewertungGebaeudeComposite.class, expr, 0, -1 );
            return matches;
        }
    }
}
