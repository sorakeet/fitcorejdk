/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.image;

public class PixelInterleavedSampleModel extends ComponentSampleModel{
    public PixelInterleavedSampleModel(int dataType,
                                       int w,int h,
                                       int pixelStride,
                                       int scanlineStride,
                                       int bandOffsets[]){
        super(dataType,w,h,pixelStride,scanlineStride,bandOffsets);
        int minBandOff=this.bandOffsets[0];
        int maxBandOff=this.bandOffsets[0];
        for(int i=1;i<this.bandOffsets.length;i++){
            minBandOff=Math.min(minBandOff,this.bandOffsets[i]);
            maxBandOff=Math.max(maxBandOff,this.bandOffsets[i]);
        }
        maxBandOff-=minBandOff;
        if(maxBandOff>scanlineStride){
            throw new IllegalArgumentException("Offsets between bands must be"+
                    " less than the scanline "+
                    " stride");
        }
        if(pixelStride*w>scanlineStride){
            throw new IllegalArgumentException("Pixel stride times width "+
                    "must be less than or "+
                    "equal to the scanline "+
                    "stride");
        }
        if(pixelStride<maxBandOff){
            throw new IllegalArgumentException("Pixel stride must be greater"+
                    " than or equal to the offsets"+
                    " between bands");
        }
    }

    public SampleModel createCompatibleSampleModel(int w,int h){
        int minBandoff=bandOffsets[0];
        int numBands=bandOffsets.length;
        for(int i=1;i<numBands;i++){
            if(bandOffsets[i]<minBandoff){
                minBandoff=bandOffsets[i];
            }
        }
        int[] bandOff;
        if(minBandoff>0){
            bandOff=new int[numBands];
            for(int i=0;i<numBands;i++){
                bandOff[i]=bandOffsets[i]-minBandoff;
            }
        }else{
            bandOff=bandOffsets;
        }
        return new PixelInterleavedSampleModel(dataType,w,h,pixelStride,
                pixelStride*w,bandOff);
    }

    public SampleModel createSubsetSampleModel(int bands[]){
        int newBandOffsets[]=new int[bands.length];
        for(int i=0;i<bands.length;i++){
            newBandOffsets[i]=bandOffsets[bands[i]];
        }
        return new PixelInterleavedSampleModel(this.dataType,width,height,
                this.pixelStride,
                scanlineStride,newBandOffsets);
    }

    // Differentiate hash code from other ComponentSampleModel subclasses
    public int hashCode(){
        return super.hashCode()^0x1;
    }
}
