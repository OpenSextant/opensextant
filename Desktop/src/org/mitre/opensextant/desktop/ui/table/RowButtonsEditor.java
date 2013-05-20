package org.mitre.opensextant.desktop.ui.table;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;

import org.mitre.opensextant.desktop.ui.forms.panels.RowButtonsImpl;

public class RowButtonsEditor extends DefaultCellEditor {

	public RowButtonsEditor(JCheckBox checkBox) {
		super(checkBox);
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

		RowButtonsImpl panel = ((OSRow) value).getButtonPanel();
		panel.setCellEditor(this);
		if (isSelected) {
			panel.setBackground(table.getSelectionBackground());
		} else {
			panel.setBackground(table.getBackground());
		}
		return panel;
	}

	public boolean stopCellEditing() {
		return super.stopCellEditing();
	}

	protected void fireEditingStopped() {
		super.fireEditingStopped();
	}
}