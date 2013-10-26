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

import org.qi4j.api.entity.Entity;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.ui.forms.widgets.Section;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.form.FormEditor;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.model.data.VertragsdatenErweitertComposite;
import org.polymap.kaps.ui.FieldCalculation;
import org.polymap.kaps.ui.FieldListener;
import org.polymap.kaps.ui.FieldSummation;
import org.polymap.kaps.ui.InterEditorListener;
import org.polymap.kaps.ui.MyNumberValidator;
import org.polymap.kaps.ui.NumberFormatter;

/**
 * Erweiterte Vertragsdaten, Vertragsanteil Kaufpreis mit Zu- Abschlag
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class KaufvertragErweitertFormEditorPage
        extends KaufvertragFormEditorPage {

    private final VertragsdatenErweitertComposite erweiterteVertragsdaten;

    private FieldCalculation                      refresher;

    private FieldListener                         fieldListener;

    private FieldSummation                        vollpreis;

    private InterEditorListener                   editorListener;


    public KaufvertragErweitertFormEditorPage( FormEditor formEditor, Feature feature, FeatureStore featureStore ) {
        super( KaufvertragErweitertFormEditorPage.class.getName(), "Zu-/Abschlag", feature, featureStore );

        erweiterteVertragsdaten = getOrCreateErweiterteVertragsdaten( kaufvertrag );
        EventManager.instance().subscribe( fieldListener = new FieldListener( kaufvertrag.vollpreis() ),
                new FieldListener.EventFilter( formEditor ) );
        EventManager.instance().subscribe(
                editorListener = new InterEditorListener( erweiterteVertragsdaten.wertbeeinflussendeUmstaende() ) {

                    @Override
                    protected void onChangedValue( IFormEditorPageSite site, Entity entity, String fieldName,
                            Object newValue ) {
                        if (fieldName.equals( erweiterteVertragsdaten.wertbeeinflussendeUmstaende().qualifiedName()
                                .name() )) {
                            site.setFieldValue( fieldName,
                                    newValue != null ? NumberFormatter.getFormatter( 2 ).format( newValue ) : null );
                        }
                    }

                }, new InterEditorListener.EventFilter( kaufvertrag ) );
    }


    @Override
    public void dispose() {
        EventManager.instance().unsubscribe( fieldListener );
        EventManager.instance().unsubscribe( editorListener );
    }


    @Override
    public void afterDoLoad( IProgressMonitor monitor )
            throws Exception {
        fieldListener.flush( pageSite );
        editorListener.flush( pageSite );
    }


    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );

        Composite newLine, lastLine = null;
        Composite parent = site.getPageBody();

        Section section = newSection( parent, "Vollpreisberechnung" );
        Composite client = (Composite)section.getClient();
        newLine = newFormField( "Vollpreis" ).setEnabled( false )
                .setProperty( new PropertyAdapter( erweiterteVertragsdaten.basispreis() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( client ).create();
        site.addFieldListener( vollpreis = new FieldSummation( site, 2, erweiterteVertragsdaten.basispreis(),
                kaufvertrag.vollpreis() ) );

        lastLine = newLine;
        newLine = newFormField( "Zuschlag" ).setProperty( new PropertyAdapter( erweiterteVertragsdaten.zuschlag() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( client ).create();

        newFormField( "Bemerkung Zuschlag" )
                .setProperty( new PropertyAdapter( erweiterteVertragsdaten.zuschlagBemerkung() ) )
                .setField( new StringFormField() ).setLayoutData( right().top( lastLine ).create() ).setParent( client )
                .create();

        lastLine = newLine;
        newLine = newFormField( "Abschlag" ).setProperty( new PropertyAdapter( erweiterteVertragsdaten.abschlag() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( client ).create();

        newFormField( "Bemerkung Abschlag" )
                .setProperty( new PropertyAdapter( erweiterteVertragsdaten.abschlagBemerkung() ) )
                .setField( new StringFormField() ).setLayoutData( right().top( lastLine ).create() ).setParent( client )
                .create();

        lastLine = newLine;
        newLine = newFormField( "Sonstiges" )
                .setToolTipText( "Sonstige wertbeeinflußende Umstände, bspw. aus Ertragswertberechnung" )
                .setProperty( new PropertyAdapter( erweiterteVertragsdaten.wertbeeinflussendeUmstaende() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) ).setEnabled( false )
                .setLayoutData( left().top( lastLine ).create() ).setParent( client ).create();

        lastLine = newLine;
        newLine = newFormField( "bereinigter Vollpreis" )
                .setProperty( new PropertyAdapter( erweiterteVertragsdaten.bereinigterVollpreis() ) )
                .setEnabled( false ).setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) )
                .setLayoutData( left().top( lastLine ).bottom( 100 ).create() ).setParent( client ).create();

        site.addFieldListener( refresher = new FieldCalculation( site, 2, erweiterteVertragsdaten
                .bereinigterVollpreis(), erweiterteVertragsdaten.basispreis(), erweiterteVertragsdaten.zuschlag(),
                erweiterteVertragsdaten.wertbeeinflussendeUmstaende(), erweiterteVertragsdaten.abschlag() ) {

            @Override
            protected Double calculate( ValueProvider values ) {
                Double result = values.get( erweiterteVertragsdaten.basispreis() );
                Double n = values.get( erweiterteVertragsdaten.zuschlag() );
                Double z = values.get( erweiterteVertragsdaten.abschlag() );
                Double w = values.get( erweiterteVertragsdaten.wertbeeinflussendeUmstaende() );

                if (result != null && n != null) {
                    result += n;
                }
                if (result != null && z != null) {
                    result -= z;
                }
                if (result != null && w != null) {
                    result += w;
                }
                return result;
            }

        } );
    }


    private VertragsdatenErweitertComposite getOrCreateErweiterteVertragsdaten( VertragComposite kaufvertrag ) {
        VertragsdatenErweitertComposite vdec = kaufvertrag.erweiterteVertragsdaten().get();
        if (vdec == null) {
            vdec = repository.newEntity( VertragsdatenErweitertComposite.class, null );
            kaufvertrag.erweiterteVertragsdaten().set( vdec );
            vdec.basispreis().set( kaufvertrag.kaufpreis().get() );
        }
        return vdec;
    }
}