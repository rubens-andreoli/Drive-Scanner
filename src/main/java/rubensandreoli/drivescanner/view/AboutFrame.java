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
package rubensandreoli.drivescanner.view;

import rubensandreoli.drivescanner.view.support.IconLoader;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class AboutFrame extends JFrame {

    public AboutFrame(int mainX, int mainY, int mainWidth, int mainHeight) {
        super("About");
        int width = 550;
        int height = 300;
        this.setSize(width, height);
        this.setResizable(false);
        this.setDefaultCloseOperation(AboutFrame.DISPOSE_ON_CLOSE);
        this.setIconImage(new IconLoader().createIcon("icon.png").getImage());
        this.setLayout(new BorderLayout());

        int x = (mainWidth / 2) - (width / 2) + mainX;
        int y = (mainHeight / 2) - (height / 2) + mainY;
        this.setLocation(x, y);

        JTextPane license = new JTextPane();
        license.setEditable(false);
        license.setContentType("text/html");
        license.setText(
                "<body style=\"text-align:justify;\"><b>Drive Scanner</b><br/>"
                + "Version: 1.0.0<br/>"
                + "<p style=\"font-size:10px;\">Copyright (C) 2022  Rubens A. Andreoli Júnior</p>"
                + "<p style=\"font-size:10px;\">This program is free software: you can redistribute it and/or modify "
                + "it under the terms of the GNU General Public License as published by "
                + "the Free Software Foundation, either version 3 of the License, or "
                + "any later version.</p>"
                + "<p style=\"font-size:10px;\">This program is distributed in the hope that it will be useful, "
                + "but WITHOUT ANY WARRANTY; without even the implied warranty of "
                + "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the "
                + "GNU General Public License for more details.</p>"
                + "<p style=\"font-size:10px;\">You should have received a copy of the GNU General Public License "
                + "along with this program.  If not, see http://www.gnu.org/licenses.</p></body>"
        );

        JScrollPane licensePane = new JScrollPane();
        licensePane.setViewportView(license);
        license.setCaretPosition(0);

        /*JButton btnOk = new JButton("OK");
        btnOk.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				AboutFrame.this.dispose();
			}	
        });*/
        JLabel logo = new JLabel("");
        logo.setVerticalAlignment(SwingConstants.TOP);
        logo.setIcon(new IconLoader().createIcon("logo.png"));

        /*JPanel westPane = new JPanel();
        westPane.setLayout(new BorderLayout());
        westPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 10));
        westPane.add(logo, BorderLayout.NORTH);
        westPane.add(btnOk, BorderLayout.SOUTH);*/
        this.add(logo, BorderLayout.WEST);
        this.add(licensePane, BorderLayout.CENTER);
        this.setVisible(true);
    }

}
