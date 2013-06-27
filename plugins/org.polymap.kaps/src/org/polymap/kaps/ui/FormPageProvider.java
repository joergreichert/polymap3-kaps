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

import org.polymap.kaps.model.data.BodennutzungComposite;
import org.polymap.kaps.model.data.FlurstuecksdatenAgrarComposite;
import org.polymap.kaps.model.data.FlurstuecksdatenBaulandComposite;
import org.polymap.kaps.model.data.GebaeudeArtComposite;
import org.polymap.kaps.model.data.RichtwertzoneComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.model.data.WohnungComposite;
import org.polymap.kaps.model.data.WohnungseigentumComposite;
import org.polymap.kaps.ui.form.BodennutzungFormEditorPage;
import org.polymap.kaps.ui.form.FlurstuecksdatenAgrarBodenwertFormEditorPage;
import org.polymap.kaps.ui.form.FlurstuecksdatenAgrarGrunddatenFormEditorPage;
import org.polymap.kaps.ui.form.FlurstuecksdatenBaulandBodenwertFormEditorPage;
import org.polymap.kaps.ui.form.FlurstuecksdatenBaulandGrunddatenFormEditorPage;
import org.polymap.kaps.ui.form.FlurstuecksdatenBaulandRichtwertFormEditorPage;
import org.polymap.kaps.ui.form.FlurstuecksdatenBaulandSonstigesFormEditorPage;
import org.polymap.kaps.ui.form.GebaeudeArtFormEditorPage;
import org.polymap.kaps.ui.form.Kaufvertrag1FormEditorPage;
import org.polymap.kaps.ui.form.Kaufvertrag2FormEditorPage;
import org.polymap.kaps.ui.form.KaufvertragErweitertFormEditorPage;
import org.polymap.kaps.ui.form.KaufvertragFlurstueckeFormEditorPage;
import org.polymap.kaps.ui.form.RichtwertzoneGrunddatenFormEditorPage;
import org.polymap.kaps.ui.form.RichtwertzoneWeitereDatenFormEditorPage;
import org.polymap.kaps.ui.form.WohnungGrunddatenFormEditorPage;
import org.polymap.kaps.ui.form.WohnungLiegenschaftzinsFormEditorPage;
import org.polymap.kaps.ui.form.WohnungVertragsdatenFormEditorPage;
import org.polymap.kaps.ui.form.WohnungseigentumFlurstueckeFormEditorPage;
import org.polymap.kaps.ui.form.WohnungseigentumGebaeudeFormEditorPage;
import org.polymap.kaps.ui.form.WohnungseigentumObjektdatenFormEditorPage;

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
            result.add( new KaufvertragFlurstueckeFormEditorPage( feature, formEditor.getFeatureStore() ) );
            result.add( new KaufvertragErweitertFormEditorPage( feature, formEditor.getFeatureStore() ) );
        }
        else if (name.equalsIgnoreCase( RichtwertzoneComposite.NAME )) {
            result.add( new RichtwertzoneGrunddatenFormEditorPage( feature, formEditor.getFeatureStore() ) );
            result.add( new RichtwertzoneWeitereDatenFormEditorPage( feature, formEditor.getFeatureStore() ) );
        }
        else if (name.equalsIgnoreCase( FlurstuecksdatenBaulandComposite.NAME )) {
            result.add( new FlurstuecksdatenBaulandGrunddatenFormEditorPage( feature, formEditor.getFeatureStore() ) );
            result.add( new FlurstuecksdatenBaulandBodenwertFormEditorPage( feature, formEditor.getFeatureStore() ) );
            result.add( new FlurstuecksdatenBaulandRichtwertFormEditorPage( feature, formEditor.getFeatureStore() ) );
            result.add( new FlurstuecksdatenBaulandSonstigesFormEditorPage( feature, formEditor.getFeatureStore() ) );
        }
        else if (name.equalsIgnoreCase( FlurstuecksdatenAgrarComposite.NAME )) {
            result.add( new FlurstuecksdatenAgrarGrunddatenFormEditorPage( feature, formEditor.getFeatureStore() ) );
            result.add( new FlurstuecksdatenAgrarBodenwertFormEditorPage( feature, formEditor.getFeatureStore() ) );
        }
        else if (name.equalsIgnoreCase( WohnungseigentumComposite.NAME )) {
            result.add( new WohnungseigentumObjektdatenFormEditorPage( feature, formEditor.getFeatureStore() ) );
            result.add( new WohnungseigentumGebaeudeFormEditorPage( feature, formEditor.getFeatureStore() ) );
            result.add( new WohnungseigentumFlurstueckeFormEditorPage( feature, formEditor.getFeatureStore() ) );
        }
        else if (name.equalsIgnoreCase( WohnungComposite.NAME )) {
            result.add( new WohnungGrunddatenFormEditorPage( feature, formEditor.getFeatureStore() ) );
            result.add( new WohnungVertragsdatenFormEditorPage( feature, formEditor.getFeatureStore() ) );
            result.add( new WohnungLiegenschaftzinsFormEditorPage( feature, formEditor.getFeatureStore() ) );
        }
        else if (name.equalsIgnoreCase( GebaeudeArtComposite.NAME )) {
            result.add( new GebaeudeArtFormEditorPage( feature, formEditor.getFeatureStore() ) );
        }
        else if (name.equalsIgnoreCase( BodennutzungComposite.NAME )) {
            result.add( new BodennutzungFormEditorPage( feature, formEditor.getFeatureStore() ) );
        }
        return result;
    }

}
