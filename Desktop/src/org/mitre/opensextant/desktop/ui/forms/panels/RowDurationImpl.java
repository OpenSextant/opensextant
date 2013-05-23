package org.mitre.opensextant.desktop.ui.forms.panels;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.mitre.opensextant.desktop.ui.table.OSRow;

public class RowDurationImpl extends RowDuration {

	private long duration = 0;
	
	public RowDurationImpl() {
		super();
	}


	public void updateDuration(OSRow row) {
		long updatedDuration = -1;
		long estimatedDuration = -1;
		if (row.isRunning()) {
			if (row.getExecutionStartTime() != null) {
				Date startTime = row.getExecutionStartTime();
				Date now = new Date();
				updatedDuration = now.getTime() - startTime.getTime();
				estimatedDuration = (long)(updatedDuration / (row.getPercent()/100.0)); 
			}
		} else {
			if (row.getExecutionStartTime() != null) {
				Date startTime = row.getExecutionStartTime();
				Date endTime = row.getEndTime();
				updatedDuration = endTime.getTime() - startTime.getTime();
			}
		}
		if (updatedDuration != duration) {
			if (updatedDuration < 0) {
				durationLabel.setText(" --");
			} else {
				long minutes = TimeUnit.MILLISECONDS.toMinutes(updatedDuration);
				long seconds = TimeUnit.MILLISECONDS.toSeconds(updatedDuration) - TimeUnit.MINUTES.toSeconds(minutes);
				
				String durationString = " ";
				if (minutes > 0) durationString += minutes + " min ";
				
				durationString += seconds + " sec";
				
				if (estimatedDuration > 0 && updatedDuration > 10*1000) {
					long estMinutes = TimeUnit.MILLISECONDS.toMinutes(estimatedDuration);
					long estSeconds = TimeUnit.MILLISECONDS.toSeconds(estimatedDuration) - TimeUnit.MINUTES.toSeconds(estMinutes);
					durationString += " of ";
					if (estMinutes > 0) durationString += estMinutes + " min ";
					durationString += estSeconds + " sec";
				}
				
				durationLabel.setText(durationString);
				duration = updatedDuration;
			}
		}

	}
	
	
}
