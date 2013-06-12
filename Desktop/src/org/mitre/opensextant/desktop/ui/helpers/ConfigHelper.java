package org.mitre.opensextant.desktop.ui.helpers;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.mitre.opensextant.desktop.ui.forms.ConfigFrame;
import org.mitre.opensextant.desktop.ui.table.OSRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigHelper {

	private static Logger log = LoggerFactory.getLogger(ConfigFrame.class);
	public static final String DATA_HOME = getUserHome() + File.separator + "Documents" + File.separator + "OpenSextant";
	private static final String OUTPUT_HOME = DATA_HOME + File.separator + "output";
	private static final String CONFIG_FILE = DATA_HOME + File.separator + "conf.properties";
	private static final String JOBS_FILE = DATA_HOME + File.separator + "jobs.properties";
	private static final String DEFAULT_CACHE_ROOT = DATA_HOME + File.separator + "cache";
	private static final int CONFIG_VERSION = 1;

	public static final int ROW_ID = 0;
	public static final int ROW_TITLE = 1;
	public static final int ROW_INPUT = 2;
	public static final int ROW_BASELOC = 3;
	public static final int ROW_OUTPUT = 4;
	public static final int ROW_TYPES = 5;
	public static final int ROW_STATUS = 6;
	public static final int ROW_START = 7;
	public static final int ROW_DURATION = 8;
	public static final int ROW_DURATION_STRING = 9;
	public static final int ROW_CHILDREN = 10;
	public static final int ROW_PARENT = 11;

	public static final String OSD_TMP_BASE = "osd-tmp";

	private static final List<String> DEFAULT_OUT_TYPE = new ArrayList<String>() {
		{
			add("CSV");
		}
	};

	private PropertiesConfiguration config = null;
	private PropertiesConfiguration jobs = null;

	private String cacheRoot = "";
	private String tmpRoot = "";
	private List<String> outTypes = new ArrayList<String>();
	private String outLocation = "";
	private String inLocation = "";
	private String osHome = "";
	private int numThreads = 1;
	private int configVersion;

	private List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();

	private String gateHome;

	private String solrHome;

	private static ConfigHelper instance = new ConfigHelper();

	public static ConfigHelper getInstance() {
		return instance;
	}

	private static String getUserHome() {
		String userHome = System.getenv("USERPROFILE");
		if (userHome == null) userHome = System.getProperty("user.home");
		return userHome;
	}

	private ConfigHelper() {
		try {
			File dataHome = new File(OUTPUT_HOME);
			if (!dataHome.exists())
				dataHome.mkdirs();

			File settingsFile = new File(CONFIG_FILE);
			if (!settingsFile.exists()) {
				settingsFile.createNewFile();
			}
			config = new PropertiesConfiguration(CONFIG_FILE);

			settingsFile = new File(JOBS_FILE);
			if (!settingsFile.exists())
				settingsFile.createNewFile();
			jobs = new PropertiesConfiguration(JOBS_FILE);
		} catch (ConfigurationException ex) {
			log.error(ex.getMessage());
		} catch (IOException e) {
			log.error(e.getMessage());
		}

		loadConfig();
	}

	public synchronized void updateRow(String id, String[] rowValues) {
		jobs.setProperty("rows." + id, rowValues);
		saveSettings();
	}

	public synchronized void saveSettings() {

		try {
			config.setProperty("outType", outTypes);
			config.setProperty("outLocation", outLocation);
			config.setProperty("inLocation", inLocation);
			config.setProperty("cacheRoot", cacheRoot);
			config.setProperty("tmpRoot", tmpRoot);
			config.setProperty("osHome", osHome);
			config.setProperty("gateHome", gateHome);
			config.setProperty("solrHome", solrHome);
			config.setProperty("numThreads", numThreads);
			config.setProperty("configVersion", configVersion);
			config.save();
			jobs.save();
			fireUpdate();
		} catch (ConfigurationException e) {
			log.error("Error saving settings", e);
		}

	}

	public void removeRow(OSRow r) {
		jobs.clearProperty("rows." + r);
	}

	private void loadConfig() {

		outTypes = (List<String>) (List<?>) config.getList("outType", (List<Object>) (List<?>) DEFAULT_OUT_TYPE);
		outLocation = config.getString("outLocation", OUTPUT_HOME);
		if (!(new File(outLocation).exists())) {
			(new File(outLocation)).mkdir();
		}
		inLocation = config.getString("inLocation", "");
		cacheRoot = config.getString("cacheRoot", DEFAULT_CACHE_ROOT);
		tmpRoot = config.getString("tmpRoot", System.getProperty("java.io.tmpdir"));
		osHome = config.getString("osHome", null);
		gateHome = config.getString("gateHome", null);
		solrHome = config.getString("solrHome", null);
		numThreads = config.getInt("numThreads", 1);
		configVersion = config.getInt("configVersion", CONFIG_VERSION);
	}

	public void loadRows(ApiHelper apiHelper, MainFrameTableHelper tableHelper) {
		Iterator<String> i = jobs.getKeys("rows");
		String rowName = "";
		while (i.hasNext()) {
			String[] rowValues = jobs.getStringArray(i.next());
			String status = rowValues[ROW_STATUS];

			// Has a parent, add it later
			if (!(rowValues[ROW_PARENT]).contains("null")) {
				continue;
			}

			// Never finished running, start it again
			if ("INITIALIZING".equals(status) || "QUEUED".equals(status) || "PROCESSING".equals(status)) {
				apiHelper.processFile(rowValues[ROW_INPUT]);
				continue;
			}
			OSRow row = new OSRow(rowValues, tableHelper, null);
			String[] children = rowValues[ROW_CHILDREN].split(":");

			// No children, add the row early
			if (rowValues[ROW_CHILDREN].length() == 0) {
				tableHelper.addRow(row);
				continue;
			}

			// Go through and add all children
			for (String c : children) {
				String[] rowValsChild = jobs.getStringArray("rows." + c);
				OSRow child = new OSRow(rowValsChild, tableHelper, row);
				row.addChild(child);
			}
			tableHelper.addRow(row);
		}
	}

	public List<String> getOutTypes() {
		return outTypes;
	}

	public static String getOutTypesString(List<String> outTypes) {
		String out = "";
		for (int i = 0; i < outTypes.size(); i++) {
			if (i > 0)
				out += ",";
			out += outTypes.get(i);
		}
		return out;
	}

	public static List<String> parseOutTypesString(String outTypesString) {
		return Arrays.asList(outTypesString.split(","));
	}

	public String getOutLocation() {
		return outLocation;
	}

	public String getCacheRoot() {
		return cacheRoot;
	}

	public String getTmpRoot() {
		return tmpRoot;
	}

	public String getXTextCacheRoot() {
		return getCacheRoot() + File.separator + "xtext";
	}

	public String getOSTmpRoot() {
		return getTmpRoot() + File.separator + "opensextant";
	}

	public String getInLocation() {
		return inLocation;
	}

	public String getOsHome() {
		return osHome;
	}

	public String getGateHome() {
		return gateHome;
	}

	public String getSolrHome() {
		return solrHome;
	}

	public void setOutTypes(List<String> outTypes) {
		this.outTypes = outTypes;
	}

	public void setCacheRoot(String cacheRoot) {
		this.cacheRoot = cacheRoot;
	}

	public void setTmpRoot(String tmpRoot) {
		this.tmpRoot = tmpRoot;
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

	public void setGateHome(String gateHomeProp) {
		gateHome = gateHomeProp;
	}

	public void setSolrHome(String solrHomeProp) {
		solrHome = solrHomeProp;
	}

	public int getNumThreads() {
		return numThreads;
	}

	public void setNumThreads(int numThreads) {
		this.numThreads = numThreads;
	}

	public void addUpdateListener(PropertyChangeListener listener) {
		listeners.add(listener);
	}

	private void fireUpdate() {
		for (PropertyChangeListener listener : listeners) {
			listener.propertyChange(null);
		}
	}

}
