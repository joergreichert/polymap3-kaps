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

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.runtime.Polymap;

import org.polymap.rhei.data.entityfeature.AssociationAdapter;
import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.CheckboxFormField;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.NumberValidator;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.model.data.GebaeudeArtComposite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class FlurstuecksdatenAgrarGrunddatenFormEditorPage
        extends FlurstuecksdatenAgrarFormEditorPage {

    private static Log         log = LogFactory.getLog( FlurstuecksdatenAgrarGrunddatenFormEditorPage.class );

    private IFormFieldListener listener;


    public FlurstuecksdatenAgrarGrunddatenFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( FlurstuecksdatenAgrarGrunddatenFormEditorPage.class.getName(), "Grunddaten", feature, featureStore );
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

        newLine = newFormField( "Lageklasse" ).setProperty( new PropertyAdapter( vb.lageklasse() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 2, 1, 2 ) )
                .setLayoutData( left().right( 33 ).top( lastLine ).create() ).setParent( client ).create();

        lastLine = newLine;
        newLine = newFormField( "ist bebaut" ).setToolTipText( "Agrarland ist bebaut" )
                .setField( new CheckboxFormField() )
                .setProperty( new PropertyAdapter( vb.istBebaut() ) ).setLayoutData( left().top( lastLine ).create() )
                .setParent( client ).create();
        pageSite.addFieldListener( listener = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == IFormFieldListener.VALUE_CHANGE
                        && ev.getFieldName().equals( vb.istBebaut().qualifiedName().name() )) {
                    enableBauFields( (Boolean)ev.getNewValue() );
                }
            }
        } );

        lastLine = newLine;
        newLine = newFormField( "Gebäudeart" ).setEnabled( vb.gebaeudeArt().get() == null )
                .setProperty( new AssociationAdapter<GebaeudeArtComposite>( vb.gebaeudeArt() ) )
                .setField( namedAssocationsPicklist( GebaeudeArtComposite.class ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( client ).create();

        lastLine = newLine;
        newLine = newFormField( "Baujahr tatsächlich" ).setProperty( new PropertyAdapter( vb.baujahr() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Integer.class, Polymap.getSessionLocale() ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( client ).create();
        newFormField( "Baujahr bereinigt" ).setProperty( new PropertyAdapter( vb.bereinigtesBaujahr() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Integer.class, Polymap.getSessionLocale() ) )
                .setLayoutData( right().top( lastLine ).create() ).setParent( client ).create();

        lastLine = newLine;
        newLine = newFormField( "Gesamtbauwert" ).setProperty( new PropertyAdapter( vb.gesamtBauWert() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 2, 1, 2 ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( client ).create();
        newFormField( "nach" ).setToolTipText( "Gesamtbauwert nach" )
                .setProperty( new PropertyAdapter( vb.gesamtBauWertNach() ) ).setEnabled( false )
                .setLayoutData( right().top( lastLine ).create() ).setParent( client ).create();

        lastLine = newLine;
        newLine = newFormField( "Bemerkungen" ).setProperty( new PropertyAdapter( vb.bemerkungen() ) )
                .setField( new TextFormField() ).setEnabled( false )
                .setLayoutData( left().right( 100 ).height( 50 ).top( lastLine ).create() ).setParent( client )
                .create();
    }


    @Override
    public void doLoad( IProgressMonitor monitor )
            throws Exception {
        super.doLoad( monitor );
        enableBauFields( vb.istBebaut().get() );
    }


    private void enableBauFields( Boolean status ) {
        boolean enabled = status != null ? status : false;
        pageSite.setFieldEnabled( vb.gebaeudeArt().qualifiedName().name(), enabled );
        pageSite.setFieldEnabled( vb.baujahr().qualifiedName().name(), enabled );
        pageSite.setFieldEnabled( vb.bereinigtesBaujahr().qualifiedName().name(), enabled );
        pageSite.setFieldEnabled( vb.gesamtBauWert().qualifiedName().name(), enabled );
    }
}
