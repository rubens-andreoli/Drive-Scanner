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

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

@SuppressWarnings("serial")
public class PMenuItem extends JMenuItem implements EventComponent {

    private EventsID eventID;

    public PMenuItem(EventsID eventID, String text) {
        this.eventID = eventID;
        this.setText(text);
    }

    public PMenuItem(EventsID eventID, String text, int mnemonic) {
        this.eventID = eventID;
        this.setText(text);
        this.setMnemonic(mnemonic);
    }

    public PMenuItem(EventsID eventID, String text, int mnemonic, int mask) {
        this.eventID = eventID;
        this.setText(text);
        this.setAccelerator(KeyStroke.getKeyStroke(mnemonic, mask));
    }

    @Override
    public EventsID getEventID() {
        return eventID;
    }

}
