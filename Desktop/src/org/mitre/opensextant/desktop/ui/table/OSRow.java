package org.mitre.opensextant.desktop.ui.table;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.mitre.opensextant.desktop.executor.OpenSextantWorker;
import org.mitre.opensextant.desktop.ui.OpenSextantMainFrameImpl;
import org.mitre.opensextant.desktop.ui.forms.panels.RowButtonsImpl;
import org.mitre.opensextant.desktop.ui.forms.panels.RowDurationImpl;
import org.mitre.opensextant.desktop.ui.forms.panels.RowProgressBarImpl;
import org.mitre.opensextant.desktop.ui.helpers.ConfigHelper;
import org.mitre.opensextant.desktop.ui.helpers.MainFrameTableHelper;
import org.mitre.opensextant.processing.Parameters;
import org.mitre.opensextant.processing.output.AbstractFormatter;
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

	private static String[] fileTypes;
	static {
		try {
			getAllowedFileTypes();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Error getting allowed file types, OpenSextant cannot continue.", "Error",
					JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}

	private String title;
	private String id;
	private STATUS status;
	private int percent;
	private RowProgressBarImpl progressBarContainer;
	private RowButtonsImpl buttonContainer;
	private RowDurationImpl durationContainer;
	private File inputFile;
	private List<String> outputTypes;
	private Map<String, String> outputLocations = new HashMap<String, String>();
	private String baseOutputLocation;
	private List<OSRow> children = new ArrayList<OSRow>();
	private OSRow parent = null;

	private OpenSextantWorker worker;
	private MainFrameTableHelper tableHelper;

	private Date startTime;
	private Date executionStartTime;
	private Date endTime;

	private int numCompletedChildren = 0;

	private AbstractFormatter formatter;

    private String identitifiersOutputLocation;

	public OSRow() {

	}

	public OSRow(String input, String baseOutputLocation, List<String> outputTypes, MainFrameTableHelper tableHelper) {
		this(null, input, baseOutputLocation, outputTypes, tableHelper);
	}

	public OSRow(OSRow parent, String input, String baseOutputLocation, List<String> outputTypes, MainFrameTableHelper tableHelper) {
		super();
		this.startTime = new Date();
		this.id = (new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss")).format(startTime) + "_" + ++counter;
		this.parent = parent;

		this.status = STATUS.QUEUED;
		this.baseOutputLocation = baseOutputLocation;
		this.outputTypes = outputTypes;
		this.inputFile = new File(input);
		this.progressBarContainer = new RowProgressBarImpl();
		this.durationContainer = new RowDurationImpl();
		this.buttonContainer = new RowButtonsImpl(this);
		this.tableHelper = tableHelper;
		
		this.title = inputFile.getAbsoluteFile().getName();
		this.updateOutputFileName();

		if (inputFile.isDirectory()) {

			List<File> childInputFiles = new ArrayList<File>(FileUtils.listFiles(inputFile, fileTypes, true));
			for (File childInputFile : childInputFiles) {
				if (childInputFile.exists()) {
					// ignore files that start with '.'
					if (!childInputFile.getName().startsWith(".")) {
						children.add(new OSRow(this, childInputFile.getAbsolutePath(), baseOutputLocation, outputTypes, tableHelper));
					}
				} else {
					JOptionPane.showMessageDialog(tableHelper.getMainFrame(), "Could not find file: " + childInputFile.getAbsolutePath()
							+ ".", "Warning", JOptionPane.WARNING_MESSAGE);
				}
			}
		}

		saveConfig();
	}

	public void addChild(OSRow child) {
		children.add(child);
	}

	public OSRow(String[] rowValues, MainFrameTableHelper tableHelper, OSRow parent) {
		super();

		this.parent = parent;
		this.startTime = new Date(Long.parseLong(rowValues[ConfigHelper.ROW_START]));
		this.id = rowValues[ConfigHelper.ROW_ID];

		String stat = rowValues[ConfigHelper.ROW_STATUS];
		this.percent = 10;
		if ("COMPLETE".equals(stat)) {
			this.status = STATUS.COMPLETE;
			this.percent = 100;
		} else if ("CANCELED".equals(stat))
			this.status = STATUS.CANCELED;
		else
			this.status = STATUS.ERROR;

		long duration = 0;

		try {
			duration = Long.parseLong(rowValues[ConfigHelper.ROW_DURATION]);
		} catch (Exception e) {
			duration = 0;
		}
		this.durationContainer = new RowDurationImpl(duration, rowValues[ConfigHelper.ROW_DURATION_STRING]);

		this.baseOutputLocation = rowValues[ConfigHelper.ROW_BASELOC];
		String tmpRowTypes = rowValues[ConfigHelper.ROW_TYPES].replaceAll(":", ",");
		this.outputTypes = ConfigHelper.parseOutTypesString(tmpRowTypes);
		this.inputFile = new File(rowValues[ConfigHelper.ROW_INPUT]);
		this.title = rowValues[ConfigHelper.ROW_TITLE];
		this.progressBarContainer = new RowProgressBarImpl();
		this.buttonContainer = new RowButtonsImpl(this);

		// this.setProgress(this.percent, this.status);
		this.tableHelper = tableHelper;
	}

	private static String[] getAllowedFileTypes() throws IOException {
		if (fileTypes == null) {
			XText converter = new XText();
			converter.setup();
			Set<String> fileTypeSet = converter.getFileTypes();
			fileTypes = fileTypeSet.toArray(new String[fileTypeSet.size()]);
		}
		return fileTypes;
	}

	private void saveConfig() {
		String[] rowValues = new String[ConfigHelper.ROW_PARENT + 1];
		rowValues[ConfigHelper.ROW_ID] = this.id;
		rowValues[ConfigHelper.ROW_TITLE] = this.title;
		rowValues[ConfigHelper.ROW_INPUT] = this.inputFile.getAbsolutePath();
		rowValues[ConfigHelper.ROW_BASELOC] = this.baseOutputLocation;
		rowValues[ConfigHelper.ROW_OUTPUT] = ""; // this.outputLocations;
		rowValues[ConfigHelper.ROW_DURATION] = "" + this.durationContainer.getDuration();
		rowValues[ConfigHelper.ROW_DURATION_STRING] = this.durationContainer.getDurationString();
		String tmpTypes = ConfigHelper.getOutTypesString(this.outputTypes);
		tmpTypes = tmpTypes.replaceAll(",", ":");
		rowValues[ConfigHelper.ROW_TYPES] = tmpTypes;
		rowValues[ConfigHelper.ROW_STATUS] = this.status.toString();
		rowValues[ConfigHelper.ROW_START] = "" + this.startTime.getTime();

		String childrenStr = "";
		for (OSRow r : this.getChildren()) {
			childrenStr += r + ":";
		}
		if (childrenStr.length() > 0)
			childrenStr = childrenStr.substring(0, childrenStr.length() - 1);
		rowValues[ConfigHelper.ROW_CHILDREN] = childrenStr;
		rowValues[ConfigHelper.ROW_PARENT] = "" + this.getParent();
		ConfigHelper.getInstance().updateRow(this.id, rowValues);
	}

	private void updateOutputFileName() {

		if (isChild()) {
			outputLocations = parent.outputLocations;
		} else {
			String dateStr = new SimpleDateFormat("_yyyyMMdd_hhmmss").format(this.startTime);
			Parameters p = new Parameters();
			p.setJobName(title + dateStr);

            String rootOutputLocation = baseOutputLocation + File.separator + p.getJobName();
            // if multiple files are processing at the same time the output
            // location
            // may not be there yet
            // if ((new File(this.outputLocation)).exists())
            rootOutputLocation += "_" + counter;
			for (String outputType : outputTypes) {
			    String outputLocation = rootOutputLocation;

				if ("KML".equals(outputType))
					outputLocation += ".kmz";
				else if ("SHAPEFILE".equals(outputType)) {
					outputLocation += "_shp";
				} else {
					outputLocation += "." + outputType.toLowerCase();
				}
				outputLocations.put(outputType, outputLocation);
			}
            identitifiersOutputLocation = rootOutputLocation + "_identifiers.xls";
		}

	}

	public String getInfo() {
		String info = "<html>Original file: " + this.inputFile.getAbsolutePath() + "<BR/>";

		for (int i = 0; i < outputTypes.size(); i++) {
			if (i > 0)
				info += "<BR/>";
			info += "Output file: " + this.outputLocations.get(outputTypes.get(i));
		}

		info += "</html>";
		return info;
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

	public void toggleDurationColor(boolean isSelected) {
		durationContainer.toggleColor(isSelected);
	}

	public void setProgress(int percent, OSRow.STATUS status, boolean force) {
		String percentString = ": " + percent + "%";
		if (hasChildren() && numCompletedChildren >= 0) {
			percentString += " (" + numCompletedChildren + "/" + getChildren().size() + ")";
		}
		if (percent < 0)
			percentString = "";

		if ((this.status != STATUS.ERROR && this.status != STATUS.CANCELED) || force) {
			this.percent = percent;

			if (this.status != STATUS.PROCESSING && status == STATUS.PROCESSING) {
				executionStartTime = new Date();
				class DurationUpdateTask extends TimerTask {
					public void run() {
						boolean updated = durationContainer.updateDuration(OSRow.this);
						if (updated) tableHelper.getMainFrame().getTable().repaint(OSRow.this);
						if (!OSRow.this.isRunning()) {
							cancel();
						}

					}
				}
				tableHelper.getTimer().schedule(new DurationUpdateTask(), 1000, 1000);
				getDurationPanel().updateDuration(this);
				tableHelper.getMainFrame().getTable().repaint(OSRow.this);
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

			buttonContainer.getReRunButton().setEnabled(true);
			if (this.status == STATUS.COMPLETE) {
				buttonContainer.getViewResultsButton().setEnabled(true);
				buttonContainer.getViewDirButton().setEnabled(true);
			}

			if (formatter != null) {
				log.info("closing formatter");
				formatter.finish();
				formatter = null;
			}
			worker = null;

			if (isChild())
				parent.incrementCompletedChildren();

                        this.durationContainer.updateDuration(this);
			saveConfig();

		}
		tableHelper.getMainFrame().getTable().repaint(this);
	}

	private synchronized void incrementCompletedChildren() {
		numCompletedChildren++;
		if (numCompletedChildren == children.size()) {
			setProgress(100, STATUS.COMPLETE);
		}
	}

	public void setProgress(int percent, OSRow.STATUS status) {
		setProgress(percent, status, false);
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

	public Map<String, String> getOutputLocations() {
		return outputLocations;
	}

    public String getIdentitifiersOutputLocation() {
        return identitifiersOutputLocation;
    }
    
	public File getInputFile() {
		return inputFile;
	}

	public List<String> getOutputTypes() {
		return outputTypes;
	}

	public void setOutputTypes(List<String> outTypes) {
		this.outputTypes = outTypes;
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

	public void setWorker(OpenSextantWorker worker) {
		this.worker = worker;

	}

	public boolean isRunning() {
		return !(getStatus() == STATUS.COMPLETE || getStatus() == STATUS.CANCELED || getStatus() == STATUS.ERROR);
	}

	public OSRow duplicate() {
		return new OSRow(inputFile.getAbsolutePath(), baseOutputLocation, outputTypes, tableHelper);
	}

	public void cancelExecution(boolean showPrompt) {
		if (showPrompt && !MainFrameTableHelper.confirmationPrompt("Cancel running job?", "Confirm cancel", tableHelper.getMainFrame()))
			return;

		if (hasChildren()) {
			for (OSRow child : children) {
				child.cancelExecution(false);
			}
			setProgress(-1, OSRow.STATUS.CANCELED);
		} else {
			if (worker != null)
				worker.cancelExecution();
			setProgress(-1, OSRow.STATUS.CANCELED);
		}
	}

	public void deleteFile() {
		try {
			for (String location : outputLocations.values()) {
				File file = new File(location);
				file.delete();
			}
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

	public void viewResults(String format) {
		tableHelper.viewResults(this, format);
	}
	
	public void viewIdentifiers() {
		tableHelper.viewIdentifiers(this);
	}


	public void viewDir() {
		tableHelper.viewDir(this);
	}

	public void rerun() {
		JButton cancelDeleteButton = buttonContainer.getCancelDeleteButton();

		cancelDeleteButton.setToolTipText("Stop current execution");
		cancelDeleteButton.setIcon(OpenSextantMainFrameImpl.getIcon(OpenSextantMainFrameImpl.IconType.CANCEL));

		// update output type
		this.setOutputTypes(ConfigHelper.getInstance().getOutTypes());

		this.startTime = new Date();
		this.numCompletedChildren = 0;

		this.updateOutputFileName();
		this.deleteFile();

		this.executionStartTime = null;
		this.endTime = null;
		this.durationContainer.reset();

		this.setProgress(0, OSRow.STATUS.QUEUED, true);
		for (OSRow r : this.children) {
			r.setProgress(0, OSRow.STATUS.QUEUED, true);
			r.executionStartTime = null;
			r.endTime = null;
			r.durationContainer.reset();
			r.updateOutputFileName();

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
		result = prime * result + ((outputLocations == null) ? 0 : outputLocations.hashCode());
		result = prime * result + ((outputTypes == null) ? 0 : outputTypes.hashCode());
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
		if (outputLocations == null) {
			if (other.outputLocations != null)
				return false;
		} else if (!outputLocations.equals(other.outputLocations))
			return false;
		if (outputTypes == null) {
			if (other.outputTypes != null)
				return false;
		} else if (!outputTypes.equals(other.outputTypes))
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

	public int completedChildCount() {
		int completed = 0;
		for (OSRow child : getChildren()) {
			if (!child.isRunning()) {
				completed++;
			}
		}
		return completed;
	}

	public void setOutputFormatter(AbstractFormatter formatter) {
		this.formatter = formatter;
	}

	public AbstractFormatter getOutputFormatter() {
		return formatter;
	}

	public String toString() {
		return getId();
	}

	public void updateProgress() {
            this.getProgressBarPanel().getProgressBar().setString(this.status.getTitle());
            this.setProgress(this.percent, this.status);
	}
	
	public OpenSextantWorker getWorker() {
		return this.worker;
	}

    public void addArchiveChild(String filePath) {
        OSRow child = new OSRow(this, filePath, baseOutputLocation, outputTypes, tableHelper);
        child.setWorker(worker);
        addChild(child);
    }
    
    public void rebuildRow() {
        tableHelper.removeRow(this);
        tableHelper.addRow(this);
    }

}
