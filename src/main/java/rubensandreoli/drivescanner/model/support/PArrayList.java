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
package rubensandreoli.drivescanner.model.support;

import rubensandreoli.drivescanner.model.ScanInfo;

import java.io.File;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class PArrayList extends ArrayList<ScanInfo> {

    public ScanInfo getScan(File drive, String scanName) {
        ScanInfo scanInfo = new ScanInfo(drive, scanName);
        if (this.contains(scanInfo)) {
            return this.get(this.indexOf(scanInfo));
        }
        return null;
    }

}
