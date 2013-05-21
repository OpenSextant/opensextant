package org.mitre.opensextant.desktop;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.UIManager;

import org.mitre.opensextant.desktop.ui.OpenSextantMainFrameImpl;
import org.mitre.opensextant.desktop.ui.SelectOSHomeFrameImpl;
import org.mitre.opensextant.desktop.ui.forms.ConfigFrame;
import org.mitre.opensextant.desktop.ui.helpers.ConfigHelper;
import org.mitre.opensextant.desktop.util.Initialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	private static Logger log = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		log.info("loaded config");

		Properties props = System.getProperties();

		String osHome = ConfigHelper.getInstance().getOsHome();

		if (osHome == null || osHome.trim().length() == 0) {
			osHome = props.getProperty("opensextant.home");
			if (osHome == null) {

				List<String> osHomes = new ArrayList<String>() {
					private static final long serialVersionUID = 2733142560232972138L;
					{
						add("opensextant");
						add((new File("")).getAbsolutePath() + File.separator + "opensextant");
						add((new File("")).getAbsolutePath() + File.separator + "dist" + File.separator + "opensextant");
					}
				};

				for (String potentialHome : osHomes) {
					if ((new File(potentialHome)).exists()) {
						osHome = potentialHome;
						log.info("Open sextant home set to: " + osHome);
						break;
					}
				}

			}

			if (osHome == null || !(new File(osHome)).exists()) {
				new SelectOSHomeFrameImpl();
			}
		}

		props.setProperty("opensextant.home", osHome);
		ConfigHelper.getInstance().setOsHome(osHome);
		ConfigHelper.getInstance().saveSettings();
		Initialize.init();
		


		openMainWindow();

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
