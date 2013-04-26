/** 
 Copyright 2009-2013 The MITRE Corporation.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.


 * **************************************************************************
 *                          NOTICE
 * This software was produced for the U. S. Government under Contract No.
 * W15P7T-12-C-F600, and is subject to the Rights in Noncommercial Computer
 * Software and Noncommercial Computer Software Documentation Clause
 * 252.227-7014 (JUN 1995)
 *
 * (c) 2012 The MITRE Corporation. All Rights Reserved.
 * **************************************************************************
**/
package org.mitre.opensextant.apps;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import javax.swing.BorderFactory;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 * Provides a GUI interface for the OpenSextantRunner class.
 * 
 * @author Rich Markeloff, MITRE Corp. 
 * Initial version created on Apr 23, 2012
 */
public class OpenSextantGUI {

    private JFrame frame;
    private File gateHome = null;
    private JButton gateHomeButt;
    private File gappFile;
    private File inputFile;
    private File outputFile;
    private String tempDirPath = "./temp";
    private String outputFormat;
    private OpenSextantRunner runner = null;

    /**
     * Instantiates this class and creates the GUI. Does not require any parameters.
     */
    public static void main(String[] args) {
        new OpenSextantGUI().buildGUI();
    }

    /**
     * Creates the GUI for running OpenSextantRunner.
     * 
     */
    public void buildGUI() {
        this.frame = new JFrame("OpenSextant");
        JPanel panel = new JPanel();

        // Select the GATE home directory
        final JFileChooser gateHomeChooser = new JFileChooser(new File(System.getProperty("user.dir")));
        gateHomeChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        this.gateHomeButt = new JButton("Set GATE Home");
        this.gateHomeButt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int retVal = gateHomeChooser.showOpenDialog(frame);
                if (retVal == JFileChooser.APPROVE_OPTION) {
                    File selectedDir = gateHomeChooser.getSelectedFile();
                    gateHome = selectedDir;
                    JOptionPane.showMessageDialog(frame, "GATE Home Directory: " + gateHome.toString()
                            + "\nTo change GATE home, you must restart the application",
                            "GATE Home Directory", JOptionPane.INFORMATION_MESSAGE);

                    // Disable the button. The user needs to quit and restart the
                    // application to change the GATE home directory.
                    gateHomeButt.setEnabled(false);

                }
            }
        });
        panel.add(this.gateHomeButt);

        // Select the GAPP file
        final JFileChooser gappChooser = new JFileChooser(new File(System.getProperty("user.dir")));
        gappChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        JButton gappFileButt = new JButton("Select GAPP File");
        gappFileButt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int retVal = gappChooser.showOpenDialog(frame);
                if (retVal == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = gappChooser.getSelectedFile();
                    gappFile = selectedFile;
                    JOptionPane.showMessageDialog(frame, "GATE Application File: " + gappFile.toString(),
                            "GAPP File", JOptionPane.INFORMATION_MESSAGE);

                }
            }
        });
        panel.add(gappFileButt);

        // Select the input file or directory
        final JFileChooser inputChooser = new JFileChooser();
        inputChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        JButton inFileButt = new JButton("Select Input");
        inFileButt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int retVal = inputChooser.showOpenDialog(frame);
                if (retVal == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = inputChooser.getSelectedFile();
                    inputFile = selectedFile;
                    String inputType = "File";
                    if (inputFile.isDirectory()) {
                        inputType = "Directory";
                    }
                    JOptionPane.showMessageDialog(frame, "Input " + inputType + ": " + inputFile.toString(),
                            "Input " + inputType, JOptionPane.INFORMATION_MESSAGE);

                }
            }
        });
        panel.add(inFileButt);

        // Select the output format
        ButtonGroup outputTypeButtGroup = new ButtonGroup();
        JPanel outputTypeRadioPanel = new JPanel(new GridLayout(1, 0));
        outputTypeRadioPanel.setBorder(BorderFactory.createTitledBorder("Output Formats"));
        ActionListener outputFormatListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                outputFormat = e.getActionCommand();
            }
        };

        boolean first = true;
        for (String f : OpenSextantRunner.OUTPUT_FORMATS) {
            JRadioButton rb = new JRadioButton(f);
            if (first) {
                rb.setSelected(true);
                outputFormat = f;
                first = false;
            }
            rb.addActionListener(outputFormatListener);
            outputTypeButtGroup.add(rb);
            outputTypeRadioPanel.add(rb);
        }
        panel.add(outputTypeRadioPanel);

        // Set the output file
        final JFileChooser outputChooser = new JFileChooser();
        JButton outFileButt = new JButton("Set Output File");
        outFileButt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int retVal = outputChooser.showSaveDialog(frame);
                if (retVal == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = outputChooser.getSelectedFile();
                    outputFile = selectedFile;
                    JOptionPane.showMessageDialog(frame, "Output File: " + outputFile.toString(),
                            "Output File", JOptionPane.INFORMATION_MESSAGE);

                }
            }
        });
        panel.add(outFileButt);

        // Select the temp directory
        final JFileChooser tempChooser = new JFileChooser();
        tempChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        JButton tempButt = new JButton("Set Temp Directory");
        tempButt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int retVal = tempChooser.showSaveDialog(frame);
                if (retVal == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = tempChooser.getSelectedFile();
                    tempDirPath = selectedFile.getPath();
                    JOptionPane.showMessageDialog(frame, "Temporary Storage Directory: " + tempDirPath,
                            "Temp Directory", JOptionPane.INFORMATION_MESSAGE);

                }
            }
        });
        panel.add(tempButt);

        // Select the description type
        ButtonGroup descTypeButtGroup = new ButtonGroup();
        JPanel descTypeRadioPanel = new JPanel(new GridLayout(1, 0));
        descTypeRadioPanel.setBorder(BorderFactory.createTitledBorder("Description Field Source"));
        ActionListener descTypeListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // NO-OP.  @deprecated.
                String descType = e.getActionCommand();
            }
        };

        /*
         JRadioButton lastButton = null;
         String lastType = null;
         for (String t: OpenSextantRunner.DESCRIPTION_TYPES) {
         JRadioButton rb = new JRadioButton(t);
         rb.addActionListener(descTypeListener);
         descTypeButtGroup.add(rb);
         descTypeRadioPanel.add(rb);
         lastButton = rb;
         lastType = t;
         }
         lastButton.setSelected(true);
         this.descType = lastType;
         panel.add(descTypeRadioPanel);
         */

        JButton runButton = new JButton("Run OpenSextant");
        runButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    runOpenSextant();
                } catch (Exception e) {
                    final Writer result = new StringWriter();
                    final PrintWriter printWriter = new PrintWriter(result);
                    e.printStackTrace(printWriter);
                    String message = result.toString();
                    JOptionPane.showMessageDialog(frame, message,
                            "Exception Thrown", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        panel.add(runButton);

        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 200);
        frame.setVisible(true);
    }

    // Instantiate and run an OpenSextantRunner object. Set the GATE Home directory the
    // first time this is invoked.
    private void runOpenSextant() throws Exception {

        // Instantiate an OpenSextantRunner
        if (this.runner == null) {
            this.runner = new OpenSextantRunner();
            this.runner.gappFile = this.gappFile.getPath();
            this.runner.initialize();
        }
        if (parametersAreValid()) {
            runner.runOpenSextant(this.inputFile.getPath(), this.outputFormat,
                    this.outputFile.getPath(), this.tempDirPath);
            JOptionPane.showMessageDialog(this.frame, "Document processing completed",
                    "OpenSextant Status", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Checks to make sure that the user's selections are valid. Returns true if everything is OK,
    // false otherwise.
    private boolean parametersAreValid() {
        boolean ok = true;
        if (this.gappFile == null) {
            JOptionPane.showMessageDialog(this.frame, "No GAPP file selected",
                    "Invalid Parameter", JOptionPane.WARNING_MESSAGE);
            ok = false;
            return ok;
        }
        if (this.inputFile == null) {
            JOptionPane.showMessageDialog(this.frame, "No input file selected",
                    "Invalid Parameter", JOptionPane.WARNING_MESSAGE);
            ok = false;
            return ok;
        }

        if (runner.validateParameters(gappFile.getPath(), inputFile.getPath(),
                outputFormat, outputFile.getPath(), tempDirPath)) {

            JOptionPane.showMessageDialog(this.frame, runner.getMessages(),
                    "Invalid Parameter", JOptionPane.WARNING_MESSAGE);
            ok = false;
        }

        return ok;
    }
}
