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

import org.polymap.rhei.data.entityfeature.AssociationAdapter;
import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.DateTimeFormField;
import org.polymap.rhei.field.NumberValidator;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.model.data.BodennutzungComposite;
import org.polymap.kaps.model.data.ErschliessungsBeitragComposite;
import org.polymap.kaps.model.data.GemeindeComposite;
import org.polymap.kaps.model.data.NutzungComposite;
import org.polymap.kaps.model.data.RichtwertZoneLageComposite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class RichtwertzoneGrunddatenFormEditorPage
        extends RichtwertZoneFormEditorPage {

    private static Log log = LogFactory.getLog( RichtwertzoneGrunddatenFormEditorPage.class );


    public RichtwertzoneGrunddatenFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( RichtwertzoneGrunddatenFormEditorPage.class.getName(), "Grunddaten", feature,
                featureStore );
    }


    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );

        GemeindeComposite gemeinde = richtwertzone.gemeinde().get();

        Composite newLine, lastLine = null;

        newLine = newFormField( "Gemeinde" )
                .setEnabled( false )
                .setProperty(
                        new AssociationAdapter<GemeindeComposite>( "gemeinde", richtwertzone
                                .gemeinde() ) )
                .setField( namedAssocationsPicklist( GemeindeComposite.class ) )
                .setLayoutData( left().top( lastLine ).create() ).create();

        newFormField( "Bezeichnung" ).setProperty( new PropertyAdapter( richtwertzone.name() ) )
                .setField( new StringFormField() ).setLayoutData( right().top( lastLine ).create() )
                .create();

        lastLine = newLine;
        newLine = newFormField( "Zone" ).setEnabled( false )
                .setProperty( new PropertyAdapter( richtwertzone.zone() ) )
                .setField( new StringFormField() ).setLayoutData( left().top( lastLine ).create() )
                .create();

        boolean lageEnabled = (gemeinde != null && gemeinde.einwohner().get() > 50000);
        newFormField( "Lage (STALA)" )
                .setEnabled( lageEnabled )
                .setProperty(
                        new AssociationAdapter<RichtwertZoneLageComposite>( "lage", richtwertzone
                                .lage() ) )
                .setField( namedAssocationsPicklist( RichtwertZoneLageComposite.class ) )
                .setLayoutData( right().top( lastLine ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( "Gültig ab" ).setEnabled( false )
                .setProperty( new PropertyAdapter( richtwertzone.gueltigAb() ) )
                .setField( new DateTimeFormField() )
                .setLayoutData( left().top( lastLine ).create() ).create();

        newFormField( "Stichtag" ).setProperty( new PropertyAdapter( richtwertzone.stichtag() ) )
                .setField( new DateTimeFormField() )
                .setLayoutData( right().top( lastLine ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( "GFZ-Bereich" )
                .setProperty( new PropertyAdapter( richtwertzone.gfzBereich() ) )
                .setField( new StringFormField() ).setLayoutData( right().top( lastLine ).create() )
                .create();

        lastLine = newLine;
        newLine = newFormField( "€ pro qm" )
                .setProperty( new PropertyAdapter( richtwertzone.euroQm() ) )
                .setField( new StringFormField() )
                .setValidator( new NumberValidator( Double.class, locale, 2, 2 ) )
                .setLayoutData( left().top( lastLine ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( "EB" )
                .setEnabled( lageEnabled )
                .setProperty(
                        new AssociationAdapter<ErschliessungsBeitragComposite>(
                                "erschliessungsBeitrag", richtwertzone.erschliessungsBeitrag() ) )
                .setField( namedAssocationsPicklist( ErschliessungsBeitragComposite.class ) )
                .setLayoutData( left().top( lastLine ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( "Nutzung" )
                .setEnabled( lageEnabled )
                .setProperty(
                        new AssociationAdapter<NutzungComposite>( "nutzung", richtwertzone
                                .nutzung() ) )
                .setField( namedAssocationsPicklist( NutzungComposite.class ) )
                .setLayoutData( left().top( lastLine ).create() ).create();

        newFormField( "Bodennutzung" )
                .setEnabled( lageEnabled )
                .setProperty(
                        new AssociationAdapter<BodennutzungComposite>( "bodenNutzung",
                                richtwertzone.bodenNutzung() ) )
                .setField( namedAssocationsPicklist( BodennutzungComposite.class ) )
                .setLayoutData( right().top( lastLine ).create() ).create();
    }
}
