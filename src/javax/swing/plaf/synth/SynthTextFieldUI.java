/**
 * Copyright (c) 2002, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.synth;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;

public class SynthTextFieldUI extends BasicTextFieldUI implements SynthUI{
    private Handler handler=new Handler();
    private SynthStyle style;

    public static ComponentUI createUI(JComponent c){
        return new SynthTextFieldUI();
    }

    @Override
    protected void propertyChange(PropertyChangeEvent evt){
        if(SynthLookAndFeel.shouldUpdateStyle(evt)){
            updateStyle((JTextComponent)evt.getSource());
        }
        super.propertyChange(evt);
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

    static void updateStyle(JTextComponent comp,SynthContext context,
                            String prefix){
        SynthStyle style=context.getStyle();
        Color color=comp.getCaretColor();
        if(color==null||color instanceof UIResource){
            comp.setCaretColor(
                    (Color)style.get(context,prefix+".caretForeground"));
        }
        Color fg=comp.getForeground();
        if(fg==null||fg instanceof UIResource){
            fg=style.getColorForState(context,ColorType.TEXT_FOREGROUND);
            if(fg!=null){
                comp.setForeground(fg);
            }
        }
        Object ar=style.get(context,prefix+".caretAspectRatio");
        if(ar instanceof Number){
            comp.putClientProperty("caretAspectRatio",ar);
        }
        context.setComponentState(SELECTED|FOCUSED);
        Color s=comp.getSelectionColor();
        if(s==null||s instanceof UIResource){
            comp.setSelectionColor(
                    style.getColor(context,ColorType.TEXT_BACKGROUND));
        }
        Color sfg=comp.getSelectedTextColor();
        if(sfg==null||sfg instanceof UIResource){
            comp.setSelectedTextColor(
                    style.getColor(context,ColorType.TEXT_FOREGROUND));
        }
        context.setComponentState(DISABLED);
        Color dfg=comp.getDisabledTextColor();
        if(dfg==null||dfg instanceof UIResource){
            comp.setDisabledTextColor(
                    style.getColor(context,ColorType.TEXT_FOREGROUND));
        }
        Insets margin=comp.getMargin();
        if(margin==null||margin instanceof UIResource){
            margin=(Insets)style.get(context,prefix+".margin");
            if(margin==null){
                // Some places assume margins are non-null.
                margin=SynthLookAndFeel.EMPTY_UIRESOURCE_INSETS;
            }
            comp.setMargin(margin);
        }
        Caret caret=comp.getCaret();
        if(caret instanceof UIResource){
            Object o=style.get(context,prefix+".caretBlinkRate");
            if(o!=null&&o instanceof Integer){
                Integer rate=(Integer)o;
                caret.setBlinkRate(rate.intValue());
            }
        }
    }

    @Override
    protected void installDefaults(){
        // Installs the text cursor on the component
        super.installDefaults();
        updateStyle(getComponent());
        getComponent().addFocusListener(handler);
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
        paintBackground(context,g,c);
        paint(context,g);
        context.dispose();
    }

    @Override
    public SynthContext getContext(JComponent c){
        return getContext(c,SynthLookAndFeel.getComponentState(c));
    }

    private SynthContext getContext(JComponent c,int state){
        return SynthContext.getContext(c,style,state);
    }

    @Override
    public void paintBorder(SynthContext context,Graphics g,int x,
                            int y,int w,int h){
        context.getPainter().paintTextFieldBorder(context,g,x,y,w,h);
    }

    protected void paint(SynthContext context,Graphics g){
        super.paint(g,getComponent());
    }

    void paintBackground(SynthContext context,Graphics g,JComponent c){
        context.getPainter().paintTextFieldBackground(context,g,0,0,
                c.getWidth(),c.getHeight());
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
