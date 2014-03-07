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

import static org.qi4j.api.query.QueryExpressions.templateFor;

import java.util.Collection;
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
import org.qi4j.api.service.ServiceReference;

import org.eclipse.jface.dialogs.MessageDialog;

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
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.kaps.model.data.AusstattungBewertungComposite;
import org.polymap.kaps.model.data.BodenRichtwertRichtlinieArtDerNutzungComposite;
import org.polymap.kaps.model.data.BodenRichtwertRichtlinieErgaenzungComposite;
import org.polymap.kaps.model.data.BodennutzungComposite;
import org.polymap.kaps.model.data.ErmittlungModernisierungsgradComposite;
import org.polymap.kaps.model.data.ErtragswertverfahrenComposite;
import org.polymap.kaps.model.data.FlurComposite;
import org.polymap.kaps.model.data.GebaeudeArtComposite;
import org.polymap.kaps.model.data.GebaeudeComposite;
import org.polymap.kaps.model.data.GemarkungComposite;
import org.polymap.kaps.model.data.GemeindeComposite;
import org.polymap.kaps.model.data.KaeuferKreisComposite;
import org.polymap.kaps.model.data.NHK2010AnbautenComposite;
import org.polymap.kaps.model.data.NHK2010BaupreisIndexComposite;
import org.polymap.kaps.model.data.NutzungComposite;
import org.polymap.kaps.model.data.RichtwertzoneZeitraumComposite;
import org.polymap.kaps.model.data.StrasseComposite;
import org.polymap.kaps.model.data.VertragsArtComposite;
import org.polymap.kaps.model.data.WohnungComposite;
import org.polymap.kaps.model.idgen.EingangsNummerGeneratorService;
import org.polymap.kaps.model.idgen.GebaeudeNummerGeneratorService;
import org.polymap.kaps.model.idgen.ObjektNummerGeneratorService;
import org.polymap.kaps.model.idgen.SchlGeneratorService;
import org.polymap.kaps.model.idgen.WohnungsNummerGeneratorService;
import org.polymap.kaps.ui.form.BewertungAnhandVonAustattungsmerkmalenFormEditorPage;

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

    private IOperationSaveListener                          operationListener = new OperationSaveListener();

    // private Map<String,VertragsArtComposite> btNamen;

    // private Map<String,VertragsArtComposite> btNummern;

    /** Allow direct access for operations. */
    protected KapsService                                   kapsService;

    // private Map<String, VertragsArtComposite> vertragsArtNamen;

    public final  ServiceReference<EingangsNummerGeneratorService> eingangsNummern;

    public final ServiceReference<ObjektNummerGeneratorService>   objektnummern;

    public final ServiceReference<GebaeudeNummerGeneratorService>   gebaeudeNummern;

    public final ServiceReference<WohnungsNummerGeneratorService>   wohnungsNummern;

    private final ServiceReference<SchlGeneratorService>   schl;


    public static class SimpleEntityProvider<T extends Entity>
            extends KapsEntityProvider<T> {

        public SimpleEntityProvider( QiModule repo, Class entityClass, Name entityName ) {
            super( repo, entityClass, entityName );
        }
    };


    public static class SchlEntityProvider<T extends SchlNamed>
            extends KapsEntityProvider<T> {

        private ServiceReference<SchlGeneratorService> schl;


        public SchlEntityProvider( QiModule repo, Class<T> entityClass, Name entityName, ServiceReference<SchlGeneratorService> schl ) {
            super( repo, entityClass, entityName );
            this.schl = schl;
        }


        @Override
        public boolean modifyFeature( T entity, String propName, Object value )
                throws Exception {
            // set defaults
            if (value == null) {
                if (entity.schl().qualifiedName().name().equals( propName )) {
                    entity.schl().set( schl.get().generate(getEntityType().getType()).toString() );
                    return true;
                }
            }
            return super.modifyFeature( entity, propName, value );
        }
    }


    public KapsRepository( final QiModuleAssembler assembler ) {
        super( assembler );
        log.debug( "Initializing Kaps module..." );

        // for the global instance of the module
        // (Qi4jPlugin.Session.globalInstance()) there
        // is no request context
        if (Polymap.getSessionDisplay() != null) {
            OperationSupport.instance().addOperationSaveListener( operationListener );
        }
        eingangsNummern = assembler.getModule().serviceFinder().findService( EingangsNummerGeneratorService.class );
        objektnummern = assembler.getModule().serviceFinder().findService( ObjektNummerGeneratorService.class );
        gebaeudeNummern = assembler.getModule().serviceFinder().findService( GebaeudeNummerGeneratorService.class );
        wohnungsNummern = assembler.getModule().serviceFinder().findService( WohnungsNummerGeneratorService.class );
        schl = assembler.getModule().serviceFinder().findService( SchlGeneratorService.class );
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

            kapsService = new KapsService( new KaufvertragEntityProvider( this ),
                    new RichtwertzoneEntityProvider( this ), new SimpleEntityProvider<VertragsArtComposite>( this,
                            VertragsArtComposite.class, new NameImpl( KapsRepository.NAMESPACE,
                                    VertragsArtComposite.NAME ) ),
                    new SimpleEntityProvider<RichtwertzoneZeitraumComposite>( this,
                            RichtwertzoneZeitraumComposite.class, new NameImpl( KapsRepository.NAMESPACE,
                                    RichtwertzoneZeitraumComposite.NAME ) ), new FlurstueckEntityProvider( this ),
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
                    new SchlEntityProvider<StrasseComposite>( this, StrasseComposite.class, new NameImpl(
                            KapsRepository.NAMESPACE, StrasseComposite.NAME ), schl ),

                    new SimpleEntityProvider<GemarkungComposite>( this, GemarkungComposite.class, new NameImpl(
                            KapsRepository.NAMESPACE, GemarkungComposite.NAME ) ),
                    new VertragsdatenBaulandEntityProvider( this ),
                    new SimpleEntityProvider<AusstattungBewertungComposite>( this, AusstattungBewertungComposite.class,
                            new NameImpl( KapsRepository.NAMESPACE, AusstattungBewertungComposite.NAME ) ),
                    new VertragsdatenAgrarEntityProvider( this ), new SimpleEntityProvider<FlurComposite>( this,
                            FlurComposite.class, new NameImpl( KapsRepository.NAMESPACE, FlurComposite.NAME ) ),
                    new WohnungseigentumEntityProvider( this ), new WohnungEntityProvider( this ),

                    new SimpleEntityProvider<NHK2010AnbautenComposite>( this, NHK2010AnbautenComposite.class,
                            new NameImpl( KapsRepository.NAMESPACE, NHK2010AnbautenComposite.NAME ) ),
                    new SimpleEntityProvider<NHK2010BaupreisIndexComposite>( this, NHK2010BaupreisIndexComposite.class,
                            new NameImpl( KapsRepository.NAMESPACE, NHK2010BaupreisIndexComposite.NAME ) ),
                    new NHK2010BewertungEntityProvider( this ),
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
    
    @Override
    public void removeEntity( Entity entity ) {
        if (entity instanceof WohnungComposite) {
            removeWohnung((WohnungComposite)entity);
        }
        else if (entity instanceof AusstattungBewertungComposite || entity instanceof ErmittlungModernisierungsgradComposite) {
            // nichts weiter zu löschen hier
            super.removeEntity( entity );
        }
        else {
            Polymap.getSessionDisplay().asyncExec( new Runnable() {

                public void run() {
                    MessageDialog.openError( PolymapWorkbench.getShellToParentOn(), "Fehler beim Löschen",
                            "Das Löschen dieses Objektes ist noch nicht implementiert.\n Bitte legen Sie ein entsprechendes Ticket im trac an.\n\n http://polymap.org/kaps/newticket" );
                }
            } );
        }
    }


    /**
     *
     * @param entity
     */
    private void removeWohnung( WohnungComposite wohnung ) {
        AusstattungBewertungComposite ausstattungBewertungComposite = AusstattungBewertungComposite.Mixin.forWohnung( wohnung );
        if (ausstattungBewertungComposite != null) {
            removeEntity( ausstattungBewertungComposite );
        }
        ErmittlungModernisierungsgradComposite ermittlungModernisierungsgradComposite = ErmittlungModernisierungsgradComposite.Mixin.forWohnung( wohnung );
        if (ermittlungModernisierungsgradComposite != null) {
            removeEntity( ermittlungModernisierungsgradComposite );
        }
        super.removeEntity( wohnung );
    }
}
