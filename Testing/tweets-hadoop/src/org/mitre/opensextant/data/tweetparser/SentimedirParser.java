package org.mitre.opensextant.data.tweetparser;

import java.text.ParseException;

import net.sf.json.JSONObject;

import org.mitre.sentimedir.data.Tweet;

public class SentimedirParser extends BaseTweetParser {

    /**  Need references to current methodologies for what data is available, reliable, etc
     *  and where/when to use it.
     */
    public Tweet readTweet(String json) throws ParseException {
		Tweet tw = new Tweet();

        try {
            JSONObject twj = JSONObject.fromObject(json.trim());
            tw.fromJSON(twj);

            // RESET using a cleaned up status text
            tw.setBody(scrubText(scrubTag(tw.getBody())));
        } catch (Exception twerr) {
            throw new ParseException("Failed to parse Tweet " + twerr.getMessage(), 0);
        }
        return tw;
    }


}
