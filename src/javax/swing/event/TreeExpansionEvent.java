/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.event;

import javax.swing.tree.TreePath;
import java.util.EventObject;

public class TreeExpansionEvent extends EventObject{
    protected TreePath path;

    public TreeExpansionEvent(Object source,TreePath path){
        super(source);
        this.path=path;
    }

    public TreePath getPath(){
        return path;
    }
}
