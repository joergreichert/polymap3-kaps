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

import org.qi4j.api.entity.association.Association;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.data.entityfeature.AssociationAdapter;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.form.IFormEditorPage2;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.model.data.ErtragswertverfahrenComposite;
import org.polymap.kaps.model.data.EtageComposite;
import org.polymap.kaps.ui.FieldCalculation;
import org.polymap.kaps.ui.FieldMultiplication;
import org.polymap.kaps.ui.FieldSummation;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class ErtragswertverfahrenErtraegeFormEditorPage
        extends ErtragswertverfahrenFormEditorPage
        implements IFormEditorPage2 {

    private static Log          log = LogFactory.getLog( ErtragswertverfahrenErtraegeFormEditorPage.class );

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

    private FieldSummation      rohertragMonatSummation;

    private FieldCalculation    rohertragJahrCalculation;

    private FieldSummation      rohertragBruttoJahrCalculation;

    private FieldCalculation    rohertragMonatCalculation;

    private Double jahresBetriebskosten;


    // private FieldListener fieldListener;

    // private IFormFieldListener gemeindeListener;

    public ErtragswertverfahrenErtraegeFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( ErtragswertverfahrenErtraegeFormEditorPage.class.getName(), "Erträge", feature, featureStore );

        jahresBetriebskosten = vb.jahresBetriebskosten().get();
        
        EventManager.instance().subscribe( this, new EventFilter<PropertyChangeEvent>() {

            public boolean apply( PropertyChangeEvent ev ) {
                Object source = ev.getSource();
                return source != null && source instanceof ErtragswertverfahrenComposite && source.equals( vb );
            }
        } );
        // EventManager.instance().subscribe( fieldListener = new FieldListener(
        // vb.jahresBetriebskosten() ),
        // new EventFilter<FormFieldEvent>() {
        //
        // @Override
        // public boolean apply( FormFieldEvent input ) {
        // Object source = input.getSource();
        // if (input.getEventCode() == IFormFieldListener.VALUE_CHANGE) {
        // if (source != null && source instanceof ErtragswertverfahrenComposite &&
        // source.equals( vb )) {
        // return true;
        // }
        // }
        // return false;
        // }
        // }
        //
        // );
    }


    @Override
    public void afterDoLoad( IProgressMonitor monitor )
            throws Exception {
            pageSite.setFieldValue( vb.jahresBetriebskostenE().qualifiedName().name(), jahresBetriebskosten != null ? getFormatter( 2 )
                    .format( jahresBetriebskosten ) : null  );
    }


    @EventHandler(display = true, delay = 1)
    public void handleExternalGebaeudeSelection( List<PropertyChangeEvent> events )
            throws Exception {
        for (PropertyChangeEvent ev : events) {
            if (ev.getPropertyName().equals( vb.jahresBetriebskostenE().qualifiedName().name() )) {
                if (initialized) {
                pageSite.setFieldValue( ev.getPropertyName(),
                        ev.getNewValue() != null ? getFormatter( 2 ).format( ev.getNewValue() ) : null );
                } else {
                  jahresBetriebskosten = (Double)ev.getNewValue();  
                }
                System.out.println( ev );
            }
        }
    }


    @Override
    public void dispose() {
        super.dispose();
        EventManager.instance().unsubscribe( this );
        // EventManager.instance().unsubscribe( fieldListener );
    }

    private boolean initialized = false;
    
    @SuppressWarnings("unchecked")
    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );

        Control newLine, lastLine = null;
        Composite parent = pageSite.getPageBody();

        // Section section = newSection( parent, "Bodenwertaufteilung" );
        // Composite client = (Composite)section.getClient();
        Composite client = parent;

        newLine = createLabel( client, "Etage", one().top( null ), SWT.CENTER );
        createLabel( client, "Fläche in m²", two().top( null ), SWT.CENTER );
        createLabel( client, "Wohnfläche?", three().top( null ), SWT.CENTER );
        createLabel( client, "Nettomiete/Monat in €/m²", four().top( null ), SWT.CENTER );
        createLabel( client, "Gesamtmiete/Monat in €", five().top( null ), SWT.CENTER );

        lastLine = newLine;
        newLine = createEtageField( vb.etageZeile1(), one().top( lastLine ), client );
        createFlaecheField( vb.flaecheZeile1(), two().top( lastLine ), client, true );
        createBooleanField( vb.wohnflaecheZeile1(), three().top( lastLine ), client );
        createPreisField( vb.mieteQmZeile1(), four().top( lastLine ), client, true );
        createPreisField( vb.miete1(), five().top( lastLine ), client, false );
        site.addFieldListener( line1multiplicator = new FieldMultiplication( site, 2, vb.flaecheZeile1(), vb
                .mieteQmZeile1(), vb.miete1() ) );

        lastLine = newLine;
        newLine = createEtageField( vb.etageZeile2(), one().top( lastLine ), client );
        createFlaecheField( vb.flaecheZeile2(), two().top( lastLine ), client, true );
        createBooleanField( vb.wohnflaecheZeile2(), three().top( lastLine ), client );
        createPreisField( vb.mieteQmZeile2(), four().top( lastLine ), client, true );
        createPreisField( vb.miete2(), five().top( lastLine ), client, false );
        site.addFieldListener( line2multiplicator = new FieldMultiplication( site, 2, vb.flaecheZeile2(), vb
                .mieteQmZeile2(), vb.miete2() ) );

        lastLine = newLine;
        newLine = createEtageField( vb.etageZeile3(), one().top( lastLine ), client );
        createFlaecheField( vb.flaecheZeile3(), two().top( lastLine ), client, true );
        createBooleanField( vb.wohnflaecheZeile3(), three().top( lastLine ), client );
        createPreisField( vb.mieteQmZeile3(), four().top( lastLine ), client, true );
        createPreisField( vb.miete3(), five().top( lastLine ), client, false );
        site.addFieldListener( line3multiplicator = new FieldMultiplication( site, 2, vb.flaecheZeile3(), vb
                .mieteQmZeile3(), vb.miete3() ) );

        lastLine = newLine;
        newLine = createEtageField( vb.etageZeile4(), one().top( lastLine ), client );
        createFlaecheField( vb.flaecheZeile4(), two().top( lastLine ), client, true );
        createBooleanField( vb.wohnflaecheZeile4(), three().top( lastLine ), client );
        createPreisField( vb.mieteQmZeile4(), four().top( lastLine ), client, true );
        createPreisField( vb.miete4(), five().top( lastLine ), client, false );
        site.addFieldListener( line4multiplicator = new FieldMultiplication( site, 2, vb.flaecheZeile4(), vb
                .mieteQmZeile4(), vb.miete4() ) );

        lastLine = newLine;
        newLine = createEtageField( vb.etageZeile5(), one().top( lastLine ), client );
        createFlaecheField( vb.flaecheZeile5(), two().top( lastLine ), client, true );
        createBooleanField( vb.wohnflaecheZeile5(), three().top( lastLine ), client );
        createPreisField( vb.mieteQmZeile5(), four().top( lastLine ), client, true );
        createPreisField( vb.miete5(), five().top( lastLine ), client, false );
        site.addFieldListener( line5multiplicator = new FieldMultiplication( site, 2, vb.flaecheZeile5(), vb
                .mieteQmZeile5(), vb.miete5() ) );

        lastLine = newLine;
        newLine = createEtageField( vb.etageZeile6(), one().top( lastLine ), client );
        createFlaecheField( vb.flaecheZeile6(), two().top( lastLine ), client, true );
        createBooleanField( vb.wohnflaecheZeile6(), three().top( lastLine ), client );
        createPreisField( vb.mieteQmZeile6(), four().top( lastLine ), client, true );
        createPreisField( vb.miete6(), five().top( lastLine ), client, false );
        site.addFieldListener( line6multiplicator = new FieldMultiplication( site, 2, vb.flaecheZeile6(), vb
                .mieteQmZeile6(), vb.miete6() ) );

        lastLine = newLine;
        newLine = createEtageField( vb.etageZeile7(), one().top( lastLine ), client );
        createFlaecheField( vb.flaecheZeile7(), two().top( lastLine ), client, true );
        createBooleanField( vb.wohnflaecheZeile7(), three().top( lastLine ), client );
        createPreisField( vb.mieteQmZeile7(), four().top( lastLine ), client, true );
        createPreisField( vb.miete7(), five().top( lastLine ), client, false );
        site.addFieldListener( line7multiplicator = new FieldMultiplication( site, 2, vb.flaecheZeile7(), vb
                .mieteQmZeile7(), vb.miete7() ) );

        lastLine = newLine;
        newLine = createTextField( vb.ertraegeZeile8(), one().top( lastLine ), client );
        createPreisField( vb.miete8(), five().top( lastLine ), client, true );

        lastLine = newLine;
        newLine = createTextField( vb.ertraegeZeile9(), one().top( lastLine ), client );
        createPreisField( vb.miete9(), five().top( lastLine ), client, true );

        lastLine = newLine;
        newLine = createTextField( vb.ertraegeZeile10(), one().top( lastLine ), client );
        createPreisField( vb.miete10(), five().top( lastLine ), client, true );

        lastLine = newLine;
        newLine = createLabel( client, "monatlicher Rohertrag (netto) in €", one().right( 83 ).top( lastLine, 30 ),
                SWT.RIGHT );
        createPreisField( vb.nettoRohertragProMonat(), five().top( lastLine, 30 ), client, false );
        site.addFieldListener( rohertragMonatSummation = new FieldSummation( site, 2, vb.nettoRohertragProMonat(), vb
                .miete1(), vb.miete2(), vb.miete3(), vb.miete4(), vb.miete5(), vb.miete6(), vb.miete7(), vb.miete8(),
                vb.miete9(), vb.miete10() ) );

        lastLine = newLine;
        newLine = createLabel( client, "jährlicher Rohertrag (netto) in €", one().right( 83 ).top( lastLine ),
                SWT.RIGHT );
        createPreisField( vb.nettoRohertragProJahr(), five().top( lastLine ), client, false );
        site.addFieldListener( rohertragJahrCalculation = new FieldCalculation( site, 2, vb.nettoRohertragProJahr(), vb
                .nettoRohertragProMonat() ) {

            @Override
            protected Double calculate( ValueProvider values ) {
                Double v = values.get( vb.nettoRohertragProMonat() );
                return v != null ? v * 12 : null;
            }
        } );

        lastLine = newLine;
        newLine = createLabel( client, "jährliche Betriebskosten in €", one().right( 83 ).top( lastLine ), SWT.RIGHT );
        createPreisField( vb.jahresBetriebskostenE(), five().top( lastLine ), client, false );

        lastLine = newLine;
        newLine = createLabel( client, "jährlicher Rohertrag (brutto) in €", one().right( 83 ).top( lastLine ),
                SWT.RIGHT );
        createPreisField( vb.bruttoRohertragProJahr(), five().top( lastLine ), client, false );
        site.addFieldListener( rohertragBruttoJahrCalculation = new FieldSummation( site, 2, vb
                .bruttoRohertragProJahr(), vb.nettoRohertragProJahr(), vb.jahresBetriebskostenE() ) );

        lastLine = newLine;
        newLine = createLabel( client, "monatlicher Rohertrag (brutto) in €", one().right( 83 ).top( lastLine ),
                SWT.RIGHT );
        createPreisField( vb.bruttoRohertragProMonat(), five().top( lastLine ), client, false );
        site.addFieldListener( rohertragMonatCalculation = new FieldCalculation( site, 2, vb.bruttoRohertragProMonat(),
                vb.bruttoRohertragProJahr() ) {

            @Override
            protected Double calculate( ValueProvider values ) {
                Double v = values.get( vb.bruttoRohertragProJahr() );
                return v != null ? v / 12 : null;
            }
        } );
        
        initialized = true;
    }


    private Composite createEtageField( Association<EtageComposite> property, SimpleFormData data, Composite client ) {
        return newFormField( IFormFieldLabel.NO_LABEL ).setEnabled( true )
                .setProperty( new AssociationAdapter<EtageComposite>( property ) )
                .setField( namedAssocationsPicklist( EtageComposite.class ) ).setLayoutData( data.create() )
                .setParent( client ).create();
    }


    @Override
    protected SimpleFormData one() {
        return new SimpleFormData( SPACING ).left( 0 ).right( 45 );
    }


    @Override
    protected SimpleFormData two() {
        return new SimpleFormData( SPACING ).left( 45 ).right( 60 );
    }


    @Override
    protected SimpleFormData three() {
        return new SimpleFormData( SPACING ).left( 60 ).right( 68 );
    }


    @Override
    protected SimpleFormData four() {
        return new SimpleFormData( SPACING ).left( 68 ).right( 83 );
    }


    @Override
    protected SimpleFormData five() {
        return new SimpleFormData( SPACING ).left( 83 ).right( 100 );
    }
}
