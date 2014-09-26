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
import org.polymap.kaps.model.data.WohnungComposite;
import org.polymap.kaps.ui.ActionButton;
import org.polymap.kaps.ui.NamedCompositesFeatureContentProvider;

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public abstract class WohnungSearcher
        extends Action {

    private static Log                                   log     = LogFactory.getLog( WohnungSearcher.class );

    private List<WohnungComposite>                       content = new ArrayList<WohnungComposite>();

    private final CompositeProvider<FlurstueckComposite> selectedComposite;


    public WohnungSearcher( CompositeProvider<FlurstueckComposite> selectedComposite ) {
        super( "vorhandene Wohnung suchen" );

        setToolTipText( "vorhandene Wohnung suchen und mit Flurstück verknüpfen" );
        setEnabled( false );
        this.selectedComposite = selectedComposite;
    }


    protected abstract void adopt( WohnungComposite element )
            throws Exception;


    public void run() {
        try {

            WohnungTableDialog dialog = new WohnungTableDialog();
            dialog.setBlockOnOpen( true );
            int returnCode = dialog.open();
            if (returnCode == IDialogConstants.OK_ID || returnCode == IDialogConstants.NEXT_ID) {
                assert dialog.sel.length == 1 : "Selected: " + dialog.sel.length;
                final IFeatureTableElement sel = dialog.sel[0];
                WohnungComposite wohnung = Iterables.find( content, new Predicate<WohnungComposite>() {

                    public boolean apply( WohnungComposite input ) {
                        return input.id().equals( sel.fid() );
                    }
                } );

                if (returnCode == IDialogConstants.NEXT_ID) {
                    // fortführung erstellen
                    wohnung = KapsRepository.instance().clone( WohnungComposite.class, wohnung );
                    Integer fortf = wohnung.wohnungsFortfuehrung().get();

                    wohnung.wohnungsFortfuehrung().set(
                            fortf == null ? Integer.valueOf( 0 ) : Integer.valueOf( fortf.intValue() + 1 ) );
                }
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
            getShell().setText( "Wohnungen suchen" );
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
            setMessage( "Bitte geben Sie in Ziffern Objektnummer, Gebäudenummer, Wohnungsnummer oder -fortführung ein und klicken Sie auf Suchen. Anschließend können Sie aus den maximal 50 Ergebnissen eines wählen, das mit Klick auf \"Übernehmen\" direkt übernommen wird oder mit Klick auf \"Fortführen\" fortgeführt. Die Suche nach dem Flurstück wird bereits im Hintergrund ausgeführt." );

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

            final Text t5 = new Text( composite, SWT.RIGHT | SWT.BORDER );
            t5.setToolTipText( "Wohnungsnummer" );
            t5.setLayoutData( new SimpleFormData().left( 32 ).right( 47 ).create() );

            final Text t6 = new Text( composite, SWT.RIGHT | SWT.BORDER );
            t6.setToolTipText( "Wohnungsfortführung" );
            t6.setLayoutData( new SimpleFormData().left( 48 ).right( 63 ).create() );

            // ein suchenknopf der nach den 6 Feldern sucht
            Action searchAction = new Action( "Suchen" ) {

                public void run() {
                    WohnungComposite template = QueryExpressions.templateFor( WohnungComposite.class );
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
                    text = t5.getText();
                    if (text != null && !text.isEmpty()) {
                        Integer asInteger = Integer.parseInt( text );
                        BooleanExpression q = QueryExpressions.eq( template.wohnungsNummer(), asInteger );
                        expr = expr == null ? q : QueryExpressions.and( expr, q );
                    }
                    text = t6.getText();
                    if (text != null && !text.isEmpty()) {
                        Integer asInteger = Integer.parseInt( text );
                        BooleanExpression q = QueryExpressions.eq( template.wohnungsFortfuehrung(), asInteger );
                        expr = expr == null ? q : QueryExpressions.and( expr, q );
                    }

                    // add search for flurstück
                    FlurstueckComposite flurstueck = selectedComposite.get();
                    BooleanExpression fsExpr = null;
                    if (flurstueck != null) {
                        // alle flurstücke mit gleicher gemarkung, nummer,
                        // unternummer finden
                        // und dann alle wohnungen suchen, die zu einem der
                        // flurstuecke gehören
                        FlurstueckComposite fsTemplate = QueryExpressions.templateFor( FlurstueckComposite.class );
                        BooleanExpression bExpr = null;
                        
                        if (flurstueck.gemarkung().get() != null) {
                          bExpr = QueryExpressions.eq( fsTemplate.gemarkung(), flurstueck.gemarkung().get() );
                        } else {
                            log.error("Flurstück " + flurstueck.hauptNummer().get() + "/" + flurstueck.unterNummer().get() + ": Gemarkung fehlt");
                        }  
                        if (flurstueck.hauptNummer().get() != null) {
                            BooleanExpression hExpr = QueryExpressions.eq( fsTemplate.hauptNummer(), flurstueck.hauptNummer().get() );
                            if (bExpr != null) {
                                bExpr = QueryExpressions.and( bExpr, hExpr );
                            } else {
                                bExpr = hExpr;
                            }
                        }
                        if (flurstueck.unterNummer().get() != null) {
                            BooleanExpression hExpr = QueryExpressions.eq( fsTemplate.unterNummer(), flurstueck.unterNummer().get() );
                            if (bExpr != null) {
                                bExpr = QueryExpressions.and( bExpr, hExpr );
                            } else {
                                bExpr = hExpr;
                            }
                        }
                        for (FlurstueckComposite fsFound : KapsRepository.instance().findEntities(
                                FlurstueckComposite.class, bExpr, 0, -1 )) {

                            fsExpr = fsExpr == null ? QueryExpressions.eq( template.flurstueck(), fsFound )
                                    : QueryExpressions.or( QueryExpressions.eq( template.flurstueck(), fsFound ),
                                            fsExpr );
                        }
                    }
                    if (fsExpr != null) {
                        if (expr != null) {
                            expr = QueryExpressions.and( expr, fsExpr );
                        }
                        else {
                            expr = fsExpr;
                        }
                    }

                    content.clear();
                    for (WohnungComposite wohnung : KapsRepository.instance().findEntities( WohnungComposite.class,
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
            final EntityType<WohnungComposite> type = repo.entityType( WohnungComposite.class );

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
            prop = new PropertyDescriptorAdapter( type.getProperty( "wohnungsNummer" ) );
            viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Wohnung" ).setWeight( 1, 50 ) );
            prop = new PropertyDescriptorAdapter( type.getProperty( "wohnungsFortfuehrung" ) );
            viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "-fortf." ).setWeight( 1, 50 ) );
            prop = new PropertyDescriptorAdapter( type.getProperty( "flurstueck" ) );
            viewer.addColumn( new DefaultFeatureTableColumn( prop ).setHeader( "Flurstück" ).setWeight( 5, 100 ) );
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
                    getButton( IDialogConstants.NEXT_ID ).setEnabled( sel.length > 0 );
                }
            } );

            composite.pack();
            searchAction.run();
            return composite;
        }


        protected void createButtonsForButtonBar( Composite parent ) {
            // super.createButtonsForButtonBar( parent );
            // createButton( parent, RESET_BUTTON, "Zurücksetzen", false );
            createButton( parent, IDialogConstants.NEXT_ID, "Fortführen", true );
            createButton( parent, IDialogConstants.OK_ID, "Übernehmen", false );
            createButton( parent, IDialogConstants.CANCEL_ID, "Abbrechen", false );
            getButton( IDialogConstants.NEXT_ID ).setEnabled( false );
            getButton( IDialogConstants.OK_ID ).setEnabled( false );
        }


        @Override
        protected void buttonPressed( int buttonId ) {
            setReturnCode( buttonId );
            close();
        }

    }
}
