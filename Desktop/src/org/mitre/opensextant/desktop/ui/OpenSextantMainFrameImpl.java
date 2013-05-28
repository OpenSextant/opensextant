package org.mitre.opensextant.desktop.ui;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
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

import org.apache.commons.collections.map.HashedMap;
import org.mitre.opensextant.desktop.ui.forms.ConfigFrame;
import org.mitre.opensextant.desktop.ui.forms.ConfigFrameImpl;
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
		
		// Listeners for everywhere
		// TODO: Is there really no better way to do this?
		// TODO: Probably key mnemonics after I add a button
		this.addKeyListener(helpListen);
		addButton.addKeyListener(helpListen);
		addTextButton.addKeyListener(helpListen);
//		this.cancelButton.addKeyListener(helpListen);
		this.configButton.addKeyListener(helpListen);
//		this.deleteButton.addKeyListener(helpListen);
//		this.filterButton.addKeyListener(helpListen);
//		this.rerunButton.addKeyListener(helpListen);
//		this.allCheck.addKeyListener(helpListen);
//		this.sortCombo.addKeyListener(helpListen);

		
		mainPanel.setTransferHandler(new FileDropTransferHandler(apiHelper));
		
		table = new OSTreeTable();
		JScrollPane scrollPane = new JScrollPane(table.create());
		treePanel.add(scrollPane);

		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
                ConfigHelper.getInstance().loadRows(apiHelper, getTableHelper());
	}

	public OSTreeTable getTable() {
		return table;
	}
	
	private void initialize(final OpenSextantMainFrameImpl parent) {
		
//        allCheck.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                allCheckActionPerformed(evt);
//            }
//        });
//
        configButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configButtonActionPerformed(evt);
            }
        });
//
//        rerunButton.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                parent.rerunButtonActionPerformed(evt);
//            }
//        });
//
//        filterButton.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//            	parent.filterButtonActionPerformed(evt);
//            }
//        });
//
//        cancelButton.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//            	parent.cancelButtonActionPerformed(evt);
//            }
//        });

//        deleteButton.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//            	parent.deleteButtonActionPerformed(evt);
//            }
//        });

//        viewButton.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//            	parent.viewButtonActionPerformed(evt);
//            }
//        });

        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	parent.addButtonActionPerformed(evt);
            }
        });

        addTextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	parent.addTextButtonActionPerformed(evt);
            }
        });

        helpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	parent.helpButtonActionPerformed(evt);
            }
        });
		
	}
	
//	private void allCheckActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_allCheckActionPerformed
//		boolean value = ((JCheckBox) evt.getSource()).isSelected();
//		tableHelper.checkAll(value);
//	}
//
//	private void rerunButtonActionPerformed(java.awt.event.ActionEvent evt) {
//		tableHelper.runTopLevelButtons(ButtonType.RERUN);
//	}
//
//	private void filterButtonActionPerformed(java.awt.event.ActionEvent evt) {
//		tableHelper.runTopLevelButtons(ButtonType.FILTER);
//	}
//
//	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
//		tableHelper.runTopLevelButtons(ButtonType.CANCEL);
//	}
//
//	private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {
//		tableHelper.runTopLevelButtons(ButtonType.DELETE);
//		tableHelper.updateActionVisibility();
//	}

	private void configButtonActionPerformed(java.awt.event.ActionEvent evt) {
		JFrame.setDefaultLookAndFeelDecorated(true);
		JFrame frame = new ConfigFrameImpl();
		frame.pack();
		frame.setVisible(true);
	}

	private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {
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
	


	private void addTextButtonActionPerformed(java.awt.event.ActionEvent evt) {
		JFrame.setDefaultLookAndFeelDecorated(true);
		JFrame frame = new TextEntryFrame(apiHelper);
		frame.setVisible(true);
	}

	private void helpButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton1ActionPerformed
		String path = System.getProperty("user.dir") + HelpKeyListener.HELP_FILE;

		try {
			Desktop.getDesktop().open(new File(path));
		} catch (IOException ex) {
			log.error(ex.getMessage());
		}
	}
	
//	private void viewButtonActionPerformed(java.awt.event.ActionEvent evt) {
//		tableHelper.viewResult();
//	}
	
	public javax.swing.JButton getAddTextButton() {
		return addTextButton;
	}
//	public javax.swing.JButton getCancelButton() {
//		return cancelButton;
//	}
	public javax.swing.JButton getConfigButton() {
		return configButton;
	}
//	public javax.swing.JButton getDeleteButton() {
//		return deleteButton;
//	}
//	public javax.swing.JButton getFilterButton() {
//		return filterButton;
//	}
	public javax.swing.JButton getHelpButton() {
		return helpButton;
	}
//	public javax.swing.JButton getRerunButton() {
//		return rerunButton;
//	}
//	public javax.swing.JButton getViewButton() {
//		return viewButton;
//	}
//	public javax.swing.JLabel getProgressLabel() {
//		return progressLabel;
//	}
//	public javax.swing.JPanel getTablePanel() {
//		return tablePanel;
//	}
//	public javax.swing.JScrollPane getTableScrollPane() {
//		return tableScrollPane;
//	}

	

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
	private static Map<String, ImageIcon> extIcons = new HashMap<String, ImageIcon>();

	public static ImageIcon getIconForExtension(String extension) {
		ImageIcon icon = extIcons.get(extension);
		if (icon == null) {
			URL iconUrl = getIconUrl("file_ext/"+extension);
			if (iconUrl == null) iconUrl = getIconUrl("file_ext/_blank");
			icon = new ImageIcon(iconUrl);
			Image img = icon.getImage();
 			BufferedImage bi = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
			Graphics g = bi.createGraphics();
			g.drawImage(img, 0, 0, 20, 20, null);
			ImageIcon newIcon = new ImageIcon(bi);
			extIcons.put(extension, newIcon);
		}
		return icon;
	}


}
