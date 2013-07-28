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

import java.util.TreeMap;

import java.beans.PropertyChangeListener;

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.ui.forms.widgets.Section;

import org.polymap.core.runtime.Polymap;

import org.polymap.rhei.data.entityfeature.AssociationAdapter;
import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.NumberValidator;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.model.data.AusstattungComposite;
import org.polymap.kaps.model.data.EigentumsartComposite;
import org.polymap.kaps.model.data.EtageComposite;
import org.polymap.kaps.model.data.FlurstueckComposite;
import org.polymap.kaps.model.data.HimmelsrichtungComposite;
import org.polymap.kaps.model.data.WohnungComposite;
import org.polymap.kaps.ui.BooleanFormField;
import org.polymap.kaps.ui.FieldCalculation;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class WohnungGrunddatenFormEditorPage
        extends WohnungFormEditorPage {

    private static Log             log = LogFactory.getLog( WohnungGrunddatenFormEditorPage.class );

    private PropertyChangeListener compositeListener;

    private IFormFieldListener     publisher;

    private FieldCalculation       riwezuschlag;

    private FieldCalculation       riweabschlag;

    private FieldCalculation       preisunbebaut;

    private FieldCalculation       bebabschlag;

    private FieldCalculation       bodenpreisbebaut;

    private IFormFieldListener     richtwertzone;


    // private IFormFieldListener gemeindeListener;

    public WohnungGrunddatenFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( WohnungGrunddatenFormEditorPage.class.getName(), "Grunddaten", feature, featureStore );
    }


    @SuppressWarnings("unchecked")
    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );

        Composite newLine, lastLine = null;
        Composite parent = pageSite.getPageBody();

        newLine = newFormField( IFormFieldLabel.NO_LABEL ).setToolTipText( "Objektnummer" )
                .setProperty( new PropertyAdapter( wohnung.objektNummer() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NotNullNumberValidator( Integer.class, Polymap.getSessionLocale() ) )
                .setEnabled( wohnung.objektNummer().get() == null ).setLayoutData( left().left( 0 ).right( 15 ).create() ).create();

        newFormField( IFormFieldLabel.NO_LABEL ).setToolTipText( "Fortführung" )
                .setProperty( new PropertyAdapter( wohnung.objektFortfuehrung() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NotNullNumberValidator( Integer.class, Polymap.getSessionLocale() ) )
                .setEnabled( wohnung.objektNummer().get() == null ).setLayoutData( left().left( 16 ).right( 31 ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( IFormFieldLabel.NO_LABEL ).setToolTipText( "Gebäudenummer" )
                .setProperty( new PropertyAdapter( wohnung.gebaeudeNummer() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NotNullNumberValidator( Integer.class, Polymap.getSessionLocale() ) )
                .setEnabled( wohnung.objektNummer().get() == null ).setLayoutData( left().left( 34 ).right( 49 ).create() )
                .create();

        newFormField( IFormFieldLabel.NO_LABEL ).setToolTipText( "Fortführung" )
                .setProperty( new PropertyAdapter( wohnung.gebaeudeFortfuehrung() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NotNullNumberValidator( Integer.class, Polymap.getSessionLocale() ) )
                .setEnabled( wohnung.objektNummer().get() == null ).setLayoutData( left().left( 50 ).right( 65 ).create() )
                .create();

        lastLine = newLine;
        newLine = newFormField( IFormFieldLabel.NO_LABEL ).setToolTipText( "Wohnungsnummer" )
                .setProperty( new PropertyAdapter( wohnung.wohnungsNummer() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NotNullNumberValidator( Integer.class, Polymap.getSessionLocale() ) )
                .setEnabled( wohnung.wohnungsNummer().get() == null ).setLayoutData( left().left( 69 ).right( 83 ).create() )
                .create();

        newFormField( IFormFieldLabel.NO_LABEL ).setToolTipText( "Fortführung" )
                .setProperty( new PropertyAdapter( wohnung.wohnungsFortfuehrung() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NotNullNumberValidator( Integer.class, Polymap.getSessionLocale() ) )
                .setEnabled( wohnung.wohnungsNummer().get() == null ).setLayoutData( left().left( 84 ).right( 100 ).create() )
                .create();

        // flurstücke
        lastLine = newLine;
        TreeMap<String, FlurstueckComposite> flurstuecke = new TreeMap<String, FlurstueckComposite>();

        if (wohnung.gebaeudeNummer().get() != null) {
            // its not a new wohnung without a gebaeude
            Iterable<FlurstueckComposite> iterable = WohnungComposite.Mixin.findFlurstueckeFor( wohnung );
            for (FlurstueckComposite flurstueck : iterable) {
                flurstuecke.put( flurstueck.name().get(), flurstueck );
            }
        }
        if (wohnung.flurstueck().get() != null) {
            flurstuecke.put( wohnung.flurstueck().get().name().get(), wohnung.flurstueck().get() );
        }
        newLine = newFormField( "Lage" )
                .setProperty( new AssociationAdapter<FlurstueckComposite>( wohnung.flurstueck() ) )
                .setField( new PicklistFormField( flurstuecke.descendingMap() ) )
                .setLayoutData( left().top( lastLine ).right( RIGHT ).create() ).setParent( parent ).create();

        lastLine = newLine;
        newLine = newFormField( "Eigentumsnr." ).setToolTipText( "Wohnungseigentumsnummer" )
                .setProperty( new PropertyAdapter( wohnung.wohnungseigentumsNummer() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( parent ).create();

        lastLine = newLine;
        newLine = newFormField( "Eigentum" ).setToolTipText( "Eigentum am Grundstück" )
                .setProperty( new AssociationAdapter<EigentumsartComposite>( wohnung.eigentumsArt() ) )
                .setField( namedAssocationsPicklist( EigentumsartComposite.class ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( parent ).create();
        newFormField( "Anzahl Zimmer" ).setProperty( new PropertyAdapter( wohnung.anzahlZimmer() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 0, 1, 0 ) )
                .setLayoutData( right().right( 75 ).top( lastLine ).create() ).setParent( parent ).create();

        lastLine = newLine;
        newLine = createFlaecheField( "Wohn-/Nutzfläche", "Woh-/Nutzfläche in m²", wohnung.wohnflaeche(),
                left().top( lastLine ), parent, true );

        lastLine = newLine;
        newLine = newFormField( "Gesamtnutzungsdauer" ).setToolTipText( "Gesamtnutzungsdauer in Jahren" )
                .setProperty( new PropertyAdapter( wohnung.gesamtNutzungsDauer() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 0, 1, 0 ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( parent ).create();

        lastLine = newLine;
        newLine = newFormField( "Baujahr tatsächlich" ).setToolTipText( "Baujahr tatsächlich" )
                .setProperty( new PropertyAdapter( wohnung.baujahr() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 0, 1, 0 ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( parent ).create();
        newFormField( "Baujahr bereinigt" ).setToolTipText( "Baujahr bereinigt" )
                .setProperty( new PropertyAdapter( wohnung.bereinigtesBaujahr() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 0, 1, 0 ) )
                .setLayoutData( right().top( lastLine ).create() ).setParent( parent ).create();

        lastLine = newLine;
        newLine = newFormField( "Etage" ).setProperty( new AssociationAdapter<EtageComposite>( wohnung.etage() ) )
                .setField( namedAssocationsPicklist( EtageComposite.class ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( parent ).create();
        newFormField( "Bemerkung" ).setToolTipText( "Bemerkung zur Etage" )
                .setProperty( new PropertyAdapter( wohnung.etageBeschreibung() ) ).setField( new StringFormField() )
                .setLayoutData( right().top( lastLine ).create() ).setParent( parent ).create();

        lastLine = newLine;
        newLine = newFormField( "Himmelsrichtung" )
                .setProperty( new AssociationAdapter<HimmelsrichtungComposite>( wohnung.himmelsrichtung() ) )
                .setField( namedAssocationsPicklist( HimmelsrichtungComposite.class ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( parent ).create();
        newFormField( "Umbau" ).setToolTipText( "Jahr des letzen Umbaus" )
                .setProperty( new PropertyAdapter( wohnung.umbau() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Integer.class, Polymap.getSessionLocale(), 12, 0, 1, 0 ) )
                .setLayoutData( right().top( lastLine ).create() ).setParent( parent ).create();

        lastLine = newLine;
        newLine = newFormField( "Bemerkung" ).setProperty( new PropertyAdapter( wohnung.bemerkung() ) )
                .setField( new TextFormField() )
                .setLayoutData( left().right( RIGHT ).height( 50 ).top( lastLine ).create() ).setParent( parent )
                .create();

        Section section = newSection( newLine, "Ausstattung" );
        Composite client = (Composite)section.getClient();

        lastLine = newLine;
        newLine = newFormField( "Balkon" ).setToolTipText( "wertrelevanter Balkon?" )
                .setProperty( new PropertyAdapter( wohnung.balkon() ) ).setField( new BooleanFormField() )
                .setLayoutData( left().top( lastLine ).create() ).setParent( client ).create();
        newFormField( "Terrasse" ).setToolTipText( "wertrelevante Terrasse?" )
                .setProperty( new PropertyAdapter( wohnung.terrasse() ) ).setField( new BooleanFormField() )
                .setLayoutData( right().top( lastLine ).create() ).setParent( client ).create();

        lastLine = newLine;
        newLine = newFormField( "Punkte" ).setToolTipText( "Bewertungspunkte der Ausstattung" )
                .setProperty( new PropertyAdapter( wohnung.bewertungsPunkte() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 0, 1, 0 ) )
                .setLayoutData( left().top( lastLine ).bottom( 100 ).create() ).setParent( client ).create();
        newFormField( "Schlüssel" ).setProperty( new AssociationAdapter<AusstattungComposite>( wohnung.ausstattung() ) )
                .setField( namedAssocationsPicklist( AusstattungComposite.class ) )
                .setLayoutData( right().top( lastLine ).create() ).setParent( client ).create();

    }
}
