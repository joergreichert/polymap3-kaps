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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.service.ServiceComposite;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.data.WohnungseigentumComposite;

@Mixins(ObjektNummerGeneratorService.Mixin.class)
public interface ObjektNummerGeneratorService
        extends ServiceComposite {

    Integer generate();

    public abstract class Mixin
            implements ObjektNummerGeneratorService {

        private static final Log      log            = LogFactory.getLog( ObjektNummerGeneratorService.class );

        private Integer highestObjektNummer = null;


        public synchronized Integer generate() {
            if (highestObjektNummer == null) {
                WohnungseigentumComposite template = templateFor( WohnungseigentumComposite.class );

                Query<WohnungseigentumComposite> entities = KapsRepository.instance().findEntities( WohnungseigentumComposite.class, null, 0, -1 );
                entities.orderBy( orderBy( template.objektNummer(), OrderBy.Order.DESCENDING ) );

                WohnungseigentumComposite v = entities.iterator().next();
                highestObjektNummer = v != null ? v.objektNummer().get() : Integer.valueOf( 0 );
            }
            highestObjektNummer += 1;
            return highestObjektNummer;
        }


        public Mixin() {
        }
    }

}
