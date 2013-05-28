package org.mitre.opensextant.desktop.ui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.mitre.opensextant.apps.Config;
import org.mitre.opensextant.desktop.Main;
import org.mitre.opensextant.desktop.ui.forms.ConfigFrame;
import org.mitre.opensextant.desktop.ui.forms.SelectOSHomeFrame;
import org.mitre.opensextant.desktop.ui.helpers.ConfigHelper;
import org.mitre.opensextant.desktop.util.Initialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class SelectOSHomeFrameImpl extends SelectOSHomeFrame {

	private static Logger log = LoggerFactory.getLogger(Main.class);

	private static List<String> osHomes = new ArrayList<String>() {
		private static final long serialVersionUID = 2733142560232972138L;
		{
			add("opensextant");
			add("..");
			add((new File("")).getAbsolutePath() + File.separator + "opensextant");
		}
	};

	public SelectOSHomeFrameImpl() {
		super();

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
		setVisible(true);

		initialize(this);

	}

	public static void setupOpenSextantHome() {
		log.info("Getting OpenSextant Home");

		Properties props = System.getProperties();

		String osHome = ConfigHelper.getInstance().getOsHome();
		String gateHome = ConfigHelper.getInstance().getGateHome();
		String solrHome = ConfigHelper.getInstance().getSolrHome();

		if (!(validateOSHome(osHome) || (validateHomeDir(gateHome) && validateHomeDir(solrHome)))) {
			osHome = props.getProperty("opensextant.home");

			if (!validateOSHome(osHome)) {
				osHome = null;
				for (String potentialHome : osHomes) {
					if (validateOSHome(potentialHome)) {
						osHome = potentialHome;
						break;
					}
				}

			}
		}

		if (!(validateOSHome(osHome) || (validateHomeDir(gateHome) && validateHomeDir(solrHome)))) {
			new SelectOSHomeFrameImpl();
		} else {
			ConfigHelper.getInstance().setOsHome(osHome);
			if (gateHome == null)
				gateHome = getGateHome(osHome);
			if (solrHome == null)
				solrHome = getSolrHome(osHome);
			ConfigHelper.getInstance().setGateHome(gateHome);
			ConfigHelper.getInstance().setSolrHome(solrHome);

			ConfigHelper.getInstance().saveSettings();
		}

	}

	private static String getGateHome(String osHome) {
		return osHome + File.separator + "gate";
	}

	private static String getSolrHome(String osHome) {
		return osHome + File.separator + ".." + File.separator + "opensextant-solr";
	}

	public static boolean validateOSHome(String osHome) {
		return validateHomeDir(osHome) && validateHomeDir(getGateHome(osHome)) && validateHomeDir(getSolrHome(osHome));
	}

	public static boolean validateHomeDir(String homeDir) {
		if (homeDir == null)
			return false;
		File homeDirFile = new File(homeDir);
		if (!(homeDirFile.exists() && homeDirFile.isDirectory()))
			return false;
		return true;
	}

	private void initialize(final SelectOSHomeFrameImpl parent) {
		
		parent.addWindowListener( new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                showErrorDialog(parent);
            }
        } );
		
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showErrorDialog(parent);
			}
		});
		
		class ChooseActionListener implements ActionListener {
			
			private JTextField textField;
			public ChooseActionListener(JTextField textField) {
				this.textField = textField;
			}
			
			@Override
			public void actionPerformed(ActionEvent e) {
	          final JFileChooser chooser = new JFileChooser();
	          chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	          int returnVal = chooser.showOpenDialog(parent);
	          
	          if (returnVal == JFileChooser.APPROVE_OPTION) {
		          textField.setText(chooser.getSelectedFile().getAbsolutePath());
	          }

			}
		};
		
		chooseOSHomeButton.addActionListener(new ChooseActionListener(osHomeTextField));
		chooseGateHomeButton.addActionListener(new ChooseActionListener(gateHomeTextField));
		chooseSolrHomeButton.addActionListener(new ChooseActionListener(solrHomeTextField));
		
		ActionListener radioActionListener = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(osHomeRadio.isSelected()) {
					gateHomeTextField.setEnabled(false);
					solrHomeTextField.setEnabled(false);
					gateHomeTextField.setText("");
					solrHomeTextField.setText("");
					chooseSolrHomeButton.setEnabled(false);
					chooseGateHomeButton.setEnabled(false);
					gateHomeLabel.setEnabled(false);
					solrHomeLabel.setEnabled(false);

					osHomeTextField.setEnabled(true);
					chooseOSHomeButton.setEnabled(true);
				} else {
					
					osHomeTextField.setEnabled(false);
					osHomeTextField.setText("");
					chooseOSHomeButton.setEnabled(false);

					gateHomeLabel.setEnabled(true);
					solrHomeLabel.setEnabled(true);
					gateHomeTextField.setEnabled(true);
					solrHomeTextField.setEnabled(true);
					chooseSolrHomeButton.setEnabled(true);
					chooseGateHomeButton.setEnabled(true);
				}
			}
		};		
		
		gateSolrRadio.addActionListener(radioActionListener);
		osHomeRadio.addActionListener(radioActionListener);
		
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
                
				if(osHomeRadio.isSelected()) {
	                System.setProperty("opensextant.home", osHomeTextField.getText());
					String osHome = osHomeTextField.getText();
			        Config.GATE_HOME = getGateHome(osHome);
					Config.SOLR_HOME = getSolrHome(osHome);

					ConfigHelper.getInstance().setOsHome(osHome);
					ConfigHelper.getInstance().setGateHome(Config.GATE_HOME);
					ConfigHelper.getInstance().setSolrHome(Config.SOLR_HOME);
				} else {
			        Config.GATE_HOME = gateHomeTextField.getText();
			        Config.SOLR_HOME = solrHomeTextField.getText();
					ConfigHelper.getInstance().setGateHome(Config.GATE_HOME);
					ConfigHelper.getInstance().setSolrHome(Config.SOLR_HOME);
				}

				if (rememberCheckbox.isSelected()) {
					ConfigHelper.getInstance().saveSettings();
				}
				SelectOSHomeFrameImpl.this.dispose();
				Initialize.init();
				Main.openMainWindow();
			}
		});
		
	}

	private void showErrorDialog(final SelectOSHomeFrameImpl parent) {
		JOptionPane.showMessageDialog(parent, "Cannot proceed without a valid Open Sextant Home directory.  Exiting.", "Exiting",
				JOptionPane.ERROR_MESSAGE);
		parent.dispose();
		System.exit(0);
	}

}
