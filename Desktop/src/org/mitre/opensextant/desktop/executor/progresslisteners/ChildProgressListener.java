package org.mitre.opensextant.desktop.executor.progresslisteners;

import gate.event.ProgressListener;

import org.mitre.opensextant.desktop.ui.table.OSRow;

public class ChildProgressListener implements ProgressListener {

	private OSRow row;

	public ChildProgressListener(OSRow row) {
		this.row = row;
	}
	
	@Override
	public void progressChanged(int progress) {
		int previousProgress = row.getPercent();
		int calculatedProgress = progress;
		if (row.getPercent() != calculatedProgress) {
			row.setProgress(calculatedProgress, OSRow.STATUS.PROCESSING);
			if (row.isChild()) updateParentProgress(row.getParent(), calculatedProgress, previousProgress);
		}
	}
	
	public synchronized static void updateParentProgress(OSRow parent, int childProgress, int previousChildProgress) {
		int numChildren = parent.getChildren().size();
		int calculatedProgress = parent.getPercent() - previousChildProgress/numChildren;
		calculatedProgress += childProgress/numChildren;
		if (parent.getPercent() != calculatedProgress) {
			parent.setProgress(calculatedProgress, OSRow.STATUS.PROCESSING);
		}
	}


	@Override
	public void processFinished() {
		int previousProgress = row.getPercent();
		if (row.isChild()) updateParentProgress(row.getParent(), 100, previousProgress);
		row.setProgress(100, OSRow.STATUS.COMPLETE);
	}

}