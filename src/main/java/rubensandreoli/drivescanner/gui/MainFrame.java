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

import java.awt.Cursor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import rubensandreoli.drivescanner.gui.support.DialogFactory;
import rubensandreoli.drivescanner.gui.support.IconLoader;
import rubensandreoli.drivescanner.io.Folder;
import rubensandreoli.drivescanner.io.Repository;
import rubensandreoli.drivescanner.io.Scan;
import rubensandreoli.drivescanner.io.Scanner;

/**
 * Resources:
 * https://www.flaticon.com/free-icon/multiply_399274
 * https://www.flaticon.com/free-icon/refresh-button_61444
 * https://www.flaticon.com/free-icon/bin_484611
 * https://www.flaticon.com/free-icon/rename_5376272
 * https://www.flaticon.com/free-icon/search_3121624
 * 
 * @author Rubens A. Andreoli Jr.
 */
public class MainFrame extends javax.swing.JFrame {

    private Repository.Data data;
    private Scan currentScan;
    private SwingWorker currentWorker;
    private boolean locked = false;
    private final DialogFactory df;

    public MainFrame() {
        initComponents();
        setLocationRelativeTo(null);
        
        //LOAD DATA
        loadData();
        
        //SET LISTENERS
        mniScan.addActionListener(e -> scanDrive());
        mniStop.addActionListener(e -> stopAction());
        mniAbout.addActionListener(e -> showAboutDialog());
        mniExit.addActionListener(e -> exit());
        mniRename.addActionListener(e -> renameScan());
        mniMerge.addActionListener(e -> mergeSelectedScans());
        mniDelete.addActionListener(e -> deleteScans(null));
        mniDeleteAll.addActionListener(e -> deleteAllScans());
        mniUpdate.addActionListener(e -> updateScan());
        mncTools.addActionListener(e -> toggleSidePanel());
        mncUnchanged.addActionListener(e -> applyTableFilters());
        mncDeleted.addActionListener(e -> applyTableFilters());
        mncChanged.addActionListener(e -> applyTableFilters());
        toolsPanel.setListener(new ToolsPanel.Listener() {
            public @Override void onDriveChange(File drive) {changeSelectedDrive(drive);}
            public @Override void onScan() {scanDrive();}
            public @Override void onUpdateScan() {updateScan();}
            public @Override void onStop() {stopAction();}
            public @Override void onDeleteScan() {deleteScans(null);}
            public @Override void onRenameScan() {renameScan();}
        });
        listPanel.setListener(new ListPanel.Listener() {
            public @Override void onSelectScan(Scan scan) {selectScan(scan);}
            public @Override void onDeleteScan(Collection<Scan> scans) {deleteScans(scans);}
        });
        tablePanel.setListener(new TablePanel.Listener() {
            public @Override void onDeleteFolders(Set<Folder> folders) {deleteFolders(folders);}
            public @Override void onMoveFoldersNew(Set<Folder> folders) {moveFoldersNew(folders);}
            public @Override void onMoveFoldersInto(Set<Folder> folders) {moveFoldersInto(folders);}
        });
        
        toolsPanel.addDrives(Scanner.getRoots());
        
        df = new DialogFactory(this);
    }
    
    //<editor-fold defaultstate="collapsed" desc="ACTIONS">
    private void changeSelectedDrive(File drive) {
        if(data != null){ //data is null if still being loaded.
            driveSelected();
            listPanel.setScans(data.getDriveScans(drive));
        }
    }
    
    private void selectScan(Scan scan) {
        currentScan = scan;
        setEditEnabled(!locked); //edit scan only if not locked.
        toolsPanel.setScan(currentScan);
        tablePanel.setScan(currentScan);
        statusPanel.setTotals(currentScan.getTotalFolders(), currentScan.getTotalFiles());
    }
    
