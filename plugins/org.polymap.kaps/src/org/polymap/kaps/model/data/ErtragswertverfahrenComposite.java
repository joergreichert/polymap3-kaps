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
@Mixins({ ErtragswertverfahrenComposite.Mixin.class, PropertyChangeSupport.Mixin.class, ModelChangeSupport.Mixin.class,
        QiEntity.Mixin.class
// JsonState.Mixin.class
})
@ImportTable("K_BEWERTN")
public interface ErtragswertverfahrenComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite {

    final static String NAME = "Ertragswertverfahren";


    // B1 - Double
    @Optional
    @ImportColumn("B1")
    Property<Double> betriebskostenZeile1();


    // B2 - Double
    @Optional
    @ImportColumn("B2")
    Property<Double> betriebskostenZeile2();


    // B3 - Double
    @Optional
    @ImportColumn("B3")
    Property<Double> betriebskostenZeile3();


    // B4 - Double
    @Optional
    @ImportColumn("B4")
    Property<Double> betriebskostenZeile4();


    // B5 - Double
    @Optional
    @ImportColumn("B5")
    Property<Double> betriebskostenZeile5();


    // B6 - Double
    @Optional
    @ImportColumn("B6")
    Property<Double> betriebskostenZeile6();


    // B7 - Double
    @Optional
    @ImportColumn("B7")
    Property<Double> betriebskostenZeile7();


    // T1 - String
    @Optional
    @ImportColumn("T1")
    Property<String> betriebskostenTextZeile9();


    // B8 - Double
    @Optional
    @ImportColumn("B8")
    Property<Double> betriebskostenZeile8();


    // T2 - String
    @Optional
    @ImportColumn("T2")
    Property<String> betriebskostenTextZeile10();


    // B9 - Double
    @Optional
    @ImportColumn("B9")
    Property<Double> betriebskostenZeile9();


    // T3 - String
    @Optional
    @ImportColumn("T3")
    Property<String> betriebskostenTextZeile11();


    // B10 - Double
    @Optional
    @ImportColumn("B10")
    Property<Double> betriebskostenZeile10();


    // B11 - Double
    @Optional
    @ImportColumn("B11")
    Property<Double> betriebskostenZeile11();


    // MB - Double
    @Optional
    @ImportColumn("MB")
    Property<Double> betriebskostenSummeMonatlich();


    // G1 - String
    @Optional
    Association<EtageComposite> etageZeile1();


    // F1 - Double
    @Optional
    @ImportColumn("F1")
    Property<Double> flaecheZeile1();


    // Q1 - Double
    @Optional
    @ImportColumn("Q1")
    Property<Double> mieteQmZeile1();


    // N1 - Double
    @Optional
    @ImportColumn("N1")
    Property<Double> miete1();


    // G2 - String
    @Optional
    Association<EtageComposite> etageZeile2();


    // F2 - Double
    @Optional
    @ImportColumn("F2")
    Property<Double> flaecheZeile2();


    // Q2 - Double
    @Optional
    @ImportColumn("Q2")
    Property<Double> mieteQmZeile2();


    // N2 - Double
    @Optional
    @ImportColumn("N2")
    Property<Double> miete2();


    // S1 - Double
    @Optional
    @ImportColumn("S1")
    Property<Double> miete16();


    // S2 - Double
    @Optional
    @ImportColumn("S2")
    Property<Double> miete17();


    // S3 - Double
    @Optional
    @ImportColumn("S3")
    Property<Double> miete18();


    //
    //
    // // SN - Double
    // @Optional
    // @ImportColumn("SN")
    // Property<Double> summeMiete();
    //
    //
    // // TODO SAB - Double
    // @Optional
    // @ImportColumn("SAB")
    // Property<Double> SAB();

    // SBM - Double
    @Optional
    @ImportColumn("SBM")
    Property<Double> nettoRohertragProMonat();


    @Optional
    Property<Double> nettoRohertragProJahr();


    // SBMG - Double
    @Optional
    @ImportColumn("SBMG")
    Property<Double> bruttoRohertragProMonat();


    @Optional
    Property<Double> bruttoRohertragProJahr();


