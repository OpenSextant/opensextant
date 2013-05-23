package org.mitre.opensextant;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.mitre.opensextant.data.GeoTweetWritable;
import org.mitre.opensextant.mapreduce.mappers.TimingOutlierTweetGeocodeMapper;
import org.mitre.opensextant.mapreduce.mappers.TimingTweetGeocodeMapper;
import org.mitre.opensextant.mapreduce.mappers.UserTweetGeocodeMapper;
import org.mitre.opensextant.mapreduce.reducers.KMeansReducer;
import org.mitre.opensextant.mapreduce.reducers.TimingHistogramReducer;
import org.mitre.opensextant.mapreduce.reducers.TimingOutlierReducer;

public class TweetOutlierTiming {

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		if (otherArgs.length != 4) {
			System.err.println("Usage: tweetOutlierTiming <in> <out> <format> <openSextantHome>");
			System.exit(2);
		}
		conf.set("mapred.child.java.opts", "-Xms2500m -Xmx2500m -Dopensextant.home=" + otherArgs[3]);
		conf.set("parserType", otherArgs[2]);

		conf.setBoolean("mapred.compress.map.output", true);
		conf.set("mapred.map.output.compression.codec","org.apache.hadoop.io.compress.SnappyCodec");		

		Job job = new Job(conf, "OpenSextant Timing Outlier Extraction");
		job.setJarByClass(TweetOutlierTiming.class);
		job.setMapperClass(TimingOutlierTweetGeocodeMapper.class);
		job.setReducerClass(TimingOutlierReducer.class);
		job.setMapOutputKeyClass(LongWritable.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputKeyClass(LongWritable.class);
		job.setOutputValueClass(Text.class);

//		job.setInputFormatClass(NLinesInputFormat.class);
//		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));

		job.setInputFormatClass(SequenceFileInputFormat.class);
		SequenceFileInputFormat.addInputPaths(job, otherArgs[0]);

		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);

		   
//		job.setNumReduceTasks(50);
//		job.setOutputFormatClass(SequenceFileOutputFormat.class);
//		SequenceFileOutputFormat.setOutputPath(job, new Path(args[1]));
//		SequenceFileOutputFormat.setCompressOutput(job, true);
//		SequenceFileOutputFormat.setOutputCompressionType(job, CompressionType.BLOCK);
//		job.getConfiguration().set("mapred.output.compression.codec","org.apache.hadoop.io.compress.SnappyCodec");

		
	}
}
