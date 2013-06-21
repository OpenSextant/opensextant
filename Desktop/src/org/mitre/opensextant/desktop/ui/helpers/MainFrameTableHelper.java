package org.mitre.opensextant.desktop.ui.helpers;

import java.awt.Component;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.Timer;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.mitre.opensextant.desktop.ui.OpenSextantMainFrameImpl;
import org.mitre.opensextant.desktop.ui.forms.StatisticsFrame;
import org.mitre.opensextant.desktop.ui.table.OSRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainFrameTableHelper {

    private static Logger log = LoggerFactory.getLogger(OpenSextantMainFrameImpl.class);
    private OpenSextantMainFrameImpl frame;
    private Timer timer = new Timer(true);

    public MainFrameTableHelper(OpenSextantMainFrameImpl frame) {
        this.frame = frame;
    }

    public static boolean confirmationPrompt(String msg, String title, Component parent) {
        Object[] options = {"Yes", "No"};
        int n = JOptionPane.showOptionDialog(parent, msg, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
        if (n == 0) {
            return true;
        } else {
            return false;
        }
    }

    public void addRow(OSRow row) {
        frame.getTable().createRow(row);
        row.updateProgress();
        for(OSRow c : row.getChildren()) c.updateProgress();
    }

    public void removeRow(OSRow row) {
        frame.getTable().removeRow(row);
        ConfigHelper.getInstance().saveSettings();
    }

    public void viewStatistics(OSRow row) {
        JFrame.setDefaultLookAndFeelDecorated(true);
        JFrame frame = new StatisticsFrame(row);
        frame.setVisible(true); 
    }
    
    public void viewIdentifiers(OSRow row) {
        try {
            Desktop.getDesktop().open(new File(row.getIdentitifiersOutputLocation()));
        } catch (IOException e) {
            log.error(e.getMessage());
            JOptionPane.showMessageDialog(frame, "Error opening file: " + row.getIdentitifiersOutputLocation() + "\nYou may need to associate the file type with an application in your operating system.  Opening parent directory.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void viewResults(OSRow row, String format) {
    	String outputLocation = row.getOutputLocations().get(format);
        File file = new File(outputLocation);
        try {
            Desktop.getDesktop().open(file);
        } catch (IOException e) {
            log.error(e.getMessage());
            JOptionPane.showMessageDialog(frame, "Error opening file: " + outputLocation + "\nYou may need to associate the file type with an application in your operating system.  Opening parent directory.", "Error", JOptionPane.ERROR_MESSAGE);
            try {
                Desktop.getDesktop().open(file.getParentFile());
            } catch (IOException e1) {
                JOptionPane.showMessageDialog(frame, "Error opening parent directory: " + file.getParentFile().getAbsolutePath() + ". Check the permissions of this directory.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            JOptionPane.showMessageDialog(frame, "The file: " + outputLocation + " is in accessible\nThe file may have been deleted or moved.", "Error", JOptionPane.ERROR_MESSAGE);          
        }

    }

    public void viewDir(OSRow row) {
        String fileLocation = row.getOutputLocations().values().iterator().next();
        File f = new File(fileLocation);
        boolean openDirOnly = !f.exists();
        
        if(!openDirOnly) {
            try { // Note: This is windows specific, if it fails we just open the folder
                new ProcessBuilder("explorer.exe", "/select," + fileLocation).start();
            } catch (IOException ex) { // Failing probably means we are on a different OS
                openDirOnly = true;
            }
        }
        
        if(openDirOnly) {
            File file = new File(row.getOutputLocations().values().iterator().next());
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.open(file.getParentFile());
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    public OpenSextantMainFrameImpl getMainFrame() {
        return frame;
    }
    
    public Timer getTimer() {
    	return this.timer;
    }

}
