/*
                  NOTICE
This software was produced for the U. S. Government
under Contract No. W15P7T-11-C-F600, and is
subject to the Rights in Noncommercial Computer Software
and Noncommercial Computer Software Documentation
Clause 252.227-7014 (JUN 1995)

Copyright 2010 The MITRE Corporation. All Rights Reserved.
 */

package org.mitre.opensextant.toolbox;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Controller;
import gate.Document;
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
import java.util.List;

import org.mitre.opensextant.placedata.PlaceCandidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This PR is used as a diagnostic tool. It collects a histogram (word counts)
 * of how many times a specific candidate place name was determined to be a
 * Place, not a place or no opinion.
 * 
 */
@CreoleResource(name = "OpenSextant PlaceNameRuleDumpPR", comment = "Diagnostic tool for dumping info on PlaceCandidate annotations")
public class PlaceNameRuleDumpPR extends AbstractLanguageAnalyser implements
		ProcessingResource, ControllerAwarePR {

	private static final long serialVersionUID = 1L;

	private File outputDir = null;
	private String outFileName = "placeNameStatsWithRules.txt";
	private File vocabFile = null;
	BufferedWriter vocabWriter = null;

	// a running count of how many documents seen so far
	private Integer docCount = 0;

	String placeAnnotationName = "placecandidate";
	String featureName = "placeCandidate";

	Long contxtSize = 75L;
	
	
	// Log object
	private static Logger log = LoggerFactory
			.getLogger(PlaceNameRuleDumpPR.class);

	private void initialize() {
		log.info("Initializing ");
		docCount = 0;
		openFiles();
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

		// get all of the annotations of interest
		AnnotationSet placeCandAnnoSet = document.getAnnotations().get(this.placeAnnotationName);

		docCount++;
		log.info("(" + docCount + ") " + document.getName() + " has "
				+ placeCandAnnoSet.size() + " " + placeAnnotationName
				+ " annotations");

		// loop over all placeCandidate annotations
		for (Annotation anno : placeCandAnnoSet) {
			writeStats(anno);
		}// end placeCandidate annotation loop

	}// end execute

	/**
	 * 
	 * @param arg0
	 * @param arg1
	 * @throws ExecutionException
	 */
	@Override
	public void controllerExecutionAborted(Controller arg0, Throwable arg1)
			throws ExecutionException {
		closeFiles();
	}

	/**
	 * 
	 * @param arg0
	 * @throws ExecutionException
	 */
	@Override
	public void controllerExecutionFinished(Controller arg0)
			throws ExecutionException {
		closeFiles();
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

	private void openFiles() {

		vocabFile = new File(outputDir, this.outFileName);

		try {
			vocabWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(vocabFile), "UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		// write header
		try {
			vocabWriter
					.write("placeName\tStart\tEnd\tConfidence\tRules\tRuleWeights\tContext\tDocument");
			vocabWriter.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void closeFiles() {

		// flush and close all the writers
		try {
			vocabWriter.flush();
			vocabWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void writeStats(Annotation anno) {
		
		// get the PlaceCandidate obj
		PlaceCandidate pc = (PlaceCandidate) anno.getFeatures().get(featureName);

		// get the name as found in the document
		String placename = gate.Utils.cleanStringFor(document, anno);

		// get the confidence score
		Double score = pc.getPlaceConfidenceScore();
		// get the rules
		List<String> rules = pc.getRules();
		// get the confidences
		List<Double> scores = pc.getConfidences();

		Long start = pc.getStart();
		Long end = pc.getEnd();
		
		String context = getContext(anno);

		try {
			vocabWriter.write(placename);
			vocabWriter.write("\t");
			vocabWriter.write(start.toString());
			vocabWriter.write("\t");
			vocabWriter.write(end.toString());
			vocabWriter.write("\t");
			vocabWriter.write(score.toString());
			vocabWriter.write("\t");
			vocabWriter.write(rules.toString());
			vocabWriter.write("\t");
			vocabWriter.write(scores.toString());
			vocabWriter.write("\t");
			vocabWriter.write(context);	
			vocabWriter.write("\t");
			vocabWriter.write(document.getName());	
			
			vocabWriter.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			vocabWriter.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
	private String getContext(Annotation anno){

		Long candStart = anno.getStartNode().getOffset();
		Long candEnd = anno.getEndNode().getOffset();

		Long contextStart = candStart - contxtSize;
		Long contextEnd = candEnd + contxtSize;

		if (contextStart < 0) {
			contextStart = 0L;
		}

		if (contextEnd > Utils.lengthLong(document)) {
			contextEnd = Utils.lengthLong(document);
		}

		String context = Utils.cleanStringFor(document, contextStart,contextEnd);

		return context;
	}
	
	
	
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
	@CreoleParameter(defaultValue = "C:\\dump\\vocab")
	public void setOutputDir(File outputDir) {
		outputDir.mkdirs();
		this.outputDir = outputDir;
	}

	public String getOutFileName() {
		return outFileName;
	}

	@CreoleParameter(defaultValue = "placeNameStatsWithRules.txt")
	public void setOutFileName(String outFileName) {
		this.outFileName = outFileName;
	}

} // class NaiveTaggerPR
