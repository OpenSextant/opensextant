/** 
 Copyright 2009-2013 The MITRE Corporation.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.


 * **************************************************************************
 *                          NOTICE
 * This software was produced for the U. S. Government under Contract No.
 * W15P7T-12-C-F600, and is subject to the Rights in Noncommercial Computer
 * Software and Noncommercial Computer Software Documentation Clause
 * 252.227-7014 (JUN 1995)
 *
 * (c) 2012 The MITRE Corporation. All Rights Reserved.
 * **************************************************************************
**/
package org.mitre.opensextant.apps;

import gate.CorpusController;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.util.persistence.PersistenceManager;
import java.io.File;
import java.io.IOException;
import org.mitre.opensextant.processing.*;
import org.mitre.opensextant.processing.output.AbstractFormatter;
//import org.mitre.opensextant.processing.output.FormatterFactory;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.FilenameUtils;

import org.mitre.xtext.XText;
import org.mitre.xtext.ConvertedDocument;
import org.mitre.xtext.ConversionListener;

import java.util.*;

/**
 * Runs OpenSextant as a standalone application. Requires
 *  the GATE home directory to be set. This can done from the command line with 
 *  <code>-Dgate.home=</code><i>path to GATE home directory</i>
 *  or the directory can be passed in as a constructor argument.
 *  </p>
 * @author Rich Markeloff, MITRE Corp. 
 * Initial version created on Apr 20, 2012
 */
public class OpenSextantRunner extends AppBase implements ConversionListener {

    /**
     * Currently supported formats include CSV, JSON, KML, WKT and Shapefile.
     */
    public final static String[] OUTPUT_FORMATS = {"CSV", "GDB", "JSON", "KML", "WKT", "Shapefile"};
    // path to the saved application file
    public String gappFile = null;
    // the input file or directory to be processed
    //private static String inputFile = null;
    // the output format
    //private static String outputFormat;
    // the output file
    //private static String outputFile;
    // the temporary storage directory
    private static String tempDir = null;
    private XText converter;
    /* # of batches */
    private int batch_count = 0;
    /* # of documents */
    private int total_docs = 0;
    private long batch_signal_size = 0;
    private long total_size = 0;
    private static long BATCH_THRESHOLD = 0x80000;
    /* Process 4 MB of text content  800 x 5KB average documents */
    protected CorpusProcessor processor = null;

    /** Demonstration main program -- demonstrates using OpenSextant from cmd line
     * 
     */
    public OpenSextantRunner() throws Exception {
        super();
        log = LoggerFactory.getLogger(OpenSextantRunner.class);
    }
    public OpenSextantRunner(String gapp) throws Exception {
        super(gapp);
        this.gappFile = gapp;
        log = LoggerFactory.getLogger(OpenSextantRunner.class);
    }

    /** The "Start Button".  Initialize GATE, Solr, etc. 
     */
    @Override
    public void initialize() throws ProcessingException {
        printConfig();

        // load the GATE application
        log.info("Loading GAPP");
        if (gappFile == null && Config.RUNTIME_GAPP_PATH == null) {
            throw new ProcessingException("AppBase default GAPP file is not in place");
        }

        converter = new XText();

        // TOOD: Rework application setup. 
        //     all inputs here should be user settable.
        converter.archiveRoot = "/tmp/opensextant";
        converter.tempRoot = "/tmp/opensextant.tmp";
        converter.zone_web_content = false;
        converter.save = true;
        ConvertedDocument.overwrite = false;
        converter.setConversionListener(this);

        // Complications:  Where do we save converted items?
        //
        try {
            converter.setup();
        } catch (IOException ioerr) {
            throw new ProcessingException("Document converter could not start", ioerr);
        }

        if (gappFile == null) {
            gappFile = Config.RUNTIME_GAPP_PATH;
        }

        try {
            controller = (CorpusController) PersistenceManager.loadObjectFromFile(new File(gappFile));

        } catch (Exception rie) {
            throw new ProcessingException("Unable to setup corpus.", rie);
        }



    }

    /**
     * Runs OpenSextant. See the <code>main</code> method for a description of the input parameters.
     */
    public void runOpenSextant(String inFile, String outFormat, String outFile) throws Exception {
        this.runOpenSextant(inFile, outFormat, outFile, Config.DEFAULT_TEMP);
    }
    private static String CORPUS_NAME = "default-geocoding-hopper";

