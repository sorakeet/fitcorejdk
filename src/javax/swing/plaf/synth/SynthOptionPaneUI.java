/**
 * Copyright (c) 2002, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.synth;

import sun.swing.DefaultLookup;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicOptionPaneUI;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class SynthOptionPaneUI extends BasicOptionPaneUI implements
        PropertyChangeListener, SynthUI{
    private SynthStyle style;

    public static ComponentUI createUI(JComponent x){
        return new SynthOptionPaneUI();
    }

    @Override
    protected void installDefaults(){
        updateStyle(optionPane);
    }

    private void updateStyle(JComponent c){
        SynthContext context=getContext(c,ENABLED);
        SynthStyle oldStyle=style;
        style=SynthLookAndFeel.updateStyle(context,this);
        if(style!=oldStyle){
            minimumSize=(Dimension)style.get(context,
                    "OptionPane.minimumSize");
            if(minimumSize==null){
                minimumSize=new Dimension(262,90);
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
    protected void uninstallDefaults(){
        SynthContext context=getContext(optionPane,ENABLED);
        style.uninstallDefaults(context);
        context.dispose();
        style=null;
    }

    @Override
    protected void installComponents(){
        optionPane.add(createMessageArea());
        Container separator=createSeparator();
        if(separator!=null){
            optionPane.add(separator);
            SynthContext context=getContext(optionPane,ENABLED);
            optionPane.add(Box.createVerticalStrut(context.getStyle().
                    getInt(context,"OptionPane.separatorPadding",6)));
            context.dispose();
        }
        optionPane.add(createButtonArea());
        optionPane.applyComponentOrientation(optionPane.getComponentOrientation());
    }

    @Override
    protected void installListeners(){
        super.installListeners();
        optionPane.addPropertyChangeListener(this);
    }

    @Override
    protected void uninstallListeners(){
        super.uninstallListeners();
        optionPane.removePropertyChangeListener(this);
    }

    @Override
    protected Container createMessageArea(){
        JPanel top=new JPanel();
        top.setName("OptionPane.messageArea");
        top.setLayout(new BorderLayout());
        /** Fill the body. */
        Container body=new JPanel(new GridBagLayout());
        Container realBody=new JPanel(new BorderLayout());
        body.setName("OptionPane.body");
        realBody.setName("OptionPane.realBody");
        if(getIcon()!=null){
            JPanel sep=new JPanel();
            sep.setName("OptionPane.separator");
            sep.setPreferredSize(new Dimension(15,1));
            realBody.add(sep,BorderLayout.BEFORE_LINE_BEGINS);
        }
        realBody.add(body,BorderLayout.CENTER);
        GridBagConstraints cons=new GridBagConstraints();
        cons.gridx=cons.gridy=0;
        cons.gridwidth=GridBagConstraints.REMAINDER;
        cons.gridheight=1;
        SynthContext context=getContext(optionPane,ENABLED);
        cons.anchor=context.getStyle().getInt(context,
                "OptionPane.messageAnchor",GridBagConstraints.CENTER);
        context.dispose();
        cons.insets=new Insets(0,0,3,0);
        addMessageComponents(body,cons,getMessage(),
                getMaxCharactersPerLineCount(),false);
        top.add(realBody,BorderLayout.CENTER);
        addIcon(top);
        return top;
    }

    @Override
    protected Container createSeparator(){
        JSeparator separator=new JSeparator(SwingConstants.HORIZONTAL);
        separator.setName("OptionPane.separator");
        return separator;
    }

    @Override
    protected boolean getSizeButtonsToSameWidth(){
        return DefaultLookup.getBoolean(optionPane,this,
                "OptionPane.sameSizeButtons",true);
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
        context.getPainter().paintOptionPaneBackground(context,
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
        context.getPainter().paintOptionPaneBorder(context,g,x,y,w,h);
    }

    protected void paint(SynthContext context,Graphics g){
    }

    @Override
    public void propertyChange(PropertyChangeEvent e){
        if(SynthLookAndFeel.shouldUpdateStyle(e)){
            updateStyle((JOptionPane)e.getSource());
        }
    }
}
