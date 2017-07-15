/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute.standard;

import javax.print.attribute.Attribute;
import javax.print.attribute.DocAttribute;
import javax.print.attribute.PrintJobAttribute;
import javax.print.attribute.PrintRequestAttribute;

public final class MediaPrintableArea
        implements DocAttribute, PrintRequestAttribute, PrintJobAttribute{
    public static final int INCH=25400;
    public static final int MM=1000;
    private static final long serialVersionUID=-1597171464050795793L;
    private int x, y, w, h;
    private int units;

    public MediaPrintableArea(float x,float y,float w,float h,int units){
        if((x<0.0)||(y<0.0)||(w<=0.0)||(h<=0.0)||
                (units<1)){
            throw new IllegalArgumentException("0 or negative value argument");
        }
        this.x=(int)(x*units+0.5f);
        this.y=(int)(y*units+0.5f);
        this.w=(int)(w*units+0.5f);
        this.h=(int)(h*units+0.5f);
    }

    public MediaPrintableArea(int x,int y,int w,int h,int units){
        if((x<0)||(y<0)||(w<=0)||(h<=0)||
                (units<1)){
            throw new IllegalArgumentException("0 or negative value argument");
        }
        this.x=x*units;
        this.y=y*units;
        this.w=w*units;
        this.h=h*units;
    }

    public final Class<? extends Attribute> getCategory(){
        return MediaPrintableArea.class;
    }

    public final String getName(){
        return "media-printable-area";
    }

    public int hashCode(){
        return x+37*y+43*w+47*h;
    }

    public boolean equals(Object object){
        boolean ret=false;
        if(object instanceof MediaPrintableArea){
            MediaPrintableArea mm=(MediaPrintableArea)object;
            if(x==mm.x&&y==mm.y&&w==mm.w&&h==mm.h){
                ret=true;
            }
        }
        return ret;
    }

    public String toString(){
        return (toString(MM,"mm"));
    }

    public String toString(int units,String unitsName){
        if(unitsName==null){
            unitsName="";
        }
        float[] vals=getPrintableArea(units);
        String str="("+vals[0]+","+vals[1]+")->("+vals[2]+","+vals[3]+")";
        return str+unitsName;
    }

    public float[] getPrintableArea(int units){
        return new float[]{getX(units),getY(units),
                getWidth(units),getHeight(units)};
    }

    public float getX(int units){
        return convertFromMicrometers(x,units);
    }

    private static float convertFromMicrometers(int x,int units){
        if(units<1){
            throw new IllegalArgumentException("units is < 1");
        }
        return ((float)x)/((float)units);
    }

    public float getY(int units){
        return convertFromMicrometers(y,units);
    }

    public float getWidth(int units){
        return convertFromMicrometers(w,units);
    }

    public float getHeight(int units){
        return convertFromMicrometers(h,units);
    }
}
