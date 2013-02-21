package org.polymap.kaps.model.constant;

import org.polymap.rhei.model.ConstantWithSynonyms;

/**
 * Provides 'Erhaltungszustand' constants.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Verkaeuferkreis
        extends ConstantWithSynonyms<String> {

    /** Provides access to the elements of this type. */
    public static final Type<Verkaeuferkreis,String> all = new Type<Verkaeuferkreis,String>();
    
    public static final Verkaeuferkreis unbestimmt = new Verkaeuferkreis( 0, "0 - noch nicht bestimmt", "noch nicht bestimmt" );

    public static final Verkaeuferkreis guenstig = new Verkaeuferkreis( 1, "1 - günstig", "günstig" );

    public static final Verkaeuferkreis unzureichend = new Verkaeuferkreis( 2, "2 - ungünstig/unzureichend", "unzureichend" );

    public static final Verkaeuferkreis schlecht = new Verkaeuferkreis( 3, "3 - ungünstig/schlecht", "schlecht" );

    
    // instance *******************************************
    
	private String description;
    
    private Verkaeuferkreis( int id, String label, String description, String... synonyms ) {
        super( id, label, synonyms );
        this.description = description;
        all.add( this );
    }

    protected String normalizeValue( String value ) {
        return value.trim().toLowerCase();
    }
    
}
