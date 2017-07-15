/**
 * Copyright (c) 1995, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

public interface LayoutManager{
    void addLayoutComponent(String name,Component comp);

    void removeLayoutComponent(Component comp);

    Dimension preferredLayoutSize(Container parent);

    Dimension minimumLayoutSize(Container parent);

    void layoutContainer(Container parent);
}
