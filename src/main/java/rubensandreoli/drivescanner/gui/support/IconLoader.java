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
package rubensandreoli.drivescanner.gui.support;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class IconLoader {

    public static ImageIcon getIcon(String url) {
        try {
            return new ImageIcon(IconLoader.class.getClassLoader().getResource("images/" + url));
        } catch (NullPointerException ex) {
            return new ImageIcon();
        }
    }
    
    public static Icon applyAlpha(final Icon icon, final Float alpha){
        return new Icon(){
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                 Graphics2D g2 = (Graphics2D) g.create();
                 g2.setComposite(AlphaComposite.SrcAtop.derive(alpha));
                 icon.paintIcon(c, g2, x, y);
                 g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return icon.getIconWidth();
            }

            @Override
            public int getIconHeight() {
                return icon.getIconHeight();
            }
        };
    }

}
