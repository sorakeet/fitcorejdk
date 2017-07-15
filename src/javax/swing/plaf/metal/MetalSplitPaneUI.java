/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.metal;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

public class MetalSplitPaneUI extends BasicSplitPaneUI{
    public static ComponentUI createUI(JComponent x){
        return new MetalSplitPaneUI();
    }

    public BasicSplitPaneDivider createDefaultDivider(){
        return new MetalSplitPaneDivider(this);
    }
}
