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

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.Action;

import org.eclipse.ui.forms.widgets.Section;

import org.polymap.rhei.data.entityfeature.AssociationAdapter;
import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.KapsPlugin;
import org.polymap.kaps.model.data.GebaeudeArtComposite;
import org.polymap.kaps.model.data.GebaeudeComposite;
import org.polymap.kaps.model.data.WohnungComposite;
import org.polymap.kaps.ui.ActionButton;
import org.polymap.kaps.ui.BooleanFormField;
import org.polymap.kaps.ui.KapsDefaultFormEditorPage;
import org.polymap.kaps.ui.MyNumberValidator;
import org.polymap.kaps.ui.NotNullMyNumberValidator;
import org.polymap.kaps.ui.SimplePickList;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class GebaeudeGrunddatenFormEditorPage
        extends KapsDefaultFormEditorPage {

    private static Log        log = LogFactory.getLog( GebaeudeGrunddatenFormEditorPage.class );

    private GebaeudeComposite gebaeude;

    private SimplePickList<WohnungComposite> wohnungPicklist;

    private ActionButton createWohnung;


    // private FlurstueckSearcher sfAction;

    // private GemarkungComposite selectedGemarkung;

    // private NutzungComposite selectedNutzung;

    // private IFormFieldListener gemarkungListener;

    // private IFormFieldListener nutzungListener;

    // private ActionButton openErweiterteDaten;

    public GebaeudeGrunddatenFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( GebaeudeGrunddatenFormEditorPage.class.getName(), "Grunddaten", feature, featureStore );

        gebaeude = repository.findEntity( GebaeudeComposite.class, feature.getIdentifier().getID() );
    }


    @Override
    public void createFormContent( IFormEditorPageSite site ) {
        super.createFormContent( site );

        String nummer = gebaeude.objektNummer().get() != null ? gebaeude.schl().get() : "neu";

        site.setEditorTitle( formattedTitle( "Gebäude", nummer, null ) );
        site.setFormTitle( formattedTitle( "Gebäude", nummer, getTitle() ) );

        Composite parent = site.getPageBody();
        Composite form = createEditorForm( parent );
        Composite extendedForm = createErweiterteDatenForm( form );
    }


    public Composite createEditorForm( Composite parent ) {

        Composite lastLine, newLine = null;

        newLine = newFormField( IFormFieldLabel.NO_LABEL ).setToolTipText( "Objektnummer" )
                .setProperty( new PropertyAdapter( gebaeude.objektNummer() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NotNullMyNumberValidator( Integer.class ) )
                .setEnabled( gebaeude.objektNummer().get() == null )
                .setLayoutData( left().left( 0 ).right( 15 ).create() ).create();

        newFormField( IFormFieldLabel.NO_LABEL ).setToolTipText( "Fortführung" )
                .setProperty( new PropertyAdapter( gebaeude.objektFortfuehrung() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NotNullMyNumberValidator( Integer.class ) )
                .setEnabled( gebaeude.objektFortfuehrung().get() == null )
                .setLayoutData( left().left( 16 ).right( 31 ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( IFormFieldLabel.NO_LABEL ).setToolTipText( "Gebäudenummer" )
                .setProperty( new PropertyAdapter( gebaeude.gebaeudeNummer() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NotNullMyNumberValidator( Integer.class ) )
                .setEnabled( gebaeude.gebaeudeNummer().get() == null )
                .setLayoutData( left().left( 34 ).right( 49 ).create() ).create();

        newFormField( IFormFieldLabel.NO_LABEL ).setToolTipText( "Fortführung" )
                .setProperty( new PropertyAdapter( gebaeude.gebaeudeFortfuehrung() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NotNullMyNumberValidator( Integer.class ) )
                .setEnabled( gebaeude.gebaeudeFortfuehrung().get() == null )
                .setLayoutData( left().left( 50 ).right( 65 ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( "Gebäudeart" ).setParent( parent )
                .setProperty( new AssociationAdapter<GebaeudeArtComposite>( gebaeude.gebaeudeArt() ) )
                .setField( namedAssocationsPicklist( GebaeudeArtComposite.class, true ) )
                .setLayoutData( left().top( lastLine ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( "Baujahr tatsächlich" ).setToolTipText( "tatsächliches Baujahr" ).setParent( parent )
                .setProperty( new PropertyAdapter( gebaeude.baujahrTatsaechlich() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setLayoutData( left().top( lastLine ).create() )
                .setValidator( new MyNumberValidator( Integer.class ) ).create();

        newFormField( "Baujahr bereinigt" ).setToolTipText( "bereinigtes Baujahr" ).setParent( parent )
                .setProperty( new PropertyAdapter( gebaeude.baujahr() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setLayoutData( right().top( lastLine ).create() )
                .setValidator( new MyNumberValidator( Integer.class ) ).create();

        lastLine = newLine;
        newLine = newFormField( "Lageklasse" ).setParent( parent )
                .setProperty( new PropertyAdapter( gebaeude.lageklasse() ) ).setField( new StringFormField() )
                .setLayoutData( left().top( lastLine ).create() )
                .setValidator( new MyNumberValidator( Double.class ) ).create();

        newFormField( "Wohn-/Gewerbeeinheiten" ).setParent( parent )
                .setProperty( new PropertyAdapter( gebaeude.wohnEinheiten() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setLayoutData( right().top( lastLine ).create() )
                .setValidator( new MyNumberValidator( Integer.class ) ).create();

        lastLine = newLine;
        newLine = newFormField( "Aufzug" )

        .setProperty( new PropertyAdapter( gebaeude.aufzug() ) ).setField( new BooleanFormField() )
                .setLayoutData( left().top( lastLine ).create() ).setParent( parent ).create();
        newFormField( "Denkmalschutz" )

        .setProperty( new PropertyAdapter( gebaeude.denkmalschutz() ) ).setField( new BooleanFormField() )
                .setLayoutData( right().top( lastLine ).create() ).setParent( parent ).create();

        lastLine = newLine;
        newLine = newFormField( "Sanierung" )

        .setProperty( new PropertyAdapter( gebaeude.sanierung() ) ).setField( new BooleanFormField() )
                .setLayoutData( left().top( lastLine ).create() ).setParent( parent ).create();

        Map<String, Object> werte = new HashMap<String, Object>();
        werte.put( "Anfangswert", "A" );
        werte.put( "Endwert", "E" );
        werte.put( "unbekannt", "U" );
        newFormField( "Sanierungswert" ).setEnabled( true )
                .setProperty( new PropertyAdapter( gebaeude.sanierungswert() ) )
                .setField( new PicklistFormField( werte ) ).setLayoutData( right().top( lastLine ).create() )
                .setParent( parent ).create();

        lastLine = newLine;
        newLine = newFormField( "Bemerkung" ).setProperty( new PropertyAdapter( gebaeude.bemerkung() ) )
                .setField( new TextFormField() )
                .setLayoutData( left().top( lastLine ).right( RIGHT ).height( 100 ).create() ).setParent( parent )
                .create();
        return newLine;
    }


    public Section createErweiterteDatenForm( Composite top ) {

        Section formSection = newSection( top, "Gebäude" );
        Composite parent = (Composite)formSection.getClient();

        final ActionButton openGebaeude = new ActionButton( parent, new Action( "Wohnung bearbeiten" ) {

            @Override
            public void run() {
                WohnungComposite gebaeude = wohnungPicklist.getSelection();
                if (gebaeude != null) {
                    KapsPlugin.openEditor( fs, WohnungComposite.NAME, gebaeude );
                }
            }

        } );
        openGebaeude.setLayoutData( left().left( 35 ).right( 55 ).height( 25 ).top( null ).create() );
        openGebaeude.setEnabled( false );

        // Liste mit Wohnung + Auswählen daneben
        wohnungPicklist = new SimplePickList<WohnungComposite>( parent, pageSite ) {

            @Override
            public SortedMap<String, WohnungComposite> getValues() {
                SortedMap<String, WohnungComposite> values = new TreeMap<String, WohnungComposite>();
                if (gebaeude.objektNummer().get() != null) {
                    Iterable<WohnungComposite> iterable = WohnungComposite.Mixin.findWohnungenFor( gebaeude );
                    for (WohnungComposite zone : iterable) {
                        values.put( zone.schl().get(), zone );
                    }
                }
                return values;
            }


            @Override
            public void onSelection( WohnungComposite selectedObject ) {
                if (openGebaeude != null) {
                    openGebaeude.setEnabled( selectedObject != null );
                }
            }
        };
        wohnungPicklist.setLayoutData( right().left( 10 ).right( 30 ).height( 25 ).top( null ).bottom( 100 ).create() );
        wohnungPicklist.setEnabled( true );

        createWohnung = new ActionButton( parent, new Action( "Wohnung anlegen" ) {

            @Override
            public void run() {
                if (gebaeude.objektNummer().get() != null) {
                    WohnungComposite wohnung = repository.newEntity( WohnungComposite.class, null );
                    wohnung.objektNummer().set( gebaeude.objektNummer().get() );
                    wohnung.objektFortfuehrung().set( gebaeude.objektFortfuehrung().get() );
                    wohnung.gebaeudeNummer().set( gebaeude.gebaeudeNummer().get() );
                    wohnung.gebaeudeFortfuehrung().set( gebaeude.gebaeudeFortfuehrung().get() );
                    // wohnung.vertrag().set( flurstueck.vertrag().get() );
                    KapsPlugin.openEditor( fs, WohnungComposite.NAME, wohnung );
                }
            }

        } );
        createWohnung.setLayoutData( left().left( 75 ).right( 95 ).height( 25 ).top( null ).create() );
        createWohnung.setEnabled( true );

        return formSection;
    }
}