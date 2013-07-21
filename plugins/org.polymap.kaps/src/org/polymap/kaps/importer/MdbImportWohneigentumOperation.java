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
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;

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
public class MdbImportWohneigentumOperation
        extends AbstractMdbImportOperation {

    private static Log log = LogFactory.getLog( MdbImportWohneigentumOperation.class );


    public MdbImportWohneigentumOperation( File dbFile, String[] tableNames ) {
        super( dbFile, tableNames, "Kaufpreissammlung Wohneigentum importieren" );
    }


    protected IStatus doExecute( IProgressMonitor monitor, IAdaptable info )
            throws Exception {
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
            importEntity( db, sub, WohnungComposite.class, new EntityCallback<WohnungComposite>() {

                @Override
                public void fillEntity( WohnungComposite entity, Map<String, Object> builderRow )
                        throws IOException {

                    Double eingangsnummer = (Double)builderRow.get( "EINGANGSNR" );
                    if (eingangsnummer != null) {
                        VertragComposite vertragTemplate = QueryExpressions.templateFor( VertragComposite.class );
                        BooleanExpression expr = QueryExpressions.eq( template.eingangsNr(), number );
                        Query<VertragComposite> matches = KapsRepository.instance().findEntities( VertragComposite.class, expr,
                                0, 1 );
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

                    // flurstück finden
                    if (entity.eingangsNummer().get() != null) {
                        
                    }
                    
- vertrag suchen, flurstueck an vertrag suchen, flurstück an wohnung setzen, 
                    - gebaeude suchen
                    - flurstücke an gebaeude suchen
                    - bzw. flurstücke am Vertrag suchen
                    - flurstück an Wohnung setzen
                    - gebaeude an wohnung setzen
                    
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
            importK_EOBJF(db, sub, allBelastungArt);
            
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

            allGebaeude.clear();
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


    protected void importK_EOBJF( Database db, IProgressMonitor monitor, Map<String, BelastungComposite> allBelastungArt )
            throws Exception {
        Table table = db.getTable( "K_EOBJF" );
        monitor.beginTask( "Tabelle: " + table.getName(), table.getRowCount() );

        // data rows
        Map<String, Object> builderRow = null;
        int count = 0;
        while ((builderRow = table.getNextRow()) != null) {

            String gemarkungSchl = (String)builderRow.get( "GEM" );
            // Integer
            Integer flurstueckNummer = (Integer)builderRow.get( "FLSTNR" );
            String flurstueckUnternummer = (String)builderRow.get( "FLSTNRU" );
            // N, J, NULL
            String erbbaurecht = (String)builderRow.get( "ERBBAUR" );
            // 00 - 06, NULL
            BelastungComposite belastung = find( allBelastungArt, builderRow, "BELASTUNG" );

            FlurstueckComposite flurstueck = findFlurstueck( gemarkungSchl, flurstueckNummer, flurstueckUnternummer );
            flurstueck.erbbaurecht().set( erbbaurecht );
            flurstueck.belastung().set( belastung );

            // gebäude suchen und flurstück daran setzen
            Integer objektNr = (Integer)builderRow.get( "OBJEKTNR" );
            Integer objektFort = (Integer)builderRow.get( "FORTF" );
            Integer gebNr = (Integer)builderRow.get( "GEBNR" );
            Integer gebFort = (Integer)builderRow.get( "GEBNRFORTF" );
            GebaeudeComposite gebaeudeTemplate = QueryExpressions.templateFor( GebaeudeComposite.class );
            BooleanExpression expr3 = QueryExpressions.and(
                    QueryExpressions.eq( gebaeudeTemplate.objektNummer(), objektNr ),
                    QueryExpressions.eq( gebaeudeTemplate.objektFortfuehrung(), objektFort ),
                    QueryExpressions.eq( gebaeudeTemplate.gebaeudeNummer(), gebNr ),
                    QueryExpressions.eq( gebaeudeTemplate.gebaeudeFortfuehrung(), gebFort ) );
            GebaeudeComposite gebaeude = repo.findEntities( GebaeudeComposite.class, expr3, 0, 1 ).find();
            if (!gebaeude.flurstuecke().contains( flurstueck )) {
                gebaeude.flurstuecke().add( flurstueck );
            }
        }
        repo.commitChanges();
        log.info( "Imported and committed: K_EOBJF -> " + count );
        monitor.done();
    }

    private FlurstueckComposite findFlurstueck( String gemarkungSchl, Integer flurstueckNummer,
            String flurstueckUnternummer ) {
        GemarkungComposite gemarkungTemplate = QueryExpressions.templateFor( GemarkungComposite.class );
        BooleanExpression expr = QueryExpressions.eq( gemarkungTemplate.schl(), gemarkungSchl );
        GemarkungComposite gemarkung = repo.findEntities( GemarkungComposite.class, expr, 0, 1 ).find();

        FlurstueckComposite flurstueckTemplate = QueryExpressions.templateFor( FlurstueckComposite.class );
        BooleanExpression expr2 = QueryExpressions.and(
                QueryExpressions.eq( flurstueckTemplate.gemarkung(), gemarkung ),
                QueryExpressions.eq( flurstueckTemplate.nummer(), flurstueckNummer ),
                QueryExpressions.eq( flurstueckTemplate.unterNummer(), flurstueckUnternummer ) );
        FlurstueckComposite flurstueck = repo.findEntities( FlurstueckComposite.class, expr2, 0, 1 ).find();
        if (flurstueck == null) {
            throw new IllegalStateException( String.format( "flurstueck could not be found for %s, %d, %s",
                    gemarkungSchl, flurstueckNummer, flurstueckUnternummer ) );
        }
        return flurstueck;
    }

}
