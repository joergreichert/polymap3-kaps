package org.polymap.kaps.importer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.unitofwork.NoSuchEntityException;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.model.CompletionException;
import org.polymap.core.runtime.SubMonitor;
import org.polymap.core.runtime.entity.ConcurrentModificationException;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.SchlNamed;
import org.polymap.kaps.model.data.AusstattungBewertungComposite;
import org.polymap.kaps.model.data.AusstattungComposite;
import org.polymap.kaps.model.data.BelastungComposite;
import org.polymap.kaps.model.data.EigentumsartComposite;
import org.polymap.kaps.model.data.EtageComposite;
import org.polymap.kaps.model.data.FlurComposite;
import org.polymap.kaps.model.data.FlurstueckComposite;
import org.polymap.kaps.model.data.GebaeudeArtComposite;
import org.polymap.kaps.model.data.GebaeudeComposite;
import org.polymap.kaps.model.data.GemarkungComposite;
import org.polymap.kaps.model.data.HimmelsrichtungComposite;
import org.polymap.kaps.model.data.NutzungComposite;
import org.polymap.kaps.model.data.RichtwertzoneComposite;
import org.polymap.kaps.model.data.StrasseComposite;
import org.polymap.kaps.model.data.VertragComposite;
import org.polymap.kaps.model.data.WohnungComposite;
import org.polymap.kaps.model.data.WohnungseigentumComposite;
import org.polymap.kaps.ui.form.EingangsNummerFormatter;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class MdbImportWohneigentumOperation
        extends AbstractMdbImportOperation {

    private static Log log = LogFactory.getLog( MdbImportWohneigentumOperation.class );


    public MdbImportWohneigentumOperation( File dbFile, String[] tableNames ) {
        super( dbFile, tableNames, "Kaufpreissammlung Wohneigentum importieren" );
    }


    protected IStatus doExecute( IProgressMonitor monitor, IAdaptable info )
            throws Exception {
        File parentFolder = new File( "kaps" );
        parentFolder.mkdirs();

        monitor.beginTask( getLabel(), 12000 );
        final Database db = Database.open( dbFile );
        try {
            SubMonitor sub = null;
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

            final Map<String, GebaeudeArtComposite> allGebaeudeArt = new HashMap<String, GebaeudeArtComposite>();
            for (GebaeudeArtComposite gebaeudeArt : repo.findEntities( GebaeudeArtComposite.class, null, 0, 10000 )) {
                allGebaeudeArt.put( gebaeudeArt.schl().get(), gebaeudeArt );
            }

            sub = new SubMonitor( monitor, 10 );
            // final Map<String, GebaeudeComposite> allGebaeude = new HashMap<String,
            // GebaeudeComposite>();
            importEntity( db, sub, GebaeudeComposite.class, new EntityCallback<GebaeudeComposite>() {

                @Override
                public void fillEntity( GebaeudeComposite entity, Map<String, Object> builderRow ) {
                    if (entity.sanierungswert().get() == null) {
                        entity.sanierungswert().set( "U" );
                    }
                    entity.gebaeudeArt().set( find( allGebaeudeArt, builderRow, "GEBART" ) );
                    // allGebaeude.put( entity.schl().get(), entity );
                }
            } );

            sub = new SubMonitor( monitor, 10 );
            importK_EOBJF( db, sub, allBelastungArt, parentFolder );

            sub = new SubMonitor( monitor, 10 );
            File wmvaopf = new File( parentFolder, "wohnungen_mit_vertrag_aber_ohne_passendes_flurstueck.txt" );
            final BufferedWriter wmvaopfW = new BufferedWriter( new FileWriter( wmvaopf ) );
            importEntity( db, sub, WohnungComposite.class, new EntityCallback<WohnungComposite>() {

                @Override
                public void fillEntity( WohnungComposite entity, Map<String, Object> builderRow )
                        throws IOException, ConcurrentModificationException, CompletionException {

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

                    Object schl = builderRow.get( "BEWSCHL" );
                    System.out.println( schl );
                    AusstattungComposite ausstattungComposite = find( allAusstattung, builderRow, "BEWSCHL" );
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

                    String gemarkungSchl = (String)builderRow.get( "GEM" );
                    // Integer
                    Integer flurstueckNummer = (Integer)builderRow.get( "FLSTNR" );
                    String flurstueckUnternummer = (String)builderRow.get( "FLSTNRU" );

                    GebaeudeComposite gebaeude = GebaeudeComposite.Mixin.forKeys( entity.objektNummer().get(), entity
                            .objektFortfuehrung().get(), entity.gebaeudeNummer().get(), entity.gebaeudeFortfuehrung()
                            .get() );
                    if (gebaeude == null) {
                        throw new IllegalStateException( String.format( "Kein Gebäude gefunden für %s mit Nummer %s",
                                WohnungComposite.class, entity.schl().get() ) );
                    }

                    // flurstück an gebaeude finden und gegebenenfalls durch
                    // das am Vertrag ersetzen
                    FlurstueckComposite gebaeudeFlurstueck = null;
                    try {
                        List<FlurstueckComposite> gFlurstuecke = gebaeude.flurstuecke().toList();
                    }
                    catch (NoSuchEntityException nse) {
                        throw nse;
                    }
                    List<FlurstueckComposite> gFlurstuecke = gebaeude.flurstuecke().toList();
                    for (FlurstueckComposite gFlurstueck : gFlurstuecke) {
                        if (gemarkungSchl.trim().equals( gFlurstueck.gemarkung().get().schl().get() )
                                && flurstueckNummer.equals( gFlurstueck.nummer().get() )
                                && flurstueckUnternummer.trim().equals( gFlurstueck.unterNummer().get() )) {
                            gebaeudeFlurstueck = gFlurstueck;
                            break;
                        }
                    }

                    Double eingangsnummer = (Double)builderRow.get( "EINGANGSNR" );
                    if (eingangsnummer != null) {
                        VertragComposite vertragTemplate = QueryExpressions.templateFor( VertragComposite.class );
                        BooleanExpression expr = QueryExpressions.eq( vertragTemplate.eingangsNr(),
                                eingangsnummer.intValue() );
                        VertragComposite vertrag = KapsRepository.instance()
                                .findEntities( VertragComposite.class, expr, 0, 1 ).find();
                        if (vertrag == null) {
                            throw new IllegalStateException( "no vertrag found for " + eingangsnummer );
                        }
                        Iterable<FlurstueckComposite> existingFlurstuecke = FlurstueckComposite.Mixin
                                .forEntity( vertrag );
                        FlurstueckComposite found = null;
                        for (FlurstueckComposite flurstueck : existingFlurstuecke) {
                            if (gemarkungSchl.trim().equals( flurstueck.gemarkung().get().schl().get() )
                                    && flurstueckNummer.equals( flurstueck.nummer().get() )
                                    && flurstueckUnternummer.trim().equals( flurstueck.unterNummer().get() )) {
                                found = flurstueck;
                                break;
                            }
                        }
                        if (found == null) {
                            wmvaopfW.write( String.format( "Wohnung %s hat Vertrag %s und braucht Flurstueck %s\n",
                                    entity.schl().get(), EingangsNummerFormatter.format( eingangsnummer.intValue() ),
                                    gemarkungSchl + "-" + flurstueckNummer + "-" + flurstueckUnternummer + "" ) );
                        }
                        else {
                            entity.flurstueck().set( found );
                            if (gebaeudeFlurstueck != null
                                    && gebaeudeFlurstueck.gemarkung().get().schl().get()
                                            .equals( found.gemarkung().get().schl().get() )
                                    && gebaeudeFlurstueck.nummer().get().equals( found.nummer().get() )
                                    && gebaeudeFlurstueck.unterNummer().get().equals( found.unterNummer().get() )) {
                                // belastung und erbbau vom Gebäudegrundstück übernehmen
                                found.belastung().set( gebaeudeFlurstueck.belastung().get() );
                                found.erbbaurecht().set( gebaeudeFlurstueck.erbbaurecht().get() );
                                
                                // pseudoflurstück an gebäude löschen und durch neues ersetzen
                                if (gebaeudeFlurstueck.vertrag().get() == null && found != gebaeudeFlurstueck) {
                                    // Flurstück aus wohnung ersetzen
                                    for( WohnungComposite wohnung : WohnungComposite.Mixin.findWohnungenFor( gebaeudeFlurstueck )) {
                                        wohnung.flurstueck().set( found );
                                    }
                                    // und an Gebäude entfernen
                                    gebaeude.flurstuecke().remove( gebaeudeFlurstueck );
                                    repo.removeEntity( gebaeudeFlurstueck );
                                    // repo.commitChanges();
                                    // repo.updateEntity( gebaeude );
                                }
                            }
                            gebaeude.flurstuecke().add( found );
                        }
                    }
                    else {
                        // wenn kein Vertrag, pseudoflurstück von Gebäude setzen
                        entity.flurstueck().set( gebaeudeFlurstueck );
                    }
                    // TODO gebaeude an wohnung setzen
                    repo.commitChanges();
                }
            } );
            wmvaopfW.flush();
            wmvaopfW.close();

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

            // allGebaeude.clear();
            allWohnungseigentum.clear();
            allHimmelsrichtung.clear();
            allEigentumsArt.clear();
            allAusstattung.clear();
            allEtageArt.clear();
            allBelastungArt.clear();
        }
        finally {
            db.close();
        }

        return Status.OK_STATUS;
    }


    protected void importK_EOBJF( Database db, IProgressMonitor monitor,
            Map<String, BelastungComposite> allBelastungArt, final File parentFolder )
            throws Exception {
        Table table = db.getTable( "K_EOBJF" );
        monitor.beginTask( "Tabelle: " + table.getName(), table.getRowCount() );

        File wmvaopf = new File( parentFolder, "K_EOBJF.txt" );
        final BufferedWriter wmvaopfW = new BufferedWriter( new FileWriter( wmvaopf ) );
        // data rows
        Map<String, Object> builderRow = null;
        int count = 0;
        while ((builderRow = table.getNextRow()) != null) {

            FlurstueckComposite flurstueck = repo.newEntity( FlurstueckComposite.class, null );

            flurstueck.gemarkung().set( findBySchl( GemarkungComposite.class, builderRow, "GEM" ) );
            // Integer
            flurstueck.nummer().set( (Integer)builderRow.get( "FLSTNR" ) );
            flurstueck.unterNummer().set( (String)builderRow.get( "FLSTNRU" ) );
            // N, J, NULL
            flurstueck.erbbaurecht().set( (String)builderRow.get( "ERBBAUR" ) );
            // 00 - 06, NULL
            flurstueck.belastung().set( find( allBelastungArt, builderRow, "BELASTUNG" ) );
            flurstueck.flaeche().set( (Double)builderRow.get( "GFLAECHE" ) );
            flurstueck.baublock().set( (String)builderRow.get( "BAUBLOCK" ) );
            flurstueck.kartenBlatt().set( (String)builderRow.get( "KARTBLATT" ) );
            flurstueck.hausnummer().set( (String)builderRow.get( "HAUSNR" ) );
            flurstueck.hausnummerZusatz().set( (String)builderRow.get( "HZUSNR" ) );
            flurstueck.kartenBlattNummer().set( (String)builderRow.get( "KARTBLATTN" ) );

            flurstueck.flur().set( repo.findSchlNamed( FlurComposite.class, "000" ) );
            flurstueck.nutzung().set( findBySchl( NutzungComposite.class, builderRow, "NUTZUNG" ) );
            flurstueck.strasse().set( findBySchl( StrasseComposite.class, builderRow, "STRNR" ) );
            try {
                flurstueck.richtwertZone().set( findBySchl( RichtwertzoneComposite.class, builderRow, "RIZONE" ) );
            }
            catch (IllegalStateException ise) {
                wmvaopfW.write( "Keine Richtwertzone  gefunden für " + builderRow.get( "RIZONE" ) + "\n" );
            }

            // gebäude suchen und flurstück daran setzen
            Integer objektNr = (Integer)builderRow.get( "OBJEKTNR" );
            Integer objektFort = (Integer)builderRow.get( "FORTF" );
            Integer gebNr = (Integer)builderRow.get( "GEBNR" );
            Integer gebFort = (Integer)builderRow.get( "GEBNRFORTF" );
            GebaeudeComposite gebaeude = GebaeudeComposite.Mixin.forKeys( objektNr, objektFort, gebNr, gebFort );
            if (gebaeude == null) {
                wmvaopfW.write( "Kein Gebäude gefunden für " + objektNr + "/" + objektFort + "/" + gebNr + "/"
                        + gebFort + "\n" );
            }
            else {
                // if (!gebaeude.flurstuecke().contains( flurstueck )) {
                gebaeude.flurstuecke().add( flurstueck );
                // }
            }
        }
        wmvaopfW.flush();
        wmvaopfW.close();
        repo.commitChanges();
        log.info( "Imported and committed: K_EOBJF -> " + count );
        monitor.done();
    }


    private <T extends SchlNamed> T findBySchl( Class<T> type, Map<String, Object> builderRow, String string ) {
        String schl = (String)builderRow.get( string );
        if (schl != null && !schl.isEmpty()) {
            T findSchlNamed = repo.findSchlNamed( type, schl );
            if (findSchlNamed == null) {
                throw new IllegalStateException( "no " + type + " found for schl() " + schl );
            }
            return findSchlNamed;
        }
        return null;
    }

    //
    // private Query<FlurstueckComposite> findFlurstuecke( String gemarkungSchl,
    // Integer flurstueckNummer,
    // String flurstueckUnternummer ) {
    // KapsRepository.instance().f
    // GemarkungComposite gemarkung = repo.findSchlNamed( GemarkungComposite.class,
    // gemarkungSchl);
    //
    // FlurstueckComposite flurstueckTemplate = QueryExpressions.templateFor(
    // FlurstueckComposite.class );
    // BooleanExpression expr2 = QueryExpressions.and(
    // QueryExpressions.eq( flurstueckTemplate.gemarkung(), gemarkung ),
    // QueryExpressions.eq( flurstueckTemplate.nummer(), flurstueckNummer ),
    // QueryExpressions.eq( flurstueckTemplate.unterNummer(), flurstueckUnternummer )
    // );
    // Query<FlurstueckComposite> flurstuecke = repo.findEntities(
    // FlurstueckComposite.class, expr2, 0, 1 );
    // // if (flurstueck == null) {
    // // throw new IllegalStateException( String.format(
    // "flurstueck could not be found for %s, %d, %s",
    // // gemarkungSchl, flurstueckNummer, flurstueckUnternummer ) );
    // // }
    // return flurstuecke;
    // }

    // public static void main(String[] args) {
    // double d = 123456789.0;
    // System.out.println(String.format( "blah %f0", d ));
    // }
}