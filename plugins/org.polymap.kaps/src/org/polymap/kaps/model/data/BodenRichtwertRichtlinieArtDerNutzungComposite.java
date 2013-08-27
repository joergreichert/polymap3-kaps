/*
 * polymap.org Copyright 2013 Polymap GmbH. All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.kaps.model.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.common.Optional;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.QueryExpressions;

import org.polymap.core.qi4j.QiEntity;
import org.polymap.core.qi4j.event.ModelChangeSupport;
import org.polymap.core.qi4j.event.PropertyChangeSupport;

import org.polymap.kaps.importer.ImportColumn;
import org.polymap.kaps.importer.ImportTable;
import org.polymap.kaps.model.KapsRepository;
import org.polymap.kaps.model.SchlNamed;

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
@Concerns({ PropertyChangeSupport.Concern.class })
@Mixins({ BodenRichtwertRichtlinieArtDerNutzungComposite.Mixin.class, PropertyChangeSupport.Mixin.class,
        ModelChangeSupport.Mixin.class, QiEntity.Mixin.class
// JsonState.Mixin.class
})
@ImportTable("K_BORIS_KEYS")
public interface BodenRichtwertRichtlinieArtDerNutzungComposite
        extends QiEntity, PropertyChangeSupport, ModelChangeSupport, EntityComposite, SchlNamed {

    static final String NAME = "BRW-RL - Art der Nutzung";
    // CREATE TABLE K_BORIS_KEYS (
    // ART_ERGAENZUNG VARCHAR(1), // A oder E mir erstmal egal
    // ENTWZUSTAND VARCHAR(1), // 1 = R,E,B, 2 = LF, 3 = SF
    // NUMMER VARCHAR(6),
    // SORT1 INTEGER, // egal
    // SCHL VARCHAR(5),
    // BEZ VARCHAR(100)
    // );

    @Optional
    @ImportColumn("SCHL")
    Property<String> schl();


    @Optional
    @ImportColumn("NUMMER")
    Property<String> nummer();


    @Optional
    @ImportColumn("BEZ")
    Property<String> name();


    @Optional
    @ImportColumn("ENTWZUSTAND")
    Property<String> entwickungsZustand();


    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements BodenRichtwertRichtlinieArtDerNutzungComposite {

        private static Log log = LogFactory.getLog( Mixin.class );

        public Iterable<BodenRichtwertRichtlinieArtDerNutzungComposite> findBy( EntwicklungsZustandComposite ezc ) {
            String ezcSchl = ezc.schl().get();
            String entwicklungsZustand = null;
            if ("R".equals( ezcSchl ) || "E".equals( ezcSchl ) || "B".equals( ezcSchl )) {
                entwicklungsZustand = "1";
            }
            else if ("LF".equals( ezcSchl )) {
                entwicklungsZustand = "2";
            }
            else if ("SF".equals( ezcSchl )) {
                entwicklungsZustand = "1";
            }
            else {
                throw new IllegalStateException( "no mapping found for " + ezcSchl );
            }
            return KapsRepository.instance().findEntities(
                    BodenRichtwertRichtlinieArtDerNutzungComposite.class,
                    QueryExpressions.eq( QueryExpressions.templateFor( BodenRichtwertRichtlinieArtDerNutzungComposite.class )
                            .entwickungsZustand(), entwicklungsZustand ), 0, -1 );
        }
    }

}
