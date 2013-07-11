/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mitre.opensextant.desktop.util;

import gate.Document;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.mitre.opensextant.apps.Config;
import org.mitre.opensextant.apps.OpenSextantRunner;
import org.mitre.opensextant.desktop.executor.opensextant.ext.converter.XTextConverter;
import org.mitre.opensextant.desktop.executor.opensextant.ext.geocode.OSGeoCoder;
import org.mitre.opensextant.desktop.ui.OpenSextantMainFrameImpl;
import org.mitre.opensextant.desktop.ui.helpers.ConfigHelper;
import org.mitre.opensextant.desktop.ui.helpers.MainFrameTableHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author GBLACK
 */
public class Initialize implements Runnable {
	private static final int PRIME_COUNT = 2;
    private static boolean isInitialized = false;
	private static boolean startedInit = false;

	public static boolean getInitialized() {
		return isInitialized;
	}

	private static Logger log = LoggerFactory.getLogger(Initialize.class);

	public void run() {
	    FileWriter writer = null;
	    InputStream reader = null;
	    try {
			log.info("Running initialization");
			log.info("Gate home: " + Config.GATE_HOME);
			log.info("Solr home: " + Config.SOLR_HOME);
            XTextConverter converter = new XTextConverter(ConfigHelper.getInstance().getXTextCacheRoot());
            OSGeoCoder geoCoder = new OSGeoCoder();
            geoCoder.initialize();
            log.info("runner initialized");
            OpenSextantMainFrameImpl frame = MainFrameTableHelper.getMainFrame();
            frame.updateStatusBar("Priming Solr Index...");            
            log.info("Priming the pump by running data");
            
            File primer = File.createTempFile("world_and_us_cities", ".txt");
            primer.deleteOnExit();
            writer = new FileWriter(primer);
            reader = getClass().getResourceAsStream("/world_and_us_cities.txt");
            IOUtils.copy(reader, writer);
            List<Document> documents = converter.convert(primer);
            for (int index = 0; index < PRIME_COUNT; index++) {
                for (Document document : documents) {
                    geoCoder.geoCodeText(document);
                }
            }
            log.info("Done priming.");
            
            frame.updateStatusBar("Initialization Done");
            frame.hideStatusBar();

            
		} catch (Throwable e) {
			log.error("error initializing: ", e);
			JOptionPane.showMessageDialog(null, "Error initializing, " + e.getMessage(), "Error Initializing", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(writer);
		}
	    

		Initialize.isInitialized = true;
	}

	public static synchronized void init() {
		log.info("initializing...");
		if (startedInit)
			return;
		startedInit = true;

		(new Thread(new Initialize())).start();
	}

}
