package org.mitre.opensextant.desktop.ui.forms.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.mitre.opensextant.desktop.ui.table.OSRow;
import org.mitre.opensextant.desktop.ui.table.RowButtonsEditor;

@SuppressWarnings("serial")
public class RowButtonsImpl extends RowButtons {
	
	private OSRow row;

	private RowButtonsEditor rowButtonsEditor;
	
	public RowButtonsImpl(OSRow osRow) {
		super();
		initialize();
		this.row = osRow;
	}

	private void initialize() {
		cancelDeleteButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!row.isRunning()) {
					row.removeFromTable();
					rowButtonsEditor.stopCellEditing();
				} else {
					row.cancelExecution();
				}
			}
		});
		
		viewResultsButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				row.viewResults();
			}
		});
		
		reRunButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				row.rerun();
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

	public void setCellEditor(RowButtonsEditor rowButtonsEditor) {
		this.rowButtonsEditor = rowButtonsEditor;
		
	}

}
