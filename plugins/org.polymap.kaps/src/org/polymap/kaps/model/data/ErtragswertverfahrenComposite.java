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
import org.qi4j.api.mixin.Mixins;
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
@Mixins({ ErtragswertverfahrenComposite.Mixin.class, PropertyChangeSupport.Mixin.class, ModelChangeSupport.Mixin.class,
        QiEntity.Mixin.class
// JsonState.Mixin.class
})
@ImportTable("K_BEWERTN")
public interface ErtragswertverfahrenComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite {

    final static String NAME = "Ertragswertverfahren";


    // TODO B1 - Double
    @Optional
    @ImportColumn("B1")
    Property<Double> B1();


    // TODO B2 - Double
    @Optional
    @ImportColumn("B2")
    Property<Double> B2();


    // TODO B3 - Double
    @Optional
    @ImportColumn("B3")
    Property<Double> B3();


    // TODO B4 - Double
    @Optional
    @ImportColumn("B4")
    Property<Double> B4();


    // TODO B5 - Double
    @Optional
    @ImportColumn("B5")
    Property<Double> B5();


    // TODO B6 - Double
    @Optional
    @ImportColumn("B6")
    Property<Double> B6();


    // TODO B7 - Double
    @Optional
    @ImportColumn("B7")
    Property<Double> B7();


    // TODO T1 - String
    @Optional
    @ImportColumn("T1")
    Property<String> T1();


    // TODO B8 - Double
    @Optional
    @ImportColumn("B8")
    Property<Double> B8();


    // TODO T2 - String
    @Optional
    @ImportColumn("T2")
    Property<String> T2();


    // TODO B9 - Double
    @Optional
    @ImportColumn("B9")
    Property<Double> B9();


    // TODO T3 - String
    @Optional
    @ImportColumn("T3")
    Property<String> T3();


    // TODO B10 - Double
    @Optional
    @ImportColumn("B10")
    Property<Double> B10();


    // TODO SUB - Double
    @Optional
    @ImportColumn("SUB")
    Property<Double> SUB();


    // TODO MB - Double
    @Optional
    @ImportColumn("MB")
    Property<Double> MB();


    // TODO G1 - String
    @Optional
    @ImportColumn("G1")
    Property<String> G1();


    // TODO GJAHR1 - Integer
    @Optional
    @ImportColumn("GJAHR1")
    Property<Integer> GJAHR1();


    // TODO F1 - Double
    @Optional
    @ImportColumn("F1")
    Property<Double> F1();


    // TODO Q1 - Double
    @Optional
    @ImportColumn("Q1")
    Property<Double> Q1();


    // TODO N1 - Double
    @Optional
    @ImportColumn("N1")
    Property<Double> N1();


    // TODO G2 - String
    @Optional
    @ImportColumn("G2")
    Property<String> G2();


    // TODO GJAHR2 - Integer
    @Optional
    @ImportColumn("GJAHR2")
    Property<Integer> GJAHR2();


    // TODO F2 - Double
    @Optional
    @ImportColumn("F2")
    Property<Double> F2();


    // TODO Q2 - Double
    @Optional
    @ImportColumn("Q2")
    Property<Double> Q2();


    // TODO N2 - Double
    @Optional
    @ImportColumn("N2")
    Property<Double> N2();


    // TODO S1 - Double
    @Optional
    @ImportColumn("S1")
    Property<Double> S1();


    // TODO S2 - Double
    @Optional
    @ImportColumn("S2")
    Property<Double> S2();


    // TODO S3 - Double
    @Optional
    @ImportColumn("S3")
    Property<Double> S3();


    // TODO SN - Double
    @Optional
    @ImportColumn("SN")
    Property<Double> SN();


    // TODO SAB - Double
    @Optional
    @ImportColumn("SAB")
    Property<Double> SAB();


    // TODO SBM - Double
    @Optional
    @ImportColumn("SBM")
    Property<Double> SBM();


