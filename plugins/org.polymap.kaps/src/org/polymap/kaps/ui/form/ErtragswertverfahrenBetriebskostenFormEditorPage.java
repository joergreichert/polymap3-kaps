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

import org.eclipse.jface.action.Action;

import org.polymap.core.project.ui.util.SimpleFormData;

import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.KapsPlugin;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.ui.ActionButton;
import org.polymap.kaps.ui.FieldCalculation;
import org.polymap.kaps.ui.FieldSummation;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class ErtragswertverfahrenBetriebskostenFormEditorPage
        extends ErtragswertverfahrenFormEditorPage {

    private static Log         log = LogFactory.getLog( ErtragswertverfahrenBetriebskostenFormEditorPage.class );

    @SuppressWarnings("unused")
    private IFormFieldListener line1multiplicator;

    @SuppressWarnings("unused")
    private IFormFieldListener line2multiplicator;

    @SuppressWarnings("unused")
    private IFormFieldListener line3multiplicator;

    @SuppressWarnings("unused")
    private IFormFieldListener line4multiplicator;

    @SuppressWarnings("unused")
    private IFormFieldListener line5multiplicator;

    @SuppressWarnings("unused")
    private IFormFieldListener line6multiplicator;

    private FieldCalculation   betriebsKostenJahrSummation;

    private FieldCalculation   betriebskostenSummeMonatlichCalculation;

    private IFormFieldListener pauschalListener;

    private boolean            summePauschal;

    private boolean            summeProzent;


    public ErtragswertverfahrenBetriebskostenFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( ErtragswertverfahrenBetriebskostenFormEditorPage.class.getName(), "Betriebskosten", feature,
                featureStore );
    }


    @SuppressWarnings("unchecked")
    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );

        Control newLine, lastLine = null;
        Composite parent = pageSite.getPageBody();

        final VertragComposite kaufvertrag = vb.vertrag().get();
        String nummer = EingangsNummerFormatter.format( kaufvertrag.eingangsNr().get() );
        String label = kaufvertrag == null ? "Kein Vertrag zugewiesen" : "Vertrag " + nummer + " öffnen";
        ActionButton openVertrag = new ActionButton( parent, new Action( label ) {

            @Override
            public void run() {
                KapsPlugin.openEditor( fs, VertragComposite.NAME, kaufvertrag );
            }
        } );
        openVertrag.setLayoutData( left().height( 25 ).create() );
        openVertrag.setEnabled( kaufvertrag != null );

        // Section section = newSection( parent, "Bodenwertaufteilung" );
        // Composite client = (Composite)section.getClient();
        Composite client = parent;

        lastLine = openVertrag;
        newLine = createLabel( client, "Betriebskosten pauschal?", one().top( lastLine, 20 ), SWT.RIGHT );
        createBooleanField( vb.pauschalBetriebskosten(), two().top( lastLine, 20 ), client );
        site.addFieldListener( pauschalListener = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == IFormFieldListener.VALUE_CHANGE) {
                    if (ev.getFieldName().equals( vb.pauschalBetriebskosten().qualifiedName().name() )) {
                        Boolean value = (Boolean)ev.getNewValue();
                        enablePauschal( site, value );
                    }
                    else if (ev.getFieldName().equals(
                            vb.betriebskostenInProzentDesJahresRohertragsErfassen().qualifiedName().name() )) {
                        Boolean value = (Boolean)ev.getNewValue();
                        enablePauschalInProzent( site, value );
                    }
                }
            }
        } );
        createPreisField( vb.jahresBetriebskostenPauschal(), three().top( lastLine, 20 ), client, false );

        lastLine = newLine;
        newLine = createLabel( client, "Betriebskosten in % vom Rohertrag?", one().top( lastLine ),
                SWT.RIGHT );
        createBooleanField( vb.betriebskostenInProzentDesJahresRohertragsErfassen(), two().top( lastLine ), client );
        createFlaecheField( vb.betriebskostenInProzentDesJahresRohertrags(), three().top( lastLine ), client, false );

        lastLine = newLine;
        newLine = createLabel( client, "Grundsteuer", one().top( lastLine, 20 ), SWT.RIGHT );
        createPreisField( vb.betriebskostenZeile1(), three().top( lastLine, 20 ), client, true );

        lastLine = newLine;
        newLine = createLabel( client, "Gebäudebrand und Elementarschadenversicherung", one().top( lastLine ),
                SWT.RIGHT );
        createPreisField( vb.betriebskostenZeile2(), three().top( lastLine ), client, true );

        lastLine = newLine;
        newLine = createLabel( client, "Sach-/Haftpflichtversicherung", one().top( lastLine ), SWT.RIGHT );
        createPreisField( vb.betriebskostenZeile3(), three().top( lastLine ), client, true );

        lastLine = newLine;
        newLine = createLabel( client, "Schornsteinreinigung/Emmissionsmessung", one().top( lastLine ), SWT.RIGHT );
        createPreisField( vb.betriebskostenZeile4(), three().top( lastLine ), client, true );

        lastLine = newLine;
        newLine = createLabel( client, "Betrieb der Aufzugsanlage", one().top( lastLine ), SWT.RIGHT );
        createPreisField( vb.betriebskostenZeile5(), three().top( lastLine ), client, true );

        lastLine = newLine;
        newLine = createLabel( client, "Beleuchtung (Treppenhaus und gem. Räume)", one().top( lastLine ), SWT.RIGHT );
        createPreisField( vb.betriebskostenZeile6(), three().top( lastLine ), client, true );

        lastLine = newLine;
        newLine = createLabel( client, "Betrieb der Heizungsanlage", one().top( lastLine ), SWT.RIGHT );
        createPreisField( vb.betriebskostenZeile7(), three().top( lastLine ), client, true );

        lastLine = newLine;
        newLine = createLabel( client, "Hausmeister/Gartenpflege", one().top( lastLine ), SWT.RIGHT );
        createPreisField( vb.betriebskostenZeile8(), three().top( lastLine ), client, true );

        lastLine = newLine;
        newLine = createTextField( vb.betriebskostenTextZeile9(), one().top( lastLine ), client );
        createPreisField( vb.betriebskostenZeile9(), three().top( lastLine ), client, true );

        lastLine = newLine;
        newLine = createTextField( vb.betriebskostenTextZeile10(), one().top( lastLine ), client );
        createPreisField( vb.betriebskostenZeile10(), three().top( lastLine ), client, true );

        lastLine = newLine;
        newLine = createTextField( vb.betriebskostenTextZeile11(), one().top( lastLine ), client );
        createPreisField( vb.betriebskostenZeile11(), three().top( lastLine ), client, true );

        lastLine = newLine;
        newLine = createLabel( client, "jährliche anteilige Betriebskosten in €", one().top( lastLine, 30 ), SWT.RIGHT );
        createPreisField( vb.jahresBetriebskosten(), three().top( lastLine, 30 ), client, false );
        site.addFieldListener( betriebsKostenJahrSummation = new FieldCalculation( site, 2, vb.jahresBetriebskosten(),
                vb.betriebskostenZeile1(), vb.betriebskostenZeile2(), vb.betriebskostenZeile3(), vb
                        .betriebskostenZeile4(), vb.betriebskostenZeile5(), vb.betriebskostenZeile6(), vb
                        .betriebskostenZeile7(), vb.betriebskostenZeile8(), vb.betriebskostenZeile9(), vb
                        .betriebskostenZeile10(), vb.betriebskostenZeile11(), vb
                        .betriebskostenInProzentDesJahresRohertrags(), vb.jahresBetriebskostenPauschal(), vb
                        .nettoRohertragProJahr() ) {

            @Override
            protected Double calculate( ValueProvider values ) {
                if (summePauschal) {
                    return values.get( vb.jahresBetriebskostenPauschal() );
                }
                if (summeProzent) {
                    Double result = values.get( vb.nettoRohertragProJahr() );
                    Double prozent = values.get( vb.betriebskostenInProzentDesJahresRohertrags() );
                    if (result != null && prozent != null) {
                        return result / 100 * prozent;
                    }
                    return null;
                }
                // summe über alles
                Double result = new Double( 0.0d );
                if (values.get( vb.betriebskostenZeile1() ) != null) {
                    result += values.get( vb.betriebskostenZeile1() );
                }
                if (values.get( vb.betriebskostenZeile2() ) != null) {
                    result += values.get( vb.betriebskostenZeile2() );
                }
                if (values.get( vb.betriebskostenZeile3() ) != null) {
                    result += values.get( vb.betriebskostenZeile3() );
                }
                if (values.get( vb.betriebskostenZeile4() ) != null) {
                    result += values.get( vb.betriebskostenZeile4() );
                }
                if (values.get( vb.betriebskostenZeile5() ) != null) {
                    result += values.get( vb.betriebskostenZeile5() );
                }
                if (values.get( vb.betriebskostenZeile6() ) != null) {
                    result += values.get( vb.betriebskostenZeile6() );
                }
                if (values.get( vb.betriebskostenZeile7() ) != null) {
                    result += values.get( vb.betriebskostenZeile7() );
                }
                if (values.get( vb.betriebskostenZeile8() ) != null) {
                    result += values.get( vb.betriebskostenZeile8() );
                }
                if (values.get( vb.betriebskostenZeile9() ) != null) {
                    result += values.get( vb.betriebskostenZeile9() );
                }
                if (values.get( vb.betriebskostenZeile10() ) != null) {
                    result += values.get( vb.betriebskostenZeile10() );
                }
                if (values.get( vb.betriebskostenZeile11() ) != null) {
                    result += values.get( vb.betriebskostenZeile11() );
                }
                return result;
            }
        } );

        lastLine = newLine;
        newLine = createLabel( client, "monatliche anteilige Betriebskosten in € in €", one().top( lastLine ),
                SWT.RIGHT );
        createPreisField( vb.betriebskostenSummeMonatlich(), three().top( lastLine ), client, false );
        site.addFieldListener( betriebskostenSummeMonatlichCalculation = new FieldCalculation( site, 2, vb
                .betriebskostenSummeMonatlich(), vb.jahresBetriebskosten() ) {

            @Override
            protected Double calculate( ValueProvider values ) {
                Double v = values.get( vb.jahresBetriebskosten() );
                return v != null ? v / 12 : null;
            }
        } );

        enablePauschal( site, vb.pauschalBetriebskosten().get() );
        enablePauschalInProzent( site, vb.betriebskostenInProzentDesJahresRohertragsErfassen().get() );
    }


    private void enablePauschal( IFormEditorPageSite site, Boolean pauschal ) {
        summePauschal = pauschal == null ? false : pauschal.booleanValue();
        if (summeProzent && summePauschal) {
            // disable summeProzent
            site.setFieldValue( vb.betriebskostenInProzentDesJahresRohertragsErfassen().qualifiedName().name(),
                    Boolean.FALSE );
            enablePauschalInProzent( site, Boolean.FALSE);
        }
        site.setFieldEnabled( vb.jahresBetriebskostenPauschal().qualifiedName().name(), summePauschal );
        enableDetails( site, !summePauschal && !summeProzent );
        betriebsKostenJahrSummation.refreshResult();
    }


    private void enablePauschalInProzent( IFormEditorPageSite site, Boolean prozent ) {
        summeProzent = prozent == null ? false : prozent.booleanValue();
        if (summeProzent && summePauschal) {
            // disable summePauschal
            site.setFieldValue( vb.pauschalBetriebskosten().qualifiedName().name(), Boolean.FALSE );
            enablePauschal(site, Boolean.FALSE);
        }
        site.setFieldEnabled( vb.betriebskostenInProzentDesJahresRohertrags().qualifiedName().name(), summeProzent );
        enableDetails( site, !summePauschal && !summeProzent );
        betriebsKostenJahrSummation.refreshResult();
    }


    private void enableDetails( IFormEditorPageSite site, Boolean enable ) {
        site.setFieldEnabled( vb.betriebskostenZeile1().qualifiedName().name(), enable );
        site.setFieldEnabled( vb.betriebskostenZeile2().qualifiedName().name(), enable );
        site.setFieldEnabled( vb.betriebskostenZeile3().qualifiedName().name(), enable );
        site.setFieldEnabled( vb.betriebskostenZeile4().qualifiedName().name(), enable );
        site.setFieldEnabled( vb.betriebskostenZeile5().qualifiedName().name(), enable );
        site.setFieldEnabled( vb.betriebskostenZeile6().qualifiedName().name(), enable );
        site.setFieldEnabled( vb.betriebskostenZeile7().qualifiedName().name(), enable );
        site.setFieldEnabled( vb.betriebskostenZeile8().qualifiedName().name(), enable );
        site.setFieldEnabled( vb.betriebskostenZeile9().qualifiedName().name(), enable );
        site.setFieldEnabled( vb.betriebskostenZeile10().qualifiedName().name(), enable );
        site.setFieldEnabled( vb.betriebskostenZeile11().qualifiedName().name(), enable );
        site.setFieldEnabled( vb.betriebskostenTextZeile9().qualifiedName().name(), enable );
        site.setFieldEnabled( vb.betriebskostenTextZeile10().qualifiedName().name(), enable );
        site.setFieldEnabled( vb.betriebskostenTextZeile11().qualifiedName().name(), enable );
        // site.setFieldEnabled( vb.jahresBetriebskosten().qualifiedName().name(),
        // summePauschal );
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
