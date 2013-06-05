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
import org.mitre.xtemporal.DateMatch;
import org.mitre.xtemporal.XTempException;
import org.mitre.xtemporal.XTemporal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * <p>
 * The Date Finder uses regular expression rules specified in an external
 * file to identify dates in a GATE {@link gate.Document Document}. Any
 * date found will be added to the output {@link gate.AnnotationSet
 * AnnotationSet}.
 * </p>
 * 
 * <p>
 * <b>Date {@link gate.AnnotationSet Annotation}</b>:<br>
 * <ul>
 * <li><b>type</b> = "date" </li>
 * </ul>
 * <ul>
 * <b>Features</b>:
 * <li>string - text of the document containing the geocoord</li>
 * <li>normedDateText - string form of the standarized date</li>
 * <li>normedDate - standardized date as a Java Date
 * </ul>
 * </p>
 * 
 * <p>
 * 
 * 
 * </p>
 * 
 */
@CreoleResource(name = "OpenSextant Date Finder", comment = "A simple plugin that finds and standardizes date references")
public class DateFinderPR extends AbstractLanguageAnalyser implements
		ProcessingResource {

	private static final long serialVersionUID = 1375472181851584128L;

	// the xtemporal object which does all of the work
	private XTemporal xt;

	// the annotationSet into which the dates will be written
	private String outputAnnotationSet;


	// the file containing the geocoord patterns
	private URL patternFile = null;

	// the log
	static Logger log = LoggerFactory.getLogger(DateFinderPR.class);

	/**
	 * Initializes the DateFinderPR resource.
	 */
	private void initialize() {
		// initialize the Xtemporal
		xt = new XTemporal(false);
		try {
			xt.configure(patternFile);
		} catch (XTempException e) {
			log.error("DateFinderPR: Error when loading patternfile" + patternFile.toString() + ":" + e.getMessage());

		}
		// since we don't need the pre/post match text set to length 0
		xt.setMatchWidth(0);

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

	// find all of the dates in a string and put them into a AnnotationSet
	void findDates(String text, AnnotationSet annotSet)
			throws ExecutionException {

		
		List<TextMatch> timeResults = xt.extract_dates(text, document.getName()).matches;

		// create an annotation for each result
		if(timeResults != null){
		for (TextMatch t : timeResults) {

                        if (t.is_submatch){
                            continue;
                        }

			DateMatch d = (DateMatch) t;
			// fill in all the annotation features
			FeatureMap feats = Factory.newFeatureMap();
			feats.put("string", d.getText());
			feats.put("normedDateText", d.datenorm_text);
			feats.put("normedDate", d.datenorm);
			feats.put("datePattern", d.pattern_id);
			feats.put("hierarchy", "Time.date");

			// create a "date" annotation
			try {
				annotSet.add((long) d.start, (long) d.end, "Date", feats);
			} catch (InvalidOffsetException e) {
				log.error("DateFinderPR: Invalid Offset exception when creating date annotation" + e.getMessage());
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
		findDates(text, annotSet);
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
