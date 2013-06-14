package org.mitre.opensextant.desktop.ui.forms.panels;

import java.awt.Color;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.mitre.opensextant.desktop.ui.table.OSRow;

public class RowDurationImpl extends RowDuration {

    private long duration = 0;
    private boolean forceUpdate = false;

    public RowDurationImpl() {
        super();
    }

    public RowDurationImpl(long duration, String durationString) {
        super();
        this.duration = duration;
        this.durationLabel.setText(durationString);
        this.forceUpdate = true;
    }

    public void reset() {
        forceUpdate = false;
        duration = 0;
        durationLabel.setText(" --");
    }

    public void toggleColor(boolean isSelected) {
        if (isSelected)
            this.durationLabel.setForeground(Color.white);
        else
            this.durationLabel.setForeground(Color.black);
    }

    public long getDuration() {
        return this.duration;
    }

    public boolean updateDuration(OSRow row) {
        long updatedDuration = -1;
        long estimatedDuration = -1;
        if (row.isRunning()) {
            if (row.getExecutionStartTime() != null) {
                Date startTime = row.getExecutionStartTime();
                Date now = new Date();
                updatedDuration = now.getTime() - startTime.getTime();
                estimatedDuration = (long) (updatedDuration / (row.getPercent() / 100.0));
            }
        } else if (forceUpdate) {
            updatedDuration = this.duration;
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
                if (minutes > 0)
                    durationString += minutes + " min ";

                durationString += seconds + " sec";
                if (estimatedDuration > 0 && updatedDuration > 10 * 1000 && row.getPercent() > 1) {
                    long estMinutes = TimeUnit.MILLISECONDS.toMinutes(estimatedDuration);
                    long estSeconds = TimeUnit.MILLISECONDS.toSeconds(estimatedDuration) - TimeUnit.MINUTES.toSeconds(estMinutes);
                    if (row.getPercent() < 100) {
                        durationString += " of ";
                        if (estMinutes < 100 * 60) { // If it's a really large
                                                     // number the estimate
                                                     // is probably off
                            if (estMinutes > 0)
                                durationString += estMinutes + " min ";
                            durationString += estSeconds + " sec";
                        } else
                            durationString += "unknown";
                    }
                }

                durationLabel.setText(durationString);
                duration = updatedDuration;
            }
            return true;
        }
        return false;

    }

    public String getDurationString() {
        return durationLabel.getText();
    }

}
