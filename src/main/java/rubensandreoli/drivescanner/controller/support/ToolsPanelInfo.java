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
package rubensandreoli.drivescanner.controller.support;

import rubensandreoli.drivescanner.model.ScanInfo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ToolsPanelInfo {

    String scanSize;
    String scanDate;
    String updatedScanSize;
    String updatedScanDate;

    public ToolsPanelInfo(ScanInfo scanInfoSelected) {
        this.scanSize = new SizeFormat().longToString(scanInfoSelected.getScanSize());
        this.updatedScanSize = "";
        if (scanInfoSelected.getUpdatedScanSize() != 0) {
            this.updatedScanSize = new SizeFormat().longToString(scanInfoSelected.getUpdatedScanSize());
        }

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        this.scanDate = dateFormat.format(scanInfoSelected.getScanDate());
        this.updatedScanDate = "";
        if (scanInfoSelected.getUpdatedScanDate() != null) {
            this.updatedScanDate = dateFormat.format(scanInfoSelected.getUpdatedScanDate());
        }
    }

    public String getScanSize() {
        return scanSize;
    }

    public String getScanDate() {
        return scanDate;
    }

    public String getUpdatedScanSize() {
        return updatedScanSize;
    }

    public String getUpdatedScanDate() {
        return updatedScanDate;
    }

}
