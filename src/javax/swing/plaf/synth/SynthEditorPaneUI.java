/**
 * Copyright (c) 2002, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.synth;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicEditorPaneUI;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.beans.PropertyChangeEvent;

public class SynthEditorPaneUI extends BasicEditorPaneUI implements SynthUI{
    private SynthStyle style;
    private Boolean localTrue=Boolean.TRUE;

    public static ComponentUI createUI(JComponent c){
        return new SynthEditorPaneUI();
    }

    @Override
    protected void installDefaults(){
        // Installs the text cursor on the component
        super.installDefaults();
        JComponent c=getComponent();
        Object clientProperty=
                c.getClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES);
        if(clientProperty==null){
            c.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES,localTrue);
        }
        updateStyle(getComponent());
    }

    @Override
    protected void uninstallDefaults(){
        SynthContext context=getContext(getComponent(),ENABLED);
        JComponent c=getComponent();
        c.putClientProperty("caretAspectRatio",null);
        style.uninstallDefaults(context);
        context.dispose();
        style=null;
        Object clientProperty=
                c.getClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES);
        if(clientProperty==localTrue){
            c.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES,
                    Boolean.FALSE);
        }
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
        paintBackground(context,g,c);
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
        context.getPainter().paintEditorPaneBorder(context,g,x,y,w,h);
    }

    protected void paint(SynthContext context,Graphics g){
        super.paint(g,getComponent());
    }

    void paintBackground(SynthContext context,Graphics g,JComponent c){
        context.getPainter().paintEditorPaneBackground(context,g,0,0,
                c.getWidth(),c.getHeight());
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
}
