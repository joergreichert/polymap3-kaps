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
@Mixins({ NHK2010BewertungComposite.Mixin.class, PropertyChangeSupport.Mixin.class, ModelChangeSupport.Mixin.class,
        QiEntity.Mixin.class
// JsonState.Mixin.class
})
@ImportTable("K_BEWERTBGF10")
public interface NHK2010BewertungComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite {

    final static String NAME = "NHK 2010 - Bewertung";


    @Optional
    Association<VertragComposite> vertrag();


    // ANBAUTEN - String
    @Optional
    // TODO @ImportColumn("ANBAUTEN")
    ManyAssociation<NHK2010AnbautenComposite> anbauten();


    // ZWSUMSONST - Double
    @Optional
    @ImportColumn("ZWSUMSONST")
    Property<Double> nichtErfassteBauteile();


    // SUMZEITWERTE - Double
    @Optional
    @ImportColumn("SUMZEITWERTE")
    Property<Double> summeZeitwerte();


    // GESBAUWERT - Double
    @Optional
    @ImportColumn("GESBAUWERT")
    Property<Double> gesamtWert();


    // AUSVH - Double
    @Optional
    @ImportColumn("AUSVH")
    Property<Double> prozentwertDerAussenanlagen();


    // AUSVHBETR - Double
    @Optional
    @ImportColumn("AUSVHBETR")
    Property<Double> wertDerAussenanlagen();


    // PROZAUSSEN - String
    @Optional
    @ImportColumn("PROZAUSSEN")
    Property<Boolean> aussenAnlagenInProzent();


    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements NHK2010BewertungComposite {

        private static Log log = LogFactory.getLog( Mixin.class );


        public final static NHK2010BewertungComposite forVertrag( VertragComposite vertrag ) {
            NHK2010BewertungComposite template = QueryExpressions.templateFor( NHK2010BewertungComposite.class );
            BooleanExpression expr = QueryExpressions.eq( template.vertrag(), vertrag );
            return KapsRepository.instance().findEntities( NHK2010BewertungComposite.class, expr, 0, 1 ).find();
        }
    }

}
