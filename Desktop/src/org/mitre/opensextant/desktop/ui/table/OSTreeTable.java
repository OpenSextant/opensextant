package org.mitre.opensextant.desktop.ui.table;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EventObject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.CellEditorListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import org.apache.commons.lang.ArrayUtils;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.mitre.opensextant.desktop.ui.forms.panels.RowButtonsImpl;
import org.mitre.opensextant.desktop.ui.forms.panels.RowDurationImpl;
import org.mitre.opensextant.desktop.ui.forms.panels.RowProgressBarImpl;
import org.mitre.opensextant.desktop.ui.helpers.ApiHelper;
import org.mitre.opensextant.desktop.ui.helpers.MainFrameTableHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OSTreeTable {
	private static final String ICON_LOCATION = "/org/mitre/opensextant/desktop/icons/";
	private JXTreeTable treeTable;
	private final OSTreeTableModel treeTableModel = generateTestModel();
	private static Logger log = LoggerFactory.getLogger(OSTreeTable.class);

	public OSTreeTable() {
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		JFrame f = new JFrame("Example of an editable JXTreeTable");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JToolBar tb = new JToolBar();
		f.add(tb, BorderLayout.NORTH);
		// tb.add(new InsertNodeAction());
		// tb.add(new DeleteNodeAction());

		OSTreeTable test = new OSTreeTable();
		JXTreeTable table = test.create();

		f.add(new JScrollPane(table));
		f.setSize(600, 300);
		f.setVisible(true);

	}

	public JXTreeTable create() {
                // Must override the tooltip renderer for the entire table to get at individual component tips
                class TooltipJXTreeTable extends JXTreeTable {
                    TooltipJXTreeTable(OSTreeTableModel model) { 
                    super(model); 
                }

                @Override
                public String getToolTipText(MouseEvent event) {
                    String tip = null;
                    Point p = event.getPoint();
                
                    // Locate the renderer under the event location
                    int hitColumnIndex = columnAtPoint(p);
                    int hitRowIndex = rowAtPoint(p);
                
                    if (hitColumnIndex != -1 && hitRowIndex != -1) {
                        TableCellRenderer renderer = getCellRenderer(hitRowIndex, hitColumnIndex);
                        Component component = prepareRenderer(renderer, hitRowIndex, hitColumnIndex);
                        Rectangle cellRect = getCellRect(hitRowIndex, hitColumnIndex, false);
                        component.setBounds(cellRect);
                        component.validate();
                        component.doLayout();
                        p.translate(-cellRect.x, -cellRect.y);
                        Component comp = component.getComponentAt(p);
                        if (comp instanceof JComponent) {
                            String txt = ((JComponent) comp).getToolTipText();
                            if(txt != null) return txt;
                        }
                    }
                    if(hitRowIndex >= 0) {
                      TreePath path = treeTable.getPathForRow(hitRowIndex);
                      DefaultMutableTreeTableNode node = (DefaultMutableTreeTableNode) path.getLastPathComponent();
                      OSRow row = (OSRow) node.getUserObject();
                      return row.getInfo();
                    } else return getToolTipText();
                  }
                }
		treeTable = new TooltipJXTreeTable(treeTableModel);

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
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				OSRow osRow = (OSRow) value;
				
				RowDurationImpl panel = osRow.getDurationPanel();
				panel.updateDuration(osRow);

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
		treeTable.getColumn(OSTreeTableModel.LAST_RUN).setHeaderRenderer(new SortIconTableHeaderRenderer());


		treeTable.setEditable(true);
		treeTable.setRowHeight(30);
		treeTable.getTableHeader().setReorderingAllowed(false);

		treeTable.setTreeCellRenderer(new DefaultTreeCellRenderer() {

			public java.awt.Component getTreeCellRendererComponent(javax.swing.JTree tree, Object value, boolean sel, boolean expanded,
					boolean leaf, int row, boolean hasFocus) {

				super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

				if (value instanceof DefaultMutableTreeTableNode) {
					DefaultMutableTreeTableNode node = (DefaultMutableTreeTableNode) value;
					setText(((OSRow) node.getUserObject()).getTitle());
					// setIcon();
				}

				return this;
			};

		});

		treeTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {

					List<Integer> rows = Arrays.asList(ArrayUtils.toObject(treeTable.getSelectedRows()));
					int r = treeTable.rowAtPoint(e.getPoint());

					if (!rows.contains(r)) {
						if (r >= 0 && r < treeTable.getRowCount()) {
							treeTable.setRowSelectionInterval(r, r);
						}
					}

					JPopupMenu popup = new JPopupMenu();
					popup.add(new DeleteNodeAction());
					popup.add(new ReRunAction());
					popup.add(new ViewResultsAction());
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});

		JTableHeader header = treeTable.getTableHeader();

		header.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				JTableHeader h = (JTableHeader) e.getSource();
				int nColumn = h.columnAtPoint(e.getPoint());

				if (nColumn != -1)
					sortColumn(nColumn);
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
		return treeTable;
	}

	public OSRow createRow(OSRow row) {
		DefaultMutableTreeTableNode root = (DefaultMutableTreeTableNode) treeTable.getTreeTableModel().getRoot();

		DefaultMutableTreeTableNode node = new DefaultMutableTreeTableNode(row);
		if (row.hasChildren()) {
			for (OSRow child : row.getChildren()) {
				DefaultMutableTreeTableNode childNode = new DefaultMutableTreeTableNode(child);
				node.add(childNode);
			}
		}

		((DefaultTreeTableModel) treeTable.getTreeTableModel()).insertNodeInto(node, root, root.getChildCount());

		return row;

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
	}

	@SuppressWarnings("serial")
	class DeleteNodeAction extends AbstractAction {
		DeleteNodeAction() {
			super("Delete/Cancel");
		}

		public void actionPerformed(ActionEvent e) {
			TreePath[] paths = treeTable.getTreeSelectionModel().getSelectionPaths();

			if (!MainFrameTableHelper.confirmationPrompt("Remove all selected jobs? WARNING: Removing children will remove the entire job.", "Confirm removing jobs", treeTable))
				return;

			Set<OSRow> rows = new HashSet<OSRow>();
			for (TreePath selp : paths) {
				DefaultMutableTreeTableNode p = (DefaultMutableTreeTableNode) selp.getLastPathComponent();
				OSRow row = (OSRow) p.getUserObject();
				if (row.isChild()) row = row.getParent();
				rows.add(row);
			}
			
			for (OSRow row : rows) {
				row.cancelExecution(false);
				row.deleteFile();
				removeRow(row);
			}

			treeTable.repaint();
		}
	}

	@SuppressWarnings("serial")
	class ViewResultsAction extends AbstractAction {
		ViewResultsAction() {
			super("View Results");
		}

		public void actionPerformed(ActionEvent e) {
			TreePath[] paths = treeTable.getTreeSelectionModel().getSelectionPaths();
			Set<OSRow> rows = new HashSet<OSRow>();
			for (TreePath selp : paths) {
				DefaultMutableTreeTableNode p = (DefaultMutableTreeTableNode) selp.getLastPathComponent();
				OSRow row = (OSRow) p.getUserObject();
				if (row.isChild()) row = row.getParent();
				rows.add(row);
			}
			for (OSRow row : rows) {
				row.viewResults();
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
