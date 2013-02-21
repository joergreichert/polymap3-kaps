/* 
 * polymap.org
 * Copyright 2013 Polymap GmbH. All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.kaps;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;

import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.runtime.UIJob;
import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.KaufvertragComposite;
import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.form.FormEditor;
import org.polymap.rhei.form.IFormEditorPage;
import org.polymap.rhei.form.IFormEditorPage2;
import org.polymap.rhei.form.IFormEditorPageSite;
import org.polymap.rhei.form.IFormPageProvider;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class FormPageProvider implements IFormPageProvider {

	public class BaseFormEditorPage implements IFormEditorPage2 {

		private Feature feature;
		private KaufvertragComposite kaufvertrag;

		public BaseFormEditorPage(Feature feature, FeatureStore featureStore) {
			this.feature = feature;
			
			kaufvertrag = KapsRepository.instance().findEntity(KaufvertragComposite.class, feature.getIdentifier().getID());
		}

		@Override
		public String getTitle() {
			return "mein Titel";
		}

		@Override
		public Action[] getEditorActions() {
			return null;
		}

		@Override
		public String getId() {
			return "steffensTitle";
		}

		@Override
		public byte getPriority() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void createFormContent(IFormEditorPageSite site) {
			site.setFormTitle("Title");
			site.getPageBody().setLayout(new FormLayout());

//			 layouter.setFieldLayoutData( site.newFormField( client, 
//	                    new PropertyAdapter( biotop.name() ),
//	                    new StringFormField(), null, "Name" ) );
			 
			Composite field = site.newFormField(site.getPageBody(),
					new PropertyAdapter(kaufvertrag.eingangsNr()), new TextFormField(), null);
			field.setLayoutData(new SimpleFormData().left(0).right(50)
					.top(0, 0).create());
		}

		// IFormEditorPage2 *******************************
		
        @Override
        public void doLoad( IProgressMonitor monitor ) throws Exception {
        }

        @Override
        public void doSubmit( IProgressMonitor monitor ) throws Exception {
            // nach dem Speichern des Formulars auch alle Änderungen committen;
            // das sollte natürlich nur in einem offenen Formular passieren;
            // 3s Verzögerung, damit alle Formulare gespeichert sind vor commit
            new UIJob( "Speichern" ) {
                protected void runWithException( IProgressMonitor _monitor ) throws Exception {
                    OperationSupport.instance().saveChanges();
                }
            }.schedule( 3000 );
        }

        @Override
        public void dispose() {
        }

        @Override
        public boolean isDirty() {
            return false;
        }

        @Override
        public boolean isValid() {
            return true;
        }
        
	}

	public FormPageProvider() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<IFormEditorPage> addPages(FormEditor formEditor, Feature feature) {
		// log.debug("addPages(): feature= " + feature);
		List<IFormEditorPage> result = new ArrayList();
		if (feature.getType().getName().getLocalPart().equalsIgnoreCase("kaufvertrag")) {
			result.add(new BaseFormEditorPage(feature, formEditor.getFeatureStore()));
		}
		
		return result;
	}

}
