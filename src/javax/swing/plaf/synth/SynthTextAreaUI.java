/**
 * Copyright (c) 2002, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.synth;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTextAreaUI;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;

public class SynthTextAreaUI extends BasicTextAreaUI implements SynthUI{
    private Handler handler=new Handler();
    private SynthStyle style;

    public static ComponentUI createUI(JComponent ta){
        return new SynthTextAreaUI();
    }

    @Override
    protected void installDefaults(){
        // Installs the text cursor on the component
        super.installDefaults();
        updateStyle(getComponent());
        getComponent().addFocusListener(handler);
    }

    private void updateStyle(JTextComponent comp){
        SynthContext context=getContext(comp,ENABLED);
        SynthStyle oldStyle=style;
        style=SynthLookAndFeel.updateStyle(context,this);
        if(style!=oldStyle){
            SynthTextFieldUI.updateStyle(comp,context,getPropertyPrefix());
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
    protected void propertyChange(PropertyChangeEvent evt){
        if(SynthLookAndFeel.shouldUpdateStyle(evt)){
            updateStyle((JTextComponent)evt.getSource());
        }
        super.propertyChange(evt);
    }

    @Override
    protected void uninstallDefaults(){
        SynthContext context=getContext(getComponent(),ENABLED);
        getComponent().putClientProperty("caretAspectRatio",null);
        getComponent().removeFocusListener(handler);
        style.uninstallDefaults(context);
        context.dispose();
        style=null;
        super.uninstallDefaults();
    }

    @Override
    protected void paintBackground(Graphics g){
        // Overriden to do nothing, all our painting is done from update/paint.
    }

    @Override
    public void update(Graphics g,JComponent c){
        SynthContext context=getContext(c);
        SynthLookAndFeel.update(context,g);
        context.getPainter().paintTextAreaBackground(context,
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
        context.getPainter().paintTextAreaBorder(context,g,x,y,w,h);
    }

    protected void paint(SynthContext context,Graphics g){
        super.paint(g,getComponent());
    }

    private final class Handler implements FocusListener{
        public void focusGained(FocusEvent e){
            getComponent().repaint();
        }

        public void focusLost(FocusEvent e){
            getComponent().repaint();
        }
    }
}
