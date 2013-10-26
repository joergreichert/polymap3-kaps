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
package org.polymap.kaps.ui.filter;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;

import org.qi4j.api.query.Query;

import org.polymap.core.model.Entity;
import org.polymap.core.project.ILayer;

import org.polymap.rhei.data.entityfeature.AbstractEntityFilter;
import org.polymap.rhei.filter.IFilterEditorSite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public abstract class KapsEntityFilter<T extends Entity>
        extends AbstractEntityFilter {

    private Calendar cal = new GregorianCalendar();


    public KapsEntityFilter( String id, ILayer layer, String label, Set<String> keywords, int maxResults,
            Class<T> entityClass ) {
        super( id, layer, label, keywords, maxResults, entityClass );
    }


    protected Date dayStart( Date date ) {
        if (date != null) {
            cal.setTime( date );
            cal.set( Calendar.HOUR_OF_DAY, 0 );
            cal.set( Calendar.MINUTE, 0 );
            cal.set( Calendar.SECOND, 0 );
            cal.set( Calendar.MILLISECOND, 0 );
            date = cal.getTime();
        }
        return date;
    }


    protected Date dayEnd( Date date ) {
        if (date != null) {
            cal.setTime( date );
            cal.set( Calendar.HOUR_OF_DAY, 23 );
            cal.set( Calendar.MINUTE, 59 );
            cal.set( Calendar.SECOND, 59 );
            cal.set( Calendar.MILLISECOND, 999 );
            date = cal.getTime();
        }
        return date;
    }


    @Override
    protected abstract Query<T> createQuery( IFilterEditorSite site );
}