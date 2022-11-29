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
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import rubensandreoli.drivescanner.gui.support.ActionEvent;
import rubensandreoli.drivescanner.gui.support.ActionEventListener;
import rubensandreoli.drivescanner.gui.support.IconLoader;
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
public class MainFrame extends javax.swing.JFrame implements ActionEventListener {

    private Repository.Data data;
    private Scan currentScan; //TODO: is this needed here? listpanel keeps current scan reference.
    private SwingWorker currentWorker;
    private boolean locked = false;
    private final AboutDialog aboutDialog;

    @SuppressWarnings("LeakingThisInConstructor")
    public MainFrame() {
        initComponents();
        setLocationRelativeTo(null);
        aboutDialog = new AboutDialog(this, new AboutDialog.ProgramInfo("Drive Scanner", null, "1.0.0", "2022"));
        aboutDialog.addAtribution("Icons", "Freepik", "https://www.flaticon.com/authors/freepik");
        
        //LOAD REPOSITORY
        Repository repository = Repository.getInstance();
        SwingWorker <Repository.Data, String> loadWorker = new SwingWorker<>() {
            private final List<Repository.Error> errors = new ArrayList<>(); //changed from another thread.
            
            @Override
            protected Repository.Data doInBackground() throws Exception {
                return repository.load(new Repository.LoadListener(){
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
                MainFrame.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                data = Repository.getInstance().getData();
                setLocked(false);
                eventOccurred(ActionEvent.SELECT_DRIVE);
                for (Repository.Error error : errors) { //TODO: better way to display errors combined.
                    showLoadException(error.cause, error.message);  //TODO: option to delete when failed to load.
                }
            }
        };
        setLocked(true);
        setStopEnabled(false);
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        loadWorker.execute();
        
        //SET LISTENERS
        mniScan.addActionListener(e -> eventOccurred(ActionEvent.SCAN));
        mniStop.addActionListener(e -> eventOccurred(ActionEvent.STOP));
        mniAbout.addActionListener(e -> eventOccurred(ActionEvent.ABOUT));
        mniExit.addActionListener(e -> eventOccurred(ActionEvent.EXIT));
        mniRename.addActionListener(e -> eventOccurred(ActionEvent.RENAME));
        mniMerge.addActionListener(e -> eventOccurred(ActionEvent.MERGE));
        mniDelete.addActionListener(e -> eventOccurred(ActionEvent.DELETE));
        mniDeleteAll.addActionListener(e -> eventOccurred(ActionEvent.DELETE_ALL));
        mniUpdate.addActionListener(e -> eventOccurred(ActionEvent.UPDATE));
        mncTools.addActionListener(e -> eventOccurred(ActionEvent.SIDE_PANEL));
        mncUnchanged.addActionListener(e -> eventOccurred(ActionEvent.TABLE_FILTER));
        mncDeleted.addActionListener(e -> eventOccurred(ActionEvent.TABLE_FILTER));
        mncChanged.addActionListener(e -> eventOccurred(ActionEvent.TABLE_FILTER));
        toolsPanel.setListener(this);
        listPanel.setListener(this);
        
        toolsPanel.addDrives(Scanner.getRoots());
    }
    
    private void setStopEnabled(boolean enabled){
        toolsPanel.setStopEnabled(enabled);
        mniStop.setEnabled(enabled);
    }
    
    private String createName(String msg, String title, String msgError){
        String name = JOptionPane.showInputDialog(MainFrame.this, msg, title, JOptionPane.QUESTION_MESSAGE);
        if (name == null || name.equals("")) return null;
        File drive = toolsPanel.getSelectedDrive();
        while(Repository.getInstance().existsScan(drive, name)){
            name = JOptionPane.showInputDialog(MainFrame.this, msgError, title, JOptionPane.WARNING_MESSAGE);
            if (name == null || name.equals("")) return null;
        }
        return name;
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
    
    private void scanSelected(){
        currentScan = listPanel.getSelectedScan();
        setEditEnabled(!locked); //edit scan only if not locked.
        toolsPanel.setScan(currentScan);
        tablePanel.setScan(currentScan);
        statusPanel.setTotals(currentScan.getTotalFolders(), currentScan.getTotalFiles());
    }
    
    private void setLocked(boolean locked){
        this.locked = locked;
        toolsPanel.setScanEnabled(locked);
        mniScan.setEnabled(!locked);
        mniStop.setEnabled(locked);
        setEditEnabled(!locked/* && listPanel.getSelectedScan() != null*/); //only if selected something.
        mniDeleteAll.setEnabled(locked? locked : isSelectedDriveNotEmpty()); //if not locked, check if there is any scans to delete.
    }
        
    private boolean isSelectedDriveNotEmpty(){
        return !data.isEmpty(toolsPanel.getSelectedDrive());
    }
    
    @Override
    public void eventOccurred(ActionEvent evt) {
        switch (evt) {
            case SELECT_DRIVE: 
                if(data != null){ //data is null if still being loaded.
                    driveSelected();
                    listPanel.setScans(data.getDriveScans(toolsPanel.getSelectedDrive()));
                }
                break;
            case SELECT_SCAN:
                scanSelected();
                break;
            case SCAN:
                scan();
                break;
            case RENAME:
                final String scanNewName = createName(
                        "Enter a new name for your scan:", 
                        "Rename", 
                        "Choose another scan name, this one has already been used:"
                );
                if(scanNewName == null) break;
                //TODO: rename scan in new thread.
                Repository.getInstance().renameScan(currentScan, scanNewName, new Repository.SaveListener() {
                    @Override
                    public void onSaved(Scan scan) {
                        listPanel.replaceScan(currentScan, scan);
                        currentScan = scan;
                    }

                    @Override
                    public void onSaveError(Repository.Error e) {
                        showSaveException(e.cause, e.message);
                    }
                });
                break;
            case DELETE:
                if (confirmationAlert("Delete", "Are you sure you want to delete the selected scan?")) {
                    if (Repository.getInstance().deleteScan(currentScan)) {
                        listPanel.removeScan(currentScan);
                        driveSelected();
                    } else {
                        JOptionPane.showMessageDialog(MainFrame.this,
                                "Scan file not found. "
                                + "Reopen the program to clear it from the list.",
                                "Error: File Not Found",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
                break;
            case DELETE_ALL:
                if(confirmationAlert("Delete All", "Are you sure you want to delete all scans from the selected drive?")){
                    Collection<Scan> failed = Repository.getInstance().deleteScans(toolsPanel.getSelectedDrive());
                    if(failed.isEmpty()){
                        listPanel.clear();
                    }else{
                        listPanel.removeScans(failed);
                        JOptionPane.showMessageDialog(this, //TODO: display list in a jlist.
                            "Scan file(s) "+Arrays.toString(failed.toArray())+" not found."
                            + "\nReopen the program to clear it from the list.",
                            "Error: File Not Found",
                            JOptionPane.ERROR_MESSAGE);
                    }
                    driveSelected();
                }
                break;
            case UPDATE:
                update();
                break;
            case MERGE:
                if(!listPanel.isMultipleSelected()){
                    JOptionPane.showMessageDialog(this, "Multiple scans must be selected to perform a merge operation.", "Merge", JOptionPane.WARNING_MESSAGE);
                }else{
                    if(confirmationAlert("Merge", "Are you sure you want to merge all the selected scans?"
                            + "\nAll scans will be merged into the first one selected: [" + currentScan +"]")){
                        Collection<Scan> selectedScans = listPanel.getSelectedScans();
                        Repository.getInstance().mergeScans(selectedScans, currentScan, new Repository.MergeListener() {
                            @Override
                            public void onMerged(Scan scan) {
                                listPanel.removeScans(selectedScans);
                                listPanel.replaceScan(currentScan, scan);
                                listPanel.setSelectedScan(scan);
                                currentScan = scan;
                            }
                            
                            @Override
                            public void onMergeError(Repository.Error e) {
                                showSaveException(e.cause, e.message);
                            }
                        });
                    }
                }
                break;
            case STOP:
                if(currentWorker != null) currentWorker.cancel(false);
                break;
            case EXIT:
                System.exit(0);
                break;
            case SIDE_PANEL:
                sidePanel.setVisible(mncTools.isSelected());
                break;
            case TABLE_FILTER:
                tablePanel.setFilter(mncUnchanged.isSelected(), mncChanged.isSelected(), mncDeleted.isSelected());
                break;
            case ABOUT:
                aboutDialog.setVisible(true);
                break;
        }
    }
 
    private void scan(){
        String scanName = createName(
                "Enter a name for your scan:", 
                "Name Scan", 
                "Choose another scan name, this one has already been used:"
        );
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
                    Repository.getInstance().addScan(newScan, new Repository.SaveListener(){
                        @Override
                        public void onSaved(Scan scan) {
                            currentScan = scan;
                            listPanel.addScan(currentScan);
                        }

                        @Override
                        public void onSaveError(Repository.Error e) {
                            showSaveException(e.cause, e.message);
                        }
                    });
                }else{
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "No new folders were found, this scan will not be saved.",
                            "Nothing New",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        };
        setLocked(true);
        statusPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        currentWorker = scanWorker;
        scanWorker.execute();
    }
    
    private void update(){
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
                    Repository.getInstance().updateScan(currentScan, e -> showSaveException(e.cause, e.message));
                    scanSelected();
                }
            }
        };
        setLocked(true);
        statusPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        currentWorker = updateWorker;
        updateWorker.execute();
    }
    
    private boolean confirmationAlert(String title, String msg){
        return (JOptionPane.showConfirmDialog(this, msg, title, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION);
    }
        
    private void showSaveException(Exception e, String scanName){
        try{
            throw e;
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(MainFrame.this, "History folder not found. Reopening the program should fix it. "
                    + "If problem persists, try creating manually a folder named 'history' in the same folder as the program.", 
                    "Error: File Not Found", 
                    JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(MainFrame.this, "Scan '" + scanName + "' information could not be saved. "
                    + "Verify folder access permissions.", "Error: File Access Denied", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(MainFrame.this, "What now?! " + ex.getMessage(), "Error: Unexpected", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showLoadException(Exception e, String fileName){
        try{
            throw e;
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(MainFrame.this, 
                    "Scan file '" + fileName + "' not found. It may have been deleted while the program was loading.", 
                    "Error: File Not Found", 
                    JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(MainFrame.this, 
                    "Scan file '" + fileName + "' could not be opened. Verify 'History' folder access permissions.", 
                    "Error: File Access Denied", 
                    JOptionPane.ERROR_MESSAGE);
        } catch (ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(MainFrame.this, 
                    "Scan file '" + fileName + "' is an outdated scan file. Manually deleting the 'history' folder or the scan should fix the problem.", 
                    "Error: Invalid Data", 
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(MainFrame.this, "What now?! " + ex.getMessage(), "Error: Unexpected", JOptionPane.ERROR_MESSAGE);
        }
    }

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

        mniDelete.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0));
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

        mncUnchanged.setSelected(true);
        mncUnchanged.setText("Unchanged Entries");
        mnuView.add(mncUnchanged);

        mncChanged.setSelected(true);
        mncChanged.setText("Changed Entries");
        mnuView.add(mncChanged);

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
