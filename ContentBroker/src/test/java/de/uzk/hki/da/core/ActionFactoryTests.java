/*
  DA-NRW Software Suite | ContentBroker
  Copyright (C) 2013 Historisch-Kulturwissenschaftliche Informationsverarbeitung
  Universität zu Köln

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

package de.uzk.hki.da.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.hibernate.Session;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import de.uzk.hki.da.cb.AbstractAction;
import de.uzk.hki.da.model.CentralDatabaseDAO;
import de.uzk.hki.da.model.Contractor;
import de.uzk.hki.da.model.Job;
import de.uzk.hki.da.model.Object;
import de.uzk.hki.da.model.Package;
import de.uzk.hki.da.model.Node;



/**
 * The Class ActionFactoryTests.
 */
public class ActionFactoryTests {
	
	/** The base dir path. */
	String baseDirPath="src/test/resources/core/ActionFactoryTests/";
	
	/** The factory. */
	private ActionFactory factory;
	
	/** The c. */
	private Contractor c = new Contractor();
	
	/**
	 * Sets the up.
	 */
	@Before
	public void setUp() {
		c.setShort_name("csn");
		
		HibernateUtil.init("src/main/conf/hibernateCentralDB.cfg.xml.inmem");
		
		Session session = HibernateUtil.openSession();
		session.beginTransaction();
		CentralDatabaseDAO dao = new CentralDatabaseDAO();
		session.save(new Node("testnode","01-testnode"));
		
		ActionCommunicatorService acs = new ActionCommunicatorService();
		
		session.getTransaction().commit();	
		session.close();		
		
		FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext(baseDirPath+"action-definitions.xml");
		factory = new ActionFactory();
		factory.setApplicationContext(context);
		factory.setDao(dao);
		factory.setActionCommunicatorService(acs);
		factory.setIrodsZonePath("/da-nrw/");
		factory.setActionRegistry((ActionRegistry)context.getBean("actionRegistry"));
	}
	
	/**
	 * Test build next action.
	 */
	@Test
	public void testBuildNextAction() {
		
		CentralDatabaseDAO dummyDao = mock(CentralDatabaseDAO.class);

		Job j = new Job("node", "450"); 
		Object o = new Object();
		Package p = new Package();
		o.getPackages().add(p);
		j.setObject(o);
		
		when(dummyDao.fetchJobFromQueue(anyString(),anyString(),(Node)anyObject())).
			thenReturn(j);
		
		Node node = new Node("da-nrw-vm3.hki.uni-koeln.de","01-vm3"); node.setId(42);
		node.setName("da-nrw-vm3.hki.uni-koeln.de");
		node.setWorkAreaRootPath("fakePath");
		node.setDipAreaRootPath("fakePath");
		
		factory.setDao(dummyDao);	
		factory.setLocalNode(node);
		
		AbstractAction a = factory.buildNextAction();
		assertNotNull(a);
		assertEquals("450", a.getStartStatus());
		assertEquals("460", a.getEndStatus());
		assertEquals("da-nrw-vm3.hki.uni-koeln.de", a.getLocalNode().getName());
		assertNotNull(a.getDao());
//		assertEquals("csn", a.getJob().getObject().getContractor().getShort_name()); XXX used?
		assertNotNull(a.getActionMap());
		
	}
	
	/**
	 * Test no job found.
	 */
	@Test
	public void testNoJobFound(){
		
		CentralDatabaseDAO dummyDao = mock(CentralDatabaseDAO.class);

		when(dummyDao.fetchJobFromQueue(anyString(),anyString(),(Node)anyObject())).
			thenReturn(null);
		
		Node node = new Node("da-nrw-vm3.hki.uni-koeln.de","01-vm3"); node.setId(42); node.setName("testnode");
		
		factory.setDao(dummyDao);
		factory.setLocalNode(node);
		
		AbstractAction a = factory.buildNextAction();
		assertNull(a);
	}
	
	
	
	
}