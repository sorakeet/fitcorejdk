/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;

public interface Action extends ActionListener{
    public static final String DEFAULT="Default";
    public static final String NAME="Name";
    public static final String SHORT_DESCRIPTION="ShortDescription";
    public static final String LONG_DESCRIPTION="LongDescription";
    public static final String SMALL_ICON="SmallIcon";
    public static final String ACTION_COMMAND_KEY="ActionCommandKey";
    public static final String ACCELERATOR_KEY="AcceleratorKey";
    public static final String MNEMONIC_KEY="MnemonicKey";
    public static final String SELECTED_KEY="SwingSelectedKey";
    public static final String DISPLAYED_MNEMONIC_INDEX_KEY=
            "SwingDisplayedMnemonicIndexKey";
    public static final String LARGE_ICON_KEY="SwingLargeIconKey";

    public Object getValue(String key);

    public void putValue(String key,Object value);

    public boolean isEnabled();

    public void setEnabled(boolean b);

    public void addPropertyChangeListener(PropertyChangeListener listener);

    public void removePropertyChangeListener(PropertyChangeListener listener);
}
