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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import rubensandreoli.drivescanner.model.ScanInfo;

public class FoldersScan {

    private Set<File> oldFoldersSet = new HashSet<File>();
    private Map<File, Long> newFoldersMap = new TreeMap<File, Long>();

    public FoldersScan(File drive, List<ScanInfo> scansInfoList) {
        //Get all 'drive' scanned folders and add to Set:
        for (ScanInfo piScanInfo : scansInfoList) {
            if (piScanInfo.getDrive().equals(drive)) {
                for (File oldFolder : piScanInfo.getScanFoldersInfo().keySet()) {
                    oldFoldersSet.add(oldFolder);
                }
            }
        }
        this.folderCrawler(drive);
    }

    private void folderCrawler(File folder) {
        File[] folderFiles = folder.listFiles();
        //Check folder size and add to Map if folder is not registered in previous scans:
        if (oldFoldersSet == null || !oldFoldersSet.contains(folder)) {
            long folderSize = 0;
            for (File childFile : folderFiles) {
                if (childFile.isFile()) {
                    folderSize += childFile.length();
                }
            }
            newFoldersMap.put(folder, folderSize);
        }
        //Crawl if file is directory and its not empty:
        for (File childFile : folderFiles) {
            if (childFile.isDirectory()) {
                File[] grandchildFiles = childFile.listFiles();
                if (grandchildFiles != null) {
                    this.folderCrawler(childFile);
                }
            }
        }
    }

    public Map<File, Long> getNewFoldersMap() {
        return newFoldersMap;
    }

}
