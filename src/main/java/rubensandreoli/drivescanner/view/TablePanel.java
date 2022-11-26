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

import rubensandreoli.drivescanner.controller.support.TablePanelInfo;
import rubensandreoli.drivescanner.view.support.PTableCellRenderer;
import rubensandreoli.drivescanner.view.support.PTableModel;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;

@SuppressWarnings("serial")
public class TablePanel extends JPanel {

    private JTable foldersTable;
    private PTableModel foldersTableModel;
    private PTableCellRenderer foldersTableCellRenderer;

    public TablePanel() {
        setLayout(new BorderLayout());
        //setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));

        foldersTableModel = new PTableModel();
        foldersTableCellRenderer = new PTableCellRenderer();

        foldersTable = new JTable(foldersTableModel);
        foldersTable.getTableHeader().setReorderingAllowed(false);
        foldersTable.getColumnModel().getColumn(1).setMaxWidth(65);
        foldersTable.getColumnModel().getColumn(0).setResizable(false);
        foldersTable.getColumnModel().getColumn(1).setResizable(false);

        foldersTable.setDefaultRenderer(Object.class, foldersTableCellRenderer);

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
                            //new PiErrorLogOut(e);
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

        JScrollPane scrollFoldersTable = new JScrollPane();
        scrollFoldersTable.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollFoldersTable.setViewportView(foldersTable);

        add(scrollFoldersTable, BorderLayout.CENTER);
    }

    public void clearFoldersTable() {
        foldersTableModel.clearData();
        foldersTableModel.fireTableDataChanged();
    }

    public void refreshFoldersTable(TablePanelInfo tablePanelInfo) {
        foldersTableModel.setData(tablePanelInfo.getScanFolders());
        foldersTableCellRenderer.setData(tablePanelInfo.getDeletedFolders(),
                tablePanelInfo.getUpdatedFoldersHigher(),
                tablePanelInfo.getUpdatedFoldersLower(),
                tablePanelInfo.getParentFolders());
        foldersTableModel.fireTableDataChanged();
    }

}
