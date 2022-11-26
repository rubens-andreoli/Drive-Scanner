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

import java.util.Collection;
import javax.swing.DefaultListModel;
import rubensandreoli.drivescanner.gui.support.ActionEvent;
import rubensandreoli.drivescanner.gui.support.ActionEventListener;
import rubensandreoli.drivescanner.io.Scan;

public class ListPanel extends javax.swing.JPanel {

    //<editor-fold defaultstate="collapsed" desc="LIST MODEL">
    private class ListModel extends DefaultListModel<Scan>{
        public void update(Scan scan){
            final int index = this.indexOf(scan);
            if(index > -1) fireContentsChanged(this, index, index);
        }
    }
    //</editor-fold>
    
    private ActionEventListener listener;
    private final ListModel lstScansModel = new ListModel();
    
    public ListPanel() {
        initComponents();
        
        lstScans.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && lstScans.getSelectedValue() != null){
                fireAction(ActionEvent.SELECT_SCAN);
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
    
    Scan getSelectedScan() {
        return lstScans.getSelectedValue();
    }
    
    void setScans(Collection<Scan> scanNames) {
        lstScans.setValueIsAdjusting(true);
        lstScansModel.clear();
        for (Scan scan : scanNames) {
            lstScansModel.addElement(scan);
        }
        lstScans.setValueIsAdjusting(false);
    }
    
    void addScan(Scan scan){
        lstScansModel.addElement(scan);
        lstScans.setSelectedValue(scan, true);
    }
    
    void removeScan(Scan scan){
        lstScansModel.removeElement(scan);
    }
    
    void updateScan(Scan scan){
        lstScansModel.update(scan);
    }
    
    void replaceScan(Scan oldScan, Scan newScan){
        lstScans.setValueIsAdjusting(true);
        final int index = lstScansModel.indexOf(oldScan);
        lstScansModel.removeElement(oldScan);
        lstScansModel.insertElementAt(newScan, index);
        lstScans.setSelectedIndex(index);
        lstScans.setValueIsAdjusting(false);
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrScans = new javax.swing.JScrollPane();
        lstScans = new javax.swing.JList<>();

        scrScans.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        lstScans.setModel(lstScansModel);
        scrScans.setViewportView(lstScans);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(scrScans, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scrScans, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList<Scan> lstScans;
    private javax.swing.JScrollPane scrScans;
    // End of variables declaration//GEN-END:variables
}
