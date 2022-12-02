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
package rubensandreoli.drivescanner.gui;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import rubensandreoli.drivescanner.io.Folder;
import rubensandreoli.drivescanner.io.Scan;

public class TreePanel extends javax.swing.JPanel {

    private static final int CACHE_SIZE = 4;
    private static final DefaultTreeModel EMPTY_MODEL = new DefaultTreeModel(null);
    
    //<editor-fold defaultstate="collapsed" desc="CACHE">
    private class Cache extends LinkedHashMap<Scan, DefaultMutableTreeNode> {

        private final int size;
        
        public Cache(int size){
            super(4, 0.75f, true);
            this.size = size;
        }
        
        @Override
        public boolean removeEldestEntry(Map.Entry eldest) { //invoked by put and putAll after insert.
             return size() > size;
        }		  
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="NODE">
    private class Node extends DefaultMutableTreeNode{

        private boolean isLeaf;
        
        public Node(String name, boolean isLeaf) {
            super(name);
            this.isLeaf = isLeaf;
        }
        
        public Node(String name){
            this(name, false);
        }

        @Override
        public boolean isLeaf() { //TODO: extend renderer instead.
            return isLeaf;
        }
        
    }
    //</editor-fold>
    
    private final Cache cachedTree = new Cache(CACHE_SIZE); //FIX: what if scan changed? clear from cache or no cache at all.
    
    public TreePanel() {
        initComponents();
    }

    void setScan(Scan scan){ //TODO: ordered tree nodes.
        treFiles.setModel(new DefaultTreeModel(createTree(scan)));
    }
    
    void clear() {
        treFiles.setModel(EMPTY_MODEL);
    }
    
    private void addFilesToFolderNode(Folder folder, DefaultMutableTreeNode node){
        Set<String> filenames = folder.getFiles().keySet(); //does this improve performance or better to just use inside the loop?
        for (String filename : filenames) {
            node.add(new Node(filename, true));
        }
    }
    
    private DefaultMutableTreeNode createTree(Scan scan){ //TODO: try to do this extending TreeModel.
        if(cachedTree.containsKey(scan)){
            System.out.println("cached");
            return cachedTree.get(scan);
        }
        System.out.println("new");
        
        Map<String, DefaultMutableTreeNode> allNodes = new HashMap<>();
        String rootPath = scan.getDrive().getPath();
        var root =  new DefaultMutableTreeNode(rootPath);
        allNodes.put(rootPath, root);
        
        Set<Folder> folders = scan.getFolders(); //does this improve performance or better to just use inside the loop?
        for (Folder folder : folders) {
            File folderFile = folder.getFile();
            String folderPath = folderFile.getPath();
            var node = allNodes.get(folderPath);
            if(node == null){
                node = new Node(folderFile.getName());
                allNodes.put(folderPath, node);
                addFilesToFolderNode(folder, node);
            }else{
                addFilesToFolderNode(folder, node);
                continue;
            }
            
            while((folderFile = folderFile.getParentFile()) != null){
                folderPath = folderFile.getPath();
                var parentNode = allNodes.get(folderPath);
                if(parentNode == null){
                    parentNode = new Node(folderFile.getName());
                    allNodes.put(folderPath, parentNode);
                    parentNode.add(node);
                    node = parentNode;
                }else{
                    parentNode.add(node);
                    break;
                }
                
            }
            
        }
        cachedTree.put(scan, root);
        return root;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrTree = new javax.swing.JScrollPane();
        treFiles = new javax.swing.JTree();

        setLayout(new java.awt.BorderLayout());

        scrTree.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        treFiles.setModel(EMPTY_MODEL);
        scrTree.setViewportView(treFiles);

        add(scrTree, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane scrTree;
    private javax.swing.JTree treFiles;
    // End of variables declaration//GEN-END:variables
}
