/**
 * Copyright (c) 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.event;

import java.util.EventListener;

public interface WindowStateListener extends EventListener{
    public void windowStateChanged(WindowEvent e);
}
