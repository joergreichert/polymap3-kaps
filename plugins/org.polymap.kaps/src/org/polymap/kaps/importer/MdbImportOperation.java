package org.polymap.kaps.importer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.entity.EntityComposite;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;

import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.qi4j.QiModule.EntityCreator;
import org.polymap.core.qi4j.event.AbstractModelChangeOperation;
import org.polymap.core.runtime.SubMonitor;

import org.polymap.kaps.model.GebaeudeArtComposite;
import org.polymap.kaps.model.GemeindeComposite;
import org.polymap.kaps.model.KaeuferKreisComposite;
import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.KaufvertragComposite;
import org.polymap.kaps.model.NutzungComposite;
import org.polymap.kaps.model.StalaComposite;
import org.polymap.kaps.model.StrasseComposite;
import org.polymap.kaps.model.VertragsArtComposite;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class MdbImportOperation
        extends AbstractModelChangeOperation
        implements IUndoableOperation {

    private static Log     log = LogFactory.getLog( MdbImportOperation.class );

    private File           dbFile;

    private String[]       tableNames;

    private KapsRepository repo;


    public MdbImportOperation( File dbFile, String[] tableNames ) {
        super( "MS-Access-Daten importieren" );
        this.dbFile = dbFile;
        this.tableNames = tableNames;
        this.repo = KapsRepository.instance();
    }


    protected IStatus doExecute( IProgressMonitor monitor, IAdaptable info )
            throws Exception {
        monitor.beginTask( getLabel(), 120 );
        Database db = Database.open( dbFile );
        try {
            SubMonitor sub = null;

            // BiotopComposite
            sub = new SubMonitor( monitor, 10 );
            // importBiotopdaten( db.getTable( "Biotopdaten" ), sub );

            // collect all stalas during import
            final Map<String, StalaComposite> allStalas = new HashMap<String, StalaComposite>();
            final Map<String, KaeuferKreisComposite> allKKreise = new HashMap<String, KaeuferKreisComposite>();
            final Map<String, VertragsArtComposite> allVertragsarten = new HashMap<String, VertragsArtComposite>();

            sub = new SubMonitor( monitor, 10 );
            importEntity( db, sub, StalaComposite.class, "K_STALA",
                    new EntityCallback<StalaComposite>() {

                        @Override
                        public void fillEntity( StalaComposite entity,
                                Map<String, Object> builderRow ) {
                            // collecting
                            allStalas.put( entity.schl().get(), entity );
                        }
                    } );

            sub = new SubMonitor( monitor, 10 );
            importEntity( db, sub, VertragsArtComposite.class, "K_VERART",
                    new EntityCallback<VertragsArtComposite>() {

                        @Override
                        public void fillEntity( VertragsArtComposite entity,
                                Map<String, Object> builderRow ) {
                            // associate stala
                            // Rückfrage welche STALAS verknüpft werden sollen
                            // SCHL aus STALA ist nicht eindeutig
                            // TODO [KAPS] #15
                            // collecting
                            allVertragsarten.put( entity.schl().get(), entity );
                        }
                    } );

            sub = new SubMonitor( monitor, 10 );
            importEntity( db, sub, KaeuferKreisComposite.class, "K_KKREIS_1",
                    new EntityCallback<KaeuferKreisComposite>() {

                        @Override
                        public void fillEntity( KaeuferKreisComposite entity,
                                Map<String, Object> builderRow ) {
                            // associate stala erstellen
                            Object stalaSchl = builderRow.get( "STALA" );
                            if (stalaSchl != null) {
                                StalaComposite stala = allStalas.get( stalaSchl.toString() );
                                if (stala == null) {
                                    throw new IllegalStateException( "no stala found for schl '"
                                            + stalaSchl + "' in K_KREIS_1 '" + entity.schl() + "'!" );
                                }
                                entity.stala().set( stala );
                            }
                            // collecting
                            allKKreise.put( entity.schl().get(), entity );
                        }
                    } );
            sub = new SubMonitor( monitor, 10 );
            importEntity( db, sub, KaufvertragComposite.class, "K_BUCH",
                    new EntityCallback<KaufvertragComposite>() {

                        @Override
                        public void fillEntity( KaufvertragComposite entity,
                                Map<String, Object> builderRow ) {
                            // mapping eingangsNr
                            // find vertragsArt
                            Object vertragsArtSchl = builderRow.get( "VERTRAGART" );
                            if (vertragsArtSchl != null) {
                                VertragsArtComposite vertragsArt = allVertragsarten
                                        .get( vertragsArtSchl );
                                if (vertragsArt == null) {
                                    throw new IllegalStateException(
                                            "no VERTRAGART found for schl '" + vertragsArtSchl
                                                    + "' in K_BUCH '" + entity.eingangsNr() + "'!" );
                                }
                                entity.vertragsArt().set( vertragsArt );
                            }

                            // find KKreis für Käufer und Verkäufer
                            Object kkreisSchl = builderRow.get( "KKREIS" );
                            if (kkreisSchl != null) {
                                KaeuferKreisComposite kkreis = allKKreise.get( kkreisSchl );
                                if (kkreis == null) {
                                    throw new IllegalStateException( "no KKREIS found for schl '"
                                            + kkreisSchl + "' in K_BUCH '" + entity.eingangsNr()
                                            + "'!" );
                                }
                                entity.kaeuferKreis().set( kkreis );
                            }
                            // find VKREIS
                            Object vkreisSchl = builderRow.get( "VKREIS" );
                            if (vkreisSchl != null) {
                                KaeuferKreisComposite vkreis = allKKreise.get( vkreisSchl );
                                if (vkreis == null) {
                                    throw new IllegalStateException( "no VKREIS found for schl '"
                                            + vkreisSchl + "' in K_BUCH '" + entity.eingangsNr()
                                            + "'!" );
                                }
                                entity.verkaeuferKreis().set( vkreis );
                            }
                            // fix imports
                            if (builderRow.get( "KANTZ" ) == null) {
                                entity.kaufpreisAnteilZaehler().set( 1 );
                            }
                            if (builderRow.get( "KANTN" ) == null) {
                                entity.kaufpreisAnteilNenner().set( 1 );
                            }
                            // BEM1 und BEM2 zusammenfassen
                            String bem1 = (String)builderRow.get( "BEM1" );
                            String bem2 = (String)builderRow.get( "BEM2" );
                            StringBuilder bem = new StringBuilder();
                            if (bem1 != null) {
                                bem.append( bem1 );
                                if (bem2 != null) {
                                    bem.append( Character.LINE_SEPARATOR );
                                }
                            }
                            if (bem2 != null) {
                                bem.append( bem2 );
                            }
                            entity.bemerkungen().set( bem.toString() );

                            // ANFR1 und ANFR2 zusammenfassen
                            String anfr1 = (String)builderRow.get( "ANFR1" );
                            String anfr2 = (String)builderRow.get( "ANFR2" );
                            StringBuilder anfr = new StringBuilder();
                            if (anfr1 != null) {
                                anfr.append( anfr1 );
                                if (anfr2 != null) {
                                    anfr.append( Character.LINE_SEPARATOR );
                                }
                            }
                            if (anfr2 != null) {
                                anfr.append( anfr2 );
                            }
                            entity.anfragen().set( anfr.toString() );
                            
                            // find also Verkaufsverträge alt und geplittete
                            // Verträge
                            // TODO die werden aber eventuell erst später
                            // assoziiert
                        }
                    } );
            sub = new SubMonitor( monitor, 10 );
            importEntity( db, sub, NutzungComposite.class, "K_NUTZ",
                    new EntityCallback<NutzungComposite>() {

                        @Override
                        public void fillEntity( NutzungComposite entity,
                                Map<String, Object> builderRow ) {
                            // associate stala erstellen
                            Object stalaSchl = builderRow.get( "STALA" );
                            if (stalaSchl != null) {
                                StalaComposite stala = allStalas.get( stalaSchl.toString() );
                                if (stala == null) {
                                    throw new IllegalStateException( "no stala found for schl '"
                                            + stalaSchl + "' in K_KREIS_1 '" + entity.schl() + "'!" );
                                }
                                entity.stala().set( stala );
                            }
                        }
                    } );
            sub = new SubMonitor( monitor, 10 );
            importEntity( db, sub, GebaeudeArtComposite.class, "K_GEBART",null);
            sub = new SubMonitor( monitor, 10 );
            
            final Map<String, GemeindeComposite> allGemeinde = new HashMap<String, GemeindeComposite>();
            importEntity( db, sub, GemeindeComposite.class, "K_GEMEINDE",
                    new EntityCallback<GemeindeComposite>() {

                @Override
                public void fillEntity( GemeindeComposite entity,
                        Map<String, Object> builderRow ) {
                    // associate stala erstellen
                    allGemeinde.put(entity.schl().get(), entity);
                }
            });
            sub = new SubMonitor( monitor, 10 );
            importEntity( db, sub, StrasseComposite.class, "K_STRASS",
                    new EntityCallback<StrasseComposite>() {

                        @Override
                        public void fillEntity( StrasseComposite entity,
                                Map<String, Object> builderRow ) {
                            // associate stala erstellen
                            Object gemSchl = builderRow.get( "GEMEINDE" );
                            if (gemSchl != null) {
                                GemeindeComposite gemeinde = allGemeinde.get( gemSchl.toString() );
                                if (gemeinde == null) {
                                    throw new IllegalStateException( "no stala found for schl '"
                                            + gemSchl + "' in K_KREIS_1 '" + entity.schl() + "'!" );
                                }
                                entity.gemeinde().set( gemeinde );
                            }
                        }
                    } );
            //
            // // Moose/Flechten/Pilze
            // sub = new SubMonitor( monitor, 10 );
            // importEntity( db, sub, PilzArtComposite.class, "Nr_Art", null );
            //
            // sub = new SubMonitor( monitor, 10 );
            // importValue( db, sub, PilzValue.class,
            // new ValueCallback<PilzValue>() {
            // public void fillValue( BiotopComposite biotop, PilzValue value )
            // {
            // Collection<PilzValue> coll = biotop.pilze().get();
            // coll.add( value );
            // biotop.pilze().set( coll );
            // }
            // });
            //
            // // Tiere
            // sub = new SubMonitor( monitor, 10 );
            // importEntity( db, sub, TierArtComposite.class, "Nr_Tier", null );
            //
            // sub = new SubMonitor( monitor, 10 );
            // importValue( db, sub, TierValue.class,
            // new ValueCallback<TierValue>() {
            // public void fillValue( BiotopComposite biotop, TierValue value )
            // {
            // Collection<TierValue> coll = biotop.tiere().get();
            // coll.add( value );
            // biotop.tiere().set( coll );
            // }
            // });
            //
            // // Gef�hrdungen/Beeintr�chtigungen
            // sub = new SubMonitor( monitor, 10 );
            // importEntity( db, sub, StoerungsArtComposite.class,
            // "Nr_Beeintr�chtigung", null );
            //
            // sub = new SubMonitor( monitor, 10 );
            // importValue( db, sub, StoerungValue.class, new
            // ValueCallback<StoerungValue>() {
            // public void fillValue( BiotopComposite biotop, StoerungValue
            // value ) {
            // Collection<StoerungValue> coll = biotop.stoerungen().get();
            // coll.add( value );
            // biotop.stoerungen().set( coll );
            // }
            // });
            //
            // // Wert(bestimmend)
            // sub = new SubMonitor( monitor, 10 );
            // importEntity( db, sub, WertArtComposite.class,
            // "Nr_Wertbestimmend", null );
            //
            // sub = new SubMonitor( monitor, 10 );
            // importValue( db, sub, WertValue.class, new
            // ValueCallback<WertValue>() {
            // public void fillValue( BiotopComposite biotop, WertValue value )
            // {
            // Collection<WertValue> coll = biotop.werterhaltend().get();
            // coll.add( value );
            // biotop.werterhaltend().set( coll );
            // }
            // });
            //
            // // Biotoptyp (als letztes damit Biotope vollst�ndig kopiert
            // werden)
            // sub = new SubMonitor( monitor, 10 );
            // importEntity( db, sub, BiotoptypArtComposite.class,
            // "Nr_Biotoptyp", null );
            //
            // sub = new SubMonitor( monitor, 10 );
            // final AtomicInteger copied = new AtomicInteger( 0 );
            // importValue( db, sub, BiotoptypValue.class,
            // new ValueCallback<BiotoptypValue>() {
            // public void fillValue( final BiotopComposite biotop, final
            // BiotoptypValue value ) {
            // String unr = value.unternummer().get();
            // assert unr != null : "Value-Unternummer == null";
            // String bunr = biotop.unr().get();
            // assert bunr != null : "Biotop-Unternummer == null";
            //
            // if (unr.equals( bunr )) {
            // // set nummer
            // if (biotop.biotoptypArtNr().get() == null) {
            // biotop.biotoptypArtNr().set( value.biotoptypArtNr().get() );
            // biotop.pflegeRueckstand().set( value.pflegerueckstand().get() );
            // }
            // // biotop exists -> copy biotop
            // else {
            // try {
            // BiotopComposite copy = repo.newBiotop( new
            // EntityCreator<BiotopComposite>() {
            // public void create( BiotopComposite prototype ) throws Exception
            // {
            // prototype.copyStateFrom( biotop );
            // prototype.objnr().set( repo.biotopnummern.get().generate() );
            //
            // prototype.biotoptypArtNr().set( value.biotoptypArtNr().get() );
            // prototype.pflegeRueckstand().set( value.pflegerueckstand().get()
            // );
            // }
            // });
            // copied.incrementAndGet();
            // }
            // catch (Exception e) {
            // throw new RuntimeException( e );
            // }
            // }
            // }
            // }
            // });
            // log.info( "Copies of BiotopComposite: " + copied.intValue() );
        }
        finally {
            db.close();
            db = null;
        }

        return Status.OK_STATUS;
    }


    //
    // protected void importBiotopdaten(Table table, IProgressMonitor monitor)
    // throws Exception {
    // monitor.beginTask("Tabelle: " + table.getName(), table.getRowCount());
    // //
    // // final AnnotatedCompositeImporter importer = new
    // // AnnotatedCompositeImporter(
    // // BiotopComposite.class, table );
    // //
    // // // data rows
    // // Map<String,Object> row = null;
    // // while ((row = table.getNextRow()) != null) {
    // // for (BiotopComposite biotop : findBiotop( row )) {
    // // if (biotop != null) {
    // // importer.fillEntity( biotop, row );
    // //
    // // // Erfassung
    // // Date erfasst = (Date)row.get( "Erfassung" );
    // // if (erfasst != null) {
    // // ValueBuilder<AktivitaetValue> builder = repo.newValueBuilder(
    // // AktivitaetValue.class );
    // // AktivitaetValue prototype = builder.prototype();
    // // prototype.wann().set( erfasst );
    // // prototype.wer().set( "SBK" );
    // // prototype.bemerkung().set( "Import aus SBK" );
    // // biotop.erfassung().set( builder.newInstance() );
    // // }
    // //
    // // // Eingabe -> Bearbeitung
    // // Date eingabe = (Date)row.get( "Eingabe" );
    // // if (eingabe != null) {
    // // ValueBuilder<AktivitaetValue> builder = repo.newValueBuilder(
    // // AktivitaetValue.class );
    // // AktivitaetValue prototype = builder.prototype();
    // // prototype.wann().set( eingabe );
    // // prototype.wer().set( "SBK" );
    // // prototype.bemerkung().set( "Import aus SBK" );
    // // biotop.bearbeitung().set( builder.newInstance() );
    // // }
    // // // Bekanntmachung
    // // ValueBuilder<AktivitaetValue> builder = repo.newValueBuilder(
    // // AktivitaetValue.class );
    // // AktivitaetValue prototype = builder.prototype();
    // // prototype.wann().set( eingabe );
    // // prototype.wer().set( "SBK" );
    // // prototype.bemerkung().set( "Import aus SBK" );
    // // biotop.bekanntmachung().set( builder.newInstance() );
    // // }
    // // else {
    // // //log.warn( "No Biotop found for: " + row );
    // // }
    // // }
    // // if (monitor.isCanceled()) {
    // // throw new RuntimeException( "Operation canceled." );
    // // }
    // // monitor.worked( 1 );
    // // }
    // }

    /*
     * 
     */
    interface EntityCallback<T extends EntityComposite> {

        void fillEntity( T prototype, Map<String, Object> builderRow );
    }


    protected <T extends EntityComposite> void importEntity( Database db, IProgressMonitor monitor,
            Class<T> type, String idColumn, final EntityCallback<T> callback )
            throws Exception {
        Table table = table( db, type );
        monitor.beginTask( "Tabelle: " + table.getName(), table.getRowCount() );

        final AnnotatedCompositeImporter importer = new AnnotatedCompositeImporter( type, table );

        // data rows
        Map<String, Object> row = null;
        int count = 0;
        while ((row = table.getNextRow()) != null) {
            if (idColumn != null) {
                Object id = row.get( idColumn );
            }
            final Map<String, Object> builderRow = row;

            // repo.newKaufvertrag(new EntityCreator<KaufvertragComposite>() {
            // public void create(KaufvertragComposite prototype)
            // throws Exception {
            // importer.fillEntity(prototype, builderRow);
            // if (callback != null) {
            // callback.fillEntity((T) prototype, builderRow);
            // }
            //
            // }
            // });
            repo.newEntity( type, null, new EntityCreator<T>() {

                public void create( T prototype )
                        throws Exception {
                    importer.fillEntity( prototype, builderRow );
                    if (callback != null) {
                        callback.fillEntity( (T)prototype, builderRow );
                    }
                }
            } );
            if (monitor.isCanceled()) {
                throw new RuntimeException( "Operation canceled." );
            }
            if ((++count % 500) == 0) {
                monitor.worked( 500 );
                monitor.setTaskName( "Objekte: " + count );
            }
        }
        log.info( "Imported: " + type + " -> " + count );
        monitor.done();
    }


    //
    // /*
    // *
    // */
    // interface ValueCallback<T extends ValueComposite> {
    // void fillValue( BiotopComposite entity, T value );
    // }

    //
    // protected <T extends ValueComposite> void importValue(
    // Database db, IProgressMonitor monitor, Class<T> type,
    // ValueCallback<T> callback )
    // throws Exception {
    // Table table = table( db, type );
    // monitor.beginTask( "Tabelle: " + table.getName(), table.getRowCount() );
    //
    // AnnotatedCompositeImporter importer = new AnnotatedCompositeImporter(
    // type, table );
    //
    // // data rows
    // Map<String, Object> row = null;
    // int count = 0;
    // while ((row = table.getNextRow()) != null) {
    // // build value
    // ValueBuilder<T> builder = BiotopRepository.instance().newValueBuilder(
    // type );
    // importer.fillEntity( builder.prototype(), row );
    // T instance = builder.newInstance();
    //
    // // callback for all biotops
    // for (BiotopComposite biotop : findBiotop( row )) {
    // if (biotop == null) {
    // //log.warn( "    No Biotop found for: " + row );
    // continue;
    // }
    // if (callback != null) {
    // callback.fillValue( biotop, instance );
    // }
    // }
    //
    // if (monitor.isCanceled()) {
    // throw new RuntimeException( "Operation canceled." );
    // }
    // if ((++count % 500) == 0) {
    // monitor.worked( 500 );
    // monitor.setTaskName( "Objekte: " + count );
    // }
    // }
    // log.info( "Imported: " + type + " -> " + count );
    // monitor.done();
    // }

    public static void printSchema( Table table ) {
        log.info( "Table: " + table.getName() );
        for (Column col : table.getColumns()) {
            log.info( "    column: " + col.getName() + " - " + col.getType() );
        }
    }


    private Table table( Database db, Class type )
            throws IOException {
        ImportTable a = (ImportTable)type.getAnnotation( ImportTable.class );
        assert a != null;
        return db.getTable( a.value() );
    }


    //
    // private Iterable<BiotopComposite> findBiotop( Map<String, Object> row ) {
    // BiotopComposite template = QueryExpressions.templateFor(
    // BiotopComposite.class );
    //
    // String objnr_sbk = row.get( "Objektnummer" ).toString();
    // String tk25 = row.get( "TK25" ).toString();
    //
    // Query<BiotopComposite> matches = repo.findEntities(
    // BiotopComposite.class,
    // QueryExpressions.and(
    // QueryExpressions.eq( template.objnr_sbk(), objnr_sbk ),
    // QueryExpressions.eq( template.tk25(), tk25 ) ),
    // 0, 100 );
    // return matches;
    //
    // // Iterator<BiotopComposite> it = matches.iterator();
    // // BiotopComposite result = it.hasNext() ? it.next() : null;
    // // return result;
    // }
    //

    private Object columnValue( Table table, Map<String, Object> row, String col ) {
        if (table.getColumn( col ) == null) {
            throw new IllegalArgumentException( "No such column: " + col );
        }
        return row.get( col );
    }

}