    /**
     * Runs OpenSextant. See the <code>main</code> method for a description of the input parameters.
     * TODO:  outFile is not used.  It is only used as a part of global settings somewhere....
     * 
     */
    public void runOpenSextant(String inFile, String outFormat,
            String outFile, String tempDir) throws Exception {

        if (!validateParameters(this.gappFile, inFile, outFormat, outFile, tempDir)) {
            log.error("VALIDATION ERRORS: " + runnerMessage.toString());
        }
        corpus = Factory.newCorpus(CORPUS_NAME);

        this.params.isdefault = false;
        this.params.inputFile = inFile;
        this.params.addOutputFormat(outFormat);
        // this.params.jobName = FilenameUtils.getBaseName(inFile);

        printRequest();

        // instantiate the corpus processor
        log.info("Setting up Corpus Processor");
        processor = new OpenSextantCorpusProcessor();

        processor.setApplicationName(gappFile);
        processor.setApplication(controller);

        // create and configure a results formatter
        // TODO: We can support mulitiple output formats
        // 
        if (outFormat != null) {
            AbstractFormatter formatter = AppBase.createFormatter(outFormat, params);
            processor.setFormatter(formatter);
            formatter.start(params.getJobName());
        }

        log.info("Starting document ingest");

        // This kicks off content conversion resulting in 
        // a stream of "converted documents" coming back to us 
        // through a call back.
        //    this.handleConversion(doc)  adds doc to corpus
        // 
        //    when corpus hits a high water mark, it processes the batch
        //    resets, and then we can return to conversion to fill up 
        //    the next pile of docs in the queue.
        // 
        //
        converter.extract_text(inFile);

        if (corpus.size() > 0) {
            // Due to batching logic that is handled in stream
            // make sure any left overs are processed before exiting.
            // E.g. a short batch would may not hit thresholding for batch
            // and would necessarily end up here.
            // 
            this.batchProcessCorpus();
        }

        log.info("Finished all processing");
        processor.shutdown();
        processor = null;
    }

    /** Shutdown all IO for the GATE app and release Corpus level resources
     */
    public void shutdown() {

        /** */
        if (corpus != null) {
            Factory.deleteResource(corpus);
            corpus = null;
        }

        if (processor != null) {
            log.info("Finished all processing");
            processor.shutdown();
        }
        log.info("All done");
        reportMemory();

    }


    List<Document> _docs = new ArrayList<>();

    /** Note -- a corpus will explode in memory if the job is too large.
     * Processor design should account for how to partition the problem
     * - ingest, conversion, geocoding, persistence, output format generation.
     * 
     * 
     * This implements the XText conversion listener -- when a document is found
     * it is reported here.  We add it to the corpus prior to executing the 
     * application on the corpus.
     * 
     * The preferred mode is to take the list of document URLs and process them
     * as a batch.
     * 
     * Alternatively if XText was set in "do not save" mode, the Converted Documents
     * could be processed by adding their payload to as Documents to the corpus
     * as buffers (not URLs).   This will certainly result in memory bloat, although
     * it might be faster.
     * 
     */
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

            this.corpus.add(doc);   // _docs.add(doc);
            batch_signal_size += txtdoc.payload.length();
            ++total_docs;

