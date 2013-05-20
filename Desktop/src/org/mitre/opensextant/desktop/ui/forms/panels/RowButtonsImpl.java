package org.mitre.opensextant.desktop.ui.forms.panels;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.mitre.opensextant.desktop.ui.helpers.MainFrameTableHelper;
import org.mitre.opensextant.desktop.ui.table.OSRow;
import org.mitre.opensextant.desktop.ui.table.RowButtonsEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RowButtonsImpl extends RowButtons {
	
	private static Logger log = LoggerFactory.getLogger(RowButtonsImpl.class);

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
