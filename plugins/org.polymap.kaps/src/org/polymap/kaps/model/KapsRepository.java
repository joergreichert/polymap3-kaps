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
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
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

import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.catalog.model.CatalogRepository;
import org.polymap.core.model.Entity;
import org.polymap.core.model.EntityType;
import org.polymap.core.model.EntityType.Property;
import org.polymap.core.operation.IOperationSaveListener;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.qi4j.Qi4jPlugin;
import org.polymap.core.qi4j.Qi4jPlugin.Session;
import org.polymap.core.qi4j.QiModule;
import org.polymap.core.qi4j.QiModuleAssembler;
import org.polymap.core.runtime.Polymap;

import org.polymap.kaps.model.data.*;

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


    public <T extends Entity> T clone( Class<T> type, T src )
            throws Exception {
        EntityType entityType = entityType( type );
        T target = newEntity( type, null );
        Collection<EntityType.Property> p = entityType.getProperties();
        for (EntityType.Property prop : p) {
            prop.setValue( target, prop.getValue( src ) );
        }
        return target;
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
                            KapsRepository.NAMESPACE, VertragsArtComposite.NAME ) ),
                    new SimpleEntityProvider<RichtwertzoneZeitraumComposite>( this,
                            RichtwertzoneZeitraumComposite.class, new NameImpl( KapsRepository.NAMESPACE,
                                    RichtwertzoneZeitraumComposite.NAME ) ),
                    new FlurstueckEntityProvider( this ),
                    // new SimpleEntityProvider<StalaComposite>( this,
                    // StalaComposite.class, new NameImpl(
                    // KapsRepository.NAMESPACE, "Stala" ) ),
                    new SimpleEntityProvider<KaeuferKreisComposite>( this, KaeuferKreisComposite.class, new NameImpl(
                            KapsRepository.NAMESPACE, KaeuferKreisComposite.NAME ) ),
                    new SimpleEntityProvider<BodenRichtwertRichtlinieErgaenzungComposite>( this,
                            BodenRichtwertRichtlinieErgaenzungComposite.class, new NameImpl( KapsRepository.NAMESPACE,
                                    BodenRichtwertRichtlinieErgaenzungComposite.NAME ) ),
                    new SimpleEntityProvider<BodenRichtwertRichtlinieArtDerNutzungComposite>( this,
                            BodenRichtwertRichtlinieArtDerNutzungComposite.class, new NameImpl(
                                    KapsRepository.NAMESPACE, BodenRichtwertRichtlinieArtDerNutzungComposite.NAME ) ),

                    new SimpleEntityProvider<NutzungComposite>( this, NutzungComposite.class, new NameImpl(
                            KapsRepository.NAMESPACE, NutzungComposite.NAME ) ),
                    new SimpleEntityProvider<GebaeudeArtComposite>( this, GebaeudeArtComposite.class, new NameImpl(
                            KapsRepository.NAMESPACE, GebaeudeArtComposite.NAME ) ),
                    new SimpleEntityProvider<GebaeudeComposite>( this, GebaeudeComposite.class, new NameImpl(
                            KapsRepository.NAMESPACE, GebaeudeComposite.NAME ) ),

                    new SimpleEntityProvider<GemeindeComposite>( this, GemeindeComposite.class, new NameImpl(
                            KapsRepository.NAMESPACE, GemeindeComposite.NAME ) ),
                    new SimpleEntityProvider<StrasseComposite>( this, StrasseComposite.class, new NameImpl(
                            KapsRepository.NAMESPACE, StrasseComposite.NAME ) ),

                    new SimpleEntityProvider<GemarkungComposite>( this, GemarkungComposite.class, new NameImpl(
                            KapsRepository.NAMESPACE, GemarkungComposite.NAME ) ),
                    new SimpleEntityProvider<VertragsdatenBaulandComposite>( this, VertragsdatenBaulandComposite.class,
                            new NameImpl( KapsRepository.NAMESPACE, VertragsdatenBaulandComposite.NAME ) ),
                    new SimpleEntityProvider<AusstattungBewertungComposite>( this, AusstattungBewertungComposite.class,
                            new NameImpl( KapsRepository.NAMESPACE, AusstattungBewertungComposite.NAME ) ),
                    new SimpleEntityProvider<VertragsdatenAgrarComposite>( this, VertragsdatenAgrarComposite.class,
                            new NameImpl( KapsRepository.NAMESPACE, VertragsdatenAgrarComposite.NAME ) ),
                    new SimpleEntityProvider<FlurComposite>( this, FlurComposite.class, new NameImpl(
                            KapsRepository.NAMESPACE, FlurComposite.NAME ) ),
                    new WohnungseigentumEntityProvider( this ),
                    // new SimpleEntityProvider<WohnungseigentumComposite>( this,
                    // WohnungseigentumComposite.class,
                    // new NameImpl( KapsRepository.NAMESPACE,
                    // WohnungseigentumComposite.NAME ) ),
                    new SimpleEntityProvider<WohnungComposite>( this, WohnungComposite.class, new NameImpl(
                            KapsRepository.NAMESPACE, WohnungComposite.NAME ) ),

                    new SimpleEntityProvider<NHK2010AnbautenComposite>( this, NHK2010AnbautenComposite.class,
                            new NameImpl( KapsRepository.NAMESPACE, NHK2010AnbautenComposite.NAME ) ),
                    new SimpleEntityProvider<NHK2010BaupreisIndexComposite>( this, NHK2010BaupreisIndexComposite.class,
                            new NameImpl( KapsRepository.NAMESPACE, NHK2010BaupreisIndexComposite.NAME ) ),
                    new SimpleEntityProvider<NHK2010BewertungComposite>( this, NHK2010BewertungComposite.class,
                            new NameImpl( KapsRepository.NAMESPACE, NHK2010BewertungComposite.NAME ) ),
                    new SimpleEntityProvider<ErmittlungModernisierungsgradComposite>( this,
                            ErmittlungModernisierungsgradComposite.class, new NameImpl( KapsRepository.NAMESPACE,
                                    ErmittlungModernisierungsgradComposite.NAME ) ),
                    new SimpleEntityProvider<ErtragswertverfahrenComposite>( this, ErtragswertverfahrenComposite.class,
                            new NameImpl( KapsRepository.NAMESPACE, ErtragswertverfahrenComposite.NAME ) ),

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
                            KapsRepository.NAMESPACE, BodennutzungComposite.NAME ) ) );
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

    //
    // @Override
    // public void commitChanges()
    // throws ConcurrentModificationException, CompletionException {
    // try {
    // // save changes
    // uow.complete();
    // uow = assembler.getModule().unitOfWorkFactory().newUnitOfWork();
    // }
    // catch (ConcurrentEntityModificationException e) {
    // throw new ConcurrentModificationException( e );
    // }
    // catch (UnitOfWorkCompletionException e) {
    // throw new CompletionException( e );
    // }
    // }

    private Map<Integer, Integer> highestNumbers = new HashMap<Integer, Integer>();


    public synchronized int highestEingangsNummer( Date vertragsdatum ) {
        Calendar cal = new GregorianCalendar();
        cal.setTime( vertragsdatum );
        cal.set( Calendar.DAY_OF_YEAR, 1 );
        cal.set( Calendar.HOUR_OF_DAY, 0 );
        cal.set( Calendar.MINUTE, 0 );
        cal.set( Calendar.SECOND, 0 );
        cal.set( Calendar.MILLISECOND, 0 );

        Date lowerDate = cal.getTime();

        // minimum aktuelles Jahr * 100000 + 1
        int currentYear = cal.get( Calendar.YEAR );

        Integer highest = highestNumbers.get( Integer.valueOf( currentYear ) );
        if (highest == null) {

            int currentMinimumNumber = currentYear * 100000;

            cal.roll( Calendar.YEAR, true );
            Date upperDate = cal.getTime();

            VertragComposite template = templateFor( VertragComposite.class );
            BooleanExpression exp = // QueryExpressions.and( QueryExpressions.ge(
                                    // template.vertragsDatum(), lowerDate ),
            QueryExpressions.lt( template.vertragsDatum(), upperDate );

            Query<VertragComposite> entities = findEntities( VertragComposite.class, exp, 0, -1 );
            entities.orderBy( orderBy( template.eingangsNr(), OrderBy.Order.DESCENDING ) );

            VertragComposite v = entities.iterator().next();
            int highestEingangsNr = v != null ? v.eingangsNr().get() : 0;

            highest = Math.max( highestEingangsNr, currentMinimumNumber );
        }
        highest += 1;
        highestNumbers.put( currentYear, highest );
        return highest;

    }

    private Integer highestObjektNummer = null;


    public synchronized int highestObjektNummer() {
        if (highestObjektNummer == null) {
            WohnungseigentumComposite template = templateFor( WohnungseigentumComposite.class );

            Query<WohnungseigentumComposite> entities = findEntities( WohnungseigentumComposite.class, null, 0, -1 );
            entities.orderBy( orderBy( template.objektNummer(), OrderBy.Order.DESCENDING ) );

            WohnungseigentumComposite v = entities.iterator().next();
            highestObjektNummer = v != null ? v.objektNummer().get() : Integer.valueOf( 0 );
        }
        highestObjektNummer += 1;
        return highestObjektNummer;

    }

    private Integer highestGebaeudeNummer = null;


    public synchronized int highestGebaeudeNummer( WohnungseigentumComposite parent ) {
        if (highestGebaeudeNummer == null) {
            GebaeudeComposite template = templateFor( GebaeudeComposite.class );

            Query<GebaeudeComposite> entities = findEntities( GebaeudeComposite.class, QueryExpressions.and(
                    QueryExpressions.eq( template.objektNummer(), parent.objektNummer().get() ),
                    QueryExpressions.eq( template.objektFortfuehrung(), parent.objektFortfuehrung().get() ) ), 0, -1 );
            entities.orderBy( orderBy( template.gebaeudeNummer(), OrderBy.Order.DESCENDING ) );

            GebaeudeComposite v = entities.iterator().hasNext() ? entities.iterator().next() : null;
            highestGebaeudeNummer = v != null ? v.gebaeudeNummer().get() : Integer.valueOf( 0 );
        }
        highestGebaeudeNummer += 1;
        return highestGebaeudeNummer;

    }

    private Integer highestWohnungsNummer = null;


    public synchronized int highestWohnungsNummer( GebaeudeComposite parent ) {
        if (highestWohnungsNummer == null) {
            WohnungComposite template = templateFor( WohnungComposite.class );

            Query<WohnungComposite> entities = findEntities( WohnungComposite.class, QueryExpressions.and(
                    QueryExpressions.eq( template.objektNummer(), parent.objektNummer().get() ),
                    QueryExpressions.eq( template.objektFortfuehrung(), parent.objektFortfuehrung().get() ),
                    QueryExpressions.eq( template.gebaeudeNummer(), parent.gebaeudeNummer().get() ),
                    QueryExpressions.eq( template.gebaeudeFortfuehrung(), parent.gebaeudeFortfuehrung().get() ) ), 0,
                    -1 );
            entities.orderBy( orderBy( template.wohnungsNummer(), OrderBy.Order.DESCENDING ) );

            WohnungComposite v = entities.iterator().hasNext() ? entities.iterator().next() : null;
            highestWohnungsNummer = v != null ? v.wohnungsNummer().get() : Integer.valueOf( 0 );
        }
        highestWohnungsNummer += 1;
        return highestWohnungsNummer;

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
        BooleanExpression expr = null;// QueryExpressions.not( QueryExpressions.eq(
                                      // template.vertrag(), null ));
        if (gemarkung != null) {
            expr = QueryExpressions.eq( template.gemarkung(), gemarkung );
        }
        if (flur != null) {
            BooleanExpression in = QueryExpressions.eq( template.flur(), flur );
            expr = (expr == null) ? in : QueryExpressions.and( expr, in );
        }
        if (flurstuecksNummer != null) {
            BooleanExpression in = QueryExpressions.eq( template.hauptNummer(), flurstuecksNummer );
            expr = (expr == null) ? in : QueryExpressions.and( expr, in );
        }
        if (unternummer != null && !unternummer.isEmpty()) {
            BooleanExpression in = QueryExpressions.eq( template.unterNummer(), unternummer );
            expr = (expr == null) ? in : QueryExpressions.and( expr, in );
        }
        Query<FlurstueckComposite> matches = KapsRepository.instance().findEntities( FlurstueckComposite.class, expr,
                0, 100 );

        // alle FlurstueckComposite finden für die Flurstücke
        //
        // FlurstueckComposite verkaufTemplate = QueryExpressions.templateFor(
        // FlurstueckComposite.class );
        // BooleanExpression dExpr = null;
        // for (FlurstueckComposite flurstueck : matches) {
        // BooleanExpression newExpr = QueryExpressions.eq(
        // verkaufTemplate.flurstueck(), flurstueck );
        // if (dExpr == null) {
        // dExpr = newExpr;
        // }
        // else {
        // dExpr = QueryExpressions.or( dExpr, newExpr );
        // }
        // }
        // Query<FlurstueckComposite> matches2 =
        // KapsRepository.instance().findEntities( FlurstueckComposite.class, dExpr,
        // 0, 100 );
        return matches;
    }
}
