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
package rubensandreoli.drivescanner.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;
import rubensandreoli.drivescanner.view.ScansManager;

public class ScanInfoOut {

    public ScanInfoOut(File drive, String scanName, Map<File, Long> foldersMap) {
        //Calculate ScanSize:
        long scanSize = 0;
        for (long folderSize : foldersMap.values()) {
            scanSize += folderSize;
        }
        //Create new 'ScanInfo' object, find and set folders parents, then save scan:
        ScanInfo piScanInfo = new ScanInfo(drive, scanName, scanSize, foldersMap);
        piScanInfo.setFoldersParents(this.findFoldersParents(piScanInfo));
        this.writeScanInfo(piScanInfo);
    }

    public ScanInfoOut(ScanInfo piScanInfo) {
        //piScanInfo.setFoldersParents(this.findFoldersParents(piScanInfo));
        this.writeScanInfo(piScanInfo);
    }

    private Set<File> findFoldersParents(ScanInfo piScanInfo) {
        //FIXME:Use proper recursion!!
        Set<File> parentFolders = new HashSet<File>();
        for (File folderName : piScanInfo.getScanFoldersInfo().keySet()) {
            if (folderName.toString().replace("\\", "").length() == folderName.toString().length() - 1) {
                parentFolders.add(folderName);
            } else {
                File parentFolder = folderName;
                while (piScanInfo.getScanFoldersInfo().containsKey(parentFolder) || !parentFolder.equals(piScanInfo.getDrive())) {
                    if (parentFolder.toString().replace("\\", "").length() == parentFolder.toString().length() - 1) {
                        break;
                    }
                    if (!piScanInfo.getScanFoldersInfo().containsKey(parentFolder.getParentFile())) {
                        break;
                    }
                    parentFolder = parentFolder.getParentFile();
                }
                parentFolders.add(parentFolder);
            }
        }
        return parentFolders;
    }

    private void writeScanInfo(ScanInfo piScanInfo) {
        //Save 'ScanInfo' object:
        try ( FileOutputStream fileOutput = new FileOutputStream(new ScansManager().formatScanFilename(piScanInfo.getDrive(), piScanInfo.getScanName()));  ObjectOutputStream objectOutput = new ObjectOutputStream(fileOutput);) {
            objectOutput.writeObject(piScanInfo);
        } catch (FileNotFoundException e) {
            new ErrorLogger(e);
            JOptionPane.showMessageDialog(null,
                    "History folder not found. "
                    + "Reopening the program should fix it. "
                    + "If problem persists, try create manually a folder named 'history' in the root folder of the program.",
                    "Error: File Not Found",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            new ErrorLogger(e);
            JOptionPane.showMessageDialog(null,
                    "Scan '" + piScanInfo.getScanName() + "' information could not be saved. "
                    + "Verify program's root folder access permissions.",
                    "Error: File Access Denied",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

}
