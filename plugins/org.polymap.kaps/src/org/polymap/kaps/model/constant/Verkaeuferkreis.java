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
package org.polymap.kaps.model.constant;

import org.polymap.rhei.model.ConstantWithSynonyms;

/**
 * Provides 'Erhaltungszustand' constants.
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class Verkaeuferkreis
        extends ConstantWithSynonyms<String> {

    /** Provides access to the elements of this type. */
    public static final Type<Verkaeuferkreis, String> all          = new Type<Verkaeuferkreis, String>();

    public static final Verkaeuferkreis               unbestimmt   = new Verkaeuferkreis(
                                                                           0,
                                                                           "0 - noch nicht bestimmt",
                                                                           "noch nicht bestimmt" );

    public static final Verkaeuferkreis               guenstig     = new Verkaeuferkreis( 1,
                                                                           "1 - g�nstig", "g�nstig" );

    public static final Verkaeuferkreis               unzureichend = new Verkaeuferkreis(
                                                                           2,
                                                                           "2 - ung�nstig/unzureichend",
                                                                           "unzureichend" );

    public static final Verkaeuferkreis               schlecht     = new Verkaeuferkreis(
                                                                           3,
                                                                           "3 - ung�nstig/schlecht",
                                                                           "schlecht" );

    // instance *******************************************

    private String                                    description;


    private Verkaeuferkreis( int id, String label, String description, String... synonyms ) {
        super( id, label, synonyms );
        this.description = description;
        all.add( this );
    }


    protected String normalizeValue( String value ) {
        return value.trim().toLowerCase();
    }

}
