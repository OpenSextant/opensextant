package org.mitre.opensextant.desktop.ui.forms.panels;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;

import javax.swing.JPopupMenu;

import org.mitre.opensextant.desktop.ui.table.OSRow;
import org.mitre.opensextant.desktop.ui.table.RowButtonsEditor;

@SuppressWarnings("serial")
public class RowButtonsImpl extends RowButtons {
	
	private OSRow row;

	private RowButtonsEditor rowButtonsEditor;
	
	public RowButtonsImpl(OSRow osRow) {
		super();
		this.row = osRow;
		initialize();
	}

	private void initialize() {
		cancelDeleteButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!row.isRunning()) {
					row.removeFromTable();
					rowButtonsEditor.stopCellEditing();
				} else {
					row.cancelExecution(true);
				}
			}
		});
		
		

		viewResultsButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
        		if (row.getOutputTypes().size() > 1) {
                	JPopupMenu popup = new JPopupMenu();
                	for (final String format : row.getOutputTypes()) {
                		popup.add(new AbstractAction(format) {
    						@Override
    						public void actionPerformed(ActionEvent e) {
    							row.viewResults(format);
    						}
                		});
                	}
                    popup.show(e.getComponent(), e.getX(), e.getY());
        		} else {
   					row.viewResults(row.getOutputTypes().get(0));
        		}
            }
        });
		
                
                viewDirButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				row.viewDir();
			}
		});
		
		reRunButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				row.rerun();
			}
		});
		
		if (row.isChild()) {
			cancelDeleteButton.setVisible(false);
			viewResultsButton.setVisible(false);
			reRunButton.setVisible(false);
                        viewDirButton.setVisible(false);
		}
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

        public javax.swing.JButton getViewDirButton() {
		return viewDirButton;
	}

        
	public void setCellEditor(RowButtonsEditor rowButtonsEditor) {
		this.rowButtonsEditor = rowButtonsEditor;
		
	}

}
