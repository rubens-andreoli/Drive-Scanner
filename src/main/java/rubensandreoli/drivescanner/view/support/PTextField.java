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
package rubensandreoli.drivescanner.view.support;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class PTextField extends JTextField {

    public PTextField(int x, int y, int width, String Alignment, String toolsTip) {
        this.setEditable(false);
        this.setBounds(x, y, width, 21);
        this.setBackground(Color.WHITE);

        switch (Alignment.toUpperCase()) {
            case "RIGHT":
                this.setHorizontalAlignment(JTextField.RIGHT);
                break;
            case "LEFT":
                this.setHorizontalAlignment(JTextField.LEFT);
                break;
            case "CENTER":
                this.setHorizontalAlignment(JTextField.CENTER);
                break;
        }

        this.setBorder(BorderFactory.createEtchedBorder());
        this.setToolTipText(toolsTip);

    }

    public void clearField() {
        this.setText("");
    }

}
