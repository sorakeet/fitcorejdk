/**
 * Copyright (c) 2005, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.nimbus;

import javax.swing.*;
import java.awt.*;

class DerivedColor extends Color{
    private final String uiDefaultParentName;
    private final float hOffset, sOffset, bOffset;
    private final int aOffset;
    private int argbValue;

    DerivedColor(String uiDefaultParentName,float hOffset,float sOffset,float bOffset,int aOffset){
        super(0);
        this.uiDefaultParentName=uiDefaultParentName;
        this.hOffset=hOffset;
        this.sOffset=sOffset;
        this.bOffset=bOffset;
        this.aOffset=aOffset;
    }

    public String getUiDefaultParentName(){
        return uiDefaultParentName;
    }

    public void rederiveColor(){
        Color src=UIManager.getColor(uiDefaultParentName);
        if(src!=null){
            float[] tmp=Color.RGBtoHSB(src.getRed(),src.getGreen(),src.getBlue(),null);
            // apply offsets
            tmp[0]=clamp(tmp[0]+hOffset);
            tmp[1]=clamp(tmp[1]+sOffset);
            tmp[2]=clamp(tmp[2]+bOffset);
            int alpha=clamp(src.getAlpha()+aOffset);
            argbValue=(Color.HSBtoRGB(tmp[0],tmp[1],tmp[2])&0xFFFFFF)|(alpha<<24);
        }else{
            float[] tmp=new float[3];
            tmp[0]=clamp(hOffset);
            tmp[1]=clamp(sOffset);
            tmp[2]=clamp(bOffset);
            int alpha=clamp(aOffset);
            argbValue=(Color.HSBtoRGB(tmp[0],tmp[1],tmp[2])&0xFFFFFF)|(alpha<<24);
        }
    }

    private float clamp(float value){
        if(value<0){
            value=0;
        }else if(value>1){
            value=1;
        }
        return value;
    }

    private int clamp(int value){
        if(value<0){
            value=0;
        }else if(value>255){
            value=255;
        }
        return value;
    }

    @Override
    public int getRGB(){
        return argbValue;
    }

    @Override
    public int hashCode(){
        int result=uiDefaultParentName.hashCode();
        result=31*result+hOffset!=+0.0f?
                Float.floatToIntBits(hOffset):0;
        result=31*result+sOffset!=+0.0f?
                Float.floatToIntBits(sOffset):0;
        result=31*result+bOffset!=+0.0f?
                Float.floatToIntBits(bOffset):0;
        result=31*result+aOffset;
        return result;
    }

    @Override
    public boolean equals(Object o){
        if(this==o) return true;
        if(!(o instanceof DerivedColor)) return false;
        DerivedColor that=(DerivedColor)o;
        if(aOffset!=that.aOffset) return false;
        if(Float.compare(that.bOffset,bOffset)!=0) return false;
        if(Float.compare(that.hOffset,hOffset)!=0) return false;
        if(Float.compare(that.sOffset,sOffset)!=0) return false;
        if(!uiDefaultParentName.equals(that.uiDefaultParentName)) return false;
        return true;
    }

    @Override
    public String toString(){
        Color src=UIManager.getColor(uiDefaultParentName);
        String s="DerivedColor(color="+getRed()+","+getGreen()+","+getBlue()+
                " parent="+uiDefaultParentName+
                " offsets="+getHueOffset()+","+getSaturationOffset()+","
                +getBrightnessOffset()+","+getAlphaOffset();
        return src==null?s:s+" pColor="+src.getRed()+","+src.getGreen()+","+src.getBlue();
    }

    public float getHueOffset(){
        return hOffset;
    }

    public float getSaturationOffset(){
        return sOffset;
    }

    public float getBrightnessOffset(){
        return bOffset;
    }

    public int getAlphaOffset(){
        return aOffset;
    }

    static class UIResource extends DerivedColor implements javax.swing.plaf.UIResource{
        UIResource(String uiDefaultParentName,float hOffset,float sOffset,
                   float bOffset,int aOffset){
            super(uiDefaultParentName,hOffset,sOffset,bOffset,aOffset);
        }

        @Override
        public boolean equals(Object o){
            return (o instanceof UIResource)&&super.equals(o);
        }

        @Override
        public int hashCode(){
            return super.hashCode()+7;
        }
    }
}
