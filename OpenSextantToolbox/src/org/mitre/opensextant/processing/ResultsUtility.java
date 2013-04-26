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
import gate.Utils;
import java.text.DecimalFormat;
import org.mitre.opensextant.util.TextUtils;
import org.mitre.opensextant.extraction.TextEntity;


/**
 * Abstract class encapsulating basic results formatter functionality.
 *
 */
public class ResultsUtility  {

    // -------------
    public final static String DEFAULT_PL_ANNOT_TYPE = "PLACE";
    public final static String DEFAULT_PL_FEAT_TYPE = "placeCandidate";
    public final static String DEFAULT_XY_ANNOT_TYPE = "geocoord";
    
    /** The default TEXT WIDTH */
    public static int TEXT_WIDTH = 200;


    /**
     * Given the GATE annotation set the context on the TextEntity object.
     */
    public static void setPrePostContextFor(String content, Annotation annot, TextEntity t, int match_size, int doc_size) {
        if (t.getContextAfter() != null) {
            return;
        }

        Long x1 = Utils.start(annot);
        // Long x2 = Utils.end(annot);

        int[] bounds = TextUtils.get_text_window(x1.intValue(), match_size, doc_size, TEXT_WIDTH);

        //TextEntity t = new TextEntity();
        t.setContext(
                content.substring(bounds[0], bounds[1]), // text before match
                content.substring(bounds[2], bounds[3])); // text after match        
    }

    /**
     * Given the GATE annotation set the context on the TextEntity object.
     */
    public static void setContextFor(String content, Annotation annot, 
            TextEntity t, int match_size, int doc_size) {
        
        if (t.getContext() != null) {
            return;
        }

        Long x1 = Utils.start(annot);
        int[] bounds = TextUtils.get_text_window(x1.intValue(), doc_size, TEXT_WIDTH);

        t.setContext(TextUtils.squeeze_whitespace(content.substring(bounds[0], bounds[1]))); // text after match        
    }

    /**
     * Is this a Location annotation type?
     */
    public static boolean isLocation(String a) {
        return DEFAULT_XY_ANNOT_TYPE.equals(a) || DEFAULT_PL_ANNOT_TYPE.equals(a);
    }

    /**
     * Is this a Location geocoordinate annotation type?
     */
    public static boolean isCoordinate(String a) {
        return DEFAULT_XY_ANNOT_TYPE.equals(a);
    }

    /**
     * Is this a Location placename annotation type?
     */
    public static boolean isPlaceName(String a) {
        return DEFAULT_PL_ANNOT_TYPE.equals(a);
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
}
