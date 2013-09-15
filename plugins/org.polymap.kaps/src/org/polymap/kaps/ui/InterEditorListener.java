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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.entity.Entity;
import org.qi4j.api.property.Property;

import org.polymap.core.runtime.event.EventHandler;

import org.polymap.rhei.form.IFormEditorPageSite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public abstract class InterEditorListener {

    private static Log log = LogFactory.getLog( InterEditorListener.class );


    public static class EventFilter
            implements org.polymap.core.runtime.event.EventFilter<InterEditorPropertyChangeEvent> {

        private final Entity filterSource;


        public EventFilter( Entity filterSource ) {
            this.filterSource = filterSource;
        }


        @Override
        public boolean apply( InterEditorPropertyChangeEvent ev ) {
            Object source = ev.getEntity();
            return source != null && source.equals( filterSource );
        }
    }

    private final Map<String, InterEditorPropertyChangeEvent> values = new HashMap<String, InterEditorPropertyChangeEvent>();

    private final Map<String, Property<?>>                    terms;

    private IFormEditorPageSite                               site;


    public InterEditorListener( Property... operators ) {
        terms = new HashMap<String, Property<?>>();
        for (Property<?> term : operators) {
            terms.put( term.qualifiedName().name(), term );
        }
    }


    @EventHandler(display = true, delay = 1)
    public void handleExternalEvents( List<InterEditorPropertyChangeEvent> events ) {
        for (InterEditorPropertyChangeEvent ev : events) {

            log.info( "handleExternalEvent " + ev );

            Object newValue = ev.getNewValue();
            String fieldName = ev.getFieldName();
            if (terms.isEmpty() || terms.keySet().contains( fieldName )) {
                values.put( fieldName, ev );
            }
            System.out.println( ev );
        }
        if (site != null) {
            flush( site );
        }
    }

    public final void flush( IFormEditorPageSite site ) {
        this.site = site;
        for (String fieldName : values.keySet()) {
            log.info( "Flush for site " + site.toString() + " and field " + fieldName + " and Value "
                    + values.get( fieldName ) );
            InterEditorPropertyChangeEvent ev = values.get( fieldName );
            onChangedValue( site, ev.getEntity(), ev.getFieldName(), ev.getNewValue() );
        }
    }


    protected abstract void onChangedValue( IFormEditorPageSite site, Entity entity, String fieldName, Object value );

}
