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

import org.eclipse.ui.forms.widgets.Section;

import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.data.entityfeature.AssociationAdapter;
import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.NumberValidator;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.model.data.BodenwertAufteilungTextComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.model.data.VertragsdatenErweitertComposite;
import org.polymap.kaps.ui.FieldCalculation;
import org.polymap.kaps.ui.FieldMultiplication;
import org.polymap.kaps.ui.FieldSummation;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class FlurstuecksdatenBaulandBodenwertFormEditorPage
        extends FlurstuecksdatenBaulandFormEditorPage {


    private static Log                        log   = LogFactory
                                                            .getLog( FlurstuecksdatenBaulandBodenwertFormEditorPage.class );

    @SuppressWarnings("unused")
    private IFormFieldListener                line1multiplicator;

    @SuppressWarnings("unused")
    private IFormFieldListener                line2multiplicator;

    @SuppressWarnings("unused")
    private IFormFieldListener                line3multiplicator;

    @SuppressWarnings("unused")
    private IFormFieldListener                line4multiplicator;

    @SuppressWarnings("unused")
    private IFormFieldListener                line5multiplicator;

    @SuppressWarnings("unused")
    private IFormFieldListener                line6multiplicator;

    @SuppressWarnings("unused")
    private IFormFieldListener                flaecheSummation;

    @SuppressWarnings("unused")
    private IFormFieldListener                bodenwertSummation;

    @SuppressWarnings("unused")
    private IFormFieldListener                bereinCalculator;

    @SuppressWarnings("unused")
    private IFormFieldListener                sachwertSummation;

    @SuppressWarnings("unused")
    private IFormFieldListener                anteilBodenwert;

    private FaktorOhneStrassenplatzCalculator faktorStrassenplatz;

    private FieldMultiplication               line1multiplicator2;

    private FieldMultiplication               line2multiplicator2;

    private FieldMultiplication               line3multiplicator2;

    private FieldMultiplication               line4multiplicator2;

    private FieldMultiplication               line5multiplicator2;

    private FieldMultiplication               line6multiplicator2;

    private IFormFieldListener                bodenpreis;

    protected Double                          bodenpreisBebaut;

    private IFormFieldListener                bodenpreisListener;


    // private IFormFieldListener gemeindeListener;

    public FlurstuecksdatenBaulandBodenwertFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( FlurstuecksdatenBaulandBodenwertFormEditorPage.class.getName(), "Boden- und Gebäudewert", feature,
                featureStore );

        EventManager.instance().subscribe( bodenpreisListener = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                bodenpreisBebaut = (Double)ev.getNewValue();
            }
        }, new EventFilter<FormFieldEvent>() {

            public boolean apply( FormFieldEvent ev ) {
                return ev.getEventCode() == IFormFieldListener.VALUE_CHANGE
                        && ev.getFieldName().equals( vb.bodenpreisBebaut().qualifiedName().name() );
            }
        } );
    }


    @SuppressWarnings("unchecked")
    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );

        Control newLine, lastLine = null;
        Composite parent = pageSite.getPageBody();

        Section section = newSection( parent, "Bodenwertaufteilung" );
        Composite client = (Composite)section.getClient();

        newLine = createLabel( client, "Fläche in m²", two().top( null ), SWT.CENTER );
        createLabel( client, "Bodenpreis in €/m²", three().top( null ), SWT.CENTER );
        createLabel( client, "Bodenwert in €", four().top( null ), SWT.CENTER );
        createLabel( client, "Abgleich auf Kaufpreis in €/m²", five().top( null ), SWT.CENTER );

        lastLine = newLine;
        newLine = createLabel( client, "anrechenbare Baulandfläche", one().top( lastLine ), SWT.RIGHT );
        createFlaecheField( vb.flaeche1(), two().top( lastLine ), client, true );
        createPreisField( vb.bodenpreisQm1(), three().top( lastLine ), client, false );
        // wenn die seite bereits an ist dann per refresher
        site.addFieldListener( bodenpreis = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == IFormFieldListener.VALUE_CHANGE
                        && ev.getFieldName().equals( vb.bodenpreisBebaut().qualifiedName().name() )) {
                    Double preis = (Double)ev.getNewValue();
                    site.setFieldValue( vb.bodenpreisQm1().qualifiedName().name(), preis == null ? ""
                            : getFormatter( 2 ).format( preis ) );
                }
            }
        } );

        createPreisField( vb.bodenwert1(), four().top( lastLine ), client, false );
        createPreisField( vb.bodenwertBereinigt1(), five().top( lastLine ), client, false );
        site.addFieldListener( line1multiplicator = new FieldMultiplication( site, 2, vb.flaeche1(),
                vb.bodenpreisQm1(), vb.bodenwert1() ) );
        site.addFieldListener( line1multiplicator2 = new FieldMultiplication( site, 2, vb.bodenpreisQm1(), vb
                .faktorBereinigterKaufpreis(), vb.bodenwertBereinigt1() ) );

        lastLine = newLine;
        newLine = createLabel( client, "Baulandmehrfläche", one().top( lastLine, 12 ), SWT.RIGHT );
        createFlaecheField( vb.flaeche2(), two().top( lastLine, 12 ), client, true );
        createPreisField( vb.bodenpreisQm2(), three().top( lastLine, 12 ), client, true );
        createPreisField( vb.bodenwert2(), four().top( lastLine, 12 ), client, false );
        createPreisField( vb.bodenwertBereinigt2(), five().top( lastLine, 12 ), client, false );
        site.addFieldListener( line2multiplicator = new FieldMultiplication( site, 2, vb.flaeche2(),
                vb.bodenpreisQm2(), vb.bodenwert2() ) );
        site.addFieldListener( line2multiplicator2 = new FieldMultiplication( site, 2, vb.bodenpreisQm2(), vb
                .faktorBereinigterKaufpreis(), vb.bodenwertBereinigt2() ) );

        lastLine = newLine;
        newLine = newFormField( IFormFieldLabel.NO_LABEL ).setEnabled( true )
                .setProperty( new AssociationAdapter<BodenwertAufteilungTextComposite>( vb.bodenwertAufteilung1() ) )
                .setField( namedAssocationsPicklist( BodenwertAufteilungTextComposite.class ) )
                .setLayoutData( one().top( lastLine, 12 ).create() ).setParent( client ).create();
        createFlaecheField( vb.flaeche3(), two().top( lastLine, 12 ), client, true );
        createPreisField( vb.bodenpreisQm3(), three().top( lastLine, 12 ), client, true );
        createPreisField( vb.bodenwert3(), four().top( lastLine, 12 ), client, false );
        createPreisField( vb.bodenwertBereinigt3(), five().top( lastLine, 12 ), client, false );
        site.addFieldListener( line3multiplicator = new FieldMultiplication( site, 2, vb.flaeche3(),
                vb.bodenpreisQm3(), vb.bodenwert3() ) );
        site.addFieldListener( line3multiplicator2 = new FieldMultiplication( site, 2, vb.bodenpreisQm3(), vb
                .faktorBereinigterKaufpreis(), vb.bodenwertBereinigt3() ) );

        lastLine = newLine;
        newLine = newFormField( IFormFieldLabel.NO_LABEL ).setEnabled( true )
                .setProperty( new AssociationAdapter<BodenwertAufteilungTextComposite>( vb.bodenwertAufteilung2() ) )
                .setField( namedAssocationsPicklist( BodenwertAufteilungTextComposite.class ) )
                .setLayoutData( one().top( lastLine ).create() ).setParent( client ).create();
        createFlaecheField( vb.flaeche4(), two().top( lastLine ), client, true );
        createPreisField( vb.bodenpreisQm4(), three().top( lastLine ), client, true );
        createPreisField( vb.bodenwert4(), four().top( lastLine ), client, false );
        createPreisField( vb.bodenwertBereinigt4(), five().top( lastLine ), client, false );
        site.addFieldListener( line4multiplicator = new FieldMultiplication( site, 2, vb.flaeche4(),
                vb.bodenpreisQm4(), vb.bodenwert4() ) );
        site.addFieldListener( line4multiplicator2 = new FieldMultiplication( site, 2, vb.bodenpreisQm4(), vb
                .faktorBereinigterKaufpreis(), vb.bodenwertBereinigt4() ) );

        lastLine = newLine;
        newLine = newFormField( IFormFieldLabel.NO_LABEL ).setEnabled( true )
                .setProperty( new AssociationAdapter<BodenwertAufteilungTextComposite>( vb.bodenwertAufteilung3() ) )
                .setField( namedAssocationsPicklist( BodenwertAufteilungTextComposite.class ) )
                .setLayoutData( one().top( lastLine ).create() ).setParent( client ).create();
        createFlaecheField( vb.flaeche5(), two().top( lastLine ), client, true );
        createPreisField( vb.bodenpreisQm5(), three().top( lastLine ), client, true );
        createPreisField( vb.bodenwert5(), four().top( lastLine ), client, false );
        createPreisField( vb.bodenwertBereinigt5(), five().top( lastLine ), client, false );
        site.addFieldListener( line5multiplicator = new FieldMultiplication( site, 2, vb.flaeche5(),
                vb.bodenpreisQm5(), vb.bodenwert5() ) );
        site.addFieldListener( line5multiplicator2 = new FieldMultiplication( site, 2, vb.bodenpreisQm5(), vb
                .faktorBereinigterKaufpreis(), vb.bodenwertBereinigt5() ) );

        lastLine = newLine;
        newLine = newFormField( IFormFieldLabel.NO_LABEL ).setEnabled( true )
                .setProperty( new PropertyAdapter( vb.bodenwertAufteilungText6() ) ).setField( new StringFormField() )
                .setLayoutData( one().top( lastLine ).create() ).setParent( client ).create();
        createFlaecheField( vb.flaeche6(), two().top( lastLine ), client, true );
        createPreisField( vb.bodenpreisQm6(), three().top( lastLine ), client, true );
        createPreisField( vb.bodenwert6(), four().top( lastLine ), client, false );
        createPreisField( vb.bodenwertBereinigt6(), five().top( lastLine ), client, false );
        site.addFieldListener( line6multiplicator = new FieldMultiplication( site, 2, vb.flaeche6(),
                vb.bodenpreisQm6(), vb.bodenwert6() ) );
        site.addFieldListener( line6multiplicator2 = new FieldMultiplication( site, 2, vb.bodenpreisQm6(), vb
                .faktorBereinigterKaufpreis(), vb.bodenwertBereinigt6() ) );

        lastLine = newLine;
        newLine = createFlaecheField( vb.verkaufteFlaecheGesamt(), two().top( lastLine ), client, false );

        lastLine = newLine;
        newLine = createLabel( client, "Fläche Flurstück", one().top( lastLine ), SWT.RIGHT );
        newFormField( IFormFieldLabel.NO_LABEL ).setEnabled( false )
                .setProperty( new PropertyAdapter( vb.flurstueck().get().verkaufteFlaeche() ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 2, 1, 2 ) )
                .setField( new StringFormField() ).setLayoutData( two().top( lastLine ).bottom( 100 ).create() )
                .setParent( client ).create();

        // createPreisField( vb.bodenwertGesamt(), four().top( lastLine ), client,
        // false );

        site.addFieldListener( flaecheSummation = new FieldSummation( site, 0, vb.verkaufteFlaecheGesamt(), vb
                .flaeche1(), vb.flaeche2(), vb.flaeche3(), vb.flaeche4(), vb.flaeche5(), vb.flaeche6() ) );

        //
        // --- BEWERTUNG
        //
        section = newSection( section, "Bewertung" );
        client = (Composite)section.getClient();

        lastLine = newLine;
        newLine = createLabel( client, "Bewertungsmethode", one().top( lastLine, 12 ), SWT.RIGHT );
        newFormField( IFormFieldLabel.NO_LABEL ).setEnabled( false )
                .setProperty( new PropertyAdapter( vb.bewertungsMethode() ) )
                .setLayoutData( two().top( lastLine ).create() ).setParent( client ).create();

        lastLine = newLine;
        newLine = createLabel( client, "Bodenwert", one().top( lastLine, 12 ), SWT.RIGHT );
        newFormField( IFormFieldLabel.NO_LABEL ).setProperty( new PropertyAdapter( vb.bodenwertGesamt() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 2, 1, 2 ) )
                .setLayoutData( two().top( lastLine ).create() ).setParent( client ).setEnabled( false ).create();
        site.addFieldListener( bodenwertSummation = new FieldSummation( site, 2, vb.bodenwertGesamt(), vb.bodenwert1(),
                vb.bodenwert2(), vb.bodenwert3(), vb.bodenwert4(), vb.bodenwert5(), vb.bodenwert6() ) );

        // lastLine = newLine;
        createLabel( client, "Sachwert", three().top( lastLine, 12 ), SWT.RIGHT );
        newFormField( IFormFieldLabel.NO_LABEL ).setProperty( new PropertyAdapter( vb.sachwert() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 2, 1, 2 ) )
                .setLayoutData( four().top( lastLine ).create() ).setParent( client ).setEnabled( false ).create();
        site.addFieldListener( sachwertSummation = new FieldSummation( site, 2, vb.sachwert(), vb.bodenwert1(), vb
                .bodenwert2(), vb.bodenwert3(), vb.bodenwert4(), vb.bodenwert5(), vb.bodenwert6() ) );

        lastLine = newLine;
        newLine = createLabel( client, "Faktor", one().top( lastLine, 12 ), SWT.RIGHT );
        newFormField( IFormFieldLabel.NO_LABEL ).setToolTipText( "Faktor bereinigter Kaufpreis/Sachwert" )
                .setProperty( new PropertyAdapter( vb.faktorBereinigterKaufpreis() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 4, 1, 4 ) )
                .setLayoutData( two().top( lastLine ).create() ).setParent( client ).setEnabled( false ).create();

        site.addFieldListener( bereinCalculator = new FieldCalculation( site, 4, vb.faktorBereinigterKaufpreis(), vb
                .bodenwertGesamt() ) {

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
                Double bodenwert = values.get( vb.bodenwertGesamt() );
                if (kaufpreis != null && bodenwert != null && bodenwert != 0.0d) {
                    return kaufpreis / bodenwert;
                }
                return null;
            }
        } );

        // kaufpreis / (bodenwert - Wert aller strflaechen)
        createLabel( client, "ohne Straßenplatz", three().top( lastLine, 12 ), SWT.RIGHT );
        newFormField( IFormFieldLabel.NO_LABEL )
                .setToolTipText( "Faktor bereinigter Kaufpreis/Sachwert ohne Straßenplatz" )
                .setProperty( new PropertyAdapter( vb.faktorOhneStrassenplatz() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 4, 1, 4 ) )
                .setLayoutData( four().top( lastLine ).create() ).setParent( client ).setEnabled( false ).create();
        site.addFieldListener( faktorStrassenplatz = new FaktorOhneStrassenplatzCalculator( site, vb ) );

        lastLine = newLine;
        newLine = createLabel( client, "Kaufpreisanteil", one().top( lastLine, 12 ), SWT.RIGHT );
        newFormField( IFormFieldLabel.NO_LABEL ).setToolTipText( "Kaufpreisanteil Bodenwert" )
                .setProperty( new PropertyAdapter( vb.kaufpreisAnteilBodenwert() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 2, 1, 2 ) )
                .setLayoutData( two().top( lastLine ).create() ).setParent( client ).setEnabled( false ).create();
        site.addFieldListener( anteilBodenwert = new FieldMultiplication( site, 2, vb.faktorBereinigterKaufpreis(), vb
                .bodenwertGesamt(), vb.kaufpreisAnteilBodenwert() ) );

        lastLine = newLine;
        newLine = createLabel( client, "Faktor geeignet?", one().top( lastLine, 12 ), SWT.RIGHT );
        newFormField( IFormFieldLabel.NO_LABEL ).setToolTipText( "Faktor für Marktanpassung geeignet?" )
                .setProperty( new PropertyAdapter( vb.faktorFuerMarktanpassungGeeignet() ) )
                .setLayoutData( two().top( lastLine ).bottom( 100 ).create() ).setParent( client ).create();

        // bodenpreis vom 1. tab könnte sich geändert haben, also neu berechnen
        if (bodenpreisBebaut == null) {
            bodenpreisBebaut = vb.bodenpreisBebaut().get();
        }
        if (bodenpreisBebaut != null && bodenpreisBebaut != vb.bodenpreisQm1().get()) {
            vb.bodenpreisQm1().set( bodenpreisBebaut );
            site.setFieldValue( vb.bodenpreisQm1().qualifiedName().name(), getFormatter( 2 ).format( bodenpreisBebaut ) );
        }
    }
}
