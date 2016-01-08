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

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.rhei.field.IFormFieldValidator;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class MyNumberValidator
        implements IFormFieldValidator {

    private static Log              log = LogFactory.getLog( MyNumberValidator.class );

    private NumberFormat            nf;

    private Class<? extends Number> targetClass;


    /**
     * Creates an instance with min integer/fraction digits set to 0 and max
     * integer/fraction digits set to 10.
     * 
     * @param locale The locale to use. Null indicates the the current default locale
     *        is to be used.
     */
    public MyNumberValidator( Class<? extends Number> targetClass ) {
        this( targetClass, 0, 0, false );
    }


    /**
     * Creates an instance with min integer and min fraction digits set to 0.
     * 
     * @param maxIntegerDigits
     * @param maxFractionDigits
     * @param locale The locale to use. Null indicates the the current default locale
     *        is to be used.
     */
    public MyNumberValidator( Class<? extends Number> targetClass, int maxFractionDigits, int minFractionDigits ) {
        this( targetClass, maxFractionDigits, minFractionDigits, true );
    }

    public MyNumberValidator( Class<? extends Number> targetClass, int fractionDigits ) {
        this( targetClass, fractionDigits, fractionDigits, true );
    }

    /**
     * 
     * @param maxIntegerDigits
     * @param maxFractionDigits
     * @param minIntegerDigits
     * @param minFractionDigits
     * @param locale The locale to use. Null indicates the the current default locale
     *        is to be used.
     */
    public MyNumberValidator( Class<? extends Number> targetClass, int maxFractionDigits, int minFractionDigits,
            boolean useGrouping ) {

        this.targetClass = targetClass;

        nf = NumberFormatter.getFormatter( maxFractionDigits, minFractionDigits, useGrouping );
    }


    public String validate( Object fieldValue ) {
        if (fieldValue instanceof String) {
            try {
                transform2Model( fieldValue );
                return null;
            }
            catch (Exception e) {
                log.error( "value: " + fieldValue + " INVALID!", e );
                return "Eingabe ist keine korrekte Zahlenangabe: " + fieldValue + "\nAnzahl Stellen vor dem Komma: "
                        + nf.getMinimumIntegerDigits() + "-" + nf.getMaximumIntegerDigits()
                        + "\nAnzahl Stellen nach dem Komma: " + nf.getMinimumFractionDigits() + "-"
                        + nf.getMaximumFractionDigits();
            }
        }
        return null;
    }


    public Object transform2Model( Object fieldValue )
            throws Exception {
        if (fieldValue == null) {
            return null;
        }
        else if (fieldValue instanceof String) {
            if (((String)fieldValue).isEmpty()) {
                return null;
            }
            ParsePosition pp = new ParsePosition( 0 );
            Number result = nf.parse( (String)fieldValue, pp );

            if (pp.getErrorIndex() > -1 || pp.getIndex() < ((String)fieldValue).length()) {
                throw new ParseException( "field value: " + fieldValue + " for targetClass " + targetClass.getName(), pp.getErrorIndex());
            }

            log.debug( "value: " + fieldValue + " -> " + result.doubleValue() );

            // XXX check max digits

            if (Float.class.isAssignableFrom( targetClass )) {
                return Float.valueOf( result.floatValue() );
            }
            else if (Double.class.isAssignableFrom( targetClass )) {
                return Double.valueOf( result.doubleValue() );
            }
            else if (Integer.class.isAssignableFrom( targetClass )) {
                return Integer.valueOf( result.intValue() );
            }
            else if (Long.class.isAssignableFrom( targetClass )) {
                return Long.valueOf( result.longValue() );
            }
            else {
                throw new RuntimeException( "Unsupported target type: " + targetClass );
            }
        }
        else {
            throw new RuntimeException( "Unhandled field value type: " + fieldValue );
        }
    }


    public Object transform2Field( Object modelValue )
            throws Exception {
        if (modelValue == null ||  modelValue instanceof String) {
            return modelValue;
        }
        return nf.format( targetClass.cast( modelValue ) );
    }

}
