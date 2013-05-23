package org.mitre.opensextant.data.tweetparser;

import org.mitre.opensextant.util.TextUtils;

public abstract class BaseTweetParser implements TweetParser {

	
    /** Remove line endings; Emoticons; what else?
     */
    protected String scrubText(String x) {
        String _new = TextUtils.fast_replace(x, "\n\r", " ");
        _new = TextUtils.remove_emoticons(_new);
        _new = TextUtils.remove_symbols(_new);
        return _new;
    }
    
    protected String scrubTag(String t) {
        return t.replace("#", "# ");
    }

    public static TweetParser getParser(ParserType type) {
    	switch (type) {
			case TW4J:
				return new TW4JParser();
			case GNIP:
				return new GnipParser();
			case SENTIMEDIR:
				return new SentimedirParser();
			default:
				return null;
		}
    }

}
