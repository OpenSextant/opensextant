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
package org.mitre.opensextant.extraction;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Date;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.DateUtil;
import org.apache.solr.core.CoreContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.mitre.solr.NoSerializeEmbeddedSolrServer;

/**
 * This class creates a read-only instance of Solr for querying.
 *
 * @author ubaldino
 */
public class SolrProxy {

    /**
     * Initializes a Solr server from a URL
     *
     * @throws IOException
     */
    public SolrProxy(String url) throws IOException {
        this.server_url = url;
        initialize();
    }

    /**
     * Initializes a Solr server from the SOLR_HOME environment variable
     *
     * @throws IOException
     */
    public SolrProxy() throws IOException {
        this.server_url = null;
        // get SOLR_HOME variable from environment
        // This produces a local EmbeddedSolrServer
        initialize();
    }

    /**
     * Initializes a Solr server from the SOLR_HOME environment variable
     *
     * @throws IOException
     */
    public SolrProxy(String solr_home, String core) throws IOException {
        this.server_url = null;
        setupCore(solr_home, core);
    }
    
    /**
     * Initializes a Solr server from the SOLR_HOME environment variable
     *
     * @throws IOException
     */
    public SolrProxy(String solr_home, String core, boolean noSerialize) throws IOException {
        this.server_url = null;
        setupCore(solr_home, core, noSerialize);
    }
    
    protected Logger logger = LoggerFactory.getLogger(SolrProxy.class);
    private SolrServer solrServer = null;
    private UpdateRequest solrUpdate = null;
    private String server_url = null;
    private boolean writable = false;

    public void setWritable(boolean b) {
        writable = b;
    }

    /**
     *
     * Is Solr server instance allowed to write to index?
     */
    public boolean isWritable() {
        return writable;
    }

    /**
     *
     * @throws IOException
     */
    public final void initialize() throws IOException {

        if (server_url == null) {
            this.solrServer = initialize_embedded();
        } else if (server_url.toLowerCase().startsWith("http:")) {
            this.solrServer = initialize_http(server_url);
        } else {
            throw new IOException("Could not initalize Solr");
        }
    }

    /**
     * Get an HTTP server for Solr.
     *
     * @param url
     * @return Instance of a Solr server
     * @throws MalformedURLException
     */
    public static SolrServer initialize_http(String url)
            throws MalformedURLException {

        HttpSolrServer server = new HttpSolrServer(url);
        server.setAllowCompression(true);

        return server;

    }

    /**
     * An improved, supported method for creating an EmbeddedSolr from a single
     * or multi-core solr instance. If you just have the one core, this setup
     * still relies on the presence of solr.xml
     */
    public final void setupCore(String solr_home, String corename) throws IOException {
        this.solrServer = SolrProxy.initialize_embedded(solr_home, corename, true);
    }

    public final void setupCore(String solr_home, String corename, boolean serializationMode) throws IOException {
        this.solrServer = SolrProxy.initialize_embedded(solr_home, corename, serializationMode);
    }

    /**
     * 
     */
    public static SolrServer initialize_embedded(String solr_home, String corename, boolean avoidSerialization)
            throws IOException {

        try {
            File solr_xml = new File(solr_home + File.separator + "solr.xml");
            CoreContainer solrContainer = new CoreContainer(solr_home);
            solrContainer.load(solr_home, solr_xml);

            if (avoidSerialization) {
                // DEFAULT Per SolrTextTagger optimization:
                return new NoSerializeEmbeddedSolrServer(solrContainer, corename);
            } else {
                // CONVENTIONAL: 
                return new EmbeddedSolrServer(solrContainer, corename);
            }

        } catch (Exception err) {
            throw new IOException("Failed to set up Embedded Solr", err);
        }
    }

    public static SolrServer initialize_embedded()
            throws IOException {
        return initialize_embedded(true);
    }

    /**
     * Much simplified EmbeddedSolr setup.
     *
     * @throws IOException
     */
    public static SolrServer initialize_embedded(boolean avoidSerialization)
            throws IOException {

        try {
            CoreContainer.Initializer initializer = new CoreContainer.Initializer();
            CoreContainer solrContainer = initializer.initialize();

            if (avoidSerialization) {
                // DEFAULT Per SolrTextTagger optimization:
                return new NoSerializeEmbeddedSolrServer(solrContainer, "");
            } else {
                // CONVENTIONAL
                return new EmbeddedSolrServer(solrContainer, "");
            }
        } catch (Exception err) {
            throw new IOException("Failed to set up Embedded Solr", err);
        }
    }

