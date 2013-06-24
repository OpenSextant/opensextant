/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mitre.opensextant.desktop.util;

import java.util.TimerTask;
import org.mitre.opensextant.desktop.ui.helpers.MainFrameTableHelper;
import org.mitre.opensextant.desktop.ui.table.OSRow;

/**
 *
 * @author GBLACK
 */
public class UpdateTask extends TimerTask {

    private OSRow row;
    private MainFrameTableHelper tableHelper;
    
    private float prevPercent = 0;
    private float diffPercent = 0;
    private float displayPercent = 0;
    private int repeatCount = 0;

    public UpdateTask(OSRow row, MainFrameTableHelper tableHelper) {
        super();
        this.row = row;
        this.tableHelper = tableHelper;
    }

    private float updatePercentView(boolean didChange, float lastDiffPercent, float curPercent, float curDisplayPercent, int sameCount) {
        float ret = curDisplayPercent;
        if (curPercent >= 100) {
            ret = 100;
        } else if (curPercent == curDisplayPercent) {
            ret = curDisplayPercent;
        } else if (didChange) {
            float logCalc = (float) Math.log((curPercent + 15) - lastDiffPercent);
            if (logCalc < 0.02) {
                logCalc = (float) 0.02;
            }
            ret += logCalc;
        } else {
            if (sameCount < 3) {
                sameCount = 3;
            }
            float logCalc = (float) Math.log(((curPercent + 15) - lastDiffPercent) / sameCount);
            if (logCalc < 0.02) {
                logCalc = (float) 0.02;
            }
            ret += logCalc;
        }
        if (ret > curPercent + 14) {
            ret = curPercent + 14;
        }
        if (ret >= 100) {
            ret = 100;
        }
        return ret;
    }

    public void run() {
        boolean updated = row.getDurationPanel().updateDuration(row);
        float percent = row.getPercent();
        int oldDisplay = (int) displayPercent;
        displayPercent = updatePercentView((prevPercent != percent), diffPercent, percent, displayPercent, repeatCount);
        if(oldDisplay != (int)displayPercent) updated = true;
        if (percent != prevPercent) {
            repeatCount = 0;
            diffPercent = prevPercent;
        } else {
            repeatCount++;
        }
        prevPercent = percent;

        String percentString = row.getStatus().getTitle() + ": " + (int)displayPercent + "%";
		if (row.hasChildren() && row.getNumCompletedChildren() >= 0) {
			percentString += " (" + row.getNumCompletedChildren() + "/" + row.getChildren().size() + ")";
		}
		if (percent < 0)
			percentString = "";

        row.getProgressBarPanel().getProgressBar().setValue((int) displayPercent);
        row.getProgressBarPanel().getProgressBar().setString(percentString);
        if (updated) {
            tableHelper.getMainFrame().getTable().repaint(row);
        }
        if (!row.isRunning()) {
            cancel();
        }
    }
}