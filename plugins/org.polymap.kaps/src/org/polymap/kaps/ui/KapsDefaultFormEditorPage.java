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

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;

import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.ui.forms.widgets.Section;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.project.ui.util.SimpleFormData;

import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.field.SelectlistFormField;
import org.polymap.rhei.form.DefaultFormEditorPage;
import org.polymap.rhei.form.IFormEditorPage;
import org.polymap.rhei.form.IFormEditorPage2;
import org.polymap.rhei.form.IFormEditorPageSite;
import org.polymap.rhei.form.IFormEditorToolkit;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.Named;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public abstract class KapsDefaultFormEditorPage
        extends DefaultFormEditorPage
        implements IFormEditorPage, IFormEditorPage2 {

    protected static final int SPACING = 6;

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
    public void doLoad( IProgressMonitor monitor )
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

}