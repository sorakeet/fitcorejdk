/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import java.awt.event.KeyEvent;

@FunctionalInterface
public interface KeyEventDispatcher{
    boolean dispatchKeyEvent(KeyEvent e);
}
