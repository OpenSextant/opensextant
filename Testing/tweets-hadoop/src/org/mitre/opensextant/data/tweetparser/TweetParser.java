package org.mitre.opensextant.data.tweetparser;

import java.text.ParseException;

import org.mitre.sentimedir.data.Tweet;

public interface TweetParser {

	public static enum ParserType {
		TW4J, GNIP, SENTIMEDIR
	}
	
	/**  Need references to current methodologies for what data is available, reliable, etc
     *  and where/when to use it.
     */
    public Tweet readTweet(String json) throws ParseException;
}
