/*
 * polymap.org Copyright 2011, Polymap GmbH. All rights reserved.
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
package org.polymap.kaps.importer;

import java.util.HashMap;
import java.util.Map;

import java.lang.reflect.Method;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.composite.Composite;
import org.qi4j.api.property.Property;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Table;

/**
 * Helper that fills properties of an {@link Composite} with values of a
 * {@link Table} row according to {@link ImportColumn} annotations of the properties.
 * 
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class AnnotatedCompositeImporter {

    private static Log                 log     = LogFactory
                                                       .getLog( AnnotatedCompositeImporter.class );

    private Class<? extends Composite> type;

    private Table                      table;

    /** Maps column name into property name. */
    private Map<String, String>        nameMap = new HashMap();


    public AnnotatedCompositeImporter( Class<? extends Composite> type, Table table ) {
        this.type = type;
        this.table = table;

        log.info( "Entity type: " + type );

        for (Method m : type.getMethods()) {
            ImportColumn a = m.getAnnotation( ImportColumn.class );
            if (a != null) {
                String propName = m.getName();
                // fail if the colums does not exist
                Column column = table.getColumn( a.value() );
                nameMap.put( column.getName(), propName );
                log.info( "    mapping found: " + column.getName() + " -> " + propName );
            }
        }
    }


    public void fillEntity( Composite composite, Map<String, Object> row ) {
        for (Map.Entry<String, Object> rowEntry : row.entrySet()) {
            String propName = nameMap.get( rowEntry.getKey() );
            if (propName != null && rowEntry.getValue() != null) {
                try {
                    Method m = type.getDeclaredMethod( propName, ArrayUtils.EMPTY_CLASS_ARRAY );
                    Property p = (Property)m.invoke( composite, ArrayUtils.EMPTY_OBJECT_ARRAY );
                    // String
                    if (String.class.equals( p.type() )) {
                        p.set( rowEntry.getValue().toString() );
                    }
                    // Integer
                    else if (Integer.class.equals( p.type() )) {
                        p.set( ((Number)rowEntry.getValue()).intValue() );
                    }
                    // Long
                    else if (Long.class.equals( p.type() )) {
                        p.set( ((Number)rowEntry.getValue()).longValue() );
                    }
                    // Float
                    else if (Float.class.equals( p.type() )) {
                        p.set( ((Number)rowEntry.getValue()).floatValue() );
                    }
                    // Double
                    else if (Double.class.equals( p.type() )) {
                        p.set( ((Number)rowEntry.getValue()).doubleValue() );
                    }
                    // Boolean
                    else if (Boolean.class.equals( p.type() )) {
                        p.set( rowEntry.getValue() );
                    }
                    // Date
                    else if (java.util.Date.class.equals( p.type() )) {
                        p.set( rowEntry.getValue() );
                    }
                    else {
                        throw new RuntimeException( "Unhandled property type: " + p.type() );
                    }
                    // log.info( "    property: " + p.qualifiedName().name() + " = "
                    // + rowEntry.getValue() );
                }
                catch (Exception e) {
                    throw new RuntimeException( "Property: " + propName, e );
                }
            }
            else {
                // log.info( "    skipping column value: " + rowEntry.getKey() );
            }
        }
    }

}
