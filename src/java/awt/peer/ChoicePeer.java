/**
 * Copyright (c) 1995, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.peer;

public interface ChoicePeer extends ComponentPeer{
    void add(String item,int index);

    void remove(int index);

    void removeAll();

    void select(int index);
}
