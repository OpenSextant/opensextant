/**
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
 *
 * **************************************************************************
 * NOTICE This software was produced for the U. S. Government under Contract No.
 * W15P7T-12-C-F600, and is subject to the Rights in Noncommercial Computer
 * Software and Noncommercial Computer Software Documentation Clause
 * 252.227-7014 (JUN 1995)
 *
 * (c) 2012 The MITRE Corporation. All Rights Reserved.
 * **************************************************************************
 *
 */
package org.mitre.opensextant.processing.output;

import gate.Corpus;
import gate.Document;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
//import java.util.List;
import org.mitre.giscore.DocumentType;
import org.mitre.giscore.events.ContainerEnd;
import org.mitre.giscore.events.ContainerStart;
import org.mitre.giscore.events.DocumentStart;
import org.mitre.giscore.events.Feature;
import org.mitre.giscore.events.Schema;
import org.mitre.giscore.events.SimpleField;
import org.mitre.giscore.geometry.Point;
import org.mitre.giscore.output.IGISOutputStream;
import org.mitre.opensextant.processing.GeocodingResult;
import org.mitre.opensextant.processing.Geocoding;
import org.mitre.opensextant.processing.ProcessingException;
import org.mitre.opensextant.processing.OpenSextantSchema;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import org.apache.commons.io.FilenameUtils;

/**
 * This is the base class for classes that convert document annotations to
 * GISCore features. Subclasses differ chiefly by choice of text string for the
 * description field. For some types of documents (e.g., news articles) the
 * sentence containing the annotation is a good choice, but for other types
 * (e.g., spreadsheets) sentence splitting may not be successful and the line of
 * text containing the annotation is a better choice.
 *
 * @author Rich Markeloff, MITRE Corp. Initial version created on Dec 20, 2011
 *
 * @author Marc C. Ubaldino, MITRE Corp. Refactored, redesigned package, 2013.
 */
public abstract class GISDataFormatter extends AbstractFormatter {

    /**
     *
     */
    protected DocumentType doc_type = null;
    /**
     *
     */
    protected Schema schema = null;
    /**
     *
     */
    protected IGISOutputStream os = null;
    /**
     *
     */
    protected boolean groupByDocument = false;
    public List<String> field_order = new ArrayList<>();
    public Set<String> field_set = new HashSet<>();
    private int id = 0;

    /**
     *
     */
    public GISDataFormatter() {
        defaultFields();
    }

    public final void defaultFields() {
        // ID occurs in all output.
        // id.

        // Matching data
        field_order.add("placename");

        // Geographic
        field_order.add("province");
        field_order.add("iso_cc");
        field_order.add("lat");
        field_order.add("lon");

        // Textual context.
        field_order.add("matchtext");
        field_order.add("context");
        field_order.add("filename");
        field_order.add("filepath");
        field_order.add("textpath");

        // File mechanics
        field_order.add("method");
        field_order.add("feat_class");
        field_order.add("feat_code");
        field_order.add("confidence");
        field_order.add("precision");
        field_order.add("start");
        field_order.add("end");

    }

    /**
     * Start output.
     */
    @Override
    public void start(String containerName) throws ProcessingException {

        try {
            createOutputStreams();
        } catch (Exception create_err) {
            throw new ProcessingException(create_err);
        }

        getSchema();
        DocumentStart ds = new DocumentStart(doc_type);
        this.os.write(ds);
        this.os.write(this.schema);

        ContainerStart containerStart = new ContainerStart();
        containerStart.setType("Folder");
        containerStart.setName(containerName);
        this.os.write(containerStart);
    }

    @Override
    public void finish() {
        if (this.os == null) {
            return;
        }

        ContainerEnd containerEnd = new ContainerEnd();
        this.os.write(containerEnd);

        try {
            closeOutputStreams();
        } catch (Exception closer) {
            log.error("ERROR finalizing data file ", closer);
        }
    }

    /**
     * Write out each place and geocoord to a file. There is a folder for each
     * corpus and a subfolder for each document.
     *
     * @param corpusList
     * @throws Exception
     */
    @Override
    public void writeOutput(Corpus c) throws Exception {

        // Iterate through the documents in this corpus
        for (Object o : c) {
            this.writeRowsFor((Document) o);
        }
    }

