package org.mitre.opensextant.desktop.ui.forms;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.mitre.opensextant.desktop.ui.helpers.ConfigHelper;
import org.mitre.opensextant.desktop.ui.helpers.ConfigHelper.GeoExtraction;
import org.mitre.opensextant.desktop.ui.helpers.ConfigHelper.TimeAssociation;
import org.mitre.opensextant.desktop.ui.helpers.ViewHelper;

@SuppressWarnings("serial")
public class ConfigFrameImpl extends ConfigFrame {

    private static final long HALF_GB = 536870912;
    private ConfigHelper configHelper;
    
    private static final int memoryLimit = (int)(Runtime.getRuntime().maxMemory()/HALF_GB);
    private static final int coreLimit = 2 * Runtime.getRuntime().availableProcessors();
    
    private void displayWarnings(){
        int val = (Integer)threadCount.getValue();
        boolean breaksMem = false;
        boolean breaksCores = false;
        String warning = null;
        
        if(memoryLimit < val) breaksMem = true;
        if (coreLimit > 1 && val > coreLimit / 2) breaksCores = true;
        
        if(breaksMem && breaksCores) warning = "Warning: Available memory and processors may be insufficient";
        else if(breaksMem) warning = "Warning: Available memory may be insufficient";
        else if(breaksCores) warning = "Warning: Available processors may be insufficient";
        
        if(warning == null) warnLabel.setVisible(false);
        else {
            warnLabel.setText(warning);
            warnLabel.setVisible(true);
        }
    }
    
