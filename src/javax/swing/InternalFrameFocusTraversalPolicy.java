/**
 * Copyright (c) 2000, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import java.awt.*;

public abstract class InternalFrameFocusTraversalPolicy
        extends FocusTraversalPolicy{
    public Component getInitialComponent(JInternalFrame frame){
        return getDefaultComponent(frame);
    }
}
