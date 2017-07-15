/**
 * Copyright (c) 1995, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.peer;

public interface LabelPeer extends ComponentPeer{
    void setText(String label);

    void setAlignment(int alignment);
}
