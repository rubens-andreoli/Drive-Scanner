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

import rubensandreoli.drivescanner.view.events.EventsID;
import rubensandreoli.drivescanner.view.events.ViewEvent;
import rubensandreoli.drivescanner.view.events.ViewEventListener;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

@SuppressWarnings("serial")
public class ScansPanel extends JPanel implements ListSelectionListener {

    private JList<String> scanList;
    private DefaultListModel<String> scanListModel;

    private ViewEventListener viewListener;

    public ScansPanel() {
        setLayout(new BorderLayout());

        scanListModel = new DefaultListModel<String>();
        scanList = new JList<String>(scanListModel);

        JScrollPane scrollScanList = new JScrollPane();
        scrollScanList.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollScanList.setViewportView(scanList);

        scanList.addListSelectionListener(this);

        add(scrollScanList, BorderLayout.CENTER);
    }

    public void setViewListener(ViewEventListener viewListener) {
        this.viewListener = viewListener;
    }

    public String getScanSelected() {
        return scanList.getSelectedValue();
    }

    public void refreshScansList(List<String> scanNames) {
        scanListModel.clear();
        for (String scanName : scanNames) {
            scanListModel.addElement(scanName);
        }
    }

    public void setScanSelected(String scanName) {
        scanList.setValueIsAdjusting(true);
        scanList.setSelectedValue(scanName, true);
    }

    @Override
    public void valueChanged(ListSelectionEvent listE) {
        if (listE.getValueIsAdjusting() && scanList.getSelectedIndex() != -1) {
            ViewEvent viewE = new ViewEvent(this, EventsID.SELECT_SCAN);
            if (viewListener != null) {
                viewListener.viewEventOccurred(viewE);
            }
        }

    }

}
