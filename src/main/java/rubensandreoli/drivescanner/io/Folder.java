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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Folder implements Serializable, Comparable<Folder> {

    private static final long serialVersionUID = 3L;

    public static enum State implements Serializable{
        UNCHANGED, DELETED, INCREASED, DECREASED
    };

    private final File file;
    private long originalSize, currentSize;
    private State state;
    private boolean calculated = false;
    private Map<String, Long> files = new HashMap<>(); //ignored files' order for performance reasons.
//    private boolean filesChanged = false;

    public Folder(File file) {
        this.file = file;
    }
        
    void addFile(String name, long size){
        files.put(name, size);
        calculated = false;
    }
    
    void setFiles(Map<String, Long> files){
//        if(!this.files.keySet().equals(files.keySet())){
//            filesChanged = true;
//        }
        this.files = files;      
        calculated = false;
    }
    
    void calculateSize(){
        long tempSize = 0L;
        for (long fileSize : files.values()) {
            tempSize += fileSize;
        }
        if(state == null){ //just created.
            state = State.UNCHANGED;
            originalSize = currentSize = tempSize;
        }else{ //updating.
            if (tempSize == this.currentSize) {
//                if(!filesChanged)
                    state = State.UNCHANGED;
//                else{
//                    state = State.CHANGED;
//                    filesChanged = false;
//                }
            }else{
                if (tempSize > this.currentSize) state = State.INCREASED;
                else state = State.DECREASED;
                this.currentSize = tempSize;
            }
        }
        calculated = true;
    }
 
    void setDeleted(){ //if folder is found again, calculateSize() will remove deleted state.
        state = State.DELETED;
        files.clear();
        currentSize = 0;
        calculated = true;
    }
    
    void resetState(){
        state = State.UNCHANGED;
        originalSize = currentSize;
    }

    public File getFile() {
        return file;
    }

    public long getCurrentSize() {
        if(!calculated) calculateSize();
        return currentSize;
    }
    
    public long getOriginalSize(){
        if(!calculated) calculateSize();
        return originalSize;
    }
    
    public int getTotalFiles(){
        return files.size();
    }

    public State getState() {
        return state;
    }

    @Override
    public int hashCode() {
        return 55 + file.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        return Objects.equals(this.file, ((Folder) obj).file);
    }
    
    @Override
    public int compareTo(Folder other) {
        return file.compareTo(other.file);
    }

    @Override
    public String toString() {
        return file.getPath();
    }

}
