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
import org.polymap.kaps.model.NHK2010GebaeudeartenProvider;
import org.polymap.kaps.model.data.NHK2010Anbauten;
import org.polymap.kaps.model.data.NHK2010Baupreisindex;
import org.polymap.kaps.model.data.NHK2010BewertungComposite;
import org.polymap.kaps.model.data.NHK2010BewertungGebaeudeComposite;
import org.polymap.kaps.model.data.VertragComposite;

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
            importEntity( db, monitor, NHK2010Anbauten.class, new EntityCallback<NHK2010Anbauten>() {

                @Override
                public void fillEntity( NHK2010Anbauten entity, Map<String, Object> builderRow ) {
                    // bewertung finden
                    entity.schl().set( String.valueOf( (Integer)builderRow.get( "SCHL" ) ) );
                }
            } );
            sub = new SubMonitor( monitor, 10 );
            importEntity( db, monitor, NHK2010Baupreisindex.class, new EntityCallback<NHK2010Baupreisindex>() {

                @Override
                public void fillEntity( NHK2010Baupreisindex entity, Map<String, Object> builderRow ) {
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
            importNHK2010Bewertung( db, monitor, parentFolder );
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
                            NHK2010Anbauten anbauTemplate = QueryExpressions.templateFor( NHK2010Anbauten.class );
                            BooleanExpression expr2 = QueryExpressions.eq( anbauTemplate.schl(), anbauteTrimmed );
                            NHK2010Anbauten anbau = KapsRepository.instance()
                                    .findEntities( NHK2010Anbauten.class, expr2, 0, 1 ).find();
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
        NHK2010GebaeudeartenProvider gebaeudeArtenProvider = NHK2010GebaeudeartenProvider.instance();
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
                Long gnd = bewertungGebaeude.gesamtNutzungsDauer().get();
                Long rnd = bewertungGebaeude.restNutzungsDauer().get();
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
}
