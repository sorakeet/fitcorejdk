/**
 * Copyright (c) 1995, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.peer;

import java.awt.*;

public interface CanvasPeer extends ComponentPeer{
    GraphicsConfiguration getAppropriateGraphicsConfiguration(
            GraphicsConfiguration gc);
}
