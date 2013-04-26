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

import java.lang.Character.UnicodeBlock;
import java.text.Normalizer;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/** 
 * Provides utility functions for String encoders.
 * 
 */
//history
//+ NOV 2012  -- heavily optimized REGEX patterns and StringBuilder.

public class PhoneticUtils {

    /**
     * Reduce word to lower case.
     */
    public static String removeCase(String word) {
        return word.toLowerCase();
    }
    
    /** Intended only as a filter for punctuation within a word.
     *  Text of the form A.T.T. or U.S. becomes ATT and US.
     *  A text such as Mr.Pibbs incorrectly becomes MrPibbs but
     * for the purposes of normalizing tokens this should be fine.
     * Use appropriate tokenization prior to using this as a filter.
     */
    public static String normalizeAbbreviation(String word){
        return word.replace(".", "");
    }

    /**
     * Remove diacritics from word.
     */
    public static String removeDiacritics(String word) {

        // first, fully decomposed all chars
        String tmpWord = Normalizer.normalize(word, Normalizer.Form.NFD);
        StringBuilder newWord = new StringBuilder();
        char[] chars = tmpWord.toCharArray();
        // now, discard any characters from one of the "Mark" categories.
        for (char c : chars) {
            if (Character.getType(c) != Character.NON_SPACING_MARK
                    && Character.getType(c) != Character.COMBINING_SPACING_MARK
                    && Character.getType(c) != Character.ENCLOSING_MARK) {
                newWord.append(c);
            }
        }
        return newWord.toString();
    }
    final static Pattern CLEAN_WORD_RIGHT = Pattern.compile("[^\\p{L}\\p{Nd}]+$");
    final static Pattern CLEAN_WORD_LEFT = Pattern.compile("^[^\\p{L}\\p{Nd}]+");
    // TODO:  convert bytes here to \\u patterns
    final static Pattern CLEAN_WORD_PUNCT = Pattern.compile("[\"'.`\\u00B4\\u2018\\u2019]");

    /**
     * Remove any leading and trailing punctuation and some internal punctuation.
     * Internal punctuation which indicates conjunction of two tokens, e.g. a hyphen,
     * should have caused a split into separate tokens at the tokenization stage.
     */
    public static String removePunctuation(String word) {
        // replace anything that is not a letter or digit, at the the start or end, with a space, then trim.
        //String tmp = word.replaceAll("^[^\\p{L}\\p{Nd}]+", " ").replaceAll("[^\\p{L}\\p{Nd}]+$", " ").trim();

        String tmp = CLEAN_WORD_LEFT.matcher(word).replaceAll(" ");
        tmp = CLEAN_WORD_RIGHT.matcher(tmp).replaceAll(" ");

        //remove some internal punctuation. To be removed: char hex unicode_name
        //	"	22	QUOTATION MARK
        //	'	27	APOSTROPHE
        //	.	2e	FULL STOP
        //	`	60	GRAVE ACCENT
        //	�	b4	ACUTE ACCENT
        //	�	2018	LEFT SINGLE QUOTATION MARK
        //	�	2019	RIGHT SINGLE QUOTATION MARK
        return CLEAN_WORD_PUNCT.matcher(tmp).replaceAll("").trim();
    }

    // what "scripts" are used in a string? 
	public static Set<String> scripts(String word){
		
		String tmpWord =  word.replaceAll("\\P{L}","");
		Set<String> scriptsSeen = new TreeSet<String>();
		
		char[] chars = tmpWord.toCharArray();
        for (char c : chars) {
        	scriptsSeen.add(script(c));
        }
		
        return scriptsSeen;
	}
    
	// simple substitute for "script" based on UniCode block
	// TODO replace with Character.UnicodeScript when we move to Java 7
	public static String script(char c) {

		UnicodeBlock blk = Character.UnicodeBlock.of(c);
		
		if(blk == null){
			return "NONE";
		}
		
		String blockName = blk.toString();
		
		if (!blockName.contains("_")) {
			return blockName;
		}

		if (blockName.contains("CJK")) {
			return "CJK";
		}

		if (blockName.equals("BASIC_LATIN")) {
			return "LATIN";
		}

		if (blockName.contains("_SYLLAB")) {
			String[] pieces = blockName.split("_SYLLAB");
			return pieces[0];
		}

		if (blockName.contains("_IDEOGRAMS")) {
			String[] pieces = blockName.split("_IDEOGRAMS");
			return pieces[0];
		}

		if (blockName.contains("MARKS")) {
			return "MARKS";
		}

		if (blockName.contains("MATHEMATICAL")) {
			return "MATH";
		}

		if (blockName.contains("PRIVATE")) {
			return "PRIVATE";
		}

		if (blockName.contains("SURROGATES")) {
			return "SURROGATES";
		}

		if (blockName.contains("BLOCK") 
				|| blockName.contains("BOX")
				|| blockName.contains("MUSICAL")
				|| blockName.contains("LETTERLIKE")
				|| blockName.contains("PICTURES")) {
			return "SYMBOL";
		}

		if (blockName.contains("NUMBER")) {
			return "NUMBER";
		}

		if (blockName.contains("OLD_ITALIC")) {
			return "OLD_ITALIC";
		}

		if (blockName.contains("TAI_LE")) {
			return "TAI_LE";
		}

		if (blockName.contains("TAI_XUAN_JING_SYMBOLS")) {
			return "TAI_XUAN_JING";
		}

		// anything left take the first piece
		String[] pieces = blockName.split("_");

		return pieces[0];

	}
    
    
    
    
    /**
     * Perform a simple test.
     */
    public static void main(String[] args) throws Exception {


        String[] testWords = {
            "Sa‘īd",
            "'Wombat",
            "Wombat'",
            "***[Image Bar",
            "99 luft Balloons...",
            "St. John U.S.S.R.",
            "Jose-Enrique",
            "Al 'farqu Ha'na",
            "Bélow",
            "Colón",
            "Ås",
            "Bäck",
            "Ön",
            "Tōp",
            "Nõmme",
            "Çat",
            "Çavuş",
            "Góry",
            "Onça",
            "Röd",
            "Armonía",
            "Poço",
            "Būm",
            "Våge"};

        System.out.println("WORD\tNO PUNCT\tNO ACCENTS\tNO CASE");
        
        for (String wd : testWords) {
            System.out.println(wd + " ->" + removePunctuation(wd) + "\t" + removeDiacritics(wd) + "\t" + removeCase(wd));
        }

    }
}