    // JB - Double
    @Optional
    @ImportColumn("JBetriebskosten")
    Property<Double> jahresBetriebskosten();

    @Optional
    Property<Double> jahresBetriebskostenPauschal();

    @Optional
    @ImportColumn("JB")
    Property<Double> jahresBetriebskostenE();


    @Optional
    // betriebskosten nochmal in Bewirtschaftungskosten
    @ImportColumn("JB")
    Property<Double> anteiligeBetriebskosten();


    // JV - Double
    @Optional
    @ImportColumn("JV")
    Property<Double> verwaltungskosten();


    // JI - Double
    @Optional
    @ImportColumn("JI")
    Property<Double> instandhaltungskosten();


    // JM - Double
    @Optional
    @ImportColumn("JM")
    Property<Double> mietausfallWagnis();


    // JSB - Double
    @Optional
    @ImportColumn("JSB")
    Property<Double> summeBewirtschaftungskosten();
    
    @Optional
    Property<Double> bewirtschaftungskostenPauschal();

    // JSP - Double
    @Optional
    @ImportColumn("JSP")
    Property<Double> bewirtschaftungskostenInProzentDesJahresRohertrags();


    // JRE - Double
    @Optional
    @ImportColumn("JRE")
    Property<Double> jahresReinErtrag();


    // BWANT - Double
    @Optional
    @ImportColumn("BWANT")
    Property<Double> bodenwertAnteil();


    // BWZ - Double
    @Optional
    @ImportColumn("BWZ")
    Property<Double> bodenwertAnteilLiegenschaftsZins();


    // BWVZ - Double
    @Optional
    @ImportColumn("BWVZ")
    Property<Double> bodenwertAnteilLiegenschaftsZinsBetrag();


    // REANT - Double
    @Optional
    @ImportColumn("REANT")
    Property<Double> anteilDerBaulichenAnlagenAmJahresreinertrag();


    // RN - Double
    @Optional
    @ImportColumn("RN")
    Property<Double> restnutzungsDauer();


    // VERV - Double
    @Optional
    @ImportColumn("VERV")
    Property<Double> vervielvaeltiger();


    // EWB - Double
    @Optional
    @ImportColumn("EWB")
    Property<Double> ertragswertDerBaulichenAnlagen();


    // WEU - Double
    @Optional
    @ImportColumn("WEU")
    Property<Double> wertbeeinflussendeUmstaende();


    // BW - Double
    @Optional
    @ImportColumn("BW")
    Property<Double> bodenwert();


    // EWERT - Double
    @Optional
    @ImportColumn("EWERT")
    Property<Double> ertragswert();


    // EWBB - Double
    @Optional
    @ImportColumn("EWBB")
    Property<Double> ertragswertDerBaulichenAnlagenZwischensumme();


    // BERBAUJ - Integer
    @Optional
    Property<Double> bereinigtesBaujahr();


    // G3 - String
    @Optional
    Association<EtageComposite> etageZeile3();


    // F3 - Double
    @Optional
    @ImportColumn("F3")
    Property<Double> flaecheZeile3();


    // Q3 - Double
    @Optional
    @ImportColumn("Q3")
    Property<Double> mieteQmZeile3();


    // N3 - Double
    @Optional
    @ImportColumn("N3")
    Property<Double> miete3();


    // JT1 - String
    @Optional
    @ImportColumn("JT1")
    Property<String> ertraegeZeile16();


    // JT2 - String
    @Optional
    @ImportColumn("JT2")
    Property<String> ertraegeZeile17();


    // JT3 - String
    @Optional
    @ImportColumn("JT3")
    Property<String> ertraegeZeile18();


    // G4 - String
    @Optional
    Association<EtageComposite> etageZeile4();


    // F4 - Double
    @Optional
    @ImportColumn("F4")
    Property<Double> flaecheZeile4();


    // Q4 - Double
    @Optional
    @ImportColumn("Q4")
    Property<Double> mieteQmZeile4();


    // G5 - String
    @Optional
    Association<EtageComposite> etageZeile5();


    // F5 - Double
    @Optional
    @ImportColumn("F5")
    Property<Double> flaecheZeile5();


