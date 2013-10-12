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

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.CheckboxFormField;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.form.FormEditor;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.ui.FieldCalculation;
import org.polymap.kaps.ui.FieldListener;
import org.polymap.kaps.ui.FieldSummation;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class VertragsdatenBaulandRichtwertFormEditorPage
        extends VertragsdatenBaulandFormEditorPage {

    private static final int   ONE   = 0;

    private static final int   TWO   = 25;

    private static final int   THREE = 50;

    private static final int   FOUR  = 75;

    private static final int   FIVE  = 100;

    private static Log         log   = LogFactory.getLog( VertragsdatenBaulandRichtwertFormEditorPage.class );

    private IFormFieldListener bodenpreis;

    // protected Double bodenpreisBebaut;

    // private IFormFieldListener bodenpreisListener;

    private FieldListener      fieldListener;

    // private IFormFieldListener bodenpreisUnbebaut;

    private FieldCalculation   zwischensumme;

    private IFormFieldListener erschliessungskosten;

    private FieldCalculation   vorlaeufig;

    private FieldCalculation   gfzbereinigt;

    private FieldCalculation   riwezuschlag;

    private FieldCalculation   riweabschlag;


    // private IFormFieldListener gemeindeListener;

    public VertragsdatenBaulandRichtwertFormEditorPage( final FormEditor editor, Feature feature,
            FeatureStore featureStore ) {
        super( VertragsdatenBaulandRichtwertFormEditorPage.class.getName(), "Richtwert", feature, featureStore );

        EventManager.instance().subscribe(
                fieldListener = new FieldListener( vb.bodenwertBereinigt1(), vb.richtwertAbschlagProzent(),
                        vb.richtwertZuschlagProzent(), vb.erschliessungsKosten(), vb.bodenpreisBebaut() ),
                new FieldListener.EventFilter( editor ) );
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


    protected SimpleFormData three() {
        return new SimpleFormData( SPACING ).left( THREE ).right( FOUR );
    }


    protected SimpleFormData four() {
        return new SimpleFormData( SPACING ).left( FOUR ).right( FIVE );
    }


    protected SimpleFormData onetwo() {
        return new SimpleFormData( SPACING ).left( ONE ).right( THREE );
    }


    //
    // protected SimpleFormData three() {
    // return new SimpleFormData( SPACING ).left( THREE ).right( FOUR );
    // }
    //
    //
    // protected SimpleFormData four() {
    // return new SimpleFormData( SPACING ).left( FOUR ).right( FIVE );
    // }
    //
    //
    // protected SimpleFormData five() {
    // return new SimpleFormData( SPACING ).left( FIVE ).right( SIX );
    // }
    //

    @SuppressWarnings("unchecked")
    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );

        Control newLine, lastLine = null;
        Composite parent = pageSite.getPageBody();

        Section section = newSection( parent, "Bodenpreise (Abgleich auf Kaufpreis)" );
        Composite client = (Composite)section.getClient();
        // Composite client = parent;

        createLabel( client, "Bodenpreis bebaut", three().top( lastLine, 12 ), SWT.RIGHT );
        newLine = createPreisField( vb.bodenpreisAbgleichAufBaupreisBebaut(), four().top( lastLine ), client, false );
        // wenn die seite bereits an ist dann per refresher
        site.addFieldListener( bodenpreis = new FieldSummation( site, 2, vb.bodenpreisAbgleichAufBaupreisBebaut(), vb
                .bodenwertBereinigt1() ) );
        //
        // createLabel( client, "Bodenpreis unbebaut", three().top( lastLine, 12 ),
        // SWT.RIGHT );
        // createPreisField( vb.bodenpreisAbgleichAufKaufpreisUnbebaut(), four().top(
        // lastLine ), client, false );
        // site.addFieldListener( bodenpreisUnbebaut = new FieldCalculation( site, 2,
        // vb
        // .bodenpreisAbgleichAufKaufpreisUnbebaut(),
        // vb.bodenpreisAbgleichAufBaupreisBebaut(), vb.bebAbschlag() ) {
        //
        // @Override
        // protected Double calculate( ValueProvider values ) {
        // Double preis = values.get( vb.bodenpreisAbgleichAufBaupreisBebaut() );
        // if (preis == null) {
        // preis = 0.0d;
        // }
        // Double abschlag = values.get( vb.bebAbschlag() );
        // if (abschlag == null) {
        // abschlag = 0.0d;
        // }
        // return preis / ((100 - abschlag) / 100);
        // }
        // } );

        lastLine = newLine;
        createLabel( client, "Verkehrswertfaktor", three().top( lastLine, 12 ), SWT.RIGHT );
        newLine = createPreisField( vb.verkehrswertFaktor(), four().top( lastLine ), client, true );

        lastLine = newLine;
        createLabel( client, "Zwischensumme", three().top( lastLine, 12 ), SWT.RIGHT );
        newLine = createPreisField( vb.zwischensummeVerkehrswertfaktor(), four().top( lastLine ), client, true );
        site.addFieldListener( zwischensumme = new FieldCalculation( site, 2, vb.zwischensummeVerkehrswertfaktor(), vb
                .bodenpreisAbgleichAufBaupreisBebaut(), vb.verkehrswertFaktor() ) {

            @Override
            protected Double calculate( ValueProvider values ) {
                Double preis = values.get( vb.bodenpreisAbgleichAufBaupreisBebaut() );
                if (preis == null) {
                    return null;
                }
                Double faktor = values.get( vb.verkehrswertFaktor() );
                if (faktor == null) {
                    faktor = 1.0d;
                }
                return preis / faktor;
            }
        } );

        lastLine = newLine;
        createLabel( client, "Richtwertzuschlag", three().top( lastLine, 12 ), SWT.RIGHT );
        newLine = createPreisField( vb.zwischensummeRichtwertZuschlag(), four().top( lastLine ), client, true );
        site.addFieldListener( riwezuschlag = new FieldCalculation( site, 2, vb.zwischensummeRichtwertZuschlag(), vb
                .zwischensummeVerkehrswertfaktor(), vb.richtwertZuschlagProzent() ) {

            @Override
            protected Double calculate( ValueProvider values ) {
                Double zwischensumme = values.get( vb.zwischensummeVerkehrswertfaktor() );
                if (zwischensumme == null) {
                    zwischensumme = 0.0d;
                }
                Double zuschlag = values.get( vb.richtwertZuschlagProzent() );
                if (zuschlag == null) {
                    zuschlag = 0.0d;
                }
                return (zwischensumme / (1 + (zuschlag / 100))) - zwischensumme;
            }
        } );

        lastLine = newLine;
        createLabel( client, "Richtwertabschlag", three().top( lastLine, 12 ), SWT.RIGHT );
        newLine = createPreisField( vb.zwischensummeRichtwertAbschlag(), four().top( lastLine ), client, true );
        site.addFieldListener( riweabschlag = new FieldCalculation( site, 2, vb.zwischensummeRichtwertAbschlag(), vb
                .zwischensummeVerkehrswertfaktor(), vb.richtwertAbschlagProzent() ) {

            @Override
            protected Double calculate( ValueProvider values ) {
                Double zwischensumme = values.get( vb.zwischensummeVerkehrswertfaktor() );
                if (zwischensumme == null) {
                    zwischensumme = 0.0d;
                }
                Double abschlag = values.get( vb.richtwertAbschlagProzent() );
                if (abschlag == null) {
                    abschlag = 0.0d;
                }
                return (zwischensumme / (1 - (abschlag / 100))) - zwischensumme;
            }
        } );

        lastLine = newLine;
        createLabel( client, "Erschließungskosten", three().top( lastLine, 12 ), SWT.RIGHT );
        newLine = createPreisField( vb.zwischensummeEK(), four().top( lastLine ), client, true );
        site.addFieldListener( erschliessungskosten = new FieldSummation( site, 2, vb.zwischensummeEK(), vb
                .erschliessungsKosten() ) );

        lastLine = newLine;
        createLabel( client, "vorläufiger Bodenpreis", three().top( lastLine, 12 ), SWT.RIGHT );
        newLine = createPreisField( vb.vorlaeufigerBodenpreis(), four().top( lastLine ), client, true );
        site.addFieldListener( vorlaeufig = new FieldCalculation( site, 2, vb.vorlaeufigerBodenpreis(), vb
                .zwischensummeRichtwertAbschlag(), vb.zwischensummeEK(), vb.zwischensummeRichtwertAbschlag(), vb
                .zwischensummeVerkehrswertfaktor() ) {

            @Override
            protected Double calculate( ValueProvider values ) {
                Double zwischensumme = values.get( vb.zwischensummeVerkehrswertfaktor() );
                if (zwischensumme == null) {
                    zwischensumme = 0.0d;
                }
                Double abschlag = values.get( vb.zwischensummeRichtwertAbschlag() );
                if (abschlag != null) {
                    zwischensumme += abschlag;
                }
                Double zuschlag = values.get( vb.zwischensummeRichtwertZuschlag() );
                if (zuschlag != null) {
                    zwischensumme -= zuschlag;
                }
                Double ek = values.get( vb.zwischensummeEK() );
                if (ek != null) {
                    zwischensumme += ek;
                }
                return zwischensumme;
            }
        } );

        lastLine = newLine;
        createLabel( client, "normierter GFZ bereinigter Bodenpreis", one().right( FOUR ).top( lastLine, 12 ),
                SWT.RIGHT );
        newLine = createPreisField( vb.normierterGfzBereinigterBodenpreis(), four().top( lastLine ), client, true );
        site.addFieldListener( gfzbereinigt = new FieldCalculation( site, 2, vb.normierterGfzBereinigterBodenpreis(),
                vb.vorlaeufigerBodenpreis() ) {

            @Override
            protected Double calculate( ValueProvider values ) {
                return values.get( vb.vorlaeufigerBodenpreis() );
            }
        } );

        lastLine = newLine;
        createLabel( client, "zur Richtwertermittlung geeignet?", one().right( FOUR ).top( lastLine, 12 ), SWT.RIGHT );
        newLine = newFormField( IFormFieldLabel.NO_LABEL ).setEnabled( true ).setField( new CheckboxFormField() )
                .setToolTipText( "zur Richtwertermittlung geeignet?" )
                .setProperty( new PropertyAdapter( vb.zurRichtwertermittlungGeeignet() ) )
                .setLayoutData( four().top( lastLine ).create() ).setParent( client ).create();

        lastLine = newLine;
        createLabel( client, "für Statistik Bodenwertaufteilung nicht geeignet?", one().right( FOUR )
                .top( lastLine, 12 ), SWT.RIGHT );
        newLine = newFormField( IFormFieldLabel.NO_LABEL ).setEnabled( true ).setField( new CheckboxFormField() )
                .setToolTipText( "für Statistik Bodenwertaufteilung nicht geeignet?" )
                .setProperty( new PropertyAdapter( vb.fuerBodenwertaufteilungNichtGeeignet() ) )
                .setLayoutData( four().top( lastLine ).create() ).setParent( client ).create();

        lastLine = newLine;
        createLabel( client, "Ergebniswürdigung", one().top( lastLine, 12 ), SWT.RIGHT );
        newLine = newFormField( IFormFieldLabel.NO_LABEL ).setEnabled( true )
                .setProperty( new PropertyAdapter( vb.ergebnisWuerdigung() ) ).setField( new TextFormField() )
                .setLayoutData( two().right( FIVE ).height( 50 ).top( lastLine ).create() ).setParent( client )
                .create();

    }


    @Override
    public void afterDoLoad( IProgressMonitor monitor )
            throws Exception {
        super.afterDoLoad( monitor );

        fieldListener.flush( pageSite );
    }
}
