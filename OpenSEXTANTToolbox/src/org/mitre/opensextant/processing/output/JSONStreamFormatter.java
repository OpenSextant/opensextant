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
package org.mitre.opensextant.processing.output;

import gate.Corpus;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;

/**
 * A formatter for JSON output. Place objects, along with naked Geocoords, are
 * serialized. By default, only the highest scoring place for each
 * PlaceCandidate is selected. Optionally, all of them can be printed out.
 *
 * @author Rich Markeloff, MITRE Corp. Initial version created on Jan 17, 2012
 */
public class JSONStreamFormatter extends JSONFormatter {

    private XStream xstream;
    private BufferedWriter writer;

    /**
     *
     */
    public JSONStreamFormatter() {
        this.outputExtension = ".json";
        this.outputType = "JSON";
    }

    /**
     * @param outFilename 
     * @throws Exception 
     * @see
     * org.mitre.opensextant.processing.output.AbstractFormatter#createOutputStreams(java.lang.String)
     */
    @Override
    protected void createOutputStreams() throws Exception {
        this.xstream = new XStream(new JsonHierarchicalStreamDriver());
        this.xstream.setMode(XStream.NO_REFERENCES);
        this.writer = new BufferedWriter(new FileWriter(getOutputFilepath()));
    }

    /**
     * @throws Exception 
     * @see
     * org.mitre.opensextant.processing.output.AbstractFormatter#closeOutputStreams()
     */
    @Override
    protected void closeOutputStreams() throws Exception {
        this.writer.flush();
        this.writer.close();
    }

    /**
     * @param corpusList 
     * @throws Exception 
     * @see
     * org.mitre.opensextant.processing.output.AbstractFormatter#writeOutput(java.util.List)
     */
    @Override
    public void writeOutput(Corpus corpusList) throws Exception { 
        throw new Exception("Awaiting implementation for web-service");
    }
}
