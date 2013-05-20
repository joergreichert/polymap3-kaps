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

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;

import org.polymap.core.runtime.Polymap;

import org.polymap.rhei.data.entityfeature.AssociationAdapter;
import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.CheckboxFormField;
import org.polymap.rhei.field.NumberValidator;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.data.BodennutzungComposite;
import org.polymap.kaps.model.data.ErschliessungsBeitragComposite;
import org.polymap.kaps.model.data.FlurstueckComposite;
import org.polymap.kaps.model.data.GebaeudeArtComposite;
import org.polymap.kaps.model.data.GemeindeComposite;
import org.polymap.kaps.model.data.RichtwertzoneComposite;
import org.polymap.kaps.model.data.RichtwertzoneZeitraumComposite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class FlurstuecksdatenBaulandGrunddatenFormEditorPage
        extends FlurstuecksdatenBaulandFormEditorPage {

    private static Log log = LogFactory.getLog( FlurstuecksdatenBaulandGrunddatenFormEditorPage.class );


    // private IFormFieldListener gemeindeListener;

    public FlurstuecksdatenBaulandGrunddatenFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( FlurstuecksdatenBaulandGrunddatenFormEditorPage.class.getName(), "Grunddaten", feature,
                featureStore );
    }


    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );

        Composite newLine, lastLine = null;
        Composite parent = pageSite.getPageBody();

        parent = newSection( parent, "Richtwertberechnung" );
 
        newLine = newFormField( "Lageklasse" ).setProperty( new PropertyAdapter( vb.lageklasse() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 2, 1, 2 ) )
                .setLayoutData( left().right( 33 ).top( lastLine ).create() ).setParent( parent )
                .create();
        newFormField( "zul. GFZ" ).setProperty( new PropertyAdapter( vb.gfz() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 2, 1, 2 ) )
                .setLayoutData( left().left( 33 ).right( 66 ).top( lastLine ).create() )
                .setParent( parent ).create();
        newFormField( "zul. Vollgeschosse" )
                .setProperty( new PropertyAdapter( vb.zulaessigeVollgeschosse() ) )
                .setField( new StringFormField() )
                .setLayoutData( right().left( 66 ).top( lastLine ).create() ).setParent( parent )
                .create();

        lastLine = newLine;
        newLine = newFormField( "Gebäudeart" )
                .setEnabled( false )
                .setProperty(
                        new AssociationAdapter<GebaeudeArtComposite>( "gebaeudeArt", vb
                                .gebaeudeArt() ) )
                .setField( namedAssocationsPicklist( GebaeudeArtComposite.class ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( parent ).create();

        lastLine = newLine;
        newLine = newFormField( "Bodennutzung" )
                .setProperty(
                        new AssociationAdapter<BodennutzungComposite>( "bodennutzung", vb
                                .bodennutzung() ) )
                .setField( namedAssocationsPicklist( BodennutzungComposite.class ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( parent ).create();

        newFormField( "Erschließungsbeitrag" )
                .setProperty(
                        new AssociationAdapter<ErschliessungsBeitragComposite>(
                                "erschliessungsBeitrag", vb.erschliessungsBeitrag() ) )
                .setField( namedAssocationsPicklist( ErschliessungsBeitragComposite.class ) )
                .setLayoutData( right().top( lastLine ).create() ).setParent( parent ).create();

        lastLine = newLine;
        newLine = newFormField( "Baujahr tatsächlich" )
                .setProperty( new PropertyAdapter( vb.baujahr() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Integer.class, Polymap.getSessionLocale() ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( parent ).create();
        newFormField( "Baujahr bereinigt" )
                .setProperty( new PropertyAdapter( vb.baujahrBereinigt() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Integer.class, Polymap.getSessionLocale() ) )
                .setLayoutData( right().top( lastLine ).create() ).setParent( parent ).create();

        lastLine = newLine;
        TreeMap<String, Object> zonen = new TreeMap<String, Object>();
        TreeMap<String, Object> zeitraeume = new TreeMap<String, Object>();
        FlurstueckComposite flurstueck = vb.flurstueck().get();
        RichtwertzoneComposite richtwertZone;
        if (flurstueck != null) {
            GemeindeComposite gemeinde = flurstueck.gemarkung().get().gemeinde().get();
            Iterable<RichtwertzoneComposite> iterable = RichtwertzoneComposite.Mixin
                    .findZoneIn( gemeinde );
            for (RichtwertzoneComposite zone : iterable) {
                String prefix = zone.schl().get();
                if (prefix.startsWith( "00" )) {
                    prefix = "*" + prefix;
                }
                zonen.put( prefix + " - " + zone.name().get(), zone );
            }

            richtwertZone = flurstueck.richtwertZone().get();
            if (richtwertZone != null) {
                for (RichtwertzoneZeitraumComposite zeitraum : RichtwertzoneZeitraumComposite.Mixin.forZone( richtwertZone )) {
                    zeitraeume.put( KapsRepository.SHORT_DATE.format( zeitraum.gueltigAb().get()), zeitraum );
                }
            }
        }
        
        newLine = newFormField( "Richtwertzone" )
                .setEnabled( false )
                .setProperty(
                        new AssociationAdapter<RichtwertzoneComposite>( "richtwertZone", vb
                                .richtwertZone() ) )
                .setField( new PicklistFormField( zonen.descendingMap() ) )
                .setLayoutData( left().top( lastLine ).create() ).setParent( parent ).create();
        
        newFormField( "Gültig ab" ).setProperty( new AssociationAdapter<RichtwertzoneZeitraumComposite>( "richtwertZoneG", vb.richtwertZoneG() ) )
                .setField( new PicklistFormField( zeitraeume.descendingMap() ) )
                .setLayoutData( right().top( lastLine ).create() ).setParent( parent ).create();

        parent = newSection( parent, "Bodenpreisberechnung" );
        lastLine = newLine;
        newLine = newFormField( "Richtwert" ).setEnabled( false )
                .setProperty( new PropertyAdapter( vb.richtwert() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 2, 1, 2 ) )
                .setLayoutData( left().left( 25 ).top( lastLine ).create() ).setParent( parent )
                .create();

        lastLine = newLine;
        newLine = newFormField( "GFZ-berein. Bodenpreis" ).setLabel( "GFZ bereinigter Bodenpreis" )
                .setEnabled( false )
                .setProperty( new PropertyAdapter( vb.gfzBereinigterBodenpreis() ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 2, 1, 2 ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setLayoutData( left().left( 25 ).top( lastLine ).create() ).setParent( parent )
                .create();
        newFormField( "verwenden?" ).setLabel( "GFZ bereinigten Bodenpreis verwenden?" )
                .setProperty( new PropertyAdapter( vb.gfzBereinigtenBodenpreisVerwenden() ) )
                .setField( new CheckboxFormField() )
                .setLayoutData( right().right( 75 ).top( lastLine ).create() ).setParent( parent )
                .create();
        newFormField( "GFZ-Bereich" ).setEnabled( false )
                .setProperty( new PropertyAdapter( vb.gfzBereich() ) )
                .setField( new StringFormField() )
                .setLayoutData( right().left( 75 ).top( lastLine ).create() ).setParent( parent )
                .create();

        lastLine = newLine;
        newLine = newFormField( "Richtwertbereinigung in €/m²" )
                .setLabel( "Richtwertbereinigung in €/m² (+/-)" )
                .setProperty( new PropertyAdapter( vb.richtwertBereinigung() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 2, 1, 2 ) )
                .setLayoutData( left().left( 25 ).top( lastLine ).create() ).setParent( parent )
                .create();
        newFormField( "Bemerkung" ).setLabel( "Bemerkung zur Richwertbereinigung" )
                .setProperty( new PropertyAdapter( vb.richtwertBereinigungBemerkung() ) )
                .setField( new StringFormField() ).setLayoutData( right().top( lastLine ).create() )
                .setParent( parent ).create();

        lastLine = newLine;
        newLine = newFormField( "Richtwertzuschlag in %" )
                .setProperty( new PropertyAdapter( vb.richtwertZuschlagProzent() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 2, 1, 2 ) )
                .setLayoutData( left().right( 25 ).top( lastLine ).create() ).setParent( parent )
                .create();
        // TODO refresher
        newLine = newFormField( "in €/m²" ).setEnabled( false )
                .setProperty( new PropertyAdapter( vb.richtwertZuschlagBerechnet() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 2, 1, 2 ) )
                .setLayoutData( left().left( 25 ).top( lastLine ).create() ).setParent( parent )
                .create();
        newFormField( "Bemerkung" ).setLabel( "Bemerkung zum Richtwertzuschlag" )
                .setProperty( new PropertyAdapter( vb.richtwertZuschlagBemerkung() ) )
                .setField( new StringFormField() ).setLayoutData( right().top( lastLine ).create() )
                .setParent( parent ).create();

        lastLine = newLine;
        newLine = newFormField( "Richtwertabschlag in %" )
                .setProperty( new PropertyAdapter( vb.richtwertAbschlagProzent() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 2, 1, 2 ) )
                .setLayoutData( left().right( 25 ).top( lastLine ).create() ).setParent( parent )
                .create();
        // TODO refresher
        newLine = newFormField( "in €/m²" ).setEnabled( false )
                .setProperty( new PropertyAdapter( vb.richtwertAbschlagBerechnet() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 2, 1, 2 ) )
                .setLayoutData( left().left( 25 ).top( lastLine ).create() ).setParent( parent )
                .create();
        newFormField( "Bemerkung" ).setLabel( "Bemerkung zum Richtwertabschlag" )
                .setProperty( new PropertyAdapter( vb.richtwertAbschlagBemerkung() ) )
                .setField( new StringFormField() ).setLayoutData( right().top( lastLine ).create() )
                .setParent( parent ).create();

        lastLine = newLine;
        newLine = newFormField( "Erschließungskosten in €/m²" )
                .setLabel( "Erschließungskosten in €/m² anrechenbarer Grundstücksgröße" )
                .setProperty( new PropertyAdapter( vb.erschliessungsKosten() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 2, 1, 2 ) )
                .setLayoutData( left().left( 25 ).top( lastLine ).create() ).setParent( parent )
                .create();

        lastLine = newLine;
        // TODO refresher
        newLine = newFormField( "Bodenpreis unbebaut in €/m²" ).setEnabled( false )
                .setProperty( new PropertyAdapter( vb.bodenpreisUnbebaut() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 2, 1, 2 ) )
                .setLayoutData( left().left( 25 ).top( lastLine ).create() ).setParent( parent )
                .create();

        lastLine = newLine;
        newLine = newFormField( "Beb.-abschlag in %" )
                .setProperty( new PropertyAdapter( vb.bebAbschlag() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 2, 1, 2 ) )
                .setLayoutData( left().right( 25 ).top( lastLine ).create() ).setParent( parent )
                .create();
        // TODO refresher
        newLine = newFormField( "in €/m²" ).setEnabled( false )
                .setProperty( new PropertyAdapter( vb.bebAbschlagBerechnet() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 2, 1, 2 ) )
                .setLayoutData( left().left( 25 ).top( lastLine ).create() ).setParent( parent )
                .create();

        lastLine = newLine;
        // TODO refresher
        newLine = newFormField( "Bodenpreis in €/m²" ).setEnabled( false )
                .setProperty( new PropertyAdapter( vb.bodenpreisBebaut() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, 2, 1, 2 ) )
                .setLayoutData( left().left( 25 ).top( lastLine ).create() ).setParent( parent )
                .create();

        // site.addFieldListener( gemeindeListener = new IFormFieldListener() {
        //
        // @Override
        // public void fieldChange( FormFieldEvent ev ) {
        // if (ev.getFieldName().equals( "gemeinde" )) {
        // GemeindeComposite gemeinde = (GemeindeComposite)ev.getNewValue();
        // lage.setEnabled( gemeinde != null && gemeinde.einwohner().get() > 50000 );
        // }
        // }
        // } );
    }
}
