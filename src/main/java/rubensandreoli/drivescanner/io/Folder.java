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

    private static final long serialVersionUID = 2L;

    public static enum State implements Serializable{
        UNCHANGED, DELETED, INCREASED, DECREASED
    };

    private final File file;
    private long size;
    private State state;
    private boolean calculated = false;
    private Map<String, Long> files = new HashMap<>(); //ignored files' order for performance reasons.
    //TODO: save on a different file (transient and save after), load only when needed (update, display). record time for comparison.
//    private boolean isRoot;

    public Folder(File file) {
        this.file = file;
    }
    
    void addFile(String name, long size){
        Long oldSize = files.put(name, size);
        if(oldSize == null || size != oldSize){
            calculated = false;
        }
    }
    
    void calculateSize(){
        long tempSize = 0L;
        for (long size : files.values()) {
            tempSize += size;
        }
        if(state == null){ //just created.
            state = State.UNCHANGED;
            this.size = tempSize;
        }else{ //updating.
            if (tempSize == this.size) {
                state = State.UNCHANGED;
            }else{
                if (tempSize > this.size) state = State.INCREASED;
                else state = State.DECREASED;
                this.size = tempSize;
            }
        }
        calculated = true;
    }
    
    void setDeleted(){ //if folder is created again, calculateSize() will remove deleted state.
        state = State.DELETED;
        files.clear();
        size = 0;
        calculated = true;
    }

    public File getFile() {
        return file;
    }

    public long getSize() {
        if(!calculated) calculateSize();
        return size;
    }

    public State getState() {
        return state;
    }

//    public boolean isRoot() {
//        return isRoot;
//    }
//
//    void setRoot(boolean isRoot) {
//        this.isRoot = isRoot;
//    }

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
