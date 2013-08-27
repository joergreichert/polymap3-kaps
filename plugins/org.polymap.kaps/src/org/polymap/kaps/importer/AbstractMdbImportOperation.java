package org.polymap.kaps.importer;

import java.util.Date;
import java.util.List;
import java.util.Map;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.composite.Composite;
import org.qi4j.api.entity.EntityComposite;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;

import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.qi4j.QiModule.EntityCreator;
import org.polymap.core.qi4j.event.AbstractModelChangeOperation;

import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.SchlNamed;
import org.polymap.kaps.model.data.RichtwertzoneZeitraumComposite;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public abstract class AbstractMdbImportOperation
        extends AbstractModelChangeOperation
        implements IUndoableOperation {

    private static Log       log = LogFactory.getLog( AbstractMdbImportOperation.class );

    protected File           dbFile;

    protected String[]       tableNames;

    protected KapsRepository repo;


    public AbstractMdbImportOperation( File dbFile, String[] tableNames, String title ) {
        super( title );
        this.dbFile = dbFile;
        this.tableNames = tableNames;
        this.repo = KapsRepository.instance();
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


    protected final <T extends SchlNamed> T findSchlNamed( Class<T> type, Map<String, Object> row, String columnName ) {
        return findSchlNamed( type, row, columnName, false );
    }


    protected final <T extends SchlNamed> T findSchlNamed( Class<T> type, Map<String, Object> row, String columnName,
            boolean nullAllowed ) {
        Object schl = row.get( columnName );
        if (schl != null) {
            if (schl instanceof Double) {
                schl = (Integer)((Double)schl).intValue();
            }
            String schlStr = schl.toString();
            T obj = repo.findSchlNamed( type, schlStr );
            if (obj == null && !nullAllowed) {
                throw new IllegalStateException( "no " + columnName + " found for schl '" + schl + "'!" );
            }
            return obj;
        }
        return null;
    }


    protected final <T extends Composite> T find( Map<String, T> all, Map<String, Object> row, String columnName ) {
        return find( all, row, columnName, false );
    }


    protected final <T extends Composite> T find( Map<String, T> all, Map<String, Object> row, String columnName,
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


    protected final <T extends SchlNamed> T findBySchl( Class<T> type, Map<String, Object> row, String columnName,
            boolean nullAllowed ) {
        String schl = (String)row.get( columnName );
        if (schl != null && !schl.isEmpty()) {
            T named = repo.findSchlNamed( type, schl );
            if (named == null && !nullAllowed) {
                throw new IllegalStateException( "no " + type + " found for schl '" + schl + "'!" );
            }
            return named;
        }
        return null;
    }


    protected final Boolean getBooleanValue( Map<String, Object> builderRow, String rowName ) {
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
                // repo.commitChanges();
            }
        }
        repo.commitChanges();
        log.info( "Imported and committed: " + type + " -> " + count );
        monitor.done();
    }


    public final static void printSchema( Table table ) {
        log.info( "Table: " + table.getName() );
        for (Column col : table.getColumns()) {
            log.info( "    column: " + col.getName() + " - " + col.getType() );
        }
    }


    protected final Table table( Database db, Class type )
            throws IOException {
        ImportTable a = (ImportTable)type.getAnnotation( ImportTable.class );
        assert a != null;
        return db.getTable( a.value() );
    }


    protected final Object columnValue( Table table, Map<String, Object> row, String col ) {
        if (table.getColumn( col ) == null) {
            throw new IllegalArgumentException( "No such column: " + col );
        }
        return row.get( col );
    }
}
