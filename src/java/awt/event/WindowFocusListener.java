/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.event;

import java.util.EventListener;

public interface WindowFocusListener extends EventListener{
    public void windowGainedFocus(WindowEvent e);

    public void windowLostFocus(WindowEvent e);
}
