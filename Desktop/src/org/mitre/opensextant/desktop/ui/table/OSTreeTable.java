package org.mitre.opensextant.desktop.ui.table;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultRowSorter;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.RowSorter;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.ArrayUtils;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableNode;
import org.mitre.opensextant.desktop.ui.forms.panels.RowButtonsImpl;
import org.mitre.opensextant.desktop.ui.forms.panels.RowProgressBarImpl;

public class OSTreeTable {

	private JXTreeTable treeTable;

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

		final OSTreeTableModel personTreeTableModel = generateTestModel();
		treeTable = new JXTreeTable(personTreeTableModel);

		// treeTable.getColumn(1).setCellRenderer(buttonEditor);
		// treeTable.getColumn(1).setCellEditor(buttonEditor);

		class MyTableHeaderRenderer extends JLabel implements TableCellRenderer {
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,int column) {
				TableCellRenderer defaultRenderer = table.getTableHeader().getDefaultRenderer();

				Component component = defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				((JLabel)component).setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mitre/opensextant/desktop/icons/arrow_down.png")));
				return component;
			}

		}
		treeTable.getColumn(0).setHeaderRenderer(new MyTableHeaderRenderer());
		treeTable.getColumn(0).setMinWidth(200);

		treeTable.getColumn(1).setMinWidth(120);
		treeTable.getColumn(1).setCellRenderer(new TableCellRenderer() {
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

		treeTable.getColumn(2).setMinWidth(120);
		treeTable.getColumn(2).setMaxWidth(120);
		treeTable.getColumn(2).setCellEditor(new RowButtonsEditor(new JCheckBox()));
		treeTable.getColumn(2).setCellRenderer(new TableCellRenderer() {
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
					setIcon(null);
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

				Collections.sort(nodes, new Comparator<DefaultMutableTreeTableNode>() {
                                    @Override
                                    public int compare(DefaultMutableTreeTableNode left, DefaultMutableTreeTableNode right) {
					switch (nColumn) {
                                            case 0:
                                        	return ((OSRow) left.getUserObject()).getTitle()
								.compareToIgnoreCase(((OSRow) right.getUserObject()).getTitle());
                                            case 1:
                                                Integer l = ((OSRow) left.getUserObject()).getPercent();
                                                Integer r = ((OSRow) right.getUserObject()).getPercent();
                                                return l.compareTo(r);
                                            default: 
                                                return 0;
                                        }
				}});

				for (DefaultMutableTreeTableNode node : nodes) {
					model.insertNodeInto(node, root, 0);
				}
			}
		});

		// treeTable.setRowSorter(
		return treeTable;
	}
	
	public void createRow(OSRow row) {
		DefaultMutableTreeTableNode root = (DefaultMutableTreeTableNode) treeTable.getTreeTableModel().getRoot();
		DefaultMutableTreeTableNode parent = new DefaultMutableTreeTableNode(row);
		

		((DefaultTreeTableModel) treeTable.getTreeTableModel()).insertNodeInto(parent, root, root.getChildCount());

	}

	/**
	 * Inserts a new node, the kind and name depends on the selected node.
	 * 
	 */
	class InsertNodeAction extends AbstractAction {
		InsertNodeAction() {
			super("Insert");
		}

		public void actionPerformed(ActionEvent e) {

			String title = JOptionPane.showInputDialog(SwingUtilities.windowForComponent(treeTable), "What is the title?");

			OSRow nuObj = new OSRow("Parent", "Started");
			createRow(nuObj);

		}
	}

	/**
	 * Deletes a node after one is selected.
	 * 
	 */
	class DeleteNodeAction extends AbstractAction {
		DeleteNodeAction() {
			super("Delete");
		}

		public void actionPerformed(ActionEvent e) {
			TreePath[] paths = treeTable.getTreeSelectionModel().getSelectionPaths();
			for (TreePath selp : paths) {
				DefaultMutableTreeTableNode p = (DefaultMutableTreeTableNode) selp.getLastPathComponent();
				((OSRow) p.getUserObject()).setProgress(30, "Running");
				// ((DefaultTreeTableModel)
				// treeTable.getTreeTableModel()).removeNodeFromParent(p);
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
		Set<OSRow> list = new TreeSet<OSRow>();
		DefaultMutableTreeTableNode aRoot = new DefaultMutableTreeTableNode(new OSRow());
		// gen test persons
		for (int i = 0; i < 6; i++) {
			list.add(new OSRow(TITLES[i], STATUSES[i]));
		}
		// shouldn't be visible
		int i = 0;
		for (OSRow child : list) {

			DefaultMutableTreeTableNode parent = new DefaultMutableTreeTableNode(new OSRow("Parent" + (++i), "Running"));
			parent.add(new DefaultMutableTreeTableNode(child));

			aRoot.add(parent);
		}
		aRoot.add(new DefaultMutableTreeTableNode(new OSRow("Last", "Last")));

		return new OSTreeTableModel(aRoot);
	}

	private static String[] STATUSES = { "Started", "Started", "Started", "Started", "Started", "Started" };

	private static String[] TITLES = { "Test1", "Test2", "Test3", "Test4", "Test5", "Test6" };

}