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
package org.mitre.opensextant.extraction;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.DateUtil;
import org.apache.solr.core.CoreContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class creates a read-only instance of Solr for querying.
 * @author ubaldino
 */
public class SolrProxy {

    /**
     * Initializes a Solr server from a URL
     * @throws IOException  
     */
    public SolrProxy(String url) throws IOException {
        this.server_url = url;
        initialize();
    }

    /**
     * Initializes a Solr server from the SOLR_HOME environment variable
     * @throws IOException
     */
    public SolrProxy() throws IOException {
        this.server_url = null;
        // get SOLR_HOME variable from environment
        // This produces a local EmbeddedSolrServer
        initialize();
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
     * @param url
     * @return  Instance of a Solr server
     * @throws MalformedURLException  
     */
    public static SolrServer initialize_http(String url)
            throws MalformedURLException {

        HttpSolrServer server = new HttpSolrServer(url);
        server.setAllowCompression(true);

        return server;

    }

    /**
     * Much simplified EmbeddedSolr setup.
     * @throws IOException 
     */
    public static SolrServer initialize_embedded()
            throws IOException {
        try {
            CoreContainer.Initializer initializer = new CoreContainer.Initializer();
            CoreContainer solrContainer = initializer.initialize();
            return new EmbeddedSolrServer(solrContainer, "");
        } catch (Exception err) {
            throw new IOException("Failed to set up Embedded Solr", err);
        }
    }

    /**
     * This is the service call to the indexer.
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

    public void openIndex()
            throws IOException {
        if (solrServer == null) {
            initialize();
        }
    }

    /**
     *  Optimizes the Solr server
     */
    public void optimize() throws IOException, SolrServerException {
        solrServer.optimize(true, false); // Don't wait'
    }

    /**
     * Invokes <code>saveIndex(false)</code>
     */
    public void saveIndex() {
        saveIndex(false);
    }

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
            solrUpdate = null;
        } catch (Exception filex) {
            logger.error("Index failed during indexing", filex);
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
            //logDebug("Date object for PUBDATE: "+obj.toString());
            return (Date) obj;
        } else if (obj instanceof String) {
            //logDebug("String object for PUBDATE: "+obj.toString());
            return DateUtil.parseDate((String) obj);
        }
        return null;
    }

    /** */
    public static char getChar(SolrDocument solrDoc, String name){
        String result = getString(solrDoc, name);
        if (result == null){
            return 0;
        }
        if (result.isEmpty()){
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
     * A simple test for verifying that a SolrProxy can be created and initialized.
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
