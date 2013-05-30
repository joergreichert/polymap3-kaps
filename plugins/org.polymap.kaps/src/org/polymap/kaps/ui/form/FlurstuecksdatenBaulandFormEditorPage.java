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

import java.text.NumberFormat;

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;

import org.qi4j.api.property.Property;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.runtime.Polymap;

import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.NumberValidator;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.form.IFormEditorPage;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.model.data.FlurstuecksdatenBaulandComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.ui.KapsDefaultFormEditorPage;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public abstract class FlurstuecksdatenBaulandFormEditorPage
        extends KapsDefaultFormEditorPage
        implements IFormEditorPage {

    protected FlurstuecksdatenBaulandComposite vb;


    public FlurstuecksdatenBaulandFormEditorPage( String id, String title, Feature feature, FeatureStore featureStore ) {
        super( id, title, feature, featureStore );

        vb = repository.findEntity( FlurstuecksdatenBaulandComposite.class, feature.getIdentifier().getID() );
    }


    @Override
    public void createFormContent( IFormEditorPageSite site ) {
        super.createFormContent( site );

        VertragComposite kaufvertrag = vb.kaufvertrag().get();
        String nummer = EingangsNummerFormatter.format( kaufvertrag.eingangsNr().get() );
        site.setEditorTitle( formattedTitle( "Flurstücksdaten Bauland", nummer, null ) );
        site.setFormTitle( formattedTitle( "erweiterte Flurstücksdaten - Bauland - für Vertrag", nummer, getTitle() ) );

    }


    protected NumberFormat getFormatter( int fractionDigits ) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits( fractionDigits );
        nf.setMinimumFractionDigits( fractionDigits );
        nf.setMinimumIntegerDigits( 1 );
        return nf;
    }


    protected Composite createPreisField( Property<Double> property, SimpleFormData data, Composite parent,
            boolean editable ) {
        return createPreisField( IFormFieldLabel.NO_LABEL, property, data, parent, editable );
    }


    protected Composite createPreisField( String label, Property<Double> property, SimpleFormData data,
            Composite parent, boolean editable ) {
        return createNumberField( label, property, data, parent, editable, 2 );
    }


    protected Composite createFlaecheField( Property<Double> property, SimpleFormData data, Composite parent,
            boolean editable ) {
        return createFlaecheField( IFormFieldLabel.NO_LABEL, property, data, parent, editable );
    }


    protected Composite createFlaecheField( String label, Property<Double> property, SimpleFormData data,
            Composite parent, boolean editable ) {
        return createNumberField( label, property, data, parent, editable, 0 );
    }


    private Composite createNumberField( String label, Property<Double> property, SimpleFormData data,
            Composite parent, boolean editable, int fractionDigits ) {
        return newFormField( label )
                .setProperty( new PropertyAdapter( property ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator(
                        new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, fractionDigits, 1,
                                fractionDigits ) ).setLayoutData( data.create() ).setParent( parent )
                .setEnabled( editable ).create();
    }


    protected Control createLabel( Composite parent, String text, SimpleFormData data, int style ) {
        Control label = pageSite.getToolkit().createLabel( parent, text, style );
        label.setLayoutData( data.create() );
        return label;
    }
}