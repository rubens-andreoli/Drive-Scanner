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
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

public class Scan implements Serializable{
    private static final long serialVersionUID = 1L;

    private static final String FILENAME_DIVISOR = "-";
    private static final String NORMALIZATION_REGEX = "[<>:\"\\\\/|?*]";
    
    private final String name, filename;
    private final File drive;
    private final Set<Folder> folders;
    
    private final Date date; 
    private long size;
    
    private Date updatedDate;
    private long updatedSize;

    public Scan(Scan scan){ //shallow copy.
        this(scan.name, scan);
    }

    public Scan(String name, Scan scan){ //shallow copy.
        this(name, scan.drive, scan.folders, scan.date, scan.size);
        if(scan.isUpdated()){
            this.updatedDate = scan.updatedDate;
            this.updatedSize = scan.updatedSize;
        }
    }

    public Scan(String name, File drive, Set<Folder> folders) {
        this(name, drive, folders, new Date(), calculateSize(folders));
    }
    
    private Scan(String name, File drive, Set<Folder> folders, Date date, long size){
        this.name = name;
        this.drive = drive;
        this.folders = folders;
        this.filename = Scan.createFilename(drive, name);
        this.date = date;
        this.size = size;
    }

    private static long calculateSize(Set<Folder> folders){
        long scanSize = 0;
        for (Folder subfolder : folders) {
            scanSize += subfolder.getCurrentSize();
        }
        return scanSize;
    }
    
    static String createFilename(File drive, String name){
        String rootPath = drive.getPath(); 
        String rootLetter = rootPath.substring(0, rootPath.length()-2).toLowerCase();
        String normalizedName = name.replaceAll(NORMALIZATION_REGEX, "").toLowerCase(); 
        return rootLetter+FILENAME_DIVISOR+normalizedName;
    }
    
    public static Set<Folder> getNewFolderSet(){
        return new LinkedHashSet<>();
    }
    
    public static Collection<Scan> getNewScanCollection(){
        return new TreeSet<>((s1, s2) -> s1.date.compareTo(s2.date));
    }

    void setUpdated(Date updatedDate) {
        this.updatedDate = updatedDate;
        updatedSize = calculateSize(folders);
    }
    
    void merge(Collection<Scan> scans){
        for (Scan s : scans) {
            folders.addAll(s.getFolders());
            if(s.isUpdated()){
                size += s.updatedSize;
            }else{
                size += s.size;
            }
        }
        updatedDate = null; //reset updated status; folders' updated information is kept.
        updatedSize = 0;
    }
    
    void removeFolders(Set<Folder> folders){
        for (Folder folder : folders) {
            if(this.folders.remove(folder)){
                if(isUpdated()){
                    updatedSize -= folder.getCurrentSize();
                }
                size -= folder.getOriginalSize();
            }
        }
    }
    
    void addFolders(Set<Folder> folders){
        for (Folder folder : folders) {
            folder.resetState();
            if(this.folders.add(folder)){
                size += folder.getCurrentSize();
            }
        }
        updatedDate = null; //reset updated status; folders' updated information is kept.
        updatedSize = 0;
    }

    public File getDrive() {
        return drive;
    }

    public String getName() {
        return name;
    }

    public String getFilename() {
        return filename;
    }
    
    public Date getDate() {
        return date;
    }
    
    public long getSize() {
        return size;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public long getUpdatedSize() {
        return updatedSize;
    }

    public boolean isEmpty(){
        return folders.isEmpty();
    }
    
    public int getTotalFolders(){
        return folders.size();
    }
    
    public int getTotalFiles(){
        int total = 0;
        for (Folder folder : folders) {
            total += folder.getTotalFiles();
        }
        return total;
    }
    
    public Set<Folder> getFolders() {
        return folders;
    }
    
    public boolean isUpdated(){
        return updatedDate != null;
    }
    
    Scan getCopy(){ //semi-deep copy (each folder is still the same).
        Set<Folder> foldersCopy = getNewFolderSet();
        foldersCopy.addAll(folders);
        return new Scan(name, drive, foldersCopy, date, size);
    }
    
    @Override
    public int hashCode() {
        return 31*(31*drive.hashCode()) + name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Scan other = (Scan) obj;
        return drive.equals(other.drive) && name.equals(other.name);
    }

    @Override
    public String toString() {
        return name;
    }

}
