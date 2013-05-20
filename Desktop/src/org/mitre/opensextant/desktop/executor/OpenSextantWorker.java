package org.mitre.opensextant.desktop.executor;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FilenameUtils;
import org.mitre.opensextant.apps.OpenSextantRunner;
import org.mitre.opensextant.desktop.ui.OpenSextantMainFrameImpl;
import org.mitre.opensextant.desktop.ui.helpers.ApiHelper;
import org.mitre.opensextant.desktop.ui.table.OSRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenSextantWorker implements Runnable {

	public static final int NOT_ON_LIST = -1;
	private static Logger log = LoggerFactory.getLogger(ApiHelper.class);

	private String inputFile;
	private String outputType;
	private String outputLocation;
	private OpenSextantMainFrameImpl parent;
	// Keep track of the ordering on the gui list
	private int guiEntry = NOT_ON_LIST;
	private String dateStr;

	public OpenSextantWorker(OpenSextantMainFrameImpl parent, String inputFile, String outputType, String outputLocation) {
		this.inputFile = inputFile;
		this.outputType = outputType;
		this.outputLocation = outputLocation;
		this.parent = parent;
		dateStr = "_" + (new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss")).format(new Date());
		addTableRow();
	}
	
	private void addTableRow() {
		String outName = FilenameUtils.getBaseName(inputFile);

		// Put in the table gui table
		String outputTypePrime = outputType;
		if ("KML".equals(outputType))
			outputTypePrime = "KMZ";
		
		String linkLocation = outputLocation + File.separator + (outName + dateStr) + "." + outputTypePrime;

		String inType = "FILE";
		guiEntry = parent.getTableHelper().addRow(linkLocation, outName, inType, inputFile);

	}

	@Override
	public void run() {
		
		String outName = FilenameUtils.getBaseName(inputFile);

		try {
			
			// this can potentially be moved up into the executor, but currently you get an array index out of bounds exception if you re-use a runner.
			OpenSextantRunner runner = new OpenSextantRunner();
			runner.initialize();
			
			parent.getTableHelper().updateRowProgress(guiEntry, OSRow.STATUS.PROCESSING, 0);
			
			runner.runOpenSextant(inputFile, outputType, outputLocation + File.separator + outName+dateStr);

		} catch (Exception e) {
			parent.getTableHelper().updateRowProgress(guiEntry, OSRow.STATUS.ERROR, 0);
			e.printStackTrace();
		}

		parent.getTableHelper().updateRowProgress(guiEntry, OSRow.STATUS.COMPLETE, 100);
	}

}