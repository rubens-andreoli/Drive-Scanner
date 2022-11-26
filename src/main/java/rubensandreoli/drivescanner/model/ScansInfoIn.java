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
package rubensandreoli.drivescanner.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JOptionPane;

public class ScansInfoIn {

    private List<ScanInfo> scansInfoList = new LinkedList<ScanInfo>();
    private Map<Date, String[]> scansFilesMap = new TreeMap<Date, String[]>();

    public ScansInfoIn() {
        //Check and list scan history files:
        File historyFolder = new File(System.getProperty("user.dir") + "\\history");
        if (!historyFolder.exists()) {
            historyFolder.mkdir();
        } else {
            //Read each scan saved files and save info to List and objects to Map: 
            File[] scansFiles = historyFolder.listFiles();
            for (File scanFile : scansFiles) {
                if (scanFile.isFile() && scanFile.getName().endsWith("scan")) {
                    try ( FileInputStream fileInput = new FileInputStream(scanFile);  ObjectInputStream objectInput = new ObjectInputStream(fileInput);) {
                        ScanInfo piScanInfo = (ScanInfo) objectInput.readObject();
                        String[] scanInfo = {piScanInfo.getDrive().toString(), piScanInfo.getScanName()};
                        scansFilesMap.put(piScanInfo.getScanDate(), scanInfo);
                        scansInfoList.add(piScanInfo);
                    } catch (FileNotFoundException e) {
                        new ErrorLogger(e);
                        JOptionPane.showMessageDialog(null,
                                "Scan file '" + scanFile.getName() + "' not found. "
                                + "Reopening the program should fix it.",
                                "Error: File Not Found",
                                JOptionPane.ERROR_MESSAGE);
                    } catch (IOException e) {
                        new ErrorLogger(e);
                        JOptionPane.showMessageDialog(null,
                                "Scan file '" + scanFile.getName() + "' could not be opened. "
                                + "Verify program's root folder access permissions.",
                                "Error: File Access Denied",
                                JOptionPane.ERROR_MESSAGE);
                    } catch (ClassNotFoundException e) {
                        new ErrorLogger(e);
                        JOptionPane.showMessageDialog(null,
                                "Scan file '" + scanFile.getName() + "' is an outdated scan file from older versions of the program. "
                                + "Manually deleting it from 'history' folder should fix the problem.",
                                "Error: Invalid Data",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    public ScanInfo getSelectedScan(File drive, String scanName) {
        for (ScanInfo piScanInfo : scansInfoList) {
            if (piScanInfo.getScanName().equals(scanName) && piScanInfo.getDrive().equals(drive)) {
                return piScanInfo;
            }
        }
        return null;
    }

    public Map<Date, String[]> getScansFilesMap() {
        return scansFilesMap;
    }

    public List<ScanInfo> getScansInfoList() {
        return scansInfoList;
    }

}
