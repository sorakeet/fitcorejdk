/**
 * Copyright (c) 2002, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.synth;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicPopupMenuUI;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class SynthPopupMenuUI extends BasicPopupMenuUI
        implements PropertyChangeListener, SynthUI{
    private SynthStyle style;

    public static ComponentUI createUI(JComponent x){
        return new SynthPopupMenuUI();
    }

    @Override
    public void installDefaults(){
        if(popupMenu.getLayout()==null||
                popupMenu.getLayout() instanceof UIResource){
            popupMenu.setLayout(new SynthMenuLayout(popupMenu,BoxLayout.Y_AXIS));
        }
        updateStyle(popupMenu);
    }

    private void updateStyle(JComponent c){
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
    protected void installListeners(){
        super.installListeners();
        popupMenu.addPropertyChangeListener(this);
    }

    @Override
    protected void uninstallDefaults(){
        SynthContext context=getContext(popupMenu,ENABLED);
        style.uninstallDefaults(context);
        context.dispose();
        style=null;
        if(popupMenu.getLayout() instanceof UIResource){
            popupMenu.setLayout(null);
        }
    }

    @Override
    protected void uninstallListeners(){
        super.uninstallListeners();
        popupMenu.removePropertyChangeListener(this);
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
        context.getPainter().paintPopupMenuBackground(context,
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
        context.getPainter().paintPopupMenuBorder(context,g,x,y,w,h);
    }

    protected void paint(SynthContext context,Graphics g){
    }

    @Override
    public void propertyChange(PropertyChangeEvent e){
        if(SynthLookAndFeel.shouldUpdateStyle(e)){
            updateStyle(popupMenu);
        }
    }
}
