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

import com.vividsolutions.jts.geom.MultiPolygon;

import org.polymap.core.qi4j.QiEntity;
import org.polymap.core.qi4j.event.ModelChangeSupport;
import org.polymap.core.qi4j.event.PropertyChangeSupport;

import org.polymap.kaps.importer.ImportColumn;
import org.polymap.kaps.importer.ImportTable;
import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.SchlNamed;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
@Concerns({ PropertyChangeSupport.Concern.class })
@Mixins({ RichtwertzoneComposite.Mixin.class, PropertyChangeSupport.Mixin.class,
        ModelChangeSupport.Mixin.class, QiEntity.Mixin.class
// JsonState.Mixin.class
})
// nur Teile hiervon
@ImportTable("K_RIWE")
public interface RichtwertzoneComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite, SchlNamed {

    public final static String NAME = "Richtwertzone";

    @Optional
    Property<MultiPolygon> geom();
    
    // GFZBER VARCHAR(20), leer
    // GFZ-Bereich
    @Optional
    @ImportColumn("GFZBER")
    Property<String> gfzBereich();


    // ART INTEGER, leer, ist jetzt lage
    // ursprünglich 1 - Landwirtschaft, 2 - Bauland, wird nicht mehr benutzt
    @Optional
    Association<RichtwertZoneLageComposite> lage();

    // RIZONE VARCHAR(7), Nummer PK, scheint keine Referenz zu sein
    @Optional
    @ImportColumn("RIZONE")
    Property<String> schl();

    // BEZ VARCHAR(40), Name
    @Optional
    @ImportColumn("BEZ")
    Property<String> name();
    
    // NUTZUNG VARCHAR(2), NUTZUNG 01-60, Referenz auf K_BONUTZ
    @Optional
    Association<BodennutzungComposite> bodenNutzung();


    // NUART VARCHAR(2), NUART 00,01,27,28,31,32 Referenz auf K_NUTZ
    @Optional
    Association<NutzungComposite> nutzung();
    
    // NUTZUNG_ART
    @Optional
    Association<BodenRichtwertRichtlinieArtDerNutzungComposite> brwrlArt();
    
    // NUTZUNG_ERGAENZ
    @Optional
    Association<BodenRichtwertRichtlinieErgaenzungComposite> brwrlErgaenzung();

    // GEMEINDE INTEGER DEFAULT 0, Referenz auf Gemeinde 522010 bspw
    @Optional
    Association<GemeindeComposite> gemeinde();


    // BASISKARTE VARCHAR(20), leer
    @Optional
    @ImportColumn("BASISKARTE")
    Property<String> basisKarte();


    // MASSSTAB INTEGER, leer, leer
    @Optional
    @ImportColumn("MASSSTAB")
    Property<Integer> massstab();


    // ENTWZUSTAND VARCHAR(2), 
    @Optional
    Association<EntwicklungsZustandComposite> entwicklungsZustand();


    // RIWEKENNUNG VARCHAR(1), leer, Default sollte zonal sein beim import
    @Optional
    Association<BodenRichtwertKennungComposite> bodenrichtwertKennung();


    // GROESSE DOUBLE, leer
    // Grundstücksgröße in qm
    @Optional
    @ImportColumn("GROESSE")
    Property<Double> grundstuecksGroesse();


    // TIEFE DOUBLE, leer in m
    @Optional
    @ImportColumn("TIEFE")
    Property<Double> grundstuecksTiefe();


    // BREITE DOUBLE, leer in m
    @Optional
    @ImportColumn("BREITE")
    Property<Double> grundstuecksBreite();


    // GRZ DOUBLE, 1 DS
    @Optional
    @ImportColumn("GRZ")
    Property<Double> grundflaechenZahl();


    // GEZ DOUBLE, 1 DS
    @Optional
    @ImportColumn("GEZ")
    Property<Double> geschossZahl();


    // BMZ DOUBLE, 1 DS
    @Optional
    @ImportColumn("BMZ")
    Property<Double> baumassenZahl();


    // YWERT DOUBLE, leer
    @Optional
    @ImportColumn("YWERT")
    Property<Double> rechtsWert();


    // XWERT DOUBLE, leer
    @Optional
    @ImportColumn("XWERT")
    Property<Double> hochWert();


    // ACKERZAHL INTEGER, leer
    @Optional
    @ImportColumn("ACKERZAHL")
    Property<Integer> ackerZahl();


    // GRUENZAHL INTEGER, leer
    @Optional
    @ImportColumn("GRUENZAHL")
    Property<Integer> gruenLandZahl();


    // ENTWZUSATZ VARCHAR(2), 
    @Optional
    Association<EntwicklungsZusatzComposite> entwicklungsZusatz();


    // BAUWEISE VARCHAR(2)
    @Optional
    Association<BauweiseComposite> bauweise();


    // WGFZ VARCHAR(11) 1 DS
    @Optional
    @ImportColumn("WGFZ")
    Property<String> geschossFlaechenZahl();

//    @Optional
//    ManyAssociation<RichtwertzoneZeitraumComposite> gueltigkeiten();

    // Handbuch Seite 23, 24

    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements RichtwertzoneComposite {

        private static Log log = LogFactory.getLog( Mixin.class );

//        private PropertyInfo schlProperty = new GenericPropertyInfo( RichtwertzoneComposite.class, "schl" );
//
//        @Override
//        public Property<String> schl() {
//            return new ComputedPropertyInstance<String>( schlProperty ) {
//
//                public String get() {
//                    return zone().get();
//                }
//                
//                @Override
//                public void set( String newValue )
//                        throws IllegalArgumentException, IllegalStateException {
//                        zone().set( newValue );
//                }
//            };
//        }
        
        public static Iterable<RichtwertzoneComposite> findZoneIn( GemeindeComposite gemeinde ) {
            RichtwertzoneComposite template = QueryExpressions
                    .templateFor( RichtwertzoneComposite.class );
            BooleanExpression expr = QueryExpressions.eq( template.gemeinde(), gemeinde );
            Query<RichtwertzoneComposite> matches = KapsRepository.instance().findEntities(
                    RichtwertzoneComposite.class, expr, 0, -1 );
            // filter auf letzte aktuelle Zone, darf nicht da ja auch ältere Zonen auswählbar sind
            return matches;
        }
    }
}