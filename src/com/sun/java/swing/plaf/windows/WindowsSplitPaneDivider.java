/**
 * Copyright (c) 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.java.swing.plaf.windows;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;

public class WindowsSplitPaneDivider extends BasicSplitPaneDivider{
    public WindowsSplitPaneDivider(BasicSplitPaneUI ui){
        super(ui);
    }

    public void paint(Graphics g){
        Color bgColor=(splitPane.hasFocus())?
                UIManager.getColor("SplitPane.shadow"):
                getBackground();
        Dimension size=getSize();
        if(bgColor!=null){
            g.setColor(bgColor);
            g.fillRect(0,0,size.width,size.height);
        }
        super.paint(g);
    }
}
