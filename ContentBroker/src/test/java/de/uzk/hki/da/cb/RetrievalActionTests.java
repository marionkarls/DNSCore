/*

  DA-NRW Software Suite | ContentBroker
  Copyright (C) 2013 Historisch-Kulturwissenschaftliche Informationsverarbeitung
  Universität zu Köln
  Copyright (C) 2014 LVRInfoKom
  Landschaftsverband Rheinland

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package de.uzk.hki.da.cb;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.utilities.SimpleResult;

import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uzk.hki.da.model.Job;
import de.uzk.hki.da.model.Object;
import de.uzk.hki.da.model.Package;
import de.uzk.hki.da.model.PreservationSystem;
import de.uzk.hki.da.utils.NativeJavaTarArchiveBuilder;
import de.uzk.hki.da.utils.Path;
import de.uzk.hki.da.utils.TC;
import de.uzk.hki.da.utils.TESTHelper;


/**
 * The Class RetrievalActionTest.
 *
 * @author Daniel M. de Oliveira
 */
public class RetrievalActionTests {
	
	private static final Path userAreaRootPath = Path.make(TC.TEST_ROOT_CB,"RetrievalActionTests","user");
	private static final Path workAreaRootPath = Path.make(TC.TEST_ROOT_CB,"RetrievalActionTests","work");
	private static final Path outgoingFolder = Path.make(userAreaRootPath,"TEST","outgoing");
	private static final Path container = Path.make(outgoingFolder,"1.tar");
	
	private static final String objectIdentifier = "1";
	private static RetrievalAction action;

	private static PreservationSystem pSystem;
	private static Object object;

	
	
	@BeforeClass
	public static void setUpBeforeClass(){
		
		pSystem = TESTHelper.setUpPS(workAreaRootPath,userAreaRootPath,userAreaRootPath);
		
	}

	
	@AfterClass
	public static void tearDownAfterClass() throws IOException{
		;
	}
	
	
	
	/**
	 * Tear down.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@After
	public void tearDown () throws IOException {
		FileUtils.deleteDirectory(Path.makeFile(workAreaRootPath,"work/TEST/1"));
		FileUtils.deleteDirectory(Path.makeFile(userAreaRootPath,"TEST/outgoing/1"));
		Path.makeFile(userAreaRootPath,"TEST/outgoing/1.tar").delete();
		FileUtils.deleteDirectory(userAreaRootPath.toFile());
	}
	
	
	
	/**
	 */
	@Before
	public void setUp() throws Exception {

		object = TESTHelper.setUpObject(objectIdentifier, workAreaRootPath, 
				userAreaRootPath,
				userAreaRootPath);
		
		Package pkg1 = object.getLatestPackage();
		Package pkg2 = new Package();
		pkg2.setName("2");
		Package pkg3 = new Package();
		pkg3.setName("3");
		object.getPackages().add(pkg2);
		object.getPackages().add(pkg3);
		
		
		
		object.reattach();
		object.getLatestPackage().setTransientBackRefToObject(object);
		action = (RetrievalAction) wireUpAction(new RetrievalAction(),object,pSystem);
		
		

		FileUtils.copyDirectory(Path.makeFile(workAreaRootPath,"work",object.getContractor().getShort_name(),"_1"), 
				                Path.makeFile(workAreaRootPath,"work",object.getContractor().getShort_name(),"1")); 
		Path.makeFile(userAreaRootPath,"TEST","outgoing").mkdirs();
		
		
		pkg1.scanRepRecursively("1+a");
		pkg1.scanRepRecursively("1+b");
		pkg2.scanRepRecursively("2+a");
		pkg2.scanRepRecursively("2+b");
		pkg3.scanRepRecursively("3+a");
		pkg3.scanRepRecursively("3+b");
	}
	

	
	@Test
	public void testContainerAndBag() throws Exception{

		action.implementation();
		assertFalse(Path.makeFile(workAreaRootPath,"TEST/1_").exists());
		assertTrue( Path.makeFile(userAreaRootPath,"TEST/outgoing/1.tar").exists() );
		unpack();
		
		BagFactory bagFactory = new BagFactory();
		Bag bag = bagFactory.createBag(Path.makeFile(userAreaRootPath,"TEST/outgoing/1/"));
		SimpleResult result = bag.verifyValid();
		assertTrue(result.isSuccess());
	}
	
	
	
	
	/**
	 * Test.
	 * @throws Exception the exception
	 */
	@Test
	public void testNormalRetrieval() throws Exception {
		
		action.implementation();
		unpack();
		
		assertTrue(Path.makeFile(outgoingFolder,objectIdentifier,"data/folder1/pic5.txt").exists());
		assertTrue(Path.makeFile(outgoingFolder,objectIdentifier,"data/folder2/pic5.txt").exists());
		assertTrue(Path.makeFile(outgoingFolder,objectIdentifier,"data/pic1.txt").exists());
		assertTrue(Path.makeFile(outgoingFolder,objectIdentifier,"data/pic2.txt").exists());
		assertTrue(Path.makeFile(outgoingFolder,objectIdentifier,"data/pic3.txt").exists());
		assertTrue(Path.makeFile(outgoingFolder,objectIdentifier,"data/pic4.txt").exists());
		
		
	}

	
	@Test
	public void testSpecialRetrieval() throws Exception{
		
		action.getJob().setQuestion("RETRIEVE:1,3");
		action.implementation();
		
		unpack();
		assertTrue(Path.makeFile(outgoingFolder,objectIdentifier,"data/1+a/pic1.txt").exists());
		assertTrue(Path.makeFile(outgoingFolder,objectIdentifier,"data/1+b/pic2.txt").exists());
		assertTrue(Path.makeFile(outgoingFolder,objectIdentifier,"data/1+b/premis.xml").exists());
		assertTrue(Path.makeFile(outgoingFolder,objectIdentifier,"data/3+a/pic1.txt").exists());
		assertTrue(Path.makeFile(outgoingFolder,objectIdentifier,"data/3+b/folder1/pic5.txt").exists());
		assertTrue(Path.makeFile(outgoingFolder,objectIdentifier,"data/3+b/folder2/pic5.txt").exists());
		assertTrue(Path.makeFile(outgoingFolder,objectIdentifier,"data/3+b/pic3.txt").exists());
		assertTrue(Path.makeFile(outgoingFolder,objectIdentifier,"data/3+b/premis.xml").exists());

		assertFalse(Path.makeFile(outgoingFolder,objectIdentifier,"data/2+a/pic3.txt").exists());
		assertFalse(Path.makeFile(outgoingFolder,objectIdentifier,"data/2+b/pic4.txt").exists());
	}
	
	
	/**
	 * @throws IOException 
	 */
	@Test
	public void testCleanup() throws IOException{
		action.implementation();
		
		assertFalse(Path.makeFile(workAreaRootPath,"work","TEST/1").exists());
	}
	

	@Test 
	public void testRollback() throws IOException{
		action.implementation();
		action.rollback();
		assertFalse(container.toFile().exists());
	}
	
	


	private static void unpack() throws Exception{
		NativeJavaTarArchiveBuilder tar = new NativeJavaTarArchiveBuilder();
		tar.unarchiveFolder(Path.makeFile(outgoingFolder,"1.tar"),
				            outgoingFolder.toFile());
	}
	
	private static AbstractAction wireUpAction(AbstractAction action,Object o,PreservationSystem ps){
		
		Job job = new Job(); 
		job.setObject(o);
		action.setObject(o);
		action.setJob(job);
		action.setLocalNode(ps.getNodes().iterator().next());
		action.setPSystem(ps);
		return action;
	}
	
	
	
}