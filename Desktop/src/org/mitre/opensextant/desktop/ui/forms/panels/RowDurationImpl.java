package org.mitre.opensextant.desktop.ui.forms.panels;

import java.util.Date;

import org.mitre.opensextant.desktop.ui.table.OSRow;

public class RowDurationImpl extends RowDuration {

	private long duration = 0;
	
	public RowDurationImpl() {
		super();
	}


	public void updateDuration(OSRow row) {
		long updatedDuration = 0;
		if (row.isRunning()) {
			Date startTime = row.getStartTime();
			Date now = new Date();
			updatedDuration = now.getTime() - startTime.getTime();
		} else {
			Date startTime = row.getStartTime();
			Date endTime = row.getEndTime();
			updatedDuration = endTime.getTime() - startTime.getTime();
		}
		if (updatedDuration != duration) {
			durationLabel.setText(duration+"");
			duration = updatedDuration;
		}

	}
	
	
}
