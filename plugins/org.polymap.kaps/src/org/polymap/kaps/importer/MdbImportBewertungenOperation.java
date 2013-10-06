package org.polymap.kaps.importer;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.runtime.SubMonitor;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.NHK2010GebaeudeArtProvider;
import org.polymap.kaps.model.data.ErmittlungModernisierungsgradComposite;
import org.polymap.kaps.model.data.ErtragswertverfahrenComposite;
import org.polymap.kaps.model.data.EtageComposite;
import org.polymap.kaps.model.data.FlurstueckComposite;
import org.polymap.kaps.model.data.FlurstuecksdatenAgrarComposite;
import org.polymap.kaps.model.data.FlurstuecksdatenBaulandComposite;
import org.polymap.kaps.model.data.NHK2010AnbautenComposite;
import org.polymap.kaps.model.data.NHK2010BaupreisIndexComposite;
import org.polymap.kaps.model.data.NHK2010BewertungComposite;
import org.polymap.kaps.model.data.NHK2010BewertungGebaeudeComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.model.data.VertragsdatenErweitertComposite;
import org.polymap.kaps.model.data.WohnungComposite;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class MdbImportBewertungenOperation
        extends AbstractMdbImportOperation {

    private static Log log = LogFactory.getLog( MdbImportBewertungenOperation.class );


    public MdbImportBewertungenOperation( File dbFile, String[] tableNames ) {
        super( dbFile, tableNames, "Kaufpreissammlung Bewertungen importieren" );
    }


    protected IStatus doExecute0( IProgressMonitor monitor, IAdaptable info )
            throws Exception {
        File parentFolder = new File( "kaps" );
        parentFolder.mkdirs();

        monitor.beginTask( getLabel(), 12000 );
        final Database db = Database.open( dbFile );
        try {
            SubMonitor sub = null;

            sub = new SubMonitor( monitor, 10 );
            importEntity( db, monitor, NHK2010AnbautenComposite.class, new EntityCallback<NHK2010AnbautenComposite>() {

                @Override
                public void fillEntity( NHK2010AnbautenComposite entity, Map<String, Object> builderRow ) {
                    // bewertung finden
                    entity.schl().set( String.valueOf( (Integer)builderRow.get( "SCHL" ) ) );
                }
            } );
            sub = new SubMonitor( monitor, 10 );
            importEntity( db, monitor, NHK2010BaupreisIndexComposite.class, new EntityCallback<NHK2010BaupreisIndexComposite>() {

                @Override
                public void fillEntity( NHK2010BaupreisIndexComposite entity, Map<String, Object> builderRow ) {
                    // jahr von bis
                    Double value = (Double)builderRow.get( "JAHR" );
                    entity.jahr().set( value.intValue() );
                    value = (Double)builderRow.get( "MONVON" );
                    entity.monatVon().set( value.intValue() );
                    value = (Double)builderRow.get( "MONBIS" );
                    entity.monatBis().set( value.intValue() );
                }
            } );

            sub = new SubMonitor( monitor, 10 );
            importNHK2010Bewertung( db, sub, parentFolder );

            sub = new SubMonitor( monitor, 10 );
            importEntity( db, monitor, ErtragswertverfahrenComposite.class,
                    new EntityCallback<ErtragswertverfahrenComposite>() {

                        @Override
                        public void fillEntity( ErtragswertverfahrenComposite entity, Map<String, Object> builderRow ) {

                            // Geschosse
                            entity.etageZeile1().set( findSchlNamed( EtageComposite.class, builderRow, "G1", false ) );
                            entity.etageZeile2().set( findSchlNamed( EtageComposite.class, builderRow, "G2", false ) );
                            entity.etageZeile3().set( findSchlNamed( EtageComposite.class, builderRow, "G3", false ) );
                            entity.etageZeile4().set( findSchlNamed( EtageComposite.class, builderRow, "G4", false ) );
                            entity.etageZeile5().set( findSchlNamed( EtageComposite.class, builderRow, "G5", false ) );
                            entity.etageZeile6().set( findSchlNamed( EtageComposite.class, builderRow, "G6", false ) );
                            entity.etageZeile7().set( findSchlNamed( EtageComposite.class, builderRow, "G7", false ) );

                            Short berbauj = (Short)builderRow.get( "BERBAUJ" );
                            if (berbauj != null) {
                                entity.bereinigtesBaujahr().set( asDouble( berbauj.intValue() ) );
                            }
                            entity.gewichtungLiegenschaftszins().set( asDouble( (Integer)builderRow.get( "GEWICHT" ) ) );
                            entity.tatsaechlichesBaujahr().set( asDouble( (Integer)builderRow.get( "BAUJAHR" ) ) );

                            Double eingangsnummer = (Double)builderRow.get( "EINGANGSNR" );
                            VertragComposite vertragTemplate = QueryExpressions.templateFor( VertragComposite.class );
                            BooleanExpression expr = QueryExpressions.eq( vertragTemplate.eingangsNr(),
                                    eingangsnummer.intValue() );
                            VertragComposite vertrag = KapsRepository.instance()
                                    .findEntities( VertragComposite.class, expr, 0, 1 ).find();
                            if (vertrag == null) {
                                throw new IllegalStateException( "no vertrag found for " + eingangsnummer );
                            }
                            entity.vertrag().set( vertrag );

                            entity.pauschalBetriebskosten().set( getBooleanValue( builderRow, "pauschal" ) );
                            entity.pauschalBewirtschaftungskosten().set( getBooleanValue( builderRow, "pauschalbew" ) );
                            entity.liziVerwenden().set( getBooleanValue( builderRow, "LIZI" ) );
                            entity.innenBereich().set( getBooleanValue( builderRow, "INNEN" ) );
                            entity.bewirtschaftskostenInProzent().set( getBooleanValue( builderRow, "ANGABEPROZ" ) );
                            entity.bodenwertAnteilIndividuell().set( getBooleanValue( builderRow, "BWANT_IND" ) );
                            entity.eingabeGesamtMiete().set( getBooleanValue( builderRow, "GESMIETE_EING" ) );
                            entity.wohnflaecheZeile1().set( getBooleanValue( builderRow, "WOHNFL1" ) );
                            entity.wohnflaecheZeile2().set( getBooleanValue( builderRow, "WOHNFL2" ) );
                            entity.wohnflaecheZeile3().set( getBooleanValue( builderRow, "WOHNFL3" ) );
                            entity.wohnflaecheZeile4().set( getBooleanValue( builderRow, "WOHNFL4" ) );
                            entity.wohnflaecheZeile5().set( getBooleanValue( builderRow, "WOHNFL5" ) );
                            entity.wohnflaecheZeile6().set( getBooleanValue( builderRow, "WOHNFL6" ) );
                            entity.wohnflaecheZeile7().set( getBooleanValue( builderRow, "WOHNFL7" ) );

                            if (entity.nettoRohertragProMonat().get() != null) {
                                entity.nettoRohertragProJahr().set( 12 * entity.nettoRohertragProMonat().get() );
                            }
                            if (entity.bruttoRohertragProMonat().get() != null) {
                                entity.bruttoRohertragProJahr().set( 12 * entity.bruttoRohertragProMonat().get() );
                            }
                            entity.jahresBetriebskostenE().set( entity.jahresBetriebskosten().get() );
                            entity.anteiligeBetriebskosten().set( entity.jahresBetriebskosten().get() );

                            VertragsdatenErweitertComposite ev = vertrag.erweiterteVertragsdaten().get();
                            if (ev != null) {
                                entity.bereinigterKaufpreis().set( ev.bereinigterVollpreis().get() );
                            }

                            Double bw = entity.bodenwert().get();
                            Double freilegung = entity.freilegung().get();

                            entity.bodenwertAbzglFreilegung().set(
                                    bw != null ? (freilegung != null ? bw - freilegung : bw) : null );
                        }
                    } );

            sub = new SubMonitor( monitor, 10 );
            importBemerkungen( db, monitor, parentFolder );
            
            sub = new SubMonitor( monitor, 10 );
            importModernisierungsgrad( db, sub, parentFolder );
        }
        finally {
            db.close();
        }

        return Status.OK_STATUS;
    }


    private void importNHK2010Bewertung( Database db, IProgressMonitor monitor, final File parentFolder )
            throws Exception {
        Table table = db.getTable( "K_BEWERTBGF10" );
        monitor.beginTask( "Tabelle: " + table.getName(), table.getRowCount() );

        AnnotatedCompositeImporter bewertungImporter = new AnnotatedCompositeImporter( NHK2010BewertungComposite.class,
                table );
        Map<Double, NHK2010BewertungComposite> allBewertungen = new HashMap<Double, NHK2010BewertungComposite>();

        Map<String, Object> builderRow = null;
        int count = 0;
        while ((builderRow = table.getNextRow()) != null) {
            // alle Bewertungen importieren

            Integer lfd = (Integer)builderRow.get( "LFDNR" );
            if (0 == lfd) {
                // Bewertung gefunden erstellen
                NHK2010BewertungComposite bewertung = repo.newEntity( NHK2010BewertungComposite.class, null );
                Double eingangsnummer = (Double)builderRow.get( "EINGANGSNR" );
                if (eingangsnummer != null) {
                    VertragComposite vertragTemplate = QueryExpressions.templateFor( VertragComposite.class );
                    BooleanExpression expr = QueryExpressions.eq( vertragTemplate.eingangsNr(),
                            eingangsnummer.intValue() );
                    VertragComposite vertrag = KapsRepository.instance()
                            .findEntities( VertragComposite.class, expr, 0, 1 ).find();
                    if (vertrag == null) {
                        throw new IllegalStateException( "no vertrag found for " + eingangsnummer );
                    }
                    bewertung.vertrag().set( vertrag );

                    allBewertungen.put( eingangsnummer, bewertung );
                    bewertungImporter.fillEntity( bewertung, builderRow );

                    // anbauten
                    String anbauten = (String)builderRow.get( "ANBAUTEN" );
                    if (anbauten != null && !anbauten.isEmpty()) {
                        String[] anbautenEinzeln = anbauten.split( "," );
                        for (String anbaute : anbautenEinzeln) {
                            String anbauteTrimmed = anbaute.trim();
                            if (anbauteTrimmed.startsWith( "0" )) {
                                anbauteTrimmed = anbauteTrimmed.substring( 1 );
                            }
                            NHK2010AnbautenComposite anbauTemplate = QueryExpressions.templateFor( NHK2010AnbautenComposite.class );
                            BooleanExpression expr2 = QueryExpressions.eq( anbauTemplate.schl(), anbauteTrimmed );
                            NHK2010AnbautenComposite anbau = KapsRepository.instance()
                                    .findEntities( NHK2010AnbautenComposite.class, expr2, 0, 1 ).find();
                            if (anbau == null) {
                                throw new IllegalStateException( "no anbau found for " + anbauteTrimmed );
                            }
                            bewertung.anbauten().add( anbau );
                        }
                    }
                }
                else {
                    throw new IllegalStateException( "no EINGANGSNR found" );
                }
                count++;
            }
            // andernfalls erstmal ignorieren
        }
        // wmvaopfW.flush();
        // wmvaopfW.close();
        repo.commitChanges();
        log.info( "Imported and committed: K_BEWERTBGF10 as Bewertung-> " + count );

        table = db.getTable( "K_BEWERTBGF10" );
        monitor.beginTask( "Tabelle: " + table.getName(), table.getRowCount() );
        AnnotatedCompositeImporter bewertungGebaeudeImporter = new AnnotatedCompositeImporter(
                NHK2010BewertungGebaeudeComposite.class, table );
        count = 0;
        NHK2010GebaeudeArtProvider gebaeudeArtenProvider = NHK2010GebaeudeArtProvider.instance();
        while ((builderRow = table.getNextRow()) != null) {
            // alle Bewertungen importieren

            Integer lfd = (Integer)builderRow.get( "LFDNR" );
            if (0 != lfd) {
                // Bewertung gefunden erstellen
                NHK2010BewertungGebaeudeComposite bewertungGebaeude = repo.newEntity(
                        NHK2010BewertungGebaeudeComposite.class, null );
                Double eingangsnummer = (Double)builderRow.get( "EINGANGSNR" );
                if (eingangsnummer != null) {
                    NHK2010BewertungComposite nhk2010BewertungComposite = allBewertungen.get( eingangsnummer );
                    if (nhk2010BewertungComposite == null) {
                        throw new IllegalStateException( "no bewertung found for " + eingangsnummer );
                    }
                    bewertungGebaeude.bewertung().set( nhk2010BewertungComposite );
                }
                else {
                    throw new IllegalStateException( "no EINGANGSNR found" );
                }
                bewertungGebaeudeImporter.fillEntity( bewertungGebaeude, builderRow );
                bewertungGebaeude.bereinigtesBaujahr().set( asDouble( (Integer)builderRow.get( "BERBAUJ1" ) ) );
                bewertungGebaeude.tatsaechlichesBaujahr().set( asDouble( (Integer)builderRow.get( "BAUJ1" ) ) );
                bewertungGebaeude.gesamtNutzungsDauer().set( asDouble( (Integer)builderRow.get( "GND" ) ) );
                bewertungGebaeude.restNutzungsDauer().set( asDouble( (Integer)builderRow.get( "RND" ) ) );
                bewertungGebaeude.alter().set( asDouble( (Integer)builderRow.get( "ALTER1" ) ) );

                Integer hnr = (Integer)builderRow.get( "HAUPTNR" );
                if (hnr != null) {
                    Integer nr = (Integer)builderRow.get( "NR" );
                    Integer unternr = (Integer)builderRow.get( "UNTERNR" );
                    bewertungGebaeude.gebaeudeArtId().set(
                            gebaeudeArtenProvider.gebaeudeForNumber( hnr, nr, unternr ).getId() );
                }
                bewertungGebaeude.zweifamilienHaus().set( getBooleanValue( builderRow, "FAMHAUS2" ) );

                Integer zimmer = (Integer)builderRow.get( "ANZZIMMER" );
                if (zimmer != null) {
                    bewertungGebaeude.anzahlWohnungen().set( zimmer.doubleValue() );
                }

                bewertungGebaeude.zeitwertRnd().set( bewertungGebaeude.gebaeudeZeitWert().get() );
                // alterswertminderung
                Double gnd = bewertungGebaeude.gesamtNutzungsDauer().get();
                Double rnd = bewertungGebaeude.restNutzungsDauer().get();
                if (gnd != null && rnd != null && gnd != 0) {
                    bewertungGebaeude.altersWertMinderung().set(
                            (gnd.doubleValue() - rnd.doubleValue()) / gnd.doubleValue() * 100 );
                }

                count++;
            }
            // andernfalls ignorieren
        }
        // wmvaopfW.flush();
        // wmvaopfW.close();
        repo.commitChanges();
        log.info( "Imported and committed: K_BEWERTBGF10 as BewertungGebaeude -> " + count );
        monitor.done();
    }


    private void importModernisierungsgrad( Database db, IProgressMonitor monitor, final File parentFolder )
            throws Exception {
        Table table = db.getTable( "K_BAUJAHRRECH" );
        monitor.beginTask( "Tabelle: " + table.getName(), table.getRowCount() );

        final BufferedWriter wmvaopfW = new BufferedWriter( new FileWriter( new File( parentFolder,
                "fehler_ermittlung_modernisierungsgrad.txt" ) ) );
        AnnotatedCompositeImporter bewertungImporter = new AnnotatedCompositeImporter(
                ErmittlungModernisierungsgradComposite.class, table );

        Calendar cal = new GregorianCalendar();
        double currentYear = new Integer( cal.get( Calendar.YEAR ) ).doubleValue();

        Map<String, Object> builderRow = null;
        int count = 0;
        while ((builderRow = table.getNextRow()) != null) {
            // alle Bewertungen importieren

            String type = (String)builderRow.get( "BEREICH" );
            if ("frm_wwohnneu".equals( type ) || "frm_sachwnhk2010".equals( type ) || "frm_ertragswertn".equals( type )) {
                // Bewertung gefunden erstellen
                ErmittlungModernisierungsgradComposite b = repo.newEntity(
                        ErmittlungModernisierungsgradComposite.class, null );
                Double eingangsnummer = (Double)builderRow.get( "EINGANGSNR" );
                if (eingangsnummer != null) {
                    VertragComposite vertragTemplate = QueryExpressions.templateFor( VertragComposite.class );
                    BooleanExpression expr = QueryExpressions.eq( vertragTemplate.eingangsNr(),
                            eingangsnummer.intValue() );
                    VertragComposite vertrag = KapsRepository.instance()
                            .findEntities( VertragComposite.class, expr, 0, 1 ).find();
                    if (vertrag == null) {
                        throw new IllegalStateException( "no vertrag found for " + eingangsnummer );
                    }
                    b.vertrag().set( vertrag );
                }
                bewertungImporter.fillEntity( b, builderRow );

                // obergrenze auf default setzen, wenn nicht gefüllt
                boolean isWohn = "frm_wwohnneu".equals( type );
                b.alterObergrenzeZeile1().set(
                        asDouble( (Integer)builderRow.get( "ALTER_OBERGRENZE2" ), 100, isWohn ? 20 : 40 ) );
                b.alterObergrenzeZeile2().set( asDouble( (Integer)builderRow.get( "ALTER_OBERGRENZE3" ), 100, 20 ) );
                b.alterObergrenzeZeile3().set( asDouble( (Integer)builderRow.get( "ALTER_OBERGRENZE4" ), 100, 20 ) );
                b.alterObergrenzeZeile4().set(
                        asDouble( (Integer)builderRow.get( "ALTER_OBERGRENZE5" ), 100, isWohn ? 20 : 15 ) );
                b.alterObergrenzeZeile5().set(
                        asDouble( (Integer)builderRow.get( "ALTER_OBERGRENZE6" ), 100, isWohn ? 20 : 30 ) );
                b.alterObergrenzeZeile6().set(
                        asDouble( (Integer)builderRow.get( "ALTER_OBERGRENZE7" ), 100, isWohn ? 20 : 15 ) );
                b.alterObergrenzeZeile7().set(
                        asDouble( (Integer)builderRow.get( "ALTER_OBERGRENZE8" ), 100, isWohn ? 20 : 15 ) );
                b.alterObergrenzeZeile8().set(
                        asDouble( (Integer)builderRow.get( "ALTER_OBERGRENZE9" ), 100, isWohn ? 20 : 30 ) );

                if (b.alterObergrenzeZeile1().get() != null && b.alterObergrenzeZeile1().get() > 100) {
                    Object v = builderRow.get( "ALTER_OBERGRENZE2" );
                    System.out.println( "gefunden" );
                }
                if ("frm_wwohnneu".equals( type )) {
                    // 6 und 7 mergen
                    if (b.punkteZeile6().get() == null && b.punkteZeile7().get() != null) {
                        b.punkteZeile6().set( b.punkteZeile7().get() );
                        b.alterObergrenzeZeile6().set( b.alterObergrenzeZeile7().get() );
                        b.alterZeile6().set( b.alterZeile7().get() );
                    }
                    // 8 wird 7
                    b.punkteZeile7().set( b.punkteZeile8().get() );
                    b.alterObergrenzeZeile7().set( b.alterObergrenzeZeile8().get() );
                    b.alterZeile7().set( b.alterZeile8().get() );
                    // 10 wird 8
                    b.punkteZeile8().set( (Double)builderRow.get( "PUNKTE10" ) );
                    b.alterObergrenzeZeile8().set( asDouble( (Integer)builderRow.get( "ALTER_OBERGRENZE10" ) ) );
                    b.alterZeile8().set( (Double)builderRow.get( "ALTER10" ) );
                }

                // auswirkungen berechnen
                // Zeile 1
                if (b.alterObergrenzeZeile1().get() != null && b.alterObergrenzeZeile1().get() > 0.0
                        && b.alterZeile1().get() != null && b.alterZeile1().get() > 0.0
                        && b.punkteZeile1().get() != null) {
                    b.auswirkungZeile1().set(
                            (1 - b.alterZeile1().get() / b.alterObergrenzeZeile1().get()) * b.punkteZeile1().get() );
                }
                else {
                    b.auswirkungZeile1().set( b.punkteZeile1().get() );
                }
                // Zeile 2
                if (b.alterObergrenzeZeile2().get() != null && b.alterObergrenzeZeile2().get() > 0.0
                        && b.alterZeile2().get() != null && b.alterZeile2().get() > 0.0
                        && b.punkteZeile2().get() != null) {
                    b.auswirkungZeile2().set(
                            (1 - b.alterZeile2().get() / b.alterObergrenzeZeile2().get()) * b.punkteZeile2().get() );
                }
                else {
                    b.auswirkungZeile2().set( b.punkteZeile2().get() );
                }
                // Zeile 1
                if (b.alterObergrenzeZeile3().get() != null && b.alterObergrenzeZeile3().get() > 0.0
                        && b.alterZeile3().get() != null && b.alterZeile3().get() > 0.0
                        && b.punkteZeile3().get() != null) {
                    b.auswirkungZeile3().set(
                            (1 - b.alterZeile3().get() / b.alterObergrenzeZeile3().get()) * b.punkteZeile3().get() );
                }
                else {
                    b.auswirkungZeile3().set( b.punkteZeile3().get() );
                }
                // Zeile 4
                if (b.alterObergrenzeZeile4().get() != null && b.alterObergrenzeZeile4().get() > 0.0
                        && b.alterZeile4().get() != null && b.alterZeile4().get() > 0.0
                        && b.punkteZeile4().get() != null) {
                    b.auswirkungZeile4().set(
                            (1 - b.alterZeile4().get() / b.alterObergrenzeZeile4().get()) * b.punkteZeile4().get() );
                }
                else {
                    b.auswirkungZeile4().set( b.punkteZeile4().get() );
                }
                // Zeile 5
                if (b.alterObergrenzeZeile5().get() != null && b.alterObergrenzeZeile5().get() > 0.0
                        && b.alterZeile5().get() != null && b.alterZeile5().get() > 0.0
                        && b.punkteZeile5().get() != null) {
                    b.auswirkungZeile5().set(
                            (1 - b.alterZeile5().get() / b.alterObergrenzeZeile5().get()) * b.punkteZeile5().get() );
                }
                else {
                    b.auswirkungZeile5().set( b.punkteZeile5().get() );
                }
                // Zeile 6
                if (b.alterObergrenzeZeile6().get() != null && b.alterObergrenzeZeile6().get() > 0.0
                        && b.alterZeile6().get() != null && b.alterZeile6().get() > 0.0
                        && b.punkteZeile6().get() != null) {
                    b.auswirkungZeile6().set(
                            (1 - b.alterZeile6().get() / b.alterObergrenzeZeile6().get()) * b.punkteZeile6().get() );
                }
                else {
                    b.auswirkungZeile6().set( b.punkteZeile6().get() );
                }
                // Zeile 7
                if (b.alterObergrenzeZeile7().get() != null && b.alterObergrenzeZeile7().get() > 0.0
                        && b.alterZeile7().get() != null && b.alterZeile7().get() > 0.0
                        && b.punkteZeile7().get() != null) {
                    b.auswirkungZeile7().set(
                            (1 - b.alterZeile7().get() / b.alterObergrenzeZeile7().get()) * b.punkteZeile7().get() );
                }
                else {
                    b.auswirkungZeile7().set( b.punkteZeile7().get() );
                }
                // Zeile 8
                if (b.alterObergrenzeZeile8().get() != null && b.alterObergrenzeZeile8().get() > 0.0
                        && b.alterZeile8().get() != null && b.alterZeile8().get() > 0.0
                        && b.punkteZeile8().get() != null) {
                    b.auswirkungZeile8().set(
                            (1 - b.alterZeile8().get() / b.alterObergrenzeZeile8().get()) * b.punkteZeile8().get() );
                }
                else {
                    b.auswirkungZeile8().set( b.punkteZeile8().get() );
                }

                b.bereinigtesBaujahr().set( asDouble( (Integer)builderRow.get( "BERBAUJ" ) ) );
                b.gesamtNutzungsDauer().set( asDouble( (Integer)builderRow.get( "GND" ) ) );

                // modernisierungsgrad berechnen
                Double grad = 0.0d;
                if (b.auswirkungZeile1().get() != null) {
                    grad += b.auswirkungZeile1().get();
                }
                if (b.auswirkungZeile2().get() != null) {
                    grad += b.auswirkungZeile2().get();
                }
                if (b.auswirkungZeile3().get() != null) {
                    grad += b.auswirkungZeile3().get();
                }
                if (b.auswirkungZeile4().get() != null) {
                    grad += b.auswirkungZeile4().get();
                }
                if (b.auswirkungZeile5().get() != null) {
                    grad += b.auswirkungZeile5().get();
                }
                if (b.auswirkungZeile6().get() != null) {
                    grad += b.auswirkungZeile6().get();
                }
                if (b.auswirkungZeile7().get() != null) {
                    grad += b.auswirkungZeile7().get();
                }
                if (b.auswirkungZeile8().get() != null) {
                    grad += b.auswirkungZeile8().get();
                }
                b.modernisierungsGrad().set( grad );

                // GND + tatsächliches Baujahr + bereinigtes Baujahr wenn nicht
                // gesetzt laden
                if ("frm_sachwnhk2010".equals( type )) {
                    NHK2010BewertungComposite nhk2010 = NHK2010BewertungComposite.Mixin.forVertrag( b.vertrag().get() );
                    if (nhk2010 == null) {
                        wmvaopfW.write( "keine NHK gefunden fuer " + b.vertrag().get().eingangsNr().get() + "\n" );
                    }
                    else {
                        for (NHK2010BewertungGebaeudeComposite gebaeude : NHK2010BewertungGebaeudeComposite.Mixin
                                .forBewertung( nhk2010 )) {
                            if (b.gebaeudeNummer().get() == gebaeude.laufendeNummer().get()) {
                                b.nhk2010().set( gebaeude );
                                if (b.bereinigtesBaujahr().get() == null) {
                                    b.bereinigtesBaujahr().set( gebaeude.bereinigtesBaujahr().get() );
                                }
                                b.gesamtNutzungsDauer().set( gebaeude.gesamtNutzungsDauer().get() );
                                b.tatsaechlichesBaujahr().set( gebaeude.tatsaechlichesBaujahr().get() );
                            }
                        }
                    }
                }
                else if ("frm_wwohnneu".equals( type )) {
                    // suche wohnung
                    WohnungComposite wohnung = WohnungComposite.Mixin.forKeys( b.objektNummer().get(), b
                            .objektFortfuehrung().get(), b.gebaeudeNummer().get(), b.gebaeudeFortfuehrung().get(), b
                            .wohnungsNummer().get(), b.wohnungsFortfuehrung().get() );
                    if (wohnung == null) {
                        wmvaopfW.write( "keine wohnung gefunden fuer " + b.objektNummer().get() + ", "
                                + b.objektFortfuehrung().get() + ", " + b.gebaeudeNummer().get() + ", "
                                + b.gebaeudeFortfuehrung().get() + ", " + b.wohnungsNummer().get() + ", "
                                + b.wohnungsFortfuehrung().get() + "\n" );
                    }
                    else {
                        b.wohnung().set( wohnung );
                        if (b.bereinigtesBaujahr().get() == null) {
                            b.bereinigtesBaujahr().set( wohnung.bereinigtesBaujahr().get() );
                        }
                        b.gesamtNutzungsDauer().set( wohnung.gesamtNutzungsDauer().get() );
                        b.tatsaechlichesBaujahr().set( wohnung.baujahr().get() );
                    }
                }
                else if ("frm_ertragswertn".equals( type )) {
                    ErtragswertverfahrenComposite ec = ErtragswertverfahrenComposite.Mixin.forVertrag( b.vertrag()
                            .get() );
                    if (ec == null) {
                        wmvaopfW.write( "kein Ertragswertverfahren gefunden fuer "
                                + +b.vertrag().get().eingangsNr().get() + "\n" );
                    }
                    else {
                        b.ertragswertVerfahren().set( ec );
                        if (b.bereinigtesBaujahr().get() == null) {
                            b.bereinigtesBaujahr().set( ec.bereinigtesBaujahr().get() );
                        }
                        b.gesamtNutzungsDauer().set( ec.gesamtNutzungsDauer().get() );
                        b.tatsaechlichesBaujahr().set( ec.tatsaechlichesBaujahr().get() );
                    }
                }
                else {
                    wmvaopfW.write( "unbekannter Typ " + type + "\n" );
                }

                if (b.gesamtNutzungsDauer().get() == null) {
                    b.gesamtNutzungsDauer().set( 80.0d );
                }
                if (b.tatsaechlichesBaujahr().get() != null) {
                    double alter = currentYear - b.tatsaechlichesBaujahr().get();
                    double gnd = b.gesamtNutzungsDauer().get();
                    if (alter >= gnd) {
                        b.restNutzungsDauer().set( 0.0d );
                    }
                    else {
                        b.restNutzungsDauer().set( gnd - alter );
                    }
                }
                if (b.bereinigtesBaujahr().get() != null) {
                    double alter = currentYear - b.bereinigtesBaujahr().get();
                    double gnd = b.gesamtNutzungsDauer().get();
                    if (alter >= gnd) {
                        b.neueRestNutzungsDauer().set( 0.0d );
                    }
                    else {
                        b.neueRestNutzungsDauer().set( gnd - alter );
                    }
                }
                else {
                    b.neueRestNutzungsDauer().set( b.restNutzungsDauer().get() );
                }
                count++;
            }
        }
        repo.commitChanges();
        log.info( "Imported and committed: K_BEWERTBGF10 as Bewertung-> " + count );
        monitor.done();
        wmvaopfW.flush();
        wmvaopfW.close();
    }


    private void importBemerkungen( Database db, IProgressMonitor monitor, final File parentFolder )
            throws Exception {
        Table table = db.getTable( "K_BEMERKUNG" );
        monitor.beginTask( "Tabelle: " + table.getName(), table.getRowCount() );

        final BufferedWriter wmvaopfW = new BufferedWriter( new FileWriter( new File( parentFolder,
                "fehler_import_bemerkung.txt" ) ) );

        Map<String, Object> builderRow = null;
        int count = 0;
        while ((builderRow = table.getNextRow()) != null) {
            // alle Bewertungen importieren
            String maske = (String)builderRow.get( "MASKE" );
            Integer eingangsnummer = (Integer)builderRow.get( "EINGANGSNR" );
            String text = (String)builderRow.get( "BEZ1" );
            VertragComposite vertragTemplate = QueryExpressions.templateFor( VertragComposite.class );
            BooleanExpression expr = QueryExpressions.eq( vertragTemplate.eingangsNr(), eingangsnummer.intValue() );
            VertragComposite vertrag = KapsRepository.instance().findEntities( VertragComposite.class, expr, 0, 1 )
                    .find();
            if (vertrag == null) {
                wmvaopfW.write( "no vertrag found for " + eingangsnummer + "\n" );
            }
            else {

                if ("frm_buch".equals( maske )) {
                    String old = vertrag.bemerkungen().get();
                    vertrag.bemerkungen().set( old != null ? old + "\n" + text : text );
                }
                else if ("frm_gveditbau".equals( maske )) {
                    boolean found = false;
                    for (FlurstueckComposite fs : FlurstueckComposite.Mixin.forEntity( vertrag )) {
                        FlurstuecksdatenBaulandComposite bc = FlurstuecksdatenBaulandComposite.Mixin.forFlurstueck( fs );
                        if (bc != null) {
                            bc.bemerkungen().set( text );
                            found = true;
                        }
                    }
                    if (!found) {
                        wmvaopfW.write( "no FlurstuecksdatenBaulandComposite found for " + eingangsnummer + "\n" );
                    }
                }
                else if ("frm_gveditagrar".equals( maske )) {
                    boolean found = false;
                    for (FlurstueckComposite fs : FlurstueckComposite.Mixin.forEntity( vertrag )) {
                        FlurstuecksdatenAgrarComposite bc = FlurstuecksdatenAgrarComposite.Mixin.forFlurstueck( fs );
                        if (bc != null) {
                            String old = bc.bemerkungen().get();
                            bc.bemerkungen().set( old != null ? old + "\n" + text : text );
                            found = true;
                        }
                    }
                    if (!found) {
                        wmvaopfW.write( "no FlurstuecksdatenBaulandComposite found for " + eingangsnummer + "\n" );
                    }
                }
                else if ("frm_ertragswertn_n".equals( maske )) {
                    ErtragswertverfahrenComposite ec = ErtragswertverfahrenComposite.Mixin.forVertrag( vertrag );
                    ec.bemerkungen().set( text );
                }
                else {
                    throw new IllegalStateException( "unbekannte MASKE " + maske );
                }
            }
            count++;
        }
        repo.commitChanges();
        log.info( "Imported and committed: K_BEMERKUNG -> " + count );
        monitor.done();
        wmvaopfW.flush();
        wmvaopfW.close();
    }

}
