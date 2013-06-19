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
import gate.FeatureMap;
import gate.Node;
import gate.ProcessingResource;
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ControllerAwarePR;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.RunTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This PR categorizes noun phrases by looking at the vocabulary and other
 * entities that they contain.
 * 
 */
@CreoleResource(name = "OpenSextant Chunk Categorizer", comment = "Categorizes noun phrase chunks by examining the vocabulary they contain")
public class ChunkCategorizerPR extends AbstractLanguageAnalyser implements
		ProcessingResource, ControllerAwarePR {

	private static final long serialVersionUID = 1L;

	// the name of the noun phrase annotation to categorize
	String nounPhraseAnnoName;

	// the feature name which identifies a vocabulary entity
	String vocabFeatureName = "hierarchy";

	// Log object
	private static Logger log = LoggerFactory
			.getLogger(ChunkCategorizerPR.class);

	private void initialize() {
		log.info("Initializing ");
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

		// get all of the noun phrase chunks annotations
		AnnotationSet npSet = document.getAnnotations().get(nounPhraseAnnoName);

		// get all of the vocabulary and simple entity annotations.
		// All vocab and simple entities annotations have a feature named "hierarchy"
		Set<String> featureNameSet = new HashSet<String>();
		featureNameSet.add("hierarchy");
		AnnotationSet vocabSet = document.getAnnotations().get(null,featureNameSet);
		AnnotationSet entitySet = document.getAnnotations().get("Entity");

		// thin out the vocab
		AnnotationSet thinnedVocabSet = thinAnnotations(vocabSet);
		
		// loop over all noun phrases annotations and categorize each one
		for (Annotation a : npSet) {
			categorize(a, thinnedVocabSet,entitySet);

			// TODO DEBUG remove for production
			if (((String) a.getFeatures().get("EntityType")) == null) {
				document.getAnnotations().add(a.getStartNode(), a.getEndNode(),"NOT_CATEGORIZED", a.getFeatures());
			}

		}// end annotation loop

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

	}

	/**
	 * 
	 * @param arg0
	 * @throws ExecutionException
	 */
	@Override
	public void controllerExecutionFinished(Controller arg0)
			throws ExecutionException {

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

	public String getAnnotationName() {
		return nounPhraseAnnoName;
	}

	@RunTime
	@CreoleParameter(defaultValue = "NounPhrase")
	public void setAnnotationName(String annotationName) {
		this.nounPhraseAnnoName = annotationName;
	}

	private void categorize(Annotation np, AnnotationSet vocab, AnnotationSet entities) {

		// get this NPs features
		FeatureMap npFeatures = np.getFeatures();
		Node npStartNode = np.getStartNode();
		Node npEndNode = np.getEndNode();

		// get the head annotation which is a feature of the NP
		AnnotationSet head = (AnnotationSet) npFeatures.get("headSetAnnotation");
		Node headStartNode = head.firstNode();
		Node headEndNode = head.lastNode();

		// get the vocabulary for the whole NP and just the head
		List<Annotation> vocabSetNP = gate.Utils.inDocumentOrder(vocab.get(npStartNode.getOffset(), npEndNode.getOffset()));
		List<Annotation> vocabSetHead = gate.Utils.inDocumentOrder(vocab.get(headStartNode.getOffset(), headEndNode.getOffset()));

		// get the entities for the whole NP and just the head
		//List<Annotation> entitySetNP = gate.Utils.inDocumentOrder(entities.get(npStartNode.getOffset(), npEndNode.getOffset()));
		List<Annotation> entitySetHead = gate.Utils.inDocumentOrder(entities.get(headStartNode.getOffset(), headEndNode.getOffset()));

		// reverse the order of the vocab and entity lists so the right most (head side)
		// comes first
		Collections.reverse(vocabSetNP);
		Collections.reverse(vocabSetHead);
		//Collections.reverse(entitySetNP);
		Collections.reverse(entitySetHead);
		
		

		// two sorted sets to keep the list of types found:
		// hierachy type anywhere in NP
		// hierachy type in HEAD
		List<String> npTypeList = new ArrayList<String>();
		List<String> headTypeList = new ArrayList<String>();

		for (Annotation a : vocabSetNP) {
			String tmp = (String) a.getFeatures().get(vocabFeatureName);
			npTypeList.add(tmp);
		}

		for (Annotation a : vocabSetHead) {
			String tmp = (String) a.getFeatures().get(vocabFeatureName);
			headTypeList.add(tmp);
		}

		// TEMP add lists of vocab seen
		npFeatures.put("VocabSeenPhrase", npTypeList);
		npFeatures.put("VocabSeenHead", headTypeList);

		// now pick the "best" category

		// if there is an entity matching the head
		if(entitySetHead.size() >0){
			npFeatures.put("EntityType", entitySetHead.get(0).getFeatures().get("hierarchy"));
			npFeatures.putAll(entitySetHead.get(0).getFeatures());
			return;
		}
		
		
		if (headTypeList.size() > 0) {
			npFeatures.put("EntityType", headTypeList.get(0));
			npFeatures.putAll(vocabSetHead.get(0).getFeatures());
			return;
		}

		if (npTypeList.size() > 0) {
			npFeatures.put("EntityType", npTypeList.get(0));
			npFeatures.putAll(vocabSetNP.get(0).getFeatures());
			return;
		}

	}

	// thin out the annotation set by removing
	// a) any annotation which is completely within but not identical to another
	private AnnotationSet thinAnnotations(AnnotationSet annoSet) {
		
		List<Annotation> survivorList = new ArrayList<Annotation>();
		survivorList.addAll(annoSet);
		
		for (Annotation currentAnno : annoSet) {

			// get all annotations that "cover" the current.
			AnnotationSet coverSet = gate.Utils.getCoveringAnnotations(annoSet,currentAnno);
			
			for (Annotation a : coverSet) {
				// if the current is smaller than something in the cover set remove if from survivor list
				if ( gate.Utils.length(currentAnno) < gate.Utils.length(a)  ){
					survivorList.remove(currentAnno);
				}
			}
		}
		
		// add all of the survivors to the "Thinned" annotation set
		AnnotationSet thinnedSet = document.getAnnotations("Thinned");
		
		for (Annotation a : survivorList) {
				thinnedSet.add(a);
		}
		
		return thinnedSet;

	}

}
