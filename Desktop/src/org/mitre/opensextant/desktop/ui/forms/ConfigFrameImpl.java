package org.mitre.opensextant.desktop.ui.forms;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.mitre.opensextant.desktop.ui.helpers.ConfigHelper;

public class ConfigFrameImpl extends ConfigFrame{

	private ConfigHelper configHelper;

	public ConfigFrameImpl() {
		super();
		
		this.configHelper = ConfigHelper.getInstance();
		
		outputText.setText(configHelper.getOutLocation());
		threadCount.setValue(configHelper.getNumThreads());

		for (String t : configHelper.getOutType().split(",")) {
			if ("CSV".equals(t))
				csvCheck.setSelected(true);
			else if ("KML".equals(t))
				kmlCheck.setSelected(true);
			else if ("WKT".equals(t))
				wktCheck.setSelected(true);
			else if ("JSON".equals(t))
				jsonCheck.setSelected(true);
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

		// Somewhat ugly due to netbeans lack of arrays in the GUI designer
		String outType = "";
		if (csvCheck.isSelected())
			outType += "CSV,";
		if (kmlCheck.isSelected())
			outType += "KML,";
		if (jsonCheck.isSelected())
			outType += "JSON,";
		if (shapefileCheck.isSelected())
			outType += "SHAPEFILE,";
		if (wktCheck.isSelected())
			outType += "WKT,";

		if (outType.length() > 1)
			outType = outType.substring(0, outType.length() - 1);

		configHelper.setOutLocation(outputText.getText());
		configHelper.setOutType(outType);
		configHelper.setNumThreads((Integer)threadCount.getValue());
		
		configHelper.saveSettings();

		this.dispose();
	}// GEN-LAST:event_doneButtonActionPerformed

	
}