    private void loadData(){
        SwingWorker <Repository.Data, String> loadWorker = new SwingWorker<>() {
            private final List<Repository.Error> errors = new ArrayList<>(); //changed from another thread.
            
            @Override
            protected Repository.Data doInBackground() throws Exception {
                return Repository.getInstance().load(new Repository.LoadListener(){
                    @Override
                    public void onLoading(String filename) {
                        publish(filename);
                    }
                    
                    @Override
                    public void onLoadError(Repository.Error e) {
                        errors.add(e);
                    }
                });
            }

            @Override
            protected void process(List<String> msgs) {
                if(isDone()) return; //queued values may be set after worker is done.
                for (String msg : msgs) {
                    statusPanel.setMessage("Loading: "+msg);
                }
            }
            
            @Override
            protected void done() {
                statusPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                data = Repository.getInstance().getData();
                setLocked(false);
                changeSelectedDrive(toolsPanel.getSelectedDrive());
                if(!errors.isEmpty() && df.showLoadingErrorDialog(errors)) {
                    Collection<String> filenames = new ArrayList<>(errors.size());
                    for (Repository.Error error : errors) {
                        filenames.add(error.message);
                    }
                    if(!Repository.getInstance().deleteScans(filenames)){
                        df.showErrorDialog("Delete", "One or more files could not be deleted."
                                + "\nPlease delete it manually if it still exists.");
                    }
                }
            }
        };
        setLocked(true);
        setStopEnabled(false);
        statusPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        loadWorker.execute();
    }
 
    private void scanDrive() {
        final String actionName = "Scan";
        if(showLocked(actionName)) return;

        String scanName = showCreateNameDialog(actionName, "Enter a name for your scan:");
        if(scanName == null) return;
        
        SwingWorker <Scan, String> scanWorker = new SwingWorker<>() {
            @Override
            protected Scan doInBackground() throws Exception {
                File drive = toolsPanel.getSelectedDrive();
                
                Scan newScan = new Scanner(new Scanner.Handler(){
                    @Override
                    public void setStatus(String status) {
                        publish(status);
                    }

                    @Override
                    public boolean isInterrupted() {
                        return isCancelled();
                    }
                }).scan(scanName, drive, data.getDriveFolders(drive));
                return newScan;
            }
            
            @Override
            protected void process(List<String> msgs) {
                if(isDone()) return; //queued values may be set after worker is done.
                for (String msg : msgs) {
                    statusPanel.setMessage(msg);
                }
            }

            @Override
            protected void done() {
                statusPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                statusPanel.clearMessage();
                setLocked(false);
                
                Scan newScan;
                try { 
                    newScan = get();
                } catch (Exception ex) {
                    newScan = null;
                }
                
                if(newScan == null) return; //exception or cancelled.
                if(!newScan.isEmpty()){
                    //TODO: save scan in new thread. Or the same as scan but return scan and only affect this frame on done.
                    Repository.getInstance().addScan(newScan, new Repository.WorkListener(){
                        @Override
                        public void onDone(Scan scan) {
                            currentScan = scan;
                            listPanel.addScan(currentScan, true);
                        }

                        @Override
                        public void onError(Repository.Error e) {
                            df.showSaveErrorDialog(e);
                        }
                    });
                }else{
                    df.showWarningDialog(actionName, "No new folders were found, this scan will not be saved.");
                }
                System.gc();
            }
        };
        setLocked(true);
        statusPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        currentWorker = scanWorker;
        scanWorker.execute();
    }
    
    private void updateScan() {
        if(showLocked("Update")) return;
        
        SwingWorker <Void, String> updateWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                new Scanner(new Scanner.Handler(){
                    @Override
                    public void setStatus(String status) {
                        publish(status);
                    }

                    @Override
                    public boolean isInterrupted() {
                        return isCancelled();
                    }
                }).update(currentScan);
                return null;
            }

            @Override
            protected void process(List<String> msgs) {
                if(isDone()) return; //queued values may be set after worker is done.
                for (String msg : msgs) {
                    statusPanel.setMessage(msg);
                }
            }

