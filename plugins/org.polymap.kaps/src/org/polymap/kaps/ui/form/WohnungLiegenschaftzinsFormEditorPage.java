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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.DateTimeFormField;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.ui.FieldCalculation;
import org.polymap.kaps.ui.FieldListener;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class WohnungLiegenschaftzinsFormEditorPage
        extends WohnungFormEditorPage {

    private static Log       log   = LogFactory.getLog( WohnungLiegenschaftzinsFormEditorPage.class );

    private static final int ONE   = 0;

    private static final int TWO   = 23;

    private static final int THREE = 43;

    private static final int FOUR  = 71;

    private static final int FIVE  = 80;

    private static final int SIX   = 100;

    private FieldCalculation bebabschlag;

    private FieldCalculation berPreis;

    private FieldListener    fieldListener;


    // private IFormFieldListener gemeindeListener;

    public WohnungLiegenschaftzinsFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( WohnungLiegenschaftzinsFormEditorPage.class.getName(), "Liegenschaftszins", feature, featureStore );
        EventManager.instance().subscribe( fieldListener = new FieldListener( wohnung.wohnflaeche() ),
                new EventFilter<FormFieldEvent>() {

                    public boolean apply( FormFieldEvent ev ) {
                        return ev.getEventCode() == IFormFieldListener.VALUE_CHANGE;
                    }
                } );
    }


    protected SimpleFormData one() {
        return new SimpleFormData( SPACING ).left( ONE ).right( TWO );
    }


    protected SimpleFormData two() {
        return new SimpleFormData( SPACING ).left( TWO ).right( THREE );
    }


    protected SimpleFormData twothree() {
        return new SimpleFormData( SPACING ).left( TWO ).right( FOUR );
    }


    protected SimpleFormData three() {
        return new SimpleFormData( SPACING ).left( THREE ).right( FOUR );
    }


    protected SimpleFormData four() {
        return new SimpleFormData( SPACING ).left( FOUR ).right( FIVE );
    }


    protected SimpleFormData five() {
        return new SimpleFormData( SPACING ).left( FIVE ).right( SIX );
    }


    @SuppressWarnings("unchecked")
    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );

        Control newLine, lastLine = null;
        Composite parent = pageSite.getPageBody();

        newLine = createLabel( parent, "Mietfestsetzung seit", one().top( lastLine ), SWT.RIGHT );
        newFormField( IFormFieldLabel.NO_LABEL ).setProperty( new PropertyAdapter( wohnung.mietfestsetzungSeit() ) )
                .setField( new DateTimeFormField() ).setLayoutData( two().top( lastLine ).create() ).create();
        createLabel( parent, "Bodenpreis in €/m²", three().top( lastLine ), SWT.RIGHT );
        createPreisField( wohnung.bodenpreis(), five().top( lastLine ), parent, true );

        lastLine = newLine;
        newLine = // createLabel( parent, "Richtwert in €/m²", one().top( lastLine ),
                  // SWT.RIGHT );
        // createPreisField( wohnung., data, parent, editable )
        createLabel( parent, "Bebauungsabschlag in %", three().top( lastLine ), SWT.RIGHT );
        createFlaecheField( wohnung.bebauungsabschlagInProzent(), four().top( lastLine ), parent, true );
        createPreisField( wohnung.bebauungsabschlag(), five().top( lastLine ), parent, false );
        site.addFieldListener( bebabschlag = new FieldCalculation( site, 2, wohnung.bebauungsabschlag(), wohnung
                .bebauungsabschlagInProzent(), wohnung.bodenpreis() ) {

            @Override
            protected Double calculate( ValueProvider values ) {
                Double prozent = values.get( wohnung.bebauungsabschlagInProzent() );
                Double preis = values.get( wohnung.bodenpreis() );
                if (preis == null) {
                    return null;
                }
                if (prozent == null) {
                    return preis;
                }
                return preis * prozent / 100;
            }

        } );

        lastLine = newLine;
        newLine = createLabel( parent, "monatlicher Rohertrag in €", one().top( lastLine ), SWT.RIGHT );
        createPreisField( wohnung.monatlicherRohertrag(), two().top( lastLine ), parent, true );

        createLabel( parent, "bereinigter Bodenpreis", three().top( lastLine ), SWT.RIGHT );
        createPreisField( wohnung.bereinigterBodenpreis(), five().top( lastLine ), parent, true );
        site.addFieldListener( berPreis = new FieldCalculation( site, 2, wohnung.bereinigterBodenpreis(), wohnung
                .bebauungsabschlag(), wohnung.bodenpreis() ) {

            @Override
            protected Double calculate( ValueProvider values ) {
                Double abschlag = values.get( wohnung.bebauungsabschlag() );
                Double preis = values.get( wohnung.bodenpreis() );
                if (preis == null) {
                    return null;
                }
                if (abschlag == null) {
                    return preis;
                }
                return preis - abschlag;
            }

        } );

        lastLine = newLine;
        newLine = createLabel( parent, "Rohertrag in €/m²", one().top( lastLine ), SWT.RIGHT );
        createPreisField( wohnung.monatlicherRohertragJeQm(), two().top( lastLine ), parent, true );
//        createLabel( parent, "bereinigter Bodenpreis", three().top( lastLine ), SWT.RIGHT );
//        // createFlaecheField( wohnung.bebauungsabschlagInProzent(), four().top(
//        // lastLine ), parent, true );
//        createPreisField( wohnung.bereinigterBodenpreis(), five().top( lastLine ), parent, true );
//        site.addFieldListener( berPreis = new FieldCalculation( site, 2, wohnung.bereinigterBodenpreis(), wohnung
//                .bebauungsabschlag(), wohnung.bodenpreis() ) {
//
//            @Override
//            protected Double calculate( ValueProvider values ) {
//                Double abschlag = values.get( wohnung.bebauungsabschlag() );
//                Double preis = values.get( wohnung.bodenpreis() );
//                if (preis == null) {
//                    return null;
//                }
//                if (abschlag == null) {
//                    return preis;
//                }
//                return preis - abschlag;
//            }
//
//        } );
    }


    @Override
    public void afterDoLoad( IProgressMonitor monitor )
            throws Exception {
        if (fieldListener.get( wohnung.wohnflaeche() ) != null) {
            pageSite.fireEvent( this, wohnung.wohnflaeche().qualifiedName().name(), IFormFieldListener.VALUE_CHANGE,
                    fieldListener.get( wohnung.wohnflaeche() ) );
        }
    }
}
