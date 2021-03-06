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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.kaps.model.NHK2010GebaeudeArtProvider;
import org.polymap.kaps.model.data.NHK2010GebaeudeArtComposite;


/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class NHK2010Test
        extends TestCase {

    private static Log log = LogFactory.getLog( NHK2010Test.class );
    
    public void testStufe5VomSchweinestall() {
        NHK2010GebaeudeArtProvider provider = NHK2010GebaeudeArtProvider.instance();
        NHK2010GebaeudeArtComposite art = provider.gebaeudeForNumber(18,3,2);
        Assert.assertEquals( 570, (int)art.getStufe5());
    }

    public void testKrankenhaus() {
        NHK2010GebaeudeArtProvider provider = NHK2010GebaeudeArtProvider.instance();
        NHK2010GebaeudeArtComposite art = provider.gebaeudeForNumber(10,1,1);
        Assert.assertEquals( 1720, (int)art.getStufe3());
        Assert.assertEquals( 2080, (int)art.getStufe4());
        Assert.assertEquals( 2765, (int)art.getStufe5());
        Assert.assertEquals( 21, (int)art.getBnk());
        Assert.assertEquals( "10.1", art.getId());
        Assert.assertEquals("Krankenhäuser und Kliniken", art.getName());
    }
    

    public void testGarageById() {
        NHK2010GebaeudeArtProvider provider = NHK2010GebaeudeArtProvider.instance();
        NHK2010GebaeudeArtComposite art = provider.gebaeudeForId("14.3");
        Assert.assertEquals( 560, (int)art.getStufe3());
    }
    

    public void testNHK() {
        NHK2010GebaeudeArtProvider provider = NHK2010GebaeudeArtProvider.instance();
        NHK2010GebaeudeArtComposite art = provider.gebaeudeForId("1.01");
        Assert.assertEquals( 835, (int)art.getStufe3());
        Assert.assertEquals( 1005, (int)art.getStufe4());

        Assert.assertEquals( 920.0d, art.calculateNHKFor( "3.5" ));
        Assert.assertEquals( 1005.0d, art.calculateNHKFor( "4.0" ));
        
    }
}
