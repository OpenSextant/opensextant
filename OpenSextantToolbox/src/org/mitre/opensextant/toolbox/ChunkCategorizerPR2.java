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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
	// boolean MarkHead = true;
	// boolean MarkModifierAndHead = true;

	// also create a Entity annotation "flattened model"
	boolean MakeFlatten = true;

	boolean DoCoref = true;

	// co-referencing mapping <word,category>
	private Map<String, String> wordCatMap = new HashMap<String, String>();

	// Log object
	private static Logger log = LoggerFactory.getLogger(ChunkCategorizerPR2.class);

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

		// get the annotation set into which we will place any annotations
		AnnotationSet annotSet = (outputAnnotationSet == null || outputAnnotationSet
				.equals("")) ? document.getAnnotations() : document
				.getAnnotations(outputAnnotationSet);

		// get all of the noun phrase chunks annotations
		AnnotationSet npSet = document.getAnnotations().get(nounPhraseAnnoName);

		// get all of the vocabulary and simple entity annotations.

		// get all of the hierarchically tagged vocab
		Set<String> featureNameSet = new HashSet<String>();
		featureNameSet.add("hierarchy");
		AnnotationSet vocabSet = document.getAnnotations().get(null,featureNameSet);

		// get all of the entities
		AnnotationSet entitySet = document.getAnnotations().get("Entity");

		// get all of the tokens
		AnnotationSet tokenSet = document.getAnnotations().get("Token");

		// categorize all tokens based on the vocab and entities
		categorizeTokens(tokenSet, vocabSet, entitySet);

		// clear out the co-ref mapping
		wordCatMap.clear();

		// do the work
		for (Annotation np : npSet) {
			// attach a category sequence to each noun phrase
			attachCategorySequence(np, tokenSet);
			// categorize the noun phases based on the category sequence
			categorize(np);
			// add the np and category info to co-reference map
			if (DoCoref) {
				addToCorefMap(np);
			}
			// output any entities derived from the noun phrase
			createDerivedEntities(np, annotSet);
		}

		// categorize any noun phrase not handled by above by co-referencing to
		// already categorized noun phrases
		if (DoCoref) {
			for (Annotation np : npSet) {
				coRef(np);
				// output any entities derived from the nounphrase
				createDerivedEntities(np, annotSet);
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

	private void categorizeTokens(AnnotationSet tokenSet,
			AnnotationSet vocabSet, AnnotationSet entitySet) {
		// add a "Category" feature to all tokens, based on part of Speech,
		// vocab and Entities

		// thin out the hierarchical vocab
		String thinnedVocabName = "TEMP_thinnedVocab";
		AnnotationSet thinnedVocabSet = thinAnnotations(vocabSet,
				thinnedVocabName);

		// thin out the basic vocab
		// String thinnedBasicVocabName = "TEMP_thinnedVocab";
		// AnnotationSet thinnedBBSet =
		// thinAnnotations(bbSet,thinnedBasicVocabName);

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
				String tmpCatLabel = (String) vSet.iterator().next().getType();
				String tmpCatHier = (String) vSet.iterator().next()
						.getFeatures().get("hierarchy");
				tmpMap.put("Category", "V." + tmpCatLabel + "/" + tmpCatHier);
			}

			// fourth layer - type from any overlapping Entities
			AnnotationSet eSet = entitySet.get(start, end);
			if (eSet.size() >= 1) {
				String tmpCat = (String) eSet.iterator().next().getFeatures()
						.get("EntityType");
				tmpMap.put("Category", "E." + tmpCat);
			}

		}

		// remove the temporary thinned vocab sets
		// document.removeAnnotationSet(thinnedBasicVocabName);
		document.removeAnnotationSet(thinnedVocabName);
	}

	// attache a CategorySequence,CategorySequence_Reduced and ProperSequence features to
	// NounPhrase
	private void attachCategorySequence(Annotation np, AnnotationSet tokens) {
		Long start = np.getStartNode().getOffset();
		Long end = np.getEndNode().getOffset();
		AnnotationSet tokensInNP = tokens.get(start, end);

		List<Annotation> tokenList = gate.Utils.inDocumentOrder(tokensInNP);
		List<String> categorySequence = new ArrayList<String>();
		List<String> properSequence = new ArrayList<String>();

		String reducedCatSeq = "";
		for (Annotation a : tokenList) {
			String tmpCat = (String) a.getFeatures().get("Category");
			categorySequence.add(tmpCat);
		
			String redCat = tmpCat.split("\\.")[0];

			if (redCat.equals("P")) {
				if (tmpCat.startsWith("P.Proper")) {
					String tmpProper = gate.Utils.cleanStringFor(document, a);
					if(tmpProper.length()>2){
					  properSequence.add(tmpProper);
					}
					reducedCatSeq = reducedCatSeq + "P";
				} else {
					reducedCatSeq = reducedCatSeq + "x";
				}
			} else {
				reducedCatSeq = reducedCatSeq + redCat;
			}
		}

		
		reducedCatSeq = reducedCatSeq.trim();

		np.getFeatures().put("CategorySequence", categorySequence);
		np.getFeatures().put("CategorySequence_Reduced", reducedCatSeq);
		np.getFeatures().put("ProperSequence", properSequence);
	}

	// categorize a nounPhrase based on its category sequence
	// also populate the coref mapping
	private void categorize(Annotation np) {
		List<String> categories = (List<String>) np.getFeatures().get("CategorySequence");
		String reducedCatSeq = (String) np.getFeatures().get("CategorySequence_Reduced");

		String cat = "";
		String type = "";
		String hier = "";
		//String[] categories = catSeq.split(" ");
		int rule = -1;

		// Rule #0 - seq is all Entities and misc = already handled
		if (reducedCatSeq.matches("[Ex]+")) {
			rule = 0;
		} else {

			// Rule #1 - seq ends with vocab -> type = type of Vocab
			if (reducedCatSeq.endsWith("V")) {
				cat = categories.get(categories.size() - 1);
				rule = 1;
			}

			// Rule #2 - seq ends with vocab and 1 Proper -> type = type of
			// Vocab
			if (reducedCatSeq.matches(".*VP$")) {
				cat = categories.get(categories.size() - 2);
				rule = 2;
			}

			// Rule #3 - seq ends with vocab and 2 Propers - type = type of
			// Vocab
			if (reducedCatSeq.matches(".*VPP$")) {
				cat = categories.get(categories.size() - 3);
				rule = 3;
			}

			// Rule #4 - seq ends with vocab and 3 Propers - type = type of
			// Vocab
			if (reducedCatSeq.matches(".*VPPP")) {
				cat = categories.get(categories.size() - 4);
				rule = 4;
			}

			if (cat != null && cat.length() > 0) {
				String[] typePieces = cat.split("/");
				// strip off the leading "V."
				type = typePieces[0].replaceFirst("^V\\.", "");
				hier = typePieces[1];
			}

		}

		np.getFeatures().put("CategorizationRule", rule);
		if (type != null && type.length() > 0) {
			np.getFeatures().put("EntityType", type);
			np.getFeatures().put("hierarchy", hier);
		}

	}

	// derive entities from the categorized nounphrase
	private void createDerivedEntities(Annotation np, AnnotationSet as) {

		String entType = (String) np.getFeatures().get("EntityType");

		if (entType != null && entType.length() > 0) {
			Long start = 0L;
			Long end = 0L;
			String str = "";

			// if we are tagging the whole noun phrase as the entity
			if (MarkPhrase) {
				str = gate.Utils.cleanStringFor(document, np);
				start = np.getStartNode().getOffset();
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
				as.add(start, end, entType, fm);
				// if we are producing the flatten model entities as well
				if (MakeFlatten) {
					as.add(start, end, "Entity", fm);
				}

			} catch (InvalidOffsetException e) {
				log.error("ChunkCategorizerPR: Invalid Offset exception when creating Entity annotation"
						+ e.getMessage());
			}

		}
	}

	// populate the co-referencing map from a categorized noun phrase
	private void addToCorefMap(Annotation np) {
		String tmpType = (String) np.getFeatures().get("EntityType");
		
		if(tmpType ==  null || tmpType.length() <1){
			return;
		}
		
		String tmpHier = (String) np.getFeatures().get("hierarchy");
		
		List<String> propers = (List<String>) np.getFeatures().get("ProperSequence");
		
		for(String wrd : propers){
			if(wrd.length() > 2 && tmpHier.startsWith("Person.name")){
			 wordCatMap.put(wrd.toLowerCase(), tmpType+"/"+tmpHier);
			}
		}
		
	}

	private void coRef(Annotation np) {

		String tmpType = (String) np.getFeatures().get("EntityType");

		// only coref if not already categorized
		if (tmpType != null && tmpType.length() != 0){
			return;
		}

		List<String> propers = (List<String>) np.getFeatures().get("ProperSequence");

		String cat = "";
		String type = "";
		String hier = "";

			// look for a previously tagged word
			for (String wrd : propers) {
				if (wordCatMap.keySet().contains(wrd.toLowerCase())) {
					cat = wordCatMap.get(wrd.toLowerCase());
				}
			}

			// if we have found a previously tagged word, use that category
			if (cat != null && cat.length() > 0) {
				String[] typePieces = cat.split("/");
				// strip off the leading "V."
				type = typePieces[0].replaceFirst("^V\\.", "");
				hier = typePieces[1];

				np.getFeatures().put("CategorizationRule", 5);
				if (type != null && type.length() > 0) {
					np.getFeatures().put("EntityType", type);
					np.getFeatures().put("hierarchy", hier);
				}
			}
		
	}

	// thin out the annotation set by removing any annotation which is
	// completely within but not identical (in length) to another
	private AnnotationSet thinAnnotations(AnnotationSet annoSet, String setName) {

		List<Annotation> survivorList = new ArrayList<Annotation>();
		survivorList.addAll(annoSet);

		for (Annotation currentAnno : annoSet) {

			// get all annotations that "cover" the current.
			AnnotationSet coverSet = gate.Utils.getCoveringAnnotations(annoSet,
					currentAnno);

			for (Annotation a : coverSet) {
				// if the current is smaller than something in the cover set
				// remove it from survivor list
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

	// reduce the part of speech tags to just "Proper" and "x" (don't care)
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
