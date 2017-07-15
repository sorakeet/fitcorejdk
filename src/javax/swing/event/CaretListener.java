/**
 * Copyright (c) 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.event;

import java.util.EventListener;

public interface CaretListener extends EventListener{
    void caretUpdate(CaretEvent e);
}
