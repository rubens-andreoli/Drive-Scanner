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

import java.awt.Frame;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InvalidClassException;
import java.util.Collection;
import javax.swing.JOptionPane;
import rubensandreoli.drivescanner.gui.AboutDialog;
import rubensandreoli.drivescanner.gui.ListDialogPanel;
import rubensandreoli.drivescanner.io.Repository;
import rubensandreoli.drivescanner.io.Scan;

public class DialogFactory {

    private static Frame parent;
    private static AboutDialog aboutDialog;

    private DialogFactory(){}
    
    public static void init(Frame parent){
        assert (parent != null): "factory already initialized";
        DialogFactory.parent = parent;
    }

    public static void showAboutDialog(){
        if(aboutDialog == null){
            aboutDialog = new AboutDialog(parent, new AboutDialog.ProgramInfo("Drive Scanner", null, "1.0.0", "2022"));
            aboutDialog.addAtribution("Icons", "Freepik", "https://www.flaticon.com/authors/freepik");
        }
        aboutDialog.setVisible(true);
    }
    
    public static void showErrorDialog(String title, String msg){
        JOptionPane.showMessageDialog(parent, msg, title, JOptionPane.ERROR_MESSAGE);
    }
    
    public static void showWarningDialog(String title, String msg){
        JOptionPane.showMessageDialog(parent, msg, title, JOptionPane.WARNING_MESSAGE);
    }
    
    public static boolean showConfirmDialog(String title, String msg){
        return (JOptionPane.showConfirmDialog(parent, msg, title, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION);
    }
    
    public static String showCreateScanNameDialog(String title, String msg, File selectedDrive){
        String name = JOptionPane.showInputDialog(parent, msg, title, JOptionPane.QUESTION_MESSAGE);
        name = name.trim();
        if (name == null || name.equals("")) return null;
        while(Repository.getInstance().existsScan(selectedDrive, name)){
            name = JOptionPane.showInputDialog(parent, "Choose another scan name, this one has already been used:", title, JOptionPane.WARNING_MESSAGE);
            if (name == null || name.equals("")) return null;
        }
        return name;
    }
    
    @SuppressWarnings("UseSpecificCatch")
    public static void showSaveErrorDialog(Repository.ExceptionMessage exMsg){
        String cause;
        try{
            throw exMsg.cause;
        } catch (FileNotFoundException ex) {
            cause = "History folder not found."
                    + "\nReopening the program should fix it. If problem persists,"
                    + "\ntry creating manually a folder named '"+Repository.FOLDER_NAME+"' in the same folder as the program.";
        } catch (IOException ex) {
            cause = "Scan '" + exMsg.message + "' information could not be saved. "
                    + "Verify folder access permissions.";
        } catch (Exception ex) {
            cause = "Unexpected error while saving scan '"+ex.getMessage()+"'. What now?!"
                    + "\n"+ex.getMessage();
            //TODO: log.
        }
        JOptionPane.showMessageDialog(parent, cause, "Save", JOptionPane.ERROR_MESSAGE);
    }
    
    @SuppressWarnings("UseSpecificCatch")
    public static boolean showLoadingErrorDialog(Collection<Repository.ExceptionMessage> exMsgs){
        var panel = new ListDialogPanel<Repository.ExceptionMessage>();
        panel.setText("The following scan(s) could not be loaded, do you want to delete them?");
        panel.setItems(exMsgs);
        panel.addSelectionListener(e -> {
            if(!e.getValueIsAdjusting()){
                String cause;
                try{
                    throw panel.getSelectedValue().cause;
                } catch (FileNotFoundException ex) {
                    cause = "Scan file not found. It may have been deleted while the program was loading.";
                } catch (InvalidClassException ex) {
                    cause = "Scan file is an outdated scan file.";
                }  catch (IOException | ClassNotFoundException ex) {
                    cause = "Scan file could not be opened. Verify 'history' folder access permissions.";
                }catch (Exception ex) {
                    cause = "Unexpected error while saving scan '"+ex.getMessage()+"'. What now?!"
                    + "\n"+ex.getMessage();
                    //TODO: log.
                }
                panel.setToolTipText(cause);
            }
        });
        
        return JOptionPane.showConfirmDialog(parent, panel, "Loading", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE) == JOptionPane.YES_OPTION;
    }
    
    public static void showDeleteAllErrorDialog(Collection<Scan> scans){
        var panel = new ListDialogPanel<Scan>();
        panel.setText("Scan file(s) could not be deleted."
                + "The file(s) may have been deleted. "
                + "\nIf reopening the program does not remove it(them) from the list,"
                + "\nplease verify '"+Repository.FOLDER_NAME+"' folder access permissions.");
        panel.setItems(scans);
        JOptionPane.showMessageDialog(parent, panel, "Delete All", JOptionPane.ERROR_MESSAGE);
    }
    
    public static Scan showSelectScanDialog(Collection<Scan> scans){
        var panel = new ListDialogPanel<Scan>();
        panel.setText("Select to which scan you want to move the selected folder(s):");
        panel.setItems(scans);
        if(JOptionPane.showConfirmDialog(parent, panel, "Move", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE) == JOptionPane.YES_OPTION){
            return panel.getSelectedValue();
        }else{
            return null;
        }
    }
    
}
