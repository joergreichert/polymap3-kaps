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

import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.model.data.VertragsdatenBaulandComposite;
import org.polymap.kaps.ui.form.Kaufvertrag1FormEditorPage;
import org.polymap.kaps.ui.form.Kaufvertrag2FormEditorPage;
import org.polymap.kaps.ui.form.KaufvertragFlurstueckeFormEditorPage;
import org.polymap.kaps.ui.form.RichtwertzoneGrunddatenFormEditorPage;
import org.polymap.kaps.ui.form.RichtwertzoneWeitereDatenFormEditorPage;
import org.polymap.kaps.ui.form.VertragsdatenBaulandGrunddatenFormEditorPage;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class FormPageProvider
        implements IFormPageProvider {

    @Override
    public List<IFormEditorPage> addPages( FormEditor formEditor, Feature feature ) {
        // log.debug("addPages(): feature= " + feature);
        List<IFormEditorPage> result = new ArrayList<IFormEditorPage>();
        String name = feature.getType().getName().getLocalPart();

        if (name.equalsIgnoreCase( VertragComposite.NAME )) {
            result.add( new Kaufvertrag1FormEditorPage( feature, formEditor.getFeatureStore() ) );
            result.add( new Kaufvertrag2FormEditorPage( feature, formEditor.getFeatureStore() ) );
            result.add( new KaufvertragFlurstueckeFormEditorPage( feature, formEditor
                    .getFeatureStore() ) );
        }
        else if (name.equalsIgnoreCase( "richtwertzone" )) {
            result.add( new RichtwertzoneGrunddatenFormEditorPage( feature, formEditor
                    .getFeatureStore() ) );
            result.add( new RichtwertzoneWeitereDatenFormEditorPage( feature, formEditor
                    .getFeatureStore() ) );
        }
        else if (name.equalsIgnoreCase( VertragsdatenBaulandComposite.NAME )) {
            result.add( new VertragsdatenBaulandGrunddatenFormEditorPage( feature, formEditor
                    .getFeatureStore() ) );
        }
        return result;
    }

}
