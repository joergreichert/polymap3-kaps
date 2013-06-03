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

import java.util.Locale;

import java.text.NumberFormat;

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;

import org.qi4j.api.property.Property;

import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.ui.forms.widgets.Section;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.NumberValidator;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.field.SelectlistFormField;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.form.DefaultFormEditorPage;
import org.polymap.rhei.form.IFormEditorPage2;
import org.polymap.rhei.form.IFormEditorPageSite;
import org.polymap.rhei.form.IFormEditorToolkit;

import org.polymap.kaps.KapsPlugin;
import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.Named;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public abstract class KapsDefaultFormEditorPage
        extends DefaultFormEditorPage
        implements IFormEditorPage2 {

    protected static final int SPACING = 12;

    protected static final int LEFT    = 0;

    protected static final int MIDDLE  = 50;

    protected static final int RIGHT   = 100;

    protected KapsRepository   repository;

    protected Locale           locale  = Locale.GERMAN;


    public KapsDefaultFormEditorPage( String id, String title, Feature feature, FeatureStore featureStore ) {
        super( id, title, feature, featureStore );

        repository = KapsRepository.instance();
    }


    @Override
    public void createFormContent( IFormEditorPageSite site ) {
        super.createFormContent( site );

        Composite parent = site.getPageBody();
        parent.setLayout( newPageLayout() );
    }


    protected FormLayout newPageLayout() {
        FormLayout result = new FormLayout();
        result.marginHeight = 10;
        result.marginWidth = 10;
        return result;
    }


    protected String formattedTitle( String type, Object name, String pageTitle ) {
        return type + ": " + (name != null ? name.toString() : "-") + (pageTitle != null ? " - " + pageTitle + "" : "");
    }


    protected Section newSection( final Composite top, final String title ) {
        Composite parent = pageSite.getPageBody();
        IFormEditorToolkit tk = pageSite.getToolkit();
        Section section = tk.createSection( parent, Section.TITLE_BAR | Section.FOCUS_TITLE | Section.TWISTIE
                | Section.CLIENT_INDENT );
        section.setText( title );
        section.setExpanded( true );
        section.setLayoutData( new SimpleFormData().left( 0 ).right( 100 ).top( top, 20 ).create() );
        Composite client = tk.createComposite( section );
        client.setLayout( new FormLayout() );
        client.setLayoutData( new SimpleFormData( SPACING ).left( 0 ).right( 100 ).top( 0, 0 ).create() );

        section.setClient( client );
        return section;
    }


    protected SimpleFormData right() {
        return new SimpleFormData( SPACING ).left( MIDDLE ).right( RIGHT );
    }


    protected SimpleFormData left() {
        return new SimpleFormData( SPACING ).left( LEFT ).right( MIDDLE );
    }


    protected <T extends Named> PicklistFormField namedAssocationsPicklist( Class<T> type ) {
        return namedAssocationsPicklist( type, false );
    }


    protected <T extends Named> SelectlistFormField namedAssocationsSelectlist( Class<T> type, boolean multiple ) {
        SelectlistFormField list = new SelectlistFormField( repository.entitiesWithNames( type ) );
        list.setIsMultiple( multiple );

        return list;
    }


    protected <T extends Named> PicklistFormField namedAssocationsPicklist( Class<T> type, boolean editable ) {
        PicklistFormField picklist = new PicklistFormField( repository.entitiesWithNames( type ) );
        picklist.setTextEditable( editable );

        return picklist;
    }


    @Override
    public void doLoad( final IProgressMonitor monitor )
            throws Exception {
        // call afterDoLoad() after fields are loaded in order to prevent
        // computed fields from re-set
        Polymap.getSessionDisplay().asyncExec( new Runnable() {
            public void run() {
                try {
                    afterDoLoad( monitor );
                }
                catch (Exception e) {
                    PolymapWorkbench.handleError( KapsPlugin.PLUGIN_ID, this, "An error occured while creating the new page.", e );
                }
            }
        });
    }
    
    public void afterDoLoad( IProgressMonitor monitor )
            throws Exception {
    }

    @Override
    public void dispose() {
    }


    @Override
    public void doSubmit( IProgressMonitor monitor )
            throws Exception {
    }


    @Override
    public boolean isDirty() {
        return false;
    }


    @Override
    public boolean isValid() {
        return true;
    }


    protected NumberFormat getFormatter( int fractionDigits ) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits( fractionDigits );
        nf.setMinimumFractionDigits( fractionDigits );
        nf.setMinimumIntegerDigits( 1 );
        return nf;
    }


    protected Composite createPreisField( Property<Double> property, SimpleFormData data, Composite parent,
            boolean editable ) {
        return createPreisField( IFormFieldLabel.NO_LABEL, property, data, parent, editable );
    }


    protected Composite createPreisField( String label, Property<Double> property, SimpleFormData data,
            Composite parent, boolean editable ) {
        return createPreisField( label, null, property, data, parent, editable );
    }


    protected Composite createPreisField( String label, String tooltip, Property<Double> property, SimpleFormData data,
            Composite parent, boolean editable ) {
        return createNumberField( label, tooltip, property, data, parent, editable, 2 );
    }


    protected Composite createFlaecheField( Property<Double> property, SimpleFormData data, Composite parent,
            boolean editable ) {
        return createFlaecheField( IFormFieldLabel.NO_LABEL, property, data, parent, editable );
    }


    protected Composite createFlaecheField( String label, Property<Double> property, SimpleFormData data,
            Composite parent, boolean editable ) {
        return createFlaecheField( label, null, property, data, parent, editable );
    }


    protected Composite createFlaecheField( String label, String tooltip, Property<Double> property,
            SimpleFormData data, Composite parent, boolean editable ) {
        return createNumberField( label, tooltip, property, data, parent, editable, 0 );
    }
    
    private Composite createNumberField( String label, String tooltip, Property<Double> property, SimpleFormData data,
            Composite parent, boolean editable, int fractionDigits ) {
        return newFormField( label )
                .setProperty( new PropertyAdapter( property ) )
                .setToolTipText( tooltip )
                .setField( new StringFormField( StringFormField.Style.ALIGN_RIGHT ) )
                .setValidator(
                        new NumberValidator( Double.class, Polymap.getSessionLocale(), 12, fractionDigits, 1,
                                fractionDigits ) ).setLayoutData( data.create() ).setParent( parent )
                .setEnabled( editable ).create();
    }


    protected Control createLabel( Composite parent, String text, SimpleFormData data, int style ) {
        return createLabel( parent, text, text, data, style );
    }


    protected Control createLabel( Composite parent, String text, String tooltip, SimpleFormData data, int style ) {
        Control label = pageSite.getToolkit().createLabel( parent, text, style );
        label.setToolTipText( tooltip );
        // label.setForeground( FormEditorToolkit.labelForeground );
        label.setLayoutData( data.create() );
        return label;
    }
}