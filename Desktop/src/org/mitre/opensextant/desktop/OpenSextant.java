/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mitre.opensextant.desktop;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.mitre.opensextant.desktop.handlers.FileDropTransferHandler;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author GBLACK
 */
public class OpenSextant extends javax.swing.JFrame {
    private static final String HELP_FILE = "/help-pages/main.htm";
    // How many entries have been entered into the table
    // used as a unique id for entries
    private static int tableCount = 0;  
    
    private static OpenSextant instance = null;
    private enum ButtonType { CANCEL, DELETE, FILTER, RERUN };
    private enum IconType { BOLD, NORMAL, TRASH, CANCEL };
    private static Logger log = LoggerFactory.getLogger(OpenSextant.class);
    
    private KeyListener helpListen = new KeyListener() {
        @Override
        public void keyPressed(KeyEvent e) { 
           if(e.getKeyCode() == KeyEvent.VK_F1) {
             String path = System.getProperty("user.dir") + HELP_FILE;

             try { Desktop.getDesktop().open(new File(path)); }
             catch (IOException ex) { log.error(ex.getMessage()); }
             
           }
        }

        @Override
        public void keyTyped(KeyEvent e) {}

        @Override
        public void keyReleased(KeyEvent e) {}
      };
    
    /**
     * Creates new form OpenSextantGUIFrame
     */
    public OpenSextant() {
      Config.loadConfig();
      log.info("loaded config");
      
      Properties props = System.getProperties();
      
      String osHome = props.getProperty("opensextant.home");
      if(osHome == null) {
    	  
    	  List<String> osHomes = new ArrayList<String>() {{
    		  add(ApiHelper.BASE_PATH + "opensextant");
    		  add((new File("")).getAbsolutePath()+File.separator+"opensextant");
    		  add((new File("")).getAbsolutePath()+File.separator+"dist"+File.separator+"opensextant");
    	  }};
    	  
    	  for (String potentialHome : osHomes) {
        	  if ((new File(potentialHome)).exists()) {
        		  props.setProperty("opensextant.home", potentialHome); 
        		  osHome = potentialHome;
        		  log.info("Open sextant home set to: " + osHome);
        		  break;
        	  }
    	  }
    	  
      }

      if(osHome == null || !(new File(osHome)).exists()) { 
          final JFileChooser chooser = new JFileChooser();
          chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
          int returnVal = chooser.showOpenDialog(this);
          
          if (returnVal == JFileChooser.APPROVE_OPTION) {
              props.setProperty("opensextant.home", chooser.getSelectedFile().getAbsolutePath());
          }
          
      }

      Initialize.init();
      
      initComponents();

      java.net.URL imgURL = OpenSextant.class.getResource("/org/mitre/opensextant/desktop/icons/logo.png");
      if (imgURL != null) {
        this.setIconImage(new ImageIcon(imgURL,"Icon").getImage());
      } 
      // Custom table layout code
      tableLayout = new javax.swing.GroupLayout(tablePanel);
      tablePanel.setLayout(tableLayout);
      horizontalGroup = tableLayout.createParallelGroup(
        javax.swing.GroupLayout.Alignment.LEADING);
      tableLayout.setHorizontalGroup(horizontalGroup);
  
      verticalGroup = tableLayout.createSequentialGroup();
      tableLayout.setVerticalGroup(
        tableLayout.createParallelGroup(
          javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(verticalGroup));
      tableScrollPane.setViewportView(tablePanel);

      setVisibleActions(false);
      instance = this;
      
      // Listeners for everywhere
      // TODO: Is there really no better way to do this?
      // TODO: Probably key mnemonics after I add a button
      this.addKeyListener(helpListen);
      addButton.addKeyListener(helpListen);
      addTextButton.addKeyListener(helpListen);   
      this.cancelButton.addKeyListener(helpListen);
      this.configButton.addKeyListener(helpListen);
      this.deleteButton.addKeyListener(helpListen);
      this.filterButton.addKeyListener(helpListen);
      this.rerunButton.addKeyListener(helpListen);
      this.allCheck.addKeyListener(helpListen);
      this.sortCombo.addKeyListener(helpListen);
      
      this.mainPanel.setTransferHandler(new FileDropTransferHandler());
    }

    
    public static void updateProgress(int guiEntry, String status, int num){
     
       if( num >= 100) {
         JLabel stopLabel = instance.tableStopLabel.get(guiEntry);
         stopLabel.setToolTipText("Delete job from list");
         updateIcon(stopLabel, IconType.TRASH);
        }
       
        // Swing ridiculously makes it incredibly difficult to change an 
        //  individual progress bar's color.  May revisit this.
        /*UIDefaults overrides = UIManager.getDefaults();
        overrides.put("nimbusOrange", (Color.red));
           
        instance.tableProgress.get(guiEntry).putClientProperty("Nimbus.Overrides", overrides);
        instance.tableProgress.get(guiEntry).putClientProperty("Nimbus.Overrides.InheritDefaults", false);
         */
       
        if(instance.tableProgressLabel.get(guiEntry)
                   .getText().contains("Cancelled")) return;
       
        instance.tableProgress.get(guiEntry).setValue(num);
        instance.tableProgressLabel.get(guiEntry).setText(status);
       
    }
    
    private void hideRow(int rowNum) {
        tableCheck.get(rowNum).setVisible(false);
        tableLabel.get(rowNum).setVisible(false);
        tableRunLabel.get(rowNum).setVisible(false);
        tableProgressLabel.get(rowNum).setVisible(false);
        tableProgress.get(rowNum).setVisible(false);
        tableStopLabel.get(rowNum).setVisible(false);
        tableTermLabel.get(rowNum).setVisible(false);
        tableSep.get(rowNum).setVisible(false);
    }
    
    private void cancelRow(int rowNum) {
      // TODO: Actually stop job
      updateProgress(rowNum, "Cancelled", 100);
    }
    
    private void rerunRow(int rowNum) {
       ApiHelper.processFile( inputFiles.get(rowNum) );
    }
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        configButton = new javax.swing.JButton();
        sortCombo = new javax.swing.JComboBox();
        topSep = new javax.swing.JSeparator();
        allCheck = new javax.swing.JCheckBox();
        titleLabel = new javax.swing.JLabel();
        progressLabel = new javax.swing.JLabel();
        lastRunLabel = new javax.swing.JLabel();
        tableTopSep = new javax.swing.JSeparator();
        tableScrollPane = new javax.swing.JScrollPane();
        tablePanel = new javax.swing.JPanel();
        rerunButton = new javax.swing.JButton();
        filterButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        viewButton = new javax.swing.JButton();
        addButton = new javax.swing.JButton();
        addTextButton = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("OpenSextant");

        configButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mitre/opensextant/desktop/icons/cog.png"))); // NOI18N
        configButton.setToolTipText("Configuration");
        configButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configButtonActionPerformed(evt);
            }
        });

        sortCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Sort" }));
        sortCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sortComboActionPerformed(evt);
            }
        });

        allCheck.setToolTipText("");
        allCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allCheckActionPerformed(evt);
            }
        });

        titleLabel.setText("TITLE");

        progressLabel.setText("PROGRESS");

        lastRunLabel.setText("LAST RUN");

        tableScrollPane.setBorder(null);

        tablePanel.setPreferredSize(new java.awt.Dimension(500, 100));

        javax.swing.GroupLayout tablePanelLayout = new javax.swing.GroupLayout(tablePanel);
        tablePanel.setLayout(tablePanelLayout);
        tablePanelLayout.setHorizontalGroup(
            tablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 903, Short.MAX_VALUE)
        );
        tablePanelLayout.setVerticalGroup(
            tablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 349, Short.MAX_VALUE)
        );

        tableScrollPane.setViewportView(tablePanel);

        rerunButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mitre/opensextant/desktop/icons/rerun.png"))); // NOI18N
        rerunButton.setToolTipText("Re-run job");
        rerunButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rerunButtonActionPerformed(evt);
            }
        });

        filterButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mitre/opensextant/desktop/icons/filter.png"))); // NOI18N
        filterButton.setToolTipText("Filter job");
        filterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterButtonActionPerformed(evt);
            }
        });

        cancelButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mitre/opensextant/desktop/icons/lineCircle.png"))); // NOI18N
        cancelButton.setToolTipText("Duplicate job");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        deleteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mitre/opensextant/desktop/icons/trash.png"))); // NOI18N
        deleteButton.setToolTipText("Delete job");
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        viewButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mitre/opensextant/desktop/icons/eye.png"))); // NOI18N
        viewButton.setToolTipText("View results");
        viewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewButtonActionPerformed(evt);
            }
        });

        addButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mitre/opensextant/desktop/icons/plusCircle.png"))); // NOI18N
        addButton.setToolTipText("Process files/folders");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        addTextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mitre/opensextant/desktop/icons/lines.png"))); // NOI18N
        addTextButton.setToolTipText("Process text entry");
        addTextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addTextButtonActionPerformed(evt);
            }
        });

        jButton1.setFont(new java.awt.Font("Tahoma", 0, 5)); // NOI18N
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mitre/opensextant/desktop/icons/questionSmall.png"))); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(topSep, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(tableScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 903, Short.MAX_VALUE)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(allCheck)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(titleLabel)
                        .addGap(126, 126, 126)
                        .addComponent(progressLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lastRunLabel))
                    .addComponent(tableTopSep)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(addButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(addTextButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(rerunButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(filterButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(deleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(viewButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(sortCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(configButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(rerunButton, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(filterButton, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(cancelButton, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(deleteButton, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(viewButton, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(addButton, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(addTextButton, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(mainPanelLayout.createSequentialGroup()
                            .addGap(11, 11, 11)
                            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(configButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(sortCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(topSep, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(allCheck, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(progressLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lastRunLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(titleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addComponent(tableTopSep, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void configButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configButtonActionPerformed
       JFrame.setDefaultLookAndFeelDecorated(true);
       JFrame frame = new Config();
       frame.pack();
       frame.setVisible(true);
    }//GEN-LAST:event_configButtonActionPerformed

    public static int addRow( String status, String loc
                            , String name, String type, String input) {
        String symbol = "file.png";
        // Set up metadata
        instance.outputLocs.add(loc);
        instance.timestamps.add(System.currentTimeMillis());
        instance.inputFiles.add(input);
        
        // Test what icon to use
        File f = new File(input);
        if(f != null && f.isDirectory()) symbol = "folder.png";
        
        return instance.addRowHelper(status, loc, name,type, symbol);
    }
    
    public static void viewFileFromRow(Object caller, ArrayList list){
      for(int i = 0; i < list.size(); i ++) {
        if(list.get(i) == caller){
          File file = new File(instance.outputLocs.get(i));
          try { Desktop.getDesktop().open(file); }
          catch (IOException ex) { log.error(ex.getMessage());
          }
        }
      }
    }

    public static void toggleCheck(Object caller, ArrayList list){
      for(int i = 0; i < list.size(); i ++) {
        if(list.get(i) == caller){
          JCheckBox jc = instance.tableCheck.get(i);
          jc.setSelected(!jc.isSelected());
        }
      }
    }
    
    public static void hoverRow(boolean inRow, Object caller, ArrayList list) {
      for(int i = 0; i < list.size(); i ++) {
        if(list.get(i) == caller){
          int fontWeight = inRow ? Font.BOLD : Font.PLAIN;
          JLabel label = instance.tableProgressLabel.get(i);
          Font newFont = new Font( label.getFont().getName()
                                 , fontWeight,label.getFont().getSize()
                                 );  
          label.setFont(newFont);
          instance.tableLabel.get(i).setFont(newFont);
          instance.tableRunLabel.get(i).setFont(newFont);
          instance.tableTermLabel.get(i).setFont(newFont);
        }
      }
    }
    
    private void cancelOrDelete(JLabel caller){
      for(int i = 0; i < tableStopLabel.size(); i ++) {
        JLabel jl = tableStopLabel.get(i);
        if(jl == caller){
          String tooltip = jl.getToolTipText();
          if(tooltip != null) {
              boolean isDelete = tooltip.startsWith("Delete");
              String dialogMsg = "Stop running job?";
              String dialogAction = "stopping";
              if(isDelete) { 
                  dialogMsg = "Delete job from list?";
                  dialogAction = "deleting";
              }
              
              Object[] options = {"Yes", "No"};
              int n = JOptionPane.showOptionDialog
                        ( this
                        , dialogMsg
                        , "Confirm " + dialogAction + " job"
                        , JOptionPane.YES_NO_OPTION
                        , JOptionPane.QUESTION_MESSAGE
                        , null
                        , options
                        , options[1]
                        ); 
              if(n == 0) {
                if(tooltip.startsWith("Delete")) hideRow(i);
                else cancelRow(i);
              }
          }
           
        }
      }
      updateActionVisibility();

    }
    
    private static void updateIcon(JLabel l, IconType t) {
      // TODO: Kludge, non-null font means bold icon
      boolean isBold = (l.getFont() == null);
      // TODO: Kludge, blue == trash, green == cancel
      //  why Swing doesn't allow metadata Objects for components is beyond me
      //  that is probably the fix for this kludge (Extend JLabel with 2 bools)
      boolean isTrash = (l.getForeground() == Color.blue);
      String sym = "xCircle";
      String bold = "";
      String icon = "";
      
      
      switch(t) {
          case BOLD: isBold = true; break;
          case NORMAL: isBold = false; break;
          case TRASH: isTrash = true; break;
          case CANCEL: isTrash = false; break;
      }
      
      if(isTrash) { 
          sym = "trash";
          l.setForeground(Color.blue);
      } else {
          l.setForeground(Color.green);
      }
      if(isBold) {
          bold = "Bold";
          l.setFont(instance.progressLabel.getFont());
      } else { 
          l.setFont(null);
      }
      
      l.setIcon(new javax.swing.ImageIcon(instance.getClass()
              .getResource( "/org/mitre/opensextant/desktop/icons/" 
                          + sym + bold + ".png"))); 
    }
    
    private int addRowHelper( String status, String loc
                            , String name, String type, String symbol) {
      // Temporary widgets to work with
      JCheckBox tmpCheck = new javax.swing.JCheckBox();
      JLabel tmpLabel = new JLabel();
      JLabel tmpProgressLabel = new JLabel();
      JLabel tmpRunLabel = new JLabel();
      JProgressBar tmpProgress = new JProgressBar();
      JSeparator tmpSep = new JSeparator();
      JLabel tmpStopLabel = new JLabel();
      JLabel tmpTermLabel = new JLabel();
      
      // Add widgets to the appropriate lists
      tableCheck.add(tmpCheck);
      tableLabel.add(tmpLabel);
      tableProgressLabel.add(tmpProgressLabel);
      tableRunLabel.add(tmpRunLabel);
      tableProgress.add(tmpProgress);
      tableSep.add(tmpSep);
      tableStopLabel.add(tmpStopLabel);
      tableTermLabel.add(tmpTermLabel);
      
      // Set up specifics for this row
      tmpStopLabel.setFont(null); // TODO: Kludge to get rid of
      tmpStopLabel.setForeground(Color.green); // TODO: Kludge to get rid of
      tmpLabel.setIcon(new javax.swing.ImageIcon(getClass()
              .getResource("/org/mitre/opensextant/desktop/icons/" + symbol))); 
      tmpLabel.setText(name);
      tmpLabel.addMouseListener(new OSMouseAdapter(tableLabel));
      tmpProgressLabel.addMouseListener(
        new OSMouseAdapter(tableProgressLabel));
      tmpRunLabel.addMouseListener(new OSMouseAdapter(tableRunLabel));
      tmpTermLabel.addMouseListener(new OSMouseAdapter(tableTermLabel));
      tmpStopLabel.addMouseListener(new MouseAdapter(){
        @Override
        public void mouseClicked(MouseEvent e){
         cancelOrDelete((JLabel)e.getSource());               
        }
        
        @Override
        public void mouseEntered(MouseEvent e) {
          updateIcon((JLabel)e.getSource(), IconType.BOLD);
        }
        
        @Override
        public void mouseExited(MouseEvent e)
        {
          updateIcon((JLabel)e.getSource(), IconType.NORMAL);
        }
        
      });
      tmpProgress.addMouseListener(new OSMouseAdapter(tableProgress));
      tmpLabel.setToolTipText("Double-click to view output");
      tmpProgressLabel.setToolTipText("Ouput Location: " + loc);
      tmpStopLabel.setToolTipText("Click to stop running job");
      tmpProgressLabel.setText(status);
      tmpStopLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource(
              "/org/mitre/opensextant/desktop/icons/xCircle.png"))); 

     
      
      // Action listener for the checkbox
      tmpCheck.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
           checkActionPerformed(evt);
         }
       });
      
      // Format and add date
      Date d = new Date();
      String dateStr =(new SimpleDateFormat("yyyy-MM-dd")).format(d);
      String dateExtStr = (new SimpleDateFormat("hh:mm:ss a")).format(d);
      tmpRunLabel.setText(dateStr);
      tmpRunLabel.setToolTipText(dateExtStr);
      
      horizontalGroup
        .addComponent(tmpSep)
        .addGroup(tableLayout.createSequentialGroup()
          .addComponent(tmpCheck)
          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
          .addComponent( tmpLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 141
                       , javax.swing.GroupLayout.PREFERRED_SIZE)
          .addGap(18, 18, 18)
          .addComponent(tmpProgressLabel
                       , javax.swing.GroupLayout.PREFERRED_SIZE
                       , 138, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
          .addComponent( tmpProgress, javax.swing.GroupLayout.DEFAULT_SIZE, 199
                       , Short.MAX_VALUE)
          .addComponent( tmpStopLabel, javax.swing.GroupLayout.DEFAULT_SIZE
                       , 10, 25)
          .addComponent( tmpTermLabel, javax.swing.GroupLayout.DEFAULT_SIZE
                       , 10, Short.MAX_VALUE)  
          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
          .addComponent(tmpRunLabel))
        .addGap(39, 39, 39);
       
      verticalGroup
        .addGroup(tableLayout.createParallelGroup(
          javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(tableLayout.createParallelGroup(
          javax.swing.GroupLayout.Alignment.LEADING, false)
          .addComponent( tmpCheck, javax.swing.GroupLayout.DEFAULT_SIZE
                       , javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addGroup(tableLayout.createParallelGroup(
             javax.swing.GroupLayout.Alignment.BASELINE)
             .addComponent( tmpLabel, javax.swing.GroupLayout.DEFAULT_SIZE
                          , javax.swing.GroupLayout.DEFAULT_SIZE
                          , Short.MAX_VALUE)
             .addComponent(tmpProgressLabel)
             .addComponent( tmpProgress, javax.swing.GroupLayout.DEFAULT_SIZE
                          , javax.swing.GroupLayout.DEFAULT_SIZE
                          , Short.MAX_VALUE)
             .addComponent( tmpStopLabel, javax.swing.GroupLayout.DEFAULT_SIZE
                          , javax.swing.GroupLayout.DEFAULT_SIZE
                          , Short.MAX_VALUE)
             .addComponent( tmpTermLabel, javax.swing.GroupLayout.DEFAULT_SIZE
                          , javax.swing.GroupLayout.DEFAULT_SIZE
                          , Short.MAX_VALUE)
              ))
           .addComponent(tmpRunLabel))
         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
         .addComponent( tmpSep, javax.swing.GroupLayout.PREFERRED_SIZE, 10
                      , javax.swing.GroupLayout.PREFERRED_SIZE)
         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
            
        tableLayout.setHorizontalGroup(horizontalGroup);
        tableLayout.setVerticalGroup(verticalGroup);
        
        return (tableCount++);
    }
    
    private void sortComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sortComboActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_sortComboActionPerformed

    private void setVisibleActions(boolean visible) {
        filterButton.setVisible(visible);
        rerunButton.setVisible(visible);
        deleteButton.setVisible(visible);
        cancelButton.setVisible(visible);
        viewButton.setVisible(visible);
    }
   
    private void updateActionVisibility() {
     boolean value = false;
     for(JCheckBox jc : tableCheck) 
         if( jc.isVisible() && jc.isSelected() ) value = true;
         
     setVisibleActions(value);        
    }
    
    private void checkActionPerformed(java.awt.event.ActionEvent evt) {
       updateActionVisibility();
    }
    
    private void allCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allCheckActionPerformed
     boolean value = ((JCheckBox)evt.getSource()).isSelected();
     boolean selectedOne = false;
     for(JCheckBox jc : tableCheck) {
         if( jc.isVisible() ) {
           jc.setSelected(value);
           selectedOne = true;
         }
     }
     if(selectedOne) setVisibleActions(value);
    }//GEN-LAST:event_allCheckActionPerformed

    private void runTopLevelButtons(ButtonType type) {
      for(int i = 0; i < tableCheck.size(); i ++) {
         JCheckBox jc = tableCheck.get(i);
         if( jc.isVisible() && jc.isSelected() ) {
             switch(type) { 
               case CANCEL: cancelRow(i); break;
               case DELETE: cancelRow(i); hideRow(i); break;
               case RERUN:  rerunRow(i); break;
               case FILTER: /*TODO*/ break;
             }
         }
     }

    }
    
    private void rerunButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rerunButtonActionPerformed
      runTopLevelButtons(ButtonType.RERUN);
    }//GEN-LAST:event_rerunButtonActionPerformed

    private void filterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterButtonActionPerformed
     runTopLevelButtons(ButtonType.FILTER);
    }//GEN-LAST:event_filterButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
      runTopLevelButtons(ButtonType.CANCEL);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
     runTopLevelButtons(ButtonType.DELETE);
     updateActionVisibility();
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void viewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewButtonActionPerformed
     for(int i = 0; i < tableCheck.size(); i ++) {
         JCheckBox jc = tableCheck.get(i);
         if( jc.isVisible() && jc.isSelected() ) {
             File file = new File(this.outputLocs.get(i));
             try { Desktop.getDesktop().open(file); }
             catch (IOException ex) {
               log.error(ex.getMessage()); 
             }
         }
     }
    }//GEN-LAST:event_viewButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
      JFileChooser chooser = new JFileChooser();
      FileNameExtensionFilter filter = TikaMimeTypes.makeFileBrowser();
      chooser.setFileFilter(filter);
      chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    
      try {
          File f = new File(Config.getInLocation());
          chooser.setCurrentDirectory(f);
      } catch (Exception e) {}
      int returnVal = chooser.showOpenDialog(this);
      if(returnVal == JFileChooser.APPROVE_OPTION) {
          String selFile = chooser.getSelectedFile().toString();
          Config.setInLocation(selFile);
          ApiHelper.processFile( selFile );
          Config.saveSettings();
      }
    }//GEN-LAST:event_addButtonActionPerformed

    private void addTextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addTextButtonActionPerformed
       JFrame.setDefaultLookAndFeelDecorated(true);
       JFrame frame = new TextEntry();
       frame.setVisible(true);// TODO add your handling code here:
    }//GEN-LAST:event_addTextButtonActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
       String path = System.getProperty("user.dir") + HELP_FILE;

       try { Desktop.getDesktop().open(new File(path)); }
       catch (IOException ex) { log.error(ex.getMessage()); }
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
    //    PropertyConfigurator.configure(l4j);
    //    logger = Logger.getLogger("RbReport");
    //    BasicConfigurator.configure();
        
        
        log.info("Starting Desktop Client");
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
             UIManager.setLookAndFeel(
               UIManager.getSystemLookAndFeelClassName());
                  if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    UIManager.getLookAndFeelDefaults().put("nimbusOrange", (Color.green));
                    break;
                  }
            }
        } catch (ClassNotFoundException ex) {
            log.error(ex.getMessage()); 
        } catch (InstantiationException ex) {
            log.error(ex.getMessage()); 
        } catch (IllegalAccessException ex) {
            log.error(ex.getMessage());  
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            log.error(ex.getMessage()); 
        }
        //</editor-fold>
//        Initialize.init();
//        Properties props = System.getProperties();
//        if(props.getProperty("opensextant.home") == null)
//          props.setProperty( "opensextant.home"
//                           , ApiHelper.BASE_PATH + "opensextant"); 
      /*  try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(OpenSextant.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(OpenSextant.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(OpenSextant.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(OpenSextant.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new OpenSextant().setVisible(true);
            }
        });
    }
    // Table layout 
    private javax.swing.GroupLayout tableLayout;
    private javax.swing.GroupLayout.Group horizontalGroup;
    private javax.swing.GroupLayout.SequentialGroup verticalGroup;
   
    // Table components
    private ArrayList<JCheckBox> tableCheck = new ArrayList<JCheckBox>();
    private ArrayList<JLabel> tableLabel = new ArrayList<JLabel>();
    private ArrayList<JLabel> tableRunLabel = new ArrayList<JLabel>();
    private ArrayList<JLabel> tableProgressLabel = new ArrayList<JLabel>();
    private ArrayList<JProgressBar> tableProgress=new ArrayList<JProgressBar>();
    private ArrayList<JSeparator> tableSep = new ArrayList<JSeparator>();
    private ArrayList<JLabel> tableStopLabel = new ArrayList<JLabel>();
    private ArrayList<JLabel> tableTermLabel = new ArrayList<JLabel>();
    
    // Table book keeping 
    private ArrayList<String> inputFiles = new ArrayList<String>();
    private ArrayList<String> outputLocs = new ArrayList<String>();
    private ArrayList<Long> timestamps = new ArrayList<Long>();
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton addTextButton;
    private javax.swing.JCheckBox allCheck;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton configButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton filterButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel lastRunLabel;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JLabel progressLabel;
    private javax.swing.JButton rerunButton;
    private javax.swing.JComboBox sortCombo;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JScrollPane tableScrollPane;
    private javax.swing.JSeparator tableTopSep;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JSeparator topSep;
    private javax.swing.JButton viewButton;
    // End of variables declaration//GEN-END:variables
}
