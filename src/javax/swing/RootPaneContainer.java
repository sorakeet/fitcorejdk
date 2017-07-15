/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import java.awt.*;

public interface RootPaneContainer{
    JRootPane getRootPane();

    Container getContentPane();

    void setContentPane(Container contentPane);

    JLayeredPane getLayeredPane();

    void setLayeredPane(JLayeredPane layeredPane);

    Component getGlassPane();

    void setGlassPane(Component glassPane);
}
