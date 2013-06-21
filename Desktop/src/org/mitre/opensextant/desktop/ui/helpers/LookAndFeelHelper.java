/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mitre.opensextant.desktop.ui.helpers;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.plaf.ActionMapUIResource;

/**
 *
 * @author GBLACK
 */
public class LookAndFeelHelper {
    public static void configureOptionPane() {
        UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);
        if (UIManager.getLookAndFeelDefaults().get("OptionPane.actionMap") == null) {
            UIManager.put("OptionPane.windowBindings", new Object[]{
                "ESCAPE", "close",
                "LEFT", "left",
                "KP_LEFT", "left",
                "RIGHT", "right",
                "KP_RIGHT", "right"
            });
            ActionMap map = new ActionMapUIResource();
            map.put("close", new OptionPaneCloseAction());
            map.put("left", new OptionPaneArrowAction(false));
            map.put("right", new OptionPaneArrowAction(true));
            UIManager.getLookAndFeelDefaults().put("OptionPane.actionMap", map);

        }
    }

    @SuppressWarnings("serial")
    private static class OptionPaneCloseAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            JOptionPane optionPane = (JOptionPane) e.getSource();
            optionPane.setValue(JOptionPane.CLOSED_OPTION);
        }
    }

    @SuppressWarnings("serial")
    private static class OptionPaneArrowAction extends AbstractAction {

        private boolean myMoveRight;

        OptionPaneArrowAction(boolean moveRight) {
            myMoveRight = moveRight;
        }

        public void actionPerformed(ActionEvent e) {
            JOptionPane optionPane = (JOptionPane) e.getSource();
            EventQueue eq = Toolkit.getDefaultToolkit().getSystemEventQueue();
            eq.postEvent(new KeyEvent(
                    optionPane,
                    KeyEvent.KEY_PRESSED,
                    e.getWhen(),
                    (myMoveRight) ? 0 : InputEvent.SHIFT_DOWN_MASK,
                    KeyEvent.VK_TAB,
                    KeyEvent.CHAR_UNDEFINED,
                    KeyEvent.KEY_LOCATION_UNKNOWN));
        }
    }
}
