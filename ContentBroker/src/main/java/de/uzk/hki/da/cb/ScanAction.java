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

package de.uzk.hki.da.cb;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uzk.hki.da.convert.FormatScanService;
import de.uzk.hki.da.core.ConfigurationException;
import de.uzk.hki.da.core.UserException;
import de.uzk.hki.da.core.UserException.UserExceptionId;
import de.uzk.hki.da.grid.DistributedConversionAdapter;
import de.uzk.hki.da.metadata.PremisXmlReader;
import de.uzk.hki.da.model.ConversionInstruction;
import de.uzk.hki.da.model.ConversionInstructionBuilder;
import de.uzk.hki.da.model.ConversionPolicy;
import de.uzk.hki.da.model.DAFile;
import de.uzk.hki.da.model.Object;
import de.uzk.hki.da.model.PreservationSystem;
import de.uzk.hki.da.service.PackageTypeDetectionService;


/**
 * Scans the files and builds ConversionInstructions for them if MIGRATION right is granted.
 * Populates the files collection of the jobs package with entries for each file of rep+a.
 * Scans the files of rep+a and attaches format info to them.
 * 
 * As a side effect this action not only counts up its own Jobs state up so that
 * the following ConvertAction is triggered but also creates Jobs for collections
 * of ConversionInstructions which can't be done on the local node due to the lack
 * of the necessary ConversionRoutines.
 * 
 * @author Daniel M. de Oliveira
 */
public class ScanAction extends AbstractAction{
	
	static final Logger logger = LoggerFactory.getLogger(ScanAction.class);
	private FormatScanService formatScanService;
	private PreservationSystem preservationSystem;
	private final ConversionInstructionBuilder ciB = new ConversionInstructionBuilder();
	private String sidecarExtensions;
	private DistributedConversionAdapter distributedConversionAdapter;
	
	
	public ScanAction(){}

	
	public FormatScanService getFormatScanService() {
		return formatScanService;
	}


	public void setFormatScanService(FormatScanService formatScanService) {
		this.formatScanService = formatScanService;
	}


	public void setSidecarExtensions(String sidecarExtensions) {
		this.sidecarExtensions = sidecarExtensions;
	}


	public String getSidecarExtensions() {
		return sidecarExtensions;
	}


	@Override
	boolean implementation() throws IOException {
		if (distributedConversionAdapter==null) throw new ConfigurationException("distributedConversionAdapter not set");
		if (formatScanService==null) throw new ConfigurationException("formatScanService not set");
		if (preservationSystem==null) // So we can prevent the preservationSystem to be instantiated in unit tests.
			preservationSystem = new PreservationSystem(dao);
		
		List<DAFile> filesArchival = formatScanService.identify(object.getLatestPackage().getFiles());
		
		String repPath = object.getDataPath() + job.getRep_name();
		Object premisObject = parsePremisToMetadata(repPath+"a");
		
		if (premisObject.grantsRight("MIGRATION"))
		{
			List<ConversionInstruction> cisArch = generateConversionInstructions(filesArchival);
			job.getConversion_instructions().addAll(cisArch);
		}
		else
			logger.info("No migration rights granted. No files will be converted for archival storage.");
		
		detectPackageTypeAndStoreInCommunicator();
		return true;
	}


	
	
	
	
	/**
	 * @author Sebastian Cuy
	 */
	private void detectPackageTypeAndStoreInCommunicator(){
		PackageTypeDetectionService ptds = new PackageTypeDetectionService(getObject().getLatestPackage());
		String packageType = ptds.getPackageType();
		String metadataFile = ptds.getMetadataFile();
		if (packageType == null || metadataFile == null) {
			logger.warn("Could not determine package type. ");
		} else {
			actionCommunicatorService.addDataObject(job.getId(), "package_type", packageType);
			actionCommunicatorService.addDataObject(job.getId(), "metadata_file", metadataFile);
		}
	}
	
	
	
	
	

	/**
	 * @author Daniel M. de Oliveira
	 * @param filesArchival
	 */
	private List<ConversionInstruction> generateConversionInstructions(List<DAFile> filesArchival) {
		
		List<ConversionInstruction> cis = new ArrayList<ConversionInstruction>();
		
		for (DAFile file : filesArchival){
			
			for	(ConversionPolicy p:
				preservationSystem.getApplicablePolicies(file, "DEFAULT"))
			{
				logger.info("Found applicable Policy for FileFormat "+
						p.getSource_format()+" -> "+p.getConversion_routine().getName() + "("+ file.getRelative_path()+ ")");
				
				ConversionInstruction ci = ciB.assembleConversionInstruction(file, p);
				ci.setSource_file(file);
				cis.add(ci);
				
				logger.info("Built conversionInstructionForArchival: "+ci.toString());
			}
		}
		
		return cis;
	}
	
	

	
	
	
	
	
	/**
	 * this is just for testing purposes
	 * @param sys
	 */
	void setPreservationSystem(PreservationSystem sys){
		preservationSystem = sys;
	}

	
	
	
	private Object parsePremisToMetadata(String pathToRepresentation) throws IOException {
		logger.debug("reading rights from " + pathToRepresentation + "/premis.xml");
		Object o = null;
				
		try {
			o = new PremisXmlReader()
			.deserialize(new File(pathToRepresentation + "/premis.xml"));
		} catch (ParseException e1) {
			throw new UserException(UserExceptionId.READ_SIP_PREMIS_ERROR, "Error while parsing premis file", e1);
		} catch (NullPointerException e2) {
			throw new UserException(UserExceptionId.READ_SIP_PREMIS_ERROR, "Error while parsing premis file", e2);
		}
		
		return o;
	}
	
	@Override
	void rollback() {

		job.getConversion_instructions().clear();
		for (ConversionInstruction ci: job.getConversion_instructions()){
			logger.warn("still exists: "+ci);
		}
		
	}


	public DistributedConversionAdapter getDistributedConversionAdapter() {
		return distributedConversionAdapter;
	}


	public void setDistributedConversionAdapter(
			DistributedConversionAdapter distributedConversionAdapter) {
		this.distributedConversionAdapter = distributedConversionAdapter;
	}

	
}