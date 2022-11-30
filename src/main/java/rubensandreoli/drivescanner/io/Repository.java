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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Repository { //not synchronized.

    //<editor-fold defaultstate="collapsed" desc="DATA">
    public class Data {

        private final Map<File, Set<Scan>> scans = new HashMap<>();

        void addScan(Scan scan){
            File key = scan.getDrive();
            if(!scans.containsKey(key)){
                scans.put(key, new TreeSet<>()); //scans with the same date won't be added.
            }
            scans.get(key).add(scan);
        }
        
        void deleteScan(Scan scan) {
            scans.get(scan.getDrive()).remove(scan);
        }
        
        void replaceScan(Scan oldScan, Scan newScan){
            deleteScan(oldScan); //delete must be performed first; set won't allow scans with the same hash.
            addScan(newScan);
        }

        public Collection<Scan> getDriveScans(File drive){
            if(!scans.containsKey(drive)){
                return new HashSet<>();
            }
            return scans.get(drive);
        }

        public Set<Folder> getDriveFolders(File drive){
            Set<Folder> value = new HashSet<>();
            if(!scans.containsKey(drive)){
                return value;
            }
            for (Scan scan : scans.get(drive)) {
                value.addAll(scan.getFolders());
            }
            return value;
        }
        
        public boolean isEmpty(File drive){
            if(drive == null) return scans.isEmpty();
            else return !scans.containsKey(drive);
        }

    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="LISTENERS">
    public static class Error{
        public final String message;
        public final Exception cause;

        public Error(String message, Exception cause) {
            this.message = message;
            this.cause = cause;
        }
    }
    
    @FunctionalInterface
    public static interface LoadListener{
        default void onLoaded(Data data){};
        default void onLoading(String filename){};
        void onLoadError(Error e);
    }

    @FunctionalInterface
    public static interface WorkListener{
        default void onDone(Scan scan){};
        void onError(Error e);
    }

    @FunctionalInterface
    public static interface MoveListener{
        default void onMoved(Scan scanFrom, Scan scanTo){};
        void onMoveError(Error e);
    }
    //</editor-fold>

    private static final String FOLDER_NAME = "history";
    private static final String FILE_EXTENSION = ".scan";
    
    private static final Repository INSTANCE = new Repository();
    private final File folder;
    private final Data data = new Data();

    private Repository() {
        folder = new File(System.getProperty("user.dir"), FOLDER_NAME);
        if (!folder.exists()) {
            folder.mkdir();
        }
    }

    public Data load(LoadListener listener) {
        for (File scanFile : folder.listFiles()) {
            final String filename = scanFile.getName();
            if (scanFile.isFile() && filename.endsWith(FILE_EXTENSION)) {
                listener.onLoading(filename);
                try (var ois = new ObjectInputStream(new FileInputStream(scanFile))) {
                    data.addScan((Scan) ois.readObject());
                } catch (FileNotFoundException e) {
                    listener.onLoadError(new Error(scanFile.getName(), e));
                } catch (IOException | ClassNotFoundException e) {
                    listener.onLoadError(new Error(scanFile.getName(), e));
                }
            }
        }
        listener.onLoaded(data);
        return data;
    }

    public void addScan(Scan scan, WorkListener listener){
        if(saveScan(scan, listener)) data.addScan(scan); //add to memory only if saved.
    }
    
    public void updateScan(Scan scan, WorkListener listener){
        saveScan(scan, listener); //TODO: changes added to memory even before saving. Scanner could return a temporary scan instead. 
    }
    
    private boolean saveScan(Scan scan, WorkListener listener) {
        try (var oos = new ObjectOutputStream(new FileOutputStream(createScanFile(scan.getFilename())))) {
            oos.writeObject(scan);
            listener.onDone(scan);
            return true;
        } catch (FileNotFoundException e) {
            listener.onError(new Error(scan.getName(), e));
        } catch (IOException e) {
            listener.onError(new Error(scan.getName(), e));
        }
        return false;
    }
    
    private File createScanFile(String scanFilename){
        return new File(folder, scanFilename+FILE_EXTENSION);
    }
    
    public boolean deleteScan(Scan scan){
        return deleteScan(scan, null);
    }
    
    private boolean deleteScan(Scan scan, Boolean remove){ //a null 'remove' will remove data from memory only if succeeded.
        File scanFile = createScanFile(scan.getFilename());
        boolean succeeded = (scanFile.isFile() && scanFile.delete());
        if((remove == null && succeeded) || (remove != null && remove)){
            data.deleteScan(scan);
        }
        return succeeded;
    }
    
    public Collection<Scan> deleteScans(File drive){
        return deleteScans(data.getDriveScans(drive), null);
    }
    
    private Collection<Scan> deleteScans(Collection<Scan> scans, Boolean remove){
        Collection<Scan> failed = new HashSet<>();
        for (Scan scan : scans) {
            if(!deleteScan(scan, remove)) failed.add(scan);
        }
        return failed;
    }
    
    public Scan renameScan(Scan scan, String newName, WorkListener listener){
        Scan newScan = new Scan(newName, scan);
        File newScanFile = createScanFile(newScan.getFilename());
        
        if(!newScanFile.isFile() && saveScan(newScan, listener)){
            deleteScan(scan, true); //delete must be performed first; treeSet won't allow scans with the same date.
            data.addScan(newScan);
            return newScan;
        }
        return null;
    }
    
    public Scan mergeScans(Collection<Scan> scans, Scan into, WorkListener listener){
        Scan mergedScan = new Scan(into);
        scans.remove(into); //removes base if passed along with the list; as it is the case.
        mergedScan.merge(scans);
        
        if(!saveScan(mergedScan, e -> listener.onError(e))){
            return null;
        }
        
        deleteScans(scans, true);
        data.addScan(mergedScan);
        
        listener.onDone(mergedScan);
        return mergedScan;
    }
    
    public Scan deleteScanFolders(Scan scan, Collection<Folder> folders, WorkListener listener){
        Scan newScan = scan.getCopy();
        newScan.removeFolders(folders);
        
        if(saveScan(newScan, listener)){
            data.replaceScan(scan, newScan);
            return newScan;
        }
        return null;
    }
    
    public Scan[] moveScanFolders(Scan scanFrom, Scan scanTo, Collection<Folder> folders, MoveListener listener){
        Scan scanToCopy = scanTo.getCopy();
        Scan scanFromCopy = scanFrom.getCopy();
        
        scanToCopy.addFolders(folders);
        scanFromCopy.removeFolders(folders);
        
        if(saveScan(scanToCopy, e -> listener.onMoveError(e)) && saveScan(scanFromCopy, e -> listener.onMoveError(e))){ //TODO: what to do if only the scanTo is saved?
            data.replaceScan(scanTo, scanToCopy);
            data.replaceScan(scanFrom, scanFromCopy);
            listener.onMoved(scanFromCopy, scanToCopy);
            return new Scan[]{scanFromCopy, scanToCopy};
        }
        return null;
    }
    
    public void moveScanFolders(Scan scanFrom, String name, File drive, Collection<Folder> folders, MoveListener listener){
        Scan scanTo = new Scan(name, drive, folders);
        moveScanFolders(scanFrom, scanTo, folders, listener);
    }
    

    public boolean existsScan(File drive, String name){
        return createScanFile(Scan.createFilename(drive, name)).isFile();
    }

    public Data getData() {
        return data;
    }

    public static Repository getInstance() {
        return INSTANCE;
    }

}
