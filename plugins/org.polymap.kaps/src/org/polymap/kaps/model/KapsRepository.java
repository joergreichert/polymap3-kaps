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

import static org.qi4j.api.query.QueryExpressions.orderBy;
import static org.qi4j.api.query.QueryExpressions.templateFor;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SortedMap;
import java.util.TreeMap;

import java.text.SimpleDateFormat;

import org.geotools.feature.NameImpl;
import org.opengis.feature.type.Name;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.catalog.model.CatalogRepository;
import org.polymap.core.model.CompletionException;
import org.polymap.core.model.Entity;
import org.polymap.core.model.EntityType.Property;
import org.polymap.core.operation.IOperationSaveListener;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.qi4j.Qi4jPlugin;
import org.polymap.core.qi4j.Qi4jPlugin.Session;
import org.polymap.core.qi4j.QiModule;
import org.polymap.core.qi4j.QiModuleAssembler;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.entity.ConcurrentModificationException;

import org.polymap.kaps.model.data.BodennutzungComposite;
import org.polymap.kaps.model.data.FlurComposite;
import org.polymap.kaps.model.data.FlurstueckComposite;
import org.polymap.kaps.model.data.FlurstuecksdatenAgrarComposite;
import org.polymap.kaps.model.data.FlurstuecksdatenBaulandComposite;
import org.polymap.kaps.model.data.GebaeudeArtComposite;
import org.polymap.kaps.model.data.GemarkungComposite;
import org.polymap.kaps.model.data.GemeindeComposite;
import org.polymap.kaps.model.data.KaeuferKreisComposite;
import org.polymap.kaps.model.data.NutzungComposite;
import org.polymap.kaps.model.data.StalaComposite;
import org.polymap.kaps.model.data.StrasseComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.model.data.VertragsArtComposite;
import org.polymap.kaps.model.data.WohnungseigentumComposite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class KapsRepository
        extends QiModule {

    private static Log                   log        = LogFactory.getLog( KapsRepository.class );

    public static final String           NAMESPACE  = "http://polymap.org/kaps";

    public final static SimpleDateFormat SHORT_DATE = new SimpleDateFormat( "dd.MM.yyyy" );


    /**
     * Get or create the repository for the current user session.
     */
    public static final KapsRepository instance() {
        return Qi4jPlugin.Session.instance().module( KapsRepository.class );
    }

    // instance *******************************************

    private IOperationSaveListener operationListener = new OperationSaveListener();

    // private Map<String,VertragsArtComposite> btNamen;

    // private Map<String,VertragsArtComposite> btNummern;

    /** Allow direct access for operations. */
    protected KapsService          kapsService;


    // private Map<String, VertragsArtComposite> vertragsArtNamen;

    // public ServiceReference<BiotopnummerGeneratorService> biotopnummern;

    public static class SimpleEntityProvider<T extends Entity>
            extends KapsEntityProvider<T> {

        public SimpleEntityProvider( QiModule repo, Class entityClass, Name entityName ) {
            super( repo, entityClass, entityName );
        }
    };


    public KapsRepository( final QiModuleAssembler assembler ) {
        super( assembler );
        log.debug( "Initializing Kaps module..." );

        // for the global instance of the module
        // (Qi4jPlugin.Session.globalInstance()) there
        // is no request context
        if (Polymap.getSessionDisplay() != null) {
            OperationSupport.instance().addOperationSaveListener( operationListener );
        }
        // biotopnummern = assembler.getModule().serviceFinder().findService(
        // BiotopnummerGeneratorService.class );
    }


    public void init( final Session session ) {
        try {

            kapsService = new KapsService(
                    new KaufvertragEntityProvider( this ),
                    new RichtwertzoneEntityProvider( this ),
                    // new SimpleEntityProvider<RichtwertzoneZeitraumComposite>(
                    // this,
                    // RichtwertzoneZeitraumComposite.class, new NameImpl(
                    // KapsRepository.NAMESPACE,
                    // "Richtwertzone - Gültigkeit" ) ),
                    new SimpleEntityProvider<VertragsArtComposite>( this, VertragsArtComposite.class, new NameImpl(
                            KapsRepository.NAMESPACE, "Vertragsart" ) ),

                    new SimpleEntityProvider<StalaComposite>( this, StalaComposite.class, new NameImpl(
                            KapsRepository.NAMESPACE, "Stala" ) ),
                    new SimpleEntityProvider<KaeuferKreisComposite>( this, KaeuferKreisComposite.class, new NameImpl(
                            KapsRepository.NAMESPACE, "Käuferkreis" ) ),

                    new SimpleEntityProvider<NutzungComposite>( this, NutzungComposite.class, new NameImpl(
                            KapsRepository.NAMESPACE, "Nutzung" ) ), new SimpleEntityProvider<GebaeudeArtComposite>(
                            this, GebaeudeArtComposite.class, new NameImpl( KapsRepository.NAMESPACE, "Gebäudeart" ) ),

                    new SimpleEntityProvider<GemeindeComposite>( this, GemeindeComposite.class, new NameImpl(
                            KapsRepository.NAMESPACE, "Gemeinde" ) ), new SimpleEntityProvider<StrasseComposite>( this,
                            StrasseComposite.class, new NameImpl( KapsRepository.NAMESPACE, "Strasse" ) ),

                    new SimpleEntityProvider<GemarkungComposite>( this, GemarkungComposite.class, new NameImpl(
                            KapsRepository.NAMESPACE, "Gemarkung" ) ),
                    new SimpleEntityProvider<FlurstuecksdatenBaulandComposite>( this,
                            FlurstuecksdatenBaulandComposite.class, new NameImpl( KapsRepository.NAMESPACE,
                                    FlurstuecksdatenBaulandComposite.NAME ) ),

                    new SimpleEntityProvider<FlurstuecksdatenAgrarComposite>( this,
                            FlurstuecksdatenAgrarComposite.class, new NameImpl( KapsRepository.NAMESPACE,
                                    FlurstuecksdatenAgrarComposite.NAME ) ), new SimpleEntityProvider<FlurComposite>(
                            this, FlurComposite.class, new NameImpl( KapsRepository.NAMESPACE, "Flur" ) ),

                    new SimpleEntityProvider<WohnungseigentumComposite>( this, WohnungseigentumComposite.class,
                            new NameImpl( KapsRepository.NAMESPACE, WohnungseigentumComposite.NAME ) ),
                    // nicht änderbare Wertelisten
                    // new SimpleEntityProvider<ErschliessungsBeitragComposite>(
                    // this,
                    // ErschliessungsBeitragComposite.class, new NameImpl(
                    // KapsRepository.NAMESPACE, "Erschliessungsbeitrag" ) ),
                    // new SimpleEntityProvider<BodenRichtwertKennungComposite>(
                    // this,
                    // BodenRichtwertKennungComposite.class, new NameImpl(
                    // KapsRepository.NAMESPACE, "Bodenrichtwertkennung" ) ),
                    // new SimpleEntityProvider<EntwicklungsZustandComposite>( this,
                    // EntwicklungsZustandComposite.class, new NameImpl(
                    // KapsRepository.NAMESPACE, "Entwicklungszustand" ) ) )

                    new SimpleEntityProvider<BodennutzungComposite>( this, BodennutzungComposite.class, new NameImpl(
                            KapsRepository.NAMESPACE, "Bodennutzung" ) ) );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }

        // register with catalog
        CatalogRepository catalogRepo = session.module( CatalogRepository.class );
        catalogRepo.getCatalog().addTransient( kapsService );
    }


    @Override
    protected void dispose() {
        if (operationListener != null) {
            OperationSupport.instance().removeOperationSaveListener( operationListener );
            operationListener = null;
        }
        if (kapsService != null) {
            kapsService.dispose( new NullProgressMonitor() );
        }
    }


    public <T extends SchlNamed> T findSchlNamed( Class<T> compositeType, String schl ) {
        Query<T> entities = findEntities( compositeType,
                QueryExpressions.eq( templateFor( compositeType ).schl(), schl ), 0, 1 );
        return entities.find();
    }


    public <T> Query<T> findEntities( Class<T> compositeType, BooleanExpression expression, int firstResult,
            int maxResults ) {
        // Lucene does not like Integer.MAX_VALUE!?
        maxResults = Math.min( maxResults, 1000000 );

        return super.findEntities( compositeType, expression, firstResult, maxResults );
    }


    public void applyChanges()
            throws ConcurrentModificationException, CompletionException {
        try {
            // save changes
            uow.apply();
        }
        catch (ConcurrentEntityModificationException e) {
            throw new ConcurrentModificationException( e );
        }
        catch (UnitOfWorkCompletionException e) {
            throw new CompletionException( e );
        }
    }


    public VertragComposite newKaufvertrag( final EntityCreator<VertragComposite> creator )
            throws Exception {
        return newEntity( VertragComposite.class, null, new EntityCreator<VertragComposite>() {

            public void create( VertragComposite prototype )
                    throws Exception {
                prototype.eingangsDatum().set( new Date() );
                prototype.kaufpreisAnteilZaehler().set( 1.0 );
                prototype.kaufpreisAnteilNenner().set( 1.0 );
                prototype.fuerGewosGeeignet().set( Boolean.TRUE );
                prototype.fuerAuswertungGeeignet().set( Boolean.TRUE );

                // eingangsnummer erst beim Speichern setzen!

                if (creator != null) {
                    creator.create( prototype );
                }
            }
        } );
    }


    public int highestEingangsNummer() {
        // TODO umstellen auf sequenzgenerator

        // nur zum Test noch die Suche über Verkäuferkreis mit rein
        // Query<KaeuferKreisComposite> kreise = findEntities(
        // KaeuferKreisComposite.class, null, 0, 1 );
        // KaeuferKreisComposite kreis1 = kreise.iterator().next();
        // KaeuferKreisComposite kreis2 = kreise.iterator().next();
        //
        VertragComposite template = templateFor( VertragComposite.class );

        // QueryBuilder<VertragComposite> builder =
        // assembler.getModule().queryBuilderFactory()
        // .newQueryBuilder( VertragComposite.class );
        // builder = builder.where( eq( template.kaeuferKreis(), kreis1 ) );
        // Query<VertragComposite> bentities = builder.newQuery( uow );
        // bentities.orderBy( orderBy( template.eingangsNr(),
        // OrderBy.Order.DESCENDING ) );
        // bentities.maxResults( 1 );
        // VertragComposite next = bentities.iterator().next();
        //
        Query<VertragComposite> entities = findEntities( VertragComposite.class,
        // eq( template.kaeuferKreis(), kreis1 ), 0, 1 );
                null, 0, 1 );
        entities.orderBy( orderBy( template.eingangsNr(), OrderBy.Order.DESCENDING ) );
        //
        // entities = findEntities( VertragComposite.class, eq(
        // template.kaeuferKreis(), kreis2 ),
        // 0, 1 );
        // VertragComposite next2 = entities
        // .orderBy( orderBy( template.eingangsNr(), OrderBy.Order.DESCENDING )
        // ).iterator()
        // .next();
        //
        // entities = findEntities( VertragComposite.class, eq(
        // template.kaeuferKreis(), kreis1 ),
        // 0, 1 );
        // VertragComposite next3 = entities
        // .orderBy( orderBy( template.eingangsNr(), OrderBy.Order.DESCENDING )
        // ).iterator()
        // .next();
        //
        // assert (next1.equals( next3 ));
        // assert (!next1.equals( next2 ));
        //
        // return 1;

        VertragComposite highest = entities.iterator().next();
        int highestEingangsNr = highest != null ? highest.eingangsNr().get() : 0;

        // minimum aktuelles Jahr * 100000 + 1
        int currentYear = new GregorianCalendar().get( Calendar.YEAR );
        int currentMinimumNumber = currentYear * 100000;

        return Math.max( highestEingangsNr, currentMinimumNumber ) + 1;
    }


    public <T extends SchlNamed> SortedMap<String, T> entitiesWithSchl( Class<T> entityClass ) {
        Query<T> entities = findEntities( entityClass, null, 0, 1000 );
        SortedMap<String, T> schluessel = new TreeMap<String, T>();
        for (T entity : entities) {
            try {
                String key = entity.schl().get();
                schluessel.put( key, entity );
            }
            catch (Exception e) {
                throw new IllegalStateException( "Exception on schl() on entity " + entity.id(), e );
            }
        }
        return schluessel;
    }


    public <T extends Named> SortedMap<String, T> entitiesWithNames( Class<T> entityClass ) {
        Property nameProperty = entityType( entityClass ).getProperty( "name" );
        Property schlProperty = entityType( entityClass ).getProperty( "schl" );
        if (nameProperty == null) {
            throw new IllegalStateException( entityClass + " doesnt have an 'name' Property" );
        }

        Query<T> entities = findEntities( entityClass, null, 0, 100000 );
        SortedMap<String, T> namen = new TreeMap<String, T>();
        for (T entity : entities) {
            try {
                String key = (String)nameProperty.getValue( entity );
                if (schlProperty != null) {
                    key = (String)schlProperty.getValue( entity ) + "  -  " + key;
                }
                namen.put( key, entity );
            }
            catch (Exception e) {
                throw new IllegalStateException( "Exception on name() on entity " + entity.id(), e );
            }
        }
        return namen;
    }


    public Iterable<FlurstueckComposite> findFlurstuecke( GemarkungComposite gemarkung, FlurComposite flur,
            Integer flurstuecksNummer, String unternummer ) {
        FlurstueckComposite template = templateFor( FlurstueckComposite.class );
        BooleanExpression expr = null;
        if (gemarkung != null) {
            expr = QueryExpressions.eq( template.gemarkung(), gemarkung );
        }
        if (flur != null) {
            BooleanExpression in = QueryExpressions.eq( template.flur(), flur );
            expr = (expr == null) ? in : QueryExpressions.and( expr, in );
        }
        if (flurstuecksNummer != null) {
            BooleanExpression in = QueryExpressions.eq( template.nummer(), flurstuecksNummer );
            expr = (expr == null) ? in : QueryExpressions.and( expr, in );
        }
        if (unternummer != null && !unternummer.isEmpty()) {
            BooleanExpression in = QueryExpressions.eq( template.unterNummer(), unternummer );
            expr = (expr == null) ? in : QueryExpressions.and( expr, in );
        }
        Query<FlurstueckComposite> matches = KapsRepository.instance().findEntities( FlurstueckComposite.class, expr,
                0, 100 );
        return matches;
    }
}
