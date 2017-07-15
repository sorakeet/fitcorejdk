/**
 * Copyright (c) 2002, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.synth;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class SynthInternalFrameUI extends BasicInternalFrameUI
        implements SynthUI, PropertyChangeListener{
    private SynthStyle style;

    protected SynthInternalFrameUI(JInternalFrame b){
        super(b);
    }

    public static ComponentUI createUI(JComponent b){
        return new SynthInternalFrameUI((JInternalFrame)b);
    }

    @Override
    public void installDefaults(){
        frame.setLayout(internalFrameLayout=createLayoutManager());
        updateStyle(frame);
    }

    @Override
    protected void installListeners(){
        super.installListeners();
        frame.addPropertyChangeListener(this);
    }

    @Override
    protected void uninstallDefaults(){
        SynthContext context=getContext(frame,ENABLED);
        style.uninstallDefaults(context);
        context.dispose();
        style=null;
        if(frame.getLayout()==internalFrameLayout){
            frame.setLayout(null);
        }
    }

    @Override
    protected void uninstallComponents(){
        if(frame.getComponentPopupMenu() instanceof UIResource){
            frame.setComponentPopupMenu(null);
        }
        super.uninstallComponents();
    }

    @Override
    protected void uninstallListeners(){
        frame.removePropertyChangeListener(this);
        super.uninstallListeners();
    }

    @Override
    protected JComponent createNorthPane(JInternalFrame w){
        titlePane=new SynthInternalFrameTitlePane(w);
        titlePane.setName("InternalFrame.northPane");
        return titlePane;
    }

    @Override
    protected ComponentListener createComponentListener(){
        if(UIManager.getBoolean("InternalFrame.useTaskBar")){
            return new ComponentHandler(){
                @Override
                public void componentResized(ComponentEvent e){
                    if(frame!=null&&frame.isMaximum()){
                        JDesktopPane desktop=(JDesktopPane)e.getSource();
                        for(Component comp : desktop.getComponents()){
                            if(comp instanceof SynthDesktopPaneUI.TaskBar){
                                frame.setBounds(0,0,
                                        desktop.getWidth(),
                                        desktop.getHeight()-comp.getHeight());
                                frame.revalidate();
                                break;
                            }
                        }
                    }
                    // Update the new parent bounds for next resize, but don't
                    // let the super method touch this frame
                    JInternalFrame f=frame;
                    frame=null;
                    super.componentResized(e);
                    frame=f;
                }
            };
        }else{
            return super.createComponentListener();
        }
    }

    private void updateStyle(JComponent c){
        SynthContext context=getContext(c,ENABLED);
        SynthStyle oldStyle=style;
        style=SynthLookAndFeel.updateStyle(context,this);
        if(style!=oldStyle){
            Icon frameIcon=frame.getFrameIcon();
            if(frameIcon==null||frameIcon instanceof UIResource){
                frame.setFrameIcon(context.getStyle().getIcon(
                        context,"InternalFrame.icon"));
            }
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
        context.getPainter().paintInternalFrameBackground(context,
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
        context.getPainter().paintInternalFrameBorder(context,
                g,x,y,w,h);
    }

    protected void paint(SynthContext context,Graphics g){
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt){
        SynthStyle oldStyle=style;
        JInternalFrame f=(JInternalFrame)evt.getSource();
        String prop=evt.getPropertyName();
        if(SynthLookAndFeel.shouldUpdateStyle(evt)){
            updateStyle(f);
        }
        if(style==oldStyle&&
                (prop==JInternalFrame.IS_MAXIMUM_PROPERTY||
                        prop==JInternalFrame.IS_SELECTED_PROPERTY)){
            // Border (and other defaults) may need to change
            SynthContext context=getContext(f,ENABLED);
            style.uninstallDefaults(context);
            style.installDefaults(context,this);
        }
    }
}
