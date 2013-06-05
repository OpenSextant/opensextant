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
package org.mitre.opensextant.processing;

import gate.Corpus;
import gate.CorpusController;
import org.mitre.opensextant.processing.output.ResultsFormatter;
import org.mitre.opensextant.extraction.ExtractionMetrics;



/**
 * Interface for classes that run an OpenSextant GATE application on a corpus or set of corpora.
 * 
 * @author Rich Markeloff, MITRE Corp. 
 * Initial version created on Jun 13, 2011
 */

public interface CorpusProcessor {
	
	/**
	 * Set the name of the GATE application.
	 */
	public void setApplicationName(String fileName);
	
	/**
	 * Set the GATE application that will process the documents.
	 */
	public void setApplication(CorpusController application);
	
	/**
	 * Set the name of the output file.
	 */
	public void  setOutputFilename(String filename);
	
	/**
	 * Set the formatter to format the output. 
	 */
	public void setFormatter(ResultsFormatter formatter);
	
	/**
	 * Process all the corpora in a directory.
	 */
	//public String processCorpora(File corporaDir) throws Exception;
	
	/**
	 * Process a single GATE corpus.
	 */
	public void processCorpus(Corpus corpus) throws Exception;

        /** TODO: method for tracking processing metrics. "Metrics" is plural
         *  as this may expand in scope.
         */
        public ExtractionMetrics getProcessingMetric();
        
    /** What you do when you want it all to end. */
    public void shutdown();
}
