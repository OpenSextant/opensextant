/**
 *
 * Copyright 2009-2013 The MITRE Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * **************************************************************************
 * NOTICE This software was produced for the U. S. Government under Contract No.
 * W15P7T-12-C-F600, and is subject to the Rights in Noncommercial Computer
 * Software and Noncommercial Computer Software Documentation Clause
 * 252.227-7014 (JUN 1995)
 *
 * (c) 2012 The MITRE Corporation. All Rights Reserved.
 * **************************************************************************
 */
///** ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|
//
// _____                                ____                     __                       __
///\  __`\                             /\  _`\                  /\ \__                   /\ \__
//\ \ \/\ \   _____      __     ___    \ \,\L\_\      __   __  _\ \ ,_\     __       ___ \ \ ,_\
// \ \ \ \ \ /\ '__`\  /'__`\ /' _ `\   \/_\__ \    /'__`\/\ \/'\\ \ \/   /'__`\   /' _ `\\ \ \/
//  \ \ \_\ \\ \ \L\ \/\  __/ /\ \/\ \    /\ \L\ \ /\  __/\/>  </ \ \ \_ /\ \L\.\_ /\ \/\ \\ \ \_
//   \ \_____\\ \ ,__/\ \____\\ \_\ \_\   \ `\____\\ \____\/\_/\_\ \ \__\\ \__/.\_\\ \_\ \_\\ \__\
//    \/_____/ \ \ \/  \/____/ \/_/\/_/    \/_____/ \/____/\//\/_/  \/__/ \/__/\/_/ \/_/\/_/ \/__/
//            \ \_\
//             \/_/
//
//   OpenSextant Commons
// *  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|
// */
package org.mitre.opensextant.util;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.*;
import java.util.Locale;
import org.apache.commons.lang.StringUtils;
import org.mitre.opensextant.data.Language;

/**
 *
 * @author ubaldino
 */
public class TextUtils {

    /**
     * @threadsafe False; use TextUtils() instance instead.
     *
     */
    protected static MessageDigest md5 = null;
    /**
     * @threadsafe True; a private instance for use with a TextUtils instance.
     */
    private MessageDigest _md5 = null;

