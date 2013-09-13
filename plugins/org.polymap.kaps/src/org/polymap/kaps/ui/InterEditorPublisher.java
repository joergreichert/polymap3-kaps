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

import java.beans.PropertyChangeEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.property.Property;

import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldListener;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class InterEditorPublisher
        implements IFormFieldListener {

    private static Log             log = LogFactory.getLog( InterEditorPublisher.class );

//    private boolean                  onFlushing = false;

    private Map<String, Property<?>> terms;


    public InterEditorPublisher( Property... properties ) {
        terms = new HashMap<String, Property<?>>();
        for (Property<?> term : properties) {
            terms.put( term.qualifiedName().name(), term );
        }
    }


    @Override
    public void fieldChange( FormFieldEvent ev ) {
//        if (!onFlushing) {
            // avoid endless loops
//            onFlushing = true;
            if (ev.getSource() instanceof IFormField && ev.getEventCode() == IFormFieldListener.VALUE_CHANGE
                    && terms.keySet().contains( ev.getFieldName() )) {
                System.out.println( "Publishing " + ev.getFieldName() + " Property" + this.toString() );
                EventManager.instance().publish(
                        new PropertyChangeEvent( ev, ev.getFieldName(), terms.get( ev.getFieldName() ).get(), ev
                                .getNewValue() ) );

            }
//            onFlushing = false;
//        }
//        else {
//            System.out.println( "During flushing in Grunddaten skip" );
//        }
    }
}