    protected File createTempFolder(String key) {
        File tempDir = new File(this.outputParams.tempDir + File.separator + key + "_" + System.currentTimeMillis());
        tempDir.mkdirs();
        return tempDir;
    }

    /**
     *
     * @throws Exception
     */
    @Override
    protected void closeOutputStreams() throws Exception {
        if (this.os != null) {
            this.os.close();
        }
    }

    /**
     * This helps you figure out what to put in the GIS products.
     */
    protected boolean filterOut(Geocoding geo) {
        if (geo.filtered_out) {
            return true;
        }
        if (!outputParams.output_coordinates && geo.is_coordinate) {
            return true;
        } else if (!outputParams.output_countries && geo.is_country) {
            return true;
        } else if (!outputParams.output_places && geo.is_place) {
            return true;
        }

        return false;
    }

    /**
     */
    protected boolean canAdd(SimpleField f) {
        if (f == null) {
            return false;
        }
        return field_set.contains(f.getName()) && (schema.get(f.getName()) != null);
    }

    /**
     * Add a column of data to output; Field is validated ; value is not added
     * if null
     */
    protected void addColumn(Feature row, SimpleField f, Object d) {
        if (d == null) {
            return;
        }
        if (canAdd(f)) {
            row.putData(f, d);
        }
    }

    /**
     * Add a column of data to output; Field is validated
     */
    protected void addColumn(Feature row, SimpleField f, int d) {
        if (canAdd(f)) {
            row.putData(f, d);
        }
    }

    /**
     * Add a column of data to output; Field is validated
     */
    protected void addColumn(Feature row, SimpleField f, double d) {
        if (canAdd(f)) {
            row.putData(f, d);
        }
    }

    /**
     * This allows you to add either output of a Corpus processor
     * (gate.Document, gate.Corpus) or to add a Geocoding Result directly
     *
     */
    @Override
    public void writeGeocodingResult(GeocodingResult rowdata) {
        Feature row;
        boolean error = false;

        if (log.isDebugEnabled()) {
            log.debug("Adding data for File " + rowdata.recordFile + " Count=" + rowdata.geocodes.size());
        }

        for (Geocoding g : rowdata.geocodes) {

            if (filterOut(g)) {
                continue;
            }
            // Increment ID
            id++;

            if (log.isDebugEnabled()) {
                log.debug("Add " + id + "#" + g.toString());
            }

            row = new Feature();
            // Administrative settings:
            // row.setName(getJobName());            
            row.setSchema(schema.getId());
            row.putData(OpenSextantSchema.SCHEMA_OID, id);

            // 
            if (includeOffsets) {
                addColumn(row, OpenSextantSchema.START_OFFSET, (int) g.start);
                addColumn(row, OpenSextantSchema.END_OFFSET, (int) g.end);
            }

            addColumn(row, OpenSextantSchema.ISO_COUNTRY, g.place.getCountryCode());
            addColumn(row, OpenSextantSchema.PROVINCE, g.place.getAdmin1());
            addColumn(row, OpenSextantSchema.FEATURE_CLASS, g.place.getFeatureClass());
            addColumn(row, OpenSextantSchema.FEATURE_CODE, g.place.getFeatureCode());
            addColumn(row, OpenSextantSchema.PRECISION, g.precision);
            addColumn(row, OpenSextantSchema.CONFIDENCE, formatConfidence(g.confidence));

            addColumn(row, OpenSextantSchema.CONTEXT, g.getContext());

            if (includeCoordinate) {
                addColumn(row, OpenSextantSchema.LAT, g.place.getLatitude());
                addColumn(row, OpenSextantSchema.LON, g.place.getLongitude());
            }

            addColumn(row, OpenSextantSchema.MATCH_TEXT, g.getText());
            addColumn(row, OpenSextantSchema.PLACE_NAME, g.place.getPlaceName());
            addColumn(row, OpenSextantSchema.MATCH_METHOD, g.method);

            // Set the name and coordinates
            // row.setName(g.getText());


            /**
             * If the caller has additional data to attach to records, allow
             * them to add fields to schema at runtime and map their data to
             * keys on GeocodingResult
             *
             * Similarly, you could have Geocoding row-level attributes unique
             * to the geocoding whereas attrs on GeocodingResult are global for
             * all geocodings in that result set
             */
            if (rowdata.attributes != null) {

                try {
                    for (String field : rowdata.attributes.keySet()) {
                        if (log.isDebugEnabled()) {
                            log.debug("FIELD=" + field + " = " + rowdata.attributes.get(field));
                        }
                        addColumn(row, OpenSextantSchema.getField(field), rowdata.attributes.get(field));
                    }
                } catch (ProcessingException fieldErr) {
                    if (!error) {
                        log.error("OUTPUTTER, ERR=" + fieldErr);
                        error = true;
                    }
                }
            }

            // Set the geometry to be a point, and add the feature to the list
            row.setGeometry(new Point(g.place.getLatitude(), g.place.getLongitude()));

            // TOOD: HPATH goes here.
            if (rowdata.recordFile != null) {
                addColumn(row, OpenSextantSchema.FILENAME, FilenameUtils.getBaseName(rowdata.recordFile));
                addColumn(row, OpenSextantSchema.FILEPATH, rowdata.recordFile);
                // Only add text path:
                //   if original is not plaintext or
                //   if original has not been converted
                //
                if (rowdata.recordTextFile != null && !rowdata.recordFile.equals(rowdata.recordTextFile)) {
                    addColumn(row, OpenSextantSchema.TEXTPATH, rowdata.recordTextFile);
                }
            } else {
                log.info("No File path given");
            }

            if (log.isDebugEnabled()) {
                log.debug("FEATURE: " + row.toString());
            }

            this.os.write(row);
        }

    }

