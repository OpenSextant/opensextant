/*
 * POSTagger.java
 * 
 */
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
import gate.util.OffsetComparator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.langkit.tagger.data.Model;
import org.langkit.tagger.languagemodel.LanguageModel;
import org.langkit.tagger.languagemodel.LinearInterpolationLM;
import org.langkit.tagger.tagger.HMMTagger;
import org.langkit.tagger.tagger.HMMTagger.Sequence;
import org.langkit.tagger.wordhandler.KnownWordHandler;
import org.langkit.tagger.wordhandler.SuffixWordHandler;
import org.langkit.tagger.wordhandler.WordHandler;

/**
 */
@CreoleResource(name = "OpenSextant_POS_Tagger", comment = "A POS tagger based on the JITAR implementation of a ngram model")
public class POSTagger extends AbstractLanguageAnalyser implements ProcessingResource {
	
	private String inputASName;
	private String outputASName;

	// the lexicon and ngrams used by the tagger
	private URL lexiconFileURL;
	private URL ngramFileURL;

	// the lexicon and ngrams used by the gueser algoithm (suffix handler)
	private URL guesserLexiconFileURL;
	private URL guesserNgramFileURL;

	private HMMTagger tagger;

	private Model model = null;
	private Model guesserModel = null;

	private void initialize() {
		// Load the model
		try {
			model = Model.readModel(new BufferedReader(new InputStreamReader(
					lexiconFileURL.openStream())), new BufferedReader(
					new InputStreamReader(ngramFileURL.openStream())));

			guesserModel = Model.readModel(new BufferedReader(
					new InputStreamReader(guesserLexiconFileURL.openStream())),
					new BufferedReader(new InputStreamReader(
							guesserNgramFileURL.openStream())));

		} catch (IOException e) {
			System.err.println("Unable to read the model!");
			e.printStackTrace();
		}

		// Set up word handlers. The suffix word handler is used as a fallback
		// to the known word handler.
		int maxSuffixLength = 3; // low long a suffix to use to guess unknown
									// words
		int maxTrainFreqNum = 5; // max freq for numerical words
		int maxTrainFreqUppercase = 100000000; // max freq for uppercase words
		int maxTrainFreqLowercase = 100000000; // max freq for lower case words
		SuffixWordHandler swh = new SuffixWordHandler(guesserModel.lexicon(),
				model.uniGrams(), maxSuffixLength, maxTrainFreqNum,
				maxTrainFreqUppercase, maxTrainFreqLowercase, 10);

		WordHandler wh = new KnownWordHandler(model.lexicon(),
				model.uniGrams(), swh);
		// Create an n-gram language model.
		LanguageModel lm = new LinearInterpolationLM(model.uniGrams(),
				model.biGrams(), model.triGrams());
		// Initialize a tagger with a beam of 1000.0.
		tagger = new HMMTagger(model, wh, lm, 1000.0);

	}

	@Override
	public Resource init() throws ResourceInstantiationException {
		initialize();
		return this;
	}

	@Override
	public void reInit() throws ResourceInstantiationException {
		initialize();
	}

	@Override
	public void execute() throws ExecutionException {
		if (inputASName != null && inputASName.equals(""))
			inputASName = null;
		AnnotationSet inputAS = (inputASName == null) ? document
				.getAnnotations() : document.getAnnotations(inputASName);

		// Get all of the sentences in document
		AnnotationSet sentenceSet = inputAS.get("Sentence");

		// For every sentence:
		// Get the tokens which make up that sentence,
		// first as Annotation[], then as a List<String>
		Iterator<Annotation> sentIter = sentenceSet.iterator();
		while (sentIter.hasNext()) {
			Annotation currSent = sentIter.next();
			Long start = currSent.getStartNode().getOffset();
			Long end = currSent.getEndNode().getOffset();

			// Get the Tokens within the current sentence
			AnnotationSet tokensInSentence = inputAS.get("Token", start, end);
			List<Annotation> AnnoList = gate.Utils.inDocumentOrder(tokensInSentence);
			
			List<String> tokenList = new ArrayList<String>();
			for (int i = 0; i < AnnoList.size(); i++) {
				// ASSUMPTION: every token has a feature "string"
				String tmpString = (String) AnnoList.get(i).getFeatures().get("string");
				tokenList.add(tmpString);
			}
			// Add start/end markers, 2 starts and 1 end.
			tokenList.add(0, "<START>");
			tokenList.add(0, "<START>");
			tokenList.add("<END>");
			int indexOffset = 2;
			String featureName = "pos";
			// Send the tokens to the tagger
			Sequence seq = HMMTagger.highestProbabilitySequence(tagger.viterbi(tokenList), model);
			// Set the probability on the Sentence as feature "posProb"
			currSent.getFeatures().put("posProb", seq.logProb());
			// get the tags from the sequence
			List<String> tags = seq.sequence();
			// Attach the returned tag to each token as feature "pos"
			// skipping the 2 <START> and 1 <END> tags
			for (int i = 0; i < AnnoList.size(); i++) {
				AnnoList.get(i).getFeatures().put(featureName, tags.get(i + indexOffset));
			}
		}// end sentence iterator
	}// end execute()

	@Override
	public void cleanup() {
	}

	public URL getLexiconFileURL() {
		return lexiconFileURL;
	}

	@CreoleParameter
	public void setLexiconFileURL(URL lexiconFileURL) {
		this.lexiconFileURL = lexiconFileURL;
	}

	public URL getNgramFileURL() {
		return ngramFileURL;
	}

	@CreoleParameter
	public void setNgramFileURL(URL ngramFileURL) {
		this.ngramFileURL = ngramFileURL;
	}

	public URL getGuesserLexiconFileURL() {
		return guesserLexiconFileURL;
	}

	@CreoleParameter
	public void setGuesserLexiconFileURL(URL guesserLexiconFileURL) {
		this.guesserLexiconFileURL = guesserLexiconFileURL;
	}

	public URL getGuesserNgramFileURL() {
		return guesserNgramFileURL;
	}

	@CreoleParameter
	public void setGuesserNgramFileURL(URL guesserNgramFileURL) {
		this.guesserNgramFileURL = guesserNgramFileURL;
	}

	public String getInputASName() {
		return inputASName;
	}

	@Optional
	@RunTime
	@CreoleParameter
	public void setInputASName(String inputASName) {
		this.inputASName = inputASName;
	}

	public String getOutputASName() {
		return outputASName;
	}

	@Optional
	@RunTime
	@CreoleParameter
	public void setOutputASName(String outputASName) {
		this.outputASName = outputASName;
	}
} // class POSTagger
