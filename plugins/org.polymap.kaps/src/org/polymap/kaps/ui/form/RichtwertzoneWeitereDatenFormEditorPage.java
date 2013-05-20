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

import org.eclipse.swt.widgets.Composite;

import org.polymap.rhei.data.entityfeature.AssociationAdapter;
import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.NumberValidator;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.form.IFormEditorPage;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.model.data.BauweiseComposite;
import org.polymap.kaps.model.data.BodenRichtwertKennungComposite;
import org.polymap.kaps.model.data.EntwicklungsZusatzComposite;
import org.polymap.kaps.model.data.EntwicklungsZustandComposite;
import org.polymap.kaps.model.data.RichtwertzoneComposite;
import org.polymap.kaps.ui.KapsDefaultFormEditorPage;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class RichtwertzoneWeitereDatenFormEditorPage
        extends KapsDefaultFormEditorPage
        implements IFormEditorPage {

    private static Log log = LogFactory.getLog( RichtwertzoneWeitereDatenFormEditorPage.class );

    private final RichtwertzoneComposite richtwertzone;

    public RichtwertzoneWeitereDatenFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( RichtwertzoneWeitereDatenFormEditorPage.class.getName(), "weitere Daten", feature,
                featureStore );
        richtwertzone = repository.findEntity( RichtwertzoneComposite.class, feature
                .getIdentifier().getID() );
    }


    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );
        
        site.setEditorTitle( formattedTitle( "Richtwertzone", richtwertzone.schl().get(), null ) );
        site.setFormTitle( formattedTitle( "Richtwertzone", richtwertzone.schl().get(),
                getTitle() ) );
        
        Composite newLine, lastLine = null;

        newLine = newFormField( "Bodenrichtwertkennung" )
                .setProperty(
                        new AssociationAdapter<BodenRichtwertKennungComposite>(
                                "bodenrichtwertKennung", richtwertzone.bodenrichtwertKennung() ) )
                .setField( namedAssocationsPicklist( BodenRichtwertKennungComposite.class ) )
                .setLayoutData( left().top( lastLine ).create() ).create();

//        lastLine = newLine;
//        newLine = newFormField( "Basiskarte" )
//                .setProperty( new PropertyAdapter( richtwertzone.basisKarte() ) )
//                .setField( new StringFormField() ).setLayoutData( left().top( lastLine ).create() )
//                .create();
//
//        newFormField( "Maßstab     1:" )
//                .setProperty( new PropertyAdapter( richtwertzone.massstab() ) )
//                .setField( new StringFormField() )
//                .setValidator( new NumberValidator( Integer.class, locale ) )
//                .setLayoutData( right().top( lastLine ).create() ).create();
//
//        lastLine = newLine;
//        newLine = newFormField( "Rechtswert" )
//                .setProperty( new PropertyAdapter( richtwertzone.rechtsWert() ) )
//                .setField( new StringFormField() )
//                .setValidator( new NumberValidator( Double.class, locale ) )
//                .setLayoutData( left().top( lastLine ).create() ).create();
//
//        newFormField( "Hochwert" ).setProperty( new PropertyAdapter( richtwertzone.hochWert() ) )
//                .setField( new StringFormField() )
//                .setValidator( new NumberValidator( Double.class, locale ) )
//                .setLayoutData( right().top( lastLine ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( "Entwicklungszustand" )
                .setProperty(
                        new AssociationAdapter<EntwicklungsZustandComposite>(
                                "entwicklungsZustand", richtwertzone.entwicklungsZustand() ) )
                .setField( namedAssocationsPicklist( EntwicklungsZustandComposite.class ) )
                .setLayoutData( left().top( lastLine ).create() ).create();

        newFormField( "Entwicklungszusatz" )
                .setProperty(
                        new AssociationAdapter<EntwicklungsZusatzComposite>( "entwicklungsZusatz",
                                richtwertzone.entwicklungsZusatz() ) )
                .setField( namedAssocationsPicklist( EntwicklungsZusatzComposite.class ) )
                .setLayoutData( right().top( lastLine ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( "Bauweise" )
                .setProperty(
                        new AssociationAdapter<BauweiseComposite>( "bauweise", richtwertzone
                                .bauweise() ) )
                .setField( namedAssocationsPicklist( BauweiseComposite.class ) )
                .setLayoutData( left().top( lastLine ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( "Grundstücksgröße in qm" )
                .setProperty( new PropertyAdapter( richtwertzone.grundstuecksGroesse() ) )
                .setField( new StringFormField() )
                .setValidator( new NumberValidator( Double.class, locale, 12, 2, 1, 2 ) )
                .setLayoutData( left().top( lastLine ).create() ).create();

        newFormField( "Grundstückstiefe in m" )
                .setProperty( new PropertyAdapter( richtwertzone.grundstuecksTiefe() ) )
                .setField( new StringFormField() )
                .setValidator( new NumberValidator( Double.class, locale, 12, 2, 1, 2 ) )
                .setLayoutData( right().top( lastLine ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( "Grundstücksbreite in m" )
                .setProperty( new PropertyAdapter( richtwertzone.grundstuecksBreite() ) )
                .setField( new StringFormField() )
                .setValidator( new NumberValidator( Double.class, locale, 12, 2, 1, 2 ) )
                .setLayoutData( left().top( lastLine ).create() ).create();

        newFormField( "Grundflächenzahl" )
                .setProperty( new PropertyAdapter( richtwertzone.grundflaechenZahl() ) )
                .setField( new StringFormField() )
                .setValidator( new NumberValidator( Double.class, locale, 12, 2, 1, 2 ) )
                .setLayoutData( right().top( lastLine ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( "Geschoßzahl" )
                .setProperty( new PropertyAdapter( richtwertzone.geschossZahl() ) )
                .setField( new StringFormField() )
                .setValidator( new NumberValidator( Double.class, locale, 12, 2, 1, 2 ) )
                .setLayoutData( left().top( lastLine ).create() ).create();

        newFormField( "Geschoßflächenzahl" )
                .setProperty( new PropertyAdapter( richtwertzone.geschossFlaechenZahl() ) )
                .setField( new StringFormField() ).setLayoutData( right().top( lastLine ).create() )
                .create();

        lastLine = newLine;
        newLine = newFormField( "Baumassenzahl" )
                .setProperty( new PropertyAdapter( richtwertzone.baumassenZahl() ) )
                .setField( new StringFormField() )
                .setValidator( new NumberValidator( Double.class, locale, 12, 2, 1, 2 ) )
                .setLayoutData( left().top( lastLine ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( "Ackerzahl" )
                .setProperty( new PropertyAdapter( richtwertzone.ackerZahl() ) )
                .setField( new StringFormField() )
                .setValidator( new NumberValidator( Integer.class, locale ) )
                .setLayoutData( left().top( lastLine ).create() ).create();

        newFormField( "Grünlandzahl" )
                .setProperty( new PropertyAdapter( richtwertzone.gruenLandZahl() ) )
                .setField( new StringFormField() )
                .setValidator( new NumberValidator( Integer.class, locale ) )
                .setLayoutData( right().top( lastLine ).create() ).create();

    }
}
