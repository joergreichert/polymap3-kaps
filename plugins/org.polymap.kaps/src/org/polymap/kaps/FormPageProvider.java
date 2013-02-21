package org.polymap.kaps;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;
import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.form.FormEditor;
import org.polymap.rhei.form.IFormEditorPage;
import org.polymap.rhei.form.IFormEditorPageSite;
import org.polymap.rhei.form.IFormPageProvider;

public class FormPageProvider implements IFormPageProvider {

	public class BaseFormEditorPage implements IFormEditorPage {

		private Feature feature;

		public BaseFormEditorPage(Feature feature, FeatureStore featureStore) {
			this.feature = feature;
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

			Composite field = site.newFormField(site.getPageBody(),
					feature.getProperty("name"), new TextFormField(), null);
			field.setLayoutData(new SimpleFormData().left(0).right(50)
					.top(0, 0).create());
		}

	}

	public FormPageProvider() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<IFormEditorPage> addPages(FormEditor formEditor, Feature feature) {
		// log.debug("addPages(): feature= " + feature);
		List<IFormEditorPage> result = new ArrayList();
		// if (feature.getType().getName().getLocalPart()
		// .equalsIgnoreCase("biotop")) {
		result.add(new BaseFormEditorPage(feature, formEditor.getFeatureStore()));
		// }
		
		return result;
	}

}
