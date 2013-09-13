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
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Computed;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;

import org.eclipse.jface.util.PropertyChangeEvent;

import org.polymap.core.qi4j.QiEntity;
import org.polymap.core.qi4j.event.ModelChangeSupport;
import org.polymap.core.qi4j.event.PropertyChangeSupport;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.kaps.importer.ImportColumn;
import org.polymap.kaps.importer.ImportTable;
import org.polymap.kaps.model.KapsRepository;

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
@Concerns({ PropertyChangeSupport.Concern.class })
@Mixins({ VertragComposite.Mixin.class, PropertyChangeSupport.Mixin.class,
        ModelChangeSupport.Mixin.class, QiEntity.Mixin.class,
// JsonState.Mixin.class
})
@ImportTable("K_BUCH")
public interface VertragComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite {

    // CREATE TABLE K_BUCH (
    // EINGANGSNR DOUBLE,
    // VERTDATUM TIMESTAMP,
    // EINGANG TIMESTAMP,
    // VKREIS VARCHAR(2),
    // KKREIS VARCHAR(2),
    // VERTRAGART VARCHAR(2),
    // BEBAUT VARCHAR(1),
    // KAUFPREIS DOUBLE,
    // KANTZ DOUBLE,
    // KANTN DOUBLE,
    // VOLLPREIS DOUBLE,
    // VERARBKZ VARCHAR(1),
    // BEM1 VARCHAR(60),
    // BEM2 VARCHAR(60),
    // VERKAUF VARCHAR(9),
    // ANFR1 VARCHAR(60),
    // ANFR2 VARCHAR(60),
    // ANKSRSTAM TIMESTAMP,
    // ANVSRSTAM TIMESTAMP,
    // ANKEINGAM TIMESTAMP,
    // ANVEINGAM TIMESTAMP,
    // BEMKAUF VARCHAR(40),
    // BEMVKAUF VARCHAR(40),
    // KAUFPREIS_EURO DOUBLE DEFAULT 0,
    // GESFLAECHE DOUBLE DEFAULT 0,
    // verkaufbem VARCHAR(25),
    // EURO_UMSTELL VARCHAR(1),
    // GESVERKFL DOUBLE,
    // STALA_AUSG TIMESTAMP,
    // GUTACHTNR1 VARCHAR(12),
    // GESPLITTET VARCHAR(1),
    // GESPLITTET_EINGANGSNR INTEGER
    // );
    //
    // CREATE INDEX K_KKREISK_BUCH ON K_BUCH (KKREIS ASC);
    //
    // CREATE INDEX K_KKREISK_BUCH1 ON K_BUCH (VKREIS ASC);
    //
    // CREATE INDEX Reference11 ON K_BUCH (VERTRAGART ASC);
    //
    // ALTER TABLE K_BUCH ADD CONSTRAINT Reference6 UNIQUE (null);
    //
    // ALTER TABLE K_BUCH ADD CONSTRAINT Reference3 UNIQUE (null);
    //
    // ALTER TABLE K_BUCH ADD CONSTRAINT K_BUCHK_BEWERTBGF95 UNIQUE (null);
    //
    // ALTER TABLE K_BUCH ADD CONSTRAINT Reference1 UNIQUE (null);
    //
    // ALTER TABLE K_BUCH ADD CONSTRAINT K_BUCHK_BEWERTBRI95 UNIQUE (null);
    //
    // ALTER TABLE K_BUCH ADD CONSTRAINT K_FKEY_KPZUSCHL UNIQUE (null);
    //
    // ALTER TABLE K_BUCH ADD CONSTRAINT Reference5 UNIQUE (null);
    //
    // ALTER TABLE K_BUCH ADD CONSTRAINT Reference UNIQUE (null);
    //
    // ALTER TABLE K_BUCH ADD CONSTRAINT Reference31 UNIQUE (null);
    //
    // ALTER TABLE K_BUCH ADD CONSTRAINT Reference4 UNIQUE (null);
    //
    // ALTER TABLE K_BUCH ADD CONSTRAINT K_BUCHK_BEVERL UNIQUE (null);
    //
    // ALTER TABLE K_BUCH ADD CONSTRAINT K_FKEY_KPZUSCHL_GEB UNIQUE (null);
    //
    // ALTER TABLE K_BUCH ADD CONSTRAINT PK_K_BUCH PRIMARY KEY (EINGANGSNR);
    //
    // @Optional
    // Property<MultiPolygon> geom();
    //
 
