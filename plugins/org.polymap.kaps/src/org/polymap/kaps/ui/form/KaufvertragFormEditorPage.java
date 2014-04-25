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

import java.util.List;

import java.beans.PropertyChangeEvent;

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;

import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.form.IFormEditorPage;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.model.data.VertragsdatenErweitertComposite;
import org.polymap.kaps.ui.KapsDefaultFormEditorPage;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public abstract class KaufvertragFormEditorPage
        extends KapsDefaultFormEditorPage
        implements IFormEditorPage {

    protected VertragComposite kaufvertrag;
    protected final VertragsdatenErweitertComposite erweiterteVertragsdaten;


    public KaufvertragFormEditorPage( String id, String title, Feature feature, FeatureStore featureStore ) {
        super( id, title, feature, featureStore );

        kaufvertrag = repository.findEntity( VertragComposite.class, feature.getIdentifier().getID() );

        erweiterteVertragsdaten = getOrCreateErweiterteVertragsdaten( kaufvertrag );

        EventManager.instance().subscribe( this, new EventFilter<PropertyChangeEvent>() {

            public boolean apply( PropertyChangeEvent ev ) {
                Object source = ev.getSource();
                return source != null && source instanceof VertragComposite && source.equals( kaufvertrag );
            }
        } );
    }


    @EventHandler(display = true, delay = 1)
    public void handleEingangsnummer( List<PropertyChangeEvent> events )
            throws Exception {
        for (PropertyChangeEvent ev : events) {
            if (ev.getPropertyName().equals( kaufvertrag.eingangsNr().qualifiedName().name() )) {
                Integer nummer = (Integer)ev.getNewValue();
                updateEingangsNummer( nummer );
            }
        }
    }


    @Override
    public void dispose() {
        super.dispose();
        EventManager.instance().unsubscribe( this );
    }


    protected void updateEingangsNummer( Integer nummer ) {
        String nummerF = EingangsNummerFormatter.format( nummer );
        if (pageSite != null) {
            pageSite.setEditorTitle( formattedTitle( "Vertrag", nummerF, null ) );
            pageSite.setFormTitle( formattedTitle( "Vertrag", nummerF, getTitle() ) );
        }
    }


    @Override
    public void createFormContent( IFormEditorPageSite site ) {
        super.createFormContent( site );

        String nummerF = EingangsNummerFormatter.format( kaufvertrag.eingangsNr().get() );
        pageSite.setEditorTitle( formattedTitle( "Vertrag", nummerF, null ) );
        pageSite.setFormTitle( formattedTitle( "Vertrag", nummerF, getTitle() ) );
    }
    

    private VertragsdatenErweitertComposite getOrCreateErweiterteVertragsdaten( VertragComposite kaufvertrag ) {
        VertragsdatenErweitertComposite vdec = kaufvertrag.erweiterteVertragsdaten().get();
        if (vdec == null) {
            vdec = repository.newEntity( VertragsdatenErweitertComposite.class, null );
            kaufvertrag.erweiterteVertragsdaten().set( vdec );
        }
        if (vdec.basispreis().get() == null || (kaufvertrag.vollpreis().get() != null && vdec.basispreis().get().doubleValue() != kaufvertrag.vollpreis().get().doubleValue())) {
            vdec.updateBasisPreis( kaufvertrag.vollpreis().get() );
        }
        return vdec;
    }
}