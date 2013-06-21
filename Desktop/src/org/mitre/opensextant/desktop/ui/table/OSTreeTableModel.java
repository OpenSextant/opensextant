package org.mitre.opensextant.desktop.ui.table;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import javax.swing.tree.TreePath;

import org.apache.commons.io.FileUtils;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableNode;
import org.mitre.opensextant.desktop.ui.helpers.ConfigHelper;
import org.mitre.opensextant.desktop.util.FileSize;

class OSTreeTableModel extends DefaultTreeTableModel {
	public static final int TITLE = 0;
	public static final int FILE_INFO = 1;
	public static final int FILE_LOC = 2;
	public static final int PROGRESS = 3;
	public static final int TIMING = 4;
	public static final int ACTIONS = 5;
	public static final int OUT_TYPES = 6;
	public static final int LAST_RUN = 7;
	private SimpleDateFormat dateFormat;

	private boolean[] ascSort = new boolean[LAST_RUN + 1];

	public OSTreeTableModel(TreeTableNode root) {
		super(root);
		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	}

	public int getColumnCount() {
		return LAST_RUN + 1;
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
		case FILE_LOC:
			return row.getInputFile().getAbsoluteFile();
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
		case OUT_TYPES:
			return ConfigHelper.getOutTypesString(row.getOutputTypes());
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
			return "File Name";
		case PROGRESS:
			return "Progress";
		case TIMING:
			return "Time";
		case ACTIONS:
			return "Actions";
		case FILE_LOC:
			return "File Path";
		case FILE_INFO:
			return "File Size";
		case OUT_TYPES:
			return "Output Type";
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

	public synchronized void update(DefaultMutableTreeTableNode row) {
		try {
			modelSupport.firePathChanged((new TreePath(getPathToRoot(row))));
		} catch (Exception e) {
			e.printStackTrace();
		}
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
				case TIMING:
					Long rDur = new Long(r.getDurationPanel().getDuration());
					Long lDur = new Long(l.getDurationPanel().getDuration());

					if (asc)
						return rDur.compareTo(lDur);
					return lDur.compareTo(rDur);
				case FILE_INFO:
					if (asc)
						return r.getInfo().compareTo(l.getInfo());
					else
						return l.getInfo().compareTo(r.getInfo());
				case FILE_LOC:
					if (asc)
						return (r.getInputFile().getAbsoluteFile().toString()).compareTo(l.getInputFile().getAbsoluteFile().toString());
					else
						return (l.getInputFile().getAbsoluteFile().toString()).compareTo(r.getInputFile().getAbsoluteFile().toString());
				case OUT_TYPES:
					if (asc)
						return ConfigHelper.getOutTypesString(r.getOutputTypes()).compareToIgnoreCase(ConfigHelper.getOutTypesString(l.getOutputTypes()));
					return ConfigHelper.getOutTypesString(l.getOutputTypes()).compareToIgnoreCase(ConfigHelper.getOutTypesString(r.getOutputTypes()));
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
