package org.polymap.kaps.importer;

import java.util.HashMap;
import java.util.Map;

import java.io.File;

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
import org.polymap.kaps.model.data.NHK2000BewertungComposite;
import org.polymap.kaps.model.data.NHK2000BewertungGebaeudeComposite;
import org.polymap.kaps.model.data.VertragComposite;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class MdbImportNHK2000Operation
        extends AbstractMdbImportOperation {

    private static Log log = LogFactory.getLog( MdbImportNHK2000Operation.class );


    public MdbImportNHK2000Operation( File dbFile, String[] tableNames ) {
        super( dbFile, tableNames, "Kaufpreissammlung Bewertungen importieren" );
    }


    protected IStatus doExecute0( IProgressMonitor monitor, IAdaptable info )
            throws Exception {
        File parentFolder = new File( "kaps" );
        parentFolder.mkdirs();

        monitor.beginTask( getLabel(), 3500 );
        final Database db = Database.open( dbFile );
        try {
            SubMonitor sub = null;
            sub = new SubMonitor( monitor, 3500 );
            importNHK2000Bewertung( db, sub, parentFolder );

        }
        finally {
            db.close();
        }

        return Status.OK_STATUS;
    }


    private void importNHK2000Bewertung( Database db, IProgressMonitor monitor, final File parentFolder )
            throws Exception {
        Table table = db.getTable( "K_BEWERTBGF00" );
        monitor.beginTask( "Tabelle: " + table.getName(), table.getRowCount() );

        AnnotatedCompositeImporter bewertungImporter = new AnnotatedCompositeImporter( NHK2000BewertungComposite.class,
                table );
        Map<Double, NHK2000BewertungComposite> allBewertungen = new HashMap<Double, NHK2000BewertungComposite>();

        Map<String, Object> builderRow = null;
        int count = 0;
        while ((builderRow = table.getNextRow()) != null) {
            // alle Bewertungen importieren

            Integer lfd = (Integer)builderRow.get( "LFDNR" );
            if (0 == lfd) {
                // Bewertung gefunden erstellen
                NHK2000BewertungComposite bewertung = repo.newEntity( NHK2000BewertungComposite.class, null );
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

                    bewertung.MAKLERBW().set( getBooleanValue( builderRow, "MAKLERBW" ) );
                    Double berzeitwert = bewertung.GESBAUWERT().get();
                    if (bewertung.AUSVHBETR().get() != null) {
                        berzeitwert -= bewertung.AUSVHBETR().get();
                    }
                    bewertung.BERZEITW1().set( berzeitwert );
                }
                else {
                    throw new IllegalStateException( "no EINGANGSNR found" );
                }
                if ((++count % 200) == 0) {
                    monitor.worked( 200 );
                    monitor.setTaskName( "Objekte: " + count );
                    repo.commitChanges();
                }
            }
            // andernfalls erstmal ignorieren
        }
        // wmvaopfW.flush();
        // wmvaopfW.close();
        repo.commitChanges();
//        log.info( "Imported and committed: K_BEWERTBGF00 as Bewertung-> " + count );

//        table = db.getTable( "K_BEWERTBGF00" );
//        monitor.beginTask( "Tabelle: " + table.getName(), table.getRowCount() );
        AnnotatedCompositeImporter bewertungGebaeudeImporter = new AnnotatedCompositeImporter(
                NHK2000BewertungGebaeudeComposite.class, table );
//        count = 0;
        while ((builderRow = table.getNextRow()) != null) {
            // alle Bewertungen importieren

            Integer lfd = (Integer)builderRow.get( "LFDNR" );
            if (0 != lfd) {
                // Bewertung gefunden erstellen
                NHK2000BewertungGebaeudeComposite bewertungGebaeude = repo.newEntity(
                        NHK2000BewertungGebaeudeComposite.class, null );
                Double eingangsnummer = (Double)builderRow.get( "EINGANGSNR" );
                if (eingangsnummer != null) {
                    NHK2000BewertungComposite nhk2000BewertungComposite = allBewertungen.get( eingangsnummer );
                    if (nhk2000BewertungComposite == null) {
                        throw new IllegalStateException( "no bewertung found for " + eingangsnummer );
                    }
                    bewertungGebaeude.bewertung().set( nhk2000BewertungComposite );
                }
                else {
                    throw new IllegalStateException( "no EINGANGSNR found" );
                }
                bewertungGebaeudeImporter.fillEntity( bewertungGebaeude, builderRow );
                bewertungGebaeude.VERKJAHR().set( asDouble( (Integer)builderRow.get( "VERKJAHR" ) ) );
                bewertungGebaeude.BERBAUJ1().set( asDouble( (Integer)builderRow.get( "BERBAUJ1" ) ) );
                bewertungGebaeude.BAUJ1().set( asDouble( (Integer)builderRow.get( "BAUJ1" ) ) );

                bewertungGebaeude.GND1().set( asDouble( (Integer)builderRow.get( "GND1" ) ) );
                bewertungGebaeude.RND1().set( asDouble( (Integer)builderRow.get( "RND1" ) ) );
                bewertungGebaeude.ALTER1().set( asDouble( (Integer)builderRow.get( "ALTER1" ) ) );
                bewertungGebaeude.WERTMIN1().set( asDouble( (Integer)builderRow.get( "WERTMIN1" ) ) );
                bewertungGebaeude.ABSCHLBM1().set( asDouble( (Integer)builderRow.get( "ABSCHLBM1" ) ) );
                bewertungGebaeude.ZUABSCHL1().set( asDouble( (Integer)builderRow.get( "ZUABSCHL1" ) ) );

                String rossV = (String)builderRow.get( "ROSS1" );
                String ross = "-";
                if ("L".equals( rossV )) {
                    ross = "linear";
                }
                else if ("R".equals( rossV )) {
                    ross = "Ross";
                }
                bewertungGebaeude.ROSS1().set( ross );

                bewertungGebaeude.WOHNGEB().set( getBooleanValue( builderRow, "WOHNGEB" ) );
                bewertungGebaeude.ABSCHLBM_BETRAG_EING().set( getBooleanValue( builderRow, "ABSCHLBM_BETRAG_EING" ) );
                bewertungGebaeude.ABSCHLSO_BETRAG_EING().set( getBooleanValue( builderRow, "ABSCHLSO_BETRAG_EING" ) );

                Double neuwert = bewertungGebaeude.NEUWERT1().get();
                Double zeitwert = bewertungGebaeude.ZEITWERT1().get();
                if (neuwert != null && zeitwert != null) {
                    bewertungGebaeude.ALTERSWERTMINDERUNG().set( neuwert - zeitwert );
                }
                if ((++count % 200) == 0) {
                    monitor.worked( 200 );
                    monitor.setTaskName( "Objekte: " + count );
                    repo.commitChanges();
                }
            }
            // andernfalls ignorieren
        }
        // wmvaopfW.flush();
        // wmvaopfW.close();
        repo.commitChanges();
        log.info( "Imported and committed: K_BEWERTBGF00 as Bewertung + Gebaeude -> " + count );
        monitor.done();
    }
}
