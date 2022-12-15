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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import rubensandreoli.drivescanner.gui.support.DialogFactory;
import rubensandreoli.drivescanner.gui.support.StringFormatter;
import rubensandreoli.drivescanner.io.Folder;
import rubensandreoli.drivescanner.io.Scan;

public class TablePanel extends javax.swing.JPanel {

    //<editor-fold defaultstate="collapsed" desc="TABLE MODEL">
    private static class FolderTableModel extends AbstractTableModel {

        static class FolderEntry {
            private final Folder folder;
            private final String size;

            private FolderEntry(Folder folder, String size) {
                this.folder = folder;
                this.size = size;
            }
        }
        
        private final String[] colNames = {"Folder", "Size"};
        private List<FolderEntry> folderEntries = Collections.EMPTY_LIST;

        void setData(Scan scan){
            Set<Folder> folderSet = scan.getFolders();
            folderEntries = new ArrayList<>(folderSet.size());
            for (Folder folder : scan.getFolders()) {
                folderEntries.add(new FolderEntry(folder, StringFormatter.formatSize(folder.getCurrentSize())));
            }
            fireTableDataChanged();
        }

        void clearData(){
            folderEntries = Collections.EMPTY_LIST;
            fireTableDataChanged();
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
            return folderEntries.size();
        }

