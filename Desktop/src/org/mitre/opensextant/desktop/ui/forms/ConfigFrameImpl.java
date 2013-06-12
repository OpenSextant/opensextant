package org.mitre.opensextant.desktop.ui.forms;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SpinnerNumberModel;

import org.mitre.opensextant.desktop.ui.helpers.ConfigHelper;

public class ConfigFrameImpl extends ConfigFrame{

	private ConfigHelper configHelper;

	public ConfigFrameImpl() {
		super();
		
		this.configHelper = ConfigHelper.getInstance();
		
		cacheText.setText(configHelper.getCacheRoot());
		tempText.setText(configHelper.getTmpRoot());
		outputText.setText(configHelper.getOutLocation());
		threadCount.setValue(configHelper.getNumThreads());
		int maxThreads = Runtime.getRuntime().availableProcessors();
		if (maxThreads > 1) maxThreads -= 1;
		((SpinnerNumberModel)threadCount.getModel()).setMaximum(maxThreads);

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
		}

		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
		
		doneButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doneButtonActionPerformed(e);
			}
		});

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

		configHelper.setOutLocation(outputText.getText());
        configHelper.setCacheRoot(cacheText.getText());

		configHelper.setOutTypes(outTypes);
		configHelper.setNumThreads((Integer)threadCount.getValue());
		
		configHelper.saveSettings();

		this.dispose();
	}// GEN-LAST:event_doneButtonActionPerformed

	
}
