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

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.ui.forms.widgets.Section;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.rhei.data.entityfeature.AssociationAdapter;
import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.model.data.KellerComposite;
import org.polymap.kaps.ui.BooleanFormField;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class FlurstuecksdatenBaulandSonstigesFormEditorPage
        extends FlurstuecksdatenBaulandFormEditorPage {

    // private static final int ONE = 0;
    //
    // private static final int TWO = 25;
    //
    // private static final int THREE = 50;
    //
    // private static final int FOUR = 75;
    //
    // private static final int FIVE = 100;

    private static Log log = LogFactory.getLog( FlurstuecksdatenBaulandSonstigesFormEditorPage.class );


    // private IFormFieldListener gemeindeListener;

    public FlurstuecksdatenBaulandSonstigesFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( FlurstuecksdatenBaulandSonstigesFormEditorPage.class.getName(), "Sonstiges", feature, featureStore );
    }


    //
    // protected SimpleFormData one() {
    // return new SimpleFormData( SPACING ).left( ONE ).right( TWO );
    // }
    //
    //
    // protected SimpleFormData two() {
    // return new SimpleFormData( SPACING ).left( TWO ).right( THREE );
    // }
    //
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
    // protected SimpleFormData onetwo() {
    // return new SimpleFormData( SPACING ).left( ONE ).right( THREE );
    // }

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

        Composite client = parent;

        newLine = newFormField( "Erbbaurecht" ).setEnabled( true )
                .setProperty( new PropertyAdapter( vb.erbbaurecht() ) ).setField( new BooleanFormField() )
                .setLayoutData( left().top( lastLine ).create() ).setParent( client ).create();

        lastLine = newLine;
        newLine = newFormField( "Denkmalschutz" ).setEnabled( true )
                .setProperty( new PropertyAdapter( vb.denkmalschutz() ) ).setField( new BooleanFormField() )
                .setLayoutData( left().top( lastLine ).create() ).setParent( client ).create();

        lastLine = newLine;
        newLine = newFormField( "Sanierung" ).setEnabled( true ).setProperty( new PropertyAdapter( vb.sanierung() ) )
                .setField( new BooleanFormField() ).setLayoutData( left().top( lastLine ).create() ).setParent( client )
                .create();

        lastLine = newLine;
        Map<String, Object> werte = new HashMap<String, Object>();
        werte.put( "Anfangswert", "A" );
        werte.put( "Endwert", "E" );
        werte.put( "unbekannt", "U" );
        newLine = newFormField( "Sanierungswert" ).setEnabled( true )
                .setProperty( new PropertyAdapter( vb.sanierungswert() ) ).setField( new PicklistFormField( werte ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( client ).create();

        lastLine = newLine;
        Map<String, Object> merkmale = new HashMap<String, Object>();
        merkmale.put( "stark gehoben", Integer.valueOf( 1 ) );
        merkmale.put( "gehoben", Integer.valueOf( 2 ) );
        merkmale.put( "mittel", Integer.valueOf( 3 ) );
        merkmale.put( "einfach", Integer.valueOf( 4 ) );
        merkmale.put( "unbekannt", Integer.valueOf( 6 ) );
        newLine = newFormField( "Ausstattung" ).setEnabled( true )
                .setProperty( new PropertyAdapter( vb.ausstattung() ) ).setField( new PicklistFormField( merkmale ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( client ).create();

        lastLine = newLine;
        Section section = newSection( (Composite)lastLine, "Gebäudedaten" );
        client = (Composite)section.getClient();

        lastLine = newLine;
        newLine = createFlaecheField( "Grundstückstiefe (m²)", vb.grundstuecksTiefe(), left().top( lastLine ), client,
                true );
        createFlaecheField( "Grundstücksbreite (m²)", vb.grundstuecksBreite(), right().top( lastLine ), client, true );

        lastLine = newLine;
        newLine = createFlaecheField( "Wohnfläche (m²)", vb.wohnflaeche(), left().top( lastLine ), client, true );
        createFlaecheField( "Gewerbefläche (m²)", vb.gewerbeflaeche(), right().top( lastLine ), client, true );

        lastLine = newLine;
        newLine = createFlaecheField( "Bruttogrundfläche (m²)", vb.bruttoGrundflaeche(), left().top( lastLine ),
                client, true );
        createFlaecheField( "Geschossfläche (m²)", vb.geschossFlaeche(), right().top( lastLine ), client, true );

        lastLine = newLine;
        newLine = createFlaecheField( "Bruttorauminhalt (m³)", vb.bruttoRaumInhalt(), left().top( lastLine ), client,
                true );

        lastLine = newLine;
        newLine = newFormField( "Keller" )
                .setProperty( new AssociationAdapter<KellerComposite>( "keller", vb.keller() ) )
                .setField( namedAssocationsPicklist( KellerComposite.class ) )
                .setLayoutData( left().top( lastLine ).bottom( 100 ).create() ).setParent( client ).create();
    }


    @Override
    public void doLoad( IProgressMonitor monitor )
            throws Exception {
        super.doLoad( monitor );

    }
}
