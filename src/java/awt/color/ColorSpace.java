/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * *********************************************************************
 * *********************************************************************
 * *********************************************************************
 * ** COPYRIGHT (c) Eastman Kodak Company, 1997                      ***
 * ** As  an unpublished  work pursuant to Title 17 of the United    ***
 * ** States Code.  All rights reserved.                             ***
 * *********************************************************************
 * *********************************************************************
 **********************************************************************/
/**
 **********************************************************************
 **********************************************************************
 **********************************************************************
 *** COPYRIGHT (c) Eastman Kodak Company, 1997                      ***
 *** As  an unpublished  work pursuant to Title 17 of the United    ***
 *** States Code.  All rights reserved.                             ***
 **********************************************************************
 **********************************************************************
 **********************************************************************/
package java.awt.color;

import sun.java2d.cmm.CMSManager;

import java.lang.annotation.Native;

public abstract class ColorSpace implements java.io.Serializable{
    @Native
    public static final int TYPE_XYZ=0;
    @Native
    public static final int TYPE_Lab=1;
    @Native
    public static final int TYPE_Luv=2;
    @Native
    public static final int TYPE_YCbCr=3;
    @Native
    public static final int TYPE_Yxy=4;
    @Native
    public static final int TYPE_RGB=5;
    @Native
    public static final int TYPE_GRAY=6;
    @Native
    public static final int TYPE_HSV=7;
    @Native
    public static final int TYPE_HLS=8;
    @Native
    public static final int TYPE_CMYK=9;
    @Native
    public static final int TYPE_CMY=11;
    @Native
    public static final int TYPE_2CLR=12;
    @Native
    public static final int TYPE_3CLR=13;
    @Native
    public static final int TYPE_4CLR=14;
    @Native
    public static final int TYPE_5CLR=15;
    @Native
    public static final int TYPE_6CLR=16;
    @Native
    public static final int TYPE_7CLR=17;
    @Native
    public static final int TYPE_8CLR=18;
    @Native
    public static final int TYPE_9CLR=19;
    @Native
    public static final int TYPE_ACLR=20;
    @Native
    public static final int TYPE_BCLR=21;
    @Native
    public static final int TYPE_CCLR=22;
    @Native
    public static final int TYPE_DCLR=23;
    @Native
    public static final int TYPE_ECLR=24;
    @Native
    public static final int TYPE_FCLR=25;
    @Native
    public static final int CS_sRGB=1000;
    @Native
    public static final int CS_LINEAR_RGB=1004;
    @Native
    public static final int CS_CIEXYZ=1001;
    @Native
    public static final int CS_PYCC=1002;
    @Native
    public static final int CS_GRAY=1003;
    static final long serialVersionUID=-409452704308689724L;
    // Cache of singletons for the predefined color spaces.
    private static ColorSpace sRGBspace;
    private static ColorSpace XYZspace;
    private static ColorSpace PYCCspace;
    private static ColorSpace GRAYspace;
    private static ColorSpace LINEAR_RGBspace;
    private int type;
    private int numComponents;
    private transient String[] compName=null;

    protected ColorSpace(int type,int numcomponents){
        this.type=type;
        this.numComponents=numcomponents;
    }

