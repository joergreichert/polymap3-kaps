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
package org.polymap.kaps.ui.form;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.property.Property;

import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.ui.FieldListener;


/**
 * only to store values and not to fire them again
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class DefaultFieldListener
        extends FieldListener {

    private static Log log = LogFactory.getLog( DefaultFieldListener.class );

    
    public DefaultFieldListener(Property... operators) {
        super( operators );
    }

    @Override
    protected void onChangedValue( IFormEditorPageSite site, String fieldName, Object value ) {
        throw new IllegalStateException( "flush must never be called here" );
    }
}
