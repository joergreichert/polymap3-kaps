/*
 * polymap.org Copyright 2013, Falko Bräutigam. All rights reserved.
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
package org.polymap.kaps.importer.test;

import java.util.Locale;

import java.text.NumberFormat;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class NumberFormatterTest
        extends TestCase {

    private static Log log = LogFactory.getLog( NumberFormatterTest.class );


    public void testThousands() {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());
        nf.setMaximumIntegerDigits( 10 );
        nf.setMaximumFractionDigits( 10 );
        nf.setMinimumIntegerDigits( 10 );
        nf.setMinimumFractionDigits( 10 );

        
        System.out.println( nf.format( 12345 ) );
    }
}
