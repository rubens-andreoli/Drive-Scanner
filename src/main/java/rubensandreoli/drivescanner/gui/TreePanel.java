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

import java.awt.Component;
import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import rubensandreoli.drivescanner.gui.support.IconLoader;
import rubensandreoli.drivescanner.io.Folder;
import rubensandreoli.drivescanner.io.Scan;

public class TreePanel extends javax.swing.JPanel {
    
    //<editor-fold defaultstate="collapsed" desc="TREE RENDERER">
    private static class ScanTreeCellRenderer extends DefaultTreeCellRenderer{

        private boolean isIncluded;
        private boolean isFolder;
        
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Node node = ((Node)value);
            isIncluded = node.included;
            isFolder = node.folder;
            return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        }
    
        private Icon getIncludedIcon(Icon icon){
            if(isIncluded) return icon;
            return IconLoader.applyAlpha(icon, 0.4f);
        }
        
        @Override
        public Icon getClosedIcon() {           
            return getIncludedIcon(super.getClosedIcon());
        }

        @Override
        public Icon getOpenIcon() {
            return getIncludedIcon(super.getOpenIcon());
        }

        @Override
        public Icon getLeafIcon() {
            if(isFolder) return getClosedIcon();
            return super.getLeafIcon();
        }
        
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="CACHE">
    private static class Cache extends LinkedHashMap<Scan, DefaultTreeModel> {

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
    private static class Node extends DefaultMutableTreeNode{

        private static final Comparator<Node> NODE_COMPARATOR = (n1, n2) -> {
            if((n1.folder && n2.folder) || (!n1.folder && !n2.folder)) return n1.toString().compareToIgnoreCase(n2.toString());
            else return n1.folder? -1:1; //folders first.
        };
        
        private final boolean included;
        private final boolean folder;
        private TreeSet<Node> childrenSet; //temporary storage to avoid sorting every time.
        
        public Node(String name){
            this(name, true, true);
        }

        public Node(String name, boolean included, boolean folder){
            super(name);
            this.included = included;
            this.folder = folder;
            childrenSet = new TreeSet<>(NODE_COMPARATOR);
        }

        public void addToBuild(Node newChild) {
            assert newChild != null: "cannot add children after node has been built";
            childrenSet.add(newChild);
        }
        
        private void build(){ //build it to really add the children.
            for (MutableTreeNode mutableTreeNode : childrenSet) {
                super.add(mutableTreeNode);
            }
            childrenSet = null;
        }

    }
    //</editor-fold>
    
    private static final DefaultTreeModel EMPTY_MODEL = new DefaultTreeModel(null);

    private final Cache cachedTree = new Cache(5);
    
    public TreePanel() {
        initComponents();
        treFiles.setCellRenderer(new ScanTreeCellRenderer());
    }

    void setScan(Scan scan){
        if(cachedTree.containsKey(scan)){
            treFiles.setModel(cachedTree.get(scan));
        }else{
            var newModel = new DefaultTreeModel(createTree(scan));
            cachedTree.put(scan, newModel);
            treFiles.setModel(newModel);
        }
    }
    
    void invalidateCache(Scan scan){
        cachedTree.remove(scan);
    }
    
    void invalidadeCache(){
        cachedTree.clear();
    }
    
    void clear() {
        treFiles.setModel(EMPTY_MODEL);
    }
    
    private void addFilesToFolderNode(Folder folder, Node node){
        Set<String> filenames = folder.getFiles().keySet(); //does this improve performance or better to just use inside the loop?
        for (String filename : filenames) {
            node.addToBuild(new Node(filename, true, false));
        }
    }
    
    private DefaultMutableTreeNode createTree(Scan scan){
        Map<String, Node> allNodes = new HashMap<>();
        String rootPath = scan.getDrive().getPath();
        var root =  new Node(rootPath, false, true);
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
                    parentNode = new Node(folderFile.getName(), false, true);
                    allNodes.put(folderPath, parentNode);
                    parentNode.addToBuild(node);
                    node = parentNode;
                }else{
                    parentNode.addToBuild(node);
                    break;
                }
                
            }
            
        }
        
        for (Node value : allNodes.values()) {
            value.build();
        }
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
