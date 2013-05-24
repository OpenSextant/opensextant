package org.mitre.opensextant.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.apache.hadoop.mrunit.types.Pair;
import org.junit.Before;
import org.junit.Test;
import org.mitre.opensextant.data.GeoTweetWritable;
import org.mitre.opensextant.data.GeoTweetWritable.Type;
import org.mitre.opensextant.data.tweetparser.TweetParser.ParserType;
import org.mitre.opensextant.mapreduce.mappers.BaseTweetGeocodeMapper;
import org.mitre.opensextant.mapreduce.mappers.UserTweetGeocodeMapper;
import org.mitre.opensextant.mapreduce.reducers.KMeansReducer;

public class MapReduceTest extends TestCase { 

	private MapDriver<Object, Text, Text, GeoTweetWritable> baseMapDriver;
	private MapDriver<Object, Text, Text, GeoTweetWritable> userMapDriver;
	private ReduceDriver<Text, GeoTweetWritable, Text, Text> kMeansReduceDriver;
	
	private String singleInputTw4j;
	private String singleInput;
	private String leInput;
	
	
    @Before  
    public void setUp() throws Exception {  
    	
		baseMapDriver = MapDriver.newMapDriver(new BaseTweetGeocodeMapper());
		userMapDriver = MapDriver.newMapDriver(new UserTweetGeocodeMapper());

		kMeansReduceDriver = ReduceDriver.newReduceDriver(new KMeansReducer());
		
		//MapReduceDriver<LongWritable, Text, Text, IntWritable, Text, IntWritable> mapReduceDriver;

		singleInputTw4j = FileUtils.readFileToString(new File("tweets-hadoop/test/data/single_tweet_tw4j.txt"));
		singleInput = FileUtils.readFileToString(new File("tweets-hadoop/test/data/single_tweet.txt"));
		leInput = FileUtils.readFileToString(new File("tweets-hadoop/test/data/le_tweet.txt"));

    }  
	
	
	@Test
	public void testBaseTweetGeocodeMapperTW4J() throws IOException {

		baseMapDriver.getContext().getConfiguration().set("parserType", ParserType.TW4J.name());
		baseMapDriver.withInput(new LongWritable(), new Text(singleInputTw4j));
	    String tweetText = "RT @barBaz: Went to boston for the day then traveled to providence.";
	    
	    baseMapDriver.addOutput(new Text("result"), new GeoTweetWritable(Type.BODY, "fooBar", new Date(1304307326000L), tweetText, 41.82399, -71.41283, "PROVIDENCE")); 
	    baseMapDriver.addOutput(new Text("result"), new GeoTweetWritable(Type.BODY, "fooBar", new Date(1304307326000L), tweetText, 42.35843, -71.05977, "BOSTON")); 
	    baseMapDriver.addOutput(new Text("user"), new GeoTweetWritable(Type.USER, "fooBar", new Date(1304307326000L), tweetText, 32.76415252685547, -117.15607452392578, "32.764154,-117.156077")); 

	    baseMapDriver.runTest();
	    

	}
	
	@Test
	public void testUserTweetGeocodeMapperTW4J() throws IOException {

		userMapDriver.getContext().getConfiguration().set("parserType", ParserType.TW4J.name());
		userMapDriver.withInput(new LongWritable(), new Text(singleInputTw4j));
	    String tweetText = "RT @barBaz: Went to boston for the day then traveled to providence.";
	    
	    userMapDriver.addOutput(new Text("fooBar"), new GeoTweetWritable(Type.BODY, "fooBar", new Date(1304307326000L), tweetText, 41.82399, -71.41283, "PROVIDENCE")); 
	    userMapDriver.addOutput(new Text("fooBar"), new GeoTweetWritable(Type.BODY, "fooBar", new Date(1304307326000L), tweetText, 42.35843, -71.05977, "BOSTON")); 
	    userMapDriver.addOutput(new Text("fooBar"), new GeoTweetWritable(Type.USER, "fooBar", new Date(1304307326000L), tweetText, 32.76415252685547, -117.15607452392578, "32.764154,-117.156077")); 

	    userMapDriver.runTest();

	}

	@Test
	public void testUserTweetGeocodeMapper() throws IOException {

		userMapDriver.getContext().getConfiguration().set("parserType", ParserType.GNIP.name());
		userMapDriver.withInput(new LongWritable(), new Text(singleInput));
	    String tweetText = "Went to boston for the day then traveled to providence.";
	    
	    userMapDriver.addOutput(new Text("fooBar"), new GeoTweetWritable(Type.BODY, "fooBar", new Date(1358641105000L), tweetText, 41.82399, -71.41283, "PROVIDENCE")); 
	    userMapDriver.addOutput(new Text("fooBar"), new GeoTweetWritable(Type.BODY, "fooBar", new Date(1358641105000L), tweetText, 42.35843, -71.05977, "BOSTON")); 
	    userMapDriver.addOutput(new Text("fooBar"), new GeoTweetWritable(Type.USER, "fooBar", new Date(1358641105000L), tweetText, 44.041985, -123.1186, " ")); 

	    userMapDriver.runTest();

//	    List<Pair<Text, GeoTweetWritable>> results = userMapDriver.run();
//	    System.out.println(results);

	}