    // TODO SBMG - Double
    @Optional
    @ImportColumn("SBMG")
    Property<Double> SBMG();


    // TODO JB - Double
    @Optional
    @ImportColumn("JB")
    Property<Double> JB();


    // TODO JBP - Double
    @Optional
    @ImportColumn("JBP")
    Property<Double> JBP();


    // TODO JV - Double
    @Optional
    @ImportColumn("JV")
    Property<Double> JV();


    // TODO JVP - Double
    @Optional
    @ImportColumn("JVP")
    Property<Double> JVP();


    // TODO JI - Double
    @Optional
    @ImportColumn("JI")
    Property<Double> JI();


    // TODO JIP - Double
    @Optional
    @ImportColumn("JIP")
    Property<Double> JIP();


    // TODO JM - Double
    @Optional
    @ImportColumn("JM")
    Property<Double> JM();


    // TODO JMP - Double
    @Optional
    @ImportColumn("JMP")
    Property<Double> JMP();


    // TODO JSB - Double
    @Optional
    @ImportColumn("JSB")
    Property<Double> JSB();


    // TODO JSP - Double
    @Optional
    @ImportColumn("JSP")
    Property<Double> JSP();


    // TODO JRE - Double
    @Optional
    @ImportColumn("JRE")
    Property<Double> JRE();


    // TODO BWANT - Double
    @Optional
    @ImportColumn("BWANT")
    Property<Double> BWANT();


    // TODO BWZ - Double
    @Optional
    @ImportColumn("BWZ")
    Property<Double> BWZ();


    // TODO BWVZ - Double
    @Optional
    @ImportColumn("BWVZ")
    Property<Double> BWVZ();


    // TODO REANT - Double
    @Optional
    @ImportColumn("REANT")
    Property<Double> REANT();


    // TODO RN - Double
    @Optional
    @ImportColumn("RN")
    Property<Double> RN();


    // TODO VERV - Double
    @Optional
    @ImportColumn("VERV")
    Property<Double> VERV();


    // TODO EWB - Double
    @Optional
    @ImportColumn("EWB")
    Property<Double> EWB();


    // TODO WEU - Double
    @Optional
    @ImportColumn("WEU")
    Property<Double> WEU();


    // TODO BW - Double
    @Optional
    @ImportColumn("BW")
    Property<Double> BW();


    // TODO EWERT - Double
    @Optional
    @ImportColumn("EWERT")
    Property<Double> EWERT();


    // TODO EWBB - Double
    @Optional
    @ImportColumn("EWBB")
    Property<Double> EWBB();


    // TODO BERBAUJ - Integer
    @Optional
    @ImportColumn("BERBAUJ")
    Property<Integer> BERBAUJ();


    // TODO G3 - String
    @Optional
    @ImportColumn("G3")
    Property<String> G3();


    // TODO GJAHR3 - Integer
    @Optional
    @ImportColumn("GJAHR3")
    Property<Integer> GJAHR3();


    // TODO F3 - Double
    @Optional
    @ImportColumn("F3")
    Property<Double> F3();


    // TODO Q3 - Double
    @Optional
    @ImportColumn("Q3")
    Property<Double> Q3();


    // TODO N3 - Double
    @Optional
    @ImportColumn("N3")
    Property<Double> N3();


    // TODO JT1 - String
    @Optional
    @ImportColumn("JT1")
    Property<String> JT1();


    // TODO JT2 - String
    @Optional
    @ImportColumn("JT2")
    Property<String> JT2();


    // TODO JT3 - String
    @Optional
    @ImportColumn("JT3")
    Property<String> JT3();


    // TODO G4 - String
    @Optional
    @ImportColumn("G4")
    Property<String> G4();


    // TODO GJAHR4 - Integer
    @Optional
    @ImportColumn("GJAHR4")
    Property<Integer> GJAHR4();


    // TODO F4 - Double
    @Optional
    @ImportColumn("F4")
    Property<Double> F4();


