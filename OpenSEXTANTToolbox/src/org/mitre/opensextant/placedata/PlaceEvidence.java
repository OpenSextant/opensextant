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
 * A PlaceEvidence represents a fragment of evidence about a Place. Its intended
 * purpose is to represent evidence about a Place's identity which has been
 * extracted from a document. This evidence is used to help disambiguate
 * (distinguish among) places which have the same or similar names. It is
 * intentionally very similar to the Place class to facilitate comparisons
 * with that class.
 * 
 */
public class PlaceEvidence implements Comparable<Object>, Serializable {

	/**
	 * SCOPE - Where did this evidence come from wrt to the PlaceCandidate
	 * it is part of?
	 * <ul>
	 * <li>APRIORI - derived from the gazetteer only, not from any information in the document
	 * </li><li>LOCAL - directly associated with this instance of PC
	 * </li><li>COREF - associated with another (related) PC in the document
	 * </li><li>MERGED - came from the merger of multiple PlaceEvidences (future use)   
	 * </li><li>DOCUMENT - in the same document but has no other direct association
	 * </li></ul>
	 */
	public enum Scope {
		APRIORI,LOCAL, COREF, MERGED,DOCUMENT
	};

	private static final long serialVersionUID = 2389068012345L;

	/** The geospatial data, the actual evidence found */
	private String placeName = null;
	private String countryCode = null;
	private String admin1 = null;
	private String featureClass = null;;
	private String featureCode = null;
	private Geocoord geocoord = null;

	// The rule which found the evidence
	private String rule = null;

	// the scope from which this evidence came
	private Scope scope = Scope.LOCAL;

	// The strength of the evidence
	private Double weight = null;

	public PlaceEvidence() {
	};

	// copy constructor
	public PlaceEvidence(PlaceEvidence old) {
		this();
		this.setAdmin1(old.getAdmin1());
		this.setCountryCode(old.getCountryCode());
		this.setFeatureClass(old.getFeatureClass());
		this.setFeatureCode(old.getFeatureCode());
		this.setGeocoord(old.getGeocoord());
		this.setPlaceName(old.getPlaceName());
		this.setRule(old.getRule());
		this.setScope(old.getScope());
		this.setWeight(old.getWeight());
	};


	// compare to other evidence by strength
	public int compareTo(Object other) {
		if (!(other instanceof PlaceEvidence)) {
			return 0;
		}
		PlaceEvidence tmp = (PlaceEvidence) other;
		return this.weight.compareTo(tmp.weight);
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

	public String getAdmin1() {
		return admin1;
	}

	public void setAdmin1(String admin1) {
		this.admin1 = admin1;
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

	public Geocoord getGeocoord() {
		return geocoord;
	}

	public void setGeocoord(Geocoord geocoord) {
		this.geocoord = geocoord;
	}

	public Scope getScope() {
		return scope;
	}

	public void setScope(Scope scope) {
		this.scope = scope;
	}

	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}

	public Double getWeight() {
		return weight;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
	}

	// Override toString to get a reasonable string label for this PlaceEvidence
	public String toString() {
		String output = this.rule + "-" + this.scope + "/" + this.weight  + "(" + this.placeName + "," + this.admin1 + "," + this.countryCode + ")";
		return output;
	}
	
}
