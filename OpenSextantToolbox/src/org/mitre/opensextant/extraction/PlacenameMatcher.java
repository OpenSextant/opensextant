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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.mitre.opensextant.util.TextUtils;
import org.mitre.opensextant.placedata.Place;
import org.mitre.opensextant.placedata.PlaceCandidate;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connects to a Solr sever via HTTP and tags place names in document. The
 * <code>SOLR_HOME</code> environment variable must be set to the location of
 * the Solr server.
 *
 * @author David Smiley - dsmiley@mitre.org
 * @author Marc Ubaldino - ubaldino@mitre.org
 */
public class PlacenameMatcher {

    /**
     * Generic Solr Matcher stuff:
     */
    protected final static String requestHandler = "/tag";
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    protected final boolean debug = log.isDebugEnabled();
    /**
     * In the interest of optimization we made the Solr instance a static class
     * attribute that should be thread safe and shareable across instances of
     * SolrMatcher
     */
    protected static SolrParams params = null;
    protected static SolrProxy solr = null;
    /**
     * Gazetteer specific stuff:
     */
    private final String APRIORI_NAME_RULE = "AprioriNameBias";
    private SolrTaggerRequest tag_request = null;
    private Map<Integer, Place> beanMap = new HashMap<>(100); // initial size
    /**
     * In the interest of optimization we made the Solr instance a static class
     * attribute that should be thread safe and shareable across instances of
     * SolrMatcher
     */
    private MatchFilter filter = null;
    private boolean allow_lowercase_abbrev = false;

    /**
     *
     * @throws IOException
     */
    public PlacenameMatcher() throws IOException {
        PlacenameMatcher.initialize();

        filter = new MatchFilter("/filters/tagging-filters.txt");

        // Instance variable that will have the transient payload to tag
        // this is not thread safe and is not static:
        tag_request = new SolrTaggerRequest(params, SolrRequest.METHOD.POST);

        // Pre-loading the Solr FST
        // 
        try {
            tagText("trivial priming of the solr pump", "__initialization___");
        } catch (MatcherException initErr) {
            throw new IOException("Unable to prime the tagger", initErr);
        }
    }

    /**
     * allow_lowercase_abbrev is a flag that will allow us to tag "in" or "in."
     * as a possible abbreviation. By default such things are not abbreviations,
     * e.g., Indiana is typically IN or In. or Ind., for example. Oregon, OR or
     * Ore. etc.
     *
     * but almost never in or or for those cases.
     */
    public void setAllowLowerCaseAbbreviations(boolean b) {
        allow_lowercase_abbrev = b;
    }

    /**
     * Close solr resources.
     */
    public static void shutdown() {
        if (solr != null) {
            solr.close();
        }
    }

    /**
     */
    protected static void initialize() throws IOException {

        if (solr != null) {
            return;
        }

        // NOTE: This is set via opensextant.apps.Config or by some other means
        // But it is required to intialize.  "gazetteer" is the core name of interest.
        // Being explicit here about the core name allows integrator to field multiple cores 
        // in the same gazetteer.  
        // 
        String config_solr_home = System.getProperty("solr.solr.home");
        solr = new SolrProxy(config_solr_home, "gazetteer");

        ModifiableSolrParams _params = new ModifiableSolrParams();
        _params.set(CommonParams.QT, requestHandler);
        //request all fields in the Solr index
        // Do we need specific fields or *?  If faster use specific fields. TODO.
        //_params.set(CommonParams.FL, "*,score");
        // Note -- removed score for now, as we have not evaluated how score could be used in this sense.
        // Score depends on FST creation and other factors.
        // 
        // TODO: verify that all the right metadata is being retrieved here
        _params.set(CommonParams.FL, "id,name,cc,adm1,adm2,feat_class,feat_code,lat,lon,place_id,name_bias,id_bias,name_type");

        _params.set("tagsLimit", 100000);
        _params.set(CommonParams.ROWS, 100000);
        _params.set("subTags", false);
        _params.set("matchText", false);//we've got the input doc as a string instead

        /* Possible overlaps: ALL, NO_SUB, LONGEST_DOMINANT_RIGHT
         * See Solr Text Tagger documentation for details. 
         */
        _params.set("overlaps", "LONGEST_DOMINANT_RIGHT");
        //_params.set("overlaps", "NO_SUB");

        params = _params;
    }
    // 
    private int tagNamesTime = 0;
    private int getNamesTime = 0;
    private int totalTime = 0;

    /** Emphemeral metric for the current tagText() call.  Caller must get these numbers immediately after call.
     * @return time to tag
     */
    public int getTaggingNamesTime() {
        return tagNamesTime;
    }

    /**
     * @return time to get gazetteer records.
     */
    public int getRetrievingNamesTime() {
        return getNamesTime;
    }
    