    // Q5 - Double
    @Optional
    @ImportColumn("Q5")
    Property<Double> mieteQmZeile5();


    // G6 - String
    @Optional
    Association<EtageComposite> etageZeile6();


    // F6 - Double
    @Optional
    @ImportColumn("F6")
    Property<Double> flaecheZeile6();


    // Q6 - Double
    @Optional
    @ImportColumn("Q6")
    Property<Double> mieteQmZeile6();


    // G7 - String
    @Optional
    Association<EtageComposite> etageZeile7();


    // F7 - Double
    @Optional
    @ImportColumn("F7")
    Property<Double> flaecheZeile7();


    // Q7 - Double
    @Optional
    @ImportColumn("Q7")
    Property<Double> mieteQmZeile7();


    // N4 - Double
    @Optional
    @ImportColumn("N4")
    Property<Double> miete4();


    // N5 - Double
    @Optional
    @ImportColumn("N5")
    Property<Double> miete5();


    // N6 - Double
    @Optional
    @ImportColumn("N6")
    Property<Double> miete6();


    // N7 - Double
    @Optional
    @ImportColumn("N7")
    Property<Double> miete7();

    @Optional
    Association<EtageComposite> etageZeile8();

    @Optional
    Property<Double> flaecheZeile8();

    @Optional
    Property<Boolean> wohnflaecheZeile8();

    @Optional
    Property<Double> mieteQmZeile8();

    @Optional
    Property<Double> miete8();

    @Optional
    Association<EtageComposite> etageZeile9();

    @Optional
    Property<Double> flaecheZeile9();

    @Optional
    Property<Boolean> wohnflaecheZeile9();

    @Optional
    Property<Double> mieteQmZeile9();

    @Optional
    Property<Double> miete9();

    @Optional
    Association<EtageComposite> etageZeile10();

    @Optional
    Property<Double> flaecheZeile10();

    @Optional
    Property<Boolean> wohnflaecheZeile10();

    @Optional
    Property<Double> mieteQmZeile10();

    @Optional
    Property<Double> miete10();

    @Optional
    Association<EtageComposite> etageZeile11();

    @Optional
    Property<Double> flaecheZeile11();

    @Optional
    Property<Boolean> wohnflaecheZeile11();

    @Optional
    Property<Double> mieteQmZeile11();

    @Optional
    Property<Double> miete11();

    @Optional
    Association<EtageComposite> etageZeile12();

    @Optional
    Property<Double> flaecheZeile12();

    @Optional
    Property<Boolean> wohnflaecheZeile12();

    @Optional
    Property<Double> mieteQmZeile12();

    @Optional
    Property<Double> miete12();
    
    @Optional
    Association<EtageComposite> etageZeile13();

    @Optional
    Property<Double> flaecheZeile13();

    @Optional
    Property<Boolean> wohnflaecheZeile13();

    @Optional
    Property<Double> mieteQmZeile13();

    @Optional
    Property<Double> miete13();
    
    @Optional
    Association<EtageComposite> etageZeile14();

    @Optional
    Property<Double> flaecheZeile14();

    @Optional
    Property<Boolean> wohnflaecheZeile14();

    @Optional
    Property<Double> mieteQmZeile14();

    @Optional
    Property<Double> miete14();
    
    @Optional
    Association<EtageComposite> etageZeile15();

    @Optional
    Property<Double> flaecheZeile15();

    @Optional
    Property<Boolean> wohnflaecheZeile15();

    @Optional
    Property<Double> mieteQmZeile15();

    @Optional
    Property<Double> miete15();
    
    // // TODO BMG - Double
    // @Optional
    // @ImportColumn("BMG")
    // Property<Double> BMG();

    // JXT - String
    @Optional
    @ImportColumn("JXT")
    Property<String> bewirtschaftungskostenZeile5Text();


    // JX - Double
    @Optional
    @ImportColumn("JX")
    Property<Double> bewirtschaftungskostenZeile5();


    // EINGANGSNR - Double
    @Optional
    Association<VertragComposite> vertrag();


    // GN - Double
    @Optional
    @ImportColumn("GN")
    Property<Double> gesamtNutzungsDauer();