    /**
     * Returns a list of Features corresponding to place and geocoord
     * annotations. The name of each Feature is the text mention.
     *
     * @param doc
     */
    @Override
    public void writeRowsFor(Document doc) {



        // Is there a doc ID?
        GeocodingResult annotations = new GeocodingResult(doc.getName());
        annotations.recordFile = (String) doc.getFeatures().get(OpenSextantSchema.FILEPATH_FLD);
        annotations.recordTextFile = doc.getSourceUrl().getPath();
        log.info("Writing output for " + annotations.recordFile);

        try {
            annotations.retrieveGeocodes(doc);

            // Support for foldered output -- KML, KMZ, others?
            if (this.groupByDocument) {
                ContainerStart containerStart = new ContainerStart();
                containerStart.setType("Folder");
                String folderName = doc.getName();
                String[] splits = folderName.split("\\.");
                if (splits.length >= 1) {
                    folderName = splits[0];
                }
                containerStart.setName(folderName);
                this.os.write(containerStart);
            }

            writeGeocodingResult(annotations);

            if (this.groupByDocument) {
                ContainerEnd end = new ContainerEnd();
                this.os.write(end);
            }
        } catch (Exception err) {
            log.error("Error writing out row ROW=" + id, err);
        }
    }

    /**
     * Create a schema instance with the fields properly typed and ordered
     *
     * @return
     * @throws ProcessingException
     */
    public Schema getSchema() throws ProcessingException {

        if (this.schema != null) {
            return this.schema;
        }

        URI uri = null;
        try {
            uri = new URI("urn:OpenSextant");
        } catch (URISyntaxException e) {
            //e.printStackTrace();
        }

        this.schema = new Schema(uri);
        // Add ID field to the schema
        this.schema.put(OpenSextantSchema.SCHEMA_OID);
        this.schema.setName(getJobName());

        for (String field : field_order) {

            if (!this.includeOffsets && (field.equals("start") | field.equals("end"))) {
                continue;
            }

            if (!this.includeCoordinate && (field.equals("lat") | field.equals("lon"))) {
                continue;
            }

            SimpleField F = OpenSextantSchema.getField(field);
            this.schema.put(F);
        }

        this.field_set.addAll(field_order);

        return this.schema;
    }
}
