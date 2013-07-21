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
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.mixin.Mixins;
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
import org.polymap.kaps.model.Named;
import org.polymap.kaps.ui.form.EingangsNummerFormatter;

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
@Concerns({ PropertyChangeSupport.Concern.class })
@Mixins({ FlurstueckVerkaufComposite.Mixin.class, PropertyChangeSupport.Mixin.class, ModelChangeSupport.Mixin.class,
        QiEntity.Mixin.class
// JsonState.Mixin.class
})
@ImportTable("FLURZWI")
public interface FlurstueckVerkaufComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite, Named {

    // CREATE TABLE FLURZWI (
    // EINGANGSNR DOUBLE,
    @Optional
    Association<VertragComposite> vertrag();


    @Optional
    Association<FlurstueckComposite> flurstueck();


    // FANTZ1 DOUBLE,
    @Optional
    @UseDefaults
    @ImportColumn("FANTZ1")
    Property<Double> flaechenAnteilZaehler();


    // FANTN1 DOUBLE,
    @Optional
    @UseDefaults
    @ImportColumn("FANTN1")
    Property<Double> flaechenAnteilNenner();


    // VERKFL1 DOUBLE,
    @Optional
    @UseDefaults
    @ImportColumn("VERKFL1")
    Property<Double> verkaufteFlaeche();


    // BEMERKUNG VARCHAR(12),
    @Optional
    @ImportColumn("BEMERKUNG")
    Property<String> bemerkung();


    // GEMEINDE INTEGER DEFAULT 0, Link zur Gemeinde ist schon über Gemarkung ignore?
    // RIZONE VARCHAR(7),
    // RIJAHR TIMESTAMP
    // Auswahl erfolgt zweistufig, erst nur die Zone und dann darin die Gültigkeit
    // ähnlich einer Kategorie - Unterkategorie auswahl
    @Optional
    Association<RichtwertzoneComposite> richtwertZone();

    // BAUBLOCK VARCHAR(12),
    @Optional
    @ImportColumn("BAUBLOCK")
    Property<String> baublock();


    // NUTZUNG VARCHAR(2),
    @Optional
    Association<NutzungComposite> nutzung();


    // LAGE VARCHAR(35),
    // Enthält nochmal den Strassennamen, in UI nicht zu erkennen

    // HAUSNR INTEGER
    @Optional
    @ImportColumn("HAUSNR")
    Property<String> hausnummer();


    // STRNR VARCHAR(5),
    @Optional
    Association<StrasseComposite> strasse();


    // GEBART VARCHAR(3),
    @Optional
    Association<GebaeudeArtComposite> gebaeudeArt();


    // BAUGEBART VARCHAR(1),
    @Optional
    Association<ArtDesBaugebietsComposite> artDesBaugebiets();


    // FLSTNR1X VARCHAR(5), identisch FLSTNR1
    // FLSTNR1UX VARCHAR(3), identisch FLSTNR1U
    // GEM_FLUR VARCHAR(4), Link zur Gemarkung ist über Gemarkung

    // HZUSNR VARCHAR(12),
    @Optional
    @ImportColumn("HZUSNR")
    Property<String> hausnummerZusatz();

    // @Optional
    // Association<RichtwertzoneZeitraumComposite> richtwertZoneG();

    // ERBBAUR - String
    @Optional
    @ImportColumn("ERBBAUR")
    Property<String> erbbaurecht();


    // BELASTUNG - String
    @Optional
    Association<BelastungComposite> belastung();

    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements FlurstueckVerkaufComposite {

        private static Log log = LogFactory.getLog( Mixin.class );


        @Override
        public Property<String> name() {
            return new ComputedPropertyInstance<String>( new GenericPropertyInfo( FlurstueckVerkaufComposite.class,
                    "name" ) ) {

                public String get() {
                    if (vertrag().get() != null) {
                        return EingangsNummerFormatter.format( vertrag().get().eingangsNr().get() );
                    }
                    return null;
                }
            };
        }


        public static Iterable<FlurstueckVerkaufComposite> forEntity( VertragComposite kaufvertrag ) {
            FlurstueckVerkaufComposite template = QueryExpressions.templateFor( FlurstueckVerkaufComposite.class );
            BooleanExpression expr = QueryExpressions.eq( template.vertrag(), kaufvertrag );
            Query<FlurstueckVerkaufComposite> matches = KapsRepository.instance().findEntities(
                    FlurstueckVerkaufComposite.class, expr, 0, -1 );
            return matches;
        }
//        
//        @Override
//        public void beforeCompletion()
//                throws UnitOfWorkCompletionException {
//            if (flurstueck().get() == null && gemarkung.get() != null) {
//                // neues Flurstueck angelegt, globales Flurstück ergänzen
//                
//            }
//        }
    }
}
