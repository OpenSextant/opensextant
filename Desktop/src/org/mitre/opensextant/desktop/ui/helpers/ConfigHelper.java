package org.mitre.opensextant.desktop.ui.helpers;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.mitre.opensextant.desktop.ui.forms.ConfigFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigHelper {

	private static Logger log = LoggerFactory.getLogger(ConfigFrame.class);

	private static final String CONFIG_FILE = "./conf.properties";
	private PropertiesConfiguration config = null;

	private String outType = "";
	private String outLocation = "";
	private String inLocation = "";
	private String osHome = "";
	private int numThreads = 1;

	private static ConfigHelper instance = new ConfigHelper();
	
	public static ConfigHelper getInstance() {
		return instance;
	}
	
	
	private ConfigHelper() {
		try {
			File settingsFile = new File(CONFIG_FILE);
			if (!settingsFile.exists()) {
				settingsFile.createNewFile();
			}
			config = new PropertiesConfiguration(CONFIG_FILE);
		} catch (ConfigurationException ex) {
			log.error(ex.getMessage());
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		
		loadConfig();
	}
	
	public synchronized void saveSettings() {

		try {
			config.setProperty("outType", outType);
			config.setProperty("outLocation", outLocation);
			config.setProperty("inLocation", inLocation);
			config.setProperty("osHome", osHome);
			config.setProperty("numThreads", numThreads);
			config.save();
		} catch (ConfigurationException e) {
			log.error("Error saving settings", e);
		}

	}

	
	private void loadConfig() {

		outType = config.getString("outType", "CSV");
		outLocation = config.getString("outLocation", new File("output").getAbsolutePath());
		if (!(new File(outLocation).exists())) {
			(new File(outLocation)).mkdir();
		}
		inLocation = config.getString("inLocation", "");
		osHome = config.getString("osHome", null);
		numThreads = config.getInt("numThreads", 1);

	}

	
	public String getOutType() {
		return outType;
	}

	public String getOutLocation() {
		return outLocation;
	}

	public String getInLocation() {
		return inLocation;
	}

	public String getOsHome() {
		return osHome;
	}

	
	public void setOutType(String outType) {
		this.outType = outType;
	}

	public void setOutLocation(String outLocation) {
		this.outLocation = outLocation;
	}

	public void setInLocation(String loc) {
		inLocation = loc;
	}

	public void setOsHome(String osHomeProp) {
		osHome = osHomeProp;
	}


	public int getNumThreads() {
		return numThreads;
	}


	public void setNumThreads(int numThreads) {
		this.numThreads = numThreads;
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		
	}

}
