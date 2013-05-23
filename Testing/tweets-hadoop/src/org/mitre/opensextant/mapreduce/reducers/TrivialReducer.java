package org.mitre.opensextant.mapreduce.reducers;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.mitre.geodesy.GeodeticCoord;
import org.mitre.opensextant.data.GeoTweetWritable;

public class TrivialReducer extends Reducer<Text, GeoTweetWritable, Text, Text> {

	public void reduce(Text key, Iterable<GeoTweetWritable> values, Context context) throws IOException, InterruptedException {
		for (GeoTweetWritable val : values) {
			context.write(key, val.toText());
		}
	}
	
}
