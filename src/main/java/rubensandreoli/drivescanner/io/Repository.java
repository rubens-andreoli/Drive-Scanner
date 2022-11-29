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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JOptionPane;

public class Repository { //not synchronized.

    //<editor-fold defaultstate="collapsed" desc="DATA">
    public class Data {

        private final Map<File, Set<Scan>> scans = new HashMap<>();

        void addScan(Scan scan){
            final File key = scan.getDrive();
            if(!scans.containsKey(key)){
                scans.put(key, new TreeSet<>());
            }

            final Set<Scan> value = scans.get(key);
            value.add(scan);
        }
        
        void deleteScan(Scan scan) {
            scans.get(scan.getDrive()).remove(scan);
        }

        public Collection<Scan> getDriveScans(File drive){
            if(!scans.containsKey(drive)){
                return new HashSet<>();
            }
            return scans.get(drive);
        }

        public Set<Folder> getDriveFolders(File drive){
            final Set<Folder> value = new HashSet<>();
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
            else{
                return !scans.containsKey(drive);
            }
        }

    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="LISTENERS">
    public static interface LoadListener{
        void onLoaded(Data data);
        void onLoadException(Exception e, String fileName);
    }

    public static interface SaveListener{
        void onSaved(Scan scan);
        void onSaveException(Exception e, String scanName);
    }
        
    public static interface DeleteListener {
        void removed(Collection<Scan> removed, Collection<Scan> failed);
    }
    //</editor-fold>
    
    private static final String FOLDER_NAME = "history";
    private static final String FILE_EXTENSION = ".scan";
    
    private static final Repository INSTANCE = new Repository();
    private final File folder;
    private Data data;

    private Repository() {
        folder = new File(System.getProperty("user.dir"), FOLDER_NAME);
        if (!folder.exists()) {
            folder.mkdir();
        }
    }

    public void load(LoadListener listener) {
        data = new Data();
        File[] scanFiles = folder.listFiles();
        for (File scanFile : scanFiles) {
            if (scanFile.isFile() && scanFile.getName().endsWith(FILE_EXTENSION)) {
                try (var ois = new ObjectInputStream(new FileInputStream(scanFile))) {
                    data.addScan((Scan) ois.readObject());
                } catch (FileNotFoundException e) {
                    listener.onLoadException(e, scanFile.getName());
                } catch (IOException | ClassNotFoundException e) {
                    listener.onLoadException(e, scanFile.getName());
                }
            }
        }
        listener.onLoaded(data);
    }

    public void addScan(Scan scan, SaveListener listener){
        if(saveScan(scan, listener)) data.addScan(scan); //add only if saved.
    }
    
    public void updateScan(Scan scan, SaveListener listener){
        saveScan(scan, listener); //TODO: what if updating fails? reload old scan?
    }
    
    private boolean saveScan(Scan scan, SaveListener listener) {
        try (var oos = new ObjectOutputStream(new FileOutputStream(createScanFile(scan.getFilename())))) {
            oos.writeObject(scan);
            listener.onSaved(scan);
            return true;
        } catch (FileNotFoundException e) {
            listener.onSaveException(e, scan.getName());
        } catch (IOException e) {
            listener.onSaveException(e, scan.getName());
        }
        return false;
    }
    
    private File createScanFile(String scanFilename){
        return new File(folder, scanFilename+FILE_EXTENSION);
    }
    
    public boolean existsScan(File drive, String name){
        return createScanFile(Scan.createFilename(drive, name)).isFile();
    }
    
    public Scan renameScan(Scan scan, String newName, SaveListener saveListener){
        Scan newScan = new Scan(newName, scan);
        File newScanFile = createScanFile(newScan.getFilename());
        
        if(!newScanFile.isFile() && saveScan(newScan, saveListener)){
            deleteScan(scan); //delete old only if saved.
            return newScan;
        }
        return null;
    }
    
    public boolean deleteScan(Scan scan){
        return deleteScan(scan, false);
    }
    
    private boolean deleteScan(Scan scan, boolean removeAnyway){
        File scanFile = createScanFile(scan.getFilename());
        boolean succeeded = false;
        if(scanFile.isFile() && scanFile.delete()){
            succeeded = true;
        }
        if(removeAnyway || succeeded) data.deleteScan(scan);
        return succeeded;
    }
    
    public void deleteScans(File drive, DeleteListener listener){
        Collection<Scan> removed = new HashSet<>();
        Collection<Scan> failed = new HashSet<>();
        for (Scan scan : data.getDriveScans(drive)) {
            if(deleteScan(scan, false)) removed.add(scan);
            else failed.add(scan);
        }
        listener.removed(removed, failed);
    }

    public Data getData() {
        return data;
    }

    public static Repository getInstance() {
        return INSTANCE;
    }

}
