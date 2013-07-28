package org.polymap.kaps.importer;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.healthmarketscience.jackcess.Database;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.runtime.SubMonitor;

import org.polymap.kaps.model.data.BodenRichtwertRichtlinieComposite;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class MdbImportBewertungenOperation
        extends AbstractMdbImportOperation {

    private static Log log = LogFactory.getLog( MdbImportBewertungenOperation.class );


    public MdbImportBewertungenOperation( File dbFile, String[] tableNames ) {
        super( dbFile, tableNames, "Kaufpreissammlung Bewertungen importieren" );
    }


    protected IStatus doExecute( IProgressMonitor monitor, IAdaptable info )
            throws Exception {
        File parentFolder = new File( "kaps" );
        parentFolder.mkdirs();

        monitor.beginTask( getLabel(), 12000 );
        final Database db = Database.open( dbFile );
        try {
            SubMonitor sub = null;
            sub = new SubMonitor( monitor, 10 );
            
            importEntity( db, monitor, BodenRichtwertRichtlinieComposite.class, null );
        }
        finally {
            db.close();
        }

        return Status.OK_STATUS;
    }
}
