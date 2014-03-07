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
import org.opengis.feature.type.PropertyDescriptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.property.Property;

import com.google.common.collect.Maps;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.action.Action;

import org.polymap.core.data.ui.featuretable.DefaultFeatureTableColumn;
import org.polymap.core.data.ui.featuretable.FeatureTableViewer;
import org.polymap.core.model.EntityType;

import org.polymap.rhei.data.entityfeature.AssociationAdapter;
import org.polymap.rhei.data.entityfeature.ManyAssociationAdapter;
import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.data.entityfeature.PropertyDescriptorAdapter;
import org.polymap.rhei.data.entityfeature.ReloadablePropertyAdapter;
import org.polymap.rhei.data.entityfeature.ReloadablePropertyAdapter.PropertyCallback;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.form.FormEditor;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.KapsPlugin;
import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.data.BelastungComposite;
import org.polymap.kaps.model.data.FlurComposite;
import org.polymap.kaps.model.data.FlurstueckComposite;
import org.polymap.kaps.model.data.GemarkungComposite;
import org.polymap.kaps.model.data.GemeindeComposite;
import org.polymap.kaps.model.data.NutzungComposite;
import org.polymap.kaps.model.data.RichtwertzoneComposite;
import org.polymap.kaps.model.data.StrasseComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.ui.ActionButton;
import org.polymap.kaps.ui.BooleanFormField;
import org.polymap.kaps.ui.KapsDefaultFormEditorPage;
import org.polymap.kaps.ui.ListNotNullValidator;
import org.polymap.kaps.ui.NotNullMyNumberValidator;
import org.polymap.kaps.ui.NotNullValidator;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class FlurstueckeFormEditorPage
        extends KapsDefaultFormEditorPage {

    private static Log          log = LogFactory.getLog( FlurstueckeFormEditorPage.class );

    private FlurstueckComposite flurstueck;

    private GemarkungComposite  selectedGemarkung;

    private IFormFieldListener  gemarkungListener;


    public FlurstueckeFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( FlurstueckeFormEditorPage.class.getName(), "Grunddaten", feature, featureStore );

        flurstueck = repository.findEntity( FlurstueckComposite.class, feature.getIdentifier().getID() );
        selectedGemarkung = flurstueck != null ? flurstueck.gemarkung().get() : null;
    }


    @Override
    public void createFormContent( IFormEditorPageSite site ) {
        super.createFormContent( site );

        String nummer = flurstueck.name().get() != null ? flurstueck.name().get() : "neu";

        site.setEditorTitle( formattedTitle( "Flurstück", nummer, null ) );
        site.setFormTitle( formattedTitle( "Flurstück", nummer, getTitle() ) );

        Composite parent = site.getPageBody();
        Control form = createFlurstueckForm( parent );
    }


    public Control createFlurstueckForm( Composite parent ) {

        Control lastLine, newLine = null;


        final VertragComposite vertrag = flurstueck != null ? flurstueck.vertrag()
                .get() : null;
        String label = vertrag == null ? "Kein Vertrag zugewiesen" : "Vertrag "
                + EingangsNummerFormatter.format( vertrag.eingangsNr().get() ) + " öffnen";
        ActionButton openErweiterteDaten = new ActionButton( parent, new Action( label ) {

            @Override
            public void run() {
                FormEditor vertragEditor = KapsPlugin.openEditor( fs, VertragComposite.NAME, vertrag );
                vertragEditor.setActivePage( KaufvertragFlurstueckeFormEditorPage.class.getName() );
            }
        } );
        openErweiterteDaten.setLayoutData( left().left( ONE ).right( TWO ).height( 25 ).create() );
        openErweiterteDaten.setEnabled( vertrag != null );
        newLine = openErweiterteDaten;
        
        
        lastLine = newLine;
        newLine = newFormField( "Gemarkung" ).setParent( parent )
                .setProperty( new AssociationAdapter<GemarkungComposite>( flurstueck.gemarkung() ) )
                .setField( namedAssocationsPicklist( GemarkungComposite.class, true ) ).setEnabled( false )
                .setValidator( new NotNullValidator() ).setLayoutData( left().top( lastLine ).create() ).create();

        newFormField( "Flurstücksnummer" ).setParent( parent )
                .setProperty( new PropertyAdapter( flurstueck.hauptNummer() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ).setEnabled( false )
                .setLayoutData( right().right( 75 ).top( lastLine ).create() )
                .setValidator( new NotNullMyNumberValidator( Integer.class ) ).create();

        newFormField( "Unternummer" ).setParent( parent ).setProperty( new PropertyAdapter( flurstueck.unterNummer() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ).setEnabled( false )
                .setValidator( new NotNullValidator() ).setLayoutData( right().left( 75 ).top( lastLine ).create() )
                .create();

        lastLine = newLine;
        final PicklistFormField strassePickList = new PicklistFormField( new PicklistFormField.ValueProvider() {

            @Override
            public SortedMap<String, Object> get() {
                SortedMap<String, Object> strassen = new TreeMap<String, Object>();

                if (selectedGemarkung != null) {
                    GemeindeComposite gemeinde = selectedGemarkung.gemeinde().get();
                    Iterable<StrasseComposite> iterable = StrasseComposite.Mixin.findStrasseIn( gemeinde );
                    for (StrasseComposite strasse : iterable) {
                        strassen.put( strasse.name().get(), strasse );
                    }
                }

                return strassen;
            }
        } );

        newLine = newFormField( "Straße/Gewann" ).setParent( parent )
                .setProperty( new AssociationAdapter<StrasseComposite>( flurstueck.strasse() ) )
                .setField( strassePickList ).setValidator( new NotNullValidator() ).setEnabled( false )
                .setLayoutData( left().top( lastLine ).create() ).create();

        newFormField( "Hausnummer" ).setParent( parent ).setProperty( new PropertyAdapter( flurstueck.hausnummer() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ).setEnabled( false )
                .setValidator( new NotNullValidator() ).setLayoutData( right().right( 75 ).top( lastLine ).create() )
                .create();

        newFormField( "Zusatz" ).setToolTipText( "Hausnummernzusatz" ).setParent( parent )
                .setProperty( new PropertyAdapter( flurstueck.hausnummerZusatz() ) ).setField( new StringFormField() )
                .setEnabled( false ).setLayoutData( right().left( 75 ).top( lastLine ).create() ).create();

        lastLine = newLine;
        final PicklistFormField richtwertZonePickList = new PicklistFormField( new PicklistFormField.ValueProvider() {

            @Override
            public SortedMap<String, Object> get() {
                if (selectedGemarkung != null) {
                    return RichtwertzoneProvider.findFor( selectedGemarkung.gemeinde().get() );
                }
                return Maps.newTreeMap();
            }
        } );

        newLine = newFormField( "Richtwertzone" ).setParent( parent )
                .setProperty( new AssociationAdapter<RichtwertzoneComposite>( flurstueck.richtwertZone() ) )
                .setField( richtwertZonePickList ).setEnabled( false ).setValidator( new NotNullValidator() )
                .setLayoutData( left().top( lastLine ).create() ).create();

        pageSite.addFieldListener( gemarkungListener = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == VALUE_CHANGE && ev.getFieldName().equalsIgnoreCase( "gemarkung" )) {
                    if ((ev.getNewValue() == null && selectedGemarkung != null)
                            || (ev.getNewValue() != null && !ev.getNewValue().equals( selectedGemarkung ))) {
                        selectedGemarkung = ev.getNewValue();
                        strassePickList.reloadValues();
                        pageSite.setFieldValue( "strasse", flurstueck.strasse().get() );
                        richtwertZonePickList.reloadValues();
                        pageSite.setFieldValue( "richtwertZone", flurstueck.richtwertZone().get() );
                    }
                }
            }
        } );

        lastLine = newLine;
        newLine = newFormField( "Nutzung" ).setParent( parent )
                .setProperty( new AssociationAdapter<NutzungComposite>( flurstueck.nutzung() ) )
                .setField( namedAssocationsPicklist( NutzungComposite.class ) ).setEnabled( false )
                .setValidator( new NotNullValidator() ).setLayoutData( left().top( lastLine ).create() ).create();

        newFormField( "Belastungen" ).setParent( parent )
                .setProperty( new ManyAssociationAdapter<BelastungComposite>( flurstueck.belastungen() ) ).setField( namedAssocationsSelectlist( BelastungComposite.class, true ) ).setEnabled(false)
                .setValidator( new ListNotNullValidator() )
                .setLayoutData( right().top( lastLine ).height( 50 ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( "Fläche in m²" )
                .setParent( parent )
                .setProperty(new PropertyAdapter(flurstueck.flaeche()) ).setField(  new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ).setEnabled( false )
                .setValidator( new NotNullMyNumberValidator( Double.class, 2 ) )
                .setLayoutData( left().top( lastLine ).create() ).create();

        newFormField( "Erbbaurecht" )
                .setProperty( new PropertyAdapter(flurstueck.erbbaurecht()) ).setField( new BooleanFormField() ).setEnabled( false )
                .setValidator( new NotNullValidator() ).setLayoutData( right().top( lastLine ).create() )
                .setParent( parent ).create();
        
        return newLine;
    }
}