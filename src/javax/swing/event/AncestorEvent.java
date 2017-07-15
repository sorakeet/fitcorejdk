/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.event;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
public class AncestorEvent extends AWTEvent{
    public static final int ANCESTOR_ADDED=1;
    public static final int ANCESTOR_REMOVED=2;
    public static final int ANCESTOR_MOVED=3;
    Container ancestor;
    Container ancestorParent;

    public AncestorEvent(JComponent source,int id,Container ancestor,Container ancestorParent){
        super(source,id);
        this.ancestor=ancestor;
        this.ancestorParent=ancestorParent;
    }

    public Container getAncestor(){
        return ancestor;
    }

    public Container getAncestorParent(){
        return ancestorParent;
    }

    public JComponent getComponent(){
        return (JComponent)getSource();
    }
}
