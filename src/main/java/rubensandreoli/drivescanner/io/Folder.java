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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Folder implements Serializable, Comparable<Folder> {

    private static final long serialVersionUID = 3L;

    public enum State implements Serializable{
        UNCHANGED, DELETED, INCREASED, DECREASED
    };

    private final File file;
    private long originalSize, currentSize;
    private State state;
    private Map<String, Long> files;

    public Folder(File file) {
        this.file = file;
    }
    
    public static Map<String, Long> getNewFileMap(){
        return new HashMap<>(); //ignored files' order for performance reasons.
    }

    void setFiles(Map<String, Long> files){
        if(files == null){
            this.files = Collections.EMPTY_MAP;
            state = State.DELETED;
            currentSize = 0L;
        }else{
            this.files = Collections.unmodifiableMap(files);
            calculateSize();
        }
    }
    
    private void calculateSize(){
        currentSize = 0L;
        for (long fileSize : files.values()) {
            currentSize += fileSize;
        }
        if(state == null){ //new.
            state = State.UNCHANGED;
            originalSize = currentSize;
        }else{ //updating.
            if (currentSize == originalSize) {
                state = State.UNCHANGED;
            }else if (currentSize > originalSize){
                state = State.INCREASED;
            }else{
                state = State.DECREASED;
            }
        }
    }

//    void resetState(){
//        state = State.UNCHANGED;
//        currentSize = originalSize;
//    }

    public File getFile() {
        return file;
    }

    public long getCurrentSize() {
        return currentSize;
    }
    
    public long getOriginalSize(){
        return originalSize;
    }
    
    public int getTotalFiles(){
        return files.size();
    }
    
    public Map<String, Long> getFiles(){
        return files;
    }

    public State getState() {
        return state;
    }

    @Override
    public int hashCode() {
        return 31 + file.hashCode();
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