    /**
     * Add one solr record.
     *
     * @param solrRecord
     * @throws Exception
     */
    public void add(SolrInputDocument solrRecord)
            throws Exception {

        if (!this.writable) {
            throw new Exception("This instance is not configured for writing to index");
        }

        // Initialize per batch if nec.y
        if (solrUpdate == null) {
            solrUpdate = new UpdateRequest();
        }

        // Initialize per record
        // .. add data to record
        //   .. add record to batch request
        solrUpdate.add(solrRecord);
    }

    /**
     * Add many solr records.
     *
     * @param solrRecords
     * @throws Exception
     */
    public void add(java.util.Collection<SolrInputDocument> solrRecords)
            throws Exception {

        if (!this.writable) {
            throw new Exception("This instance is not configured for writing to index");
        }

        // Initialize per batch if nec.y
        if (solrUpdate == null) {
            solrUpdate = new UpdateRequest();
        }

        // Initialize per record
        // .. add data to record
        //   .. add record to batch request
        solrUpdate.add(solrRecords);
    }

    public void openIndex()
            throws IOException {
        if (solrServer == null) {
            initialize();
        }
    }

    /**
     * Optimizes the Solr server
     */
    public void optimize() throws IOException, SolrServerException {
        solrServer.optimize(true, false); // Don't wait'
    }

    /**
     * Invokes
     * <code>saveIndex(false)</code>
     */
    public void saveIndex() {
        saveIndex(false);
    }

    /**
     * Save and optionally records to server or index On failure, current
     * accumulating request is cleared and nullified to avoid retransmitting bad
     * data.
     *
     * In the event of a failure all records since last "saveIndex" would be
     * lost and should be resubmitted.
     */
    public void saveIndex(boolean commit) {
        if (solrUpdate == null) {
            return;
        }

        logger.info("Saving records to index");
        try {
            solrServer.request(solrUpdate);
            if (commit) {
                solrServer.commit();
            }
            solrUpdate.clear();
            solrUpdate = null;
        } catch (Exception filex) {
            logger.error("Index failed during indexing", filex);
            solrUpdate.clear();
            solrUpdate = null;
        }
    }

    /**
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void saveAndReopen()
            throws FileNotFoundException, IOException {
        saveIndex(/* commit = */true);
        openIndex();
    }

    /**
     *
     */
    public void close() {
        if (isWritable()) {
            saveIndex();
        }

        if (solrServer != null) {
            solrServer.shutdown();
            solrServer = null;
        }
    }

    public SolrServer getInternalSolrServer() {
        return solrServer;
    }

    /**
     * Get an integer from a record
     */
    public static int getInteger(SolrDocument d, String f) {
        Object obj = d.getFieldValue(f);
        if (obj == null) {
            return 0;
        }

        if (obj instanceof Integer) {
            return ((Integer) obj).intValue();
        } else {
            Integer v = Integer.parseInt(obj.toString());
            return v.intValue();
        }
    }

    /**
     * Get a floating point object from a record
     */
    public static Float getFloat(SolrDocument d, String f) {
        Object obj = d.getFieldValue(f);
        if (obj == null) {
            return 0F;
        } else {
            return (Float) obj;
        }
    }

    /**
     * Get a Date object from a record
     *
     * @throws java.text.ParseException
     */
    public static Date getDate(SolrDocument d, String f)
            throws java.text.ParseException {
        if (d == null || f == null) {
            return null;
        }
        Object obj = d.getFieldValue(f);
        if (obj == null) {
            return null;
        }
        if (obj instanceof Date) {
            return (Date) obj;
        } else if (obj instanceof String) {
            return DateUtil.parseDate((String) obj);
        }
        return null;
    }

    /**
     *
     */
    public static char getChar(SolrDocument solrDoc, String name) {
        String result = getString(solrDoc, name);
        if (result == null) {
            return 0;
        }
        if (result.isEmpty()) {
            return 0;
        }
        return result.charAt(0);
    }

    /**
     * Get a String object from a record
     */
    public static String getString(SolrDocument solrDoc, String name) {
        Object result = solrDoc.getFirstValue(name);
        if (result == null) {
            return null;
        }
        return result.toString();
    }

    /**
     *
     * Get a double from a record
     */
    public static double getDouble(SolrDocument solrDoc, String name) {
        Object result = solrDoc.getFirstValue(name);
        if (result == null) {
            throw new IllegalStateException("Blank: " + name + " in " + solrDoc);
        }
        if (result instanceof Number) {
            Number number = (Number) result;
            return number.doubleValue();
        } else {
            return Double.parseDouble(result.toString());
        }
    }

    /**
     * A simple test for verifying that a SolrProxy can be created and
     * initialized.
     */
    public static void main(String[] args) {

        try {
            SolrProxy test = new SolrProxy();
            test.initialize();
        } catch (Exception err) {
            err.printStackTrace();
        }
    }
}
