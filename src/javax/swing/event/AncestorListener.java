/**
 * Copyright (c) 1997, 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.event;

import java.util.EventListener;

public interface AncestorListener extends EventListener{
    public void ancestorAdded(AncestorEvent event);

    public void ancestorRemoved(AncestorEvent event);

    public void ancestorMoved(AncestorEvent event);
}
