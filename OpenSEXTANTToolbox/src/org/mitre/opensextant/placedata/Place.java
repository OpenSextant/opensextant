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

import java.io.Serializable;

/**
 * A Place represents a named geographic location, a "place". It contains
 * basic information about that place, such as its name, its geographic
 * location, the country it is part of or located in, what type of place it is
 * (e.g. city, river, province) and the original source of this information. These
 * reflect the data traditionally found in gazetteers.
 */
public class Place implements Comparable<Object>, Serializable {

    private static final long serialVersionUID = 2389068012345L;

    // Name metadata
    private String placeName;
    private char name_type = NAME_TYPE; // Default.

    // The geospatial data
    private String countryCode;
    private String admin1;
    private String admin2;
    private String featureClass;
    private String featureCode;
    private Geocoord geocoord = new Geocoord();
    private String sourceNameID;
    private String sourceFeatureID;
    private String placeID;
    private String source;
    // the a priori estimates
    private Double name_bias;
    private Double id_bias;

    // construct a place with defaults
    public Place() {
    }

    @Override
    public String toString() {
        String output = this.placeName + "(" + this.getAdmin1() + ","
                + this.countryCode + "," + this.featureCode + ")";
        return output;
    }

    // two Places with the same PlaceID are the same "place"
    // two Places with different PlaceIDs ARE PROBABLY different "places"
    @Override
    public int compareTo(Object other) {
        if (!(other instanceof Place)) {
            return 0;
        }
        Place tmp = (Place) other;
        return this.placeID.compareTo(tmp.placeID);
    }

    /**
     * Is this Place a Country?
     * 
     * @return - true if this is a country or "country-like" place
     */
    public boolean isCountry() {
        return featureCode.startsWith("PCL");
        /** matches("PCL.*") actually invokes a new Regex */
    }

    /**
     * Is this Place a State or Province?
     * 
     * @return - true if this is a State, Province or other first level admin
     *         area
     */
    public boolean isAdmin1() {
        return "ADM1".equalsIgnoreCase(featureCode);
    }

    /**
     * Is this Place a National Capital?
     * 
     * @return - true if this is a a national Capital
     *         area
     */
    public boolean isNationalCapital() {
        return "PPLC".equalsIgnoreCase(featureCode);
    }

    // The getters and setters
    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getFeatureClass() {
        return featureClass;
    }

    public void setFeatureClass(String featureClass) {
        this.featureClass = featureClass;
    }

    public String getFeatureCode() {
        return featureCode;
    }

    public void setFeatureCode(String featureCode) {
        this.featureCode = featureCode;
    }

    public Double getLatitude() {
        return this.geocoord.getLatitude();
    }

    public void setLatitude(Double latitude) {
        this.geocoord.setLatitude(latitude);
    }

    public Double getLongitude() {
        return this.geocoord.getLongitude();
    }

    public void setLongitude(Double longitude) {
        this.geocoord.setLongitude(longitude);
    }

    public String getSourceNameID() {
        return sourceNameID;
    }

    public void setSourceNameID(String uni) {
        this.sourceNameID = uni;
    }

    public String getSourceFeatureID() {
        return sourceFeatureID;
    }

    public void setSourceFeatureID(String ufi) {
        this.sourceFeatureID = ufi;
    }

    public String getPlaceID() {
        return placeID;
    }

    public void setPlaceID(String placeID) {
        this.placeID = placeID;
    }

    public String getAdmin1() {
        return admin1;
    }

    public void setAdmin1(String key) {
        this.admin1 = key;
    }

    public String getAdmin2() {
        return admin2;
    }

    public void setAdmin2(String key) {
        this.admin2 = key;
    }

    /**
     * Get the original source of this information.
     */
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Geocoord getGeocoord() {
        return geocoord;
    }

    public void setGeocoord(Geocoord geocoord) {
        this.geocoord = geocoord;
    }

    /**
     * The name bias is a measure of the a priori likelihood that
     * a mention of this place's name actually refers to a place.
     */
    public Double getName_bias() {
        return name_bias;
    }

    public void setName_bias(Double name_bias) {
        this.name_bias = name_bias;
    }

    /**
     * The ID bias is a measure of the a priori likelihood that
     * a mention of this name refers to this particular place.
     */
    public Double getId_bias() {
        return id_bias;
    }

    public void setId_bias(Double id_bias) {
        this.id_bias = id_bias;
    }
    private String geohash;

    /** Set and get Geohash -- this is delegated to caller
     *  as core processing need not have a geohash generated when lat/lon is set.
     */
    public void setGeohash(String gh) {
        geohash = gh;
    }

    public String getGeohash() {
        return geohash;
    }

    public void setName_type(char t) {
        this.name_type = t;
    }

    public char getName_type() {
        return name_type;
    }

    public boolean isAbbreviation() {
        return this.name_type == ABBREVIATION_TYPE;
    }
    public final static char ABBREVIATION_TYPE = 'A';
    public final static char NAME_TYPE = 'N';
}
