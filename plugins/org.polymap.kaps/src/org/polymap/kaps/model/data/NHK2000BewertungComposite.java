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
@Mixins({ NHK2000BewertungComposite.Mixin.class, PropertyChangeSupport.Mixin.class, ModelChangeSupport.Mixin.class,
        QiEntity.Mixin.class
// JsonState.Mixin.class
})
@ImportTable("K_BEWERTBGF00")
public interface NHK2000BewertungComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite {

    final static String NAME = "NHK 2000 - Bewertung";


    @Optional
    Association<VertragComposite> vertrag();

    // GESBAUWERT - Double
    @Optional
    @ImportColumn("GESBAUWERT")
    Property<Double> GESBAUWERT();


    // ZWSUMSONST - Double
    @Optional
    @ImportColumn("ZWSUMSONST")
    Property<Double> ZWSUMSONST();


    // ZEITWTEXT - String
    @Optional
    @ImportColumn("ZEITWTEXT")
    Property<String> ZEITWTEXT();


    // PROZAUSSEN - String
    @Optional
    @ImportColumn("PROZAUSSEN")
    Property<String> PROZAUSSEN();


    // AUSVHBETR - Double
    @Optional
    @ImportColumn("AUSVHBETR")
    Property<Double> AUSVHBETR();


    // SUMWOHNFL - Double
    @Optional
    @ImportColumn("SUMWOHNFL")
    Property<Double> SUMWOHNFL();


    // SUMBGF - Double
    @Optional
    @ImportColumn("SUMBGF")
    Property<Double> SUMBGF();

    @Optional
    Property<Double> BERZEITW1();

    // GFAKTOR1 - Double
    @Optional
    @ImportColumn("GFAKTOR1")
    Property<Double> GFAKTOR1();


    // GWERT1 - Double
    @Optional
    @ImportColumn("GWERT1")
    Property<Double> GWERT1();


    // GFAKTOR2 - Double
    @Optional
    @ImportColumn("GFAKTOR2")
    Property<Double> GFAKTOR2();


    // GWERT2 - Double
    @Optional
    @ImportColumn("GWERT2")
    Property<Double> GWERT2();


    // MAKLERBW - String
    @Optional
    //@ImportColumn("MAKLERBW")
    Property<Boolean> MAKLERBW();


    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements NHK2000BewertungComposite {

        private static Log log = LogFactory.getLog( Mixin.class );


        public final static NHK2000BewertungComposite forVertrag( VertragComposite vertrag ) {
            NHK2000BewertungComposite template = QueryExpressions.templateFor( NHK2000BewertungComposite.class );
            BooleanExpression expr = QueryExpressions.eq( template.vertrag(), vertrag );
            return KapsRepository.instance().findEntities( NHK2000BewertungComposite.class, expr, 0, 1 ).find();
        }
    }

}
