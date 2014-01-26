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
package org.polymap.kaps.ui.form;

import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.data.GemeindeComposite;
import org.polymap.kaps.model.data.RichtwertzoneComposite;
import org.polymap.kaps.model.data.RichtwertzoneZeitraumComposite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class RichtwertzoneProvider {

    /**
     * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
     */
    public static class RWComparator
            implements Comparator<String> {

        @Override
        public int compare( String o1, String o2 ) {
            if (o1 == null || o1.isEmpty()) {
                if (o2 == null || o2.isEmpty()) {
                    // beide leer
                    return 0;
                }
                // o2 scheint nicht leer zu sein
                return -1;
            }
            char o1s = o1.charAt( 0 );
            char o2s = o2.charAt( 0 );
            if (o1s == o2s) {
                return o1.compareTo( o2 );
            }
            if ('2' == o1s) {
                return 1;
            }
            else if ('2' == o2s) {
                return -1;
            }
            else if ('3' == o1s) {
                return 1;
            }
            else if ('3' == o2s) {
                return -1;
            }
            else if ('4' == o1s) {
                return 1;
            }
            else if ('4' == o2s) {
                return -1;
            }
            return o1.compareTo( o2 );
        }
    }

    private static Log log = LogFactory.getLog( RichtwertzoneProvider.class );


    /**
     * 
     * @param gemeindeComposite
     * @param date
     * @return
     */
    public static SortedMap<String, Object> findFor( GemeindeComposite gemeinde, Date date ) {
        if (gemeinde == null) {
            throw new IllegalArgumentException( "gemeinde must not be null" );
        }
        if (date == null) {
            throw new IllegalArgumentException( "date must not be null" );
        }
        TreeMap<String, Object> zonen = new TreeMap<String, Object>(
                new RWComparator() );
        Iterable<RichtwertzoneComposite> iterable = RichtwertzoneComposite.Mixin.findZoneIn( gemeinde );
        for (RichtwertzoneComposite zone : iterable) {
            String prefix = zone.schl().get();
            if (prefix.startsWith( "00" )) {
                prefix = "*" + prefix;
            }

            // find zeitraum
            RichtwertzoneZeitraumComposite zeitraum = RichtwertzoneZeitraumComposite.Mixin.findZeitraumFor( zone, date );
            if (zeitraum != null) {
                zonen.put(
                        prefix + " - " + zone.name().get() + " ("
                                + KapsRepository.SHORT_DATE.format( zeitraum.gueltigAb().get() ) + ")", zeitraum );
            }
        }
        return zonen.descendingMap();
    }
    
    public static void main(String[] args) {
        TreeMap<String, RichtwertzoneZeitraumComposite> zonen = new TreeMap<String, RichtwertzoneZeitraumComposite>(
                new RWComparator() );
        zonen.put( "01", null );
        zonen.put( "21", null );
        zonen.put( "31", null );
        zonen.put( "21", null );
        zonen.put( "411", null );
        zonen.put( "*00", null );
        zonen.put( "88", null );
        zonen.put( "00", null );
        
        NavigableMap<String, RichtwertzoneZeitraumComposite> map = zonen.descendingMap();
        
        for (String key : map.keySet()) {
            System.out.println(key);
        }
    }

    public static SortedMap<String, Object> findFor( GemeindeComposite gemeinde ) {
        TreeMap<String, Object> zonen = new TreeMap<String, Object>(new RWComparator());
        Iterable<RichtwertzoneComposite> iterable = RichtwertzoneComposite.Mixin.findZoneIn( gemeinde );
        for (RichtwertzoneComposite zone : iterable) {
            String prefix = zone.schl().get();
            if (prefix.startsWith( "00" )) {
                prefix = "*" + prefix;
            }
            zonen.put( prefix + " - " + zone.name().get(), zone );
        }
        return zonen.descendingMap();
    }
}
