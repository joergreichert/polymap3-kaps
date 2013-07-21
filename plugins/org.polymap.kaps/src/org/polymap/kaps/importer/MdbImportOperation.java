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

import com.healthmarketscience.jackcess.Database;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.qi4j.QiModule.EntityCreator;
import org.polymap.core.runtime.SubMonitor;

import org.polymap.kaps.model.data.*;
import org.polymap.kaps.ui.form.EingangsNummerFormatter;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class MdbImportOperation
        extends AbstractMdbImportOperation {

    private static Log log = LogFactory.getLog( MdbImportOperation.class );


    public MdbImportOperation( File dbFile, String[] tableNames ) {
        super( dbFile, tableNames, "Kaufpreissammlung importieren" );
    }


    protected IStatus doExecute( IProgressMonitor monitor, IAdaptable info )
            throws Exception {
        monitor.beginTask( getLabel(), 12000 );
        final Database db = Database.open( dbFile );
        try {
            SubMonitor sub = null;

            sub = new SubMonitor( monitor, 10 );

            final List<StalaComposite> allStalas = new ArrayList<StalaComposite>();
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
                    allStalas.add( entity );
                }
            } );

            sub = new SubMonitor( monitor, 10 );
            importEntity( db, sub, VertragsArtComposite.class, new EntityCallback<VertragsArtComposite>() {

                @Override
                public void fillEntity( VertragsArtComposite entity, Map<String, Object> builderRow ) {
                    // associate stala
                    entity.stala().set(
                            findStala( allStalas, "STALA", builderRow, StalaComposite.VERWANDSCHAFTSVERHAELTNIS ) );
                    // collecting
                    allVertragsarten.put( entity.schl().get(), entity );
                }
            } );

            sub = new SubMonitor( monitor, 10 );
            importEntity( db, sub, KaeuferKreisComposite.class, new EntityCallback<KaeuferKreisComposite>() {

                @Override
                public void fillEntity( KaeuferKreisComposite entity, Map<String, Object> builderRow ) {
                    // associate stala erstellen
                    entity.stala()
                            .set( findStala( allStalas, "STALA", builderRow, StalaComposite.VERAEUSSERER_BAULAND ) );
                    entity.stalaAgrar().set(
                            findStala( allStalas, "STALA_AGRAR", builderRow, StalaComposite.VERAEUSSERER_AGRARLAND ) );
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
                    entity.stala().set( findStala( allStalas, "STALA", builderRow, StalaComposite.GRUNDSTUECKSART ) );
                    entity.isAgrar().set( getBooleanValue( builderRow, "AGRAR" ) );
                    allNutzung.put( entity.schl().get(), entity );
                }
            } );

            // erschließungsbeitrag laden
            final Map<String, GebaeudeArtStaBuComposite> allGebaeudeArtStaBu = repo
                    .entitiesWithSchl( GebaeudeArtStaBuComposite.class );

            sub = new SubMonitor( monitor, 10 );
            final Map<String, GebaeudeArtComposite> allGebaeudeArt = new HashMap<String, GebaeudeArtComposite>();
            importEntity( db, sub, GebaeudeArtComposite.class, new EntityCallback<GebaeudeArtComposite>() {

                @Override
                public void fillEntity( GebaeudeArtComposite entity, Map<String, Object> builderRow ) {

                    entity.gebaeudeArtStabu().set( find( allGebaeudeArtStaBu, builderRow, "STAT_BUND_ART" ) );
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
                    entity.stala().set( findStala( allStalas, "STALA", builderRow, StalaComposite.ARTDESBAUGEBIETES ) );
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
            final Map<String, FlurstueckComposite> allFlurstuecke = new HashMap<String, FlurstueckComposite>();

            final Map<VertragComposite, FlurstueckVerkaufComposite> allHauptflurstuecke = new HashMap<VertragComposite, FlurstueckVerkaufComposite>();
            final AnnotatedCompositeImporter flurstueckImporter = new AnnotatedCompositeImporter(
                    FlurstueckComposite.class, table( db, FlurstueckComposite.class ) );

            sub = new SubMonitor( monitor, 10 );
            importEntity( db, sub, FlurstueckVerkaufComposite.class, new EntityCallback<FlurstueckVerkaufComposite>() {

                @Override
                public void fillEntity( final FlurstueckVerkaufComposite entity, final Map<String, Object> builderRow )
                        throws Exception {
                    entity.vertrag().set( find( allKaufvertrag, builderRow, "EINGANGSNR" ) );

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
                    entity.nutzung().set( find( allNutzung, builderRow, "NUTZUNG" ) );
                    entity.strasse().set( find( allStrasse, builderRow, "STRNR" ) );
                    entity.gebaeudeArt().set( find( allGebaeudeArt, builderRow, "GEBART" ) );
                    entity.artDesBaugebiets().set(
                            find( allArtDesBaugebietes, builderRow, "BAUGEBART" ) );
                    // entity.richtwertZoneG().set( found );

                    String gemarkung = (String)builderRow.get( "GEMARKUNG" );
                    Integer flurstueckNummer = (Integer)builderRow.get( "FLSTNR1" );
                    String unterNummer = (String)builderRow.get( "FLSTNR1U" );
                    // check if always loaded
                    String key = gemarkung + "-" + flurstueckNummer + "-" + unterNummer;
                    FlurstueckComposite flurstueck = allFlurstuecke.get( key );
                    if (flurstueck == null) {
                        flurstueck = repo.newEntity( FlurstueckComposite.class, null,
                                new EntityCreator<FlurstueckComposite>() {

                                    public void create( FlurstueckComposite prototype )
                                            throws Exception {
                                        flurstueckImporter.fillEntity( prototype, builderRow );
                                        prototype.gemarkung().set( find( allGemarkung, builderRow, "GEMARKUNG" ) );                                      
                                        prototype.flur().set( flur );
                                    }
                                } );
                        allFlurstuecke.put( key, flurstueck );
                    }
                    entity.flurstueck().set( flurstueck );
                }
            } );
            allFlurstuecke.clear();

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
                            FlurstueckVerkaufComposite flurstueck = repo.findEntity( FlurstueckVerkaufComposite.class,
                                    allHauptflurstuecke.get( entity.vertrag().get() ).id() );
                            if (flurstueck == null) {
                                throw new IllegalStateException( String.format(
                                        "no flurstueck found for FlurstuecksdatenBauland for vertrag %s", entity
                                                .vertrag().get().eingangsNr().get() ) );
                            }
                            entity.flurstueck().set( flurstueck );

                            // ERBBAU setzen an übergeordnetem Flurstück
                            String erbbau = (String)builderRow.get( "ERBBAU" );
                            flurstueck.erbbaurecht().set( erbbau );

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
                            FlurstueckVerkaufComposite flurstueck = allHauptflurstuecke.get( entity.vertrag().get() );
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

            w.flush();
            w.close();
            log.error( "WRITTEN LOG TO FILE: " + importfehler.getAbsolutePath() );
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


    protected final StalaComposite findStala( List<StalaComposite> allStalas, String columnName,
            Map<String, Object> builderRow, String neededArt ) {
        String stalaSchl = (String)builderRow.get( columnName );
        StalaComposite foundStala = null;
        if (stalaSchl != null && !stalaSchl.isEmpty()) {
            for (StalaComposite stala : allStalas) {
                String schl = stala.schl().get();
                String art = stala.art().get();
                if (schl != null && schl.trim().equals( stalaSchl.trim() ) && art != null
                        && art.trim().equals( neededArt.trim() )) {
                    foundStala = stala;
                    break;
                }
            }
            if (foundStala == null) {
                throw new IllegalStateException( "no stala found for schl '" + stalaSchl + "'!" );
            }
        }
        return foundStala;
    }
}
