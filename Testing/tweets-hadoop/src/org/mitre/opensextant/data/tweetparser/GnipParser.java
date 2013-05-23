package org.mitre.opensextant.data.tweetparser;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.mitre.opensextant.data.Coordinate;
import org.mitre.sentimedir.data.Tweet;

import twitter4j.GeoLocation;
import twitter4j.Status;
import twitter4j.User;
import twitter4j.json.DataObjectFactory;

public class GnipParser extends BaseTweetParser {

	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
	
	
	@Override
	public Tweet readTweet(String json) throws ParseException {

        if (!json.startsWith("{\"id\":")) return null;

		Tweet tw = new Tweet();
        try {
            
            JSONObject twj = JSONObject.fromObject(json.trim());

            tw.id = twj.getString("id");
            tw.setBody(scrubText(scrubTag(twj.getString("body"))));
            
            
            tw.pub_date = format.parse(twj.getString("postedTime"));
            
            // Interesting objects
            // --------------------------
            
            
            JSONObject actor = twj.getJSONObject("actor");
            if (!actor.isNullObject()) {
                tw.author = actor.getString("preferredUsername");
                
                JSONObject location = actor.getJSONObject("location");
                if (!location.isNullObject()) {
                    tw.author_location = location.getString("displayName");
                }
            }
            
            JSONObject geoNode = twj.getJSONObject("geo");
			if (!geoNode.isNullObject() && geoNode.get("type") != null && geoNode.get("type").equals("Point")) {
				if (geoNode.has("coordinates")) {
					JSONArray coordinatesNode = geoNode.getJSONArray("coordinates");
					Double latitude = coordinatesNode.getDouble(0);
					Double longitude = coordinatesNode.getDouble(1);
					if (latitude != 0.0 && longitude != 0.0) {
		                tw.author_xy = new Coordinate(null);
		                tw.author_xy.setLatitude(latitude);
		                tw.author_xy.setLongitude(longitude);
					}
				}
			}
            
        } catch (Exception twerr) {
            throw new ParseException("Failed to parse Tweet " + twerr.getMessage() + "\n " + json, 0);
        }
        
        return tw;


	}

}
