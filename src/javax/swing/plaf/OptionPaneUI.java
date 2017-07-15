/**
 * Copyright (c) 1997, 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf;

import javax.swing.*;

public abstract class OptionPaneUI extends ComponentUI{
    public abstract void selectInitialValue(JOptionPane op);

    public abstract boolean containsCustomComponents(JOptionPane op);
}
