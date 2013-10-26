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
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;

import org.polymap.core.qi4j.QiEntity;
import org.polymap.core.qi4j.event.ModelChangeSupport;
import org.polymap.core.qi4j.event.PropertyChangeSupport;

import org.polymap.kaps.importer.ImportColumn;
import org.polymap.kaps.model.KapsRepository;

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
@Concerns({ PropertyChangeSupport.Concern.class })
@Mixins({ VertragsdatenErweitertComposite.Mixin.class, PropertyChangeSupport.Mixin.class,
        ModelChangeSupport.Mixin.class, QiEntity.Mixin.class,
// JsonState.Mixin.class
})
// @ImportTable("K_BEVERW")
public interface VertragsdatenErweitertComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite {

    String NAME = "Erweiterte Vertragsdaten";


    // KAUFPREIS DOUBLE,
    @Optional
    @ImportColumn("KAUFPREIS")
    Property<Double> basispreis();


    // ZUSCHLAG DOUBLE,
    @Optional
    @ImportColumn("ZUSCHLAG")
    Property<Double> zuschlag();


    // ABSCHLAG DOUBLE,
    @Optional
    @ImportColumn("ABSCHLAG")
    Property<Double> abschlag();


    // BERKPREIS DOUBLE,
    @Optional
    @ImportColumn("BERKPREIS")
    Property<Double> bereinigterVollpreis();


    // ZUBEM1 VARCHAR(100),
    @Optional
    @ImportColumn("ZUBEM1")
    Property<String> zuschlagBemerkung();


    // ABBEM VARCHAR(100),
    @Optional
    @ImportColumn("ABBEM")
    Property<String> abschlagBemerkung();


    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements VertragsdatenErweitertComposite {

        private static Log log = LogFactory.getLog( Mixin.class );


        private VertragComposite kaufvertrag() {
            VertragComposite template = QueryExpressions.templateFor( VertragComposite.class );
            BooleanExpression expr = QueryExpressions.eq( template.erweiterteVertragsdaten(), this );
            Query<VertragComposite> matches = KapsRepository.instance().findEntities( VertragComposite.class, expr, 0,
                    1 );
            return matches.find();
        }
    }


    @Optional
    Property<Double> wertbeeinflussendeUmstaende();
}
