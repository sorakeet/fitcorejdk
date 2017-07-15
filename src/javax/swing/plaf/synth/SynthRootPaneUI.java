/**
 * Copyright (c) 2002, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.synth;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicRootPaneUI;
import java.awt.*;
import java.beans.PropertyChangeEvent;

public class SynthRootPaneUI extends BasicRootPaneUI implements SynthUI{
    private SynthStyle style;

    public static ComponentUI createUI(JComponent c){
        return new SynthRootPaneUI();
    }

    @Override
    protected void installDefaults(JRootPane c){
        updateStyle(c);
    }

    @Override
    protected void uninstallDefaults(JRootPane root){
        SynthContext context=getContext(root,ENABLED);
        style.uninstallDefaults(context);
        context.dispose();
        style=null;
    }

    @Override
    public void propertyChange(PropertyChangeEvent e){
        if(SynthLookAndFeel.shouldUpdateStyle(e)){
            updateStyle((JRootPane)e.getSource());
        }
        super.propertyChange(e);
    }

    private void updateStyle(JComponent c){
        SynthContext context=getContext(c,ENABLED);
        SynthStyle oldStyle=style;
        style=SynthLookAndFeel.updateStyle(context,this);
        if(style!=oldStyle){
            if(oldStyle!=null){
                uninstallKeyboardActions((JRootPane)c);
                installKeyboardActions((JRootPane)c);
            }
        }
        context.dispose();
    }

    private SynthContext getContext(JComponent c,int state){
        return SynthContext.getContext(c,style,state);
    }

    @Override
    public void paint(Graphics g,JComponent c){
        SynthContext context=getContext(c);
        paint(context,g);
        context.dispose();
    }

    @Override
    public void update(Graphics g,JComponent c){
        SynthContext context=getContext(c);
        SynthLookAndFeel.update(context,g);
        context.getPainter().paintRootPaneBackground(context,
                g,0,0,c.getWidth(),c.getHeight());
        paint(context,g);
        context.dispose();
    }

    @Override
    public SynthContext getContext(JComponent c){
        return getContext(c,getComponentState(c));
    }

    private int getComponentState(JComponent c){
        return SynthLookAndFeel.getComponentState(c);
    }

    @Override
    public void paintBorder(SynthContext context,Graphics g,int x,
                            int y,int w,int h){
        context.getPainter().paintRootPaneBorder(context,g,x,y,w,h);
    }

    protected void paint(SynthContext context,Graphics g){
    }
}
