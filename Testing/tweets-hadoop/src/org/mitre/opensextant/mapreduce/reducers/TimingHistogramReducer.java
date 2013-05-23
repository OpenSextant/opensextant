package org.mitre.opensextant.mapreduce.reducers;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.mitre.geodesy.GeodeticCoord;
import org.mitre.opensextant.data.GeoTweetWritable;

public class TimingHistogramReducer extends Reducer<LongWritable, IntWritable, LongWritable, IntWritable> {

	public void reduce(LongWritable key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
		int sum = 0;
		for (IntWritable count : values) {
			sum += count.get();
		}
		context.write(key, new IntWritable(sum));
	}

}
