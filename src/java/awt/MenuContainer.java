/**
 * Copyright (c) 1995, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

public interface MenuContainer{
    Font getFont();

    void remove(MenuComponent comp);

    @Deprecated
    boolean postEvent(Event evt);
}