        @Override
        public Object getValueAt(int row, int column) {
            if (column == 0) {
                return folderEntries.get(row).folder;
            } else {
                return folderEntries.get(row).size;
            }
        }

    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="TABLE RENDERER">
    private static class FolderTableCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            Folder folder = (Folder) table.getModel().getValueAt(table.convertRowIndexToModel(row), 0);
            Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            if(!isSelected){
                switch (folder.getState()) {
                    case DELETED:
                        cell.setBackground(Color.RED);
                        break;
                    case INCREASED:
                        cell.setBackground(Color.GREEN);
                        break;
                    case DECREASED:
                        cell.setBackground(Color.YELLOW);
                        break;
                    default:
                        cell.setBackground(table.getBackground());
                }
            }
            return cell;
        }

    }
    //</editor-fold>
        
    //<editor-fold defaultstate="collapsed" desc="LISTENER">
    public static interface Listener{
        void onDeleteFolders(Set<Folder> folders);
        void onMoveFoldersNew(Set<Folder> folders);
        void onMoveFoldersInto(Set<Folder> folders);
    }
    //</editor-fold>
    
    private final FolderTableModel model = new FolderTableModel();
    private TableRowSorter<FolderTableModel> sorter = new TableRowSorter<>(model){ //used only for filtering.
        @Override
        public boolean isSortable(int column) {
            return false;
        }
    };
    private Listener listener; //listener cannot be null when action is performed.
    private boolean hasDesktopSupport;
    
    public TablePanel() {
        initComponents();
        
        hasDesktopSupport = Desktop.isDesktopSupported();
        if(hasDesktopSupport){
            mniOpen.addActionListener(e -> openSelectedFolder());
        } else {
            popupMenu.remove(mniOpen);
            popupMenu.remove(sprPopup);
        }
        
        //LISTENERS
        mniMoveNew.addActionListener(e -> listener.onMoveFoldersNew(getSelectedFolders()));
        mniMoveInto.addActionListener(e -> listener.onMoveFoldersInto(getSelectedFolders()));
        mniDelete.addActionListener(e -> listener.onDeleteFolders(getSelectedFolders()));
        tblFolders.addKeyListener(new KeyAdapter(){
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_DELETE && isSelectionNotEmpty()){
                    listener.onDeleteFolders(getSelectedFolders());
                }
            }
        });
        tblFolders.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent evt) {
                showPopupMenu(evt);
            }
            
            @Override
            public void mousePressed(MouseEvent evt) {
                if(!showPopupMenu(evt) && evt.getClickCount() == 2 && isSelectionNotEmpty()){
                    openSelectedFolder();
                }
            }
            
            private boolean showPopupMenu(MouseEvent evt){
                if(evt.isPopupTrigger()){
                    setPopupItemsEnabled(isSelectionNotEmpty());
                    popupMenu.show(tblFolders, evt.getX(), evt.getY());
                    return true;
                }
                return false;
            }
        });
        
        tblFolders.setRowSorter(sorter);
    }
    
    private boolean isSelectionNotEmpty(){
        return tblFolders.getSelectedRowCount() > 0;
    }
    
    private void openSelectedFolder(){
        if(!hasDesktopSupport) return; //feature not supported.
        for (Folder selectedFolder : getSelectedFolders()) {
            openFolder(selectedFolder);
        }
    }
    
    private void openFolder(Folder folder){
        File folderPath = folder.getFile();
        if (folderPath.canRead()) {
            try{
                Desktop.getDesktop().open(folderPath);
            }catch(SecurityException | IOException ex ){
                DialogFactory.showErrorDialog("Open Folder", "Folder could not be opened."
                                + "\n"+folder
                                + "\nVerify folder access permissions.");
            }
        } else {
            DialogFactory.showErrorDialog("Open Folder", "Folder not found."
                            + "\n"+folder
                            + "\nPerform an update to highlight in red the deleted folders.");
        }
    }
    
    void setListener(Listener listener) {
        this.listener = listener;
    }
    
    private Set<Folder> getSelectedFolders(){
        Set<Folder> folders = Scan.getNewFolderSet();
        for (int selectedRow : tblFolders.getSelectedRows()) {
            folders.add((Folder) tblFolders.getValueAt(selectedRow, 0));
        }
        return folders;
    }

    private void setPopupItemsEnabled(boolean enabled){
        mniOpen.setEnabled(enabled);
        mnuMove.setEnabled(enabled);
        mniDelete.setEnabled(enabled);
    }
    
    void clear() {
        model.clearData();
    }

    void setScan(Scan scan) {
        model.setData(scan);
    }
    
    void setFilter(final boolean unchanged, final boolean changed, final boolean deleted){
        if(unchanged && changed && deleted){
            sorter.setRowFilter(null);
        }else{
            sorter.setRowFilter(new RowFilter<FolderTableModel, Object>(){
                @Override
                public boolean include(RowFilter.Entry<? extends FolderTableModel, ? extends Object> entry) {
                    Folder.State folderState = ((Folder)entry.getValue(0)).getState();
                    if(unchanged && folderState == Folder.State.UNCHANGED) return true;
                    else if(deleted && folderState == Folder.State.DELETED) return true;
                    else if(changed && (folderState == Folder.State.INCREASED || folderState == Folder.State.DECREASED)) return true;
                    return false;
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        popupMenu = new javax.swing.JPopupMenu();
        mniOpen = new javax.swing.JMenuItem();
        sprPopup = new javax.swing.JPopupMenu.Separator();
        mnuMove = new javax.swing.JMenu();
        mniMoveNew = new javax.swing.JMenuItem();
        mniMoveInto = new javax.swing.JMenuItem();
        mniDelete = new javax.swing.JMenuItem();
        scrFolders = new javax.swing.JScrollPane();
        tblFolders = new javax.swing.JTable(model);
        tblFolders.getTableHeader().setReorderingAllowed(false);
        tblFolders.getColumnModel().getColumn(1).setMaxWidth(65);
        tblFolders.getColumnModel().getColumn(0).setResizable(false);
        tblFolders.getColumnModel().getColumn(1).setResizable(false);

        tblFolders.setDefaultRenderer(Object.class, new FolderTableCellRenderer());

        mniOpen.setText("Open");
        popupMenu.add(mniOpen);
        popupMenu.add(sprPopup);

        mnuMove.setText("Move");

        mniMoveNew.setText("New");
        mnuMove.add(mniMoveNew);

        mniMoveInto.setText("Into...");
        mnuMove.add(mniMoveInto);

        popupMenu.add(mnuMove);

        mniDelete.setText("Delete");
        popupMenu.add(mniDelete);

        setLayout(new java.awt.BorderLayout());

        scrFolders.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        tblFolders.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        tblFolders.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        scrFolders.setViewportView(tblFolders);

        add(scrFolders, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem mniDelete;
    private javax.swing.JMenuItem mniMoveInto;
    private javax.swing.JMenuItem mniMoveNew;
    private javax.swing.JMenuItem mniOpen;
    private javax.swing.JMenu mnuMove;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JScrollPane scrFolders;
    private javax.swing.JPopupMenu.Separator sprPopup;
    private javax.swing.JTable tblFolders;
    // End of variables declaration//GEN-END:variables
}
