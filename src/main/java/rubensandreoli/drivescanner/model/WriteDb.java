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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.swing.JOptionPane;

public class WriteDb {

    public WriteDb(ScanInfo scanInfo) {
        try ( FileOutputStream fileOutput = new FileOutputStream(scanInfo.toFile());  ObjectOutputStream objectOutput = new ObjectOutputStream(fileOutput);) {
            objectOutput.writeObject(scanInfo);
        } catch (FileNotFoundException e) {
            //new PiErrorLogOut(e);
            JOptionPane.showMessageDialog(null,
                    "History folder not found. "
                    + "Reopening the program should fix it. "
                    + "If problem persists, try create manually a folder named 'history' in the root folder of the program.",
                    "Error: File Not Found",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            //new PiErrorLogOut(e);
            JOptionPane.showMessageDialog(null,
                    "Scan '" + scanInfo.getScanName() + "' information could not be saved. "
                    + "Verify folder access permissions.",
                    "Error: File Access Denied",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

}
