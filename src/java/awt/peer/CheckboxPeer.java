/**
 * Copyright (c) 1995, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.peer;

import java.awt.*;

public interface CheckboxPeer extends ComponentPeer{
    void setState(boolean state);

    void setCheckboxGroup(CheckboxGroup g);

    void setLabel(String label);
}
