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
package org.polymap.kaps.ui;

import java.util.ArrayList;
import java.util.List;

import org.opengis.feature.Feature;

import org.polymap.rhei.form.FormEditor;
import org.polymap.rhei.form.IFormEditorPage;
import org.polymap.rhei.form.IFormPageProvider;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.data.*;
import org.polymap.kaps.ui.form.*;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class FormPageProvider
        implements IFormPageProvider {

    @Override
    public List<IFormEditorPage> addPages( FormEditor formEditor, Feature feature ) {
        List<IFormEditorPage> result = new ArrayList<IFormEditorPage>();
        String name = feature.getType().getName().getLocalPart();

        if (name.equalsIgnoreCase( VertragComposite.NAME )) {
            result.add( new Kaufvertrag1FormEditorPage( feature, formEditor.getFeatureStore() ) );
            result.add( new Kaufvertrag2FormEditorPage( feature, formEditor.getFeatureStore() ) );
            result.add( new KaufvertragFlurstueckeFormEditorPage( formEditor, feature, formEditor.getFeatureStore() ) );
            result.add( new KaufvertragErweitertFormEditorPage( formEditor, feature, formEditor.getFeatureStore() ) );
        }
        else if (name.equalsIgnoreCase( FlurstueckComposite.NAME )) {
            result.add( new DontUseThisFormEditorPage( "Bitte benutzen Sie zum Anlegen und Bearbeiten von Flurstücken das Formular in Verträge.", feature, formEditor.getFeatureStore() ));
        }
        else if (name.equalsIgnoreCase( RichtwertzoneComposite.NAME )) {
            result.add( new RichtwertzoneGrunddatenFormEditorPage( feature, formEditor.getFeatureStore() ) );
            result.add( new RichtwertzoneWeitereDatenFormEditorPage( feature, formEditor.getFeatureStore() ) );
        }
        else if (name.equalsIgnoreCase( RichtwertzoneZeitraumComposite.NAME )) {
            result.add( new RichtwertzoneZeitraumGrunddatenFormEditorPage( feature, formEditor.getFeatureStore() ) );
            result.add( new RichtwertzoneZeitraumWeitereDatenFormEditorPage( feature, formEditor.getFeatureStore() ) );
        }
        else if (name.equalsIgnoreCase( FlurstuecksdatenBaulandComposite.NAME )) {
            result.add( new FlurstuecksdatenBaulandGrunddatenFormEditorPage( feature, formEditor.getFeatureStore() ) );
            result.add( new FlurstuecksdatenBaulandBodenwertFormEditorPage( formEditor, feature, formEditor
                    .getFeatureStore() ) );
            result.add( new FlurstuecksdatenBaulandRichtwertFormEditorPage( formEditor, feature, formEditor
                    .getFeatureStore() ) );
            result.add( new FlurstuecksdatenBaulandSonstigesFormEditorPage( feature, formEditor.getFeatureStore() ) );
        }
        else if (name.equalsIgnoreCase( FlurstuecksdatenAgrarComposite.NAME )) {
            result.add( new FlurstuecksdatenAgrarGrunddatenFormEditorPage( formEditor, feature, formEditor
                    .getFeatureStore() ) );
            result.add( new FlurstuecksdatenAgrarBodenwertFormEditorPage( formEditor, feature, formEditor
                    .getFeatureStore() ) );
        }
        else if (name.equalsIgnoreCase( WohnungseigentumComposite.NAME )) {
            result.add( new WohnungseigentumFormEditorPage( feature, formEditor.getFeatureStore() ) );
        }
        else if (name.equalsIgnoreCase( AusstattungBewertungComposite.NAME )) {
            result.add( new BewertungAnhandVonAustattungsmerkmalenFormEditorPage( formEditor, feature, formEditor
                    .getFeatureStore() ) );
        }
        else if (name.equalsIgnoreCase( ErmittlungModernisierungsgradComposite.NAME )) {
            result.add( new ErmittlungModernisierungsgradFormEditorPage( formEditor, feature, formEditor
                    .getFeatureStore() ) );
        }
        else if (name.equalsIgnoreCase( ErtragswertverfahrenComposite.NAME )) {
            result.add( new ErtragswertverfahrenBetriebskostenFormEditorPage( feature, formEditor.getFeatureStore() ) );
            result.add( new ErtragswertverfahrenErtraegeFormEditorPage( formEditor, feature, formEditor
                    .getFeatureStore() ) );
            result.add( new ErtragswertverfahrenBewirtschaftungskostenFormEditorPage( formEditor, feature, formEditor
                    .getFeatureStore() ) );
            result.add( new ErtragswertverfahrenErtragswertFormEditorPage( formEditor, feature, formEditor
                    .getFeatureStore() ) );
            result.add( new ErtragswertverfahrenLiegenschaftszinsFormEditorPage( formEditor, feature, formEditor
                    .getFeatureStore() ) );
        }
        else if (name.equalsIgnoreCase( GebaeudeComposite.NAME )) {
            result.add( new GebaeudeGrunddatenFormEditorPage( feature, formEditor.getFeatureStore() ) );
            result.add( new GebaeudeFlurstueckeFormEditorPage( feature, formEditor.getFeatureStore() ) );
        }
        else if (name.equalsIgnoreCase( WohnungComposite.NAME )) {
            result.add( new WohnungGrunddatenFormEditorPage( formEditor, feature, formEditor.getFeatureStore() ) );
            result.add( new WohnungVertragsdatenFormEditorPage( formEditor, feature, formEditor.getFeatureStore() ) );
            result.add( new WohnungLiegenschaftzinsFormEditorPage( formEditor, feature, formEditor.getFeatureStore() ) );
        }
        else if (name.equalsIgnoreCase( GebaeudeArtComposite.NAME )) {
            result.add( new DefaultEntityFormEditorPage( feature, formEditor.getFeatureStore(),
                    GebaeudeArtComposite.class, KapsRepository.instance(), GebaeudeArtComposite.NAME ) {

                @Override
                protected String labelFor( String name ) {
                    if ("gebaeudeArtStabu".equals( name )) {
                        return "Gebäudeart";
                    }
                    return super.labelFor( name );
                }


                @Override
                protected String tooltipFor( String name ) {
                    if ("gebaeudeArtStabu".equals( name )) {
                        return "Gebäudeart entsprechend Statistischem Bundesamt";
                    }
                    return super.tooltipFor( name );
                }
            } );
        }
        else if (name.equalsIgnoreCase( GemarkungComposite.NAME )) {
            result.add( new DefaultEntityFormEditorPage( feature, formEditor.getFeatureStore(),
                    GemarkungComposite.class, KapsRepository.instance(), GemarkungComposite.NAME ) );
        }
        else if (name.equalsIgnoreCase( GemeindeComposite.NAME )) {
            result.add( new GemeindeFormEditorPage( feature, formEditor.getFeatureStore() ) );
        }
        else if (name.equalsIgnoreCase( BodennutzungComposite.NAME )) {
            result.add( new DefaultEntityFormEditorPage( feature, formEditor.getFeatureStore(),
                    BodennutzungComposite.class, KapsRepository.instance(), BodennutzungComposite.NAME ) );
        }
        else if (name.equalsIgnoreCase( NutzungComposite.NAME )) {
            result.add( new DefaultEntityFormEditorPage( feature, formEditor.getFeatureStore(), NutzungComposite.class,
                    KapsRepository.instance(), NutzungComposite.NAME ) {

                @Override
                protected String labelFor( String name ) {
                    if ("name".equals( name )) {
                        return "Bezeichnung";
                    }
                    if ("isAgrar".equals( name )) {
                        return "Ist Agrarland?";
                    }
                    if ("isWohneigentum".equals( name )) {
                        return "Ist Wohneigentum?";
                    }
                    if ("stala".equals( name )) {
                        return "STALA";
                    }
                    if ("artDerBauflaeche".equals( name )) {
                        return "STABU";
                    }
                    return super.labelFor( name );
                }


                @Override
                protected String tooltipFor( String name ) {
                    if ("artDerBauflaeche".equals( name )) {
                        return "Schlüssel Statistisches Bundesamt - Art der Baufläche";
                    }
                    if ("stala".equals( name )) {
                        return "Schlüssel Statistisches Landesamt - Art des Grundstücks";
                    }
                    return super.labelFor( name );
                }
            } );
        }
        else if (name.equalsIgnoreCase( VertragsArtComposite.NAME )) {
            result.add( new DefaultEntityFormEditorPage( feature, formEditor.getFeatureStore(),
                    VertragsArtComposite.class, KapsRepository.instance(), VertragsArtComposite.NAME ) {

                @Override
                protected String labelFor( String name ) {
                    if ("name".equals( name )) {
                        return "Bezeichnung";
                    }
                    if ("stala".equals( name )) {
                        return "STALA";
                    }
                    return super.labelFor( name );
                }


                @Override
                protected String tooltipFor( String name ) {
                    if ("stala".equals( name )) {
                        return "Schlüssel Statistisches Landesamt - Verwandschaftsverhältnis";
                    }
                    return super.labelFor( name );
                }
            } );
        }
        else if (name.equalsIgnoreCase( KaeuferKreisComposite.NAME )) {
            result.add( new DefaultEntityFormEditorPage( feature, formEditor.getFeatureStore(),
                    KaeuferKreisComposite.class, KapsRepository.instance(), KaeuferKreisComposite.NAME ) {

                @Override
                protected String labelFor( String name ) {
                    if ("name".equals( name )) {
                        return "Bezeichnung";
                    }
                    if ("stala".equals( name )) {
                        return "STALA Bauland";
                    }
                    if ("stalaAgrar".equals( name )) {
                        return "STALA Agrar";
                    }
                    if ("kaeuferKreisStabu".equals( name )) {
                        return "STABU";
                    }
                    return super.labelFor( name );
                }


                @Override
                protected String tooltipFor( String name ) {
                    if ("stala".equals( name )) {
                        return "Schlüssel Statistisches Landesamt - Veräußerer Bauland";
                    }
                    if ("stalaAgrar".equals( name )) {
                        return "Schlüssel Statistisches Landesamt - Veräußerer Agrarland";
                    }
                    if ("kaeuferKreisStabu".equals( name )) {
                        return "Schlüssel Statistisches Bundesamt";
                    }
                    return super.labelFor( name );
                }
            } );
        }
        else if (name.equalsIgnoreCase( StrasseComposite.NAME )) {
            result.add( new DefaultEntityFormEditorPage( feature, formEditor.getFeatureStore(), StrasseComposite.class,
                    KapsRepository.instance(), StrasseComposite.NAME ) );
        }
        else if (name.equalsIgnoreCase( FlurComposite.NAME )) {
            result.add( new DefaultEntityFormEditorPage( feature, formEditor.getFeatureStore(), FlurComposite.class,
                    KapsRepository.instance(), FlurComposite.NAME ) );
        }
        else if (name.equalsIgnoreCase( NHK2010BewertungComposite.NAME )) {
            result.add( new NHK2010BewertungFormEditorPage( formEditor, feature, formEditor.getFeatureStore() ) );
        }
        return result;
    }

}
