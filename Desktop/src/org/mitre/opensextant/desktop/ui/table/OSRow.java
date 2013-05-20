package org.mitre.opensextant.desktop.ui.table;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Future;

import javax.swing.JButton;

import org.apache.commons.io.FilenameUtils;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.mitre.opensextant.desktop.executor.OpenSextantWorker;
import org.mitre.opensextant.desktop.ui.OpenSextantMainFrameImpl;
import org.mitre.opensextant.desktop.ui.forms.panels.RowButtonsImpl;
import org.mitre.opensextant.desktop.ui.forms.panels.RowProgressBarImpl;
import org.mitre.opensextant.desktop.ui.helpers.MainFrameTableHelper;
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
	
	private static int counter = 0;
	
	private String title;
	private String id;
	private STATUS status;
	private int percent;
	private Date lastRun;
	private RowProgressBarImpl progressBarContainer;
	private RowButtonsImpl buttonContainer;
	private String outputLocation;
	private String inputFile;
	private String outputType;
	private String baseOutputLocation;

	private Future<?> executor;

	private MainFrameTableHelper tableHelper;


	public OSRow() {

	}

	public OSRow(String inputFile, String baseOutputLocation, String outputType, MainFrameTableHelper tableHelper) {
		super();
		this.id = (new Date()).getTime() + "_" + ++counter;

		this.status = STATUS.INITIALIZING;
		this.baseOutputLocation = baseOutputLocation;
		this.outputType = outputType;
		this.inputFile = inputFile;
		this.progressBarContainer = new RowProgressBarImpl();
		this.tableHelper = tableHelper;
		this.buttonContainer = new RowButtonsImpl(this);
		this.lastRun = new Date();
		
		// Put in the table gui table
		String outputTypePrime = outputType;
		if ("KML".equals(outputType))
			outputTypePrime = "KMZ";
		
		this.title = FilenameUtils.getBaseName(inputFile).replaceAll(" ", "_");
		String dateStr = "_" + (new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss")).format(lastRun);
		this.outputLocation = baseOutputLocation + File.separator + (title + dateStr) + "." + outputTypePrime;


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
		String percentString = ": " + percent + "%";
		if (percent < 0) percentString = "";
		this.percent = percent;
		this.status = status;
		progressBarContainer.getProgressBar().setValue(percent);
		progressBarContainer.getProgressBar().setString(status.getTitle() + percentString);
	
		if (!isRunning()) {
			JButton cancelDeleteButton = buttonContainer.getCancelDeleteButton();
			
			cancelDeleteButton.setToolTipText("Delete job from list");
			cancelDeleteButton.setIcon(OpenSextantMainFrameImpl.getIcon(OpenSextantMainFrameImpl.IconType.TRASH));
			
			if (status == STATUS.COMPLETE) {
				buttonContainer.getReRunButton().setEnabled(true);
				buttonContainer.getViewResultsButton().setEnabled(true);
			}

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

	public String getOutputType() {
		return outputType;
	}
	public String getId() {
		return id;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((inputFile == null) ? 0 : inputFile.hashCode());
		result = prime * result + ((lastRun == null) ? 0 : lastRun.hashCode());
		result = prime * result + ((outputLocation == null) ? 0 : outputLocation.hashCode());
		result = prime * result + ((outputType == null) ? 0 : outputType.hashCode());
		result = prime * result + percent;
		result = prime * result + ((status == null) ? 0 : status.hashCode());
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
		if (outputLocation == null) {
			if (other.outputLocation != null)
				return false;
		} else if (!outputLocation.equals(other.outputLocation))
			return false;
		if (outputType == null) {
			if (other.outputType != null)
				return false;
		} else if (!outputType.equals(other.outputType))
			return false;
		if (percent != other.percent)
			return false;
		if (status != other.status)
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}

	public void setExecutor(Future<?> future) {
		this.executor = future;
		
	}

	public boolean isRunning() {
		return !(getStatus() == STATUS.COMPLETE || getStatus() == STATUS.CANCELED || getStatus() == STATUS.ERROR);
	}

	public OSRow duplicate() {
		return new OSRow(inputFile, baseOutputLocation, outputType, tableHelper);
	}

	public void cancelExecution() {
		executor.cancel(true);
		setProgress(-1, OSRow.STATUS.CANCELED);
	}

	public void removeFromTable() {
		tableHelper.removeRow(this);
	}

	public void viewResults() {
		tableHelper.viewResults(this);
	}

	public void rerun() {
		tableHelper.getMainFrame().getApiHelper().reRun(this);
	}

	

}
