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

public class ScanInfo implements Serializable, Comparable<ScanInfo> {

    private static final long serialVersionUID = 6770983295472043527L;

    private File drive;
    private String scanName;
    private Date scanDate;
    private Map<File, Long> scanFolders;

    private long scanSize;
    private Set<File> parentFolders;

    private Date updatedScanDate;
    private long updatedScanSize;
    private Set<File> updatedFoldersHigher;
    private Set<File> updatedFoldersLower;
    private Set<File> deletedFolders;

    public ScanInfo(File drive, String scanName, Map<File, Long> foldersInfo) {
        this.drive = drive;
        this.scanName = scanName;
        this.scanDate = new Date();
        this.scanFolders = foldersInfo;
        this.setScanSize();
        this.setParentFolders();
    }

    public ScanInfo(File drive, String scanName) {
        this.drive = drive;
        this.scanName = scanName;
    }

    private void setScanSize() {
        long scanSize = 0;
        for (long folderSize : scanFolders.values()) {
            scanSize += folderSize;
        }
        this.scanSize = scanSize;
    }

    private void setParentFolders() {
        parentFolders = new HashSet<File>();
        for (File folderName : scanFolders.keySet()) {
            if (folderName.toString().replace("\\", "").length() == folderName.toString().length() - 1) {
                parentFolders.add(folderName);
            } else {
                File parentFolder = folderName;
                while (scanFolders.containsKey(parentFolder) && !parentFolder.equals(this.drive)) {
                    if (!scanFolders.containsKey(parentFolder.getParentFile())) {
                        break;
                    }
                    parentFolder = parentFolder.getParentFile();
                }
                parentFolders.add(parentFolder);
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

    public Map<File, Long> getScanFolders() {
        return scanFolders;
    }

    public Set<File> getParentFolders() {
        return parentFolders;
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

    public File toFile() {
        String driveLetter = drive.toString().substring(0, drive.toString().length() - 2).toLowerCase();
        String scanFilename = scanName.replaceAll("[<>:\"\\\\/|?*]", "").toLowerCase();
        String historyFilename = "\\history\\" + driveLetter + "-" + scanFilename + ".scan";
        return new File(System.getProperty("user.dir") + historyFilename);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((drive == null) ? 0 : drive.hashCode());
        result = prime * result + ((scanName == null) ? 0 : scanName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ScanInfo other = (ScanInfo) obj;
        if (drive == null) {
            if (other.drive != null) {
                return false;
            }
        } else if (!drive.equals(other.drive)) {
            return false;
        }
        if (scanName == null) {
            if (other.scanName != null) {
                return false;
            }
        } else if (!scanName.equals(other.scanName)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(ScanInfo scanInfo) {
        return this.scanDate.compareTo(scanInfo.scanDate);
    }

}
