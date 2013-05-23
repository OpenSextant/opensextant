package org.mitre.opensextant.desktop.executor;

import org.mitre.opensextant.desktop.executor.opensextant.ext.OSDOpenSextantRunner;
import org.mitre.opensextant.desktop.ui.OpenSextantMainFrameImpl;
import org.mitre.opensextant.desktop.ui.table.OSRow;

public class OpenSextantWorker implements Runnable {

	private OSRow row;
	private OpenSextantMainFrameImpl parent;

	public OpenSextantWorker(OpenSextantMainFrameImpl parent, OSRow row, boolean addToTable) {
		this.parent = parent;
                
		if(addToTable) this.row = parent.getTableHelper().addRow(row);
                else this.row = row;
        }

	@Override
	public void run() {
		
		try {
			
			row.setProgress(0, OSRow.STATUS.INITIALIZING, 0);
			
			// this can potentially be moved up into the executor, but currently you get an array index out of bounds exception if you re-use a runner.
			OSDOpenSextantRunner runner = new OSDOpenSextantRunner(row);
			runner.initialize();
			
			row.setProgress(0, OSRow.STATUS.PROCESSING, 0);
                        runner.runOpenSextant(row.getInputFile().getAbsolutePath(), row.getOutputType(), row.getOutputLocation());

			row.setProgress(100, OSRow.STATUS.COMPLETE);
			
			for (OSRow child : row.getChildren()) {
				// everything should be finished at this point... if anything did not finish, then there was an error with that file.
				if (child.isRunning()) {
					child.setProgress(-1, OSRow.STATUS.ERROR);
				}
			}
			
		} catch (InterruptedException ie) {
			row.setProgress(-1, OSRow.STATUS.CANCELED);
		} catch (Exception e) {
			row.setProgress(-1, OSRow.STATUS.ERROR);
			e.printStackTrace();
		}

	}

}