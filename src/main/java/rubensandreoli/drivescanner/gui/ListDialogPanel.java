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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.event.ListSelectionListener;

public class ListDialogPanel<T> extends javax.swing.JPanel {

    //<editor-fold defaultstate="collapsed" desc="LIST MODEL">
    private static class DialogListModel extends AbstractListModel<Object> {

        private static final Comparator<Object> OBJECT_COMPARATOR = (o1, o2) -> o1.toString().compareToIgnoreCase(o2.toString());
        
        private final List<Object> data;

        public DialogListModel(Collection<?> items){
            data = new ArrayList<>(items);
            Collections.sort(data, OBJECT_COMPARATOR);
        }
        
        @Override
        public int getSize() {
            return data.size();
        }

        @Override
        public Object getElementAt(int index) {
            return data.get(index);
        }
        
    }
    //</editor-fold>

    public ListDialogPanel() {
        initComponents();
    }
    
    public void setText(String msg){
        lblMsg.setText("<html>"+msg+"</html>");
    }

    public void setItems(Collection<T> items){
        lstScans.setModel(new DialogListModel(items));
    }
    
    public void addSelectionListener(ListSelectionListener listener){
        lstScans.addListSelectionListener(listener);
    }
    
    public void setListTootltip(String text){
        lstScans.setToolTipText(text);
    }
    
    public T getSelectedValue(){
        return (T) lstScans.getSelectedValue();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblMsg = new javax.swing.JLabel();
        scrScans = new javax.swing.JScrollPane();
        lstScans = new javax.swing.JList();

        setMaximumSize(new java.awt.Dimension(300, 100));
        setPreferredSize(new java.awt.Dimension(300, 100));

        lblMsg.setText("Label");

        scrScans.setPreferredSize(new java.awt.Dimension(200, 100));

        lstScans.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        scrScans.setViewportView(lstScans);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblMsg, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(scrScans, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(lblMsg, javax.swing.GroupLayout.DEFAULT_SIZE, 19, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrScans, javax.swing.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblMsg;
    private javax.swing.JList lstScans;
    private javax.swing.JScrollPane scrScans;
    // End of variables declaration//GEN-END:variables
}
