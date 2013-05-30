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

import org.qi4j.api.property.Property;

import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class FieldListener
        implements IFormFieldListener {

    private final Map<String, Object> values = new HashMap<String, Object>();


    public void put( Property term, Object value ) {
        values.put( term.qualifiedName().name(), value );
    }


    public <T> T get( Property<T> term ) {
        return (T)values.get( term.qualifiedName().name() );
    }

    private final Map<String, Property<?>> terms;


    public FieldListener( Property... operators ) {
        terms = new HashMap<String, Property<?>>();
        for (Property<?> term : operators) {
            terms.put( term.qualifiedName().name(), term );
        }
        for (Property term : operators) {
            put( term, term.get() );
        }
    }


    @Override
    public void fieldChange( FormFieldEvent ev ) {
        if (ev.getEventCode() != IFormFieldListener.VALUE_CHANGE) {
            return;
        }
        String fieldName = ev.getFieldName();
        if (terms.keySet().contains( fieldName )) {
            Double newValue = (Double)ev.getNewValue(); //explizitely deleting this value
            if (newValue == null) {
                newValue = Double.valueOf( 0.0d );
            }
            put( terms.get( fieldName ), newValue );
        }
    }
}
