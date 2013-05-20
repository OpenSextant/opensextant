package org.mitre.opensextant.desktop.ui.table;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;

import org.mitre.opensextant.desktop.ui.forms.panels.RowButtonsImpl;

class RowButtonsEditor extends DefaultCellEditor {

	public RowButtonsEditor(JCheckBox checkBox) {
		super(checkBox);
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

		RowButtonsImpl panel = ((OSRow) value).getButtonPanel();
		panel.setBackground(table.getSelectionBackground());
		return panel;
	}

	public boolean stopCellEditing() {
		return super.stopCellEditing();
	}

	protected void fireEditingStopped() {
		super.fireEditingStopped();
	}
}