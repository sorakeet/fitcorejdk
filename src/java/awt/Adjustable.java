/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import java.awt.event.AdjustmentListener;
import java.lang.annotation.Native;

public interface Adjustable{
    @Native
    public static final int HORIZONTAL=0;
    @Native
    public static final int VERTICAL=1;
    @Native
    public static final int NO_ORIENTATION=2;

    int getOrientation();

    int getMinimum();

    void setMinimum(int min);

    int getMaximum();

    void setMaximum(int max);

    int getUnitIncrement();

    void setUnitIncrement(int u);

    int getBlockIncrement();

    void setBlockIncrement(int b);

    int getVisibleAmount();

    void setVisibleAmount(int v);

    int getValue();

    void setValue(int v);

    void addAdjustmentListener(AdjustmentListener l);

    void removeAdjustmentListener(AdjustmentListener l);
}
