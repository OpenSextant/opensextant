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
package org.mitre.opensextant.toolbox;

import gate.AnnotationSet;
import gate.Factory;
import gate.FeatureMap;
import gate.ProcessingResource;
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;
import gate.util.InvalidOffsetException;

import java.io.IOException;
import java.util.List;

import org.mitre.opensextant.extraction.PlacenameMatcher;
import org.mitre.opensextant.extraction.ExtractionMetrics;
import org.mitre.opensextant.placedata.PlaceCandidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Solr-based ProcessingResource that tags mentions of geospatial candidates found in a dcoument.
 * The <code>SOLR_HOME</code> environment variable must be set to the location of the Solr server.
 *  
 * @author David Smiley, MITRE, dsmiley@mitre.org
 * @author Marc Ubaldino, MITRE, ubaldino@mitre.org
 * 
 */
@CreoleResource(name = "OpenSextant NaiveTaggerSolr", comment = "A Solr-based tagger")
public class NaiveTaggerSolrPR
        extends AbstractLanguageAnalyser implements ProcessingResource {

    /**
     * 
     */
    private static final long serialVersionUID = -6167312014577862928L;
    // Log object
    static Logger log = LoggerFactory.getLogger(NaiveTaggerSolrPR.class);
    private PlacenameMatcher matcher;
    private String outputASName;
    private String annotationType;
    // The parameters passed in by the user
    String inputASName; // The name of the input AnnotationSet
    String tokenAnnoName; // the type of the annotation to examine, usually "Token"
    String tokenFeatureName; // the name of the feature on tokenAnnoType to examine
    Boolean tagAbbreviations; // tag placenames which are abreviations or codes
    //TODO expose CALIBRATE and CALIBRATE_SCORE as PR parameters 
    // to force all confidences to CALIBRATE_SCORE for calibration
    boolean CALIBRATE = false;
    Double CALIBRATE_SCORE = 0.0;
    
    private static ExtractionMetrics taggingTimes = new  ExtractionMetrics("tagging");
    private static ExtractionMetrics retrievalTimes = new  ExtractionMetrics("retrieval");
    private static ExtractionMetrics matcherTotalTimes = new  ExtractionMetrics("matcher-total");

    /**
     *
     * @return gate_resource
     * @throws ResourceInstantiationException
     */
    @Override
    public Resource init() throws ResourceInstantiationException {
        super.init();
        try {
            matcher = new PlacenameMatcher();            
            matcher.setAllowLowerCaseAbbreviations(tagAbbreviations);
        } catch (IOException ioerr) {
            throw new ResourceInstantiationException("Failed to initialize Solr Matcher", ioerr);
        }
        return this;//weird
    }
    
    public static ExtractionMetrics getTaggingMetric(){
        return taggingTimes;
    }
    public static ExtractionMetrics getRetrievalMetric(){
        return retrievalTimes;
    }
    public static ExtractionMetrics getTotalsMetric(){
        return matcherTotalTimes;
    }

    /**
     *
     */
    @Override
    public void cleanup() {
        super.cleanup();
    }

    /**
     * Uses a SolrMatcher object to tag place names.
     *     Key elements:
     *
     * <pre> 
     * + SOLR_HOME -- see the Gazetteer/solr folder for the data index used here
     * + Matcher -- PlaceNameMatcher has the logic for matching; SolrProxy
     *    is used to broker interaction with the Solr server at SOLR_HOME. 
     *    If Given a URL, SolrMatcher will attempt to use a solr server via http -- this is not common
     * + PlaceNameMatcher -- wraps SolrProxy, which brokers access 
     * + MatcherException -- error to throw for low level matching implementation
     * </pre>
     * @throws ExecutionException
     */

    @Override
    public void execute() throws ExecutionException {
        if (matcher == null) {
            throw new IllegalStateException("This PR hasn't been init'ed!");
        }

        List<PlaceCandidate> matches = null;
        try {
            matches = matcher.tagText(document.getContent().toString(), document.getName());
            retrievalTimes.addTime( matcher.getRetrievingNamesTime() );
            taggingTimes.addTime( matcher.getTaggingNamesTime() );
            matcherTotalTimes.addTime( matcher.getTotalTime() );
            
        } catch (Exception err) {
            log.error("Error when tagging document " + document.getName(), err);
            return;
        }

        // If no output Annotation set was given, append to the input AS
        AnnotationSet annotSet = (output_as_name ? document.getAnnotations(outputASName) : document.getAnnotations());

        for (PlaceCandidate pc : matches) {
            // create and populate the PlaceCandidate annotation
            FeatureMap feats = Factory.newFeatureMap();
            feats.put("string", pc.getPlaceName());
            feats.put("placeCandidate", pc);

            if (CALIBRATE) {
                pc.setPlaceConfidenceScore(CALIBRATE_SCORE);
            }
            try {
                annotSet.add(pc.getStart(), pc.getEnd(), annotationType, feats);
            } catch (InvalidOffsetException offsetErr) {
                // Silent.
                // Should do something more interesting.
            }
        }
    }
    
    /**
     *
     * @return
     */
    public String getInputASName() {
        return inputASName;
    }

    /**
     *
     * @param inputASName
     */
    @Optional
    @RunTime
    @CreoleParameter
    public void setInputASName(String inputASName) {
        this.inputASName = inputASName;
    }

    /**
     *
     * @return
     */
    public String getOutputASName() {
        return outputASName;
    }

    /**
     *
     * @return
     */
    public String getAnnotationType() {
        return annotationType;
    }

    /**
     *
     * @param annotationType
     */
    @Optional
    @RunTime
    @CreoleParameter(defaultValue = "placecandidate")
    public void setAnnotationType(String annotationType) {
        this.annotationType = annotationType;
    }
    private boolean output_as_name = false;

    /**
     *
     * @param outputASName
     */
    @Optional
    @RunTime
    @CreoleParameter
    public void setOutputASName(String outputASName) {
        this.outputASName = outputASName;
        output_as_name = (outputASName != null && !outputASName.isEmpty());
    }

 

    /**
     *
     * @return
     */
    public String getTokenAnnoType() {
        return tokenAnnoName;
    }

    /**
     *
     * @param tokenAnnoType
     */
    @Optional
    @RunTime
    @CreoleParameter(defaultValue = "Token")
    public void setTokenAnnoType(String tokenAnnoType) {
        this.tokenAnnoName = tokenAnnoType;
    }

    /**
     *
     * @return
     */
    public String getTokenFeature() {
        return tokenFeatureName;
    }

    /**
     *
     * @param tokenFeature
     */
    @Optional
    @RunTime
    @CreoleParameter(defaultValue = "string")
    public void setTokenFeature(String tokenFeature) {
        this.tokenFeatureName = tokenFeature;
    }

    public Boolean getTagAbbreviations() {
        return tagAbbreviations;
    }

    @Optional
    @CreoleParameter(defaultValue = "false")
    public void setTagAbbreviations(Boolean tagAbbreviations) {
        this.tagAbbreviations = tagAbbreviations;
    }
}
