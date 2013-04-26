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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import gate.Corpus;
import gate.Document;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import org.mitre.opensextant.placedata.Geocoord;
import org.mitre.opensextant.placedata.Place;
import org.mitre.opensextant.processing.*;

/**
 * A formatter for JSON output. Place objects, along with naked Geocoords, are
 * serialized. By default, only the highest scoring place for each
 * PlaceCandidate is selected. Optionally, all of them can be printed out.
 *
 * @author Rich Markeloff, MITRE Corp. Initial version created on Jan 17, 2012
 */
public class JSONFormatter extends AbstractFormatter {

    private XStream xstream;
    private BufferedWriter writer;

    /**
     *
     */
    public JSONFormatter() {
        this.outputExtension = ".json";
        this.outputType = "JSON";
    }

    @Override
    public void start(String nm) throws ProcessingException {
        try {
            createOutputStreams();
        } catch (Exception err) {
            throw new ProcessingException("Unable to create IO", err);
        }
    }

    /**
     *
     */
    @Override
    public void finish() {
        try {
            closeOutputStreams();
        } catch (Exception err) {
            // Silently ignore .... for now.
        }
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
        if (this.writer != null) {
            this.writer.flush();
            this.writer.close();
        }
    }

    /**
     * @param corpusList 
     * @throws Exception 
     * @see
     * org.mitre.opensextant.processing.output.AbstractFormatter#writeOutput(java.util.List)
     */
    @Override
    public void writeOutput(Corpus c) throws Exception {

        // Iterate through the documents in this corpus
        for (Object o : c) {
            this.writeRowsFor((Document) o);
        }
    }

    /**
     *
     * @param doc
     * @throws IOException  
     */
    @Override
    public void writeRowsFor(Document doc) throws IOException {
        // Is there a doc ID?
        GeocodingResult annotations = new GeocodingResult(doc.getName());
        try {
            annotations.retrieveGeocodes(doc);
        } catch (ProcessingException err) {
            throw new IOException(err);
        }
        writeGeocodingResult(annotations, doc.getSourceUrl().getPath());
    }

    /** TODO: re-write this outputter.
     * this output does not obide by the OpenSextant Schema
     */
    @Override
    public void writeGeocodingResult(GeocodingResult rowdata, String link) {
        try {
            for (Geocoding g : rowdata.geocodes) {
                if (g.is_coordinate) {
                    this.xstream.alias("Coordinate", Geocoord.class);
                } else {
                    this.xstream.alias("NamedPlace", Place.class);
                }
                this.writer.write(xstream.toXML(g.place));
            }
        } catch (IOException ioerr) {
            AbstractFormatter.log.error("TODO: change this logging. ", ioerr);
        }
    }
}
