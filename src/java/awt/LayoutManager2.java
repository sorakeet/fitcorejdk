/**
 * Copyright (c) 1996, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

public interface LayoutManager2 extends LayoutManager{
    void addLayoutComponent(Component comp,Object constraints);

    public Dimension maximumLayoutSize(Container target);

    public float getLayoutAlignmentX(Container target);

    public float getLayoutAlignmentY(Container target);

    public void invalidateLayout(Container target);
}
