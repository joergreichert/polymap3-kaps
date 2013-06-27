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

import org.polymap.kaps.model.data.BodennutzungComposite;
import org.polymap.kaps.ui.KapsDefaultFormEditorPage;
import org.polymap.kaps.ui.NotNullValidator;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class BodennutzungFormEditorPage
        extends KapsDefaultFormEditorPage {

    private static Log               log    = LogFactory.getLog( BodennutzungFormEditorPage.class );

    protected BodennutzungComposite composite;


    public BodennutzungFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( BodennutzungFormEditorPage.class.getName(), "Grunddaten", feature, featureStore );

        composite = repository.findEntity( BodennutzungComposite.class, feature.getIdentifier().getID() );
    }


    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );
        site.setEditorTitle( formattedTitle( "Bodennutzung", composite.schl().get(), null ) );
        site.setFormTitle( formattedTitle( "Bodennutzung", composite.schl().get(), getTitle() ) );

        Composite parent = site.getPageBody();
        Composite newLine, lastLine = null;

        newLine = newFormField( "Schlüssel" ).setEnabled( composite.schl().get() == null )
                .setProperty( new PropertyAdapter( composite.schl() ) )
                .setField( new StringFormField () ).setValidator( new NotNullValidator() )
                .setLayoutData( left().top( lastLine ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( "Bezeichung" )
                .setProperty( new PropertyAdapter( composite.name() ) ).setValidator( new NotNullValidator() )
                .setField( new StringFormField() ).setLayoutData( left().top( lastLine ).create() ).create();
    }
}
