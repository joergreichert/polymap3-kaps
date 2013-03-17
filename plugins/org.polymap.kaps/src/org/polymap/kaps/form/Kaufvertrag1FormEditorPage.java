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

import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.runtime.UIJob;

import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.DateTimeFormField;
import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.form.DefaultFormPageLayouter;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.model.KaeuferKreisComposite;
import org.polymap.kaps.model.VertragsArtComposite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class Kaufvertrag1FormEditorPage
        extends KaufvertragFormEditorPage {

    public Kaufvertrag1FormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( feature, featureStore );
    }


    @Override
    public String getTitle() {
        // return "Kaufvertrag " + (kaufvertrag.eingangsNr().get() != null ?
        // kaufvertrag.eingangsNr().get() : "");
        return "Vertragsdaten";
    }


    @Override
    public void createFormContent( IFormEditorPageSite site ) {
        // TODO Title von Formular entfernen und dafür nur in den Title des
        // Editors
        site.setFormTitle( "Kaufvertrag " );

        // DefaultFormPageLayouter layouter = new DefaultFormPageLayouter();
        FormData left = new SimpleFormData( SPACING ).left( LEFT ).right( MIDDLE ).create();
        FormData right = new SimpleFormData( SPACING ).left( MIDDLE ).right( RIGHT ).create();

        Composite parent = site.getPageBody();
        // Layout layout = new FormLayout();
        parent.setLayout( new FormLayout() );
        // layouter.setFieldLayoutData( site.newFormField( client,
        // new PropertyAdapter( biotop.name() ),
        // new StringFormField(), null, "Name" ) );

        // readonly
        Composite eingangsNr = site.newFormField( parent,
                new PropertyAdapter( kaufvertrag.eingangsNr() ), new TextFormField(), null,
                "Eingangsnummer" );
        eingangsNr.setEnabled( false );
        eingangsNr.setLayoutData( new SimpleFormData( left ).create() );

        // Datumse
        Composite line2 = site.newFormField( parent,
                new PropertyAdapter( kaufvertrag.vertragsDatum() ), new DateTimeFormField(), null,
                "Vertragsdatum" );
        line2.setLayoutData( new SimpleFormData( left ).top( eingangsNr ).create() );

        site.newFormField( parent, new PropertyAdapter( kaufvertrag.eingangsDatum() ),
                new DateTimeFormField(), null, "Eingangsdatum" ).setLayoutData(
                new SimpleFormData( right ).top( eingangsNr ).create() );

        // kreise
        Composite line3 = site.newFormField( parent, new AssociationAdapter<KaeuferKreisComposite>(
                "verkaeuferKreis", kaufvertrag.verkaeuferKreis() ), kaeuferKreise(), null,
                "Verkäuferkreis" );
        line3.setLayoutData( new SimpleFormData( left ).top( line2 ).create() );

        site.newFormField(
                parent,
                new AssociationAdapter<KaeuferKreisComposite>( "kaeuferKreis", kaufvertrag
                        .kaeuferKreis() ), kaeuferKreise(), null, "Käuferkreis" ).setLayoutData(
                new SimpleFormData( right ).top( line2 ).create() );

        // alle Vertragsarten in PickList
        Composite line4 = site.newFormField( parent, new AssociationAdapter<VertragsArtComposite>(
                "vertragsArt", kaufvertrag.vertragsArt() ), vertragsArten(), null, "Vertragsart" );
        line4.setLayoutData( new SimpleFormData( left ).top( line3 ).create() );

        //
        // layouter.setFieldLayoutData(site.newFormField(site.getPageBody(),
        // new PropertyAdapter(kaufvertrag.vertragsArt().get().name()),
        // new TextFormField(), null, "Vertragsart"));
    }


    // IFormEditorPage2 *******************************

    private IFormField kaeuferKreise() {
        PicklistFormField picklist = new PicklistFormField(
                kapsRepository.entitiesWithNames( KaeuferKreisComposite.class ) );
        picklist.setTextEditable( false );
        return picklist;
    }


    private IFormField vertragsArten() {
        PicklistFormField picklist = new PicklistFormField(
                kapsRepository.entitiesWithNames( VertragsArtComposite.class ) );
        picklist.setTextEditable( false );
        return picklist;
    }
}