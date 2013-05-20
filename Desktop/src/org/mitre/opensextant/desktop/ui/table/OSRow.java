package org.mitre.opensextant.desktop.ui.table;

import java.util.Date;

import javax.swing.JButton;

import org.mitre.opensextant.desktop.ui.OpenSextantMainFrameImpl;
import org.mitre.opensextant.desktop.ui.forms.panels.RowButtonsImpl;
import org.mitre.opensextant.desktop.ui.forms.panels.RowProgressBarImpl;
import org.mitre.opensextant.desktop.ui.table.OSRow.STATUS;

public class OSRow implements Comparable<OSRow> {

	public static enum STATUS {
		INITIALIZING("Initializing"),
		PROCESSING("Processing"),
		COMPLETE("Complete"),
		CANCELED("Canceled"),
		ERROR("Error");
		
		private String title;
		private STATUS(String title) {
			this.title = title;
		}
		public String getTitle() {
			return title;
		}
	};
	
	private String title;
	private STATUS status;
	private int percent;
	private Date lastRun;
	private RowProgressBarImpl progressBarContainer;
	private RowButtonsImpl buttonContainer;
	private String outputLocation;
	private String inputFile;

	public OSRow() {

	}

	public OSRow(String title, STATUS status, String outputLocation, String inputFile) {
		super();
		this.title = title;
		this.status = status;
		this.outputLocation = outputLocation;
		this.inputFile = inputFile;
		this.progressBarContainer = new RowProgressBarImpl();
		this.buttonContainer = new RowButtonsImpl(this);
		this.lastRun = new Date();
	}


	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public STATUS getStatus() {
		return status;
	}

	public int getPercent() {
		return percent;
	}

	public void setProgress(int percent, OSRow.STATUS status) {
		this.percent = percent;
		this.status = status;
		progressBarContainer.getProgressBar().setValue(percent);
		progressBarContainer.getProgressBar().setString(status.getTitle() + ": " + percent + "%");
	
		if (status == STATUS.COMPLETE) {
			JButton cancelDeleteButton = buttonContainer.getCancelDeleteButton();
			
			cancelDeleteButton.setToolTipText("Delete job from list");
			cancelDeleteButton.setIcon(OpenSextantMainFrameImpl.getIcon(OpenSextantMainFrameImpl.IconType.TRASH));
			
			buttonContainer.getReRunButton().setEnabled(true);
			buttonContainer.getViewResultsButton().setEnabled(true);

		}
	}

	public Date getLastRun() {
		return lastRun;
	}
        
        public void setLastRun(Date lastRun) {
		this.lastRun = lastRun;
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

	public String getOutputLocation() {
		return outputLocation;
	}

	public String getInputFile() {
		return inputFile;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((inputFile == null) ? 0 : inputFile.hashCode());
		result = prime * result + ((lastRun == null) ? 0 : lastRun.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OSRow other = (OSRow) obj;
		if (inputFile == null) {
			if (other.inputFile != null)
				return false;
		} else if (!inputFile.equals(other.inputFile))
			return false;
		if (lastRun == null) {
			if (other.lastRun != null)
				return false;
		} else if (!lastRun.equals(other.lastRun))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}
	
	
	

}
