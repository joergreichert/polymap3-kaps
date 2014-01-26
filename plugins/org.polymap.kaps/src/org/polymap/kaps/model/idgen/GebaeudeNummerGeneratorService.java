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
import org.polymap.kaps.model.SchlNamed;
import org.polymap.kaps.model.data.GebaeudeComposite;
import org.polymap.kaps.model.data.WohnungseigentumComposite;

@Mixins(GebaeudeNummerGeneratorService.Mixin.class)
public interface GebaeudeNummerGeneratorService
        extends ServiceComposite {

    Integer generate(WohnungseigentumComposite parent);

    public abstract class Mixin
            implements GebaeudeNummerGeneratorService {

        private static final Log      log            = LogFactory.getLog( GebaeudeNummerGeneratorService.class );

        private Map<String, Integer> highest = new HashMap<String, Integer>();


        public synchronized Integer generate(WohnungseigentumComposite parent) {
            Integer schl = highest.get( parent.schl().get() );
            if (schl == null) {
                GebaeudeComposite template = templateFor( GebaeudeComposite.class );

                Query<GebaeudeComposite> entities = KapsRepository.instance().findEntities( GebaeudeComposite.class, QueryExpressions.and(
                        QueryExpressions.eq( template.objektNummer(), parent.objektNummer().get() ),
                        QueryExpressions.eq( template.objektFortfuehrung(), parent.objektFortfuehrung().get() ) ), 0, -1 );
                entities.orderBy( orderBy( template.gebaeudeNummer(), OrderBy.Order.DESCENDING ) );

                GebaeudeComposite v = entities.iterator().hasNext() ? entities.iterator().next() : null;
                schl = Integer.valueOf( v != null && v.gebaeudeNummer().get() != null ? v.gebaeudeNummer().get() : 0 );
            }
            schl = Integer.valueOf( schl + 1 );
            highest.put( parent.schl().get(), schl );

            return schl;
        }

        public Mixin() {
        }
    }

}
