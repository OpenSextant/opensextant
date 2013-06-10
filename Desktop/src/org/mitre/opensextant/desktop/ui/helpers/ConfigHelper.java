package org.mitre.opensextant.desktop.ui.helpers;

import java.awt.Container;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.mitre.opensextant.desktop.ui.OpenSextantMainFrameImpl;
import org.mitre.opensextant.desktop.ui.forms.ConfigFrame;
import org.mitre.opensextant.desktop.ui.table.OSRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigHelper {

	private static Logger log = LoggerFactory.getLogger(ConfigFrame.class);
	private static final String DATA_HOME = System.getProperty("user.home") + File.separator + "Documents" + File.separator + "OpenSextant";
	private static final String OUTPUT_HOME = DATA_HOME + File.separator + "output";
	private static final String CONFIG_FILE = DATA_HOME + File.separator + "conf.properties";
        private static final String JOBS_FILE = DATA_HOME + File.separator + "jobs.properties";
	private static final int CONFIG_VERSION = 1;
	public static final String OSD_TMP_BASE = "osd-tmp";
	
	private static final List<String> DEFAULT_OUT_TYPE = new ArrayList<String>() {{
		add("CSV");
	}};

	private PropertiesConfiguration config = null;
	private PropertiesConfiguration jobs = null;
	

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
	
	
	private ConfigHelper() {
		try {
			File dataHome = new File(OUTPUT_HOME);
			if (!dataHome.exists()) dataHome.mkdirs();
			
			File settingsFile = new File(CONFIG_FILE);
			if (!settingsFile.exists()) {
				settingsFile.createNewFile();
			}
			config = new PropertiesConfiguration(CONFIG_FILE);
                        
                        settingsFile = new File(JOBS_FILE);
			if (!settingsFile.exists()) settingsFile.createNewFile();
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
			config.setProperty("tmpLocation", tmpRoot);
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

	
	private void loadConfig() {
		
		outTypes = (List<String>)(List<?>)config.getList("outType", (List<Object>)(List<?>)DEFAULT_OUT_TYPE);
		outLocation = config.getString("outLocation", OUTPUT_HOME);
		if (!(new File(outLocation).exists())) {
			(new File(outLocation)).mkdir();
		}
		inLocation = config.getString("inLocation", "");
		tmpRoot = config.getString("tmpLocation", System.getProperty("java.io.tmpdir"));
		if (tmpRoot == null || tmpRoot.isEmpty()) tmpRoot = System.getProperty("java.io.tmpdir");
		osHome = config.getString("osHome", null);
		gateHome = config.getString("gateHome", null);
		solrHome = config.getString("solrHome", null);
		numThreads = config.getInt("numThreads", 1);
		configVersion = config.getInt("configVersion", CONFIG_VERSION);
        }
        
        public void loadRows(ApiHelper apiHelper, MainFrameTableHelper tableHelper) {
             if(true) return;  
             Iterator<String> i = jobs.getKeys("rows");
                String rowName = "";
                while( i.hasNext()) {
                    String[] rowValues = jobs.getStringArray(i.next());
                    String status = rowValues[4];
                    
                    // If we were waiting to run or hadn't run yet, redo the job from the start
                    if("INITIALIZING".equals(status) || "QUEUED".equals(status) || "PROCESSING".equals(status)) {
                      apiHelper.processFile(rowValues[1]);
                    } else {
                      System.out.println(">>>>>>>>>>>>>>row: " + rowValues[0] + " -- " + rowValues[8]);
                      if(!(rowValues[8]).contains("null")) continue; // Has parent
                      OSRow row = new OSRow(rowValues, tableHelper);
                      tableHelper.addRow(row);
                      System.out.println("HERE>>>");
                      
                    }
                }
        }

	
	public List<String> getOutTypes() {
		return outTypes;
	}
	
	public static String getOutTypesString(List<String> outTypes) {
		String out = "";
		for (int i = 0; i < outTypes.size(); i++) {
			if (i > 0) out += ",";
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

    public String getTmpLocation() {
		return tmpRoot + File.separator + OSD_TMP_BASE;
	}
    public String getTmpRoot() {
		return tmpRoot;
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
	
        
	public void setTmpLocation(String tmpLocation) {
		this.tmpRoot = tmpLocation;
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
