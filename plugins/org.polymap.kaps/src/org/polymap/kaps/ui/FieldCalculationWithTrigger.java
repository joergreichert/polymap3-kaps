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


/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public abstract class FieldCalculationWithTrigger
        extends FieldCalculation {

    private static Log log = LogFactory.getLog( FieldCalculationWithTrigger.class );
    private final Property<Boolean> trigger;
    private Boolean triggerValue;


    public FieldCalculationWithTrigger( IFormEditorPageSite site, int fractionDigits, Property<Double> result, Property<Boolean> trigger, 
            Property<Double>... operators ) {
        super( site, fractionDigits, result, operators );
        this.trigger = trigger;
    }
    
    @Override
    public void fieldChange( FormFieldEvent ev ) {
        if (ev.getEventCode() != IFormFieldListener.VALUE_CHANGE) {
            return;
        }
        String fieldName = ev.getFieldName();
        if (trigger.qualifiedName().name().equals( fieldName )) {
            Boolean newValue = (Boolean)ev.getNewValue(); //explizitely deleting this value
            if (newValue == null) {
                newValue = Boolean.FALSE;
            }
            triggerValue = newValue;
            refreshResult();
        }
        super.fieldChange( ev );
    }
    
    protected Boolean triggerValue() {
        return triggerValue == null ? trigger.get() : triggerValue;
    }
}
