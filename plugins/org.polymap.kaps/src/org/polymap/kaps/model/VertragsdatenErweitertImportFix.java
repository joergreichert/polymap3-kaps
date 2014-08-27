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

import java.util.Locale;

import java.text.NumberFormat;
import java.text.ParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.unitofwork.UnitOfWork;

import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.model.data.VertragsdatenErweitertComposite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class VertragsdatenErweitertImportFix {

    private static Log log = LogFactory.getLog( VertragsdatenErweitertImportFix.class );


    public static void add( UnitOfWork uow, QueryBuilder<VertragComposite> builder, VertragComposite template,
            String eingangsNr, String zuschlag, String abschlag, String zubem, String abbem )
            throws ParseException {

        BooleanExpression expr = QueryExpressions.eq( template.eingangsNr(),
                ((Long)NumberFormat.getNumberInstance( Locale.ENGLISH ).parse( eingangsNr )).intValue() );
        VertragComposite latest = builder.where( expr ).newQuery( uow ).maxResults( 1 ).find();
        if (latest != null) {
            log.info( "Changing Vertrag " + latest.eingangsNr().get() );
            VertragsdatenErweitertComposite vertragsdatenErweitertComposite = latest.erweiterteVertragsdaten().get();
            if (zuschlag != null) {
                vertragsdatenErweitertComposite.zuschlag().set( Double.valueOf( zuschlag ) );
            }
            if (zubem != null) {
                vertragsdatenErweitertComposite.zuschlagBemerkung().set( zubem );
            }
            if (abschlag != null) {
                vertragsdatenErweitertComposite.abschlag().set( Double.valueOf( abschlag ) );
            }
            if (abbem != null) {
                vertragsdatenErweitertComposite.abschlagBemerkung().set( abbem );
            }
            recalculate( vertragsdatenErweitertComposite );
        }
    }


    private static void recalculate( VertragsdatenErweitertComposite erweiterteVertragsdaten ) {
        Double result = erweiterteVertragsdaten.basispreis().get();
        Double n = erweiterteVertragsdaten.zuschlag().get();
        Double z = erweiterteVertragsdaten.abschlag().get();
        Double w = erweiterteVertragsdaten.wertbeeinflussendeUmstaende().get();

        if (result != null && n != null) {
            result += n;
        }
        if (result != null && z != null) {
            result -= z;
        }
        if (result != null && w != null) {
            result += w;
        }
        erweiterteVertragsdaten.bereinigterVollpreis().set( result );
    }


    public static void fix( UnitOfWork uow, QueryBuilder<VertragComposite> builder )
            throws ParseException {
        VertragComposite template = QueryExpressions.templateFor( VertragComposite.class );
        add( uow, builder, template, "2.00110231E8", null, "447.0", null, "Preis für Eingangstor" );
        add( uow, builder, template, "2.00110674E8", null, "5113.0", null, "Gebäudewert" );
        add( uow, builder, template, "2.00111236E8", null, "18768.0", null, "restlicher 2/3tel Anteil" );
        add( uow, builder, template, "2.00120567E8", null, "256.0", null, "transpotabler Wagen" );
        add( uow, builder, template, "2.0012073E8", null, "854.0", null, "f. Vermessung" );
        add( uow, builder, template, "2.00121148E8", null, "1276.0", null, "Vermessungskosten" );
        add( uow, builder, template, "2.00210416E8", null, "22380.0", null, "Wert des Bungslows" );
        add( uow, builder, template, "2.00210725E8", null, "3000.0", null, "für vorhandene Anpflanzung" );
        add( uow, builder, template, "2.00210948E8", null, "4588.0", null, "für Inventar und Gebäude" );
        add( uow, builder, template, "2.00210949E8", null, "4060.0", null, "Wert des Bungalows" );
        add( uow, builder, template, "2.0021095E8", null, "21020.0", null, "Wert des Bungalows" );
        add( uow, builder, template, "2.0021107E8", null, "883.0", null, "Wert Gartenlaube" );
        add( uow, builder, template, "2.00211297E8", null, "9027.0", null, null );
        add( uow, builder, template, "2.00211391E8", null, "32198.0", null, "Preis für evtl. Bau Windkraftanlage+" );
        add( uow, builder, template, "2.00211396E8", null, "34099.0", null, "= Zuschlag für evtl.Bau Windkraftanlage" );
        add( uow, builder, template, "2.00220085E8", null, "4108.0", null, null );
        add( uow, builder, template, "2.00220249E8", null, "146.0", null, "Kosten f. Gutachten" );
        add( uow, builder, template, "2.00220252E8", null, "146.0", null, null );
        add( uow, builder, template, "2.00220254E8", null, "145.0", null, null );
        add( uow, builder, template, "2.00220551E8", "2277.0", null, null, null );
        add( uow, builder, template, "2.00220786E8", null, "2335.0", "Erstverkauf 200200776", "f Pachtzinsauskehr" );
        add( uow, builder, template, "2.00220851E8", "4156.0", null, "30 DM/m³ Abriss der Beb. wurde dem Boden",
                "wert gegengerechnet->Ausw.Bodenw. lt. GA" );
        add( uow, builder, template, "2.00220853E8", "5114.0", null, "Abbruch der Bebauung dem Boden gesamt",
                "gegengerechnet" );
        add( uow, builder, template, "2.00220946E8", null, "219.0", null, "d.Rundung lt. Gutachten" );
        add( uow, builder, template, "2.00221386E8", null, "1473000.0", "1473000 € Abschlag für",
                "f. Betriebseinrichtg.+f. Maschinen" );
        add( uow, builder, template, "2.00221388E8", null, "655000.0", null, "Betriebsvorricht.+Maschinen" );
        add( uow, builder, template, "2.00310021E8", null, "5500.0", null, "Preis für Inventar" );
        add( uow, builder, template, "2.0031003E8", null, "500.0", null, "für Garage" );
        add( uow, builder, template, "2.00310234E8", null, "3008.0", null, "Nebenentschädigung" );
        add( uow, builder, template, "2.00310241E8", null, "4015.0", null, "Entschädigung f. Gebäude u. Außenanlagen" );
        add( uow, builder, template, "2.00310316E8", "174.0", null, "ist Verkehrswert aller Flurstücke", null );
        add( uow, builder, template, "2.00310321E8", "461.0", "336.0", null, "anteilige Vermessungskosten" );
        add( uow, builder, template, "2.00310323E8", null, "47.0", null, "anteilige Vermessungskoten" );
        add( uow, builder, template, "2.00310379E8", null, "1469.0", null, "Nebenentschädigung" );
        add( uow, builder, template, "2.00310446E8", null, "10000.0", null, "Zeitwert für Stützmauer" );
        add( uow, builder, template, "2.0031045E8", "7032.0", null, "Wert aller Flurstücke", null );
        add( uow, builder, template, "2.00310721E8", "8294.0", null, "sind abgezogene Prozente vom Vw", null );
        add( uow, builder, template, "2.00310853E8", null, "620.0", null, "Anteil gehört Erwerber bereits" );
        add( uow, builder, template, "2.00310882E8", "53162.0", null, "voller Verkehrswert", null );
        add( uow, builder, template, "2.00310931E8", "13943.0", null, "voller Verkehrswert", null );
        add( uow, builder, template, "2.00310936E8", "19290.0", null, "voller Verkehrswert", null );
        add( uow, builder, template, "2.00310966E8", "105428.0", null, "Differenz voller Verkehrswert", null );
        add( uow, builder, template, "2.00311001E8", "3630.0", null, "= voller Verkehrswert", null );
        add( uow, builder, template, "2.00311173E8", "50528.0", null, "voller Verkehrswert", null );
        add( uow, builder, template, "2.00311265E8", null, "10000.0", null, "für Inventar - Ruderboot, Anlegesteg" );
        add( uow, builder, template, "2.00320084E8", "27530.0", null, "Aufschl.Nu Arten entspr.tel.Rückspr.mit Nutzer",
                "der Flächen Agarargen.über Pachtvertrag" );
        add( uow, builder, template, "2.00320085E8", "730.0", null, "auf Grund Hochrechnung auf 40-50 Pfg/m²",
                "-> 0,23 €/m²" );
        add( uow, builder, template, "2.00320164E8", null, "50.0", null, "für Bestockung" );
        add( uow, builder, template, "2.00320864E8", null, "1323.0", null, "minimiert aufgrund v.Belastungen" );
        add( uow, builder, template, "2.00321017E8", null, "1704.0", null, "für Bewuchs" );
        add( uow, builder, template, "2.00321019E8", null, "3561.0", null, "für Bewuchs" );
        add( uow, builder, template, "2.00321284E8", null, "1829.0", null, "Zinsen" );
        add( uow, builder, template, "2.00321292E8", null, "1989.0", null, null );
        add( uow, builder, template, "2.00321328E8", "1375.0", null, "Hochrechnung auf 100 % lt. GA", null );
        add( uow, builder, template, "2.0041011E8", null, "150.0", null, "Bearbeitungsgebühr der Stadt" );
        add( uow, builder, template, "2.00410124E8", null, "150.0", null, "Bearbeitungsgebühr Stadt Penig" );
        add( uow, builder, template, "2.00410125E8", "247.0", null, "= Verkehrswert gesamt", null );
        add( uow, builder, template, "2.00410308E8", "9005.0", null, "Differenz zum Verkehrswert", null );
        add( uow, builder, template, "2.00410347E8", "11048.0", null, "Abrisskosten vom Flst. 42 abgezogen", null );
        add( uow, builder, template, "2.00410376E8", "4263.0", null, "Differenz zum Verkehrswert", null );
        add( uow, builder, template, "2.00410573E8", null, "169.0", null, "Nebenentschädigung" );
        add( uow, builder, template, "2.00410599E8", "10220.0", null, "Differenz zum ausgew. Verkehrswert", null );
        add( uow, builder, template, "2.00410756E8", null, "4471.0", null, "= Wert der Nebenentschädigung" );
        add( uow, builder, template, "2.00410814E8", null, "2760.0", null, "für Nebenentschädigung" );
        add( uow, builder, template, "2.00420377E8", null, "1711.0", null, "aufstockender Bestand" );
        add( uow, builder, template, "2.00420508E8", null, "5177.0", null, "Durchschneideentschädigung" );
        add( uow, builder, template, "2.0042129E8", null, "32.0", null, "für Nebenkosten" );
        add( uow, builder, template, "2.00510101E8", "8865.0", null, "Differenz zum Verkehrswert", null );
        add( uow, builder, template, "2.00510114E8", "12161.0", null, "= Differenz zum Verkehrswert", null );
        add( uow, builder, template, "2.00510203E8", "16409.0", null, "Differenz zum Verkehrswert", null );
        add( uow, builder, template, "2.00510375E8", "21609.0", null, "Differenz zum Verkehrswert", null );
        add( uow, builder, template, "2.00510376E8", null, "11000.0", null, "für Bungalow" );
        add( uow, builder, template, "2.00510478E8", null, "1500.0", null, "für Inventar und Dienstbarkeit" );
        add( uow, builder, template, "2.00510535E8", "5947.0", null, "= Differenzbetrag FlErwV", null );
        add( uow, builder, template, "2.00510539E8", null, "538.0", null, "verbleibende Schäden am Restgrundstück" );
        add( uow, builder, template, "2.0051054E8", null, "4475.0", null, "verbleibende Schäden am Restgrundstück" );
        add( uow, builder, template, "2.00510541E8", null, "4118.0", null, "verbleibende Schäden am Restgrundstück" );
        add( uow, builder, template, "2.00510565E8", null, "1500.0", null, "für 1/2 MEA" );
        add( uow, builder, template, "2.00510739E8", null, "1000.0", "Baujahr ist geschätzt", "für Inventar" );
        add( uow, builder, template, "2.0051078E8", null, "30000.0", null, "für 1/2 MEA" );
        add( uow, builder, template, "2.00510799E8", null, "5103.0", null, "Entschädigung für Aufwuchs und Restschäd" );
        add( uow, builder, template, "2.00510802E8", null, "1409.0", null, "für verbleibene Schäden am Restgrdst." );
        add( uow, builder, template, "2.0051084E8", null, "24727.0", null, "Neben-Entschädigungssumme" );
        add( uow, builder, template, "2.00510846E8", "13420.0", null, "Kauf eines 1/2 MEA - hier: voller Vw", null );
        add( uow, builder, template, "2.00510903E8", null, "2014.0", null, "Entschädigung für Wertminderung am Grdst" );
        add( uow, builder, template, "2.00510905E8", null, "13108.0", null, "Entschädigungf für Wertminderung Restgrd" );
        add( uow, builder, template, "2.00510942E8", null, "3000.0", null, "für Boot und Einrichtung" );
        add( uow, builder, template, "2.00511164E8", null, "4037.0", null, "für verbleibenden Schaden am Restgrundst" );
        add( uow, builder, template, "2.00511167E8", null, "1987.0", null, "für verbleib.Schaden am Restgrundstück" );
        add( uow, builder, template, "2.00511199E8", null, "1010.0", null, "für verbleibenden Schaden am Restgrdst." );
        add( uow, builder, template, "2.005112E8", null, "74.0", null, "für verbl.Schaden am Restgrdst." );
        add( uow, builder, template, "2.00511201E8", null, "52583.0", null, "Entschädig.für Aufwuchs+Schäden am Grdst" );
        add( uow, builder, template, "2.0052014E8", null, "1655.0", null, "f. Zinsen lt. KV" );
        add( uow, builder, template, "2.00520144E8", null, "67.0", null, null );
        add( uow, builder, template, "2.00520228E8", null, "46.0", null, "aufgrund Rundung des Kaufpreises" );
        add( uow, builder, template, "2.00520608E8", null, "250.0", null, "für die Erstattung des Gutachtens" );
        add( uow, builder, template, "2.00520708E8", null, "22052.0",
                "./. Bewuchs: Fl-Nr. 447= 11981 €+ ~448= 5847 €+", "~449= 4224 €->ges.22052 €" );
        add( uow, builder, template, "2.00520839E8", "1905.0", null, "Fl-Nr.3490 a = GR à 0,20 €/m²",
                "Fl.-Nr. 3421/64 = Acker 0.30 €/m²" );
        add( uow, builder, template, "2.0052084E8", null, "1906.0", null, "sind 50 % der Differenz zwischen den" );
        add( uow, builder, template, "2.00520892E8", null, "24.0", null, "f. Rechtsgeschäft" );
        add( uow, builder, template, "2.00521282E8", null, "1000.0", null, "für Aufwuchs (Verkehrswert)" );
        add( uow, builder, template, "2.0061001E8", null, "20500.0", null, "Entschädigung für Umweg+Erwerbsverlust" );
        add( uow, builder, template, "2.00610091E8", null, "805.0", null, "Nebenentschädigung" );
        add( uow, builder, template, "2.00610167E8", null, "816.0", null, "Entschädigung f. Schaden am Restgrdst." );
        add( uow, builder, template, "2.00610168E8", null, "3706.0", null, "Entschädig.f. Schäden am Restgrundstück" );
        add( uow, builder, template, "2.00610169E8", null, "2942.0", null, "Entschädig.f. Schäden am Restgrundstück" );
        add( uow, builder, template, "2.00610171E8", null, "1114.0", null, "Entschädig.f.Schäden am Restgrundstück" );
        add( uow, builder, template, "2.00610196E8", null, "332.0", null, "für Feldinventar" );
        add( uow, builder, template, "2.00610285E8", null, "3914.0", null, "für verbleib.Schäden am Restgrundstück" );
        add( uow, builder, template, "2.00610509E8", null, "1100.0", null, "Wert für Bungalow" );
        add( uow, builder, template, "2.00610541E8", null, "180.0", null, "Rohbau/Baumaterial" );
        add( uow, builder, template, "2.00610622E8", null, "106.0", null, "Entschädigung für Erwerbsverlust" );
        add( uow, builder, template, "2.00610743E8", null, "1482.0", null, "Entschädigung für bleibende Schäden" );
        add( uow, builder, template, "2.00610744E8", null, "2820.0", null, "Entschädig.f.bleibende Schäden am Grdst." );
        add( uow, builder, template, "2.00610745E8", null, "1761.0", null, "Entschädig.f.verbleibende Schäden am Grd" );
        add( uow, builder, template, "2.00610746E8", null, "140.0", null, "Wertminderung vom vom KP abgezogen" );
        add( uow, builder, template, "2.00610747E8", null, "1842.0", null, "Entschädig.f.Restschäden am Grdst." );
        add( uow, builder, template, "2.00610754E8", null, "27.0", null, "Wirtschaftsschäden" );
        add( uow, builder, template, "2.00610761E8", null, "1011.0", null, "Entschädigung Bestandswert+Randschaden" );
        add( uow, builder, template, "2.00610785E8", "27125.0", null, "= voller Verkehrswert", null );
        add( uow, builder, template, "2.0061079E8", "295.0", null, "= voller Verkehrswert", null );
        add( uow, builder, template, "2.00610794E8", null, "450.0", null, "Wert Gartenlaube" );
        add( uow, builder, template, "2.00610807E8", "665.0", null, "= voller Verkehrswert", null );
        add( uow, builder, template, "2.00610809E8", "86690.0", null, "= voller Verkehrswert", null );
        add( uow, builder, template, "2.00610833E8", "5219.0", null, "= voller Verkehrswert", null );
        add( uow, builder, template, "2.00610834E8", "13880.0", null, "= voller Verkehrswert", null );
        add( uow, builder, template, "2.00610835E8", "38219.0", null, "= Diff. zum vollen Verkehrswert", null );
        add( uow, builder, template, "2.00610928E8", null, "100.0", null, "Gartenhaus" );
        add( uow, builder, template, "2.00610942E8", null, "10000.0", null, "für verbleibendes Inventar" );
        add( uow, builder, template, "2.00611138E8", null, "102.0", null, "für verbleibende Schäden am Restgrdst." );
        add( uow, builder, template, "2.00611161E8", null, "1155.0", null, "Entschädigung für Baumbestand/Wald" );
        add( uow, builder, template, "2.00611177E8", null, "3975.0", null, "= Wert des Bungalow" );
        add( uow, builder, template, "2.00611185E8", null, "311.0", null, "Entschädig.für Aufwuchs und Schäden" );
        add( uow, builder, template, "2.00611196E8", null, "739.0", null, "für verbleibende Schäden am Restgrdst." );
        add( uow, builder, template, "2.00611252E8", null, "150.0", null, "Entschädigung für Aufwuchs" );
        add( uow, builder, template, "2.00611262E8", null, "900.0", null, "verbleibende Schäden am Restgrundstück" );
        add( uow, builder, template, "2.00620027E8", null, "2856.0", null, "Waldwert" );
        add( uow, builder, template, "2.00620051E8", null, "24.0", null, "einmalige Summe" );
        add( uow, builder, template, "2.00620133E8", null, "2568.0", null, "Aufgeld f. Zinsen" );
        add( uow, builder, template, "2.00620476E8", null, "269.0", null, "Sonstiges->Differenz" );
        add( uow, builder, template, "2.0071032E8", null, "3339.0", null, "= Entschädigung f. Schaden am Restgrdst." );
        add( uow, builder, template, "2.00710409E8", null, "1413.0", null, "für verbl. Inventar+Wegbau anteilig" );
        add( uow, builder, template, "2.00710445E8", null, "500.0", null, "für verbl.Inventar" );
        add( uow, builder, template, "2.00710556E8", null, "5000.0", null, "für verbleibendes Inventar" );
        add( uow, builder, template, "2.00710717E8", null, "2000.0", null, "für Unterkunftswagen+Inventar" );
        add( uow, builder, template, "2.00710769E8", null, "800.0", null, "Nebenentschädigung Flst. 333/2" );
        add( uow, builder, template, "2.00710968E8", null, "3600.0", null, "Inventar, elektr.Geräte+Grillkamin" );
        add( uow, builder, template, "2.00711066E8", null, "239300.0", null, "Entschädig.für Erwerbsverlust, Nachteile" );
        add( uow, builder, template, "2.00711071E8", null, "3068.0", null, "Nebenentschädigung" );
        add( uow, builder, template, "2.00711149E8", null, "2176.0", null, "Entschädigung Anschneideschaden" );
        add( uow, builder, template, "2.00711211E8", "39590.0", null, "=Differenz zum Verkehrswert", null );
        add( uow, builder, template, "2.00711212E8", "20489.0", null, "= Differenz zum Verkehrswert", null );
        add( uow, builder, template, "2.00711232E8", null, "732.0", null, "Entschädigung f. Aufwuchs" );
        add( uow, builder, template, "2.00711254E8", null, "5365.0", null, "für Wertminderung für Grundstück 89,40 €" );
        add( uow, builder, template, "2.00711256E8", null, "20168.0", null, "Entschädigungen" );
        add( uow, builder, template, "2.00711265E8", null, "2000.0", null, "für verbleibende Schäden am Restgrdst." );
        add( uow, builder, template, "2.00711269E8", "1283.0", null, "= Differenz zum Verkehrswert", null );
        add( uow, builder, template, "2.0071127E8", "631.0", null, "= Differenz zum Verkehrswert", null );
        add( uow, builder, template, "2.00711271E8", "537.0", null, "=Differenz zum Verkehrswert", null );
        add( uow, builder, template, "2.00711354E8", "20243.0", null, "=Differenz zum Verkehrswert", null );
        add( uow, builder, template, "2.00720209E8", null, "224.0", null, "Entschädigung" );
        add( uow, builder, template, "2.0072021E8", null, "210.0", null, "für Entschädigung" );
        add( uow, builder, template, "2.00720343E8", null, "637.0", null, "Differenz aufgr. Ausschreibung" );
        add( uow, builder, template, "2.00720919E8", null, "7711.0", null, null );
        add( uow, builder, template, "2.00721468E8", null, "16478.0", null, null );
        add( uow, builder, template, "2.00810077E8", null, "25.0", null, "Aufwandsentschädigung" );
        add( uow, builder, template, "2.00810189E8", null, "1355.0", null, "Entschädigung" );
        add( uow, builder, template, "2.00810225E8", null, "844.0", null, "Entsch.für verblei.Schäden am Restgrdst." );
        add( uow, builder, template, "2.00810319E8", null, "11.0", null, "Entschädigung Anschneideschaden" );
        add( uow, builder, template, "2.00810322E8", null, "22.0", null, "Entschädigung Anschneideschaden" );
        add( uow, builder, template, "2.00810399E8", null, "72.0", null, "Anschneideschaden" );
        add( uow, builder, template, "2.008104E8", null, "314.0", null, "Anschneideschaden" );
        add( uow, builder, template, "2.00810401E8", null, "71.0", null, "Anschneideschaden" );
        add( uow, builder, template, "2.00810413E8", null, "91.0", null, "Entschädigung für Schäden am Restgrdst." );
        add( uow, builder, template, "2.00810516E8", null, "148.0", null, "für Anschneideschaden" );
        add( uow, builder, template, "2.00810522E8", "7261.0", null, "Differenz zum Verkehrswert", null );
        add( uow, builder, template, "2.00810523E8", "160163.0", null, "= Differenz zum Verkehrswert", null );
        add( uow, builder, template, "2.00810524E8", "6817.0", null, "= Differenz zum Verkehrswert", null );
        add( uow, builder, template, "2.00810536E8", null, "13.0", null, "für Anschneideschaden" );
        add( uow, builder, template, "2.00810538E8", null, "77.0", null, "für Anschneideschaden" );
        add( uow, builder, template, "2.00810585E8", "20678.0", null, "= Differenz zum Verkehrswert", null );
        add( uow, builder, template, "2.00810612E8", "35802.0", null, "= Differenz zum Verkehrswert", null );
        add( uow, builder, template, "2.00810633E8", "22009.0", null, "=Differenz zum Verkehrswert", null );
        add( uow, builder, template, "2.00810655E8", null, "14.0", null, "für Anschneideschaden" );
        add( uow, builder, template, "2.00810656E8", null, "13.0", null, "für Anschneideschaden" );
        add( uow, builder, template, "2.00810659E8", null, "89.0", null, "Anschneideschaden und Erwerbsverlust" );
        add( uow, builder, template, "2.00810664E8", null, "43.0", null, "Anschneideschaden und Erwerbsverlust" );
        add( uow, builder, template, "2.00810665E8", null, "43.0", null, "Anschneideschaden" );
        add( uow, builder, template, "2.00810675E8", null, "3000.0", null, "für verbleibendes Inventar" );
        add( uow, builder, template, "2.00810712E8", null, "931.0", null, "Entschädigung für verbleibende Schäden" );
        add( uow, builder, template, "2.00810722E8", null, "1859.0", null, "für Aufwuchs und Wirtschaftsschäden" );
        add( uow, builder, template, "2.00810723E8", null, "4864.0", null, "für Aufwuchs und Wirtschaftsschäden" );
        add( uow, builder, template, "2.00810724E8", null, "521.0", null, "für verbl.Schäden und" );
        add( uow, builder, template, "2.00810725E8", null, "5527.0", null, "für verbleibende Schäden am Restgrdst." );
        add( uow, builder, template, "2.00810789E8", null, "1.0", null, "Wert für aufstehenden Schuppen" );
        add( uow, builder, template, "2.00810808E8", null, "3000.0", null, "für verbleibendes Inventar" );
        add( uow, builder, template, "2.00810859E8", "22168.0", null, "= Differenz zum Verkehrswert", null );
        add( uow, builder, template, "2.00810944E8", null, "16000.0", null, "für Ruder-+Motorboot,Einbauküche,Badmöbe" );
        add( uow, builder, template, "2.00811024E8", null, "27.0", null, "Entschädigung" );
        add( uow, builder, template, "2.00811025E8", null, "35.0", null, "Nebenentschädigung" );
        add( uow, builder, template, "2.00811092E8", "10071.0", null, "Differenz zum Verkehrswert", null );
        add( uow, builder, template, "2.00811126E8", null, "2000.0", null, "Wert Holzlaube" );
        add( uow, builder, template, "2.0082002E8", null, "4617.0", null, "aufstockender Bestand" );
        add( uow, builder, template, "2.0082005E8", null, "288.0", null, "Aufwandsentschädigung" );
        add( uow, builder, template, "2.00820928E8", "1507.0", null, "um die Daten auszuwerten", null );
        add( uow, builder, template, "2.00821015E8", null, "665.0", null, "für Aufwuchs" );
        add( uow, builder, template, "2.00900187E8", null, "900.0", null, "für Gebäude" );
        add( uow, builder, template, "2.00900227E8", null, "65687.0", null, null );
        add( uow, builder, template, "2.0090044E8", "145040.0", null, "Diff. zum Vw", null );
        add( uow, builder, template, "2.00900441E8", "72246.0", null, "Differenz zum Vw", null );
        add( uow, builder, template, "2.00900442E8", "2888.0", null, "Differenz zum Vw", null );
        add( uow, builder, template, "2.00900443E8", "11984.0", null, "Differenz zum Vw", null );
        add( uow, builder, template, "2.00900444E8", "434065.0", null, null, null );
        add( uow, builder, template, "2.00900449E8", "27640.0", null, "= Vw - Differenz zum KP", null );
        add( uow, builder, template, "2.0090045E8", "463.0", null, "= Differenz zum Vw", null );
        add( uow, builder, template, "2.00900451E8", "12497.0", null, "= Differenz zum Vw", null );
        add( uow, builder, template, "2.00900452E8", "43209.0", null, "= Differenz zum Vw", null );
        add( uow, builder, template, "2.00900453E8", "8636.0", null, "= Differenz zum Vw", null );
        add( uow, builder, template, "2.00900454E8", "44497.0", null, "= Differenz zum Vw", null );
        add( uow, builder, template, "2.00900495E8", "15764.0", null, null, null );
        add( uow, builder, template, "2.00900509E8", "172171.0", null, "Differenz zum Verkehrswert", null );
        add( uow, builder, template, "2.00900647E8", "222526.0", null, "Differenz zum KP = Vw der Flächen", null );
        add( uow, builder, template, "2.00900648E8", "39560.0", null, "= Vw Differenz zum KP", null );
        add( uow, builder, template, "2.00900663E8", null, "500.0", null, "Wert der Laube" );
        add( uow, builder, template, "2.00900942E8", null, "4815.0", null, null );
        add( uow, builder, template, "2.00900945E8", null, "336.0", null, null );
        add( uow, builder, template, "2.00901504E8", null, "2500.0", "Abzug fürs Inventar 2500 €", "Geh-und Fahrtrecht" );
        add( uow, builder, template, "2.00901728E8", "450.0", null, "zuzüglich zum KP 450 € Nutzungsentschädigung",
                "Wasserleitungsrecht" );
        add( uow, builder, template, "2.00902276E8", null, "684.0", null, "verbleibender Schaden am Restflurstück" );
        add( uow, builder, template, "2.00902277E8", null, "628.0", null, "verbleibender Schaden am Restflurstück" );
        add( uow, builder, template, "2.00902294E8", null, "7000.0", null, "Bungalow" );
        add( uow, builder, template, "2.00902296E8", null, "12257.0", null, "Aufwuchs, Zaun" );
        add( uow, builder, template, "2.0090242E8", null, "716.0", null, "verbl. Schaden am Restflurstück" );
        add( uow, builder, template, "2.00902423E8", null, "300.0", null, "für Laube" );
        add( uow, builder, template, "2.00902612E8", null, "4041.0", null, "=Vermessungskosten trägt Erwerber" );
        add( uow, builder, template, "2.00902481E8", null, "16000.0", null, "für Inventar" );
        add( uow, builder, template, "2.00902576E8", null, "26.0", null, "Bearbeitungsgebühr" );
        add( uow, builder, template, "2.00902647E8", null, "454.0", "454,36 € verbleibender Schaden am Restgrundstück",
                null );
        add( uow, builder, template, "2.00902665E8", null, "173109.0", null, "Kaufpreis nach AusglLeistG- s. 200902666" );
        add( uow, builder, template, "2.01301469E8", null, "9000.0", null, "für verbleibendes Inventar" );
        add( uow, builder, template, "2.00902666E8", null, "18674.0", null, "Kaufpreis nach Vw-s. 200902665" );
        add( uow, builder, template, "2.00902807E8", null, "55.0", "Sanierungsvermerk", null );
        add( uow, builder, template, "2.00902864E8", "34566.0", null, "= Differenz  Kaufpreis/Verkehrswert", null );
        add( uow, builder, template, "2.00903207E8", null, "1.0", null, "f. stark rep.-,san.-bedürftigen Schuppen" );
        add( uow, builder, template, "2.01301879E8", null, "375680.0", null, "für  Inventar" );
        add( uow, builder, template, "2.00903253E8", null, "1405.0", null, "f. verbleibenden Schaden an Restgrdst." );
        add( uow, builder, template, "2.0130201E8", "1932.0", null, "Zuschlag für verbleibende Restschäden", null );
        add( uow, builder, template, "2.01302178E8", null, "7982.0", null, "Schaden am Restflurstück" );
        add( uow, builder, template, "2.0120127E8", null, "9685.0", null, "EALG-75% Verzinsung Ausgleichsleistung" );
        add( uow, builder, template, "2.01201446E8", null, "13529.0", null, "Kaufpreisaufschlag nach AusglLeistG" );
        add( uow, builder, template, "2.01202763E8", null, "2248.0", null, "Nebenentschädigung" );
        add( uow, builder, template, "2.00903626E8", "8706.0", null, "Betrag muss abgezoen werden!!!!!",
                "falsche Auswertung (Sche.-11.09.2012)" );
        add( uow, builder, template, "2.00903646E8", "4419.0", null, "Betrag ist als Abschlag zu setzen",
                "=falsche Auswertung (Sche.11.09.2012)" );
        add( uow, builder, template, "2.01202911E8", null, "351.0", null, "Nebenentschädigung" );
        add( uow, builder, template, "2.00903878E8", "18571.0", null, "Anschneidschaden u. Entschädigung", null );
        add( uow, builder, template, "2.00903886E8", "206.0", null, "Entschädigung f. künft. Dienstbarkeit",
                "Pachtvertrag wird umgehend beendet" );
        add( uow, builder, template, "2.00903991E8", null, "5000.0", null, "für mitverkauftes Inventar" );
        add( uow, builder, template, "2.00904031E8", null, "2000.0", null, "für Inventar" );
        add( uow, builder, template, "2.0090405E8", null, "45000.0", null, "für Gebäude und Inventar" );
        add( uow, builder, template, "2.00904063E8", null, "3000.0", null, "für Inventar" );
        add( uow, builder, template, "2.01000361E8", null, "3388.0", null, "verbleibende Schäden am Restgrdst." );
        add( uow, builder, template, "2.01000375E8", "25.0", null, null, null );
        add( uow, builder, template, "2.01000387E8", null, "19804.0", null, "Betrag für Aufwuchs/Wirtschaftsschäden +" );
        add( uow, builder, template, "2.01000388E8", null, "22.0", null, "für verbleibenden Schaden" );
        add( uow, builder, template, "2.01000389E8", null, "43.0", null, "verbleib.Restschäden am Grdst." );
        add( uow, builder, template, "2.01001103E8", null, "3592.0", "vom KP entfallen auf Gr. u. Bo. 3908,32 €",
                "für Bungalow, Geräteschuppen" );
        add( uow, builder, template, "2.01001106E8", null, "8395.0", "Bungalow m. 2 Räumen, Küchenbereich,",
                "für Gebäude mit Inventar" );
        add( uow, builder, template, "2.01001206E8", null, "14100.0", null, null );
        add( uow, builder, template, "2.01001385E8", null, "4000.0", "Wegerecht - Zufahrt über Flst.  342/5",
                "für Gebäude" );
        add( uow, builder, template, "2.01001395E8", null, "2710.0", "Pachtvertrag mit Käufer endet",
                "Vermessungskosten" );
        add( uow, builder, template, "2.01001467E8", null, "5000.0", null, "für Inventar" );
        add( uow, builder, template, "2.01001627E8", "152.0", null, "Vorauszahlung wurde bereits getätigt", null );
        add( uow, builder, template, "2.01001629E8", "43.0", null, "Vorauszahlung wurde bereits getätigt", null );
        add( uow, builder, template, "2.01001642E8", "6000.0", null, "6000 € für bewegliche Sachen",
                "Brunnen auf Fremdflurstück" );
        add( uow, builder, template, "2.01001754E8", "125.0", null, "für Aufwuchs und Wirtschaftsschäden", null );
        add( uow, builder, template, "2.01001755E8", "212.0", null, "f. Wirtschaftsschäden u. Sachden Restgrundstück",
                null );
        add( uow, builder, template, "2.01001961E8", null, "2780.0", null, "für verbleib.Schäden am Restgrundstück" );
        add( uow, builder, template, "2.01001962E8", null, "919.0", null, "für verbl.Schäden am Restgrundstück" );
        add( uow, builder, template, "2.01001963E8", null, "1676.0", "Entschädigung für Aufwuchs",
                "für verbl.Schäden am Restgrundstück" );
        add( uow, builder, template, "2.01001964E8", null, "6061.0", null, "für verbleib. Restschäden am Grdst." );
        add( uow, builder, template, "2.01002086E8", null, "1000.0", "Strom, Trinkwasseranschluss",
                "diverse Einrichtungsgegenstände" );
        add( uow, builder, template, "2.01002161E8", "9600.0", null, null, null );
        add( uow, builder, template, "2.01002289E8", null, "5800.0", null, "für Inventar" );
        add( uow, builder, template, "2.01002387E8", "298.0", null, null, null );
        add( uow, builder, template, "2.0100241E8", "1219.0", null, null, null );
        add( uow, builder, template, "2.01002691E8", null, "1648.0", null, "für verbleib.Restschäden am Grdst." );
        add( uow, builder, template, "2.01100246E8", "378.0", null, "Bestandswert lt. GA Forstverwaltung", null );
        add( uow, builder, template, "2.01100275E8", null, "1296.0", null, "einmalige Entschädigung" );
        add( uow, builder, template, "2.01100676E8", null, "236.0", null, "Vermessungskosten" );
        add( uow, builder, template, "2.01101267E8", null, "1000.0", null, "mitverkauftes Inventar" );
        add( uow, builder, template, "2.01101442E8", null, "6762.0", null, "verbleibenden Schaden am Restflurstück" );
        add( uow, builder, template, "2.01101516E8", null, "5791.0", null, "für bauliche Außenanlagen/Aufwuchs" );
        add( uow, builder, template, "2.0110185E8", null, "57260.0", null, "für Meliorationsanlagen" );
        add( uow, builder, template, "2.01102215E8", null, "1555.0", null, "Nebenentschädigung" );
        add( uow, builder, template, "2.01102397E8", null, "2960.0", "Entschädigung für Aufwuchs 78,60 € und",
                "2881,50 € für Restschäden" );
        add( uow, builder, template, "2.01102662E8", null, "1000.0", null, "für Inventar u. Gartengeräte" );
        add( uow, builder, template, "2.01200061E8", "23872.0", null, "für aufstehenden Baumbestand = 0,32 €/m²",
                "Bodenwert = 0,12 €/m² - geä.Sche." );
        add( uow, builder, template, "2.0120009E8", null, "500.0", null, null );
        add( uow, builder, template, "2.01102848E8", null, "10659.0", "Leitungsrecht", null );
        add( uow, builder, template, "2.01203055E8", null, "3000.0", "erschl. mit Strom, Gartenwasseranschluss",
                "Inventar u. div. Werkzeuge und Geräte" );
        add( uow, builder, template, "2.01102927E8", null, "17615.0", null, "Nebenentschädigung" );
        add( uow, builder, template, "2.01102989E8", null, "4.0", null, "für verbleibenden Schaden am Restgrdst." );
        add( uow, builder, template, "2.01102925E8", null, "1288.0", null, "für verbleibenden Schaden am Restgrst." );
        add( uow, builder, template, "2.01102926E8", null, "104.0", null, "für verbleibenden Schaden am Restgrdst." );
        add( uow, builder, template, "2.0110303E8", null, "2000.0", null, "Gebäude" );
        add( uow, builder, template, "2.0120093E8", null, "8000.0", null, null );
        add( uow, builder, template, "2.01200968E8", null, "2000.0", "moderniesiert", "Abschlag für Inventar" );
        add( uow, builder, template, "2.01201384E8", "19.0", null, "Einmalzahlung Entschädigung (19,44 €)", null );
        add( uow, builder, template, "2.01201385E8", "38.0", null, "Einmalzahlung Entschädigung 38,16 €", null );
        add( uow, builder, template, "2.01201346E8", null, "300.0", null, "Inventar" );
        add( uow, builder, template, "2.01201277E8", null, "4718.0", null, "aufstockender Bestand" );
        add( uow, builder, template, "2.01201982E8", null, "5000.0", null, "Brennstoffvorräte, Kücheneinabauten" );
        add( uow, builder, template, "2.01201837E8", null, "5700.0", null, "Abschlag für Inventar" );
        add( uow, builder, template, "2.0120174E8", null, "87272.0", null, null );
        add( uow, builder, template, "2.01202163E8", null, "1500.0", null, "für bewegl. Sachen" );
        add( uow, builder, template, "2.01103327E8", "2496.0", null, "für aufstehenden Baumbestand 0,32 €/m²", null );
        add( uow, builder, template, "2.01202475E8", null, "2500.0", null, "Inventar" );
        add( uow, builder, template, "2.01202466E8", null, "5000.0", "Abschlag für Inventar", null );
        add( uow, builder, template, "2.0120262E8", "6216.0", null, null, null );
        add( uow, builder, template, "2.01202744E8", null, "2000.0", null, "für bewegliche Sachen" );
        add( uow, builder, template, "2.0120269E8", null, "1900.0", "Abschlag für Vermessungskosten", null );
        add( uow, builder, template, "2.01203084E8", null, "864.0", null, null );
        add( uow, builder, template, "2.01203085E8", null, "120.0", null, "Entschädigung" );
        add( uow, builder, template, "2.01203178E8", null, "1045.0", null, "Nebenentschädigung" );
        add( uow, builder, template, "2.01300286E8", null, "7232.0", null, "Schaden am Restflurstück" );
        add( uow, builder, template, "2.01300287E8", null, "795.0", null, "Schaden am Restflurstück" );
        add( uow, builder, template, "2.01300289E8", null, "10493.0", null, "Schaden am Restflurstück" );
        add( uow, builder, template, "2.0130029E8", null, "4116.0", null, "Schaden am Restflurstück" );
        add( uow, builder, template, "2.01300426E8", null, "7140.0", "Abschlag für Betrag für Baul. Anlagen",
                "und Bewuchs" );
        add( uow, builder, template, "2.0130056E8", null, "2786.0", null, "für Vermessungskosten" );
        add( uow, builder, template, "2.01300686E8", null, "6607.0", null, "Nebenentschädigung" );
        add( uow, builder, template, "2.01300783E8", null, "4000.0", null, "für bewegl. Inventar" );
        add( uow, builder, template, "2.01301082E8", null, "200.0", "Abschlag für Laube und Zaun", null );
        add( uow, builder, template, "2.01302488E8", null, "1536.0", null, "für Schäden am Restgrundstück" );
        add( uow, builder, template, "2.01301212E8", null, "26.0", null, "Nebenentschädigung für Anschneidung" );
        add( uow, builder, template, "2.01302493E8", null, "4400.0", null, "Wohnwagen mit Anbau" );
        add( uow, builder, template, "2.01302578E8", null, "25000.0", null, "Grundstück mit Wochenendhaus" );
        add( uow, builder, template, "2.01301215E8", null, "585.0", null, "Nebenentschädigung für Anschneidung" );
    }
    //
    // public static void main(String[] args) {
    // // System.out.println(Integer.parseInt( "2.00110231E8" ));
    // try {
    // System.out.println(;
    // }
    // catch (ParseException e) {
    // // TODO Auto-generated catch block
    //
    // }
    // }
}
