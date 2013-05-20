package org.mitre.opensextant.desktop.ui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.mitre.opensextant.desktop.Main;
import org.mitre.opensextant.desktop.ui.forms.ConfigFrame;
import org.mitre.opensextant.desktop.ui.forms.SelectOSHomeFrame;

@SuppressWarnings("serial")
public class SelectOSHomeFrameImpl extends SelectOSHomeFrame {

	public SelectOSHomeFrameImpl() {
		super();
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		setVisible(true);
		
		initialize(this);
		
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
		
		chooseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
	          final JFileChooser chooser = new JFileChooser();
	          chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	          int returnVal = chooser.showOpenDialog(parent);
	          
	          if (returnVal == JFileChooser.APPROVE_OPTION) {
		          directoryTextField.setText(chooser.getSelectedFile().getAbsolutePath());
	          }

			}
		});
		
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Properties props = System.getProperties();
                props.setProperty("opensextant.home", directoryTextField.getText());
				if (rememberCheckbox.isSelected()) {
					ConfigFrame.setOsHome(directoryTextField.getText());
					ConfigFrame.saveSettings();
				}
				SelectOSHomeFrameImpl.this.dispose();
				Main.openMainWindow();
			}
		});
		
	}
	
	private void showErrorDialog(final SelectOSHomeFrameImpl parent) {
		JOptionPane.showMessageDialog(parent, "Cannot proceed without a valid Open Sextant Home directory.  Exiting.", "Exiting", JOptionPane.ERROR_MESSAGE);
		parent.dispose();
		System.exit(0);
	}
	
	
}
