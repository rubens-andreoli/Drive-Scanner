package rubensandreoli.drivescanner.view;

import rubensandreoli.drivescanner.controller.DataController;
import rubensandreoli.drivescanner.view.events.ViewEvent;
import rubensandreoli.drivescanner.view.events.ViewEventListener;
import rubensandreoli.drivescanner.view.support.IconLoader;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

@SuppressWarnings("serial")
public class MainFrame extends JFrame implements ViewEventListener {
	
	private MenuBarPanel menuBarPanel;
	private JPanel sidePanel;
	private ScansPanel scansPanel;
	private ToolsPanel toolsPanel;
	private JTabbedPane foldersTab;
	private TablePanel tablePanel;
	
	private DataController dataController;

	public MainFrame() {
		//set frame properties:
		super("Drive Scanner");
		setSize(800, 450);
		setMinimumSize(new Dimension(500, 250));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		this.setIconImage(new IconLoader().createIcon("icon.png").getImage());
		
		//instantiate components:
		dataController = new DataController();
		
		menuBarPanel = new MenuBarPanel();
		sidePanel = new JPanel();
		toolsPanel = new ToolsPanel();
		scansPanel = new ScansPanel();
		foldersTab = new JTabbedPane();
		tablePanel = new TablePanel();
		
		//set components properties:
		Dimension dim = getPreferredSize();
		dim.width = 200;
		sidePanel.setPreferredSize(dim);
		sidePanel.setLayout (new BorderLayout());
		sidePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));
		sidePanel.add(toolsPanel, BorderLayout.NORTH);
		sidePanel.add(scansPanel, BorderLayout.CENTER);
		
		foldersTab.add("Table", tablePanel);
		foldersTab.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));
		setJMenuBar(menuBarPanel);
		
		//set components listeners:
		toolsPanel.setViewListener(this);
		scansPanel.setViewListener(this);
		menuBarPanel.setViewListener(this);		
		
		//initialize frame:
		toolsPanel.addDrives(File.listRoots());	
		
		add(foldersTab, BorderLayout.CENTER);
		add(sidePanel, BorderLayout.WEST);
		
		setVisible(true);	
	}
	
	private void repondEvent(ViewEvent viewE) {
		switch(viewE.getEventId()){
		case SELECT_DRIVE:
			dataController.setDriveSelected(toolsPanel.getDriveSelected());
			toolsPanel.toggleScansBtn(false);
			menuBarPanel.toggleScansItems(false);		
			toolsPanel.clearFolderFields();
			tablePanel.clearFoldersTable();
			scansPanel.refreshScansList(dataController.getDriveScans());
			break;
		case SELECT_SCAN:
			dataController.setScanInfoSelected(scansPanel.getScanSelected());
			toolsPanel.toggleScansBtn(true);
			menuBarPanel.toggleScansItems(true);
			tablePanel.refreshFoldersTable(dataController.getTablePanelInfo(menuBarPanel.isFolderFilter()));
			toolsPanel.refreshFoldersFields(dataController.getToolsPanelInfo());
			break;
		case SCAN:	
			String scanName = JOptionPane.showInputDialog(MainFrame.this, 
					"Enter a name for your scan:", 
					"Name Scan", 
					JOptionPane.QUESTION_MESSAGE);
			
			while(!dataController.testScanName(scanName)) {
				scanName = JOptionPane.showInputDialog(MainFrame.this, 
						"Choose another scan name, this one has already been used:", 
						"Name Scan", 
						JOptionPane.WARNING_MESSAGE);
			}
			
			if(scanName == null || scanName.equals("")) {
				break;
			}
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			if(dataController.startScan(scanName)) {
				scansPanel.refreshScansList(dataController.getDriveScans());
				scansPanel.setScanSelected(scanName);
			} else {
				JOptionPane.showMessageDialog(MainFrame.this,
					    "No new folders were found, this scan will not be saved.",
					    "Nothing New",
					    JOptionPane.WARNING_MESSAGE);
			}
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			break;
		case RENAME:
			String scanNewName = JOptionPane.showInputDialog(MainFrame.this, 
					"Enter a new name for your scan:", 
					"Rename Scan", 
					JOptionPane.QUESTION_MESSAGE);
			
			while(!dataController.testScanName(scanNewName)) {
				scanNewName = JOptionPane.showInputDialog(MainFrame.this, 
						"Choose another scan name, this one has already been used:", 
						"Rename Scan", 
						JOptionPane.WARNING_MESSAGE);
			}
			
			if(scanNewName == null || scanNewName.equals("")) {
				break;
			}
			
			if(dataController.renameScan(scanNewName)) {
				scansPanel.refreshScansList(dataController.getDriveScans());
				scansPanel.setScanSelected(scanNewName);
			} else {
				JOptionPane.showMessageDialog(MainFrame.this,
						"Scan file not found or could not be renamed. "
						+ "Reopen the program to clear it from the Scans List. "
						+ "Or verify folder access permissions.",
						"Error: File Not Found",
						JOptionPane.ERROR_MESSAGE);
			}
			break;
		case DELETE: 
			int confirm = JOptionPane.showConfirmDialog(MainFrame.this, 
					"Are you sure you want to delete this scan?",
			        "Delete Scan", 
			        JOptionPane.OK_CANCEL_OPTION);		
			
			if(confirm != JOptionPane.OK_OPTION) {
				break;	
			}

			if(dataController.deleteScan()) {
				toolsPanel.toggleScansBtn(false);
				menuBarPanel.toggleScansItems(false);
				toolsPanel.clearFolderFields();
				tablePanel.clearFoldersTable();
				scansPanel.refreshScansList(dataController.getDriveScans());
			} else {
				JOptionPane.showMessageDialog(MainFrame.this,
					    "Scan file not found. "
					    + "Reopen the program to clear it from the Scans List.",
					    "Error: File Not Found",
					    JOptionPane.ERROR_MESSAGE);
			}
			break;
		case UPDATE:
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			dataController.updateScan();
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			tablePanel.refreshFoldersTable(dataController.getTablePanelInfo(menuBarPanel.isFolderFilter()));
			toolsPanel.refreshFoldersFields(dataController.getToolsPanelInfo());
			break;
		case EXIT:
			System.exit(0);
			break;	
		case SCAN_PANEL:
			sidePanel.setVisible(menuBarPanel.isScanPanel());
			break;
		case TABLE_FILTER:
			if(scansPanel.getScanSelected() != null)
				tablePanel.refreshFoldersTable(dataController.getTablePanelInfo(menuBarPanel.isFolderFilter()));
			break;
		case ABOUT:
			new AboutFrame(this.getX(), this.getY(), this.getWidth(), this.getHeight());
			break;
		}
	}

	@Override
	public void viewEventOccurred(ViewEvent viewE) {
		repondEvent(viewE);	
	}
	
}
