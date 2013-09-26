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
import org.polymap.kaps.model.data.FlurstuecksdatenAgrarComposite;
import org.polymap.kaps.model.data.FlurstuecksdatenBaulandComposite;
import org.polymap.kaps.model.data.GebaeudeComposite;
import org.polymap.kaps.model.data.NutzungComposite;
import org.polymap.kaps.model.data.RichtwertzoneComposite;
import org.polymap.kaps.model.data.RichtwertzoneZeitraumComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.model.data.WohnungComposite;
import org.polymap.kaps.ui.filter.DefaultEntityFilter;
import org.polymap.kaps.ui.filter.EinzelneVertragsdatenAgrarFilter;
import org.polymap.kaps.ui.filter.EinzelneVertragsdatenBaulandFilter;
import org.polymap.kaps.ui.filter.EinzelnerVertragFilter;
import org.polymap.kaps.ui.filter.RichtwertZoneFilter;
import org.polymap.kaps.ui.filter.RichtwertZoneZeitraumFilter;
import org.polymap.kaps.ui.filter.VertraegeFuerBaujahrUndGebaeudeartFilter;
import org.polymap.kaps.ui.filter.VertraegeStabuFilter;

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

        List<IFilter> result = new ArrayList<IFilter>();

        if (geores instanceof EntityGeoResourceImpl) {
            EntityProvider provider = geores.resolve( EntityProvider.class, null );
            if (provider != null && provider instanceof KapsEntityProvider) {
                Class type = provider.getEntityType().getType();
                // egeo.
                if (type.isAssignableFrom( RichtwertzoneComposite.class )) {
                    result.add( new RichtwertZoneFilter( layer ) );
                    result.add( new RichtwertZoneZeitraumFilter( layer ) );
                    result.add( new DefaultEntityFilter( layer, type, repo ) );
//                }
//                else if (type.isAssignableFrom( FlurstueckComposite.class )) {
//                    result.add( new FlurstueckeStabuFilter( layer )); 
                }
                else if (type.isAssignableFrom( RichtwertzoneZeitraumComposite.class )) {
                    result.add( new DefaultEntityFilter( layer, type, repo ).exclude( "zone" ) );
                }
                else if (type.isAssignableFrom( VertragComposite.class )) {
                    result.add( new EinzelnerVertragFilter( layer ) );
                    result.add( new VertraegeFuerBaujahrUndGebaeudeartFilter( layer ) );
                    result.add( new DefaultEntityFilter( layer, type, repo ) );
                    result.add( new VertraegeStabuFilter( layer ) );
                }
                else if (type.isAssignableFrom( FlurstuecksdatenBaulandComposite.class )) {
                    result.add( new EinzelneVertragsdatenBaulandFilter( layer ) );
                    // result.add( new DefaultEntityFilter( layer, type, repo ) );
                }
                else if (type.isAssignableFrom( FlurstuecksdatenAgrarComposite.class )) {
                    result.add( new EinzelneVertragsdatenAgrarFilter( layer ) );
                }
                else if (type.isAssignableFrom( WohnungComposite.class )) {
                    result.add( new DefaultEntityFilter( layer, type, repo, "objektNummer", "objektFortfuehrung",
                            "gebaeudeNummer", "gebaeudeFortfuehrung", "wohnungsNummer", "wohnungsFortfuehrung" ) );
                }
                else if (type.isAssignableFrom( NutzungComposite.class )) {
                    result.add( new DefaultEntityFilter( layer, type, repo ) );
                }
                else if (type.isAssignableFrom( GebaeudeComposite.class )) {
                    result.add( new DefaultEntityFilter( layer, type, repo, "objektNummer", "objektFortfuehrung",
                            "gebaeudeNummer", "gebaeudeFortfuehrung", "gebaeudeArt", "baujahr" ) );
                }

                else {
                    // standard to all other entitytypes
                    result.add( new DefaultEntityFilter( layer, type, repo ) );
                }
            }
        }
        return result;
    }
}