    /**
     * @return time to get gazetteer records.
     */
    public int getTotalTime() {
        return totalTime;
    }
    

    /**
     * Tag a document, returning PlaceCandidates for the mentions in document.
     * Converts a GATE document to a string and passes it to the Solr server via
     * HTTP POST. The tokens and featureName parameters are not used.
     *
     * @param buffer
     * @param docid
     *
     * @return place_candidates List of place candidates
     * @throws MatcherException
     */
    public List<PlaceCandidate> tagText(String buffer, String docid) throws MatcherException {
        // "tagsCount":10, "tags":[{ "ids":[35], "endOffset":40, "startOffset":38},
        // { "ids":[750308, 2769912, 2770041, 10413973, 10417546], "endOffset":49,
        // "startOffset":41},
        // ...
        // "matchingDocs":{"numFound":75, "start":0, "docs":[ {
        // "place_id":"USGS1992921", "name":"Monterrey", "cc":"PR"}, {
        //"place_id":"USGS1991763", "name":"Monterrey", "cc":"PR"}, ]   

        long t0 = System.currentTimeMillis();
        if (debug) {
            log.debug("TEXT SIZE = " + buffer.length());
        }

        List<PlaceCandidate> candidates = new ArrayList<>();

        // Setup request to tag... 
        tag_request.input = buffer;
        QueryResponse response = null;
        try {
            response = tag_request.process(solr.getInternalSolrServer());
        } catch (Exception err) {
            throw new MatcherException("Failed to tag document=" + docid, err);
        }

        this.tagNamesTime = response.getQTime();
        
        // -- Process Solr Response

        //List<GeoBean> geoBeans = response.getBeans(GeoBean.class); maybe works but probably slow
        SolrDocumentList docList = (SolrDocumentList) response.getResponse().get("matchingDocs");

        long t1 = System.currentTimeMillis();
        beanMap.clear();
        String name = null;
        for (SolrDocument solrDoc : docList) {

            name = SolrProxy.getString(solrDoc, "name");
            if (filter.filterOut(name.toLowerCase())) {
                continue;
            }

            Place bean = new Place();

            bean.setName_type(SolrProxy.getChar(solrDoc, "name_type"));

            // Gazetteer place name & country:
            //   NOTE: this may be different than "matchtext" or PlaceCandidate.name field.
            // 
            bean.setPlaceName(name);
            bean.setCountryCode(SolrProxy.getString(solrDoc, "cc"));

            // Other metadata.
            bean.setAdmin1(SolrProxy.getString(solrDoc, "adm1"));
            bean.setAdmin2(SolrProxy.getString(solrDoc, "adm2"));
            bean.setFeatureClass(SolrProxy.getString(solrDoc, "feat_class"));
            bean.setFeatureCode(SolrProxy.getString(solrDoc, "feat_code"));
            bean.setLatitude(SolrProxy.getDouble(solrDoc, "lat"));
            bean.setLongitude(SolrProxy.getDouble(solrDoc, "lon"));

            bean.setPlaceID(SolrProxy.getString(solrDoc, "place_id"));
            bean.setName_bias(SolrProxy.getDouble(solrDoc, "name_bias"));
            bean.setId_bias(SolrProxy.getDouble(solrDoc, "id_bias"));

            // Hashed on "id"
            Integer id = (Integer) solrDoc.getFirstValue("id");
            beanMap.put(id, bean);
        }

        long t2 = System.currentTimeMillis();

        @SuppressWarnings("unchecked")
        List<NamedList<?>> tags = (List<NamedList<?>>) response.getResponse().get("tags");

        if (debug) {
            log.debug("DOC=" + docid + " TAGS SIZE = " + tags.size());
        }

        /*
         * Retrieve all offsets into a long list.  These offsets will report
         * a text span and all the gazetteer record IDs that are associated to that span.
         * The text could either be a name, a code or some other abbreviation.
         * 
         * For practical reasons the default behavior is to filter trivial spans given 
         * the gazetteer data that is returned for them.
         * 
         * WARNING: lots of optimizations occur here due to the potentially large volume of tags
         * and gazetteer data that is involved.  And this is relatively early in the pipline.
         * 
         */
        PlaceCandidate pc;
        Place Pgeo;
        int x1 = -1, x2 = -1;
        Set<String> seenPlaces = new HashSet<>();
        Double name_bias = 0.0;

        String matchText = null;
        for (NamedList<?> tag : tags) {
            x1 = (Integer) tag.get("startOffset");
            x2 = (Integer) tag.get("endOffset");//+1 char after last matched
            matchText = buffer.substring(x1, x2);

            /**
             * We can filter out trivial place name matches that we know to be
             * close to false positives 100% of the time. E.g,. "way", "back",
             * "north" You might consider two different stop filters, Is "North"
             * different than "north"? This first pass filter should really
             * filter out only text we know to be false positives regardless of
             * case.
             */
            if (filter.filterOut(matchText.toLowerCase())) {
                continue;
            }

            pc = new PlaceCandidate();
            pc.setStart(x1);
            pc.setEnd(x2);

            // Could have enabled the "matchText" option from the tagger to get
            // this, but since we already have the content as a String then
            // we might as well not make the tagger do any more work.
            pc.setPlaceName(matchText); //
            name_bias = 0.0;

            @SuppressWarnings("unchecked")
            List<Integer> placeRecordIds = (List<Integer>) tag.get("ids");
            //clear out places seen for the next candidate
            seenPlaces.clear();
            boolean _is_valid = true;
            boolean _is_lower = StringUtils.isAllLowerCase(pc.getText());

            for (Integer solrId : placeRecordIds) {
                Pgeo = beanMap.get(solrId);
                if (Pgeo == null) {
                    if (debug) {
                        log.debug("Logic error. Did not find place object for Solr ID=" + solrId);
                    }
                    continue;
                }

                // Optimization:  abbreviation filter.
                // 
                // Do not add PlaceCandidates for lower case tokens that are marked as Abbreviations
                // Unless flagged to do so.
                // DEFAULT behavior is to avoid lower case text that is tagged as an abbreviation in gazetteer,
                // 
                // Common terms:  in, or, oh, me, us, we, 
                //   etc.
                // Are all not typically place names or valid abbreviations in text.
                //                 
                if (!allow_lowercase_abbrev) {
                    if (Pgeo.isAbbreviation() && _is_lower) {
                        _is_valid = false;
                        if (debug) {
                            log.debug("Ignore lower case term=" + pc.getText());
                        }

                        break;
                    }

                }
                // Optimization: Add distinct place objects once. 
                //   don't add Place if null or already added to this instance of PlaceCandidate
                // 
                if (!seenPlaces.contains(Pgeo.getPlaceID())) {
                    pc.addPlace(Pgeo);
                    seenPlaces.add(Pgeo.getPlaceID());

                    // get max name bias
                    Double n_bias = Pgeo.getName_bias();
                    if (n_bias > name_bias) {
                        name_bias = n_bias;
                    }
                }

                // Indeed this does happen.
                // else { log.info("Does this ever happen -- ? " + pc.getText() + " " + Pgeo.getPlaceName()); }
            }

            /**
             * Some rule above triggered a flag that indicates this
             * token/place/name is not valid.
             *
             */
            if (!_is_valid) {
                continue;
            }

            // if the max name bias seen >0; add apriori evidence
            if (name_bias != 0.0) {
                pc.addRuleAndConfidence(APRIORI_NAME_RULE, name_bias);
            }

            candidates.add(pc);
        }
        long t3 = System.currentTimeMillis();

        if (debug) {
            summarizeExtraction(candidates, docid);
        }

        //this.tagNamesTime = (int)(t1 - t0);
        this.getNamesTime = (int)(t2 - t1);
        this.totalTime = (int)(t3 - t0);
        
        return candidates;
    }

