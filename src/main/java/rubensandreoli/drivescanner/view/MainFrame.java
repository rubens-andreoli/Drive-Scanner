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
package rubensandreoli.drivescanner.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import rubensandreoli.drivescanner.model.ErrorLogger;
import rubensandreoli.drivescanner.model.ScanInfo;
import rubensandreoli.drivescanner.model.ScanInfoOut;
import rubensandreoli.drivescanner.model.ScansInfoIn;

public class MainFrame extends JFrame {

    private static final long serialVersionUID = -69936227325989005L;

    private JComboBox<File> boxDrives;
    private JButton btnScan;
    private JScrollPane scrollScanList;
    private JList<String> scanList;
    private JScrollPane scrollFoldersTable;
    private JTable foldersTable;
    private JTextField fieldScanSize;
    private JTextField fieldScanDate;
    private JTextField updatedFieldScanSize;
    private JTextField updatedFieldScanDate;
    private JButton btnRenameScan;
    private JButton btnDelScan;
    private JButton btnTestScanFolders;

    private DefaultListModel<String> scanListModel;
    private DefaultTableModel foldersTableModel;
    private ScansInfoIn piScansInfoIn;
    private ScanInfo piScanInfoSelected;

    public MainFrame() {
///////////////////////////////////// JFRAME PROPERTIES ///////////////////////////////////////
        JPanel contentPane;
        setResizable(false);
        setTitle("Drive Scanner");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 800, 450);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);
        ImageIcon img = new ImageIcon(System.getProperty("user.dir") + "\\imagens\\icon.png");
        //TODO: change icon.
        this.setIconImage(img.getImage());

        boxDrives = new JComboBox<File>();
        boxDrives.setBackground(Color.WHITE);
        boxDrives.setBounds(10, 11, 40, 20);
        boxDrives.setToolTipText("<html>Selecte Drive to scan.</html>");
        contentPane.add(boxDrives);

        btnScan = new JButton("Scan");
        btnScan.setBounds(60, 11, 135, 20);
        btnScan.setToolTipText("<html>Click to scan folders from selected Drive.</html>");
        btnScan.setEnabled(false);
        contentPane.add(btnScan);

        scanListModel = new DefaultListModel<String>();
        scrollScanList = new JScrollPane();
        scanList = new JList<String>(scanListModel);
        scrollScanList.setBounds(10, 100, 185, 311);
        scrollScanList.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        contentPane.add(scrollScanList);
        scrollScanList.setViewportView(scanList);

        foldersTableModel = new DefaultTableModel() {
            private static final long serialVersionUID = 2257292295322930261L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        foldersTable = new JTable(foldersTableModel);
        foldersTableModel.addColumn("Folder");
        foldersTableModel.addColumn("Size");
        foldersTable.getColumnModel().getColumn(0).setResizable(false);
        foldersTable.getColumnModel().getColumn(0).setPreferredWidth(517);
        foldersTable.getColumnModel().getColumn(1).setResizable(false);
        foldersTable.getColumnModel().getColumn(1).setPreferredWidth(62);
        scrollFoldersTable = new JScrollPane();
        scrollFoldersTable.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollFoldersTable.setBounds(205, 11, 579, 400);
        contentPane.add(scrollFoldersTable);
        scrollFoldersTable.setViewportView(foldersTable);
        foldersTable.getTableHeader().setReorderingAllowed(false);

        fieldScanSize = new JTextField();
        fieldScanSize.setEditable(false);
        fieldScanSize.setBounds(10, 42, 62, 20);
        contentPane.add(fieldScanSize);
        fieldScanSize.setColumns(10);
        fieldScanSize.setBackground(Color.WHITE);
        fieldScanSize.setToolTipText("<html>Scan size.</html>");
        fieldScanSize.setHorizontalAlignment(JTextField.RIGHT);

        updatedFieldScanSize = new JTextField();
        updatedFieldScanSize.setEditable(false);
        updatedFieldScanSize.setBounds(10, 70, 62, 20);
        contentPane.add(updatedFieldScanSize);
        updatedFieldScanSize.setColumns(10);
        updatedFieldScanSize.setBackground(Color.WHITE);
        updatedFieldScanSize.setToolTipText("<html>Updated scan size.</html>");
        updatedFieldScanSize.setHorizontalAlignment(JTextField.RIGHT);

        btnDelScan = new JButton(new ImageIcon(System.getProperty("user.dir") + "\\imagens\\remove.png"));
        //TODO: change icon.
        btnDelScan.setSize(20, 20);
        btnDelScan.setLocation(175, 42);
        btnDelScan.setBackground(Color.WHITE);
        btnDelScan.setToolTipText("<html>Click to remove selected scan.</html>");
        contentPane.add(btnDelScan);
        btnDelScan.setEnabled(false);

        btnRenameScan = new JButton(new ImageIcon(System.getProperty("user.dir") + "\\imagens\\rename.png"));
        //TODO: change icon.
        btnRenameScan.setSize(20, 20);
        btnRenameScan.setLocation(150, 42);
        btnRenameScan.setBackground(Color.WHITE);
        btnRenameScan.setToolTipText("<html>Click to rename selected scan.</html>");
        contentPane.add(btnRenameScan);
        btnRenameScan.setEnabled(false);

        btnTestScanFolders = new JButton(new ImageIcon(System.getProperty("user.dir") + "\\imagens\\updatefolders.png"));
        //TODO: change icon.
        btnTestScanFolders.setSize(20, 20);
        btnTestScanFolders.setLocation(150, 70);
        btnTestScanFolders.setBackground(Color.WHITE);
        btnTestScanFolders.setToolTipText("<html>Click to test for updated information about scan's folders.</html>");
        contentPane.add(btnTestScanFolders);
        btnTestScanFolders.setEnabled(false);

        fieldScanDate = new JTextField();
        fieldScanDate.setEditable(false);
        fieldScanDate.setBounds(77, 42, 68, 20);
        contentPane.add(fieldScanDate);
        fieldScanDate.setColumns(10);
        fieldScanDate.setToolTipText("<html>Scan date.</html>");
        fieldScanDate.setBackground(Color.WHITE);

        updatedFieldScanDate = new JTextField();
        updatedFieldScanDate.setEditable(false);
        updatedFieldScanDate.setBounds(77, 70, 68, 20);
        contentPane.add(updatedFieldScanDate);
        updatedFieldScanDate.setColumns(10);
        updatedFieldScanDate.setToolTipText("<html>Updated scan date.</html>");
        updatedFieldScanDate.setBackground(Color.WHITE);

///////////////////////////////////// JFRAME ACTIONS ///////////////////////////////////////		
        //Read scans history:
        piScansInfoIn = new ScansInfoIn();

        boxDrives.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemE) {
                if (itemE.getStateChange() == ItemEvent.SELECTED) {
                    populateScanList();
                    btnScan.setEnabled(true);
                    clearScanInfo();
                }
            }
        });

        //List drives available:
        File[] drives = File.listRoots();
        for (int i = 0; i < drives.length; i++) {
            boxDrives.addItem(drives[i]);
        }

        btnScan.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                //Start scan, save result if changed, rebuild history with new info, populate scan list and select new scan:
                String scanName = JOptionPane.showInputDialog("Enter a name for your scan:");
                scanName = testScanName(scanName);
                if (scanName != null) {

                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    btnScan.setEnabled(false);
                    FoldersScan PiFoldersRead = new FoldersScan(
                            (File) boxDrives.getSelectedItem(), piScansInfoIn.getScansInfoList());
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    btnScan.setEnabled(true);

                    if (PiFoldersRead.getNewFoldersMap().isEmpty() || PiFoldersRead.getNewFoldersMap() == null) {
                        JOptionPane.showMessageDialog(null, "No new folders were found, this scan will not be saved.");
                    } else {
                        new ScanInfoOut((File) boxDrives.getSelectedItem(), scanName, PiFoldersRead.getNewFoldersMap());
                        clearScanInfo();
                        piScansInfoIn = new ScansInfoIn();
                        populateScanList();
                        scanList.setSelectedValue(scanName, true);
                    }
                }
            }
        });

        scanList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                //if (arg0.getValueIsAdjusting()){
                if (scanList.getSelectedValue() != null) {
                    populateScanInfo();
                    btnRenameScan.setEnabled(true);
                    btnDelScan.setEnabled(true);
                    btnTestScanFolders.setEnabled(true);
                }
            }
        });

        btnDelScan.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                ScansManager piScansManager = new ScansManager();
                if (piScansManager.deleteScan((File) boxDrives.getSelectedItem(), scanList.getSelectedValue())) {
                    piScansInfoIn = new ScansInfoIn();
                    populateScanList();
                    clearScanInfo();
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Scan file not found. "
                            + "Reopen the program to clear it from the Scans List.",
                            "Error: File Not Found",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnRenameScan.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                String scanNewName = JOptionPane.showInputDialog("Enter a new name for your scan:");
                scanNewName = testScanName(scanNewName);
                if (scanNewName != null) {
                    ScansManager piScansManager = new ScansManager();
                    if (piScansManager.renameScan((File) boxDrives.getSelectedItem(), scanList.getSelectedValue(), scanNewName, piScansInfoIn)) {
                        piScansInfoIn = new ScansInfoIn();
                        populateScanList();
                        scanList.setSelectedValue(scanNewName, isFocused());
                    } else {
                        JOptionPane.showMessageDialog(null,
                                "Old scan file not found or could not be deleted. "
                                + "Reopen the program to clear it from the Scans List. "
                                + "If problem persists, try deleting it manually.",
                                "Error: File Not Found",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        btnTestScanFolders.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                ScansManager piScansManager = new ScansManager();
                piScansManager.testScanFolders(piScanInfoSelected);
                populateScanInfo();
                btnRenameScan.setEnabled(true);
                btnDelScan.setEnabled(true);
                btnTestScanFolders.setEnabled(true);
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });

        foldersTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = -196498225664671156L;

            @Override
            public Component getTableCellRendererComponent(JTable table,
                    Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (piScanInfoSelected.getDeletedFolders() != null
                        && piScanInfoSelected.getDeletedFolders().contains(table.getModel().getValueAt(row, 0))) {
                    setBackground(Color.RED);
                } else if (piScanInfoSelected.getUpdatedFoldersHigher() != null
                        && piScanInfoSelected.getUpdatedFoldersHigher().contains(table.getModel().getValueAt(row, 0))) {
                    setBackground(Color.GREEN);
                } else if (piScanInfoSelected.getUpdatedFoldersLower() != null
                        && piScanInfoSelected.getUpdatedFoldersLower().contains(table.getModel().getValueAt(row, 0))) {
                    setBackground(Color.YELLOW);
                } else {
                    setBackground(table.getBackground());
                }
                if (piScanInfoSelected.getFoldersParents() != null
                        && piScanInfoSelected.getFoldersParents().contains(table.getModel().getValueAt(row, 0))) {
                    setFont(getFont().deriveFont(Font.BOLD));
                }
                return this;
            }
        });

        foldersTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                JTable table = (JTable) me.getSource();
                Point p = me.getPoint();
                int row = table.columnAtPoint(p);
                if (me.getClickCount() == 2 && row == 0) {
                    File folderPath = (File) table.getValueAt(table.getSelectedRow(), 0);
                    if (Desktop.isDesktopSupported() && folderPath.canRead()) {
                        try {
                            Desktop.getDesktop().open(folderPath);
                        } catch (IOException e) {
                            new ErrorLogger(e);
                            JOptionPane.showMessageDialog(null,
                                    "Folder could not be opened. "
                                    + "Verify folder access permissions.",
                                    "Error: File Access Denied",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null,
                                "Folder not found. "
                                + "Perform an update to highlight in red the deleted folders.",
                                "Error: File Not Found",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

    }

    public void populateScanList() {
        scanListModel.clear();
        for (String[] scanInfo : piScansInfoIn.getScansFilesMap().values()) {
            if (scanInfo[0].equals(boxDrives.getSelectedItem().toString())) {
                scanListModel.addElement(scanInfo[1]);
            }
        }
    }

    public void clearScanInfo() {
        piScanInfoSelected = null;
        while (foldersTableModel.getRowCount() > 0) {
            foldersTableModel.removeRow(0);
        }
        fieldScanDate.setText(null);
        updatedFieldScanDate.setText(null);
        fieldScanSize.setText(null);
        updatedFieldScanSize.setText(null);
        btnRenameScan.setEnabled(false);
        btnDelScan.setEnabled(false);
        btnTestScanFolders.setEnabled(false);
    }

    public void populateScanInfo() {
        clearScanInfo();
        //Get select scan info:
        piScanInfoSelected = piScansInfoIn.getSelectedScan((File) boxDrives.getSelectedItem(), scanList.getSelectedValue());
        //Display each folder in rows:
        for (Map.Entry<File, Long> folderInfo : piScanInfoSelected.getScanFoldersInfo().entrySet()) {
            foldersTableModel.addRow(new Object[]{folderInfo.getKey(), this.formatSize(folderInfo.getValue())});
        }
        //Display scan date:
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        fieldScanDate.setText(dateFormat.format(piScanInfoSelected.getScanDate()));
        if (piScanInfoSelected.getUpdatedScanDate() != null) {
            updatedFieldScanDate.setText(dateFormat.format(piScanInfoSelected.getUpdatedScanDate()));
        }
        //Display scan size:
        fieldScanSize.setText(this.formatSize(piScanInfoSelected.getScanSize()));
        if (piScanInfoSelected.getUpdatedScanSize() != 0) {
            updatedFieldScanSize.setText(this.formatSize(piScanInfoSelected.getUpdatedScanSize()));
        }
    }

    public String formatSize(long size) {
        String formatedSize = "0";
        if (size != 0) {
            final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
            int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
            formatedSize = new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups))
                    + " " + units[digitGroups];
        }
        return formatedSize;
    }

    public String testScanName(String scanName) {
        if (scanName != null && scanName != "") {
            File scanFile = new ScansManager().formatScanFilename((File) boxDrives.getSelectedItem(), scanName);
            if (scanFile.exists()) {
                scanName = JOptionPane.showInputDialog("Choose another scan name, this one has already been used:");
                return this.testScanName(scanName);
            } else {
                return scanName;
            }
        }
        return null;
    }

}