    static {
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception err) {
            System.err.println("MD5 algorighthm could not intitialize");
        }

    }
    final static Pattern delws = Pattern.compile("\\s+");
    // Match ALL empty lines:
    //   \n followed by other ootional whitespace
    //  Up to 2 empty lines or more.  This matches 3 line endings
    //  The first EOL could be on a non-empty line, but then followed by 2 empty lines.
    // The intent is to reduce 3 or more EOL to 2.  Preserving paragraph breaks.
    //
    final static Pattern multi_eol = Pattern.compile("(\n[ \t\r]*){3,}");

    /**
     * Convenience constructor -- for thread-safe instances
     */
    public TextUtils() {
        try {
            _md5 = MessageDigest.getInstance("MD5");
        } catch (Exception err) {
            System.err.println("MD5 algorighthm could not intitialize");
        }
    }

    /**
     * @param data
     * @return boolean if data is ASCII or not
     */
    public static boolean isASCII(byte[] data) {
        for (byte b : data) {
            if (b < 0 || b > 0x7F) {
                return false;
            }
        }
        return true;
    }

    /**
     * count the number of ASCII bytes
     *
     * @param data
     * @return count of ASCII bytes
     */
    public static int countASCII(byte[] data) {
        int ascii = 0;
        for (byte b : data) {
            if (b > 0 || b < 0x80) {
                ++ascii;
            }
        }
        return ascii;
    }

    /**
     * Replaces all 3 or more blank lines with a single paragraph break (\n\n)
     *
     * @param t
     * @return A string with fewer line breaks;
     *
     */
    public static String reduce_line_breaks(String t) {

        Matcher m = multi_eol.matcher(t);
        if (m != null) {
            return m.replaceAll("\n\n");
        }
        return t;
    }

    /**
     * Delete whitespace of any sort.
     *
     * @param t
     * @return String, without whitespace.
     */
    public static String delete_whitespace(String t) {
        Matcher m = delws.matcher(t);
        if (m != null) {
            return m.replaceAll("");
        }
        return t;
    }

    /**
     * Minimize whitespace.
     *
     * @param t
     * @return String
     */
    public static String squeeze_whitespace(String t) {
        Matcher m = delws.matcher(t);
        if (m != null) {
            return m.replaceAll(" ");
        }
        return t;
    }

    /**
     *
     * @param t
     * @return
     */
    public static String delete_eol(String t) {
        return t.replace('\n', ' ').replace('\r', ' ');
    }

    /**
     * Counts all digits in text.
     *
     * @param txt
     * @return
     */
    public static int count_digits(String txt) {
        if (txt == null) {
            return 0;
        }

        int digits = 0;
        for (char c : txt.toCharArray()) {
            if (Character.isDigit(c)) {
                ++digits;
            }
        }
        return digits;
    }

    /**
     *
     * @param v
     * @return
     */
    public static boolean is_numeric(String v) {

        if (v == null) {
            return false;
        }

        for (char ch : v.toCharArray()) {

            if (ch == '.' || ch == '-' || ch == '+') {
                continue;
            }

            if (!Character.isDigit(ch)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Counts all digits in text.
     *
     * @param txt
     * @return
     */
    public static int count_ws(String txt) {
        if (txt == null) {
            return 0;
        }

        int ws = 0;
        for (char c : txt.toCharArray()) {
            // isWhitespaceChar(c)?
            if (Character.isWhitespace(c)) {
                ++ws;
            }
        }
        return ws;
    }

    /**
     * EH: EHCommons.get_text_window
     *
     * @param offset
     * @param width
     * @param textsize
     * @param matchlen
     * @return
     */
    public static int[] get_text_window(int offset, int matchlen, int textsize, int width) {
        /*
         prepreprepre MATCH postpostpost
         ^            ^   ^            ^
         l-width      l   l+len        l+len+width
         left_y  left_x   right_x      right_y
        
         * 
         */
        int left_x = offset - width;
        int left_y = offset - 1;
        int right_x = offset + matchlen;
        int right_y = right_x + width;
        if (left_x < 0) {
            left_x = 0;
        }

        // Fix left side of bounds
        if (left_y < left_x) {
            left_y = left_x;
        }

        // Fix right side of bounds
        if (right_y >= textsize) {
            right_y = textsize;
        }
        if (right_x > right_y) {
            right_x = right_y;
        }


        int[] slice = {
            left_x, left_y, right_x, right_y};

        return slice;
    }

    /**
     * Get a single text window around the offset.
     *
     * @param offset
     * @param width
     * @param textsize
     * @return offsets of window bounded by document ends.
     */
    public static int[] get_text_window(int offset, int textsize, int width) {
        /*
         left  .... match   .... right        
         * 
         */
        int half = (int) (width / 2);
        int left = offset - half;
        int right = offset + half;

        if (left < 0) {
            left = 0;
        }

        // Fix right side of bounds
        if (right >= textsize) {
            right = textsize;
        }

        int[] slice = {left, right};

        return slice;
    }

    /**
     * Static method -- use only if you are sure of thread-safety.
     *
     * @param text
     * @return
     */
    public static String text_id(String text) {
        if (text == null) {
            return null;
        }

        md5.reset();
        md5.update(text.getBytes());

        return md5_id(md5.digest());
    }

    /**
     * Generate a Text ID using the raw bytes and MD5 algorithm.
     *
     * @param text
     * @return
     */
    public String genTextID(String text) {
        if (text == null) {
            return null;
        }

        _md5.reset();
        _md5.update(text.getBytes());

        return md5_id(_md5.digest());
    }

    /**
     *
     * @param md5digest
     * @return
     */
    public static String md5_id(byte[] md5digest) {
        // Thanks to javacream:
        //create hex string from the 16-byte hash
        StringBuilder hashbuf = new StringBuilder(md5digest.length * 2);
        for (byte b : md5digest) {
            int intVal = b & 0xff;
            if (intVal < 0x10) {
                hashbuf.append("0");
            }
            hashbuf.append(Integer.toHexString(intVal));
        }
        return hashbuf.toString().toLowerCase();
    }

    /**
     * Get a list of values into a nice, scrubbed array of values, no
     * whitespace.
     *
     * a, b, c d e, f => [ "a", "b", "c d e", "f" ]
     *
     * @param s
     * @param delim
     * @return
     */
    public static List<String> string2list(String s, String delim) {
        if (s == null) {
            return null;
        }

        List<String> values = new ArrayList<String>();
        String[] _vals = s.split(delim);
        for (String v : _vals) {
            String val = v.trim();
            if (!val.isEmpty()) {
                values.add(val);
            }
        }
        return values;
    }

    /**
     * Given a string S and a list of characters to replace with a substitute,
     *
     * return the new string, S'.
     *
     * "-name-with.invalid characters;" // replace "-. ;" with "_"
     * "_name_with_invalid_characters_" //
     *
     * @param buf
     * @param replace string of characters to replace with the one substitute
     * char
     * @param substitution
     * @return
     */
    public static String fast_replace(String buf, String replace, String substitution) {

        StringBuilder _new = new StringBuilder();
        for (char ch : buf.toCharArray()) {
            if (replace.indexOf(ch) > 0) {
                _new.append(substitution);
            } else {
                _new.append(ch);
            }
        }
        return _new.toString();
    }

    /**
     * Remove instances of any char in the remove string from buf
     *
     */
    public static String fast_remove(String buf, String remove) {

        StringBuilder _new = new StringBuilder();
        for (char ch : buf.toCharArray()) {
            if (remove.indexOf(ch) < 0) {
                _new.append(ch);
            }
        }
        return _new.toString();
    }
    // UnicodeBlock.MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS;
    private final static Pattern scrub_symbols = Pattern.compile("\\p{block=Miscellaneous Symbols And Pictographs}+");
    private final static Pattern scrub_symbols2 = Pattern.compile("\\p{block=Transport and Map Symbols}+");
    private final static Pattern scrub_emoticon = Pattern.compile("\\p{block=Emoticons}+");
    private final static Pattern scrub_alphasup = Pattern.compile("\\p{block=Enclosed Alphanumeric Supplement}+");
    private final static Pattern scrub_symbols_tiles1 = Pattern.compile("\\p{block=Mahjong Tiles}+");
    private final static Pattern scrub_symbols_tiles2 = Pattern.compile("\\p{block=Domino Tiles}+");
    private final static Pattern scrub_symbols_misc = Pattern.compile("\\p{block=Miscellaneous Symbols}+");
    private final static Pattern scrub_symbols_cards = Pattern.compile("\\p{block=Playing Cards}+");

    /**
     * replace Emoticons with something less nefarious -- UTF-16 characters do
     * not play well with some I/O routines.
     *
     * @param t
     * @return
     */
    public static String remove_emoticons(String t) {
        return scrub_emoticon.matcher(t).replaceAll("{icon}");
    }

    /**
     * Replace symbology
     *
     * @param t
     * @return
     */
    public static String remove_symbols(String t) {
        String _new = scrub_symbols.matcher(t).replaceAll("{sym}");
        _new = scrub_symbols2.matcher(_new).replaceAll("{sym2}");
        _new = scrub_alphasup.matcher(_new).replaceAll("{asup}");
        _new = scrub_symbols_tiles1.matcher(_new).replaceAll("{tile1}");
        _new = scrub_symbols_tiles2.matcher(_new).replaceAll("{tile2}");
        _new = scrub_symbols_misc.matcher(_new).replaceAll("{sym}");
        _new = scrub_symbols_cards.matcher(_new).replaceAll("{card}");

        return _new;
    }

    /**
     * Normalization: Clean the ends, Remove Line-endings from middle of entity.
     * <pre>
     *  Example:
     *        TEXT: **The Daily Newsletter of \n\rBarbara, So.**
     *       CLEAN: __The Daily Newsletter of __Barbara, So___
     *
     * Where "__" represents omitted characters.
     * </pre>
     */
    public static String normalize_text_entity(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }

        char[] chars = str.toCharArray();

        int s1 = 0, s2 = chars.length - 1;
        int end = s2;

        while (s1 < s2
                && !(Character.isLetter(chars[s1])
                || Character.isDigit(chars[s1]))) {
            ++s1;
        }

        // No text found
        if (s1 == s2) {
            return null;
        }

        while (s2 > s1
                && !(Character.isLetter(chars[s2])
                || Character.isDigit(chars[s2]))) {
            --s2;
        }

        if (s1 == 0 && s2 == end) {
            // No cleanup to do.
            return squeeze_whitespace(str);
        }

        // NOT possible, I hope... 
        if (s2 <= s1) {
            return null;
        }

        // Some cleanup was done on ends of String. Now clear up whitespace.
        // 
        return squeeze_whitespace(str.substring(s1, s2 + 1));
    }
    // Alphabetic list of top-N languages -- ISO-639_1  "ISO2" language codes
    // 
    public final static String arabicLang = "ar";
    public final static String bahasaLang = "id";
    public final static String chineseLang = "zh";
    public final static String chineseTradLang = "zt";
    public final static String englishLang = "en";
    public final static String farsiLang = "fa";
    public final static String frenchLang = "fr";
    public final static String germanLang = "de";
    public final static String italianLang = "it";
    public final static String japaneseLang = "ja";
    public final static String koreanLang = "ko";
    public final static String portugueseLang = "pt";
    public final static String russianLang = "ru";
    public final static String spanishLang = "es";
    public final static String turkishLang = "tr";
    public final static String thaiLang = "th";
    public final static String vietnameseLang = "vi";
    public final static String romanianLang = "ro";
    private final static Map<String, Language> LanguageMap_ISO639 = new HashMap<String, Language>();

    static {
        initLanguageData();
    }

    /**
     * Initialize language codes and metadata. This establishes a map for the
     * most common language codes/names that exist in at least ISO-639-1 and
     * have a non-zero 2-char ID.
     *
     * Based on:
     * http://stackoverflow.com/questions/674041/is-there-an-elegant-way-to-convert-iso-639-2-3-letter-language-codes-to-java-lo
     *
     * Actual code mappings: en => eng eng => en
     *
     * cel => '' // Celtic; Avoid this.
     *
     * tr => tur tur => tr
     *
     * Names: tr => turkish tur => turkish turkish => tr // ISO2 only
     *
     *
     */
    public static void initLanguageData() {
        Locale[] locales = Locale.getAvailableLocales();
        for (Locale locale : locales) {
            Language l = new Language(locale.getISO3Language(), 
                    locale.getLanguage(), locale.getDisplayLanguage());
            String iso2 = l.getISO639_1_Code();

            if (iso2 != null && iso2.length() > 0) {
                String namekey = l.getName().toLowerCase();
                LanguageMap_ISO639.put(iso2, l);
                LanguageMap_ISO639.put(l.getCode(), l);
                LanguageMap_ISO639.put(namekey, l);
            }
        }
    }

    /**
     * Given an ISO2 char code (least common denominator) retrieve Language
     * Name.
     *
     * This is best effort, so if your code finds nothing, this returns code
     * normalized to lowercase.
     */
    public static String getLanguageName(String code) {
        if (code == null) {
            return null;
        }

        Language l = LanguageMap_ISO639.get(code.toLowerCase());
        if (l == null) {
            return code;
        }
        return l.getName();
    }

    /**
     * ISO2 and ISO3 char codes for languages are unique.
     *
     * @param code iso2 or iso3 code
     * @return the other code.
     */
    public static Language getLanguage(String code) {
        if (code == null) {
            return null;
        }

        return LanguageMap_ISO639.get(code.toLowerCase());
    }

    /**
     * ISO2 and ISO3 char codes for languages are unique.
     *
     * @param code iso2 or iso3 code
     * @return the other code.
     */
    public static String getLanguageCode(String code) {
        if (code == null) {
            return null;
        }

        Language l = LanguageMap_ISO639.get(code.toLowerCase());
        if (l != null) {
            return l.getCode();
        }
        return null;
    }

    private static boolean _isRomanceLanguage(String l) {
        return (l.equals(spanishLang)
                || l.equals(portugueseLang)
                || l.equals(italianLang)
                || l.equals(frenchLang)
                || l.equals(romanianLang));
    }

    /**
     * European languages = Romance + GER + ENG
     *
     * Extend definition as needed.
     */
    public static boolean isEuroLanguage(String l) {
        Language lang = getLanguage(l);

        if (lang == null) {
            return false;
        }
        String id = lang.getISO639_1_Code();
        return (_isRomanceLanguage(id)
                || id.equals(germanLang)
                || id.equals(englishLang));
    }

    /**
     * Romance languages = SPA + POR + ITA + FRA + ROM
     *
     * Extend definition as needed.
     */
    public static boolean isRomanceLanguage(String l) {
        Language lang = getLanguage(l);

        if (lang == null) {
            return false;
        }
        String id = lang.getISO639_1_Code();
        return _isRomanceLanguage(id);
    }

    /**
     * Utility method to check if lang ID is English...
     *
     * @param x a langcode
     * @return whether langcode is english
     */
    public static boolean isEnglish(String x) {
        Language lang = getLanguage(x);

        if (lang == null) {
            return false;
        }
        String id = lang.getISO639_1_Code();
        return (id.equals(englishLang));
    }

    /**
     * Utility method to check if lang ID is Chinese(Traditional or
     * Simplified)...
     *
     * @param x a langcode
     * @return whether langcode is chinese
     */
    public static boolean isChinese(String x) {
        Language lang = getLanguage(x);

        if (lang == null) {
            return false;
        }
        String id = lang.getISO639_1_Code();
        return (id.equals(chineseLang)
                | id.equals(chineseTradLang));
    }

    /**
     * Utility method to check if lang ID is Chinese, Korean, or Japanese
     *
     * @param x a langcode
     * @return whether langcode is a CJK language
     */
    public static boolean isCJK(String x) {
        Language lang = getLanguage(x);

        if (lang == null) {
            return false;
        }
        String id = lang.getISO639_1_Code();
        return (id.equals(koreanLang)
                | id.equals(japaneseLang)
                | id.equals(chineseLang)
                | id.equals(chineseTradLang));
    }
}
