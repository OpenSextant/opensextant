package org.mitre.opensextant.desktop.ui.handlers;

import java.awt.Desktop;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelpKeyListener implements KeyListener {

    private static Logger log = LoggerFactory.getLogger(HelpKeyListener.class);
	public static final String HELP_FILE = "/help/OpenSextant_Desktop.html";

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_F1) {
			String path = System.getProperty("user.dir") + HELP_FILE;

			try {
				Desktop.getDesktop().open(new File(path));
			} catch (IOException ex) {
				log.error(ex.getMessage());
			}

		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}
};
