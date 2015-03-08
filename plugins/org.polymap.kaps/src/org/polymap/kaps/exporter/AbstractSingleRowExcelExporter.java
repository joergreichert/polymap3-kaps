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
package org.polymap.kaps.exporter;

import java.util.ArrayList;
import java.util.List;

import org.polymap.core.model.Entity;

public abstract class AbstractSingleRowExcelExporter<T extends Entity>
        extends AbstractExcelExporter<T> {

    protected AbstractSingleRowExcelExporter( Class<T> type, String typename, String filename, String messagename ) {
        super( type, typename, filename, messagename );
    }


    @Override
    protected List<List<Value>> createMultiRowValues( T entity, List<String> errors ) {
        List<List<Value>> result = new ArrayList<List<Value>>();
        List<Value> createValues = createValues( entity, errors  );
        if (!createValues.isEmpty()) {
            result.add( createValues );
        }
        return result;
    }


    protected abstract List<Value> createValues( T entity, List<String> errors );
}
