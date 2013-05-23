package org.mitre.opensextant.mapreduce.mappers;

import java.io.IOException;
import java.util.Date;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.mitre.opensextant.data.GeoTweetWritable;
import org.mitre.opensextant.data.GeoTweetWritable.Type;
import org.mitre.opensextant.data.tweetparser.BaseTweetParser;
import org.mitre.opensextant.data.tweetparser.TweetParser;
import org.mitre.opensextant.data.tweetparser.TweetParser.ParserType;
import org.mitre.opensextant.placedata.Place;
import org.mitre.opensextant.processing.Geocoding;
import org.mitre.opensextant.processing.GeocodingResult;
import org.mitre.opensextant.processing.ProcessingException;
import org.mitre.opensextant.util.TweetGeocoderUtil;
import org.mitre.sentimedir.data.Tweet;

public class BaseTweetGeocodeMapper extends Mapper<Object, Text, Text, GeoTweetWritable> {

	private static enum TweetCounter { NULL_TWEET, PRCESSED_TWEET }
	
	private TweetGeocoderUtil tweetUtil;
	private TweetParser tweetParser;

	protected static Logger log = Logger.getLogger(BaseTweetGeocodeMapper.class);
	
	@Override
	protected void setup(Context context) throws IOException, InterruptedException {
		super.setup(context);
		try {
			
			InputSplit split = context.getInputSplit();
			if (split.getClass() == FileSplit.class) {
				log.info("**** Processing split from: " + ((FileSplit)split).getPath());
			} else {
				log.info("**** SPLIT CLASS: " + split.getClass());
			}
			
			tweetUtil = new TweetGeocoderUtil(true);
			tweetUtil.getGeocoder().params.isdefault = false;
			tweetUtil.getGeocoder().params.setJobName("TweetMapRed");
			tweetUtil.getGeocoder().initialize();
			
			ParserType parserType = ParserType.valueOf(context.getConfiguration().get("parserType"));
			tweetParser = BaseTweetParser.getParser(parserType);
			
			Logger.getLogger(org.apache.solr.core.SolrCore.class).setLevel(Level.ERROR);
			
			log.info("Done with setup of BaseTweetGeocodeMapper");

		} catch (ProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
		try {
			Tweet tw = tweetParser.readTweet(value.toString());
			if (tw != null) {
				outputTweetGeocode(tweetUtil.geocodeTweet(tw), context);
				outputTweetUserGeocode(tweetUtil.geocodeTweetUser(tw), context);
				context.getCounter(TweetCounter.PRCESSED_TWEET).increment(1);
			} else {
				context.getCounter(TweetCounter.NULL_TWEET).increment(1);
			}
		} catch (Exception err) {
			err.printStackTrace();
		}

	}
	
	protected void outputTweetUserGeocode(GeocodingResult result, Context context) throws IOException, InterruptedException {
		outputMapEntry("user", Type.USER, result, context);
	}

	protected void outputTweetGeocode(GeocodingResult result, Context context) throws IOException, InterruptedException {
		outputMapEntry("result", Type.BODY, result, context);
	}

	protected void outputMapEntry(String key, Type type, GeocodingResult result, Context context) throws IOException, InterruptedException {
		if (result != null && result.geocodes != null) {
			for (Geocoding geo : result.geocodes) {
				GeoTweetWritable geoTweetWritable = getGeoTweetWritable(type, result, geo);
				if (geoTweetWritable != null) {
					context.write(new Text(key), geoTweetWritable);
				}
			}

		} else {
			System.out.println("tweetResult " + key + " is null");
		}
	}

	private GeoTweetWritable getGeoTweetWritable(Type type, GeocodingResult result, Geocoding geo) {
		Place place = geo.place;
		if (place != null) {
			
			String author = (String)result.attributes.get("author");
			String tweet = (String)result.attributes.get("tweet");
			Date timestamp = (Date)result.attributes.get("timestamp");
			String matchText = (geo.getText() != null) ? geo.getText() : " ";
			
			return new GeoTweetWritable(type, author, timestamp, tweet, matchText, place);
		}
		return null;
	}
}
