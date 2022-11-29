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
import java.util.Date;
import java.util.Set;

public class Scan implements Serializable, Comparable<Scan> {
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

    public Scan(Scan scan){
        this(scan.name, scan);
    }
    
    public Scan(String name, Scan scan){
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
            scanSize += subfolder.getSize();
        }
        return scanSize;
    }

    static String createFilename(File drive, String name){
        String rootPath = drive.getPath(); 
        String rootLetter = rootPath.substring(0, rootPath.length()-2).toLowerCase();
        String normalizedName = name.replaceAll(NORMALIZATION_REGEX, "").toLowerCase(); 
        return rootLetter+FILENAME_DIVISOR+normalizedName;
    }

    void setUpdated(Date updatedDate) {
        this.updatedDate = updatedDate;
        updatedSize = calculateSize(folders);
    }
    
    void merge(Collection<Scan> scans){
        for (Scan s : scans) {
            folders.addAll(s.getFolders());
            size += s.size;
        }
        updatedDate = null;
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

    @Override
    public int hashCode() {
        return 31 + drive.hashCode() + name.hashCode();
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
    public int compareTo(Scan other) {
        return date.compareTo(other.date);
    }

    @Override
    public String toString() {
        return name;
    }
    
}
