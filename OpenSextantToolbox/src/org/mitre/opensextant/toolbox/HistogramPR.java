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
import gate.ProcessingResource;
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ControllerAwarePR;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.RunTime;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This PR is used as a diagnostic tool. It collects a histogram (word counts) of the specified annotation type
 * 
 */
@CreoleResource(name = "OpenSextant HistogramPR", comment = "Diagnostic tool for collecting a histogram based on selected annotations")
public class HistogramPR extends AbstractLanguageAnalyser implements
		ProcessingResource, ControllerAwarePR {

	private static final long serialVersionUID = 1L;

	private File outputDir = null;
	private String outFileName = "vocabStats.txt";
	private File vocabFile = null;
	BufferedWriter vocabWriter = null;
	Map<String, Long> vocabStats = new HashMap<String, Long>();

	private Integer docCount = 0;

	String annotationName;
	String featureName;
	Boolean convertToLower;

	// Log object
	private static Logger log = LoggerFactory.getLogger(HistogramPR.class);

	private void initialize() {
		log.info("Initializing ");
		docCount = 0;
		openFiles();
		vocabStats.clear();
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
		
		AnnotationSet annoSet = document.getAnnotations().get(annotationName);
		
		// if no explicit feature name is given, use word/phrase as found in document
		Boolean explicitFeatureName = true;
		if(featureName == null || featureName.equalsIgnoreCase("")){
			explicitFeatureName = false;
		}
		

		docCount++;
		System.out.println("(" + docCount + ") This document has " + annoSet.size() + " " + annotationName + " annotations");

		// loop over all selected annotations
		for (Annotation a : annoSet) {
			
			String feat = "";
			
			if(explicitFeatureName){
				Object tmp = a.getFeatures().get(featureName);
				if(tmp != null){
					feat = tmp.toString();
				}
			 
			}else{
				feat = gate.Utils.cleanStringFor(document, a);
			}
			
			
			if (convertToLower) {
				feat = feat.toLowerCase();
			}

			// if not previously seen, add entry to Map
			if (!vocabStats.containsKey(feat)) {
				vocabStats.put(feat, 0L);
			}
			// increment stats
			vocabStats.put(feat, vocabStats.get(feat) + 1);
		}// end annottaion loop

		// write out interim stats files
		if (docCount % 500 == 0) {
			System.out.println("Writing incremental stats at " + docCount + " documents");
			closeFiles();
			openFiles();
			writeStats();

		}

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
		openFiles();
		writeStats();
		vocabStats.clear();
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
		openFiles();
		writeStats();
		vocabStats.clear();
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
			vocabWriter.write("word\tcount");
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

	private void writeStats() {

		System.out.println("Vocab stats has " + vocabStats.size() + " entries");
		// write out vocab stats
		for (String word : vocabStats.keySet()) {
			Long count = vocabStats.get(word);
			try {
				vocabWriter.write(word);
				vocabWriter.write("\t");
				vocabWriter.write(count.toString());
				vocabWriter.newLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			vocabWriter.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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

	@CreoleParameter(defaultValue = "vocabStats.txt")
	public void setOutFileName(String outFileName) {
		this.outFileName = outFileName;
	}
	
	public String getAnnotationName() {
		return annotationName;
	}

    @RunTime
	@CreoleParameter(defaultValue = "Token")
	public void setAnnotationName(String annotationName) {
		this.annotationName = annotationName;
	}

	public String getFeatureName() {
		return featureName;
	}

    @RunTime
	@CreoleParameter(defaultValue = "string")
	public void setFeatureName(String featureName) {
		this.featureName = featureName;
	}

	public Boolean getLower() {
		return convertToLower;
	}

    @RunTime
	@CreoleParameter(defaultValue = "true")
	public void setLower(Boolean lower) {
		this.convertToLower = lower;
	}

}
