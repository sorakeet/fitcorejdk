/**
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.datatransfer;

import java.util.EventListener;

public interface FlavorListener extends EventListener{
    void flavorsChanged(FlavorEvent e);
}
