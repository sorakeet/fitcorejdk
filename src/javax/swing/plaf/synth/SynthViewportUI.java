/**
 * Copyright (c) 2002, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.synth;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.ViewportUI;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class SynthViewportUI extends ViewportUI
        implements PropertyChangeListener, SynthUI{
    private SynthStyle style;

    public static ComponentUI createUI(JComponent c){
        return new SynthViewportUI();
    }

    @Override
    public void installUI(JComponent c){
        super.installUI(c);
        installDefaults(c);
        installListeners(c);
    }

    @Override
    public void uninstallUI(JComponent c){
        super.uninstallUI(c);
        uninstallListeners(c);
        uninstallDefaults(c);
    }

    protected void uninstallListeners(JComponent c){
        c.removePropertyChangeListener(this);
    }

    protected void uninstallDefaults(JComponent c){
        SynthContext context=getContext(c,ENABLED);
        style.uninstallDefaults(context);
        context.dispose();
        style=null;
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
        context.getPainter().paintViewportBackground(context,
                g,0,0,c.getWidth(),c.getHeight());
        paint(context,g);
        context.dispose();
    }

    @Override
    public SynthContext getContext(JComponent c){
        return getContext(c,SynthLookAndFeel.getComponentState(c));
    }

    @Override
    public void paintBorder(SynthContext context,Graphics g,int x,
                            int y,int w,int h){
    }

    protected void paint(SynthContext context,Graphics g){
    }

    protected void installDefaults(JComponent c){
        updateStyle(c);
    }

    private void updateStyle(JComponent c){
        SynthContext context=getContext(c,ENABLED);
        // Note: JViewport is special cased as it does not allow for
        // a border to be set. JViewport.setBorder is overriden to throw
        // an IllegalArgumentException. Refer to SynthScrollPaneUI for
        // details of this.
        SynthStyle newStyle=SynthLookAndFeel.getStyle(context.getComponent(),
                context.getRegion());
        SynthStyle oldStyle=context.getStyle();
        if(newStyle!=oldStyle){
            if(oldStyle!=null){
                oldStyle.uninstallDefaults(context);
            }
            context.setStyle(newStyle);
            newStyle.installDefaults(context);
        }
        this.style=newStyle;
        context.dispose();
    }

    private SynthContext getContext(JComponent c,int state){
        return SynthContext.getContext(c,style,state);
    }

    protected void installListeners(JComponent c){
        c.addPropertyChangeListener(this);
    }

    private Region getRegion(JComponent c){
        return SynthLookAndFeel.getRegion(c);
    }

    @Override
    public void propertyChange(PropertyChangeEvent e){
        if(SynthLookAndFeel.shouldUpdateStyle(e)){
            updateStyle((JComponent)e.getSource());
        }
    }
}
