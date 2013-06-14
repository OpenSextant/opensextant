package org.mitre.opensextant.desktop.executor.opensextant.ext.extract;

import gate.Corpus;
import gate.CorpusController;
import gate.Document;
import gate.Factory;
import gate.creole.ConditionalSerialAnalyserController;
import gate.event.ProgressListener;
import gate.util.GateException;
import gate.util.persistence.PersistenceManager;

import java.io.File;
import java.io.IOException;

import org.mitre.opensextant.apps.AppBase;
import org.mitre.opensextant.apps.Config;
import org.mitre.opensextant.desktop.ui.helpers.ConfigHelper;
import org.mitre.opensextant.desktop.ui.table.OSRow;
import org.mitre.opensextant.processing.ProcessingException;
import org.slf4j.LoggerFactory;

public class OSExtractor extends AppBase{
    private static String CORPUS_NAME = "default-geocoding-hopper";

    public OSExtractor() throws ProcessingException {
		super();
	}

	// path to the saved application file
    private String gappFile = null;
    // the temporary storage directory
    private static String tempDir = null;

	/** The "Start Button".  Initialize GATE, Solr, etc. 
     */
    @Override
    public void initialize() throws ProcessingException {
    	log = LoggerFactory.getLogger(OSExtractor.class);
        printConfig();

        if (gappFile == null) gappFile = Config.RUNTIME_GAPP_PATH;

        // load the GATE application
        log.info("Loading GAPP: " + gappFile);
        if (gappFile == null) {
            throw new ProcessingException("AppBase default GAPP file is not in place");
        }


        try {
            controller = (CorpusController) PersistenceManager.loadObjectFromFile(new File(gappFile));

        } catch (Exception rie) {
            throw new ProcessingException("Unable to setup corpus.", rie);
        }
        
        params.isdefault = false;
        params.tempDir = ConfigHelper.getInstance().getOSTmpRoot();

    }
    
    /**
     * Please shutdown the application cleanly when done.
     */
    public void shutdown() {

        if (controller != null) {
            controller.interrupt();
            controller.cleanup();
            Factory.deleteResource(controller);
        }
        if (corpus != null) {
        	corpus.cleanup();
            Factory.deleteResource(corpus);
        }
    }

    
    
    public void addProgressListener(ProgressListener listener) {
    	ConditionalSerialAnalyserController serialController = (ConditionalSerialAnalyserController) controller;
    	serialController.addProgressListener(listener);
    }

    public void removeProgressListener(ProgressListener listener) {
    	ConditionalSerialAnalyserController serialController = (ConditionalSerialAnalyserController) controller;
    	serialController.removeProgressListener(listener);
    }

    public Corpus extractEntities(Document document) throws ProcessingException {
    	try {
        	ConditionalSerialAnalyserController serialController = (ConditionalSerialAnalyserController) controller;
            corpus = Factory.newCorpus(CORPUS_NAME);
            corpus.add(document);
        	serialController.setDocument(document);
        	serialController.setCorpus(corpus);
        	serialController.execute();
            Factory.deleteResource(corpus);
            // Create anew.
        	return corpus;
    	} catch (GateException e) {
    		throw new ProcessingException(e);
    	} 
    }
    
    public void formatResults(OSRow row, Document document) throws IOException, ProcessingException {

    }

    /**
     * output the run configuration.
     */
    private void printConfig() {
        log.info("----------------- CONFIGURATION -----------------");
        log.info("GAPP file: " + gappFile);
        if (tempDir != null) {
            log.info("Temporary storage location: " + tempDir);
        }
    }

}
