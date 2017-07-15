/**
 * Copyright (c) 1995, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.peer;

import java.awt.*;

public interface TextAreaPeer extends TextComponentPeer{
    void insert(String text,int pos);

    void replaceRange(String text,int start,int end);

    Dimension getPreferredSize(int rows,int columns);

    Dimension getMinimumSize(int rows,int columns);
}
