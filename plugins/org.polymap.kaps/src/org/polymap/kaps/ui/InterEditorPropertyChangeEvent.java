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

import java.util.EventObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.entity.Entity;

import org.polymap.rhei.form.FormEditor;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class InterEditorPropertyChangeEvent
        extends EventObject {

    private static final long serialVersionUID = 1L;

    private final FormEditor  targetEditor;

    private final Entity      entity;

    private final String      fieldName;

    private final Object      oldValue;

    private final Object      newValue;

    private static Log        log              = LogFactory.getLog( InterEditorPropertyChangeEvent.class );


    public InterEditorPropertyChangeEvent( FormEditor srcEditor, FormEditor targetEditor, Entity entity,
            String fieldName, Object oldValue, Object newValue ) {
        super( srcEditor );
        this.targetEditor = targetEditor;
        this.entity = entity;
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }


    public FormEditor getTargetEditor() {
        return targetEditor;
    }


    public Entity getEntity() {
        return entity;
    }


    public String getFieldName() {
        return fieldName;
    }


    public Object getOldValue() {
        return oldValue;
    }


    public Object getNewValue() {
        return newValue;
    }


    public static Log getLog() {
        return log;
    }
}
