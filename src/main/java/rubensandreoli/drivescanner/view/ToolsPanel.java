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

import rubensandreoli.drivescanner.controller.support.ToolsPanelInfo;
import rubensandreoli.drivescanner.view.events.EventComponent;
import rubensandreoli.drivescanner.view.events.EventsID;
import rubensandreoli.drivescanner.view.events.ViewEvent;
import rubensandreoli.drivescanner.view.events.ViewEventListener;
import rubensandreoli.drivescanner.view.support.PButton;
import rubensandreoli.drivescanner.view.support.PFileComboBox;
import rubensandreoli.drivescanner.view.support.PTextField;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ToolsPanel extends JPanel implements ActionListener, ItemListener {

    private PFileComboBox boxDrives;
    private PButton btnScan;
    private PButton btnRenameScan;
    private PButton btnDelScan;
    private PButton btnUpdateScan;
    private PTextField fieldScanSize;
    private PTextField fieldScanDate;
    private PTextField updatedFieldScanSize;
    private PTextField updatedFieldScanDate;

    private ViewEventListener viewListener;

    public ToolsPanel() {
        Dimension dim = getPreferredSize();
        dim.height = 90;
        setPreferredSize(dim);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        setLayout(null);

        boxDrives = new PFileComboBox(EventsID.SELECT_DRIVE, 0, 0, 40, "Selecte Drive to scan.");
        btnScan = new PButton(EventsID.SCAN, "Scan", 45, 0, 140, "Click to scan folders from selected Drive.");
        btnRenameScan = new PButton(EventsID.RENAME, "rename.png", 140, 30, 20, "Click to rename selected scan.");
        btnDelScan = new PButton(EventsID.DELETE, "remove.png", 165, 30, 20, "Click to remove selected scan.");
        btnUpdateScan = new PButton(EventsID.UPDATE, "updatefolders.png", 140, 60, 20, "Click to test for updated information about scan's folders.");
        fieldScanSize = new PTextField(0, 30, 62, "RIGHT", "Scan size.");
        fieldScanDate = new PTextField(67, 30, 68, "CENTER", "Scan date.");
        updatedFieldScanSize = new PTextField(0, 60, 62, "RIGHT", "Updated scan size.");
        updatedFieldScanDate = new PTextField(67, 60, 68, "CENTER", "Updated scan date.");

        btnScan.setMnemonic(KeyEvent.VK_S);

        btnScan.addActionListener(this);
        btnRenameScan.addActionListener(this);
        btnDelScan.addActionListener(this);
        btnUpdateScan.addActionListener(this);
        boxDrives.addItemListener(this);

        add(boxDrives);
        add(btnScan);
        add(fieldScanSize);
        add(fieldScanDate);
        add(updatedFieldScanSize);
        add(updatedFieldScanDate);
        add(btnRenameScan);
        add(btnDelScan);
        add(btnUpdateScan);
    }

    public void setViewListener(ViewEventListener viewListener) {
        this.viewListener = viewListener;
    }

    public void addDrives(File[] drives) {
        for (int i = 0; i < drives.length; i++) {
            boxDrives.addItem(drives[i]);
        }
    }

    public File getDriveSelected() {
        return (File) boxDrives.getSelectedItem();
    }

    public void toggleScansBtn(boolean isEnabled) {
        btnRenameScan.setEnabled(isEnabled);
        btnDelScan.setEnabled(isEnabled);
        btnUpdateScan.setEnabled(isEnabled);
    }

    public void clearFolderFields() {
        fieldScanSize.clearField();
        fieldScanDate.clearField();
        updatedFieldScanSize.clearField();
        updatedFieldScanDate.clearField();
    }

    public void refreshFoldersFields(ToolsPanelInfo toolsPanelInfo) {
        fieldScanSize.setText(toolsPanelInfo.getScanSize());
        fieldScanDate.setText(toolsPanelInfo.getScanDate());
        updatedFieldScanSize.setText(toolsPanelInfo.getUpdatedScanSize());
        updatedFieldScanDate.setText(toolsPanelInfo.getUpdatedScanDate());
    }

    @Override
    public void actionPerformed(ActionEvent actionE) {
        EventComponent eventComponent = (EventComponent) actionE.getSource();
        ViewEvent viewE = new ViewEvent(this, eventComponent.getEventID());
        if (viewListener != null) {
            viewListener.viewEventOccurred(viewE);
        }
    }

    @Override
    public void itemStateChanged(ItemEvent itemE) {
        if (itemE.getStateChange() == ItemEvent.SELECTED) {
            EventComponent eventComponent = (EventComponent) itemE.getSource();
            ViewEvent viewE = new ViewEvent(this, eventComponent.getEventID());
            if (viewListener != null) {
                viewListener.viewEventOccurred(viewE);
            }
        }
    }

}
