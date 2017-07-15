/**
 * Copyright (c) 1998, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.colorchooser;

import javax.swing.event.ChangeListener;
import java.awt.*;

public interface ColorSelectionModel{
    Color getSelectedColor();

    void setSelectedColor(Color color);

    void addChangeListener(ChangeListener listener);

    void removeChangeListener(ChangeListener listener);
}
