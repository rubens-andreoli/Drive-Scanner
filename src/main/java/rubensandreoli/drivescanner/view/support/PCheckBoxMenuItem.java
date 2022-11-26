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

import javax.swing.JCheckBoxMenuItem;
import javax.swing.KeyStroke;

@SuppressWarnings("serial")
public class PCheckBoxMenuItem extends JCheckBoxMenuItem implements EventComponent {

    private EventsID eventID;

    public PCheckBoxMenuItem(EventsID eventID, String text, boolean isChecked) {
        this.eventID = eventID;
        this.setText(text);
        this.setSelected(isChecked);
    }

    public PCheckBoxMenuItem(EventsID eventID, String text, boolean isChecked, int mnemonic) {
        this.eventID = eventID;
        this.setText(text);
        this.setSelected(isChecked);
        this.setMnemonic(mnemonic);
    }

    public PCheckBoxMenuItem(EventsID eventID, String text, boolean isChecked, int mnemonic, int mask) {
        this.eventID = eventID;
        this.setText(text);
        this.setSelected(isChecked);
        this.setAccelerator(KeyStroke.getKeyStroke(mnemonic, mask));
    }

    @Override
    public EventsID getEventID() {
        return eventID;
    }

}
