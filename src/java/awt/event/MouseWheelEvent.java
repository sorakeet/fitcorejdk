/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.event;

import java.awt.*;
import java.lang.annotation.Native;

public class MouseWheelEvent extends MouseEvent{
    @Native
    public static final int WHEEL_UNIT_SCROLL=0;
    @Native
    public static final int WHEEL_BLOCK_SCROLL=1;
    private static final long serialVersionUID=6459879390515399677L;
    int scrollType;
    int scrollAmount;
    int wheelRotation;
    double preciseWheelRotation;

    public MouseWheelEvent(Component source,int id,long when,int modifiers,
                           int x,int y,int clickCount,boolean popupTrigger,
                           int scrollType,int scrollAmount,int wheelRotation){
        this(source,id,when,modifiers,x,y,0,0,clickCount,
                popupTrigger,scrollType,scrollAmount,wheelRotation);
    }

    public MouseWheelEvent(Component source,int id,long when,int modifiers,
                           int x,int y,int xAbs,int yAbs,int clickCount,boolean popupTrigger,
                           int scrollType,int scrollAmount,int wheelRotation){
        this(source,id,when,modifiers,x,y,xAbs,yAbs,clickCount,popupTrigger,
                scrollType,scrollAmount,wheelRotation,wheelRotation);
    }

    public MouseWheelEvent(Component source,int id,long when,int modifiers,
                           int x,int y,int xAbs,int yAbs,int clickCount,boolean popupTrigger,
                           int scrollType,int scrollAmount,int wheelRotation,double preciseWheelRotation){
        super(source,id,when,modifiers,x,y,xAbs,yAbs,clickCount,
                popupTrigger,MouseEvent.NOBUTTON);
        this.scrollType=scrollType;
        this.scrollAmount=scrollAmount;
        this.wheelRotation=wheelRotation;
        this.preciseWheelRotation=preciseWheelRotation;
    }

    public int getUnitsToScroll(){
        return scrollAmount*wheelRotation;
    }

    public String paramString(){
        String scrollTypeStr=null;
        if(getScrollType()==WHEEL_UNIT_SCROLL){
            scrollTypeStr="WHEEL_UNIT_SCROLL";
        }else if(getScrollType()==WHEEL_BLOCK_SCROLL){
            scrollTypeStr="WHEEL_BLOCK_SCROLL";
        }else{
            scrollTypeStr="unknown scroll type";
        }
        return super.paramString()+",scrollType="+scrollTypeStr+
                ",scrollAmount="+getScrollAmount()+",wheelRotation="+
                getWheelRotation()+",preciseWheelRotation="+getPreciseWheelRotation();
    }

    public int getScrollType(){
        return scrollType;
    }

    public int getScrollAmount(){
        return scrollAmount;
    }

    public int getWheelRotation(){
        return wheelRotation;
    }

    public double getPreciseWheelRotation(){
        return preciseWheelRotation;
    }
}
