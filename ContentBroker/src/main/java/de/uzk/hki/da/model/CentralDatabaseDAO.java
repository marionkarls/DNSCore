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

package de.uzk.hki.da.model;

import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uzk.hki.da.core.HibernateUtil;


/**
 * @author Daniel M. de Oliveira
 */
public class CentralDatabaseDAO {

	/** The logger. */
	private static Logger logger = LoggerFactory
			.getLogger(CentralDatabaseDAO.class);

	/**
	 */
	public CentralDatabaseDAO() {}
	
	
	
	
	
	
	
	
	

	
	
	
	
	
	
	/**
	 * Gets the second stage scan policies.
	 *
	 * @return the second stage scan policies
	 */
	public List<SecondStageScanPolicy> getSecondStageScanPolicies(Session session) {
		@SuppressWarnings("unchecked")
		List<SecondStageScanPolicy> l = session
				.createQuery("from SecondStageScanPolicy").list();

		return l;
	}
	
	
	
	
	/**
	 * 
	 * Only needed for proper tests of convert action.
	 * Updates the jobs status to the current state of the database.
	 *
	 * @param job the job
	 * @return Job
	 */
	@SuppressWarnings("unused")
	public Job refreshJob(Job job) {
		Session session = HibernateUtil.openSession();
		session.beginTransaction();
		session.refresh(job);
		for (ConversionInstruction ci:job.getConversion_instructions()){}
		for (Job j:job.getChildren()){}
		for (Package p:job.getObject().getPackages()){
			for (DAFile f:p.getFiles()){}
			for (Event e:p.getEvents()){}
		}
		session.close();
		return job;
	}

	
	
	
	/**
	 * Only needed for proper tests of convert action.
	 *
	 * @param id the id
	 * @return Job
	 */
	public Job getJob(Session session, int id) {
		Job job = (Job) session.get(Job.class, id);
		return job;
	}
}
