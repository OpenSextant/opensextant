package org.mitre.opensextant.desktop;

import java.awt.Color;
import java.io.File;

import javax.swing.UIManager;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.xml.DOMConfigurator;
import org.mitre.opensextant.apps.AppBase;
import org.mitre.opensextant.apps.Config;
import org.mitre.opensextant.desktop.ui.OpenSextantMainFrameImpl;
import org.mitre.opensextant.desktop.ui.SelectOSHomeFrameImpl;
import org.mitre.opensextant.desktop.ui.helpers.ConfigHelper;
import org.mitre.opensextant.desktop.ui.helpers.LookAndFeelHelper;
import org.mitre.opensextant.desktop.util.Initialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
	
	// OPENSEXTANT-334: due to a bug in java we cannot simply use user.home... we need to set it up properly using USERPROFILE if that exists.
	static {
		System.setProperty("osd.log.root", ConfigHelper.DATA_HOME);
	}

	private static Logger log;

	public static void main(String[] args) {

		DOMConfigurator.configure(Main.class.getResource("/log4j_config.xml"));
		log = LoggerFactory.getLogger(Main.class);
		
		try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		    LookAndFeelHelper.configureOptionPane();
                    // Nimbus look and feel
                    /* for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                        if ("Nimbus".equals(info.getName())) {
                            UIManager.setLookAndFeel(info.getClassName());
                            break;
                        }
                    }*/
                } catch (Exception e) {
			e.printStackTrace();
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread() { public void run() {
			log.info("*** Shutting down Solr");
			AppBase.globalShutdown();
		}});


		FileUtils.deleteQuietly(new File(ConfigHelper.getInstance().getOSTmpRoot()));

		SelectOSHomeFrameImpl.setupOpenSextantHome();

		String osHome = ConfigHelper.getInstance().getOsHome();
		String gateHome = ConfigHelper.getInstance().getGateHome();
		String solrHome = ConfigHelper.getInstance().getSolrHome();

		if ((SelectOSHomeFrameImpl.validateOSHome(osHome) || (SelectOSHomeFrameImpl.validateHomeDir(gateHome) && SelectOSHomeFrameImpl
				.validateHomeDir(solrHome)))) {
			if (osHome != null) {
				System.setProperty("opensextant.home", osHome);
			}

			Config.GATE_HOME = gateHome;
			Config.SOLR_HOME = solrHome;

			Initialize.init();
			openMainWindow();
		}

		UIManager.put("ProgressBar.foreground", new Color(133, 196, 17));

	}

	public static void openMainWindow() {
		log.info("Starting Desktop Client");
		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new OpenSextantMainFrameImpl().setVisible(true);
			}
		});

	}

}
