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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.service.ServiceComposite;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.data.VertragComposite;

@Mixins(EingangsNummerGeneratorService.Mixin.class)
public interface EingangsNummerGeneratorService
        extends ServiceComposite {

    int generate( Date vertragsDatum );


    public abstract class Mixin
            implements EingangsNummerGeneratorService {

        private static final Log      log            = LogFactory.getLog( EingangsNummerGeneratorService.class );

        private Map<Integer, Integer> highestNumbers = new HashMap<Integer, Integer>();


        public Mixin() {
        }


        public synchronized int generate( Date vertragsDatum ) {
            Calendar cal = new GregorianCalendar();
            cal.setTime( vertragsDatum );
            cal.set( Calendar.DAY_OF_YEAR, 1 );
            cal.set( Calendar.HOUR_OF_DAY, 0 );
            cal.set( Calendar.MINUTE, 0 );
            cal.set( Calendar.SECOND, 0 );
            cal.set( Calendar.MILLISECOND, 0 );

            Date lowerDate = cal.getTime();

            // minimum aktuelles Jahr * 100000 + 1
            int currentYear = cal.get( Calendar.YEAR );

            Integer highest = highestNumbers.get( Integer.valueOf( currentYear ) );
            if (highest == null) {

                int currentMinimumNumber = currentYear * 100000;

                cal.roll( Calendar.YEAR, true );
                Date upperDate = cal.getTime();

                VertragComposite template = templateFor( VertragComposite.class );
                BooleanExpression exp = // QueryExpressions.and( QueryExpressions.ge(
                                        // template.vertragsDatum(), lowerDate ),
                QueryExpressions.lt( template.vertragsDatum(), upperDate );

                Query<VertragComposite> entities = KapsRepository.instance().findEntities( VertragComposite.class, exp,
                        0, -1 );
                entities.orderBy( orderBy( template.eingangsNr(), OrderBy.Order.DESCENDING ) );

                VertragComposite v = entities.iterator().next();
                int highestEingangsNr = v != null ? v.eingangsNr().get() : 0;

                highest = Math.max( highestEingangsNr, currentMinimumNumber );
            }
            highest = Integer.valueOf( highest + 1 );
            highestNumbers.put( currentYear, highest );
            return highest;
        }
    }

}
