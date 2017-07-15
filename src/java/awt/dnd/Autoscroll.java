/**
 * Copyright (c) 1998, 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.dnd;

import java.awt.*;

public interface Autoscroll{
    public Insets getAutoscrollInsets();

    public void autoscroll(Point cursorLocn);
}