    // TODO Q4 - Double
    @Optional
    @ImportColumn("Q4")
    Property<Double> Q4();


    // TODO G5 - String
    @Optional
    @ImportColumn("G5")
    Property<String> G5();


    // TODO GJAHR5 - Integer
    @Optional
    @ImportColumn("GJAHR5")
    Property<Integer> GJAHR5();


    // TODO F5 - Double
    @Optional
    @ImportColumn("F5")
    Property<Double> F5();


    // TODO Q5 - Double
    @Optional
    @ImportColumn("Q5")
    Property<Double> Q5();


    // TODO G6 - String
    @Optional
    @ImportColumn("G6")
    Property<String> G6();


    // TODO GJAHR6 - Integer
    @Optional
    @ImportColumn("GJAHR6")
    Property<Integer> GJAHR6();


    // TODO F6 - Double
    @Optional
    @ImportColumn("F6")
    Property<Double> F6();


    // TODO Q6 - Double
    @Optional
    @ImportColumn("Q6")
    Property<Double> Q6();


    // TODO G7 - String
    @Optional
    @ImportColumn("G7")
    Property<String> G7();


    // TODO GJAHR7 - Integer
    @Optional
    @ImportColumn("GJAHR7")
    Property<Integer> GJAHR7();


    // TODO F7 - Double
    @Optional
    @ImportColumn("F7")
    Property<Double> F7();


    // TODO Q7 - Double
    @Optional
    @ImportColumn("Q7")
    Property<Double> Q7();


    // TODO N4 - Double
    @Optional
    @ImportColumn("N4")
    Property<Double> N4();


    // TODO N5 - Double
    @Optional
    @ImportColumn("N5")
    Property<Double> N5();


    // TODO N6 - Double
    @Optional
    @ImportColumn("N6")
    Property<Double> N6();


    // TODO N7 - Double
    @Optional
    @ImportColumn("N7")
    Property<Double> N7();


    // TODO BMG - Double
    @Optional
    @ImportColumn("BMG")
    Property<Double> BMG();


    // TODO JXT - String
    @Optional
    @ImportColumn("JXT")
    Property<String> JXT();


    // TODO JX - Double
    @Optional
    @ImportColumn("JX")
    Property<Double> JX();


    // TODO JXP - Double
    @Optional
    @ImportColumn("JXP")
    Property<Double> JXP();


    // TODO EINGANGSNR - Double
    @Optional
    @ImportColumn("EINGANGSNR")
    Property<Double> EINGANGSNR();


    // TODO B11 - Double
    @Optional
    @ImportColumn("B11")
    Property<Double> B11();


    // TODO GN - Double
    @Optional
    @ImportColumn("GN")
    Property<Double> GN();


    // TODO pauschal - String
    @Optional
    @ImportColumn("pauschal")
    Property<String> pauschal();


    // TODO pauschalbew - String
    @Optional
    @ImportColumn("pauschalbew")
    Property<String> pauschalbew();


    // TODO DMEURO - String
    @Optional
    @ImportColumn("DMEURO")
    Property<String> DMEURO();


    // TODO Prozentertrag - Double
    @Optional
    @ImportColumn("Prozentertrag")
    Property<Double> Prozentertrag();


    // TODO JBetriebskosten - Double
    @Optional
    @ImportColumn("JBetriebskosten")
    Property<Double> JBetriebskosten();


    // TODO LIEZINS - Double
    @Optional
    @ImportColumn("LIEZINS")
    Property<Double> LIEZINS();


    // TODO LIZI - String
    @Optional
    @ImportColumn("LIZI")
    Property<String> LIZI();


    // TODO INNEN - String
    @Optional
    @ImportColumn("INNEN")
    Property<String> INNEN();


    // TODO GEWART - String
    @Optional
    @ImportColumn("GEWART")
    Property<String> GEWART();


    // TODO GEWANT - Double
    @Optional
    @ImportColumn("GEWANT")
    Property<Double> GEWANT();


