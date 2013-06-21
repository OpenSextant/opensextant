package org.mitre.opensextant.desktop.ui.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.mitre.opensextant.desktop.ui.OpenSextantMainFrameImpl;

// Must override the tooltip renderer for the entire table to get at
// individual component tips
@SuppressWarnings("serial")
public class TooltipJXTreeTable extends JXTreeTable {

	TooltipJXTreeTable(OSTreeTableModel model) {
		super(model);
		setOpaque(false);
	}

	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
		final Component c = super.prepareRenderer(renderer, row, column);
		if (!isRowSelected(row)) {
			if (c instanceof JComponent) {
				((JComponent) c).setOpaque(false);
			}
        } else {
			if (c instanceof JComponent) {
				((JComponent) c).setOpaque(true);
			}
        }
		return c;
	}

	private final static ImageIcon image = new ImageIcon(OpenSextantMainFrameImpl.class.getResource("/org/mitre/opensextant/desktop/icons/OpenSextantLogoBigWatermark.png"));
	
	@Override
	public void paintComponent(Graphics g) {
		// draw image in centre
		final int imageWidth = image.getIconWidth();
		final int imageHeight = image.getIconHeight();
		final Dimension d = getSize();
		final int x = (d.width - imageWidth) / 2;
		final int y = (d.height - imageHeight) / 2;
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, (int)d.getWidth(), (int)d.getHeight());
		g.drawImage(image.getImage(), x, y, null, null);
		super.paintComponent(g);
	}

	@Override
	public String getToolTipText(MouseEvent event) {
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
				if (txt != null)
					return txt;
			}
		}
		if (hitRowIndex >= 0) {
			TreePath path = getPathForRow(hitRowIndex);
			DefaultMutableTreeTableNode node = (DefaultMutableTreeTableNode) path.getLastPathComponent();
			OSRow row = (OSRow) node.getUserObject();
			return row.getInfo();
		} else
			return getToolTipText();
	}
}
