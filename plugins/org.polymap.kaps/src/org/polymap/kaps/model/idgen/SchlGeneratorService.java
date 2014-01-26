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
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.service.ServiceComposite;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.SchlNamed;

@Mixins(SchlGeneratorService.Mixin.class)
public interface SchlGeneratorService
        extends ServiceComposite {

    Integer generate( Class type );


    public abstract class Mixin
            implements SchlGeneratorService {

        private static final Log    log     = LogFactory.getLog( SchlGeneratorService.class );

        private Map<Class, Integer> highest = new HashMap<Class, Integer>();


        public synchronized Integer generate( Class type ) {
            Integer schl = highest.get( type );
            if (schl == null) {
                // schl is a string and does not support numeric sorting
                SchlNamed template = (SchlNamed)templateFor( type );

                Query<SchlNamed> entities = KapsRepository.instance().findEntities( type, null, 0, 100000 );
                int currentSchl = 0;
                for (SchlNamed schlNamed : entities) {
                    String schlNamedSchl = schlNamed.schl().get();
                    if (schlNamedSchl != null && Integer.parseInt( schlNamedSchl ) > currentSchl) {
                        currentSchl = Integer.parseInt( schlNamedSchl );
                    }
                }
                schl = Integer.valueOf( currentSchl );
            }
            schl = Integer.valueOf( schl + 1 );
            highest.put( type, schl );

            return schl;
        }


        public Mixin() {
        }
    }

}
