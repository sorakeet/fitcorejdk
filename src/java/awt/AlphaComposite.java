/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import sun.java2d.SunCompositeContext;

import java.awt.image.ColorModel;
import java.lang.annotation.Native;

public final class AlphaComposite implements Composite{
    @Native
    public static final int CLEAR=1;
    @Native
    public static final int SRC=2;
    @Native
    public static final int DST=9;
    // Note that DST was added in 1.4 so it is numbered out of order...
    @Native
    public static final int SRC_OVER=3;
    @Native
    public static final int DST_OVER=4;
    @Native
    public static final int SRC_IN=5;
    @Native
    public static final int DST_IN=6;
    @Native
    public static final int SRC_OUT=7;
    @Native
    public static final int DST_OUT=8;
    // Rule 9 is DST which is defined above where it fits into the
    // list logically, rather than numerically
    //
    // public static final int  DST             = 9;
    @Native
    public static final int SRC_ATOP=10;
    @Native
    public static final int DST_ATOP=11;
    @Native
    public static final int XOR=12;
    public static final AlphaComposite Clear=new AlphaComposite(CLEAR);
    public static final AlphaComposite Src=new AlphaComposite(SRC);
    public static final AlphaComposite Dst=new AlphaComposite(DST);
    public static final AlphaComposite SrcOver=new AlphaComposite(SRC_OVER);
    public static final AlphaComposite DstOver=new AlphaComposite(DST_OVER);
    public static final AlphaComposite SrcIn=new AlphaComposite(SRC_IN);
    public static final AlphaComposite DstIn=new AlphaComposite(DST_IN);
    public static final AlphaComposite SrcOut=new AlphaComposite(SRC_OUT);
    public static final AlphaComposite DstOut=new AlphaComposite(DST_OUT);
    public static final AlphaComposite SrcAtop=new AlphaComposite(SRC_ATOP);
    public static final AlphaComposite DstAtop=new AlphaComposite(DST_ATOP);
    public static final AlphaComposite Xor=new AlphaComposite(XOR);
    @Native
    private static final int MIN_RULE=CLEAR;
    @Native
    private static final int MAX_RULE=XOR;
    float extraAlpha;
    int rule;

    private AlphaComposite(int rule){
        this(rule,1.0f);
    }

    private AlphaComposite(int rule,float alpha){
        if(rule<MIN_RULE||rule>MAX_RULE){
            throw new IllegalArgumentException("unknown composite rule");
        }
        if(alpha>=0.0f&&alpha<=1.0f){
            this.rule=rule;
            this.extraAlpha=alpha;
        }else{
            throw new IllegalArgumentException("alpha value out of range");
        }
    }

    public CompositeContext createContext(ColorModel srcColorModel,
                                          ColorModel dstColorModel,
                                          RenderingHints hints){
        return new SunCompositeContext(this,srcColorModel,dstColorModel);
    }

    public float getAlpha(){
        return extraAlpha;
    }

    public int getRule(){
        return rule;
    }

    public AlphaComposite derive(int rule){
        return (this.rule==rule)
                ?this
                :getInstance(rule,this.extraAlpha);
    }

    public static AlphaComposite getInstance(int rule,float alpha){
        if(alpha==1.0f){
            return getInstance(rule);
        }
        return new AlphaComposite(rule,alpha);
    }

    public static AlphaComposite getInstance(int rule){
        switch(rule){
            case CLEAR:
                return Clear;
            case SRC:
                return Src;
            case DST:
                return Dst;
            case SRC_OVER:
                return SrcOver;
            case DST_OVER:
                return DstOver;
            case SRC_IN:
                return SrcIn;
            case DST_IN:
                return DstIn;
            case SRC_OUT:
                return SrcOut;
            case DST_OUT:
                return DstOut;
            case SRC_ATOP:
                return SrcAtop;
            case DST_ATOP:
                return DstAtop;
            case XOR:
                return Xor;
            default:
                throw new IllegalArgumentException("unknown composite rule");
        }
    }

    public AlphaComposite derive(float alpha){
        return (this.extraAlpha==alpha)
                ?this
                :getInstance(this.rule,alpha);
    }

    public int hashCode(){
        return (Float.floatToIntBits(extraAlpha)*31+rule);
    }

    public boolean equals(Object obj){
        if(!(obj instanceof AlphaComposite)){
            return false;
        }
        AlphaComposite ac=(AlphaComposite)obj;
        if(rule!=ac.rule){
            return false;
        }
        if(extraAlpha!=ac.extraAlpha){
            return false;
        }
        return true;
    }
}
