package org.polymap.kaps.importer;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import java.io.File;

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

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.data.RichtwertzoneComposite;
import org.polymap.kaps.ui.form.EingangsNummerFormatter;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class MdbFindDuplicateRWZOperation
        extends AbstractMdbImportOperation {

    private static Log log = LogFactory.getLog( MdbFindDuplicateRWZOperation.class );


    public MdbFindDuplicateRWZOperation( File dbFile, String[] tableNames ) {
        super( dbFile, tableNames, "Kaufpreissammlung Bewertungen importieren" );
    }


    protected IStatus doExecute0( IProgressMonitor monitor, IAdaptable info )
            throws Exception {
        File parentFolder = new File( "kaps" );
        parentFolder.mkdirs();

        monitor.beginTask( getLabel(), 3300 );
        final Database db = Database.open( dbFile );
        try {
            Table table = db.getTable( "K_RIWE" );
            monitor.beginTask( "Tabelle: " + table.getName(), table.getRowCount() );

            Map<String, String> foundRWZ = new TreeMap<String, String>();
            Set<String> duplicateRWZ = new TreeSet<String>();
            
            // data rows
            Map<String, Object> row = null;
            while ((row = table.getNextRow()) != null) {
                final Map<String, Object> builderRow = row;

                String zone = (String)builderRow.get( "RIZONE" );
                String currentGemeinde = "" + (Integer)builderRow.get( "GEMEINDE" );
                if (foundRWZ.get( zone ) != null && !foundRWZ.get( zone ).equals( currentGemeinde )) {
                    duplicateRWZ.add( zone + ";" + currentGemeinde );
                    duplicateRWZ.add( zone + ";" + foundRWZ.get( zone ) );
                } else {
                    foundRWZ.put( zone, currentGemeinde );
                }    
                
                if (monitor.isCanceled()) {
                    throw new RuntimeException( "Operation canceled." );
                }
            }
            RichtwertzoneComposite template = QueryExpressions.templateFor( RichtwertzoneComposite.class );
            
            Map<String, String> notImported = new TreeMap<String, String>();
            int count = 0;
            for (String composite : duplicateRWZ) {
                count++;
                String zone = composite.split( ";" )[0];
                String gemeinde = composite.split( ";" )[1];
                
                BooleanExpression expr = QueryExpressions.eq( template.schl(), zone );
                RichtwertzoneComposite rwz = KapsRepository.instance().findEntities( RichtwertzoneComposite.class,
                        expr, 0, -1 ).find();
                if (rwz != null && gemeinde.equals( rwz.gemeinde().get().schl().get())) {
                    log.info( count + ". doppelte Zone " + composite + " wurde importiert" );
                } else {
                    log.info( count + ". doppelte Zone " + composite + " wurde nicht importiert" );
                    notImported.put( zone, gemeinde );
                } 
            }
            
            // lookup in der DB nach Flurstücken
            table = db.getTable( "FLURZWI" );
            monitor.beginTask( "Tabelle: " + table.getName(), table.getRowCount() );
            
            // data rows
            row = null;
            count = 0;
            while ((row = table.getNextRow()) != null) {
                final Map<String, Object> builderRow = row;

                String zone = (String)builderRow.get( "RIZONE" );
                String gemeinde = "" + (Integer)builderRow.get( "GEMEINDE" );
                if (notImported.get( zone ) != null && notImported.get( zone ).equals( gemeinde )) {
                    count++;
                    log.info( count + ". Flurstück " + EingangsNummerFormatter.format( ((Double)builderRow.get( "EINGANGSNR" )).intValue()) + ";" + builderRow.get( "FLSTNR1" ) + ";" + builderRow.get( "FLSTNR1U" ) + " wurde falsch importiert" );
                }    
                
                if (monitor.isCanceled()) {
                    throw new RuntimeException( "Operation canceled." );
                }
            }
            
            monitor.done();
        }
        finally {
            db.close();
        }

        return Status.OK_STATUS;
    }
}
