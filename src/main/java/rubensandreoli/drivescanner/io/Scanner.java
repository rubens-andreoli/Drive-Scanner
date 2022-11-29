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
import java.util.LinkedHashSet;
import java.util.Set;

public class Scanner {

    //<editor-fold defaultstate="collapsed" desc="LISTENER">
    public static interface Handler{
        void setStatus(String status);
        boolean isInterrupted();
    }
    //</editor-fold>
    
    private final Handler handler;

    public Scanner(Handler handler) {
       this.handler = handler;
    }
 
    public Scan scan(String name, File drive, Set<Folder> oldFolders) {
        Set<Folder> newFolders = new LinkedHashSet<>();
        this.folderCrawler(new Folder(drive), newFolders, oldFolders);
        if(handler.isInterrupted()) return null;
        return new Scan(name, drive, newFolders);
    }

    //22957; 22352; 30700; 23713; 24975
    //17023; 17519; 19046/ 17797; 18120
    private void folderCrawler(Folder folder, Set<Folder> newFolders, Set<Folder> oldFolders) { //TODO: try to improve performance.
        File[] folderFiles = folder.getFile().listFiles();
        //Check folder size and add to Map if folder is not registered in previous scans:
        if (!oldFolders.contains(folder)) {
            for (File childFile : folderFiles) {
                if(handler.isInterrupted()) return;
                if (childFile.isFile()) {
                    folder.addFile(childFile.getName(), childFile.length());
                }
            }
            folder.calculateSize();
            newFolders.add(folder); 
        }
        handler.setStatus(folder.toString());
        //Crawl if file is directory and its not empty:
        for (File childFile : folderFiles) {
            if(handler.isInterrupted()) return;
            if (childFile.isDirectory()) {
                File[] grandchildFiles = childFile.listFiles();
                if (grandchildFiles != null) {
                    this.folderCrawler(new Folder(childFile), newFolders, oldFolders);
                }
            }
        }
    }

    public void update(Scan scan) { //TODO: try to improve performance.
        Set<Folder> folders = scan.getFolders();
        for (Folder folder : folders) {
            if(handler.isInterrupted()) return;
            handler.setStatus(folder.toString());
            File folderFile = folder.getFile();
            if (!folderFile.canRead()) {
                folder.setDeleted();
            } else {
                File[] folderFiles = folderFile.listFiles();
                if (folderFiles != null) {
                    for (File childFile : folderFiles) {
                        if(handler.isInterrupted()) return;
                        if (childFile.isFile()) {
                            folder.addFile(childFile.getName(), childFile.length());
                        }
                    }
                }
                folder.calculateSize();
            }
        }
        scan.setUpdated(new Date());
    }
        
    public static File[] getRoots(){
        return File.listRoots();
    }

}
