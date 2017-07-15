/**
 * Copyright (c) 1997, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.image;

import java.awt.*;
import java.awt.color.ColorSpace;

public abstract class PackedColorModel extends ColorModel{
    int[] maskArray;
    int[] maskOffsets;
    float[] scaleFactors;

    public PackedColorModel(ColorSpace space,int bits,
                            int[] colorMaskArray,int alphaMask,
                            boolean isAlphaPremultiplied,
                            int trans,int transferType){
        super(bits,PackedColorModel.createBitsArray(colorMaskArray,
                alphaMask),
                space,(alphaMask==0?false:true),
                isAlphaPremultiplied,trans,transferType);
        if(bits<1||bits>32){
            throw new IllegalArgumentException("Number of bits must be between"
                    +" 1 and 32.");
        }
        maskArray=new int[numComponents];
        maskOffsets=new int[numComponents];
        scaleFactors=new float[numComponents];
        for(int i=0;i<numColorComponents;i++){
            // Get the mask offset and #bits
            DecomposeMask(colorMaskArray[i],i,space.getName(i));
        }
        if(alphaMask!=0){
            DecomposeMask(alphaMask,numColorComponents,"alpha");
            if(nBits[numComponents-1]==1){
                transparency=Transparency.BITMASK;
            }
        }
    }

    private void DecomposeMask(int mask,int idx,String componentName){
        int off=0;
        int count=nBits[idx];
        // Store the mask
        maskArray[idx]=mask;
        // Now find the shift
        if(mask!=0){
            while((mask&1)==0){
                mask>>>=1;
                off++;
            }
        }
        if(off+count>pixel_bits){
            throw new IllegalArgumentException(componentName+" mask "+
                    Integer.toHexString(maskArray[idx])+
                    " overflows pixel (expecting "+
                    pixel_bits+" bits");
        }
        maskOffsets[idx]=off;
        if(count==0){
            // High enough to scale any 0-ff value down to 0.0, but not
            // high enough to get Infinity when scaling back to pixel bits
            scaleFactors[idx]=256.0f;
        }else{
            scaleFactors[idx]=255.0f/((1<<count)-1);
        }
    }

    private final static int[] createBitsArray(int[] colorMaskArray,
                                               int alphaMask){
        int numColors=colorMaskArray.length;
        int numAlpha=(alphaMask==0?0:1);
        int[] arr=new int[numColors+numAlpha];
        for(int i=0;i<numColors;i++){
            arr[i]=countBits(colorMaskArray[i]);
            if(arr[i]<0){
                throw new IllegalArgumentException("Noncontiguous color mask ("
                        +Integer.toHexString(colorMaskArray[i])+
                        "at index "+i);
            }
        }
        if(alphaMask!=0){
            arr[numColors]=countBits(alphaMask);
            if(arr[numColors]<0){
                throw new IllegalArgumentException("Noncontiguous alpha mask ("
                        +Integer.toHexString(alphaMask));
            }
        }
        return arr;
    }

    private final static int countBits(int mask){
        int count=0;
        if(mask!=0){
            while((mask&1)==0){
                mask>>>=1;
            }
            while((mask&1)==1){
                mask>>>=1;
                count++;
            }
        }
        if(mask!=0){
            return -1;
        }
        return count;
    }

    public PackedColorModel(ColorSpace space,int bits,int rmask,int gmask,
                            int bmask,int amask,
                            boolean isAlphaPremultiplied,
                            int trans,int transferType){
        super(bits,PackedColorModel.createBitsArray(rmask,gmask,bmask,
                amask),
                space,(amask==0?false:true),
                isAlphaPremultiplied,trans,transferType);
        if(space.getType()!=ColorSpace.TYPE_RGB){
            throw new IllegalArgumentException("ColorSpace must be TYPE_RGB.");
        }
        maskArray=new int[numComponents];
        maskOffsets=new int[numComponents];
        scaleFactors=new float[numComponents];
        DecomposeMask(rmask,0,"red");
        DecomposeMask(gmask,1,"green");
        DecomposeMask(bmask,2,"blue");
        if(amask!=0){
            DecomposeMask(amask,3,"alpha");
            if(nBits[3]==1){
                transparency=Transparency.BITMASK;
            }
        }
    }

    private final static int[] createBitsArray(int rmask,int gmask,int bmask,
                                               int amask){
        int[] arr=new int[3+(amask==0?0:1)];
        arr[0]=countBits(rmask);
        arr[1]=countBits(gmask);
        arr[2]=countBits(bmask);
        if(arr[0]<0){
            throw new IllegalArgumentException("Noncontiguous red mask ("
                    +Integer.toHexString(rmask));
        }else if(arr[1]<0){
            throw new IllegalArgumentException("Noncontiguous green mask ("
                    +Integer.toHexString(gmask));
        }else if(arr[2]<0){
            throw new IllegalArgumentException("Noncontiguous blue mask ("
                    +Integer.toHexString(bmask));
        }
        if(amask!=0){
            arr[3]=countBits(amask);
            if(arr[3]<0){
                throw new IllegalArgumentException("Noncontiguous alpha mask ("
                        +Integer.toHexString(amask));
            }
        }
        return arr;
    }

    final public int getMask(int index){
        return maskArray[index];
    }

    final public int[] getMasks(){
        return (int[])maskArray.clone();
    }

    public boolean equals(Object obj){
        if(!(obj instanceof PackedColorModel)){
            return false;
        }
        if(!super.equals(obj)){
            return false;
        }
        PackedColorModel cm=(PackedColorModel)obj;
        int numC=cm.getNumComponents();
        if(numC!=numComponents){
            return false;
        }
        for(int i=0;i<numC;i++){
            if(maskArray[i]!=cm.getMask(i)){
                return false;
            }
        }
        return true;
    }

    public SampleModel createCompatibleSampleModel(int w,int h){
        return new SinglePixelPackedSampleModel(transferType,w,h,
                maskArray);
    }

    public boolean isCompatibleSampleModel(SampleModel sm){
        if(!(sm instanceof SinglePixelPackedSampleModel)){
            return false;
        }
        // Must have the same number of components
        if(numComponents!=sm.getNumBands()){
            return false;
        }
        // Transfer type must be the same
        if(sm.getTransferType()!=transferType){
            return false;
        }
        SinglePixelPackedSampleModel sppsm=(SinglePixelPackedSampleModel)sm;
        // Now compare the specific masks
        int[] bitMasks=sppsm.getBitMasks();
        if(bitMasks.length!=maskArray.length){
            return false;
        }
        /** compare 'effective' masks only, i.e. only part of the mask
         * which fits the capacity of the transfer type.
         */
        int maxMask=(int)((1L<<DataBuffer.getDataTypeSize(transferType))-1);
        for(int i=0;i<bitMasks.length;i++){
            if((maxMask&bitMasks[i])!=(maxMask&maskArray[i])){
                return false;
            }
        }
        return true;
    }

    public WritableRaster getAlphaRaster(WritableRaster raster){
        if(hasAlpha()==false){
            return null;
        }
        int x=raster.getMinX();
        int y=raster.getMinY();
        int[] band=new int[1];
        band[0]=raster.getNumBands()-1;
        return raster.createWritableChild(x,y,raster.getWidth(),
                raster.getHeight(),x,y,
                band);
    }
}
