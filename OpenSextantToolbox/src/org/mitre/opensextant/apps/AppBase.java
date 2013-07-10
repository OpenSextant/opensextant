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
package org.mitre.opensextant.apps;

import gate.CorpusController;
import gate.Corpus;
import java.io.IOException;
import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

import org.mitre.opensextant.processing.ProcessingException;
import org.mitre.opensextant.processing.Parameters;
import org.mitre.opensextant.processing.output.AbstractFormatter;
import org.mitre.opensextant.processing.output.FormatterFactory;

/**
 *
 * @author Marc C. Ubaldino, MITRE <ubaldino at mitre dot org>
 */
public abstract class AppBase {

    protected Logger log = null;
    /**
     * To use any of this OpenSextant apps need some settings.
     */
    protected Config defaultSettings = null;
    /**
     * The controller manages insertion of data into the GATE pipeline execution
     * and retrieval of results from the pipeline.
     */
    protected CorpusController controller = null;
    protected Corpus corpus = null;
    public Parameters params = new Parameters();

    /**
     * A default OpenSextant GATE app -- the defaults are provided by Config()
     */
    public AppBase() throws ProcessingException {
        // If you don't need much this works
        defaultSettings = new Config();
    }

    /**
     * If you need a special GAPP -- that is not the standard production GAPP
     * use a single String arg of the name of the GAPP file, by default in your
     * OpenSextant ./gate GATE_HOME
     */
    public AppBase(String gapp) throws ProcessingException {
        defaultSettings = new Config(gapp);
    }

    /**
     * This implementation of an OpenSextant App should call initializePlatform
     * before anything else. And then most initialization follows the
     * SimpleGeocoder logic at least: setup your GAPP and a few other resources.
     * The OpenSextantRunner is a more complete document processing pipeline.
     *
     * Both are examples of complete geocoders -- as you implement your own you
     * may choose to extend either of these ore create your own from AppBase
     *
     *
     */
    public abstract void initialize() throws ProcessingException;

    public static AbstractFormatter createFormatter(String outputFormat, Parameters p)
            throws IOException, ProcessingException {

        if (p.isdefault) {
            throw new ProcessingException("Caller is required to use non-default Parameters; "
                    + "\nat least set the output options, folder, jobname, etc.");
        }
        AbstractFormatter formatter = (AbstractFormatter) FormatterFactory.getInstance(outputFormat);
        if (formatter == null) {
            throw new ProcessingException("Wrong formatter?");
        }

        formatter.setParameters(p);
        // formatter.setOutputDir(params.outputDir);
        formatter.setOutputFilename(p.getJobName() + formatter.outputExtension);

        return formatter;
    }

    public void reportMemory() {
        Runtime R = Runtime.getRuntime();
        long usedMemory = R.totalMemory() - R.freeMemory();
        log.info("CURRENT MEM USAGE(K)=" + (int) (usedMemory / 1024));
    }

    /** We have some emerging metrics to report out... As these metrics are volatile, I'm not changing imports.
     */
    public void reportMetrics() {
        log.info( "=======================\nTAGGING METRICS");
        log.info(org.mitre.opensextant.toolbox.NaiveTaggerSolrPR.getRetrievalMetric().toString());
        log.info(org.mitre.opensextant.toolbox.NaiveTaggerSolrPR.getTaggingMetric().toString());
        log.info(org.mitre.opensextant.toolbox.NaiveTaggerSolrPR.getTotalsMetric().toString());
    }

    /**
     * Call this from your instance of apps when you are really done. This is an
     * attempt to close global, static JVM-wide resources that we have optimized
     * for one reason or another. Call this from your Main program -- not from
     * your child threads -- when you are exiting the process.
     */
    public static void globalShutdown() {
        org.mitre.opensextant.extraction.PlacenameMatcher.shutdown();
    }
}