    // NOTE: This method may be called by privileged threads.
    //       DO NOT INVOKE CLIENT CODE ON THIS THREAD!
    public static ColorSpace getInstance(int colorspace){
        ColorSpace theColorSpace;
        switch(colorspace){
            case CS_sRGB:
                synchronized(ColorSpace.class){
                    if(sRGBspace==null){
                        ICC_Profile theProfile=ICC_Profile.getInstance(CS_sRGB);
                        sRGBspace=new ICC_ColorSpace(theProfile);
                    }
                    theColorSpace=sRGBspace;
                }
                break;
            case CS_CIEXYZ:
                synchronized(ColorSpace.class){
                    if(XYZspace==null){
                        ICC_Profile theProfile=
                                ICC_Profile.getInstance(CS_CIEXYZ);
                        XYZspace=new ICC_ColorSpace(theProfile);
                    }
                    theColorSpace=XYZspace;
                }
                break;
            case CS_PYCC:
                synchronized(ColorSpace.class){
                    if(PYCCspace==null){
                        ICC_Profile theProfile=ICC_Profile.getInstance(CS_PYCC);
                        PYCCspace=new ICC_ColorSpace(theProfile);
                    }
                    theColorSpace=PYCCspace;
                }
                break;
            case CS_GRAY:
                synchronized(ColorSpace.class){
                    if(GRAYspace==null){
                        ICC_Profile theProfile=ICC_Profile.getInstance(CS_GRAY);
                        GRAYspace=new ICC_ColorSpace(theProfile);
                        /** to allow access from java.awt.ColorModel */
                        CMSManager.GRAYspace=GRAYspace;
                    }
                    theColorSpace=GRAYspace;
                }
                break;
            case CS_LINEAR_RGB:
                synchronized(ColorSpace.class){
                    if(LINEAR_RGBspace==null){
                        ICC_Profile theProfile=
                                ICC_Profile.getInstance(CS_LINEAR_RGB);
                        LINEAR_RGBspace=new ICC_ColorSpace(theProfile);
                        /** to allow access from java.awt.ColorModel */
                        CMSManager.LINEAR_RGBspace=LINEAR_RGBspace;
                    }
                    theColorSpace=LINEAR_RGBspace;
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown color space");
        }
        return theColorSpace;
    }

    static boolean isCS_CIEXYZ(ColorSpace cspace){
        return (cspace==XYZspace);
    }

    public boolean isCS_sRGB(){
        /** REMIND - make sure we know sRGBspace exists already */
        return (this==sRGBspace);
    }

    public abstract float[] toRGB(float[] colorvalue);

    public abstract float[] fromRGB(float[] rgbvalue);

    public abstract float[] toCIEXYZ(float[] colorvalue);

    public abstract float[] fromCIEXYZ(float[] colorvalue);

    public int getType(){
        return type;
    }

    public int getNumComponents(){
        return numComponents;
    }

    public String getName(int idx){
        /** REMIND - handle common cases here */
        if((idx<0)||(idx>numComponents-1)){
            throw new IllegalArgumentException(
                    "Component index out of range: "+idx);
        }
        if(compName==null){
            switch(type){
                case ColorSpace.TYPE_XYZ:
                    compName=new String[]{"X","Y","Z"};
                    break;
                case ColorSpace.TYPE_Lab:
                    compName=new String[]{"L","a","b"};
                    break;
                case ColorSpace.TYPE_Luv:
                    compName=new String[]{"L","u","v"};
                    break;
                case ColorSpace.TYPE_YCbCr:
                    compName=new String[]{"Y","Cb","Cr"};
                    break;
                case ColorSpace.TYPE_Yxy:
                    compName=new String[]{"Y","x","y"};
                    break;
                case ColorSpace.TYPE_RGB:
                    compName=new String[]{"Red","Green","Blue"};
                    break;
                case ColorSpace.TYPE_GRAY:
                    compName=new String[]{"Gray"};
                    break;
                case ColorSpace.TYPE_HSV:
                    compName=new String[]{"Hue","Saturation","Value"};
                    break;
                case ColorSpace.TYPE_HLS:
                    compName=new String[]{"Hue","Lightness",
                            "Saturation"};
                    break;
                case ColorSpace.TYPE_CMYK:
                    compName=new String[]{"Cyan","Magenta","Yellow",
                            "Black"};
                    break;
                case ColorSpace.TYPE_CMY:
                    compName=new String[]{"Cyan","Magenta","Yellow"};
                    break;
                default:
                    String[] tmp=new String[numComponents];
                    for(int i=0;i<tmp.length;i++){
                        tmp[i]="Unnamed color component("+i+")";
                    }
                    compName=tmp;
            }
        }
        return compName[idx];
    }

    public float getMinValue(int component){
        if((component<0)||(component>numComponents-1)){
            throw new IllegalArgumentException(
                    "Component index out of range: "+component);
        }
        return 0.0f;
    }

    public float getMaxValue(int component){
        if((component<0)||(component>numComponents-1)){
            throw new IllegalArgumentException(
                    "Component index out of range: "+component);
        }
        return 1.0f;
    }
}