	@Test
	public void testUserLeTweetGeocodeMapper() throws IOException {

		userMapDriver.getContext().getConfiguration().set("parserType", ParserType.GNIP.name());
		userMapDriver.withInput(new LongWritable(), new Text(leInput));

		System.out.println("Running LE tweet");
		Date start = new Date();
	    userMapDriver.run();
		Date end = new Date();
		System.out.println("Done! " + (end.getTime() - start.getTime()));

//	    List<Pair<Text, GeoTweetWritable>> results = userMapDriver.run();
//	    System.out.println(results);

	}
	@Test
	public void testKMeansReducer() throws IOException {

	    String tweetText = "RT @barBaz: Went to boston for the day then traveled to providence.";

		List<GeoTweetWritable> inputs = new ArrayList<GeoTweetWritable>();
		inputs.add(new GeoTweetWritable(Type.BODY, "fooBar", new Date(1304307326000L), tweetText, 42.35843, -71.05977, "B1.1"));
		inputs.add(new GeoTweetWritable(Type.BODY, "fooBar", new Date(1304307326000L), tweetText, 42.36843, -71.06977, "B1.2"));
		inputs.add(new GeoTweetWritable(Type.BODY, "fooBar", new Date(1304307326000L), tweetText, 42.33843, -71.04977, "B1.3"));
		inputs.add(new GeoTweetWritable(Type.BODY, "fooBar", new Date(1304307326000L), tweetText, 41.82399, -71.41283, "P1.1"));
		inputs.add(new GeoTweetWritable(Type.BODY, "fooBar", new Date(1304307326000L), tweetText, 41.84399, -71.43283, "P1.2"));
		inputs.add(new GeoTweetWritable(Type.BODY, "fooBar", new Date(1304307326000L), tweetText, 41.85399, -71.42283, "P1.3"));
		inputs.add(new GeoTweetWritable(Type.BODY, "fooBar", new Date(1304307326000L), tweetText, 41.82399, -71.43283, "P1.4"));
		inputs.add(new GeoTweetWritable(Type.BODY, "fooBar", new Date(1304307326000L), tweetText, 41.81399, -71.44283, "P1.5"));
		inputs.add(new GeoTweetWritable(Type.USER, "fooBar", new Date(1304307326000L), tweetText, 32.76415252685547, -117.15607452392578, "32.764154,-117.156077"));
		
		
		kMeansReduceDriver.addInput(new Text("fooBar"), inputs);
		
		List<Pair<Text,Text>> values = kMeansReduceDriver.run();
		
		System.out.println(values);
	}

	@Test
	public void testKMeansReducerNoBodies() throws IOException {

	    String tweetText = "RT @barBaz: Went to boston for the day then traveled to providence.";

		List<GeoTweetWritable> inputs = new ArrayList<GeoTweetWritable>();
		inputs.add(new GeoTweetWritable(Type.USER, "fooBar", new Date(1304307326000L), tweetText, 32.76415252685547, -117.15607452392578, "32.764154,-117.156077"));
		
		
		kMeansReduceDriver.addInput(new Text("fooBar"), inputs);
		
		List<Pair<Text,Text>> values = kMeansReduceDriver.run();
		
		System.out.println(values);
	}

	@Test
	public void testKMeansReducerDistance() throws IOException {

	    String tweetText = "RT @barBaz: Went to boston for the day then traveled to providence.";

		List<GeoTweetWritable> inputs = new ArrayList<GeoTweetWritable>();
		inputs.add(new GeoTweetWritable(Type.BODY, "fooBar", new Date(1304307326000L), tweetText, 29.999999999999996, 70.0, "PAKISTAN"));
		inputs.add(new GeoTweetWritable(Type.BODY, "fooBar", new Date(1304307326000L), tweetText, 33.69, 73.05509999999998, "ISLAMABAD"));
		inputs.add(new GeoTweetWritable(Type.USER, "fooBar", new Date(1304307326000L), tweetText, 32.76415252685547, -117.15607452392578, "32.764154,-117.156077"));
		
		kMeansReduceDriver.addInput(new Text("fooBar"), inputs);
		
		List<Pair<Text,Text>> values = kMeansReduceDriver.run();
		
		System.out.println(values);
	}
	
}
