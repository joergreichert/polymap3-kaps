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

import java.util.HashMap;
import java.util.Map;

import java.text.NumberFormat;

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;

import org.qi4j.api.property.Property;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.ui.forms.widgets.Section;

import org.polymap.core.project.ui.util.SimpleFormData;

import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.CheckboxFormField;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.form.FormEditor;
import org.polymap.rhei.form.IFormEditorPage;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.model.data.AusstattungBewertungComposite;
import org.polymap.kaps.model.data.WohnungComposite;
import org.polymap.kaps.ui.KapsDefaultFormEditorPage;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class BewertungAnhandVonAustattungsmerkmalenFormEditorPage
        extends KapsDefaultFormEditorPage
        implements IFormEditorPage {

    public class BooleanListener
            implements IFormFieldListener {

        private final Map<String, Boolean> values = new HashMap<String, Boolean>();


        public void put( String fieldName, Boolean value ) {
            values.put( fieldName, value );
        }


        private final Map<String, Integer> terms;

        private IFormEditorPageSite        site;

        private Property<Double>           result;

        private Integer maxValue;


        public BooleanListener( IFormEditorPageSite site, Integer maxValue, Property<Double> result,
                Map<Property<Boolean>, Integer> operators ) {
            this.site = site;
            this.result = result;
            this.maxValue = maxValue;
            
            terms = new HashMap<String, Integer>();

            for (Property<Boolean> term : operators.keySet()) {
                terms.put( term.qualifiedName().name(), operators.get( term ) );
            }
        }


        @Override
        public void fieldChange( FormFieldEvent ev ) {
            if (ev.getEventCode() != IFormFieldListener.VALUE_CHANGE) {
                return;
            }
            String fieldName = ev.getFieldName();
            if (terms.keySet().contains( fieldName )) {
                put( fieldName, (Boolean)ev.getNewValue() );
                //
                refreshResult();
            }
        }


        public final void refreshResult() {
            Double result = 0.0d;
            for (String fieldName : values.keySet()) {
                Boolean isSet = values.get( fieldName );

                if (isSet != null && isSet.booleanValue()) {
                    result += terms.get( fieldName );
                }
            }
            site.setFieldValue( this.result.qualifiedName().name(), getFormatter().format( Math.min( maxValue, result ) ) );
        }

        private NumberFormat getFormatter() {
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits( 0 );
            nf.setMinimumFractionDigits( 0 );
            nf.setMinimumIntegerDigits( 1 );
            return nf;
        }
    }

    protected AusstattungBewertungComposite vb;

    private BooleanListener                 listener1;

    private FormEditor formEditor;

    private BooleanListener listener2;


    public BewertungAnhandVonAustattungsmerkmalenFormEditorPage( FormEditor editor, Feature feature,
            FeatureStore featureStore ) {
        super( BewertungAnhandVonAustattungsmerkmalenFormEditorPage.class.getName(), "Bewertung", feature, featureStore );
        this.formEditor = editor;

        vb = repository.findEntity( AusstattungBewertungComposite.class, feature.getIdentifier().getID() );

        if (vb.wohnung().get() == null) {
            throw new IllegalStateException(
                    "Bitte nutzen Sie ausschließlich den Knopf 'Bewerten' bei Wohnung, um eine solche Bewertung zu erstellen." );
        }
    }

    @Override
    public void createFormContent( IFormEditorPageSite site ) {
        super.createFormContent( site );

        final WohnungComposite wohnung = vb.wohnung().get();
        site.setEditorTitle( formattedTitle( "Bewertung für Wohnung", wohnung != null ? wohnung.schl().get() : null,
                null ) );
        site.setFormTitle( formattedTitle( "Bewertung anhang von Ausstattungsmerkmalen für Wohnung",
                wohnung != null ? wohnung.schl().get() : null, getTitle() ) );

        Control newLine = null;
        Control lastLine = null;
        Composite parent = pageSite.getPageBody();

        Section section1 = newSection( parent, "Heizung (maximal 12 Punkte)" );
        section1.setLayoutData( new SimpleFormData( SECTION_SPACING ).left( 0 ).right( 50 ).create() );
        Composite client1 = (Composite)section1.getClient();

        Map<Property<Boolean>, Integer> values1 = new HashMap<Property<Boolean>, Integer>();
        newLine = createCheckboxField( "Zentralheizung", values1, 10, vb.ME11(), client1, newLine );
        newLine = createCheckboxField( "Eletrospeicheröfen/Einzelofen", values1, 2, vb.ME12(), client1, newLine );
        newLine = createCheckboxField( "Kachelofen mit automatische Befeuerung", values1, 4, vb.ME13(), client1, newLine );
        newLine = createCheckboxField( "Kachelofen mit manueller Befeuerung", values1, 2, vb.ME14(), client1, newLine );
        newLine = createCheckboxField( "Fußbodenheizung", values1, 4, vb.ME15(), client1, newLine );

        lastLine = newLine;
        newLine = createLabel( client1, "Summe", one().top( lastLine ), SWT.CENTER );
        createFlaecheField( vb.ME1P(), two().bottom( 100 ).top( lastLine ), client1, false );
        site.addFieldListener( listener1 = new BooleanListener( site, 12, vb.ME1P(), values1) );
        
        // Section 2
        Section section2 = newSection( parent, "WC - separat (maximal 6 Punkte)" );
        section2.setLayoutData( new SimpleFormData( SECTION_SPACING ).left( 0 ).right( 50 ).top( section1 ).create() );
        Composite client2 = (Composite)section2.getClient();

        Map<Property<Boolean>, Integer> values2 = new HashMap<Property<Boolean>, Integer>();
        newLine = createCheckboxField( "innerhalb der Wohnung", values2, 1, vb.ME21(), client2, newLine );
        newLine = createCheckboxField( "Wände deckenhoch gefliest", values2, 2, vb.ME22(), client2, newLine );
        newLine = createCheckboxField( "1 Waschbecken", values2, 2, vb.ME23(), client2, newLine );
        newLine = createCheckboxField( "2 Waschbecken", values2, 2, vb.ME24(), client2, newLine );

        lastLine = newLine;
        newLine = createLabel( client2, "Summe", one().top( lastLine ), SWT.CENTER );
        createFlaecheField( vb.ME2P(), two().bottom( 100 ).top( lastLine ), client2, false );
        site.addFieldListener( listener2 = new BooleanListener( site, 6, vb.ME2P(), values2) );
        

    }


    private Control createCheckboxField( String label, Map<Property<Boolean>, Integer> values, Integer count, Property<Boolean> property, Composite client, Control top ) {
        values.put( property, count );
        label = label + " (" + count + ")";
        
        Control newLine = createLabel( client, label, one().top( top ), SWT.RIGHT );
        
        newFormField( IFormFieldLabel.NO_LABEL ).setProperty( new PropertyAdapter( property ) ).setToolTipText( label )
                .setField( new CheckboxFormField() ).setEnabled( true ).setLayoutData( two().top( top ).create() ).setParent( client )
                .create();
        
        return newLine;
    }

    @Override
    protected SimpleFormData one() {
        return new SimpleFormData( SPACING ).left( 0 ).right( 80 );
    }


    protected SimpleFormData two() {
        return new SimpleFormData( SPACING ).left( 80 ).right( 100 );
    }

}