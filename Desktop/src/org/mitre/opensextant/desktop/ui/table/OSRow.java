package org.mitre.opensextant.desktop.ui.table;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.mitre.opensextant.desktop.executor.opensextant.ext.OSDOpenSextantRunner;
import org.mitre.opensextant.desktop.ui.OpenSextantMainFrameImpl;
import org.mitre.opensextant.desktop.ui.forms.panels.RowButtonsImpl;
import org.mitre.opensextant.desktop.ui.forms.panels.RowDurationImpl;
import org.mitre.opensextant.desktop.ui.forms.panels.RowProgressBarImpl;
import org.mitre.opensextant.desktop.ui.helpers.ConfigHelper;
import org.mitre.opensextant.desktop.ui.helpers.MainFrameTableHelper;
import org.mitre.opensextant.processing.Parameters;
import org.mitre.xtext.XText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OSRow implements Comparable<OSRow> {

	public static enum STATUS {
		INITIALIZING("Initializing"), QUEUED("Queued"), PROCESSING("Processing"), COMPLETE("Complete"), CANCELED("Canceled"), ERROR("Error");

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
	private RowProgressBarImpl progressBarContainer;
	private RowButtonsImpl buttonContainer;
	private RowDurationImpl durationContainer;
	private String outputLocation;
	private File inputFile;
	private String outputType;
	private String baseOutputLocation;
	private List<OSRow> children = new ArrayList<OSRow>();
	private OSRow parent = null;
	private Timer timer = new Timer();

	private Future<?> executor;

	private MainFrameTableHelper tableHelper;

	private Date startTime;
	private Date executionStartTime;
	private Date endTime;

	private OSDOpenSextantRunner runner;

	public OSRow() {

	}

	public OSRow(String input, String baseOutputLocation, String outputType, MainFrameTableHelper tableHelper) {
		this(null, input, baseOutputLocation, outputType, tableHelper);
	}

	public OSRow(OSRow parent, String input, String baseOutputLocation, String outputType, MainFrameTableHelper tableHelper) {
		super();
		this.startTime = new Date();
		this.id = (new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss")).format(startTime) + "_" + ++counter;
		this.parent = parent;

		this.status = STATUS.QUEUED;
		this.baseOutputLocation = baseOutputLocation;
		this.outputType = outputType;
		this.inputFile = new File(input);
		this.progressBarContainer = new RowProgressBarImpl();
		this.durationContainer = new RowDurationImpl();
		this.buttonContainer = new RowButtonsImpl(this);
		this.tableHelper = tableHelper;

		if (inputFile.isDirectory()) {
			List<File> childInputFiles = new ArrayList<File>(FileUtils.listFiles(inputFile, XText.FILE_FILTER, true));
			for (File childInputFile : childInputFiles) {
				if (childInputFile.exists()) {
					// ignore files that start with '.'
					if (!childInputFile.getName().startsWith(".")) {
						children.add(new OSRow(this, childInputFile.getAbsolutePath(), baseOutputLocation, outputType, tableHelper));
					}
				} else {
					JOptionPane.showMessageDialog(tableHelper.getMainFrame(), "Could not find file: " + childInputFile.getAbsolutePath()
							+ ".", "Warning", JOptionPane.WARNING_MESSAGE);
				}
			}
		}

		this.title = inputFile.getAbsoluteFile().getName();

		this.updateOutputFileName();

		saveConfig();
	}

	public OSRow(String[] rowValues, MainFrameTableHelper tableHelper) {
		super();

		this.startTime = new Date(Long.parseLong(rowValues[6]));
		this.id = rowValues[0];

		String stat = rowValues[5];
		if ("COMPLETE".equals(stat))
			this.status = STATUS.COMPLETE;
		else if ("CANCELED".equals(stat))
			this.status = STATUS.CANCELED;
		else
			this.status = STATUS.ERROR;

		this.baseOutputLocation = rowValues[3];
		this.outputType = rowValues[4];
		this.inputFile = new File(rowValues[2]);
		this.title = rowValues[1];
		this.progressBarContainer = new RowProgressBarImpl();
		this.buttonContainer = new RowButtonsImpl(this);

		this.tableHelper = tableHelper;
	}

	private void saveConfig() {
		String[] rowValues = new String[7];
		rowValues[0] = this.id;
		rowValues[1] = this.title;
		rowValues[2] = this.inputFile.getAbsolutePath();
		rowValues[3] = this.baseOutputLocation;
		rowValues[4] = this.outputType;
		rowValues[5] = this.status.toString();
		rowValues[6] = "" + this.startTime.getTime();
		ConfigHelper.getInstance().updateRow(this.id, rowValues);
	}

	private void updateOutputFileName() {
		String dateStr = new SimpleDateFormat("_yyyyMMdd_hhmmss").format(this.startTime);
		Parameters p = new Parameters();
		String outputTypePrime = outputType;
		if ("KML".equals(outputType))
			outputTypePrime = "KMZ";
		p.setJobName(title + dateStr);
		this.outputLocation = baseOutputLocation + File.separator + p.getJobName();

		if ((new File(this.outputLocation)).exists())
			this.outputLocation += "(" + counter + ")" ;
		
		this.outputLocation += "." + outputTypePrime;
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

	public Date getStartTime() {
		return startTime;
	}

	public Date getEndTime() {
		return endTime;
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

	public void setProgress(int percent, OSRow.STATUS status, int childrenCompleted) {
		String percentString = ": " + percent + "%";
		if (hasChildren() && childrenCompleted >= 0) {
			percentString += " (" + childrenCompleted + "/" + getChildren().size() + ")";
		}
		if (percent < 0)
			percentString = "";


		if (this.status != STATUS.ERROR && this.status != STATUS.CANCELED) {
			this.percent = percent;
			
			if (this.status != STATUS.PROCESSING && status == STATUS.PROCESSING) {
				executionStartTime = new Date();
				class DurationUpdateTask extends TimerTask {
					public void run() {
						durationContainer.updateDuration(OSRow.this);
						tableHelper.getMainFrame().getTable().repaint(OSRow.this);
						if (!OSRow.this.isRunning())
							cancel();
					}
				}
				timer.schedule(new DurationUpdateTask(), 1000, 1000);

			}

			this.status = status;
			progressBarContainer.getProgressBar().setValue(percent);
			progressBarContainer.getProgressBar().setString(status.getTitle() + percentString);
		}
		

		if (!isRunning()) {

			endTime = new Date();
			JButton cancelDeleteButton = buttonContainer.getCancelDeleteButton();

			cancelDeleteButton.setToolTipText("Delete job from list");
			cancelDeleteButton.setIcon(OpenSextantMainFrameImpl.getIcon(OpenSextantMainFrameImpl.IconType.TRASH));

			if (status == STATUS.COMPLETE) {
				buttonContainer.getReRunButton().setEnabled(true);
				buttonContainer.getViewResultsButton().setEnabled(true);
			}
			saveConfig();

		}
		tableHelper.getMainFrame().getTable().repaint(this);
	}

	public void setProgress(int percent, OSRow.STATUS status) {
		setProgress(percent, status, -1);
	}

	@Override
	public int compareTo(OSRow other) {
		return title.compareTo(other.getTitle());
	}

	public RowProgressBarImpl getProgressBarPanel() {
		return progressBarContainer;
	}

	public RowDurationImpl getDurationPanel() {
		return durationContainer;
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

	public OSRow getChildForInputFile(File selection) {
		for (OSRow child : getChildren()) {
			if (child.getInputFile().getAbsolutePath().equals(selection.getAbsolutePath())) {
				return child;
			}
		}
		return null;
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

	public void cancelExecution(boolean showPrompt) {
		if (showPrompt && !MainFrameTableHelper.confirmationPrompt("Cancel running job?", "Confirm cancel", tableHelper.getMainFrame()))
			return;

		if (!isChild()) {
			executor.cancel(true);
			runner.cancelExecution();
			setProgress(-1, OSRow.STATUS.CANCELED);
			for (OSRow child : getChildren()) {
				child.setProgress(-1, OSRow.STATUS.CANCELED);
			}
		} else {
			// TODO: NEED TO IMPLEMENT CANCELING CHILD
			setProgress(-1, OSRow.STATUS.CANCELED);
			log.warn("NEED TO IMPLEMENT CANCELING CHILD");
		}
	}

	public void deleteFile() {
		try {
			File file = new File(this.getOutputLocation());
			file.delete();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	public void removeFromTable() {
		if (!MainFrameTableHelper.confirmationPrompt("Delete this job?", "Confirm delete", tableHelper.getMainFrame()))
			return;
		deleteFile();
		tableHelper.removeRow(this);
	}

	public void viewResults() {
		tableHelper.viewResults(this);
	}

	public void rerun() {
		JButton cancelDeleteButton = buttonContainer.getCancelDeleteButton();

		cancelDeleteButton.setToolTipText("Stop current execution");
		cancelDeleteButton.setIcon(OpenSextantMainFrameImpl.getIcon(OpenSextantMainFrameImpl.IconType.CANCEL));

		this.startTime = new Date();

		this.updateOutputFileName();
		this.deleteFile();
		
		this.executionStartTime = null;
		this.endTime = null;
		this.durationContainer.reset();

		this.setProgress(0, OSRow.STATUS.QUEUED);
		for (OSRow r : this.children) {
			r.setProgress(0, OSRow.STATUS.QUEUED);
			r.executionStartTime = null;
			r.endTime = null;
			r.durationContainer.reset();
			
		}
		
		buttonContainer.getReRunButton().setEnabled(false);
		buttonContainer.getViewResultsButton().setEnabled(false);

		tableHelper.getMainFrame().getApiHelper().reRun(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((baseOutputLocation == null) ? 0 : baseOutputLocation.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((inputFile == null) ? 0 : inputFile.hashCode());
		result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
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
		if (startTime == null) {
			if (other.startTime != null)
				return false;
		} else if (!startTime.equals(other.startTime))
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

	public Date getExecutionStartTime() {
		return executionStartTime;
	}

	public void setRunner(OSDOpenSextantRunner runner) {
		this.runner = runner;
	}

}
