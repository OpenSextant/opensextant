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
import org.mitre.opensextant.processing.Parameters;
import org.mitre.opensextant.processing.ProcessingException;

/**
 * Interface for classes that generate output from corpora that have been processed by OpenSextant.
 * @author Rich Markeloff, MITRE Corp. 
 * Initial version created on Jul 13, 2011
 */
public interface ResultsFormatter {

    /** A more convenient way of passing in a list of parameters.
     */
    public void setParameters(Parameters params);

    /** 
     * @return 
     */
    public String getJobName();

    /**
     * Set the path to the output directory.
     * @param pathname 
     */
    public void setOutputDir(String pathname);

    /**
     * Set the name of the output file.
     * @param filename 
     */
    public void setOutputFilename(String filename);

    /**
     * Get the type of output produced by this formatter.
     * @return 
     */
    public String getOutputType();

    /**
     * Get the path to the output file.
     * @return 
     */
    public String getOutputFilepath();

    /**
     * Formats the results obtained from processing a corpus through OpenSextant.
     * Returns a string to display to the user. Typically this will be HTML to be
     * shown in a browser.
     * 
     * @param corpusList 
     * @return A message for the user
     * @throws Exception  
     */
    public String formatResults(Corpus corpus) throws Exception;

    public void start(String nm) throws ProcessingException;

    public void finish();
}
