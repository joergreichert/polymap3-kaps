/* 
 * polymap.org
 * Copyright 2013 Polymap GmbH. All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.kaps.importer.test;

import java.io.File;

import junit.framework.TestCase;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.polymap.core.runtime.DefaultSessionContextProvider;
import org.polymap.core.runtime.ISessionContextProvider;
import org.polymap.core.runtime.SessionContext;
import org.polymap.kaps.importer.MdbImportOperation;
import org.qi4j.bootstrap.Energy4Java;

/**
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class ImporterTest extends TestCase {


	public void testCompleteImport() {
		DefaultSessionContextProvider contextProvider = new DefaultSessionContextProvider();
        SessionContext.addProvider( contextProvider );
        contextProvider.mapContext("test", true);
        
        //Energy4Java.setServiceLoader(new )
		// wahrscheinlihc muss ich das Qi4jModule und den KapsRepositoryAssembler vorher per OSGi initialisieren
		File dbFile = new File("kaufdat.mdb");
		MdbImportOperation mdbImportOperation = new MdbImportOperation(dbFile, null);
		try {
			mdbImportOperation.execute(new NullProgressMonitor(), null);
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
