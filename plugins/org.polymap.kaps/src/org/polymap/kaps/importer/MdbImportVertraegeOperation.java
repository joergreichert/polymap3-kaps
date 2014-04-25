package org.polymap.kaps.importer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.qi4j.QiModule.EntityCreator;
import org.polymap.core.runtime.SubMonitor;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.SchlNamed;
import org.polymap.kaps.model.data.*;
import org.polymap.kaps.ui.form.EingangsNummerFormatter;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class MdbImportVertraegeOperation
        extends AbstractMdbImportOperation {

    private static Log log = LogFactory.getLog( MdbImportVertraegeOperation.class );


    public MdbImportVertraegeOperation( File dbFile, String[] tableNames ) {
        super( dbFile, tableNames, "Kaufpreissammlung importieren" );
    }


    protected IStatus doExecute0( IProgressMonitor monitor, IAdaptable info )
            throws Exception {
        File parentFolder = new File( "kaps" );
        parentFolder.mkdirs();

        monitor.beginTask( getLabel(), 12000 );
        final Database db = Database.open( dbFile );
        try {
            SubMonitor sub = null;

            sub = new SubMonitor( monitor, 10 );
            importStalas( db, sub );

            sub = new SubMonitor( monitor, 10 );
            importBRWRL( db, sub, parentFolder );

            sub = new SubMonitor( monitor, 10 );
            importEntity( db, sub, VertragsArtComposite.class, new EntityCallback<VertragsArtComposite>() {

                @Override
                public void fillEntity( VertragsArtComposite entity, Map<String, Object> builderRow ) {
                    // associate stala
                    entity.stala().set(
                            findSchlNamed( VerwandschaftsVerhaeltnisStalaComposite.class, builderRow, "STALA", false ) );
                    // collecting
                    // allVertragsarten.put( entity.schl().get(), entity );
                }
            } );

            sub = new SubMonitor( monitor, 10 );
            importEntity( db, sub, KaeuferKreisComposite.class, new EntityCallback<KaeuferKreisComposite>() {

                @Override
                public void fillEntity( KaeuferKreisComposite entity, Map<String, Object> builderRow ) {
                    // associate stala erstellen
                    entity.stala().set(
                            findSchlNamed( VeraeussererBaulandStalaComposite.class, builderRow, "STALA", false ) );
                    entity.stalaAgrar()
                            .set( findSchlNamed( VeraeussererAgrarLandStalaComposite.class, builderRow, "STALA_AGRAR",
                                    false ) );
                    entity.kaeuferKreisStabu().set(
                            findSchlNamed( KaeuferKreisStaBuComposite.class, builderRow, "STAT_BUND", false ) );

                    // collecting
                    // allKKreise.put( entity.schl().get(), entity );
                }
            } );
            sub = new SubMonitor( monitor, 10 );
            importEntity( db, sub, VertragComposite.class, new EntityCallback<VertragComposite>() {

                @Override
                public void fillEntity( VertragComposite entity, Map<String, Object> builderRow )
                        throws Exception {
                    // VERARBKZ
                    entity.fuerAuswertungGeeignet().set( getBooleanValue( builderRow, "VERARBKZ" ) );
                    // GESPLITTET
                    entity.gesplittet().set( getBooleanValue( builderRow, "GESPLITTET" ) );

                    // mapping eingangsNr
                    // find vertragsArt
                    entity.vertragsArt().set( findSchlNamed( VertragsArtComposite.class, builderRow, "VERTRAGART" ) );

                    // find KKreis für Käufer und Verkäufer
                    entity.kaeuferKreis().set( findSchlNamed( KaeuferKreisComposite.class, builderRow, "KKREIS" ) );
                    // find VKREIS
                    entity.verkaeuferKreis().set( findSchlNamed( KaeuferKreisComposite.class, builderRow, "VKREIS" ) );

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

                    VertragsdatenErweitertComposite vdec = repo.newEntity( VertragsdatenErweitertComposite.class, null );
                    // ,
                    // new EntityCreator<VertragsdatenErweitertComposite>() {
                    //
                    // public void create( VertragsdatenErweitertComposite prototype
                    // )
                    // throws Exception {
                    // // vertragsdatenErweitertCompositeImporter.fillEntity(
                    // // prototype, builderRow );
                    // }
                    // } );
                    vdec.updateBasisPreis( entity.vollpreis().get() );
                    entity.erweiterteVertragsdaten().set( vdec );

                    // find also Verkaufsverträge alt und geplittete
                    // Verträge
                    // TODO die werden aber eventuell erst später
                    // assoziiert

                    // allKaufvertrag.put( entity.eingangsNr().get().toString(),
                    // entity );
                }
            } );

            sub = new SubMonitor( monitor, 10 );
            importEntity( db, sub, NutzungComposite.class, new EntityCallback<NutzungComposite>() {

                @Override
                public void fillEntity( NutzungComposite entity, Map<String, Object> builderRow ) {
                    entity.stala().set(
                            findSchlNamed( GrundstuecksArtBaulandStalaComposite.class, builderRow, "STALA", false ) );
                    entity.artDerBauflaeche().set(
                            findSchlNamed( ArtDerBauflaecheStaBuComposite.class, builderRow, "STAT_BUND_ART", false ) );
                    entity.isAgrar().set( getBooleanValue( builderRow, "AGRAR" ) );
                    entity.isWohneigentum().set( getBooleanValue( builderRow, "STAT_BUND_WE" ) );
                    // allNutzung.put( entity.schl().get(), entity );
                }
            } );

            sub = new SubMonitor( monitor, 10 );
            importEntity( db, sub, GebaeudeArtComposite.class, new EntityCallback<GebaeudeArtComposite>() {

                @Override
                public void fillEntity( GebaeudeArtComposite entity, Map<String, Object> builderRow ) {

                    entity.gebaeudeArtStabu().set(
                            findSchlNamed( GebaeudeArtStaBuComposite.class, builderRow, "STAT_BUND_ART" ) );
                }
            } );

            sub = new SubMonitor( monitor, 10 );
            importEntity( db, sub, GemeindeComposite.class, null );

            sub = new SubMonitor( monitor, 10 );
            importEntity( db, sub, StrasseComposite.class, new EntityCallback<StrasseComposite>() {

                @Override
                public void fillEntity( StrasseComposite entity, Map<String, Object> builderRow ) {
                    entity.gemeinde().set( findSchlNamed( GemeindeComposite.class, builderRow, "GEMEINDE" ) );
                }
            } );
            sub = new SubMonitor( monitor, 10 );
            importEntity( db, sub, BodennutzungComposite.class, new EntityCallback<BodennutzungComposite>() {

                @Override
                public void fillEntity( BodennutzungComposite entity, Map<String, Object> builderRow ) {
                    // associate stala erstellen
                    entity.stala().set(
                            findSchlNamed( ArtDesBaugebietesStalaComposite.class, builderRow, "STALA", false ) );
                    // allBodennutzung.put( entity.schl().get(), entity );
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

            // final Map<String, GemarkungComposite> allGemarkung = new
            // HashMap<String, GemarkungComposite>();
            sub = new SubMonitor( monitor, 10 );
            importEntity( db, sub, GemarkungComposite.class, new EntityCallback<GemarkungComposite>() {

                @Override
                public void fillEntity( GemarkungComposite entity, Map<String, Object> builderRow ) {
                    entity.gemeinde().set( findSchlNamed( GemeindeComposite.class, builderRow, "GEMEINDE" ) );
                    entity.flur().set( flur );
                    // allGemarkung.put( entity.schl().get(), entity );
                }
            } );

            sub = new SubMonitor( monitor, 10 );
            importEntity( db, sub, GemeindeFaktorComposite.class, new EntityCallback<GemeindeFaktorComposite>() {

                @Override
                public void fillEntity( GemeindeFaktorComposite entity, Map<String, Object> builderRow ) {
                    // alte Gemeinden können ignoriert werden, Leichen in der
                    // kaufdat.mdb
                    entity.gemeinde().set( findSchlNamed( GemeindeComposite.class, builderRow, "GEMEINDE", true ) );
                }
            } );

            // bodenrichtwertkennung laden
            final BodenRichtwertKennungComposite zonal = findSchlNamed( BodenRichtwertKennungComposite.class, "1" );
            // erschließungsbeitrag laden
            // RichtwertzoneLage auf 00
            final RichtwertZoneLageComposite richtwertZoneLageComposite = findSchlNamed(
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

                            entity.erschliessungsBeitrag().set(
                                    findSchlNamed( ErschliessungsBeitragComposite.class, builderRow, "EB" ) );

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
                                                prototype.gemeinde()
                                                        .set( findSchlNamed( GemeindeComposite.class, builderRow,
                                                                "GEMEINDE" ) );
                                                prototype.nutzung().set(
                                                        findSchlNamed( NutzungComposite.class, builderRow, "NUART" ) );
                                                prototype.bodenNutzung().set(
                                                        findSchlNamed( BodennutzungComposite.class, builderRow,
                                                                "NUTZUNG" ) );
                                                prototype.lage().set( richtwertZoneLageComposite );

                                                String RIWEKENNUNG = (String)builderRow.get( "RIWEKENNUNG" );
                                                if (RIWEKENNUNG != null) {
                                                    prototype.bodenrichtwertKennung().set(
                                                            findSchlNamed( BodenRichtwertKennungComposite.class,
                                                                    RIWEKENNUNG.trim() ) );
                                                }
                                                else {
                                                    prototype.bodenrichtwertKennung().set( zonal );
                                                }
                                                // entwicklungszustand importieren
                                                String ENTWZUSTAND = (String)builderRow.get( "ENTWZUSTAND" );
                                                if (ENTWZUSTAND != null) {
                                                    prototype.entwicklungsZustand().set(
                                                            findSchlNamed( EntwicklungsZustandComposite.class,
                                                                    ENTWZUSTAND.trim() ) );
                                                }
                                                // brwrl-art
                                                String NUTZUNG_ART = (String)builderRow.get( "NUTZUNG_ART" );
                                                if (NUTZUNG_ART != null) {
                                                    prototype
                                                            .brwrlArt()
                                                            .set( repo
                                                                    .findSchlNamed(
                                                                            BodenRichtwertRichtlinieArtDerNutzungComposite.class,
                                                                            NUTZUNG_ART.trim() ) );
                                                }
                                                // brwrl-ergänzung
                                                String NUTZUNG_ERGAENZ = (String)builderRow.get( "NUTZUNG_ERGAENZ" );
                                                if (NUTZUNG_ERGAENZ != null) {
                                                    prototype.brwrlErgaenzung().set(
                                                            findSchlNamed(
                                                                    BodenRichtwertRichtlinieErgaenzungComposite.class,
                                                                    NUTZUNG_ERGAENZ.trim() ) );
                                                }
                                                // entwicklungszusatz
                                                String ENTWZUSATZ = (String)builderRow.get( "ENTWZUSATZ" );
                                                if (ENTWZUSATZ != null) {
                                                    prototype.entwicklungsZusatz().set(
                                                            findSchlNamed( EntwicklungsZusatzComposite.class,
                                                                    ENTWZUSATZ.trim() ) );
                                                }
                                                // bauweise
                                                String bauweise = (String)builderRow.get( "BAUWEISE" );
                                                if (bauweise != null) {
                                                    prototype.bauweise().set(
                                                            findSchlNamed( BauweiseComposite.class, bauweise.trim() ) );
                                                }
                                            }
                                        } );
                                allRichtwertZone.put( zone.schl().get(), zone );
                            }
                            // zone.gueltigkeiten().add( entity );
                            entity.zone().set( zone );
                        }
                    } );
            allRichtwertZone.clear();

            final Map<Integer, FlurstueckComposite> allHauptflurstueckeI = new HashMap<Integer, FlurstueckComposite>();

            sub = new SubMonitor( monitor, 10 );
            importEntity( db, sub, FlurstueckComposite.class, new EntityCallback<FlurstueckComposite>() {

                @Override
                public void fillEntity( final FlurstueckComposite entity, final Map<String, Object> builderRow )
                        throws Exception {

                    Double eingangsnummer = (Double)builderRow.get( "EINGANGSNR" );
                    // if (eingangsnummer != null) {
                    VertragComposite vertragTemplate = QueryExpressions.templateFor( VertragComposite.class );
                    BooleanExpression expr = QueryExpressions.eq( vertragTemplate.eingangsNr(),
                            eingangsnummer.intValue() );
                    VertragComposite vertrag = KapsRepository.instance()
                            .findEntities( VertragComposite.class, expr, 0, 1 ).find();
                    if (vertrag == null) {
                        throw new IllegalStateException( "no vertrag found for " + eingangsnummer );
                    }
                    entity.vertrag().set( vertrag );
                    // }
                    // entity.vertrag().set( find( allKaufvertrag, builderRow,
                    // "EINGANGSNR" ) );

                    Object hauptteil = builderRow.get( "HAUPTTEIL" );
                    if ("×".equals( hauptteil )) {
                        allHauptflurstueckeI.put( eingangsnummer.intValue(), entity );
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
                    entity.nutzung().set( findSchlNamed( NutzungComposite.class, builderRow, "NUTZUNG" ) );
                    String strasse = (String)builderRow.get( "STRNR" );
                    if (strasse != null && gemeinde != null) {
                        entity.strasse().set(
                                StrasseComposite.Mixin.findStrasse( findSchlNamed( GemeindeComposite.class, gemeinde ),
                                        strasse ) );
                    }
                    entity.gebaeudeArt().set( findSchlNamed( GebaeudeArtComposite.class, builderRow, "GEBART" ) );
                    entity.artDesBaugebiets().set(
                            findSchlNamed( ArtDesBaugebietsComposite.class, builderRow, "BAUGEBART" ) );
                    // entity.richtwertZoneG().set( found );
                    entity.gemarkung().set( findSchlNamed( GemarkungComposite.class, builderRow, "GEMARKUNG" ) );
                    entity.flur().set( flur );

                    // String gemarkung = (String)builderRow.get( "GEMARKUNG" );
                    // Integer flurstueckNummer = (Integer)builderRow.get( "FLSTNR1"
                    // );
                    // entity.nummer().set( flurstueckNummer );
                    // String unterNummer = (String)builderRow.get( "FLSTNR1U" );
                    // // check if always loaded
                    // String key = gemarkung + "-" + flurstueckNummer + "-" +
                    // unterNummer;
                    // FlurstueckComposite flurstueck = allFlurstuecke.get( key );
                    // if (flurstueck == null) {
                    // flurstueck = repo.newEntity( FlurstueckComposite.class, null,
                    // new EntityCreator<FlurstueckComposite>() {
                    //
                    // public void create( FlurstueckComposite prototype )
                    // throws Exception {
                    // flurstueckImporter.fillEntity( prototype, builderRow );
                    // prototype.gemarkung().set( find( allGemarkung, builderRow,
                    // "GEMARKUNG" ) );
                    // prototype.flur().set( flur );
                    // }
                    // } );
                    // allFlurstuecke.put( key, flurstueck );
                    // }
                    // entity.flurstueck().set( flurstueck );
                    // repo.commitChanges();
                }
            } );
            // allFlurstuecke.clear();

            sub = new SubMonitor( monitor, 10 );
            importEntity( db, sub, BodenwertAufteilungTextComposite.class,
                    new EntityCallback<BodenwertAufteilungTextComposite>() {

                        @Override
                        public void fillEntity( BodenwertAufteilungTextComposite entity, Map<String, Object> builderRow ) {
                            entity.strflaeche().set( getBooleanValue( builderRow, "STRFLAECHE" ) );
                            // allBodenwertText.put( entity.schl().get(), entity );
                        }
                    } );

            File importfehler = new File( parentFolder, "importfehler.txt" );
            final BufferedWriter w = new BufferedWriter( new FileWriter( importfehler ) );
            sub = new SubMonitor( monitor, 10 );
            final AnnotatedCompositeImporter vertragsdatenErweitertCompositeImporter = new AnnotatedCompositeImporter(
                    VertragsdatenErweitertComposite.class, db.getTable( "K_BEVERW" ) );
            importEntity( db, sub, VertragsdatenBaulandComposite.class,
                    new EntityCallback<VertragsdatenBaulandComposite>() {

                        @Override
                        public void fillEntity( VertragsdatenBaulandComposite entity,
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
                            Double eingangsnummer = (Double)builderRow.get( "EINGANGSNR" );
                            // if (eingangsnummer != null) {
                            VertragComposite vertragTemplate = QueryExpressions.templateFor( VertragComposite.class );
                            BooleanExpression expr = QueryExpressions.eq( vertragTemplate.eingangsNr(),
                                    eingangsnummer.intValue() );
                            VertragComposite vertrag = KapsRepository.instance()
                                    .findEntities( VertragComposite.class, expr, 0, 1 ).find();
                            if (eingangsnummer == null || vertrag == null) {
                                throw new IllegalStateException( "no vertrag found for " + eingangsnummer );
                            }
                            entity.vertrag().set( vertrag );

                            // }
                            // VertragComposite vertrag = repo.findEntity(
                            // VertragComposite.class,
                            // find( allKaufvertrag, builderRow, "EINGANGSNR" ).id()
                            // );
                            // entity.vertrag().set( vertrag );
                            // entity.erbbauRecht2().set( (String)builderRow.get(
                            // "ERBBAU" ) );
                            entity.faktorFuerMarktanpassungGeeignet().set( getBooleanValue( builderRow, "MARKTANP" ) );
                            entity.gfzBereinigtenBodenpreisVerwenden().set(
                                    getBooleanValue( builderRow, "GFZVERWENDEN" ) );
                            // entity.denkmalschutz().set( (String)builderRow.get(
                            // "Denkmalschutz" ) );
                            entity.bodenwertAufteilung1().set(
                                    findSchlNamed( BodenwertAufteilungTextComposite.class, builderRow, "BODWTEXT1",
                                            true ) );
                            entity.bodenwertAufteilung2().set(
                                    findSchlNamed( BodenwertAufteilungTextComposite.class, builderRow, "BODWTEXT2",
                                            true ) );
                            entity.bodenwertAufteilung3().set(
                                    findSchlNamed( BodenwertAufteilungTextComposite.class, builderRow, "BODWTEXT3",
                                            true ) );
                            entity.bodennutzung().set(
                                    findSchlNamed( BodennutzungComposite.class, builderRow, "BONUTZ" ) );
                            entity.erschliessungsBeitrag().set(
                                    findSchlNamed( ErschliessungsBeitragComposite.class, builderRow, "EB" ) );
                            entity.fuerBodenwertaufteilungNichtGeeignet().set(
                                    getBooleanValue( builderRow, "BODWNICHT" ) );

                            // entity.sanierung().set( (String)builderRow.get( "SAN"
                            // ) );
                            entity.bereinigterBodenpreisMitNachkommastellen().set(
                                    getBooleanValue( builderRow, "GFZKOMMA" ) );
                            entity.keller().set( findSchlNamed( KellerComposite.class, builderRow, "KELLER" ) );

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
                            entity.richtwertZone().set( found.zone().get() );
                            entity.richtwertZoneG().set( found );

                            // Flurstück setzen, bisher Hauptflurstück, ab jetzt je
                            // Flurstück einmal
                            // erweiterte Daten
                            FlurstueckComposite flurstueck = repo.findEntity( FlurstueckComposite.class,
                                    allHauptflurstueckeI.get( eingangsnummer.intValue() ).id() );
                            if (flurstueck == null) {
                                throw new IllegalStateException( String.format(
                                        "no flurstueck found for FlurstuecksdatenBauland for vertrag %s", entity
                                                .vertrag().get().eingangsNr().get() ) );
                            }
                            // entity.flurstueck().set( flurstueck );

                            // ERBBAU setzen an übergeordnetem Flurstück
                            String erbbau = (String)builderRow.get( "ERBBAU" );
                            flurstueck.erbbaurecht().set( erbbau );

                            // subcreate VertragsdatenErweitert
                            // in der Tabelle K_BEVERW sind Vertrags- und
                            // Vertragsdaten, letztere werden hier separat
                            // erzeugt
                            // VertragsdatenErweitertComposite vdec = repo.newEntity(
                            // VertragsdatenErweitertComposite.class, null,
                            // new EntityCreator<VertragsdatenErweitertComposite>() {
                            //
                            // public void create( VertragsdatenErweitertComposite
                            // prototype )
                            // throws Exception {
                            vertragsdatenErweitertCompositeImporter.fillEntity(
                                    vertrag.erweiterteVertragsdaten().get(), builderRow );
                            // }
                            // } );
                            // vertrag.erweiterteVertragsdaten().set( vdec );

                            // checken ob Flurstück tatsächlich agrar ist, in der DB
                            // ist ganz schöner Mist drin
                            // if (flurstueck.nutzung().get().isAgrar().get()) {
                            // // log.error( String.format(
                            // //
                            // "Flurstück ist AGRAR müsste aber Bauland sein für Vertrag %s mit Nutzung %s",
                            // // entity.vertrag().get().eingangsNr().get(),
                            // // flurstueck.nutzung().get().schl()
                            // // .get() ) );
                            // w.write( String.format(
                            // "Flurstück ist AGRAR müsste aber Bauland sein für Vertrag %s mit Nutzung %s\n",
                            // entity.vertrag().get().eingangsNr().get(),
                            // flurstueck.nutzung().get().schl()
                            // .get() ) );
                            // }
                        }
                    } );

            final AnnotatedCompositeImporter vertragsdatenErweitertAgrarCompositeImporter = new AnnotatedCompositeImporter(
                    VertragsdatenErweitertComposite.class, db.getTable( "K_BEVERL" ) );

            final Set<Double> foundVertraege = new HashSet<Double>();
            final Set<Double> duplicateVertraege = new HashSet<Double>();

            sub = new SubMonitor( monitor, 10 );
            importEntity( db, sub, VertragsdatenAgrarComposite.class,
                    new EntityCallback<VertragsdatenAgrarComposite>() {

                        @Override
                        public void fillEntity( VertragsdatenAgrarComposite entity, final Map<String, Object> builderRow )
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
                            entity.gebaeudeArt()
                                    .set( findSchlNamed( GebaeudeArtComposite.class, builderRow, "GEBART" ) );
                            // VertragComposite vertrag = repo.findEntity(
                            // VertragComposite.class,
                            // find( allKaufvertrag, builderRow, "EINGANGSNR" ).id()
                            // );
                            // entity.vertrag().set( vertrag );
                            Double eingangsnummer = (Double)builderRow.get( "EINGANGSNR" );
                            // if (eingangsnummer != null) {
                            VertragComposite vertragTemplate = QueryExpressions.templateFor( VertragComposite.class );
                            BooleanExpression expr = QueryExpressions.eq( vertragTemplate.eingangsNr(),
                                    eingangsnummer.intValue() );
                            VertragComposite vertrag = KapsRepository.instance()
                                    .findEntities( VertragComposite.class, expr, 0, 1 ).find();
                            if (vertrag == null) {
                                throw new IllegalStateException( "no vertrag found for " + eingangsnummer );
                            }
                            if (foundVertraege.contains( eingangsnummer )) {
                                duplicateVertraege.add( eingangsnummer );
                            }
                            else {
                                foundVertraege.add( eingangsnummer );
                                // dont set them here, because it must be a 1-1
                                entity.vertrag().set( vertrag );

                                // }
                                // entity.erbbauRecht2().set( (String)builderRow.get(
                                // "ERBBAU" ) );
                                entity.zurRichtwertermittlungGeeignet().set(
                                        getBooleanValue( builderRow, "RIWEGEEIGNET" ) );
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
                                entity.bodennutzung1().set(
                                        findSchlNamed( BodennutzungComposite.class, builderRow, "BONU1" ) );
                                entity.bodennutzung2().set(
                                        findSchlNamed( BodennutzungComposite.class, builderRow, "BONU2" ) );
                                entity.bodennutzung3().set(
                                        findSchlNamed( BodennutzungComposite.class, builderRow, "BONU3" ) );
                                entity.bodennutzung4().set(
                                        findSchlNamed( BodennutzungComposite.class, builderRow, "BONU4" ) );
                                entity.bodennutzung5().set(
                                        findSchlNamed( BodennutzungComposite.class, builderRow, "BONU5" ) );
                                entity.bodennutzung6().set(
                                        findSchlNamed( BodennutzungComposite.class, builderRow, "BONU6" ) );

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

                                // Flurstück setzen, bisher Hauptflurstück, ab jetzt
                                // je
                                // Flurstück einmal
                                // erweiterte Daten
                                FlurstueckComposite flurstueck = allHauptflurstueckeI.get( eingangsnummer.intValue() );
                                if (flurstueck == null) {
                                    throw new IllegalStateException( String.format(
                                            "no flurstueck found for FlurstuecksdatenAgrar for vertrag %s", entity
                                                    .vertrag().get().eingangsNr().get() ) );
                                }
                                // entity.flurstueck().set( flurstueck );

                                // checken ob Flurstück tatsächlich agrar ist, in der
                                // DB
                                // ist ganz schöner Mist drin
                                //
                                if (// flurstueck.nutzung().get().isAgrar().get()
                                    // ||
                                vertrag.erweiterteVertragsdaten().get().bereinigterVollpreis().get() == null) {
                                    // subcreate VertragsdatenErweitert
                                    // in der Tabelle K_BEVERW sind Vertrags- und
                                    // Vertragsdaten, letztere werden hier separate
                                    // erzeugt
                                    // VertragsdatenErweitertComposite vdec =
                                    // repo.newEntity(
                                    // VertragsdatenErweitertComposite.class, null,
                                    // new
                                    // EntityCreator<VertragsdatenErweitertComposite>()
                                    // {
                                    //
                                    // public void create(
                                    // VertragsdatenErweitertComposite prototype )
                                    // throws Exception {
                                    vertragsdatenErweitertAgrarCompositeImporter.fillEntity( vertrag
                                            .erweiterteVertragsdaten().get(), builderRow );
                                    // }
                                    // } );
                                    // vertrag.eingangsNr().get();
                                    // vertrag.erweiterteVertragsdaten().set( vdec );
                                }
                                // if (flurstueck.nutzung().get().isAgrar().get()) {
                                // // log.error( String.format(
                                // //
                                // "Flurstück ist Bauland müsste aber AGRAR sein für Vertrag %s",
                                // // entity.vertrag()
                                // // .get().eingangsNr().get() ) );
                                // w.write( String.format(
                                // "Flurstück ist Bauland müsste aber AGRAR sein für Vertrag %s mit Nutzung %s\n",
                                // entity.vertrag().get().eingangsNr().get(),
                                // flurstueck.nutzung().get().schl()
                                // .get() ) );
                                // }
                            }
                        }


                        private RichtwertzoneZeitraumComposite findRichtwertZone(
                                final Map<String, List<RichtwertzoneZeitraumComposite>> allRichtwertZoneGueltigkeit,
                                VertragsdatenAgrarComposite entity, final Map<String, Object> builderRow, String number )
                                throws IOException {
                            // RIZONE
                            String zone = (String)builderRow.get( "RIZO" + number );
                            if (zone != null) {
                                String gemeinde = builderRow.get( "RIZOGEM" + number ).toString();
                                Date jahr = (Date)builderRow.get( "RIZOJAHR" + number );
                                //
                                return MdbImportVertraegeOperation.this.findRichtwertZone( w,
                                        allRichtwertZoneGueltigkeit, zone, gemeinde, jahr,
                                        VertragsdatenAgrarComposite.class,
                                        EingangsNummerFormatter.format( entity.vertrag().get().eingangsNr().get() ) );
                            }
                            return null;
                        }
                    } );

            for (Double eingangsNr : duplicateVertraege) {
                w.write( "Mehr als 1 VertragsdatenErweitertAgrar gefunden für " + eingangsNr + "\n" );
            }

            sub = new SubMonitor( monitor, 10 );
            importFlurZwiAgrar( db, sub, parentFolder );

            sub = new SubMonitor( monitor, 10 );
            importStabuVertragsdaten( db, sub, parentFolder );

            w.flush();
            w.close();
            log.error( "WRITTEN LOG TO FILE: " + importfehler.getAbsolutePath() );
            allRichtwertZoneGueltigkeit.clear();
            allHauptflurstueckeI.clear();
        }
        finally {
            db.close();
        }

        repo.commitChanges();
        return Status.OK_STATUS;
    }


    private void importFlurZwiAgrar( Database db, SubMonitor monitor, File parentFolder )
            throws Exception {
        Table table = db.getTable( "FLURZWI_AGRAR" );
        monitor.beginTask( "Tabelle: " + table.getName(), table.getRowCount() );

        File wmvaopf = new File( parentFolder, "FLURZWI_AGRAR.txt" );
        final BufferedWriter wmvaopfW = new BufferedWriter( new FileWriter( wmvaopf ) );
        // data rows
        Map<String, Object> builderRow = null;
        int count = 0;
        while ((builderRow = table.getNextRow()) != null) {

            Integer eingangsnummer = (Integer)builderRow.get( "EINGANGSNR" );
            if (eingangsnummer != null) {
                VertragComposite vertragTemplate = QueryExpressions.templateFor( VertragComposite.class );
                BooleanExpression expr = QueryExpressions.eq( vertragTemplate.eingangsNr(), eingangsnummer.intValue() );
                VertragComposite vertrag = KapsRepository.instance().findEntities( VertragComposite.class, expr, 0, 1 )
                        .find();
                if (vertrag == null) {
                    throw new IllegalStateException( "no vertrag found for " + eingangsnummer );
                }
                // suchen nach FlurstuecksDatenAgrar
                // boolean found = false;
                VertragsdatenAgrarComposite agrar = VertragsdatenAgrarComposite.Mixin.forVertrag( vertrag );
                if (agrar != null) {
                    agrar.flaecheLandwirtschaftStala().set( (Double)builderRow.get( "FL_LANDW" ) );
                    agrar.hypothekStala().set( (Double)builderRow.get( "HYPOTHEK" ) );
                    agrar.wertTauschStala().set( (Double)builderRow.get( "TAUSCHGRUND" ) );
                    agrar.wertSonstigesStala().set( (Double)builderRow.get( "WERTSONST" ) );
                    agrar.bemerkungStala().set( (String)builderRow.get( "BEM_AGRAR" ) );
                }
                else {
                    wmvaopfW.write( "no flurstuecksdatenagrar found for " + eingangsnummer + "\n" );
                }
            }
            else {
                throw new IllegalStateException( "no vertrag EINGANGSNR found" );
            }
        }
        wmvaopfW.flush();
        wmvaopfW.close();
        repo.commitChanges();
        log.info( "Imported and committed: FLURZWI_AGRAR -> " + count );
        monitor.done();
    }


    private void importStabuVertragsdaten( Database db, SubMonitor monitor, File parentFolder )
            throws Exception {
        Table table = db.getTable( "K_BUCH" );
        monitor.beginTask( "Tabelle: " + table.getName(), table.getRowCount() );

        File wmvaopf = new File( parentFolder, "K_BUCH_STABU.txt" );
        final BufferedWriter wmvaopfW = new BufferedWriter( new FileWriter( wmvaopf ) );
        // data rows
        Map<String, Object> builderRow = null;
        int count = 0;
        while ((builderRow = table.getNextRow()) != null) {

            Double eingangsnummer = (Double)builderRow.get( "EINGANGSNR" );
            GebaeudeArtStaBuComposite gebaeudeArtStaBuComposite = findSchlNamed( GebaeudeArtStaBuComposite.class,
                    builderRow, "STABU_GEBART" );
            GebaeudeTypStaBuComposite gebaeudeTypStaBuComposite = findSchlNamed( GebaeudeTypStaBuComposite.class,
                    builderRow, "STABU_GEBTYP" );
            KellerComposite kellerComposite = findSchlNamed( KellerComposite.class, builderRow, "KELLER" );
            String stellplaetze = (String)builderRow.get( "STELLPLATZ" );
            String garage = (String)builderRow.get( "GARAGE" );
            String carport = (String)builderRow.get( "CARPORT" );

            if (gebaeudeArtStaBuComposite != null || gebaeudeTypStaBuComposite != null || kellerComposite != null
                    || stellplaetze != null || garage != null || carport != null) {
                if (eingangsnummer != null) {
                    VertragComposite vertragTemplate = QueryExpressions.templateFor( VertragComposite.class );
                    BooleanExpression expr = QueryExpressions.eq( vertragTemplate.eingangsNr(),
                            eingangsnummer.intValue() );
                    VertragComposite vertrag = KapsRepository.instance()
                            .findEntities( VertragComposite.class, expr, 0, 1 ).find();
                    if (vertrag == null) {
                        throw new IllegalStateException( "no vertrag found for " + eingangsnummer );
                    }
                    // suchen nach FlurstuecksDatenAgrar
                    VertragsdatenBaulandComposite bauland = VertragsdatenBaulandComposite.Mixin.forVertrag( vertrag );
                    if (bauland != null) {

                        bauland.gebaeudeArtStaBu().set( gebaeudeArtStaBuComposite );

                        bauland.gebaeudeTypStaBu().set( gebaeudeTypStaBuComposite );
                        // keller ist doppelt beleget, also heir nur setzen wenn
                        // nicht null
                        if (kellerComposite != null) {
                            bauland.keller().set( kellerComposite );
                        }
                        bauland.garage().set( garage );
                        bauland.stellplaetze().set( stellplaetze );
                        bauland.carport().set( carport );
                    }
                    else {
                        wmvaopfW.write( "no flurstuecksdatenbauland found for " + eingangsnummer + "\n" );
                    }
                }
                else {
                    throw new IllegalStateException( "no vertrag EINGANGSNR found" );
                }
            }
        }
        wmvaopfW.flush();
        wmvaopfW.close();
        repo.commitChanges();
        log.info( "Imported and committed: K_BUCH_STABU -> " + count );
        monitor.done();
    }


    protected void importStalas( Database db, IProgressMonitor monitor )
            throws Exception {
        Table table = db.getTable( "K_STALA" );
        monitor.beginTask( "Tabelle: " + table.getName(), table.getRowCount() );

        // data rows
        Map<String, Object> builderRow = null;
        int count = 0;
        while ((builderRow = table.getNextRow()) != null) {

            String art = (String)builderRow.get( "ART" );
            SchlNamed stala = null;
            if ("1".equals( art )) {
                stala = repo.newEntity( GrundstuecksArtBaulandStalaComposite.class, null );
            }
            else if ("2".equals( art )) {
                stala = repo.newEntity( ArtDesBaugebietesStalaComposite.class, null );
            }
            else if ("3".equals( art )) {
                stala = repo.newEntity( VeraeussererBaulandStalaComposite.class, null );
            }
            else if ("4".equals( art )) {
                stala = repo.newEntity( VerwandschaftsVerhaeltnisStalaComposite.class, null );
            }
            else if ("5".equals( art )) {
                stala = repo.newEntity( VeraeussererAgrarLandStalaComposite.class, null );
            }
            else if ("6".equals( art )) {
                stala = repo.newEntity( GrundstuecksArtAgrarLandStalaComposite.class, null );
            }
            else if ("7".equals( art )) {
                stala = repo.newEntity( ErwerberStalaComposite.class, null );
            }
            else {
                throw new IllegalStateException( "unknown Stala Art " + art );
            }
            String value = (String)builderRow.get( "SCHL" );
            stala.schl().set( value != null ? value.trim() : null );
            value = (String)builderRow.get( "BEZ" );
            stala.name().set( value != null ? value.trim() : null );
            count++;
        }
        repo.commitChanges();
        log.info( "Imported and committed: K_Stala -> " + count );
        monitor.done();
    }


    private void importBRWRL( Database db, IProgressMonitor monitor, final File parentFolder )
            throws Exception {
        Table table = db.getTable( "K_BORIS_KEYS" );
        monitor.beginTask( "Tabelle: " + table.getName(), table.getRowCount() );

        AnnotatedCompositeImporter artImporter = new AnnotatedCompositeImporter(
                BodenRichtwertRichtlinieArtDerNutzungComposite.class, table );
        AnnotatedCompositeImporter ergaenzungImporter = new AnnotatedCompositeImporter(
                BodenRichtwertRichtlinieErgaenzungComposite.class, table );

        Map<String, Object> builderRow = null;
        int count = 0;
        while ((builderRow = table.getNextRow()) != null) {
            // alle Bewertungen importieren

            String aOderE = (String)builderRow.get( "ART_ERGAENZUNG" );
            if ("A".equals( aOderE )) {
                BodenRichtwertRichtlinieArtDerNutzungComposite entity = repo.newEntity(
                        BodenRichtwertRichtlinieArtDerNutzungComposite.class, null );
                artImporter.fillEntity( entity, builderRow );
            }
            else if ("E".equals( aOderE )) {
                BodenRichtwertRichtlinieErgaenzungComposite entity = repo.newEntity(
                        BodenRichtwertRichtlinieErgaenzungComposite.class, null );
                ergaenzungImporter.fillEntity( entity, builderRow );
            }
            else {
                throw new IllegalStateException( "unknown ART_ERGAENZUNG: '" + aOderE + "'" );
            }
            count++;
            // andernfalls erstmal ignorieren
        }
        // wmvaopfW.flush();
        // wmvaopfW.close();
        repo.commitChanges();
        log.info( "Imported and committed: K_BORIS_KEYS as Bodenrichtwertrichtlinie-> " + count );
        monitor.done();
    }
}
