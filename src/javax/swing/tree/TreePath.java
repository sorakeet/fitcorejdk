/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.tree;

import java.beans.ConstructorProperties;
import java.io.Serializable;

public class TreePath extends Object implements Serializable{
    private TreePath parentPath;
    private Object lastPathComponent;

    @ConstructorProperties({"path"})
    public TreePath(Object[] path){
        if(path==null||path.length==0)
            throw new IllegalArgumentException("path in TreePath must be non null and not empty.");
        lastPathComponent=path[path.length-1];
        if(lastPathComponent==null){
            throw new IllegalArgumentException(
                    "Last path component must be non-null");
        }
        if(path.length>1)
            parentPath=new TreePath(path,path.length-1);
    }

    public TreePath(Object lastPathComponent){
        if(lastPathComponent==null)
            throw new IllegalArgumentException("path in TreePath must be non null.");
        this.lastPathComponent=lastPathComponent;
        parentPath=null;
    }

    protected TreePath(TreePath parent,Object lastPathComponent){
        if(lastPathComponent==null)
            throw new IllegalArgumentException("path in TreePath must be non null.");
        parentPath=parent;
        this.lastPathComponent=lastPathComponent;
    }

    protected TreePath(Object[] path,int length){
        lastPathComponent=path[length-1];
        if(lastPathComponent==null){
            throw new IllegalArgumentException(
                    "Path elements must be non-null");
        }
        if(length>1)
            parentPath=new TreePath(path,length-1);
    }

    protected TreePath(){
    }

    public Object[] getPath(){
        int i=getPathCount();
        Object[] result=new Object[i--];
        for(TreePath path=this;path!=null;path=path.getParentPath()){
            result[i--]=path.getLastPathComponent();
        }
        return result;
    }

    public int getPathCount(){
        int result=0;
        for(TreePath path=this;path!=null;path=path.getParentPath()){
            result++;
        }
        return result;
    }

    public int hashCode(){
        return getLastPathComponent().hashCode();
    }

    public Object getLastPathComponent(){
        return lastPathComponent;
    }

    public boolean equals(Object o){
        if(o==this)
            return true;
        if(o instanceof TreePath){
            TreePath oTreePath=(TreePath)o;
            if(getPathCount()!=oTreePath.getPathCount())
                return false;
            for(TreePath path=this;path!=null;
                path=path.getParentPath()){
                if(!(path.getLastPathComponent().equals
                        (oTreePath.getLastPathComponent()))){
                    return false;
                }
                oTreePath=oTreePath.getParentPath();
            }
            return true;
        }
        return false;
    }

    public String toString(){
        StringBuffer tempSpot=new StringBuffer("[");
        for(int counter=0, maxCounter=getPathCount();counter<maxCounter;
            counter++){
            if(counter>0)
                tempSpot.append(", ");
            tempSpot.append(getPathComponent(counter));
        }
        tempSpot.append("]");
        return tempSpot.toString();
    }

    public Object getPathComponent(int index){
        int pathLength=getPathCount();
        if(index<0||index>=pathLength)
            throw new IllegalArgumentException("Index "+index+
                    " is out of the specified range");
        TreePath path=this;
        for(int i=pathLength-1;i!=index;i--){
            path=path.getParentPath();
        }
        return path.getLastPathComponent();
    }

    public boolean isDescendant(TreePath aTreePath){
        if(aTreePath==this)
            return true;
        if(aTreePath!=null){
            int pathLength=getPathCount();
            int oPathLength=aTreePath.getPathCount();
            if(oPathLength<pathLength)
                // Can't be a descendant, has fewer components in the path.
                return false;
            while(oPathLength-->pathLength)
                aTreePath=aTreePath.getParentPath();
            return equals(aTreePath);
        }
        return false;
    }

    public TreePath pathByAddingChild(Object child){
        if(child==null)
            throw new NullPointerException("Null child not allowed");
        return new TreePath(this,child);
    }

    public TreePath getParentPath(){
        return parentPath;
    }
}