    /**
     * Debugging
     */
    private void summarizeExtraction(List<PlaceCandidate> candidates, String docid) {
        if (candidates == null) {
            log.error("Something is very wrong.");
            return;
        }
        log.debug("DOC=" + docid + " PLACE CANDIDATES SIZE = " + candidates.size());
        Map<String, Integer> countries = new HashMap<>();

        // This loops through findings and reports out just Country names for now.
        for (PlaceCandidate candidate : candidates) {
            boolean _break = false;
            String namekey = TextUtils.normalize_text_entity(candidate.getText()); // .toLowerCase();
            namekey = namekey.toLowerCase();

            for (Place p : candidate.getPlaces()) {
                if (p.isAbbreviation()) {
                    log.debug("Ignore all abbreviations for now " + candidate.getText());
                    _break = true;
                    break;
                }
                if (p.isCountry()) {
                    Integer count = countries.get(namekey);
                    if (count == null) {
                        count = new Integer(1);
                        countries.put(namekey, count);
                    }
                    ++count;
                    countries.put(namekey, count);
                    _break = true;
                    break;
                }
            }
            if (_break) {
                continue;
            }
        }
        log.debug("Countries found:" + countries.toString());
    }

    /**
     * Do a basic test
     */
    public static void main(String[] args) throws Exception {
        //String solrHome = args[0];

        PlacenameMatcher sm = new PlacenameMatcher();

        try {
            String docContent = "I want to go to New York City some day.";

            System.out.println(docContent);

            List<PlaceCandidate> matches = sm.tagText(docContent, "main-test");

            for (PlaceCandidate pc : matches) {
                System.out.println(pc.toString());
            }

            sm.shutdown();
        } catch (Exception err) {
            err.printStackTrace();
        }
    }
}
