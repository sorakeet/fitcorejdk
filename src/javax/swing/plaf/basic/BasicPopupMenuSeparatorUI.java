/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.basic;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;

public class BasicPopupMenuSeparatorUI extends BasicSeparatorUI{
    public static ComponentUI createUI(JComponent c){
        return new BasicPopupMenuSeparatorUI();
    }

    public void paint(Graphics g,JComponent c){
        Dimension s=c.getSize();
        g.setColor(c.getForeground());
        g.drawLine(0,0,s.width,0);
        g.setColor(c.getBackground());
        g.drawLine(0,1,s.width,1);
    }

    public Dimension getPreferredSize(JComponent c){
        return new Dimension(0,2);
    }
}
