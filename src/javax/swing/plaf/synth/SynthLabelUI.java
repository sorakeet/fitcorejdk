/**
 * Copyright (c) 2002, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.synth;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.text.View;
import java.awt.*;
import java.beans.PropertyChangeEvent;

public class SynthLabelUI extends BasicLabelUI implements SynthUI{
    private SynthStyle style;

    public static ComponentUI createUI(JComponent c){
        return new SynthLabelUI();
    }

    @Override
    public void update(Graphics g,JComponent c){
        SynthContext context=getContext(c);
        SynthLookAndFeel.update(context,g);
        context.getPainter().paintLabelBackground(context,
                g,0,0,c.getWidth(),c.getHeight());
        paint(context,g);
        context.dispose();
    }

    protected void paint(SynthContext context,Graphics g){
        JLabel label=(JLabel)context.getComponent();
        Icon icon=(label.isEnabled())?label.getIcon():
                label.getDisabledIcon();
        g.setColor(context.getStyle().getColor(context,
                ColorType.TEXT_FOREGROUND));
        g.setFont(style.getFont(context));
        context.getStyle().getGraphicsUtils(context).paintText(
                context,g,label.getText(),icon,
                label.getHorizontalAlignment(),label.getVerticalAlignment(),
                label.getHorizontalTextPosition(),label.getVerticalTextPosition(),
                label.getIconTextGap(),label.getDisplayedMnemonicIndex(),0);
    }

    @Override
    public void paint(Graphics g,JComponent c){
        SynthContext context=getContext(c);
        paint(context,g);
        context.dispose();
    }

    @Override
    public Dimension getPreferredSize(JComponent c){
        JLabel label=(JLabel)c;
        Icon icon=(label.isEnabled())?label.getIcon():
                label.getDisabledIcon();
        SynthContext context=getContext(c);
        Dimension size=context.getStyle().getGraphicsUtils(context).
                getPreferredSize(
                        context,context.getStyle().getFont(context),label.getText(),
                        icon,label.getHorizontalAlignment(),
                        label.getVerticalAlignment(),label.getHorizontalTextPosition(),
                        label.getVerticalTextPosition(),label.getIconTextGap(),
                        label.getDisplayedMnemonicIndex());
        context.dispose();
        return size;
    }

    @Override
    public Dimension getMinimumSize(JComponent c){
        JLabel label=(JLabel)c;
        Icon icon=(label.isEnabled())?label.getIcon():
                label.getDisabledIcon();
        SynthContext context=getContext(c);
        Dimension size=context.getStyle().getGraphicsUtils(context).
                getMinimumSize(
                        context,context.getStyle().getFont(context),label.getText(),
                        icon,label.getHorizontalAlignment(),
                        label.getVerticalAlignment(),label.getHorizontalTextPosition(),
                        label.getVerticalTextPosition(),label.getIconTextGap(),
                        label.getDisplayedMnemonicIndex());
        context.dispose();
        return size;
    }

    @Override
    public Dimension getMaximumSize(JComponent c){
        JLabel label=(JLabel)c;
        Icon icon=(label.isEnabled())?label.getIcon():
                label.getDisabledIcon();
        SynthContext context=getContext(c);
        Dimension size=context.getStyle().getGraphicsUtils(context).
                getMaximumSize(
                        context,context.getStyle().getFont(context),label.getText(),
                        icon,label.getHorizontalAlignment(),
                        label.getVerticalAlignment(),label.getHorizontalTextPosition(),
                        label.getVerticalTextPosition(),label.getIconTextGap(),
                        label.getDisplayedMnemonicIndex());
        context.dispose();
        return size;
    }

    @Override
    public int getBaseline(JComponent c,int width,int height){
        if(c==null){
            throw new NullPointerException("Component must be non-null");
        }
        if(width<0||height<0){
            throw new IllegalArgumentException(
                    "Width and height must be >= 0");
        }
        JLabel label=(JLabel)c;
        String text=label.getText();
        if(text==null||"".equals(text)){
            return -1;
        }
        Insets i=label.getInsets();
        Rectangle viewRect=new Rectangle();
        Rectangle textRect=new Rectangle();
        Rectangle iconRect=new Rectangle();
        viewRect.x=i.left;
        viewRect.y=i.top;
        viewRect.width=width-(i.right+viewRect.x);
        viewRect.height=height-(i.bottom+viewRect.y);
        // layout the text and icon
        SynthContext context=getContext(label);
        FontMetrics fm=context.getComponent().getFontMetrics(
                context.getStyle().getFont(context));
        context.getStyle().getGraphicsUtils(context).layoutText(
                context,fm,label.getText(),label.getIcon(),
                label.getHorizontalAlignment(),label.getVerticalAlignment(),
                label.getHorizontalTextPosition(),label.getVerticalTextPosition(),
                viewRect,iconRect,textRect,label.getIconTextGap());
        View view=(View)label.getClientProperty(BasicHTML.propertyKey);
        int baseline;
        if(view!=null){
            baseline=BasicHTML.getHTMLBaseline(view,textRect.width,
                    textRect.height);
            if(baseline>=0){
                baseline+=textRect.y;
            }
        }else{
            baseline=textRect.y+fm.getAscent();
        }
        context.dispose();
        return baseline;
    }

    @Override
    protected void installDefaults(JLabel c){
        updateStyle(c);
    }

    void updateStyle(JLabel c){
        SynthContext context=getContext(c,ENABLED);
        style=SynthLookAndFeel.updateStyle(context,this);
        context.dispose();
    }

    private SynthContext getContext(JComponent c,int state){
        return SynthContext.getContext(c,style,state);
    }

    @Override
    protected void uninstallDefaults(JLabel c){
        SynthContext context=getContext(c,ENABLED);
        style.uninstallDefaults(context);
        context.dispose();
        style=null;
    }

    @Override
    public void propertyChange(PropertyChangeEvent e){
        super.propertyChange(e);
        if(SynthLookAndFeel.shouldUpdateStyle(e)){
            updateStyle((JLabel)e.getSource());
        }
    }

    @Override
    public SynthContext getContext(JComponent c){
        return getContext(c,getComponentState(c));
    }

    private int getComponentState(JComponent c){
        int state=SynthLookAndFeel.getComponentState(c);
        if(SynthLookAndFeel.getSelectedUI()==this&&
                state==SynthConstants.ENABLED){
            state=SynthLookAndFeel.getSelectedUIState()|SynthConstants.ENABLED;
        }
        return state;
    }

    @Override
    public void paintBorder(SynthContext context,Graphics g,int x,
                            int y,int w,int h){
        context.getPainter().paintLabelBorder(context,g,x,y,w,h);
    }
}
