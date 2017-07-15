/**
 * Copyright (c) 2002, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.synth;

import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.ColorChooserComponentFactory;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicColorChooserUI;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class SynthColorChooserUI extends BasicColorChooserUI implements
        PropertyChangeListener, SynthUI{
    private SynthStyle style;

    public static ComponentUI createUI(JComponent c){
        return new SynthColorChooserUI();
    }

    @Override
    protected AbstractColorChooserPanel[] createDefaultChoosers(){
        SynthContext context=getContext(chooser,ENABLED);
        AbstractColorChooserPanel[] panels=(AbstractColorChooserPanel[])
                context.getStyle().get(context,"ColorChooser.panels");
        context.dispose();
        if(panels==null){
            panels=ColorChooserComponentFactory.getDefaultChooserPanels();
        }
        return panels;
    }

    @Override
    protected void installDefaults(){
        super.installDefaults();
        updateStyle(chooser);
    }

    private void updateStyle(JComponent c){
        SynthContext context=getContext(c,ENABLED);
        style=SynthLookAndFeel.updateStyle(context,this);
        context.dispose();
    }

    @Override
    protected void uninstallDefaults(){
        SynthContext context=getContext(chooser,ENABLED);
        style.uninstallDefaults(context);
        context.dispose();
        style=null;
        super.uninstallDefaults();
    }

    @Override
    protected void installListeners(){
        super.installListeners();
        chooser.addPropertyChangeListener(this);
    }

    @Override
    protected void uninstallListeners(){
        chooser.removePropertyChangeListener(this);
        super.uninstallListeners();
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
        context.getPainter().paintColorChooserBackground(context,g,0,0,
                c.getWidth(),c.getHeight());
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
        context.getPainter().paintColorChooserBorder(context,g,x,y,w,h);
    }

    protected void paint(SynthContext context,Graphics g){
    }

    @Override
    public void propertyChange(PropertyChangeEvent e){
        if(SynthLookAndFeel.shouldUpdateStyle(e)){
            updateStyle((JColorChooser)e.getSource());
        }
    }
}
