package org.mitre.opensextant.data.tweetparser;

import java.text.ParseException;

import org.mitre.opensextant.data.Coordinate;
import org.mitre.sentimedir.data.Tweet;

import twitter4j.GeoLocation;
import twitter4j.Status;
import twitter4j.User;
import twitter4j.json.DataObjectFactory;

public class TW4JParser  extends BaseTweetParser {

    /**  Need references to current methodologies for what data is available, reliable, etc
     *  and where/when to use it.
     */
    public Tweet readTweet(String json) throws ParseException {

		Tweet tw = new Tweet();

        try {
            Status apiTweet = DataObjectFactory.createStatus(json.trim());

            tw.id = Long.toString(apiTweet.getId());
            // tw.setBody(apiTweet.getText());
            tw.setBody(scrubText(scrubTag(apiTweet.getText())));
            tw.pub_date = apiTweet.getCreatedAt();

            // Interesting objects
            // --------------------------
            User u = apiTweet.getUser();
            tw.author = u.getScreenName();
            String uloc = u.getLocation();
            tw.author_location = uloc;
            //Status ustat = u.getStatus();
            GeoLocation gloc = apiTweet.getGeoLocation();
            if (gloc != null) {
                tw.author_xy = new Coordinate(null);
                tw.author_xy.setLatitude(gloc.getLatitude());
                tw.author_xy.setLongitude(gloc.getLongitude());
            }
            //log.info( "USER="+u.getScreenName() + " User Loc="+uloc + " status="+apiTweet.getText() + " LOC="+gloc);
        } catch (Exception twerr) {
            throw new ParseException("Failed to parse Tweet " + twerr.getMessage(), 0);
        }
        return tw;
    }

}
