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

import org.mitre.opensextant.phonetic.Phoneticizer;

/**
 * This GATE ProcessingResource implements a tagger which adds a "phonetically" reduced string to tokens as a feature. The
 * nature/form of that reduction is determined by the algorithm selected via the <code>setPhoneticAlgo()</code> method. 
 * These algorithms include
 * case removal, diacritic stripping, punctuation removal and others. For
 * all available algorithms see org.mitre.opensextant.phonetic.Phoneticizer.
 * 
 */
@SuppressWarnings("serial")
@CreoleResource(name = "OpenSextant PhoneticTagger", comment = "Tags tokens with a phonetic or reduced form")
public class PhoneticTaggerPR extends AbstractLanguageAnalyser implements
		ProcessingResource {

	// The parameters passed in by the user
	String inputASName; // The name of the input AnnotationSet
	String outputASName; // The name of the output AnnotationSet
	String tokenAnnoName; // the annotation to examine,usually "Token"
	String stringFeatureName; // the existing feature on tokenAnnoType containg the word
	String phoneticFeatureName; // the phonetic feature on tokenAnnoType to create
	String phoneticAlgo; // algorithm/method used to produce the phonetic form

	// the thing that does all the phonetic work
	Phoneticizer  phoner = null;

	// Log object
	//private static Logger log = Logger.getLogger(NaiveTaggerMicroPR.class);

	private void initialize() {
		 phoner = new Phoneticizer();
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

		// If no output Annotation set was given, append to the input AS
		AnnotationSet annotSet = (outputASName == null || outputASName.equals("")) ? document.getAnnotations() : document.getAnnotations(outputASName);

		// get the tokens
		AnnotationSet tokenSet = annotSet.get(tokenAnnoName);

		for (Annotation an : tokenSet) {
			String tmp = (String) an.getFeatures().get(stringFeatureName);
			String phonForm = phoner.phoneticForm(tmp, phoneticAlgo);
			an.getFeatures().put(phoneticFeatureName, phonForm);
		}

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
    public String getOutputASName() {
		return outputASName;
	}

	/**
     *
     * @param outputASName
     */
    @Optional
	@RunTime
	@CreoleParameter
	public void setOutputASName(String outputASName) {
		this.outputASName = outputASName;
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
    public String getPhoneticFeatureName() {
		return phoneticFeatureName;
	}

	/**
     *
     * @param phoneticFeatureName
     */
    @Optional
	@RunTime
	@CreoleParameter(defaultValue = "phoneticString")
	public void setPhoneticFeatureName(String phoneticFeatureName) {
		String tmp = phoneticFeatureName;
		// don't overwrite "string", the default token feature name
		if(phoneticFeatureName.equalsIgnoreCase("string")){
			tmp = phoneticFeatureName + "_reduced";
		}
		// don't allow output feature same as input feature name
		// not sure this will always prevent overwrite
		if(phoneticFeatureName.equalsIgnoreCase(this.getStringFeatureName())){
			tmp = phoneticFeatureName + "_reduced";
		}
		this.phoneticFeatureName = tmp;
	}

	/**
     *
     * @return
     */
    public String getPhoneticAlgo() {
		return phoneticAlgo;
	}

	/**
     *
     * @param phoneticAlgo
     */
    @Optional
	@RunTime
	@CreoleParameter(defaultValue = "Simple_Phonetic0")
	public void setPhoneticAlgo(String phoneticAlgo) {
		this.phoneticAlgo = phoneticAlgo;
	}

}
