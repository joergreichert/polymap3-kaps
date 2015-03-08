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
package org.polymap.kaps.ui;

import java.util.Locale;

import java.text.NumberFormat;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class NumberFormatter {

    public final static NumberFormat getFormatter( int fractionDigits ) {
        return getFormatter( fractionDigits, fractionDigits > 0 );
    }


    public final static NumberFormat getFormatter( int fractionDigits, boolean useGrouping ) {
        return getFormatter( fractionDigits, fractionDigits, useGrouping );
    }


    public final static NumberFormat getFormatter( int maxFractionDigits, int minFractionDigits, boolean useGrouping ) {
        NumberFormat nf = NumberFormat.getInstance(Locale.GERMANY);
        nf.setMaximumFractionDigits( maxFractionDigits );
        nf.setMinimumFractionDigits( minFractionDigits );
        nf.setMinimumIntegerDigits( 1 );
        nf.setGroupingUsed( useGrouping );
        return nf;
    }
}
