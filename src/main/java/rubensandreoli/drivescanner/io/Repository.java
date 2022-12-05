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
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Repository { //not synchronized.

    //<editor-fold defaultstate="collapsed" desc="DATA">
    public static class Data {

        private final Map<File, Collection<Scan>> scans = new HashMap<>();
        
        private Data(){}

        /**
         * @param scan if data already contains a scan with the same date, it won't be added.
         */
        void addScan(Scan scan){
            File key = scan.getDrive();
            if(!scans.containsKey(key)){
                scans.put(key, Scan.getNewScanCollection());
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
                return Collections.EMPTY_SET;
            }
            return scans.get(drive);
        }

        public Set<Folder> getDriveFolders(File drive){ 
            if(!scans.containsKey(drive)){
                return Collections.EMPTY_SET;
            }
            Set<Folder> value = new HashSet<>(); //used for reference when performing a new scan; return is only read and must be fast for contains.
            for (Scan scan : scans.get(drive)) {
                value.addAll(scan.getFolders());
            }
            return value;
        }
        
        public boolean isDriveEmpty(File drive){
            return !scans.containsKey(drive);
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

        @Override
        public String toString() {
            return message;
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

    public static final String FOLDER_NAME = "history";
    public static final String FILE_EXTENSION = ".scan";
    
    private static final Repository INSTANCE = new Repository();
    private final File folder;
    private final Data data = new Data();

    private Repository() {
        folder = new File(System.getProperty("user.dir"), FOLDER_NAME);
        if (!folder.exists()) {
            folder.mkdir();
        }
    }
    
    public static Repository getInstance() {
        return INSTANCE;
    }

    public Data load(LoadListener listener) {
        for (File scanFile : folder.listFiles()) {
            final String filename = scanFile.getName();
            if (scanFile.isFile() && filename.endsWith(FILE_EXTENSION)) {
                listener.onLoading(filename);
                try (var ois = new ObjectInputStream(new FileInputStream(scanFile))) {
                    data.addScan((Scan) ois.readObject()); 
                } catch (FileNotFoundException ex) {
                    listener.onLoadError(new Error(scanFile.getName(), ex));
                } catch (InvalidClassException ex){
                     listener.onLoadError(new Error(scanFile.getName(), ex));
                }catch (IOException | ClassNotFoundException ex) {
                    listener.onLoadError(new Error(scanFile.getName(), ex));
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
        } catch (FileNotFoundException ex) {
            listener.onError(new Error(scan.getName(), ex));
        } catch (IOException ex) {
            listener.onError(new Error(scan.getName(), ex));
        }
        return false;
    }
    
    public boolean deleteScan(Scan scan){ //TODO: change behaviour to remove from memory even if failed to delete, just show warning.
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
    
    /**
     * Scans that failed to be deleted will not be removed from memory.
     * 
     * @param drive
     * @return scans that failed to be deleted.
     */
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
    
    /**
     * @param scanFilenames with extension.
     * @return {@code false} if at least one error occurred.
     */
    public boolean deleteScans(Collection<String> scanFilenames) {
        boolean succedded = true;
        for (String scanFilename : scanFilenames) {
            File scanFile = new File(folder, scanFilename);
            try{
                if(!scanFile.isFile() || !scanFile.delete()){
                    succedded = false;
                }
            }catch(Exception ex){
                succedded = false;
            }
        }
        return succedded;
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
    
    /**
     * @param scans if contains the 'into' scan, it will be removed.
     * @param into
     * @param listener
     * @return 
     */
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
    
    public Scan deleteScanFolders(Scan scan, Set<Folder> folders, WorkListener listener){
        Scan newScan = scan.getCopy();
        newScan.removeFolders(folders);
        
        if(saveScan(newScan, listener)){
            data.replaceScan(scan, newScan);
            return newScan;
        }
        return null;
    }
    
    public Scan[] moveScanFolders(Scan scanFrom, Scan scanTo, Set<Folder> folders, MoveListener listener){
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
    
    public void moveScanFolders(Scan scanFrom, String name, File drive, Set<Folder> folders, MoveListener listener){
        Scan scanTo = new Scan(name, drive, folders);
        moveScanFolders(scanFrom, scanTo, folders, listener);
    }
    
    /**
     * @param scanFilename without extension.
     */
    private File createScanFile(String scanFilename){
        return new File(folder, scanFilename+FILE_EXTENSION);
    }

    public boolean existsScan(File drive, String name){
        return createScanFile(Scan.createFilename(drive, name)).isFile();
    }

    public Data getData() {
        return data;
    }

}
