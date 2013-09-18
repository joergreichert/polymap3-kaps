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
import java.util.TreeMap;

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.Action;

import org.eclipse.ui.forms.widgets.Section;

import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.KapsPlugin;
import org.polymap.kaps.model.data.GebaeudeComposite;
import org.polymap.kaps.model.data.WohnungseigentumComposite;
import org.polymap.kaps.ui.ActionButton;
import org.polymap.kaps.ui.KapsDefaultFormEditorPage;
import org.polymap.kaps.ui.NotNullMyNumberValidator;
import org.polymap.kaps.ui.SimplePickList;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class WohnungseigentumFormEditorPage
        extends KapsDefaultFormEditorPage {

    private SimplePickList<GebaeudeComposite> gebaudePicklist;

    private ActionButton                      createGebaeude;

    private final WohnungseigentumComposite   eigentum;


    public WohnungseigentumFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( WohnungseigentumFormEditorPage.class.getName(), "Grunddaten", feature, featureStore );

        eigentum = repository.findEntity( WohnungseigentumComposite.class, feature.getIdentifier().getID() );
    }


    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );

        String nummer = eigentum.objektNummer().get() != null ? eigentum.schl().get() : "neu";

        site.setEditorTitle( formattedTitle( "Wohnungseigentum", nummer, null ) );
        site.setFormTitle( formattedTitle( "Wohnungseigentum", nummer, getTitle() ) );

        Composite parent = site.getPageBody();
        Composite schildForm = createEditorForm( parent );

        Composite extendedForm = createErweiterteDatenForm( schildForm );
    }


    private Composite createEditorForm( Composite parent ) {
        Composite lastLine, newLine = null;

        // readonly
        newLine = newFormField( IFormFieldLabel.NO_LABEL ).setToolTipText( "Objektnummer" )
                .setProperty( new PropertyAdapter( eigentum.objektNummer() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NotNullMyNumberValidator( Integer.class ) )
                .setEnabled( eigentum.objektNummer().get() == null )
                .setLayoutData( left().left( 0 ).right( 15 ).create() ).create();

        newFormField( IFormFieldLabel.NO_LABEL ).setToolTipText( "Fortführung" )
                .setProperty( new PropertyAdapter( eigentum.objektFortfuehrung() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NotNullMyNumberValidator( Integer.class ) )
                .setEnabled( eigentum.objektNummer().get() == null )
                .setLayoutData( left().left( 16 ).right( 31 ).create() ).create();

        lastLine = newLine;
        newLine = createFlaecheField( "Gesamtfläche in m²", eigentum.gesamtFlaeche(), left().top( lastLine ), parent,
                false );

        lastLine = newLine;
        newLine = newFormField( "TE-Datum" ).setToolTipText( "letzter Nachtrag der Teilungserklärung" )
                .setProperty( new PropertyAdapter( eigentum.datumTeilungserklerung() ) )
                .setLayoutData( left().top( lastLine ).create() ).create();
        newFormField( "TE-Urkunde Nr." ).setToolTipText( "Urkundennummer der letzten Teilungserklärung" )
                .setProperty( new PropertyAdapter( eigentum.urkundenNummerDerTeilungserklaerung() ) )
                .setLayoutData( right().top( lastLine ).create() ).create();

        lastLine = newLine;
        // Bemerkungen
        newLine = newFormField( "Bemerkungen" ).setProperty( new PropertyAdapter( eigentum.bemerkungen() ) )
                .setField( new TextFormField() )
                .setLayoutData( left().right( RIGHT ).height( 100 ).top( lastLine ).create() ).create();

        return newLine;
    }


    public Section createErweiterteDatenForm( Composite top ) {

        Section formSection = newSection( top, "Gebäude" );
        Composite parent = (Composite)formSection.getClient();

        final ActionButton openGebaeude = new ActionButton( parent, new Action( "Gebäude bearbeiten" ) {

            @Override
            public void run() {
                GebaeudeComposite gebaeude = gebaudePicklist.getSelection();
                if (gebaeude != null) {
                    KapsPlugin.openEditor( fs, GebaeudeComposite.NAME, gebaeude );
                }
            }

        } );
        openGebaeude.setLayoutData( left().left( 35 ).right( 55 ).height( 25 ).top( null ).create() );
        openGebaeude.setEnabled( false );

        // Liste mit Wohnung + Auswählen daneben
        gebaudePicklist = new SimplePickList<GebaeudeComposite>( parent, pageSite ) {

            @Override
            public SortedMap<String, GebaeudeComposite> getValues() {
                SortedMap<String, GebaeudeComposite> values = new TreeMap<String, GebaeudeComposite>();
                if (eigentum.objektNummer().get() != null) {
                    Iterable<GebaeudeComposite> iterable = GebaeudeComposite.Mixin.forEntity( eigentum );
                    for (GebaeudeComposite zone : iterable) {
                        values.put( zone.schl().get(), zone );
                    }
                }
                return values;
            }


            @Override
            public void onSelection( GebaeudeComposite selectedObject ) {
                if (openGebaeude != null) {
                    openGebaeude.setEnabled( selectedObject != null );
                }
            }
        };
        gebaudePicklist.setLayoutData( right().left( 10 ).right( 30 ).height( 25 ).top( null ).bottom( 100 ).create() );
        gebaudePicklist.setEnabled( true );

        createGebaeude = new ActionButton( parent, new Action( "Gebäude anlegen" ) {

            @Override
            public void run() {
                if (eigentum.objektNummer().get() != null) {
                    GebaeudeComposite gebaeude = repository.newEntity( GebaeudeComposite.class, null );
                    gebaeude.objektNummer().set( eigentum.objektNummer().get() );
                    gebaeude.objektFortfuehrung().set( eigentum.objektFortfuehrung().get() );
                    // wohnung.vertrag().set( flurstueck.vertrag().get() );
                    KapsPlugin.openEditor( fs, GebaeudeComposite.NAME, gebaeude );
                }
            }

        } );
        createGebaeude.setLayoutData( left().left( 75 ).right( 95 ).height( 25 ).top( null ).create() );
        createGebaeude.setEnabled( true );
        
        return formSection;
    }
}