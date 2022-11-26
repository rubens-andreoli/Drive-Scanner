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

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import rubensandreoli.drivescanner.gui.support.StringFormatter;
import rubensandreoli.drivescanner.io.Folder;
import rubensandreoli.drivescanner.io.Scan;

public class TablePanel extends javax.swing.JPanel {

    //<editor-fold defaultstate="collapsed" desc="TABLE MODEL">
    private class TableModel extends AbstractTableModel {

        private final String[] colNames = {"Folder", "Size"};
        private Scan scan;
        private TableFilter filter;
        private Map<Folder, String> scanFolders = getMap();

        void setData(Scan scan){
            this.scan = scan;
            populate();
        }
        
        private void populate(){
            scanFolders = getMap();
            if(filter == null || filter.shouldDisplayAll()){
                for (Folder folder : scan.getFolders()) {
                    scanFolders.put(folder, StringFormatter.formatSize(folder.getSize()));
                }
            }else{
                for (Folder folder : scan.getFolders()) {
                    if(filter.shouldDisplay(folder.getState())){
                        scanFolders.put(folder, StringFormatter.formatSize(folder.getSize()));
                    }
                }
            }
            fireTableDataChanged();
        }
        
        void clearData(){
            this.scan = null;
            scanFolders = getMap();
            fireTableDataChanged();
        }
        
        void setFilter(TableFilter filter){
            this.filter = filter;
            if(scan != null){
                populate();
            }
        }
        
        private static Map<Folder, String> getMap(){
            return new TreeMap<>();
        }

        @Override
        public String getColumnName(int column) {
            return colNames[column];
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public int getRowCount() {
            return scanFolders.size();
        }

        @Override
        public Object getValueAt(int row, int column) {
            if (column == 0) {
                return scanFolders.keySet().toArray()[row];
            } else {
                return scanFolders.get(scanFolders.keySet().toArray()[row]);
            }
        }

    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="TABLE RENDERER">
    private class TableCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            final Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            final Folder folder = (Folder) table.getModel().getValueAt(row, 0);
            if (folder.getState() == Folder.State.DELETED) {
                cell.setBackground(Color.RED);
            } else if (folder.getState() == Folder.State.INCREASED) {
                cell.setBackground(Color.GREEN);
            } else if (folder.getState() == Folder.State.DECREASED) {
                cell.setBackground(Color.YELLOW);
            } else {
                cell.setBackground(table.getBackground());
            }
//            if (folder.isRoot()) { //TODO: implement bold root folder.
//                setFont(getFont().deriveFont(Font.BOLD));
//            }
            return cell;
        }

    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="TABLE FILTER">
    private class TableFilter {

        private final HashSet<Folder.State> toDisplay;

        private TableFilter(boolean unchanged, boolean changed, boolean deleted) {
            toDisplay = new HashSet<>();
            if(unchanged) toDisplay.add(Folder.State.UNCHANGED);
            if(changed){
                toDisplay.add(Folder.State.INCREASED);
                toDisplay.add(Folder.State.DECREASED);
            }
            if(deleted) toDisplay.add(Folder.State.DELETED);
        }

        boolean shouldDisplay(Folder.State state){
            return toDisplay.contains(state);
        }

        boolean shouldDisplayAll(){
            return toDisplay.size() == 4;
        }

    }
    //</editor-fold>
    
    private final TableModel tblFoldersModel = new TableModel();

    public TablePanel() {
        initComponents();
        
        tblFolders.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) {
                final int row = tblFolders.columnAtPoint(me.getPoint());
                if (me.getClickCount() == 2 && row == 0) {
                    final File folderPath = ((Folder) tblFolders.getValueAt(tblFolders.getSelectedRow(), 0)).getFile();
                    if(!Desktop.isDesktopSupported()) return; //feature not supported
                    try{
                        if (folderPath.canRead()) {
                            Desktop.getDesktop().open(folderPath);
                        } else {
                            JOptionPane.showMessageDialog(null,
                                    "Folder not found. Perform an update to highlight in red the deleted folders.",
                                    "Error: File Not Found",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } catch(SecurityException | IOException ex ){
                        JOptionPane.showMessageDialog(null,
                                        "Folder could not be opened. Verify folder access permissions.",
                                        "Error: File Access Denied",
                                        JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }

    void clear() {
        tblFoldersModel.clearData();
    }

    void setScan(Scan scan) {
        tblFoldersModel.setData(scan);
    }
    
    void setFilter(boolean unchanged, boolean changed, boolean deleted){
        tblFoldersModel.setFilter(new TableFilter(unchanged, changed, deleted));
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrFolders = new javax.swing.JScrollPane();
        tblFolders = new javax.swing.JTable(tblFoldersModel);
        tblFolders.getTableHeader().setReorderingAllowed(false);
        tblFolders.getColumnModel().getColumn(1).setMaxWidth(65);
        tblFolders.getColumnModel().getColumn(0).setResizable(false);
        tblFolders.getColumnModel().getColumn(1).setResizable(false);

        tblFolders.setDefaultRenderer(Object.class, new TableCellRenderer());

        setLayout(new java.awt.BorderLayout());

        scrFolders.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrFolders.setViewportView(tblFolders);

        add(scrFolders, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane scrFolders;
    private javax.swing.JTable tblFolders;
    // End of variables declaration//GEN-END:variables
}
