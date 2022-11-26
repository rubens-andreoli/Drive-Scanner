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
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import rubensandreoli.drivescanner.gui.support.ActionEvent;
import rubensandreoli.drivescanner.gui.support.ActionEventListener;
import rubensandreoli.drivescanner.gui.support.IconLoader;
import rubensandreoli.drivescanner.io.History;
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

    private final History.Data data;
    private Scan currentScan;
    private SwingWorker <Boolean, String> worker;
    private boolean locked = false;

    @SuppressWarnings("LeakingThisInConstructor")
    public MainFrame() {
        initComponents();
        setLocationRelativeTo(null);
        
        data = History.getInstance().getData();

        //SET LISTENERS
        mniScan.addActionListener(e -> eventOccurred(ActionEvent.SCAN));
        mniStop.addActionListener(e -> eventOccurred(ActionEvent.STOP));
        mniAbout.addActionListener(e -> eventOccurred(ActionEvent.ABOUT));
        mniExit.addActionListener(e -> eventOccurred(ActionEvent.EXIT));
        mniRename.addActionListener(e -> eventOccurred(ActionEvent.RENAME));
        mniDelete.addActionListener(e -> eventOccurred(ActionEvent.DELETE));
        mniUpdate.addActionListener(e -> eventOccurred(ActionEvent.UPDATE));
        mncTools.addActionListener(e -> eventOccurred(ActionEvent.SIDE_PANEL));
        mncUnchanged.addActionListener(e -> eventOccurred(ActionEvent.TABLE_FILTER));
        mncDeleted.addActionListener(e -> eventOccurred(ActionEvent.TABLE_FILTER));
        mncChanged.addActionListener(e -> eventOccurred(ActionEvent.TABLE_FILTER));
        toolsPanel.setListener(this);
        listPanel.setListener(this);
        
        toolsPanel.addDrives(Scanner.getRoots());
    }
    
    private String createName(String msg, String title, String msgError){
        String name = JOptionPane.showInputDialog(MainFrame.this, msg, title, JOptionPane.QUESTION_MESSAGE);
        if (name == null || name.equals("")) return null;
        final File drive = toolsPanel.getSelectedDrive();
        while(History.getInstance().existsScan(drive, name)){
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
    }
    
    private void scanDeselected(){
        currentScan = null;
        setEditEnabled(false);
        toolsPanel.clear();
        tablePanel.clear();
        statusPanel.clear();
    }
    
    private void scanSelected(){
        currentScan = listPanel.getSelectedScan();
        setEditEnabled(!locked);
        toolsPanel.setScan(currentScan);
        tablePanel.setScan(currentScan);
        statusPanel.setTotal(currentScan.getLenght());
    }
    
    private void setLocked(boolean locked){
        this.locked = locked;
        toolsPanel.setScanEnabled(locked);
        mniScan.setEnabled(!locked);
        mniStop.setEnabled(locked);
        setEditEnabled(!locked);
    }
    
    @Override
    public void eventOccurred(ActionEvent e) {
        switch (e) {
            case SELECT_DRIVE:
                scanDeselected();
                listPanel.setScans(data.getDriveScans(toolsPanel.getSelectedDrive()));
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
                        "Rename Scan", 
                        "Choose another scan name, this one has already been used:"
                );
                if(scanNewName == null) break;

                final Scan renamedScan = History.getInstance().renameScan(currentScan, scanNewName);
                if(renamedScan != null){
                    listPanel.replaceScan(currentScan, renamedScan);
                    currentScan = renamedScan;
                }else{
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "Scan file not found or could not be renamed. "
                            + "Reopen the program to clear it from the Scans List. "
                            + "Or verify folder access permissions.",
                            "Error: File Not Found",
                            JOptionPane.ERROR_MESSAGE);
                }
                break;
            case DELETE:
                int confirm = JOptionPane.showConfirmDialog(MainFrame.this,
                        "Are you sure you want to delete this scan?",
                        "Delete Scan",
                        JOptionPane.OK_CANCEL_OPTION);

                if (confirm == JOptionPane.OK_OPTION) {
                    if (History.getInstance().deleteScan(currentScan)) {
                        listPanel.removeScan(currentScan);
                        scanDeselected();
                    } else {
                        JOptionPane.showMessageDialog(MainFrame.this,
                                "Scan file not found. "
                                + "Reopen the program to clear it from the Scans List.",
                                "Error: File Not Found",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
                break;
            case UPDATE:
                update();
                break;
            case STOP:
                if(worker != null){ //shouldn't happen, just a precaution
                    worker.cancel(false);
                }
                break;
            case EXIT:
                System.exit(0);
                break;
            case SIDE_PANEL:
                toolsPanel.setVisible(mncTools.isSelected());
                break;
            case TABLE_FILTER:
                tablePanel.setFilter(mncUnchanged.isSelected(), mncChanged.isSelected(), mncDeleted.isSelected());
                break;
            case ABOUT:
                new AboutDialog(this, new AboutDialog.ProgramInfo("Drive Scanner", null, "1.0.0", "2022"))
                        .addAtribution("Icons", "Freepik", "https://www.flaticon.com/authors/freepik")
                        .setVisible(true);
                break;
        }
    }
 
    private void scan(){
        final String scanName = createName(
                "Enter a name for your scan:", 
                "Name Scan", 
                "Choose another scan name, this one has already been used:"
        );
        if(scanName == null) return;
        worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                final File drive = toolsPanel.getSelectedDrive();
                
                final Scan newScan = new Scanner(new Scanner.Handler(){
                    @Override
                    public void setStatus(String status) {
                        publish(status);
                    }

                    @Override
                    public boolean isInterrupted() {
                        return isCancelled();
                    }
                }).scan(scanName, drive, data.getDriveFolders(drive));
                if(newScan == null) return null;
                if(!newScan.isEmpty()){
                    currentScan = newScan;
                    History.getInstance().saveScan(currentScan, false);
                    return true;
                }
                return false;
            }
            
            @Override
            protected void process(List<String> msgs) {
                if(isDone()) return; //queued values may be set after worker is done
                for (String msg : msgs) {
                    statusPanel.setStatus(msg);
                }
            }

            @Override
            protected void done() {
                statusPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                statusPanel.clearStatus();
                setLocked(false);
                
                Boolean result;
                try { 
                    result = get();
                } catch (Exception ex) {
                    result = null;
                }
                if(result == null) return; //exeption or cancelled
                if(result){
                    listPanel.addScan(currentScan);
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
        worker.execute();
    }
    
    private void update(){
        worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
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
                History.getInstance().saveScan(currentScan, true);
                return null;
            }

            @Override
            protected void process(List<String> msgs) {
                if(isDone()) return;
                for (String msg : msgs) {
                    statusPanel.setStatus(msg);
                }
            }

            @Override
            protected void done() {
                statusPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                statusPanel.clearStatus();
                setLocked(false);
                if(!isCancelled()) scanSelected();
            }
        };
        setLocked(true);
        statusPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        worker.execute();
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
        mniDelete = new javax.swing.JMenuItem();
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

        mniDelete.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0));
        mniDelete.setText("Delete");
        mnuEdit.add(mniDelete);
        mnuEdit.add(sprEdit);

        mniUpdate.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0));
        mniUpdate.setText("Update");
        mnuEdit.add(mniUpdate);

        mnuFile.add(mnuEdit);

        mnuView.setMnemonic('V');
        mnuView.setText("View");

        mncTools.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F3, 0));
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
    private javax.swing.JMenuItem mniExit;
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
