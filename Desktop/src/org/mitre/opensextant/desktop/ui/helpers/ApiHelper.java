/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mitre.opensextant.desktop.ui.helpers;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.mitre.opensextant.desktop.executor.OpenSextantExecutor;
import org.mitre.opensextant.desktop.ui.OpenSextantMainFrameImpl;
import org.mitre.opensextant.desktop.ui.forms.ConfigFrame;
import org.mitre.opensextant.desktop.ui.table.OSRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiHelper {
	public static final long HALF_GIGABYTE = 536870912L;

	// TODO: this probably should be set via some form of intelligence... memory, or cores
	private static final int MAXIMUM_THREADS = 2;

	private static Logger log = LoggerFactory.getLogger(ApiHelper.class);

	// Need to keep track of memory usage of the JVM
	private static Runtime runtime = Runtime.getRuntime();

	// Keep track of the number of raw text entries to avoid overwriting each
	private static int textCount = 0;

	private OpenSextantMainFrameImpl parent;
	private OpenSextantExecutor executor;
	
	public ApiHelper(OpenSextantMainFrameImpl parent) {
		this.parent = parent;
		try {
			this.executor = new OpenSextantExecutor(MAXIMUM_THREADS);
		} catch (Exception e) {
			log.error("Error setting up executor", e);
		}
	}

	public void processText(String text) {
		File file;
		try {
			file = File.createTempFile("textEntry" + (textCount++), ".txt");
			FileUtils.writeStringToFile(file, text);
			processFile(file.getAbsolutePath());
		} catch (IOException e) {
			log.error("Error processing text entry", e);
		}
	}

	public void processFile(String file) {
		
		String outType = ConfigFrame.getOutType();
		String baseOutputLocation = ConfigFrame.getOutLocation();
		
		OSRow row = new OSRow(file, baseOutputLocation, outType, parent.getTableHelper());
		
		executor.execute(parent, row);
	}

	public void reRun(OSRow row) {
		executor.execute(parent, row.duplicate());
	}
}
