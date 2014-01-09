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

import java.util.Iterator;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
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

import org.polymap.kaps.KapsPlugin;
import org.polymap.kaps.model.SchlNamedCreatorCallback.Impl;
import org.polymap.kaps.model.data.*;

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
                EntwicklungsZustandComposite.class, RichtwertZoneLageComposite.class,
                EntwicklungsZusatzComposite.class, BauweiseComposite.class, ArtDesBaugebietsComposite.class,
                FlurstueckComposite.class, GemeindeFaktorComposite.class, BodenwertAufteilungTextComposite.class,
                VertragsdatenBaulandComposite.class, VertragsdatenErweitertComposite.class, KellerComposite.class,
                VertragsdatenAgrarComposite.class, BelastungComposite.class, EtageComposite.class,
                AusstattungComposite.class, EigentumsartComposite.class, HimmelsrichtungComposite.class,
                WohnungseigentumComposite.class, GebaeudeComposite.class, AusstattungBewertungComposite.class,
                WohnungComposite.class, GebaeudeArtStaBuComposite.class, ArtDerBauflaecheStaBuComposite.class,
                ArtDesBaugebietesStalaComposite.class, GrundstuecksArtAgrarLandStalaComposite.class,
                ErwerberStalaComposite.class, GrundstuecksArtBaulandStalaComposite.class,
                KaeuferKreisStaBuComposite.class, VeraeussererAgrarLandStalaComposite.class,
                VeraeussererBaulandStalaComposite.class, VerwandschaftsVerhaeltnisStalaComposite.class,
                BodenRichtwertRichtlinieErgaenzungComposite.class,
                BodenRichtwertRichtlinieArtDerNutzungComposite.class, NHK2010AnbautenComposite.class,
                NHK2010BaupreisIndexComposite.class, NHK2010BewertungComposite.class, NHK2010BewertungGebaeudeComposite.class,
                ErmittlungModernisierungsgradComposite.class, ErtragswertverfahrenComposite.class,
                GebaeudeTypStaBuComposite.class, StockwerkStaBuComposite.class, ImmobilienArtStaBuComposite.class,
                WohnlageStaBuComposite.class );

        // persistence: workspace/Lucene
        File moduleRoot = createDataDir();

        domainModule.addServices( LuceneEntityStoreService.class )
                .setMetaInfo( new LuceneEntityStoreInfo( moduleRoot ) ).instantiateOnStartup()
                .identifiedBy( "lucene-repository" );

        // indexer
        domainModule.addServices( LuceneEntityStoreQueryService.class ).instantiateOnStartup();

        domainModule.addServices( HRIdentityGeneratorService.class );

        // additional services
        // domainModule.addServices( BiotopnummerGeneratorService.class )
        // .identifiedBy( "biotopnummer" );
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

        if (!isDBInitialized( uow )) {

            log.info( "Create Init Data" );

            final Impl schlCreator = new SchlNamedCreatorCallback.Impl( uow );
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

            final Impl schlCreator = new SchlNamedCreatorCallback.Impl( uow );
            GebaeudeArtStaBuComposite.Mixin.createInitData( schlCreator );
            ArtDerBauflaecheStaBuComposite.Mixin.createInitData( schlCreator );
            KaeuferKreisStaBuComposite.Mixin.createInitData( schlCreator );
            GebaeudeTypStaBuComposite.Mixin.createInitData( schlCreator );
            StockwerkStaBuComposite.Mixin.createInitData( schlCreator );
            ImmobilienArtStaBuComposite.Mixin.createInitData( schlCreator );
            WohnlageStaBuComposite.Mixin.createInitData( schlCreator );
        }
        migrateBelastung(uow);
        uow.complete();
        log.info( "Create Init Data Completed" );
    }


    private boolean isDBInitialized( UnitOfWork uow ) {
        QueryBuilder<ErschliessungsBeitragComposite> builder = getModule().queryBuilderFactory().newQueryBuilder(
                ErschliessungsBeitragComposite.class );
        Query<ErschliessungsBeitragComposite> query = builder.newQuery( uow ).maxResults( 1 ).firstResult( 0 );
        return query.iterator().hasNext();
    }


    private boolean isDBStaBuInitialized( UnitOfWork uow ) {
        QueryBuilder<GebaeudeArtStaBuComposite> builder = getModule().queryBuilderFactory().newQueryBuilder(
                GebaeudeArtStaBuComposite.class );
        Query<GebaeudeArtStaBuComposite> query = builder.newQuery( uow ).maxResults( 1 ).firstResult( 0 );
        return query.iterator().hasNext();
    }

    private void migrateBelastung( UnitOfWork uow ) throws IOException {
        File file = new File(createDataDir(), "migration.manyBelastung");
        if (!file.exists()) {
            log.info("Migrating Belastungen");
            QueryBuilder<FlurstueckComposite> builder = getModule().queryBuilderFactory().newQueryBuilder(
                    FlurstueckComposite.class );
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
            log.info("Migration of " + count + " Belastungen Completed");
        }
    }
}
