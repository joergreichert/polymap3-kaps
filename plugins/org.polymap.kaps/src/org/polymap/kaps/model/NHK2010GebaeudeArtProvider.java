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
package org.polymap.kaps.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.polymap.kaps.model.data.NHK2010GebaeudeArtComposite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class NHK2010GebaeudeArtProvider
        implements ITreeContentProvider {

    private static Log                          log          = LogFactory.getLog( NHK2010GebaeudeArtProvider.class );

    private static NHK2010GebaeudeArtProvider instance;

    private final List<NHK2010GebaeudeArtComposite>      rootElements = new ArrayList<NHK2010GebaeudeArtComposite>();

    private Map<String, NHK2010GebaeudeArtComposite>     registry     = new HashMap<String, NHK2010GebaeudeArtComposite>();

    private Map<String, NHK2010GebaeudeArtComposite>     idRegistry   = new HashMap<String, NHK2010GebaeudeArtComposite>();


    private NHK2010GebaeudeArtProvider() {
        initializeData();
    }


    public final static NHK2010GebaeudeArtProvider instance() {
        if (instance == null) {
            instance = new NHK2010GebaeudeArtProvider();
        }
        return instance;
    }


    public List<NHK2010GebaeudeArtComposite> getElements() {
        return rootElements;
    }


    private void initializeData() {
        initializeGebaeude();
        initializeWerte();
    }


    private void add( NHK2010GebaeudeArtComposite rootElement ) {
        rootElements.add( rootElement );
    }


    private NHK2010GebaeudeArtComposite create( int hauptnr, int nummer, int unternummer, String name ) {
        return create( hauptnr, nummer, unternummer, name, null );
    }


    private NHK2010GebaeudeArtComposite create( int hauptnr, int nummer, int unternummer, String name, String id ) {
        return create( hauptnr, nummer, unternummer, name, id, null, null );
    }


    private NHK2010GebaeudeArtComposite create( int hauptnr, int nummer, int unternummer, String name, String id,
            Integer gndVon, Integer gndBis ) {
        return create( hauptnr, nummer, unternummer, name, id, gndVon, gndBis, null, null, null, null, null, null );
    }


    private NHK2010GebaeudeArtComposite create( int hauptnr, int nummer, int unternummer, String name, String id,
            Integer gndVon, Integer gndBis, Integer korrekturGroesse1, Double korrekturFaktor1,
            Integer korrekturGroesse2, Double korrekturFaktor2, Integer korrekturGroesse3, Double korrekturFaktor3 ) {
        NHK2010GebaeudeArtComposite art = new NHK2010GebaeudeArtComposite( hauptnr, nummer, unternummer, name, id, gndVon, gndBis,
                korrekturGroesse3, korrekturFaktor3, korrekturGroesse3, korrekturFaktor3, korrekturGroesse3,
                korrekturFaktor3 );
        registry.put( art.getNumber(), art );
        if (id != null) {
            if (idRegistry.containsKey( id )) {
                throw new IllegalStateException( "id " + id + " already exists" );
            }
            idRegistry.put( id, art );
        }
        return art;
    }


    private void setValues( Integer hnr, Integer nr, Integer unternr, Integer stufe, Integer wert, Double briBgf,
            Double bgfNutz, Integer bnk, String nutzeinheit ) {
        NHK2010GebaeudeArtComposite art = registry.get( createKey( hnr, nr, unternr ) );
        art.setValues( stufe, wert, briBgf, bgfNutz, bnk, nutzeinheit );
    }


    private String createKey( Integer hnr, Integer nr, Integer unternr ) {
        return hnr + "." + nr + "." + unternr;
    }


    public NHK2010GebaeudeArtComposite gebaeudeForNumber( Integer hnr, Integer nr, Integer unternr ) {
        return registry.get( createKey( hnr, nr, unternr ) );
    }


    public NHK2010GebaeudeArtComposite gebaeudeForId( String id ) {
        return idRegistry.get( id );
    }


    private void initializeGebaeude() {
        NHK2010GebaeudeArtComposite n100 = create( 1, 0, 0, "Ein- und Zweifamilienhäuser freistehend" );
        add( n100 );
        NHK2010GebaeudeArtComposite n110 = create( 1, 1, 0, "Keller-, Erdgeschoss" );
        n100.add( n110 );
        n110.add( create( 1, 1, 1, "Dachgeschoss voll ausgebaut", "1.01" ) );
        n110.add( create( 1, 1, 2, "Dachgeschoss nicht ausgebaut", "1.02" ) );
        n110.add( create( 1, 1, 3, "Flachdach oder flach geneigtes Dach", "1.03" ) );
        NHK2010GebaeudeArtComposite n120 = create( 1, 2, 0, "Keller-, Erd-, Obergeschoss" );
        n100.add( n120 );
        n120.add( create( 1, 2, 1, "Dachgeschoss voll ausgebaut", "1.11" ) );
        n120.add( create( 1, 2, 2, "Dachgeschoss nicht ausgebaut", "1.12" ) );
        n120.add( create( 1, 2, 3, "Flachdach oder flach geneigtes Dach", "1.13" ) );
        NHK2010GebaeudeArtComposite n130 = create( 1, 3, 0, "Erdgeschoss, nicht unterkellert" );
        n100.add( n130 );
        n130.add( create( 1, 3, 1, "Dachgeschoss voll ausgebaut", "1.21" ) );
        n130.add( create( 1, 3, 2, "Dachgeschoss nicht ausgebaut", "1.22" ) );
        n130.add( create( 1, 3, 3, "Flachdach oder flach geneigtes Dach", "1.23" ) );
        NHK2010GebaeudeArtComposite n140 = create( 1, 4, 0, "Erd-, Obergeschoss, nicht unterkellert" );
        n100.add( n140 );
        n140.add( create( 1, 4, 1, "Dachgeschoss voll ausgebaut", "1.31" ) );
        n140.add( create( 1, 4, 2, "Dachgeschoss nicht ausgebaut", "1.32" ) );
        n140.add( create( 1, 4, 3, "Flachdach oder flach geneigtes Dach", "1.33" ) );
        NHK2010GebaeudeArtComposite n200 = create( 2, 0, 0, "Doppel- und Reihenendhäuser" );
        add( n200 );
        NHK2010GebaeudeArtComposite n210 = create( 2, 1, 0, "Keller-, Erdgeschoss" );
        n200.add( n210 );
        n210.add( create( 2, 1, 1, "Dachgeschoss voll ausgebaut", "2.01" ) );
        n210.add( create( 2, 1, 2, "Dachgeschoss nicht ausgebaut", "2.02" ) );
        n210.add( create( 2, 1, 3, "Flachdach oder flach geneigtes Dach", "2.03" ) );
        NHK2010GebaeudeArtComposite n220 = create( 2, 2, 0, "Keller-, Erd-, Obergeschoss" );
        n200.add( n220 );
        n220.add( create( 2, 2, 1, "Dachgeschoss voll ausgebaut", "2.11" ) );
        n220.add( create( 2, 2, 2, "Dachgeschoss nicht ausgebaut", "2.12" ) );
        n220.add( create( 2, 2, 3, "Flachdach oder flach geneigtes Dach", "2.13" ) );
        NHK2010GebaeudeArtComposite n230 = create( 2, 3, 0, "Erdgeschoss, nicht unterkellert" );
        n200.add( n230 );
        n230.add( create( 2, 3, 1, "Dachgeschoss voll ausgebaut", "2.21" ) );
        n230.add( create( 2, 3, 2, "Dachgeschoss nicht ausgebaut", "2.22" ) );
        n230.add( create( 2, 3, 3, "Flachdach oder flach geneigtes Dach", "2.23" ) );
        NHK2010GebaeudeArtComposite n240 = create( 2, 4, 0, "Erd-, Obergeschoss, nicht unterkellert" );
        n200.add( n240 );
        n240.add( create( 2, 4, 1, "Dachgeschoss voll ausgebaut", "2.31" ) );
        n240.add( create( 2, 4, 2, "Dachgeschoss nicht ausgebaut", "2.32" ) );
        n240.add( create( 2, 4, 3, "Flachdach oder flach geneigtes Dach", "2.33" ) );
        NHK2010GebaeudeArtComposite n300 = create( 3, 0, 0, "Reihenmittelhäuser" );
        add( n300 );
        NHK2010GebaeudeArtComposite n310 = create( 3, 1, 0, "Keller-, Erdgeschoss" );
        n300.add( n310 );
        n310.add( create( 3, 1, 1, "Dachgeschoss voll ausgebaut", "3.01" ) );
        n310.add( create( 3, 1, 2, "Dachgeschoss nicht ausgebaut", "3.02" ) );
        n310.add( create( 3, 1, 3, "Flachdach oder flach geneigtes Dach", "3.03" ) );
        NHK2010GebaeudeArtComposite n320 = create( 3, 2, 0, "Keller-, Erd-, Obergeschoss" );
        n300.add( n320 );
        n320.add( create( 3, 2, 1, "Dachgeschoss voll ausgebaut", "3.11" ) );
        n320.add( create( 3, 2, 2, "Dachgeschoss nicht ausgebaut", "3.12" ) );
        n320.add( create( 3, 2, 3, "Flachdach oder flach geneigtes Dach", "3.13" ) );
        NHK2010GebaeudeArtComposite n330 = create( 3, 3, 0, "Erdgeschoss, nicht unterkellert" );
        n300.add( n330 );
        n330.add( create( 3, 3, 1, "Dachgeschoss voll ausgebaut", "3.21" ) );
        n330.add( create( 3, 3, 2, "Dachgeschoss nicht ausgebaut", "3.22" ) );
        n330.add( create( 3, 3, 3, "Flachdach oder flach geneigtes Dach", "3.23" ) );
        NHK2010GebaeudeArtComposite n340 = create( 3, 4, 0, "Erd-, Obergeschoss, nicht unterkellert" );
        n300.add( n340 );
        n340.add( create( 3, 4, 1, "Dachgeschoss voll ausgebaut", "3.31" ) );
        n340.add( create( 3, 4, 2, "Dachgeschoss nicht ausgebaut", "3.32" ) );
        n340.add( create( 3, 4, 3, "Flachdach oder flach geneigtes Dach", "3.33" ) );
        NHK2010GebaeudeArtComposite n410 = create( 4, 0, 0, "Mehrfamilienhäuser" );
        add( n410 );
        n410.add( create( 4, 1, 1, "mit bis zu 6 WE", "4.1", 60, 80 ) );
        n410.add( create( 4, 1, 2, "mit 7 bis 20 WE", "4.2", 60, 80 ) );
        n410.add( create( 4, 1, 3, "mit mehr als 20 WE", "4.3", 60, 80 ) );
        NHK2010GebaeudeArtComposite n510 = create( 5, 0, 0, "Wohnhäuser mit Mischnutzung, Banken und Geschäftshäuser" );
        add( n510 );
        n510.add( create( 5, 1, 1, "Wohnhäuser mit Mischnutzung", "5.1", 60, 80 ) );
        n510.add( create( 5, 1, 2, "Banken und Geschäftshäuser mit Wohnungen", "5.2", 50, 70 ) );
        n510.add( create( 5, 1, 3, "Banken und Geschäftshäuser ohne Wohnungen", "5.3", 50, 70 ) );
        NHK2010GebaeudeArtComposite n610 = create( 6, 0, 0, "Bürogebäude" );
        add( n610 );
        n610.add( create( 6, 1, 1, "Massivbau", "6.1", 50, 70 ) );
        n610.add( create( 6, 1, 2, "Stahlbetonskelettbau", "6.2", 50, 70 ) );
        NHK2010GebaeudeArtComposite n710 = create( 7, 0, 0, "Gemeindezentren, Saalbauten und Veranstaltungsgebäude" );
        add( n710 );
        n710.add( create( 7, 1, 1, "Gemeindezentren", "7.1", 30, 50 ) );
        n710.add( create( 7, 1, 2, "Saalbauten und Veranstaltungsgebäude", "7.2", 30, 50 ) );
        NHK2010GebaeudeArtComposite n810 = create( 8, 0, 0, "Kindergärten, Schulen" );
        add( n810 );
        n810.add( create( 8, 1, 1, "Kindergärten", "8.1", 40, 60 ) );
        n810.add( create( 8, 1, 2, "Allgemeinbildende Schulen, Berufsbildende Schulen", "8.2", 40, 60 ) );
        n810.add( create( 8, 1, 3, "Sonderschulen", "8.3", 40, 60 ) );
        NHK2010GebaeudeArtComposite n910 = create( 9, 0, 0, "Wohnheime, Alten- und Pflegeheime" );
        add( n910 );
        n910.add( create( 9, 1, 1, "Wohnheime und Internate", "9.1", 40, 60 ) );
        n910.add( create( 9, 1, 2, "Alten- und Pflegeheime", "9.2", 40, 60 ) );
        NHK2010GebaeudeArtComposite n1010 = create( 10, 0, 0, "Krankenhäuser, Tageskliniken" );
        add( n1010 );
        n1010.add( create( 10, 1, 1, "Krankenhäuser und Kliniken", "10.1", 30, 50 ) );
        n1010.add( create( 10, 1, 2, "Tageskliniken und Ärztehäuser", "10.2", 30, 50 ) );
        NHK2010GebaeudeArtComposite n1110 = create( 11, 0, 0, "Beherbergungsstätten, Verpflegungseinrichtungen" );
        add( n1110 );
        n1110.add( create( 11, 1, 1, "Hotels", "11.1", 30, 50 ) );
        NHK2010GebaeudeArtComposite n1210 = create( 12, 0, 0, "Sporthallen, Freizeitbäder und Heilbäder" );
        add( n1210 );
        n1210.add( create( 12, 1, 1, "Sporthallen (Einfeldhallen)", "12.1", 30, 50 ) );
        n1210.add( create( 12, 1, 2, "Sporthallen (Dreifeldhallen und Mehrzweckhallen)", "12.2", 30, 50 ) );
        n1210.add( create( 12, 1, 3, "Tennishallen", "12.3", 30, 50 ) );
        n1210.add( create( 12, 1, 4, "Freizeitbäder und Heilbäder", "12.4", 30, 50 ) );
        NHK2010GebaeudeArtComposite n1310 = create( 13, 0, 0, "Verbrauchermärkte, Kauf- und Warenhäuser, Autohäuser" );
        add( n1310 );
        n1310.add( create( 13, 1, 1, "Verbrauchermärkte", "13.1", 20, 40 ) );
        n1310.add( create( 13, 1, 2, "Kauf- und Warenhäuser", "13.2", 40, 60 ) );
        n1310.add( create( 13, 1, 3, "Autohäuser ohne Werkstatt", "13.3", 20, 40 ) );
        NHK2010GebaeudeArtComposite n1410 = create( 14, 0, 0, "Garagen" );
        add( n1410 );
        n1410.add( create( 14, 1, 1, "Einzelgaragen und Mehrfachgaragen", "14.1", 50, 70 ) );
        n1410.add( create( 14, 1, 2, "Hochgaragen", "14.2", 30, 50 ) );
        n1410.add( create( 14, 1, 3, "Tiefgaragen", "14.3", 30, 50 ) );
        n1410.add( create( 14, 1, 4, "Nutzfahrzeuggaragen", "14.4", 30, 50 ) );
        NHK2010GebaeudeArtComposite n1510 = create( 15, 0, 0, "Betriebs- und Werkstätten, Produktionsgebäude" );
        add( n1510 );
        n1510.add( create( 15, 1, 1, "Betriebs- und Werkstätten, eingeschossig", "15.1", 30, 50 ) );
        n1510.add( create( 15, 1, 2, "Betriebs- und Werkstätten, mehrgeschossig, ohne Hallenanteil", "15.2", 30, 50 ) );
        n1510.add( create( 15, 1, 3, "Betriebs- und Werkstätten, mehrgeschossig, hoher Hallenanteil", "15.3", 30, 50 ) );
        n1510.add( create( 15, 1, 4, "Industrielle Produktionsgebäude, Massivbauweise", "15.4", 30, 50 ) );
        n1510.add( create( 15, 1, 5, "Industrielle Produktionsgebäude, überwiegend Skelettbauweise", "15.5", 30, 50 ) );
        NHK2010GebaeudeArtComposite n1610 = create( 16, 0, 0, "Lagergebäude" );
        add( n1610 );
        n1610.add( create( 16, 1, 1, "Lagergebäude ohne Mischnutzung, Kaltlager", "16.1", 30, 50 ) );
        n1610.add( create( 16, 1, 2, "Lagergebäude mit bis zu 25 % Mischnutzung", "16.2", 30, 50 ) );
        n1610.add( create( 16, 1, 3, "Lagergebäude mit mehr als 25 % Mischnutzung", "16.3", 30, 50 ) );
        NHK2010GebaeudeArtComposite n1710 = create( 17, 0, 0, "Sonstige Gebäude" );
        add( n1710 );
        n1710.add( create( 17, 1, 1, "Museen", "17.1" ) );
        n1710.add( create( 17, 1, 2, "Theater", "17.2" ) );
        n1710.add( create( 17, 1, 3, "Sakralbauten", "17.3" ) );
        n1710.add( create( 17, 1, 4, "Friedhofsgebäude", "17.4" ) );
        NHK2010GebaeudeArtComposite n1800 = create( 18, 0, 0, "Landwirtschaftliche Betriebsgebäude" );
        add( n1800 );
        NHK2010GebaeudeArtComposite n1810 = create( 18, 1, 0, "Reithallen, Pferdeställe" );
        n1800.add( n1810 );
        n1810.add( create( 18, 1, 1, "Reithallen", "18.1.1", 20, 40, 500, 1.2, 1000, 1.0, 1000, 1.0 ) );
        n1810.add( create( 18, 1, 2, "Pferdeställe", "18.1.2", 20, 40, 250, 1.2, 500, 1.0, 500, 1.0 ) );
        NHK2010GebaeudeArtComposite n1820 = create( 18, 2, 0, "Rinderställe, Melkhäuser" );
        n1800.add( n1820 );
        n1820.add( create( 18, 2, 1, "Kälberställe", "18.2.1", 20, 40, 100, 1.2, 150, 1.0, 150, 1.0 ) );
        n1820.add( create( 18, 2, 2, "Jungvieh- und Mastbullen- und Milchviehställe ohne Melkstand und Warteraum",
                "18.2.2", 20, 40, 500, 1.2, 1000, 1.0, 1000, 1.0 ) );
        n1820.add( create( 18, 2, 3, "Milchviehställe mit Melkstand und Milchlager", "18.2.3", 20, 40, 1000, 1.2, 1500,
                1.0, 1500, 1.0 ) );
        n1820.add( create( 18, 2, 4,
                "Melkhäuser mit Milchlager und Nebenräume als Einzelgebäude ohne Warteraum und Selektion", "18.2.4",
                20, 40, 100, 1.2, 150, 1.0, 150, 1.0 ) );
        NHK2010GebaeudeArtComposite n1830 = create( 18, 3, 0, "Schweineställe" );
        n1800.add( n1830 );
        n1830.add( create( 18, 3, 1, "Ferkelaufzuchtställe", "18.3.1", 20, 40, 400, 1.2, 600, 1.0, 600, 1.0 ) );
        n1830.add( create( 18, 3, 2, "Mastschweineställe", "18.3.2", 20, 40, 750, 1.2, 1250, 1.0, 1250, 1.0 ) );
        n1830.add( create( 18, 3, 3, "Zuchtschweineställe, Deck-, Warte- und Abferkelbereich", "18.3.3", 20, 40, 750,
                1.2, 1250, 1.0, 1250, 1.0 ) );
        n1830.add( create( 18, 3, 4, "Abferkelstall als Einzelgebäude", "18.3.4", 20, 40, 200, 1.2, 400, 1.0, 400, 1.0 ) );
        NHK2010GebaeudeArtComposite n1840 = create( 18, 4, 0, "Geflügelställe" );
        n1800.add( n1840 );
        n1840.add( create( 18, 4, 1, "Mastgeflügel, Bodenhaltung (Hähnchen, Puten, Gänse)", "18.4.1", 20, 40, 1000,
                1.2, 1900, 1.0, 1900, 1.0 ) );
        n1840.add( create( 18, 4, 2, "Legehennen, Bodenhaltung", "18.4.2", 20, 40, 1000, 1.2, 2500, 1.0, 2500, 1.0 ) );
        n1840.add( create( 18, 4, 3, "Legehennen, Volierenhaltung", "18.4.3", 20, 40, 500, 1.2, 1600, 1.0, 1600, 1.0 ) );
        n1840.add( create( 18, 4, 4, "Legehennen, Kleingruppenhaltung, ausgestalteter Käfig", "18.4.4", 20, 40, 500,
                1.2, 1200, 1.0, 1200, 1.0 ) );
        NHK2010GebaeudeArtComposite n1850 = create( 18, 5, 0, "Landwirtschaftliche Mehrzweckhallen" );
        n1800.add( n1850 );
        n1850.add( create( 18, 5, 1, "Landwirtschaftliche Mehrzweckhallen", "18.5", 20, 40, 250, 1.2, 800, 1.0, 800,
                1.0 ) );
        NHK2010GebaeudeArtComposite n1860 = create( 18, 6, 0, "Außenanlagen zu allen landwirtschaftlichen Betriebsgebäuden" );
        n1800.add( n1860 );
        n1860.add( create( 18, 6, 1, "Außenanlagen zu allen landwirtschaftlichen Betriebsgebäuden", "18.6", 20, 40 ) );
    }


    private void initializeWerte() {
        setValues( 1, 1, 1, 1, 655, null, null, 17, null );

        setValues( 1, 1, 1, 1, 655, null, null, 17, null );
        setValues( 1, 1, 1, 2, 725, null, null, 17, null );
        setValues( 1, 1, 1, 3, 835, null, null, 17, null );
        setValues( 1, 1, 1, 4, 1005, null, null, 17, null );
        setValues( 1, 1, 1, 5, 1260, null, null, 17, null );
        setValues( 1, 1, 2, 1, 545, null, null, 17, null );
        setValues( 1, 1, 2, 2, 605, null, null, 17, null );
        setValues( 1, 1, 2, 3, 695, null, null, 17, null );
        setValues( 1, 1, 2, 4, 840, null, null, 17, null );
        setValues( 1, 1, 2, 5, 1050, null, null, 17, null );
        setValues( 1, 1, 3, 1, 705, null, null, 17, null );
        setValues( 1, 1, 3, 2, 785, null, null, 17, null );
        setValues( 1, 1, 3, 3, 900, null, null, 17, null );
        setValues( 1, 1, 3, 4, 1085, null, null, 17, null );
        setValues( 1, 1, 3, 5, 1360, null, null, 17, null );
        setValues( 1, 2, 1, 1, 655, null, null, 17, null );
        setValues( 1, 2, 1, 2, 725, null, null, 17, null );
        setValues( 1, 2, 1, 3, 835, null, null, 17, null );
        setValues( 1, 2, 1, 4, 1005, null, null, 17, null );
        setValues( 1, 2, 1, 5, 1260, null, null, 17, null );
        setValues( 1, 2, 2, 1, 570, null, null, 17, null );
        setValues( 1, 2, 2, 2, 635, null, null, 17, null );
        setValues( 1, 2, 2, 3, 730, null, null, 17, null );
        setValues( 1, 2, 2, 4, 880, null, null, 17, null );
        setValues( 1, 2, 2, 5, 1100, null, null, 17, null );
        setValues( 1, 2, 3, 3, 850, null, null, 17, null );
        setValues( 1, 2, 3, 4, 1025, null, null, 17, null );
        setValues( 1, 2, 3, 5, 1285, null, null, 17, null );
        setValues( 1, 2, 3, 1, 665, null, null, 17, null );
        setValues( 1, 2, 3, 2, 740, null, null, 17, null );
        setValues( 1, 3, 1, 2, 875, null, null, 17, null );
        setValues( 1, 3, 1, 3, 1005, null, null, 17, null );
        setValues( 1, 3, 1, 4, 1215, null, null, 17, null );
        setValues( 1, 3, 1, 5, 1515, null, null, 17, null );
        setValues( 1, 3, 1, 1, 790, null, null, 17, null );
        setValues( 1, 3, 2, 2, 650, null, null, 17, null );
        setValues( 1, 3, 2, 3, 745, null, null, 17, null );
        setValues( 1, 3, 2, 4, 900, null, null, 17, null );
        setValues( 1, 3, 2, 5, 1125, null, null, 17, null );
        setValues( 1, 3, 2, 1, 585, null, null, 17, null );
        setValues( 1, 3, 3, 2, 1025, null, null, 17, null );
        setValues( 1, 3, 3, 3, 1180, null, null, 17, null );
        setValues( 1, 3, 3, 4, 1420, null, null, 17, null );
        setValues( 1, 3, 3, 5, 1775, null, null, 17, null );
        setValues( 1, 3, 3, 1, 920, null, null, 17, null );
        setValues( 1, 4, 1, 2, 800, null, null, 17, null );
        setValues( 1, 4, 1, 3, 920, null, null, 17, null );
        setValues( 1, 4, 1, 4, 1105, null, null, 17, null );
        setValues( 1, 4, 1, 5, 1385, null, null, 17, null );
        setValues( 1, 4, 1, 1, 720, null, null, 17, null );
        setValues( 1, 4, 2, 2, 690, null, null, 17, null );
        setValues( 1, 4, 2, 3, 790, null, null, 17, null );
        setValues( 1, 4, 2, 4, 955, null, null, 17, null );
        setValues( 1, 4, 2, 5, 1190, null, null, 17, null );
        setValues( 1, 4, 2, 1, 620, null, null, 17, null );
        setValues( 1, 4, 3, 2, 870, null, null, 17, null );
        setValues( 1, 4, 3, 3, 1000, null, null, 17, null );
        setValues( 1, 4, 3, 4, 1205, null, null, 17, null );
        setValues( 1, 4, 3, 5, 1510, null, null, 17, null );
        setValues( 1, 4, 3, 1, 785, null, null, 17, null );
        setValues( 2, 1, 1, 2, 685, null, null, 17, null );
        setValues( 2, 1, 1, 3, 785, null, null, 17, null );
        setValues( 2, 1, 1, 4, 945, null, null, 17, null );
        setValues( 2, 1, 1, 5, 1180, null, null, 17, null );
        setValues( 2, 1, 1, 1, 615, null, null, 17, null );
        setValues( 2, 1, 2, 2, 570, null, null, 17, null );
        setValues( 2, 1, 2, 3, 655, null, null, 17, null );
        setValues( 2, 1, 2, 4, 790, null, null, 17, null );
        setValues( 2, 1, 2, 5, 985, null, null, 17, null );
        setValues( 2, 1, 2, 1, 515, null, null, 17, null );
        setValues( 2, 1, 3, 2, 735, null, null, 17, null );
        setValues( 2, 1, 3, 3, 845, null, null, 17, null );
        setValues( 2, 1, 3, 4, 1020, null, null, 17, null );
        setValues( 2, 1, 3, 5, 1275, null, null, 17, null );
        setValues( 2, 1, 3, 1, 665, null, null, 17, null );
        setValues( 2, 2, 1, 2, 685, null, null, 17, null );
        setValues( 2, 2, 1, 3, 785, null, null, 17, null );
        setValues( 2, 2, 1, 4, 945, null, null, 17, null );
        setValues( 2, 2, 1, 5, 1180, null, null, 17, null );
        setValues( 2, 2, 1, 1, 615, null, null, 17, null );
        setValues( 2, 2, 2, 2, 595, null, null, 17, null );
        setValues( 2, 2, 2, 3, 685, null, null, 17, null );
        setValues( 2, 2, 2, 4, 825, null, null, 17, null );
        setValues( 2, 2, 2, 5, 1035, null, null, 17, null );
        setValues( 2, 2, 2, 1, 535, null, null, 17, null );
        setValues( 2, 2, 3, 2, 695, null, null, 17, null );
        setValues( 2, 2, 3, 3, 800, null, null, 17, null );
        setValues( 2, 2, 3, 4, 965, null, null, 17, null );
        setValues( 2, 2, 3, 5, 1205, null, null, 17, null );
        setValues( 2, 2, 3, 1, 625, null, null, 17, null );
        setValues( 2, 3, 1, 2, 825, null, null, 17, null );
        setValues( 2, 3, 1, 3, 945, null, null, 17, null );
        setValues( 2, 3, 1, 4, 1140, null, null, 17, null );
        setValues( 2, 3, 1, 5, 1425, null, null, 17, null );
        setValues( 2, 3, 1, 1, 740, null, null, 17, null );
        setValues( 2, 3, 2, 2, 610, null, null, 17, null );
        setValues( 2, 3, 2, 3, 700, null, null, 17, null );
        setValues( 2, 3, 2, 4, 845, null, null, 17, null );
        setValues( 2, 3, 2, 5, 1055, null, null, 17, null );
        setValues( 2, 3, 2, 1, 550, null, null, 17, null );
        setValues( 2, 3, 3, 2, 965, null, null, 17, null );
        setValues( 2, 3, 3, 3, 1105, null, null, 17, null );
        setValues( 2, 3, 3, 4, 1335, null, null, 17, null );
        setValues( 2, 3, 3, 5, 1670, null, null, 17, null );
        setValues( 2, 3, 3, 1, 865, null, null, 17, null );
        setValues( 2, 4, 1, 2, 750, null, null, 17, null );
        setValues( 2, 4, 1, 3, 865, null, null, 17, null );
        setValues( 2, 4, 1, 4, 1040, null, null, 17, null );
        setValues( 2, 4, 1, 5, 1300, null, null, 17, null );
        setValues( 2, 4, 1, 1, 675, null, null, 17, null );
        setValues( 2, 4, 2, 2, 645, null, null, 17, null );
        setValues( 2, 4, 2, 3, 745, null, null, 17, null );
        setValues( 2, 4, 2, 4, 895, null, null, 17, null );
        setValues( 2, 4, 2, 5, 1120, null, null, 17, null );
        setValues( 2, 4, 2, 1, 580, null, null, 17, null );
        setValues( 2, 4, 3, 2, 820, null, null, 17, null );
        setValues( 2, 4, 3, 3, 940, null, null, 17, null );
        setValues( 2, 4, 3, 4, 1135, null, null, 17, null );
        setValues( 2, 4, 3, 5, 1415, null, null, 17, null );
        setValues( 2, 4, 3, 1, 735, null, null, 17, null );
        setValues( 3, 1, 1, 2, 640, null, null, 17, null );
        setValues( 3, 1, 1, 3, 735, null, null, 17, null );
        setValues( 3, 1, 1, 4, 885, null, null, 17, null );
        setValues( 3, 1, 1, 5, 1105, null, null, 17, null );
        setValues( 3, 1, 1, 1, 575, null, null, 17, null );
        setValues( 3, 1, 2, 2, 535, null, null, 17, null );
        setValues( 3, 1, 2, 3, 615, null, null, 17, null );
        setValues( 3, 1, 2, 4, 740, null, null, 17, null );
        setValues( 3, 1, 2, 5, 925, null, null, 17, null );
        setValues( 3, 1, 2, 1, 480, null, null, 17, null );
        setValues( 3, 1, 3, 2, 690, null, null, 17, null );
        setValues( 3, 1, 3, 3, 795, null, null, 17, null );
        setValues( 3, 1, 3, 4, 955, null, null, 17, null );
        setValues( 3, 1, 3, 5, 1195, null, null, 17, null );
        setValues( 3, 1, 3, 1, 620, null, null, 17, null );
        setValues( 3, 2, 1, 2, 640, null, null, 17, null );
        setValues( 3, 2, 1, 3, 735, null, null, 17, null );
        setValues( 3, 2, 1, 4, 885, null, null, 17, null );
        setValues( 3, 2, 1, 5, 1105, null, null, 17, null );
        setValues( 3, 2, 1, 1, 575, null, null, 17, null );
        setValues( 3, 2, 2, 2, 560, null, null, 17, null );
        setValues( 3, 2, 2, 3, 640, null, null, 17, null );
        setValues( 3, 2, 2, 4, 775, null, null, 17, null );
        setValues( 3, 2, 2, 5, 965, null, null, 17, null );
        setValues( 3, 2, 2, 1, 505, null, null, 17, null );
        setValues( 3, 2, 3, 2, 650, null, null, 17, null );
        setValues( 3, 2, 3, 3, 750, null, null, 17, null );
        setValues( 3, 2, 3, 4, 905, null, null, 17, null );
        setValues( 3, 2, 3, 5, 1130, null, null, 17, null );
        setValues( 3, 2, 3, 1, 585, null, null, 17, null );
        setValues( 3, 3, 1, 2, 770, null, null, 17, null );
        setValues( 3, 3, 1, 3, 885, null, null, 17, null );
        setValues( 3, 3, 1, 4, 1065, null, null, 17, null );
        setValues( 3, 3, 1, 5, 1335, null, null, 17, null );
        setValues( 3, 3, 1, 1, 695, null, null, 17, null );
        setValues( 3, 3, 2, 2, 570, null, null, 17, null );
        setValues( 3, 3, 2, 3, 655, null, null, 17, null );
        setValues( 3, 3, 2, 4, 790, null, null, 17, null );
        setValues( 3, 3, 2, 5, 990, null, null, 17, null );
        setValues( 3, 3, 2, 1, 515, null, null, 17, null );
        setValues( 3, 3, 3, 2, 900, null, null, 17, null );
        setValues( 3, 3, 3, 3, 1035, null, null, 17, null );
        setValues( 3, 3, 3, 4, 1250, null, null, 17, null );
        setValues( 3, 3, 3, 5, 1560, null, null, 17, null );
        setValues( 3, 3, 3, 1, 810, null, null, 17, null );
        setValues( 3, 4, 1, 2, 705, null, null, 17, null );
        setValues( 3, 4, 1, 3, 810, null, null, 17, null );
        setValues( 3, 4, 1, 4, 975, null, null, 17, null );
        setValues( 3, 4, 1, 5, 1215, null, null, 17, null );
        setValues( 3, 4, 1, 1, 635, null, null, 17, null );
        setValues( 3, 4, 2, 2, 605, null, null, 17, null );
        setValues( 3, 4, 2, 3, 695, null, null, 17, null );
        setValues( 3, 4, 2, 4, 840, null, null, 17, null );
        setValues( 3, 4, 2, 5, 1050, null, null, 17, null );
        setValues( 3, 4, 2, 1, 545, null, null, 17, null );
        setValues( 3, 4, 3, 2, 765, null, null, 17, null );
        setValues( 3, 4, 3, 3, 880, null, null, 17, null );
        setValues( 3, 4, 3, 4, 1060, null, null, 17, null );
        setValues( 3, 4, 3, 5, 1325, null, null, 17, null );
        setValues( 3, 4, 3, 1, 690, null, null, 17, null );
        setValues( 4, 1, 1, 3, 825, 2.85, 1.8, 19, "m²/Wohnfläche" );
        setValues( 4, 1, 1, 4, 985, 2.86, 1.9, 19, "m²/Wohnfläche" );
        setValues( 4, 1, 1, 5, 1190, 2.78, 2.1, 19, "m²/Wohnfläche" );
        setValues( 4, 1, 2, 3, 765, 2.85, 1.8, 19, "m²/Wohnfläche" );
        setValues( 4, 1, 2, 4, 915, 2.83, 2.4, 19, "m²/Wohnfläche" );
        setValues( 4, 1, 2, 5, 1105, 2.8, 2.1, 19, "m²/Wohnfläche" );
        setValues( 4, 1, 3, 3, 755, 2.96, 2.1, 19, "m²/Wohnfläche" );
        setValues( 4, 1, 3, 4, 900, 2.95, 2.5, 19, "m²/Wohnfläche" );
        setValues( 4, 1, 3, 5, 1090, 2.91, 2.0, 19, "m²/Wohnfläche" );
        setValues( 5, 1, 1, 3, 860, 2.81, null, 18, null );
        setValues( 5, 1, 1, 4, 1085, 3.26, null, 18, null );
        setValues( 5, 1, 1, 5, 1375, 3.14, null, 18, null );
        setValues( 5, 1, 2, 3, 890, 3.19, null, 22, null );
        setValues( 5, 1, 2, 4, 1375, null, null, 22, null );
        setValues( 5, 1, 2, 5, 1720, null, null, 22, null );
        setValues( 5, 1, 3, 3, 930, null, null, 22, null );
        setValues( 5, 1, 3, 4, 1520, null, null, 22, null );
        setValues( 5, 1, 3, 5, 1900, null, null, 22, null );
        setValues( 6, 1, 1, 3, 1040, 3.35, 44.4, 18, "m²/Arbeitsplatz" );
        setValues( 6, 1, 1, 4, 1685, 3.62, 47.2, 18, "m²/Arbeitsplatz" );
        setValues( 6, 1, 1, 5, 1900, 3.5, 71.9, 18, "m²/Arbeitsplatz" );
        setValues( 6, 1, 2, 3, 1175, 3.51, 30.2, 18, "m²/Arbeitsplatz" );
        setValues( 6, 1, 2, 4, 1840, 3.38, 37.3, 18, "m²/Arbeitsplatz" );
        setValues( 6, 1, 2, 5, 2090, 3.92, 39.4, 18, "m²/Arbeitsplatz" );
        setValues( 7, 1, 1, 3, 1130, 3.4, null, 18, null );
        setValues( 7, 1, 1, 4, 1425, 3.56, null, 18, null );
        setValues( 7, 1, 1, 5, 1905, 3.73, null, 18, null );
        setValues( 7, 1, 2, 3, 1355, 4.11, null, 18, null );
        setValues( 7, 1, 2, 4, 1595, 4.1, null, 18, null );
        setValues( 7, 1, 2, 5, 2085, 4.31, null, 18, null );
        setValues( 8, 1, 1, 3, 1300, 3.9, 238.1, 20, "m²/Gruppe" );
        setValues( 8, 1, 1, 4, 1495, 3.86, 217.4, 20, "m²/Gruppe" );
        setValues( 8, 1, 1, 5, 1900, 3.82, 217.4, 20, "m²/Gruppe" );
        setValues( 8, 1, 2, 3, 1450, 3.94, 11.4, 21, "m²/Schüler" );
        setValues( 8, 1, 2, 4, 1670, 4.23, 17.6, 21, "m²/Schüler" );
        setValues( 8, 1, 2, 5, 2120, 3.92, 17.2, 21, "m²/Schüler" );
        setValues( 8, 1, 3, 3, 1585, null, null, 17, null );
        setValues( 8, 1, 3, 4, 1820, null, null, 17, null );
        setValues( 8, 1, 3, 5, 2315, null, null, 17, null );
        setValues( 9, 1, 1, 3, 1000, 2.86, 33.1, 18, "m²/Bett" );
        setValues( 9, 1, 1, 4, 1225, 3.34, 49.0, 18, "m²/Bett" );
        setValues( 9, 1, 1, 5, 1425, 3.7, 35.2, 18, "m²/Bett" );
        setValues( 9, 1, 2, 3, 1170, 3.02, 72.5, 18, "m²/Bett" );
        setValues( 9, 1, 2, 4, 1435, 3.12, 74.1, 18, "m²/Bett" );
        setValues( 9, 1, 2, 5, 1665, 3.33, 71.4, 18, "m²/Bett" );
        setValues( 10, 1, 1, 3, 1720, 3.87, 98.0, 21, "m²/Bett" );
        setValues( 10, 1, 1, 4, 2080, 3.38, null, 21, null );
        setValues( 10, 1, 1, 5, 2765, null, null, 21, null );
        setValues( 10, 1, 2, 3, 1585, null, null, 21, null );
        setValues( 10, 1, 2, 4, 1945, null, null, 21, null );
        setValues( 10, 1, 2, 5, 2255, null, null, 21, null );
        setValues( 11, 1, 1, 3, 1385, null, null, 21, null );
        setValues( 11, 1, 1, 4, 1805, 3.17, 38.5, 21, "m²/Bett" );
        setValues( 11, 1, 1, 5, 2595, null, null, 21, null );
        setValues( 12, 1, 1, 3, 1320, 5.98, null, 17, null );
        setValues( 12, 1, 1, 4, 1670, null, null, 17, null );
        setValues( 12, 1, 1, 5, 1955, null, null, 17, null );
        setValues( 12, 1, 2, 3, 1490, 6.07, null, 19, null );
        setValues( 12, 1, 2, 4, 1775, null, null, 19, null );
        setValues( 12, 1, 2, 5, 2070, null, null, 19, null );
        setValues( 12, 1, 3, 3, 1010, 4.84, null, 17, null );
        setValues( 12, 1, 3, 4, 1190, null, null, 17, null );
        setValues( 12, 1, 3, 5, 1555, null, null, 17, null );
        setValues( 12, 1, 4, 3, 2450, 7.15, null, 24, null );
        setValues( 12, 1, 4, 4, 2985, null, null, 24, null );
        setValues( 12, 1, 4, 5, 3840, null, null, 24, null );
        setValues( 13, 1, 1, 3, 720, 3.4, null, 16, null );
        setValues( 13, 1, 1, 4, 870, null, null, 16, null );
        setValues( 13, 1, 1, 5, 1020, null, null, 16, null );
        setValues( 13, 1, 2, 3, 1320, 4.85, null, 22, null );
        setValues( 13, 1, 2, 4, 1585, null, null, 22, null );
        setValues( 13, 1, 2, 5, 1850, null, null, 22, null );
        setValues( 13, 1, 3, 3, 940, 3.47, null, 21, null );
        setValues( 13, 1, 3, 4, 1240, null, null, 21, null );
        setValues( 13, 1, 3, 5, 1480, null, null, 21, null );
        setValues( 14, 1, 1, 3, 245, 2.7, 20.0, 12, "m²/Stellplatz" );
        setValues( 14, 1, 1, 4, 485, 2.8, 22.0, 12, "m²/Stellplatz" );
        setValues( 14, 1, 1, 5, 780, 2.9, 23.0, 12, "m²/Stellplatz" );
        setValues( 14, 1, 2, 3, 480, 2.95, 25.3, 15, "m²/Stellplatz" );
        setValues( 14, 1, 2, 4, 655, null, null, 15, null );
        setValues( 14, 1, 2, 5, 780, null, null, 15, null );
        setValues( 14, 1, 3, 3, 560, 2.97, 26.5, 15, "m²/Stellplatz" );
        setValues( 14, 1, 3, 4, 715, null, null, 15, null );
        setValues( 14, 1, 3, 5, 850, null, null, 15, null );
        setValues( 14, 1, 4, 3, 530, 5.41, 95.2, 13, "m²/Stellplatz" );
        setValues( 14, 1, 4, 4, 680, null, null, 13, null );
        setValues( 14, 1, 4, 5, 810, null, null, 13, null );
        setValues( 15, 1, 1, 3, 970, 5.56, null, 19, null );
        setValues( 15, 1, 1, 4, 1165, null, null, 19, null );
        setValues( 15, 1, 1, 5, 1430, null, null, 19, null );
        setValues( 15, 1, 2, 3, 910, 4.19, null, 19, null );
        setValues( 15, 1, 2, 4, 1090, null, null, 19, null );
        setValues( 15, 1, 2, 5, 1340, null, null, 19, null );
        setValues( 15, 1, 3, 3, 620, 4.73, null, 19, null );
        setValues( 15, 1, 3, 4, 860, null, null, 19, null );
        setValues( 15, 1, 3, 5, 1070, null, null, 19, null );
        setValues( 15, 1, 4, 3, 950, 5.1, null, 19, null );
        setValues( 15, 1, 4, 4, 1155, null, null, 19, null );
        setValues( 15, 1, 4, 5, 1440, null, null, 19, null );
        setValues( 15, 1, 5, 3, 700, 6.77, null, 18, null );
        setValues( 15, 1, 5, 4, 965, null, null, 18, null );
        setValues( 15, 1, 5, 5, 1260, null, null, 18, null );
        setValues( 16, 1, 1, 3, 350, 6.71, null, 16, null );
        setValues( 16, 1, 1, 4, 490, null, null, 16, null );
        setValues( 16, 1, 1, 5, 640, null, null, 16, null );
        setValues( 16, 1, 2, 3, 550, 6.1, null, 17, null );
        setValues( 16, 1, 2, 4, 690, null, null, 17, null );
        setValues( 16, 1, 2, 5, 880, null, null, 17, null );
        setValues( 16, 1, 3, 3, 890, 5.58, null, 18, null );
        setValues( 16, 1, 3, 4, 1095, null, null, 18, null );
        setValues( 16, 1, 3, 5, 1340, null, null, 18, null );
        setValues( 17, 1, 1, 3, 1880, 4.5, null, 18, null );
        setValues( 17, 1, 1, 4, 2295, null, null, 18, null );
        setValues( 17, 1, 1, 5, 2670, null, null, 18, null );
        setValues( 17, 1, 2, 3, 2070, 4.48, null, 22, null );
        setValues( 17, 1, 2, 4, 2625, null, null, 22, null );
        setValues( 17, 1, 2, 5, 3680, null, null, 22, null );
        setValues( 17, 1, 3, 3, 1510, 4.0, null, 16, null );
        setValues( 17, 1, 3, 4, 2060, null, null, 16, null );
        setValues( 17, 1, 3, 5, 2335, null, null, 16, null );
        setValues( 17, 1, 4, 3, 1320, 5.34, null, 19, null );
        setValues( 17, 1, 4, 4, 1490, null, null, 19, null );
        setValues( 17, 1, 4, 5, 1720, null, null, 19, null );
        setValues( 18, 1, 1, 3, 235, null, null, 12, null );
        setValues( 18, 1, 1, 4, 260, null, null, 12, null );
        setValues( 18, 1, 1, 5, 310, null, null, 12, null );
        setValues( 18, 1, 2, 3, 365, null, null, 12, null );
        setValues( 18, 1, 2, 4, 520, null, null, 12, null );
        setValues( 18, 1, 2, 5, 625, null, null, 12, null );
        setValues( 18, 2, 1, 3, 480, null, null, 12, null );
        setValues( 18, 2, 1, 4, 540, null, null, 12, null );
        setValues( 18, 2, 1, 5, 650, null, null, 12, null );
        setValues( 18, 2, 2, 3, 290, null, null, 12, null );
        setValues( 18, 2, 2, 4, 325, null, null, 12, null );
        setValues( 18, 2, 2, 5, 390, null, null, 12, null );
        setValues( 18, 2, 3, 3, 325, null, null, 12, null );
        setValues( 18, 2, 3, 4, 365, null, null, 12, null );
        setValues( 18, 2, 3, 5, 440, null, null, 12, null );
        setValues( 18, 2, 4, 3, 1170, null, null, 12, null );
        setValues( 18, 2, 4, 4, 1300, null, null, 12, null );
        setValues( 18, 2, 4, 5, 1560, null, null, 12, null );
        setValues( 18, 3, 1, 3, 455, null, null, 12, null );
        setValues( 18, 3, 1, 4, 505, null, null, 12, null );
        setValues( 18, 3, 1, 5, 610, null, null, 12, null );
        setValues( 18, 3, 2, 3, 415, null, null, 12, null );
        setValues( 18, 3, 2, 4, 470, null, null, 12, null );
        setValues( 18, 3, 2, 5, 570, null, null, 12, null );
        setValues( 18, 3, 3, 3, 470, null, null, 12, null );
        setValues( 18, 3, 3, 4, 520, null, null, 12, null );
        setValues( 18, 3, 3, 5, 625, null, null, 12, null );
        setValues( 18, 3, 4, 3, 525, null, null, 12, null );
        setValues( 18, 3, 4, 4, 585, null, null, 12, null );
        setValues( 18, 3, 4, 5, 700, null, null, 12, null );
        setValues( 18, 4, 1, 3, 260, null, null, 12, null );
        setValues( 18, 4, 1, 4, 290, null, null, 12, null );
        setValues( 18, 4, 1, 5, 350, null, null, 12, null );
        setValues( 18, 4, 2, 3, 420, null, null, 12, null );
        setValues( 18, 4, 2, 4, 470, null, null, 12, null );
        setValues( 18, 4, 2, 5, 560, null, null, 12, null );
        setValues( 18, 4, 3, 3, 610, null, null, 12, null );
        setValues( 18, 4, 3, 4, 675, null, null, 12, null );
        setValues( 18, 4, 3, 5, 810, null, null, 12, null );
        setValues( 18, 4, 4, 3, 675, null, null, 12, null );
        setValues( 18, 4, 4, 4, 740, null, null, 12, null );
        setValues( 18, 4, 4, 5, 895, null, null, 12, null );
        setValues( 18, 5, 1, 3, 245, null, null, 11, null );
        setValues( 18, 5, 1, 4, 270, null, null, 11, null );
        setValues( 18, 5, 1, 5, 350, null, null, 11, null );
    }


    @Override
    public Object[] getElements( Object inputElement ) {
        return rootElements.toArray();
    }


    @Override
    public Object[] getChildren( Object parentElement ) {
        return ((NHK2010GebaeudeArtComposite)parentElement).getChildren().toArray();
    }


    @Override
    public Object getParent( Object element ) {
        return null;
    }


    @Override
    public boolean hasChildren( Object element ) {
        return getChildren( element ).length > 0;
    }


    @Override
    public void dispose() {
    }


    @Override
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
    }
}
