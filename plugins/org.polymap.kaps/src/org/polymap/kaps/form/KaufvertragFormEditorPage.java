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
package org.polymap.kaps.form;

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;

import org.eclipse.jface.action.Action;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.operation.OperationSupport;
import org.polymap.core.runtime.UIJob;

import org.polymap.rhei.form.DefaultFormEditorPage;
import org.polymap.rhei.form.IFormEditorPage;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.KaufvertragComposite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public abstract class KaufvertragFormEditorPage
        extends DefaultFormEditorPage
        implements IFormEditorPage {

    protected KaufvertragComposite kaufvertrag;

    protected KapsRepository       kapsRepository;

    protected int                  SPACING = 6;

    protected int                  LEFT    = 0;

    protected int                  MIDDLE  = 50;

    protected int                  RIGHT   = 100;


    public KaufvertragFormEditorPage( String id, String title, Feature feature,
            FeatureStore featureStore ) {
        super( id, title, feature, featureStore );

        kapsRepository = KapsRepository.instance();

        kaufvertrag = kapsRepository.findEntity( KaufvertragComposite.class, feature
                .getIdentifier().getID() );
    }


    @Override
    public void createFormContent( IFormEditorPageSite site ) {
        super.createFormContent( site );
        site.setEditorTitle( "Kaufvertrag" );
    }

}