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

    private FieldSummation      bewKostenJahrSummation;

    private FieldCalculation    reinertragCalculation;

    private IFormFieldListener  pauschalListener;

    private boolean             initialized = false;

    // private Double anteiligeBetriebskosten;

    // private Double bruttoRohertragProJahr;

    private boolean             summePauschal;

    private FieldListener       fieldListener;

    private FieldSummation      jahresBetriebskostenListener;


    public ErtragswertverfahrenBewirtschaftungskostenFormEditorPage( FormEditor formEditor, Feature feature,
            FeatureStore featureStore ) {
        super( ErtragswertverfahrenBewirtschaftungskostenFormEditorPage.class.getName(), "Bewirtschaftungskosten",
                feature, featureStore );

        summePauschal = vb.pauschalBewirtschaftungskosten().get();

        EventManager.instance().subscribe(
                fieldListener = new FieldListener( vb.jahresBetriebskosten(), vb.bruttoRohertragProJahr() ) , new FieldListener.EventFilter( formEditor ) );
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

        newLine = createLabel( client, "Bewirtschaftungskosten pauschal eingeben?", one().top( lastLine, 20 ),
                SWT.RIGHT );
        createBooleanField( vb.pauschalBewirtschaftungskosten(), two().top( lastLine, 20 ), client );
        site.addFieldListener( pauschalListener = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == IFormFieldListener.VALUE_CHANGE
                        && ev.getFieldName().equals( vb.pauschalBewirtschaftungskosten().qualifiedName().name() )) {
                    Boolean value = (Boolean)ev.getNewValue();
                    enablePauschal( site, value );
                }
            }
        } );

        lastLine = newLine;
        newLine = createLabel( client, "anteilige Betriebskosten", one().top( lastLine, 20 ), SWT.RIGHT );
        createPreisField( vb.anteiligeBetriebskosten(), two().top( lastLine, 20 ), client, false );
        site.addFieldListener( jahresBetriebskostenListener = new FieldSummation( site, 2,
                vb.anteiligeBetriebskosten(), vb.jahresBetriebskosten() ) );

        lastLine = newLine;
        newLine = createLabel( client, "Verwaltungskosten", one().top( lastLine ), SWT.RIGHT );
        createPreisField( vb.verwaltungskosten(), two().top( lastLine ), client, true );

        lastLine = newLine;
        newLine = createLabel( client, "Instandhaltungskosten", one().top( lastLine ), SWT.RIGHT );
        createPreisField( vb.instandhaltungskosten(), two().top( lastLine ), client, true );

        lastLine = newLine;
        newLine = createLabel( client, "Mietausfallwagnis", one().top( lastLine ), SWT.RIGHT );
        createPreisField( vb.mietausfallWagnis(), two().top( lastLine ), client, true );

        lastLine = newLine;
        newLine = createTextField( vb.bewirtschaftskostenZeile5Text(), one().top( lastLine ), client );
        createPreisField( vb.bewirtschaftskostenZeile5(), two().top( lastLine ), client, true );

        lastLine = newLine;
        newLine = createLabel( client, "jährliche Bewirtschaftungskosten in €", one().top( lastLine, 30 ), SWT.RIGHT );
        createPreisField( vb.summeBewirtschaftskosten(), two().top( lastLine, 30 ), client, false );
        site.addFieldListener( bewKostenJahrSummation = new FieldSummation( site, 2, vb.summeBewirtschaftskosten(), vb
                .anteiligeBetriebskosten(), vb.verwaltungskosten(), vb.instandhaltungskosten(), vb.mietausfallWagnis(),
                vb.bewirtschaftskostenZeile5() ) {

            @Override
            public void refreshResult() {
                if (!summePauschal) {
                    super.refreshResult();
                }
            }
        } );

        lastLine = newLine;
        newLine = createLabel( client, "jährlicher Reinertrag in €",
                "jährlicher Rohertrag (brutto) - jährliche Bewirtschaftungskosten", one().top( lastLine ), SWT.RIGHT );
        createPreisField( vb.jahresReinErtrag(), two().top( lastLine ), client, false );
        site.addFieldListener( reinertragCalculation = new FieldCalculation( site, 2, vb.jahresReinErtrag(), vb
                .summeBewirtschaftskosten(), vb.bruttoRohertragProJahr() ) {

            @Override
            protected Double calculate( ValueProvider values ) {
                Double ertrag = values.get( vb.bruttoRohertragProJahr() );
                Double kosten = values.get( vb.summeBewirtschaftskosten() );
                // FIXME

                Double reinertrag = ertrag;
                if (reinertrag != null && kosten != null) {
                    reinertrag = reinertrag - kosten;
                }

                return reinertrag;
            }
        } );

        enablePauschal( site, vb.pauschalBewirtschaftungskosten().get() );
    }


    private void enablePauschal( IFormEditorPageSite site, Boolean pauschal ) {
        summePauschal = pauschal == null ? false : pauschal.booleanValue();
        site.setFieldEnabled( vb.verwaltungskosten().qualifiedName().name(), !summePauschal );
        site.setFieldEnabled( vb.instandhaltungskosten().qualifiedName().name(), !summePauschal );
        site.setFieldEnabled( vb.mietausfallWagnis().qualifiedName().name(), !summePauschal );
        site.setFieldEnabled( vb.bewirtschaftskostenZeile5().qualifiedName().name(), !summePauschal );
        site.setFieldEnabled( vb.bewirtschaftskostenZeile5Text().qualifiedName().name(), !summePauschal );
        site.setFieldEnabled( vb.summeBewirtschaftskosten().qualifiedName().name(), summePauschal );
    }


    @Override
    protected SimpleFormData one() {
        return new SimpleFormData( SPACING ).left( 0 ).right( 45 );
    }


    @Override
    protected SimpleFormData two() {
        return new SimpleFormData( SPACING ).left( 45 ).right( 60 );
    }

}