package org.polymap.kaps.importer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.model.CompletionException;
import org.polymap.core.qi4j.QiModule.EntityCreator;
import org.polymap.core.runtime.entity.ConcurrentModificationException;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.data.BauweiseComposite;
import org.polymap.kaps.model.data.BodenRichtwertKennungComposite;
import org.polymap.kaps.model.data.BodenRichtwertRichtlinieArtDerNutzungComposite;
import org.polymap.kaps.model.data.BodenRichtwertRichtlinieErgaenzungComposite;
import org.polymap.kaps.model.data.BodennutzungComposite;
import org.polymap.kaps.model.data.EntwicklungsZusatzComposite;
import org.polymap.kaps.model.data.EntwicklungsZustandComposite;
import org.polymap.kaps.model.data.FlurstueckComposite;
import org.polymap.kaps.model.data.GebaeudeComposite;
import org.polymap.kaps.model.data.GemeindeComposite;
import org.polymap.kaps.model.data.NutzungComposite;
import org.polymap.kaps.model.data.RichtwertZoneLageComposite;
import org.polymap.kaps.model.data.RichtwertzoneComposite;
import org.polymap.kaps.model.data.RichtwertzoneZeitraumComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.model.data.VertragsdatenAgrarComposite;
import org.polymap.kaps.model.data.VertragsdatenBaulandComposite;
import org.polymap.kaps.ui.form.EingangsNummerFormatter;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class MdbFindDuplicateRWZOperation
        extends AbstractMdbImportOperation {

    private static Log log = LogFactory.getLog( MdbFindDuplicateRWZOperation.class );


    public MdbFindDuplicateRWZOperation( File dbFile, String[] tableNames ) {
        super( dbFile, tableNames, "Kaufpreissammlung Bewertungen importieren" );
    }


    protected IStatus doExecute0( IProgressMonitor monitor, IAdaptable info )
            throws Exception {
        File parentFolder = new File( "kaps" );
        parentFolder.mkdirs();

        monitor.beginTask( getLabel(), 3300 );
        final Database db = Database.open( dbFile );
        try {
            Table table = db.getTable( "K_RIWE" );
            monitor.beginTask( "Tabelle: " + table.getName(), table.getRowCount() );

            Map<String, String> foundRWZ = new TreeMap<String, String>();
            Set<String> duplicateRWZ = new LinkedHashSet<String>();

            // data rows
            Map<String, Object> row = null;
            while ((row = table.getNextRow()) != null) {
                final Map<String, Object> builderRow = row;

                String zone = (String)builderRow.get( "RIZONE" );
                String currentGemeinde = "" + (Integer)builderRow.get( "GEMEINDE" );
                if (foundRWZ.get( zone ) != null && !foundRWZ.get( zone ).equals( currentGemeinde )) {
                    duplicateRWZ.add( zone + ";" + currentGemeinde );
                    duplicateRWZ.add( zone + ";" + foundRWZ.get( zone ) );
                }
                else {
                    foundRWZ.put( zone, currentGemeinde );
                }

                if (monitor.isCanceled()) {
                    throw new RuntimeException( "Operation canceled." );
                }
            }
            RichtwertzoneComposite template = QueryExpressions.templateFor( RichtwertzoneComposite.class );

            Map<String, String> notImported = new TreeMap<String, String>();
            int count = 0;
            for (String composite : duplicateRWZ) {
                count++;
                String zone = composite.split( ";" )[0];
                String gemeinde = composite.split( ";" )[1];

                BooleanExpression expr = QueryExpressions.eq( template.schl(), zone );
                boolean found = false;
                int cound = 0;
                Query<RichtwertzoneComposite> entities = KapsRepository.instance().findEntities( RichtwertzoneComposite.class,
                        expr, 0, -1 );
                for (RichtwertzoneComposite rwz : entities) {
                    if (rwz != null && gemeinde.equals( rwz.gemeinde().get().schl().get() )) {
                        found = true;
                    }
                    count++;
                }
                if (count > 1) {
                    log.info( count + ". Zone " + composite + " existiert schon mehrfach" );                    
                }
                if (found) {
                    log.info( count + ". doppelte Zone " + composite + " wurde importiert" );
                }
                else {
                    log.info( count + ". doppelte Zone " + composite + " wurde nicht importiert" );
                    notImported.put( zone, gemeinde );
                }
            }

            // alle richtwertzonen aus notimported nachimportieren
            Map<String, RichtwertzoneComposite> vergesseneZonen = importiereVergesseneZonen( db, monitor, notImported );

            // alle doppelten richtwertzonezeitraum aus DB löschen
            bewegeZeitraeumeUm( db, monitor, notImported, vergesseneZonen );

            // alle geladenen Zonen merken
            final Map<String, List<RichtwertzoneZeitraumComposite>> allRichtwertZoneGueltigkeit = ladeAlleZonen();

            // alle richtwertzonen neu zu Flurstücken an Wohnungen zuweisen
            korrigiereWohnungsFlurstuecke( db, monitor, allRichtwertZoneGueltigkeit, duplicateRWZ );

            // alle richtwertzonen neu zu Vertragsdatenbauland zuweisen
            // alle richtwertzonen neu zu Flurstücken zuweisen
            korrigiereVertragsdatenBauland( db, monitor, allRichtwertZoneGueltigkeit, duplicateRWZ );

            // alle richtwertzonen neu zu Vertragsdatenagrarland zuweisen
            korrigiereVertragsdatenAgrarland( db, monitor, allRichtwertZoneGueltigkeit, duplicateRWZ );

            // alle richtwertzonen neu zu Flurstücken zuweisen
            korrigiereFlurstuecke( db, monitor, allRichtwertZoneGueltigkeit, duplicateRWZ );

            KapsRepository.instance().commitChanges();
            monitor.done();
        }
        finally {
            db.close();
        }

        return Status.OK_STATUS;
    }


    private Map<String, List<RichtwertzoneZeitraumComposite>> ladeAlleZonen() {
        Map<String, List<RichtwertzoneZeitraumComposite>> all = new HashMap<String, List<RichtwertzoneZeitraumComposite>>();
        KapsRepository repo = KapsRepository.instance();
        for (GemeindeComposite gemeinde : repo.findEntities( GemeindeComposite.class, null, 0, -1 )) {
            List<RichtwertzoneZeitraumComposite> list = new ArrayList<RichtwertzoneZeitraumComposite>();
            all.put( gemeinde.schl().get(), list );
            for (RichtwertzoneComposite zone : RichtwertzoneComposite.Mixin.findZoneIn( gemeinde )) {
                for (RichtwertzoneZeitraumComposite zeitraum : RichtwertzoneZeitraumComposite.Mixin.forZone( zone )) {
                    list.add( zeitraum );
                }
            }
        }
        return all;
    }


    private void korrigiereVertragsdatenBauland( Database db, IProgressMonitor monitor,
            Map<String, List<RichtwertzoneZeitraumComposite>> allRichtwertZoneGueltigkeit, Set<String> duplicateRWZ )
            throws IOException, CompletionException, ConcurrentModificationException {
        // lookup in der DB nach Flurstücken

        Table table = db.getTable( "K_BEVERW" );
        monitor.beginTask( "Tabelle: " + table.getName(), table.getRowCount() );
        File parentFolder = new File( "kaps" );
        parentFolder.mkdirs();
        File importfehler = new File( parentFolder, "vertragsdaten_bauland_korrigiert.txt" );
        final BufferedWriter w = new BufferedWriter( new FileWriter( importfehler ) );
        // data rows
        Map<String, Object> row = null;
        long count = 0;
        while ((row = table.getNextRow()) != null) {
            final Map<String, Object> builderRow = row;

            String zone = (String)builderRow.get( "RIZONE" );
            String gemeinde = "" + (Integer)builderRow.get( "GEMEINDE" );

            // nur die potenziell kaputten nehmen und nicht alle
            if (duplicateRWZ.contains( zone + ";" + gemeinde )) {
                Double eingangsnummer = (Double)builderRow.get( "EINGANGSNR" );
                Date jahr = (Date)builderRow.get( "RIJAHR" );
                count++;
                log.info( count + ". korrigiere VertragsdatenBauland "
                        + EingangsNummerFormatter.format( eingangsnummer.intValue() ) );

                // lade flurstück und weise zu
                VertragsdatenBaulandComposite bauland = VertragsdatenBaulandComposite.Mixin
                        .forVertrag( findVertrag( eingangsnummer ) );
                if (bauland == null) {
                    log.error( "Bauland for " + EingangsNummerFormatter.format( eingangsnummer.intValue() )
                            + " wurde nicht geladen" );
                }
                else {
                    RichtwertzoneZeitraumComposite found = findRichtwertZone( w, allRichtwertZoneGueltigkeit, zone,
                            gemeinde, jahr, VertragsdatenBaulandComposite.class,
                            EingangsNummerFormatter.format( eingangsnummer.intValue() ) );
                    bauland.richtwertZone().set( found.zone().get() );
                    bauland.richtwertZoneG().set( found );
                }

                if (monitor.isCanceled()) {
                    throw new RuntimeException( "Operation canceled." );
                }
                if (count % 100 == 0) {
                    KapsRepository.instance().commitChanges();
                }
            }
        }
        KapsRepository.instance().commitChanges();
    }


    private void korrigiereVertragsdatenAgrarland( Database db, IProgressMonitor monitor,
            Map<String, List<RichtwertzoneZeitraumComposite>> allRichtwertZoneGueltigkeit, Set<String> duplicateRWZ )
            throws IOException, CompletionException, ConcurrentModificationException {
        // lookup in der DB nach Flurstücken

        Table table = db.getTable( "K_BEVERL" );
        monitor.beginTask( "Tabelle: " + table.getName(), table.getRowCount() );
        File parentFolder = new File( "kaps" );
        parentFolder.mkdirs();
        File importfehler = new File( parentFolder, "vertragsdaten_agrar_korrigiert.txt" );
        final BufferedWriter w = new BufferedWriter( new FileWriter( importfehler ) );
        // data rows
        Map<String, Object> row = null;
        long count = 0;
        while ((row = table.getNextRow()) != null) {
            final Map<String, Object> builderRow = row;

            for (int i = 1; i <= 6; i++) {
                String zone = (String)builderRow.get( "RIZO" + i );
                String gemeinde = "" + (Integer)builderRow.get( "RIZOGEM" + i );

                // nur die potenziell kaputten nehmen und nicht alle
                if (duplicateRWZ.contains( zone + ";" + gemeinde )) {
                    Double eingangsnummer = (Double)builderRow.get( "EINGANGSNR" );
                    Date jahr = (Date)builderRow.get( "RIZOJAHR" + i );
                    count++;
                    log.info( count + ". korrigiere VertragsdatenAgrar "
                            + EingangsNummerFormatter.format( eingangsnummer.intValue() ) );

                    // lade flurstück und weise zu
                    VertragsdatenAgrarComposite agrar = VertragsdatenAgrarComposite.Mixin
                            .forVertrag( findVertrag( eingangsnummer ) );
                    if (agrar == null) {
                        log.error( "Agrar für " + EingangsNummerFormatter.format( eingangsnummer.intValue() )
                                + " wurde nicht geladen" );
                    }
                    else {
                        RichtwertzoneZeitraumComposite found = findRichtwertZone( w, allRichtwertZoneGueltigkeit, zone,
                                gemeinde, jahr, VertragsdatenAgrarComposite.class,
                                EingangsNummerFormatter.format( eingangsnummer.intValue() ) );
                        switch (i) {
                            case 1:
                                agrar.richtwertZone1().set( found );
                                break;
                            case 2:
                                agrar.richtwertZone2().set( found );
                                break;
                            case 3:
                                agrar.richtwertZone3().set( found );
                                break;
                            case 4:
                                agrar.richtwertZone4().set( found );
                                break;
                            case 5:
                                agrar.richtwertZone5().set( found );
                                break;
                            case 6:
                                agrar.richtwertZone6().set( found );
                                break;
                            default:
                                throw new IllegalStateException( "nur 6 erlaubt" );
                        }

                        if (count % 100 == 0) {
                            KapsRepository.instance().commitChanges();
                        }
                    }

                    if (monitor.isCanceled()) {
                        throw new RuntimeException( "Operation canceled." );
                    }
                }
            }
        }
        KapsRepository.instance().commitChanges();
    }


    private void korrigiereFlurstuecke( Database db, IProgressMonitor monitor,
            Map<String, List<RichtwertzoneZeitraumComposite>> allRichtwertZoneGueltigkeit, Set<String> duplicateRWZ )
            throws IOException, CompletionException, ConcurrentModificationException {
        // lookup in der DB nach Flurstücken

        Table table = db.getTable( "FLURZWI" );
        monitor.beginTask( "Tabelle: " + table.getName(), table.getRowCount() );
        File parentFolder = new File( "kaps" );
        parentFolder.mkdirs();
        File importfehler = new File( parentFolder, "flurstuecke_korrigiert.txt" );
        final BufferedWriter w = new BufferedWriter( new FileWriter( importfehler ) );
        // data rows
        Map<String, Object> row = null;
        long count = 0;
        while ((row = table.getNextRow()) != null) {
            final Map<String, Object> builderRow = row;

            String zone = (String)builderRow.get( "RIZONE" );
            String gemeinde = "" + (Integer)builderRow.get( "GEMEINDE" );

            // nur die potenziell kaputten nehmen und nicht alle
            if (duplicateRWZ.contains( zone + ";" + gemeinde )) {
                Double eingangsnummer = (Double)builderRow.get( "EINGANGSNR" );
                Date jahr = (Date)builderRow.get( "RIJAHR" );
                count++;
                log.info( count + ". korrigiere Flurstück "
                        + EingangsNummerFormatter.format( eingangsnummer.intValue() ) + ";"
                        + builderRow.get( "FLSTNR1" ) + ";" + builderRow.get( "FLSTNR1U" ) );

                RichtwertzoneZeitraumComposite found = findRichtwertZone( w, allRichtwertZoneGueltigkeit, zone,
                        gemeinde, jahr, FlurstueckComposite.class,
                        EingangsNummerFormatter.format( eingangsnummer.intValue() ) );
                // lade flurstück und weise zu
                boolean oneFound = false;
                for (FlurstueckComposite flurstueck : findFlurstuecke( eingangsnummer,
                        (Integer)builderRow.get( "FLSTNR1" ), (String)builderRow.get( "FLSTNR1U" ) )) {
                    flurstueck.richtwertZone().set( found.zone().get() );
                    if (oneFound) {
                        log.error( "DUPLICATE ENTRY FOR " + "Flurstück "
                                + EingangsNummerFormatter.format( eingangsnummer.intValue() ) + ";"
                                + builderRow.get( "FLSTNR1" ) + ";" + builderRow.get( "FLSTNR1U" ) );
                    }
                    oneFound = true;

                }
                if (!oneFound) {
                    log.error( "Flurstück " + EingangsNummerFormatter.format( eingangsnummer.intValue() ) + ";"
                            + builderRow.get( "FLSTNR1" ) + ";" + builderRow.get( "FLSTNR1U" ) + " wurde nicht geladen" );
                }

                if (monitor.isCanceled()) {
                    throw new RuntimeException( "Operation canceled." );
                }
                if (count % 100 == 0) {
                    KapsRepository.instance().commitChanges();
                }
            }
        }
        KapsRepository.instance().commitChanges();
    }


    private void korrigiereWohnungsFlurstuecke( Database db, IProgressMonitor monitor,
            Map<String, List<RichtwertzoneZeitraumComposite>> allRichtwertZoneGueltigkeit, Set<String> duplicateRWZ )
            throws IOException, CompletionException, ConcurrentModificationException {
        // lookup in der DB nach Flurstücken

        Table table = db.getTable( "K_EOBJF" );
        monitor.beginTask( "Tabelle: " + table.getName(), table.getRowCount() );
        File parentFolder = new File( "kaps" );
        parentFolder.mkdirs();
        File importfehler = new File( parentFolder, "flurstuecke_wohnung_korrigiert.txt" );
        final BufferedWriter w = new BufferedWriter( new FileWriter( importfehler ) );
        // data rows
        Map<String, Object> row = null;
        long count = 0;
        while ((row = table.getNextRow()) != null) {
            final Map<String, Object> builderRow = row;

            String zone = (String)builderRow.get( "RIZONE" );
            String gemeinde = "" + (Integer)builderRow.get( "GEMEINDE" );

            // nur die potenziell kaputten nehmen und nicht alle
            if (duplicateRWZ.contains( zone + ";" + gemeinde )) {
                Date jahr = (Date)builderRow.get( "RIJAHR" );
                count++;
                log.info( count + ". korrigiere Flurstück " + builderRow.get( "FLSTNR" ) + ";"
                        + builderRow.get( "FLSTNRU" ) );

                RichtwertzoneZeitraumComposite found = findRichtwertZone( w, allRichtwertZoneGueltigkeit, zone,
                        gemeinde, jahr, FlurstueckComposite.class,
                        builderRow.get( "OBJEKTNR" ) + "/" + builderRow.get( "GEBNR" ) );
                // lade flurstück und weise zu
                Integer objektNr = (Integer)builderRow.get( "OBJEKTNR" );
                Integer gebNr = (Integer)builderRow.get( "GEBNR" );
                GebaeudeComposite gebaeude = GebaeudeComposite.Mixin.forKeys( objektNr, gebNr );
                if (gebaeude == null) {
                    log.error( "Kein Gebäude gefunden für " + objektNr + "/" + gebNr + "\n" );
                }
                else {
                    // if (!gebaeude.flurstuecke().contains( flurstueck )) {
                    for (FlurstueckComposite flurstueck : gebaeude.flurstuecke().toList()) {
                        flurstueck.richtwertZone().set( found.zone().get() );
                    }
                    // }
                }

                if (monitor.isCanceled()) {
                    throw new RuntimeException( "Operation canceled." );
                }
                if (count % 100 == 0) {
                    KapsRepository.instance().commitChanges();
                }
            }
        }
        KapsRepository.instance().commitChanges();
    }


    private Query<FlurstueckComposite> findFlurstuecke( Double eingangsnummer, Integer nummer, String unternummer ) {
        BooleanExpression expr;
        VertragComposite vertrag = findVertrag( eingangsnummer );
        FlurstueckComposite flurTemplate = QueryExpressions.templateFor( FlurstueckComposite.class );
        expr = QueryExpressions.and( QueryExpressions.eq( flurTemplate.vertrag(), vertrag ),
                QueryExpressions.eq( flurTemplate.hauptNummer(), nummer ),
                QueryExpressions.eq( flurTemplate.unterNummer(), unternummer ) );
        return KapsRepository.instance().findEntities( FlurstueckComposite.class, expr, 0, -1 );
    }


    /**
     *
     * @param eingangsnummer
     * @return
     */
    private VertragComposite findVertrag( Double eingangsnummer ) {
        VertragComposite vertragTemplate = QueryExpressions.templateFor( VertragComposite.class );
        BooleanExpression expr = QueryExpressions.eq( vertragTemplate.eingangsNr(), eingangsnummer.intValue() );
        VertragComposite vertrag = KapsRepository.instance().findEntities( VertragComposite.class, expr, 0, 1 ).find();
        if (vertrag == null) {
            throw new IllegalStateException( "no vertrag found for " + eingangsnummer );
        }
        return vertrag;
    }


    private void bewegeZeitraeumeUm( Database db, IProgressMonitor monitor, Map<String, String> notImported,
            Map<String, RichtwertzoneComposite> vergesseneZonen )
            throws Exception {
        Table table = db.getTable( "K_RIWE" );
        monitor.beginTask( "Tabelle: " + table.getName(), table.getRowCount() );
        KapsRepository repository = KapsRepository.instance();
        RichtwertzoneZeitraumComposite template = QueryExpressions.templateFor( RichtwertzoneZeitraumComposite.class );
        // data rows
        int count = 0;
        Map<String, Object> row = null;
        Set<String> alwaysChecked = new HashSet<String>();
        while ((row = table.getNextRow()) != null) {
            final Map<String, Object> builderRow = row;

            String zoneStr = (String)builderRow.get( "RIZONE" );
            String gemeindeStr = "" + (Integer)builderRow.get( "GEMEINDE" );
            String bezStr = "" + (String)builderRow.get( "BEZ" );

            if (!alwaysChecked.contains( zoneStr + ";" + bezStr ) && notImported.get( zoneStr ) != null
                    && notImported.get( zoneStr ).equals( gemeindeStr )) {
                alwaysChecked.add( zoneStr + ";" + bezStr );
                // query nach den vergessenen

                BooleanExpression expr = QueryExpressions.and( QueryExpressions.eq( template.schl(), zoneStr ),
                        QueryExpressions.eq( template.name(), bezStr ) );
                for (RichtwertzoneZeitraumComposite rwz : repository.findEntities(
                        RichtwertzoneZeitraumComposite.class, expr, 0, -1 )) {
                    log.info( count + ". ändere Zeitraum " + zoneStr + " " + gemeindeStr + " " + bezStr );
                    RichtwertzoneComposite zone = vergesseneZonen.get( zoneStr + ";" + gemeindeStr );
                    if (zone == null) {
                        throw new IllegalStateException( "no zone found for " + zoneStr + ";" + gemeindeStr );
                    }
                    rwz.zone().set( zone );
                    // repository.forceRemoveEntity( rwz );
                    count++;
                    if (count % 100 == 0) {
                        KapsRepository.instance().commitChanges();
                    }
                }
            }
        }
        repository.commitChanges();
    }


    private Map<String, RichtwertzoneComposite> importiereVergesseneZonen( Database db, IProgressMonitor monitor,
            Map<String, String> notImported )
            throws Exception {
        Table table = db.getTable( "K_RIWE" );
        monitor.beginTask( "Tabelle: " + table.getName(), table.getRowCount() );

        // bodenrichtwertkennung laden
        final BodenRichtwertKennungComposite zonal = findSchlNamed( BodenRichtwertKennungComposite.class, "1" );
        // erschließungsbeitrag laden
        // RichtwertzoneLage auf 00
        final RichtwertZoneLageComposite richtwertZoneLageComposite = findSchlNamed( RichtwertZoneLageComposite.class,
                "00" );

        final Map<String, RichtwertzoneComposite> allRichtwertZone = new HashMap<String, RichtwertzoneComposite>();
        // final Map<String, List<RichtwertzoneZeitraumComposite>>
        // allRichtwertZoneGueltigkeit = new HashMap<String,
        // List<RichtwertzoneZeitraumComposite>>();
        final AnnotatedCompositeImporter richtwertzoneCompositeImporter = new AnnotatedCompositeImporter(
                RichtwertzoneComposite.class, table );
        final AnnotatedCompositeImporter richtwertzoneZeitraumCompositeImporter = new AnnotatedCompositeImporter(
                RichtwertzoneZeitraumComposite.class, table );

        // data rows
        Map<String, Object> row = null;
        while ((row = table.getNextRow()) != null) {
            final Map<String, Object> builderRow = row;

            String zoneStr = (String)builderRow.get( "RIZONE" );
            String gemeindeStr = "" + (Integer)builderRow.get( "GEMEINDE" );
            if (notImported.get( zoneStr ) != null && notImported.get( zoneStr ).equals( gemeindeStr )) {
                // import them
                // log.info( "Import zone " + zoneStr + ";" + gemeindeStr );
                // RichtwertzoneZeitraumComposite entity = repo.newEntity(
                // RichtwertzoneZeitraumComposite.class, null );
                //
                // richtwertzoneZeitraumCompositeImporter.fillEntity( entity,
                // builderRow );
                //
                // entity.erschliessungsBeitrag().set(
                // findSchlNamed( ErschliessungsBeitragComposite.class, builderRow,
                // "EB" ) );

                // subcreate Richtwertzone
                RichtwertzoneComposite zone = allRichtwertZone.get( zoneStr + ";" + gemeindeStr );
                if (zone == null) {
                    log.info( "Create zone " + zoneStr + ";" + gemeindeStr );
                    zone = repo.newEntity( RichtwertzoneComposite.class, null,
                            new EntityCreator<RichtwertzoneComposite>() {

                                public void create( RichtwertzoneComposite prototype )
                                        throws Exception {

                                    richtwertzoneCompositeImporter.fillEntity( prototype, builderRow );
                                    prototype.gemeinde().set(
                                            findSchlNamed( GemeindeComposite.class, builderRow, "GEMEINDE" ) );
                                    prototype.nutzung().set(
                                            findSchlNamed( NutzungComposite.class, builderRow, "NUART" ) );
                                    prototype.bodenNutzung().set(
                                            findSchlNamed( BodennutzungComposite.class, builderRow, "NUTZUNG" ) );
                                    prototype.lage().set( richtwertZoneLageComposite );

                                    String RIWEKENNUNG = (String)builderRow.get( "RIWEKENNUNG" );
                                    if (RIWEKENNUNG != null) {
                                        prototype.bodenrichtwertKennung()
                                                .set( findSchlNamed( BodenRichtwertKennungComposite.class,
                                                        RIWEKENNUNG.trim() ) );
                                    }
                                    else {
                                        prototype.bodenrichtwertKennung().set( zonal );
                                    }
                                    // entwicklungszustand importieren
                                    String ENTWZUSTAND = (String)builderRow.get( "ENTWZUSTAND" );
                                    if (ENTWZUSTAND != null) {
                                        prototype.entwicklungsZustand()
                                                .set( findSchlNamed( EntwicklungsZustandComposite.class,
                                                        ENTWZUSTAND.trim() ) );
                                    }
                                    // brwrl-art
                                    String NUTZUNG_ART = (String)builderRow.get( "NUTZUNG_ART" );
                                    if (NUTZUNG_ART != null) {
                                        prototype.brwrlArt().set(
                                                findSchlNamed( BodenRichtwertRichtlinieArtDerNutzungComposite.class,
                                                        NUTZUNG_ART.trim() ) );
                                    }
                                    // brwrl-ergänzung
                                    String NUTZUNG_ERGAENZ = (String)builderRow.get( "NUTZUNG_ERGAENZ" );
                                    if (NUTZUNG_ERGAENZ != null) {
                                        prototype.brwrlErgaenzung().set(
                                                findSchlNamed( BodenRichtwertRichtlinieErgaenzungComposite.class,
                                                        NUTZUNG_ERGAENZ.trim() ) );
                                    }
                                    // entwicklungszusatz
                                    String ENTWZUSATZ = (String)builderRow.get( "ENTWZUSATZ" );
                                    if (ENTWZUSATZ != null) {
                                        prototype.entwicklungsZusatz().set(
                                                findSchlNamed( EntwicklungsZusatzComposite.class, ENTWZUSATZ.trim() ) );
                                    }
                                    // bauweise
                                    String bauweise = (String)builderRow.get( "BAUWEISE" );
                                    if (bauweise != null) {
                                        prototype.bauweise().set(
                                                findSchlNamed( BauweiseComposite.class, bauweise.trim() ) );
                                    }
                                }
                            } );
                    allRichtwertZone.put( zoneStr + ";" + gemeindeStr, zone );

                }
                // zone.gueltigkeiten().add( entity );
                // entity.zone().set( zone );
            }
        }
        return allRichtwertZone;
    }
}
