/**
 * Copyright (c) 2002, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.synth;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicPanelUI;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class SynthPanelUI extends BasicPanelUI
        implements PropertyChangeListener, SynthUI{
    private SynthStyle style;

    public static ComponentUI createUI(JComponent c){
        return new SynthPanelUI();
    }

    @Override
    public void installUI(JComponent c){
        JPanel p=(JPanel)c;
        super.installUI(c);
        installListeners(p);
    }

    @Override
    public void uninstallUI(JComponent c){
        JPanel p=(JPanel)c;
        uninstallListeners(p);
        super.uninstallUI(c);
    }

    protected void uninstallListeners(JPanel p){
        p.removePropertyChangeListener(this);
    }

    @Override
    protected void installDefaults(JPanel p){
        updateStyle(p);
    }

    @Override
    protected void uninstallDefaults(JPanel p){
        SynthContext context=getContext(p,ENABLED);
        style.uninstallDefaults(context);
        context.dispose();
        style=null;
    }

    private void updateStyle(JPanel c){
        SynthContext context=getContext(c,ENABLED);
        style=SynthLookAndFeel.updateStyle(context,this);
        context.dispose();
    }

    private SynthContext getContext(JComponent c,int state){
        return SynthContext.getContext(c,style,state);
    }

    protected void installListeners(JPanel p){
        p.addPropertyChangeListener(this);
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
        context.getPainter().paintPanelBackground(context,
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
        context.getPainter().paintPanelBorder(context,g,x,y,w,h);
    }

    protected void paint(SynthContext context,Graphics g){
        // do actual painting
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce){
        if(SynthLookAndFeel.shouldUpdateStyle(pce)){
            updateStyle((JPanel)pce.getSource());
        }
    }
}
