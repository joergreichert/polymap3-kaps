/*
 * Copyright 2008 Niclas Hedhman. Copyright 2008 Edward Yakop.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.
 * 
 * See the License for the specific language governing permissions and limitations
 * under the License.
 */
package org.qi4j.api.entity.association.kaps;

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.property.Property;

/**
 * {@code ComputedAssociationInstance} is the base implementation of {@link Property}
 * .
 */
public abstract class ComputedAssociationInstance<T>
        extends AbstractAssocationInstance<T> {

    /**
     * Construct an instance of {@code ComputedAssociationInstance}.
     * 
     * @param aPropertyInfo The property info. This argument must not be {@code null}
     *        .
     * 
     * @throws IllegalArgumentException Thrown if the specified {@code aPropertyInfo}
     *         argument is {@code null}.
     */
    protected ComputedAssociationInstance( AssociationInfo aAssociationInfo )
            throws IllegalArgumentException {
        super( aAssociationInfo );
    }


    /**
     * This is the method to implement.
     * 
     * @return Returns null by default.
     */
    public abstract T get();


    /**
     * Throws {@link IllegalArgumentException} exception.
     * 
     * @param anIgnoredValue This value is ignored.
     * 
     * @throws IllegalArgumentException Thrown by default.
     */
    public void set( T anIgnoredValue )
            throws IllegalArgumentException, IllegalStateException {
        QualifiedName qualifiedName = qualifiedName();
        throw new IllegalStateException( "Association [" + qualifiedName + "] is read-only" );
    }
}