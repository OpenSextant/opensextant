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

import org.mitre.giscore.DocumentType;
import org.mitre.giscore.events.ContainerEnd;
import org.mitre.giscore.events.ContainerStart;
import org.mitre.giscore.events.DocumentStart;
import org.mitre.giscore.events.Feature;
import org.mitre.giscore.output.IGISOutputStream;
import org.mitre.opensextant.processing.Geocoding;
import org.mitre.opensextant.processing.GeocodingResult;
import org.mitre.opensextant.processing.OpenSextantSchema;
import org.mitre.opensextant.processing.ProcessingException;
import org.mitre.opensextant.processing.output.GISDataModel;
//import java.util.List;

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
    protected IGISOutputStream os = null;
    /**
     *
     */
    protected boolean groupByDocument = false;
    private int id = 0;
    private GISDataModel gisDataModel;

    /**
     *
     */
    public GISDataFormatter() {
    }
    
    public void setGisDataModel(GISDataModel gisDataModel) {
        this.gisDataModel = gisDataModel;
    }


    /**
     * Start output.
     */
    @Override
    public void start(String containerName) throws ProcessingException {

        if (this.gisDataModel == null) this.gisDataModel = new GISDataModel(getJobName(), includeOffsets, includeCoordinate);
        
        try {
            createOutputStreams();
        } catch (Exception create_err) {
            throw new ProcessingException(create_err);
        }

        
        DocumentStart ds = new DocumentStart(doc_type);
        this.os.write(ds);
        this.os.write(gisDataModel.getSchema());

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
     * This allows you to add either output of a Corpus processor
     * (gate.Document, gate.Corpus) or to add a Geocoding Result directly
     *
     */
    @Override
    public void writeGeocodingResult(GeocodingResult rowdata) {
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

            try {
                for (Feature row : gisDataModel.buildRows(id, g, rowdata.attributes, rowdata.recordFile, rowdata.recordTextFile, rowdata)) {
                    if (log.isDebugEnabled()) {
                        log.debug("FEATURE: " + row.toString());
                    }
                    
                    this.os.write(row);
                }

            } catch (ProcessingException fieldErr) {
                
                if (!error) log.error("OUTPUTTER, ERR=" + fieldErr);
                error = true;
                
            }

            

        }

    }

    /**
     * Returns a list of Features corresponding to place and geocoord
     * annotations. The name of each Feature is the text mention.
     *  <pre>
     * Result/output processing includes 
     * (a) digesting all Annotations from Document
     *     which are consumed here by GeocodingResult. 
     *     (GeocodingResult geoAnnotations).retrieveGeocodes(doc);
     * 
     * (b) digesting all optional Features provided on the Document
     *     val = (Object) doc.getFeatures().get("key")
     * 
     * </pre>
     * @param doc
     */
    @Override
    public void writeRowsFor(Document doc) {

        // Is there a doc ID?
        GeocodingResult geoAnnotations = gisDataModel.buildGeocodingResults(doc.getName());
        geoAnnotations.recordFile = (String) doc.getFeatures().get(OpenSextantSchema.FILEPATH_FLD);
        geoAnnotations.recordTextFile = doc.getSourceUrl().getPath();
        log.info("Writing output for " + geoAnnotations.recordFile);

        try {
            geoAnnotations.retrieveGeocodes(doc);

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

            writeGeocodingResult(geoAnnotations);

            if (this.groupByDocument) {
                ContainerEnd end = new ContainerEnd();
                this.os.write(end);
            }
        } catch (Exception err) {
            log.error("Error writing out row ROW=" + id, err);
        }
    }

}
