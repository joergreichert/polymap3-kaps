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
package org.polymap.kaps.model;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.polymap.core.qi4j.QiEntity;
import org.polymap.core.qi4j.event.ModelChangeSupport;
import org.polymap.core.qi4j.event.PropertyChangeSupport;
import org.polymap.kaps.importer.ImportColumn;
import org.polymap.kaps.importer.ImportTable;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
@Concerns({ PropertyChangeSupport.Concern.class })
@Mixins({ KaufvertragComposite.Mixin.class, PropertyChangeSupport.Mixin.class,
        ModelChangeSupport.Mixin.class, QiEntity.Mixin.class,
// JsonState.Mixin.class
})
@ImportTable("K_BUCH")
public interface KaufvertragComposite
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
    // /** Wird aus der Geometry errechnet. */
    // @Computed
    // Property<Double> flaeche();
    //
    // /** Wird aus der Geometry errechnet. */
    // @Computed
    // Property<Double> umfang();
    //
    // /** Anzahl der Teil-Geometrien. Wird aus der Geometry errechnet. */
    // @Computed
    // Property<Integer> numGeom();

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
    Property<Integer> kaufpreis();


    // KANTZ DOUBLE,
    @ImportColumn("KANTZ")
    Property<Integer> kaufpreisAnteilZaehler();


    // KANTN DOUBLE,
    @ImportColumn("KANTN")
    Property<Integer> kaufpreisAnteilNenner();


    // VOLLPREIS DOUBLE,
    @UseDefaults
    @ImportColumn("VOLLPREIS")
    Property<Integer> vollpreis();


    // VERARBKZ VARCHAR(1),
    // J oder N
    // zur Auswertung geeignet
    @UseDefaults
    @ImportColumn("VERARBKZ")
    Property<Boolean> zurAuswertungGeeignet();


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
    // @ImportColumn("VERKAUF")
    Association<KaufvertragComposite> verkauf();


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
    Property<Integer> gesamtFlaeche();


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
    Property<Integer> gesamtVerkaufsFlaeche();


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
    @ImportColumn("GESPLITTET")
    Property<Boolean> gesplittet();


    // GESPLITTET_EINGANGSNR INTEGER
    @Optional
    // @ImportColumn("GESPLITTET_EINGANGSNR")
    @UseDefaults
    Association<KaufvertragComposite> gesplittetHauptvertrag();


    // neue Felder
    @Optional
    Property<String> urkundenNummer();


    @Optional
    Property<Boolean> fuerGewosGeeignet();


    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements KaufvertragComposite {

        private static Log log = LogFactory.getLog( Mixin.class );
        
        @Override
        public void beforeCompletion()
                throws UnitOfWorkCompletionException {
            if (eingangsNr().get() == null) {
                eingangsNr().set( KapsRepository.instance().highestEingangsNummer() );
            }
        }

        // private BiotopRepository repo = BiotopRepository.instance();
        //
        // private PropertyInfo flaecheInfo = new GenericPropertyInfo(
        // KaufvertragComposite.class, "flaeche" );
        // private PropertyInfo umfangInfo = new GenericPropertyInfo(
        // KaufvertragComposite.class, "umfang" );
        // private PropertyInfo numGeomInfo = new GenericPropertyInfo(
        // KaufvertragComposite.class, "numGeom" );
        // // private PropertyInfo bearbeitetInfo = new GenericPropertyInfo(
        // BiotopComposite.class, "bearbeitet" );
        // // private PropertyInfo bearbeiterInfo = new GenericPropertyInfo(
        // BiotopComposite.class, "bearbeiter" );
        //
        //
        // public Property<Double> flaeche() {
        // return new ComputedPropertyInstance( flaecheInfo ) {
        // public Object get() {
        // Geometry geom = geom().get();
        // return geom != null ? geom.getArea() : -1;
        // }
        // };
        // }
        //
        // public Property<Double> umfang() {
        // return new ComputedPropertyInstance( umfangInfo ) {
        // public Object get() {
        // Geometry geom = geom().get();
        // return geom != null ? geom.getLength() : -1;
        // }
        // };
        // }
        //
        // public Property<Integer> numGeom() {
        // return new ComputedPropertyInstance( numGeomInfo ) {
        // public Object get() {
        // Geometry geom = geom().get();
        // return geom != null ? geom.getNumGeometries() : -1;
        // }
        // };
        // }

        // public void beforeCompletion()
        // throws UnitOfWorkCompletionException {
        // EntityState entityState = EntityInstance.getEntityInstance( composite
        // ).entityState();
        // //return qi4j.getEntityState( composite ).lastModified();
        //
        // // hope that Qi4J lets run just one UoW completion at once; otherwise
        // we have
        // // race consitions between check and set of lastModified property
        // between the
        // // threads
        //
        // switch (entityState.status()) {
        // case NEW:
        // case UPDATED:
        // Principal user = Polymap.instance().getUser();
        //
        // ValueBuilder<AktivitaetValue> builder =
        // BiotopRepository.instance().newValueBuilder( AktivitaetValue.class );
        // AktivitaetValue prototype = builder.prototype();
        // prototype.wann().set( new Date() );
        // prototype.wer().set( user.getName() );
        // prototype.bemerkung().set( "" );
        // bearbeitung().set( builder.newInstance() );
        // }
        // public Property<Date> bearbeitet() {
        // return new ComputedPropertyInstance( groesseInfo ) {
        // public Object get() {
        // Long lastModified = _lastModified().get();
        // return new Date( lastModified != null ? lastModified : 0 );
        // }
        // };
        // }
        //
        // public Property<String> bearbeiter() {
        // return new ComputedPropertyInstance( groesseInfo ) {
        // public Object get() {
        // return _lastModifiedBy().get();
        // }
        // };
        // }

    }

}
