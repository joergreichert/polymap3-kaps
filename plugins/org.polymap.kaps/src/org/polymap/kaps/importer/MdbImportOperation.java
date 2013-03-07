package org.polymap.kaps.importer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.polymap.core.qi4j.QiModule.EntityCreator;
import org.polymap.core.qi4j.event.AbstractModelChangeOperation;
import org.polymap.core.runtime.SubMonitor;
import org.polymap.kaps.model.KaeuferKreisComposite;
import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.KaufvertragComposite;
import org.polymap.kaps.model.StalaComposite;
import org.polymap.kaps.model.VertragsArtComposite;
import org.qi4j.api.entity.EntityComposite;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class MdbImportOperation extends AbstractModelChangeOperation implements
		IUndoableOperation {

	private static Log log = LogFactory.getLog(MdbImportOperation.class);

	private File dbFile;

	private String[] tableNames;

	private KapsRepository repo;

	protected MdbImportOperation(File dbFile, String[] tableNames) {
		super("MS-Access-Daten importieren");
		this.dbFile = dbFile;
		this.tableNames = tableNames;
		this.repo = KapsRepository.instance();
	}

	protected IStatus doExecute(IProgressMonitor monitor, IAdaptable info)
			throws Exception {
		monitor.beginTask(getLabel(), 120);
		Database db = Database.open(dbFile);
		try {
			SubMonitor sub = null;

			// BiotopComposite
			sub = new SubMonitor(monitor, 10);
			// importBiotopdaten( db.getTable( "Biotopdaten" ), sub );

			// collect all stalas during import
			final Map<String, StalaComposite> allStalas = new HashMap<String, StalaComposite>();
			final Map<String, KaeuferKreisComposite> allKKreise = new HashMap<String, KaeuferKreisComposite>();
			final Map<String, VertragsArtComposite> allVertragsarten = new HashMap<String, VertragsArtComposite>();
			
			
			sub = new SubMonitor(monitor, 10);
			importEntity(db, sub, StalaComposite.class, "K_STALA",
					new EntityCallback<StalaComposite>() {

						@Override
						public void fillEntity(StalaComposite entity,
								Map<String, Object> builderRow) {
							allStalas.put(entity.schl().get(), entity);
						}
					});
			
			sub = new SubMonitor(monitor, 10);
			importEntity(db, sub, VertragsArtComposite.class, "K_VERART",
					new EntityCallback<VertragsArtComposite>() {

						@Override
						public void fillEntity(VertragsArtComposite entity,
								Map<String, Object> builderRow) {
							// associate stala
							// Rückfrage welche STALAS verknüpft werden sollen SCHL aus STALA ist nicht eindeutig
							// TODO [KAPS] #15
							// collecting
							allVertragsarten.put(entity.schl().get(), entity);
						}
					});

			sub = new SubMonitor(monitor, 10);
			importEntity(db, sub, KaeuferKreisComposite.class, "K_KKREIS_1",
					new EntityCallback<KaeuferKreisComposite>() {

						@Override
						public void fillEntity(KaeuferKreisComposite entity,
								Map<String, Object> builderRow) {
							// associate stala erstellen
							Object stalaSchl = builderRow
									.get("STALA");
							if (stalaSchl != null) {
								StalaComposite stala = allStalas.get(stalaSchl.toString());
								if (stala == null) {
									throw new IllegalStateException("no stala found for schl '" + stalaSchl + "' in K_KREIS_1 '" + entity.schl() + "'!" );
								}
								entity.stala().set(stala);
							}
							// collecting
							allKKreise.put(entity.schl().get(), entity);
						}
					});

			sub = new SubMonitor(monitor, 10);
			importEntity(db, sub, KaufvertragComposite.class, "K_BUCH",
					new EntityCallback<KaufvertragComposite>() {

						@Override
						public void fillEntity(KaufvertragComposite entity,
								Map<String, Object> builderRow) {
							// mapping eingangsNr
							// find vertragsArt
							Object vertragsArtSchl = builderRow.get("VERTRAGART"); 
							VertragsArtComposite vertragsArt = allVertragsarten.get(vertragsArtSchl);
							if (vertragsArt == null) {
								throw new IllegalStateException("no VERTRAGART found for schl '" + vertragsArtSchl + "' in K_BUCH '" + entity.eingangsNr() + "'!" );								
							}
							entity.vertragsArt().set(vertragsArt);
							
							// find KKreis für Käufer und Verkäufer
							Object kkreisSchl = builderRow.get("KKREIS"); 
							KaeuferKreisComposite kkreis = allKKreise.get(kkreisSchl);
							if (kkreis == null) {
								throw new IllegalStateException("no KKREIS found for schl '" + kkreisSchl + "' in K_BUCH '" + entity.eingangsNr() + "'!" );								
							}
							entity.kaeuferKreis().set(kkreis);
							
							// find VKREIS
							Object vkreisSchl = builderRow.get("VKREIS"); 
							KaeuferKreisComposite vkreis = allKKreise.get(vkreisSchl);
							if (vkreis == null) {
								throw new IllegalStateException("no VKREIS found for schl '" + vkreisSchl + "' in K_BUCH '" + entity.eingangsNr() + "'!" );								
							}
							entity.verkaeuferKreis().set(vkreis);
							
							// find also Verkaufsverträge alt und geplittete Verträge
							// TODO die werden aber eventuell erst später assoziiert
						}
					});
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
		} finally {
			db.close();
			db = null;
		}

		return Status.OK_STATUS;
	}

	protected void importBiotopdaten(Table table, IProgressMonitor monitor)
			throws Exception {
		monitor.beginTask("Tabelle: " + table.getName(), table.getRowCount());
		//
		// final AnnotatedCompositeImporter importer = new
		// AnnotatedCompositeImporter(
		// BiotopComposite.class, table );
		//
		// // data rows
		// Map<String,Object> row = null;
		// while ((row = table.getNextRow()) != null) {
		// for (BiotopComposite biotop : findBiotop( row )) {
		// if (biotop != null) {
		// importer.fillEntity( biotop, row );
		//
		// // Erfassung
		// Date erfasst = (Date)row.get( "Erfassung" );
		// if (erfasst != null) {
		// ValueBuilder<AktivitaetValue> builder = repo.newValueBuilder(
		// AktivitaetValue.class );
		// AktivitaetValue prototype = builder.prototype();
		// prototype.wann().set( erfasst );
		// prototype.wer().set( "SBK" );
		// prototype.bemerkung().set( "Import aus SBK" );
		// biotop.erfassung().set( builder.newInstance() );
		// }
		//
		// // Eingabe -> Bearbeitung
		// Date eingabe = (Date)row.get( "Eingabe" );
		// if (eingabe != null) {
		// ValueBuilder<AktivitaetValue> builder = repo.newValueBuilder(
		// AktivitaetValue.class );
		// AktivitaetValue prototype = builder.prototype();
		// prototype.wann().set( eingabe );
		// prototype.wer().set( "SBK" );
		// prototype.bemerkung().set( "Import aus SBK" );
		// biotop.bearbeitung().set( builder.newInstance() );
		// }
		// // Bekanntmachung
		// ValueBuilder<AktivitaetValue> builder = repo.newValueBuilder(
		// AktivitaetValue.class );
		// AktivitaetValue prototype = builder.prototype();
		// prototype.wann().set( eingabe );
		// prototype.wer().set( "SBK" );
		// prototype.bemerkung().set( "Import aus SBK" );
		// biotop.bekanntmachung().set( builder.newInstance() );
		// }
		// else {
		// //log.warn( "No Biotop found for: " + row );
		// }
		// }
		// if (monitor.isCanceled()) {
		// throw new RuntimeException( "Operation canceled." );
		// }
		// monitor.worked( 1 );
		// }
	}

	/*
     * 
     */
	interface EntityCallback<T extends EntityComposite> {
		void fillEntity(T prototype, Map<String, Object> builderRow);
	}

	protected <T extends EntityComposite> void importEntity(Database db,
			IProgressMonitor monitor, Class<T> type, String idColumn,
			final EntityCallback<T> callback) throws Exception {
		Table table = table(db, type);
		monitor.beginTask("Tabelle: " + table.getName(), table.getRowCount());

		final AnnotatedCompositeImporter importer = new AnnotatedCompositeImporter(
				type, table);

		// data rows
		Map<String, Object> row = null;
		int count = 0;
		while ((row = table.getNextRow()) != null) {
			if (idColumn != null) {
				Object id = row.get(idColumn);
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
			repo.newEntity(type, null, new EntityCreator<T>() {
				public void create(T prototype) throws Exception {
					importer.fillEntity(prototype, builderRow);
					if (callback != null) {
						callback.fillEntity((T) prototype, builderRow);
					}
				}
			});
			if (monitor.isCanceled()) {
				throw new RuntimeException("Operation canceled.");
			}
			if ((++count % 500) == 0) {
				monitor.worked(500);
				monitor.setTaskName("Objekte: " + count);
			}
		}
		log.info("Imported: " + type + " -> " + count);
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

	public static void printSchema(Table table) {
		log.info("Table: " + table.getName());
		for (Column col : table.getColumns()) {
			log.info("    column: " + col.getName() + " - " + col.getType());
		}
	}

	private Table table(Database db, Class type) throws IOException {
		ImportTable a = (ImportTable) type.getAnnotation(ImportTable.class);
		assert a != null;
		return db.getTable(a.value());
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

	private Object columnValue(Table table, Map<String, Object> row, String col) {
		if (table.getColumn(col) == null) {
			throw new IllegalArgumentException("No such column: " + col);
		}
		return row.get(col);
	}

}
