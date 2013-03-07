/* 
 * polymap.org
 * Copyright 2013 Polymap GmbH. All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.kaps.model;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.geotools.feature.NameImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.type.Name;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.polymap.core.catalog.model.CatalogRepository;
import org.polymap.core.model.CompletionException;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.qi4j.Qi4jPlugin;
import org.polymap.core.qi4j.Qi4jPlugin.Session;
import org.polymap.core.qi4j.QiModule;
import org.polymap.core.qi4j.QiModuleAssembler;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.entity.ConcurrentModificationException;
import org.polymap.kaps.model.constant.Verkaeuferkreis;
import org.polymap.rhei.data.entityfeature.DefaultEntityProvider;
import org.polymap.rhei.data.entityfeature.EntityProvider.FidsQueryProvider;
import org.polymap.rhei.data.entitystore.lucene.LuceneEntityStoreService;
import org.polymap.rhei.data.entitystore.lucene.LuceneQueryProvider;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class KapsRepository extends QiModule {

	private static Log log = LogFactory.getLog(KapsRepository.class);

	public static final String NAMESPACE = "http://polymap.org/kaps";

	/**
	 * Get or create the repository for the current user session.
	 */
	public static final KapsRepository instance() {
		return Qi4jPlugin.Session.instance().module(KapsRepository.class);
	}

	// instance *******************************************

	private OperationSaveListener operationListener = new OperationSaveListener();

	// private Map<String,VertragsArtComposite> btNamen;

	// private Map<String,VertragsArtComposite> btNummern;

	/** Allow direct access for operations. */
	protected KapsService kapsService;

	// public ServiceReference<BiotopnummerGeneratorService> biotopnummern;

	/**
     * 
     */
	public static class ArtEntityProvider extends DefaultEntityProvider {

		public ArtEntityProvider(QiModule repo, Class entityClass,
				Name entityName, FidsQueryProvider queryProvider) {
			super(repo, entityClass, entityName, queryProvider);
		}

		public ReferencedEnvelope getBounds() {
			return new ReferencedEnvelope(4000000, 5000000, 5000000, 6000000,
					getCoordinateReferenceSystem(null));
		}

		public CoordinateReferenceSystem getCoordinateReferenceSystem(
				String propName) {
			try {
				return CRS.decode("EPSG:31468");
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		public String getDefaultGeometry() {
			throw new RuntimeException("not yet implemented.");
		}
	};

	public KapsRepository(final QiModuleAssembler assembler) {
		super(assembler);
		log.debug("Initializing Kaps module...");

		// for the global instance of the module
		// (Qi4jPlugin.Session.globalInstance()) there
		// is no request context
		if (Polymap.getSessionDisplay() != null) {
			OperationSupport.instance().addOperationSaveListener(
					operationListener);
		}
		// biotopnummern = assembler.getModule().serviceFinder().findService(
		// BiotopnummerGeneratorService.class );
	}

	public void init(final Session session) {
		try {
			// build the queryProvider
			ServiceReference<LuceneEntityStoreService> storeService = assembler
					.getModule().serviceFinder()
					.findService(LuceneEntityStoreService.class);
			LuceneEntityStoreService luceneStore = storeService.get();
			FidsQueryProvider queryProvider = new LuceneQueryProvider(
					luceneStore.getStore());

			kapsService = new KapsService(
					// BiotopComposite
					new KaufvertragEntityProvider(this, queryProvider),
					new KaufvertragEntityProvider(this, queryProvider),
					// Arten...
					new ArtEntityProvider(
							this,
							VertragsArtComposite.class,
							new NameImpl(KapsRepository.NAMESPACE, "Biotoptyp"),
							queryProvider)
			// new ArtEntityProvider( this, PflanzenArtComposite.class,
			// new NameImpl( KapsRepository.NAMESPACE, "Pflanzenart" ),
			// queryProvider ),
			// new ArtEntityProvider( this, PilzArtComposite.class,
			// new NameImpl( KapsRepository.NAMESPACE, "Pilzart" ),
			// queryProvider ),
			// new ArtEntityProvider( this, TierArtComposite.class,
			// new NameImpl( KapsRepository.NAMESPACE, "Tierart" ),
			// queryProvider ),
			// new ArtEntityProvider( this, StoerungsArtComposite.class,
			// new NameImpl( KapsRepository.NAMESPACE, "Beeintr�chtigungen" ),
			// queryProvider ),
			// new ArtEntityProvider( this, WertArtComposite.class,
			// new NameImpl( KapsRepository.NAMESPACE, "Wertbestimmend" ),
			// queryProvider )
			);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		// register with catalog
		// if (Polymap.getSessionDisplay() != null) {
		// Polymap.getSessionDisplay().asyncExec( new Runnable() {
		// public void run() {
		CatalogRepository catalogRepo = session.module(CatalogRepository.class);
		catalogRepo.getCatalog().addTransient(kapsService);
		// CatalogPluginSession.instance().getLocalCatalog().add( biotopService
		// );
		// }
		// });
		// }
	}

	protected void done() {
		if (operationListener != null) {
			OperationSupport.instance().removeOperationSaveListener(
					operationListener);
			operationListener = null;
		}
		if (kapsService != null) {
			kapsService.dispose(new NullProgressMonitor());
		}
		// check ThreadLocal;
		// code from
		// http://blog.igorminar.com/2009/03/identifying-threadlocal-memory-leaks-in.html
		try {
			Thread thread = Thread.currentThread();

			Field threadLocalsField = Thread.class
					.getDeclaredField("threadLocals");
			threadLocalsField.setAccessible(true);

			Class threadLocalMapKlazz = Class
					.forName("java.lang.ThreadLocal$ThreadLocalMap");
			Field tableField = threadLocalMapKlazz.getDeclaredField("table");
			tableField.setAccessible(true);

			Object table = tableField.get(threadLocalsField.get(thread));

			int threadLocalCount = Array.getLength(table);
			StringBuilder sb = new StringBuilder();
			StringBuilder classSb = new StringBuilder();

			int leakCount = 0;

			for (int i = 0; i < threadLocalCount; i++) {
				Object entry = Array.get(table, i);
				if (entry != null) {
					Field valueField = entry.getClass().getDeclaredField(
							"value");
					valueField.setAccessible(true);
					Object value = valueField.get(entry);
					if (value != null) {
						classSb.append(value.getClass().getName()).append(", ");
					} else {
						classSb.append("null, ");
					}
					leakCount++;
				}
			}

			sb.append("possible ThreadLocal leaks: ").append(leakCount)
					.append(" of ").append(threadLocalCount).append(" = [")
					.append(classSb.substring(0, classSb.length() - 2))
					.append("] ");

			log.info(sb);
		} catch (Exception e) {
			log.warn("", e);
		}

		super.done();

		log.info("Running GC ...");
		Runtime.getRuntime().gc();
	}

	public <T> Query<T> findEntities(Class<T> compositeType,
			BooleanExpression expression, int firstResult, int maxResults) {
		// Lucene does not like Integer.MAX_VALUE!?
		maxResults = Math.min(maxResults, 1000000);

		return super.findEntities(compositeType, expression, firstResult,
				maxResults);
	}

	public void applyChanges() throws ConcurrentModificationException,
			CompletionException {
		try {
			// save changes
			uow.apply();
		} catch (ConcurrentEntityModificationException e) {
			throw new ConcurrentModificationException(e);
		} catch (UnitOfWorkCompletionException e) {
			throw new CompletionException(e);
		}
	}

	public KaufvertragComposite newKaufvertrag(
			final EntityCreator<KaufvertragComposite> creator) throws Exception {
		return newEntity(KaufvertragComposite.class, null,
				new EntityCreator<KaufvertragComposite>() {
					public void create(KaufvertragComposite prototype)
							throws Exception {
						// // objnr
						// prototype.objnr().set( biotopnummern.get().generate()
						// );
						// // status
						// prototype.status().set( Status.aktuell.id );
						//
						// // erfassung
						// ValueBuilder<AktivitaetValue> builder =
						// newValueBuilder( AktivitaetValue.class );
						// AktivitaetValue _prototype = builder.prototype();
						// _prototype.wann().set( new Date() );
						// _prototype.wer().set(
						// Polymap.instance().getUser().getName() );
						// _prototype.bemerkung().set( "" );
						// prototype.erfassung().set( builder.newInstance() );
						// // bearbeitung
						// prototype.bearbeitung().set( builder.newInstance() );
						//
						// // biotoptyp (zuf�llig)
						// String randomBt =
						// btNamen().values().iterator().next().nummer().get();
						// // prototype.biotoptypArtNr().set( randomBt );

						prototype.eingangsDatum().set(new Date());
						// eingangsnummer erst beim Speichern setzen!
						// prototype.eingangsNr().set(
						// System.currentTimeMillis() + "");
//						prototype.verkaeuferKreis().set(
//								Verkaeuferkreis.schlecht.id);

//						VertragsArtComposite art = newEntity(
//								VertragsArtComposite.class, null);
//						art.name().set(
//								"Vertragsart-" + System.currentTimeMillis());
//						art.nummer().set(
//								String.valueOf(System.currentTimeMillis()));
//						prototype.vertragArt().set(art);
//						prototype.vertragArten().add(art);

						if (creator != null) {
							creator.create(prototype);
						}
					}
				});
	}
	//
	//
	// public Map<String,VertragsArtComposite> btNamen() {
	// if (btNamen == null) {
	// Query<VertragsArtComposite> entities = findEntities(
	// VertragsArtComposite.class, null, 0, 1000 );
	//
	// btNamen = new HashMap();
	// for (VertragsArtComposite entity : entities) {
	// btNamen.put( entity.name().get(), entity );
	// }
	// }
	// return btNamen;
	// }
	//
	//
	// public VertragsArtComposite btForNummer( String nummer ) {
	// if (btNummern == null) {
	// Query<VertragsArtComposite> entities = findEntities(
	// VertragsArtComposite.class, null, 0, 1000 );
	//
	// btNummern = new HashMap();
	// for (VertragsArtComposite entity : btNamen().values()) {
	// btNummern.put( entity.nummer().get(), entity );
	// }
	// }
	// return btNummern.get( nummer );
	// }

	// public <V extends ValueComposite,A extends Entity,C extends
	// ValueArtComposite<V,A>>
	// C createValueArt(
	// Class<C> cl,
	// V value,
	// ValueArtFinder<V,A> artFinder ) {
	//
	// C result = assembler.getModule().transientBuilderFactory().newTransient(
	// cl );
	// result.init( value, artFinder );
	// return result;
	// }

}
