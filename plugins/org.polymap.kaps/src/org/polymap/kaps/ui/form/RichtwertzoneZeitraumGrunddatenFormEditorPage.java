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

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;

import org.polymap.kaps.model.data.RichtwertzoneComposite;
import org.polymap.kaps.model.data.RichtwertzoneZeitraumComposite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class RichtwertzoneZeitraumGrunddatenFormEditorPage
        extends RichtwertzoneGrunddatenFormEditorPage {

    private final static String      prefix = RichtwertzoneZeitraumGrunddatenFormEditorPage.class.getSimpleName();


    public RichtwertzoneZeitraumGrunddatenFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( feature, featureStore );
    }

    @Override
    protected RichtwertzoneComposite lookupRichtwertzoneComposite() {
        RichtwertzoneZeitraumComposite zz = repository.findEntity( RichtwertzoneZeitraumComposite.class, feature
                .getIdentifier().getID() );
        // preselect
        selectedComposite.set( zz );
        if (zz != null) {
            return zz.zone().get();
        }
        return null;
    }
}
