/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mitre.opensextant.desktop.util;

import javax.swing.JOptionPane;

import org.mitre.opensextant.apps.Config;
import org.mitre.opensextant.apps.OpenSextantRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author GBLACK
 */
public class Initialize implements Runnable {
	private static boolean isInitialized = false;
	private static boolean startedInit = false;

	public static boolean getInitialized() {
		return isInitialized;
	}

	private static Logger log = LoggerFactory.getLogger(Initialize.class);

	public void run() {
		try {
			log.info("Running initialization");
			log.info("Gate home: " + Config.GATE_HOME);
			log.info("Solr home: " + Config.SOLR_HOME);
			(new OpenSextantRunner()).initialize();
			log.info("runner initialized");
		} catch (Throwable e) {
			log.error("error initializing: ", e);
			JOptionPane.showMessageDialog(null, "Error initializing, " + e.getMessage(), "Error Initializing", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}

		Initialize.isInitialized = true;
	}

	public static void init() {
		log.info("initializing...");
		if (startedInit)
			return;
		startedInit = true;

		(new Thread(new Initialize())).start();
	}

}
