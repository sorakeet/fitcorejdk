/**
 * Copyright (c) 1997, 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import java.awt.*;

public interface Icon{
    void paintIcon(Component c,Graphics g,int x,int y);

    int getIconWidth();

    int getIconHeight();
}
