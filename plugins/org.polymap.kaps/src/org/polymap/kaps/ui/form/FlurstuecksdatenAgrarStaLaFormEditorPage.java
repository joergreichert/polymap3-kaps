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

import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.form.FormEditor;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.ui.MyNumberValidator;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class FlurstuecksdatenAgrarStaLaFormEditorPage
        extends FlurstuecksdatenAgrarFormEditorPage {

    private static Log log = LogFactory.getLog( FlurstuecksdatenAgrarStaLaFormEditorPage.class );


    // private IFormFieldListener informer;

    public FlurstuecksdatenAgrarStaLaFormEditorPage( FormEditor formEditor, Feature feature, FeatureStore featureStore ) {
        super( FlurstuecksdatenAgrarStaLaFormEditorPage.class.getName(), "Statistisches Landesamt", feature,
                featureStore );
    }


    @SuppressWarnings("unchecked")
    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );

        Composite newLine, lastLine = null;
        Composite parent = pageSite.getPageBody();

        // Section section = newSection( parent, "Richtwertberechnung" );
        // Composite client = (Composite)section.getClient();
        Composite client = parent;

        newLine = newFormField( "Verkaufte Fläche" ).setToolTipText( "Gesamte verkaufte Fläche" )
                .setProperty( new PropertyAdapter( vb.flurstueck().get().verkaufteFlaeche() ) ).setEnabled( false )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new MyNumberValidator( Double.class, 0 ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( client ).create();

        lastLine = newLine;
        newLine = newFormField( "Fläche LaWi" ).setToolTipText( "Fläche der landwirtschaftlichen Nutzung" )
                .setProperty( new PropertyAdapter( vb.flaecheLandwirtschaftStala() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new MyNumberValidator( Double.class, 0 ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( client ).create();

        lastLine = newLine;
        newLine = newFormField( "Hypotheken" ).setToolTipText( "Gegenleistung: übernommene Hypotheken" )
                .setProperty( new PropertyAdapter( vb.hypothekStala() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( client ).create();

        lastLine = newLine;
        newLine = newFormField( "Tausch" ).setToolTipText( "Gegenleistung: Wert des Tauschgrundstücks" )
                .setProperty( new PropertyAdapter( vb.wertTauschStala() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( client ).create();

        lastLine = newLine;
        newLine = newFormField( "Sonstiges" ).setToolTipText( "Gegenleistung: sonstige Leistungen" )
                .setProperty( new PropertyAdapter( vb.wertSonstigesStala() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( client ).create();

        lastLine = newLine;
        newLine = newFormField( "Bemerkungen" ).setProperty( new PropertyAdapter( vb.bemerkungStala() ) )
                .setField( new TextFormField() )
                .setLayoutData( left().right( 100 ).height( 50 ).top( lastLine ).create() ).setParent( client )
                .create();

        // site.addFieldListener( informer = new
        // InterEditorPublisher(vb.gesamtBauWert()));
    }
}
