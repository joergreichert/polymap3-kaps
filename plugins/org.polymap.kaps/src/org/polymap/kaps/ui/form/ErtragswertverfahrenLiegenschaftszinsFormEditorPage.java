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

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.form.FormEditor;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.ui.FieldCalculation;
import org.polymap.kaps.ui.FieldListener;
import org.polymap.kaps.ui.NumberFormatter;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class ErtragswertverfahrenLiegenschaftszinsFormEditorPage
        extends ErtragswertverfahrenFormEditorPage {

    private static Log       log = LogFactory.getLog( ErtragswertverfahrenLiegenschaftszinsFormEditorPage.class );

    private FieldListener    fieldListener;

    private FieldCalculation liziListener;


    // private IFormFieldListener gemeindeListener;

    public ErtragswertverfahrenLiegenschaftszinsFormEditorPage( FormEditor formEditor, Feature feature,
            FeatureStore featureStore ) {
        super( ErtragswertverfahrenLiegenschaftszinsFormEditorPage.class.getName(), "Liegenschaftszins", feature,
                featureStore );

        EventManager.instance().subscribe(
                fieldListener = new FieldListener( vb.bereinigterKaufpreis(), vb.jahresReinErtrag(), vb.ertragswert(),
                        vb.bodenwertAnteil(), vb.restnutzungsDauer() ), new FieldListener.EventFilter( formEditor ) );

        // EventManager.instance().subscribe(
        // editorListener = new InterEditorListener( vb.bereinigtesBaujahr(),
        // vb.bodenwertAnteil(),
        // vb.bereinigterKaufpreis() ) {
        //
        // @Override
        // protected void onChangedValue( IFormEditorPageSite site, Entity entity,
        // String fieldName,
        // Object value ) {
        // if (fieldName.equals( vb.bereinigtesBaujahr().qualifiedName().name() )) {
        // site.setFieldValue( fieldName, value != null ?
        // NumberFormatter.getFormatter( 0, false ).format( value ) : null );
        // }
        // else if (fieldName.equals( vb.bodenwertAnteil().qualifiedName().name() ))
        // {
        // site.setFieldValue( fieldName, value != null ?
        // NumberFormatter.getFormatter( 2 ).format( value ) : null );
        // }
        // else if (fieldName.equals(
        // vb.bereinigterKaufpreis().qualifiedName().name() )) {
        // site.setFieldValue( fieldName, value != null ?
        // NumberFormatter.getFormatter( 2 ).format( value ) : null );
        // }
        // }
        // }, new InterEditorListener.EventFilter( vb ) );
    }


    @Override
    public void dispose() {
        super.dispose();
        // EventManager.instance().unsubscribe( editorListener );
        EventManager.instance().unsubscribe( fieldListener );
    }


    @Override
    public void afterDoLoad( IProgressMonitor monitor )
            throws Exception {
        fieldListener.flush( pageSite );
        // editorListener.flush( pageSite );
    }


    @SuppressWarnings("unchecked")
    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );

        Control newLine, lastLine = null;
        Composite parent = pageSite.getPageBody();

        // Section section = newSection( parent, "Bodenwertaufteilung" );
        // Composite client = (Composite)section.getClient();
        Composite client = parent;

        newLine = createLabel( client, "Anteil des Gewerbes in %", one().top( lastLine, 20 ), SWT.RIGHT );
        createFlaecheField( vb.anteilDesGewerbes(), two().top( lastLine, 20 ), client, true );

        lastLine = newLine;
        newLine = createLabel( client, "Art des Gewerbes", one().top( lastLine ), SWT.RIGHT );
        createTextField( vb.artDesGewerbes(), two().right( 75 ).top( lastLine ), client );

        lastLine = newLine;
        newLine = createLabel( client, "Grundstückart", one().top( lastLine ), SWT.RIGHT );
        createFlaecheField( vb.grundstuecksArt(), two().top( lastLine ), client, true );

        lastLine = newLine;
        newLine = createLabel( client, "Liegenschaftszins", one().top( lastLine ), SWT.RIGHT );
        createPreisField( vb.liegenschaftsZins(), two().top( lastLine ), client, false );

        lastLine = newLine;
        newLine = createLabel( client, "Gewichtung", one().top( lastLine ), SWT.RIGHT );
        createFlaecheField( vb.gewichtungLiegenschaftszins(), two().top( lastLine ), client, false );
        site.addFieldListener( liziListener = new FieldCalculation( site, 2, vb.liegenschaftsZins(), vb
                .bereinigterKaufpreis(), vb.jahresReinErtrag(), vb.ertragswert(), vb.bodenwertAnteil(), vb
                .restnutzungsDauer() ) {

            @Override
            protected Double calculate( ValueProvider values ) {
                Double re = values.get( vb.jahresReinErtrag() );
                Double kp = values.get( vb.bereinigterKaufpreis() );
                Double ertragsWert = values.get( vb.ertragswert() );
                Double bodenWert = values.get( vb.bodenwertAnteil() );
                Double n = values.get( vb.restnutzungsDauer() );
                if (ertragsWert != null && bodenWert != null && re != null && kp != null && kp.doubleValue() != 0
                        && n != null) {
                    Double g = ertragsWert - bodenWert;
                    Double f1 = re / kp;
                    Double f2 = g / kp;

                    // vorher
                    Double liziV = f1;
                    // neu
                    Double liziN = f1 - (liziV / (Math.pow( 1 + liziV, n ) - 1) * f2);

                    int iteration = 1;
                    while (Math.abs( liziN - liziV ) > 0.005d) {
                        liziV = liziN;
                        liziN = f1 - (liziV / (Math.pow( 1 + liziV, n ) - 1) * f2);
                        iteration++;
                    }

                    site.setFieldValue( vb.gewichtungLiegenschaftszins().qualifiedName().name(), NumberFormatter
                            .getFormatter( 0 ).format( iteration ) );
                    // in % umrechnen
                    return liziN * 100;
                }
                return null;
            }
        } );

        lastLine = newLine;
        newLine = createLabel( client, "LiZi verwenden?", "Liegenschaftszins für wertrelevante Daten verwenden?", one()
                .top( lastLine ), SWT.RIGHT );
        createBooleanField( vb.liziVerwenden(), two().top( lastLine ), client );
    }


    @Override
    protected SimpleFormData one() {
        return new SimpleFormData( SPACING ).left( 0 ).right( 45 );
    }


    @Override
    protected SimpleFormData two() {
        return new SimpleFormData( SPACING ).left( 45 ).right( 60 );
    }
}
