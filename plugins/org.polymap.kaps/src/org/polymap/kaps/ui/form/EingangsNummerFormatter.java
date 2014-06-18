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
package org.polymap.kaps.ui.form;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.rhei.field.IFormFieldValidator;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class EingangsNummerFormatter
        implements IFormFieldValidator {

    private static Log log = LogFactory.getLog( EingangsNummerFormatter.class );


    @Override
    public String validate( Object fieldValue ) {
        return null;
    }


    @Override
    public Object transform2Model( Object fieldValue )
            throws Exception {
        if (fieldValue != null) {
            String text = fieldValue.toString();
            text = text.replaceAll( "/", "" );
            return Integer.parseInt( text );
        }
        return fieldValue;
    }


    @Override
    public Object transform2Field( Object modelValue )
            throws Exception {
        if (modelValue != null) {
            return format( modelValue.toString() );
        }
        return modelValue;
    }

    public final static String format( Integer in ) {
        if (in != null) {
            return format( in.toString());
        }
        return null;
    }
    
    public final static String format( String in ) {
        if (in != null && in.length() > 4 && in.indexOf( "/" ) == -1) {
            return in.substring( 0, 4 ) + "/" + in.substring( 4 );
        }
        return in;
    }
}
