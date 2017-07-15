/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.metal;

import sun.swing.SwingUtilities2;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.plaf.basic.BasicToolTipUI;
import javax.swing.text.View;
import java.awt.*;
import java.awt.event.KeyEvent;

public class MetalToolTipUI extends BasicToolTipUI{
    public static final int padSpaceBetweenStrings=12;
    static MetalToolTipUI sharedInstance=new MetalToolTipUI();
    private Font smallFont;
    // Refer to note in getAcceleratorString about this field.
    private JToolTip tip;
    private String acceleratorDelimiter;

    public MetalToolTipUI(){
        super();
    }

    public static ComponentUI createUI(JComponent c){
        return sharedInstance;
    }

    public void installUI(JComponent c){
        super.installUI(c);
        tip=(JToolTip)c;
        Font f=c.getFont();
        smallFont=new Font(f.getName(),f.getStyle(),f.getSize()-2);
        acceleratorDelimiter=UIManager.getString("MenuItem.acceleratorDelimiter");
        if(acceleratorDelimiter==null){
            acceleratorDelimiter="-";
        }
    }

    public void uninstallUI(JComponent c){
        super.uninstallUI(c);
        tip=null;
    }

    public void paint(Graphics g,JComponent c){
        JToolTip tip=(JToolTip)c;
        Font font=c.getFont();
        FontMetrics metrics=SwingUtilities2.getFontMetrics(c,g,font);
        Dimension size=c.getSize();
        int accelBL;
        g.setColor(c.getForeground());
        // fix for bug 4153892
        String tipText=tip.getTipText();
        if(tipText==null){
            tipText="";
        }
        String accelString=getAcceleratorString(tip);
        FontMetrics accelMetrics=SwingUtilities2.getFontMetrics(c,g,smallFont);
        int accelSpacing=calcAccelSpacing(c,accelMetrics,accelString);
        Insets insets=tip.getInsets();
        Rectangle paintTextR=new Rectangle(
                insets.left+3,
                insets.top,
                size.width-(insets.left+insets.right)-6-accelSpacing,
                size.height-(insets.top+insets.bottom));
        View v=(View)c.getClientProperty(BasicHTML.propertyKey);
        if(v!=null){
            v.paint(g,paintTextR);
            accelBL=BasicHTML.getHTMLBaseline(v,paintTextR.width,
                    paintTextR.height);
        }else{
            g.setFont(font);
            SwingUtilities2.drawString(tip,g,tipText,paintTextR.x,
                    paintTextR.y+metrics.getAscent());
            accelBL=metrics.getAscent();
        }
        if(!accelString.equals("")){
            g.setFont(smallFont);
            g.setColor(MetalLookAndFeel.getPrimaryControlDarkShadow());
            SwingUtilities2.drawString(tip,g,accelString,
                    tip.getWidth()-1-insets.right
                            -accelSpacing
                            +padSpaceBetweenStrings
                            -3,
                    paintTextR.y+accelBL);
        }
    }

    private int calcAccelSpacing(JComponent c,FontMetrics fm,String accel){
        return accel.equals("")
                ?0
                :padSpaceBetweenStrings+
                SwingUtilities2.stringWidth(c,fm,accel);
    }

    public Dimension getPreferredSize(JComponent c){
        Dimension d=super.getPreferredSize(c);
        String key=getAcceleratorString((JToolTip)c);
        if(!(key.equals(""))){
            d.width+=calcAccelSpacing(c,c.getFontMetrics(smallFont),key);
        }
        return d;
    }

    private String getAcceleratorString(JToolTip tip){
        this.tip=tip;
        String retValue=getAcceleratorString();
        this.tip=null;
        return retValue;
    }

    // NOTE: This requires the tip field to be set before this is invoked.
    // As MetalToolTipUI is shared between all JToolTips the tip field is
    // set appropriately before this is invoked. Unfortunately this means
    // that subclasses that randomly invoke this method will see varying
    // results. If this becomes an issue, MetalToolTipUI should no longer be
    // shared.
    public String getAcceleratorString(){
        if(tip==null||isAcceleratorHidden()){
            return "";
        }
        JComponent comp=tip.getComponent();
        if(!(comp instanceof AbstractButton)){
            return "";
        }
        KeyStroke[] keys=comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).keys();
        if(keys==null){
            return "";
        }
        String controlKeyStr="";
        for(int i=0;i<keys.length;i++){
            int mod=keys[i].getModifiers();
            controlKeyStr=KeyEvent.getKeyModifiersText(mod)+
                    acceleratorDelimiter+
                    KeyEvent.getKeyText(keys[i].getKeyCode());
            break;
        }
        return controlKeyStr;
    }

    protected boolean isAcceleratorHidden(){
        Boolean b=(Boolean)UIManager.get("ToolTip.hideAccelerator");
        return b!=null&&b.booleanValue();
    }
}
