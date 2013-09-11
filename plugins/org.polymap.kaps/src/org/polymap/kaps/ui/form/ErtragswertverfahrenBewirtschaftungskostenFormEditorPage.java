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

import java.util.List;

import java.beans.PropertyChangeEvent;

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
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.model.data.ErtragswertverfahrenComposite;
import org.polymap.kaps.ui.FieldCalculation;
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

    private Double              anteiligeBetriebskosten;

    private Double bruttoRohertragProJahr;


    // private IFormFieldListener gemeindeListener;

    public ErtragswertverfahrenBewirtschaftungskostenFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( ErtragswertverfahrenBewirtschaftungskostenFormEditorPage.class.getName(), "Bewirtschaftungskosten",
                feature, featureStore );

        anteiligeBetriebskosten = vb.anteiligeBetriebskosten().get();
        bruttoRohertragProJahr = vb.bruttoRohertragProJahr().get();

        EventManager.instance().subscribe( this, new EventFilter<PropertyChangeEvent>() {

            public boolean apply( PropertyChangeEvent ev ) {
                Object source = ev.getSource();
                return source != null && source instanceof ErtragswertverfahrenComposite && source.equals( vb );
            }
        } );
    }


    @EventHandler(display = true, delay = 1)
    public void handleExternalGebaeudeSelection( List<PropertyChangeEvent> events )
            throws Exception {
        for (PropertyChangeEvent ev : events) {
            if (ev.getPropertyName().equals( vb.jahresBetriebskostenE().qualifiedName().name() )) {
                if (initialized) {
                    pageSite.setFieldValue( vb.anteiligeBetriebskosten().qualifiedName().name(),
                            ev.getNewValue() != null ? getFormatter( 2 ).format( ev.getNewValue() ) : null );
                }
                else {
                    anteiligeBetriebskosten = (Double)ev.getNewValue();
                }
                System.out.println( ev );
            } else if (ev.getPropertyName().equals( vb.bruttoRohertragProJahr().qualifiedName().name() )) {
                bruttoRohertragProJahr = (Double)ev.getNewValue();
                if (reinertragCalculation != null) {
                    reinertragCalculation.refreshResult();
                    // verboten, führt zu endlosschleife mit listener in Erträge
                    //pageSite.fireEvent( this, vb.bruttoRohertragProJahr().qualifiedName().name(), IFormFieldListener.VALUE_CHANGE, (Double)ev.getNewValue() );
                }
                System.out.println( ev );
            }
        }
    }


    @Override
    public void afterDoLoad( IProgressMonitor monitor )
            throws Exception {
        pageSite.setFieldValue( vb.anteiligeBetriebskosten().qualifiedName().name(),
                anteiligeBetriebskosten != null ? getFormatter( 2 ).format( anteiligeBetriebskosten ) : null );
        reinertragCalculation.refreshResult();
    }


    @Override
    public void dispose() {
        super.dispose();
        EventManager.instance().unsubscribe( this );
        // EventManager.instance().unsubscribe( fieldListener );
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
                vb.bewirtschaftskostenZeile5() ) );

        lastLine = newLine;
        newLine = createLabel( client, "jährlicher Reinertrag in €",
                "jährlicher Rohertrag (brutto) - jährliche Bewirtschaftungskosten", one().top( lastLine ), SWT.RIGHT );
        createPreisField( vb.jahresReinErtrag(), two().top( lastLine ), client, false );
        site.addFieldListener( reinertragCalculation = new FieldCalculation( site, 2, vb.jahresReinErtrag(), vb
                .summeBewirtschaftskosten() ) {

            @Override
            protected Double calculate( ValueProvider values ) {
                Double ertrag = bruttoRohertragProJahr;
                Double kosten = values.get( vb.summeBewirtschaftskosten() );
                // FIXME

                Double reinertrag = ertrag;
                if (reinertrag != null && kosten != null) {
                    reinertrag = reinertrag - kosten;
                }
                // Reiter 4 informieren
                EventManager.instance().publish(
                        new PropertyChangeEvent( vb, vb.jahresReinErtrag().qualifiedName().name(), vb
                                .jahresReinErtrag().get(), reinertrag ) );

                return reinertrag;
            }
        } );

        enablePauschal( site, vb.pauschalBewirtschaftungskosten().get() );
    }


    private void enablePauschal( IFormEditorPageSite site, Boolean pauschal ) {
        boolean b = pauschal == null ? false : pauschal.booleanValue();
        site.setFieldEnabled( vb.verwaltungskosten().qualifiedName().name(), !b );
        site.setFieldEnabled( vb.instandhaltungskosten().qualifiedName().name(), !b );
        site.setFieldEnabled( vb.mietausfallWagnis().qualifiedName().name(), !b );
        site.setFieldEnabled( vb.bewirtschaftskostenZeile5().qualifiedName().name(), !b );
        site.setFieldEnabled( vb.bewirtschaftskostenZeile5Text().qualifiedName().name(), !b );
        site.setFieldEnabled( vb.summeBewirtschaftskosten().qualifiedName().name(), b );
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
