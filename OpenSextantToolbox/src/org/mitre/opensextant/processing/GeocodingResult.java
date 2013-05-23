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

import gate.Annotation;
import gate.FeatureMap;
import gate.Utils;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import org.mitre.opensextant.placedata.Geocoord;
import org.mitre.opensextant.placedata.Place;
import org.mitre.opensextant.placedata.PlaceCandidate;
//import org.mitre.opensextant.processing.Parameters;
import org.mitre.xcoord.GeocoordMatch;

import org.jgeohash.GeoHashUtils;


/**
 *
 * @author Marc C. Ubaldino, MITRE <ubaldino at mitre dot org>
 */
public class GeocodingResult {

    public List<Geocoding> geocodes = new ArrayList<>();
    
    /** short ID or name of file*/
    public String recordID = null;
    /** Original file for record */
    public String recordFile = null;
    /** Text version of file used for processing */
    public String recordTextFile = null;
    private final static Parameters DEFAULT_FILTERS = new Parameters();
    public Map<String, Object> attributes = null;
    
    /** Given a record ID, create a container for holding onto all the geocodes
     * for that particular data object.
     */
    public GeocodingResult(String rid) {
        recordID = rid;
        if (recordID == null) {
            recordID = "";
        }
    }

    /** Add some piece of amplifying metadata about the record which may be carried through to output format in some way
     */
    public void addAttribute(String f, Object v) {
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        attributes.put(f, v);
    }

    private String getAnnotationID(int x) {
        return recordID + "." + x;
    }

    public boolean hasGeocodes() {
        return !geocodes.isEmpty();
    }

    public void retrieveGeocodes(gate.Document doc) throws ProcessingException {
        retrieveGeocodes(doc, DEFAULT_FILTERS);
    }

    /**<pre> Parse out geocodes into a more linear list of common objects.
     * This is a complicated post-processing step. Which is why it is important to do 
     * this in one place.
     * 
     *  Geocoding is a TextEntity (aka a text span)
     *   + has place object
     *      +  ... the place object has a Geocoord
     * 
     * Methods attached to each result are: 
     * GAZ         - a gazetteer place record.
     * CTRY        - a country name variation
     * COORD+xxxx  - coordinates with pattern ID
     * </pre>
     *  
     */
    public void retrieveGeocodes(gate.Document doc, Parameters params) throws ProcessingException {
        int incr = 0;
        String content = doc.getContent().toString();
        int content_length = content.length();

        // Initialize name and coords, and get feature map for the next step
        String match;
        String placename;

        for (Annotation a : doc.getAnnotations().get(ResultsUtility.GATE_GEOCODE_ANNOTATIONS)) {

            ++incr;
            FeatureMap fm = a.getFeatures();

            // Hmmmm... we have to try a few
            // possibilities where the actual matched text lives.
            // 
            match = Utils.cleanStringFor(doc, a);

            if (match == null) {
                throw new ProcessingException("Match should not be null for ANNOT=" + a.getType());
            }
            Geocoding geo = new Geocoding(getAnnotationID(incr), match);

            geo.start = Utils.start(a).longValue();
            geo.end = Utils.end(a).longValue();

            //Places and Geocoords are handled differently
            if ((params.tag_places | params.tag_countries)
                    && ResultsUtility.isPlaceName(a.getType())) {
                // get the PlaceCanidate obj which represent the all the
                PlaceCandidate placeMeta = (PlaceCandidate) fm.get(ResultsUtility.PLACE_CANDIDATE_ANNOTATION);

                // get the 
                geo.place = placeMeta.getBestPlace();

                if (geo.place.isCountry()) {
                    if (!params.tag_countries) {
                        // Allrighty ... Place names and Country names are mixed together
                        // which is why we're in this branch now.  But if we omitting country names
                        // from output do so now.
                        // IMPACT: Countries found will not appear in GeocodingResult nor in GIS output formats downstream.
                        // 
                        continue;
                    }
                    geo.method = "CTRY";
                    geo.is_country = true;
                    geo.is_administrative = geo.is_country;
                } else {
                    geo.method = "GAZ";
                    geo.is_place = true;
                    geo.is_administrative = geo.place.isAdmin1();
                    // For now tracking ADM1 names and abbreviations suffices.
                    //  is "AL." administrative?  yes
                    //  is "Alabama" administrative? yes.
                    //  is "al " ... no.
                    // 
                    //  While short terms or codes can be tagged as a place, it will help to have
                    //  a flag to indicate if this short name/code is a state or province code.
                    //
                    
                    geo.precision = ResultsUtility.getFeaturePrecision(geo.place.getFeatureClass(), geo.place.getFeatureCode());
                }

                geo.confidence = placeMeta.getBestPlaceScore();

                if ((Parameters.RUNTIME_FLAGS & Parameters.FLAG_EXTRACT_CONTEXT) > 0) {
                    // TODO: Acquire "context" in a more consistent fashion for both Places and coords.
                    ResultsUtility.setContextFor(content, geo, (int)geo.start, match.length(), content_length);
                }

            } else if (params.tag_coordinates
                    && ResultsUtility.isCoordinate(a.getType())) {

                placename = (String) fm.get("name");
                if (placename == null) {
                    placename = (String) fm.get("string");
                }

                // Coordinates -- here there are a few place holders. 
                //
                Geocoord g = (Geocoord) fm.get("geo");
                
                // Filter out duplicate coordinates as you post-process you results
                // E.g., caller may set output_coordinate_duplicates = false to omit dups from Output
                // Allowing duplicate coordinates in JAPE rules should have little impact 
                // 
                if (g.is_duplicate && ! params.output_coordinate_duplicates){
                    continue;
                }
                String form = (String) fm.get("geoform");
                geo.method = "COORD+" + form;

                // Trying to formalize the method for creating a "Geocoding" result
                // that represents a found coordinate
                // 
                copy(g, geo);
                
                // TODO: fix this.  
                geo.place.setPlaceName(placename);
            }
            
            // Enrich with geohash.
            
            // TODO:  incorporate Geohash as a core field for  enrichment.
            String gh = GeoHashUtils.encode(geo.place.getLatitude(), geo.place.getLatitude());
            geo.place.setGeohash(gh);

            geocodes.add(geo);
        }

    }

