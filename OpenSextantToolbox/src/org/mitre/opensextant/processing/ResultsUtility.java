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

import java.text.DecimalFormat;
import org.mitre.opensextant.util.TextUtils;
import org.mitre.opensextant.extraction.TextEntity;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

/**
 * Abstract class encapsulating basic results formatter functionality.
 *
 */
public class ResultsUtility {

    // -------------
    public final static String PLACE_ANNOTATION = "PLACE";
    public final static String PLACE_CANDIDATE_ANNOTATION = "placeCandidate";
    public final static String GEOCOORD_ANNOTATION = "geocoord";
    public final static Set<String> GATE_GEOCODE_ANNOTATIONS = new HashSet<String>();

    static {
        // This annot set matches "isLocation(annotType)"
        GATE_GEOCODE_ANNOTATIONS.add(GEOCOORD_ANNOTATION);
        GATE_GEOCODE_ANNOTATIONS.add(PLACE_ANNOTATION);
    }
    /**
     * The default TEXT WIDTH. ~75 chars per line yields 2 lines of text.
     */
    public static int TEXT_WIDTH = 150;

    /**
     * Given the GATE annotation set the context on the TextEntity object.
     */
    public static void setPrePostContextFor(String content, TextEntity t, int offset, int match_size, int doc_size) {
        if (t.getContextAfter() != null) {
            return;
        }

        int[] bounds = TextUtils.get_text_window(offset, match_size, doc_size, TEXT_WIDTH);

        t.setContext(
                content.substring(bounds[0], bounds[1]), // text before match
                content.substring(bounds[2], bounds[3])); // text after match        
    }

    /**
     * Given the GATE annotation set the context on the TextEntity object.
     */
    public static void setContextFor(String content,
            TextEntity t, int offset, int match_size, int doc_size) {

        if (t.getContext() != null) {
            return;
        }

        int[] bounds = TextUtils.get_text_window(offset, doc_size, TEXT_WIDTH);

        t.setContext(TextUtils.squeeze_whitespace(content.substring(bounds[0], bounds[1]))); // text after match        
    }

    /**
     * Is this a Location annotation type?
     */
    public static boolean isLocation(String a) {
        return GEOCOORD_ANNOTATION.equals(a) || PLACE_ANNOTATION.equals(a);
    }

    /**
     * Is this a Location geocoordinate annotation type?
     */
    public static boolean isCoordinate(String a) {
        return GEOCOORD_ANNOTATION.equals(a);
    }

    /**
     * Is this a Location placename annotation type?
     */
    public static boolean isPlaceName(String a) {
        return PLACE_ANNOTATION.equals(a);
    }
    /**
     * Control floating point accuracy on any results.
     *
     * @return A string representation of a double with a fixed number of digits
     * to the right of the decimal point.
     */
    final static DecimalFormat confFmt = new DecimalFormat("0.000");

    public static String formatConfidence(double d) {
        return confFmt.format(d);
    }
    /**
     * Precision -- this is a first draft attempt at assigning some error bars
     * to geocoding results.
     *
     * TODO: move this to a configuration file
     *
     * feat/code: prec # precision is meters of error for a given gazetteer
     * entry with feat/code)
     *
     * A/ADM1: 50000 # ADM1 is generally +/- 50km, world wide P/PPL: 1000 # city
     * is generally +/- 1km within center point P/PPLC: 10000 # major capital
     * city is 10km of error, etc.
     *
     */
    public final static Map<String, Integer> FEATURE_PRECISION = new HashMap<String, Integer>();
    public final static Map<String, Integer> FEATURE_GEOHASH_PRECISION = new HashMap<String, Integer>();
    public final static int DEFAULT_PRECISION = 50000; // +/- 50KM
    public final static int DEFAULT_GEOHASH_PRECISION = 5;
    
    static {
        FEATURE_PRECISION.put("P", 5000);
        FEATURE_PRECISION.put("A", DEFAULT_PRECISION);
        FEATURE_PRECISION.put("S", 1000);

        FEATURE_PRECISION.put("A/ADM1", DEFAULT_PRECISION);
        FEATURE_PRECISION.put("A/ADM2", 20000);
        FEATURE_PRECISION.put("P/PPL", 5000);
        FEATURE_PRECISION.put("P/PPLC", 10000);

        // This helps guage how long should a geohash be for a given feature.
        FEATURE_GEOHASH_PRECISION.put("A/PCLI", 3);
        FEATURE_GEOHASH_PRECISION.put("CTRY", 3);
        FEATURE_GEOHASH_PRECISION.put("P", 6);
        FEATURE_GEOHASH_PRECISION.put("A", 4);
        FEATURE_GEOHASH_PRECISION.put("S", 8);
        FEATURE_GEOHASH_PRECISION.put("A/ADM2", 5);
    }

    /**
     * For a given feature type and code, determine what sort of resolution or
     * precision should be considered for that place, approximately.
     *
     * @return precision approx error in meters for a given feature. -1 if no
     * feature type given.
     */
    public static int getFeaturePrecision(String feat_type, String feat_code) {

        if (feat_type == null && feat_code == null) {
            // Unknown, uncategorized feature
            return DEFAULT_PRECISION;
        }

        String lookup = (feat_code != null
                ? feat_type + "/" + feat_code : feat_type);

        Integer prec = FEATURE_PRECISION.get(lookup);

        if (prec != null) {
            return prec.intValue();
        }

        prec = FEATURE_PRECISION.get(feat_type);
        if (prec != null) {
            return prec.intValue();
        }

        return DEFAULT_PRECISION;
    }

    /** For a given Geonames feature class/designation provide a guess about how long
     * geohash should be.
     */
    public static int getGeohashPrecision(String feat_type, String feat_code) {
        if (feat_type == null && feat_code == null) {
            // Unknown, uncategorized feature
            return DEFAULT_GEOHASH_PRECISION;
        }

        String lookup = (feat_code != null
                ? feat_type + "/" + feat_code : feat_type);

        Integer prec = FEATURE_GEOHASH_PRECISION.get(lookup);

        if (prec != null) {
            return prec.intValue();
        }

        prec = FEATURE_GEOHASH_PRECISION.get(feat_type);
        if (prec != null) {
            return prec.intValue();
        }

        return DEFAULT_GEOHASH_PRECISION;
    }
}
