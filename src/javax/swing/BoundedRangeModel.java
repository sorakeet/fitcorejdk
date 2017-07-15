/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.swing.event.ChangeListener;

public interface BoundedRangeModel{
    int getMinimum();

    void setMinimum(int newMinimum);

    int getMaximum();

    void setMaximum(int newMaximum);

    int getValue();

    void setValue(int newValue);

    boolean getValueIsAdjusting();

    void setValueIsAdjusting(boolean b);

    int getExtent();

    void setExtent(int newExtent);

    void setRangeProperties(int value,int extent,int min,int max,boolean adjusting);

    void addChangeListener(ChangeListener x);

    void removeChangeListener(ChangeListener x);
}