    /** Since "Geocoords" are currently GATE-based annotations (from the GeocoordFinder PR)
     *  the creation of m and g are performed outside of this and some attributes may 
     * already be set.
     * 
     * currently:  Geocoding() ctr (id + matchtext), and start/end offsets are set prior.
     */
    public static void copy(Geocoord m, Geocoding g) {

        g.is_coordinate = true;

        g.precision = m.precision;
        g.place = new Place();
        g.place.setGeocoord(m);
        // Copying context from Geocoord up to instance.
        g.setContext(m.getContextBefore(), m.getContextAfter());

        g.confidence = 0.90;
        g.place.setCountryCode("TBD"); // TODO: reverse lookup
        g.place.setFeatureClass("S");
        g.place.setFeatureCode("SITE");
        g.place.setAdmin1("*"); // TODO:  use reverse lookkup 
    }

    /** Add a coordinate match from XCoord directly 
     *  Migrate these two things together...  
     * 
     *    copy(xcoord match, geocoding)
     *    copy(geocoord, geocoding)
     */
    public static void copy(GeocoordMatch m, Geocoding g) {
        g.is_coordinate = true;

        g.precision = m.getPrecision();
        g.place = new Place();
        Geocoord loc = new Geocoord();
        loc.setLatitude(m.latitude);
        loc.setLongitude(m.longitude);
        g.place.setGeocoord(loc);

        // Copying context from Geocoord up to instance.
        g.setContext(m.getContextBefore(), m.getContextAfter());

        g.confidence = 0.90;
        g.place.setCountryCode("TBD"); // TODO: reverse lookup
        g.place.setFeatureClass("S");
        g.place.setFeatureCode("SITE");
        g.place.setAdmin1("*"); // TODO:  use reverse lookkup 

    }
}
