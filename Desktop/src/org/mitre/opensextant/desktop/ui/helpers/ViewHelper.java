/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mitre.opensextant.desktop.ui.helpers;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreePath;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.mitre.opensextant.desktop.ui.table.OSRow;

/**
 *
 * @author GBLACK
 */
public class ViewHelper {

    private static ViewHelper instance = new ViewHelper();
    private Map<String, Boolean> visibleCols = new HashMap<String, Boolean>();
    private Map<String, Integer> colWidth = new HashMap<String, Integer>();
    private String[] colOrder = {"Filename", "Progress", "Time", "Actions", "File Size", "Output Type", "Last Run"
    };

    public static ViewHelper getInstance() {
        return instance;
    }

    public void initialize(JXTreeTable table) {
        for (String x : colOrder) {
            visibleCols.put(x, Boolean.TRUE);
            colWidth.put(x, table.getColumn(x).getWidth());
        }
    }

    public JPopupMenu makePopup(JXTreeTable table) {
        JPopupMenu ret = new JPopupMenu();
        for (String x : colOrder) {
            boolean visible = visibleCols.get(x);
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(x, visible);
            item.addActionListener(new MenuItemAction(x, visible, table));
            ret.add(item);
        }
        return ret;
    }

    @SuppressWarnings("serial")
    class MenuItemAction extends AbstractAction {

        private JXTreeTable table;
        private String name;
        private boolean visible;

        MenuItemAction(String name, boolean visible, JXTreeTable table) {
            super(name);
            this.table = table;
            this.name = name;
            this.visible = visible;
        }

        public void actionPerformed(ActionEvent e) {
            TableColumn c = table.getColumn(name);
            ViewHelper v = ViewHelper.getInstance();
            if (visible) {
                c.setMinWidth(0);
                c.setMaxWidth(0);
                c.setPreferredWidth(0);
                v.visibleCols.put(name, false);
            } else {
                c.setMinWidth(2);
                c.setMaxWidth(10000);
                c.setPreferredWidth(v.colWidth.get(name));
                v.visibleCols.put(name, true);    
            }
        }
    }
}
