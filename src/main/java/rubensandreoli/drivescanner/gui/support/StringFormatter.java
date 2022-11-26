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

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StringFormatter {

    private static final String[] UNITS = new String[]{"B", "KB", "MB", "GB", "TB"};
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.#");
    
    public static String formatSize(long size) {
        String formatedSize = "0";
        if (size != 0) {
            int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
            formatedSize = DECIMAL_FORMAT.format(size / Math.pow(1024, digitGroups))+ " " + UNITS[digitGroups];
        }
        return formatedSize;
    }
    
    public static String formatDate(Date date){
        return DATE_FORMAT.format(date);
    }
    
    public static String formatNumber(int value){
        return NumberFormat.getInstance().format(value);
    }

}
