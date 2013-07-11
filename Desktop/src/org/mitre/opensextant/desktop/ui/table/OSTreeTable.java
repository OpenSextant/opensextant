package org.mitre.opensextant.desktop.ui.table;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import org.apache.commons.lang.ArrayUtils;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.mitre.opensextant.desktop.ui.OpenSextantMainFrameImpl;
import org.mitre.opensextant.desktop.ui.forms.panels.RowButtonsImpl;
import org.mitre.opensextant.desktop.ui.forms.panels.RowDurationImpl;
import org.mitre.opensextant.desktop.ui.forms.panels.RowProgressBarImpl;
import org.mitre.opensextant.desktop.ui.helpers.ConfigHelper;
import org.mitre.opensextant.desktop.ui.helpers.MainFrameTableHelper;
import org.mitre.opensextant.desktop.ui.helpers.ViewHelper;

public class OSTreeTable {
	private static final String ICON_LOCATION = "/org/mitre/opensextant/desktop/icons/";
	private JXTreeTable treeTable;
	private final OSTreeTableModel treeTableModel = generateTestModel();
	private OpenSextantMainFrameImpl frame;

	public OSTreeTable(OpenSextantMainFrameImpl frame) {
		this.frame = frame;
	}

	public JXTreeTable create() {
		treeTable = new TooltipJXTreeTable(treeTableModel);

                treeTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
                    public void valueChanged(ListSelectionEvent event) {
                        for(int i = 0; i < treeTable.getRowCount(); i ++) {
                            OSRow r = (OSRow)treeTable.getValueAt(i, OSTreeTableModel.TIMING);
                            r.toggleDurationColor(false);    
                        }
                        int[] rows = treeTable.getSelectedRows();
                        for(int rowId : rows) {
                            OSRow r = (OSRow)treeTable.getValueAt(rowId, OSTreeTableModel.TIMING);
                            r.toggleDurationColor(true);
                        }
//                        OSRow r = (OSRow)treeTable.getValueAt(treeTable.getSelectedRow(), OSTreeTableModel.TIMING);
                        
                    }
                });
                
        @SuppressWarnings("serial")
		class SortIconTableHeaderRenderer extends JLabel implements TableCellRenderer {

			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
					int column) {
				TableCellRenderer defaultRenderer = table.getTableHeader().getDefaultRenderer();

				Component component = defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				String iconLoc = ICON_LOCATION;

				if (treeTableModel.getSortAscending(column))
					iconLoc += "arrow_up.png";
				else
					iconLoc += "arrow_down.png";

				((JLabel) component).setIcon(new javax.swing.ImageIcon(getClass().getResource(iconLoc)));
				return component;
			}

		}
		treeTable.getColumn(OSTreeTableModel.TITLE).setHeaderRenderer(new SortIconTableHeaderRenderer());
		treeTable.getColumn(OSTreeTableModel.TITLE).setMinWidth(200);

		treeTable.getColumn(OSTreeTableModel.PROGRESS).setMinWidth(120);
		treeTable.getColumn(OSTreeTableModel.PROGRESS).setHeaderRenderer(new SortIconTableHeaderRenderer());
		treeTable.getColumn(OSTreeTableModel.PROGRESS).setCellRenderer(new TableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
					int column) {
				RowProgressBarImpl panel = ((OSRow) value).getProgressBarPanel();

				if (isSelected) {
					panel.setBackground(table.getSelectionBackground());
				} else {
					panel.setBackground(table.getBackground());
				}
				return panel;
			}
		});

		treeTable.getColumn(OSTreeTableModel.TIMING).setMinWidth(110);
		treeTable.getColumn(OSTreeTableModel.TIMING).setWidth(110);

		treeTable.getColumn(OSTreeTableModel.TIMING).setHeaderRenderer(new SortIconTableHeaderRenderer());
		treeTable.getColumn(OSTreeTableModel.TIMING).setCellRenderer(new TableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
					int column) {
				OSRow osRow = (OSRow) value;

				RowDurationImpl panel = osRow.getDurationPanel();
//				panel.updateDuration(osRow);

				if (isSelected) {
					panel.setBackground(table.getSelectionBackground());
				} else {
					panel.setBackground(table.getBackground());
				}
				return panel;
			}
		});

		treeTable.getColumn(OSTreeTableModel.ACTIONS).setMinWidth(120);
		treeTable.getColumn(OSTreeTableModel.ACTIONS).setMaxWidth(120);
		treeTable.getColumn(OSTreeTableModel.ACTIONS).setCellEditor(new RowButtonsEditor(new JCheckBox()));
		treeTable.getColumn(OSTreeTableModel.ACTIONS).setCellRenderer(new TableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
					int column) {
				RowButtonsImpl panel = ((OSRow) value).getButtonPanel();
				if (isSelected) {
					panel.setBackground(table.getSelectionBackground());
				} else {
					panel.setBackground(table.getBackground());
				}
				return panel;

			}
		});

		treeTable.getColumn(OSTreeTableModel.FILE_INFO).setHeaderRenderer(new SortIconTableHeaderRenderer());
		treeTable.getColumn(OSTreeTableModel.OUT_TYPES).setHeaderRenderer(new SortIconTableHeaderRenderer());
		treeTable.getColumn(OSTreeTableModel.LAST_RUN).setHeaderRenderer(new SortIconTableHeaderRenderer());

		treeTable.setEditable(true);
		treeTable.setRowHeight(30);
		treeTable.getTableHeader().setReorderingAllowed(false);

		treeTable.setTreeCellRenderer(new DefaultTreeCellRenderer() {
            private static final long serialVersionUID = -5532638460304402889L;

            public java.awt.Component getTreeCellRendererComponent(javax.swing.JTree tree, Object value, boolean sel, boolean expanded,
					boolean leaf, int row, boolean hasFocus) {

				super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

				if (value instanceof DefaultMutableTreeTableNode) {
					DefaultMutableTreeTableNode node = (DefaultMutableTreeTableNode) value;
					OSRow thisRow = (OSRow) node.getUserObject();
					setText(thisRow.getTitle());

					if (thisRow.getInputFile() != null && !thisRow.hasChildren()) {
						setIcon(OpenSextantMainFrameImpl.getIconForExtension(thisRow.getInputFile()));
					}
				}

				return this;
			};

		});

		treeTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {

					List<Integer> rowIndexes = Arrays.asList(ArrayUtils.toObject(treeTable.getSelectedRows()));
					int r = treeTable.rowAtPoint(e.getPoint());

					if (!rowIndexes.contains(r)) {
						if (r >= 0 && r < treeTable.getRowCount()) {
							treeTable.setRowSelectionInterval(r, r);
						}
					}

					TreePath[] paths = treeTable.getTreeSelectionModel().getSelectionPaths();
					Set<OSRow> rows = new HashSet<OSRow>();
					for (TreePath selp : paths) {
						DefaultMutableTreeTableNode p = (DefaultMutableTreeTableNode) selp.getLastPathComponent();
						OSRow row = (OSRow) p.getUserObject();
						if (row.isChild())
							row = row.getParent();
						rows.add(row);
					}
					Set<String> formats = new HashSet<String>();
					for (OSRow row : rows) {
						formats.addAll(row.getOutputTypes());
					}
					
					JPopupMenu popup = new JPopupMenu();
					popup.add(new DeleteNodeAction());
					popup.add(new ReRunAction());
					if (formats.size() > 1 || ConfigHelper.getInstance().isExtractIdentifiers()) {
						JMenu formatsMenu = new JMenu("View Results");
						for (String format : formats) {
                            if (!"ABI".equals(format)) {
                                formatsMenu.add(new ViewResultsAction(format));
                            }
						}
                        if (ConfigHelper.getInstance().isExtractIdentifiers()) {
                        	formatsMenu.add(new ViewResultsAction("Identifiers"));
                        }
                        formatsMenu.add(new ViewResultsAction("Statistics"));

						popup.add(formatsMenu);
					} else if (formats.size() > 0) {
						popup.add(new ViewResultsAction(formats.iterator().next()));
					}
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});

		// Catch Delete
		int condition = JComponent.WHEN_IN_FOCUSED_WINDOW;
		InputMap inputMap = treeTable.getInputMap(condition);
		ActionMap actionMap = treeTable.getActionMap();

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "Delete");
		actionMap.put("Delete", new DeleteNodeAction());

		JTableHeader header = treeTable.getTableHeader();

		header.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
                if(SwingUtilities.isLeftMouseButton(e)) { 
                    JTableHeader h = (JTableHeader) e.getSource();
                    int nColumn = h.columnAtPoint(e.getPoint());

                    if (nColumn != -1)
                        sortColumn(nColumn);
                } else if (SwingUtilities.isRightMouseButton(e)) {

                    JPopupMenu popup = ViewHelper.getInstance().makePopup(treeTable);

                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }

			void sortColumn(final int nColumn) {
				DefaultTreeTableModel model = ((DefaultTreeTableModel) treeTable.getTreeTableModel());
				DefaultMutableTreeTableNode root = (DefaultMutableTreeTableNode) model.getRoot();

				ArrayList<DefaultMutableTreeTableNode> nodes = new ArrayList<DefaultMutableTreeTableNode>();
				while (root.getChildCount() > 0) {
					DefaultMutableTreeTableNode child = (DefaultMutableTreeTableNode) root.getChildAt(0);
					nodes.add(child);
					model.removeNodeFromParent(child);
				}
				treeTableModel.sortRows(nodes, nColumn, treeTable);

				for (DefaultMutableTreeTableNode node : nodes) {
					model.insertNodeInto(node, root, 0);
				}
			}
		});

		// treeTable.setRowSorter(
        treeTable.getTableHeader().setReorderingAllowed(true); 
        ViewHelper.getInstance().initialize(treeTable);
		return treeTable;
	}

	public void createRow(OSRow row) {
		DefaultMutableTreeTableNode root = (DefaultMutableTreeTableNode) treeTable.getTreeTableModel().getRoot();

		DefaultMutableTreeTableNode node = new DefaultMutableTreeTableNode(row);
		if (row.hasChildren()) {
			for (OSRow child : row.getChildren()) {
				DefaultMutableTreeTableNode childNode = new DefaultMutableTreeTableNode(child);
				node.add(childNode);
			}
		}

		((DefaultTreeTableModel) treeTable.getTreeTableModel()).insertNodeInto(node, root, root.getChildCount());

	}

	public DefaultMutableTreeTableNode getNodeForRow(OSRow row) {

		DefaultMutableTreeTableNode parentNode = null;

		if (row.isChild()) {
			parentNode = getNodeForRow(row.getParent());
		} else {
			parentNode = (DefaultMutableTreeTableNode) treeTableModel.getRoot();
		}

		for (int i = 0; i < parentNode.getChildCount(); i++) {
			DefaultMutableTreeTableNode candidate = (DefaultMutableTreeTableNode) parentNode.getChildAt(i);
			if (candidate.getUserObject().equals(row)) {
				return candidate;
			}
		}
		return null;
	}

	public void removeRow(OSRow row) {
		((DefaultTreeTableModel) treeTable.getTreeTableModel()).removeNodeFromParent(getNodeForRow(row));
                ConfigHelper.getInstance().removeRow(row);
	}

	@SuppressWarnings("serial")
	class DeleteNodeAction extends AbstractAction {
		DeleteNodeAction() {
			super("Delete/Cancel");
		}

		public void actionPerformed(ActionEvent e) {
			TreePath[] paths = treeTable.getTreeSelectionModel().getSelectionPaths();

			if (!MainFrameTableHelper.confirmationPrompt(
					"Remove all selected jobs? WARNING: Removing children will remove the entire job.", "Confirm removing jobs", treeTable))
				return;

			Set<OSRow> rows = new HashSet<OSRow>();
			for (TreePath selp : paths) {
				DefaultMutableTreeTableNode p = (DefaultMutableTreeTableNode) selp.getLastPathComponent();
				OSRow row = (OSRow) p.getUserObject();
				if (row.isChild())
					row = row.getParent();
				rows.add(row);
			}

			for (OSRow row : rows) {
				row.cancelExecution(false);
				row.deleteFile();
				removeRow(row);
			}
                        ConfigHelper.getInstance().saveSettings();


			treeTable.repaint();
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) { }
					frame.revalidate();
					frame.repaint();
					frame.repaint(0, 0, frame.getSize().width, frame.getSize().height);
				}
			});			
		}
	}

	@SuppressWarnings("serial")
	class ViewResultsAction extends AbstractAction {
		private String format;

		ViewResultsAction(String format) {
			super(format);
			this.format = format;
		}

		public void actionPerformed(ActionEvent e) {
			TreePath[] paths = treeTable.getTreeSelectionModel().getSelectionPaths();
			Set<OSRow> rows = new HashSet<OSRow>();
			for (TreePath selp : paths) {
				DefaultMutableTreeTableNode p = (DefaultMutableTreeTableNode) selp.getLastPathComponent();
				OSRow row = (OSRow) p.getUserObject();
				if (row.isChild())
					row = row.getParent();
				rows.add(row);
			}
			for (OSRow row : rows) {
				if ("Identifiers".equals(format)) {
					row.viewIdentifiers();
                } else if ("Statistics".equals(format)) {
                    row.viewStatistics();
				} else {
					row.viewResults(format);
				}
			}
			treeTable.repaint();
		}
	}

	@SuppressWarnings("serial")
	class ReRunAction extends AbstractAction {
		ReRunAction() {
			super("Re-Run");
		}

		public void actionPerformed(ActionEvent e) {
			TreePath[] paths = treeTable.getTreeSelectionModel().getSelectionPaths();
			for (TreePath selp : paths) {
				DefaultMutableTreeTableNode p = (DefaultMutableTreeTableNode) selp.getLastPathComponent();
				OSRow row = (OSRow) p.getUserObject();
				row.rerun();
			}
			treeTable.repaint();
		}
	}

	/**
	 * Generates a PersonTreeTableModel of fake persons.
	 * 
	 * @return
	 */
	public OSTreeTableModel generateTestModel() {
		DefaultMutableTreeTableNode aRoot = new DefaultMutableTreeTableNode(new OSRow());
		return new OSTreeTableModel(aRoot);
	}

	public void repaint(OSRow row) {
		treeTableModel.update(getNodeForRow(row));
	}

}
