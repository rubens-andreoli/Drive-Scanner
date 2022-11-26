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
import javax.swing.JOptionPane;

public class History {

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

    }
    //</editor-fold>
    
    private static final String FOLDER_NAME = "history";
    private static final String FILE_EXTENSION = ".scan";
    
    private volatile static History instance;
    private final File folder;
    private Data data;

    private History() {
        folder = new File(System.getProperty("user.dir"), FOLDER_NAME);
        if (!folder.exists()) {
            folder.mkdir();
        }
    }

    private void load() {
        data = new Data();
        final File[] scanFiles = folder.listFiles();
        for (File scanFile : scanFiles) {
            if (scanFile.isFile() && scanFile.getName().endsWith(FILE_EXTENSION)) {
                try (var ois = new ObjectInputStream(new FileInputStream(scanFile))) {
                    final Scan scan = (Scan) ois.readObject();
                    data.addScan(scan);
                } catch (FileNotFoundException e) { //TODO: better way to handle exceptions.
                    JOptionPane.showMessageDialog(null,
                            "Scan file '" + scanFile.getName() + "' not found. "
                            + "Reopening the program should fix it.",
                            "Error: File Not Found",
                            JOptionPane.ERROR_MESSAGE);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null,
                            "Scan file '" + scanFile.getName() + "' could not be opened. "
                            + "Verify folder access permissions.",
                            "Error: File Access Denied",
                            JOptionPane.ERROR_MESSAGE);
                } catch (ClassNotFoundException e) {
                    JOptionPane.showMessageDialog(null,
                            "Scan file '" + scanFile.getName() + "' is an outdated scan file. "
                            + "Manually deleting the 'history' folder should fix the problem.",
                            "Error: Invalid Data",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    public boolean saveScan(Scan scan, boolean updated) {
        try (var oos = new ObjectOutputStream(new FileOutputStream(createScanFile(scan.getFilename())))) {
            oos.writeObject(scan);
            if(!updated) data.addScan(scan);
            return true;
        } catch (FileNotFoundException e) { //TODO: better way to handle exceptions.
            JOptionPane.showMessageDialog(null,
                    "History folder not found. "
                    + "Reopening the program should fix it. "
                    + "If problem persists, try create manually a folder named 'history' in the root folder of the program.",
                    "Error: File Not Found",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Scan '" + scan.getName() + "' information could not be saved. "
                    + "Verify folder access permissions.",
                    "Error: File Access Denied",
                    JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }
    
    private File createScanFile(String scanFilename){
        return new File(folder, scanFilename+FILE_EXTENSION);
    }
    
    public boolean existsScan(File drive, String name){
        final File scanFile = createScanFile(Scan.createFilename(drive, name));
        return scanFile.isFile();
    }
    
    public Scan renameScan(Scan scan, String newName){
        final Scan newScan = new Scan(newName, scan);
        final File newScanFile = createScanFile(newScan.getFilename());
        
        if(!newScanFile.isFile() && saveScan(newScan, false)){
            deleteScan(scan);
            return newScan;
        }
        return null;
    }
    
    public boolean deleteScan(Scan scan){
        final File scanFile = createScanFile(scan.getFilename());
        if(scanFile.isFile() && scanFile.delete()){
            data.deleteScan(scan);
            return true;
        }
        return false;
    }

    public Data getData() {
        return data;
    }

    @SuppressWarnings("DoubleCheckedLocking")
    public static History getInstance() {
        if (instance == null) {
            synchronized (History.class) {
                if (instance == null) {
                    instance = new History();
                    instance.load();
                }
            }
        }
        return instance;
    }

}
