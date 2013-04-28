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
import org.qi4j.api.property.Computed;
import org.qi4j.api.property.ComputedPropertyInstance;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyInfo;
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
@Mixins({ FlurstueckComposite.Mixin.class, PropertyChangeSupport.Mixin.class,
        ModelChangeSupport.Mixin.class, QiEntity.Mixin.class
// JsonState.Mixin.class
})
@ImportTable("FLURZWI")
public interface FlurstueckComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite, Named {

    // CREATE TABLE FLURZWI (
    // EINGANGSNR DOUBLE,
    @Optional
    Association<KaufvertragComposite> vertrag();


    @Computed
    Property<String> vertragsNummer();


    // GEMARKUNG VARCHAR(4),
    @Optional
    Association<GemarkungComposite> gemarkung();


    // FLUR VARCHAR(3),
    @Optional
    Association<FlurComposite> flur();


    // FLSTNR1 INTEGER DEFAULT 0,
    @Optional
    @ImportColumn("FLSTNR1")
    @UseDefaults
    Property<Integer> nummer();


    // FLSTNR1U VARCHAR(3),
    @Optional
    @ImportColumn("FLSTNR1U")
    Property<String> unterNummer();


    // BAUBLOCK VARCHAR(12),
    @Optional
    @ImportColumn("BAUBLOCK")
    Property<String> baublock();


    // FLAECHE1 DOUBLE,
    @Optional
    @UseDefaults
    @ImportColumn("FLAECHE1")
    Property<Double> flaeche();


    // FANTZ1 DOUBLE,
    @Optional
    @UseDefaults
    @ImportColumn("FANTZ1")
    Property<Double> flaecheAnteilZaehler();


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


    // NUTZUNG VARCHAR(2),
    @Optional
    Association<NutzungComposite> nutzung();


    // LAGE VARCHAR(35),
    // Enthält nochmal den Strassennamen, in UI nicht zu erkennen

    // HAUSNR INTEGER DEFAULT 0,
    @Optional
    @ImportColumn("HAUSNR")
    @UseDefaults
    Property<Integer> hausnummer();


    // STRNR VARCHAR(5),
    @Optional
    Association<StrasseComposite> strasse();


    // GEBART VARCHAR(3),
    @Optional
    Association<GebaeudeArtComposite> gebaeudeArt();


    // KARTBLATT VARCHAR(6),
    @Optional
    @ImportColumn("KARTBLATT")
    Property<String> kartenBlatt();


    // XGK DOUBLE, IGNORE, eventuell später mal als geom()
    // YGK DOUBLE,

    // HAUPTTEIL VARCHAR(1),
    @Optional
    Property<Boolean> hauptFlurstueck();


    // BEMERKUNG VARCHAR(12),
    @Optional
    @ImportColumn("BEMERKUNG")
    Property<String> bemerkung();


    // KARTBLATTN VARCHAR(10),
    @Optional
    @ImportColumn("KARTBLATTN")
    Property<String> kartenBlattNummer();


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


    // GEMEINDE INTEGER DEFAULT 0, Link zur Gemeinde ist schon über Gemarkung ignore?
    // RIZONE VARCHAR(7),
    // RIJAHR TIMESTAMP
    @Optional
    Association<RichtwertzoneComposite> richtwertZone();


    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements FlurstueckComposite {

        private static Log   log          = LogFactory.getLog( Mixin.class );

        private PropertyInfo nameProperty = new GenericPropertyInfo( FlurstueckComposite.class,
                                                  "vertragsNummer" );


        @Override
        public Property<String> vertragsNummer() {
            return new ComputedPropertyInstance<String>( nameProperty ) {

                public String get() {
                    if (vertrag().get() != null) {
                        return EingangsNummerFormatter.format( vertrag().get().eingangsNr().get() );
                    }
                    return null;
                }


                @Override
                public void set( String anIgnoredValue )
                        throws IllegalArgumentException, IllegalStateException {
                    // ignored
                }
            };
        }


        public static Iterable<FlurstueckComposite> forEntity( KaufvertragComposite kaufvertrag ) {
            FlurstueckComposite template = QueryExpressions.templateFor( FlurstueckComposite.class );
            BooleanExpression expr = QueryExpressions.eq( template.vertrag(), kaufvertrag );
            Query<FlurstueckComposite> matches = KapsRepository.instance().findEntities(
                    FlurstueckComposite.class, expr, 0, -1 );
            return matches;
        }


        public static FlurstueckComposite mainForEntity( KaufvertragComposite kaufvertrag ) {
            FlurstueckComposite template = QueryExpressions.templateFor( FlurstueckComposite.class );
            BooleanExpression expr = QueryExpressions.and(
                    QueryExpressions.eq( template.vertrag(), kaufvertrag ),
                    QueryExpressions.eq( template.hauptFlurstueck(), Boolean.TRUE ) );

            Query<FlurstueckComposite> matches = KapsRepository.instance().findEntities(
                    FlurstueckComposite.class, expr, 0, 1 );
            return matches.find();
        }
    }
}
