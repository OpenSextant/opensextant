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

import gate.Annotation;
import gate.AnnotationSet;
import gate.ProcessingResource;
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mitre.opensextant.placedata.PlaceCandidate;
import org.mitre.opensextant.placedata.PlaceEvidence;
import org.mitre.opensextant.placedata.PlaceEvidence.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the GATE ProcessingResource wrapper for the Cantilever class. It performs a 
 * geospatial-specific form of co-referencing. It also propagates evidence
 * from a geospatial entity to co-references of that entity.
 */
@CreoleResource(name = "OpenSextant Cantilever Processor", comment = "A plugin that performs a simple form of co-reference matching among geospatial entities ")
public class CantileverPR extends AbstractLanguageAnalyser implements
		ProcessingResource {

	private static final long serialVersionUID = -5055098862407377701L;
	private String outputAnnotationSet;
	private String candidateAnnotationName;
	private String candidateFeatureName;
	
	private boolean DOCOREF = true;

	private Cantilever cntlvr;
	private Scorer scr;
	static Logger log = LoggerFactory.getLogger(CantileverPR.class);

	private void initialize() {
		log.info("Initializing Cantilever");
		cntlvr = new Cantilever();
		scr = new Scorer();
	}

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

	/**
	 * This does the actual work of analyzing the evidence attached to the place
	 * candidates. It invokes Cantilever's <code>propagateEvidence()</code> for the
	 * place candidates found in each document. 
     * 
     * @throws ExecutionException 
     */
	public void execute() throws ExecutionException {

		// the list of PC objects
		List<PlaceCandidate> pcList = new ArrayList<PlaceCandidate>();

		// get the annotation set
		AnnotationSet annotSet = (outputAnnotationSet == null || outputAnnotationSet
				.equals("")) ? document.getAnnotations() : document
				.getAnnotations(outputAnnotationSet);

		// Get all of the placecandidate annotations
		AnnotationSet candidateSet = annotSet.get(candidateAnnotationName);
		// add each of the PlaceCandidate objs which are attached to the
		// annotations to the list
		for (Annotation candAnno : candidateSet) {
			// get the PlaceCandidate object
			PlaceCandidate pc = (PlaceCandidate) candAnno.getFeatures().get(candidateFeatureName);
			if (pc != null) {
				pcList.add(pc);
			} else {
				log.error("Null PC on annotation" + candAnno.toString());
			}
		}// end place candidate loop

		// enable/disable co-referencing
		if(DOCOREF){
			// do the coreferencing and propagate the evidence amongst the PCs
			cntlvr.propagateEvidence(pcList);

			// collect the document level evidence
			List<PlaceEvidence> docEvidList = collectDocumentEvidence(annotSet);

			// attach document level evidence to scorer
			scr.setDocumentLevelEvidence(docEvidList);
		}
		
		// score and rank the Places in each PC according to the evidence
		scr.score(pcList);

	}// end execute



	/**
     *
     * @return
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
    public String getCandidateAnnotationName() {
		return candidateAnnotationName;
	}

	/**
     *
     * @param candidateAnnotationName
     */
    @Optional
	@RunTime
	@CreoleParameter(defaultValue = "placecandidate")
	public void setCandidateAnnotationName(String candidateAnnotationName) {
		this.candidateAnnotationName = candidateAnnotationName;
	}

	/**
     *
     * @return
     */
    public String getCandidateFeatureName() {
		return candidateFeatureName;
	}

	/**
     *
     * @param candidateFeatureName
     */
    @Optional
	@RunTime
	@CreoleParameter(defaultValue = "placeCandidate")
	public void setCandidateFeatureName(String candidateFeatureName) {
		this.candidateFeatureName = candidateFeatureName;
	}

	
	private List<PlaceEvidence> collectDocumentEvidence (AnnotationSet annotSet){
		
		Double countryWeight = 0.1;
		Double adminWeight = 0.05;
		
		
		// document level country and admin1  evidence
		List<PlaceEvidence> docEvidList = new ArrayList<PlaceEvidence>();
		
		
		// collect all of the countries, capitals and admin1s mentioned
		AnnotationSet countrySet = annotSet.get("Country");
		AnnotationSet capitalSet = annotSet.get("NationalCapital");
		AnnotationSet adminSet = annotSet.get("Admin1");

		// how many times has a country been mentioned
		 Map<String, Integer> countryCounts = new HashMap<String, Integer>();
		 Map<String, Double> countryBiases = new HashMap<String, Double>();
		 Double countryTotal =0.0;

		// how many times has an admin1 been mentioned
		 Map<String, Integer> adminCounts = new HashMap<String, Integer>();
		 Map<String, Double> adminBiases = new HashMap<String, Double>();
		 Double adminTotal =0.0;
		 
		 // collect country counts
		for (Annotation countryAnno : countrySet) {
			String tmpCC = (String) countryAnno.getFeatures().get("countryCode");
			String countryName = (String) countryAnno.getFeatures().get("string");

			// TODO replace with abbreviation when added to gazetteer
			// don't use countries when only country code
			if (countryName != null && countryName.length() > 3) {
				if (!countryCounts.keySet().contains(tmpCC)) {
					countryCounts.put(tmpCC, 0);
				}

				countryCounts.put(tmpCC, countryCounts.get(tmpCC) + 1);
				countryTotal = countryTotal + 1;
			}
		}


		
		// collect capital counts
		for (Annotation capitalAnno : capitalSet) {
			String tmpCC = (String) capitalAnno.getFeatures().get("countryCode");
			String capitalName = (String) capitalAnno.getFeatures().get("string");

			// TODO replace with abbreviation when added to gazetteer
			// don't use capital when only code/short
			if (capitalName != null && capitalName.length() > 3) {
				if (!countryCounts.keySet().contains(tmpCC)) {
					countryCounts.put(tmpCC, 0);
				}
				countryCounts.put(tmpCC, countryCounts.get(tmpCC) + 1);
				countryTotal = countryTotal + 1;
			}
		}

		// collect admin counts
		for (Annotation adminAnno : adminSet) {
			String tmpCC = (String) adminAnno.getFeatures().get("countryCode");
			String tmpAdmCode = (String) adminAnno.getFeatures().get("adm1code");
			String adminName = (String) adminAnno.getFeatures().get("string");

			String adminKey = tmpCC + "/" + tmpAdmCode;

			// TODO replace with abbreviation when added to gazetteer
			// don't use countries/admin when only admin code
			if (adminName != null && adminName.length() > 3 && tmpAdmCode != null && tmpAdmCode.length() > 1) {

				if (!countryCounts.keySet().contains(tmpCC)) {
					countryCounts.put(tmpCC, 0);
				}

				if (!adminCounts.keySet().contains(adminKey)) {
					adminCounts.put(adminKey, 0);
				}

				countryCounts.put(tmpCC, countryCounts.get(tmpCC) + 1);
				countryTotal = countryTotal + 1;
				adminCounts.put(adminKey, adminCounts.get(adminKey) + 1);
				adminTotal = adminTotal + 1;
			}
		}

		// normalize country and admin counts by total seen
		for (String cc : countryCounts.keySet()) {
			countryBiases.put(cc, countryCounts.get(cc) / countryTotal);
		}
		
		for (String ac : adminCounts.keySet()) {
			adminBiases.put(ac, adminCounts.get(ac) / adminTotal);
		}
		
		//create document level evidence based on the countries and admin1s seen
		
			for (String cc : countryBiases.keySet()) {
				PlaceEvidence ccEvid = new PlaceEvidence();
				ccEvid.setCountryCode(cc);
				ccEvid.setScope(Scope.DOCUMENT);
				ccEvid.setWeight(countryWeight * countryBiases.get(cc));
				ccEvid.setRule("CountryBias");
				docEvidList.add(ccEvid);
			}

			for (String adminKey : adminBiases.keySet()) {
				PlaceEvidence acEvid = new PlaceEvidence();
				String[] pieces = adminKey.split("/");
				acEvid.setCountryCode(pieces[0]);
				acEvid.setAdmin1(pieces[1]);
				acEvid.setScope(Scope.DOCUMENT);
				acEvid.setWeight(adminWeight *adminBiases.get(adminKey));
				acEvid.setRule("AdminBias");
				docEvidList.add(acEvid);
			}

		return docEvidList;
	}
	
	
} // end class