    final static String NAME = "Vertrag";

    /** Eingangsnummer. */
    // EINGANGSNR DOUBLE,
    @Optional
    @ImportColumn("EINGANGSNR")
    Property<Integer> eingangsNr();


    // VERTDATUM TIMESTAMP,
    @Optional
    @ImportColumn("VERTDATUM")
    Property<Date> vertragsDatum();


    // EINGANG TIMESTAMP,
    @Optional
    @ImportColumn("EINGANG")
    Property<Date> eingangsDatum();


    // VKREIS VARCHAR(2),
    @Optional
    // @ImportColumn("VKREIS")
    Association<KaeuferKreisComposite> verkaeuferKreis();


    // KKREIS VARCHAR(2),
    @Optional
    // @ImportColumn("KKREIS")
    Association<KaeuferKreisComposite> kaeuferKreis();


    // VERTRAGART VARCHAR(2),
    @Optional
    // @ImportColumn("VERTRAGART")
    Association<VertragsArtComposite> vertragsArt();


    // BEBAUT VARCHAR(1),
    // wird nicht benutzt

    // KAUFPREIS DOUBLE,
    @UseDefaults
    @ImportColumn("KAUFPREIS")
    Property<Double> kaufpreis();


    // KANTZ DOUBLE,
    @ImportColumn("KANTZ")
    Property<Double> kaufpreisAnteilZaehler();


    // KANTN DOUBLE,
    @ImportColumn("KANTN")
    Property<Double> kaufpreisAnteilNenner();


    // VOLLPREIS DOUBLE,
    @UseDefaults
    @ImportColumn("VOLLPREIS")
    Property<Double> vollpreis();


    // VERARBKZ VARCHAR(1),
    // J oder N
    // zur Auswertung geeignet
    @UseDefaults
    //@ImportColumn("VERARBKZ")
    Property<Boolean> fuerAuswertungGeeignet();


    // BEM1 VARCHAR(60),
    @Optional
    //@ImportColumn("BEM1")
    Property<String> bemerkungen();


    // BEM2 VARCHAR(60),
    //@Optional
    //@ImportColumn("BEM2")
    //Property<String> bemerkungen2();


    // VERKAUF VARCHAR(9),
    // letzter Verkauf, Referenz auf anderen Vertrag
    @Optional
    @ImportColumn("VERKAUF")
    Property<String> verkaufEingangsnr();
    
    @Computed
    @Optional
    Association<VertragComposite> letzterVerkauf();


    // ANFR1 VARCHAR(60),
    @Optional
//    @ImportColumn("ANFR1")
    Property<String> anfragen();


    // ANFR2 VARCHAR(60),
//    @Optional
//    @ImportColumn("ANFR2")
//    Property<String> anfragen2();


    // ANKSRSTAM TIMESTAMP,
    // Anschreiben Kaeufer erstellt am
    @Optional
    @ImportColumn("ANKSRSTAM")
    Property<Date> anschreibenKaeuferErstelltAm();


    // ANVSRSTAM TIMESTAMP,
    @Optional
    @ImportColumn("ANVSRSTAM")
    Property<Date> anschreibenVerkaeuferErstelltAm();


    // ANKEINGAM TIMESTAMP,
    @Optional
    @ImportColumn("ANKEINGAM")
    Property<Date> anschreibenKaeuferEingangAntwort();


