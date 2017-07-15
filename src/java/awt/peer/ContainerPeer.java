/**
 * Copyright (c) 1995, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.peer;

import java.awt.*;

public interface ContainerPeer extends ComponentPeer{
    Insets getInsets();

    void beginValidate();

    void endValidate();

    void beginLayout();

    void endLayout();
}
