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
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.model.data.VertragsdatenErweitertComposite;

/**
 * Erweiterte Vertragsdaten, Vertragsanteil Kaufpreis mit Zu- Abschlag
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class KaufvertragErweitertFormEditorPage
        extends KaufvertragFormEditorPage {

    private final VertragsdatenErweitertComposite erweiterteVertragsdaten;
    private KaufpreisBereinigtRefresher refresher;
    
    public KaufvertragErweitertFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( KaufvertragErweitertFormEditorPage.class.getName(), "Zu-/Abschlag", feature,
                featureStore );
        
        erweiterteVertragsdaten = getErweiterteVertragsdaten(kaufvertrag);
    }


    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );
        
        Composite newLine, lastLine = null;
        Composite parent = pageSite.getPageBody();
        
        parent = newSection( parent, "Vollpreisberechnung" );
        newLine = newFormField( "Vollpreis" ).setEnabled( false )
                .setProperty( new PropertyAdapter( erweiterteVertragsdaten.vollpreis() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 2, 1, 2 ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( parent )
                .create();

        lastLine = newLine;
        newLine = newFormField( "Zuschlag" ).setProperty( new PropertyAdapter( erweiterteVertragsdaten.zuschlag() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 2, 1, 2 ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( parent )
                .create();

        newFormField( "Bemerkung Zuschlag" )
                .setProperty( new PropertyAdapter( erweiterteVertragsdaten.zuschlagBemerkung() ) )
                .setField( new StringFormField() ).setLayoutData( right().top( lastLine ).create() )
                .setParent( parent ).create();

        lastLine = newLine;
        newLine = newFormField( "Abschlag" ).setProperty( new PropertyAdapter( erweiterteVertragsdaten.abschlag() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 2, 1, 2 ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( parent )
                .create();

        newFormField( "Bemerkung Abschlag" )
                .setProperty( new PropertyAdapter( erweiterteVertragsdaten.abschlagBemerkung() ) )
                .setField( new StringFormField() ).setLayoutData( right().top( lastLine ).create() )
                .setParent( parent ).create();

        lastLine = newLine;
        // TODO Refresher
        newLine = newFormField( "bereinigter Vollpreis" )
                .setProperty( new PropertyAdapter( erweiterteVertragsdaten.bereinigterVollpreis() ) ).setEnabled( false )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 2, 1, 2 ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( parent )
                .create();
        
        
        site.addFieldListener( refresher = new KaufpreisBereinigtRefresher( site, erweiterteVertragsdaten ) );
    }


    /**
     *
     * @param kaufvertrag
     * @return
     */
    private VertragsdatenErweitertComposite getErweiterteVertragsdaten( VertragComposite kaufvertrag ) {
        VertragsdatenErweitertComposite vdec = kaufvertrag.erweiterteVertragsdaten().get();
        if (vdec == null) {
            vdec = repository.newEntity( VertragsdatenErweitertComposite.class, null );
            kaufvertrag.erweiterteVertragsdaten().set( vdec );
        }
        return vdec;
    }
}