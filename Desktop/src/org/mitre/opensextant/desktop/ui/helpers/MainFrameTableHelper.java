package org.mitre.opensextant.desktop.ui.helpers;

import java.awt.Component;
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
	
	public void updateRowProgress(OSRow row, OSRow.STATUS status, int num) {
		
		row.setProgress(num, status);
		frame.getTable().repaint(row);
		
		// Swing ridiculously makes it incredibly difficult to change an
		// individual progress bar's color. May revisit this.
		/*
		 * UIDefaults overrides = UIManager.getDefaults();
		 * overrides.put("nimbusOrange", (Color.red));
		 * 
		 * instance.tableProgress.get(guiEntry).putClientProperty("Nimbus.Overrides"
		 * , overrides); instance.tableProgress.get(guiEntry).putClientProperty(
		 * "Nimbus.Overrides.InheritDefaults", false);
		 */

	}

        public static boolean confirmationPrompt( String msg, String title, Component parent) {
                Object[] options = {"Yes", "No"};
                int n = JOptionPane.showOptionDialog( parent
                                                     , msg
                                                     , title
                                                     , JOptionPane.YES_NO_OPTION
                                                     , JOptionPane.QUESTION_MESSAGE
                                                     , null
                                                     , options
                                                     , options[1]
                                                     );
                if(n == 0) return true; 
                else return false;
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
