package org.mitre.opensextant.desktop;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import org.jdesktop.swingx.JXTreeTable;
import org.mitre.opensextant.desktop.ui.OpenSextantMainFrameImpl;
import org.mitre.opensextant.desktop.ui.SelectOSHomeFrameImpl;
import org.mitre.opensextant.desktop.ui.forms.ConfigFrame;
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

		ConfigFrame.loadConfig();
		log.info("loaded config");

		Properties props = System.getProperties();

		String osHome = ConfigFrame.getOsHome();

		if (osHome == null || osHome.trim().length() == 0) {
			osHome = props.getProperty("opensextant.home");
			if (osHome == null) {

				List<String> osHomes = new ArrayList<String>() {
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
		ConfigFrame.setOsHome(osHome);
		ConfigFrame.saveSettings();
		Initialize.init();
		


		openMainWindow();

	}

	public static void openMainWindow() {
		log.info("Starting Desktop Client");
//		try {
//			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//				if ("Nimbus".equals(info.getName())) {
//					javax.swing.UIManager.setLookAndFeel(info.getClassName());
//					UIManager.getLookAndFeelDefaults().put("nimbusOrange", (Color.green));
//					break;
//				}
//			}
//		} catch (ClassNotFoundException ex) {
//			log.error(ex.getMessage());
//		} catch (InstantiationException ex) {
//			log.error(ex.getMessage());
//		} catch (IllegalAccessException ex) {
//			log.error(ex.getMessage());
//		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
//			log.error(ex.getMessage());
//		}
		// </editor-fold>
		// Initialize.init();
		// Properties props = System.getProperties();
		// if(props.getProperty("opensextant.home") == null)
		// props.setProperty( "opensextant.home"
		// , ApiHelper.BASE_PATH + "opensextant");
		/*
		 * try {
		 * UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		 * } catch (ClassNotFoundException ex) {
		 * java.util.logging.Logger.getLogger
		 * (OpenSextant.class.getName()).log(Level.SEVERE, null, ex); } catch
		 * (InstantiationException ex) {
		 * java.util.logging.Logger.getLogger(OpenSextant
		 * .class.getName()).log(Level.SEVERE, null, ex); } catch
		 * (IllegalAccessException ex) {
		 * java.util.logging.Logger.getLogger(OpenSextant
		 * .class.getName()).log(Level.SEVERE, null, ex); } catch
		 * (UnsupportedLookAndFeelException ex) {
		 * java.util.logging.Logger.getLogger
		 * (OpenSextant.class.getName()).log(Level.SEVERE, null, ex); }
		 */
		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new OpenSextantMainFrameImpl().setVisible(true);
			}
		});

	}

}
