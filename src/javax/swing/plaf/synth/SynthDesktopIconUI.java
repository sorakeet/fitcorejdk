/**
 * Copyright (c) 2002, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.synth;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicDesktopIconUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;

public class SynthDesktopIconUI extends BasicDesktopIconUI
        implements SynthUI, PropertyChangeListener{
    private SynthStyle style;
    private Handler handler=new Handler();

    public static ComponentUI createUI(JComponent c){
        return new SynthDesktopIconUI();
    }

    @Override
    protected void installComponents(){
        if(UIManager.getBoolean("InternalFrame.useTaskBar")){
            iconPane=new JToggleButton(frame.getTitle(),frame.getFrameIcon()){
                @Override
                public JPopupMenu getComponentPopupMenu(){
                    return frame.getComponentPopupMenu();
                }

                @Override
                public String getToolTipText(){
                    return getText();
                }
            };
            ToolTipManager.sharedInstance().registerComponent(iconPane);
            iconPane.setFont(desktopIcon.getFont());
            iconPane.setBackground(desktopIcon.getBackground());
            iconPane.setForeground(desktopIcon.getForeground());
        }else{
            iconPane=new SynthInternalFrameTitlePane(frame);
            iconPane.setName("InternalFrame.northPane");
        }
        desktopIcon.setLayout(new BorderLayout());
        desktopIcon.add(iconPane,BorderLayout.CENTER);
    }

    @Override
    protected void installListeners(){
        super.installListeners();
        desktopIcon.addPropertyChangeListener(this);
        if(iconPane instanceof JToggleButton){
            frame.addPropertyChangeListener(this);
            ((JToggleButton)iconPane).addActionListener(handler);
        }
    }

    @Override
    protected void uninstallListeners(){
        if(iconPane instanceof JToggleButton){
            ((JToggleButton)iconPane).removeActionListener(handler);
            frame.removePropertyChangeListener(this);
        }
        desktopIcon.removePropertyChangeListener(this);
        super.uninstallListeners();
    }

    @Override
    protected void installDefaults(){
        updateStyle(desktopIcon);
    }

    private void updateStyle(JComponent c){
        SynthContext context=getContext(c,ENABLED);
        style=SynthLookAndFeel.updateStyle(context,this);
        context.dispose();
    }

    private SynthContext getContext(JComponent c,int state){
        return SynthContext.getContext(c,style,state);
    }

    @Override
    protected void uninstallDefaults(){
        SynthContext context=getContext(desktopIcon,ENABLED);
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
        context.getPainter().paintDesktopIconBackground(context,g,0,0,
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
        context.getPainter().paintDesktopIconBorder(context,g,x,y,w,h);
    }

    protected void paint(SynthContext context,Graphics g){
    }

    public void propertyChange(PropertyChangeEvent evt){
        if(evt.getSource() instanceof JInternalFrame.JDesktopIcon){
            if(SynthLookAndFeel.shouldUpdateStyle(evt)){
                updateStyle((JInternalFrame.JDesktopIcon)evt.getSource());
            }
        }else if(evt.getSource() instanceof JInternalFrame){
            JInternalFrame frame=(JInternalFrame)evt.getSource();
            if(iconPane instanceof JToggleButton){
                JToggleButton button=(JToggleButton)iconPane;
                String prop=evt.getPropertyName();
                if(prop=="title"){
                    button.setText((String)evt.getNewValue());
                }else if(prop=="frameIcon"){
                    button.setIcon((Icon)evt.getNewValue());
                }else if(prop==JInternalFrame.IS_ICON_PROPERTY||
                        prop==JInternalFrame.IS_SELECTED_PROPERTY){
                    button.setSelected(!frame.isIcon()&&frame.isSelected());
                }
            }
        }
    }

    private final class Handler implements ActionListener{
        public void actionPerformed(ActionEvent evt){
            if(evt.getSource() instanceof JToggleButton){
                // Either iconify the frame or deiconify and activate it.
                JToggleButton button=(JToggleButton)evt.getSource();
                try{
                    boolean selected=button.isSelected();
                    if(!selected&&!frame.isIconifiable()){
                        button.setSelected(true);
                    }else{
                        frame.setIcon(!selected);
                        if(selected){
                            frame.setSelected(true);
                        }
                    }
                }catch(PropertyVetoException e2){
                }
            }
        }
    }
}
