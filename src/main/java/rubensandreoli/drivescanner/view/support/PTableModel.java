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
package rubensandreoli.drivescanner.view.support;

import rubensandreoli.drivescanner.controller.support.SizeFormat;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class PTableModel extends AbstractTableModel {

    private Map<File, Long> scanFolders;
    private String[] colNames = {"Folder", "Size"};

    public PTableModel() {
        this.clearData();
    }

    public void setData(Map<File, Long> scanFolders) {
        this.scanFolders = scanFolders;
    }

    public void clearData() {
        scanFolders = new TreeMap<File, Long>();
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
            return new SizeFormat().longToString(scanFolders.get(scanFolders.keySet().toArray()[row]));
        }
    }

}
