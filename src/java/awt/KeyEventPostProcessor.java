/**
 * Copyright (c) 2001, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import java.awt.event.KeyEvent;

@FunctionalInterface
public interface KeyEventPostProcessor{
    boolean postProcessKeyEvent(KeyEvent e);
}
