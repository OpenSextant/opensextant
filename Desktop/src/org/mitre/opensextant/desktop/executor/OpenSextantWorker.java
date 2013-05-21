package org.mitre.opensextant.desktop.executor;

import org.mitre.opensextant.apps.OpenSextantRunner;
import org.mitre.opensextant.desktop.ui.OpenSextantMainFrameImpl;
import org.mitre.opensextant.desktop.ui.table.OSRow;

public class OpenSextantWorker implements Runnable {

	private OSRow row;
	private OpenSextantMainFrameImpl parent;

	public OpenSextantWorker(OpenSextantMainFrameImpl parent, OSRow row) {
		this.parent = parent;
		this.row = parent.getTableHelper().addRow(row);
	}

	@Override
	public void run() {
		
		try {
			
			// this can potentially be moved up into the executor, but currently you get an array index out of bounds exception if you re-use a runner.
			OpenSextantRunner runner = new OpenSextantRunner();
			runner.initialize();
			
			parent.getTableHelper().updateRowProgress(row, OSRow.STATUS.PROCESSING, 0);

			runner.runOpenSextant(row.getInputFile().getAbsolutePath(), row.getOutputType(), row.getOutputLocation());

			parent.getTableHelper().updateRowProgress(row, OSRow.STATUS.COMPLETE, 100);
		} catch (InterruptedException ie) {
			parent.getTableHelper().updateRowProgress(row, OSRow.STATUS.CANCELED, -1);
		} catch (Exception e) {
			parent.getTableHelper().updateRowProgress(row, OSRow.STATUS.ERROR, -1);
			e.printStackTrace();
		}

	}

}