            @Override
            protected void done() {
                statusPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                statusPanel.clearMessage();
                setLocked(false);
                if(!isCancelled()){
                    //TODO: save scan in new thread. Or the same as scan but return scan and only affect this frame on done.
                    Repository.getInstance().updateScan(currentScan, e -> df.showSaveErrorDialog(e));
                    selectScan(currentScan);
                }
                System.gc();
            }
        };
        setLocked(true);
        statusPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        currentWorker = updateWorker;
        updateWorker.execute();
    }
    
    private void stopAction() {
        if(currentWorker != null) currentWorker.cancel(false);
    }
    
    private void deleteScans(Collection<Scan> scans) {
        final String actionName = "Delete Selected";
        if(showLocked(actionName)) return;
        
        if (df.showConfirmDialog(actionName, "Are you sure you want to delete the selected scan(s)?")) {
            if(scans == null) deleteScan(currentScan, actionName);
            else{
                for (Scan scan : scans) {
                    deleteScan(scan, actionName);
                }
            }
        }
    }
       
    private void deleteAllScans(){
        final String actionName = "Delete All";
        if(showLocked(actionName)) return;
        
        if(df.showConfirmDialog(actionName, "Are you sure you want to delete all scans from the selected drive?")){
            Collection<Scan> failed = Repository.getInstance().deleteScans(toolsPanel.getSelectedDrive());
            if(failed.isEmpty()){
                listPanel.clear();
            }else{
                listPanel.removeScans(failed);
                df.showDeleteAllErrorDialog(failed);
            }
            driveSelected();
        }
    }
    
    private void renameScan() {
        final String actionName = "Rename";
        if(showLocked(actionName)) return;

        String scanNewName = showCreateNameDialog(actionName, "Enter a new name for your scan:");
        if(scanNewName == null) return;
        
        //TODO: rename scan in new thread.
        Repository.getInstance().renameScan(currentScan, scanNewName, new Repository.WorkListener() {
            @Override
            public void onDone(Scan scan) {
                listPanel.replaceScan(currentScan, scan, true);
            }

            @Override
            public void onError(Repository.Error e) {
               df.showSaveErrorDialog(e);
            }
        });
    }
    
    private void mergeSelectedScans(){
        final String actionName = "Merge";
        
        if(!listPanel.isMultipleSelected()){
            df.showWarningDialog(actionName, "Multiple scans must be selected to perform a merge operation.");
        }else{
            if(showLocked(actionName)) return;
            if(df.showConfirmDialog(actionName, "Are you sure you want to merge all the selected scans?"
                    + "\nAll scans will be merged into the first one selected: [" + currentScan +"]")){
                Collection<Scan> selectedScans = listPanel.getSelectedScans();
                Repository.getInstance().mergeScans(selectedScans, currentScan, new Repository.WorkListener() {
                    @Override
                    public void onDone(Scan scan) {
                        listPanel.removeScans(selectedScans);
                        listPanel.replaceScan(currentScan, scan, true);
                    }

                    @Override
                    public void onError(Repository.Error e) {
                        df.showSaveErrorDialog(e);
                    }
                });
            }
        }
    }
    
    private void deleteFolders(Set<Folder> folders) {
        final String actionName = "Delete Folders";
        if(showLocked(actionName)) return;
        
        if(df.showConfirmDialog(actionName, "Are you sure you want to delete the selected folder(s) from this scan?")){
            //TODO: delete scan's folders in new thread.
            Repository.getInstance().deleteScanFolders(currentScan, folders, new Repository.WorkListener() {
            @Override
            public void onDone(Scan scan) {
                listPanel.replaceScan(currentScan, scan, true);
                checkScanEmpty(actionName);
            }

            @Override
            public void onError(Repository.Error e) {
                df.showSaveErrorDialog(e);
            }
        });
        }
    }

    private void moveFoldersInto(Set<Folder> folders) {
        final String actionName = "Move Folders";
        if(showLocked(actionName)) return;
        
        Scan selectedScan = df.showSelectScanDialog(data.getDriveScans(toolsPanel.getSelectedDrive()));
        if(selectedScan != null){
            Repository.getInstance().moveScanFolders(currentScan, selectedScan, folders, new Repository.MoveListener() {
                @Override
                public void onMoved(Scan scanFrom, Scan scanTo) {
                    listPanel.replaceScan(selectedScan, scanTo, false); //without selection must be first.
                    listPanel.replaceScan(currentScan, scanFrom, true);
                    checkScanEmpty(actionName);
                }
                
                @Override
                public void onMoveError(Repository.Error e) {
                    df.showSaveErrorDialog(e);
                }
            });
        }
    }
    
    private void moveFoldersNew(Set<Folder> folders){
        final String actionName = "Move Folders";
        if(showLocked(actionName)) return;
        
        String scanName = showCreateNameDialog(actionName, "Enter a name for your scan:");
        if(scanName == null) return;
        Repository.getInstance().moveScanFolders(currentScan, scanName, toolsPanel.getSelectedDrive(), folders, new Repository.MoveListener() {
            @Override
            public void onMoved(Scan scanFrom, Scan scanTo) {
                listPanel.addScan(scanTo, false); //without selection must be first.
                listPanel.replaceScan(currentScan, scanFrom, true);
                checkScanEmpty(actionName);
            }

            @Override
            public void onMoveError(Repository.Error e) {
                df.showSaveErrorDialog(e);
            }
        });
    }
        
    private void exit(){
        System.exit(0);
    }
    
    private void toggleSidePanel(){
        sidePanel.setVisible(mncTools.isSelected());
    }
    
    private void applyTableFilters(){
        tablePanel.setFilter(mncUnchanged.isSelected(), mncChanged.isSelected(), mncDeleted.isSelected());
    }
    
    private void showAboutDialog(){
        df.showAboutDialog();
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="HELPERS">
    private void setStopEnabled(boolean enabled){
        toolsPanel.setStopEnabled(enabled);
        mniStop.setEnabled(enabled);
    }
    
    private void setEditEnabled(boolean enabled){
        toolsPanel.setEditEnabled(enabled);
        mniRename.setEnabled(enabled);
        mniDelete.setEnabled(enabled);
        mniUpdate.setEnabled(enabled);
        mniMerge.setEnabled(enabled);
    }
    
    private void driveSelected(){
        currentScan = null;
        setEditEnabled(false);
        mniDeleteAll.setEnabled(isSelectedDriveNotEmpty());
        toolsPanel.clear();
        tablePanel.clear();
        statusPanel.clear();
    }
    
    private void setLocked(boolean locked){
        this.locked = locked;
        toolsPanel.setScanEnabled(locked);
        mniScan.setEnabled(!locked);
        mniStop.setEnabled(locked);
        setEditEnabled(!locked/* && listPanel.getSelectedScan() != null*/); //only if selected something.
        mniDeleteAll.setEnabled(locked? locked : isSelectedDriveNotEmpty()); //if not locked, check if there is any scans to delete.
    }
    
    private void deleteScan(Scan scan, String action){
        if (Repository.getInstance().deleteScan(scan)) {
            listPanel.removeScan(scan);
            driveSelected();
        } else {
            df.showErrorDialog(action, "Scan file not found.\nReopening the program should clear it from the list.");
        }
    }
        
    private boolean isSelectedDriveNotEmpty(){
        return !data.isDriveEmpty(toolsPanel.getSelectedDrive());
    }
    
    private boolean showLocked(String action){
        if(locked) df.showWarningDialog(action, "This action cannot be performed while the program is busy.");
        return locked;
    }
    
    private void checkScanEmpty(String action){
        if(currentScan.isEmpty() && df.showConfirmDialog(action, "Due to being edited the selected scan is now empty."
                + "\nDo you want to delete it?")){
            deleteScan(currentScan, action);
        }
    }
    
    private String showCreateNameDialog(String title, String msg){
        return df.showCreateScanNameDialog(title, msg, toolsPanel.getSelectedDrive());
    }
    //</editor-fold>

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        contentPanel = new javax.swing.JPanel();
        tablePanel = new rubensandreoli.drivescanner.gui.TablePanel();
        statusPanel = new rubensandreoli.drivescanner.gui.StatusPanel();
        sidePanel = new javax.swing.JPanel();
        toolsPanel = new rubensandreoli.drivescanner.gui.ToolsPanel();
        listPanel = new rubensandreoli.drivescanner.gui.ListPanel();
        menuBar = new javax.swing.JMenuBar();
        mnuFile = new javax.swing.JMenu();
        mniScan = new javax.swing.JMenuItem();
        mniStop = new javax.swing.JMenuItem();
        sprFile1 = new javax.swing.JPopupMenu.Separator();
        mnuEdit = new javax.swing.JMenu();
        mniRename = new javax.swing.JMenuItem();
        mniMerge = new javax.swing.JMenuItem();
        mniDelete = new javax.swing.JMenuItem();
        mniDeleteAll = new javax.swing.JMenuItem();
        sprEdit = new javax.swing.JPopupMenu.Separator();
        mniUpdate = new javax.swing.JMenuItem();
        mnuView = new javax.swing.JMenu();
        mncTools = new javax.swing.JCheckBoxMenuItem();
        sprView = new javax.swing.JPopupMenu.Separator();
        mncUnchanged = new javax.swing.JCheckBoxMenuItem();
        mncChanged = new javax.swing.JCheckBoxMenuItem();
        mncDeleted = new javax.swing.JCheckBoxMenuItem();
        sprFile2 = new javax.swing.JPopupMenu.Separator();
        mniExit = new javax.swing.JMenuItem();
        mnuHelp = new javax.swing.JMenu();
        mniAbout = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Drive Scanner");
        setIconImage(IconLoader.getIcon("icon.png").getImage());
        setMinimumSize(new java.awt.Dimension(500, 250));

        contentPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 6, 6, 6));
        contentPanel.setLayout(new java.awt.BorderLayout(0, 6));
        contentPanel.add(tablePanel, java.awt.BorderLayout.CENTER);
        contentPanel.add(statusPanel, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(contentPanel, java.awt.BorderLayout.CENTER);

        sidePanel.setLayout(new java.awt.BorderLayout());

        toolsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 6, 6, 0));
        sidePanel.add(toolsPanel, java.awt.BorderLayout.PAGE_START);

        listPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 6, 6, 0));
        sidePanel.add(listPanel, java.awt.BorderLayout.CENTER);

        getContentPane().add(sidePanel, java.awt.BorderLayout.WEST);

        mnuFile.setMnemonic('F');
        mnuFile.setText("File");

        mniScan.setText("Scan");
        mnuFile.add(mniScan);

        mniStop.setText("Stop");
        mniStop.setEnabled(false);
        mnuFile.add(mniStop);
        mnuFile.add(sprFile1);

        mnuEdit.setMnemonic('E');
        mnuEdit.setText("Edit");

        mniRename.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0));
        mniRename.setText("Rename");
        mnuEdit.add(mniRename);

        mniMerge.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F3, 0));
        mniMerge.setText("Merge");
        mnuEdit.add(mniMerge);

        mniDelete.setText("Delete");
        mnuEdit.add(mniDelete);

        mniDeleteAll.setText("Delete All");
        mniDeleteAll.setEnabled(false);
        mnuEdit.add(mniDeleteAll);
        mnuEdit.add(sprEdit);

        mniUpdate.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0));
        mniUpdate.setText("Update");
        mnuEdit.add(mniUpdate);

        mnuFile.add(mnuEdit);

        mnuView.setMnemonic('V');
        mnuView.setText("View");

        mncTools.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.SHIFT_DOWN_MASK));
        mncTools.setSelected(true);
        mncTools.setText("Tools");
        mnuView.add(mncTools);
        mnuView.add(sprView);

        mncUnchanged.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_U, java.awt.event.InputEvent.SHIFT_DOWN_MASK));
        mncUnchanged.setSelected(true);
        mncUnchanged.setText("Unchanged Entries");
        mnuView.add(mncUnchanged);

        mncChanged.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.SHIFT_DOWN_MASK));
        mncChanged.setSelected(true);
        mncChanged.setText("Changed Entries");
        mnuView.add(mncChanged);

        mncDeleted.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.SHIFT_DOWN_MASK));
        mncDeleted.setSelected(true);
        mncDeleted.setText("Deleted Entries");
        mnuView.add(mncDeleted);

        mnuFile.add(mnuView);
        mnuFile.add(sprFile2);

        mniExit.setMnemonic('x');
        mniExit.setText("Exit");
        mnuFile.add(mniExit);

        menuBar.add(mnuFile);

        mnuHelp.setMnemonic('H');
        mnuHelp.setText("Help");

        mniAbout.setMnemonic('b');
        mniAbout.setText("About");
        mnuHelp.add(mniAbout);

        menuBar.add(mnuHelp);

        setJMenuBar(menuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel contentPanel;
    private rubensandreoli.drivescanner.gui.ListPanel listPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JCheckBoxMenuItem mncChanged;
    private javax.swing.JCheckBoxMenuItem mncDeleted;
    private javax.swing.JCheckBoxMenuItem mncTools;
    private javax.swing.JCheckBoxMenuItem mncUnchanged;
    private javax.swing.JMenuItem mniAbout;
    private javax.swing.JMenuItem mniDelete;
    private javax.swing.JMenuItem mniDeleteAll;
    private javax.swing.JMenuItem mniExit;
    private javax.swing.JMenuItem mniMerge;
    private javax.swing.JMenuItem mniRename;
    private javax.swing.JMenuItem mniScan;
    private javax.swing.JMenuItem mniStop;
    private javax.swing.JMenuItem mniUpdate;
    private javax.swing.JMenu mnuEdit;
    private javax.swing.JMenu mnuFile;
    private javax.swing.JMenu mnuHelp;
    private javax.swing.JMenu mnuView;
    private javax.swing.JPanel sidePanel;
    private javax.swing.JPopupMenu.Separator sprEdit;
    private javax.swing.JPopupMenu.Separator sprFile1;
    private javax.swing.JPopupMenu.Separator sprFile2;
    private javax.swing.JPopupMenu.Separator sprView;
    private rubensandreoli.drivescanner.gui.StatusPanel statusPanel;
    private rubensandreoli.drivescanner.gui.TablePanel tablePanel;
    private rubensandreoli.drivescanner.gui.ToolsPanel toolsPanel;
    // End of variables declaration//GEN-END:variables

}
