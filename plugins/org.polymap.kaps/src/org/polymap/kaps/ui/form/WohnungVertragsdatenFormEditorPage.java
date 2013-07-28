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

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.data.entityfeature.AssociationAdapter;
import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.CheckboxFormField;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.NumberValidator;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.KapsPlugin;
import org.polymap.kaps.model.data.GebaeudeArtComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.model.data.VertragsdatenErweitertComposite;
import org.polymap.kaps.model.data.WohnungComposite;
import org.polymap.kaps.ui.ActionButton;
import org.polymap.kaps.ui.BooleanFormField;
import org.polymap.kaps.ui.FieldCalculation;
import org.polymap.kaps.ui.FieldListener;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class WohnungVertragsdatenFormEditorPage
        extends WohnungFormEditorPage {

    private static Log       log = LogFactory.getLog( WohnungVertragsdatenFormEditorPage.class );

    private FieldListener    fieldListener;

    @SuppressWarnings("unused")
    private FieldCalculation vollpreis;

    @SuppressWarnings("unused")
    private FieldCalculation vollpreisWohnflaeche;


    // private IFormFieldListener gemeindeListener;

    public WohnungVertragsdatenFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( WohnungVertragsdatenFormEditorPage.class.getName(), "Vertragsdaten", feature, featureStore );
        EventManager.instance().subscribe( fieldListener = new FieldListener( wohnung.wohnflaeche() ),
                new EventFilter<FormFieldEvent>() {

                    public boolean apply( FormFieldEvent ev ) {
                        return ev.getEventCode() == IFormFieldListener.VALUE_CHANGE;
                    }
                } );
    }


    @SuppressWarnings("unchecked")
    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );

        Control newLine, lastLine = null;
        Composite parent = pageSite.getPageBody();

        int ONE = 15;
        int TWO = 40;
        int THREE = 50;
        int FOUR = 80;

        final VertragComposite vertrag = wohnung.flurstueck().get() != null ? wohnung.flurstueck().get().vertrag()
                .get() : null;
        String label = vertrag == null ? "Kein Vertrag zugewiesen" : "Vertrag "
                + EingangsNummerFormatter.format( vertrag.eingangsNr().get() ) + " öffnen";
        ActionButton openErweiterteDaten = new ActionButton( parent, new Action( label ) {

            @Override
            public void run() {
                KapsPlugin.openEditor( fs, VertragComposite.NAME, vertrag );
            }
        } );
        openErweiterteDaten.setLayoutData( left().right( 20 ).height( 25 ).create() );
        openErweiterteDaten.setEnabled( vertrag != null );
        newLine = openErweiterteDaten;

        lastLine = newLine;
        newLine = createLabel( parent, "Vollpreis", left().right( ONE ).top( lastLine ), SWT.RIGHT );
        createPreisField( wohnung.kaufpreis(), left().left( ONE ).right( TWO ).top( lastLine ), parent, false );

        lastLine = newLine;
        newLine = createLabel( parent, "Abschlag in €", left().right( TWO ).top( lastLine, 22 ), SWT.CENTER );
        createLabel( parent, "geschätzt", left().left( TWO ).right( THREE ).top( lastLine ), SWT.CENTER );
        createLabel( parent, "Beschreibung", left().left( THREE ).right( FOUR ).top( lastLine ), SWT.CENTER );
        createLabel( parent, "Anzahl", left().left( FOUR ).right( 100 ).top( lastLine ), SWT.CENTER );

        lastLine = newLine;
        newLine = createLabel( parent, "Garage", left().right( ONE ).top( lastLine ), SWT.RIGHT );
        createPreisField( wohnung.abschlagGarage(), left().left( ONE ).right( TWO ).top( lastLine ), parent, true );
        newFormField( IFormFieldLabel.NO_LABEL ).setProperty( new PropertyAdapter( wohnung.schaetzungGarage() ) )
                .setField( new CheckboxFormField() )
                .setLayoutData( left().left( TWO ).right( THREE ).top( lastLine ).create() ).create();
        newFormField( IFormFieldLabel.NO_LABEL )
                .setProperty( new AssociationAdapter<GebaeudeArtComposite>( wohnung.gebaeudeArtGarage() ) )
                .setField( namedAssocationsPicklist( GebaeudeArtComposite.class ) )
                .setLayoutData( left().left( THREE ).right( FOUR ).top( lastLine ).create() ).create();
        newFormField( IFormFieldLabel.NO_LABEL ).setProperty( new PropertyAdapter( wohnung.anzahlGaragen() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Integer.class, Polymap.getSessionLocale() ) )
                .setLayoutData( left().left( FOUR ).right( 100 ).top( lastLine ).create() ).create();

        lastLine = newLine;
        newLine = createLabel( parent, "Stellplatz", left().right( ONE ).top( lastLine ), SWT.RIGHT );
        createPreisField( wohnung.abschlagStellplatz(), left().left( ONE ).right( TWO ).top( lastLine ), parent, true );
        newFormField( IFormFieldLabel.NO_LABEL ).setProperty( new PropertyAdapter( wohnung.schaetzungStellplatz() ) )
                .setField( new CheckboxFormField() )
                .setLayoutData( left().left( TWO ).right( THREE ).top( lastLine ).create() ).create();
        newFormField( IFormFieldLabel.NO_LABEL )
                .setProperty( new AssociationAdapter<GebaeudeArtComposite>( wohnung.gebaeudeArtStellplatz() ) )
                .setField( namedAssocationsPicklist( GebaeudeArtComposite.class ) )
                .setLayoutData( left().left( THREE ).right( FOUR ).top( lastLine ).create() ).create();
        newFormField( IFormFieldLabel.NO_LABEL ).setProperty( new PropertyAdapter( wohnung.anzahlStellplatz() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Integer.class, Polymap.getSessionLocale() ) )
                .setLayoutData( left().left( FOUR ).right( 100 ).top( lastLine ).create() ).create();

        lastLine = newLine;
        newLine = createLabel( parent, "Anderes", left().right( ONE ).top( lastLine ), SWT.RIGHT );
        createPreisField( wohnung.abschlagAnderes(), left().left( ONE ).right( TWO ).top( lastLine ), parent, true );
        newFormField( IFormFieldLabel.NO_LABEL ).setProperty( new PropertyAdapter( wohnung.schaetzungAnderes() ) )
                .setField( new CheckboxFormField() )
                .setLayoutData( left().left( TWO ).right( THREE ).top( lastLine ).create() ).create();
        newFormField( IFormFieldLabel.NO_LABEL )
                .setProperty( new AssociationAdapter<GebaeudeArtComposite>( wohnung.gebaeudeArtAnderes() ) )
                .setField( namedAssocationsPicklist( GebaeudeArtComposite.class ) )
                .setLayoutData( left().left( THREE ).right( FOUR ).top( lastLine ).create() ).create();
        newFormField( IFormFieldLabel.NO_LABEL ).setProperty( new PropertyAdapter( wohnung.anzahlAnderes() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Integer.class, Polymap.getSessionLocale() ) )
                .setLayoutData( left().left( FOUR ).right( 100 ).top( lastLine ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( IFormFieldLabel.NO_LABEL ).setToolTipText( "Zusatz" )
                .setProperty( new PropertyAdapter( wohnung.abschlagBemerkung() ) ).setField( new StringFormField() )
                .setLayoutData( left().left( THREE ).right( FOUR ).top( lastLine ).create() ).create();

        lastLine = newLine;
        newLine = createLabel( parent, "bereinigter Vollpreis", left().right( ONE ).top( lastLine, 12 ), SWT.RIGHT );
        createPreisField( wohnung.bereinigterVollpreis(), left().left( ONE ).right( TWO ).top( lastLine ), parent,
                false );
        site.addFieldListener( vollpreis = new FieldCalculation( pageSite, 2, wohnung.bereinigterVollpreis(), wohnung
                .kaufpreis(), wohnung.abschlagGarage(), wohnung.abschlagStellplatz(), wohnung.abschlagAnderes() ) {

            @Override
            protected Double calculate( ValueProvider values ) {
                Double kaufpreis = values.get( wohnung.kaufpreis() );
                if (kaufpreis != null) {
                    Double ag = values.get( wohnung.abschlagGarage() );
                    if (ag != null) {
                        kaufpreis += ag;
                    }
                    ag = values.get( wohnung.abschlagStellplatz() );
                    if (ag != null) {
                        kaufpreis += ag;
                    }
                    ag = values.get( wohnung.abschlagAnderes() );
                    if (ag != null) {
                        kaufpreis += ag;
                    }
                }
                return kaufpreis;
            }
        } );

        lastLine = newLine;
        newLine = createLabel( parent, "Vollpreis Wohnfläche", left().right( ONE ).top( lastLine ), SWT.RIGHT );
        createPreisField( wohnung.vollpreisWohnflaeche(), left().left( ONE ).right( TWO ).top( lastLine ), parent,
                false );
        site.addFieldListener( vollpreisWohnflaeche = new FieldCalculation( pageSite, 2,
                wohnung.vollpreisWohnflaeche(), wohnung.bereinigterVollpreis(), wohnung.wohnflaeche() ) {

            @Override
            protected Double calculate( ValueProvider values ) {
                Double bereinigterVollpreis = values.get( wohnung.bereinigterVollpreis() );
                if (bereinigterVollpreis != null) {
                    Double wfl = values.get( wohnung.wohnflaeche() );
                    if (wfl != null && wfl.doubleValue() != 0.0d) {
                        bereinigterVollpreis /= wfl;
                    }
                }
                return bereinigterVollpreis;
            }
        } );

        lastLine = newLine;
        newLine = newFormField( "vermietet?" ).setProperty( new PropertyAdapter( wohnung.vermietet() ) )
                .setField( new BooleanFormField() ).setLayoutData( left().top( lastLine ).create() ).create();
        newLine = newFormField( "geeignet?" ).setToolTipText( "zur Auswertung geeignet?" )
                .setProperty( new PropertyAdapter( wohnung.zurAuswertungGeeignet() ) )
                .setField( new CheckboxFormField() ).setLayoutData( right().top( lastLine ).create() ).create();

        lastLine = newLine;
        newLine = createFlaecheField( "Gewichtung", "Gewichtung (Norm = 1,0)", wohnung.gewichtung(),
                left().top( lastLine ), parent, true );

        lastLine = newLine;
        newLine = newFormField( "Bemerkung" ).setProperty( new PropertyAdapter( wohnung.bemerkungVertragsdaten() ) )
                .setField( new TextFormField() )
                .setLayoutData( left().right( RIGHT ).height( 50 ).top( lastLine ).create() ).setParent( parent )
                .create();

    }


    @Override
    public void afterDoLoad( IProgressMonitor monitor )
            throws Exception {
        VertragComposite vertrag = WohnungComposite.Mixin.vertragFor( wohnung );
        Double kaufpreis = null;
        if (vertrag != null) {
            if (vertrag.erweiterteVertragsdaten().get() != null) {
                VertragsdatenErweitertComposite vertragsdatenErweitertComposite = vertrag.erweiterteVertragsdaten()
                        .get();
                kaufpreis = vertragsdatenErweitertComposite.bereinigterVollpreis().get();
            }
            else {
                kaufpreis = vertrag.vollpreis().get();
            }
        }
        if (kaufpreis != null && !kaufpreis.equals( wohnung.kaufpreis().get() )) {
            pageSite.setFieldValue( wohnung.kaufpreis().qualifiedName().name(), getFormatter( 2 ).format( kaufpreis ) );
        }

        if (fieldListener.get( wohnung.wohnflaeche() ) != null) {
            pageSite.fireEvent( this, wohnung.wohnflaeche().qualifiedName().name(), IFormFieldListener.VALUE_CHANGE,
                    fieldListener.get( wohnung.wohnflaeche() ) );
        }
    }
}
