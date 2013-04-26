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

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringEncoder;

/**
 * An encoder that reduces all diacritics to the appropriate base character.
 */
public class DiacriticEncoder implements StringEncoder {

	@Override
	public Object encode(Object obj) throws EncoderException {
		if (!(obj instanceof String)) {
			throw new EncoderException(
					"Parameter supplied to DiacriticEncoder is not of type java.lang.String");
		}
		return encode((String) obj);
	}

	@Override
	public String encode(String word) throws EncoderException {
		return PhoneticUtils.removeDiacritics(word);
	}

}
