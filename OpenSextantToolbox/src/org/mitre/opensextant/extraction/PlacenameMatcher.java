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
package org.mitre.opensextant.extraction;

import gate.Annotation;
import gate.Document;
import java.io.IOException;
import java.util.List;

import org.mitre.opensextant.placedata.PlaceCandidate;

/**
 * Abstract superclass for classes that tag place names in a GATE document.
 *
 * @author ubaldino
 */
public abstract class PlacenameMatcher {

    /**
     *
     * @throws IOException
     */
    public PlacenameMatcher() throws IOException{
        // null ctor    
    }

    /**
     * Given a GATE Document, and optionally tokens and feature name, generate a
     * list of matching place candidates.
     * 
     * @return List of place candidates
     * @throws MatcherException  
     */
    public abstract List<PlaceCandidate> tagDocument(Document doc,
            List<Annotation> tokens, String featureName) throws MatcherException;

    /**
     *
     */
    public abstract void cleanup();

    /**
     * Forms a key from two long integers which is useful for identifying the same span by start/end offsets.   
     */
    public String getKey(long x1, long x2) {
        return x1 + "_" + x2;
    }
}
