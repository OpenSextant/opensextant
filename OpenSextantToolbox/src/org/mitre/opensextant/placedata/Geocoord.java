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
package org.mitre.opensextant.placedata;

import org.mitre.opensextant.extraction.TextEntity;

/**
 * A Geocoord represents spatial coordinates on the globe. 
 */
public class Geocoord extends TextEntity implements java.io.Serializable {

   
    private static final long serialVersionUID = -3313528469542406371L;
    // canonical form is decimal degree
    Double latitude = null;
    Double longitude = null;

    // the phrase as found in the document
    String expression = "";

    public int precision = -1;
    /**
     * Set to false if expression cannot be interpreted as meaningful lat/lon
     */
    public boolean isValid = false;

    // empty public constructor so this class be used like a Bean
    public Geocoord() {
        isValid = true;        
    }

    public Geocoord(double lat, double lon) {
        isValid = true;        
        setLatitude(lat);
        setLongitude(lon);
    }

    
    //TODO We need a real distance method
    

    /**
     * This returns distance in degrees. Only to be used for fast comparison of relative 
     * distances from the same point.
     * 
     * @return distance from the given latitude and longitude, in degrees.
     */
    public Double distanceDeg(double lat, double lon) {
        Double d = Double.MAX_VALUE;
        if (isValid) {
            d = Math.sqrt(Math.pow((this.latitude - lat), 2.0)
                    + Math.pow((this.longitude - lon), 2.0));
        } else {
            // System.err.println("Null in Geocoord" + this.expression);
        }
        return d;
    }

    /**
     * This returns distance in degrees. Only to be used for fast comparison of relative 
     * distances from the same point.
     * 
     * @return distance from the given Geocoord, in degrees.
     */
    public Double distanceDeg(Geocoord another) {
        Double d = Double.MAX_VALUE;
        if (isValid && another != null) {
            d = Math.sqrt(Math.pow((this.latitude - another.getLatitude()), 2.0)
                    + Math.pow((this.longitude - another.getLongitude()), 2.0));
        } else {
            // System.err.println("Null in Geocoord" + this.expression);
        }
        return d;
    }
    
    
    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    /**
     * If the coordinates were extracted from  an explicit text mention, 
	 * such as a latitude-longitude pair or Military Grid Reference number, this
     * returns the text mention from which the spatial coordinates were derived.
     * 
     */
    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        if (isValid) {
            return "(" + this.latitude + "," + this.longitude + ")";
        }
        return "(NULL)";
    }
}
