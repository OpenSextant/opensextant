package org.mitre.opensextant.desktop.executor;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.mitre.opensextant.desktop.ui.OpenSextantMainFrameImpl;
import org.mitre.opensextant.desktop.ui.helpers.ConfigHelper;
import org.mitre.opensextant.desktop.ui.table.OSRow;
import org.mitre.opensextant.desktop.ui.table.OSRow.STATUS;
import org.mitre.opensextant.desktop.util.Initialize;
import org.mitre.opensextant.desktop.util.OutputUtil;
import org.mitre.opensextant.processing.ProcessingException;
import org.mitre.opensextant.processing.output.AbstractFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenSextantExecutor {

	private static Logger log = LoggerFactory.getLogger(OpenSextantExecutor.class);

	private final PausableThreadPoolExecutor executor;

	public OpenSextantExecutor(int threads) throws Exception{
		super();
		BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
		executor = new PausableThreadPoolExecutor(threads, threads, 0L, TimeUnit.MILLISECONDS, queue);
		executor.pause();

		(new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (!Initialize.getInitialized()) {
						Thread.sleep(500);
					}
					executor.resume();
				} catch (Exception e) {
					log.error("Error initializing Open Sextant Runner", e);
				}
			}
		})).start();
		
		ConfigHelper.getInstance().addUpdateListener(new ThreadCountChangeListener(this));
	}
	
	public void execute(OpenSextantMainFrameImpl parent, OSRow row, boolean addToTable) {
		if (addToTable) parent.getTableHelper().addRow(row);

		AbstractFormatter formatter = null;
		try {
			formatter = OutputUtil.createFormatter(row);
	        row.setOutputFormatter(formatter);

			if (row.hasChildren()) {
				row.setProgress(0, STATUS.PROCESSING);
				for (OSRow child : row.getChildren()) {
					OpenSextantWorker worker = new OpenSextantWorker(child);
					worker.setExecutor(executor.submit(worker));
					child.setWorker(worker);
				}
			} else {
				OpenSextantWorker worker = new OpenSextantWorker(row);
				worker.setExecutor(executor.submit(worker));
				row.setWorker(worker);
			}

	        
		} catch (ProcessingException e) {
			log.error("error scheduling job", e);
			row.setProgress(-1, STATUS.ERROR);
		} catch (IOException e) {
			log.error("error scheduling job", e);
			row.setProgress(-1, STATUS.ERROR);
		} 
	}

	public int getThreadCount() {
		return executor.getMaximumPoolSize();
	}

	public void setThreadCount(int threadCount) {
		executor.setCorePoolSize(threadCount);
		executor.setMaximumPoolSize(threadCount);
	}

}