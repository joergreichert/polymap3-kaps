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

import java.util.SortedMap;

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.ui.forms.widgets.Section;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.data.entityfeature.AssociationAdapter;
import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.CheckboxFormField;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.form.FormEditor;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.model.data.BodennutzungComposite;
import org.polymap.kaps.model.data.RichtwertzoneZeitraumComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.model.data.VertragsdatenErweitertComposite;
import org.polymap.kaps.ui.FieldCalculation;
import org.polymap.kaps.ui.FieldListener;
import org.polymap.kaps.ui.FieldMultiplication;
import org.polymap.kaps.ui.FieldSummation;
import org.polymap.kaps.ui.MyNumberValidator;
import org.polymap.kaps.ui.NumberFormatter;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class VertragsdatenAgrarBodenwertFormEditorPage
        extends VertragsdatenAgrarFormEditorPage {

    private static final int                          ONE   = 0;

    private static final int                          TWO   = 20;

    private static final int                          THREE = 40;

    private static final int                          FOUR  = 50;

    private static final int                          FIVE  = 64;

    private static final int                          SIX   = 74;

    private static final int                          SEVEN = 90;

    private static final int                          EIGHT = 100;

    private static Log                                log   = LogFactory
                                                                    .getLog( VertragsdatenAgrarBodenwertFormEditorPage.class );

    private IFormFieldListener                        riwezone1;

    private FieldMultiplication                       bodenwert1;

    private FieldMultiplication                       bodenwert6;

    private IFormFieldListener                        riwezone6;

    private FieldMultiplication                       bodenwert5;

    private IFormFieldListener                        riwezone5;

    private FieldMultiplication                       bodenwert4;

    private IFormFieldListener                        riwezone4;

    private FieldMultiplication                       bodenwert3;

    private IFormFieldListener                        riwezone3;

    private FieldMultiplication                       bodenwert2;

    private IFormFieldListener                        riwezone2;

    private FieldSummation                            flaecheSummation;

    private FieldSummation                            wertSummation;

    private FieldListener                             fieldListener;

    private FieldSummation                            sachwertSummation;

    private FieldCalculation                          bereinCalculator;

    private FieldMultiplication                       anteilBau;

    private FieldMultiplication                       anteilBoden;

    private FieldMultiplication                       anteil1;

    private FieldMultiplication                       anteil2;

    private FieldMultiplication                       anteil3;

    private FieldMultiplication                       anteil4;

    private FieldMultiplication                       anteil5;

    private FieldMultiplication                       anteil6;

    final PicklistFormField.ValueProvider zonen;


    // private IFormFieldListener gemeindeListener;

    public VertragsdatenAgrarBodenwertFormEditorPage( final FormEditor formEditor, Feature feature,
            FeatureStore featureStore ) {
        super( VertragsdatenAgrarBodenwertFormEditorPage.class.getName(), "Bodenwertaufteilung", feature,
                featureStore );

        // call only once for all 6 fields
        final SortedMap<String, Object> searchZonen = searchZonen();
        zonen = new PicklistFormField.ValueProvider(){

            @Override
            public SortedMap<String, Object> get() {
               return searchZonen;
            }};

        EventManager.instance().subscribe( fieldListener = new FieldListener( vb.gesamtBauWert() ),
                new FieldListener.EventFilter( formEditor ) );
    }


    @Override
    public void dispose() {
        EventManager.instance().unsubscribe( fieldListener );
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


    protected SimpleFormData six() {
        return new SimpleFormData( SPACING ).left( SIX ).right( SEVEN );
    }


    protected SimpleFormData seven() {
        return new SimpleFormData( SPACING ).left( SEVEN ).right( EIGHT );
    }


    @SuppressWarnings("unchecked")
    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );

        Control newLine, lastLine = null;
        Composite parent = pageSite.getPageBody();

        Section section = newSection( parent, "Bodenwertaufteilung" );
        Composite client = (Composite)section.getClient();

        newLine = createLabel( client, "Richtwertzone", one(), SWT.CENTER );
        createLabel( client, "Bodennutzung", two(), SWT.CENTER );
        createLabel( client, "AZ/GZ", "Acker-/Grünlandzahl", three(), SWT.CENTER );
        createLabel( client, "Flächenanteil", four(), SWT.CENTER );
        createLabel( client, "Bodenpreis", five(), SWT.CENTER );
        createLabel( client, "Bodenwert", six(), SWT.CENTER );
        createLabel( client, "Abgleich auf", seven(), SWT.CENTER );

        lastLine = newLine;
        // newLine = createLabel( client, "Richtwertzone", one().top( lastLine ),
        // SWT.CENTER );
        // createLabel( client, "Bodennutzung", two().top( lastLine ), SWT.CENTER );
        newLine = // createLabel( client, "Grünland", three().top( lastLine ),
                  // SWT.CENTER );
        createLabel( client, "in m²", four().top( lastLine ), SWT.CENTER );
        createLabel( client, "in €/m²", five().top( lastLine ), SWT.CENTER );
        createLabel( client, "in €", six().top( lastLine ), SWT.CENTER );
        createLabel( client, "Kaufpreis in €/m²", "Abgleich auf Kaufpreis in €/m²", seven().top( lastLine ), SWT.CENTER );

        // 1
        lastLine = newLine;
        newLine = newFormField( IFormFieldLabel.NO_LABEL )
                .setProperty( new AssociationAdapter<RichtwertzoneZeitraumComposite>( vb.richtwertZone1() ) )
                .setField( new PicklistFormField( zonen )).setLayoutData( one().top( lastLine ).create() )
                .setParent( client ).create();
        newFormField( IFormFieldLabel.NO_LABEL )
                .setProperty( new AssociationAdapter<BodennutzungComposite>( vb.bodennutzung1() ) )
                .setField( namedAssocationsPicklist( BodennutzungComposite.class ) )
                .setLayoutData( two().top( lastLine ).create() ).setParent( client ).create();

        newFormField( IFormFieldLabel.NO_LABEL ).setProperty( new PropertyAdapter( vb.ackerzahl1() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new MyNumberValidator( Long.class ) ).setLayoutData( three().top( lastLine ).create() )
                .setParent( client ).create();

        createFlaecheField( vb.flaechenAnteil1(), four().top( lastLine ), client, true );
        createPreisField( vb.bodenrichtwert1(), five().top( lastLine ), client, true );
        createPreisField( vb.bodenwert1(), six().top( lastLine ), client, false );
        createPreisField( vb.abgleichAufKaufpreis1(), seven().top( lastLine ), client, false );
        site.addFieldListener( riwezone1 = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == IFormFieldListener.VALUE_CHANGE
                        && ev.getFieldName().equals( vb.richtwertZone1().qualifiedName().name() )) {
                    RichtwertzoneZeitraumComposite zone = (RichtwertzoneZeitraumComposite)ev.getNewValue();
                    Double wert = zone != null ? zone.euroQm().get() : null;
                    site.setFieldValue( vb.bodenrichtwert1().qualifiedName().name(), wert != null ? NumberFormatter
                            .getFormatter( 2 ).format( wert ) : null );
                }
            }
        } );
        site.addFieldListener( bodenwert1 = new FieldMultiplication( site, 2, vb.flaechenAnteil1(), vb
                .bodenrichtwert1(), vb.bodenwert1() ) );

        // 2
        lastLine = newLine;
        newLine = newFormField( IFormFieldLabel.NO_LABEL )
                .setProperty( new AssociationAdapter<RichtwertzoneZeitraumComposite>( vb.richtwertZone2() ) )
                .setField( new PicklistFormField( zonen ) ).setLayoutData( one().top( lastLine ).create() )
                .setParent( client ).create();
        newFormField( IFormFieldLabel.NO_LABEL )
                .setProperty( new AssociationAdapter<BodennutzungComposite>( vb.bodennutzung2() ) )
                .setField( namedAssocationsPicklist( BodennutzungComposite.class ) )
                .setLayoutData( two().top( lastLine ).create() ).setParent( client ).create();

        newFormField( IFormFieldLabel.NO_LABEL ).setProperty( new PropertyAdapter( vb.ackerzahl2() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new MyNumberValidator( Long.class ) ).setLayoutData( three().top( lastLine ).create() )
                .setParent( client ).create();

        createFlaecheField( vb.flaechenAnteil2(), four().top( lastLine ), client, true );
        createPreisField( vb.bodenrichtwert2(), five().top( lastLine ), client, true );
        createPreisField( vb.bodenwert2(), six().top( lastLine ), client, false );
        createPreisField( vb.abgleichAufKaufpreis2(), seven().top( lastLine ), client, false );
        site.addFieldListener( riwezone2 = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == IFormFieldListener.VALUE_CHANGE
                        && ev.getFieldName().equals( vb.richtwertZone2().qualifiedName().name() )) {
                    RichtwertzoneZeitraumComposite zone = (RichtwertzoneZeitraumComposite)ev.getNewValue();
                    Double wert = zone.euroQm().get();
                    site.setFieldValue( vb.bodenrichtwert2().qualifiedName().name(), wert != null ? NumberFormatter
                            .getFormatter( 2 ).format( wert ) : null );
                }
            }
        } );
        site.addFieldListener( bodenwert2 = new FieldMultiplication( site, 2, vb.flaechenAnteil2(), vb
                .bodenrichtwert2(), vb.bodenwert2() ) );

        // 3
        lastLine = newLine;
        newLine = newFormField( IFormFieldLabel.NO_LABEL )
                .setProperty( new AssociationAdapter<RichtwertzoneZeitraumComposite>( vb.richtwertZone3() ) )
                .setField( new PicklistFormField( zonen ) ).setLayoutData( one().top( lastLine ).create() )
                .setParent( client ).create();
        newFormField( IFormFieldLabel.NO_LABEL )
                .setProperty( new AssociationAdapter<BodennutzungComposite>( vb.bodennutzung3() ) )
                .setField( namedAssocationsPicklist( BodennutzungComposite.class ) )
                .setLayoutData( two().top( lastLine ).create() ).setParent( client ).create();

        newFormField( IFormFieldLabel.NO_LABEL ).setProperty( new PropertyAdapter( vb.ackerzahl3() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new MyNumberValidator( Long.class ) ).setLayoutData( three().top( lastLine ).create() )
                .setParent( client ).create();

        createFlaecheField( vb.flaechenAnteil3(), four().top( lastLine ), client, true );
        createPreisField( vb.bodenrichtwert3(), five().top( lastLine ), client, true );
        createPreisField( vb.bodenwert3(), six().top( lastLine ), client, false );
        createPreisField( vb.abgleichAufKaufpreis3(), seven().top( lastLine ), client, false );
        site.addFieldListener( riwezone3 = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == IFormFieldListener.VALUE_CHANGE
                        && ev.getFieldName().equals( vb.richtwertZone3().qualifiedName().name() )) {
                    RichtwertzoneZeitraumComposite zone = (RichtwertzoneZeitraumComposite)ev.getNewValue();
                    Double wert = zone.euroQm().get();
                    site.setFieldValue( vb.bodenrichtwert3().qualifiedName().name(), wert != null ? NumberFormatter
                            .getFormatter( 2 ).format( wert ) : null );
                }
            }
        } );
        site.addFieldListener( bodenwert3 = new FieldMultiplication( site, 2, vb.flaechenAnteil3(), vb
                .bodenrichtwert3(), vb.bodenwert3() ) );

        // 4
        lastLine = newLine;
        newLine = newFormField( IFormFieldLabel.NO_LABEL )
                .setProperty( new AssociationAdapter<RichtwertzoneZeitraumComposite>( vb.richtwertZone4() ) )
                .setField( new PicklistFormField( zonen ) ).setLayoutData( one().top( lastLine ).create() )
                .setParent( client ).create();
        newFormField( IFormFieldLabel.NO_LABEL )
                .setProperty( new AssociationAdapter<BodennutzungComposite>( vb.bodennutzung4() ) )
                .setField( namedAssocationsPicklist( BodennutzungComposite.class ) )
                .setLayoutData( two().top( lastLine ).create() ).setParent( client ).create();

        newFormField( IFormFieldLabel.NO_LABEL ).setProperty( new PropertyAdapter( vb.ackerzahl4() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new MyNumberValidator( Long.class ) ).setLayoutData( three().top( lastLine ).create() )
                .setParent( client ).create();

        createFlaecheField( vb.flaechenAnteil4(), four().top( lastLine ), client, true );
        createPreisField( vb.bodenrichtwert4(), five().top( lastLine ), client, true );
        createPreisField( vb.bodenwert4(), six().top( lastLine ), client, false );
        createPreisField( vb.abgleichAufKaufpreis4(), seven().top( lastLine ), client, false );
        site.addFieldListener( riwezone4 = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == IFormFieldListener.VALUE_CHANGE
                        && ev.getFieldName().equals( vb.richtwertZone4().qualifiedName().name() )) {
                    RichtwertzoneZeitraumComposite zone = (RichtwertzoneZeitraumComposite)ev.getNewValue();
                    Double wert = zone.euroQm().get();
                    site.setFieldValue( vb.bodenrichtwert4().qualifiedName().name(), wert != null ? NumberFormatter
                            .getFormatter( 2 ).format( wert ) : null );
                }
            }
        } );
        site.addFieldListener( bodenwert4 = new FieldMultiplication( site, 2, vb.flaechenAnteil4(), vb
                .bodenrichtwert4(), vb.bodenwert4() ) );
        lastLine = newLine;
        newLine = newFormField( IFormFieldLabel.NO_LABEL )
                .setProperty( new AssociationAdapter<RichtwertzoneZeitraumComposite>( vb.richtwertZone5() ) )
                .setField( new PicklistFormField( zonen ) ).setLayoutData( one().top( lastLine ).create() )
                .setParent( client ).create();
        newFormField( IFormFieldLabel.NO_LABEL )
                .setProperty( new AssociationAdapter<BodennutzungComposite>( vb.bodennutzung5() ) )
                .setField( namedAssocationsPicklist( BodennutzungComposite.class ) )
                .setLayoutData( two().top( lastLine ).create() ).setParent( client ).create();

        newFormField( IFormFieldLabel.NO_LABEL ).setProperty( new PropertyAdapter( vb.ackerzahl5() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new MyNumberValidator( Long.class ) ).setLayoutData( three().top( lastLine ).create() )
                .setParent( client ).create();

        createFlaecheField( vb.flaechenAnteil5(), four().top( lastLine ), client, true );
        createPreisField( vb.bodenrichtwert5(), five().top( lastLine ), client, true );
        createPreisField( vb.bodenwert5(), six().top( lastLine ), client, false );
        createPreisField( vb.abgleichAufKaufpreis5(), seven().top( lastLine ), client, false );
        site.addFieldListener( riwezone5 = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == IFormFieldListener.VALUE_CHANGE
                        && ev.getFieldName().equals( vb.richtwertZone5().qualifiedName().name() )) {
                    RichtwertzoneZeitraumComposite zone = (RichtwertzoneZeitraumComposite)ev.getNewValue();
                    Double wert = zone.euroQm().get();
                    site.setFieldValue( vb.bodenrichtwert5().qualifiedName().name(), wert != null ? NumberFormatter
                            .getFormatter( 2 ).format( wert ) : null );
                }
            }
        } );
        site.addFieldListener( bodenwert5 = new FieldMultiplication( site, 2, vb.flaechenAnteil5(), vb
                .bodenrichtwert5(), vb.bodenwert5() ) );
        lastLine = newLine;
        newLine = newFormField( IFormFieldLabel.NO_LABEL )
                .setProperty( new AssociationAdapter<RichtwertzoneZeitraumComposite>( vb.richtwertZone6() ) )
                .setField( new PicklistFormField( zonen ) ).setLayoutData( one().top( lastLine ).create() )
                .setParent( client ).create();
        newFormField( IFormFieldLabel.NO_LABEL )
                .setProperty( new AssociationAdapter<BodennutzungComposite>( vb.bodennutzung6() ) )
                .setField( namedAssocationsPicklist( BodennutzungComposite.class ) )
                .setLayoutData( two().top( lastLine ).create() ).setParent( client ).create();

        newFormField( IFormFieldLabel.NO_LABEL ).setProperty( new PropertyAdapter( vb.ackerzahl6() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new MyNumberValidator( Long.class ) ).setLayoutData( three().top( lastLine ).create() )
                .setParent( client ).create();

        createFlaecheField( vb.flaechenAnteil6(), four().top( lastLine ), client, true );
        createPreisField( vb.bodenrichtwert6(), five().top( lastLine ), client, true );
        createPreisField( vb.bodenwert6(), six().top( lastLine ), client, false );
        createPreisField( vb.abgleichAufKaufpreis6(), seven().top( lastLine ), client, false );
        site.addFieldListener( riwezone6 = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == IFormFieldListener.VALUE_CHANGE
                        && ev.getFieldName().equals( vb.richtwertZone6().qualifiedName().name() )) {
                    RichtwertzoneZeitraumComposite zone = (RichtwertzoneZeitraumComposite)ev.getNewValue();
                    Double wert = zone.euroQm().get();
                    site.setFieldValue( vb.bodenrichtwert6().qualifiedName().name(), wert != null ? NumberFormatter
                            .getFormatter( 2 ).format( wert ) : null );
                }
            }
        } );
        site.addFieldListener( bodenwert6 = new FieldMultiplication( site, 2, vb.flaechenAnteil6(), vb
                .bodenrichtwert6(), vb.bodenwert6() ) );

        // anteile
        site.addFieldListener( anteil1 = new FieldMultiplication( site, 2, vb.bodenrichtwert1(), vb
                .faktorKaufpreisZuSachwert(), vb.abgleichAufKaufpreis1() ) );
        site.addFieldListener( anteil2 = new FieldMultiplication( site, 2, vb.bodenrichtwert2(), vb
                .faktorKaufpreisZuSachwert(), vb.abgleichAufKaufpreis2() ) );
        site.addFieldListener( anteil3 = new FieldMultiplication( site, 2, vb.bodenrichtwert3(), vb
                .faktorKaufpreisZuSachwert(), vb.abgleichAufKaufpreis3() ) );
        site.addFieldListener( anteil4 = new FieldMultiplication( site, 2, vb.bodenrichtwert4(), vb
                .faktorKaufpreisZuSachwert(), vb.abgleichAufKaufpreis4() ) );
        site.addFieldListener( anteil5 = new FieldMultiplication( site, 2, vb.bodenrichtwert5(), vb
                .faktorKaufpreisZuSachwert(), vb.abgleichAufKaufpreis5() ) );
        site.addFieldListener( anteil6 = new FieldMultiplication( site, 2, vb.bodenrichtwert6(), vb
                .faktorKaufpreisZuSachwert(), vb.abgleichAufKaufpreis6() ) );

        lastLine = newLine;
        //
        // lastLine = newLine;
        newLine = createLabel( client, "Verkaufte Fläche", one().top( lastLine ), SWT.RIGHT );
        newFormField( IFormFieldLabel.NO_LABEL ).setEnabled( false )
                .setProperty( new PropertyAdapter( vb.verkaufteFlaeche() ) )
                .setValidator( new MyNumberValidator( Double.class, 2 ) ).setField( new StringFormField() )
                .setLayoutData( two().top( lastLine ).bottom( 100 ).create() ).setParent( client ).create();
        createFlaecheField( vb.flaechenAnteilGesamt(), four().top( lastLine ), client, false );
        site.addFieldListener( flaecheSummation = new FieldSummation( site, 0, vb.flaechenAnteilGesamt(), vb
                .flaechenAnteil1(), vb.flaechenAnteil2(), vb.flaechenAnteil3(), vb.flaechenAnteil4(), vb
                .flaechenAnteil5(), vb.flaechenAnteil6() ) );

        //
        // --- BEWERTUNG
        //
        section = newSection( section, "Bewertung" );
        client = (Composite)section.getClient();

        lastLine = newLine;
        newLine = createLabel( client, "Bodenwert", five().top( lastLine, 12 ), SWT.RIGHT );
        createPreisField( vb.bodenwertGesamt(), six().top( lastLine ), client, false );
        site.addFieldListener( wertSummation = new FieldSummation( site, 0, vb.bodenwertGesamt(), vb.bodenwert1(), vb
                .bodenwert2(), vb.bodenwert3(), vb.bodenwert4(), vb.bodenwert5(), vb.bodenwert6() ) );

        lastLine = newLine;
        newLine = createLabel( client, "Sachwert", "Sachwert = Bodenwert + Bauwert", five().top( lastLine, 12 ),
                SWT.RIGHT );
        createPreisField( IFormFieldLabel.NO_LABEL, "Sachwert = Bodenwert + Bauwert", vb.sachwertGesamt(),
                six().top( lastLine ), client, false );
        site.addFieldListener( sachwertSummation = new FieldSummation( site, 2, vb.sachwertGesamt(),
                vb.gesamtBauWert(), vb.bodenwertGesamt() ) );

        lastLine = newLine;
        newLine = createLabel( client, "Faktor", five().top( lastLine, 12 ), SWT.RIGHT );
        newFormField( IFormFieldLabel.NO_LABEL ).setToolTipText( "Faktor = bereinigter Kaufpreis/Sachwert" )
                .setProperty( new PropertyAdapter( vb.faktorKaufpreisZuSachwert() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new MyNumberValidator( Double.class, 4 ) )
                .setLayoutData( six().top( lastLine ).create() ).setParent( client ).setEnabled( false ).create();
        site.addFieldListener( bereinCalculator = new FieldCalculation( site, 4, vb.faktorKaufpreisZuSachwert(), vb
                .sachwertGesamt() ) {

            @Override
            protected Double calculate( org.polymap.kaps.ui.FieldCalculation.ValueProvider values ) {
                VertragComposite vertrag = vb.vertrag().get();
                Double kaufpreis = vertrag.vollpreis().get();
                VertragsdatenErweitertComposite vertragsdatenErweitertComposite = vb.vertrag().get()
                        .erweiterteVertragsdaten().get();
                if (vertragsdatenErweitertComposite != null) {
                    Double bereinigt = vertragsdatenErweitertComposite.bereinigterVollpreis().get();
                    if (bereinigt != null) {
                        kaufpreis = bereinigt;
                    }
                }
                Double sachwert = values.get( vb.sachwertGesamt() );
                if (kaufpreis != null && sachwert != null && sachwert != 0.0d) {
                    return kaufpreis / sachwert;
                }
                return null;
            }
        } );

        lastLine = newLine;
        newLine = createLabel( client, "Kaufpreisanteil Bau", four().right( SIX ).top( lastLine, 12 ), SWT.RIGHT );
        createPreisField( IFormFieldLabel.NO_LABEL, "Kaufpreisanteil der baulichen Anlagen = Gesamtbauwert * Faktor",
                vb.kaufpreisAnteilBaulicheAnlagen(), six().top( lastLine ), client, false );
        site.addFieldListener( anteilBau = new FieldMultiplication( site, 2, vb.faktorKaufpreisZuSachwert(), vb
                .gesamtBauWert(), vb.kaufpreisAnteilBaulicheAnlagen() ) );

        lastLine = newLine;
        newLine = createLabel( client, "Kaufpreisanteil Boden", four().right( SIX ).top( lastLine, 12 ), SWT.RIGHT );
        createPreisField( IFormFieldLabel.NO_LABEL, "Kaufpreisanteil Bodenwert = Bodenwert * Faktor",
                vb.kaufpreisAnteilBodenwert(), six().top( lastLine ), client, false );
        site.addFieldListener( anteilBoden = new FieldMultiplication( site, 2, vb.faktorKaufpreisZuSachwert(), vb
                .bodenwertGesamt(), vb.kaufpreisAnteilBodenwert() ) );

        lastLine = newLine;
        newLine = createLabel( client, "für Statistiken geeignet?", one().right( THREE ).top( lastLine, 12 ), SWT.RIGHT );
        newFormField( IFormFieldLabel.NO_LABEL ).setToolTipText( "Für Statisiken geeignet?" )
                .setProperty( new PropertyAdapter( vb.fuerStatistikGeeignet() ) ).setField( new CheckboxFormField() )
                .setLayoutData( three().top( lastLine ).bottom( 100 ).create() ).setParent( client ).create();

        createLabel( client, "für Richtwertermittlung geeignet?", four().right( SIX ).top( lastLine, 12 ), SWT.RIGHT );
        newFormField( IFormFieldLabel.NO_LABEL ).setToolTipText( "Für Richtwertermittlung geeignet?" )
                .setField( new CheckboxFormField() )
                .setProperty( new PropertyAdapter( vb.zurRichtwertermittlungGeeignet() ) )
                .setLayoutData( six().top( lastLine ).bottom( 100 ).create() ).setParent( client ).create();
    }


    private SortedMap<String, Object> searchZonen() {
        return RichtwertzoneProvider.findFor(vb.vertrag().get().richtwertZoneAgrar().get().gemeinde().get(), vb.vertrag().get().vertragsDatum().get());
    }


    @Override
    public void afterDoLoad( IProgressMonitor monitor )
            throws Exception {
        super.afterDoLoad( monitor );

        if (vb.richtwertZone1().get() == null) {
            // set the default zone from flurstueck
            RichtwertzoneZeitraumComposite zone = RichtwertzoneZeitraumComposite.Mixin.findZeitraumFor( vb.vertrag()
                    .get().richtwertZoneAgrar().get(), vb.vertrag().get().vertragsDatum().get() );
            // vb.richtwertZone1().set( zone );
            pageSite.setFieldValue( vb.richtwertZone1().qualifiedName().name(), zone );
        }
        fieldListener.flush( pageSite );
    }
}
