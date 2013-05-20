package org.mitre.opensextant.desktop.ui.helpers;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.mitre.opensextant.desktop.ui.OpenSextantMainFrameImpl;
import org.mitre.opensextant.desktop.ui.forms.panels.RowButtonsImpl;
import org.mitre.opensextant.desktop.ui.table.OSRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainFrameTableHelper {

	private static Logger log = LoggerFactory.getLogger(OpenSextantMainFrameImpl.class);
	private OpenSextantMainFrameImpl frame;

	public MainFrameTableHelper(OpenSextantMainFrameImpl frame) {
		this.frame = frame;
		
		// Custom table layout code
//		tableLayout = new javax.swing.GroupLayout(frame.getTablePanel());
//		frame.getTablePanel().setLayout(tableLayout);
//		horizontalGroup = tableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING);
//		tableLayout.setHorizontalGroup(horizontalGroup);
//
//		verticalGroup = tableLayout.createSequentialGroup();
//		tableLayout.setVerticalGroup(tableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(verticalGroup));
		
//		setVisibleActions(false);

	}
	
	public void updateRowProgress(OSRow row, OSRow.STATUS status, int num) {

//		if (num >= 100) {
//			JLabel stopLabel = tableStopLabel.get(guiEntry);
//			stopLabel.setToolTipText("Delete job from list");
//			updateIcon(stopLabel, OpenSextantMainFrameImpl.IconType.TRASH);
//		}
		
		row.setProgress(num, status);
		frame.getTable().repaint(row);
		

		// Swing ridiculously makes it incredibly difficult to change an
		// individual progress bar's color. May revisit this.
		/*
		 * UIDefaults overrides = UIManager.getDefaults();
		 * overrides.put("nimbusOrange", (Color.red));
		 * 
		 * instance.tableProgress.get(guiEntry).putClientProperty("Nimbus.Overrides"
		 * , overrides); instance.tableProgress.get(guiEntry).putClientProperty(
		 * "Nimbus.Overrides.InheritDefaults", false);
		 */

//		if (tableProgressLabel.get(guiEntry).getText().contains("Cancelled"))
//			return;
//
//		tableProgress.get(guiEntry).setValue(num);
//		tableProgressLabel.get(guiEntry).setText(status);

	}

	public OSRow addRow(OSRow row) {
		

		return frame.getTable().createRow(row);
		
//		
//		String symbol = "file.png";
//		// Set up metadata
//		outputLocs.add(loc);
//		timestamps.add(System.currentTimeMillis());
//		inputFiles.add(input);
//
//		// Test what icon to use
//		File f = new File(input);
//		if (f != null && f.isDirectory())
//			symbol = "folder.png";
//
//		return addRowHelper(status, loc, name, type, symbol);
	}

	public void removeRow(OSRow row) {
		frame.getTable().removeRow(row);
	}
	
	public void viewResults(OSRow row) {
		File file = new File(row.getOutputLocation());
		try {
			log.info("FILE: " + file);
			Desktop.getDesktop().open(file);
		} catch (Exception ex) {
			log.error(ex.getMessage());
			JOptionPane.showMessageDialog(frame, "Error opening file: " + row.getOutputLocation(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public OpenSextantMainFrameImpl getMainFrame() {
		return frame;
	}


//	private void hideRow(int rowNum) {
//		tableCheck.get(rowNum).setVisible(false);
//		tableLabel.get(rowNum).setVisible(false);
//		tableRunLabel.get(rowNum).setVisible(false);
//		tableProgressLabel.get(rowNum).setVisible(false);
//		tableProgress.get(rowNum).setVisible(false);
//		tableStopLabel.get(rowNum).setVisible(false);
//		tableTermLabel.get(rowNum).setVisible(false);
//		tableSep.get(rowNum).setVisible(false);
//	}
//
//	
//	
//	private void cancelRow(int rowNum) {
//		// TODO: Actually stop job
//		updateProgress(rowNum, "Cancelled", 100);
//	}
//	
//	
//
//	private void rerunRow(int rowNum) {
//		frame.getApiHelper().processFile(inputFiles.get(rowNum));
//	}
//
//	public void toggleCheck(Object caller, ArrayList list) {
//		for (int i = 0; i < list.size(); i++) {
//			if (list.get(i) == caller) {
//				JCheckBox jc = tableCheck.get(i);
//				jc.setSelected(!jc.isSelected());
//				checkActionPerformed(null);
//			}
//		}
//	}
//
//	public void viewFileFromRow(Object caller, ArrayList list) {
//		for (int i = 0; i < list.size(); i++) {
//			if (list.get(i) == caller) {
//				viewFile(outputLocs.get(i));
//			}
//		}
//	}
//
//
//	public void runTopLevelButtons(ButtonType type) {
//		for (int i = 0; i < tableCheck.size(); i++) {
//			JCheckBox jc = tableCheck.get(i);
//			if (jc.isVisible() && jc.isSelected()) {
//				switch (type) {
//				case CANCEL:
//					cancelRow(i);
//					break;
//				case DELETE:
//					cancelRow(i);
//					hideRow(i);
//					break;
//				case RERUN:
//					rerunRow(i);
//					break;
//				case FILTER: /* TODO */
//					break;
//				}
//			}
//		}
//
//	}
//
//
//	public void checkAll(boolean value) {
//		boolean selectedOne = false;
//		for (JCheckBox jc : tableCheck) {
//			if (jc.isVisible()) {
//				jc.setSelected(value);
//				selectedOne = true;
//			}
//		}
//		if (selectedOne)
//			setVisibleActions(value);
//	}
//
//	private void updateIcon(JLabel l, OpenSextantMainFrameImpl.IconType t) {
//		// TODO: Kludge, non-null font means bold icon
//		boolean isBold = (l.getFont() == null);
//		// TODO: Kludge, blue == trash, green == cancel
//		// why Swing doesn't allow metadata Objects for components is beyond me
//		// that is probably the fix for this kludge (Extend JLabel with 2 bools)
//		boolean isTrash = (l.getForeground() == Color.blue);
//		String sym = "xCircle";
//		String bold = "";
//
//		switch (t) {
//		case BOLD:
//			isBold = true;
//			break;
//		case NORMAL:
//			isBold = false;
//			break;
//		case TRASH:
//			isTrash = true;
//			break;
//		case CANCEL:
//			isTrash = false;
//			break;
//		}
//
//		if (isTrash) {
//			sym = "trash";
//			l.setForeground(Color.blue);
//		} else {
//			l.setForeground(Color.green);
//		}
//		if (isBold) {
//			bold = "Bold";
//			l.setFont(frame.getProgressLabel().getFont());
//		} else {
//			l.setFont(null);
//		}
//
//		l.setIcon(new javax.swing.ImageIcon(frame.getClass().getResource("/org/mitre/opensextant/desktop/icons/" + sym + bold + ".png")));
//	}
//
//	private void setVisibleActions(boolean visible) {
//		frame.getFilterButton().setVisible(visible);
//		frame.getRerunButton().setVisible(visible);
//		frame.getDeleteButton().setVisible(visible);
//		frame.getCancelButton().setVisible(visible);
//		frame.getViewButton().setVisible(visible);
//	}
//
//	public void updateActionVisibility() {
//		boolean value = false;
//		for (JCheckBox jc : tableCheck)
//			if (jc.isVisible() && jc.isSelected())
//				value = true;
//
//		setVisibleActions(value);
//	}
//
//	public void viewResult() {
//		for (int i = 0; i < tableCheck.size(); i++) {
//			JCheckBox jc = tableCheck.get(i);
//			if (jc.isVisible() && jc.isSelected()) {
//				File file = new File(this.outputLocs.get(i));
//				try {
//					Desktop.getDesktop().open(file);
//				} catch (IOException ex) {
//					log.error(ex.getMessage());
//				}
//			}
//		}
//	}
//
//	
//	private void checkActionPerformed(java.awt.event.ActionEvent evt) {
//		updateActionVisibility();
//	}
//	
//	public void hoverRow(boolean inRow, Object caller, ArrayList list) {
//		for (int i = 0; i < list.size(); i++) {
//			if (list.get(i) == caller) {
//				int fontWeight = inRow ? Font.BOLD : Font.PLAIN;
//				JLabel label = tableProgressLabel.get(i);
//				Font newFont = new Font(label.getFont().getName(), fontWeight, label.getFont().getSize());
//				label.setFont(newFont);
//				tableLabel.get(i).setFont(newFont);
//				tableRunLabel.get(i).setFont(newFont);
//				tableTermLabel.get(i).setFont(newFont);
//			}
//		}
//	}
//
//
//	private int addRowHelper(String status, String loc, String name, String type, String symbol) {
//		// Temporary widgets to work with
//		JCheckBox tmpCheck = new javax.swing.JCheckBox();
//		JLabel tmpLabel = new JLabel();
//		JLabel tmpProgressLabel = new JLabel();
//		JLabel tmpRunLabel = new JLabel();
//		JProgressBar tmpProgress = new JProgressBar();
//		JSeparator tmpSep = new JSeparator();
//		JLabel tmpStopLabel = new JLabel();
//		JLabel tmpTermLabel = new JLabel();
//
//		// Add widgets to the appropriate lists
//		tableCheck.add(tmpCheck);
//		tableLabel.add(tmpLabel);
//		tableProgressLabel.add(tmpProgressLabel);
//		tableRunLabel.add(tmpRunLabel);
//		tableProgress.add(tmpProgress);
//		tableSep.add(tmpSep);
//		tableStopLabel.add(tmpStopLabel);
//		tableTermLabel.add(tmpTermLabel);
//
//		// Set up specifics for this row
//		tmpStopLabel.setFont(null); // TODO: Kludge to get rid of
//		tmpStopLabel.setForeground(Color.green); // TODO: Kludge to get rid of
//		tmpLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mitre/opensextant/desktop/icons/" + symbol)));
//		tmpLabel.setText(name);
//		tmpLabel.addMouseListener(new OSMouseAdapter(frame,tableLabel));
//		tmpProgressLabel.addMouseListener(new OSMouseAdapter(frame,tableProgressLabel));
//		tmpRunLabel.addMouseListener(new OSMouseAdapter(frame,tableRunLabel));
//		tmpTermLabel.addMouseListener(new OSMouseAdapter(frame,tableTermLabel));
//		tmpStopLabel.addMouseListener(new MouseAdapter() {
//			@Override
//			public void mouseClicked(MouseEvent e) {
//				cancelOrDelete((JLabel) e.getSource());
//			}
//
//			@Override
//			public void mouseEntered(MouseEvent e) {
//				updateIcon((JLabel) e.getSource(), IconType.BOLD);
//			}
//
//			@Override
//			public void mouseExited(MouseEvent e) {
//				updateIcon((JLabel) e.getSource(), IconType.NORMAL);
//			}
//
//		});
//		tmpProgress.addMouseListener(new OSMouseAdapter(frame,tableProgress));
//		tmpLabel.setToolTipText("Double-click to view output");
//		tmpProgressLabel.setToolTipText("Ouput Location: " + loc);
//		tmpStopLabel.setToolTipText("Click to stop running job");
//		tmpProgressLabel.setText(status);
//		tmpStopLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mitre/opensextant/desktop/icons/xCircle.png")));
//
//		// Action listener for the checkbox
//		tmpCheck.addActionListener(new java.awt.event.ActionListener() {
//			public void actionPerformed(java.awt.event.ActionEvent evt) {
//				checkActionPerformed(evt);
//			}
//		});
//
//		// Format and add date
//		Date d = new Date();
//		String dateStr = (new SimpleDateFormat("yyyy-MM-dd")).format(d);
//		String dateExtStr = (new SimpleDateFormat("hh:mm:ss a")).format(d);
//		tmpRunLabel.setText(dateStr);
//		tmpRunLabel.setToolTipText(dateExtStr);
//
//		horizontalGroup
//				.addComponent(tmpSep)
//				.addGroup(
//						tableLayout
//								.createSequentialGroup()
//								.addComponent(tmpCheck)
//								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
//								.addComponent(tmpLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
//								.addGap(18, 18, 18)
//								.addComponent(tmpProgressLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 138,
//										javax.swing.GroupLayout.PREFERRED_SIZE)
//								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
//								.addComponent(tmpProgress, javax.swing.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
//								.addComponent(tmpStopLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 10, 25)
//								.addComponent(tmpTermLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 10, Short.MAX_VALUE)
//								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(tmpRunLabel))
//				.addGap(39, 39, 39);
//
//		verticalGroup
//				.addGroup(
//						tableLayout
//								.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//								.addGroup(
//										tableLayout
//												.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
//												.addComponent(tmpCheck, javax.swing.GroupLayout.DEFAULT_SIZE,
//														javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//												.addGroup(
//														tableLayout
//																.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
//																.addComponent(tmpLabel, javax.swing.GroupLayout.DEFAULT_SIZE,
//																		javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//																.addComponent(tmpProgressLabel)
//																.addComponent(tmpProgress, javax.swing.GroupLayout.DEFAULT_SIZE,
//																		javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//																.addComponent(tmpStopLabel, javax.swing.GroupLayout.DEFAULT_SIZE,
//																		javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//																.addComponent(tmpTermLabel, javax.swing.GroupLayout.DEFAULT_SIZE,
//																		javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
//								.addComponent(tmpRunLabel)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
//				.addComponent(tmpSep, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
//				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
//
//		tableLayout.setHorizontalGroup(horizontalGroup);
//		tableLayout.setVerticalGroup(verticalGroup);
//
//		return (tableCount++);
//	}
//	private void cancelOrDelete(JLabel caller) {
//		for (int i = 0; i < tableStopLabel.size(); i++) {
//			JLabel jl = tableStopLabel.get(i);
//			if (jl == caller) {
//				String tooltip = jl.getToolTipText();
//				if (tooltip != null) {
//					boolean isDelete = tooltip.startsWith("Delete");
//					String dialogMsg = "Stop running job?";
//					String dialogAction = "stopping";
//					if (isDelete) {
//						dialogMsg = "Delete job from list?";
//						dialogAction = "deleting";
//					}
//
//					Object[] options = { "Yes", "No" };
//					int n = JOptionPane.showOptionDialog(frame, dialogMsg, "Confirm " + dialogAction + " job", JOptionPane.YES_NO_OPTION,
//							JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
//					if (n == 0) {
//						if (tooltip.startsWith("Delete"))
//							hideRow(i);
//						else
//							cancelRow(i);
//					}
//				}
//
//			}
//		}
//		updateActionVisibility();
//
//	}

}
