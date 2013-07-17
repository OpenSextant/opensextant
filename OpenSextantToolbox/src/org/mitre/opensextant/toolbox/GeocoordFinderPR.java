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
import java.net.URL;
import java.util.List;
import org.mitre.opensextant.placedata.Geocoord;
import org.mitre.flexpat.TextMatch;
import org.mitre.opensextant.processing.Parameters;
import org.mitre.xcoord.GeocoordMatch;
import org.mitre.xcoord.XCoord;
import org.mitre.xcoord.XCoordException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * <p> The Geocoord Finder uses regular expression rules specified in an
 * external file to identify geographic coordinates (geocoords) in a GATE {@link gate.Document Document}.
 * Any geocoord found will be added to the output {@link gate.AnnotationSet
 * AnnotationSet}. </p>
 *
 * <p> Features for Geocoord Annotations include:
 * <ul>
 * <li>string - text of the document containing the geocoord</li> 
 * <li>geoform - name of the geocoord type</li> 
 * <li>rule - name of the rule the geocoord satisfied</li> 
 * <li>geo - object storing information about this geocoord</li> 
 * </ul> </p>
 *
 */
@CreoleResource(name = "OpenSextant Geocoord Finder", comment = "A simple plugin that finds and standardizes geographic coordinates")
public class GeocoordFinderPR extends AbstractLanguageAnalyser implements
        ProcessingResource {

    private static final long serialVersionUID = 1375472181851584128L;
    // the xcoord object which does all of the work
    private XCoord xc;
    // the annotationSet into which the geocoords will be written
    private String outputAnnotationSet;
    // the document being processed
    // private gate.Document document;
    // the file containing the geocoord patterns
    private URL patternFile = null;
    // the log
    static Logger log = LoggerFactory.getLogger(GeocoordFinderPR.class);

    /**
     * Initializes the GeocoordFinder resource.
     */
    private void initialize() {
        // initialize the XCoord
        xc = new XCoord(false);
        try {
            xc.configure(patternFile);
        } catch (XCoordException e) {
            log.error("GeocoordFinderPR: Error when loading patternfile" + patternFile.toString() + ":" + e.getMessage());
        }
        // since we don't need the pre/post match text set to length 0
        xc.setMatchWidth(0);

    }// end initialize

    /**
     *
     * @return
     * @throws ResourceInstantiationException
     */
    @Override
    public Resource init() throws ResourceInstantiationException {
        this.initialize();
        return this;
    }

    /**
     *
     * @throws ResourceInstantiationException
     */
    @Override
    public void reInit() throws ResourceInstantiationException {
        this.initialize();
    }

    // find all of the geocoords in a string and put them into a AnnotationSet
    void findGeocoords(String text, AnnotationSet annotSet)
            throws ExecutionException {

        List<TextMatch> geoResults = xc.extract_coordinates(text, document.getName()).matches;

        // create an annotation for each result
        for (TextMatch t : geoResults) {

            /**
             * XCoord TextMatch denotes when an item is a match within another
             * match such cases are more than likely false-positives.
             */
            if (t.is_submatch) {
                continue;
            }

            GeocoordMatch geomatch = (GeocoordMatch) t;


            // fill in all the annotation features
            FeatureMap feats = Factory.newFeatureMap();
            // The matched text:
            feats.put("string", geomatch.getText());
            feats.put("geomatch", geomatch);

            // create and populate a geocoord -- This XY point is 
            // only used for place name disambiguation in the pipeline.
            // the output is still the GeocoordMatch, g
            Geocoord geo = new Geocoord(geomatch.latitude, geomatch.longitude);
            
            // The normalized coordinate
            geo.setExpression(geomatch.coord_text);
            feats.put("geo", geo);

            feats.put("hierarchy", "Geo.place.geocoordinate");
            feats.put("isEntity", true);

            // create a "geocoord" annotation
            try {
                annotSet.add(geomatch.start, geomatch.end, "geocoord", feats);
            } catch (InvalidOffsetException e) {
                log.error("GeoCoordFinder: Invalid Offset exception when creating geocoord annotation" + e.getMessage());
            }

        }

    }

    /**
     * Converts the GATE document to a string and finds all matching geocoord substrings. Each geocoord found becomes
     * a geocoord annotation.
     * 
     * @throws ExecutionException
     */
    @Override
    public void execute() throws ExecutionException {

        //  Shunt added to allow us to use the same GAPP setup for our application
        //  but temporarily by pass XCoord extraction if we know a data source
        //  has no coordinates.
        // 
        if ((Parameters.RUNTIME_FLAGS & Parameters.FLAG_NO_COORDINATES) >0) {
            return;
        }

        // get the annotation set into which we will place any annotations found
        AnnotationSet annotSet = (outputAnnotationSet == null || outputAnnotationSet
                .equals("")) ? document.getAnnotations() : document
                .getAnnotations(outputAnnotationSet);

        // get the text of the document
        String text = getDocument().getContent().toString();
        // find the geocoords
        findGeocoords(text, annotSet);
    }


    /**
     *
     * @return  annotSet ????
     */
    public String getOutputAnnotationSet() {
        return outputAnnotationSet;
    }

    /**
     *
     * @param outputAnnotationSet
     */
    @Optional
    @RunTime
    @CreoleParameter
    public void setOutputAnnotationSet(String outputAnnotationSet) {
        this.outputAnnotationSet = outputAnnotationSet;
    }

    /**
     *
     * @return
     */
    public URL getPatternFile() {
        return patternFile;
    }

    /**
     *
     * @param patternFile
     */
    @CreoleParameter
    public void setPatternFile(URL patternFile) {
        this.patternFile = patternFile;
    }
} // class GeocoordFinder
