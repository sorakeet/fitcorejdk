/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.metal;

import sun.awt.AppContext;
import sun.swing.SwingUtilities2;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import java.awt.*;

public class MetalToggleButtonUI extends BasicToggleButtonUI{
    private static final Object METAL_TOGGLE_BUTTON_UI_KEY=new Object();
    protected Color focusColor;
    protected Color selectColor;
    protected Color disabledTextColor;
    private boolean defaults_initialized=false;

    // ********************************
    //        Create PLAF
    // ********************************
    public static ComponentUI createUI(JComponent b){
        AppContext appContext=AppContext.getAppContext();
        MetalToggleButtonUI metalToggleButtonUI=
                (MetalToggleButtonUI)appContext.get(METAL_TOGGLE_BUTTON_UI_KEY);
        if(metalToggleButtonUI==null){
            metalToggleButtonUI=new MetalToggleButtonUI();
            appContext.put(METAL_TOGGLE_BUTTON_UI_KEY,metalToggleButtonUI);
        }
        return metalToggleButtonUI;
    }

    // ********************************
    //        Install Defaults
    // ********************************
    public void installDefaults(AbstractButton b){
        super.installDefaults(b);
        if(!defaults_initialized){
            focusColor=UIManager.getColor(getPropertyPrefix()+"focus");
            selectColor=UIManager.getColor(getPropertyPrefix()+"select");
            disabledTextColor=UIManager.getColor(getPropertyPrefix()+"disabledText");
            defaults_initialized=true;
        }
    }

    protected void uninstallDefaults(AbstractButton b){
        super.uninstallDefaults(b);
        defaults_initialized=false;
    }

    protected void paintText(Graphics g,JComponent c,Rectangle textRect,String text){
        AbstractButton b=(AbstractButton)c;
        ButtonModel model=b.getModel();
        FontMetrics fm=SwingUtilities2.getFontMetrics(b,g);
        int mnemIndex=b.getDisplayedMnemonicIndex();
        /** Draw the Text */
        if(model.isEnabled()){
            /*** paint the text normally */
            g.setColor(b.getForeground());
        }else{
            /*** paint the text disabled ***/
            if(model.isSelected()){
                g.setColor(c.getBackground());
            }else{
                g.setColor(getDisabledTextColor());
            }
        }
        SwingUtilities2.drawStringUnderlineCharAt(c,g,text,mnemIndex,
                textRect.x,textRect.y+fm.getAscent());
    }

    protected Color getDisabledTextColor(){
        return disabledTextColor;
    }

    protected void paintFocus(Graphics g,AbstractButton b,
                              Rectangle viewRect,Rectangle textRect,Rectangle iconRect){
        Rectangle focusRect=new Rectangle();
        String text=b.getText();
        boolean isIcon=b.getIcon()!=null;
        // If there is text
        if(text!=null&&!text.equals("")){
            if(!isIcon){
                focusRect.setBounds(textRect);
            }else{
                focusRect.setBounds(iconRect.union(textRect));
            }
        }
        // If there is an icon and no text
        else if(isIcon){
            focusRect.setBounds(iconRect);
        }
        g.setColor(getFocusColor());
        g.drawRect((focusRect.x-1),(focusRect.y-1),
                focusRect.width+1,focusRect.height+1);
    }

    protected Color getFocusColor(){
        return focusColor;
    }

    protected void paintButtonPressed(Graphics g,AbstractButton b){
        if(b.isContentAreaFilled()){
            g.setColor(getSelectColor());
            g.fillRect(0,0,b.getWidth(),b.getHeight());
        }
    }

    // ********************************
    //         Default Accessors
    // ********************************
    protected Color getSelectColor(){
        return selectColor;
    }

    // ********************************
    //        Paint Methods
    // ********************************
    public void update(Graphics g,JComponent c){
        AbstractButton button=(AbstractButton)c;
        if((c.getBackground() instanceof UIResource)&&
                button.isContentAreaFilled()&&c.isEnabled()){
            ButtonModel model=button.getModel();
            if(!MetalUtils.isToolBarButton(c)){
                if(!model.isArmed()&&!model.isPressed()&&
                        MetalUtils.drawGradient(
                                c,g,"ToggleButton.gradient",0,0,c.getWidth(),
                                c.getHeight(),true)){
                    paint(g,c);
                    return;
                }
            }else if((model.isRollover()||model.isSelected())&&
                    MetalUtils.drawGradient(c,g,"ToggleButton.gradient",
                            0,0,c.getWidth(),c.getHeight(),true)){
                paint(g,c);
                return;
            }
        }
        super.update(g,c);
    }

    protected void paintIcon(Graphics g,AbstractButton b,Rectangle iconRect){
        super.paintIcon(g,b,iconRect);
    }
}