    // ANVEINGAM TIMESTAMP,
    @Optional
    @ImportColumn("ANVEINGAM")
    Property<Date> anschreibenVerkaeuferEingangAntwort();


    // BEMKAUF VARCHAR(40),
    @Optional
    @ImportColumn("BEMKAUF")
    Property<String> bemerkungenKaeufer();


    // BEMVKAUF VARCHAR(40),
    @Optional
    @ImportColumn("BEMVKAUF")
    Property<String> bemerkungenVerkaeufer();


    // KAUFPREIS_EURO DOUBLE DEFAULT 0,
    // wird nicht benutzt

    // GESFLAECHE DOUBLE DEFAULT 0,
    @UseDefaults
    @ImportColumn("GESFLAECHE")
    Property<Double> gesamtFlaeche();


    // verkaufbem VARCHAR(25),
    @Optional
    @ImportColumn("verkaufbem")
    Property<String> bemerkungLetzterVerkauf();


    // EURO_UMSTELL VARCHAR(1),
    // wird nicht benutzt

    // GESVERKFL DOUBLE,
    // korrelliert mit Zähler/Nenner
    // bspw. N = 2, Z = 1 ergibt gesflaeche 800 und gesverkflaeche 400
    @UseDefaults
    @ImportColumn("GESVERKFL")
    Property<Double> gesamtVerkaufsFlaeche();


    // STALA_AUSG TIMESTAMP,
    // Schalter ob schon nach STALA exportiert
    @Optional
    @ImportColumn("STALA_AUSG")
    Property<Date> stalaAusgang();


    // GUTACHTNR1 VARCHAR(12),
    @Optional
    @ImportColumn("GUTACHTNR1")
    Property<String> gutachtenNummer();


    // GESPLITTET VARCHAR(1),
    // Grundstück geht über mehrere Gemeinden
    @Optional
    //@ImportColumn("GESPLITTET")
    Property<Boolean> gesplittet();


    // GESPLITTET_EINGANGSNR INTEGER
    @Optional
    @ImportColumn("GESPLITTET_EINGANGSNR")
    Property<String> gesplittetEingangsnr();
    
    @Computed
    @Optional
    Association<VertragComposite> gesplitteterHauptvertrag();


    // neue Felder
    @Optional
    Property<String> urkundenNummer();


    @Optional
    Property<Boolean> fuerGewosGeeignet();
    
    @Optional
    Association<VertragsdatenErweitertComposite> erweiterteVertragsdaten();

//    @Optional
//    @Computed
//    Association<FlurstueckComposite> hauptFlurstueck();

    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements VertragComposite {

        private static Log log = LogFactory.getLog( Mixin.class );
        
        @Override
        public void beforeCompletion()
                throws UnitOfWorkCompletionException {
            if (eingangsNr().get() == null) {
                eingangsNr().set( KapsRepository.instance().highestEingangsNummer(vertragsDatum().get()) );
                EventManager.instance().publish( new PropertyChangeEvent( this, eingangsNr().qualifiedName().name(), null, eingangsNr().get() ) );
            }
        }

        public Association<VertragComposite> gesplitteterHauptvertrag() {
            // TODO
            return null;
        }
        
//        private AssociationInfo KaufvertragCompositeAss = new GenericAssociationInfo( VertragComposite.class, "hauptFlurstueck" );
//        private final VertragComposite kc = this;
//        
//        @Override
//        public Association<FlurstueckComposite> hauptFlurstueck() {
//            return new ComputedAssociationInstance<FlurstueckComposite>( KaufvertragCompositeAss ) {
//
//                public FlurstueckComposite get() {
//                    return FlurstueckComposite.Mixin.mainForEntity( kc );
//                }
//                
//                @Override
//                public void set( FlurstueckComposite anIgnoredValue )
//                        throws IllegalArgumentException, IllegalStateException {
//                        // ignored
//                }
//            };
//        }
    }

}
