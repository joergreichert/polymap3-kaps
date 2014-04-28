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
import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.form.FormEditor;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.ui.FieldCalculation;
import org.polymap.kaps.ui.FieldListener;
import org.polymap.kaps.ui.FieldMultiplication;
import org.polymap.kaps.ui.FieldSummation;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class ErtragswertverfahrenBewirtschaftungskostenFormEditorPage
        extends ErtragswertverfahrenFormEditorPage {

    private static Log          log         = LogFactory
                                                    .getLog( ErtragswertverfahrenBewirtschaftungskostenFormEditorPage.class );

    @SuppressWarnings("unused")
    private IFormFieldListener  line1multiplicator;

    @SuppressWarnings("unused")
    private IFormFieldListener  line2multiplicator;

    @SuppressWarnings("unused")
    private IFormFieldListener  line3multiplicator;

    @SuppressWarnings("unused")
    private IFormFieldListener  line4multiplicator;

    @SuppressWarnings("unused")
    private IFormFieldListener  line5multiplicator;

    @SuppressWarnings("unused")
    private IFormFieldListener  line6multiplicator;

    private FieldMultiplication line7multiplicator;

    private FieldCalculation    bewKostenJahrSummation;

    private FieldCalculation    reinertragCalculation;

    private IFormFieldListener  pauschalListener;

    private boolean             initialized = false;

    // private Double anteiligeBetriebskosten;

    // private Double bruttoRohertragProJahr;

    private Boolean             summePauschal;

    private FieldListener       fieldListener;

    private FieldSummation      jahresBetriebskostenListener;

    private boolean             summeProzent;


    public ErtragswertverfahrenBewirtschaftungskostenFormEditorPage( FormEditor formEditor, Feature feature,
            FeatureStore featureStore ) {
        super( ErtragswertverfahrenBewirtschaftungskostenFormEditorPage.class.getName(), "Bewirtschaftungskosten",
                feature, featureStore );

        // summePauschal = vb.pauschalBewirtschaftungskosten().get();

        EventManager.instance().subscribe(
                fieldListener = new FieldListener( vb.jahresBetriebskosten(), vb.bruttoRohertragProJahr() ),
                new FieldListener.EventFilter( formEditor ) );
    }


    @Override
    public void dispose() {
        super.dispose();
        EventManager.instance().unsubscribe( fieldListener );
    }


    @Override
    public void afterDoLoad( IProgressMonitor monitor )
            throws Exception {
        fieldListener.flush( pageSite );
    }


    @SuppressWarnings("unchecked")
    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );

        initialized = true;

        Control newLine, lastLine = null;
        Composite parent = pageSite.getPageBody();

        // Section section = newSection( parent, "Bodenwertaufteilung" );
        // Composite client = (Composite)section.getClient();
        Composite client = parent;

        newLine = createLabel( client, "Bewirtschaftungskosten pauschal?", one().top( lastLine, 20 ),
                SWT.RIGHT );
        createBooleanField( vb.pauschalBewirtschaftungskosten(), two().top( lastLine, 20 ), client );
        createPreisField( vb.bewirtschaftungskostenPauschal(), three().top( lastLine, 20 ), client, false );

        site.addFieldListener( pauschalListener = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == IFormFieldListener.VALUE_CHANGE) {
                    if (ev.getFieldName().equals( vb.pauschalBewirtschaftungskosten().qualifiedName().name() )) {
                        Boolean value = (Boolean)ev.getNewValue();
                        enablePauschal( site, value );
                    }
                    else if (ev.getFieldName().equals(
                            vb.bewirtschaftungskostenInProzentDesJahresRohertragsErfassen().qualifiedName().name() )) {
                        Boolean value = (Boolean)ev.getNewValue();
                        enablePauschalInProzent( site, value );
                    }
                }
            }
        } );

        lastLine = newLine;
        newLine = createLabel( client, "Bewirtschaftungskosten in % vom Rohertrag(brutto)?",
                one().top( lastLine ), SWT.RIGHT );
        createBooleanField( vb.bewirtschaftungskostenInProzentDesJahresRohertragsErfassen(), two().top( lastLine ),
                client );
        createFlaecheField( vb.bewirtschaftungskostenInProzentDesJahresRohertrags(), three().top( lastLine ), client,
                false );

        lastLine = newLine;
        newLine = createLabel( client, "anteilige Betriebskosten", one().top( lastLine, 20 ), SWT.RIGHT );
        createPreisField( vb.anteiligeBetriebskosten(), three().top( lastLine, 20 ), client, false );
        site.addFieldListener( jahresBetriebskostenListener = new FieldSummation( site, 2,
                vb.anteiligeBetriebskosten(), vb.jahresBetriebskosten() ) );

        lastLine = newLine;
        newLine = createLabel( client, "Verwaltungskosten", one().top( lastLine ), SWT.RIGHT );
        createPreisField( vb.verwaltungskosten(), three().top( lastLine ), client, true );

        lastLine = newLine;
        newLine = createLabel( client, "Instandhaltungskosten", one().top( lastLine ), SWT.RIGHT );
        createPreisField( vb.instandhaltungskosten(), three().top( lastLine ), client, true );

        lastLine = newLine;
        newLine = createLabel( client, "Mietausfallwagnis", one().top( lastLine ), SWT.RIGHT );
        createPreisField( vb.mietausfallWagnis(), three().top( lastLine ), client, true );

        lastLine = newLine;
        newLine = createTextField( vb.bewirtschaftungskostenZeile5Text(), one().top( lastLine ), client );
        createPreisField( vb.bewirtschaftungskostenZeile5(), three().top( lastLine ), client, true );

        lastLine = newLine;
        newLine = createLabel( client, "jährliche Bewirtschaftungskosten in €", one().top( lastLine, 30 ), SWT.RIGHT );
        createPreisField( vb.summeBewirtschaftungskosten(), three().top( lastLine, 30 ), client, false );
        site.addFieldListener( bewKostenJahrSummation = new FieldCalculation( site, 2, vb.summeBewirtschaftungskosten(),
                vb.anteiligeBetriebskosten(), vb.verwaltungskosten(), vb.instandhaltungskosten(), vb
                        .mietausfallWagnis(), vb.bewirtschaftungskostenZeile5(), vb.bewirtschaftungskostenPauschal(), vb
                        .bruttoRohertragProJahr(), vb.bewirtschaftungskostenInProzentDesJahresRohertrags() ) {

            @Override
            protected Double calculate( ValueProvider values ) {
                if (summePauschal) {
                    return values.get( vb.bewirtschaftungskostenPauschal() );
                }
                if (summeProzent) {
                    Double result = values.get( vb.bruttoRohertragProJahr() );
                    Double prozent = values.get( vb.bewirtschaftungskostenInProzentDesJahresRohertrags() );
                    if (result != null && prozent != null) {
                        return result / 100 * prozent;
                    }
                    return null;
                }
                // summe über alles
                Double result = new Double( 0.0d );
                if (values.get( vb.anteiligeBetriebskosten() ) != null) {
                    result += values.get( vb.anteiligeBetriebskosten() );
                }
                if (values.get( vb.verwaltungskosten() ) != null) {
                    result += values.get( vb.verwaltungskosten() );
                }
                if (values.get( vb.instandhaltungskosten() ) != null) {
                    result += values.get( vb.instandhaltungskosten() );
                }
                if (values.get( vb.mietausfallWagnis() ) != null) {
                    result += values.get( vb.mietausfallWagnis() );
                }
                if (values.get( vb.bewirtschaftungskostenZeile5() ) != null) {
                    result += values.get( vb.bewirtschaftungskostenZeile5() );
                }
                return result;
            }
        } );

        lastLine = newLine;
        newLine = createLabel( client, "jährlicher Reinertrag in €",
                "jährlicher Rohertrag (brutto) - jährliche Bewirtschaftungskosten", one().top( lastLine ), SWT.RIGHT );
        createPreisField( vb.jahresReinErtrag(), three().top( lastLine ), client, false );
        site.addFieldListener( reinertragCalculation = new FieldCalculation( site, 2, vb.jahresReinErtrag(), vb
                .summeBewirtschaftungskosten(), vb.bruttoRohertragProJahr() ) {

            @Override
            protected Double calculate( ValueProvider values ) {
                Double reinertrag = values.get( vb.bruttoRohertragProJahr() );
                Double kosten = values.get( vb.summeBewirtschaftungskosten() );

                if (reinertrag != null && kosten != null) {
                    reinertrag = reinertrag - kosten;
                }

                return reinertrag;
            }
        } );

        enablePauschal( site, vb.pauschalBewirtschaftungskosten().get() );
        enablePauschalInProzent( site, vb.bewirtschaftungskostenInProzentDesJahresRohertragsErfassen().get() );
    }


    private void enablePauschal( IFormEditorPageSite site, Boolean pauschal ) {
        summePauschal = pauschal == null ? false : pauschal.booleanValue();
        if (summeProzent && summePauschal) {
            // disable summeProzent
            site.setFieldValue( vb.bewirtschaftungskostenInProzentDesJahresRohertragsErfassen().qualifiedName().name(),
                    Boolean.FALSE );
            enablePauschalInProzent( site, Boolean.FALSE );
        }
        site.setFieldEnabled( vb.bewirtschaftungskostenPauschal().qualifiedName().name(), summePauschal );
        enableDetails( site, !summePauschal && !summeProzent );
        bewKostenJahrSummation.refreshResult();
    }


    private void enablePauschalInProzent( IFormEditorPageSite site, Boolean prozent ) {
        summeProzent = prozent == null ? false : prozent.booleanValue();
        if (summeProzent && summePauschal) {
            // disable summePauschal
            site.setFieldValue( vb.pauschalBewirtschaftungskosten().qualifiedName().name(), Boolean.FALSE );
            enablePauschal( site, Boolean.FALSE );
        }
        site.setFieldEnabled( vb.bewirtschaftungskostenInProzentDesJahresRohertrags().qualifiedName().name(),
                summeProzent );
        enableDetails( site, !summePauschal && !summeProzent );
        bewKostenJahrSummation.refreshResult();
    }


    private void enableDetails( IFormEditorPageSite site, Boolean enable ) {
        site.setFieldEnabled( vb.verwaltungskosten().qualifiedName().name(), enable );
        site.setFieldEnabled( vb.instandhaltungskosten().qualifiedName().name(), enable );
        site.setFieldEnabled( vb.mietausfallWagnis().qualifiedName().name(), enable );
        site.setFieldEnabled( vb.bewirtschaftungskostenZeile5().qualifiedName().name(), enable );
        site.setFieldEnabled( vb.bewirtschaftungskostenZeile5Text().qualifiedName().name(), enable );
    }


    @Override
    protected SimpleFormData one() {
        return new SimpleFormData( SPACING ).left( 0 ).right( 45 );
    }


    @Override
    protected SimpleFormData two() {
        return new SimpleFormData( SPACING ).left( 45 ).right( 55 );
    }


    @Override
    protected SimpleFormData three() {
        return new SimpleFormData( SPACING ).left( 55 ).right( 75 );
    }

}
