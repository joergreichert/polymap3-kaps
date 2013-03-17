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
package org.polymap.kaps.form;

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;

import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.ui.forms.widgets.Section;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.runtime.UIJob;

import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.DateTimeFormField;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.form.DefaultFormEditorPage;
import org.polymap.rhei.form.DefaultFormPageLayouter;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.KaufvertragComposite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class Kaufvertrag2FormEditorPage
        extends DefaultFormEditorPage {

    public Kaufvertrag2FormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( Kaufvertrag2FormEditorPage.class.getName(), "Bearbeitungshinweise", feature,
                featureStore );
    }


    @Override
    public void createFormContent( IFormEditorPageSite site ) {
        super.createFormContent( site );

        KaufvertragComposite kaufvertrag = KapsRepository.instance().findEntity(
                KaufvertragComposite.class, feature.getIdentifier().getID() );
        // Composite eingangsNr = site.newFormField( parent,
        // new PropertyAdapter( kaufvertrag.eingangsNr() ), new TextFormField(),
        // null,
        // "Eingangsnummer" );
        // eingangsNr.setEnabled( false );
        // eingangsNr.setLayoutData( new SimpleFormData( left ).create() );
        //
        // // Datumse
        // site.newFormField( parent, new PropertyAdapter(
        // kaufvertrag.vertragsDatum() ),
        // new DateTimeFormField(), null, "Vertragsdatum" ).setLayoutData(
        // new SimpleFormData( left ).top( eingangsNr ).create() );
        //
        // site.newFormField( parent, new PropertyAdapter(
        // kaufvertrag.eingangsDatum() ),
        // new DateTimeFormField(), null, "Eingangsdatum" ).setLayoutData(
        // new SimpleFormData( right ).top( eingangsNr ).create() );
        Section left = newSection( "", false, null );

        Composite eingangsNr = newFormField( "eingangsNr" )
                .setProperty( new PropertyAdapter( kaufvertrag.eingangsNr() ) )
                .setField( new TextFormField() ).setParent( left ).create();

        Section right = newSection( "", true, left );
        right.setLayoutData( new SimpleFormData( SECTION_SPACING ).left( 50 ).right( 100 )
                .top( eingangsNr ).create() );

        newFormField( "vertragsDatum" )
                .setProperty( new PropertyAdapter( kaufvertrag.vertragsDatum() ) )
                .setField( new DateTimeFormField() ).setParent( left ).create();

        newFormField( "eingangsDatum" )
                .setProperty( new PropertyAdapter( kaufvertrag.eingangsDatum() ) )
                .setField( new DateTimeFormField() ).setParent( right ).create();// .setLayoutData(
                                                                                 // new
                                                                                 // SimpleFormData().top(
                                                                                 // eingangsNr
                                                                                 // ).create());
    }

}