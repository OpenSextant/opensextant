/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mitre.opensextant.desktop.ui.forms;


/**
 *
 * @author aquina
 */
public class OpenSextantMainFrame extends javax.swing.JFrame {

    /**
     * Creates new form OpenSextantMainFrame
     */
    public OpenSextantMainFrame() {
        initComponents();
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
        topSep = new javax.swing.JSeparator();
        tableTopSep = new javax.swing.JSeparator();
        addButton = new javax.swing.JButton();
        addTextButton = new javax.swing.JButton();
        bodyPanel = new javax.swing.JPanel();
        treePanel = new javax.swing.JPanel();
        statusLabel = new javax.swing.JLabel();
        helpButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("OpenSextant Desktop");

        configButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mitre/opensextant/desktop/icons/cog.png"))); // NOI18N
        configButton.setToolTipText("Configuration");
        configButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configButtonActionPerformed(evt);
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

        treePanel.setLayout(new java.awt.GridLayout(1, 0));

        statusLabel.setText(" ");

        helpButton.setFont(new java.awt.Font("Tahoma", 0, 5)); // NOI18N
        helpButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mitre/opensextant/desktop/icons/questionSmall.png"))); // NOI18N
        helpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout bodyPanelLayout = new org.jdesktop.layout.GroupLayout(bodyPanel);
        bodyPanel.setLayout(bodyPanelLayout);
        bodyPanelLayout.setHorizontalGroup(
            bodyPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(bodyPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(bodyPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(bodyPanelLayout.createSequentialGroup()
                        .add(statusLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 850, Short.MAX_VALUE)
                        .add(18, 18, 18)
                        .add(helpButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(treePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        bodyPanelLayout.setVerticalGroup(
            bodyPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(bodyPanelLayout.createSequentialGroup()
                .add(treePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 330, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(bodyPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(statusLabel)
                    .add(helpButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout mainPanelLayout = new org.jdesktop.layout.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(mainPanelLayout.createSequentialGroup()
                        .add(addButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 35, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(addTextButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 35, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(configButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 35, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(mainPanelLayout.createSequentialGroup()
                        .add(bodyPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(34, 34, 34)
                        .add(tableTopSep)
                        .add(907, 907, 907))
                    .add(mainPanelLayout.createSequentialGroup()
                        .add(topSep, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 903, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(0, 0, Short.MAX_VALUE))))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(addButton)
                    .add(addTextButton)
                    .add(configButton))
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(mainPanelLayout.createSequentialGroup()
                        .add(105, 105, 105)
                        .add(tableTopSep, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(mainPanelLayout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(topSep, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(2, 2, 2)
                        .add(bodyPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(mainPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 906, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mainPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void configButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_configButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addButtonActionPerformed

    private void addTextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addTextButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addTextButtonActionPerformed

    private void helpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_helpButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(OpenSextantMainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(OpenSextantMainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(OpenSextantMainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(OpenSextantMainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new OpenSextantMainFrame().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JButton addButton;
    protected javax.swing.JButton addTextButton;
    private javax.swing.JPanel bodyPanel;
    protected javax.swing.JButton configButton;
    protected javax.swing.JButton helpButton;
    protected javax.swing.JPanel mainPanel;
    private javax.swing.JLabel statusLabel;
    protected javax.swing.JSeparator tableTopSep;
    protected javax.swing.JSeparator topSep;
    protected javax.swing.JPanel treePanel;
    // End of variables declaration//GEN-END:variables
	
    
}
