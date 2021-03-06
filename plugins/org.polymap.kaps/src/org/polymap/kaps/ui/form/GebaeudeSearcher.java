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

import java.util.ArrayList;
import java.util.List;

import org.opengis.feature.type.PropertyDescriptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import org.eclipse.ui.PlatformUI;

import org.polymap.core.data.ui.featuretable.DefaultFeatureTableColumn;
import org.polymap.core.data.ui.featuretable.FeatureTableViewer;
import org.polymap.core.data.ui.featuretable.IFeatureTableElement;
import org.polymap.core.model.EntityType;
import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.data.entityfeature.PropertyDescriptorAdapter;
import org.polymap.rhei.data.entityfeature.ReloadablePropertyAdapter.CompositeProvider;

import org.polymap.kaps.KapsPlugin;
import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.data.FlurstueckComposite;
import org.polymap.kaps.model.data.GebaeudeComposite;
import org.polymap.kaps.ui.ActionButton;
import org.polymap.kaps.ui.NamedCompositesFeatureContentProvider;

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public abstract class GebaeudeSearcher
        extends Action {

    private static Log                                   log     = LogFactory.getLog( GebaeudeSearcher.class );

    private List<GebaeudeComposite>                      content = new ArrayList<GebaeudeComposite>();

    private final CompositeProvider<FlurstueckComposite> selectedComposite;


    public GebaeudeSearcher( CompositeProvider<FlurstueckComposite> selectedComposite ) {
        super( "Wohnung anlegen" );

        setToolTipText( "vorhandene Gebäude suchen und Wohnung darin anlegen" );
        setEnabled( false );
        this.selectedComposite = selectedComposite;
    }


    protected abstract void adopt( GebaeudeComposite element )
            throws Exception;


    public void run() {
        try {

            WohnungTableDialog dialog = new WohnungTableDialog();
            dialog.setBlockOnOpen( true );
            int returnCode = dialog.open();
            if (returnCode == IDialogConstants.OK_ID) {
                assert dialog.sel.length == 1 : "Selected: " + dialog.sel.length;
                final IFeatureTableElement sel = dialog.sel[0];
                GebaeudeComposite wohnung = Iterables.find( content, new Predicate<GebaeudeComposite>() {

                    public boolean apply( GebaeudeComposite input ) {
                        return input.id().equals( sel.fid() );
                    }
                } );

                adopt( wohnung );
            }
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( KapsPlugin.PLUGIN_ID, this, "Fehler beim Öffnen der Wohnungstabelle.", e );
        }
    }


    /**
     * 
     */
    class WohnungTableDialog
            extends TitleAreaDialog {

        private FeatureTableViewer     viewer;

        private IFeatureTableElement[] sel;


        public WohnungTableDialog() {
            super( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() );
            setShellStyle( getShellStyle() | SWT.RESIZE );
        }


        protected Image getImage() {
            return getShell().getDisplay().getSystemImage( SWT.ICON_QUESTION );
        }


        protected Point getInitialSize() {
            return new Point( 800, 600 );
            // return super.getInitialSize();
        }


        protected Control createDialogArea( Composite parent ) {
            getShell().setText( "Gebäude suchen" );
            // Section section = new Section( parent, Section.TITLE_BAR );
            // section.setText( "Wohnung suchen" );
            // Composite composite = (Composite)section.getClient();
            Composite composite = new Composite( parent, SWT.NORMAL | SWT.H_SCROLL | SWT.V_SCROLL );
            FormLayout pageLayout = new FormLayout();
            pageLayout.marginHeight = 5;
            pageLayout.marginWidth = 5;
            composite.setLayout( pageLayout );

            // composite.setLayoutData( new SimpleFormData().fill() );
            // section.setClient( composite );
            // Composite area = (Composite)super.createDialogArea( parent );
            setMessage( "Wählen Sie das Gebäude für die neue Wohnung. Wenn Sie kein Gebäude finden, legen Sie dies bitte vorher an." );

            // 6 Textfelder mit den Suchboxen nach Objektnummer
            final Text t1 = new Text( composite, SWT.RIGHT | SWT.BORDER );
            t1.setToolTipText( "Objektnummer" );
            t1.setLayoutData( new SimpleFormData().left( 0 ).right( 15 ).create() );
            //
            // final Text t2 = new Text( composite, SWT.RIGHT | SWT.BORDER );
            // t2.setToolTipText( "Objektfortführung" );
            // t2.setLayoutData( new SimpleFormData().left( 16 ).right( 31 ).create()
            // );

            final Text t3 = new Text( composite, SWT.RIGHT | SWT.BORDER );
            t3.setToolTipText( "Gebäudenummer" );
            t3.setLayoutData( new SimpleFormData().left( 16 ).right( 31 ).create() );
            //
            // final Text t4 = new Text( composite, SWT.RIGHT | SWT.BORDER );
            // t4.setToolTipText( "Gebäudefortführung" );
            // t4.setLayoutData( new SimpleFormData().left( 34 ).right( 49 ).create()
            // );

            // ein suchenknopf der nach den 6 Feldern sucht
            Action searchAction = new Action( "Suchen" ) {

                public void run() {
                    GebaeudeComposite template = QueryExpressions.templateFor( GebaeudeComposite.class );
                    BooleanExpression expr = null;
                    String text = t1.getText();
                    if (text != null && !text.isEmpty()) {
                        Integer asInteger = Integer.parseInt( text );
                        expr = QueryExpressions.eq( template.objektNummer(), asInteger );
                    }
                    // text = t2.getText();
                    // if (text != null && !text.isEmpty()) {
                    // Integer asInteger = Integer.parseInt( text );
                    // BooleanExpression q = QueryExpressions.eq(
                    // template.objektFortfuehrung(), asInteger );
                    // expr = expr == null ? q : QueryExpressions.and( expr, q );
                    // }
                    text = t3.getText();
                    if (text != null && !text.isEmpty()) {
                        Integer asInteger = Integer.parseInt( text );
                        BooleanExpression q = QueryExpressions.eq( template.gebaeudeNummer(), asInteger );
                        expr = expr == null ? q : QueryExpressions.and( expr, q );
                    }
                    // text = t4.getText();
                    // if (text != null && !text.isEmpty()) {
                    // Integer asInteger = Integer.parseInt( text );
                    // BooleanExpression q = QueryExpressions.eq(
                    // template.gebaeudeFortfuehrung(), asInteger );
                    // expr = expr == null ? q : QueryExpressions.and( expr, q );
                    // }

                    content.clear();
                    for (GebaeudeComposite wohnung : KapsRepository.instance().findEntities( GebaeudeComposite.class,
                            expr, 0, 100 )) {
                        content.add( wohnung );
                    }
                    viewer.setInput( content );
                };
            };
            ActionButton search = new ActionButton( composite, searchAction );
            search.setLayoutData( new SimpleFormData().left( 2 ).right( 14 ).top( t1, 20 ).create() );

            // section = new Section( section, Section.TITLE_BAR );
            // section.setText( "Auswählen" );
            // // Composite composite = (Composite)section.getClient();
            // composite = new Composite( section, SWT.NO_FOCUS );

            viewer = new FeatureTableViewer( composite, SWT.V_SCROLL | SWT.H_SCROLL );
            viewer.getTable().setLayoutData( new SimpleFormData().fill().height( 300 ).top( search, 20 ).create() );

            // entity types
            final KapsRepository repo = KapsRepository.instance();
            final EntityType<GebaeudeComposite> type = repo.entityType( GebaeudeComposite.class );

            // columns
            PropertyDescriptor prop = null;
            prop = new PropertyDescriptorAdapter( type.getProperty( "objektNummer" ) );
            viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Objekt" ).setWeight( 1, 50 ) );
            // prop = new PropertyDescriptorAdapter( type.getProperty(
            // "objektFortfuehrung" ) );
            // viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader(
            // "-fortf." ) );
            prop = new PropertyDescriptorAdapter( type.getProperty( "gebaeudeNummer" ) );
            viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Gebäude" ).setWeight( 1, 50 ) );
            // prop = new PropertyDescriptorAdapter( type.getProperty(
            // "gebaeudeFortfuehrung" ) );
            // viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader(
            // "-fortf." ) );
            // prop = new PropertyDescriptorAdapter( type.getProperty(
            // "wohnungsNummer" ) );
            // viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader(
            // "Wohnung" ).setWeight( 1, 50 ) );
            // prop = new PropertyDescriptorAdapter( type.getProperty(
            // "wohnungsFortfuehrung" ) );
            // viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader(
            // "-fortf." ).setWeight( 1, 50 ) );
            // prop = new PropertyDescriptorAdapter( type.getProperty( "flurstueck" )
            // );
            // viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader(
            // "Flurstück" ).setWeight( 5, 100 ) );
            // prop = new PropertyDescriptorAdapter( type.getProperty(
            // "wohnungsNummer" ) );
            // viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader(
            // "wohnungsNummer" ) );
            // prop = new PropertyDescriptorAdapter( type.getProperty(
            // "hauptFlurstueck" ) );
            // viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader(
            // "Hauptflurstück" ) );
            // prop = new PropertyDescriptorAdapter( type.getProperty( "strasse" ) );
            // viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader(
            // "Straße" ) );
            // prop = new PropertyDescriptorAdapter( type.getProperty( "hausnummer" )
            // );
            // viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader(
            // "Hausnummer" ) );

            // model/content
            viewer.setContent( new NamedCompositesFeatureContentProvider( content, type ) );
            viewer.setInput( content );

            // selection
            viewer.addSelectionChangedListener( new ISelectionChangedListener() {

                public void selectionChanged( SelectionChangedEvent ev ) {
                    sel = viewer.getSelectedElements();
                    getButton( IDialogConstants.OK_ID ).setEnabled( sel.length > 0 );
                }
            } );

            composite.pack();
            searchForFlurstueck();
            return composite;
        }


        /**
         *
         */
        private void searchForFlurstueck() {
            GebaeudeComposite template = QueryExpressions.templateFor( GebaeudeComposite.class );

            content.clear();
            FlurstueckComposite flurstueck = selectedComposite.get();
            if (flurstueck != null) {
                // suche über alle flurstücke
                
                FlurstueckComposite fsTemplate = QueryExpressions.templateFor( FlurstueckComposite.class );
                BooleanExpression bExpr = QueryExpressions.and(
                        QueryExpressions.eq( fsTemplate.gemarkung(), flurstueck.gemarkung().get() ),
                        QueryExpressions.eq( fsTemplate.hauptNummer(), flurstueck.hauptNummer().get() ),
                        QueryExpressions.eq( fsTemplate.unterNummer(), flurstueck.unterNummer().get() ) );
                for (FlurstueckComposite fsFound : KapsRepository.instance().findEntities( FlurstueckComposite.class,
                        bExpr, 0, -1 )) {
                    for (GebaeudeComposite gebaeude : KapsRepository.instance().findEntities( GebaeudeComposite.class,
                            null, 0, -1 )) {
                        if (gebaeude.flurstuecke().contains( fsFound )) {
                            if (!content.contains( gebaeude )) {
                                content.add( gebaeude );
                            }
                        }
                    }
                }
            }
            if (content.isEmpty()) {
                MessageDialog
                        .openWarning(
                                PolymapWorkbench.getShellToParentOn(),
                                "Keine Gebäude gefunden",
                                "Es wurden keine Gebäude zu diesem Flurstück gefunden. Bitte benutzen Sie die Suche nach Objekt- und Gebäudenummer, falls das Gebäude schon existiert." );
            }
            viewer.setInput( content );
        }


        protected void createButtonsForButtonBar( Composite parent ) {
            super.createButtonsForButtonBar( parent );
            // createButton( parent, RESET_BUTTON, "Zurücksetzen", false );
            // createButton( parent, IDialogConstants.NEXT_ID, "Fortführen", true );
            // createButton( parent, IDialogConstants.OK_ID, "Übernehmen", false );
            // createButton( parent, IDialogConstants.CANCEL_ID, "Abbrechen", false
            // );
            // getButton( IDialogConstants.NEXT_ID ).setEnabled( false );
            getButton( IDialogConstants.OK_ID ).setEnabled( false );
        }


        @Override
        protected void buttonPressed( int buttonId ) {
            setReturnCode( buttonId );
            close();
        }

    }
}
