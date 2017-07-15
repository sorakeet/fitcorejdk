/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.event;

import java.util.EventListener;

public interface FocusListener extends EventListener{
    public void focusGained(FocusEvent e);

    public void focusLost(FocusEvent e);
}
