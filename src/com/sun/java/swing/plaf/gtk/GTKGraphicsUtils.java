/**
 * Copyright (c) 2002, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.java.swing.plaf.gtk;

import javax.swing.*;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthGraphicsUtils;
import java.awt.*;

class GTKGraphicsUtils extends SynthGraphicsUtils{
    public void paintText(SynthContext context,Graphics g,String text,
                          Rectangle bounds,int mnemonicIndex){
        if(text==null||text.length()<=0){
            // We don't need to paint empty strings
            return;
        }
        Region id=context.getRegion();
        if((id==Region.RADIO_BUTTON||
                id==Region.CHECK_BOX||
                id==Region.TABBED_PANE_TAB)&&
                (context.getComponentState()&SynthConstants.FOCUSED)!=0){
            JComponent source=context.getComponent();
            if(!(source instanceof AbstractButton)||
                    ((AbstractButton)source).isFocusPainted()){
                // The "bounds" parameter encompasses only the actual text;
                // when drawing the focus, we need to expand that bounding
                // box by "focus-line-width" plus "focus-padding".  Note that
                // the layout process for these components will have already
                // taken these values into account, so there should always
                // be enough space allocated for drawing the focus indicator.
                int synthState=context.getComponentState();
                GTKStyle style=(GTKStyle)context.getStyle();
                int focusSize=
                        style.getClassSpecificIntValue(context,
                                "focus-line-width",1);
                int focusPad=
                        style.getClassSpecificIntValue(context,
                                "focus-padding",1);
                int totalFocus=focusSize+focusPad;
                int x=bounds.x-totalFocus;
                int y=bounds.y-totalFocus;
                int w=bounds.width+(2*totalFocus);
                int h=bounds.height+(2*totalFocus);
                Color color=g.getColor();
                GTKPainter.INSTANCE.paintFocus(context,g,id,
                        synthState,"checkbutton",
                        x,y,w,h);
                g.setColor(color);
            }
        }
        super.paintText(context,g,text,bounds,mnemonicIndex);
    }

    public void paintText(SynthContext context,Graphics g,String text,
                          int x,int y,int mnemonicIndex){
        if(text==null||text.length()<=0){
            // We don't need to paint empty strings
            return;
        }
        if(context.getRegion()==Region.INTERNAL_FRAME_TITLE_PANE){
            // Metacity handles painting of text on internal frame title,
            // ignore this.
            return;
        }
        int componentState=context.getComponentState();
        if((componentState&SynthConstants.DISABLED)==
                SynthConstants.DISABLED){
            Color orgColor=g.getColor();
            g.setColor(context.getStyle().getColor(context,
                    GTKColorType.WHITE));
            x+=1;
            y+=1;
            super.paintText(context,g,text,x,y,mnemonicIndex);
            g.setColor(orgColor);
            x-=1;
            y-=1;
            super.paintText(context,g,text,x,y,mnemonicIndex);
        }else{
            String themeName=GTKLookAndFeel.getGtkThemeName();
            if(themeName!=null&&themeName.startsWith("blueprint")&&
                    shouldShadowText(context.getRegion(),componentState)){
                g.setColor(Color.BLACK);
                super.paintText(context,g,text,x+1,y+1,mnemonicIndex);
                g.setColor(Color.WHITE);
            }
            super.paintText(context,g,text,x,y,mnemonicIndex);
        }
    }

    private static boolean shouldShadowText(Region id,int state){
        int gtkState=GTKLookAndFeel.synthStateToGTKState(id,state);
        return ((gtkState==SynthConstants.MOUSE_OVER)&&
                (id==Region.MENU||
                        id==Region.MENU_ITEM||
                        id==Region.CHECK_BOX_MENU_ITEM||
                        id==Region.RADIO_BUTTON_MENU_ITEM));
    }
}
