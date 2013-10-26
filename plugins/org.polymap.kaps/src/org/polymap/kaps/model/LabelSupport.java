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

import java.lang.reflect.Method;

import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

import org.polymap.core.model.Entity;
import org.polymap.core.qi4j.QiEntity;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
@Mixins(LabelSupport.Mixin.class)
public interface LabelSupport
        extends Entity {

    String getLabel( Property p );


    String getLabel( Association a );


    String getLabel( ManyAssociation m );


    abstract static class Mixin
            implements LabelSupport {

        @This
        private QiEntity composite;


        @Override
        public String getLabel( Association p ) {
            return getLabel( p.qualifiedName().name() );
        }


        @Override
        public String getLabel( ManyAssociation p ) {
            return getLabel( p.qualifiedName().name() );
        }


        @Override
        public String getLabel( Property p ) {
            return getLabel( p.qualifiedName().name() );
        }


        private String getLabel( String name ) {
            for (Method m : composite.getCompositeType().getMethods()) {
                if (m.getName().equals( name )) {
                    Label a = m.getAnnotation( Label.class );
                    if (a != null) {
                        return a.value();
                    }
                    else {

                        return name.substring( 0, 1 ).toUpperCase() + name.substring( 1 );
                    }
                }
            }
            throw new IllegalArgumentException( "Property " + name + " could not be found!" );
        }
    }
}
