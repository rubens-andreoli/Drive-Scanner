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

import rubensandreoli.drivescanner.view.events.EventComponent;
import rubensandreoli.drivescanner.view.events.EventsID;

import javax.swing.JButton;

@SuppressWarnings("serial")
public class PButton extends JButton implements EventComponent {

    private EventsID eventID;

    public PButton(EventsID eventID, String label, int x, int y, int width, String toolsTip) {
        this.eventID = eventID;
        if (label.contains(".")) {
            setIcon(new IconLoader().createIcon(label));
        } else {
            setText(label);
        }
        this.setBounds(x, y, width, 20);
        this.setToolTipText(toolsTip);
    }

    @Override
    public EventsID getEventID() {
        return eventID;
    }

}
