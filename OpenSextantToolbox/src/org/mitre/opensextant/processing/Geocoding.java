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

import org.mitre.opensextant.extraction.TextEntity;
import org.mitre.opensextant.placedata.Place;

/**
 * TODO: this begins to duplicate the metadata in PlaceCandidate
 * 
 * @author Marc C. Ubaldino, MITRE <ubaldino at mitre dot org>
 */
public class Geocoding extends TextEntity {

    /** Use placedata.Place ? */
    public Place place = null;
    public String id = null;
    public String method = null;
    public double confidence = -0.001;
    public int precision  = 100000; // 100 KM
    // Can only be one of the following:
    public boolean is_coordinate = false;
    public boolean is_country = false;
    public boolean is_place = false;
    public boolean is_administrative = false;
    
    public boolean filtered_out = false;

    public Geocoding(String id, String match) {
        this.id = id;
        this.text = match;
    }
    
    /*
     * Copy constructor
     */
    public Geocoding(Geocoding other) {
        super();
        copy(other);
    }

    public void copy(Geocoding other) {
        super.copy(other);
        this.place = other.place;
        this.id = other.id;
        this.method = other.method;
        this.confidence = other.confidence;
        this.precision  = other.precision;
        this.is_coordinate = other.is_coordinate;
        this.is_country = other.is_country;
        this.is_place = other.is_place;
        this.is_administrative = other.is_administrative;
    }
    
    
}
