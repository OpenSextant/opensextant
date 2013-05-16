/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mitre.opensextant.desktop.ui.handlers;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import org.mitre.opensextant.desktop.ui.OpenSextantMainFrameImpl;

/**
 * 
 * @author GBLACK
 */
public class OSMouseAdapter extends MouseAdapter {
	private ArrayList list;
	private OpenSextantMainFrameImpl frame;

	public OSMouseAdapter(OpenSextantMainFrameImpl frame, ArrayList list) {
		super();
		this.list = list;
		this.frame = frame;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2)
			frame.getTableHelper().viewFileFromRow(e.getSource(), list);
		else
			frame.getTableHelper().toggleCheck(e.getSource(), list);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		frame.getTableHelper().hoverRow(true, e.getSource(), list);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		frame.getTableHelper().hoverRow(false, e.getSource(), list);
	}

}
