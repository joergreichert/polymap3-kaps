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

import org.qi4j.api.property.Property;

import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.MathUtil;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class FieldMultiplication
        implements IFormFieldListener {

    private static Log                log = LogFactory.getLog( FieldMultiplication.class );

    private final IFormEditorPageSite site;

    private final Property<Double>    factor1;

    private final Property<Double>    factor2;

    private final Property<Double>    result;

    private Double                    factor1Value;

    private Double                    factor2Value;

    private final int                 fractionDigits;

    private final boolean             roundUp;


    public FieldMultiplication( IFormEditorPageSite site, int fractionDigits, final Property<Double> factor1,
            final Property<Double> factor2, Property<Double> result ) {
        this( site, fractionDigits, false, factor1, factor2, result );

    }


    public FieldMultiplication( IFormEditorPageSite site, int fractionDigits, final boolean roundUp,
            final Property<Double> factor1, final Property<Double> factor2, Property<Double> result ) {
        this.site = site;
        this.fractionDigits = fractionDigits;
        this.roundUp = roundUp;
        this.factor1 = factor1;
        this.factor2 = factor2;
        this.result = result;
    }


    @Override
    public void fieldChange( FormFieldEvent ev ) {
        if (ev.getEventCode() != IFormFieldListener.VALUE_CHANGE) {
            return;
        }
        String fieldName = ev.getFieldName();
        if (fieldName.equals( factor1.qualifiedName().name() )) {
            Double newValue = (Double)ev.getNewValue(); // explizitely deleting this
                                                        // value
            if (newValue == null) {
                newValue = Double.valueOf( 0.0d );
            }
            factor1Value = newValue;
            refreshResult();
        }
        else if (fieldName.equals( factor2.qualifiedName().name() )) {
            Double newValue = (Double)ev.getNewValue(); // explizitely deleting this
                                                        // value
            if (newValue == null) {
                newValue = Double.valueOf( 0.0d );
            }
            factor2Value = newValue;
            refreshResult();
        }
    }


    private void refreshResult() {
        Double f1 = factor1Value == null ? factor1.get() : factor1Value;
        Double f2 = factor2Value == null ? factor2.get() : factor2Value;

        Double resultValue = (f1 == null ? 0 : f1) * (f2 == null ? 0 : f2);
        if (roundUp) {
            resultValue = MathUtil.round( resultValue );
        }
        site.setFieldValue( result.qualifiedName().name(),
                NumberFormatter.getFormatter( fractionDigits ).format( resultValue ) );
    }
}
