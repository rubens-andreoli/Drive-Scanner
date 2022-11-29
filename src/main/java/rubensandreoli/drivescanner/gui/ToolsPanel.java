/*
 * Copyright (C) 2022 Rubens A. Andreoli Jr.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package rubensandreoli.drivescanner.gui;

import java.awt.event.ItemEvent;
import java.io.File;
import rubensandreoli.drivescanner.gui.support.ActionEvent;
import rubensandreoli.drivescanner.gui.support.StringFormatter;
import rubensandreoli.drivescanner.io.Scan;
import rubensandreoli.drivescanner.gui.support.ActionEventListener;

public class ToolsPanel extends javax.swing.JPanel {

    private ActionEventListener listener;
    
    public ToolsPanel() {
        initComponents();
        
        btnScan.addActionListener(e -> fireAction(ActionEvent.SCAN));
        btnRename.addActionListener(e -> fireAction(ActionEvent.RENAME));
        btnDelete.addActionListener(e -> fireAction(ActionEvent.DELETE));
        btnUpdate.addActionListener(e -> fireAction(ActionEvent.UPDATE));
        btnStop.addActionListener(e -> fireAction(ActionEvent.STOP));
        cmbDrives.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                fireAction(ActionEvent.SELECT_DRIVE);
            }
        });
    }

    void setListener(ActionEventListener listener) {
        this.listener = listener;
    }
        
    private void fireAction(ActionEvent e){
        if (listener != null) {
            listener.eventOccurred(e);
        }
    }
    
    void addDrives(File[] drives) {
        for (File drive : drives) {
            cmbDrives.addItem(drive);
        }
    }
            
    File getSelectedDrive() {
        return (File) cmbDrives.getSelectedItem();
    }
    
    void setScan(Scan scan) {
        txtSize.setText(StringFormatter.formatSize(scan.getSize()));
        txtDate.setText(StringFormatter.formatDate(scan.getDate()));
        if(scan.isUpdated()){
            txtUpdatedSize.setText(StringFormatter.formatSize(scan.getUpdatedSize()));
            txtUpdatedDate.setText(StringFormatter.formatDate(scan.getUpdatedDate()));
        }
    }
    
    void setScanEnabled(boolean enabled){
        btnScan.setEnabled(!enabled);
        btnStop.setEnabled(enabled);
    }

    void setEditEnabled(boolean enabled) {
        btnRename.setEnabled(enabled);
        btnDelete.setEnabled(enabled);
        btnUpdate.setEnabled(enabled);
    }
    
    void setStopEnabled(boolean enabled){
        btnStop.setEnabled(enabled);
    }
    
    void clear() {
        txtSize.setText("");
        txtDate.setText("");
        txtUpdatedSize.setText("");
        txtUpdatedDate.setText("");
    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cmbDrives = new javax.swing.JComboBox<>();
        btnScan = new javax.swing.JButton();
        txtUpdatedSize = new javax.swing.JTextField();
        txtUpdatedDate = new javax.swing.JTextField();
        txtSize = new javax.swing.JTextField();
        txtDate = new javax.swing.JTextField();
        btnRename = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnStop = new javax.swing.JButton();

        cmbDrives.setToolTipText("Select a Drive to scan.");

        btnScan.setMnemonic('S');
        btnScan.setText("Scan");
        btnScan.setToolTipText("Scan folders from the selected Drive.");

        txtUpdatedSize.setEditable(false);
        txtUpdatedSize.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        txtUpdatedSize.setToolTipText("Updated scan size.");

        txtUpdatedDate.setEditable(false);
        txtUpdatedDate.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtUpdatedDate.setToolTipText("Updated scan date.");

        txtSize.setEditable(false);
        txtSize.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        txtSize.setToolTipText("Scan size.");

        txtDate.setEditable(false);
        txtDate.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtDate.setToolTipText("Scan date.");

        btnRename.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/rename.png"))); // NOI18N
        btnRename.setToolTipText("Rename the selected scan.");

        btnDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/delete.png"))); // NOI18N
        btnDelete.setToolTipText("Delete the selected scan.");

        btnUpdate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/update.png"))); // NOI18N
        btnUpdate.setToolTipText("Update the information about the selected scan folders.");

        btnStop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/stop.png"))); // NOI18N
        btnStop.setToolTipText("Stop current scan.");
        btnStop.setEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(txtSize, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtDate, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRename, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cmbDrives, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnScan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(txtUpdatedSize, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtUpdatedDate, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnStop, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbDrives, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnScan))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtSize, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtDate, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnRename, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtUpdatedSize, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtUpdatedDate, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnStop, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnRename;
    private javax.swing.JButton btnScan;
    private javax.swing.JButton btnStop;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JComboBox<File> cmbDrives;
    private javax.swing.JTextField txtDate;
    private javax.swing.JTextField txtSize;
    private javax.swing.JTextField txtUpdatedDate;
    private javax.swing.JTextField txtUpdatedSize;
    // End of variables declaration//GEN-END:variables
}
