package org.mitre.opensextant.desktop.ui;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.mitre.opensextant.desktop.ui.forms.AboutFrame;
import org.mitre.opensextant.desktop.ui.forms.ConfigFrame;
import org.mitre.opensextant.desktop.ui.forms.ConfigFrameImpl;
import org.mitre.opensextant.desktop.ui.forms.LogFrame;
import org.mitre.opensextant.desktop.ui.forms.OpenSextantMainFrame;
import org.mitre.opensextant.desktop.ui.forms.TextEntryFrame;
import org.mitre.opensextant.desktop.ui.handlers.FileDropTransferHandler;
import org.mitre.opensextant.desktop.ui.handlers.HelpKeyListener;
import org.mitre.opensextant.desktop.ui.helpers.ApiHelper;
import org.mitre.opensextant.desktop.ui.helpers.ConfigHelper;
import org.mitre.opensextant.desktop.ui.helpers.MainFrameTableHelper;
import org.mitre.opensextant.desktop.ui.table.OSTreeTable;
import org.mitre.opensextant.desktop.util.TikaMimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class OpenSextantMainFrameImpl extends OpenSextantMainFrame{


    private static Logger log = LoggerFactory.getLogger(OpenSextantMainFrameImpl.class);
    private MainFrameTableHelper tableHelper;
    private ApiHelper apiHelper;
    private OSTreeTable table;


	public enum ButtonType {
		CANCEL, DELETE, FILTER, RERUN
	};

	public enum IconType {
		BOLD, NORMAL, TRASH, CANCEL
	};

	public OpenSextantMainFrameImpl() {
		super();
		tableHelper = new MainFrameTableHelper(this);
		apiHelper = new ApiHelper(this);
		initialize(this);

		java.net.URL imgURL = OpenSextantMainFrameImpl.class.getResource("/org/mitre/opensextant/desktop/icons/logo.png");
		if (imgURL != null) {
			this.setIconImage(new ImageIcon(imgURL, "Icon").getImage());
		}

		HelpKeyListener helpListen = new HelpKeyListener();
		
		
		this.treePanel.setTransferHandler(new FileDropTransferHandler(apiHelper));
		
		table = new OSTreeTable(this);
                this.tableScrollPane.setViewportView(table.create());
	
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
                ConfigHelper.getInstance().loadRows(apiHelper, getTableHelper());
                
        this.addWindowListener(new WindowListener() {
			@Override public void windowOpened(WindowEvent e) {}
			@Override public void windowIconified(WindowEvent e) {}
			@Override public void windowDeiconified(WindowEvent e) {}
			@Override public void windowDeactivated(WindowEvent e) {}
			@Override public void windowClosing(WindowEvent e) {}
			@Override public void windowClosed(WindowEvent e) {
				FileUtils.deleteQuietly(new File(ConfigHelper.getInstance().getOSTmpRoot()));
			}
			@Override public void windowActivated(WindowEvent e) {}
		});
	}

	public OSTreeTable getTable() {
		return table;
	}
	
	private void initialize(final OpenSextantMainFrameImpl parent) {
		
        configMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configMenuItemActionPerformed(evt);
            }
        });

        this.textButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	parent.textMenuItemActionPerformed(evt);
            }
        });
        
        this.configButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configMenuItemActionPerformed(evt);
            }
        });
        
        this.aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        
        this.addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	parent.addFileMenuItemActionPerformed(evt);
            }
        });
        
        this.addFileMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	parent.addFileMenuItemActionPerformed(evt);
            }
        });

        this.exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	parent.exitMenuItemActionPerformed(evt);
            }
        });

        
        this.textMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	parent.textMenuItemActionPerformed(evt);
            }
        });

        this.helpMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	parent.helpMenuItemActionPerformed(evt);
            }
        });
        
        this.logButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	logMenuItemActionPerformed(evt);
            }
        });
        
        this.logMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	logMenuItemActionPerformed(evt);
            }
        });
		
	}

	private void configMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
		JFrame.setDefaultLookAndFeelDecorated(true);
		JFrame frame = new ConfigFrameImpl();
		frame.pack();
		frame.setVisible(true);
	}

	private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
                JFrame about = new AboutFrame();
                about.setLocationByPlatform(true);
                about.setVisible(true);
        }

        private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
            if(this.isDisplayable()) this.dispose();
            System.exit(0); // For good measure :P
        }           
        
	private void addFileMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = TikaMimeTypes.makeFileBrowser();
		chooser.setFileFilter(filter);
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setMultiSelectionEnabled(true);

		try {
			File f = new File(ConfigHelper.getInstance().getInLocation());
			chooser.setCurrentDirectory(f);
		} catch (Exception e) {
		}
		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File[] files = chooser.getSelectedFiles();
			for (File file : files) {
				apiHelper.processFile(file.getAbsolutePath());
			}
			if (files.length > 0) {
				ConfigHelper.getInstance().setInLocation(files[0].getParentFile().getAbsolutePath());
			}
			ConfigHelper.getInstance().saveSettings();
		}
	}
	


	private void textMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
		JFrame.setDefaultLookAndFeelDecorated(true);
		JFrame frame = new TextEntryFrame(apiHelper);
		frame.setVisible(true);
	}

	private void helpMenuItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton1ActionPerformed
		String path = System.getProperty("user.dir") + HelpKeyListener.HELP_FILE;

		try {
			Desktop.getDesktop().open(new File(path));
		} catch (IOException ex) {
			log.error(ex.getMessage());
		}
	}
        
        private void logMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
            JFrame.setDefaultLookAndFeelDecorated(true);
            JFrame frame = new LogFrame();
            frame.setVisible(true);
        }
	
	public MainFrameTableHelper getTableHelper() {
		return tableHelper;
	}
	public ApiHelper getApiHelper() {
		return apiHelper;
	}

	private static URL getIconUrl(String path) {
		return OpenSextantMainFrameImpl.class.getResource("/org/mitre/opensextant/desktop/icons/" + path + ".png");
	}

	public static ImageIcon getIcon(IconType type) {
		return new ImageIcon(getIconUrl(type.toString().toLowerCase()));
	}
	private static Map<String, Icon> extIcons = new HashMap<String, Icon>();

	public static Icon getIconForExtension(File file) {
		Icon icon = extIcons.get(FilenameUtils.getExtension(file.getAbsolutePath()));
		if (icon == null) {
			icon = (new JFileChooser()).getIcon(file);
			extIcons.put(FilenameUtils.getExtension(file.getAbsolutePath()), icon);
		}
		return icon;
	}


}
