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

import static org.qi4j.api.query.QueryExpressions.orderBy;

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
import org.qi4j.api.query.grammar.OrderBy;

import org.polymap.core.qi4j.QiEntity;
import org.polymap.core.qi4j.event.ModelChangeSupport;
import org.polymap.core.qi4j.event.PropertyChangeSupport;

import org.polymap.kaps.importer.ImportColumn;
import org.polymap.kaps.importer.ImportTable;
import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.SchlNamed;

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
@Concerns({ PropertyChangeSupport.Concern.class })
@Mixins({ GemarkungComposite.Mixin.class, PropertyChangeSupport.Mixin.class, ModelChangeSupport.Mixin.class,
        QiEntity.Mixin.class
// JsonState.Mixin.class
})
@ImportTable("K_GEMFLU")
public interface GemarkungComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite, SchlNamed {

    final static String NAME = "Gemarkung";


    @Optional
    @ImportColumn("SCHL")
    Property<String> schl();


    @Optional
    @ImportColumn("TEXT")
    Property<String> name();


    @Optional
    // FLURSCHL
    Association<FlurComposite> flur();


    @Optional
    // GEMEINDE
    Association<GemeindeComposite> gemeinde();


    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements GemarkungComposite {

        private static Log log = LogFactory.getLog( Mixin.class );


        public static Iterable<GemarkungComposite> forGemeinde( GemeindeComposite gemeinde ) {
            GemarkungComposite template = QueryExpressions.templateFor( GemarkungComposite.class );
            BooleanExpression expr = QueryExpressions.eq( template.gemeinde(), gemeinde );
            Query<GemarkungComposite> matches = KapsRepository.instance().findEntities( GemarkungComposite.class, expr,
                    0, -1 );
            return matches;
        }
    }

}
