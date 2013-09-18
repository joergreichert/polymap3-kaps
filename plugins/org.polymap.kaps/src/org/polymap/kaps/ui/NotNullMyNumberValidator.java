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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class NotNullMyNumberValidator
        extends MyNumberValidator {

    /**
     * 
     * @param targetClass
     */
    public NotNullMyNumberValidator( Class<? extends Number> targetClass ) {
        super( targetClass );
    }


    public NotNullMyNumberValidator( Class<? extends Number> targetClass , int fractionDigits ) {
        super( targetClass, fractionDigits );
    }



    private static Log log = LogFactory.getLog( NotNullMyNumberValidator.class );


    
    @Override
    public String validate( Object fieldValue ) {
        if (fieldValue == null ||
                // wird auch für TextField verwendet, mit der Bedeutung: "nicht leer"
                (fieldValue instanceof String && ((String)fieldValue).length() == 0)) {
            return "Dieses Attribut darf nicht leer sein";
        }
        return super.validate( fieldValue );
    }
}
