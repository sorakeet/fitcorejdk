/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.border;

import java.awt.*;

public interface Border{
    void paintBorder(Component c,Graphics g,int x,int y,int width,int height);

    Insets getBorderInsets(Component c);

    boolean isBorderOpaque();
}
