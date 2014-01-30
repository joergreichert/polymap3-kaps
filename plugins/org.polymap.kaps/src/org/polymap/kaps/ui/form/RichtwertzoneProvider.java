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
import java.util.HashMap;
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

    public static class RWZComparator
            implements Comparator<String> {

        Map<String, RichtwertzoneZeitraumComposite> base;


        public RWZComparator( Map<String, RichtwertzoneZeitraumComposite> base ) {
            this.base = base;
        }


        @Override
        public int compare( String o1, String o2 ) {

            if (o1 == null || o1.isEmpty() || base.get( o1 ) == null) {
                if (o2 == null || o2.isEmpty() || base.get( o2 ) == null) {
                    // beide leer
                    return 0;
                }
                // o2 scheint nicht leer zu sein
                return -1;
            }
            Date g1 = base.get( o1 ).gueltigAb().get();
            Date g2 = base.get( o2 ).gueltigAb().get();
            if (g1 == null || g2 == null || g1.equals( g2 )) {
                return o1.compareTo( o2 );
            }
            return g1.compareTo( g2 );
        }
    }


    public static class RWComparator
            implements Comparator<String> {

        Map<String, RichtwertzoneComposite> base;


        public RWComparator( Map<String, RichtwertzoneComposite> base ) {
            this.base = base;
        }


        @Override
        public int compare( String o1, String o2 ) {

            if (o1 == null || o1.isEmpty() || base.get( o1 ) == null) {
                if (o2 == null || o2.isEmpty() || base.get( o2 ) == null) {
                    // beide leer
                    return 0;
                }
                // o2 scheint nicht leer zu sein
                return -1;
            }
            RichtwertzoneZeitraumComposite zz1 = base.get( o1 ).latest().get();
            RichtwertzoneZeitraumComposite zz2 = base.get( o2 ).latest().get();
            if (zz1 == null) {
                if (zz2 == null) {
                    return o1.compareTo( o2 );
                }
                return -1;
            }

            Date g1= zz1.gueltigAb().get();
            Date g2 = zz2.gueltigAb().get();
            
            if (g1 == null || g2 == null || g1.equals( g2 )) {
                return o1.compareTo( o2 );
            }
            return g1.compareTo( g2 );
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
        Map<String, RichtwertzoneZeitraumComposite> zonen = new HashMap<String, RichtwertzoneZeitraumComposite>();
        Iterable<RichtwertzoneComposite> iterable = RichtwertzoneComposite.Mixin.findZoneIn( gemeinde );
        for (RichtwertzoneComposite zone : iterable) {
            String prefix = zone.schl().get();

            // find zeitraum
            RichtwertzoneZeitraumComposite zeitraum = RichtwertzoneZeitraumComposite.Mixin.findZeitraumFor( zone, date );
            if (zeitraum != null) {
                zonen.put(
                        prefix + " - " + zone.name().get() + " ("
                                + KapsRepository.SHORT_DATE.format( zeitraum.gueltigAb().get() ) + ")", zeitraum );
            }
        }
        TreeMap<String, Object> sorted = new TreeMap<String, Object>( new RWZComparator( zonen ) );
        sorted.putAll( zonen );
        return sorted.descendingMap();
    }


    //
    // public static void main(String[] args) {
    // TreeMap<String, RichtwertzoneZeitraumComposite> zonen = new TreeMap<String,
    // RichtwertzoneZeitraumComposite>(
    // new RWComparator() );
    // zonen.put( "01", null );
    // zonen.put( "21", null );
    // zonen.put( "31", null );
    // zonen.put( "21", null );
    // zonen.put( "411", null );
    // zonen.put( "*00", null );
    // zonen.put( "88", null );
    // zonen.put( "00", null );
    //
    // NavigableMap<String, RichtwertzoneZeitraumComposite> map =
    // zonen.descendingMap();
    //
    // for (String key : map.keySet()) {
    // System.out.println(key);
    // }
    // }

    public static SortedMap<String, Object> findFor( GemeindeComposite gemeinde ) {
        Map<String, RichtwertzoneComposite> zonen = new HashMap<String, RichtwertzoneComposite>();
        Iterable<RichtwertzoneComposite> iterable = RichtwertzoneComposite.Mixin.findZoneIn( gemeinde );
        for (RichtwertzoneComposite zone : iterable) {
            String prefix = zone.schl().get();
            zonen.put( prefix + " - " + zone.name().get(), zone );
        }
        TreeMap<String, Object> sorted = new TreeMap<String, Object>( new RWComparator( zonen ) );
        sorted.putAll( zonen );
        return sorted.descendingMap();
    }
}
