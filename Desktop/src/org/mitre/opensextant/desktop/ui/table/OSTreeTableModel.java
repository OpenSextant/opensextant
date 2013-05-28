package org.mitre.opensextant.desktop.ui.table;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.io.FileUtils;
import org.jdesktop.swingx.JXTreeTable;

import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableNode;
import org.mitre.opensextant.desktop.util.FileSize;

class OSTreeTableModel extends DefaultTreeTableModel {
	public static final int TITLE = 0;
	public static final int PROGRESS = 1;
	public static final int TIMING = 2;
	public static final int ACTIONS = 3;
	public static final int FILE_INFO = 4;
	public static final int LAST_RUN = 5;
	private SimpleDateFormat dateFormat;

	private boolean[] ascSort = new boolean[LAST_RUN + 1];

	public OSTreeTableModel(TreeTableNode root) {
		super(root);
		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	}

	public int getColumnCount() {
		return 6;
	}

	/**
	 * Returns which object is displayed in this column.
	 */
	public Object getValueAt(Object node, int column) {
		OSRow row = (OSRow) ((DefaultMutableTreeTableNode) node).getUserObject();
		switch (column) {
			case TITLE:
				return row.getTitle();
			case TIMING:
				return row;
			case PROGRESS:
				return row;
			case ACTIONS:
				return row;
			case FILE_INFO:
				String info = "";
				if (row.hasChildren()) {
					long size = 0;
					for (OSRow child : row.getChildren()) {
						size += FileUtils.sizeOf(child.getInputFile());
					}
					info += FileSize.byteCountToDisplaySize(size);
					info += " (" + row.getChildren().size() + " files)";
				} else {
					info += FileSize.byteCountToDisplaySize(FileUtils.sizeOf(row.getInputFile()));
					
				}
				return info;
			case LAST_RUN:
				return dateFormat.format(row.getStartTime());
		}
		return "n/a";
	}

	/**
	 * What the TableHeader displays when the Table is in a JScrollPane.
	 */
	public String getColumnName(int column) {
		switch (column) {
			case TITLE:
				return "Filename";
			case PROGRESS:
				return "Progress";
			case TIMING:
				return "Time";
			case ACTIONS:
				return "Actions";
			case FILE_INFO:
				return "File Size";
			case LAST_RUN:
				return "Last Run";
		}
		return "";
	}

	/**
	 * Tells if a column can be edited.
	 */
	public boolean isCellEditable(Object node, int column) {
		return column == ACTIONS && !((OSRow) ((DefaultMutableTreeTableNode) node).getUserObject()).isChild();
	}

	/**
	 * Called when done editing a cell.
	 */
	public void setValueAt(Object value, Object node, int column) {
	}

	public void update(DefaultMutableTreeTableNode row) {
		modelSupport.firePathChanged((new TreePath(getPathToRoot(row))));
	}

	/**
	 * Get which rows are sorted ascending
	 */
	public boolean getSortAscending(int column) {
		return ascSort[column];
	}

	/**
	 * Used to sort the rows depending on the column clicked
	 */
	public void sortRows(ArrayList<DefaultMutableTreeTableNode> nodes, final int nColumn, JXTreeTable caller) {
		final boolean asc = ascSort[nColumn];
		ascSort[nColumn] = !asc;
		caller.getTableHeader().repaint();

		Collections.sort(nodes, new Comparator<DefaultMutableTreeTableNode>() {
			@Override
			public int compare(DefaultMutableTreeTableNode left, DefaultMutableTreeTableNode right) {
				OSRow l = (OSRow) left.getUserObject();
				OSRow r = (OSRow) right.getUserObject();

				switch (nColumn) {
				case LAST_RUN:
					Date rd = r.getStartTime();
					Date ld = l.getStartTime();
					if (rd == null)
						rd = new Date(0);
					if (ld == null)
						ld = new Date(0);
					if (asc)
						return rd.compareTo(ld);
					else
						return ld.compareTo(rd);
				case TITLE:
					if (asc)
						return r.getTitle().compareToIgnoreCase(l.getTitle());
					return l.getTitle().compareToIgnoreCase(r.getTitle());
				case PROGRESS:
					if (asc)
						return ((Integer) r.getPercent()).compareTo(l.getPercent());
					else
						return ((Integer) l.getPercent()).compareTo(r.getPercent());
				default:
					return 0;
				}
			}
		});
	}
}