            if (batch_signal_size > BATCH_THRESHOLD) {
                batchProcessCorpus();
            }
        } catch (Exception rie) {
            //throw new ProcessingException("Unable to load file", rie)
            log.error("Unable to ingest file FILE=" + txtdoc.textpath);
        }
    }

    private void batchProcessCorpus() throws Exception {

        ++batch_count;
        // Track numbers and move on.
        total_size += batch_signal_size;
        // Reset batcher size to 0.
        batch_signal_size = 0;
        int _doccount = corpus.size();

        log.info("Starting document geocoding BATCH=" + this.batch_count + " BATCH SIZE="+ _doccount +" TOTAL_SIZE=" + this.total_size);
        reportMemory();

        // GATE:  each time corpus is processed, documents are release from within the processor.
        //
        processor.processCorpus(corpus);

        for (Iterator<Document> iter = corpus.iterator(); iter.hasNext(); ) {
            Document doc = iter.next();
            iter.remove();
            Factory.deleteResource(doc);
        }

        // Any difference with memory management if I just create a new corpus each time?
        boolean use_new = true;
        if (use_new) {
            Factory.deleteResource(corpus);
            // Create anew.
            corpus = Factory.newCorpus(CORPUS_NAME);
        }

        reportMemory();
    }

    public String getMessages() {
        return runnerMessage.toString();
    }


    private static String _gappFile = null;
    private static String _inFile = null;
    private static String _outFile = null;
    private static String _outFormat = null;
    private static String _tempDir = null;
    
    /**
     * Parse command line options.
     */
    private static void parseCommandLine(String[] args) {
        gnu.getopt.Getopt opts = new gnu.getopt.Getopt("OpenSextant", args, "hg:i:f:o:t:");

        int c;
        while ((c = opts.getopt()) != -1) {
            switch (c) {

                // -g gappFile = path to the saved GATE application
                case 'g':
                    _gappFile = opts.getOptarg();
                    break;

                // -i inputFile = path to file or directory of files to be processed
                case 'i':
                    _inFile = opts.getOptarg();
                    break;

                // -f outputFormat = the desired output format
                case 'f':
                    _outFormat = opts.getOptarg();
                    break;

                // -o outputDir = the path to output file
                case 'o':
                    _outFile = opts.getOptarg();
                    break;

                // -t tempDir = the path to temp directory
                case 't':
                    _tempDir = opts.getOptarg();
                    break;
                case 'h':
                default:
                    printHelp();
                    System.exit(-1);
            }
        }
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

    private void printRequest() {
        log.info("----------------- REQUEST -----------------");
        log.info("Input file: " + params.inputFile);
        log.info("Output format: " + params.getOutputFormats());
        log.info("Output location: " + params.outputDir);
    }

    /**
     * Print a usage message
     */
    private static final void printHelp() {

        System.out.println("Options:");
        System.out.println("\t-g gappFile = path to the saved GATE application");
        System.out.println("\t-i inputFile = path to file or directory of files to be processed");
        System.out.println("\t-f outputFormat = the desired output format");
        System.out.println("\t-o outputFile = the path to output file");
        System.out.println("\t-t tempDir = the path to the temporary storage directory");
    }
    private StringBuilder runnerMessage = new StringBuilder();

    /**
     * Check that the input parameters are valid and complete.
     * @return either <q>OK</q> or an error message
     */
    public boolean validateParameters(String gateApp, String inPath,
            String outFormat, String outPath, String tempDir) {

        runnerMessage = new StringBuilder();

        if (outPath == null) {
            runnerMessage.append("Please specify an Output file or folder");
            return false;
        }

        if (gateApp == null) {
            log.info("Will use default GAPP");
        } // Check GAPP file
        else if (!new File(gateApp).exists()) {
            runnerMessage.append("GAPP file " + gateApp + " does not exist");
            return false;
        }

        // Make sure input file exists
        File inFile = new File(inPath);
        if (!inFile.exists()) {
            runnerMessage.append("Input file " + inPath + " does not exist");
            return false;
        }

        // Check output format
        if (outFormat != null) {
            boolean validOutputFormat = false;
            String fmt = outFormat.toLowerCase();
            for (String f : OUTPUT_FORMATS) {
                if (f.toLowerCase().equalsIgnoreCase(fmt)) {
                    validOutputFormat = true;
                }
            }

            if (!validOutputFormat) {
                runnerMessage.append("Unrecognized output format: " + outFormat);
                return false;
            }
        }

        if (inPath.startsWith("$") || outPath.startsWith("$")) {
            runnerMessage.append("Invalid input/output -- Ant style arguments are null");
            return false;
        }
        // Verify user has specified a directory for unpacking an archive

        // Get file extension
        String ext = FilenameUtils.getExtension(inPath);

        if (isArchiveExtension(ext) && tempDir == null) {
            runnerMessage.append("A directory for temporary storage must be provided for unpacking " + ext + " files");
            return false;
        }

        // Split the path name into directory and file names
        File container = new File(outPath);
        File destDir = null;
        String destFile = null;
        log.info("Working off INPUT=" + container.getAbsolutePath());

        if (container.isDirectory()) {
            destDir = container;
            try {
                // DEFAULT file name.
                params.setJobName("OpenSextant_Output_" + OpenSextantCorpusProcessor.getProcessingKey());
                //destFile = params.jobName + formatter.outputExtension;
            } catch (Exception fmterr) {
                runnerMessage.append("Failed to invoke the requested format to create a default output file");
                return false;
            }
        } else {
            destDir = container.getParentFile();
            destFile = container.getName();
            params.setJobName(FilenameUtils.getBaseName(destFile));
        }

        if (!destDir.exists()) {
            // throw new IOException("Sorry - your destination folder " + destDir + " must exist");
            runnerMessage.append("Destination folder must exist, DIR=" + destDir.getAbsolutePath());
            return false;
        }

        params.outputDir = destDir.getAbsolutePath();

        return true;
    }

    private static boolean isArchiveExtension(String ext) {
        return ext.equalsIgnoreCase("zip") || ext.equalsIgnoreCase("tar")
                || ext.equalsIgnoreCase("gz") || ext.equalsIgnoreCase("tgz") || ext.equalsIgnoreCase("tar.gz");
    }

    /**
     * Runs OpenSextant from the command line. Command line options are:
     * <ul>
     * <li>
     * <code>-g </code><i>gappFile</i> Path to the saved GATE application
     * </li><li>
     * <code>-i </code><i>inputFile</i> Path to file or directory of files to be processed
     * </li><li>
     * <code>-f </code><i>outputFormat</i> The desired output format
     * </li><li>
     * <code>-o </code><i>outputDir</i> Path to output file
     * </li><li>
     * <code>-t </code><i>tempDir</i> Path to the temporary storage directory, if one is required
     * </li><li>
     * <code>-d </code><i>descriptionType</i> Choice of text string used to fill description fields, if the output
     * format has a description field. 
     * </li>
     * </ul><p> 
     */
    public static void main(String[] args) {

        System.out.println("Parsing Commandline");
        parseCommandLine(args);
        try {
            OpenSextantRunner runner = null;
            if (_gappFile != null) { 
                runner = new OpenSextantRunner(_gappFile);
            } else {
                runner = new OpenSextantRunner();
            }
            // TESTING:
            //OpenSextantRunner.BATCH_THRESHOLD = 0x10000;
            runner.initialize();
            runner.runOpenSextant(_inFile, _outFormat, _outFile, _tempDir);
            runner.shutdown();

            // Release all resources
            AppBase.globalShutdown();
            // Success.
            // System.exit(0);
        } catch (Exception err) {
            err.printStackTrace();
        }
        // Failed 
        System.exit(-1);
    }
}
