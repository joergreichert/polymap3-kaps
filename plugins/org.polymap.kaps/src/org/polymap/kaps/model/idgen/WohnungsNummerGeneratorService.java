/*
 * polymap.org Copyright 2010, Falko Br�utigam, and other contributors as indicated
 * by the @authors tag.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * $Id: $
 */
package org.polymap.kaps.model.idgen;

import static org.qi4j.api.query.QueryExpressions.orderBy;
import static org.qi4j.api.query.QueryExpressions.templateFor;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.service.ServiceComposite;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.data.GebaeudeComposite;
import org.polymap.kaps.model.data.WohnungComposite;
import org.polymap.kaps.model.data.WohnungseigentumComposite;

@Mixins(WohnungsNummerGeneratorService.Mixin.class)
public interface WohnungsNummerGeneratorService
        extends ServiceComposite {

    Integer generate( GebaeudeComposite parent );


    public abstract class Mixin
            implements WohnungsNummerGeneratorService {

        private static final Log     log     = LogFactory.getLog( WohnungsNummerGeneratorService.class );

        private Map<String, Integer> highest = new HashMap<String, Integer>();


        public synchronized Integer generate( GebaeudeComposite parent ) {
            Integer schl = highest.get( parent.schl().get() );
            if (schl == null) {
                WohnungComposite template = templateFor( WohnungComposite.class );

                Query<WohnungComposite> entities = KapsRepository.instance()
                        .findEntities(
                                WohnungComposite.class,
                                QueryExpressions.and( QueryExpressions.eq( template.objektNummer(), parent
                                        .objektNummer().get() ), QueryExpressions.eq( template.objektFortfuehrung(),
                                        parent.objektFortfuehrung().get() ), QueryExpressions.eq(
                                        template.gebaeudeNummer(), parent.gebaeudeNummer().get() ), QueryExpressions
                                        .eq( template.gebaeudeFortfuehrung(), parent.gebaeudeFortfuehrung().get() ) ),
                                0, -1 );
                entities.orderBy( orderBy( template.wohnungsNummer(), OrderBy.Order.DESCENDING ) );

                WohnungComposite v = entities.iterator().hasNext() ? entities.iterator().next() : null;
                schl = Integer.valueOf( v != null && v.wohnungsNummer().get() != null ? v.wohnungsNummer().get() : 0 );
            }
            schl = Integer.valueOf( schl + 1 );
            highest.put( parent.schl().get(), schl );

            return schl;
        }


        public Mixin() {
        }
    }

}
