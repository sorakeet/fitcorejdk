/**
 * Copyright (c) 2002, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.synth;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicMenuBarUI;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class SynthMenuBarUI extends BasicMenuBarUI
        implements PropertyChangeListener, SynthUI{
    private SynthStyle style;

    public static ComponentUI createUI(JComponent x){
        return new SynthMenuBarUI();
    }

    @Override
    protected void installDefaults(){
        if(menuBar.getLayout()==null||
                menuBar.getLayout() instanceof UIResource){
            menuBar.setLayout(new SynthMenuLayout(menuBar,BoxLayout.LINE_AXIS));
        }
        updateStyle(menuBar);
    }

    @Override
    protected void installListeners(){
        super.installListeners();
        menuBar.addPropertyChangeListener(this);
    }

    @Override
    protected void uninstallDefaults(){
        SynthContext context=getContext(menuBar,ENABLED);
        style.uninstallDefaults(context);
        context.dispose();
        style=null;
    }

    @Override
    protected void uninstallListeners(){
        super.uninstallListeners();
        menuBar.removePropertyChangeListener(this);
    }

    private void updateStyle(JMenuBar c){
        SynthContext context=getContext(c,ENABLED);
        SynthStyle oldStyle=style;
        style=SynthLookAndFeel.updateStyle(context,this);
        if(style!=oldStyle){
            if(oldStyle!=null){
                uninstallKeyboardActions();
                installKeyboardActions();
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
        context.getPainter().paintMenuBarBackground(context,
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
        context.getPainter().paintMenuBarBorder(context,g,x,y,w,h);
    }

    protected void paint(SynthContext context,Graphics g){
    }

    @Override
    public void propertyChange(PropertyChangeEvent e){
        if(SynthLookAndFeel.shouldUpdateStyle(e)){
            updateStyle((JMenuBar)e.getSource());
        }
    }
}
