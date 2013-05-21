package org.mitre.opensextant.desktop.executor.opensextant.ext;

import gate.creole.ConditionalSerialAnalyserController;

import org.mitre.opensextant.apps.OpenSextantRunner;
import org.mitre.opensextant.desktop.ui.table.OSRow;
import org.mitre.opensextant.processing.ProcessingException;
import org.mitre.xtext.ConvertedDocument;

public class OSDOpenSextantRunner extends OpenSextantRunner {

	public static final String ORIGINAL_FILE="originalFile";
	
	private OSRow row;

	public OSDOpenSextantRunner(OSRow row) throws Exception {
		super();
		this.row = row;
	}

	@Override
	public void initialize() throws ProcessingException {
		super.initialize();
		controller = new OSDCorpusControllerWrapper((ConditionalSerialAnalyserController)controller, row);
	}

	@Override
	public void handleConversion(ConvertedDocument txtdoc) {
		super.handleConversion(txtdoc);
		corpus.get(corpus.size()-1).getFeatures().put(ORIGINAL_FILE, txtdoc.filepath);
	}
	

}
