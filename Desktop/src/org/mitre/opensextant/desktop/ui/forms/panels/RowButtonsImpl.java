package org.mitre.opensextant.desktop.ui.forms.panels;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.mitre.opensextant.desktop.ui.table.OSRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RowButtonsImpl extends RowButtons {
	
	private static Logger log = LoggerFactory.getLogger(RowButtonsImpl.class);

	private OSRow row;
	
	public RowButtonsImpl(OSRow osRow) {
		super();
		initialize();
		this.row = osRow;
	}

	private void initialize() {
		cancelDeleteButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(RowButtonsImpl.this, "ROW: " + row.getTitle() + " : " + row.getPercent(), "TITLE", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		
		viewResultsButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				File file = new File(row.getOutputLocation());
				try {
					Desktop.getDesktop().open(file);
				} catch (IOException ex) {
					log.error(ex.getMessage());
					JOptionPane.showMessageDialog(RowButtonsImpl.this, "Error opening file: " + row.getOutputLocation(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		reRunButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(RowButtonsImpl.this, "Need to add re-run functionality", "Error", JOptionPane.ERROR_MESSAGE);
				
			}
		});
	}

	public javax.swing.JButton getCancelDeleteButton() {
		return cancelDeleteButton;
	}

	public javax.swing.JButton getReRunButton() {
		return reRunButton;
	}

	public javax.swing.JButton getViewResultsButton() {
		return viewResultsButton;
	}

}
