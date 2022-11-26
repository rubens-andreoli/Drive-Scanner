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
package rubensandreoli.drivescanner;

import java.awt.EventQueue;
import javax.swing.JOptionPane;
import rubensandreoli.drivescanner.view.MainFrame;
import rubensandreoli.drivescanner.model.ErrorLogger;

public class Launcher {

    public static void main(String[] args) {

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    MainFrame frame = new MainFrame();
                    frame.setVisible(true);
                } catch (Exception e) {
                    new ErrorLogger(e);
                    JOptionPane.showMessageDialog(null,
                            "The program has encountered an unexpected error, "
                            + "please send error_log.txt information to the developer.",
                            "Error: Flow Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

}
