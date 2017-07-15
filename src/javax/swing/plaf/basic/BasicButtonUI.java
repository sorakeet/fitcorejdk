/**
 * Copyright (c) 1997, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.basic;

import sun.awt.AppContext;
import sun.swing.SwingUtilities2;

import javax.swing.*;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.text.View;
import java.awt.*;
import java.awt.event.MouseMotionListener;

public class BasicButtonUI extends ButtonUI{
    private final static String propertyPrefix="Button"+".";
    private static final Object BASIC_BUTTON_UI_KEY=new Object();
    private static Rectangle viewRect=new Rectangle();
    private static Rectangle textRect=new Rectangle();
    private static Rectangle iconRect=new Rectangle();
    // Visual constants
    // NOTE: This is not used or set any where. Were we allowed to remove
    // fields, this would be removed.
    protected int defaultTextIconGap;
    // Value that is set in shiftOffset once setTextShiftOffset has been
    // invoked. The value of this comes from the defaults table.
    protected int defaultTextShiftOffset;
    // Amount to offset text, the value of this comes from
    // defaultTextShiftOffset once setTextShiftOffset has been invoked.
    private int shiftOffset=0;

    // ********************************
    //          Create PLAF
    // ********************************
    public static ComponentUI createUI(JComponent c){
        AppContext appContext=AppContext.getAppContext();
        BasicButtonUI buttonUI=
                (BasicButtonUI)appContext.get(BASIC_BUTTON_UI_KEY);
        if(buttonUI==null){
            buttonUI=new BasicButtonUI();
            appContext.put(BASIC_BUTTON_UI_KEY,buttonUI);
        }
        return buttonUI;
    }

    // ********************************
    //          Install PLAF
    // ********************************
    public void installUI(JComponent c){
        installDefaults((AbstractButton)c);
        installListeners((AbstractButton)c);
        installKeyboardActions((AbstractButton)c);
        BasicHTML.updateRenderer(c,((AbstractButton)c).getText());
    }

    protected void installDefaults(AbstractButton b){
        // load shared instance defaults
        String pp=getPropertyPrefix();
        defaultTextShiftOffset=UIManager.getInt(pp+"textShiftOffset");
        // set the following defaults on the button
        if(b.isContentAreaFilled()){
            LookAndFeel.installProperty(b,"opaque",Boolean.TRUE);
        }else{
            LookAndFeel.installProperty(b,"opaque",Boolean.FALSE);
        }
        if(b.getMargin()==null||(b.getMargin() instanceof UIResource)){
            b.setMargin(UIManager.getInsets(pp+"margin"));
        }
        LookAndFeel.installColorsAndFont(b,pp+"background",
                pp+"foreground",pp+"font");
        LookAndFeel.installBorder(b,pp+"border");
        Object rollover=UIManager.get(pp+"rollover");
        if(rollover!=null){
            LookAndFeel.installProperty(b,"rolloverEnabled",rollover);
        }
        LookAndFeel.installProperty(b,"iconTextGap",Integer.valueOf(4));
    }

    protected String getPropertyPrefix(){
        return propertyPrefix;
    }

    protected void installListeners(AbstractButton b){
        BasicButtonListener listener=createButtonListener(b);
        if(listener!=null){
            b.addMouseListener(listener);
            b.addMouseMotionListener(listener);
            b.addFocusListener(listener);
            b.addPropertyChangeListener(listener);
            b.addChangeListener(listener);
        }
    }

    // ********************************
    //        Create Listeners
    // ********************************
    protected BasicButtonListener createButtonListener(AbstractButton b){
        return new BasicButtonListener(b);
    }

    protected void installKeyboardActions(AbstractButton b){
        BasicButtonListener listener=getButtonListener(b);
        if(listener!=null){
            listener.installKeyboardActions(b);
        }
    }

    private BasicButtonListener getButtonListener(AbstractButton b){
        MouseMotionListener[] listeners=b.getMouseMotionListeners();
        if(listeners!=null){
            for(MouseMotionListener listener : listeners){
                if(listener instanceof BasicButtonListener){
                    return (BasicButtonListener)listener;
                }
            }
        }
        return null;
    }

    // ********************************
    //         Uninstall PLAF
    // ********************************
    public void uninstallUI(JComponent c){
        uninstallKeyboardActions((AbstractButton)c);
        uninstallListeners((AbstractButton)c);
        uninstallDefaults((AbstractButton)c);
        BasicHTML.updateRenderer(c,"");
    }

    protected void uninstallKeyboardActions(AbstractButton b){
        BasicButtonListener listener=getButtonListener(b);
        if(listener!=null){
            listener.uninstallKeyboardActions(b);
        }
    }

    protected void uninstallListeners(AbstractButton b){
        BasicButtonListener listener=getButtonListener(b);
        if(listener!=null){
            b.removeMouseListener(listener);
            b.removeMouseMotionListener(listener);
            b.removeFocusListener(listener);
            b.removeChangeListener(listener);
            b.removePropertyChangeListener(listener);
        }
    }

    protected void uninstallDefaults(AbstractButton b){
        LookAndFeel.uninstallBorder(b);
    }
    // ********************************
    //          Paint Methods
    // ********************************

    public void paint(Graphics g,JComponent c){
        AbstractButton b=(AbstractButton)c;
        ButtonModel model=b.getModel();
        String text=layout(b,SwingUtilities2.getFontMetrics(b,g),
                b.getWidth(),b.getHeight());
        clearTextShiftOffset();
        // perform UI specific press action, e.g. Windows L&F shifts text
        if(model.isArmed()&&model.isPressed()){
            paintButtonPressed(g,b);
        }
        // Paint the Icon
        if(b.getIcon()!=null){
            paintIcon(g,c,iconRect);
        }
        if(text!=null&&!text.equals("")){
            View v=(View)c.getClientProperty(BasicHTML.propertyKey);
            if(v!=null){
                v.paint(g,textRect);
            }else{
                paintText(g,b,textRect,text);
            }
        }
        if(b.isFocusPainted()&&b.hasFocus()){
            // paint UI specific focus
            paintFocus(g,b,viewRect,textRect,iconRect);
        }
    }

    protected void paintIcon(Graphics g,JComponent c,Rectangle iconRect){
        AbstractButton b=(AbstractButton)c;
        ButtonModel model=b.getModel();
        Icon icon=b.getIcon();
        Icon tmpIcon=null;
        if(icon==null){
            return;
        }
        Icon selectedIcon=null;
        /** the fallback icon should be based on the selected state */
        if(model.isSelected()){
            selectedIcon=b.getSelectedIcon();
            if(selectedIcon!=null){
                icon=selectedIcon;
            }
        }
        if(!model.isEnabled()){
            if(model.isSelected()){
                tmpIcon=b.getDisabledSelectedIcon();
                if(tmpIcon==null){
                    tmpIcon=selectedIcon;
                }
            }
            if(tmpIcon==null){
                tmpIcon=b.getDisabledIcon();
            }
        }else if(model.isPressed()&&model.isArmed()){
            tmpIcon=b.getPressedIcon();
            if(tmpIcon!=null){
                // revert back to 0 offset
                clearTextShiftOffset();
            }
        }else if(b.isRolloverEnabled()&&model.isRollover()){
            if(model.isSelected()){
                tmpIcon=b.getRolloverSelectedIcon();
                if(tmpIcon==null){
                    tmpIcon=selectedIcon;
                }
            }
            if(tmpIcon==null){
                tmpIcon=b.getRolloverIcon();
            }
        }
        if(tmpIcon!=null){
            icon=tmpIcon;
        }
        if(model.isPressed()&&model.isArmed()){
            icon.paintIcon(c,g,iconRect.x+getTextShiftOffset(),
                    iconRect.y+getTextShiftOffset());
        }else{
            icon.paintIcon(c,g,iconRect.x,iconRect.y);
        }
    }

    protected int getTextShiftOffset(){
        return shiftOffset;
    }

    protected void paintText(Graphics g,AbstractButton b,Rectangle textRect,String text){
        paintText(g,(JComponent)b,textRect,text);
    }

    protected void paintText(Graphics g,JComponent c,Rectangle textRect,String text){
        AbstractButton b=(AbstractButton)c;
        ButtonModel model=b.getModel();
        FontMetrics fm=SwingUtilities2.getFontMetrics(c,g);
        int mnemonicIndex=b.getDisplayedMnemonicIndex();
        /** Draw the Text */
        if(model.isEnabled()){
            /*** paint the text normally */
            g.setColor(b.getForeground());
            SwingUtilities2.drawStringUnderlineCharAt(c,g,text,mnemonicIndex,
                    textRect.x+getTextShiftOffset(),
                    textRect.y+fm.getAscent()+getTextShiftOffset());
        }else{
            /*** paint the text disabled ***/
            g.setColor(b.getBackground().brighter());
            SwingUtilities2.drawStringUnderlineCharAt(c,g,text,mnemonicIndex,
                    textRect.x,textRect.y+fm.getAscent());
            g.setColor(b.getBackground().darker());
            SwingUtilities2.drawStringUnderlineCharAt(c,g,text,mnemonicIndex,
                    textRect.x-1,textRect.y+fm.getAscent()-1);
        }
    }

    // Method signature defined here overriden in subclasses.
    // Perhaps this class should be abstract?
    protected void paintFocus(Graphics g,AbstractButton b,
                              Rectangle viewRect,Rectangle textRect,Rectangle iconRect){
    }

    protected void paintButtonPressed(Graphics g,AbstractButton b){
    }

    protected void clearTextShiftOffset(){
        this.shiftOffset=0;
    }

    public Dimension getPreferredSize(JComponent c){
        AbstractButton b=(AbstractButton)c;
        return BasicGraphicsUtils.getPreferredButtonSize(b,b.getIconTextGap());
    }

    // ********************************
    //          Layout Methods
    // ********************************
    public Dimension getMinimumSize(JComponent c){
        Dimension d=getPreferredSize(c);
        View v=(View)c.getClientProperty(BasicHTML.propertyKey);
        if(v!=null){
            d.width-=v.getPreferredSpan(View.X_AXIS)-v.getMinimumSpan(View.X_AXIS);
        }
        return d;
    }

    public Dimension getMaximumSize(JComponent c){
        Dimension d=getPreferredSize(c);
        View v=(View)c.getClientProperty(BasicHTML.propertyKey);
        if(v!=null){
            d.width+=v.getMaximumSpan(View.X_AXIS)-v.getPreferredSpan(View.X_AXIS);
        }
        return d;
    }

    public int getBaseline(JComponent c,int width,int height){
        super.getBaseline(c,width,height);
        AbstractButton b=(AbstractButton)c;
        String text=b.getText();
        if(text==null||"".equals(text)){
            return -1;
        }
        FontMetrics fm=b.getFontMetrics(b.getFont());
        layout(b,fm,width,height);
        return BasicHTML.getBaseline(b,textRect.y,fm.getAscent(),
                textRect.width,textRect.height);
    }

    public Component.BaselineResizeBehavior getBaselineResizeBehavior(
            JComponent c){
        super.getBaselineResizeBehavior(c);
        if(c.getClientProperty(BasicHTML.propertyKey)!=null){
            return Component.BaselineResizeBehavior.OTHER;
        }
        switch(((AbstractButton)c).getVerticalAlignment()){
            case AbstractButton.TOP:
                return Component.BaselineResizeBehavior.CONSTANT_ASCENT;
            case AbstractButton.BOTTOM:
                return Component.BaselineResizeBehavior.CONSTANT_DESCENT;
            case AbstractButton.CENTER:
                return Component.BaselineResizeBehavior.CENTER_OFFSET;
        }
        return Component.BaselineResizeBehavior.OTHER;
    }

    private String layout(AbstractButton b,FontMetrics fm,
                          int width,int height){
        Insets i=b.getInsets();
        viewRect.x=i.left;
        viewRect.y=i.top;
        viewRect.width=width-(i.right+viewRect.x);
        viewRect.height=height-(i.bottom+viewRect.y);
        textRect.x=textRect.y=textRect.width=textRect.height=0;
        iconRect.x=iconRect.y=iconRect.width=iconRect.height=0;
        // layout the text and icon
        return SwingUtilities.layoutCompoundLabel(
                b,fm,b.getText(),b.getIcon(),
                b.getVerticalAlignment(),b.getHorizontalAlignment(),
                b.getVerticalTextPosition(),b.getHorizontalTextPosition(),
                viewRect,iconRect,textRect,
                b.getText()==null?0:b.getIconTextGap());
    }

    public int getDefaultTextIconGap(AbstractButton b){
        return defaultTextIconGap;
    }

    protected void setTextShiftOffset(){
        this.shiftOffset=defaultTextShiftOffset;
    }
}
