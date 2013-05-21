package org.mitre.opensextant.desktop.ui.table;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

import javax.swing.JButton;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.mitre.opensextant.desktop.ui.OpenSextantMainFrameImpl;
import org.mitre.opensextant.desktop.ui.forms.panels.RowButtonsImpl;
import org.mitre.opensextant.desktop.ui.forms.panels.RowProgressBarImpl;
import org.mitre.opensextant.desktop.ui.helpers.MainFrameTableHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	private static Logger log = LoggerFactory.getLogger(OSRow.class);

	
	private static int counter = 0;
	
	private String title;
	private String id;
	private STATUS status;
	private int percent;
	private Date lastRun;
	private RowProgressBarImpl progressBarContainer;
	private RowButtonsImpl buttonContainer;
	private String outputLocation;
	private File inputFile;
	private String outputType;
	private String baseOutputLocation;
	private List<OSRow> children = new ArrayList<OSRow>();
	private OSRow parent = null;

	private Future<?> executor;

	private MainFrameTableHelper tableHelper;

	public OSRow() {
		
	}

	public OSRow(String input, String baseOutputLocation, String outputType, MainFrameTableHelper tableHelper) {
		this(null, input, baseOutputLocation, outputType, tableHelper);
	}

	public OSRow(OSRow parent, String input, String baseOutputLocation, String outputType, MainFrameTableHelper tableHelper) {
		super();
		this.lastRun = new Date();
		this.id = (new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss")).format(lastRun) + "_" + ++counter;
		this.parent = parent;

		this.status = STATUS.INITIALIZING;
		this.baseOutputLocation = baseOutputLocation;
		this.outputType = outputType;
		this.inputFile = new File(input);
		this.progressBarContainer = new RowProgressBarImpl();
		this.tableHelper = tableHelper;
		
		String childrenCountString = "";
		if (inputFile.isDirectory()) {
			List<File> childInputFiles = new ArrayList<File>(FileUtils.listFiles(inputFile, null, true));
			for (File childInputFile : childInputFiles) {
				children.add(new OSRow(this, childInputFile.getAbsolutePath(), baseOutputLocation, outputType, tableHelper));
			}
			childrenCountString = " ("+children.size()+" children)";
		}
		
		// Put in the table gui table
		String outputTypePrime = outputType;
		if ("KML".equals(outputType))
			outputTypePrime = "KMZ";
		
		if (inputFile.getName().startsWith(".")) {
			this.title = inputFile.getName();
		} else {
			this.title = FilenameUtils.getBaseName(inputFile.getAbsolutePath());
		}
		this.title += childrenCountString;
		
		this.outputLocation = baseOutputLocation + File.separator + (title.replaceAll(" ", "_") + "_" + id) + "." + outputTypePrime;
		this.buttonContainer = new RowButtonsImpl(this);


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
	
	public boolean hasChildren() {
		return children.size() > 0;
	}
	public boolean isChild() {
		return parent != null;
	}
	public OSRow getParent() {
		return parent;
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

	public File getInputFile() {
		return inputFile;
	}

	public String getOutputType() {
		return outputType;
	}
	public String getId() {
		return id;
	}
	
	public List<OSRow> getChildren() {
		return children;
	}

	public void setExecutor(Future<?> future) {
		this.executor = future;
		
	}

	public boolean isRunning() {
		return !(getStatus() == STATUS.COMPLETE || getStatus() == STATUS.CANCELED || getStatus() == STATUS.ERROR);
	}

	public OSRow duplicate() {
		return new OSRow(inputFile.getAbsolutePath(), baseOutputLocation, outputType, tableHelper);
	}

	public void cancelExecution() {
		if (!isChild()) {
			executor.cancel(true);
			setProgress(-1, OSRow.STATUS.CANCELED);
		} else {
			// TODO: NEED TO IMPLEMENT CANCELING CHILD
			setProgress(-1, OSRow.STATUS.CANCELED);
			log.warn("NEED TO IMPLEMENT CANCELING CHILD");
		}
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((baseOutputLocation == null) ? 0 : baseOutputLocation.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((inputFile == null) ? 0 : inputFile.hashCode());
		result = prime * result + ((lastRun == null) ? 0 : lastRun.hashCode());
		result = prime * result + ((outputLocation == null) ? 0 : outputLocation.hashCode());
		result = prime * result + ((outputType == null) ? 0 : outputType.hashCode());
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
		if (baseOutputLocation == null) {
			if (other.baseOutputLocation != null)
				return false;
		} else if (!baseOutputLocation.equals(other.baseOutputLocation))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
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
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}

}
