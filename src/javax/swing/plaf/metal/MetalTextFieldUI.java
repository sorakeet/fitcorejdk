/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.metal;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTextFieldUI;
import java.beans.PropertyChangeEvent;

public class MetalTextFieldUI extends BasicTextFieldUI{
    public static ComponentUI createUI(JComponent c){
        return new MetalTextFieldUI();
    }

    public void propertyChange(PropertyChangeEvent evt){
        super.propertyChange(evt);
    }
}
