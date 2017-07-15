/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.swing.event.ChangeListener;

public interface SpinnerModel{
    Object getValue();

    void setValue(Object value);

    Object getNextValue();

    Object getPreviousValue();

    void addChangeListener(ChangeListener l);

    void removeChangeListener(ChangeListener l);
}
