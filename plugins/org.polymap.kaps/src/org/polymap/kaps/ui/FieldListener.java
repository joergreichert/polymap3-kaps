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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.property.Property;

import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.form.FormEditor;
import org.polymap.rhei.form.IFormEditorPageSite;

/**
 * the only job of this listener is to store all field events, from within this
 * editor to support the lazy loading of different tabs
 * 
 * the method flush should be called from afterDoLoad() in den Page
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class FieldListener
        implements IFormFieldListener {

    /**
     * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
     */
    public static class EventFilter
            implements org.polymap.core.runtime.event.EventFilter<FormFieldEvent> {

        private static Log       log = LogFactory.getLog( EventFilter.class );

        private final FormEditor editor;


        public EventFilter( final FormEditor editor ) {
            this.editor = editor;
        }


        public boolean apply( FormFieldEvent ev ) {
            return ev.getEventCode() == IFormFieldListener.VALUE_CHANGE && ev.getEditor() == editor;
        }
    }

    private static Log                log    = LogFactory.getLog( FieldListener.class );

    private final Map<String, Object> values = new HashMap<String, Object>();


    public void put( Property term, Object value ) {
        values.put( term.qualifiedName().name(), value );
    }


    public <T> T get( Property<T> term ) {
        T result = (T)values.get( term.qualifiedName().name() );
        if (result == null) {
            result = term.get();
        }
        return result;
    }

    private final Map<String, Property<?>> terms;

    private boolean                        blocked = false;


    public FieldListener( Property... operators ) {
        terms = new HashMap<String, Property<?>>();
        for (Property<?> term : operators) {
            terms.put( term.qualifiedName().name(), term );
        }
    }

    private final List<String> currentCalls = new ArrayList<String>();


    @Override
    public void fieldChange( FormFieldEvent ev ) {
        if (!blocked && ev.getEventCode() != IFormFieldListener.VALUE_CHANGE) {
            return;
        }
        // log.info( "field change for " + ev.toString() + " on  " + this );
        String fieldName = ev.getFieldName();
        if (terms.keySet().contains( fieldName )) {
            put( terms.get( fieldName ), ev.getNewValue() );
        }
        // if (site != null && !blocked) {
        // synchronized (currentCalls) {
        // if (!currentCalls.contains( fieldName.intern() )) {
        // currentCalls.add( fieldName.intern() );
        // // avoid endless loops
        // log.info( "onchange for " + site.toString() + " and field " + fieldName +
        // " and Value "
        // + values.get( fieldName ) );
        // onChangedValue( site, fieldName, values.get( fieldName ) );
        // currentCalls.remove( fieldName.intern() );
        // }
        // }
        // }
    }


    //
    // public void unBlock() {
    // blocked = false;
    // }

    public final void flush( IFormEditorPageSite site ) {
        // after flush, no more events would be tracked
        // initial flush, block after that call
        // this.site = site;
        blocked = true;
        for (String fieldName : values.keySet()) {
            log.info( "Flush for site " + site.toString() + " and field " + fieldName + " and Value "
                    + values.get( fieldName ) );
            onChangedValue( site, fieldName, values.get( fieldName ) );
        }
    }


    // default implementation, overwrite if necessary
    protected void onChangedValue( IFormEditorPageSite site, String fieldName, Object value ) {
        site.fireEvent( this, fieldName, IFormFieldListener.VALUE_CHANGE, value );
    }
}
