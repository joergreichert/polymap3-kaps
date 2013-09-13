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
import java.util.List;
import java.util.Map;

import java.beans.PropertyChangeEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.property.Property;

import org.polymap.core.runtime.event.EventHandler;

import org.polymap.rhei.form.IFormEditorPageSite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public abstract class InterEditorListener {

    private static Log log = LogFactory.getLog( InterEditorListener.class );


    public static class EventFilter
            implements org.polymap.core.runtime.event.EventFilter<PropertyChangeEvent> {

        private final Object filterSource;


        public EventFilter( Object filterSource ) {
            this.filterSource = filterSource;
        }


        @Override
        public boolean apply( PropertyChangeEvent ev ) {
            Object source = ev.getSource();
            return source != null && source.equals( filterSource );
        }
    }

    private final Map<String, Object> values = new HashMap<String, Object>();


    public void put( Property term, Object value ) {
        values.put( term.qualifiedName().name(), value );
    }


    public <T> T get( Property<T> term ) {
        return (T)values.get( term.qualifiedName().name() );
    }

    private final Map<String, Property<?>>  terms;

    private final KapsDefaultFormEditorPage page;


    public InterEditorListener( KapsDefaultFormEditorPage page, Property... operators ) {
        this.page = page;
        terms = new HashMap<String, Property<?>>();
        for (Property<?> term : operators) {
            terms.put( term.qualifiedName().name(), term );
        }
        for (Property term : operators) {
            put( term, term.get() );
        }
    }


    @EventHandler(display = true, delay = 1)
    public void handleExternalEvents( List<PropertyChangeEvent> events ) {
        for (PropertyChangeEvent ev : events) {

            log.info( "handleExternalEvent " + ev );

            Object newValue = ev.getNewValue();
            String fieldName = ev.getPropertyName();
            if (terms.keySet().contains( fieldName )) {
                put( terms.get( fieldName ), newValue );
            }
            // // wert kann vor dem Öffnen des Formulars schon geändert sein
            // else if (ev.getPropertyName().equals(
            // vb.bodenpreisBebaut().qualifiedName().name() )) {
            // bodenpreisBebaut = (Double)ev.getNewValue();
            // newValue = newValue != null ? getFormatter( 2 ).format( newValue ) :
            // null;
            // if (pageSite != null) {
            // pageSite.setFieldValue( vb.bodenpreisQm1().qualifiedName().name(),
            // getFormatter( 2 ).format( bodenpreisBebaut ) );
            // }
            // }
            System.out.println( ev );
        }
        flush();
    }

    private boolean onFlushing = false;


    public final void flush() {
        if (!onFlushing) {
            // avoid endless loops
            onFlushing = true;
            IFormEditorPageSite site = page.getPageSite();
            if (site != null) {
                for (String fieldName : values.keySet()) {
                    log.info( "Flush for " + page.getClass() + " and site " + site.toString() + " and field " + fieldName + " and Value " + values.get( fieldName ));
                    onNewValue( site, fieldName, values.get( fieldName ) );
                }
            } else {
                log.info( "Flush for " + page.getClass() + " and site null" );
            }
            onFlushing = false;
        }
        else {
            System.out.println( "During flushing skip" );
        }
    }


    protected abstract void onNewValue( IFormEditorPageSite site, String fieldName, Object value );

    //
    // @Override
    // public void fieldChange( FormFieldEvent ev ) {
    // String fieldName = ev.getFieldName();
    // if (terms.keySet().contains( fieldName )) {
    // Double newValue = (Double)ev.getNewValue(); // explizitely deleting this
    // // value
    // if (newValue == null) {
    // newValue = Double.valueOf( 0.0d );
    // }
    // put( terms.get( fieldName ), newValue );
    // }
    // }
}
