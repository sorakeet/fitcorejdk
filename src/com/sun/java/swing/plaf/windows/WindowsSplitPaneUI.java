/**
 * Copyright (c) 1997, 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.java.swing.plaf.windows;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

public class WindowsSplitPaneUI extends BasicSplitPaneUI{
    public WindowsSplitPaneUI(){
        super();
    }

    public static ComponentUI createUI(JComponent x){
        return new WindowsSplitPaneUI();
    }

    public BasicSplitPaneDivider createDefaultDivider(){
        return new WindowsSplitPaneDivider(this);
    }
}
