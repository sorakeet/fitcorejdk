/**
 * Copyright (c) 1997, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf;

import javax.swing.*;
import java.awt.*;

public abstract class ListUI extends ComponentUI{
    public abstract int locationToIndex(JList list,Point location);

    public abstract Point indexToLocation(JList list,int index);

    public abstract Rectangle getCellBounds(JList list,int index1,int index2);
}
