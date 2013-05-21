package org.mitre.opensextant.desktop.ui.helpers;

import java.awt.Desktop;
import java.io.File;

import javax.swing.JOptionPane;

import org.mitre.opensextant.desktop.ui.OpenSextantMainFrameImpl;
import org.mitre.opensextant.desktop.ui.table.OSRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainFrameTableHelper {

	private static Logger log = LoggerFactory.getLogger(OpenSextantMainFrameImpl.class);
	private OpenSextantMainFrameImpl frame;

	public MainFrameTableHelper(OpenSextantMainFrameImpl frame) {
		this.frame = frame;
	}
	

	public OSRow addRow(OSRow row) {
		return frame.getTable().createRow(row);
	}

	public void removeRow(OSRow row) {
		frame.getTable().removeRow(row);
	}
	
	public void viewResults(OSRow row) {
		File file = new File(row.getOutputLocation());
		try {
			Desktop.getDesktop().open(file);
		} catch (Exception ex) {
			log.error(ex.getMessage());
			JOptionPane.showMessageDialog(frame, "Error opening file: " + row.getOutputLocation(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public OpenSextantMainFrameImpl getMainFrame() {
		return frame;
	}

}
