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
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.kaps.AssociationInfo;
import org.qi4j.api.entity.association.kaps.ComputedAssociationInstance;
import org.qi4j.api.entity.association.kaps.GenericAssociationInfo;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Computed;
import org.qi4j.api.property.ComputedPropertyInstance;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;

import org.eclipse.jface.util.PropertyChangeEvent;

import org.polymap.core.qi4j.QiEntity;
import org.polymap.core.qi4j.event.ModelChangeSupport;
import org.polymap.core.qi4j.event.PropertyChangeSupport;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.kaps.importer.ImportColumn;
import org.polymap.kaps.importer.ImportTable;
import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.Named;
import org.polymap.kaps.ui.form.EingangsNummerFormatter;

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
@Concerns({ PropertyChangeSupport.Concern.class })
@Mixins({ VertragComposite.Mixin.class, PropertyChangeSupport.Mixin.class, ModelChangeSupport.Mixin.class,
        QiEntity.Mixin.class,
// JsonState.Mixin.class
})
@ImportTable("K_BUCH")
public interface VertragComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite, Named {

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
    // GESPLITTET_EINGANGSNR INTEGER,
    // STABU_GEBART VARCHAR(1),
    // STABU_GEBTYP VARCHAR(1),
    // KELLER VARCHAR(1),
    // GARAGE VARCHAR(1),
    // STELLPLATZ VARCHAR(1),
    // CARPORT VARCHAR(1)
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
    // @ImportColumn("VERARBKZ")
    Property<Boolean> fuerAuswertungGeeignet();


    // BEM1 VARCHAR(60),
    @Optional
    // @ImportColumn("BEM1")
    Property<String> bemerkungen();


    // BEM2 VARCHAR(60),
    // @Optional
    // @ImportColumn("BEM2")
    // Property<String> bemerkungen2();

    // VERKAUF VARCHAR(9),
    // letzter Verkauf, Referenz auf anderen Vertrag
    @Optional
    @ImportColumn("VERKAUF")
    Property<String> verkaufEingangsnr();


    //
    // @Computed
    // @Optional
    // Association<VertragComposite> letzterVerkauf();

    // ANFR1 VARCHAR(60),
    @Optional
    // @ImportColumn("ANFR1")
    Property<String> anfragen();


    // ANFR2 VARCHAR(60),
    // @Optional
    // @ImportColumn("ANFR2")
    // Property<String> anfragen2();

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

    // // GESFLAECHE DOUBLE DEFAULT 0,
    // @UseDefaults
    // @ImportColumn("GESFLAECHE")
    // Property<Double> gesamtFlaeche();

    // verkaufbem VARCHAR(25),
    // @Optional
    // @ImportColumn("verkaufbem")
    // Property<String> bemerkungLetzterVerkauf();
    // wird nicht benutzt
    // EURO_UMSTELL VARCHAR(1),
    // wird nicht benutzt
    //
    // // GESVERKFL DOUBLE,
    // TODO muss aus Flurstücken berechnet werden
    // // korrelliert mit Zähler/Nenner
    // // bspw. N = 2, Z = 1 ergibt gesflaeche 800 und gesverkflaeche 400
    // @UseDefaults
    // @ImportColumn("GESVERKFL")
    // Property<Double> gesamtVerkaufsFlaeche();
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
    // @ImportColumn("GESPLITTET")
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
    Property<String> notariat();


    @Optional
    Property<Boolean> fuerGewosGeeignet();


    @Optional
    Association<VertragsdatenErweitertComposite> erweiterteVertragsdaten();


    // @Optional
    // @Computed
    // Association<FlurstueckComposite> hauptFlurstueck();

    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements VertragComposite {

        private static Log log = LogFactory.getLog( Mixin.class );


        @Override
        public Property<String> name() {
            return new ComputedPropertyInstance<String>( new GenericPropertyInfo( VertragComposite.class, "name" ) ) {

                public String get() {
                    if (eingangsNr().get() != null) {
                        return EingangsNummerFormatter.format( eingangsNr().get() );
                    }
                    else {
                        return "-";
                    }
                }
            };

        }


        // FIXME uncomment after import
        @Override
        public void beforeCompletion()
                throws UnitOfWorkCompletionException {
            if (eingangsNr().get() == null) {
                eingangsNr().set( KapsRepository.instance().eingangsNummern.get().generate( vertragsDatum().get() ) );
                EventManager.instance().publish(
                        new PropertyChangeEvent( this, eingangsNr().qualifiedName().name(), null, eingangsNr().get() ) );
            }
        }

        private AssociationInfo vertragCompositeAss = new GenericAssociationInfo( VertragComposite.class,
                                                            "gesplitteterHauptvertrag" );


        @Override
        public Association<VertragComposite> gesplitteterHauptvertrag() {
            return new ComputedAssociationInstance<VertragComposite>( vertragCompositeAss ) {

                @Override
                public VertragComposite get() {
                    if (gesplittetEingangsnr().get() != null) {
                        VertragComposite template = QueryExpressions.templateFor( VertragComposite.class );

                        return KapsRepository
                                .instance()
                                .findEntities(
                                        VertragComposite.class,
                                        QueryExpressions.eq( template.eingangsNr(),
                                                Integer.parseInt( gesplittetEingangsnr().get() ) ), 0, 1 ).find();
                    }

                    return null;

                }


                @Override
                public void set( VertragComposite vertrag )
                        throws IllegalArgumentException, IllegalStateException {
                    gesplittetEingangsnr().set( vertrag.eingangsNr().get() + "" );
                }
            };
        }

        private final VertragComposite vertrag = this;


        @Override
        public Association<RichtwertzoneComposite> richtwertZoneBauland() {
            return new ComputedAssociationInstance<RichtwertzoneComposite>( new GenericAssociationInfo(
                    VertragComposite.class, "richtwertZoneBauland" ) ) {

                @Override
                public RichtwertzoneComposite get() {
                    for (FlurstueckComposite flurstueck : FlurstueckComposite.Mixin.forEntity( vertrag )) {
                        NutzungComposite nutzung = flurstueck.nutzung().get();
                        if (nutzung != null
                                && (nutzung.isAgrar().get() == null || nutzung.isAgrar().get() == Boolean.FALSE)) {
                            return flurstueck.richtwertZone().get();
                        }
                    }
                    return null;

                }


                @Override
                public void set( RichtwertzoneComposite vertrag )
                        throws IllegalArgumentException, IllegalStateException {
                    // ignore
                }
            };
        }


        @Override
        public Set<RichtwertzoneComposite> richtwertZonenAgrar() {
            Set<RichtwertzoneComposite> zonen = new HashSet<RichtwertzoneComposite>();
            for (FlurstueckComposite flurstueck : FlurstueckComposite.Mixin.forEntity( vertrag )) {
                NutzungComposite nutzung = flurstueck.nutzung().get();
                if (nutzung != null && nutzung.isAgrar().get() != null && nutzung.isAgrar().get() == Boolean.TRUE) {
                    zonen.add( flurstueck.richtwertZone().get() );
                }
            }
            return zonen;
        }


        public static VertragComposite forErweiterteDaten( VertragsdatenErweitertComposite entity ) {
            VertragComposite template = QueryExpressions.templateFor( VertragComposite.class );
            BooleanExpression expr = QueryExpressions.eq( template.erweiterteVertragsdaten(), entity );
            return KapsRepository.instance().findEntities( VertragComposite.class, expr, 0, 1 ).find();
        }
    }


    @Optional
    @Computed
    Association<RichtwertzoneComposite> richtwertZoneBauland();


    Set<RichtwertzoneComposite> richtwertZonenAgrar();
}
