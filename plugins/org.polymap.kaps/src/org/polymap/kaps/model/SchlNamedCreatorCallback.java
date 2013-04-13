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
package org.polymap.kaps.model;

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.unitofwork.UnitOfWork;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public interface SchlNamedCreatorCallback {

    <T extends SchlNamed> T create( Class<T> type, String schl, String name );


    public class Impl
            implements SchlNamedCreatorCallback {

        private final UnitOfWork uow;


        public Impl( UnitOfWork uow ) {
            this.uow = uow;
        }


        public <T extends SchlNamed> T create( Class<T> type, String schl, String name ) {
            EntityBuilder<T> builder = uow.newEntityBuilder( type );
            builder.instance().name().set( name );
            builder.instance().schl().set( schl );
            return builder.newInstance();
        }
    }

}
