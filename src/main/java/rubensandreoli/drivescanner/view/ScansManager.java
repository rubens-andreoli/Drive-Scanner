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

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import rubensandreoli.drivescanner.model.ScanInfo;
import rubensandreoli.drivescanner.model.ScanInfoOut;
import rubensandreoli.drivescanner.model.ScansInfoIn;

public class ScansManager {

    public boolean deleteScan(File drive, String scanName) {
        //Create scan file and delete it: [TRUE: old scan deleted / FALSE: old scan not found]
        File scanFile = this.formatScanFilename(drive, scanName);
        if (scanFile.exists() && scanFile.isFile()) {
            scanFile.delete();
            return true;
        }
        return false;
    }

    public boolean renameScan(File drive, String scanName, String scanNewName, ScansInfoIn piScansInfoIn) {
        //Change scan name and send to be saved, then delete old scan: [TRUE: old scan deleted / FALSE: old scan not found]
        ScanInfo piScanInfo = piScansInfoIn.getSelectedScan(drive, scanName);
        piScanInfo.setScanName(scanNewName);
        new ScanInfoOut(piScanInfo);
        return this.deleteScan(drive, scanName);
    }

    public void testScanFolders(ScanInfo piScanInfo) {
        Set<File> deletedFolders = new HashSet<File>();
        Set<File> updatedFoldersHigher = new HashSet<File>();
        Set<File> updatedFoldersLower = new HashSet<File>();

        //Test each folder of selected scan:
        long updatedScanSize = 0;
        for (File folder : piScanInfo.getScanFoldersInfo().keySet()) {
            if (!folder.canRead()) {
                deletedFolders.add(folder);
            } else {
                File[] folderFiles = folder.listFiles();
                //Check folder size:
                long updatedFolderSize = 0;
                for (File folderFileToSize : folderFiles) {
                    if (folderFileToSize.isFile()) {
                        updatedFolderSize += folderFileToSize.length();
                    }
                }
                updatedScanSize += updatedFolderSize;
                //Register different size folders:
                if (updatedFolderSize != piScanInfo.getScanFoldersInfo().get(folder)) {
                    if (updatedFolderSize > piScanInfo.getScanFoldersInfo().get(folder)) {
                        updatedFoldersHigher.add(folder);
                    } else {
                        updatedFoldersLower.add(folder);
                    }
                    piScanInfo.getScanFoldersInfo().put(folder, updatedFolderSize);
                }
            }
        }

        //Set updated info to scan and send it to be saved:
        piScanInfo.setUpdatedScanSize(updatedScanSize);
        piScanInfo.setUpdatedScanDate(new Date());
        piScanInfo.setDeletedFolders(deletedFolders);
        if (!updatedFoldersHigher.isEmpty()) {
            piScanInfo.setUpdatedFoldersHigher(updatedFoldersHigher);
        }
        if (!updatedFoldersLower.isEmpty()) {
            piScanInfo.setUpdatedFoldersLower(updatedFoldersLower);
        }
        new ScanInfoOut(piScanInfo);
    }

    public File formatScanFilename(File drive, String scanName) {
        //Return scan file with formated name:
        String driveLetter = drive.toString().substring(0, drive.toString().length() - 2).toLowerCase();
        String scanFilename = scanName.replaceAll("[<>:\"\\\\/|?*]", "").toLowerCase();
        String historyFilename = "\\history\\" + driveLetter + "-" + scanFilename + ".scan";
        File scanFile = new File(System.getProperty("user.dir") + historyFilename);
        return scanFile;
    }

}
