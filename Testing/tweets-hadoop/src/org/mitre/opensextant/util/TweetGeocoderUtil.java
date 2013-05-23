package org.mitre.opensextant.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mitre.flexpat.TextMatch;
import org.mitre.flexpat.TextMatchResultSet;
import org.mitre.opensextant.apps.SimpleGeocoder;
import org.mitre.opensextant.processing.Geocoding;
import org.mitre.opensextant.processing.GeocodingResult;
import org.mitre.opensextant.processing.Parameters;
import org.mitre.opensextant.processing.ProcessingException;
import org.mitre.opensextant.processing.TextInput;
import org.mitre.opensextant.processing.output.GISDataFormatter;
import org.mitre.sentimedir.data.Tweet;
import org.mitre.xcoord.GeocoordMatch;
import org.mitre.xcoord.XConstants;
import org.mitre.xcoord.XCoord;
import org.mitre.xcoord.XCoordException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TweetGeocoderUtil {

    private final Logger log = LoggerFactory.getLogger(TweetGeocoderUtil.class);

    private GISDataFormatter tw_output;
    private GISDataFormatter user_output;
    private XCoord userlocX;
    private SimpleGeocoder geocoder = null;

    private Set<String> distinct_names = new HashSet<>();

    private boolean processStatusText;
    private final boolean debug = log.isDebugEnabled();

    private final static Set<String> tweet_stop = new HashSet<>();
    private final static Set<String> tweet_pass = new HashSet<>();

    static {

        // STOP terms -- these are not usually distinct places, if places at all.
        tweet_stop.add("lol");
        tweet_stop.add("pueblo");
        tweet_stop.add("avion");
        tweet_stop.add("santos");
        tweet_stop.add("santo");
        tweet_stop.add("este");
        tweet_stop.add("p��pa");
        tweet_stop.add("papa");
        tweet_stop.add("more");
        tweet_stop.add("nokia");


        // PASS terms -- these are places usually.
        tweet_pass.add("qom");
        tweet_pass.add("osh");
        
        // These can only be run once
        XCoord.RUNTIME_FLAGS ^= XConstants.DD_FILTERS_ON;  // Be less strict with Decimal degrees.
        XCoord.RUNTIME_FLAGS ^= XConstants.FLAG_EXTRACT_CONTEXT;  // ignore text context.

    }


    public TweetGeocoderUtil(boolean processStatusText) throws ProcessingException {
    	
    	this.processStatusText = processStatusText;
    	
    	
        // This is to bypass XCoord processing within GATE
        // Although we still want to use XCoord for some adhoc processing.        
        // This is not internal to XCoord.  It is internal to the PR that uses XCoord: GeocoordFinderPR
        Parameters.RUNTIME_FLAGS = Parameters.FLAG_NO_COORDINATES;
        Parameters.RUNTIME_FLAGS ^= Parameters.FLAG_EXTRACT_CONTEXT;

        userlocX = new XCoord();
        try {
            userlocX.configure(TweetGeocoderUtil.class.getResource("/tweet-xcoord.cfg"));
            userlocX.match_MGRS(false);
            userlocX.match_UTM(false);
            // Explicitly enable DD

            // Note -- for parsing coordinates in Tweet metadata 
            // we need to turn off the normal Decimal degree filters.
            //  Decimal degrees are really the only thing we want out of tweets,
            //  so we need to carefully undo DD filters.
            // 
            userlocX.match_DD(true);

        } catch (XCoordException xcerr) {
            throw new ProcessingException(xcerr);

        }

        geocoder = new SimpleGeocoder();


    }
    
    
    
    /** Batching happens inside -- each tweet is pushed onto batch 
     *  Every N tweets is geocoded.
     * 
     *  If user loc.xy:
     *    write out( xy )
     *  else if user loc
     *    geocode (user loc)
     *    write out ()
     * 
     *  geocode(status)
     *  write out ()
     */
    public GeocodingResult geocodeTweetUser(Tweet tw) {

        GeocodingResult geo = new GeocodingResult(tw.id);
        geo.addAttribute("timestamp", tw.pub_date);
        geo.addAttribute("author", tw.author);
        geo.addAttribute("tweet", tw.getBody());

        if (tw.author_xy != null) {

            // Geocoding object is the final data that is written to GISCore or other outputs, per the ResultsFormatter IF.
            Geocoding userLoc_geocoded = new Geocoding(tw.id, tw.author_xy.toString());

            GeocoordMatch xy = new GeocoordMatch();
            xy.latitude = tw.author_xy.getLatitude();
            xy.longitude = tw.author_xy.getLongitude();

            // results utility method --- note copy(Coordinate, Geocoding) ensures that the right metadata for a coordinate type is copied to Geocoding.
            GeocodingResult.copy(xy, userLoc_geocoded);
            userLoc_geocoded.method = "USER-GEO";
            // userLoc_geocoded.place.setPlaceName(tw.author_xy.toString());

            geo.geocodes.add(userLoc_geocoded);
            writeResult(geo, user_output);

        } else if (tw.author_location != null) {
            TextMatchResultSet userResults = userlocX.extract_coordinates(tw.author_location, tw.id);

            List<TextMatch> userLocation = userResults.matches;
            if (!userLocation.isEmpty()) {
                for (TextMatch m : userLocation) {
                    Geocoding userLoc_geocoded = new Geocoding(m.match_id, m.getText());
                    userLoc_geocoded.method = "USER-GEO-TEXT";

                    // results utility method --- note copy(Coordinate, Geocoding) ensures that the right metadata for a coordinate type is copied to Geocoding.
                    GeocodingResult.copy((GeocoordMatch) m, userLoc_geocoded);

                    geo.geocodes.add(userLoc_geocoded);
                }
                writeResult(geo, user_output);
            } else {
                try {
                    GeocodingResult res = geocoder.geocode(new TextInput(tw.id, tw.author_location.toUpperCase()));
                    if (!res.geocodes.isEmpty()) {
                        res.addAttribute("timestamp", tw.pub_date);
                        res.addAttribute("author", tw.author);
                        res.addAttribute("tweet", tw.getBody());
                        writeResult(res, user_output);
                        geo = res;
                    }
                } catch (Exception userErr) {
                    log.error("Geocoding error with Users?", userErr);
                }
            }
        } else {
        	geo = null;
        }
        return geo;
    }
    
    /**  If a tweet has a non-zero status text, let's find all places in the content.
     */
    public GeocodingResult geocodeTweet(Tweet tw) {
    	
    	GeocodingResult res = null;
    	
        if (processStatusText && tw.getBody() != null && !tw.getBody().isEmpty()) {
            try {
                // String buf = scrubText(scrubTag(tw.getBody()));
                res = geocoder.geocode(new TextInput(tw.id, tw.getBody().toUpperCase()));
                res.addAttribute("timestamp", tw.pub_date);
                res.addAttribute("tweet", tw.getBody());
                res.addAttribute("author", tw.author);
                enrichResults(res);
                writeResult(res, tw_output);
            } catch (Exception err) {
                log.error("Geocoding error?", err);
            }
        }
        return res;

    }
    
    private void writeResult(GeocodingResult result, GISDataFormatter formatter) {
    	if (formatter != null) {
    		formatter.writeGeocodingResult(result, result.recordID);
    	}
    }
    
    /**  Enrich and filter geocoding as needed.
     *   
     *   FILTER OUT from GIS output:
     *     + name or matchtext is a known stop word (non-place), 
     *     + short terms that are not countries 
     */
    private void enrichResults(GeocodingResult res /*, Tweet tw*/) {
        distinct_names.clear();
        for (Geocoding g : res.geocodes) {

            String norm = g.getText().toLowerCase();
            
            // Filter out duplicates
            if (distinct_names.contains(norm)){
                g.filtered_out = true;
            } else {
                // Track distinct names
                distinct_names.add(norm);
            }

            if (tweet_stop.contains(norm)) {
                g.filtered_out = true;
                if (debug) {
                    log.debug("Filter out:" + norm);
                }
            } else if (tweet_pass.contains(norm) || !TextUtils.isASCII(g.getText().getBytes())) {
                // DO Nothing.
                //
            } else if (norm.length() < 4 && !(g.is_country || g.is_administrative)) {
                g.filtered_out = true;
                if (debug) {
                    log.info("Filter out short term:" + norm);
                }
            }
        }
    }
    
    public void shutdown() {
        // Close connections and save your output.
        if (geocoder != null) {
            geocoder.shutdown();
        }
        if (tw_output != null) {
            tw_output.finish();
        }
        if (user_output != null) {
            user_output.finish();
        }
    }


    public SimpleGeocoder getGeocoder() {
    	return geocoder;
    }
    
    public void setOutputFormats(GISDataFormatter tw_output, GISDataFormatter user_output) {
    	this.tw_output = tw_output;
    	this.user_output = user_output;
    }

}
