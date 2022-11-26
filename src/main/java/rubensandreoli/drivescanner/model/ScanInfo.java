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
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ScanInfo implements Serializable {

    private static final long serialVersionUID = 6770983295472043526L;

    private File drive;
    private String scanName;
    private Date scanDate;
    private long scanSize;

    private Map<File, Long> scanFoldersInfo;
    private Set<File> foldersParents;

    private Date updatedScanDate;
    private long updatedScanSize;
    private Set<File> updatedFoldersHigher;
    private Set<File> updatedFoldersLower;
    private Set<File> deletedFolders;

    public ScanInfo(File drive, String scanName, Map<File, Long> foldersMap) {
        this.drive = drive;
        this.scanName = scanName;
        this.scanDate = new Date();
        this.scanFoldersInfo = foldersMap;
        this.setScanSize();
        this.setParentFolders();
    }

    private void setScanSize() {
        long scanSize = 0;
        for (long folderSize : scanFoldersInfo.values()) {
            scanSize += folderSize;
        }
        this.scanSize = scanSize;
    }

    private void setParentFolders() {
        foldersParents = new HashSet<File>();
        for (File folderName : scanFoldersInfo.keySet()) {
            if (folderName.toString().replace("\\", "").length() == folderName.toString().length() - 1) {
                foldersParents.add(folderName);
            } else {
                File parentFolder = folderName;
                while (scanFoldersInfo.containsKey(parentFolder) && !parentFolder.equals(this.drive)) {
                    if (!scanFoldersInfo.containsKey(parentFolder.getParentFile())) {
                        break;
                    }
                    parentFolder = parentFolder.getParentFile();
                }
                foldersParents.add(parentFolder);
            }
        }
    }

    public File getDrive() {
        return drive;
    }

    public String getScanName() {
        return scanName;
    }

    public void setScanName(String scanName) {
        this.scanName = scanName;
    }

    public Date getScanDate() {
        return scanDate;
    }

    public long getScanSize() {
        return scanSize;
    }

    public Map<File, Long> getScanFoldersInfo() {
        return scanFoldersInfo;
    }

    public Set<File> getFoldersParents() {
        return foldersParents;
    }

    public void setFoldersParents(Set<File> foldersParents) {
        this.foldersParents = foldersParents;
    }

    public Date getUpdatedScanDate() {
        return updatedScanDate;
    }

    public void setUpdatedScanDate(Date updatedScanDate) {
        this.updatedScanDate = updatedScanDate;
    }

    public long getUpdatedScanSize() {
        return updatedScanSize;
    }

    public void setUpdatedScanSize(long updatedScanSize) {
        this.updatedScanSize = updatedScanSize;
    }

    public Set<File> getDeletedFolders() {
        return deletedFolders;
    }

    public void setDeletedFolders(Set<File> deletedFolders) {
        this.deletedFolders = deletedFolders;
    }

    public Set<File> getUpdatedFoldersHigher() {
        return updatedFoldersHigher;
    }

    public void setUpdatedFoldersHigher(Set<File> updatedFoldersHigher) {
        this.updatedFoldersHigher = updatedFoldersHigher;
    }

    public Set<File> getUpdatedFoldersLower() {
        return updatedFoldersLower;
    }

    public void setUpdatedFoldersLower(Set<File> updatedFoldersLower) {
        this.updatedFoldersLower = updatedFoldersLower;
    }

}
