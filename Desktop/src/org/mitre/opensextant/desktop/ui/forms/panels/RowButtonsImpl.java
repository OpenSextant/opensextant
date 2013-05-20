package org.mitre.opensextant.desktop.ui.forms.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import org.mitre.opensextant.desktop.ui.table.OSRow;

public class RowButtonsImpl extends RowButtons {
	
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
