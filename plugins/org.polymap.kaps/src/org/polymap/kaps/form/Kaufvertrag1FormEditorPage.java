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

import java.util.Locale;

import java.text.NumberFormat;

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import org.polymap.core.project.ui.util.SimpleFormData;

import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.CheckboxFormField;
import org.polymap.rhei.field.DateTimeFormField;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.NumberValidator;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.model.KaeuferKreisComposite;
import org.polymap.kaps.model.VertragsArtComposite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class Kaufvertrag1FormEditorPage
        extends KaufvertragFormEditorPage {

    private IFormFieldListener vollpreisRefresher;


    public Kaufvertrag1FormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( Kaufvertrag1FormEditorPage.class.getName(), "Vertragsdaten", feature, featureStore );
    }


    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );

        // DefaultFormPageLayouter layouter = new DefaultFormPageLayouter();

        Composite parent = site.getPageBody();
        // Layout layout = new FormLayout();
        parent.setLayout( new FormLayout() );
        // layouter.setFieldLayoutData( site.newFormField( client,
        // new PropertyAdapter( biotop.name() ),
        // new StringFormField(), null, "Name" ) );

        // readonly
        Composite eingangsNr = site.newFormField( parent,
                new PropertyAdapter( kaufvertrag.eingangsNr() ), new TextFormField(),
                new EingangsNummerFormatter(), "Eingangsnummer" );
        eingangsNr.setEnabled( false );
        eingangsNr.setLayoutData( new SimpleFormData( left ).create() );

        // Datumse
        Composite line2 = site.newFormField( parent,
                new PropertyAdapter( kaufvertrag.vertragsDatum() ), new DateTimeFormField(),
                new DateMustBeforeValidator( kaufvertrag.eingangsDatum(),
                        "Das Vertragsdatum muss vor dem Eingangsdatum liegen." ), "Vertragsdatum" );
        line2.setLayoutData( new SimpleFormData( left ).top( eingangsNr, SPACING ).create() );

        site.newFormField(
                parent,
                new PropertyAdapter( kaufvertrag.eingangsDatum() ),
                new DateTimeFormField(),
                new DateMustAfterValidator( kaufvertrag.vertragsDatum(),
                        "Das Vertragsdatum muss vor dem Eingangsdatum liegen." ), "Eingangsdatum" )
                .setLayoutData( new SimpleFormData( right ).top( eingangsNr, SPACING ).create() );

        // kreise
        Composite line3 = site.newFormField( parent, new AssociationAdapter<KaeuferKreisComposite>(
                "verkaeuferKreis", kaufvertrag.verkaeuferKreis() ), kaeuferKreise(), null,
                "Verkäuferkreis" );
        line3.setLayoutData( new SimpleFormData( left ).top( line2, SPACING ).create() );

        site.newFormField(
                parent,
                new AssociationAdapter<KaeuferKreisComposite>( "kaeuferKreis", kaufvertrag
                        .kaeuferKreis() ), kaeuferKreise(), null, "Käuferkreis" ).setLayoutData(
                new SimpleFormData( right ).top( line2, SPACING ).create() );

        // alle Vertragsarten in PickList
        Composite line4 = site.newFormField( parent, new AssociationAdapter<VertragsArtComposite>(
                "vertragsArt", kaufvertrag.vertragsArt() ), vertragsArten(), null, "Vertragsart" );
        line4.setLayoutData( new SimpleFormData( left ).top( line3, SPACING ).create() );

        // Kaufpreis Nenner/Zähler
        final Composite line5 = site.newFormField( parent,
                new PropertyAdapter( kaufvertrag.kaufpreis() ), new TextFormField(
                        StringFormField.Style.ALIGN_RIGHT ), new NumberValidator( Integer.class,
                        Locale.getDefault() ), "Kaufpreis (€)" );
        line5.setLayoutData( new SimpleFormData( left ).right( 30 ).top( line4, SPACING ).create() );

        site.newFormField( parent, new PropertyAdapter( kaufvertrag.kaufpreisAnteilZaehler() ),
                new TextFormField( StringFormField.Style.ALIGN_RIGHT ),
                new NumberValidator( Integer.class, Locale.getDefault(), 3, 0 ), "Anteil Zähler" )
                .setLayoutData(
                        new SimpleFormData( SPACING ).left( 60 ).right( 80 ).top( line4, SPACING )
                                .create() );

        site.newFormField( parent, new PropertyAdapter( kaufvertrag.kaufpreisAnteilNenner() ),
                new TextFormField( StringFormField.Style.ALIGN_RIGHT ),
                new NumberValidator( Integer.class, Locale.getDefault(), 3, 0 ), "/Nenner" )
                .setLayoutData(
                        new SimpleFormData( SPACING ).left( 80 ).right( RIGHT )
                                .top( line4, SPACING ).create() );

        final Composite line6 = site.newFormField( parent,
                new PropertyAdapter( kaufvertrag.vollpreis() ), new TextFormField(
                        StringFormField.Style.ALIGN_RIGHT ), new NumberValidator( Integer.class,
                        Locale.getDefault() ), "Vollpreis (€)" );
        line6.setEnabled( false );
        line6.setLayoutData( new SimpleFormData( left ).right( 30 ).top( line5, SPACING ).create() );

        // Bemerkungen
        final Composite line7 = site.newFormField( parent,
                new PropertyAdapter( kaufvertrag.bemerkungen() ), new TextFormField(), null,
                "Bemerkungen" );
        line7.setLayoutData( new SimpleFormData( left ).right( RIGHT ).top( line6, SPACING )
                .height( 100 ).create() );

        // Eignungen
        final Composite line8 = site.newFormField( parent,
                new PropertyAdapter( kaufvertrag.fuerAuswertungGeeignet() ),
                new CheckboxFormField(), null, "für Auswertung" );
        line8.setToolTipText( "Ist dieser Vertrag zur Auswertung geeignet?" );
        line8.setLayoutData( new SimpleFormData( left ).top( line7, SPACING ).create() );

        newFormField( "für GEWOS" )
                .setProperty( new PropertyAdapter( kaufvertrag.fuerGewosGeeignet() ) )
                .setField( new CheckboxFormField() )
                .setLayoutData( new SimpleFormData( right ).top( line7, SPACING ).create() )
                .setToolTipText( "Ist dieser Vertrag für GEWOS geeignet?" ).create();

        // Splittung
        final Composite line9 = site.newFormField( parent,
                new PropertyAdapter( kaufvertrag.gesplittet() ), new CheckboxFormField(), null,
                "gesplittet" );
        line9.setToolTipText( "Handelt es sich bei diesem Vertrag um einen geplitteten Vertrag?" );
        line9.setLayoutData( new SimpleFormData( left ).top( line8, SPACING ).create() );
        site.newFormField( parent, new PropertyAdapter( kaufvertrag.gesplittetEingangsnr() ),
                new StringFormField(), null, "zugeordneter Vertrag" ).setLayoutData(
                new SimpleFormData( right ).top( line8, SPACING ).create() );

        // Listener
        site.addFieldListener( vollpreisRefresher = new VollpreisRefresher( site, kaufvertrag ) );
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