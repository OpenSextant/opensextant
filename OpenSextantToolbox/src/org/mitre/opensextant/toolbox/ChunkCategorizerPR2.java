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
import gate.ProcessingResource;
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ControllerAwarePR;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.RunTime;
import gate.util.InvalidOffsetException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This PR categorizes noun phrases by looking at the vocabulary and other
 * entities that they contain.
 * 
 */
@CreoleResource(name = "OpenSextant Sequence Abstractor", comment = "Categorizes Annotations by examining the vocabulary and entities they contain")
public class ChunkCategorizerPR2 extends AbstractLanguageAnalyser implements
		ProcessingResource, ControllerAwarePR {

	private static final long serialVersionUID = 1L;

	
	// the annotationSet into which the created annotations will be written
	private String outputAnnotationSet;
	
	// the name of the noun phrase annotation to categorize
	String nounPhraseAnnoName;

	// the feature name which identifies a vocabulary entity
	String vocabFeatureName = "hierarchy";

	// What portion of the NounPhrasew should be tagged as a derived entity?
	boolean MarkPhrase = true;
	//boolean MarkHead = true;
	//boolean MarkModifierAndHead = true;
	
	// also create a Entity annotation "flattened model" 
	boolean MakeFlatten = true;
	
	// Log object
	private static Logger log = LoggerFactory
			.getLogger(ChunkCategorizerPR2.class);

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

		// get the annotation set into which we will place any annotations found
		AnnotationSet annotSet = (outputAnnotationSet == null || outputAnnotationSet
				.equals("")) ? document.getAnnotations() : document
				.getAnnotations(outputAnnotationSet);
		
		
		
		// get all of the noun phrase chunks annotations
		AnnotationSet npSet = document.getAnnotations().get(nounPhraseAnnoName);

		// get all of the vocabulary and simple entity annotations.
		// All vocab and simple entities annotations have a feature named
		// "hierarchy"

		// get all of the vocabs from the Lookup annotations
		// AnnotationSet bbSet = document.getAnnotations().get("Lookup");

		// get all of the hierarchically tagged vocab (anything with a
		// "hierarchy" feature)
		Set<String> featureNameSet = new HashSet<String>();
		featureNameSet.add("hierarchy");
		AnnotationSet vocabSet = document.getAnnotations().get(null,
				featureNameSet);

		// get all of the entities
		AnnotationSet entitySet = document.getAnnotations().get("Entity");

		// get all of the tokens
		AnnotationSet tokenSet = document.getAnnotations().get("Token");

		// thin out the basic vocab
		// AnnotationSet thinnedBBSet = thinAnnotations(bbSet,"thinnedBB");

		// thin out the hierarchical vocab
		AnnotationSet thinnedVocabSet = thinAnnotations(vocabSet,
				"thinnedVocab");

		// add a "Category" feature to all tokens, based on part of Speech,
		// vocab and Entities
		for (Annotation a : tokenSet) {
			Long start = a.getStartNode().getOffset();
			Long end = a.getEndNode().getOffset();

			FeatureMap tmpMap = a.getFeatures();

			// first layer - Part of Speech already on Token
			tmpMap.put("Category",
					"P." + reducePOSTags((String) tmpMap.get("pos")));

			// second layer - building block vocab (i.e. doesn't have a
			// hierarchical feature)
			// AnnotationSet bSet = thinnedBBSet.get(start, end);
			// if (bSet.size() >= 1) {
			// String tmpCat = (String)
			// bSet.iterator().next().getFeatures().get("majorType");
			// tmpMap.put("Category", tmpCat);
			// }

			// third layer - type from any overlapping Vocab
			AnnotationSet vSet = thinnedVocabSet.get(start, end);
			if (vSet.size() >= 1) {
				String tmpCatLabel = (String) vSet.iterator().next().getType(); // .getFeatures().get("hierarchy");
				String tmpCatHier = (String) vSet.iterator().next()
						.getFeatures().get("hierarchy");
				tmpMap.put("Category", "V." + tmpCatLabel + "/" + tmpCatHier);
				// tmpMap.put("Category", "V."+tmpCatLabel);
			}

			// fourth layer - type from any overlapping Entities
			AnnotationSet eSet = entitySet.get(start, end);
			if (eSet.size() >= 1) {
				String tmpCat = (String) eSet.iterator().next().getFeatures()
						.get("EntityType"); // .getFeatures().get("hierarchy");
				tmpMap.put("Category", "E." + tmpCat);
			}

		}

		// now all tokens have the most specific "Category" feature

		// loop over all noun phrases annotations and attach a
		// "CategorySequence" feature to each one from the tokens it covers
		for (Annotation np : npSet) {
			attachCategorySequence(np, tokenSet);
		}
		
		// loop over all noun phrases annotations and create the appropriate derived entities
		for (Annotation np : npSet) {
			String entType =(String) np.getFeatures().get("EntityType");
			
			if(entType != null && entType.length() >0){
				Long start =  0L;
				Long end = 0L;
				String str = "";
				
				if(MarkPhrase){
					str = gate.Utils.cleanStringFor(document, np);
					start =  np.getStartNode().getOffset();
					end = np.getEndNode().getOffset();
				}
				
				// if(markHead){ }
				// if(markModifierAndHead){ }
				
				
				
				String hier = (String) np.getFeatures().get("hierarchy");
				
				FeatureMap fm = gate.Factory.newFeatureMap();
				fm.put("string", str);
				fm.put("hierarchy", hier);
				fm.put("EntityType", entType);
				
				try {
					annotSet.add(start, end, entType, fm);
					if(MakeFlatten){
						annotSet.add(start, end, "Entity", fm);
					}
					
				} catch (InvalidOffsetException e) {
					log.error("ChunkCategorizerPR: Invalid Offset exception when creating Entity annotation" + e.getMessage());
				}
				
				
			}
			
			
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

	private void attachCategorySequence(Annotation np, AnnotationSet tokens) {
		Long start = np.getStartNode().getOffset();
		Long end = np.getEndNode().getOffset();
		AnnotationSet catSet = tokens.get(start, end);

		List<Annotation> tmpList = gate.Utils.inDocumentOrder(catSet);

		String catSeq = "";
		String reducedCatSeq = "";
		for (Annotation a : tmpList) {
			String tmp = (String) a.getFeatures().get("Category");
			catSeq = catSeq + " " + tmp;

			String redCat = tmp.split("\\.")[0];
			
			if (redCat.equals("P")) {
				if (tmp.startsWith("P.Proper")) {
					reducedCatSeq = reducedCatSeq + "P";
				} else {
					reducedCatSeq = reducedCatSeq + "x";
				}
			}else{
				reducedCatSeq = reducedCatSeq + redCat;
			}

		}

		catSeq = catSeq.trim();
		reducedCatSeq = reducedCatSeq.trim();

		// make decision based on sequences

		String cat = "";
		String type = "";
		String hier = "";
		String[] categories = catSeq.split(" ");
		int rule = -1;

		// Rule #0 - seq is all Entities and misc
		if (reducedCatSeq.matches("[Ex]+")) {
			rule = 0;
		} else {

			// Rule #1 - seq ends with vocab - type = type of Vocab
			if (reducedCatSeq.endsWith("V")) {
				cat = categories[categories.length - 1];
				String[] typePieces = cat.split("/");
				type = typePieces[0];
				hier = typePieces[1];
				rule = 1;
			}

			// Rule #2 - seq ends with vocab and 1 Propers - type = type of
			// Vocab
			if (reducedCatSeq.matches(".*VP")) {
				cat = categories[categories.length - 2];
				String[] typePieces = cat.split("/");
				type = typePieces[0];
				hier = typePieces[1];
				rule = 2;
			}

			// Rule #3 - seq ends with vocab and 2 Propers - type = type of
			// Vocab
			if (reducedCatSeq.matches(".*VPP")) {
				cat = categories[categories.length - 3];
				String[] typePieces = cat.split("/");
				type = typePieces[0];
				hier = typePieces[1];
				rule = 3;
			}

			// Rule #4 - seq ends with vocab and 3 Propers - type = type of
			// Vocab
			if (reducedCatSeq.matches(".*VPPP")) {
				cat = categories[categories.length - 4];
				String[] typePieces = cat.split("/");
				type = typePieces[0];
				hier = typePieces[1];
				rule = 4;
			}

		}

		np.getFeatures().put("CategorizationRule", rule);
		if (type != null && type.length() > 0) {
			// strip off the leading V.
			type =  type.replaceFirst("^V\\.", "");
			np.getFeatures().put("EntityType", type);
			np.getFeatures().put("hierarchy", hier);
		}

		np.getFeatures().put("CategorySequence", catSeq);
		np.getFeatures().put("CategorySequence_Reduced", reducedCatSeq);
	}

	// thin out the annotation set by removing any annotation which is
	// completely
	// within but not identical to another
	private AnnotationSet thinAnnotations(AnnotationSet annoSet, String setName) {

		List<Annotation> survivorList = new ArrayList<Annotation>();
		survivorList.addAll(annoSet);

		for (Annotation currentAnno : annoSet) {

			// get all annotations that "cover" the current.
			AnnotationSet coverSet = gate.Utils.getCoveringAnnotations(annoSet,
					currentAnno);

			for (Annotation a : coverSet) {
				// if the current is smaller than something in the cover set
				// remove if from survivor list
				if (gate.Utils.length(currentAnno) < gate.Utils.length(a)) {
					survivorList.remove(currentAnno);
				}
			}
		}

		// add all of the survivors to the "Thinned" annotation set
		AnnotationSet thinnedSet = document.getAnnotations(setName);

		for (Annotation a : survivorList) {
			thinnedSet.add(a);
		}

		return thinnedSet;

	}

	private String reducePOSTags(String tag) {
		if (tag == null) {
			return "x";
		}

		if (tag.matches("NP.*")) {
			return "Proper";
		}

		return "x";
	}

}
