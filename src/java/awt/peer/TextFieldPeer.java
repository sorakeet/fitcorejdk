/**
 * Copyright (c) 1995, 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.peer;

import java.awt.*;

public interface TextFieldPeer extends TextComponentPeer{
    void setEchoChar(char echoChar);

    Dimension getPreferredSize(int columns);

    Dimension getMinimumSize(int columns);
}
