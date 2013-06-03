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

import java.io.File;

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
                StalaComposite.class, GemeindeComposite.class, GebaeudeArtComposite.class, NutzungComposite.class,
                StrasseComposite.class, BodennutzungComposite.class, FlurComposite.class, GemarkungComposite.class,
                RichtwertzoneComposite.class, RichtwertzoneZeitraumComposite.class,
                ErschliessungsBeitragComposite.class, BodenRichtwertKennungComposite.class,
                EntwicklungsZustandComposite.class, RichtwertZoneLageComposite.class,
                EntwicklungsZusatzComposite.class, BauweiseComposite.class, ArtDesBaugebietsComposite.class,
                FlurstueckComposite.class, GemeindeFaktorComposite.class, BodenwertAufteilungTextComposite.class,
                FlurstuecksdatenBaulandComposite.class, VertragsdatenErweitertComposite.class, KellerComposite.class,
                FlurstuecksdatenAgrarComposite.class, BelastungComposite.class, EtageComposite.class,
                AusstattungComposite.class, EigentumsartComposite.class, HimmelsrichtungComposite.class,
                WohnungseigentumComposite.class, GebaeudeComposite.class, FlurstueckWohneigentumComposite.class,
                AusstattungBewertungComposite.class, WohnungComposite.class, WohnungsTeileigentumComposite.class );

        // persistence: workspace/Lucene
        File root = new File( Polymap.getWorkspacePath().toFile(), "data" );

        File moduleRoot = new File( root, "org.polymap.kaps" );
        moduleRoot.mkdir();

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
        uow.complete();
        log.info( "Create Init Data Completed" );
    }


    private boolean isDBInitialized( UnitOfWork uow ) {
        QueryBuilder<ErschliessungsBeitragComposite> builder = getModule().queryBuilderFactory().newQueryBuilder(
                ErschliessungsBeitragComposite.class );
        Query<ErschliessungsBeitragComposite> query = builder.newQuery( uow ).maxResults( 1 ).firstResult( 0 );
        return query.iterator().hasNext();
    }

}
