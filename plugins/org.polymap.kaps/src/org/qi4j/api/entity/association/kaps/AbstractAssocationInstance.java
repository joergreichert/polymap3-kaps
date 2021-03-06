/*
 * Copyright 2008 Niclas Hedhman.
 * Copyright 2008 Edward Yakop.
 * Copyright 2008 Rickard Öberg.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.api.entity.association.kaps;

import static org.qi4j.api.util.NullArgumentException.validateNotNull;

import java.lang.reflect.Type;

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.association.Association;

/**
 * {@code AbstractAssocationInstance} is the base implementation of {@link org.qi4j.api.property.Property}.
 */
public abstract class AbstractAssocationInstance<T>
    implements Association<T>
{
    protected final AssociationInfo associationInfo;

    /**
     * Construct an instance of {@code ComputedAssociationInstance}.
     *
     * @param aAssociationInfo The property info. This argument must not be {@code null}.
     *
     * @throws IllegalArgumentException Thrown if the specified {@code aAssociationInfo} argument is {@code null}.
     */
    protected AbstractAssocationInstance( AssociationInfo aAssociationInfo )
        throws IllegalArgumentException
    {
        validateNotNull( "aAssociationInfo", aAssociationInfo );
        associationInfo = aAssociationInfo;
    }

    /**
     * Returns the property info given {@code anInfoType} argument.
     *
     * @param anInfoType The info type.
     *
     * @return Property info given {@code anInfoType} argument.
     */
    // Was it a mistake to have another T here? (I think so...)
    public final <V> V metaInfo( Class<V> anInfoType )
    {
        return associationInfo.metaInfo( anInfoType );
    }

    /**
     * Returns the qualified name of this {@code Property}. Must not return {@code null}.
     *
     * @return The qualified name of this {@code Property}.
     */
    public final QualifiedName qualifiedName()
    {
        return associationInfo.qualifiedName();
    }

    public final Type type()
    {
        return associationInfo.type();
    }

    public boolean isImmutable()
    {
        return associationInfo.isImmutable();
    }

    public boolean isComputed()
    {
        return associationInfo.isComputed();
    }


    public boolean isAggregated()
    {
        return associationInfo.isAggregated();
    }
    
    /**
     * Perform equals with {@code o} argument.
     * <p/>
     * The definition of equals() for the ComputedProperty is that if the Value, subclass and all the metaInfo are
     * equal, then th
     *
     * @param o The other object to compare.
     *
     * @return Returns a {@code boolean} indicator whether this object is equals the other.
     */
    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        Association<?> that = (Association<?>) o;

        if( !type().equals( that.type() ) )
        {
            return false;
        }
        T value = get();
        if( value == null )
        {
            return that.get() == null;
        }
        return value.equals( that.get() );
    }

    /**
     * Calculate hash code.
     *
     * @return the hashcode of this {@code ComputedAssociationInstance} instance.
     */
    public int hashCode()
    {
        int hash = getClass().hashCode();
        if( associationInfo != null )
        {
            hash = associationInfo.type().hashCode();
        }
        hash = hash * 19;
        T value = get();
        if( value != null )
        {
            hash = hash + value.hashCode() * 13;
        }
        return hash;
    }
}