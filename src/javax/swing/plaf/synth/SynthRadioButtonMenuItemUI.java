/**
 * Copyright (c) 2002, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.synth;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;

public class SynthRadioButtonMenuItemUI extends SynthMenuItemUI{
    public static ComponentUI createUI(JComponent b){
        return new SynthRadioButtonMenuItemUI();
    }

    @Override
    protected String getPropertyPrefix(){
        return "RadioButtonMenuItem";
    }

    @Override
    void paintBackground(SynthContext context,Graphics g,JComponent c){
        context.getPainter().paintRadioButtonMenuItemBackground(context,g,0,
                0,c.getWidth(),c.getHeight());
    }

    @Override
    public void paintBorder(SynthContext context,Graphics g,int x,
                            int y,int w,int h){
        context.getPainter().paintRadioButtonMenuItemBorder(context,g,x,
                y,w,h);
    }
}
