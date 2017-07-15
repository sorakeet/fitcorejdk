/**
 * Copyright (c) 1997, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import java.awt.*;

public interface Scrollable{
    Dimension getPreferredScrollableViewportSize();

    int getScrollableUnitIncrement(Rectangle visibleRect,int orientation,int direction);

    int getScrollableBlockIncrement(Rectangle visibleRect,int orientation,int direction);

    boolean getScrollableTracksViewportWidth();

    boolean getScrollableTracksViewportHeight();
}
