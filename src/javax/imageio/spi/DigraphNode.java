/**
 * Copyright (c) 2000, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.imageio.spi;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

class DigraphNode implements Cloneable, Serializable{
    protected Object data;
    protected Set outNodes=new HashSet();
    protected int inDegree=0;
    private Set inNodes=new HashSet();

    public DigraphNode(Object data){
        this.data=data;
    }

    public Object getData(){
        return data;
    }

    public Iterator getOutNodes(){
        return outNodes.iterator();
    }

    public boolean addEdge(DigraphNode node){
        if(outNodes.contains(node)){
            return false;
        }
        outNodes.add(node);
        node.inNodes.add(this);
        node.incrementInDegree();
        return true;
    }

    public boolean hasEdge(DigraphNode node){
        return outNodes.contains(node);
    }

    public void dispose(){
        Object[] inNodesArray=inNodes.toArray();
        for(int i=0;i<inNodesArray.length;i++){
            DigraphNode node=(DigraphNode)inNodesArray[i];
            node.removeEdge(this);
        }
        Object[] outNodesArray=outNodes.toArray();
        for(int i=0;i<outNodesArray.length;i++){
            DigraphNode node=(DigraphNode)outNodesArray[i];
            removeEdge(node);
        }
    }

    public boolean removeEdge(DigraphNode node){
        if(!outNodes.contains(node)){
            return false;
        }
        outNodes.remove(node);
        node.inNodes.remove(this);
        node.decrementInDegree();
        return true;
    }

    public int getInDegree(){
        return inDegree;
    }

    private void incrementInDegree(){
        ++inDegree;
    }

    private void decrementInDegree(){
        --inDegree;
    }
}
