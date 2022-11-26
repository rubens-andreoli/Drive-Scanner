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

import rubensandreoli.drivescanner.model.support.PArrayList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collections;

import javax.swing.JOptionPane;

public class ReadDb {

    private PArrayList scansInfoList;

    public ReadDb() {
        File historyFolder = new File(System.getProperty("user.dir") + "\\history");
        if (!historyFolder.exists()) {
            historyFolder.mkdir();
        } else {
            this.readScanFiles(historyFolder.listFiles());
            if (!scansInfoList.isEmpty()) {
                Collections.sort(scansInfoList);
            }
        }
    }

    public void readScanFiles(File[] scanFiles) {
        scansInfoList = new PArrayList();
        for (File scanFile : scanFiles) {
            if (scanFile.isFile() && scanFile.getName().endsWith("scan")) {
                try ( FileInputStream fileInput = new FileInputStream(scanFile);  ObjectInputStream objectInput = new ObjectInputStream(fileInput);) {
                    ScanInfo scanInfo = (ScanInfo) objectInput.readObject();
                    scansInfoList.add(scanInfo);
                } catch (FileNotFoundException e) {
                    //new PiErrorLogOut(e);
                    JOptionPane.showMessageDialog(null,
                            "Scan file '" + scanFile.getName() + "' not found. "
                            + "Reopening the program should fix it.",
                            "Error: File Not Found",
                            JOptionPane.ERROR_MESSAGE);
                } catch (IOException e) {
                    //new PiErrorLogOut(e);
                    JOptionPane.showMessageDialog(null,
                            "Scan file '" + scanFile.getName() + "' could not be opened. "
                            + "Verify folder access permissions.",
                            "Error: File Access Denied",
                            JOptionPane.ERROR_MESSAGE);
                } catch (ClassNotFoundException e) {
                    //new PiErrorLogOut(e);
                    JOptionPane.showMessageDialog(null,
                            "Scan file '" + scanFile.getName() + "' is an outdated scan file. "
                            + "Manually deleting the 'history' folder should fix the problem.",
                            "Error: Invalid Data",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    public PArrayList getScansInfoList() {
        return scansInfoList;
    }

}
