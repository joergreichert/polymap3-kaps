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

import org.eclipse.swt.widgets.Composite;

import org.polymap.core.runtime.Polymap;

import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.NumberValidator;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.form.IFormEditorPageSite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class WohnungseigentumObjektdatenFormEditorPage
        extends WohnungseigentumFormEditorPage {

    public WohnungseigentumObjektdatenFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( WohnungseigentumObjektdatenFormEditorPage.class.getName(), "Objektdaten", feature, featureStore );
    }


    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );

        Composite parent = site.getPageBody();
        Composite lastLine, newLine = null;
        // readonly

        newLine = newFormField( "Objekt-Nr." ).setProperty( new PropertyAdapter( eigentum.objektNummer() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Integer.class, Polymap.getSessionLocale() ) )
                .setLayoutData( left().create() ).setEnabled( eigentum.objektNummer().get() == null ).create();

        newFormField( "Fortführung" ).setProperty( new PropertyAdapter( eigentum.objektFortfuehrung() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Integer.class, Polymap.getSessionLocale() ) )
                .setLayoutData( right().create() ).setEnabled( eigentum.objektNummer().get() == null ).create();

        lastLine = newLine;
        newLine = createFlaecheField( "Gesamtfläche in m²", eigentum.gesamtFlaeche(), left().top( lastLine ), parent, false );

        lastLine = newLine;
        newLine = newFormField( "TE-Datum" ).setToolTipText( "letzter Nachtrag der Teilungserklärung" )
                .setProperty( new PropertyAdapter( eigentum.datumTeilungserklerung() ) )
                .setLayoutData( left().top( lastLine ).create() ).create();
        newFormField( "TE-Urkunde Nr." ).setToolTipText( "Urkundennummer der letzten Teilungserklärung" )
                .setProperty( new PropertyAdapter( eigentum.urkundenNummerDerTeilungserklaerung() ) )
                .setLayoutData( right().top( lastLine ).create() ).create();

        lastLine = newLine;
        // Bemerkungen
        newLine = newFormField( "Bemerkungen" ).setProperty( new PropertyAdapter( eigentum.bemerkungen() ) )
                .setField( new TextFormField() )
                .setLayoutData( left().right( RIGHT ).height( 100 ).top( lastLine ).create() ).create();
    }
}