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

import java.util.Date;

import org.opengis.feature.Property;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class DateMustAfterValidator
        extends DateCompareValidator {

    public DateMustAfterValidator( Property otherDateProperty, String errorMessage ) {
        super(otherDateProperty, errorMessage);
    }


    @Override
    public String validate( Object fieldValue ) {
        if (fieldValue == null) {
            return "Dieses Attribut darf nicht leer sein";
        }
        Date currentDate = (Date)fieldValue;
        if (currentDate != null) {
            Date otherDate = (Date)otherDateProperty.getValue();
            if (otherDate != null && !currentDate.after( otherDate )) {
                return errorMessage;
            }
        }
        return null;
    }

}
