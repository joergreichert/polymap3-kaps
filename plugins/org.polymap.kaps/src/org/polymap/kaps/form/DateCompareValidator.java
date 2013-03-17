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
package org.polymap.kaps.form;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.property.Property;

import org.polymap.rhei.field.IFormFieldValidator;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public abstract class DateCompareValidator implements IFormFieldValidator {

    private static Log             log = LogFactory.getLog( DateMustBeforeValidator.class );

    protected final Property<Date> otherDateProperty;

    protected final String         errorMessage;


    /**
     * 
     */
    public DateCompareValidator( Property<Date> laterDateProperty, String errorMessage ) {
        this.otherDateProperty = laterDateProperty;
        this.errorMessage = errorMessage;

    }


    @Override
    public Object transform2Model( Object fieldValue )
            throws Exception {
        return fieldValue;
    }


    @Override
    public Object transform2Field( Object modelValue )
            throws Exception {
        return modelValue;
    }
}