    public ConfigFrameImpl() {
        super();

        this.configHelper = ConfigHelper.getInstance();

        cacheText.setText(configHelper.getCacheRoot());
        tempText.setText(configHelper.getTmpRoot());
        outputText.setText(configHelper.getOutLocation());
        threadCount.setValue(configHelper.getNumThreads());

        extractTimeCheck.setSelected(configHelper.isExtractTime());
        if (configHelper.getTimeAssociation() == TimeAssociation.CROSS) {
            cartProdTimeRadioButton.setSelected(true);
        }
        if (!configHelper.isExtractTime()) {
            csvTimeRadioButton.setEnabled(false);
            cartProdTimeRadioButton.setEnabled(false);
        }
        extractIdentifiersCheck.setSelected(configHelper.isExtractIdentifiers());

        extractGeoCheck.setSelected(ConfigHelper.getInstance().getGeoExtraction().extractPlaces() || ConfigHelper.getInstance().getGeoExtraction().extractCoordinates());
        extractPlacesCheck.setSelected(ConfigHelper.getInstance().getGeoExtraction().extractPlaces());
        extractCoordinatesCheck.setSelected(ConfigHelper.getInstance().getGeoExtraction().extractCoordinates());
        
        displayWarnings();
        ((SpinnerNumberModel) threadCount.getModel()).setMaximum(coreLimit);

        for (String t : configHelper.getOutTypes()) {
            if ("CSV".equals(t))
                csvCheck.setSelected(true);
            else if ("KML".equals(t))
                kmlCheck.setSelected(true);
            else if ("WKT".equals(t))
                wktCheck.setSelected(true);
            else if ("JSON".equals(t))
                jsonCheck.setSelected(true);
            else if ("XLS".equals(t))
                xlsCheck.setSelected(true);
            else if ("SHAPEFILE".equals(t))
                shapefileCheck.setSelected(true);
            else if (("GDB").equals(t))
                gdbCheck.setSelected(true);
            else if (("SQLITE").equals(t))
                sqliteCheck.setSelected(true);
            else if (("ABI").equals(t))
                abiToolCheck.setSelected(true);
        }

        ViewHelper.centerTheWindow(this);

        Hashtable<Integer, JLabel> sliderLabels = new Hashtable<Integer, JLabel>();
        sliderLabels.put(0, new JLabel("Fatal"));
        sliderLabels.put(1, new JLabel("Error"));
        sliderLabels.put(2, new JLabel("Warn"));
        sliderLabels.put(3, new JLabel("Info"));
        sliderLabels.put(4, new JLabel("Debug"));

        loggingSlider.setLabelTable(sliderLabels);

        loggingSlider.setValue(configHelper.getLoggingLevel());

        threadCount.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                displayWarnings();
            }
        });

        doneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doneButtonActionPerformed(e);
            }
        });
        

        
        extractGeoCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (extractGeoCheck.isSelected()) {
                    extractPlacesCheck.setSelected(true);
                    extractCoordinatesCheck.setSelected(true);
                } else {
                    extractPlacesCheck.setSelected(false);
                    extractCoordinatesCheck.setSelected(false);
                }
            }
        });

        ActionListener uncheckGeoActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!extractPlacesCheck.isSelected() && !extractCoordinatesCheck.isSelected()) {
                    if (extractGeoCheck.isSelected()) extractGeoCheck.setSelected(false);
                } else {
                    if (!extractGeoCheck.isSelected()) extractGeoCheck.setSelected(true);
                }
            }
        };
        
        extractPlacesCheck.addActionListener(uncheckGeoActionListener);
        extractCoordinatesCheck.addActionListener(uncheckGeoActionListener);

        extractTimeCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (extractTimeCheck.isSelected()) {
                    csvTimeRadioButton.setEnabled(true);
                    cartProdTimeRadioButton.setEnabled(true);
                } else {
                    csvTimeRadioButton.setEnabled(false);
                    cartProdTimeRadioButton.setEnabled(false);
                }
            }
        });

        java.net.URL imgURL = ConfigFrameImpl.class.getResource("/org/mitre/opensextant/desktop/icons/logo.png");
        if (imgURL != null) {
            this.setIconImage(new ImageIcon(imgURL, "Icon").getImage());
        }
    }

    private void doneButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_doneButtonActionPerformed

        List<String> outTypes = new ArrayList<String>();
        if (csvCheck.isSelected())
            outTypes.add("CSV");
        if (kmlCheck.isSelected())
            outTypes.add("KML");
        if (jsonCheck.isSelected())
            outTypes.add("JSON");
        if (xlsCheck.isSelected())
            outTypes.add("XLS");
        if (shapefileCheck.isSelected())
            outTypes.add("SHAPEFILE");
        if (wktCheck.isSelected())
            outTypes.add("WKT");
        if (gdbCheck.isSelected())
            outTypes.add("GDB");
        if (sqliteCheck.isSelected())
            outTypes.add("SQLITE");
        if (abiToolCheck.isSelected())
            outTypes.add("ABI");

        configHelper.setOutLocation(outputText.getText());
        configHelper.setCacheRoot(cacheText.getText());

        configHelper.setOutTypes(outTypes);
        configHelper.setNumThreads((Integer) threadCount.getValue());

        FileAppender loggingFileAppender = (FileAppender) Logger.getRootLogger().getAppender("default.file");

        switch (loggingSlider.getValue()) {
        case 0:
            loggingFileAppender.setThreshold(Level.FATAL);
            break;
        case 1:
            loggingFileAppender.setThreshold(Level.ERROR);
            break;
        case 2:
            loggingFileAppender.setThreshold(Level.WARN);
            break;
        case 3:
            loggingFileAppender.setThreshold(Level.INFO);
            break;
        case 4:
            loggingFileAppender.setThreshold(Level.DEBUG);
            break;
        }

        configHelper.setLoggingLevel(loggingSlider.getValue());
        
        configHelper.setExtractIdentifiers(extractIdentifiersCheck.isSelected());
        configHelper.setExtractTime(extractTimeCheck.isSelected());
        configHelper.setTimeAssociation((csvTimeRadioButton.isSelected()) ? TimeAssociation.CSV : TimeAssociation.CROSS);

        if (extractPlacesCheck.isSelected() && extractCoordinatesCheck.isSelected()) {
            configHelper.setGeoExtraction(GeoExtraction.BOTH);
        } else if (extractPlacesCheck.isSelected()) {
            configHelper.setGeoExtraction(GeoExtraction.PLACE);
        } else if (extractCoordinatesCheck.isSelected()) {
            configHelper.setGeoExtraction(GeoExtraction.COORD);
        } else {
            configHelper.setGeoExtraction(GeoExtraction.NONE);
        }
        
        configHelper.saveSettings();

        this.dispose();
    }// GEN-LAST:event_doneButtonActionPerformed

}
