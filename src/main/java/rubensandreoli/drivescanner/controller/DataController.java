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
package rubensandreoli.drivescanner.controller;

import rubensandreoli.drivescanner.controller.events.Scan;
import rubensandreoli.drivescanner.controller.events.Update;
import rubensandreoli.drivescanner.controller.support.TablePanelInfo;
import rubensandreoli.drivescanner.controller.support.ToolsPanelInfo;
import rubensandreoli.drivescanner.model.ReadDb;
import rubensandreoli.drivescanner.model.ScanInfo;
import rubensandreoli.drivescanner.model.WriteDb;
import rubensandreoli.drivescanner.model.support.PArrayList;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class DataController {

    private File driveSelected;

    private PArrayList scansInfoList;
    private ScanInfo scanInfoSelected;

    public DataController() {
        ReadDb readDb = new ReadDb();
        this.scansInfoList = readDb.getScansInfoList();
    }

    public void setDriveSelected(File driveSelected) {
        this.driveSelected = driveSelected;
    }

    public void setScanInfoSelected(String scanNameSelected) {
        if (!scansInfoList.isEmpty() && scansInfoList != null) {
            this.scanInfoSelected = scansInfoList.getScan(driveSelected, scanNameSelected);
        }
    }

    public List<String> getDriveScans() {
        List<String> scansName = new LinkedList<String>();
        if (scansInfoList != null) {
            for (ScanInfo scanInfo : scansInfoList) {
                if (scanInfo.getDrive().equals(driveSelected)) {
                    scansName.add(scanInfo.getScanName());
                }
            }
        }
        return scansName;
    }

    public ToolsPanelInfo getToolsPanelInfo() {
        return new ToolsPanelInfo(scanInfoSelected);
    }

    public TablePanelInfo getTablePanelInfo(boolean isFilter) {
        return new TablePanelInfo(isFilter, scanInfoSelected);
    }

    public boolean deleteScan() {
        if (scanInfoSelected.toFile().exists() && scanInfoSelected.toFile().isFile()) {
            if (scanInfoSelected.toFile().delete()) {
                scansInfoList.remove(scanInfoSelected);
                return true;
            }
        }
        return false;
    }

    public boolean testScanName(String scanName) {
        if (scanName != null && !scanName.equals("")) {
            ScanInfo scanInfo = new ScanInfo(driveSelected, scanName);
            if (scanInfo.toFile().exists()) {
                return false;
            }
        }
        return true;
    }

    public boolean renameScan(String scanNewName) {
        if (scanInfoSelected.toFile().exists()) {
            File scanFileTest = new ScanInfo(driveSelected, scanNewName).toFile();
            scanInfoSelected.toFile().renameTo(scanFileTest);
            if (scanFileTest.exists()) {
                scanInfoSelected.setScanName(scanNewName);
                return true;
            }
        }
        return false;
    }

    public void updateScan() {
        new Update(scanInfoSelected);
    }

    public boolean startScan(String scanName) {
        Set<File> oldFoldersSet = new HashSet<File>();
        for (ScanInfo scanInfo : scansInfoList) {
            if (scanInfo.getDrive().equals(driveSelected)) {
                oldFoldersSet.addAll(scanInfo.getScanFolders().keySet());
            }
        }

        Scan scan = new Scan(driveSelected, oldFoldersSet);
        if (!scan.getNewFoldersMap().isEmpty()) {
            ScanInfo scanInfo = new ScanInfo(driveSelected, scanName, scan.getNewFoldersMap());
            new WriteDb(scanInfo);
            scansInfoList.add(scanInfo);
            return true;
        }

        return false;
    }

}
