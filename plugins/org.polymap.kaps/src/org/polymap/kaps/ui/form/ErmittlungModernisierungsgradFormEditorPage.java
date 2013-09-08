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

import java.util.Calendar;
import java.util.GregorianCalendar;

import java.beans.PropertyChangeEvent;

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.property.Property;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.action.Action;

import org.eclipse.ui.forms.widgets.Section;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.kaps.KapsPlugin;
import org.polymap.kaps.model.data.ErmittlungModernisierungsgradComposite;
import org.polymap.kaps.model.data.NHK2010BewertungComposite;
import org.polymap.kaps.model.data.NHK2010BewertungGebaeudeComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.ui.ActionButton;
import org.polymap.kaps.ui.FieldCalculation;
import org.polymap.kaps.ui.FieldListener;
import org.polymap.kaps.ui.FieldSummation;
import org.polymap.kaps.ui.KapsDefaultFormEditorPage;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class ErmittlungModernisierungsgradFormEditorPage
        extends KapsDefaultFormEditorPage {

    private static Log                                   log = LogFactory
                                                                     .getLog( ErmittlungModernisierungsgradFormEditorPage.class );

    @SuppressWarnings("unused")
    private IFormFieldListener                           line1multiplicator;

    @SuppressWarnings("unused")
    private IFormFieldListener                           line2multiplicator;

    @SuppressWarnings("unused")
    private IFormFieldListener                           line3multiplicator;

    @SuppressWarnings("unused")
    private IFormFieldListener                           line4multiplicator;

    @SuppressWarnings("unused")
    private IFormFieldListener                           line5multiplicator;

    @SuppressWarnings("unused")
    private IFormFieldListener                           line6multiplicator;

    @SuppressWarnings("unused")
    private IFormFieldListener                           modernisierungGradSummation;

    @SuppressWarnings("unused")
    private IFormFieldListener                           line7multiplicator;

    @SuppressWarnings("unused")
    private IFormFieldListener                           line8multiplicator;

    private final ErmittlungModernisierungsgradComposite em;

    private IFormFieldListener                           modernisierungText;

    private IFormFieldListener                           restnutzungListener;

    private final Double                                 heute;

    private FieldListener                                baujahrListener;

    private ActionButton                                 baujahrUebernehmenAction;


    // private IFormFieldListener gemeindeListener;

    public ErmittlungModernisierungsgradFormEditorPage( Feature feature, FeatureStore featureStore ) {
        super( ErmittlungModernisierungsgradFormEditorPage.class.getName(), "Modernisierungsgrad", feature,
                featureStore );
        em = repository.findEntity( ErmittlungModernisierungsgradComposite.class, feature.getIdentifier().getID() );
        if (em.vertrag().get() == null && em.objektNummer().get() == null) {
            throw new IllegalStateException(
                    "Zum Ermitteln von Modernisierungsgraden nutzen Sie bitte immer die 'Berechnen' Funktionen aus den Formularen." );
        }
        // EventManager.instance().subscribe( bodenpreisListener = new
        // IFormFieldListener() {
        //
        // @Override
        // public void fieldChange( FormFieldEvent ev ) {
        // bodenpreisBebaut = (Double)ev.getNewValue();
        // }
        // }, new EventFilter<FormFieldEvent>() {
        //
        // public boolean apply( FormFieldEvent ev ) {
        // return ev.getEventCode() == IFormFieldListener.VALUE_CHANGE
        // && ev.getFieldName().equals( vb.bodenpreisBebaut().qualifiedName().name()
        // );
        // }
        // } );
        final Calendar cal = new GregorianCalendar();
        heute = new Integer( cal.get( Calendar.YEAR ) ).doubleValue();
    }


    @SuppressWarnings("unchecked")
    @Override
    public void createFormContent( final IFormEditorPageSite site ) {
        super.createFormContent( site );

        final VertragComposite vertragComposite = em.vertrag().get();
        if (vertragComposite != null) {
            String nummer = vertragComposite != null ? EingangsNummerFormatter.format( vertragComposite.eingangsNr()
                    .get() ) : null;
            site.setEditorTitle( formattedTitle( "Berechnung für Vertrag", nummer, null ) );
            site.setFormTitle( formattedTitle( "Berechnung des bereinigten Baujahres für Vertrag", nummer, getTitle() ) );
        }
        else {
            site.setEditorTitle( formattedTitle( "Berechnung für Objekt", em.objektNummer().get(), null ) );
            site.setFormTitle( formattedTitle( "Berechnung des bereinigten Baujahres für Objekt", em.objektNummer()
                    .get(), getTitle() ) );
        }

        Control newLine, lastLine = null;
        Composite parent = pageSite.getPageBody();

        Section section = newSection( parent, "Ermittlung des Modernisierungsgrades" );
        section.setLayoutData( new SimpleFormData( SECTION_SPACING ).left( 0 ).right( 100 ).create() );
        Composite client = (Composite)section.getClient();

        newLine = createLabel( client, "Modernisierungselemente", one().top( null ), SWT.CENTER );
        createLabel( client, "Punkte", two().top( null ), SWT.CENTER );
        createLabel( client, "Alter", three().top( null ), SWT.CENTER );
        createLabel( client, "Maximales Alter", four().top( null ), SWT.CENTER );
        createLabel( client, "Auswirkung", five().top( null ), SWT.CENTER );

        lastLine = newLine;
        newLine = createLabel( client, "Dacherneuerung inkl. Verbesserung der Wärmedämmung", one().top( lastLine ),
                SWT.RIGHT );
        createPunkteField( em.punkteZeile1(), two().top( lastLine ), client, true, 4 );
        createFlaecheField( em.alterZeile1(), three().top( lastLine ), client, true );
        createFlaecheField( em.alterObergrenzeZeile1(), four().top( lastLine ), client, false );
        createPreisField( em.auswirkungZeile1(), five().top( lastLine ), client, false );
        site.addFieldListener( line1multiplicator = new FieldCalculation( site, 2, em.auswirkungZeile1(), em
                .punkteZeile1(), em.alterZeile1(), em.alterObergrenzeZeile1() ) {

            @Override
            protected Double calculate( ValueProvider values ) {
                Double alter = values.get( em.alterZeile1() );
                Double obergrenze = values.get( em.alterObergrenzeZeile1() );
                Double punkte = values.get( em.punkteZeile1() );
                if (punkte != null && alter != null && obergrenze != null) {
                    if (alter >= obergrenze) {
                        return 0.0d;
                    }
                    return (1 - alter / obergrenze) * punkte;
                }
                return punkte;

            }
        } );

        lastLine = newLine;
        newLine = createLabel( client, "Modernisierung der Fenster und Außentüren", one().top( lastLine ), SWT.RIGHT );
        createPunkteField( em.punkteZeile2(), two().top( lastLine ), client, true, 2 );
        createFlaecheField( em.alterZeile2(), three().top( lastLine ), client, true );
        createFlaecheField( em.alterObergrenzeZeile2(), four().top( lastLine ), client, false );
        createPreisField( em.auswirkungZeile2(), five().top( lastLine ), client, false );
        site.addFieldListener( line2multiplicator = new FieldCalculation( site, 2, em.auswirkungZeile2(), em
                .punkteZeile2(), em.alterZeile2(), em.alterObergrenzeZeile2() ) {

            @Override
            protected Double calculate( ValueProvider values ) {
                Double alter = values.get( em.alterZeile2() );
                Double obergrenze = values.get( em.alterObergrenzeZeile2() );
                Double punkte = values.get( em.punkteZeile2() );
                if (punkte != null && alter != null && obergrenze != null) {
                    if (alter >= obergrenze) {
                        return 0.0d;
                    }
                    return (1 - alter / obergrenze) * punkte;
                }
                return punkte;

            }
        } );

        lastLine = newLine;
        newLine = createLabel( client, "Modernisierung der Leitungssysteme (Strom, Gas, Wasser, Abwasser)",
                one().top( lastLine ), SWT.RIGHT );
        createPunkteField( em.punkteZeile3(), two().top( lastLine ), client, true, 2 );
        createFlaecheField( em.alterZeile3(), three().top( lastLine ), client, true );
        createFlaecheField( em.alterObergrenzeZeile3(), four().top( lastLine ), client, false );
        createPreisField( em.auswirkungZeile3(), five().top( lastLine ), client, false );
        site.addFieldListener( line3multiplicator = new FieldCalculation( site, 2, em.auswirkungZeile3(), em
                .punkteZeile3(), em.alterZeile3(), em.alterObergrenzeZeile3() ) {

            @Override
            protected Double calculate( ValueProvider values ) {
                Double alter = values.get( em.alterZeile3() );
                Double obergrenze = values.get( em.alterObergrenzeZeile3() );
                Double punkte = values.get( em.punkteZeile3() );
                if (punkte != null && alter != null && obergrenze != null) {
                    if (alter >= obergrenze) {
                        return 0.0d;
                    }
                    return (1 - alter / obergrenze) * punkte;
                }
                return punkte;

            }
        } );

        lastLine = newLine;
        newLine = createLabel( client, "Modernisierung der Heizungsanlage", one().top( lastLine ), SWT.RIGHT );
        createPunkteField( em.punkteZeile4(), two().top( lastLine ), client, true, 2 );
        createFlaecheField( em.alterZeile4(), three().top( lastLine ), client, true );
        createFlaecheField( em.alterObergrenzeZeile4(), four().top( lastLine ), client, false );
        createPreisField( em.auswirkungZeile4(), five().top( lastLine ), client, false );
        site.addFieldListener( line4multiplicator = new FieldCalculation( site, 2, em.auswirkungZeile4(), em
                .punkteZeile4(), em.alterZeile4(), em.alterObergrenzeZeile4() ) {

            @Override
            protected Double calculate( ValueProvider values ) {
                Double alter = values.get( em.alterZeile4() );
                Double obergrenze = values.get( em.alterObergrenzeZeile4() );
                Double punkte = values.get( em.punkteZeile4() );
                if (punkte != null && alter != null && obergrenze != null) {
                    if (alter >= obergrenze) {
                        return 0.0d;
                    }
                    return (1 - alter / obergrenze) * punkte;
                }
                return punkte;

            }
        } );

        lastLine = newLine;
        newLine = createLabel( client, "Wärmedämmung der Außenwände", one().top( lastLine ), SWT.RIGHT );
        createPunkteField( em.punkteZeile5(), two().top( lastLine ), client, true, 4 );
        createFlaecheField( em.alterZeile5(), three().top( lastLine ), client, true );
        createFlaecheField( em.alterObergrenzeZeile5(), four().top( lastLine ), client, false );
        createPreisField( em.auswirkungZeile5(), five().top( lastLine ), client, false );
        site.addFieldListener( line5multiplicator = new FieldCalculation( site, 2, em.auswirkungZeile5(), em
                .punkteZeile5(), em.alterZeile5(), em.alterObergrenzeZeile5() ) {

            @Override
            protected Double calculate( ValueProvider values ) {
                Double alter = values.get( em.alterZeile5() );
                Double obergrenze = values.get( em.alterObergrenzeZeile5() );
                Double punkte = values.get( em.punkteZeile5() );
                if (punkte != null && alter != null && obergrenze != null) {
                    if (alter >= obergrenze) {
                        return 0.0d;
                    }
                    return (1 - alter / obergrenze) * punkte;
                }
                return punkte;

            }
        } );

        lastLine = newLine;
        newLine = createLabel( client, "Modernisierung von Bädern", one().top( lastLine ), SWT.RIGHT );
        createPunkteField( em.punkteZeile6(), two().top( lastLine ), client, true, 2 );
        createFlaecheField( em.alterZeile6(), three().top( lastLine ), client, true );
        createFlaecheField( em.alterObergrenzeZeile6(), four().top( lastLine ), client, false );
        createPreisField( em.auswirkungZeile6(), five().top( lastLine ), client, false );
        site.addFieldListener( line6multiplicator = new FieldCalculation( site, 2, em.auswirkungZeile6(), em
                .punkteZeile6(), em.alterZeile6(), em.alterObergrenzeZeile6() ) {

            @Override
            protected Double calculate( ValueProvider values ) {
                Double alter = values.get( em.alterZeile6() );
                Double obergrenze = values.get( em.alterObergrenzeZeile6() );
                Double punkte = values.get( em.punkteZeile6() );
                if (punkte != null && alter != null && obergrenze != null) {
                    if (alter >= obergrenze) {
                        return 0.0d;
                    }
                    return (1 - alter / obergrenze) * punkte;
                }
                return punkte;

            }
        } );

        lastLine = newLine;
        newLine = createLabel( client, "Modernisierung des Innenausbaus", one().top( lastLine ), SWT.RIGHT );
        createPunkteField( em.punkteZeile7(), two().top( lastLine ), client, true, 2 );
        createFlaecheField( em.alterZeile7(), three().top( lastLine ), client, true );
        createFlaecheField( em.alterObergrenzeZeile7(), four().top( lastLine ), client, false );
        createPreisField( em.auswirkungZeile7(), five().top( lastLine ), client, false );
        site.addFieldListener( line7multiplicator = new FieldCalculation( site, 2, em.auswirkungZeile7(), em
                .punkteZeile7(), em.alterZeile7(), em.alterObergrenzeZeile7() ) {

            @Override
            protected Double calculate( ValueProvider values ) {
                Double alter = values.get( em.alterZeile7() );
                Double obergrenze = values.get( em.alterObergrenzeZeile7() );
                Double punkte = values.get( em.punkteZeile7() );
                if (punkte != null && alter != null && obergrenze != null) {
                    if (alter >= obergrenze) {
                        return 0.0d;
                    }
                    return (1 - alter / obergrenze) * punkte;
                }
                return punkte;

            }
        } );

        lastLine = newLine;
        newLine = createLabel( client, "Wesentliche Verbesserung der Grundrissgestaltung", one().top( lastLine ),
                SWT.RIGHT );
        createPunkteField( em.punkteZeile8(), two().top( lastLine ), client, true, 2 );
        createFlaecheField( em.alterZeile8(), three().top( lastLine ), client, true );
        createFlaecheField( em.alterObergrenzeZeile8(), four().top( lastLine ), client, false );
        createPreisField( em.auswirkungZeile8(), five().top( lastLine ), client, false );
        site.addFieldListener( line8multiplicator = new FieldCalculation( site, 2, em.auswirkungZeile8(), em
                .punkteZeile8(), em.alterZeile8(), em.alterObergrenzeZeile8() ) {

            @Override
            protected Double calculate( ValueProvider values ) {
                Double alter = values.get( em.alterZeile8() );
                Double obergrenze = values.get( em.alterObergrenzeZeile8() );
                Double punkte = values.get( em.punkteZeile8() );
                if (punkte != null && alter != null && obergrenze != null) {
                    if (alter >= obergrenze) {
                        return 0.0d;
                    }
                    return (1 - alter / obergrenze) * punkte;
                }
                return punkte;

            }
        } );

        lastLine = newLine;
        newLine = createLabel( client, "Summe", one().top( lastLine ), SWT.RIGHT );
        createPreisField( em.modernisierungsGrad(), five().top( lastLine ), client, false );
        site.addFieldListener( modernisierungGradSummation = new FieldSummation( site, 2, em.modernisierungsGrad(), em
                .auswirkungZeile1(), em.auswirkungZeile2(), em.auswirkungZeile3(), em.auswirkungZeile4(), em
                .auswirkungZeile5(), em.auswirkungZeile6(), em.auswirkungZeile7(), em.auswirkungZeile8() ) );

        lastLine = newLine;
        newLine = createLabel( client, "Modernisierungsgrad", one().top( lastLine ), SWT.RIGHT );
        final Text modernisierungsGradText = pageSite.getToolkit().createText( client, "",
                SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL );
        modernisierungsGradText.setToolTipText( "Modernisierungsgrad nach NHK 2010" );
        modernisierungsGradText.setLayoutData( two().top( lastLine ).right( 100 ).height( 40 ).bottom( 100 ).create() );
        modernisierungsGradText.setEnabled( false );
        site.addFieldListener( modernisierungText = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() != IFormFieldListener.VALUE_CHANGE) {
                    return;
                }
                String fieldName = ev.getFieldName();
                if (fieldName.equals( em.modernisierungsGrad().qualifiedName().name() )) {
                    Double grad = ev.getNewValue();
                    StringBuffer text = new StringBuffer();
                    if (grad != null) {
                        if (grad <= 1.0d) {
                            text.append( "1: <= 1 Punkt, nicht modernisiert\n" );
                        }
                        if (grad > 1.0d && grad < 8.0d) {
                            text.append( "2: 4 Punkte, kleine Modernisierungen im Rahmen der Instandhaltung\n" );
                        }
                        if (grad > 4.0d && grad < 13.0d) {
                            text.append( "3: 8 Punkte, mittlerer Modernisierungsgrad\n" );
                        }
                        if (grad > 8.0d && grad < 18.0d) {
                            text.append( "4: 13 Punkte, überwiegend modernisiert\n" );
                        }
                        if (grad >= 18.0d) {
                            text.append( "5: 18 Punkte, umfassend modernisiert\n" );
                        }
                    }
                    modernisierungsGradText.setText( text.toString() );
                }
            }
        } );

        //
        // --- BEWERTUNG
        //
        Section sectionB = newSection( parent, "Bewertung" );
        sectionB.setLayoutData( new SimpleFormData( SECTION_SPACING ).left( 0 ).right( 100 ).top( section ).create() );
        client = (Composite)sectionB.getClient();

        lastLine = newLine;
        newLine = createLabel( client, "Tatsächliches Baujahr", one().top( null ), SWT.RIGHT );
        createFlaecheField( em.tatsaechlichesBaujahr(), two().top( null ), client, false );

        lastLine = newLine;
        newLine = createLabel( client, "Gesamtnutzungsdauer", one().top( lastLine ), SWT.RIGHT );
        createFlaecheField( em.gesamtNutzungsDauer(), two().top( lastLine ), client, false );

        lastLine = newLine;
        newLine = createLabel( client, "Restnutzungdauer (ohne Modernisierung)", one().top( lastLine ), SWT.RIGHT );
        createFlaecheField( em.restNutzungsDauer(), two().top( lastLine ), client, false );

        lastLine = newLine;
        newLine = createLabel( client, "verlängerte Restnutzungdauer aufgrund Modernisierung", one().top( lastLine ),
                SWT.RIGHT );
        createFlaecheField( em.neueRestNutzungsDauer(), two().top( lastLine ), client, false );

        lastLine = newLine;
        newLine = createLabel( client, "Bereinigtes Baujahr", one().top( lastLine ), SWT.RIGHT );
        createFlaecheField( em.bereinigtesBaujahr(), two().top( lastLine ).bottom( 100 ), client, false );

        site.addFieldListener( restnutzungListener = new IFormFieldListener() {

            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() != IFormFieldListener.VALUE_CHANGE) {
                    return;
                }
                String fieldName = ev.getFieldName();
                if (fieldName.equals( em.modernisierungsGrad().qualifiedName().name() )) {
                    Double grad = ev.getNewValue();
                    Double tatsaechlichesBaujahr = em.tatsaechlichesBaujahr().get();
                    Double gnd = em.gesamtNutzungsDauer().get();
                    Double bereinigtesBaujahr = tatsaechlichesBaujahr;
                    Double neueRND = em.restNutzungsDauer().get();
                    if (tatsaechlichesBaujahr != null && gnd != null) {
                        Double alter = heute - tatsaechlichesBaujahr;
                        neueRND = ErmittlungModernisierungsgradComposite.Mixin.berechneRND( grad, alter, gnd );
                        bereinigtesBaujahr = heute - (gnd - neueRND);
                    }
                    pageSite.setFieldValue( em.neueRestNutzungsDauer().qualifiedName().name(),
                            neueRND != null ? getFormatter( 0 ).format( neueRND ) : null );
                    pageSite.setFieldValue( em.bereinigtesBaujahr().qualifiedName().name(),
                            bereinigtesBaujahr != null ? getFormatter( 0 ).format( bereinigtesBaujahr ) : null );
                }
            }

        } );

        site.addFieldListener( baujahrListener = new FieldListener( em.bereinigtesBaujahr(), em.neueRestNutzungsDauer() ) );
        baujahrUebernehmenAction = new ActionButton( client, new Action( "Baujahr übernehmen" ) {

            @Override
            public void run() {
                VertragComposite vertrag = em.vertrag().get();
                if (vertrag != null) {
                    // NHK oder Ertragswert
                    NHK2010BewertungComposite nhk2010 = NHK2010BewertungComposite.Mixin.forVertrag( vertrag );
                    if (nhk2010 != null) {
                        for (NHK2010BewertungGebaeudeComposite gebaeude : NHK2010BewertungGebaeudeComposite.Mixin
                                .forBewertung( nhk2010 )) {
                            if (em.gebaeudeNummer().get() == gebaeude.laufendeNummer().get()) {
                                KapsPlugin.openEditor( fs, NHK2010BewertungComposite.NAME, nhk2010 );
                                EventManager.instance().publish(
                                        new PropertyChangeEvent( gebaeude, gebaeude.bereinigtesBaujahr()
                                                .qualifiedName().name(), gebaeude.bereinigtesBaujahr().get(),
                                                baujahrListener.get( em.bereinigtesBaujahr() ) ) );
                                EventManager.instance().publish(
                                        new PropertyChangeEvent( gebaeude, gebaeude.gesamtNutzungsDauer()
                                                .qualifiedName().name(), gebaeude.gesamtNutzungsDauer().get(),
                                                baujahrListener.get( em.gesamtNutzungsDauer() ) ) );
                                EventManager.instance().publish(
                                        new PropertyChangeEvent( gebaeude, gebaeude.restNutzungsDauer()
                                                .qualifiedName().name(), gebaeude.restNutzungsDauer().get(),
                                                baujahrListener.get( em.neueRestNutzungsDauer() ) ) );
                            }
                        }
                    }

                }
            }
        } );
        baujahrUebernehmenAction.setLayoutData( three().top( lastLine ).height( 25 ).bottom( 100 ).create() );
    }


    @Override
    public void afterDoLoad( IProgressMonitor monitor )
            throws Exception {
        pageSite.setFieldValue( em.auswirkungZeile1().qualifiedName().name(),
                getFormatter( 2 ).format( em.auswirkungZeile1().get() ) );
        if (em.tatsaechlichesBaujahr().get() != null && em.gesamtNutzungsDauer().get() != null) {
            Double rnd = Math.max( 0.0d, em.gesamtNutzungsDauer().get() - (heute - em.tatsaechlichesBaujahr().get()) );
            // pageSite.fireEvent( this,
            // em.restNutzungsDauer().qualifiedName().name(),
            // IFormFieldListener.VALUE_CHANGE, em
            // .auswirkungZeile1().get() );
            pageSite.setFieldValue( em.restNutzungsDauer().qualifiedName().name(), getFormatter( 0 ).format( rnd ) );
        }
    }


    private void createPunkteField( Property<Double> property, SimpleFormData data, Composite parent, boolean editable,
            int max ) {
        createPreisField( IFormFieldLabel.NO_LABEL, "Der Wert muss zwischen 1,0 und " + max + ",0 liegen", property,
                data, parent, editable );
    }


    @Override
    protected SimpleFormData one() {
        return new SimpleFormData( SPACING ).left( 0 ).right( 40 );
    }


    @Override
    protected SimpleFormData two() {
        return new SimpleFormData( SPACING ).left( 40 ).right( 55 );
    }


    @Override
    protected SimpleFormData three() {
        return new SimpleFormData( SPACING ).left( 55 ).right( 70 );
    }


    @Override
    protected SimpleFormData four() {
        return new SimpleFormData( SPACING ).left( 70 ).right( 85 );
    }


    @Override
    protected SimpleFormData five() {
        return new SimpleFormData( SPACING ).left( 85 ).right( 100 );
    }
}
