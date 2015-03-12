package org.polymap.kaps.importer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import java.io.File;
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

import org.polymap.core.model.CompletionException;
import org.polymap.core.runtime.entity.ConcurrentModificationException;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.data.BelastungComposite;
import org.polymap.kaps.model.data.FlurComposite;
import org.polymap.kaps.model.data.FlurstueckComposite;
import org.polymap.kaps.model.data.GebaeudeComposite;
import org.polymap.kaps.model.data.GemarkungComposite;
import org.polymap.kaps.model.data.NutzungComposite;
import org.polymap.kaps.model.data.RichtwertzoneComposite;
import org.polymap.kaps.model.data.StrasseComposite;
import org.polymap.kaps.model.data.WohnungComposite;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class MdbFixFlurstueckWohnungRelationOperation
        extends AbstractMdbImportOperation {

    private static Log log = LogFactory.getLog( MdbFixFlurstueckWohnungRelationOperation.class );


    public MdbFixFlurstueckWohnungRelationOperation( File dbFile, String[] tableNames ) {
        super( dbFile, tableNames, "Kaufpreissammlung Verträge an Wohnungen korrigieren" );
    }


    protected IStatus doExecute0( IProgressMonitor monitor, IAdaptable info )
            throws Exception {
        File parentFolder = new File( "kaps" );
        parentFolder.mkdirs();

        monitor.beginTask( getLabel(), 3300 );
        final Database db = Database.open( dbFile );
        try {
            Table table = db.getTable( "K_EWOHN" );
            monitor.beginTask( "Tabelle: " + table.getName(), table.getRowCount() );

            Map<String, Set<WohnungComposite>> wrongWohnungen = new HashMap<String, Set<WohnungComposite>>();
            // Set<String> duplicateRWZ = new LinkedHashSet<String>();

            // data rows
            Map<String, Object> row = null;
            while ((row = table.getNextRow()) != null) {
                final Map<String, Object> builderRow = row;

                Double eingangsNummer = (Double)builderRow.get( "EINGANGSNR" );
                Integer objektNummer = (Integer)builderRow.get( "OBJEKTNR" );
                Integer gebaeudeNummer = (Integer)builderRow.get( "GEBNR" );
                Integer wohnungsNummer = (Integer)builderRow.get( "WOHNUNGSNR" );
                Integer wohnungsFortfuehrung = (Integer)builderRow.get( "FORTF" );

                // wenn eingangsnummer == null und kein flurstück verlinkt oder
                // flurstück mit Vertrag
                if (eingangsNummer == null) {
                    WohnungComposite wohnung = WohnungComposite.Mixin.forKeys( objektNummer, gebaeudeNummer,
                            wohnungsNummer, wohnungsFortfuehrung );
                    if (wohnung != null) {
                        FlurstueckComposite aktuellesFlurstueck = wohnung.flurstueck().get();
//                        String gemarkung = (String)builderRow.get( "GEM" );
//                        Integer flurNummer = (Integer)builderRow.get( "FLSTNR" );
//                        String flurUnternummer = (String)builderRow.get( "FLSTNRU" );

                        // --> check ob flurstück ohne vertrag existiert
                        if (aktuellesFlurstueck == null || aktuellesFlurstueck.vertrag().get() != null) {
                            String key = objektNummer + ";" + gebaeudeNummer;
                            Set<WohnungComposite> wohnungen = wrongWohnungen.get( key );
                            if (wohnungen == null) {
                                wohnungen = new HashSet<WohnungComposite>();
                                wrongWohnungen.put( key, wohnungen );
                            }
                            log.info( "Wohnung kaputt: " +  wohnung.schl().get());
                            wohnungen.add( wohnung );
                        }
                    }
                    else {
                        throw new IllegalStateException( String.format( "Keine Wohnung gefunden für %s/%s/%s/%s",
                                objektNummer, gebaeudeNummer, wohnungsNummer, wohnungsFortfuehrung ) );
                    }
                }
            }
            // alle fehlenden Grundstücke anlegen
            importingMissingWohnungen( db, monitor, wrongWohnungen );

            // --> neues Flurstück anlegen
            // --> flurstück an Wohnung
            // --> flurstück an Gebäude

            KapsRepository.instance().commitChanges();
            monitor.done();
        }
        finally {
            db.close();
        }

        return Status.OK_STATUS;
    }


    /**
     *
     * @param monitor
     * @param db
     * @param wrongWohnungen
     * @throws ConcurrentModificationException
     * @throws CompletionException
     * @throws IOException
     */
    private void importingMissingWohnungen( Database db, IProgressMonitor monitor,
            Map<String, Set<WohnungComposite>> wrongWohnungen )
            throws CompletionException, ConcurrentModificationException, IOException {
        Table table = db.getTable( "K_EOBJF" );
//        monitor.beginTask( "Tabelle: " + table.getName(), table.getRowCount() );
        // data rows
        Map<String, Object> builderRow = null;
//        int count = 0;
        while ((builderRow = table.getNextRow()) != null) {
            Integer objektNummer = (Integer)builderRow.get( "OBJEKTNR" );
            Integer gebaeudeNummer = (Integer)builderRow.get( "GEBNR" );
           

            if (wrongWohnungen.get( objektNummer + ";" + gebaeudeNummer ) != null) {
                FlurstueckComposite flurstueck = findFlurstueck( builderRow );

                if (flurstueck == null) {
                    log.info( "Flurstueck für " + objektNummer + ";" + gebaeudeNummer + " angelegt" );
                    flurstueck = repo.newEntity( FlurstueckComposite.class, null );

                    flurstueck.gemarkung().set( findSchlNamed( GemarkungComposite.class, builderRow, "GEM", false ) );
                    // Integer
                    flurstueck.hauptNummer().set( (Integer)builderRow.get( "FLSTNR" ) );
                    flurstueck.unterNummer().set( (String)builderRow.get( "FLSTNRU" ) );
                    // N, J, NULL
                    flurstueck.erbbaurecht().set( (String)builderRow.get( "ERBBAUR" ) );
                    // 00 - 06, NULL
                    flurstueck.belastung().set( findSchlNamed( BelastungComposite.class, builderRow, "BELASTUNG" ) );
                    flurstueck.flaeche().set( (Double)builderRow.get( "GFLAECHE" ) );
                    flurstueck.baublock().set( (String)builderRow.get( "BAUBLOCK" ) );
                    flurstueck.kartenBlatt().set( (String)builderRow.get( "KARTBLATT" ) );
                    flurstueck.hausnummer().set( (String)builderRow.get( "HAUSNR" ) );
                    flurstueck.hausnummerZusatz().set( (String)builderRow.get( "HZUSNR" ) );
                    flurstueck.kartenBlattNummer().set( (String)builderRow.get( "KARTBLATTN" ) );

                    flurstueck.flur().set( findSchlNamed( FlurComposite.class, "000" ) );
                    flurstueck.nutzung().set( findSchlNamed( NutzungComposite.class, builderRow, "NUTZUNG", false ) );
                    flurstueck.strasse().set( findSchlNamed( StrasseComposite.class, builderRow, "STRNR", false ) );
                    try {
                        flurstueck.richtwertZone().set(
                                findSchlNamed( RichtwertzoneComposite.class, builderRow, "RIZONE", false ) );
                    }
                    catch (IllegalStateException ise) {
                        log.error( "Keine Richtwertzone  gefunden für " + builderRow.get( "RIZONE" ) + "\n" );
                    }

                    // gebäude suchen und flurstück daran setzen
                    Integer objektNr = (Integer)builderRow.get( "OBJEKTNR" );
                    Integer gebNr = (Integer)builderRow.get( "GEBNR" );
                    GebaeudeComposite gebaeude = GebaeudeComposite.Mixin.forKeys( objektNr, gebNr );
                    if (gebaeude == null) {
                        log.error( "Kein Gebäude gefunden für " + objektNr + "/" + gebNr + "\n" );
                    }
                    else {
                        // if (!gebaeude.flurstuecke().contains( flurstueck )) {
                        gebaeude.flurstuecke().add( flurstueck );
                        // }
                    }
                }

                // nun an allen Wohnungen setzen
                for (WohnungComposite wohnung : wrongWohnungen.get( objektNummer + ";" + gebaeudeNummer )) {
                    wohnung.flurstueck().set( flurstueck );
                    log.info( "Flurstueck für Wohnung " + wohnung.schl().get() + " aktualisiert" );
                }
            }

//            log.info( "Imported and committed: K_EOBJF -> " + count );
//            monitor.done();
        }
//        KapsRepository.instance().commitChanges();
    }


    /**
     *
     * @param builderRow
     * @return
     */
    private FlurstueckComposite findFlurstueck( Map<String, Object> builderRow ) {
        GemarkungComposite gemarkung =  findSchlNamed( GemarkungComposite.class, builderRow, "GEM", false );
        Integer nummer = (Integer)builderRow.get( "FLSTNR" );
        String unternummer = (String)builderRow.get( "FLSTNRU" );

        FlurstueckComposite flurTemplate = QueryExpressions.templateFor( FlurstueckComposite.class );
        BooleanExpression expr = QueryExpressions.and( QueryExpressions.eq( flurTemplate.gemarkung(), gemarkung ),
                QueryExpressions.eq( flurTemplate.hauptNummer(), nummer ),
                QueryExpressions.eq( flurTemplate.unterNummer(), unternummer ) );
        
        Iterator<FlurstueckComposite> it = KapsRepository.instance().findEntities( FlurstueckComposite.class, expr, 0, -1 ).iterator();
        while (it.hasNext()) {
            FlurstueckComposite flurstueck = it.next();
            if (flurstueck.vertrag().get() == null) {
                log.info( "vertragloses Flurstueck für " + gemarkung.schl().get() + ";" + nummer + ";" + unternummer + " gefunden" );
                return flurstueck;
            }
        }
        return null;
        

    }

}
