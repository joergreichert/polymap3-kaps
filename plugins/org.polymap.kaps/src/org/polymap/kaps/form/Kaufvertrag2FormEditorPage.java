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
import org.polymap.rhei.field.CheckboxFormField;
import org.polymap.rhei.field.DateTimeFormField;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.form.DefaultFormEditorPage;
import org.polymap.rhei.form.DefaultFormPageLayouter;
import org.polymap.rhei.form.IFormEditorPageSite;
import org.polymap.rhei.form.IFormEditorToolkit;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.KaufvertragComposite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class Kaufvertrag2FormEditorPage
        extends KaufvertragFormEditorPage {

    public Kaufvertrag2FormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( Kaufvertrag1FormEditorPage.class.getName(), "Bearbeitungshinweise", feature,
                featureStore );
    }


    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );

        final Composite line1 = newFormField( "Urkundennummer" )
                .setProperty( new PropertyAdapter( kaufvertrag.urkundenNummer() ) )
                .setField( new StringFormField() )
                .setLayoutData( new SimpleFormData( left ).create() ).create();

        newFormField( "Gutachtennummer" )
                .setProperty( new PropertyAdapter( kaufvertrag.gutachtenNummer() ) )
                .setField( new StringFormField() )
                .setLayoutData( new SimpleFormData( right ).create() ).create();

        final Composite line2 = newFormField( "Anfragen" )
                .setProperty( new PropertyAdapter( kaufvertrag.anfragen() ) )
                .setField( new TextFormField() )
                .setLayoutData(
                        new SimpleFormData( left ).right(100).top( line1, SPACING ).height( 100 ).create() )
                .create();

        Composite verkaeufer = newSection( line2, "Auskunft vom Verkäufer" );
        final Composite verkaeuferLine1 = newFormField( "Versendet am" )
                .setProperty( new PropertyAdapter( kaufvertrag.anschreibenVerkaeuferErstelltAm() ) )
                .setField( new DateTimeFormField() )
                .setLayoutData( new SimpleFormData( left ).create() ).setParent( verkaeufer )
                .create();

        newFormField( "Antwort am" )
                .setProperty(
                        new PropertyAdapter( kaufvertrag.anschreibenVerkaeuferEingangAntwort() ) )
                .setField( new DateTimeFormField() )
                .setLayoutData( new SimpleFormData( right ).create() ).setParent( verkaeufer )
                .create();

        Composite verkaeuferBemerkungen = newFormField( "Bemerkungen" )
                .setProperty( new PropertyAdapter( kaufvertrag.bemerkungenVerkaeufer() ) )
                .setField( new TextFormField() )
                .setLayoutData(
                        new SimpleFormData( left ).right( 100 ).top( verkaeuferLine1, SPACING )
                                .height( 50 ).create() ).setParent( verkaeufer ).create();

        Composite kaeufer = newSection( verkaeufer, "Auskunft vom Käufer" );
        final Composite kaeuferLine1 = newFormField( "Versendet am" )
                .setProperty( new PropertyAdapter( kaufvertrag.anschreibenKaeuferErstelltAm() ) )
                .setField( new DateTimeFormField() )
                .setLayoutData( new SimpleFormData( left ).create() ).setParent( kaeufer ).create();

        newFormField( "Antwort am" )
                .setProperty( new PropertyAdapter( kaufvertrag.anschreibenKaeuferEingangAntwort() ) )
                .setField( new DateTimeFormField() )
                .setLayoutData( new SimpleFormData( right ).create() ).setParent( kaeufer )
                .create();

        newFormField( "Bemerkungen" )
                .setProperty( new PropertyAdapter( kaufvertrag.bemerkungenKaeufer() ) )
                .setField( new TextFormField() )
                .setLayoutData(
                        new SimpleFormData( left ).right(100).top( kaeuferLine1, SPACING ).height( 50 )
                                .create() ).setParent( kaeufer ).create();
    }

}