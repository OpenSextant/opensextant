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
import gate.Controller;
import gate.FeatureMap;
import gate.ProcessingResource;
import gate.Resource;
import gate.Utils;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ControllerAwarePR;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mitre.opensextant.placedata.Geocoord;
import org.mitre.opensextant.placedata.Place;
import org.mitre.opensextant.placedata.PlaceCandidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This PR is used as a diagnostic tool.
 * 
 */
@CreoleResource(name = "OpenSextant PlaceCandidateDiag", comment = "Diag tool for OS")
public class PlaceCandidateDiagPR extends AbstractLanguageAnalyser implements
		ProcessingResource, ControllerAwarePR {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private File outputDir = null;

	private File dumpFile = null;
	BufferedWriter dumpWriter = null;

	private File missFile = null;
	BufferedWriter missWriter = null;

	private File ruleFile = null;
	BufferedWriter ruleWriter = null;

	Map<String, Integer[]> ruleStats = new HashMap<String, Integer[]>();

	private File vocabFile = null;
	BufferedWriter vocabWriter = null;

	Map<String, Integer[]> vocabStats = new HashMap<String, Integer[]>();
	Map<String, Integer> misses = new HashMap<String, Integer>();

	private Integer docCount =0;
	
	// the types of span interactions
	enum INTERACTION {
		NONE, EXACT, OVERLAP, INSIDE, OUTSIDE
	};

	// Log object
	private static Logger log = LoggerFactory
			.getLogger(PlaceCandidateDiagPR.class);

	private void initialize() {
		log.info("Initializing ");
		docCount =0;
		openFiles(true);
	}

	// Do the initialization
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

	// Re-do the initialization
	/**
     *
     * @throws ResourceInstantiationException
     */
    @Override
	public void reInit() throws ResourceInstantiationException {
		initialize();
	}

	// Do the work
	/**
     *
     * @throws ExecutionException
     */
    @Override
	public void execute() throws ExecutionException {

		// get all of the "placecandidate" annotations
		AnnotationSet candSet = document.getAnnotations().get("placecandidate");
		// get all of the PLACE annotations from the "KEY" annotation set
		AnnotationSet keySet = document.getAnnotations("Key").get("PLACE");
		// make a copy of the key set to keep track of unmatched annotations
		Set<Annotation> unmatchedKeySet = new HashSet<Annotation>(keySet);

		// sort the place candidates in document order
		List<Annotation> sortedCandidates = gate.Utils.inDocumentOrder(candSet);
		docCount++;
		System.out.println("("+ docCount + ") This document has " + candSet.size()
				+ " candidates " + " and " + keySet.size() + " keys");

		// loop over all place candidates
		for (Annotation cand : sortedCandidates) {
			// get the PlaceCandidate object which is attached to each
			// placecandidate annotation
			PlaceCandidate pc = (PlaceCandidate) cand.getFeatures().get("placeCandidate");
			
			Place bestPlace = pc.getBestPlace();
			Place truthPlace = null;
			
			
			String cleanName = Utils.cleanString(pc.getPlaceName());
			
			

			String candString = PCToString(pc);

			Long candStart = cand.getStartNode().getOffset();
			Long candEnd = cand.getEndNode().getOffset();

			Long contxtSize = 75L;
			Long contextStart = candStart - contxtSize;
			Long contextEnd = candEnd + contxtSize;

			if (contextStart < 0) {
				contextStart = 0L;
			}

			if (contextEnd > Utils.lengthLong(document)) {
				contextEnd = Utils.lengthLong(document);
			}

			String context = Utils.cleanStringFor(document, contextStart,
					contextEnd);

			// the place/not-place score
			boolean isPlace = pc.getPlaceConfidenceScore() > 0.0;
			List<String> others = new ArrayList<String>();

			AnnotationSet otherPCs = candSet.get(cand.getStartNode()
					.getOffset(), cand.getEndNode().getOffset());

			if (otherPCs.size() > 1) {
				for (Annotation o : otherPCs) {
					others.add(Utils.cleanStringFor(document, o));
				}
			}

			// the span relations between a candidate and a key
			List<INTERACTION> interactions = new ArrayList<INTERACTION>();
			Integer interactionCount = 0;
			List<String> matchedStrings = new ArrayList<String>();

			Long truthStart = 0L;
			Long truthEnd = 0L;

			String truthString = truthToString(null);

			for (Annotation k : keySet) {
				// compare the PC to the keys
				INTERACTION tmp = interaction(cand, k);

				if (!tmp.equals(INTERACTION.NONE)) {

					truthPlace = (Place) k.getFeatures().get("PLACE");
					truthString = truthToString(k);

					if (interactionCount == 0) {
						truthStart = k.getStartNode().getOffset();
						truthEnd = k.getEndNode().getOffset();
					}

					interactions.add(tmp);
					interactionCount++;
					matchedStrings.add(Utils.cleanStringFor(document, k));
					// if any interaction remove from list of unmatched keys
					unmatchedKeySet.remove(k);
				}

			}// end compare to key loop

			
			
			String placeCompareString = placeCompare(bestPlace, truthPlace);
			
			String assessment = "UNSET";

			// assess the interaction and score combinations
			// no interaction
			if (interactions.size() == 0) {
				if (isPlace) {
					assessment = "FP";
				} else {
					assessment = "TN";
				}

			} else { // some kind of interaction
				if (isPlace) {
					assessment = "TP";
				} else {
					assessment = "FN";
				}
			}

			// collect rule info
			List<String> rules = pc.getRules();
			List<Double> ruleScores = pc.getConfidences();

			for (int rIndex = 0; rIndex < rules.size(); rIndex++) {
				// get the rule name and its weight
				String r = rules.get(rIndex);
				Double s = ruleScores.get(rIndex);

				// increment rules stats
				if (!ruleStats.containsKey(r)) {
					Integer[] tmp = { 0, 0 };
					ruleStats.put(r, tmp);
				}

				// flip polarity for negative rules
				String ruleAssessment = "";

				if (s < 0.0) {
					if (assessment.equals("TP")) {
						ruleAssessment = "WRONG";
					}

					if (assessment.equals("TN")) {
						ruleAssessment = "RIGHT";
					}

					if (assessment.equals("FP")) {
						ruleAssessment = "RIGHT";
					}

					if (assessment.equals("FN")) {
						ruleAssessment = "WRONG";
					}

				} else {
					if (assessment.equals("TP")) {
						ruleAssessment = "RIGHT";
					}

					if (assessment.equals("TN")) {
						ruleAssessment = "WRONG";
					}

					if (assessment.equals("FP")) {
						ruleAssessment = "WRONG";
					}

					if (assessment.equals("FN")) {
						ruleAssessment = "RIGHT";
					}

				}

				// increment the counts
				if (ruleAssessment.equals("RIGHT")) {
					ruleStats.get(r)[0]++;
				}

				if (ruleAssessment.equals("WRONG")) {
					ruleStats.get(r)[1]++;
				}

			}// end rule info loop

			// get vocab info
			// increment vocab stats

			if (!vocabStats.containsKey(cleanName)) {
				Integer[] tmp = { 0, 0, 0, 0 };
				vocabStats.put(cleanName, tmp);
			}

			if (assessment.equals("TP")) {
				vocabStats.get(cleanName)[0]++;
			}

			if (assessment.equals("TN")) {
				vocabStats.get(cleanName)[1]++;
			}

			if (assessment.equals("FP")) {
				vocabStats.get(cleanName)[2]++;
			}

			if (assessment.equals("FN")) {
				vocabStats.get(cleanName)[3]++;
			}

			// write out details
			try {
				dumpWriter.write(cleanName);
				dumpWriter.write("\t");
				dumpWriter.write(candStart.toString());
				dumpWriter.write("\t");
				dumpWriter.write(candEnd.toString());
				dumpWriter.write("\t");
				dumpWriter.write(matchedStrings.toString());
				dumpWriter.write("\t");
				dumpWriter.write(truthStart.toString());
				dumpWriter.write("\t");
				dumpWriter.write(truthEnd.toString());
				dumpWriter.write("\t");
				dumpWriter.write(context);
				dumpWriter.write("\t");
				dumpWriter.write(assessment);
				dumpWriter.write("\t");

				if (interactions.size() == 0) {
					dumpWriter.write(INTERACTION.NONE.toString());
				} else {
					dumpWriter.write(interactions.toString());
				}

				dumpWriter.write("\t");
				dumpWriter.write(placeCompareString);
				dumpWriter.write("\t");
				dumpWriter.write(candString);
				dumpWriter.write("\t");
				dumpWriter.write(truthString);
				dumpWriter.write("\t");
				dumpWriter.write(pc.getPlaceConfidenceScore().toString());
				dumpWriter.write("\t");
				dumpWriter.write(pc.getRules().toString());
				dumpWriter.write("\t");
				dumpWriter.write(pc.getEvidence().toString());
				dumpWriter.write("\t");
				dumpWriter.write(document.getName());
				dumpWriter.newLine();
				dumpWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}// end place candidate loop

		// dump out anything in the unmatched key set
		for (Annotation miss : unmatchedKeySet) {
			try {
				String cleanName = gate.Utils.cleanStringFor(document, miss);
				Long contxtSize = 75L;

				Long truthStart = miss.getStartNode().getOffset();
				Long truthEnd = miss.getEndNode().getOffset();

				String truthString = truthToString(miss);
				Long contextStart = truthStart - contxtSize;
				Long contextEnd = truthEnd + contxtSize;

				if (contextStart < 0) {
					contextStart = 0L;
				}

				if (contextEnd > Utils.lengthLong(document)) {
					contextEnd = Utils.lengthLong(document);
				}

				String context = Utils.cleanStringFor(document, contextStart,
						contextEnd);

				dumpWriter.write(" ");
				dumpWriter.write("\t");
				dumpWriter.write("0");
				dumpWriter.write("\t");
				dumpWriter.write("0");
				dumpWriter.write("\t");
				dumpWriter.write(cleanName);
				dumpWriter.write("\t");
				dumpWriter.write(truthStart.toString());
				dumpWriter.write("\t");
				dumpWriter.write(truthEnd.toString());
				dumpWriter.write("\t");
				dumpWriter.write(context);
				dumpWriter.write("\t");
				dumpWriter.write("FN_NoCandidate");
				dumpWriter.write("\t");
				dumpWriter.write(INTERACTION.NONE.toString());

				dumpWriter.write("\t");
				dumpWriter.write("?\t?\t?");
				dumpWriter.write("\t");
				dumpWriter.write(PCToString(null));
				dumpWriter.write("\t");
				dumpWriter.write(truthString);
				dumpWriter.write("\t");
				dumpWriter.write("0.0");
				dumpWriter.write("\t");
				dumpWriter.write(" ");
				dumpWriter.write("\t");
				dumpWriter.write(" ");
				dumpWriter.write("\t");
				dumpWriter.write(document.getName());
				dumpWriter.newLine();
				dumpWriter.flush();

				// add to missed name histogram
				if (!misses.containsKey(cleanName)) {
					misses.put(cleanName, 0);
				}
				misses.put(cleanName, misses.get(cleanName) + 1);

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		
		if(docCount % 500 == 0){
			System.out.println("Writing incremental stats at " + docCount + " documents");
			closeFiles(false);
			openFiles(false);
			writeStats();

		}
		
		
		
		
	}// end execute

	/**
     *
     * @return
     */
    public File getOutputDir() {
		return outputDir;
	}

	/**
     *
     * @param outputDir
     */
    @CreoleParameter(defaultValue = "C:\\dump\\PCDump")
	public void setOutputDir(File outputDir) {
		this.outputDir = outputDir;
	}

	/**
     *
     * @param arg0
     * @param arg1
     * @throws ExecutionException
     */
    @Override
	public void controllerExecutionAborted(Controller arg0, Throwable arg1)
			throws ExecutionException {
		closeFiles(true);
		openFiles(false);
		writeStats();
	}

	/**
     *
     * @param arg0
     * @throws ExecutionException
     */
    @Override
	public void controllerExecutionFinished(Controller arg0) throws ExecutionException {
		closeFiles(true);
		openFiles(false);
		writeStats();
	}

	// the interaction between a and b annotations
	private INTERACTION interaction(Annotation a, Annotation b) {

		// a fully before b
		if (a.getEndNode().getOffset() < b.getStartNode().getOffset()) {
			return INTERACTION.NONE;
		}

		// a fully after b
		if (a.getStartNode().getOffset() > b.getEndNode().getOffset()) {
			return INTERACTION.NONE;
		}

		// exactly the same
		if (a.coextensive(b)) {
			return INTERACTION.EXACT;
		}

		// a inside b
		if (a.withinSpanOf(b)) {
			return INTERACTION.INSIDE;
		}

		// a outside b (= b inside a)
		if (b.withinSpanOf(a)) {
			return INTERACTION.OUTSIDE;
		}

		// any other situation is called an overlap
		return INTERACTION.OVERLAP;
	}

	private String truthToString(Annotation t) {

		String[] keys = { "id", "type", "form", "country", "state", "county",
				"continent", "gazref", "latLong", "PLACE" , "CTV", "comment",
				"description", "mod", "nonLocUse", };
		String tmp = "";

		if (t == null) {
			for (String k : keys) {
				if (k.equalsIgnoreCase("PLACE")) {
				tmp = tmp + "\t" + " " + "\t" + " ";
				} else {
					tmp = tmp + "\t" + " ";
				}
			}
			return tmp;
		}

		FeatureMap fm = t.getFeatures();
		for (String k : keys) {
			if (k.equalsIgnoreCase("PLACE")) {

				Place pl = (Place) fm.get(k);
				if(pl != null){
					tmp = tmp + "\t" +  pl.getLatitude().toString() + "\t" + pl.getLongitude().toString();
				}else{
					tmp = tmp + "\t" + " "                          + "\t" + " ";
				}


			} else {
				String attr = (String) fm.get(k);
				tmp = tmp + "\t" + attr;
			}

		}

		return tmp;
	}

	private String PCToString(PlaceCandidate pc) {

		String tmp = "";

		if (pc == null) {
			tmp = " " + "\t" + " " + "\t" + " " + "\t" + " " + "\t" + " ";
			return tmp;
		}

		Place best = pc.getBestPlace();
		if (best == null) {
			tmp = " " + "\t" + " " + "\t" + " " + "\t" + " ";
			return tmp;
		}

		tmp = best.getCountryCode() + "\t" + best.getLatitude() + "\t"
				+ best.getLongitude() + "\t" + best.getFeatureCode();

		return tmp;
	}

	private String placeCompare(Place cand, Place truth) {

		String sameCountry = "?";
		//String sameAdm1 = "?";
		String closeGeo = "?";
		String samePlace = "false";
		
		Double dist = null;

		
		if(truth == null){
			return   sameCountry  +   "\t" + closeGeo + "\t" + dist  + "\t" + samePlace ;
		}
		
		// compare country
		String cndCC = cand.getCountryCode();
		String truthCC = truth.getCountryCode();
		if (cndCC != null && truthCC != null) {

			if (cndCC.equalsIgnoreCase(truthCC)) {
				sameCountry = "true";
			} else {
				sameCountry = "false";
			}

		}

		// compare admin1
		//String cndAdm = cand.getAdmin1();
		//String truthAdm = truth.getAdmin1();
		//if (cndAdm != null && truthAdm != null) {

		//	if (cndAdm.equalsIgnoreCase(truthAdm)) {
		//		sameAdm1 = "true";
		//	} else {
		//		sameAdm1 = "false";
		//	}

		//}

		Geocoord cndGeo = cand.getGeocoord();
		Geocoord truthGeo = truth.getGeocoord();
		// compare geo
		
		if (cndGeo != null && truthGeo != null && cndGeo.isValid && truthGeo.isValid) {
			 dist = cndGeo.distanceDeg(truthGeo);
			if (dist < .25) {
				closeGeo = "true";
			} else {
				closeGeo = "false";
			}

		}
		// build return

		String rtn = "";
		
		if(closeGeo.matches("true")){
			samePlace ="true";
			 rtn = sameCountry  +   "\t" + closeGeo + "\t" + dist  + "\t" + samePlace ;
			return rtn;
			
		}
		
		
		String candFC = cand.getFeatureCode();
		String truthFC = truth.getFeatureCode();
		
		if(candFC ==  null){
			candFC = "";
		}
		if(truthFC ==  null){
			truthFC = "";
		}
		
		
		
		// countries with same cc
		if(  candFC.matches("PCL.*") && truthFC.matches("COUNTRY") &&    sameCountry.matches("true")){
			samePlace ="true";
			 rtn = sameCountry  +   "\t" + closeGeo + "\t" + dist  + "\t" + samePlace ;
				return rtn;
		}
		
		if( candFC.matches("CONT") && truthFC.matches("CONTINENT")  ){
			samePlace ="true";
			 rtn = sameCountry  +   "\t" + closeGeo + "\t" + dist  + "\t" + samePlace ;
				return rtn;
		}
		
		
		// admin1 with same cc
		if( candFC.matches("ADM1") && truthFC.matches("CIVIL") &&   sameCountry.matches("true")){
			samePlace ="true";
			 rtn = sameCountry  +   "\t" + closeGeo + "\t" + dist  + "\t" + samePlace ;
				return rtn;
		}
		
		
		if(sameCountry.equalsIgnoreCase("?")   && closeGeo.equalsIgnoreCase("?") && candFC.matches("RGN|AREA") && truthFC.matches("RGN")){
			samePlace ="true";
			 rtn = sameCountry  +   "\t" + closeGeo + "\t" + dist  + "\t" + samePlace ;
				return rtn;
		}

		
		// ambig
		if(sameCountry.equalsIgnoreCase("?")   && closeGeo.equalsIgnoreCase("?") ){
			samePlace ="?";
			 rtn = sameCountry  +   "\t" + closeGeo + "\t" + dist  + "\t" + samePlace ;
				return rtn;
		}
		
		samePlace ="false";
		 rtn = sameCountry  +   "\t" + closeGeo + "\t" + dist  + "\t" + samePlace ;

		return rtn;

	}

	/**
     *
     * @param arg0
     * @throws ExecutionException
     */
    @Override
	public void controllerExecutionStarted(Controller arg0)
			throws ExecutionException {
		initialize();
	}

	
	private void openFiles(boolean dumpAlso){

		if(dumpAlso){
		dumpFile = new File(outputDir, "dump.txt");
		}
		missFile = new File(outputDir, "miss.txt");
		ruleFile = new File(outputDir, "ruleStats.txt");
		vocabFile = new File(outputDir, "vocabStats.txt");

		if(dumpAlso){
		try {
			dumpWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dumpFile), "UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		}
		try {
			missWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(missFile), "UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		try {
			ruleWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ruleFile), "UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		try {
			vocabWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(vocabFile), "UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		// write headers
		try {
			if(dumpAlso){
			dumpWriter.write("HEADER");
			dumpWriter.newLine();
			}
			missWriter.write("HEADER");
			missWriter.newLine();
			ruleWriter.write("HEADER");
			ruleWriter.newLine();
			vocabWriter.write("HEADER");
			vocabWriter.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	
	private void closeFiles(boolean dumpAlso){
		
		// flush and close all the writers
				try {
					dumpWriter.flush();
					ruleWriter.flush();
					vocabWriter.flush();
					missWriter.flush();

					if(dumpAlso){
					dumpWriter.close();
					}
					ruleWriter.close();
					vocabWriter.close();
					missWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		
	}
	
	
	private void writeStats(){
		
		// write out rule stats
		for (String r : ruleStats.keySet()) {
			Integer[] tmp = ruleStats.get(r);

			Double RIGHT = 1.0 * tmp[0];
			Double WRONG = 1.0 * tmp[1];

			Double correctPercent = (RIGHT / (RIGHT + WRONG));

			try {
				ruleWriter.write(r);
				ruleWriter.write("\t");
				ruleWriter.write(RIGHT.toString());
				ruleWriter.write("\t");
				ruleWriter.write(WRONG.toString());
				ruleWriter.write("\t");
				ruleWriter.write(correctPercent.toString());
				ruleWriter.newLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		
		System.out.println("Vocab stats has " + vocabStats.size() +  " entries");
		// write out vocab stats
		for (String word : vocabStats.keySet()) {
			Integer[] tmp = vocabStats.get(word);

			Double TP = 1.0 * tmp[0];
			Double TN = 1.0 * tmp[1];
			Double FP = 1.0 * tmp[2];
			Double FN = 1.0 * tmp[3];

			Double P = (TP / (TP + FP));
			Double R = (TP / (TP + FN));
			Double F = 2 * P * R / (P + R);

			if (P.isNaN()) {
				P = 0.0;
			}

			if (R.isNaN()) {
				R = 0.0;
			}

			if (F.isNaN()) {
				F = 0.0;
			}

			try {
				vocabWriter.write(word);
				vocabWriter.write("\t");
				vocabWriter.write(TP.toString());
				vocabWriter.write("\t");
				vocabWriter.write(TN.toString());
				vocabWriter.write("\t");
				vocabWriter.write(FP.toString());
				vocabWriter.write("\t");
				vocabWriter.write(FN.toString());
				vocabWriter.write("\t");
				vocabWriter.write(P.toString());
				vocabWriter.write("\t");
				vocabWriter.write(R.toString());
				vocabWriter.write("\t");
				vocabWriter.write(F.toString());
				vocabWriter.newLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// write out the miss histogram 2 ways
		// add to vocab and also the miss writer
		for (String m : misses.keySet()) {
			try {
				Integer missCount = misses.get(m);
				vocabWriter.write(m);
				vocabWriter.write("\t");
				vocabWriter.write("0");
				vocabWriter.write("\t");
				vocabWriter.write("0");
				vocabWriter.write("\t");
				vocabWriter.write("0");
				vocabWriter.write("\t");
				vocabWriter.write(missCount.toString());
				vocabWriter.write("\t");
				vocabWriter.write("0.0");
				vocabWriter.write("\t");
				vocabWriter.write("0.0");
				vocabWriter.write("\t");
				vocabWriter.write("0.0");
				vocabWriter.newLine();

				missWriter.write(m);
				missWriter.write("\t");
				missWriter.write(missCount.toString());
				missWriter.newLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			ruleWriter.flush();
			vocabWriter.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	
} // class NaiveTaggerPR
