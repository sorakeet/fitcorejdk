/**
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.orbutil.graph;

public class NodeData{
    private boolean visited;
    private boolean root;

    public NodeData(){
        clear();
    }

    public void clear(){
        this.visited=false;
        this.root=true;
    }

    boolean isVisited(){
        return visited;
    }

    void visited(){
        visited=true;
    }

    boolean isRoot(){
        return root;
    }

    void notRoot(){
        root=false;
    }
}
