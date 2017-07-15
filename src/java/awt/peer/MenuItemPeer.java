/**
 * Copyright (c) 1995, 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.peer;

public interface MenuItemPeer extends MenuComponentPeer{
    void setLabel(String label);

    void setEnabled(boolean e);
}
