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
package rubensandreoli.drivescanner.controller.support;

import rubensandreoli.drivescanner.model.ScanInfo;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class TablePanelInfo {

    private Map<File, Long> scanFolders;

    private Set<File> parentFolders;
    private Set<File> updatedFoldersHigher;
    private Set<File> updatedFoldersLower;
    private Set<File> deletedFolders;

    public TablePanelInfo(boolean isFilter, ScanInfo scanInfoSelected) {

        if (isFilter) {
            Map<File, Long> filteredFolders = new LinkedHashMap<File, Long>();
            for (Map.Entry<File, Long> folderInfo : scanInfoSelected.getScanFolders().entrySet()) {
                if (scanInfoSelected.getDeletedFolders() != null
                        && scanInfoSelected.getDeletedFolders().contains(folderInfo.getKey())) {
                    filteredFolders.put(folderInfo.getKey(), folderInfo.getValue());
                }
                if (scanInfoSelected.getUpdatedFoldersHigher() != null
                        && scanInfoSelected.getUpdatedFoldersHigher().contains(folderInfo.getKey())) {
                    filteredFolders.put(folderInfo.getKey(), folderInfo.getValue());
                }
                if (scanInfoSelected.getUpdatedFoldersLower() != null
                        && scanInfoSelected.getUpdatedFoldersLower().contains(folderInfo.getKey())) {
                    filteredFolders.put(folderInfo.getKey(), folderInfo.getValue());
                }
            }
            this.scanFolders = filteredFolders;
        } else {
            this.scanFolders = scanInfoSelected.getScanFolders();
        }

        this.parentFolders = scanInfoSelected.getParentFolders();
        this.updatedFoldersHigher = scanInfoSelected.getUpdatedFoldersHigher();
        this.updatedFoldersLower = scanInfoSelected.getUpdatedFoldersLower();
        this.deletedFolders = scanInfoSelected.getDeletedFolders();
    }

    public Map<File, Long> getScanFolders() {
        return scanFolders;
    }

    public Set<File> getParentFolders() {
        return parentFolders;
    }

    public Set<File> getUpdatedFoldersHigher() {
        return updatedFoldersHigher;
    }

    public Set<File> getUpdatedFoldersLower() {
        return updatedFoldersLower;
    }

    public Set<File> getDeletedFolders() {
        return deletedFolders;
    }

}
