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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;

import org.polymap.core.qi4j.QiModule;
import org.polymap.core.qi4j.QiModuleAssembler;
import org.polymap.core.qi4j.idgen.HRIdentityGeneratorService;
import org.polymap.core.runtime.Polymap;

import org.polymap.rhei.data.entitystore.lucene.LuceneEntityStoreInfo;
import org.polymap.rhei.data.entitystore.lucene.LuceneEntityStoreQueryService;
import org.polymap.rhei.data.entitystore.lucene.LuceneEntityStoreService;

import org.polymap.kaps.model.SchlNamedCreatorCallback.Impl;
import org.polymap.kaps.model.data.*;
import org.polymap.kaps.model.idgen.EingangsNummerGeneratorService;
import org.polymap.kaps.model.idgen.GebaeudeNummerGeneratorService;
import org.polymap.kaps.model.idgen.ObjektNummerGeneratorService;
import org.polymap.kaps.model.idgen.SchlGeneratorService;
import org.polymap.kaps.model.idgen.WohnungsNummerGeneratorService;

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class KapsRepositoryAssembler
        extends QiModuleAssembler {

    private static Log        log = LogFactory.getLog( KapsRepositoryAssembler.class );

    private Application       app;

    private UnitOfWorkFactory uowf;

    private Module            module;


    public KapsRepositoryAssembler() {
    }


    public Module getModule() {
        return module;
    }


    protected void setApp( Application app ) {
        this.app = app;
        this.module = app.findModule( "application-layer", "kaps-module" );
        this.uowf = module.unitOfWorkFactory();
    }


    public QiModule newModule() {
        return new KapsRepository( this );
    }


    @SuppressWarnings("unchecked")
    public void assemble( ApplicationAssembly _app )
            throws Exception {
        log.info( "Assembling: org.polymap.kaps ..." );

        // project layer / module
        LayerAssembly domainLayer = _app.layerAssembly( "application-layer" );
        ModuleAssembly domainModule = domainLayer.moduleAssembly( "kaps-module" );
        domainModule.addEntities( VertragComposite.class, VertragsArtComposite.class, KaeuferKreisComposite.class,
                GemeindeComposite.class, GebaeudeArtComposite.class, NutzungComposite.class, StrasseComposite.class,
                BodennutzungComposite.class, FlurComposite.class, GemarkungComposite.class,
                RichtwertzoneComposite.class, RichtwertzoneZeitraumComposite.class,
                ErschliessungsBeitragComposite.class, BodenRichtwertKennungComposite.class,
                EntwicklungsZustandComposite.class, RichtwertZoneLageComposite.class, EntwicklungsZusatzComposite.class,
                BauweiseComposite.class, ArtDesBaugebietsComposite.class, FlurstueckComposite.class,
                GemeindeFaktorComposite.class, BodenwertAufteilungTextComposite.class,
                VertragsdatenBaulandComposite.class, VertragsdatenErweitertComposite.class, KellerComposite.class,
                VertragsdatenAgrarComposite.class, BelastungComposite.class, EtageComposite.class,
                AusstattungComposite.class, EigentumsartComposite.class, HimmelsrichtungComposite.class,
                WohnungseigentumComposite.class, GebaeudeComposite.class, AusstattungBewertungComposite.class,
                WohnungComposite.class, GebaeudeArtStaBuComposite.class, ArtDerBauflaecheStaBuComposite.class,
                ArtDesBaugebietesStalaComposite.class, GrundstuecksArtAgrarLandStalaComposite.class,
                ErwerberStalaComposite.class, GrundstuecksArtBaulandStalaComposite.class,
                KaeuferKreisStaBuComposite.class, VeraeussererAgrarLandStalaComposite.class,
                VeraeussererBaulandStalaComposite.class, VerwandschaftsVerhaeltnisStalaComposite.class,
                VertragsArtArtComposite.class, BodenRichtwertRichtlinieErgaenzungComposite.class,
                BodenRichtwertRichtlinieArtDerNutzungComposite.class, NHK2010AnbautenComposite.class,
                NHK2010BaupreisIndexComposite.class, NHK2010BewertungComposite.class,
                NHK2010BewertungGebaeudeComposite.class, ErmittlungModernisierungsgradComposite.class,
                ErtragswertverfahrenComposite.class, GebaeudeTypStaBuComposite.class, StockwerkStaBuComposite.class,
                ImmobilienArtStaBuComposite.class, WohnlageStaBuComposite.class, NHK2000BewertungComposite.class,
                NHK2000BewertungGebaeudeComposite.class );

        // persistence: workspace/Lucene
        File moduleRoot = createDataDir();

        domainModule.addServices( LuceneEntityStoreService.class )
                .setMetaInfo( new LuceneEntityStoreInfo( moduleRoot ) ).instantiateOnStartup()
                .identifiedBy( "lucene-repository" );

        // indexer
        domainModule.addServices( LuceneEntityStoreQueryService.class ).instantiateOnStartup();

        domainModule.addServices( HRIdentityGeneratorService.class );

        // additional services
        domainModule.addServices( EingangsNummerGeneratorService.class ).identifiedBy( "eingangsnummer" );
        domainModule.addServices( ObjektNummerGeneratorService.class ).identifiedBy( "objektnummer" );
        domainModule.addServices( SchlGeneratorService.class ).identifiedBy( "schl" );
        domainModule.addServices( GebaeudeNummerGeneratorService.class ).identifiedBy( "gebaeudenummer" );
        domainModule.addServices( WohnungsNummerGeneratorService.class ).identifiedBy( "wohnungsnummer" );

    }


    private File createDataDir() {
        File root = new File( Polymap.getWorkspacePath().toFile(), "data" );

        File moduleRoot = new File( root, "org.polymap.kaps" );
        moduleRoot.mkdir();
        return moduleRoot;
    }


    public void createInitData()
            throws Exception {

        // create the composites
        final UnitOfWork uow = uowf.newUnitOfWork();
        final Impl schlCreator = new SchlNamedCreatorCallback.Impl( uow );
        if (!isDBInitialized( uow )) {

            log.info( "Create Init Data" );

            ErschliessungsBeitragComposite.Mixin.createInitData( schlCreator );
            BodenRichtwertKennungComposite.Mixin.createInitData( schlCreator );
            EntwicklungsZustandComposite.Mixin.createInitData( schlCreator );
            RichtwertZoneLageComposite.Mixin.createInitData( schlCreator );
            EntwicklungsZusatzComposite.Mixin.createInitData( schlCreator );
            BauweiseComposite.Mixin.createInitData( schlCreator );
            ArtDesBaugebietsComposite.Mixin.createInitData( uow );
            KellerComposite.Mixin.createInitData( schlCreator );

        }
        if (!isDBStaBuInitialized( uow )) {
            log.info( "Create StaBu Data" );

            
            GebaeudeArtStaBuComposite.Mixin.createInitData( schlCreator );
            ArtDerBauflaecheStaBuComposite.Mixin.createInitData( schlCreator );
            KaeuferKreisStaBuComposite.Mixin.createInitData( schlCreator );
            GebaeudeTypStaBuComposite.Mixin.createInitData( schlCreator );
            StockwerkStaBuComposite.Mixin.createInitData( schlCreator );
            ImmobilienArtStaBuComposite.Mixin.createInitData( schlCreator );
            WohnlageStaBuComposite.Mixin.createInitData( schlCreator );
        }
        migrateBelastung( uow );
        migrateRichtwertzone( uow );
        // findEmptyRichtwertZone( uow );
        migrateBaukosten( uow );
        migrateEingangsnummern( uow );
        migrateVertragsdatenErweitert( uow );

        deleteBrokenRWZ( uow );
        
        migrateStaBu( schlCreator, uow );

        uow.complete();
        log.info( "Create Init Data Completed" );
    }


    private boolean isDBInitialized( UnitOfWork uow ) {
        QueryBuilder<ErschliessungsBeitragComposite> builder = getModule().queryBuilderFactory()
                .newQueryBuilder( ErschliessungsBeitragComposite.class );
        Query<ErschliessungsBeitragComposite> query = builder.newQuery( uow ).maxResults( 1 ).firstResult( 0 );
        return query.iterator().hasNext();
    }


    private boolean isDBStaBuInitialized( UnitOfWork uow ) {
        QueryBuilder<GebaeudeArtStaBuComposite> builder = getModule().queryBuilderFactory()
                .newQueryBuilder( GebaeudeArtStaBuComposite.class );
        Query<GebaeudeArtStaBuComposite> query = builder.newQuery( uow ).maxResults( 1 ).firstResult( 0 );
        return query.iterator().hasNext();
    }


    private void migrateBaukosten( UnitOfWork uow )
            throws IOException {
        File file = new File( createDataDir(), "migration.baukostenindex" );
        if (!file.exists()) {
            log.info( "Migrating Baukostenindex" );
            QueryBuilder<NHK2010BewertungGebaeudeComposite> builder = getModule().queryBuilderFactory()
                    .newQueryBuilder( NHK2010BewertungGebaeudeComposite.class );
            Query<NHK2010BewertungGebaeudeComposite> query = builder.newQuery( uow ).maxResults( Integer.MAX_VALUE )
                    .firstResult( 0 );
            Iterator<NHK2010BewertungGebaeudeComposite> it = query.iterator();
            int count = 0;
            while (it.hasNext()) {
                // neu setzen
                NHK2010BewertungGebaeudeComposite gebaeude = it.next();
                String indexTyp = gebaeude.baukostenIndexTyp().get();
                if (indexTyp != null && ("E".equals( indexTyp ) || "M".equals( indexTyp ))) {
                    count++;
                    gebaeude.baukostenIndexTyp().set( "W" );
                }
            }
            file.createNewFile();
            log.info( "Migration of " + count + " Baukostenindex Completed" );
        }
    }


    private void migrateBelastung( UnitOfWork uow )
            throws IOException {
        File file = new File( createDataDir(), "migration.manyBelastung" );
        if (!file.exists()) {
            log.info( "Migrating Belastungen" );
            QueryBuilder<FlurstueckComposite> builder = getModule().queryBuilderFactory()
                    .newQueryBuilder( FlurstueckComposite.class );
            Query<FlurstueckComposite> query = builder.newQuery( uow ).maxResults( Integer.MAX_VALUE ).firstResult( 0 );
            Iterator<FlurstueckComposite> it = query.iterator();
            int count = 0;
            while (it.hasNext()) {
                FlurstueckComposite fs = it.next();
                BelastungComposite b = fs.belastung().get();
                if (b != null) {
                    count++;
                    fs.belastungen().add( b );
                }
            }
            file.createNewFile();
            log.info( "Migration of " + count + " Belastungen Completed" );
        }
    }


    private void migrateRichtwertzone( UnitOfWork uow )
            throws IOException {
        File file = new File( createDataDir(), "migration.richtwertZoneLatest2" );
        if (!file.exists()) {
            log.info( "Migrating Richtwertzone" );
            QueryBuilder<RichtwertzoneComposite> builder = getModule().queryBuilderFactory()
                    .newQueryBuilder( RichtwertzoneComposite.class );
            QueryBuilder<RichtwertzoneZeitraumComposite> builderZ = getModule().queryBuilderFactory()
                    .newQueryBuilder( RichtwertzoneZeitraumComposite.class );
            RichtwertzoneZeitraumComposite template = QueryExpressions
                    .templateFor( RichtwertzoneZeitraumComposite.class );
            Query<RichtwertzoneComposite> query = builder.newQuery( uow ).maxResults( Integer.MAX_VALUE )
                    .firstResult( 0 );
            Iterator<RichtwertzoneComposite> it = query.iterator();
            int count = 0;
            while (it.hasNext()) {
                RichtwertzoneComposite zone = it.next();
                BooleanExpression expr = QueryExpressions.eq( template.zone(), zone );
                RichtwertzoneZeitraumComposite zeitraum = builderZ.where( expr ).newQuery( uow )
                        .maxResults( Integer.MAX_VALUE )
                        .orderBy( orderBy( template.gueltigAb(), OrderBy.Order.DESCENDING ) ).maxResults( 1 ).find();

                zone.latestZone().set( zeitraum );
                count++;
            }
            file.createNewFile();
            log.info( "Migration of " + count + " Richtwertzone Completed" );
        }
    }


    //
    // private void findEmptyRichtwertZone( UnitOfWork uow )
    // throws IOException {
    // log.info( "Suche ungueltige Richtwertzone" );
    // QueryBuilder<RichtwertzoneZeitraumComposite> builderZ =
    // getModule().queryBuilderFactory().newQueryBuilder(
    // RichtwertzoneZeitraumComposite.class );
    // Iterator<RichtwertzoneZeitraumComposite> it = builderZ.newQuery( uow
    // ).maxResults( Integer.MAX_VALUE )
    // .iterator();
    // int count = 0;
    // while (it.hasNext()) {
    // RichtwertzoneZeitraumComposite zeitraum = it.next();
    // if (zeitraum.schl().get() == null || zeitraum.gueltigAb().get() == null) {
    // log.info( "Zone " + zeitraum.id() + " " + (zeitraum.zone().get() != null ?
    // zeitraum.zone().get().schl().get() : "null") + ":" + zeitraum.schl().get() +
    // " fehlen Daten." );
    // }
    // }
    // log.info( "Migration of " + count + " Richtwertzone Completed" );
    // }

    private void migrateEingangsnummern( UnitOfWork uow )
            throws IOException {
        File file = new File( createDataDir(), "migration.Eingangsnummern" );
        if (!file.exists()) {
            log.info( "Migrating Eingangsnummern" );

            QueryBuilder<VertragComposite> builder = getModule().queryBuilderFactory()
                    .newQueryBuilder( VertragComposite.class );
            VertragComposite template = QueryExpressions.templateFor( VertragComposite.class );

            // find höchsten aus 2013
            Calendar cal = new GregorianCalendar();
            cal.set( Calendar.DAY_OF_YEAR, 1 );
            cal.set( Calendar.HOUR_OF_DAY, 0 );
            cal.set( Calendar.MINUTE, 0 );
            cal.set( Calendar.SECOND, 0 );
            cal.set( Calendar.MILLISECOND, 0 );
            cal.set( Calendar.YEAR, 2013 );

            BooleanExpression expr = QueryExpressions.and( QueryExpressions.lt( template.eingangsNr(), 2014 * 100000 ),
                    QueryExpressions.ge( template.vertragsDatum(), cal.getTime() ) );
            VertragComposite latest = builder.where( expr ).newQuery( uow )
                    .orderBy( orderBy( template.eingangsNr(), OrderBy.Order.DESCENDING ) ).maxResults( 1 ).find();

            int latestNumber = latest.eingangsNr().get();
            log.info( "Migrating Eingangsnummern, latest number was " + latestNumber );

            // update alle > 201400000

            cal.set( Calendar.YEAR, 2014 );

            expr = QueryExpressions.and( QueryExpressions.ge( template.eingangsNr(), 2014 * 100000 ),
                    QueryExpressions.lt( template.vertragsDatum(), cal.getTime() ) );
            Query<VertragComposite> query = builder.where( expr ).newQuery( uow )
                    .orderBy( orderBy( template.vertragsDatum(), OrderBy.Order.DESCENDING ) )
                    .maxResults( Integer.MAX_VALUE ).firstResult( 0 );
            log.info( "Migrating Eingangsnummern, found " + query.count() + " wrong numbers" );
            Iterator<VertragComposite> it = query.iterator();
            int count = 0;
            while (it.hasNext()) {
                latestNumber = latestNumber + 1;
                VertragComposite vertrag = it.next();
                vertrag.eingangsNr().set( latestNumber );
                count++;
            }
            file.createNewFile();
            log.info( "Migration of " + count + " Eingangsnummern Completed, latest number is " + latestNumber );
        }
    }


    private void migrateVertragsdatenErweitert( UnitOfWork uow )
            throws IOException, ParseException {
        File file = new File( createDataDir(), "migration.Vertragsdatenerweitert" );
        if (!file.exists()) {
            log.info( "Migrating Eingangsnummern" );

            QueryBuilder<VertragComposite> builder = getModule().queryBuilderFactory()
                    .newQueryBuilder( VertragComposite.class );
            VertragsdatenErweitertImportFix.fix( uow, builder );
            file.createNewFile();
            log.info( "Migration of  Vertragsdatenerweitert Completed" );
        }
    }


    private void deleteBrokenRWZ( UnitOfWork uow ) {
        // File file = new File( createDataDir(), "migration.brokenRWZ" );
        // if (!file.exists()) {
        log.info( "Migrating broken RWZ" );
        QueryBuilder<RichtwertzoneZeitraumComposite> builderZ = getModule().queryBuilderFactory()
                .newQueryBuilder( RichtwertzoneZeitraumComposite.class );
        Iterator<RichtwertzoneZeitraumComposite> it = builderZ.newQuery( uow ).maxResults( Integer.MAX_VALUE )
                .iterator();
        int count = 0;
        while (it.hasNext()) {
            RichtwertzoneZeitraumComposite zeitraum = it.next();
            try {
                RichtwertzoneComposite zone = zeitraum.zone().get();
                if (zone == null) {
                    zeitraum.schl().set( "--- Zone fehlt ---" );
                }
                if (zeitraum.schl().get() == null && zone != null) {
                    zeitraum.schl().set( zone.schl().get() );
                }
                if (zeitraum.gueltigAb().get() == null) {
                    zeitraum.gueltigAb().set( new Date( 0 ) );
                }
            }
            catch (NoSuchEntityException nse) {
                log.error( "zone was removed, remove zeitraum too for " + zeitraum.id() );
                zeitraum.zone().set( null );
            }
        }
        log.info( "Migration of " + count + " broken RWZ Completed" );
        // }
    }


    private void migrateStaBu( Impl schlCreator, UnitOfWork uow )
            throws IOException {
        File file = new File( createDataDir(), "migration.stabuWohnung" );
        if (!file.exists()) {
            log.info( "Migrating Wohnung StaBu" );
            schlCreator.create( StockwerkStaBuComposite.class, "97", "Zweigeschossig" );
            {
                QueryBuilder<ImmobilienArtStaBuComposite> builder = getModule().queryBuilderFactory()
                        .newQueryBuilder( ImmobilienArtStaBuComposite.class );
                Query<ImmobilienArtStaBuComposite> query = builder.newQuery( uow ).maxResults( Integer.MAX_VALUE )
                        .firstResult( 0 );
                Iterator<ImmobilienArtStaBuComposite> it = query.iterator();
                int count = 0;
                while (it.hasNext()) {
                    ImmobilienArtStaBuComposite fs = it.next();
                    String schl = fs.schl().get();
                    if ("N".equals( schl )) {
                        fs.schl().set( "1" );
                        fs.name().set( "Neubau/Erstverkauf" );
                    }
                    else if ("G".equals( schl )) {
                        fs.schl().set( "2" );
                    }
                }
            }
            {
                QueryBuilder<StockwerkStaBuComposite> builder = getModule().queryBuilderFactory()
                        .newQueryBuilder( StockwerkStaBuComposite.class );
                Query<StockwerkStaBuComposite> query = builder.newQuery( uow ).maxResults( Integer.MAX_VALUE )
                        .firstResult( 0 );
                Iterator<StockwerkStaBuComposite> it = query.iterator();
                int count = 0;
                while (it.hasNext()) {
                    StockwerkStaBuComposite fs = it.next();
                    String schl = fs.schl().get();
                    if ("E".equals( schl )) {
                        fs.schl().set( "0" );
                    }
                    else if ("K".equals( schl )) {
                        fs.schl().set( "98" );
                    }
                    else if ("D".equals( schl )) {
                        fs.schl().set( "99" );
                    }
                    count++;
                }
            }
            file.createNewFile();
            log.info( "Migration of Wohnung StaBu Completed" );
        }
    }
}
