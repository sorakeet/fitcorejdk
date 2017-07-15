/**
 * Copyright (c) 1998, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.event;

import java.awt.*;
import java.util.EventListener;

public interface AWTEventListener extends EventListener{
    public void eventDispatched(AWTEvent event);
}
