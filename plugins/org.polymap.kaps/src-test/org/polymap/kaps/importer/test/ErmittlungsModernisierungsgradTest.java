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
package org.polymap.kaps.importer.test;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.polymap.kaps.model.data.ErmittlungModernisierungsgradComposite;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class ErmittlungsModernisierungsgradTest
        extends TestCase {

    public void testGrad4() {
        Assert.assertEquals( 41, Math.round( ErmittlungModernisierungsgradComposite.Mixin.berechneRND( 4.0d, 40, 80 ) ) );
        Assert.assertEquals( 62, Math.round( ErmittlungModernisierungsgradComposite.Mixin.berechneRND( 24.0d, 40, 80 ) ) );
    }


    public void testGrad24() {
        Assert.assertEquals( 59, Math.round( ErmittlungModernisierungsgradComposite.Mixin.berechneRND( 24.0d, 35, 75 ) ) );
        Assert.assertEquals( 70, Math.round( ErmittlungModernisierungsgradComposite.Mixin.berechneRND( 24.0d, 5, 75 ) ) );
    }


    public void testGrad1() {
        Assert.assertEquals( 12, Math.round( ErmittlungModernisierungsgradComposite.Mixin.berechneRND( 1.0d, 1600, 80 ) ) );
    }

    public void testGrad48() {
        Assert.assertEquals( 30, Math.round( ErmittlungModernisierungsgradComposite.Mixin.berechneRND( 6.0d, 165, 80 ) ) );
    }
}
