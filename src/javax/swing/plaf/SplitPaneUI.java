/**
 * Copyright (c) 1997, 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf;

import javax.swing.*;
import java.awt.*;

public abstract class SplitPaneUI extends ComponentUI{
    public abstract void resetToPreferredSizes(JSplitPane jc);

    public abstract void setDividerLocation(JSplitPane jc,int location);

    public abstract int getDividerLocation(JSplitPane jc);

    public abstract int getMinimumDividerLocation(JSplitPane jc);

    public abstract int getMaximumDividerLocation(JSplitPane jc);

    public abstract void finishedPaintingChildren(JSplitPane jc,Graphics g);
}
