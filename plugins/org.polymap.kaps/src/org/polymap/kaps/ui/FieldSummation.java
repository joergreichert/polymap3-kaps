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

import java.util.HashMap;
import java.util.Map;

import java.text.NumberFormat;

import org.qi4j.api.property.Property;

import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.form.IFormEditorPageSite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class FieldSummation
        implements IFormFieldListener {

    private final IFormEditorPageSite           site;

    private final Property<Double>              result;

    private Double                              factor1Value;

    private Double                              factor2Value;

    private final int                           fractionDigits;

    private final Map<String, Property<Double>> terms;

    private final Map<String, Double>           values;


    public FieldSummation( IFormEditorPageSite site, int fractionDigits, Property<Double> result,
            Property<Double>... summand ) {
        this.site = site;
        this.fractionDigits = fractionDigits;
        this.result = result;
        terms = new HashMap<String, Property<Double>>();
        for (Property<Double> term : summand) {
            terms.put( term.qualifiedName().name(), term );
        }
        values = new HashMap<String, Double>();
    }


    @Override
    public void fieldChange( FormFieldEvent ev ) {
        if (ev.getNewValue() == null || ev.getEventCode() != IFormFieldListener.VALUE_CHANGE) {
            return;
        }
        String fieldName = ev.getFieldName();
        if (terms.keySet().contains( fieldName )) {
            values.put( fieldName, (Double)ev.getNewValue() );
            refreshResult();
        }
    }


    private void refreshResult() {
        Double resultValue = 0.0;

        for (String term : terms.keySet()) {
            // value changed, then look here
            Double termValue = values.get( term );
            if (termValue == null) {
                // value unchanged then look in the base entity
                termValue = terms.get( term ).get();
            }
            if (termValue != null) {
                resultValue += termValue;
            }
        }
        site.setFieldValue( result.qualifiedName().name(), getFormatter().format( resultValue ) );
    }


    /**
     * 
     * @return
     */
    private NumberFormat getFormatter() {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits( fractionDigits );
        nf.setMinimumFractionDigits( fractionDigits );
        nf.setMinimumIntegerDigits( 1 );
        return nf;
    }
}
