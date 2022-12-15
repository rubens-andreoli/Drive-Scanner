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

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Collection;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import rubensandreoli.drivescanner.io.Scan;

public class ListPanel extends javax.swing.JPanel {

    //<editor-fold defaultstate="collapsed" desc="LISTENER">
    public static interface Listener{
        void onSelectScan(Scan scan);
        void onDeleteScan(Collection<Scan> scans);
    }
    //</editor-fold>
    
    private Listener listener;
    private final DefaultListModel<Scan> lstScansModel = new DefaultListModel<>();
    
    public ListPanel() {
        initComponents();

        lstScans.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()){
                if(!isMultipleSelected() && getSelectedScan() != null){
                    listener.onSelectScan(getSelectedScan());
                }
            }
        });
        
        lstScans.addKeyListener(new KeyAdapter(){
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_DELETE && !lstScans.isSelectionEmpty()){
                    listener.onDeleteScan(getSelectedScans());
                }
            }
        });
    }
    
    void setListener(Listener listener) {
        this.listener = listener;
    }
        
    Scan getSelectedScan() {
        return lstScans.getSelectedValue();
    }

    Collection<Scan> getSelectedScans(){
        return lstScans.getSelectedValuesList();
    }
    
    boolean isMultipleSelected(){
        return lstScans.getSelectedIndices().length > 1;
    }

    void setScans(Collection<Scan> scans) {
        lstScans.setValueIsAdjusting(true);
        lstScansModel.clear();
        for (Scan scan : scans) {
            lstScansModel.addElement(scan);
        }
        lstScans.setValueIsAdjusting(false);
    }
    
    void setSelectedScan(Scan scan){
        lstScans.setSelectedValue(scan, true);
    }
    
    void addScan(Scan scan, boolean select){
        lstScansModel.addElement(scan);
        if(select) setSelectedScan(scan);
    }
    
    void removeScan(Scan scan){
        lstScansModel.removeElement(scan);
    }
    
    void removeScans(Collection<Scan> scans){
        lstScans.setValueIsAdjusting(true);
        for (Scan scan : scans) {
            removeScan(scan);
        }
        lstScans.setValueIsAdjusting(false);
    }
    
    void clear(){
        lstScansModel.clear();
    }

    void replaceScan(Scan oldScan, Scan newScan, boolean select){
        lstScans.setValueIsAdjusting(true);
        int index = lstScansModel.indexOf(oldScan);
        lstScansModel.removeElement(oldScan);
        lstScansModel.insertElementAt(newScan, index);
        if(select) lstScans.setSelectedIndex(index);
        lstScans.setValueIsAdjusting(false);
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrScans = new javax.swing.JScrollPane();
        lstScans = new JList<>(){
            @Override
            protected void processKeyEvent(KeyEvent e) {
                if(e.isShiftDown()){
                    //bypass JComponent.processKeyEvent(e) call to Component.processKeyEvent(e) when shift is down.
                    ListPanel.this.processKeyEvent(e);
                }else{
                    super.processKeyEvent(e);
                }
            }
        };

        scrScans.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        lstScans.setModel(lstScansModel);
        scrScans.setViewportView(lstScans);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(scrScans, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scrScans, javax.swing.GroupLayout.Alignment.TRAILING)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList<Scan> lstScans;
    private javax.swing.JScrollPane scrScans;
    // End of variables declaration//GEN-END:variables
}
