package de.uzk.hki.da.at;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import de.uzk.hki.da.core.C;
import de.uzk.hki.da.model.Object;
import de.uzk.hki.da.util.Path;

/**
 * 
 * @author Polina Gubaidullina
 *
 */

public class ATUseCaseIngestEadMetsVariousRefs extends AcceptanceTest{
	private static final String origName = "ATUseCaseIngestEadMetsVariousRefs";
	
	@Before
	public void setUp() throws IOException{
		ath.putSIPtoIngestArea(origName, "tgz", origName);
		ath.awaitObjectState(origName,Object.ObjectStatus.ArchivedAndValid);
	}
	
	@Test
	public void test() {
		Path.make(localNode.getWorkAreaRootPath(),C.WA_PIPS, C.WA_PUBLIC, C.TEST_USER_SHORT_NAME);
	}
}
