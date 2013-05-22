package org.mitre.opensextant.desktop.executor.opensextant.ext;

import java.io.File;

import gate.creole.ConditionalSerialAnalyserController;

import org.mitre.opensextant.apps.Config;
import org.mitre.opensextant.apps.OpenSextantRunner;
import org.mitre.opensextant.desktop.Main;
import org.mitre.opensextant.desktop.ui.helpers.ConfigHelper;
import org.mitre.opensextant.desktop.ui.table.OSRow;
import org.mitre.opensextant.processing.ProcessingException;
import org.mitre.xtext.ConvertedDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OSDOpenSextantRunner extends OpenSextantRunner {

	public static final String ORIGINAL_FILE="originalFile";
	private static Logger log = LoggerFactory.getLogger(OSDOpenSextantRunner.class);

	private OSRow row;

	public OSDOpenSextantRunner(OSRow row) throws Exception {
		super();
		this.row = row;
		log.info("Created ODS Desktop runner");
	}

	@Override
	public void initialize() throws ProcessingException {
		log.info("initializing OSDOpenSextantRunner");
		super.initialize();
		controller = new OSDCorpusControllerWrapper((ConditionalSerialAnalyserController)controller, row);
	}

	@Override
	public void handleConversion(ConvertedDocument txtdoc) {
		super.handleConversion(txtdoc);
		corpus.get(corpus.size()-1).getFeatures().put(ORIGINAL_FILE, txtdoc.filepath);
	}
	

}
