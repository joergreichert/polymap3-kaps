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
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Computed;
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

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
@Concerns({ PropertyChangeSupport.Concern.class })
@Mixins({ GebaeudeComposite.Mixin.class, PropertyChangeSupport.Mixin.class, ModelChangeSupport.Mixin.class,
        QiEntity.Mixin.class
// JsonState.Mixin.class
})
@ImportTable("K_EGEB")
public interface GebaeudeComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite {

    String NAME = "Gebäude";


    // OBJEKTNR - Long
    @Optional
    @ImportColumn("OBJEKTNR")
    Property<Integer> objektNummer();


    // GEBNR - Long
    @Optional
    @ImportColumn("GEBNR")
    Property<Integer> gebaeudeNummer();


    // GEBART - String
    @Optional
    Association<GebaeudeArtComposite> gebaeudeArt();


    // AUFZUG - String JNNull
    @Optional
    @ImportColumn("AUFZUG")
    Property<String> aufzug();


    // LAGEKLWE - Double
    @Optional
    @ImportColumn("LAGEKLWE")
    Property<Double> lageklasse();


    // BEMERKUNG - String
    @Optional
    @ImportColumn("BEMERKUNG")
    Property<String> bemerkung();


    // BAUJAHR - Long
    @Optional
    @ImportColumn("BAUJAHR")
    Property<Integer> baujahr();


    // BAUJAHRTATS - Long
    @Optional
    @ImportColumn("BAUJAHRTATS")
    Property<Integer> baujahrTatsaechlich();


    // Sanierung - String JNNull
    @Optional
    @ImportColumn("Sanierung")
    Property<String> sanierung();


    // Weinheit - Long
    @Optional
    @ImportColumn("Weinheit")
    Property<Integer> wohnEinheiten();


    @Optional
    @ImportColumn("ANZGESCHOSSE")
    Property<Integer> anzahlGeschosse();


    // SANANFEND - String
    @Optional
    @ImportColumn("SANANFEND")
    // AENull
    Property<String> sanierungswert();


    // DENKMALSCHUTZ - String
    @Optional
    @ImportColumn("DENKMALSCHUTZ")
    Property<String> denkmalschutz();


    @Optional
    @Computed
    Property<String> schl();


    @Optional
    ManyAssociation<FlurstueckComposite> flurstuecke();


    // TODO Wohnungseigentum als Association, auch beim Import beachten

    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements GebaeudeComposite {

        private static Log log = LogFactory.getLog( Mixin.class );


        @Override
        public Property<String> schl() {
            return new ComputedPropertyInstance<String>( new GenericPropertyInfo( WohnungseigentumComposite.class,
                    "schl" ) ) {

                @Override
                public String get() {
                    return objektNummer().get() + "/" + gebaeudeNummer().get();
                }


                @Override
                public void set( String anIgnoredValue )
                        throws IllegalArgumentException, IllegalStateException {
                }
            };
        }


        public static Iterable<GebaeudeComposite> forEntity( WohnungseigentumComposite eigentum ) {
            GebaeudeComposite template = QueryExpressions.templateFor( GebaeudeComposite.class );
            BooleanExpression expr = QueryExpressions.eq( template.objektNummer(), eigentum.objektNummer().get() );
            Query<GebaeudeComposite> matches = KapsRepository.instance().findEntities( GebaeudeComposite.class, expr,
                    0, -1 );
            return matches;
        }


        public static GebaeudeComposite forKeys( final Integer objektNummer, final Integer gebaeudeNummer ) {
            GebaeudeComposite template = QueryExpressions.templateFor( GebaeudeComposite.class );
            BooleanExpression expr = QueryExpressions.and(
                    QueryExpressions.eq( template.objektNummer(), objektNummer ),
                    QueryExpressions.eq( template.gebaeudeNummer(), gebaeudeNummer ) );
            Query<GebaeudeComposite> matches = KapsRepository.instance().findEntities( GebaeudeComposite.class, expr,
                    0, 1 );
            return matches.find();
        }
    }
}
