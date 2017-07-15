/**
 * Copyright (c) 1997, 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.event;

import java.util.EventListener;

public interface CellEditorListener extends EventListener{
    public void editingStopped(ChangeEvent e);

    public void editingCanceled(ChangeEvent e);
}
