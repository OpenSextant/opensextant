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

import gate.AnnotationSet;
import gate.Factory;
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
import gate.util.InvalidOffsetException;

import java.net.URL;
import java.util.List;

import org.mitre.flexpat.TextMatch;
import org.mitre.opensextant.poli.PatternsOfLife;
import org.mitre.opensextant.poli.PoliException;
import org.mitre.opensextant.poli.PoliMatch;
import org.mitre.opensextant.poli.data.MACAddress;
import org.mitre.opensextant.poli.data.Money;
import org.mitre.opensextant.poli.data.TelephoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * <p>
 * The Pattern of Life Finder uses regular expression rules specified in an
 * external file to identify entity type such as phone numbers, money and others
 * in a GATE {@link gate.Document Document}. Any date found will be added to the
 * output {@link gate.AnnotationSet AnnotationSet}.
 * </p>
 */
@CreoleResource(name = "OpenSextant POLI Finder", comment = "A simple plugin that finds various entity types")
public class PatternOfLifeFinderPR extends AbstractLanguageAnalyser implements
		ProcessingResource {

	private static final long serialVersionUID = 1375472181851584128L;

	// the POLI object which does all of the work
	private PatternsOfLife poli;

	// the annotationSet into which the dates will be written
	private String outputAnnotationSet;

	// the file containing the patterns
	private URL patternFile = null;

	// the log
	static Logger log = LoggerFactory.getLogger(PatternOfLifeFinderPR.class);

	/**
	 * Initializes the PR resource.
	 */
	private void initialize() {
		// initialize the the PatternsOfLife matcher
		poli = new PatternsOfLife(false);
		try {
			poli.configure(patternFile);
		} catch (PoliException e) {
			log.error("POLIFinderPR: Error when loading patternfile"
					+ patternFile.toString() + ":" + e.getMessage());
		}
		// since we don't need the pre/post match text set to length 0
		poli.setMatchWidth(0);

	}// end initialize

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

	// find all of the polis in a string and put them into a AnnotationSet
	void findPOLIs(String text, AnnotationSet annotSet)
			throws ExecutionException {
		List<TextMatch> poliResults = poli.extract_patterns(text,
				document.getName(), null).matches;

		// create an annotation for each result
		if (poliResults != null) {
			for (TextMatch t : poliResults) {

				if (t.is_submatch) {
					continue;
				}

				PoliMatch p = (PoliMatch) t;
				// fill in all the annotation features
				FeatureMap feats = Factory.newFeatureMap();
				feats.put("string", p.getText());
				String patID = p.pattern_id;
				
				feats.put("patternID", patID);
				

				String tmpType = "PatternOfLife";
				String tmpHier = "Information.identifier";

				if (patID.startsWith("CYBER.MAC")) {
					tmpType = "MACAddress";
					tmpHier = "Information.identifier.MACAddress";
				}

				if (patID.startsWith("MONEY.AMT")) {
					tmpType = "Money";
					tmpHier = "Object.finance.money";
				}

				if (patID.startsWith("PHONE")) {
					tmpType = "TelephoneNumber";
					tmpHier = "Information.identifier.telephoneNumber";
				}

				if (patID.startsWith("TITLE")) {
					 tmpType = "DocumentTitle";
					 tmpHier = "Information.identifier.documentTitle";
				}

				feats.put("hierarchy", tmpHier);
				feats.put("isEntity", true);
				// create a annotation
				try {
					annotSet.add((long) p.start, (long) p.end, tmpType, feats);
				} catch (InvalidOffsetException e) {
					log.error("DateFinderPR: Invalid Offset exception when creating date annotation"
							+ e.getMessage());
				}

			}
		}
	}

	/**
	 * 
	 * @throws ExecutionException
	 */
	public void execute() throws ExecutionException {

		// get the annotation set into which we will place any annotations found
		AnnotationSet annotSet = (outputAnnotationSet == null || outputAnnotationSet
				.equals("")) ? document.getAnnotations() : document
				.getAnnotations(outputAnnotationSet);

		// get the text of the document
		String text = getDocument().getContent().toString();
		// find the dates
		findPOLIs(text, annotSet);
	}

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
	public URL getPatternFile() {
		return patternFile;
	}

	/**
	 * 
	 * @param patternFile
	 */
	@CreoleParameter
	public void setPatternFile(URL patternFile) {
		this.patternFile = patternFile;
	}

} // class DateFinderPR
