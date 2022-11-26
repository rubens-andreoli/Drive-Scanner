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

import rubensandreoli.drivescanner.view.events.EventComponent;
import rubensandreoli.drivescanner.view.events.EventsID;
import rubensandreoli.drivescanner.view.events.ViewEvent;
import rubensandreoli.drivescanner.view.events.ViewEventListener;
import rubensandreoli.drivescanner.view.support.PCheckBoxMenuItem;
import rubensandreoli.drivescanner.view.support.PMenuItem;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

@SuppressWarnings("serial")
public class MenuBarPanel extends JMenuBar implements ActionListener {

    private PMenuItem renameItem;
    private PMenuItem deleteItem;
    private PMenuItem updateItem;
    private PCheckBoxMenuItem folderFilterItem;
    private PCheckBoxMenuItem scanInfoItem;

    private ViewEventListener viewListener;

    public MenuBarPanel() {

        JMenu windowMenu = new JMenu("Window");
        JMenu showItem = new JMenu("Show");
        scanInfoItem = new PCheckBoxMenuItem(EventsID.SCAN_PANEL, "Scan Info", true, KeyEvent.VK_F3, 0);
        folderFilterItem = new PCheckBoxMenuItem(EventsID.TABLE_FILTER, "Table Filter", false, KeyEvent.VK_F4, 0);
        showItem.add(scanInfoItem);
        showItem.add(folderFilterItem);
        showItem.setMnemonic(KeyEvent.VK_H);
        PMenuItem exitItem = new PMenuItem(EventsID.EXIT, "Exit", KeyEvent.VK_X);
        windowMenu.add(showItem);
        windowMenu.addSeparator();
        windowMenu.add(exitItem);
        windowMenu.setMnemonic(KeyEvent.VK_W);

        JMenu editMenu = new JMenu("Edit");
        renameItem = new PMenuItem(EventsID.RENAME, "Rename", KeyEvent.VK_F2, 0);
        deleteItem = new PMenuItem(EventsID.DELETE, "Delete", KeyEvent.VK_DELETE, 0);
        updateItem = new PMenuItem(EventsID.UPDATE, "Update", KeyEvent.VK_F5, 0);
        editMenu.add(renameItem);
        editMenu.add(deleteItem);
        editMenu.addSeparator();
        editMenu.add(updateItem);
        editMenu.setMnemonic(KeyEvent.VK_E);

        JMenu helpMenu = new JMenu("Help");
        PMenuItem aboutItem = new PMenuItem(EventsID.ABOUT, "About", KeyEvent.VK_B);
        helpMenu.add(aboutItem);
        helpMenu.setMnemonic(KeyEvent.VK_H);

        scanInfoItem.addActionListener(this);
        folderFilterItem.addActionListener(this);
        renameItem.addActionListener(this);
        deleteItem.addActionListener(this);
        updateItem.addActionListener(this);
        exitItem.addActionListener(this);
        aboutItem.addActionListener(this);

        add(windowMenu);
        add(editMenu);
        add(helpMenu);
    }

    public void setViewListener(ViewEventListener viewListener) {
        this.viewListener = viewListener;
    }

    public void toggleScansItems(boolean isEnabled) {
        renameItem.setEnabled(isEnabled);
        deleteItem.setEnabled(isEnabled);
        updateItem.setEnabled(isEnabled);
    }

    public boolean isFolderFilter() {
        return folderFilterItem.isSelected();
    }

    public boolean isScanPanel() {
        return scanInfoItem.isSelected();
    }

    @Override
    public void actionPerformed(ActionEvent actionE) {
        EventComponent eventComponent = (EventComponent) actionE.getSource();
        ViewEvent viewE = new ViewEvent(this, eventComponent.getEventID());
        if (viewListener != null) {
            viewListener.viewEventOccurred(viewE);
        }

    }

}
