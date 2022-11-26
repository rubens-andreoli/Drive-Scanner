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
import java.util.Objects;

public class Folder implements Serializable, Comparable<Folder> {

    private static final long serialVersionUID = 1L;

    public static enum State implements Serializable{
        UNCHANGED, DELETED, INCREASED, DECREASED
    };

    private final File file;
    private long size;
    private State state;
//    private boolean isRoot;

    public Folder(File file) {
        this.file = Objects.requireNonNull(file);
    }

    void setSize(long size) {
        if (size < 0) {
            this.size = 0;
            state = State.DELETED;
            return;
        }

        if (state == null) {
            this.size = size;
            state = State.UNCHANGED;
            return;
        }

        if (size == this.size) {
            state = State.UNCHANGED;
            return;
        }

        if (size > this.size) state = State.INCREASED;
        else state = State.DECREASED;
        this.size = size;
    }

    public File getFile() {
        return file;
    }

    public long getSize() {
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
        final Folder other = (Folder) obj;
        return Objects.equals(this.file, other.file);
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
