package org.mitre.opensextant.mapreduce.mappers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.Date;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;
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

public class TimingTweetGeocodeMapper extends Mapper<Object, Text, LongWritable, IntWritable> {

	private static enum TweetCounter {
		NULL_TWEET, PRCESSED_TWEET, FOUND_GEOCODES_USER, FOUND_GEOCODES_TWEET, FOUND_GEOCODES_BOTH
	}

	private TweetGeocoderUtil tweetUtil;
	private TweetParser tweetParser;
	private static IntWritable ONE = new IntWritable(1);

	protected static Logger log = Logger.getLogger(BaseTweetGeocodeMapper.class);

	@Override
	protected void setup(Context context) throws IOException, InterruptedException {
		super.setup(context);
		try {

			InputSplit split = context.getInputSplit();
			if (split.getClass() == FileSplit.class) {
				log.info("**** Processing split from: " + ((FileSplit) split).getPath());
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
		long start = 0;
		long end = 0;
		try {
			Tweet tw = tweetParser.readTweet(value.toString());
			if (tw != null) {
				start = (new Date()).getTime();
				GeocodingResult tweetResult = tweetUtil.geocodeTweet(tw);
				GeocodingResult userResult = tweetUtil.geocodeTweetUser(tw);
				end = (new Date()).getTime();
				
				if (hasData(tweetResult) || hasData(userResult)) {
					if (hasData(tweetResult)) {
						context.getCounter(TweetCounter.FOUND_GEOCODES_TWEET).increment(1);
					}
					if (hasData(userResult)) {
						context.getCounter(TweetCounter.FOUND_GEOCODES_USER).increment(1);
					}
					if (hasData(tweetResult) && hasData(userResult)) {
						context.getCounter(TweetCounter.FOUND_GEOCODES_BOTH).increment(1);
					}
				}
				context.write(new LongWritable(end - start), ONE);
				context.getCounter(TweetCounter.PRCESSED_TWEET).increment(1);
			} else {
				context.getCounter(TweetCounter.NULL_TWEET).increment(1);
			}
		} catch (Exception err) {
			err.printStackTrace();
		}
		if (end - start > 4000) throw new RuntimeException("Sym Failure: "+ (end - start));

	}
	
	private boolean hasData(GeocodingResult result) throws IOException, InterruptedException {
		if (result != null && result.geocodes != null) {
			if (result.geocodes.size() > 0) {
				return true;
			}
		}
		return false;
	}

}
