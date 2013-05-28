package org.mitre.opensextant.desktop.executor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.mitre.opensextant.desktop.ui.helpers.ConfigHelper;


public class ThreadCountChangeListener implements PropertyChangeListener {
	
	private OpenSextantExecutor executor;

	public ThreadCountChangeListener(OpenSextantExecutor executor) {
		this.executor = executor;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (this.executor.getThreadCount() != ConfigHelper.getInstance().getNumThreads()) {
			this.executor.setThreadCount(ConfigHelper.getInstance().getNumThreads());
		}
	}

}
