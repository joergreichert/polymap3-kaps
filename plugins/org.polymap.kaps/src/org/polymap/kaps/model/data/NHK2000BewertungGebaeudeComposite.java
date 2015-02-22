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
@Mixins({ NHK2000BewertungGebaeudeComposite.Mixin.class, PropertyChangeSupport.Mixin.class,
        ModelChangeSupport.Mixin.class, QiEntity.Mixin.class
// JsonState.Mixin.class
})
@ImportTable("K_BEWERTBGF00")
public interface NHK2000BewertungGebaeudeComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite, SchlNamed {

    @Optional
    Association<NHK2000BewertungComposite> bewertung();


    // VERKJAHR - Long
    @Optional
    Property<Double> VERKJAHR();


    // BKIND - Double
    @Optional
    @ImportColumn("BKIND")
    Property<Double> BKIND();


    // GEBNR1 - String
    @Optional
    @ImportColumn("GEBNR1")
    Property<String> GEBNR1();


    // AUSSTATT1 - String
    @Optional
    @ImportColumn("AUSSTATT1")
    Property<String> AUSSTATT1();


    // BERBAUJ1 - Long
    @Optional
    Property<Double> BERBAUJ1();


    // BAUJ1 - Long
    @Optional
    Property<Double> BAUJ1();


    // BGF1 - Double
    @Optional
    @ImportColumn("BGF1")
    Property<Double> BGF1();


    // NHK1 - Double
    @Optional
    @ImportColumn("NHK1")
    Property<Double> NHK1();


    // BNK1 - Double
    @Optional
    @ImportColumn("BNK1")
    Property<Double> BNK1();


    //
    // // MEHRFAM1 - String
    // @Optional
    // @ImportColumn("MEHRFAM1")
    // Property<String> MEHRFAM1();

    // GRDRISS1 - String
    @Optional
    @ImportColumn("GRDRISS1")
    Property<String> GRDRISS1();


    // WOHNGR1 - Double
    @Optional
    @ImportColumn("WOHNGR1")
    Property<Double> WOHNGR1();


    // NHKBNK1 - Double
    @Optional
    @ImportColumn("NHKBNK1")
    Property<Double> NHKBNK1();


    // NHWERT1 - Double
    @Optional
    @ImportColumn("NHWERT1")
    Property<Double> NHWERT1();


    // NEUWERT1 - Double
    @Optional
    @ImportColumn("NEUWERT1")
    Property<Double> NEUWERT1();


    // GND1 - Long
    @Optional
    Property<Double> GND1();


    // RND1 - Long
    @Optional
    Property<Double> RND1();


    // ALTER1 - Long
    @Optional
    Property<Double> ALTER1();


    // WERTMIN1 - Long
    @Optional
    Property<Double> WERTMIN1();


    // ZEITWERT1 - Double
    @Optional
    @ImportColumn("ZEITWERT1")
    Property<Double> ZEITWERT1();


    // ABSCHLBM1 - Long
    @Optional
    Property<Double> ABSCHLBM1();


    // ZUABSCHL1 - Long
    @Optional
    Property<Double> ZUABSCHL1();


    // BERZEITW1 - Double
    @Optional
    @ImportColumn("BERZEITW1")
    Property<Double> BERZEITW1();

    @Optional
    Property<Double> ALTERSWERTMINDERUNG();
    
    // ROSS1 - String
    @Optional
    Property<String> ROSS1();


    //
    // // AUSVH - Long
    // @Optional
    // @ImportColumn("AUSVH")
    // Property<Long> AUSVH();
    //
    //
    // // OBJEKTNR - Long
    // @Optional
    // @ImportColumn("OBJEKTNR")
    // Property<Long> OBJEKTNR();
    //
    //
    // // OBJEKTNRFORTF - Long
    // @Optional
    // @ImportColumn("OBJEKTNRFORTF")
    // Property<Long> OBJEKTNRFORTF();
    //
    //
    // // GEBNR - Long
    // @Optional
    // @ImportColumn("GEBNR")
    // Property<Long> GEBNR();
    //
    //
    // // GESBAUWERT - Double
    // @Optional
    // @ImportColumn("GESBAUWERT")
    // Property<Double> GESBAUWERT();
    //
    //
    // // GEBFORTF - Long
    // @Optional
    // @ImportColumn("GEBFORTF")
    // Property<Long> GEBFORTF();
    //
    //
    // LFDNR - Long
    @Optional
    @ImportColumn("LFDNR")
    Property<Integer> LFDNR();


