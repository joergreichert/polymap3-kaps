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
import java.util.List;

import net.refractions.udig.catalog.IGeoResource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.project.ILayer;

import org.polymap.rhei.data.entityfeature.EntityProvider;
import org.polymap.rhei.data.entityfeature.catalog.EntityGeoResourceImpl;
import org.polymap.rhei.filter.IFilter;
import org.polymap.rhei.filter.IFilterProvider;

import org.polymap.kaps.model.KapsEntityProvider;
import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.ui.filter.RichtwertZoneFilter;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class FilterProvider
        implements IFilterProvider {

    private static Log log = LogFactory.getLog( FilterProvider.class );

    private ILayer     layer;

    public List<IFilter> addFilters( ILayer _layer )
            throws Exception {
        this.layer = _layer;
        log.debug( "addFilters(): layer= " + layer );

        final KapsRepository repo = KapsRepository.instance();
        IGeoResource geores = layer.getGeoResource();

        if (geores instanceof EntityGeoResourceImpl
                && geores.resolve( EntityProvider.class, null ) instanceof KapsEntityProvider) {

            List<IFilter> result = new ArrayList<IFilter>();

            result.add( new RichtwertZoneFilter( layer ) );
            
            return result;
        }
        return null;
    }
}
