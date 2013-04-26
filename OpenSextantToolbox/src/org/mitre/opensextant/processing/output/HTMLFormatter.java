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

import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.mitre.opensextant.processing.Parameters;
import org.mitre.opensextant.processing.ProcessingException;

/**
 * A wrapper around a ResultsFormatter that adds some HTML output for the user.
 * 
 * @author Rich Markeloff, MITRE Corp. 
 * Initial version created on May 1, 2012
 */
public class HTMLFormatter implements ResultsFormatter {

    private String serviceUrl;
    private ResultsFormatter resFormatter;

    /**
     * Specify the URL of the web service
     * @param url 
     */
    public void setServiceUrl(String url) {
        this.serviceUrl = url;
    }

    @Override
    public void start(String nm) throws ProcessingException {
        this.resFormatter.start(nm);
    }

    @Override
    public void finish() {
        this.resFormatter.finish();
    }

    @Override
    public void setParameters(Parameters params) {
        resFormatter.setParameters(params);
    }

    /**
     *
     * @param formatter
     */
    public void setFormatter(ResultsFormatter formatter) {
        this.resFormatter = formatter;
    }

    public String getJobName() {
        return this.resFormatter.getJobName();
    }

    /**
     * @param pathname 
     * @see org.mitre.opensextant.ResultsFormatter#setOutputDir(java.lang.String)
     */
    @Override
    public void setOutputDir(String pathname) {
        this.resFormatter.setOutputDir(pathname);
    }

    /**
     * @param filename 
     * @see org.mitre.opensextant.ResultsFormatter#setJobName(java.lang.String)
     */
    @Override
    public void setOutputFilename(String filename) {
        this.resFormatter.setOutputFilename(filename);
    }

    /**
     * @return 
     * @see org.mitre.opensextant.ResultsFormatter#getOutputType()
     */
    @Override
    public String getOutputType() {
        return this.resFormatter.getOutputType();
    }

    /**
     * @return 
     * @see org.mitre.opensextant.ResultsFormatter#getOutputFilepath()
     */
    @Override
    public String getOutputFilepath() {
        return this.resFormatter.getOutputFilepath();
    }

    /**
     * @param corpusList 
     * @throws Exception 
     * @see org.mitre.opensextant.ResultsFormatter#formatResults(java.util.List)
     */
    @Override
    public String formatResults(Corpus c) throws Exception {
        String results = this.resFormatter.formatResults(c);

        results = results + "<h1>" + getOutputType() + " Output</h1>" + "\n";
        results = results + "<p>Output file: <a href=" + this.serviceUrl + "?" + getOutputFilepath()
                + ">" + FilenameUtils.getName(getOutputFilepath()) + "</a></p>";
        return results;
    }
}
