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

import org.qi4j.api.query.QueryExpressions;

import org.eclipse.swt.widgets.Composite;

import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.data.NHK2010BaupreisIndexComposite;
import org.polymap.kaps.ui.KapsDefaultFormEditorPage;
import org.polymap.kaps.ui.MyNumberValidator;
import org.polymap.kaps.ui.NotNullMyNumberValidator;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class BaupreisIndexFormEditorPage
        extends KapsDefaultFormEditorPage {

    private static Log                      log = LogFactory.getLog( BaupreisIndexFormEditorPage.class );

    protected NHK2010BaupreisIndexComposite composite;

    private ZeitraumValidator               zeitraumValidator;


    public BaupreisIndexFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( BaupreisIndexFormEditorPage.class.getName(), "Grunddaten", feature, featureStore );

        composite = repository.findEntity( NHK2010BaupreisIndexComposite.class, feature.getIdentifier().getID() );
        zeitraumValidator = new ZeitraumValidator( composite );
    }


    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );
        site.setEditorTitle( formattedTitle( NHK2010BaupreisIndexComposite.NAME, composite.schl().get(), null ) );
        site.setFormTitle( formattedTitle( NHK2010BaupreisIndexComposite.NAME, composite.schl().get(), getTitle() ) );

        Composite newLine, lastLine = null;

        MyNumberValidator monatVonValidator = new NotNullMyNumberValidator( Integer.class ) {

            @Override
            public String validate( Object fieldValue ) {
                String ret = super.validate( fieldValue );
                if (ret == null) {
                    // kein Fehler
                    zeitraumValidator.monatVon = Integer.parseInt( (String)fieldValue );
                    if (zeitraumValidator.monatVon >= zeitraumValidator.monatBis) {
                        return "Monat von muss kleiner sein als Monat bis!";
                    }
                    if (zeitraumValidator.monatVon < 1 || zeitraumValidator.monatVon > 11) {
                        return "Monat von muss zwischen 1 und 11 liegen!";
                    }
                    if (!zeitraumValidator.revalidate()) {
                        return "In diesem Zeitraum liegen bereits Werte vor!";
                    }
                }
                return ret;
            }
        };
        MyNumberValidator monatBisValidator = new NotNullMyNumberValidator( Integer.class ) {

            @Override
            public String validate( Object fieldValue ) {
                String ret = super.validate( fieldValue );
                if (ret == null) {
                    // kein Fehler
                    zeitraumValidator.monatBis = Integer.parseInt( (String)fieldValue );
                    if (zeitraumValidator.monatBis <= zeitraumValidator.monatVon) {
                        return "Monat bis muss größer sein als Monat von!";
                    }
                    if (zeitraumValidator.monatBis < 2 || zeitraumValidator.monatBis > 12) {
                        return "Monat bis muss zwischen 2 und 12 liegen!";
                    }
                    if (!zeitraumValidator.revalidate()) {
                        return "In diesem Zeitraum liegen bereits Werte vor!";
                    }
                }
                return ret;
            }
        };
        newLine = newFormField( "Jahr" ).setProperty( new PropertyAdapter( composite.jahr() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NotNullMyNumberValidator( Integer.class ) )
                .setLayoutData( left().top( lastLine ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( "Monat von" ).setProperty( new PropertyAdapter( composite.monatVon() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ).setValidator( monatVonValidator )
                .setLayoutData( left().top( lastLine ).create() ).create();

        newFormField( "Monat bis" ).setProperty( new PropertyAdapter( composite.monatBis() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) ).setValidator( monatBisValidator )
                .setLayoutData( right().top( lastLine ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( "Gesamt" ).setToolTipText( "Index Insgesamt" )
                .setProperty( new PropertyAdapter( composite.wohneigentum() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NotNullMyNumberValidator( Double.class, 1 ) )
                .setLayoutData( left().top( lastLine ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( "Einfamiliengebäude" ).setToolTipText( "Index Einfamiliengebäude" )
                .setProperty( new PropertyAdapter( composite.einfamilienGebaeude() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NotNullMyNumberValidator( Double.class, 1 ) )
                .setLayoutData( left().top( lastLine ).create() ).create();

        newLine = newFormField( "Mehrfamiliengebäude" ).setToolTipText( "Index Mehrfamiliengebäude" )
                .setProperty( new PropertyAdapter( composite.mehrfamilienGebaeude() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NotNullMyNumberValidator( Double.class, 1 ) )
                .setLayoutData( right().top( lastLine ).create() ).create();

        lastLine = newLine;
        newLine = newFormField( "Bürogebäude" ).setToolTipText( "Index Bürogebäude" )
                .setProperty( new PropertyAdapter( composite.bueroGebaeude() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NotNullMyNumberValidator( Double.class, 1 ) )
                .setLayoutData( left().top( lastLine ).create() ).create();

        newLine = newFormField( "Gewerbe" ).setToolTipText( "Index gewerblicher Betrieb" )
                .setProperty( new PropertyAdapter( composite.gewerbeBetrieb() ) )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator( new NotNullMyNumberValidator( Double.class, 1 ) )
                .setLayoutData( right().top( lastLine ).create() ).create();

        site.addFieldListener( zeitraumValidator );
    }


    public static class ZeitraumValidator
            implements IFormFieldListener {

        Integer                               jahr     = null;

        Integer                               monatVon = null;

        Integer                               monatBis = null;

        private NHK2010BaupreisIndexComposite composite;


        public ZeitraumValidator( NHK2010BaupreisIndexComposite composite ) {
            this.composite = composite;
            jahr = composite.jahr().get();
            monatVon = composite.monatVon().get();
            monatBis = composite.monatBis().get();
        }


        @Override
        public void fieldChange( FormFieldEvent ev ) {
            if (ev.getEventCode() == IFormFieldListener.VALUE_CHANGE) {
                if (ev.getFieldName().equals( composite.jahr().qualifiedName().name() )) {
                    jahr = (Integer)ev.getNewValue();
                    revalidate();
                }
                else if (ev.getFieldName().equals( composite.monatVon().qualifiedName().name() )) {
                    monatVon = (Integer)ev.getNewValue();
                    revalidate();
                }
                else if (ev.getFieldName().equals( composite.monatBis().qualifiedName().name() )) {
                    monatBis = (Integer)ev.getNewValue();
                    revalidate();
                }
            }
        }


        public boolean revalidate() {
            if (jahr != null && monatVon != null && monatBis != null) {
                if (monatVon < monatBis) {
                    NHK2010BaupreisIndexComposite template = QueryExpressions
                            .templateFor( NHK2010BaupreisIndexComposite.class );
                    KapsRepository repo = KapsRepository.instance();
                    for (NHK2010BaupreisIndexComposite index : repo.findEntities( NHK2010BaupreisIndexComposite.class,
                            QueryExpressions.eq( template.jahr(), jahr ), 0, 100 )) {

                        Integer monatBisI = index.monatBis().get();
                        Integer monatVonI = index.monatVon().get();
                        if (monatVon > monatBisI || monatBis < monatVonI) {
                            // Zeitraum außerhalb, alles ok
                        }
                        else if (!index.equals( composite )) {
                            return false;
                        }
                    }
                }
                else {
                    return false;
                }
            }
            return true;
        }
    }
}