    //
    //
    // // WOHNUNGSNR - Long
    // @Optional
    // @ImportColumn("WOHNUNGSNR")
    // Property<Long> WOHNUNGSNR();
    //
    //
    // // ZWSUMSONST - Double
    // @Optional
    // @ImportColumn("ZWSUMSONST")
    // Property<Double> ZWSUMSONST();
    //
    //
    // // FORTF - Long
    // @Optional
    // @ImportColumn("FORTF")
    // Property<Long> FORTF();
    //
    //
    // // ZEITWString - String
    // @Optional
    // @ImportColumn("ZEITWString")
    // Property<String> ZEITWString();
    //
    //
    // // EURO_UMSTELL - String
    // @Optional
    // @ImportColumn("EURO_UMSTELL")
    // Property<String> EURO_UMSTELL();
    //

    // BGF1_2000 - Double
    @Optional
    @ImportColumn("BGF1_2000")
    Property<Double> BGF1_2000();


    //
    //
    // // PROZAUSSEN - String
    // @Optional
    // @ImportColumn("PROZAUSSEN")
    // Property<String> PROZAUSSEN();
    //
    //
    // // AUSVHBETR - Double
    // @Optional
    // @ImportColumn("AUSVHBETR")
    // Property<Double> AUSVHBETR();
    //

    // TATSHOEHE - Double
    @Optional
    @ImportColumn("TATSHOEHE")
    Property<Double> TATSHOEHE();


    // WOHNGEB - String
    @Optional
    Property<Boolean> WOHNGEB();


    //
    //
    // // ANBAUTEN - String
    // @Optional
    // @ImportColumn("ANBAUTEN")
    // Property<String> ANBAUTEN();
    //
    //
    // // NICHTWOHN - String
    // @Optional
    // @ImportColumn("NICHTWOHN")
    // Property<String> NICHTWOHN();
    //
    //
    // // BKIND_JAHR - String
    // @Optional
    // @ImportColumn("BKIND_JAHR")
    // Property<String> BKIND_JAHR();
    //
    //
    // // SUMWOHNFL - Double
    // @Optional
    // @ImportColumn("SUMWOHNFL")
    // Property<Double> SUMWOHNFL();
    //
    //
    // // SUMBGF - Double
    // @Optional
    // @ImportColumn("SUMBGF")
    // Property<Double> SUMBGF();
    //
    //
    // // GFAKTOR1 - Double
    // @Optional
    // @ImportColumn("GFAKTOR1")
    // Property<Double> GFAKTOR1();
    //
    //
    // // GWERT1 - Double
    // @Optional
    // @ImportColumn("GWERT1")
    // Property<Double> GWERT1();
    //
    //
    // // GFAKTOR2 - Double
    // @Optional
    // @ImportColumn("GFAKTOR2")
    // Property<Double> GFAKTOR2();
    //
    //
    // // GWERT2 - Double
    // @Optional
    // @ImportColumn("GWERT2")
    // Property<Double> GWERT2();
    //
    //
    // // MAKLERBW - String
    // @Optional
    // @ImportColumn("MAKLERBW")
    // Property<String> MAKLERBW();
    //
    //
    // // FORMELNEU - String
    // @Optional
    // @ImportColumn("FORMELNEU")
    // Property<String> FORMELNEU();
    //
    //
    // // GEB_METHODE_NEU - String
    // @Optional
    // @ImportColumn("GEB_METHODE_NEU")
    // Property<String> GEB_METHODE_NEU();
    //

    // ABSCHLBM_BETRAG_EING - String
    @Optional
    Property<Boolean> ABSCHLBM_BETRAG_EING();


    // ABSCHLSO_BETRAG_EING - String
    @Optional
    Property<Boolean> ABSCHLSO_BETRAG_EING();


    // ABSCHLBM_BETRAG - Double
    @Optional
    @ImportColumn("ABSCHLBM_BETRAG")
    Property<Double> ABSCHLBM_BETRAG();


    // ABSCHLSO_BETRAG - Double
    @Optional
    @ImportColumn("ABSCHLSO_BETRAG")
    Property<Double> ABSCHLSO_BETRAG();


    //
    // // ABSCHL_METHODE_NEU - String
    // @Optional
    // @ImportColumn("ABSCHL_METHODE_NEU")
    // Property<String> ABSCHL_METHODE_NEU();
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

    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements NHK2000BewertungGebaeudeComposite {

        private static Log log = LogFactory.getLog( Mixin.class );


        public static Iterable<NHK2000BewertungGebaeudeComposite> forBewertung( NHK2000BewertungComposite bewertung ) {
            NHK2000BewertungGebaeudeComposite template = QueryExpressions
                    .templateFor( NHK2000BewertungGebaeudeComposite.class );
            BooleanExpression expr = QueryExpressions.eq( template.bewertung(), bewertung );
            Query<NHK2000BewertungGebaeudeComposite> matches = KapsRepository.instance().findEntities(
                    NHK2000BewertungGebaeudeComposite.class, expr, 0, -1 );
            return matches;
        }
    }
}
