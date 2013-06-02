package org.polymap.kaps.importer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.composite.Composite;
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

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.data.*;
import org.polymap.kaps.ui.form.EingangsNummerFormatter;

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
        super( "Kaufpreissammlung importieren" );
        this.dbFile = dbFile;
        this.tableNames = tableNames;
        this.repo = KapsRepository.instance();
    }


    protected IStatus doExecute( IProgressMonitor monitor, IAdaptable info )
            throws Exception {
        monitor.beginTask( getLabel(), 12000 );
        final Database db = Database.open( dbFile );
        try {
            SubMonitor sub = null;

            sub = new SubMonitor( monitor, 10 );

            final Map<String, StalaComposite> allStalas = new HashMap<String, StalaComposite>();
            final Map<String, KaeuferKreisComposite> allKKreise = new HashMap<String, KaeuferKreisComposite>();
            final Map<String, VertragsArtComposite> allVertragsarten = new HashMap<String, VertragsArtComposite>();
            final Map<String, NutzungComposite> allNutzung = new HashMap<String, NutzungComposite>();
            final Map<String, BodennutzungComposite> allBodennutzung = new HashMap<String, BodennutzungComposite>();
            final Map<String, VertragComposite> allKaufvertrag = new HashMap<String, VertragComposite>();

            sub = new SubMonitor( monitor, 10 );
            importEntity( db, sub, StalaComposite.class, new EntityCallback<StalaComposite>() {

                @Override
                public void fillEntity( StalaComposite entity, Map<String, Object> builderRow ) {
                    // collecting
                    allStalas.put( entity.schl().get(), entity );
                }
            } );

            sub = new SubMonitor( monitor, 10 );
            importEntity( db, sub, VertragsArtComposite.class, new EntityCallback<VertragsArtComposite>() {

                @Override
                public void fillEntity( VertragsArtComposite entity, Map<String, Object> builderRow ) {
                    // associate stala
                    // Rückfrage welche STALAS verknüpft werden sollen
                    // SCHL aus STALA ist nicht eindeutig
                    // TODO [KAPS] #15
                    // collecting
                    allVertragsarten.put( entity.schl().get(), entity );
                }
            } );

            sub = new SubMonitor( monitor, 10 );
            importEntity( db, sub, KaeuferKreisComposite.class, new EntityCallback<KaeuferKreisComposite>() {

                @Override
                public void fillEntity( KaeuferKreisComposite entity, Map<String, Object> builderRow ) {
                    // associate stala erstellen
                    Object stalaSchl = builderRow.get( "STALA" );
                    if (stalaSchl != null) {
                        StalaComposite stala = allStalas.get( stalaSchl.toString() );
                        if (stala == null) {
                            throw new IllegalStateException( "no stala found for schl '" + stalaSchl
                                    + "' in K_KREIS_1 '" + entity.schl() + "'!" );
                        }
                        entity.stala().set( stala );
                    }
                    // collecting
                    allKKreise.put( entity.schl().get(), entity );
                }
            } );
            sub = new SubMonitor( monitor, 10 );
            importEntity( db, sub, VertragComposite.class, new EntityCallback<VertragComposite>() {

                @Override
                public void fillEntity( VertragComposite entity, Map<String, Object> builderRow ) {
                    // VERARBKZ
                    entity.fuerAuswertungGeeignet().set( getBooleanValue( builderRow, "VERARBKZ" ) );
                    // GESPLITTET
                    entity.gesplittet().set( getBooleanValue( builderRow, "GESPLITTET" ) );

                    // mapping eingangsNr
                    // find vertragsArt
                    entity.vertragsArt().set( find( allVertragsarten, builderRow, "VERTRAGART" ) );

                    // find KKreis für Käufer und Verkäufer
                    Object kkreisSchl = builderRow.get( "KKREIS" );
                    if (kkreisSchl != null) {
                        KaeuferKreisComposite kkreis = allKKreise.get( kkreisSchl );
                        if (kkreis == null) {
                            throw new IllegalStateException( "no KKREIS found for schl '" + kkreisSchl
                                    + "' in K_BUCH '" + entity.eingangsNr() + "'!" );
                        }
                        entity.kaeuferKreis().set( kkreis );
                    }
                    // find VKREIS
                    Object vkreisSchl = builderRow.get( "VKREIS" );
                    if (vkreisSchl != null) {
                        KaeuferKreisComposite vkreis = allKKreise.get( vkreisSchl );
                        if (vkreis == null) {
                            throw new IllegalStateException( "no VKREIS found for schl '" + vkreisSchl
                                    + "' in K_BUCH '" + entity.eingangsNr() + "'!" );
                        }
                        entity.verkaeuferKreis().set( vkreis );
                    }
                    // fix imports
                    if (builderRow.get( "KANTZ" ) == null) {
                        entity.kaufpreisAnteilZaehler().set( 1.0 );
                    }
                    if (builderRow.get( "KANTN" ) == null) {
                        entity.kaufpreisAnteilNenner().set( 1.0 );
                    }

                    String separator = System.getProperty( "line.separator" );
                    // BEM1 und BEM2 zusammenfassen
                    String bem1 = (String)builderRow.get( "BEM1" );
                    String bem2 = (String)builderRow.get( "BEM2" );
                    StringBuilder bem = new StringBuilder();
                    if (bem1 != null) {
                        bem.append( bem1 );
                        if (bem2 != null) {
                            bem.append( separator );
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
                            anfr.append( separator );
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

                    allKaufvertrag.put( entity.eingangsNr().get().toString(), entity );
                }
            } );
            sub = new SubMonitor( monitor, 10 );
            importEntity( db, sub, NutzungComposite.class, new EntityCallback<NutzungComposite>() {

                @Override
                public void fillEntity( NutzungComposite entity, Map<String, Object> builderRow ) {
                    entity.stala().set( find( allStalas, builderRow, "STALA" ) );
                    entity.isAgrar().set( getBooleanValue( builderRow, "AGRAR" ) );
                    allNutzung.put( entity.schl().get(), entity );
                }
            } );
            sub = new SubMonitor( monitor, 10 );
            final Map<String, GebaeudeArtComposite> allGebaeudeArt = new HashMap<String, GebaeudeArtComposite>();
            importEntity( db, sub, GebaeudeArtComposite.class, new EntityCallback<GebaeudeArtComposite>() {

                @Override
                public void fillEntity( GebaeudeArtComposite entity, Map<String, Object> builderRow ) {
                    allGebaeudeArt.put( entity.schl().get(), entity );
                }
            } );

            sub = new SubMonitor( monitor, 10 );
            final Map<String, GemeindeComposite> allGemeinde = new HashMap<String, GemeindeComposite>();
            importEntity( db, sub, GemeindeComposite.class, new EntityCallback<GemeindeComposite>() {

                @Override
                public void fillEntity( GemeindeComposite entity, Map<String, Object> builderRow ) {
                    allGemeinde.put( entity.schl().get(), entity );
                }
            } );
            final Map<String, StrasseComposite> allStrasse = new HashMap<String, StrasseComposite>();
            sub = new SubMonitor( monitor, 10 );
            importEntity( db, sub, StrasseComposite.class, new EntityCallback<StrasseComposite>() {

                @Override
                public void fillEntity( StrasseComposite entity, Map<String, Object> builderRow ) {
                    entity.gemeinde().set( find( allGemeinde, builderRow, "GEMEINDE" ) );
                    allStrasse.put( entity.schl().get(), entity );
                }
            } );
            sub = new SubMonitor( monitor, 10 );
            importEntity( db, sub, BodennutzungComposite.class, new EntityCallback<BodennutzungComposite>() {

                @Override
                public void fillEntity( BodennutzungComposite entity, Map<String, Object> builderRow ) {
                    // associate stala erstellen
                    entity.stala().set( find( allStalas, builderRow, "STALA" ) );
                    allBodennutzung.put( entity.schl().get(), entity );
                }
            } );
            final FlurComposite flur = repo.newEntity( FlurComposite.class, null, new EntityCreator<FlurComposite>() {

                public void create( FlurComposite prototype )
                        throws Exception {
                    prototype.schl().set( "000" );
                    prototype.name().set( "" );
                }
            } );
            repo.commitChanges();

            final Map<String, GemarkungComposite> allGemarkung = new HashMap<String, GemarkungComposite>();
            sub = new SubMonitor( monitor, 10 );
            importEntity( db, sub, GemarkungComposite.class, new EntityCallback<GemarkungComposite>() {

                @Override
                public void fillEntity( GemarkungComposite entity, Map<String, Object> builderRow ) {
                    entity.gemeinde().set( find( allGemeinde, builderRow, "GEMEINDE" ) );
                    entity.flur().set( flur );
                    allGemarkung.put( entity.schl().get(), entity );
                }
            } );

            sub = new SubMonitor( monitor, 10 );
            importEntity( db, sub, GemeindeFaktorComposite.class, new EntityCallback<GemeindeFaktorComposite>() {

                @Override
                public void fillEntity( GemeindeFaktorComposite entity, Map<String, Object> builderRow ) {
                    // alte Gemeinden können ignoriert werden, Leichen in der
                    // kaufdat.mdb
                    entity.gemeinde().set( find( allGemeinde, builderRow, "GEMEINDE", true ) );
                }
            } );

            // bodenrichtwertkennung laden
            final BodenRichtwertKennungComposite zonal = repo.findSchlNamed( BodenRichtwertKennungComposite.class, "1" );
            // erschließungsbeitrag laden
            final Map<String, ErschliessungsBeitragComposite> allErschliessungsbeitrag = repo
                    .entitiesWithSchl( ErschliessungsBeitragComposite.class );

            // RichtwertzoneLage auf 00
            final RichtwertZoneLageComposite richtwertZoneLageComposite = repo.findSchlNamed(
                    RichtwertZoneLageComposite.class, "00" );

            final Map<String, RichtwertzoneComposite> allRichtwertZone = new HashMap<String, RichtwertzoneComposite>();
            final Map<String, List<RichtwertzoneZeitraumComposite>> allRichtwertZoneGueltigkeit = new HashMap<String, List<RichtwertzoneZeitraumComposite>>();

            sub = new SubMonitor( monitor, 10 );
            final AnnotatedCompositeImporter richtwertzoneCompositeImporter = new AnnotatedCompositeImporter(
                    RichtwertzoneComposite.class, table( db, RichtwertzoneComposite.class ) );
            importEntity( db, sub, RichtwertzoneZeitraumComposite.class,
                    new EntityCallback<RichtwertzoneZeitraumComposite>() {

                        @Override
                        public void fillEntity( RichtwertzoneZeitraumComposite entity,
                                final Map<String, Object> builderRow )
                                throws Exception {

                            entity.erschliessungsBeitrag().set( find( allErschliessungsbeitrag, builderRow, "EB" ) );

                            String gemeinde = builderRow.get( "GEMEINDE" ).toString();
                            List<RichtwertzoneZeitraumComposite> list = allRichtwertZoneGueltigkeit.get( gemeinde );
                            if (list == null) {
                                list = new ArrayList<RichtwertzoneZeitraumComposite>();
                                allRichtwertZoneGueltigkeit.put( gemeinde, list );
                            }
                            list.add( entity );

                            // subcreate Richtwertzone
                            RichtwertzoneComposite zone = allRichtwertZone.get( entity.schl().get() );
                            if (zone == null) {
                                zone = repo.newEntity( RichtwertzoneComposite.class, null,
                                        new EntityCreator<RichtwertzoneComposite>() {

                                            public void create( RichtwertzoneComposite prototype )
                                                    throws Exception {

                                                richtwertzoneCompositeImporter.fillEntity( prototype, builderRow );
                                                prototype.gemeinde().set( find( allGemeinde, builderRow, "GEMEINDE" ) );
                                                prototype.nutzung().set( find( allNutzung, builderRow, "NUART" ) );
                                                prototype.bodenNutzung().set(
                                                        find( allBodennutzung, builderRow, "NUTZUNG" ) );
                                                prototype.lage().set( richtwertZoneLageComposite );
                                                prototype.bodenrichtwertKennung().set( zonal );
                                            }
                                        } );
                                allRichtwertZone.put( zone.schl().get(), zone );
                            }
                            // zone.gueltigkeiten().add( entity );
                            entity.zone().set( zone );
                        }
                    } );

            final Map<String, ArtDesBaugebietsComposite> allArtDesBaugebietes = repo
                    .entitiesWithSchl( ArtDesBaugebietsComposite.class );
            final Map<VertragComposite, FlurstueckComposite> allHauptflurstuecke = new HashMap<VertragComposite, FlurstueckComposite>();

            sub = new SubMonitor( monitor, 10 );
            importEntity( db, sub, FlurstueckComposite.class, new EntityCallback<FlurstueckComposite>() {

                @Override
                public void fillEntity( FlurstueckComposite entity, Map<String, Object> builderRow ) {
                    entity.gemarkung().set( find( allGemarkung, builderRow, "GEMARKUNG" ) );
                    entity.vertrag().set( find( allKaufvertrag, builderRow, "EINGANGSNR" ) );
                    entity.nutzung().set( find( allNutzung, builderRow, "NUTZUNG" ) );
                    entity.strasse().set( find( allStrasse, builderRow, "STRNR" ) );
                    entity.gebaeudeArt().set( find( allGebaeudeArt, builderRow, "GEBART" ) );
                    entity.flur().set( flur );
                    entity.artDesBaugebiets().set( find( allArtDesBaugebietes, builderRow, "BAUGEBART" ) );

                    Object hauptteil = builderRow.get( "HAUPTTEIL" );
                    if ("×".equals( hauptteil )) {
                        allHauptflurstuecke.put( entity.vertrag().get(), entity );
                    }

                    // RIZONE Zone und Jahr sind eindeutig
                    String zone = (String)builderRow.get( "RIZONE" );
                    String gemeinde = builderRow.get( "GEMEINDE" ).toString();
                    Date jahr = (Date)builderRow.get( "RIJAHR" );
                    //
                    RichtwertzoneZeitraumComposite found = null;
                    List<RichtwertzoneZeitraumComposite> zonen = allRichtwertZoneGueltigkeit.get( gemeinde );
                    for (RichtwertzoneZeitraumComposite richtwertzone : zonen) {
                        if (richtwertzone.schl().get().equals( zone ) && richtwertzone.gueltigAb().get().equals( jahr )) {
                            found = richtwertzone;
                            break;
                        }
                    }
                    if (found == null) {
                        throw new IllegalStateException( String.format( "no richtwertzone found for %s, %s, %s in %s",
                                zone, gemeinde, jahr, entity.vertrag().get().eingangsNr().get() ) );
                    }
                    entity.richtwertZone().set( found.zone().get() );
                    // entity.richtwertZoneG().set( found );
                }
            } );

            final Map<String, BodenwertAufteilungTextComposite> allBodenwertText = new HashMap<String, BodenwertAufteilungTextComposite>();

            sub = new SubMonitor( monitor, 10 );
            importEntity( db, sub, BodenwertAufteilungTextComposite.class,
                    new EntityCallback<BodenwertAufteilungTextComposite>() {

                        @Override
                        public void fillEntity( BodenwertAufteilungTextComposite entity, Map<String, Object> builderRow ) {
                            entity.strflaeche().set( getBooleanValue( builderRow, "STRFLAECHE" ) );
                            allBodenwertText.put( entity.schl().get(), entity );
                        }
                    } );

            final Map<String, KellerComposite> allKeller = repo.entitiesWithSchl( KellerComposite.class );

            File importfehler = new File( "importfehler.txt" );
            final BufferedWriter w = new BufferedWriter( new FileWriter( importfehler ) );
            sub = new SubMonitor( monitor, 10 );
            final AnnotatedCompositeImporter vertragsdatenErweitertCompositeImporter = new AnnotatedCompositeImporter(
                    VertragsdatenErweitertComposite.class, db.getTable( "K_BEVERW" ) );
            importEntity( db, sub, FlurstuecksdatenBaulandComposite.class,
                    new EntityCallback<FlurstuecksdatenBaulandComposite>() {

                        @Override
                        public void fillEntity( FlurstuecksdatenBaulandComposite entity,
                                final Map<String, Object> builderRow )
                                throws Exception {
                            // defaults
                            if (entity.sanierungswert() == null) {
                                entity.sanierungswert().set( "U" );
                            }
                            if (entity.ausstattung() == null) {
                                entity.ausstattung().set( Integer.valueOf( 6 ) );
                            }
                            // Object value = builderRow.get( "SANANFEND" );
                            // entity.sanierungAnfangswert()
                            // .set( value != null && "A".equalsIgnoreCase(
                            // value.toString() ) );
                            // entity.gebaeudeArt().set( find(allGebaeudeArt,
                            // builderRow, "GEBART") );
                            VertragComposite vertrag = repo.findEntity( VertragComposite.class,
                                    find( allKaufvertrag, builderRow, "EINGANGSNR" ).id() );
                            entity.vertrag().set( vertrag );
                            // entity.erbbauRecht2().set( (String)builderRow.get(
                            // "ERBBAU" ) );
                            entity.faktorFuerMarktanpassungGeeignet().set( getBooleanValue( builderRow, "MARKTANP" ) );
                            entity.gfzBereinigtenBodenpreisVerwenden().set(
                                    getBooleanValue( builderRow, "GFZVERWENDEN" ) );
                            // entity.denkmalschutz().set( (String)builderRow.get(
                            // "Denkmalschutz" ) );
                            entity.bodenwertAufteilung1().set( find( allBodenwertText, builderRow, "BODWTEXT1", true ) );
                            entity.bodenwertAufteilung2().set( find( allBodenwertText, builderRow, "BODWTEXT2", true ) );
                            entity.bodenwertAufteilung3().set( find( allBodenwertText, builderRow, "BODWTEXT3", true ) );
                            entity.bodennutzung().set( find( allBodennutzung, builderRow, "BONUTZ" ) );
                            entity.erschliessungsBeitrag().set( find( allErschliessungsbeitrag, builderRow, "EB" ) );
                            entity.fuerBodenwertaufteilungNichtGeeignet().set(
                                    getBooleanValue( builderRow, "BODWNICHT" ) );

                            // entity.sanierung().set( (String)builderRow.get( "SAN"
                            // ) );
                            entity.bereinigterBodenpreisMitNachkommastellen().set(
                                    getBooleanValue( builderRow, "GFZKOMMA" ) );
                            entity.keller().set( find( allKeller, builderRow, "KELLER" ) );

                            // autoimport
                            if (Integer.valueOf( 5 ).equals( entity.ausstattung().get() )) {
                                entity.ausstattung().set( 4 );
                            }
                            // RIZONE
                            String zone = (String)builderRow.get( "RIZONE" );
                            String gemeinde = builderRow.get( "GEMEINDE" ).toString();
                            Date jahr = (Date)builderRow.get( "RIJAHR" );
                            //
                            RichtwertzoneZeitraumComposite found = null;
                            List<RichtwertzoneZeitraumComposite> zonen = allRichtwertZoneGueltigkeit.get( gemeinde );
                            for (RichtwertzoneZeitraumComposite richtwertzone : zonen) {
                                if (richtwertzone.schl().get().equals( zone )
                                        && richtwertzone.gueltigAb().get().equals( jahr )) {
                                    found = richtwertzone;
                                    break;
                                }
                            }
                            if (found == null) {
                                throw new IllegalStateException( String.format(
                                        "no richtwertzone found for %s, %s, %s in %s", zone, gemeinde, jahr, entity
                                                .vertrag().get().eingangsNr().get() ) );
                            }
                            // entity.richtwertZone().set( found.zone().get() );
                            entity.richtwertZoneG().set( found );

                            // Flurstück setzen, bisher Hauptflurstück, ab jetzt je
                            // Flurstück einmal
                            // erweiterte Daten
                            FlurstueckComposite flurstueck = allHauptflurstuecke.get( entity.vertrag().get() );
                            if (flurstueck == null) {
                                throw new IllegalStateException( String.format(
                                        "no flurstueck found for FlurstuecksdatenBauland for vertrag %s", entity
                                                .vertrag().get().eingangsNr().get() ) );
                            }
                            entity.flurstueck().set( flurstueck );

                            // checken ob Flurstück tatsächlich agrar ist, in der DB
                            // ist ganz schöner Mist drin
                            //
                            if (!flurstueck.nutzung().get().isAgrar().get()) {
                                // subcreate VertragsdatenErweitert
                                // in der Tabelle K_BEVERW sind Vertrags- und
                                // Flurstücksdaten, letztere werden hier separat
                                // erzeugt
                                VertragsdatenErweitertComposite vdec = repo.newEntity(
                                        VertragsdatenErweitertComposite.class, null,
                                        new EntityCreator<VertragsdatenErweitertComposite>() {

                                            public void create( VertragsdatenErweitertComposite prototype )
                                                    throws Exception {
                                                vertragsdatenErweitertCompositeImporter.fillEntity( prototype,
                                                        builderRow );
                                            }
                                        } );
                                vertrag.eingangsNr().get();
                                vertrag.erweiterteVertragsdaten().set( vdec );
                            }
                            else {
                                log.error( String.format(
                                        "Flurstück ist AGRAR müsste aber Bauland sein für Vertrag %s mit Nutzung %s",
                                        entity.vertrag().get().eingangsNr().get(), flurstueck.nutzung().get().schl()
                                                .get() ) );
                                w.write( String.format(
                                        "Flurstück ist AGRAR müsste aber Bauland sein für Vertrag %s mit Nutzung %s\n",
                                        entity.vertrag().get().eingangsNr().get(), flurstueck.nutzung().get().schl()
                                                .get() ) );
                            }
                        }
                    } );

            final AnnotatedCompositeImporter vertragsdatenErweitertAgrarCompositeImporter = new AnnotatedCompositeImporter(
                    VertragsdatenErweitertComposite.class, db.getTable( "K_BEVERL" ) );

            sub = new SubMonitor( monitor, 10 );
            importEntity( db, sub, FlurstuecksdatenAgrarComposite.class,
                    new EntityCallback<FlurstuecksdatenAgrarComposite>() {

                        @Override
                        public void fillEntity( FlurstuecksdatenAgrarComposite entity,
                                final Map<String, Object> builderRow )
                                throws Exception {
                            // // defaults
                            // if (entity.sanierungswert() == null) {
                            // entity.sanierungswert().set( "U" );
                            // }
                            // if (entity.ausstattung() == null) {
                            // entity.ausstattung().set( Integer.valueOf( 6 ) );
                            // }
                            // Object value = builderRow.get( "SANANFEND" );
                            // entity.sanierungAnfangswert()
                            // .set( value != null && "A".equalsIgnoreCase(
                            // value.toString() ) );
                            entity.gebaeudeArt().set( find( allGebaeudeArt, builderRow, "GEBART" ) );
                            VertragComposite vertrag = repo.findEntity( VertragComposite.class,
                                    find( allKaufvertrag, builderRow, "EINGANGSNR" ).id() );
                            entity.vertrag().set( vertrag );
                            // entity.erbbauRecht2().set( (String)builderRow.get(
                            // "ERBBAU" ) );
                            entity.zurRichtwertermittlungGeeignet().set( getBooleanValue( builderRow, "RIWEGEEIGNET" ) );
                            entity.istBebaut().set( getBooleanValue( builderRow, "bebaut" ) );
                            entity.fuerStatistikGeeignet().set( getBooleanValue( builderRow, "VERARBKZ" ) );

                            // entity.richtwertZone().set( found.zone().get() );
                            entity.richtwertZone1().set(
                                    findRichtwertZone( allRichtwertZoneGueltigkeit, entity, builderRow, "1" ) );
                            entity.richtwertZone2().set(
                                    findRichtwertZone( allRichtwertZoneGueltigkeit, entity, builderRow, "2" ) );
                            entity.richtwertZone3().set(
                                    findRichtwertZone( allRichtwertZoneGueltigkeit, entity, builderRow, "3" ) );
                            entity.richtwertZone4().set(
                                    findRichtwertZone( allRichtwertZoneGueltigkeit, entity, builderRow, "4" ) );
                            entity.richtwertZone5().set(
                                    findRichtwertZone( allRichtwertZoneGueltigkeit, entity, builderRow, "5" ) );
                            entity.richtwertZone6().set(
                                    findRichtwertZone( allRichtwertZoneGueltigkeit, entity, builderRow, "6" ) );
                            entity.bodennutzung1().set( find( allBodennutzung, builderRow, "BONU1" ) );
                            entity.bodennutzung2().set( find( allBodennutzung, builderRow, "BONU2" ) );
                            entity.bodennutzung3().set( find( allBodennutzung, builderRow, "BONU3" ) );
                            entity.bodennutzung4().set( find( allBodennutzung, builderRow, "BONU4" ) );
                            entity.bodennutzung5().set( find( allBodennutzung, builderRow, "BONU5" ) );
                            entity.bodennutzung6().set( find( allBodennutzung, builderRow, "BONU6" ) );

                            String separator = System.getProperty( "line.separator" );
                            // BEM1 und BEM2 zusammenfassen
                            String bem1 = (String)builderRow.get( "BEM1" );
                            String bem2 = (String)builderRow.get( "BEM2" );
                            StringBuilder bem = new StringBuilder();
                            if (bem1 != null) {
                                bem.append( bem1 );
                                if (bem2 != null) {
                                    bem.append( separator );
                                }
                            }
                            if (bem2 != null) {
                                bem.append( bem2 );
                            }
                            entity.bemerkungen().set( bem.toString() );

                            // Flurstück setzen, bisher Hauptflurstück, ab jetzt je
                            // Flurstück einmal
                            // erweiterte Daten
                            FlurstueckComposite flurstueck = allHauptflurstuecke.get( entity.vertrag().get() );
                            if (flurstueck == null) {
                                throw new IllegalStateException( String.format(
                                        "no flurstueck found for FlurstuecksdatenAgrar for vertrag %s", entity
                                                .vertrag().get().eingangsNr().get() ) );
                            }
                            entity.flurstueck().set( flurstueck );

                            // checken ob Flurstück tatsächlich agrar ist, in der DB
                            // ist ganz schöner Mist drin
                            //
                            if (flurstueck.nutzung().get().isAgrar().get()) {
                                // subcreate VertragsdatenErweitert
                                // in der Tabelle K_BEVERW sind Vertrags- und
                                // Flurstücksdaten, letztere werden hier separate
                                // erzeugt
                                VertragsdatenErweitertComposite vdec = repo.newEntity(
                                        VertragsdatenErweitertComposite.class, null,
                                        new EntityCreator<VertragsdatenErweitertComposite>() {

                                            public void create( VertragsdatenErweitertComposite prototype )
                                                    throws Exception {
                                                vertragsdatenErweitertAgrarCompositeImporter.fillEntity( prototype,
                                                        builderRow );
                                            }
                                        } );
                                vertrag.eingangsNr().get();
                                vertrag.erweiterteVertragsdaten().set( vdec );
                            }
                            else {
                                log.error( String.format(
                                        "Flurstück ist Bauland müsste aber AGRAR sein für Vertrag %s", entity.vertrag()
                                                .get().eingangsNr().get() ) );
                                w.write( String.format(
                                        "Flurstück ist Bauland müsste aber AGRAR sein für Vertrag %s mit Nutzung %s\n",
                                        entity.vertrag().get().eingangsNr().get(), flurstueck.nutzung().get().schl()
                                                .get() ) );
                            }
                        }


                        private RichtwertzoneZeitraumComposite findRichtwertZone(
                                final Map<String, List<RichtwertzoneZeitraumComposite>> allRichtwertZoneGueltigkeit,
                                FlurstuecksdatenAgrarComposite entity, final Map<String, Object> builderRow,
                                String number )
                                throws IOException {
                            // RIZONE
                            String zone = (String)builderRow.get( "RIZO" + number );
                            if (zone != null) {
                                String gemeinde = builderRow.get( "RIZOGEM" + number ).toString();
                                Date jahr = (Date)builderRow.get( "RIZOJAHR" + number );
                                //
                                return MdbImportOperation.this.findRichtwertZone( w, allRichtwertZoneGueltigkeit, zone,
                                        gemeinde, jahr, FlurstuecksdatenAgrarComposite.class,
                                        EingangsNummerFormatter.format( entity.vertrag().get().eingangsNr().get() ) );
                            }
                            return null;
                        }
                    } );

            /**
             * WOHNUNGEN
             */
            sub = new SubMonitor( monitor, 10 );
            final Map<String, BelastungComposite> allBelastungArt = new HashMap<String, BelastungComposite>();
            importEntity( db, sub, BelastungComposite.class, new EntityCallback<BelastungComposite>() {

                @Override
                public void fillEntity( BelastungComposite entity, Map<String, Object> builderRow ) {
                    allBelastungArt.put( entity.schl().get(), entity );
                }
            } );
            sub = new SubMonitor( monitor, 10 );
            final Map<String, EtageComposite> allEtageArt = new HashMap<String, EtageComposite>();
            importEntity( db, sub, EtageComposite.class, new EntityCallback<EtageComposite>() {

                @Override
                public void fillEntity( EtageComposite entity, Map<String, Object> builderRow ) {
                    allEtageArt.put( entity.schl().get(), entity );
                }
            } );
            sub = new SubMonitor( monitor, 10 );
            final Map<String, AusstattungComposite> allAusstattung = new HashMap<String, AusstattungComposite>();
            importEntity( db, sub, AusstattungComposite.class, new EntityCallback<AusstattungComposite>() {

                @Override
                public void fillEntity( AusstattungComposite entity, Map<String, Object> builderRow ) {
                    allAusstattung.put( entity.schl().get(), entity );
                }
            } );
            sub = new SubMonitor( monitor, 10 );
            final Map<String, EigentumsartComposite> allEigentumsArt = new HashMap<String, EigentumsartComposite>();
            importEntity( db, sub, EigentumsartComposite.class, new EntityCallback<EigentumsartComposite>() {

                @Override
                public void fillEntity( EigentumsartComposite entity, Map<String, Object> builderRow ) {
                    allEigentumsArt.put( entity.schl().get(), entity );
                }
            } );
            sub = new SubMonitor( monitor, 10 );
            final Map<String, HimmelsrichtungComposite> allHimmelsrichtung = new HashMap<String, HimmelsrichtungComposite>();
            importEntity( db, sub, HimmelsrichtungComposite.class, new EntityCallback<HimmelsrichtungComposite>() {

                @Override
                public void fillEntity( HimmelsrichtungComposite entity, Map<String, Object> builderRow ) {
                    allHimmelsrichtung.put( entity.schl().get(), entity );
                }
            } );
            sub = new SubMonitor( monitor, 10 );
            final Map<String, WohnungseigentumComposite> allWohnungseigentum = new HashMap<String, WohnungseigentumComposite>();
            importEntity( db, sub, WohnungseigentumComposite.class, new EntityCallback<WohnungseigentumComposite>() {

                @Override
                public void fillEntity( WohnungseigentumComposite entity, Map<String, Object> builderRow ) {

                    // LONG to Double

                    Integer flaeche = (Integer)builderRow.get( "GFLAECHE_AKT" );
                    if (flaeche != null) {
                        entity.gesamtFlaeche().set( Double.valueOf( flaeche.doubleValue() ) );
                    }
                    String separator = System.getProperty( "line.separator" );
                    // BEM1 und BEM2 zusammenfassen
                    String bem1 = (String)builderRow.get( "BEMERKUNG" );
                    String bem2 = (String)builderRow.get( "BEMERKUNG1" );
                    StringBuilder bem = new StringBuilder();
                    if (bem1 != null) {
                        bem.append( bem1 );
                        if (bem2 != null) {
                            bem.append( separator );
                        }
                    }
                    if (bem2 != null) {
                        bem.append( bem2 );
                    }
                    entity.bemerkungen().set( bem.toString() );
                    allWohnungseigentum.put( entity.schl().get(), entity );
                }
            } );
            sub = new SubMonitor( monitor, 10 );
            final Map<String, GebaeudeComposite> allGebaeude = new HashMap<String, GebaeudeComposite>();
            importEntity( db, sub, GebaeudeComposite.class, new EntityCallback<GebaeudeComposite>() {

                @Override
                public void fillEntity( GebaeudeComposite entity, Map<String, Object> builderRow ) {
                    if (entity.sanierungswert().get() == null) {
                        entity.sanierungswert().set( "U" );
                    }
                    entity.gebaeudeArt().set( find( allGebaeudeArt, builderRow, "GEBART" ) );

                    allGebaeude.put( entity.schl().get(), entity );
                }
            } );

            sub = new SubMonitor( monitor, 10 );
            final Map<String, FlurstueckWohneigentumComposite> allFlurstueckWohneigentum = new HashMap<String, FlurstueckWohneigentumComposite>();
            importEntity( db, sub, FlurstueckWohneigentumComposite.class,
                    new EntityCallback<FlurstueckWohneigentumComposite>() {

                        @Override
                        public void fillEntity( FlurstueckWohneigentumComposite entity, Map<String, Object> builderRow )
                                throws IOException {
                            entity.gemarkung().set( find( allGemarkung, builderRow, "GEM" ) );
                            entity.nutzung().set( find( allNutzung, builderRow, "NUTZUNG" ) );
                            entity.strasse().set( find( allStrasse, builderRow, "STRNR" ) );
                            entity.flur().set( flur );
                            entity.belastung().set( find( allBelastungArt, builderRow, "BELASTUNG" ) );

                            // RIZONE Zone und Jahr sind eindeutig
                            String zone = (String)builderRow.get( "RIZONE" );
                            String gemeinde = builderRow.get( "GEMEINDE" ).toString();
                            Date jahr = (Date)builderRow.get( "RIJAHR" );
                            //
                            try {
                                RichtwertzoneZeitraumComposite found = MdbImportOperation.this.findRichtwertZone( w,
                                        allRichtwertZoneGueltigkeit, zone, gemeinde, jahr,
                                        FlurstueckWohneigentumComposite.class, entity.schl().get() + "_"
                                                + entity.gemarkung().get().name().get() );
                                entity.richtwertZone().set( found.zone().get() );
                            }
                            catch (Exception e) {
                                // weitermachen und importieren, die suche stimmt,
                                // aber die Daten sind Mist
                            }

                            allFlurstueckWohneigentum.put( entity.schl().get(), entity );
                        }
                    } );
            sub = new SubMonitor( monitor, 10 );
            importEntity( db, sub, WohnungComposite.class, new EntityCallback<WohnungComposite>() {

                @Override
                public void fillEntity( WohnungComposite entity, Map<String, Object> builderRow )
                        throws IOException {

                    Double eingangsnummer = (Double)builderRow.get( "EINGANGSNR" );
                    if (eingangsnummer != null) {
                        entity.eingangsNummer().set( eingangsnummer.intValue() );
                    }
                    Integer abschl = (Integer)builderRow.get( "BEBABSCHL" );
                    if (abschl != null) {
                        entity.bebauungsabschlagInProzent().set( abschl.doubleValue() );
                    }

                    entity.mitBebauungsabschlag().set( getBooleanValue( builderRow, "BEBAB" ) );
                    entity.geeignet().set( getBooleanValue( builderRow, "VERWERTEN" ) );
                    entity.schaetzungGarage().set( getBooleanValue( builderRow, "SCHAETZGA" ) );
                    entity.schaetzungStellplatz().set( getBooleanValue( builderRow, "SCHAETZST" ) );
                    entity.schaetzungAnderes().set( getBooleanValue( builderRow, "SCHAETZNE" ) );
                    entity.eingabeGesamtMiete().set( getBooleanValue( builderRow, "GESMIETE_EING" ) );
                    entity.tatsaechlicheMieteVerwenden().set( getBooleanValue( builderRow, "MIETE_TATS" ) );
                    entity.garagenBeiLiegenschaftszinsBeruecksichtigen().set(
                            getBooleanValue( builderRow, "LIZI_GARAGE" ) );
                    entity.zurAuswertungGeeignet().set( getBooleanValue( builderRow, "VERARBKZ" ) );

                    entity.ausstattung().set( find( allAusstattung, builderRow, "BEWSCHL" ) );
                    entity.eigentumsArt().set( find( allEigentumsArt, builderRow, "EIGENTART" ) );
                    entity.etage().set( find( allEtageArt, builderRow, "GESCHOSS" ) );
                    entity.himmelsrichtung().set( find( allHimmelsrichtung, builderRow, "HIMMELSRI" ) );
                    entity.gebaeudeArtGarage().set( find( allGebaeudeArt, builderRow, "GEBARTG" ) );
                    entity.gebaeudeArtStellplatz().set( find( allGebaeudeArt, builderRow, "GEBARTS" ) );
                    entity.gebaeudeArtAnderes().set( find( allGebaeudeArt, builderRow, "GEBARTN" ) );

                    String separator = System.getProperty( "line.separator" );
                    // BEM1 und BEM2 zusammenfassen
                    String bem1 = (String)builderRow.get( "BEMERKUNG3" );
                    String bem2 = (String)builderRow.get( "BEMERKUNG" );
                    StringBuilder bem = new StringBuilder();
                    if (bem1 != null) {
                        bem.append( bem1 );
                        if (bem2 != null) {
                            bem.append( separator );
                        }
                    }
                    if (bem2 != null) {
                        bem.append( bem2 );
                    }
                    entity.bemerkung().set( bem.toString() );

                    // flurstück finden

                    String flurstueckNummer = entity.objektNummer().get() + "/" + entity.objektFortfuehrung().get()
                            + "/" + entity.gebaeudeNummer().get() + "/" + entity.gebaeudeFortfuehrung().get() + "/"
                            + builderRow.get( "FLSTNR" ) + "/" + builderRow.get( "FLSTNRU" );
                    FlurstueckWohneigentumComposite flurstueck = allFlurstueckWohneigentum.get( flurstueckNummer );
                    if (flurstueck == null) {
                        w.write( String.format( "Kein Flurstück in Gebäude gefunden für %s in %s:%s\n",
                                flurstueckNummer, WohnungComposite.class, entity.schl().get() ) );
                    }
                    else {
                        entity.flurstueck().set( flurstueck );
                    }
                }
            } );

            sub = new SubMonitor( monitor, 10 );
            importEntity( db, sub, AusstattungBewertungComposite.class,
                    new EntityCallback<AusstattungBewertungComposite>() {

                        @Override
                        public void fillEntity( AusstattungBewertungComposite entity, Map<String, Object> builderRow ) {
                            entity.ME11().set( getBooleanValue( builderRow, "ME11" ) );
                            entity.ME12().set( getBooleanValue( builderRow, "ME12" ) );
                            entity.ME13().set( getBooleanValue( builderRow, "ME13" ) );
                            entity.ME14().set( getBooleanValue( builderRow, "ME14" ) );
                            entity.ME21().set( getBooleanValue( builderRow, "ME21" ) );
                            entity.ME22().set( getBooleanValue( builderRow, "ME22" ) );
                            entity.ME23().set( getBooleanValue( builderRow, "ME23" ) );
                            entity.ME31().set( getBooleanValue( builderRow, "ME31" ) );
                            entity.ME32().set( getBooleanValue( builderRow, "ME32" ) );
                            entity.ME33().set( getBooleanValue( builderRow, "ME33" ) );
                            entity.ME34().set( getBooleanValue( builderRow, "ME34" ) );
                            entity.ME35().set( getBooleanValue( builderRow, "ME35" ) );
                            entity.ME36().set( getBooleanValue( builderRow, "ME36" ) );
                            entity.ME37().set( getBooleanValue( builderRow, "ME37" ) );
                            entity.ME372().set( getBooleanValue( builderRow, "ME372" ) );
                            entity.ME38().set( getBooleanValue( builderRow, "ME38" ) );
                            entity.ME39().set( getBooleanValue( builderRow, "ME39" ) );
                            entity.ME41().set( getBooleanValue( builderRow, "ME41" ) );
                            entity.ME42().set( getBooleanValue( builderRow, "ME42" ) );
                            entity.ME43().set( getBooleanValue( builderRow, "ME43" ) );
                            entity.ME51().set( getBooleanValue( builderRow, "ME51" ) );
                            entity.ME52().set( getBooleanValue( builderRow, "ME52" ) );
                            entity.ME53().set( getBooleanValue( builderRow, "ME53" ) );
                            entity.ME15().set( getBooleanValue( builderRow, "ME15" ) );
                            entity.ME24().set( getBooleanValue( builderRow, "ME24" ) );
                            entity.ME44().set( getBooleanValue( builderRow, "ME44" ) );
                            entity.ME45().set( getBooleanValue( builderRow, "ME45" ) );
                            entity.ME46().set( getBooleanValue( builderRow, "ME46" ) );
                            entity.ME32A().set( getBooleanValue( builderRow, "ME32A" ) );
                            entity.ME33A().set( getBooleanValue( builderRow, "ME33A" ) );
                            entity.ME54().set( getBooleanValue( builderRow, "ME54" ) );
                            entity.ME61().set( getBooleanValue( builderRow, "ME61" ) );
                            entity.ME62().set( getBooleanValue( builderRow, "ME62" ) );
                            entity.ME63().set( getBooleanValue( builderRow, "ME63" ) );
                            entity.ME64().set( getBooleanValue( builderRow, "ME64" ) );
                            entity.ME71().set( getBooleanValue( builderRow, "ME71" ) );
                            entity.ME72().set( getBooleanValue( builderRow, "ME72" ) );
                            entity.ME73().set( getBooleanValue( builderRow, "ME73" ) );
                            entity.NEU().set( getBooleanValue( builderRow, "NEU" ) );
                        }
                    } );
            sub = new SubMonitor( monitor, 10 );
            importEntity( db, sub, WohnungsTeileigentumComposite.class,
                    new EntityCallback<WohnungsTeileigentumComposite>() {

                        @Override
                        public void fillEntity( WohnungsTeileigentumComposite entity, Map<String, Object> builderRow ) {
                            entity.gebaeudeArt().set( find( allGebaeudeArt, builderRow, "TEBEZ" ) );
                        }
                    } );

            w.flush();
            w.close();
            log.error( "WRITTEN LOG TO FILE: " + importfehler.getAbsolutePath() );
            allGebaeude.clear();
            allWohnungseigentum.clear();
            allHimmelsrichtung.clear();
            allEigentumsArt.clear();
            allAusstattung.clear();
            allEtageArt.clear();
            allBelastungArt.clear();
            allKeller.clear();
            allBodenwertText.clear();
            allArtDesBaugebietes.clear();
            allBodennutzung.clear();
            allErschliessungsbeitrag.clear();
            allGebaeudeArt.clear();
            allGemarkung.clear();
            allGemeinde.clear();
            allKaufvertrag.clear();
            allKKreise.clear();
            allNutzung.clear();
            allRichtwertZoneGueltigkeit.clear();
            allStalas.clear();
            allStrasse.clear();
            allVertragsarten.clear();
        }
        finally {
            db.close();
        }

        return Status.OK_STATUS;
    }


    protected final RichtwertzoneZeitraumComposite findRichtwertZone( BufferedWriter w,
            Map<String, List<RichtwertzoneZeitraumComposite>> allRichtwertZoneGueltigkeit, String zone,
            String gemeinde, Date jahr, Class src, String id )
            throws IOException {
        RichtwertzoneZeitraumComposite found = null;
        List<RichtwertzoneZeitraumComposite> zonen = allRichtwertZoneGueltigkeit.get( gemeinde );
        for (RichtwertzoneZeitraumComposite richtwertzone : zonen) {
            if (richtwertzone.schl().get().equals( zone ) && richtwertzone.gueltigAb().get().equals( jahr )) {
                found = richtwertzone;
                break;
            }
        }
        if (found == null) {
            // versuche ein Jahr älter zu finden falls eine
            // Zone nicht gefunden wird.
            Date newYear = new Date( jahr.getTime() );
            // bis 2000 versuchen
            while (found == null && newYear.getYear() >= 100) {
                w.write( String.format(
                        "Keine Richtwertzone gefunden für %s, %s, %s in %s für ID %s, versuche 1 Jahr früher\n", src,
                        zone, gemeinde, newYear, id ) );
                newYear.setYear( newYear.getYear() - 1 );

                for (RichtwertzoneZeitraumComposite richtwertzone : zonen) {
                    if (richtwertzone.schl().get().equals( zone ) && richtwertzone.gueltigAb().get().equals( newYear )) {
                        found = richtwertzone;
                        break;
                    }
                }
            }
        }
        if (found == null) {
            // versuche ohne Gemeinde
            w.write( String.format(
                    "Keine Richtwertzone gefunden für %s, %s, %s in %s für ID %s, versuche über alle Gemeinden\n", src,
                    zone, gemeinde, jahr, id ) );
            for (List<RichtwertzoneZeitraumComposite> alleZonen : allRichtwertZoneGueltigkeit.values()) {
                for (RichtwertzoneZeitraumComposite richtwertzone : alleZonen) {
                    if (richtwertzone.schl().get().equals( zone ) && richtwertzone.gueltigAb().get().equals( jahr )) {
                        found = richtwertzone;
                        break;
                    }
                }
            }
        }
        if (found == null) {
            // versuche ein Jahr älter zu finden und ohne
            // Gemeinde
            Date newYear = new Date( jahr.getTime() );
            // bis 2000 versuchen
            while (found == null && newYear.getYear() >= 100) {
                w.write( String
                        .format(
                                "Keine Richtwertzone gefunden für %s, %s, %s in %s für ID %s, versuche über alle Gemeinden und 1 Jahr früher\n",
                                src, zone, gemeinde, newYear, id ) );
                newYear.setYear( newYear.getYear() - 1 );
                for (List<RichtwertzoneZeitraumComposite> alleZonen : allRichtwertZoneGueltigkeit.values()) {
                    for (RichtwertzoneZeitraumComposite richtwertzone : alleZonen) {
                        if (richtwertzone.schl().get().equals( zone )
                                && richtwertzone.gueltigAb().get().equals( newYear )) {
                            found = richtwertzone;
                            break;
                        }
                    }
                }
            }
        }
        if (found == null) {
            w.write( String.format(
                    "Keine Richtwertzone gefunden für %s, %s in %s für ID %s, suche nur nach Zonennummer\n", src, zone,
                    gemeinde, id ) );
            for (List<RichtwertzoneZeitraumComposite> alleZonen : allRichtwertZoneGueltigkeit.values()) {
                for (RichtwertzoneZeitraumComposite richtwertzone : alleZonen) {
                    if (richtwertzone.schl().get().equals( zone )) {
                        found = richtwertzone;
                        break;
                    }
                }

            }
        }
        if (found == null) {
            throw new IllegalStateException( String.format(
                    "Keine Richtwertzone gefunden für %s, %s, %s in %s für ID %s\n", src, zone, gemeinde, jahr, id ) );
        }
        return found;
    }


    protected <T extends Composite> T find( Map<String, T> all, Map<String, Object> row, String columnName ) {
        return find( all, row, columnName, false );
    }


    protected <T extends Composite> T find( Map<String, T> all, Map<String, Object> row, String columnName,
            boolean nullAllowed ) {
        Object schl = row.get( columnName );

        // schl ist mal String mal Integer und mal Double
        if (schl != null) {
            if (schl instanceof Double) {
                schl = (Integer)((Double)schl).intValue();
            }
            String schlStr = schl.toString();
            if (!schlStr.isEmpty()) {
                T obj = all.get( schlStr );
                if (obj == null && !nullAllowed) {
                    throw new IllegalStateException( "no " + columnName + " found for schl '" + schl + "'!" );
                }
                return obj;
            }
        }
        return null;
    }


    protected Boolean getBooleanValue( Map<String, Object> builderRow, String rowName ) {
        Object value = builderRow.get( rowName );
        return (value == null || "".equals( value ) || "N".equalsIgnoreCase( value.toString() )) ? Boolean.FALSE
                : Boolean.TRUE;
    }


    interface EntityCallback<T extends EntityComposite> {

        void fillEntity( T prototype, Map<String, Object> builderRow )
                throws Exception;
    }


    protected <T extends EntityComposite> void importEntity( Database db, IProgressMonitor monitor, Class<T> type,
            final EntityCallback<T> callback )
            throws Exception {
        Table table = table( db, type );
        monitor.beginTask( "Tabelle: " + table.getName(), table.getRowCount() );

        final AnnotatedCompositeImporter importer = new AnnotatedCompositeImporter( type, table );

        // data rows
        Map<String, Object> row = null;
        int count = 0;
        while ((row = table.getNextRow()) != null) {
            final Map<String, Object> builderRow = row;

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
            if ((++count % 200) == 0) {
                monitor.worked( 200 );
                monitor.setTaskName( "Objekte: " + count );
                repo.commitChanges();
            }
        }
        repo.commitChanges();
        log.info( "Imported and committed: " + type + " -> " + count );
        monitor.done();
    }


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


    private Object columnValue( Table table, Map<String, Object> row, String col ) {
        if (table.getColumn( col ) == null) {
            throw new IllegalArgumentException( "No such column: " + col );
        }
        return row.get( col );
    }

}
