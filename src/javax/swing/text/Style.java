/**
 * Copyright (c) 1997, 2000, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

import javax.swing.event.ChangeListener;

public interface Style extends MutableAttributeSet{
    public String getName();

    public void addChangeListener(ChangeListener l);

    public void removeChangeListener(ChangeListener l);
}
