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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.polymap.rhei.data.entityfeature.AssociationAdapter;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.model.data.ImmobilienArtStaBuComposite;
import org.polymap.kaps.model.data.StockwerkStaBuComposite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class WohnungStaBuFormEditorPage
        extends WohnungFormEditorPage {

    private static Log log = LogFactory.getLog( WohnungStaBuFormEditorPage.class );


    public WohnungStaBuFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( WohnungStaBuFormEditorPage.class.getName(), "Statistisches Bundesamt", feature,
                featureStore );
    }


    @SuppressWarnings("unchecked")
    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );

        Control newLine, lastLine = null;
        Composite parent = pageSite.getPageBody();

        Composite client = parent;
        newLine = newFormField( "Stockwerk" ).setEnabled( true )
                .setProperty( new AssociationAdapter<StockwerkStaBuComposite>( wohnung.stockwerkStaBu() ) )
                .setField( namedAssocationsPicklist( StockwerkStaBuComposite.class ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( client ).create();

        lastLine = newLine;
        newLine = newFormField( "Immobilienart" ).setEnabled( true )
                .setProperty( new AssociationAdapter<ImmobilienArtStaBuComposite>( wohnung.immobilienArtStaBu() ) )
                .setField( namedAssocationsPicklist( ImmobilienArtStaBuComposite.class ) )
                .setLayoutData( left().top( lastLine ).bottom( 100 ).create() ).setParent( client ).create();
    }
}
