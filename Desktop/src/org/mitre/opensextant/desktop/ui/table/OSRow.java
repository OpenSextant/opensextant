package org.mitre.opensextant.desktop.ui.table;

import java.util.Date;

import org.mitre.opensextant.desktop.ui.forms.panels.RowButtonsImpl;
import org.mitre.opensextant.desktop.ui.forms.panels.RowProgressBarImpl;

public class OSRow implements Comparable<OSRow> {

	private String title;
	private String status;
	private int percent;
	private Date lastRun;
	private RowProgressBarImpl progressBarContainer;
	private RowButtonsImpl buttonContainer;

	public OSRow() {

	}

	public OSRow(String title, String status) {
		super();
		this.title = title;
		this.status = status;
		this.progressBarContainer = new RowProgressBarImpl();
		this.buttonContainer = new RowButtonsImpl(this);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getStatus() {
		return status;
	}

	public int getPercent() {
		return percent;
	}

	public void setProgress(int percent, String status) {
		this.percent = percent;
		this.status = status;
		progressBarContainer.getProgressBar().setValue(percent);
		progressBarContainer.getProgressBar().setString(status + ": " + percent + "%");
	}

	public Date getLastRun() {
		return lastRun;
	}

	@Override
	public int compareTo(OSRow other) {
		return title.compareTo(other.getTitle());
	}

	public RowProgressBarImpl getProgressBarPanel() {
		return progressBarContainer;
	}

	public RowButtonsImpl getButtonPanel() {
		return buttonContainer;
	}

}
