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
import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.model.data.GebaeudeArtStaBuComposite;
import org.polymap.kaps.model.data.GebaeudeTypStaBuComposite;
import org.polymap.kaps.model.data.KellerComposite;
import org.polymap.kaps.model.data.WohnlageStaBuComposite;
import org.polymap.kaps.ui.BooleanFormField;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class VertragsdatenBaulandStaBuFormEditorPage
        extends VertragsdatenBaulandFormEditorPage {

    private static Log log = LogFactory.getLog( VertragsdatenBaulandStaBuFormEditorPage.class );


    public VertragsdatenBaulandStaBuFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( VertragsdatenBaulandStaBuFormEditorPage.class.getName(), "Statistisches Bundesamt", feature,
                featureStore );
    }


    @SuppressWarnings("unchecked")
    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );

        Control newLine, lastLine = null;
        Composite parent = pageSite.getPageBody();

        Composite client = parent;

        // newLine = newFormField( "Erbbaurecht" ).setEnabled( true )
        // .setProperty( new PropertyAdapter( vb.erbbaurecht() ) ).setField( new
        // BooleanFormField() )
        // .setLayoutData( left().top( lastLine ).create() ).setParent( client
        // ).create();
        //
        // lastLine = newLine;
        newLine = newFormField( "Gebäudetyp" ).setEnabled( true )
                .setProperty( new AssociationAdapter<GebaeudeTypStaBuComposite>( vb.gebaeudeTypStaBu() ) )
                .setField( namedAssocationsPicklist( GebaeudeTypStaBuComposite.class ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( client ).create();

        lastLine = newLine;
        newLine = newFormField( "Gebäudeart" ).setEnabled( true )
                .setProperty( new AssociationAdapter<GebaeudeArtStaBuComposite>( vb.gebaeudeArtStaBu() ) )
                .setField( namedAssocationsPicklist( GebaeudeArtStaBuComposite.class ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( client ).create();

        lastLine = newLine;
        newLine = newFormField( "Stadträumliche Wohnlage" ).setEnabled( true )
                .setProperty( new AssociationAdapter<WohnlageStaBuComposite>( vb.wohnlageStaBu() ) )
                .setField( namedAssocationsPicklist( WohnlageStaBuComposite.class ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( client ).create();

        lastLine = newLine;
        newLine = newFormField( "Garage" ).setEnabled( true ).setProperty( new PropertyAdapter( vb.garage() ) )
                .setField( new BooleanFormField() ).setLayoutData( left().top( lastLine ).create() ).setParent( client )
                .create();

        lastLine = newLine;
        newLine = newFormField( "Stellplatz" ).setEnabled( true )
                .setProperty( new PropertyAdapter( vb.stellplaetze() ) ).setField( new BooleanFormField() )
                .setLayoutData( left().top( lastLine ).create() ).setParent( client ).create();

        lastLine = newLine;
        newLine = newFormField( "Carport" ).setEnabled( true ).setProperty( new PropertyAdapter( vb.carport() ) )
                .setField( new BooleanFormField() ).setLayoutData( left().top( lastLine ).create() ).setParent( client )
                .create();

        lastLine = newLine;
        newLine = newFormField( "Keller" ).setProperty( new AssociationAdapter<KellerComposite>( vb.keller() ) )
                .setField( namedAssocationsPicklist( KellerComposite.class ) )
                .setLayoutData( left().top( lastLine ).bottom( 100 ).create() ).setParent( client ).create();
    }
}
