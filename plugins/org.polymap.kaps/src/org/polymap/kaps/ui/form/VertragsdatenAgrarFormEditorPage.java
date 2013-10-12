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

import org.polymap.rhei.form.IFormEditorPage;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.model.data.VertragsdatenAgrarComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.ui.KapsDefaultFormEditorPage;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public abstract class VertragsdatenAgrarFormEditorPage
        extends KapsDefaultFormEditorPage
        implements IFormEditorPage {

    protected VertragsdatenAgrarComposite vb;


    public VertragsdatenAgrarFormEditorPage( String id, String title, Feature feature, FeatureStore featureStore ) {
        super( id, title, feature, featureStore );

        vb = repository.findEntity( VertragsdatenAgrarComposite.class, feature.getIdentifier().getID() );
        if (vb.vertrag().get() == null) {
            throw new IllegalStateException("Zum Anlegen von Vertragsdaten Bauland nutzen Sie bitte 'bewerten' bei den Flurstücken in einem Vertrag.");
        }
    }


    @Override
    public void createFormContent( IFormEditorPageSite site ) {
        super.createFormContent( site );

        VertragComposite kaufvertrag = vb.vertrag().get();
        String nummer = EingangsNummerFormatter.format( kaufvertrag.eingangsNr().get() );
        site.setEditorTitle( formattedTitle( "Vertragsdaten Agrar", nummer, null ) );
        site.setFormTitle( formattedTitle( "erweiterte Vertragsdaten - Agrar - für Vertrag", nummer, getTitle() ) );
    }

}