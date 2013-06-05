/**
 * Copyright 2009-2013 The MITRE Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 *
 * **************************************************************************
 * NOTICE This software was produced for the U. S. Government under Contract No.
 * W15P7T-12-C-F600, and is subject to the Rights in Noncommercial Computer
 * Software and Noncommercial Computer Software Documentation Clause
 * 252.227-7014 (JUN 1995)
 *
 * (c) 2012 The MITRE Corporation. All Rights Reserved.
 * **************************************************************************
*
 */
package org.mitre.opensextant.processing;

import gate.Corpus;
import gate.CorpusController;
import gate.Factory;

import java.util.Date;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormat;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.mitre.opensextant.processing.output.*;
import org.mitre.opensextant.extraction.ExtractionMetrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs an OpenSextant GATE application on a corpus or set of corpora.
 *
 * @author Rich Markeloff, MITRE Corp. Initial version created on Jun 13, 2011
 *
 * @author Marc Ubaldino, MITRE Corp. Revised, APR 2013
 */
public class OpenSextantCorpusProcessor implements CorpusProcessor {

    private Logger log = LoggerFactory.getLogger(OpenSextantCorpusProcessor.class);
    private CorpusController application;
    private ResultsFormatter formatter = null;
    private String applicationName = null;
    private String filename = null;
    private final static DateTimeFormatter procdate_fmt = DateTimeFormat.forPattern("yyyyMMMdd_HHmm");
    private ExtractionMetrics processingTime = new ExtractionMetrics("Corpus Processing");

    /**
     * Generates a simple date key for the job
     */
    public static String getProcessingKey() {
        return procdate_fmt.print(new Date().getTime());
    }

    /**
     * Set the formatter to generate the output.
     *
     * @param formatter the formatter to set
     */
    @Override
    public void setFormatter(ResultsFormatter formatter) {
        this.formatter = formatter;
    }

    /**
     * Set the name of the GATE application.
     *
     * @param applicationName the applicationName to set
     */
    @Override
    public void setApplicationName(String app) {
        this.applicationName = app;
    }

    /**
     * The output file name. This is not the full path name; the output
     * directory is provided separately to the results formatter.
     *
     * @param filename the file name to set
     */
    @Override
    public void setOutputFilename(String fname) {
        this.filename = fname;
    }

    /**
     * Set the GATE application that will be run over the documents.
     */
    @Override
    public void setApplication(CorpusController application) {
        this.application = application;
    }

    /**
     * The PostConstruct annotation means that this method will be called by
     * Spring once the object has been constructed and its properties (i.e. the
     * application) have been set.
     */
    @PostConstruct
    public void init() throws Exception {
    }

    /**
     * Clean-up method. The PreDestroy annotation means that Spring will call
     * the method when the object is no longer required.
     */
    @PreDestroy
    public void cleanup() throws Exception {
        Factory.deleteResource(application);
    }

    @Override
    public void shutdown() {
        application.interrupt();
        application.cleanup();
        if (formatter != null) {
            formatter.finish();
        }
    }

    /**
     */
    @Override
    public void processCorpus(Corpus corpus) throws Exception {

        long t0 = System.currentTimeMillis();

        // Give the corpus to the controller
        this.application.setCorpus(corpus);

        String msg = "Execute Corpus@" + corpus.getName() + " size: " + corpus.size();
        log.info(msg);

        // Run GATE
        //
        this.application.execute();
        long t1 = System.currentTimeMillis();
        // msg += " duration(ms)=" + (t1 - t0);
        processingTime.addTime(t1-t0, corpus.size());

        // If you have no desire to format results 
        // you will have to iterate over the results some other way.
        if (this.formatter != null) {
            this.formatter.formatResults(corpus);
        }

        /**
         * Anything else happeningn to the Document, e.g. getting more
         * annotations other than gecoding results must happen before we release
         * the corpus.
         */
        //return ;
    }

    @Override
    public ExtractionMetrics getProcessingMetric() {
        return processingTime;
    }
}
