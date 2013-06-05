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

import gate.Corpus;
import gate.CorpusController;
import gate.FeatureMap;
import gate.Document;
import gate.Factory;
import gate.util.persistence.PersistenceManager;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import org.mitre.opensextant.extraction.ExtractionMetrics;
import org.mitre.opensextant.processing.GeocodingResult;
import org.mitre.opensextant.processing.TextInput;
import org.mitre.opensextant.processing.ProcessingException;
import org.slf4j.LoggerFactory;
import org.mitre.opensextant.util.TextUtils;

/**
 * This is the simplest geocoding API we could devise to support embedding
 * OpenSextant into your Java applications directly. You instantiate it and can
 * geocode text rapidly and repeatedly. You, the caller is responsible for
 * managing storage of results and formatting of results.
 *
 * Our other API classes demonstrate how to format results, and eventually how
 * to store them.
 *
 * In no way does this SimpleGeocoder compromise on the quality or thoroughness
 * of the geocoding processing. It is the same processing, just a lighter weight
 * method than say using the WS or GATE Runner versions.
 *
 * @author Marc C. Ubaldino, MITRE <ubaldino at mitre dot org>
 */
public class SimpleGeocoder extends AppBase {

    /**
     *
     */
    public static String GATE_APP = "OpenSextant_Solr.gapp";
    private Corpus feeder = null;
    private final TextUtils utility = new TextUtils();
    private ExtractionMetrics processingMetric = new ExtractionMetrics("processing");

    /**
     * Demonstration main program -- demonstrates using OpenSextant from cmd
     * line
     *
     * @throws Exception
     */
    public SimpleGeocoder() throws ProcessingException {
        super();
        log = LoggerFactory.getLogger(SimpleGeocoder.class);
    }

    /**
     *
     * @throws ProcessingException
     */
    @Override
    /**
     * We do whatever is needed to init resources... that varies depending on
     * the use case.
     *
     * Guidelines: this class is custodian of the app controller, Corpus feeder,
     * and any Document instances passed into/out of the feeder.
     */
    public void initialize() throws ProcessingException {

        if (this.params.isdefault || this.params.getJobName() == null) {
            throw new ProcessingException("Please set AppBase.params with non-null values. "
                    + "Caller is responsible for reviewing Parameters and setting isdefault=false.");
        }

        try {
            // load the GATE application
            log.info("Loading GAPP");
            File gapp = new File(Config.GATE_HOME + File.separator + GATE_APP);
            controller = (CorpusController) PersistenceManager.loadObjectFromFile(gapp);
            feeder = Factory.newCorpus(this.params.getJobName());

            controller.setCorpus(feeder);

        } catch (Exception gerr) {
            throw new ProcessingException("GATE would not start", gerr);
        }
    }

    /**
     * Geocode an input buffer
     *
     * @return result GeocodingResult has an array of Geocoding annotations.
     * @throws Exception
     * @param text TextInput is a simple record that has an ID and a buffer.
     * @see geocode(List&lt;TextInput&gt; texts) - its not clear if there are
     * benefits in batching larger #'s of smaller records
     *
     */
    public GeocodingResult geocode(TextInput text) throws Exception {
        long t1 = System.currentTimeMillis();
        // create 
        Document doc = Factory.newDocument(text.buffer);
        feeder.add(doc);

        // Annotations are ID'd as <TEXT_ID> + "." ##
        // This is to ensure only uniqueness within the current session.
        // 
        // Caller must be aware of how uniqueness of results is managed.
        //   if not in control of it.  
        if (text.id == null) {
            text.id = utility.genTextID(text.buffer);
        }

        controller.execute();
        GeocodingResult annotations = new GeocodingResult(text.id);
        annotations.retrieveGeocodes(doc, params);
        feeder.remove(doc);
        Factory.deleteResource(doc);

        long t2 = System.currentTimeMillis();
        processingMetric.addTime(t2 - t1);

        return annotations;
    }

    /**
     * Process a list of TextInputs, generating a text-id if for each item, if
     * none exists. This is an optimization over the simpler geocode(one) for
     * geocoding one input buffer.
     *
     * Batching is not determined by this API. The caller should decide for the
     * amount and size of their own records how to organize the list of inputs
     * and call this.
     *
     * @param texts
     * @return
     * @throws Exception
     */
    public List<GeocodingResult> geocode(List<TextInput> texts) throws Exception {

        // Add all documents first 
        // --------------------------
        List<GeocodingResult> results = new ArrayList<>();
        for (TextInput text : texts) {
            Document doc = Factory.newDocument(text.buffer);
            if (text.id == null) {
                text.id = utility.genTextID(text.buffer);
            }

            FeatureMap fm = Factory.newFeatureMap();
            fm.put("rec_id", text.id);
            doc.setFeatures(fm);

            feeder.add(doc);
        }
        controller.execute();

        // Retrieve the the various geocoding metadata into a flattened list.
        // --------------------------
        for (Document doc : feeder) {
            String rec_id = (String) doc.getFeatures().get("rec_id");
            GeocodingResult annotations = new GeocodingResult(rec_id);
            annotations.retrieveGeocodes(doc, params);
            if (annotations.hasGeocodes()) {
                results.add(annotations);
            }
            Factory.deleteResource(doc);
            doc = null;
        }

        feeder.clear();

        reportMemory();

        return results;
    }

    @Override
    public void reportMetrics() {
        super.reportMetrics();
        log.info("=================\nSimple Geocoder");
        log.info(this.processingMetric.toString());
    }

    /**
     * Please shutdown the application cleanly when done.
     */
    public void shutdown() {

        reportMetrics();

        if (controller != null) {
            controller.interrupt();
            controller.cleanup();
            Factory.deleteResource(controller);
        }
        if (feeder != null) {
            feeder.cleanup();
            Factory.deleteResource(feeder);
        }
    }
}
