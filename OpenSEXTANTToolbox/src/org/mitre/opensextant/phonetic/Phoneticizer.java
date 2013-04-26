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

package org.mitre.opensextant.phonetic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringEncoder;
import org.apache.commons.codec.language.Caverphone1;
import org.apache.commons.codec.language.Caverphone2;
import org.apache.commons.codec.language.ColognePhonetic;
import org.apache.commons.codec.language.DoubleMetaphone;
import org.apache.commons.codec.language.Metaphone;
import org.apache.commons.codec.language.RefinedSoundex;
import org.apache.commons.codec.language.Soundex;
import org.apache.commons.codec.language.bm.BeiderMorseEncoder;
import org.apache.commons.codec.language.bm.RuleType;

/**
 * This class provides an interface to various encoders for producing phonetic or reduced forms from words.  
 */
public class Phoneticizer {

	Map<String, StringEncoder> algorithms = new HashMap<String, StringEncoder>();

	public Phoneticizer() {

		// populate the algorithms Map with an instance of each encoder

		// first the ones from Apache Commons
		BeiderMorseEncoder bmExact = new BeiderMorseEncoder();
		bmExact.setRuleType(RuleType.EXACT);
		bmExact.setConcat(false);
		BeiderMorseEncoder bmApprox = new BeiderMorseEncoder();
		bmApprox.setRuleType(RuleType.APPROX);
		bmApprox.setConcat(false);
		// StringEncoder caver = new Caverphone();
		StringEncoder caver1 = new Caverphone1();
		StringEncoder caver2 = new Caverphone2();
		StringEncoder colgne = new ColognePhonetic();
		DoubleMetaphone doubleMeta = new DoubleMetaphone();
		doubleMeta.setMaxCodeLen(10);
		StringEncoder meta = new Metaphone();
		StringEncoder refinedSound = new RefinedSoundex();
		StringEncoder sound = new Soundex();

		// now, the home-brewed ones
		StringEncoder noop = new NullEncoder();
		StringEncoder caser = new CaseEncoder();
		StringEncoder diaRemover = new DiacriticEncoder();
		StringEncoder punctRemover = new PunctEncoder();
		StringEncoder simple0 = new SimplePhonetic0Encoder();
		StringEncoder simple1 = new SimplePhonetic1Encoder();
		StringEncoder simple2 = new SimplePhonetic2Encoder();

		// not really language encodings
		// StringEncoder qcode = new QCodec();
		// StringEncoder qpcode = new QuotedPrintableCodec();
		// StringEncoder urlcode = new URLCodec();

		algorithms.put("Beider-Morse-Exact", bmExact);
		algorithms.put("Beider-Morse-Approximate", bmApprox);
		// algorithms.put("CaverPhone", caver);
		algorithms.put("CaverPhone_1.0", caver1);
		algorithms.put("CaverPhone_2.0", caver2);
		algorithms.put("Cologne_Phonetic", colgne);
		algorithms.put("Double_Metaphone", doubleMeta);
		algorithms.put("Metaphone", meta);
		algorithms.put("Refined_Soundex", refinedSound);
		algorithms.put("Soundex", sound);

		algorithms.put("Nothing", noop);
		algorithms.put("Case_Insensitive", caser);
		algorithms.put("Diacritic_Insensitive", diaRemover);
		algorithms.put("Puncuation_Insensitive", punctRemover);
		algorithms.put("Simple_Phonetic0", simple0);
		algorithms.put("Simple_Phonetic1", simple1);
		algorithms.put("Simple_Phonetic2", simple2);

		// not really language encodings
		// algorithms.put("Q Code", qcode);
		// algorithms.put("Q Printable", qpcode);
		// algorithms.put("URL Code", urlcode);

	}

	public List<String> supportedAlgorithms() {
		List<String> tmpList = new ArrayList<String>(algorithms.keySet());

		Collections.sort(tmpList);
		return tmpList;
	}

	/**
	 * Invoke an encoder and return the results.
	 * @param word The word to encode.
	 * @param method A key for selecting the desired encoder.
	 * @return The output from the encoder.
	 */
	public String phoneticForm(String word, String method) {

		String tmpMeth = method;
		// if unknown method requested, force to default rather then failing
		if (!(this.supportedAlgorithms().contains(tmpMeth))) {
			tmpMeth = "Simple_Phonetic0";
			// need to log something here to identify parameter change
		}

		
		String tmpResult = "";

		try {
			tmpResult = algorithms.get(tmpMeth).encode(word);
		} catch (EncoderException e) {
			e.printStackTrace();
		}

		return tmpResult;
	}

	// for testing
	public static void main(String[] args) throws Exception {

		Phoneticizer phoner = new Phoneticizer();

		List<String> algos = phoner.supportedAlgorithms();

		System.out.println("Supported Algorithms are:");
		for (String alg : algos) {
			System.out.println("\t" + alg);
		}
		System.out.println();

		//String testWord1 = "C.U.A.";
		//String testWord2 = "C. U. A. ";
		//String testWord3 = "C . U . A . ";
		String testWord4 = "'Wombat";
		
		System.out.println("Test word:" + testWord4);
		for (String alg : algos) {
			String tmp = phoner.phoneticForm(testWord4, alg);
			System.out.println(tmp + "\t" + alg);
		}

	}

}
