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
package rubensandreoli.drivescanner.controller.events;

import rubensandreoli.drivescanner.model.ScanInfo;
import rubensandreoli.drivescanner.model.WriteDb;

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Update {

    public Update(ScanInfo scanInfo) {
        Set<File> deletedFolders = new HashSet<File>();
        Set<File> updatedFoldersHigher = new HashSet<File>();
        Set<File> updatedFoldersLower = new HashSet<File>();

        long updatedScanSize = 0;
        for (File folder : scanInfo.getScanFolders().keySet()) {
            if (!folder.canRead()) {
                deletedFolders.add(folder);
            } else {
                File[] folderFiles = folder.listFiles();
                long updatedFolderSize = 0;
                if (folderFiles != null) {
                    for (File folderFileToSize : folderFiles) {
                        if (folderFileToSize.isFile()) {
                            updatedFolderSize += folderFileToSize.length();
                        }
                    }
                    updatedScanSize += updatedFolderSize;
                    if (updatedFolderSize != scanInfo.getScanFolders().get(folder)) {
                        if (updatedFolderSize > scanInfo.getScanFolders().get(folder)) {
                            updatedFoldersHigher.add(folder);
                        } else {
                            updatedFoldersLower.add(folder);
                        }
                        scanInfo.getScanFolders().put(folder, updatedFolderSize);
                    }
                }
            }
        }

        scanInfo.setUpdatedScanSize(updatedScanSize);
        scanInfo.setUpdatedScanDate(new Date());
        scanInfo.setDeletedFolders(deletedFolders);
        if (!updatedFoldersHigher.isEmpty() || !updatedFoldersLower.isEmpty()) {
            scanInfo.setUpdatedFoldersHigher(updatedFoldersHigher);
            scanInfo.setUpdatedFoldersLower(updatedFoldersLower);
        }

        new WriteDb(scanInfo);
    }

}
