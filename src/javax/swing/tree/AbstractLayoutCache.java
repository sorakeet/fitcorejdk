/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.tree;

import javax.swing.event.TreeModelEvent;
import java.awt.*;
import java.util.Enumeration;

public abstract class AbstractLayoutCache implements RowMapper{
    protected NodeDimensions nodeDimensions;
    protected TreeModel treeModel;
    protected TreeSelectionModel treeSelectionModel;
    protected boolean rootVisible;
    protected int rowHeight;

    public TreeModel getModel(){
        return treeModel;
    }

    public void setModel(TreeModel newModel){
        treeModel=newModel;
    }

    public boolean isRootVisible(){
        return rootVisible;
    }

    public void setRootVisible(boolean rootVisible){
        this.rootVisible=rootVisible;
    }

    public int getRowHeight(){
        return rowHeight;
    }

    public void setRowHeight(int rowHeight){
        this.rowHeight=rowHeight;
    }

    public TreeSelectionModel getSelectionModel(){
        return treeSelectionModel;
    }

    public void setSelectionModel(TreeSelectionModel newLSM){
        if(treeSelectionModel!=null)
            treeSelectionModel.setRowMapper(null);
        treeSelectionModel=newLSM;
        if(treeSelectionModel!=null)
            treeSelectionModel.setRowMapper(this);
    }

    public int getPreferredHeight(){
        // Get the height
        int rowCount=getRowCount();
        if(rowCount>0){
            Rectangle bounds=getBounds(getPathForRow(rowCount-1),
                    null);
            if(bounds!=null)
                return bounds.y+bounds.height;
        }
        return 0;
    }

    public abstract Rectangle getBounds(TreePath path,Rectangle placeIn);

    public abstract TreePath getPathForRow(int row);

    public abstract int getRowCount();
    //
    // Abstract methods that must be implemented to be concrete.
    //

    public int getPreferredWidth(Rectangle bounds){
        int rowCount=getRowCount();
        if(rowCount>0){
            // Get the width
            TreePath firstPath;
            int endY;
            if(bounds==null){
                firstPath=getPathForRow(0);
                endY=Integer.MAX_VALUE;
            }else{
                firstPath=getPathClosestTo(bounds.x,bounds.y);
                endY=bounds.height+bounds.y;
            }
            Enumeration paths=getVisiblePathsFrom(firstPath);
            if(paths!=null&&paths.hasMoreElements()){
                Rectangle pBounds=getBounds((TreePath)paths.nextElement(),
                        null);
                int width;
                if(pBounds!=null){
                    width=pBounds.x+pBounds.width;
                    if(pBounds.y>=endY){
                        return width;
                    }
                }else
                    width=0;
                while(pBounds!=null&&paths.hasMoreElements()){
                    pBounds=getBounds((TreePath)paths.nextElement(),
                            pBounds);
                    if(pBounds!=null&&pBounds.y<endY){
                        width=Math.max(width,pBounds.x+pBounds.width);
                    }else{
                        pBounds=null;
                    }
                }
                return width;
            }
        }
        return 0;
    }

    public abstract TreePath getPathClosestTo(int x,int y);

    public abstract Enumeration<TreePath> getVisiblePathsFrom(TreePath path);

    public abstract boolean isExpanded(TreePath path);

    public abstract int getVisibleChildCount(TreePath path);

    public abstract void setExpandedState(TreePath path,boolean isExpanded);

    public abstract boolean getExpandedState(TreePath path);

    public abstract void invalidateSizes();

    public abstract void invalidatePathBounds(TreePath path);

    public abstract void treeNodesChanged(TreeModelEvent e);

    public abstract void treeNodesInserted(TreeModelEvent e);

    public abstract void treeNodesRemoved(TreeModelEvent e);
    //
    // TreeModelListener methods
    // AbstractTreeState does not directly become a TreeModelListener on
    // the model, it is up to some other object to forward these methods.
    //

    public abstract void treeStructureChanged(TreeModelEvent e);

    public int[] getRowsForPaths(TreePath[] paths){
        if(paths==null)
            return null;
        int numPaths=paths.length;
        int[] rows=new int[numPaths];
        for(int counter=0;counter<numPaths;counter++)
            rows[counter]=getRowForPath(paths[counter]);
        return rows;
    }

    public abstract int getRowForPath(TreePath path);

    protected Rectangle getNodeDimensions(Object value,int row,int depth,
                                          boolean expanded,
                                          Rectangle placeIn){
        NodeDimensions nd=getNodeDimensions();
        if(nd!=null){
            return nd.getNodeDimensions(value,row,depth,expanded,placeIn);
        }
        return null;
    }
    //
    // RowMapper
    //

    public NodeDimensions getNodeDimensions(){
        return nodeDimensions;
    }
    //
    // Local methods that subclassers may wish to use that are primarly
    // convenience methods.
    //

    public void setNodeDimensions(NodeDimensions nd){
        this.nodeDimensions=nd;
    }

    protected boolean isFixedRowHeight(){
        return (rowHeight>0);
    }

    static public abstract class NodeDimensions{
        public abstract Rectangle getNodeDimensions(Object value,int row,
                                                    int depth,
                                                    boolean expanded,
                                                    Rectangle bounds);
    }
}
