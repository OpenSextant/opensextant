package org.mitre.opensextant.desktop;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.apache.commons.io.FileUtils;
import org.mitre.opensextant.apps.Config;
import org.mitre.opensextant.desktop.ui.OpenSextantMainFrameImpl;
import org.mitre.opensextant.desktop.ui.SelectOSHomeFrameImpl;
import org.mitre.opensextant.desktop.ui.forms.ConfigFrame;
import org.mitre.opensextant.desktop.ui.helpers.ConfigHelper;
import org.mitre.opensextant.desktop.ui.helpers.LookAndFeelHelper;
import org.mitre.opensextant.desktop.util.Initialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	private static Logger log = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {

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

		FileUtils.deleteQuietly(new File("." + File.separator + "tmp"));

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
