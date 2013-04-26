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
package org.mitre.opensextant.processing.output;

import gate.Corpus;
import gate.Document;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.mitre.opensextant.processing.GeocodingResult;
import org.mitre.opensextant.processing.Parameters;
import org.mitre.opensextant.processing.ProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.text.DecimalFormat;


/**
 * Abstract class encapsulating basic results formatter functionality.
 *
 * @author Rich Markeloff, MITRE Corp. Initial version created on Aug 22, 2011
 * @author Marc Ubaldino, MITRE Corp. Refactored, 2013
 */
abstract public class AbstractFormatter implements ResultsFormatter {

    protected Parameters outputParams = null;
    public boolean overwrite = false;

    @Override
    public void setParameters(Parameters params) {
        outputParams = params;
    }
    /**
     *
     */
    protected static final Logger log = LoggerFactory.getLogger(AbstractFormatter.class);
    private String filename = "unset";
    /** File extension for callers to know. */
    public String outputExtension = null;
    /**
     *
     */
    protected String current_corpus = null;
    /**
     *
     */
    protected String outputType = null;
    /**
     *
     */
    protected static int TEXT_WIDTH = 150;
    /**
     *
     */
    protected float min_score = 0.000f;
    /**
     *
     */
    public boolean debug = false;
    /**
     * Schema-specific stuff.  GIS formats would not make use of offsets.
     * CSV format is only one where offsets make sense.
     */
    public boolean includeOffsets = false;
    /**
     * GIS formats may optionally include coordinates as fields.
     * GDB and SHP have a Point geometry which carries the lat/lon already.
     * 
     * KML, CSV, JSON, etc. it makes sense to include these explicitly.
     */
    public boolean includeCoordinate = false;

    /** A basic job name that reflects file name 
     * @return 
     */
    @Override
    public String getJobName() {
        return this.outputParams.getJobName();
    }

    /**
     * @param fname 
     * @see
     * org.mitre.opensextant.processing.output.ResultsFormatter#setJobName(java.lang.String)
     */
    @Override
    public void setOutputFilename(String fname) {
        this.filename = fname;
        //FilenameUtils.getBaseName(fname.replace(' ', '_'));
    }

    /**
     * @param path 
     * @see
     * org.mitre.opensextant.processing.output.ResultsFormatter#setOutputDir(java.lang.String)
     */
    @Override
    public void setOutputDir(String path) {

        // Create the directory if necessary
        File resultsDir = new File(path);
        if (!resultsDir.exists()) {
            resultsDir.mkdir();
        }

        this.outputParams.outputDir = resultsDir.getPath();
    }

    /**
     * Write to a file and return HTML containing a link to the file.
     * @param corpusList
     * @throws Exception  
     */
    @Override
    public String formatResults(Corpus corpus) throws Exception {

        //createOutputStreams();
        writeOutput(corpus);
        //closeOutputStreams();

        return "";
    }

    @Override
    public String getOutputFilepath() {
        return this.outputParams.outputDir + File.separator + this.filename;
    }

    /**
     *
     * @return
     */
    protected String createOutputFileName() {
        return this.filename + this.outputExtension;
    }

    /**
     * @return 
     * @see
     * org.mitre.opensextant.processing.output.ResultsFormatter#getOutputType()
     */
    @Override
    public String getOutputType() {
        return this.outputType;
    }

    /** This is checked only by internal classes as they create output streams.
     */
    protected void deleteOutput(File previous_run) {
        if (previous_run.exists()) {
            FileUtils.deleteQuietly(previous_run);
        }
    }

    /**  uniform helper for overwrite check.
     */
    protected void checkOverwrite(File item) throws ProcessingException {
        if (this.overwrite) {
            this.deleteOutput(item);
        } else if (item.exists()) {
            throw new ProcessingException("OpenSextant API cannot overwrite GIS output files -- caller must do that.  FILE=" + item.getPath() + " exists");
        }
    }
    
    
    private static final DecimalFormat confFmt = new DecimalFormat("0.000");    
    /** Convenience method for managing how confidence number is reported in output.
     */
    protected String formatConfidence(double conf){
        return confFmt.format(conf);
    }

    

    @Override
    abstract public void start(String nm) throws ProcessingException;

    @Override
    abstract public void finish();

    /**
     * Create the output stream appropriate for the output type.
     * IO is created using the filename represented by getOutputFilepath()
     * @throws Exception 
     */
    abstract protected void createOutputStreams() throws Exception;

    /**
     *
     * @throws Exception
     */
    abstract protected void closeOutputStreams() throws Exception;

    /**
     * Write out each place and geocoord to a file. The folder structure is
     * different for KML and shapefiles. For KML output, there is a folder for
     * each corpus and a subfolder for each document. For shapefile output,
     * there is only a top-level folder and a document's position in the
     * directory structure is encoded in a hyperlink.
     *
     * @param corpusList 
     * @throws Exception 
     */
    abstract public void writeOutput(Corpus corpusList) throws Exception;

    /**
     *
     * @param doc
     * @throws IOException
     */
    abstract public void writeRowsFor(Document doc) throws IOException;

    /** Write your geocoding result directly to output, instead of passing GATE objects.
     *  
     * @param link  a link or file name pointer to the original
     * @param rowdata the data to write out
     */
    abstract public void writeGeocodingResult(GeocodingResult rowdata, String link);
}
