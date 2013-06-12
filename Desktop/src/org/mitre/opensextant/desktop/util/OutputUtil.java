package org.mitre.opensextant.desktop.util;

import gate.Corpus;

import java.io.File;
import java.io.IOException;

import org.mitre.opensextant.apps.AppBase;
import org.mitre.opensextant.desktop.ui.table.OSRow;
import org.mitre.opensextant.processing.Parameters;
import org.mitre.opensextant.processing.ProcessingException;
import org.mitre.opensextant.processing.output.AbstractFormatter;
import org.mitre.opensextant.processing.output.MultiFormatter;

public class OutputUtil {

	public static AbstractFormatter createFormatter(OSRow row) throws IOException, ProcessingException {
		// params.inputFile = row.getInputFile().getAbsolutePath();
		// params.addOutputFormat(row.getOutputType());
		
		MultiFormatter formatter = new MultiFormatter();
		
		for (String outputType : row.getOutputTypes()) {

			String outputLocation = row.getOutputLocations().get(outputType);

			if ("SHAPEFILE".equals(outputType)) {
				outputLocation = outputLocation.substring(0, outputLocation.length() - 4);
			}

			File outputFile = new File(outputLocation);
			
			String filename = outputFile.getName();
			
			Parameters params = new Parameters();
	        params.isdefault = false;
	        params.inputFile = row.getInputFile().getAbsolutePath();
	        params.addOutputFormat(outputType);
	        params.outputDir = outputFile.getParent();
	        params.setJobName(filename);

	        AbstractFormatter childFormatter = AppBase.createFormatter(outputType, params);
			childFormatter.setOutputFilename(filename);
			childFormatter.start((String) params.getJobName());
			formatter.addChild(childFormatter);

		}
		
		return formatter;
	}

	public static synchronized void writeResults(AbstractFormatter formatter, Corpus corpus) throws Exception {
		formatter.formatResults(corpus);	
	}

}
