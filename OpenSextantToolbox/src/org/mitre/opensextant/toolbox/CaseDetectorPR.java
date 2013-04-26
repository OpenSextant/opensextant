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
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 
 * This GATE Processing Resource  determines if a document appears to be in all/mostly lower case, all/mostly upper case or proper mixed case.
 * Its primary purpose is to allow downstream rule sets to be applied only where their case assumptions are valid. 
 *
 */
@CreoleResource(name = "OpenSextant Case Detector ", comment = "Determines if the document is in proper case, all upper case or all lower case")
public class CaseDetectorPR extends AbstractLanguageAnalyser implements
		ProcessingResource {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8638479048484746444L;
	// The parameters passed in by the user
	String inputASName; // The name of the input AnnotationSet
	String tokenAnnoName; // the annotation to examine,usually "Token"
	String sentenceAnnoName; // the annotation to examine,usually "Sentence"
	String stringFeatureName; // the existing feature on tokenAnnoType
								// containing the word, usually "string"
	String caseFeatureName; // the feature on the token which is the case
							// category usually "orth"
	String caseDecisionName; // the feature to create on the document that
								// contains the result of the analysis

	// Log object
	static Logger log = LoggerFactory.getLogger(CaseDetectorPR.class);

	// the thresholds thst define the majority case level
	//TODO: make these runtime parameters
	Double lowerCaseThresh  = 0.90;
	Double upperCaseThresh  = 0.90;
	Double sentenceRatioThresh = 1.0;
	
	
	private void initialize() {

	}

	/**
     *
     * @return
     * @throws ResourceInstantiationException
     */
    @Override
	public Resource init() throws ResourceInstantiationException {
		initialize();
		return this;
	}

	/**
     *
     * @throws ResourceInstantiationException
     */
    @Override
	public void reInit() throws ResourceInstantiationException {
		initialize();
	}

	/**
     *
     * @throws ExecutionException
     */
    @Override
	public void execute() throws ExecutionException {

		// If no Annotation set was given, use the default
		AnnotationSet annotSet = (inputASName == null || inputASName.equals("")) ? document
				.getAnnotations() : document.getAnnotations(inputASName);

		// the histogram of case types counts seen in the document
		Map<String, Integer> caseCounts = new HashMap<String, Integer>();

		// find out how many sentences there are in the document 
		AnnotationSet sentenceSet = annotSet.get(sentenceAnnoName);
		Integer sentenceCount = sentenceSet.size();
		if(sentenceCount == null){
			sentenceCount = 1;
		}
		
		// get the tokens
		AnnotationSet tokenSet = annotSet.get(tokenAnnoName);
		
		//see if there any tokens to work with
		if(tokenSet == null || tokenSet.size() == 0){
			log.error("No tokens found in " + document.getName()) ;
			return;
		}

		// accumulate the case stats
		for (Annotation an : tokenSet) {
			FeatureMap fm = an.getFeatures();
			// get the case for this token
			String kase = (String) fm.get(caseFeatureName);

			if (!caseCounts.containsKey(kase)) {
				caseCounts.put(kase, 0);
			}
			// increment case count
			caseCounts.put(kase, caseCounts.get(kase) + 1);
		}

		// make decision
		String decision = null;
		
		// sum total relevant counts
		//TODO: the case names should be parameterized
		Integer lowercaseCount = caseCounts.get("lowercase");
		Integer uppercaseCount = caseCounts.get("allCaps");
		Integer initialcaseCount = caseCounts.get("upperInitial");
		
		// set nulls to 0
		if(lowercaseCount == null){
			lowercaseCount =0;
		}
		if(uppercaseCount == null){
			uppercaseCount =0;
		}
		if(initialcaseCount == null){
			initialcaseCount =0;
		}
		
		
		//calculate percentage of total for each case category
		Double total = 1.0 * lowercaseCount + uppercaseCount + initialcaseCount;
		double lowerPercent = lowercaseCount/total;
		double upperPercent = uppercaseCount/total;
		//double initialPercent = initialcaseCount/total;
		double sentenceInitialRatio = initialcaseCount/(1.0*sentenceCount);
		
		// if mostly lower case and (significantly) fewer initials than sentences
		if(lowerPercent > lowerCaseThresh && sentenceInitialRatio < sentenceRatioThresh){
			decision = "LOWERCASE";
		}
		
		// if mostly upper case
		if(upperPercent   > upperCaseThresh){
			decision = "UPPERCASE";
		}
		
		// if neither of the above, must be proper
		if(decision == null){
			decision = "PROPERCASE";
		}
		

		// attach decision to document
		document.getFeatures().put(caseDecisionName, decision);

		// cleanup
		caseCounts.clear();
		
		
	}// end execute

	/**
     *
     * @return
     */
    public String getInputASName() {
		return inputASName;
	}

	/**
     *
     * @param inputASName
     */
    @Optional
	@RunTime
	@CreoleParameter
	public void setInputASName(String inputASName) {
		this.inputASName = inputASName;
	}

	/**
     *
     * @return
     */
    public String getTokenAnnoName() {
		return tokenAnnoName;
	}

	/**
     *
     * @param tokenAnnoName
     */
    @Optional
	@RunTime
	@CreoleParameter(defaultValue = "Token")
	public void setTokenAnnoName(String tokenAnnoName) {
		this.tokenAnnoName = tokenAnnoName;
	}

	
	/**
     *
     * @return
     */
    public String getSentenceAnnoName() {
		return sentenceAnnoName;
	}

	/**
     *
     * @param sentenceAnnoName
     */
    @Optional
	@RunTime
	@CreoleParameter(defaultValue = "Sentence")
	public void setSentenceAnnoName(String sentenceAnnoName) {
		this.sentenceAnnoName = sentenceAnnoName;
	}

	/**
     *
     * @return
     */
    public String getStringFeatureName() {
		return stringFeatureName;
	}

	/**
     *
     * @param stringFeatureName
     */
    @Optional
	@RunTime
	@CreoleParameter(defaultValue = "string")
	public void setStringFeatureName(String stringFeatureName) {
		this.stringFeatureName = stringFeatureName;
	}

	/**
     *
     * @return
     */
    public String getCaseFeatureName() {
		return caseFeatureName;
	}

	/**
     *
     * @param caseFeatureName
     */
    @Optional
	@RunTime
	@CreoleParameter(defaultValue = "orth")
	public void setCaseFeatureName(String caseFeatureName) {
		this.caseFeatureName = caseFeatureName;
	}

	/**
     *
     * @return
     */
    public String getCaseDecisionName() {
		return caseDecisionName;
	}

	/**
     *
     * @param caseDecisionName
     */
    @Optional
	@RunTime
	@CreoleParameter(defaultValue = "CaseDecision")
	public void setCaseDecisionName(String caseDecisionName) {
		this.caseDecisionName = caseDecisionName;
	}

	

}
