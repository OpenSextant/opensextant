package org.mitre.opensextant.desktop.executor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.mitre.opensextant.desktop.ui.OpenSextantMainFrameImpl;
import org.mitre.opensextant.desktop.ui.helpers.ConfigHelper;
import org.mitre.opensextant.desktop.ui.table.OSRow;
import org.mitre.opensextant.desktop.util.Initialize;
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
		row.setExecutor(executor.submit(new OpenSextantWorker(parent, row, addToTable)));
	}

	public int getThreadCount() {
		return executor.getMaximumPoolSize();
	}

	public void setThreadCount(int threadCount) {
		executor.setCorePoolSize(threadCount);
		executor.setMaximumPoolSize(threadCount);
	}

}