    // pauschal - String
    @Optional
    // @ImportColumn("pauschal")
    Property<Boolean> pauschalBetriebskosten();

    @Optional
    Property<Boolean> betriebskostenInProzentDesJahresRohertragsErfassen();
    

    // pauschalbew - String
    @Optional
    // @ImportColumn("pauschalbew")
    Property<Boolean> pauschalBewirtschaftungskosten();


    // Prozentertrag - Double
    @Optional
    @ImportColumn("Prozentertrag")
    Property<Double> betriebskostenInProzentDesJahresRohertrags();


    // LIEZINS - Double
    @Optional
    @ImportColumn("LIEZINS")
    Property<Double> liegenschaftsZins();


    // LIZI - String
    @Optional
    // @ImportColumn("LIZI")
    Property<Boolean> liziVerwenden();


    // INNEN - String
    @Optional
    // @ImportColumn("INNEN")
    Property<Boolean> innenBereich();


    // GEWART - String
    @Optional
    @ImportColumn("GEWART")
    Property<String> artDesGewerbes();


    // GEWANT - Double
    @Optional
    @ImportColumn("GEWANT")
    Property<Double> anteilDesGewerbes();


    // GRART - Double
    @Optional
    @ImportColumn("GRART")
    Property<Double> grundstuecksArt();


    //
    // // TODO BETRPAUSCH - String
    // @Optional
    // @ImportColumn("BETRPAUSCH")
    // Property<String> BETRPAUSCH();

    // ANGABEPROZ - String
    @Optional
    // @ImportColumn("ANGABEPROZ")
    Property<Boolean> bewirtschaftungskostenInProzentDesJahresRohertragsErfassen();


    @Optional
    @ImportColumn("JBP")
    Property<Double> bewirtschaftungskostenInProzentDesJahresRohertrages();


    // GEWICHT - Long
    @Optional
    // @ImportColumn("GEWICHT")
    Property<Double> gewichtungLiegenschaftszins();


    // BAUJAHR - Long
    @Optional
    // @ImportColumn("BAUJAHR")
    Property<Double> tatsaechlichesBaujahr();


    // BWANT_IND - String
    @Optional
    Property<Boolean> bodenwertAnteilIndividuell();


    // GESMIETE_EING - String
    @Optional
    Property<Boolean> eingabeGesamtMiete();


    // WBU_BEZ - String
    @Optional
    @ImportColumn("WBU_BEZ")
    Property<String> wertbeeinflussendeUmstaendeText();


    // FREILEGUNG - Double
    @Optional
    @ImportColumn("FREILEGUNG")
    Property<Double> freilegung();


    // WOHNFL1 - String
    @Optional
    Property<Boolean> wohnflaecheZeile1();


    // WOHNFL2 - String
    @Optional
    Property<Boolean> wohnflaecheZeile2();


    // WOHNFL3 - String
    @Optional
    Property<Boolean> wohnflaecheZeile3();


    // WOHNFL4 - String
    @Optional
    Property<Boolean> wohnflaecheZeile4();


    // WOHNFL5 - String
    @Optional
    Property<Boolean> wohnflaecheZeile5();


    // WOHNFL6 - String
    @Optional
    Property<Boolean> wohnflaecheZeile6();


    // WOHNFL7 - String
    @Optional
    Property<Boolean> wohnflaecheZeile7();


    @Optional
    Property<Double> bereinigterKaufpreis();


    public static abstract class Mixin
            implements ErtragswertverfahrenComposite {

        private static Log log = LogFactory.getLog( Mixin.class );


        public static ErtragswertverfahrenComposite forVertrag( VertragComposite vertrag ) {
            ErtragswertverfahrenComposite template = QueryExpressions.templateFor( ErtragswertverfahrenComposite.class );
            BooleanExpression expr = QueryExpressions.eq( template.vertrag(), vertrag );
            return KapsRepository.instance().findEntities( ErtragswertverfahrenComposite.class, expr, 0, 1 ).find();
        }
    }


    @Optional
    Property<String> bemerkungen();


    @Optional
    Property<Double> bodenwertAbzglFreilegung();
}
