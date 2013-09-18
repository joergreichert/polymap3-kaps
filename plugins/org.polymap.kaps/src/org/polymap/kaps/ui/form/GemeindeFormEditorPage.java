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
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.model.data.GemeindeComposite;
import org.polymap.kaps.ui.KapsDefaultFormEditorPage;
import org.polymap.kaps.ui.MyNumberValidator;
import org.polymap.kaps.ui.NotNullValidator;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class GemeindeFormEditorPage
        extends KapsDefaultFormEditorPage {

    private static Log          log = LogFactory.getLog( GemeindeFormEditorPage.class );

    protected GemeindeComposite composite;


    public GemeindeFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( GemeindeFormEditorPage.class.getName(), "Grunddaten", feature, featureStore );

        composite = repository.findEntity( GemeindeComposite.class, feature.getIdentifier().getID() );
    }


    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );
        site.setEditorTitle( formattedTitle( GemeindeComposite.NAME, composite.schl().get(), null ) );
        site.setFormTitle( formattedTitle( GemeindeComposite.NAME, composite.schl().get(), getTitle() ) );

        Composite parent = site.getPageBody();
        Composite newLine, lastLine = null;

        newLine = newFormField( "Schlüssel" ).setEnabled( composite.schl().get() == null )
                .setProperty( new PropertyAdapter( composite.schl() ) ).setField( new StringFormField() )
                .setValidator( new NotNullValidator() ).setLayoutData( left().top( lastLine ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( "Bezeichung" ).setProperty( new PropertyAdapter( composite.name() ) )
                .setValidator( new NotNullValidator() ).setField( new StringFormField() )
                .setLayoutData( left().top( lastLine ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( "Einwohner" ).setProperty( new PropertyAdapter( composite.einwohner() ) )
                .setValidator( new MyNumberValidator( Integer.class ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setLayoutData( left().top( lastLine ).create() ).create();

        newFormField( "tatsächlich" ).setProperty( new PropertyAdapter( composite.einwohnerTatsaechlich() ) )
                .setValidator( new MyNumberValidator( Integer.class ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setLayoutData( right().top( lastLine ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( "Faktor" ).setProperty( new PropertyAdapter( composite.faktor() ) )
                .setValidator( new MyNumberValidator( Double.class, 1, 2 ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setLayoutData( left().top( lastLine ).create() ).create();
    }
}
