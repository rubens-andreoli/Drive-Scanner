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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.io.File;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

@SuppressWarnings("serial")
public class PTableCellRenderer extends DefaultTableCellRenderer {

    private Set<File> deletedFolders;
    private Set<File> updatedFoldersHigher;
    private Set<File> updatedFoldersLower;
    private Set<File> parentFolders;

    public void setData(Set<File> deletedFolders, Set<File> updatedFoldersHigher, Set<File> updatedFoldersLower, Set<File> parentFolders) {
        this.deletedFolders = deletedFolders;
        this.updatedFoldersHigher = updatedFoldersHigher;
        this.updatedFoldersLower = updatedFoldersLower;
        this.parentFolders = parentFolders;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table,
            Object value, boolean isSelected, boolean hasFocus, int row, int col) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
        if (deletedFolders != null
                && deletedFolders.contains(table.getModel().getValueAt(row, 0))) {
            setBackground(Color.RED);
        } else if (updatedFoldersHigher != null
                && updatedFoldersHigher.contains(table.getModel().getValueAt(row, 0))) {
            setBackground(Color.GREEN);
        } else if (updatedFoldersLower != null
                && updatedFoldersLower.contains(table.getModel().getValueAt(row, 0))) {
            setBackground(Color.YELLOW);
        } else {
            setBackground(table.getBackground());
        }
        if (parentFolders != null
                && parentFolders.contains(table.getModel().getValueAt(row, 0))) {
            setFont(getFont().deriveFont(Font.BOLD));
        }
        return this;
    }

}
