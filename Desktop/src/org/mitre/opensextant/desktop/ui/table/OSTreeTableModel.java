package org.mitre.opensextant.desktop.ui.table;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableNode;

class OSTreeTableModel extends DefaultTreeTableModel {
	private static final int TITLE = 0;
	private static final int PROGRESS = 1;
	private static final int ACTIONS = 2;
	private static final int LAST_RUN = 3;
	private SimpleDateFormat dateFormat;

        private static boolean[] ascSort = new boolean[LAST_RUN + 1];
        
	public OSTreeTableModel(TreeTableNode root) {
		super(root);
		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	}

	public int getColumnCount() {
		return 4;
	}

	/**
	 * Returns which object is displayed in this column.
	 */
	public Object getValueAt(Object node, int column) {
		Object res = "n/a";
		if (node instanceof DefaultMutableTreeTableNode) {
			DefaultMutableTreeTableNode defNode = (DefaultMutableTreeTableNode) node;
			if (defNode.getUserObject() instanceof OSRow) {
				OSRow person = (OSRow) defNode.getUserObject();
				switch (column) {
				case PROGRESS:
					res = person;
					break;
				case ACTIONS:
					res = person;
					break;
				case TITLE:
					res = person.getTitle();
					break;
				case LAST_RUN:
					res = dateFormat.format(person.getLastRun());
					break;
				}
			}
		}
		return res;
	}

	/**
	 * What the TableHeader displays when the Table is in a JScrollPane.
	 */
	public String getColumnName(int column) {
		String res = "";
		switch (column) {
		case PROGRESS:
			res = "Progress";
			break;
		case TITLE:
			res = "Title";
			break;
		case ACTIONS:
			res = "";
			break;
		case LAST_RUN:
			res = "Last Run";
			break;
		}
		return res;
	}

	/**
	 * Tells if a column can be edited.
	 */
	public boolean isCellEditable(Object node, int column) {
		return true;
	}
	
	/**
	 * Called when done editing a cell.
	 */
	public void setValueAt(Object value, Object node, int column) {
		if (node instanceof DefaultMutableTreeNode) {
			DefaultMutableTreeNode defNode = (DefaultMutableTreeNode) node;
			if (defNode.getUserObject() instanceof OSRow) {
				OSRow row = (OSRow) defNode.getUserObject();
			}
		}
	}
	

	public void update(DefaultMutableTreeTableNode row) {
		modelSupport.firePathChanged((new TreePath(getPathToRoot(row))));
	}
	
        
        /**
         * Used to sort the rows depending on the column clicked
         */
        public static void sortRows(ArrayList<DefaultMutableTreeTableNode> nodes, final int nColumn){
            final boolean asc = ascSort[nColumn];
            ascSort[nColumn] = !asc;
                  
            Collections.sort(nodes, new Comparator<DefaultMutableTreeTableNode>() {
              @Override
              public int compare(DefaultMutableTreeTableNode left, DefaultMutableTreeTableNode right) {
                  OSRow l = (OSRow) left.getUserObject();
                  OSRow r = (OSRow) right.getUserObject();
                 
                  switch (nColumn) {
                    case LAST_RUN:
                        Date rd = r.getLastRun();
                        Date ld = l.getLastRun();
                        if(rd == null) rd = new Date(0);
                        if(ld == null) ld = new Date(0);
                        if(asc) return rd.compareTo(ld);
                        else return ld.compareTo(rd);
                    case TITLE:
                        if(asc) return r.getTitle().compareToIgnoreCase(l.getTitle());
                        return l.getTitle().compareToIgnoreCase(r.getTitle());
                    case PROGRESS:
                        if(asc) return ((Integer)r.getPercent()).compareTo(l.getPercent());
                        else return ((Integer)l.getPercent()).compareTo(r.getPercent());
                    default: 
                        return 0;
                  }
            }});
        }
}