    // TODO GRART - Double
    @Optional
    @ImportColumn("GRART")
    Property<Double> GRART();


    // TODO EURO_UMSTELL - String
    @Optional
    @ImportColumn("EURO_UMSTELL")
    Property<String> EURO_UMSTELL();


    // TODO BETRPAUSCH - String
    @Optional
    @ImportColumn("BETRPAUSCH")
    Property<String> BETRPAUSCH();


    // TODO ANGABEPROZ - String
    @Optional
    @ImportColumn("ANGABEPROZ")
    Property<String> ANGABEPROZ();


    // TODO GEWICHT - Long
    @Optional
    @ImportColumn("GEWICHT")
    Property<Long> GEWICHT();


    // TODO BAUJAHR - Long
    @Optional
    @ImportColumn("BAUJAHR")
    Property<Long> BAUJAHR();


    // TODO BWANT_IND - String
    @Optional
    @ImportColumn("BWANT_IND")
    Property<String> BWANT_IND();


    // TODO GESMIETE_EING - String
    @Optional
    @ImportColumn("GESMIETE_EING")
    Property<String> GESMIETE_EING();


    // TODO WBU_BEZ - String
    @Optional
    @ImportColumn("WBU_BEZ")
    Property<String> WBU_BEZ();


    // TODO FREILEGUNG - Double
    @Optional
    @ImportColumn("FREILEGUNG")
    Property<Double> FREILEGUNG();


    // TODO MAKLERBW - String
    @Optional
    @ImportColumn("MAKLERBW")
    Property<String> MAKLERBW();


    // TODO EFAKTOR1 - Double
    @Optional
    @ImportColumn("EFAKTOR1")
    Property<Double> EFAKTOR1();


    // TODO EWERT1 - Double
    @Optional
    @ImportColumn("EWERT1")
    Property<Double> EWERT1();


    // TODO EFAKTOR2 - Double
    @Optional
    @ImportColumn("EFAKTOR2")
    Property<Double> EFAKTOR2();


    // TODO EWERT2 - Double
    @Optional
    @ImportColumn("EWERT2")
    Property<Double> EWERT2();


    // TODO ABSCHL_METHODE_NEU - String
    @Optional
    @ImportColumn("ABSCHL_METHODE_NEU")
    Property<String> ABSCHL_METHODE_NEU();


    // TODO ANTRAGNR_GUTACHTEN - String
    @Optional
    @ImportColumn("ANTRAGNR_GUTACHTEN")
    Property<String> ANTRAGNR_GUTACHTEN();


    // TODO GUTACHTNR_GUTACHTEN - String
    @Optional
    @ImportColumn("GUTACHTNR_GUTACHTEN")
    Property<String> GUTACHTNR_GUTACHTEN();


    // TODO WOHNFL1 - String
    @Optional
    @ImportColumn("WOHNFL1")
    Property<String> WOHNFL1();


    // TODO WOHNFL2 - String
    @Optional
    @ImportColumn("WOHNFL2")
    Property<String> WOHNFL2();


    // TODO WOHNFL3 - String
    @Optional
    @ImportColumn("WOHNFL3")
    Property<String> WOHNFL3();


    // TODO WOHNFL4 - String
    @Optional
    @ImportColumn("WOHNFL4")
    Property<String> WOHNFL4();


    // TODO WOHNFL5 - String
    @Optional
    @ImportColumn("WOHNFL5")
    Property<String> WOHNFL5();


    // TODO WOHNFL6 - String
    @Optional
    @ImportColumn("WOHNFL6")
    Property<String> WOHNFL6();


    // TODO WOHNFL7 - String
    @Optional
    @ImportColumn("WOHNFL7")
    Property<String> WOHNFL7();


    public static abstract class Mixin
            implements ErtragswertverfahrenComposite {

        private static Log log = LogFactory.getLog( Mixin.class );

    }

}
