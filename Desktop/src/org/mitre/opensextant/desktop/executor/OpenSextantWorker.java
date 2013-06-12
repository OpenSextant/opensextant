package org.mitre.opensextant.desktop.executor;

import gate.Corpus;
import gate.Document;
import gate.Factory;

import java.util.List;
import java.util.concurrent.Future;

import org.mitre.opensextant.desktop.executor.opensextant.ext.converter.XTextConverter;
import org.mitre.opensextant.desktop.executor.opensextant.ext.geocode.OSGeoCoder;
import org.mitre.opensextant.desktop.executor.progresslisteners.ChildProgressListener;
import org.mitre.opensextant.desktop.ui.helpers.ConfigHelper;
import org.mitre.opensextant.desktop.ui.table.OSRow;
import org.mitre.opensextant.desktop.util.OutputUtil;
import org.mitre.opensextant.processing.output.AbstractFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenSextantWorker implements Runnable {

	private static Logger log = LoggerFactory.getLogger(OSRow.class);

	private OSRow row;

	private boolean canceled;

	private Future<?> executor;

	public OpenSextantWorker(OSRow row) {
		this.row = row;
	}

	@Override
	public void run() {

		try {
			
			row.setProgress(0, OSRow.STATUS.INITIALIZING);

			XTextConverter converter = new XTextConverter(ConfigHelper.getInstance().getXTextCacheRoot());
			OSGeoCoder geoCoder = new OSGeoCoder();
			geoCoder.initialize();

			List<Document> contents = converter.convert(row.getInputFile());

			ChildProgressListener listener = new ChildProgressListener(row);
			geoCoder.addProgressListener(listener);

			row.setProgress(0, OSRow.STATUS.PROCESSING);
			
			for (int i = 0; i < contents.size() && !canceled; i++) {
				Document content = contents.get(i);
				Corpus corpus = geoCoder.geoCodeText(content);
				AbstractFormatter formatter = row.getOutputFormatter();
				if (row.isChild()) formatter = row.getParent().getOutputFormatter();
				OutputUtil.writeResults(formatter, corpus);
				Factory.deleteResource(corpus);
				Factory.deleteResource(content);
			}
			

			listener.processFinished();

			for (OSRow child : row.getChildren()) {
				// everything should be finished at this point... if anything
				// did not finish, then there was an error with that file.
				if (child.isRunning()) {
					child.setProgress(-1, OSRow.STATUS.ERROR);
				}
			}
			
			geoCoder.removeProgressListener(listener);
			geoCoder.shutdown();


//		} catch (InterruptedException ie) {
//			if (runner != null)
//				runner.cancelExecution();
//			row.setProgress(-1, OSRow.STATUS.CANCELED);
		} catch (Exception e) {
			log.error("error processing file", e);
			row.setProgress(-1, OSRow.STATUS.ERROR);
		}

	}
	
	public void cancelExecution() {
		if (executor != null) executor.cancel(true);
		this.canceled = true;
	}
	
	public void setExecutor(Future<?> executor) {
		this.executor = executor;
	}

}