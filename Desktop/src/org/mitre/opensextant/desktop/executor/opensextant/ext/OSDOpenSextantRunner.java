package org.mitre.opensextant.desktop.executor.opensextant.ext;

import java.io.File;
import java.io.IOException;

import gate.Document;
import gate.Factory;
import gate.creole.ConditionalSerialAnalyserController;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.mitre.opensextant.apps.OpenSextantRunner;
import org.mitre.opensextant.desktop.ui.table.OSRow;
import org.mitre.opensextant.processing.OpenSextantSchema;
import org.mitre.opensextant.processing.ProcessingException;
import org.mitre.xtext.ConvertedDocument;
import org.mitre.xtext.XText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OSDOpenSextantRunner extends OpenSextantRunner {

	private static Logger log = LoggerFactory.getLogger(OSDOpenSextantRunner.class);

	private OSRow row;
	private String tmpRoot = (new File("." + File.separator + "tmp")).getAbsolutePath();
	private String archiveTmpRoot;
	private String tmpTmpRoot;

	public OSDOpenSextantRunner(OSRow row) throws Exception {
		super(); 
		this.row = row;
		log.info("Created ODS Desktop runner");

                String tmpLoc = row.getTmpLocation();
                if(tmpLoc != null && !"".equals(tmpLoc)) this.tmpRoot = row.getTmpLocation();
		tmpRoot += File.separator + FilenameUtils.getBaseName(row.getOutputLocation());
		archiveTmpRoot = tmpRoot + File.separator + "archives";
		tmpTmpRoot = tmpRoot + File.separator + "tmp";
	}
	
	@Override
	public void initialize() throws ProcessingException {
		log.info("initializing OSDOpenSextantRunner");
		super.initialize();
		controller = new OSDCorpusControllerWrapper((ConditionalSerialAnalyserController)controller, row);
		
		converter.archiveRoot= archiveTmpRoot;
		converter.tempRoot=tmpTmpRoot;
		params.tempDir = tmpTmpRoot;
	}

	
	
	@Override
	public void runOpenSextant(String inFile, String outFormat, String outFile, String tempDir) throws Exception {
		try {
			super.runOpenSextant(inFile, outFormat, outFile, this.tmpRoot);
		} finally {
			// remove the tmp directory once done
			File tmp = new File(this.tmpRoot);
			try {
				FileUtils.deleteDirectory(tmp);
			} catch (IOException e) {
				log.warn("File could not be deleted, attempt will be made on JVM shutdown: " + tmp.getAbsolutePath());
				tmp.deleteOnExit();
			}
		}
	}

	@Override
	public void handleConversion(ConvertedDocument txtdoc) {

        try {
            Document doc;
            // Get File path to text version of document.
            if (txtdoc.textpath != null) {
                doc = Factory.newDocument(new File(txtdoc.textpath).toURI().toURL());
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Found binary file, but textpath is null FILE=" + txtdoc.filepath);
                }
                if (txtdoc.payload == null) {
                    log.error("Both payload and textpath URL are null. FILE=" + txtdoc.filepath);
                    return;
                }
                doc = Factory.newDocument(txtdoc.payload);
            }

            doc.getFeatures().put(OpenSextantSchema.FILEPATH_FLD, txtdoc.filepath);
            
            this.corpus.add(doc);   // _docs.add(doc);

        } catch (Exception rie) {
            //throw new ProcessingException("Unable to load file", rie)
            log.error("Unable to ingest file FILE=" + txtdoc.textpath, rie);
        }

	}

	public void cancelExecution() {
		((OSDCorpusControllerWrapper)controller).cancelExecution();
	}
	
	public XText getConverter() {
		return this.converter;
	}
}
