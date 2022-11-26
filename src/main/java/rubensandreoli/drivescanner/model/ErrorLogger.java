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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JOptionPane;

public class ErrorLogger {

    public ErrorLogger(Exception eToWrite) {
        File file = new File(System.getProperty("user.dir") + "error_log.txt");
        if (!file.exists() || file.isDirectory()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "IOException: error creating 'error_log' text file.");
            }
        }

        StringWriter stringWritter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWritter);
        eToWrite.printStackTrace(printWriter);
        stringWritter.toString();

        try ( PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(System.getProperty("user.dir") + "error_log.txt", true)))) {
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date date = new Date();
            out.println("------------------------------ " + dateFormat.format(date) + " ------------------------------");
            out.println(stringWritter);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "IOException: error writing in 'error_log' text file.");
        }
    }

}
