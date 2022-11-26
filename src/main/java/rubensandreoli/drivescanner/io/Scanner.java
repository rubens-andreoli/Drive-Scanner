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
package rubensandreoli.drivescanner.io;

import java.io.File;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class Scanner {

    //<editor-fold defaultstate="collapsed" desc="LISTENER">
    public static interface Listener{
        void setStatus(String status);
        boolean isInterrupted();
    }
    //</editor-fold>
    
    private final Listener listener;

    public Scanner(Listener listener) {
        this.listener = Objects.requireNonNull(listener);
    }
 
    public Scan scan(String name, File drive, Set<Folder> oldFolders) {
        final Set<Folder> newFolders = new TreeSet<>();
        this.folderCrawler(new Folder(drive), newFolders, oldFolders);
        if(listener.isInterrupted()) return null;
        return new Scan(name, drive, newFolders);
    }

    //22957; 22352; 30700; 23713; 24975
    //17023; 17519; 19046/ 17797; 18120
    private void folderCrawler(Folder folder, Set<Folder> newFolders, Set<Folder> oldFolders) { //TODO: deal with security exceptions; test altenatives for performance.
        final File[] folderFiles = folder.getFile().listFiles();
        //Check folder size and add to Map if folder is not registered in previous scans:
        if (oldFolders == null || !oldFolders.contains(folder)) {
            long folderSize = 0;
            for (File childFile : folderFiles) {
                if(listener.isInterrupted()) return;
                if (childFile.isFile()) {
                    folderSize += childFile.length();
                }
            }
            folder.setSize(folderSize);
            newFolders.add(folder);
            listener.setStatus(folder.toString());
        }
        //Crawl if file is directory and its not empty:
        for (File childFile : folderFiles) {
            if(listener.isInterrupted()) return;
            if (childFile.isDirectory()) {
                final File[] grandchildFiles = childFile.listFiles();
                if (grandchildFiles != null) {
                    this.folderCrawler(new Folder(childFile), newFolders, oldFolders);
                }
            }
        }
    }

    public void update(Scan scan) {  //TODO: deal with security exceptions.
        final Set<Folder> folders = scan.getFolders();
        for (Folder folder : folders) {
            if(listener.isInterrupted()) return;
            listener.setStatus(folder.toString());
            final File folderFile = folder.getFile();
            if (!folderFile.canRead()) {
                folder.setSize(-1);
            } else {
                final File[] folderFiles = folderFile.listFiles();
                long updatedFolderSize = 0;
                if (folderFiles != null) {
                    for (File folderFileToSize : folderFiles) {
                        if(listener.isInterrupted()) return;
                        if (folderFileToSize.isFile()) {
                            updatedFolderSize += folderFileToSize.length();
                        }
                    }
                }
                folder.setSize(updatedFolderSize);
            }
        }
        scan.setUpdated(new Date());
    }
        
    public static File[] getRoots(){
        return File.listRoots();
    }

}
