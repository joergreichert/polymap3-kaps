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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class NHK2000GebaeudeArtLabelProvider {

    private static Log                             log    = LogFactory.getLog( NHK2000GebaeudeArtLabelProvider.class );

    private static NHK2000GebaeudeArtLabelProvider instance;

    private Map<String, String>                    labels = new HashMap<String, String>();


    private NHK2000GebaeudeArtLabelProvider() {
        initializeData();
    }


    public final static NHK2000GebaeudeArtLabelProvider instance() {
        if (instance == null) {
            instance = new NHK2000GebaeudeArtLabelProvider();
        }
        return instance;
    }


    private void add( String name, String... label ) {
        labels.put( name, StringUtils.join( label, ", " ) );
    }


    private void initializeData() {
        add( "1.01", "Einfamilien - Wohnhäuser, freistehend", "Keller-, Erdgeschoss, voll ausgebautes Dachgeschoss" );
        add( "1.02", "Einfamilien - Wohnhäuser, freistehend", "Keller-, Erdgeschoss, nicht ausgebautes Dachgeschoss" );
        add( "1.03", "Einfamilien - Wohnhäuser, freistehend", "Keller-, Erdgeschoss, Flachdach" );
        add( "1.11", "Einfamilien - Wohnhäuser, freistehend",
                "Keller-, Erd-, Obergeschoss, voll ausgebautes Dachgeschoss" );
        add( "1.12", "Einfamilien - Wohnhäuser, freistehend",
                "Keller-, Erd-, Obergeschoss, nicht ausgebautes Dachgeschoss" );
        add( "1.13", "Einfamilien - Wohnhäuser, freistehend", "Keller-, Erd-, Obergeschoss, Flachdach" );
        add( "1.21", "Einfamilien - Wohnhäuser, freistehend",
                "Erdgeschoss, voll ausgebautes Dachgeschoss, nicht unterkellert" );
        add( "1.22", "Einfamilien - Wohnhäuser, freistehend",
                "Erdgeschoss, nicht ausgebautes Dachgeschoss, nicht unterkellert" );
        add( "1.23", "Einfamilien - Wohnhäuser, freistehend", "Erdgeschoss, Flachdach, nicht unterkellert" );
        add( "1.31", "Einfamilien - Wohnhäuser, freistehend",
                "Erd-, Obergeschoss, voll ausgebautes Dachgeschoss, nicht unterkellert" );
        add( "1.32", "Einfamilien - Wohnhäuser, freistehend",
                "Erd-, Obergeschoss, nicht ausgebautes Dachgeschoss, nicht unterkellert" );
        add( "1.33", "Einfamilien - Wohnhäuser, freistehend", "Erd-, Obergeschoss, Flachdach, nicht unterkellert" );
        add( "10", "Gemeinde- und Veranstaltungszentren", "Vereins- und Jugendheime, Tagesstätten",
                "1- bis 2-geschossig, unterkellert bzw. teilunterkellert, Dach geneigt oder Flachdach" );
        add( "11", "Kindergärten, Kindertagesstätten",
                "eingeschossig, nicht- bzw. teilunterkellert, Dach geneigt (nicht ausgebaut) oder Flachdach" );
        add( "12", "Schulen", "2- bis 3-geschossig, unterkellert, Dach geneigt (nicht ausgebaut) oder Flachdach" );
        add( "13", "Berufsschulen",
                "1- bis 3-geschossig, unterkellert bzw. teilunterkellert, Dach geneigt (nicht ausgebaut),",
                "oder Flachdach" );
        add( "14", "Hochschulen, Universitäten",
                "2- bis 4-geschossig, unterkellert, Dach geneigt (nicht ausgebaut) oder Flachdach" );
        add( "15", "Personal- und Schwesternwohnheime",
                "2- bis 6-geschossig, unterkellert, Dach geneigt (nicht ausgebaut) oder Flachdach" );
        add( "16", "Altenwohnheime", "2- bis 4-geschossig, unterkellert, Dach geneigt (nicht ausgebaut) oder Flachdach" );
        add( "17", "Allgemeine Krankenhäuser",
                "2- bis 6-geschossig, unterkellert, Dach geneigt (nicht ausgebaut) oder Flachdach" );
        add( "18", "Hotels", "2- bis 6-geschossig, unterkellert, Dach geneigt (nicht ausgebaut) oder Flachdach" );
        add( "19", "Tennishallen", "eingeschossig, nicht unterkellert, Dach geneigt oder Flachdach" );
        add( "2.01k", "Einfamilien - Reihenhäuser", "Keller-, Erdgeschoss, voll ausgebautes Dachgeschoss", "Kopfhaus" );
        add( "2.01m", "Einfamilien - Reihenhäuser", "Keller-, Erdgeschoss, voll ausgebautes Dachgeschoss", "Mittelhaus" );
        add( "2.02k", "Einfamilien - Reihenhäuser", "Keller-, Erdgeschoss, nicht ausgebautes Dachgeschoss", "Kopfhaus" );
        add( "2.02m", "Einfamilien - Reihenhäuser", "Keller-, Erdgeschoss, nicht ausgebautes Dachgeschoss",
                "Mittelhaus" );
        add( "2.03k", "Einfamilien - Reihenhäuser", "Keller-, Erdgeschoss, Flachdach", "Kopfhaus" );
        add( "2.03m", "Einfamilien - Reihenhäuser", "Keller-, Erdgeschoss, Flachdach", "Mittelhaus" );
        add( "2.11k", "Einfamilien - Reihenhäuser", "Keller-, Erd-, Obergeschoss, voll ausgebautes Dachgeschoss",
                "Kopfhaus" );
        add( "2.11m", "Einfamilien - Reihenhäuser", "Keller-, Erd-, Obergeschoss, voll ausgebautes Dachgeschoss",
                "Mittelhaus" );
        add( "2.12k", "Einfamilien - Reihenhäuser", "Keller-, Erd-, Obergeschoss, nicht ausgebautes Dachgeschoss",
                "Kopfhaus" );
        add( "2.12m", "Einfamilien - Reihenhäuser", "Keller-, Erd-, Obergeschoss, nicht ausgebautes Dachgeschoss",
                "Mittelhaus" );
        add( "2.13k", "Einfamilien - Reihenhäuser", "Keller-, Erd-, Obergeschoss, Flachdach", "Kopfhaus" );
        add( "2.13m", "Einfamilien - Reihenhäuser", "Keller-, Erd-, Obergeschoss, Flachdach", "Mittelhaus" );
        add( "2.21k", "Einfamilien - Reihenhäuser", "Erdgeschoss, voll ausgebautes Dachgeschoss, nicht unterkellert",
                "Kopfhaus" );
        add( "2.21m", "Einfamilien - Reihenhäuser", "Erdgeschoss, voll ausgebautes Dachgeschoss, nicht unterkellert",
                "Mittelhaus" );
        add( "2.22k", "Einfamilien - Reihenhäuser", "Erdgeschoss, nicht ausgebautes Dachgeschoss, nicht unterkellert",
                "Kopfhaus" );
        add( "2.22m", "Einfamilien - Reihenhäuser", "Erdgeschoss, nicht ausgebautes Dachgeschoss, nicht unterkellert",
                "Mittelhaus" );
        add( "2.23k", "Einfamilien - Reihenhäuser", "Erdgeschoss, Flachdach, nicht unterkellert", "Kopfhaus" );
        add( "2.23m", "Einfamilien - Reihenhäuser", "Erdgeschoss, Flachdach, nicht unterkellert", "Mittelhaus" );
        add( "2.31k", "Einfamilien - Reihenhäuser",
                "Erd-, Obergeschoss, voll ausgebautes Dachgeschoss, nicht unterkellert", "Kopfhaus" );
        add( "2.31m", "Einfamilien - Reihenhäuser",
                "Erd-, Obergeschoss, voll ausgebautes Dachgeschoss, nicht unterkellert", "Mittelhaus" );
        add( "2.32k", "Einfamilien - Reihenhäuser",
                "Erd-, Obergeschoss, nicht ausgebautes Dachgeschoss, nicht unterkellert", "Kopfhaus" );
        add( "2.32m", "Einfamilien - Reihenhäuser",
                "Erd-, Obergeschoss, nicht ausgebautes Dachgeschoss, nicht unterkellert", "Mittelhaus" );
        add( "2.33k", "Einfamilien - Reihenhäuser", "Erd-, Obergeschoss, Flachdach, nicht unterkellert", "Kopfhaus" );
        add( "2.33m", "Einfamilien - Reihenhäuser", "Erd-, Obergeschoss, Flachdach, nicht unterkellert", "Mittelhaus" );
        add( "20", "Turn- und Sporthallen", "eingeschossig, unterkellert, Dach flach geneigt oder Flachdach" );
        add( "21", "Funktionsgebäude für Sportanlagen",
                "1- bis 2-geschossig, nicht unterkellert, Dach geneigt (nicht ausgebaut) oder Flachdach" );
        add( "22", "Hallenbäder", "eingeschossig, teilunterkellert, Dach flach geneigt oder Flachdach" );
        add( "23", "Kur- und Heilbäder", "eingeschossig, teilunterkellert, Dach flach geneigt oder Flachdach" );
        add( "24", "Kirchen, Stadt-/ Dorfkirche, Kapelle",
                "eingeschossig, nicht unterkellert bzw. teilunterkellert, Dach geneigt oder Flachdach" );
        add( "25", "Einkaufsmärkte", "eingeschossig, nicht unterkellert, Dach geneigt oder Flachdach" );
        add( "26", "Kauf- und Warenhäuser", "3- bis 6-geschossig, unterkellert, Dach geneigt oder Flachdach" );
        add( "27", "Ausstellungsgebäude",
                "2- bis 4-geschossig, unterkellert bzw. teilunterkellert, Dach geneigt oder Flachdach" );
        add( "28.1", "Parkhäuser", "mehrgeschossig, offene Ausführung ohne Lüftungsanlage" );
        add( "28.2", "Parkhäuser", "mehrgeschossig, geschlossene Ausführung mit Lüftungsanlage" );
        add( "29", "Tiefgarage" );
        add( "29.1a", "KFZ-Stellplätze", "Kleingaragen freistehend" );
        add( "29.1b", "KFZ-Stellplätze", "Kellergaragen" );
        add( "29.1c", "KFZ-Stellplätze", "Carports" );
        add( "3.11f", "Mehrfamilien - Wohnhäuser", "Keller-, Erd, Obergeschoss, voll ausgebautes Dachgeschoss",
                "freistehend" );
        add( "3.11k", "Mehrfamilien - Wohnhäuser", "Keller-, Erd, Obergeschoss, voll ausgebautes Dachgeschoss",
                "Kopfhaus" );
        add( "3.11m", "Mehrfamilien - Wohnhäuser", "Keller-, Erd, Obergeschoss, voll ausgebautes Dachgeschoss",
                "Mittelhaus" );
        add( "3.12f", "Mehrfamilien - Wohnhäuser", "Keller-, Erd, Obergeschoss, nicht ausgebautes Dachgeschoss",
                "freistehend" );
        add( "3.12k", "Mehrfamilien - Wohnhäuser", "Keller-, Erd, Obergeschoss, nicht ausgebautes Dachgeschoss",
                "Kopfhaus" );
        add( "3.12m", "Mehrfamilien - Wohnhäuser", "Keller-, Erd, Obergeschoss, nicht ausgebautes Dachgeschoss",
                "Mittelhaus" );
        add( "3.13f", "Mehrfamilien - Wohnhäuser", "Keller-, Erd, Obergeschoss, Flachdach", "freistehend" );
        add( "3.13k", "Mehrfamilien - Wohnhäuser", "Keller-, Erd, Obergeschoss, Flachdach", "Kopfhaus" );
        add( "3.13m", "Mehrfamilien - Wohnhäuser", "Keller-, Erd, Obergeschoss, Flachdach", "Mittelhaus" );
        add( "3.21f", "Mehrfamilien - Wohnhäuser",
                "Keller-, Erdgeschoss, 2 Obergeschosse, voll ausgebautes Dachgeschoss", "freistehend" );
        add( "3.21k", "Mehrfamilien - Wohnhäuser",
                "Keller-, Erdgeschoss, 2 Obergeschosse, voll ausgebautes Dachgeschoss", "Kopfhaus" );
        add( "3.21m", "Mehrfamilien - Wohnhäuser",
                "Keller-, Erdgeschoss, 2 Obergeschosse, voll ausgebautes Dachgeschoss", "Mittelhaus" );
        add( "3.22f", "Mehrfamilien - Wohnhäuser",
                "Keller-, Erdgeschoss, 2 Obergeschosse, nicht ausgebautes Dachgeschoss", "freistehend" );
        add( "3.22k", "Mehrfamilien - Wohnhäuser",
                "Keller-, Erdgeschoss, 2 Obergeschosse, nicht ausgebautes Dachgeschoss", "Kopfhaus" );
        add( "3.22m", "Mehrfamilien - Wohnhäuser",
                "Keller-, Erdgeschoss, 2 Obergeschosse, nicht ausgebautes Dachgeschoss", "Mittelhaus" );
        add( "3.23f", "Mehrfamilien - Wohnhäuser", "Keller-, Erdgeschoss, 2 Obergeschosse, Flachdach", "freistehend" );
        add( "3.23k", "Mehrfamilien - Wohnhäuser", "Keller-, Erdgeschoss, 2 Obergeschosse, Flachdach", "Kopfhaus" );
        add( "3.23m", "Mehrfamilien - Wohnhäuser", "Keller-, Erdgeschoss, 2 Obergeschosse, Flachdach", "Mittelhaus" );
        add( "3.32f", "Mehrfamilien - Wohnhäuser",
                "Keller-, Erdgeschoss, 3 Obergeschosse, nicht ausgebautes Dachgeschoss", "freistehend" );
        add( "3.32k", "Mehrfamilien - Wohnhäuser",
                "Keller-, Erdgeschoss, 3 Obergeschosse, nicht ausgebautes Dachgeschoss", "Kopfhaus" );
        add( "3.32kp", "Mehrfamilien - Wohnhäuser",
                "Keller-, Erdgeschoss, 3 Obergeschosse, nicht ausgebautes Dachgeschoss",
                "Kopfhaus, Großblock- und Plattenbauweise, Typ IW 62" );
        add( "3.32m", "Mehrfamilien - Wohnhäuser",
                "Keller-, Erdgeschoss, 3 Obergeschosse, nicht ausgebautes Dachgeschoss", "Mittelhaus" );
        add( "3.32mp", "Mehrfamilien - Wohnhäuser",
                "Keller-, Erdgeschoss, 3 Obergeschosse, nicht ausgebautes Dachgeschoss",
                "Mittelhaus, Großblock- und Plattenbauweise, Typ IW 62" );
        add( "3.33f", "Mehrfamilien - Wohnhäuser", "Keller-, Erdgeschoss, 3 Obergeschosse, Flachdach", "freistehend" );
        add( "3.33k", "Mehrfamilien - Wohnhäuser", "Keller-, Erdgeschoss, 3 Obergeschosse, Flachdach", "Kopfhaus" );
        add( "3.33m", "Mehrfamilien - Wohnhäuser", "Keller-, Erdgeschoss, 3 Obergeschosse, Flachdach", "Mittelhaus" );
        add( "3.42f", "Mehrfamilien - Wohnhäuser",
                "Keller-, Erdgeschoss, 4-5 Obergeschosse, nicht ausgebautes Dachgeschoss", "freistehend" );
        add( "3.42k", "Mehrfamilien - Wohnhäuser",
                "Keller-, Erdgeschoss, 4-5 Obergeschosse, nicht ausgebautes Dachgeschoss", "Kopfhaus" );
        add( "3.42kp1", "Mehrfamilien - Wohnhäuser",
                "Keller-, Erdgeschoss, 4-5 Obergeschosse, nicht ausgebautes Dachgeschoss",
                "Kopfhaus, Großblock- und Plattenbauweise, Typ QD 58/60 P 2-11" );
        add( "3.42kp2", "Mehrfamilien - Wohnhäuser",
                "Keller-, Erdgeschoss, 4-5 Obergeschosse, nicht ausgebautes Dachgeschoss",
                "Kopfhaus, Großblock- und Plattenbauweise, Typ IW 62 P 2-11" );
        add( "3.42m", "Mehrfamilien - Wohnhäuser",
                "Keller-, Erdgeschoss, 4-5 Obergeschosse, nicht ausgebautes Dachgeschoss", "Mittelhaus" );
        add( "3.42mp1", "Mehrfamilien - Wohnhäuser",
                "Keller-, Erdgeschoss, 4-5 Obergeschosse, nicht ausgebautes Dachgeschoss",
                "Mittelhaus, Großblock- und Plattenbauweise Typ QD 58/60 P 2-11" );
        add( "3.42mp2", "Mehrfamilien - Wohnhäuser",
                "Keller-, Erdgeschoss, 4-5 Obergeschosse, nicht ausgebautes Dachgeschoss",
                "Mittelhaus, Großblock- und Plattenbauweise Typ IW 62 P 2-11" );
        add( "3.53f", "Mehrfamilien - Wohnhäuser", "Keller-, Erdgeschoss, 5 Obergeschosse, Flachdach", "freistehend" );
        add( "3.53k", "Mehrfamilien - Wohnhäuser", "Keller-, Erdgeschoss, 5 Obergeschosse, Flachdach", "Kopfhaus" );
        add( "3.53kp1", "Mehrfamilien - Wohnhäuser", "Keller-, Erdgeschoss, 5 Obergeschosse, Flachdach",
                "Kopfhaus, Großblock- und Plattenbauweise Typ IW 65" );
        add( "3.53kp2", "Mehrfamilien - Wohnhäuser", "Keller-, Erdgeschoss, 5 Obergeschosse, Flachdach",
                "Kopfhaus, Großblock- und Plattenbauweise Typ IW 66 P 2-6" );
        add( "3.53m", "Mehrfamilien - Wohnhäuser", "Keller-, Erdgeschoss, 5 Obergeschosse, Flachdach", "Mittelhaus" );
        add( "3.53mp1", "Mehrfamilien - Wohnhäuser", "Keller-, Erdgeschoss, 5 Obergeschosse, Flachdach",
                "Mittelhaus, Großblock- und Plattenbauweise Typ IW 65" );
        add( "3.53mp2", "Mehrfamilien - Wohnhäuser", "Keller-, Erdgeschoss, 5 Obergeschosse, Flachdach",
                "Mittelhaus, Großblock- und Plattenbauweise Typ IW 66 P 2-6" );
        add( "3.73f", "Mehrfamilien - Wohnhäuser", "Keller-, Erdgeschoss, 7-10 Obergeschosse, Flachdach", "freistehend" );
        add( "3.73k", "Mehrfamilien - Wohnhäuser", "Keller-, Erdgeschoss, 7-10 Obergeschosse, Flachdach", "Kopfhaus" );
        add( "3.73kp", "Mehrfamilien - Wohnhäuser", "Keller-, Erdgeschoss, 7-10 Obergeschosse, Flachdach",
                "Kopfhaus, Großblock- und Plattenbauweise Typ IW 66 P 2-11" );
        add( "3.73m", "Mehrfamilien - Wohnhäuser", "Keller-, Erdgeschoss, 7-10 Obergeschosse, Flachdach", "Mittelhaus" );
        add( "3.73mp", "Mehrfamilien - Wohnhäuser", "Keller-, Erdgeschoss, 7-10 Obergeschosse, Flachdach",
                "Mittelhaus, Großblock- und Plattenbauweise Typ IW 66 P 2-11" );
        add( "30.1", "Industriegebäude, Werkstätten", "ohne Büro- und Sozialtrakt" );
        add( "30.2", "Industriegebäude, Werkstätten", "mit Büro- und Sozialtrakt" );
        add( "31.1", "Lagergebäude", "Kaltlager" );
        add( "31.2", "Lagergebäude", "Warmlager" );
        add( "31.3", "Lagergebäude", "Warmlager mit Büro- und Sozialtrakt" );
        add( "32.1", "Reithallen, eingeschossig, Dach geneigt" );
        add( "32.2", "Pferdeställe, eingeschossig, Dach geneigt" );
        add( "33.1.1", "Kälberställe, eingeschossig, ohne Güllekanäle, Dach geneigt" );
        add( "33.1.2", "Rinderställe, eingeschossig, ohne Güllekanäle, Dach geneigt",
                "(Jungvieh-, Mastbullen- und Milchviehställe ohne Melkstand)" );
        add( "33.1.3", "Milchviehställe, eingeschossig, ohne Güllekanäle, Dach geneigt", "mit Melkstand und Milchlager" );
        add( "33.1.4", "Rinderställe", "Melkstand, eingeschossig, ohne Güllekanäle, Dach geneigt",
                "mit Milchlager und Nebenräumen als Einzelgebäude" );
        add( "33.2.1", "Schweineställe", "Ferkelaufzuchtställe, eingeschossig, ohne Güllekanäle, Dach geneigt" );
        add( "33.2.2", "Schweineställe", "Mastschweineställe, eingeschossig, ohne Güllekanäle, Dach geneigt" );
        add( "33.2.3", "Schweineställe", "Zuchtschweineställe, eingeschossig, ohne Güllekanäle, Dach geneigt",
                "(Deck-, Warte- und Abferkelbereiche)" );
        add( "33.2.4", "Schweineställe",
                "Abferkelställe als Einzelgebäude, eingeschossig, ohne Güllekanäle, Dach geneigt" );
        add( "33.3.1", "Geflügelställe",
                "Mastgeflügel, Bodenhaltung, eingeschossig, Dach geneigt (Hähnchen, Puten, Gänse)" );
        add( "33.3.2", "Geflügelställe", "Legehennen, Bodenhaltung, eingeschossig, Dach geneigt" );
        add( "33.3.3", "Geflügelställe", "Legehennen, Volierenhaltung, eingeschossig, Dach geneigt" );
        add( "33.3.4", "Geflügelställe", "Legehennen, Käfighaltung, eingeschossig, Dach geneigt" );
        add( "33.4.1", "Landwirtschaftliche Mehrzweckhallen", "eingeschossig, Dach geneigt" );
        add( "33.4.2", "Scheunen ohne Stallanteil", "eingeschossig, Dach geneigt" );
        add( "4", "Gemischt genutzte Wohn- und Geschäftshäuser",
                "3- bis 4-geschossig, unterkellert, Dach geneigt oder Flachdach" );
        add( "5.1", "Verwaltungsgebäude", "1- bis 2-geschossig, nicht unterkellert, Dach geneigt oder Flachdach" );
        add( "5.2", "Verwaltungsgebäude", "2- bis 5-geschossig, unterkellert, Dach geneigt oder Flachdach" );
        add( "5.3", "Verwaltungsgebäude", "6- und mehrgeschossig, Flachdach" );
        add( "6", "Bankgebäude", "2- bis 6-geschossig, unterkellert, Dach geneigt oder Flachdach" );
        add( "7", "Gerichtsgebäude", "2- bis 6-geschossig, unterkellert, Dach geneigt oder Flachdach" );
        add( "8", "Gemeinde- und Veranstaltungszentren, Vereins- und Jugendheime", "Gemeindezentren, Bürgerhäuser",
                "1- bis 3-geschossig, unterkellert bzw. teilunterkellert, Dach geneigt oder Flachdach" );
        add( "9", "Gemeinde- und Veranstaltungszentren, Vereins- und Jugendheime", "Saalbauten, Veranstaltungszentren",
                "1- bis 3-geschossig, unterkellert bzw. teilunterkellert, Dach geneigt oder Flachdach" );
        add( "99", "Sonstige Gebäude" );
    }


    /**
     *
     * @param string
     * @return
     */
    public String labelFor( String key ) {
        return labels.get( key );
    }

}
