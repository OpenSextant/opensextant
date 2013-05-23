package org.mitre.opensextant.mapreduce.mappers;

import java.io.IOException;

import org.mitre.opensextant.data.GeoTweetWritable.Type;
import org.mitre.opensextant.processing.GeocodingResult;

public class UserTweetGeocodeMapper extends BaseTweetGeocodeMapper {

	@Override
	protected void outputTweetUserGeocode(GeocodingResult result, Context context) throws IOException, InterruptedException {
		 output(Type.USER, result, context);
	}

	@Override
	protected void outputTweetGeocode(GeocodingResult result, Context context) throws IOException, InterruptedException {
		 output(Type.BODY, result, context);
	}
	
	private void output(Type type, GeocodingResult result, Context context) throws IOException, InterruptedException {
		if (result == null) return;
		String author = (String)result.attributes.get("author");
		outputMapEntry(author, type, result, context);
	}


	
	
}
