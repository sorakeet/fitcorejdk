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
 * ***************************************************************
 * *****************************************************************
 * *****************************************************************
 * ** COPYRIGHT (c) Eastman Kodak Company, 1997
 * ** As  an unpublished  work pursuant to Title 17 of the United
 * ** States Code.  All rights reserved.
 * *****************************************************************
 * *****************************************************************
 ******************************************************************/
/** ****************************************************************
 ******************************************************************
 ******************************************************************
 *** COPYRIGHT (c) Eastman Kodak Company, 1997
 *** As  an unpublished  work pursuant to Title 17 of the United
 *** States Code.  All rights reserved.
 ******************************************************************
 ******************************************************************
 ******************************************************************/
package java.awt.image;

import java.util.Arrays;

public class ComponentSampleModel extends SampleModel{
    static{
        ColorModel.loadLibraries();
        initIDs();
    }

    protected int bandOffsets[];
    protected int[] bankIndices;
    protected int numBands=1;
    protected int numBanks=1;
    protected int scanlineStride;
    protected int pixelStride;

    public ComponentSampleModel(int dataType,
                                int w,int h,
                                int pixelStride,
                                int scanlineStride,
                                int bandOffsets[]){
        super(dataType,w,h,bandOffsets.length);
        this.dataType=dataType;
        this.pixelStride=pixelStride;
        this.scanlineStride=scanlineStride;
        this.bandOffsets=(int[])bandOffsets.clone();
        numBands=this.bandOffsets.length;
        if(pixelStride<0){
            throw new IllegalArgumentException("Pixel stride must be >= 0");
        }
        // TODO - bug 4296691 - remove this check
        if(scanlineStride<0){
            throw new IllegalArgumentException("Scanline stride must be >= 0");
        }
        if(numBands<1){
            throw new IllegalArgumentException("Must have at least one band.");
        }
        if((dataType<DataBuffer.TYPE_BYTE)||
                (dataType>DataBuffer.TYPE_DOUBLE)){
            throw new IllegalArgumentException("Unsupported dataType.");
        }
        bankIndices=new int[numBands];
        for(int i=0;i<numBands;i++){
            bankIndices[i]=0;
        }
        verify();
    }

    private void verify(){
        int requiredSize=getBufferSize();
    }

    private int getBufferSize(){
        int maxBandOff=bandOffsets[0];
        for(int i=1;i<bandOffsets.length;i++){
            maxBandOff=Math.max(maxBandOff,bandOffsets[i]);
        }
        if(maxBandOff<0||maxBandOff>(Integer.MAX_VALUE-1)){
            throw new IllegalArgumentException("Invalid band offset");
        }
        if(pixelStride<0||pixelStride>(Integer.MAX_VALUE/width)){
            throw new IllegalArgumentException("Invalid pixel stride");
        }
        if(scanlineStride<0||scanlineStride>(Integer.MAX_VALUE/height)){
            throw new IllegalArgumentException("Invalid scanline stride");
        }
        int size=maxBandOff+1;
        int val=pixelStride*(width-1);
        if(val>(Integer.MAX_VALUE-size)){
            throw new IllegalArgumentException("Invalid pixel stride");
        }
        size+=val;
        val=scanlineStride*(height-1);
        if(val>(Integer.MAX_VALUE-size)){
            throw new IllegalArgumentException("Invalid scan stride");
        }
        size+=val;
        return size;
    }

    public ComponentSampleModel(int dataType,
                                int w,int h,
                                int pixelStride,
                                int scanlineStride,
                                int bankIndices[],
                                int bandOffsets[]){
        super(dataType,w,h,bandOffsets.length);
        this.dataType=dataType;
        this.pixelStride=pixelStride;
        this.scanlineStride=scanlineStride;
        this.bandOffsets=(int[])bandOffsets.clone();
        this.bankIndices=(int[])bankIndices.clone();
        if(pixelStride<0){
            throw new IllegalArgumentException("Pixel stride must be >= 0");
        }
        // TODO - bug 4296691 - remove this check
        if(scanlineStride<0){
            throw new IllegalArgumentException("Scanline stride must be >= 0");
        }
        if((dataType<DataBuffer.TYPE_BYTE)||
                (dataType>DataBuffer.TYPE_DOUBLE)){
            throw new IllegalArgumentException("Unsupported dataType.");
        }
        int maxBank=this.bankIndices[0];
        if(maxBank<0){
            throw new IllegalArgumentException("Index of bank 0 is less than "+
                    "0 ("+maxBank+")");
        }
        for(int i=1;i<this.bankIndices.length;i++){
            if(this.bankIndices[i]>maxBank){
                maxBank=this.bankIndices[i];
            }else if(this.bankIndices[i]<0){
                throw new IllegalArgumentException("Index of bank "+i+
                        " is less than 0 ("+
                        maxBank+")");
            }
        }
        numBanks=maxBank+1;
        numBands=this.bandOffsets.length;
        if(this.bandOffsets.length!=this.bankIndices.length){
            throw new IllegalArgumentException("Length of bandOffsets must "+
                    "equal length of bankIndices.");
        }
        verify();
    }

    static private native void initIDs();

    public int getOffset(int x,int y){
        int offset=y*scanlineStride+x*pixelStride+bandOffsets[0];
        return offset;
    }

    public int getOffset(int x,int y,int b){
        int offset=y*scanlineStride+x*pixelStride+bandOffsets[b];
        return offset;
    }    int[] orderBands(int orig[],int step){
        int map[]=new int[orig.length];
        int ret[]=new int[orig.length];
        for(int i=0;i<map.length;i++) map[i]=i;
        for(int i=0;i<ret.length;i++){
            int index=i;
            for(int j=i+1;j<ret.length;j++){
                if(orig[map[index]]>orig[map[j]]){
                    index=j;
                }
            }
            ret[map[index]]=i*step;
            map[index]=map[i];
        }
        return ret;
    }

    public final int[] getBankIndices(){
        return (int[])bankIndices.clone();
    }

    public final int[] getBandOffsets(){
        return (int[])bandOffsets.clone();
    }    public SampleModel createCompatibleSampleModel(int w,int h){
        SampleModel ret=null;
        long size;
        int minBandOff=bandOffsets[0];
        int maxBandOff=bandOffsets[0];
        for(int i=1;i<bandOffsets.length;i++){
            minBandOff=Math.min(minBandOff,bandOffsets[i]);
            maxBandOff=Math.max(maxBandOff,bandOffsets[i]);
        }
        maxBandOff-=minBandOff;
        int bands=bandOffsets.length;
        int bandOff[];
        int pStride=Math.abs(pixelStride);
        int lStride=Math.abs(scanlineStride);
        int bStride=Math.abs(maxBandOff);
        if(pStride>lStride){
            if(pStride>bStride){
                if(lStride>bStride){ // pix > line > band
                    bandOff=new int[bandOffsets.length];
                    for(int i=0;i<bands;i++)
                        bandOff[i]=bandOffsets[i]-minBandOff;
                    lStride=bStride+1;
                    pStride=lStride*h;
                }else{ // pix > band > line
                    bandOff=orderBands(bandOffsets,lStride*h);
                    pStride=bands*lStride*h;
                }
            }else{ // band > pix > line
                pStride=lStride*h;
                bandOff=orderBands(bandOffsets,pStride*w);
            }
        }else{
            if(pStride>bStride){ // line > pix > band
                bandOff=new int[bandOffsets.length];
                for(int i=0;i<bands;i++)
                    bandOff[i]=bandOffsets[i]-minBandOff;
                pStride=bStride+1;
                lStride=pStride*w;
            }else{
                if(lStride>bStride){ // line > band > pix
                    bandOff=orderBands(bandOffsets,pStride*w);
                    lStride=bands*pStride*w;
                }else{ // band > line > pix
                    lStride=pStride*w;
                    bandOff=orderBands(bandOffsets,lStride*h);
                }
            }
        }
        // make sure we make room for negative offsets...
        int base=0;
        if(scanlineStride<0){
            base+=lStride*h;
            lStride*=-1;
        }
        if(pixelStride<0){
            base+=pStride*w;
            pStride*=-1;
        }
        for(int i=0;i<bands;i++)
            bandOff[i]+=base;
        return new ComponentSampleModel(dataType,w,h,pStride,
                lStride,bankIndices,bandOff);
    }

    public final int getScanlineStride(){
        return scanlineStride;
    }

    public final int getPixelStride(){
        return pixelStride;
    }    public SampleModel createSubsetSampleModel(int bands[]){
        if(bands.length>bankIndices.length)
            throw new RasterFormatException("There are only "+
                    bankIndices.length+
                    " bands");
        int newBankIndices[]=new int[bands.length];
        int newBandOffsets[]=new int[bands.length];
        for(int i=0;i<bands.length;i++){
            newBankIndices[i]=bankIndices[bands[i]];
            newBandOffsets[i]=bandOffsets[bands[i]];
        }
        return new ComponentSampleModel(this.dataType,width,height,
                this.pixelStride,
                this.scanlineStride,
                newBankIndices,newBandOffsets);
    }

    // If we implement equals() we must also implement hashCode
    public int hashCode(){
        int hash=0;
        hash=width;
        hash<<=8;
        hash^=height;
        hash<<=8;
        hash^=numBands;
        hash<<=8;
        hash^=dataType;
        hash<<=8;
        for(int i=0;i<bandOffsets.length;i++){
            hash^=bandOffsets[i];
            hash<<=8;
        }
        for(int i=0;i<bankIndices.length;i++){
            hash^=bankIndices[i];
            hash<<=8;
        }
        hash^=numBands;
        hash<<=8;
        hash^=numBanks;
        hash<<=8;
        hash^=scanlineStride;
        hash<<=8;
        hash^=pixelStride;
        return hash;
    }

    public boolean equals(Object o){
        if((o==null)||!(o instanceof ComponentSampleModel)){
            return false;
        }
        ComponentSampleModel that=(ComponentSampleModel)o;
        return this.width==that.width&&
                this.height==that.height&&
                this.numBands==that.numBands&&
                this.dataType==that.dataType&&
                Arrays.equals(this.bandOffsets,that.bandOffsets)&&
                Arrays.equals(this.bankIndices,that.bankIndices)&&
                this.numBands==that.numBands&&
                this.numBanks==that.numBanks&&
                this.scanlineStride==that.scanlineStride&&
                this.pixelStride==that.pixelStride;
    }    public DataBuffer createDataBuffer(){
        DataBuffer dataBuffer=null;
        int size=getBufferSize();
        switch(dataType){
            case DataBuffer.TYPE_BYTE:
                dataBuffer=new DataBufferByte(size,numBanks);
                break;
            case DataBuffer.TYPE_USHORT:
                dataBuffer=new DataBufferUShort(size,numBanks);
                break;
            case DataBuffer.TYPE_SHORT:
                dataBuffer=new DataBufferShort(size,numBanks);
                break;
            case DataBuffer.TYPE_INT:
                dataBuffer=new DataBufferInt(size,numBanks);
                break;
            case DataBuffer.TYPE_FLOAT:
                dataBuffer=new DataBufferFloat(size,numBanks);
                break;
            case DataBuffer.TYPE_DOUBLE:
                dataBuffer=new DataBufferDouble(size,numBanks);
                break;
        }
        return dataBuffer;
    }







    public final int[] getSampleSize(){
        int sampleSize[]=new int[numBands];
        int sizeInBits=getSampleSize(0);
        for(int i=0;i<numBands;i++)
            sampleSize[i]=sizeInBits;
        return sampleSize;
    }



    public final int getSampleSize(int band){
        return DataBuffer.getDataTypeSize(dataType);
    }

    public final int getNumDataElements(){
        return getNumBands();
    }

    public Object getDataElements(int x,int y,Object obj,DataBuffer data){
        if((x<0)||(y<0)||(x>=width)||(y>=height)){
            throw new ArrayIndexOutOfBoundsException
                    ("Coordinate out of bounds!");
        }
        int type=getTransferType();
        int numDataElems=getNumDataElements();
        int pixelOffset=y*scanlineStride+x*pixelStride;
        switch(type){
            case DataBuffer.TYPE_BYTE:
                byte[] bdata;
                if(obj==null)
                    bdata=new byte[numDataElems];
                else
                    bdata=(byte[])obj;
                for(int i=0;i<numDataElems;i++){
                    bdata[i]=(byte)data.getElem(bankIndices[i],
                            pixelOffset+bandOffsets[i]);
                }
                obj=(Object)bdata;
                break;
            case DataBuffer.TYPE_USHORT:
            case DataBuffer.TYPE_SHORT:
                short[] sdata;
                if(obj==null)
                    sdata=new short[numDataElems];
                else
                    sdata=(short[])obj;
                for(int i=0;i<numDataElems;i++){
                    sdata[i]=(short)data.getElem(bankIndices[i],
                            pixelOffset+bandOffsets[i]);
                }
                obj=(Object)sdata;
                break;
            case DataBuffer.TYPE_INT:
                int[] idata;
                if(obj==null)
                    idata=new int[numDataElems];
                else
                    idata=(int[])obj;
                for(int i=0;i<numDataElems;i++){
                    idata[i]=data.getElem(bankIndices[i],
                            pixelOffset+bandOffsets[i]);
                }
                obj=(Object)idata;
                break;
            case DataBuffer.TYPE_FLOAT:
                float[] fdata;
                if(obj==null)
                    fdata=new float[numDataElems];
                else
                    fdata=(float[])obj;
                for(int i=0;i<numDataElems;i++){
                    fdata[i]=data.getElemFloat(bankIndices[i],
                            pixelOffset+bandOffsets[i]);
                }
                obj=(Object)fdata;
                break;
            case DataBuffer.TYPE_DOUBLE:
                double[] ddata;
                if(obj==null)
                    ddata=new double[numDataElems];
                else
                    ddata=(double[])obj;
                for(int i=0;i<numDataElems;i++){
                    ddata[i]=data.getElemDouble(bankIndices[i],
                            pixelOffset+bandOffsets[i]);
                }
                obj=(Object)ddata;
                break;
        }
        return obj;
    }

    public int[] getPixel(int x,int y,int iArray[],DataBuffer data){
        if((x<0)||(y<0)||(x>=width)||(y>=height)){
            throw new ArrayIndexOutOfBoundsException
                    ("Coordinate out of bounds!");
        }
        int pixels[];
        if(iArray!=null){
            pixels=iArray;
        }else{
            pixels=new int[numBands];
        }
        int pixelOffset=y*scanlineStride+x*pixelStride;
        for(int i=0;i<numBands;i++){
            pixels[i]=data.getElem(bankIndices[i],
                    pixelOffset+bandOffsets[i]);
        }
        return pixels;
    }

    public int[] getPixels(int x,int y,int w,int h,
                           int iArray[],DataBuffer data){
        int x1=x+w;
        int y1=y+h;
        if(x<0||x>=width||w>width||x1<0||x1>width||
                y<0||y>=height||y>height||y1<0||y1>height){
            throw new ArrayIndexOutOfBoundsException
                    ("Coordinate out of bounds!");
        }
        int pixels[];
        if(iArray!=null){
            pixels=iArray;
        }else{
            pixels=new int[w*h*numBands];
        }
        int lineOffset=y*scanlineStride+x*pixelStride;
        int srcOffset=0;
        for(int i=0;i<h;i++){
            int pixelOffset=lineOffset;
            for(int j=0;j<w;j++){
                for(int k=0;k<numBands;k++){
                    pixels[srcOffset++]=
                            data.getElem(bankIndices[k],pixelOffset+bandOffsets[k]);
                }
                pixelOffset+=pixelStride;
            }
            lineOffset+=scanlineStride;
        }
        return pixels;
    }

    public int getSample(int x,int y,int b,DataBuffer data){
        // Bounds check for 'b' will be performed automatically
        if((x<0)||(y<0)||(x>=width)||(y>=height)){
            throw new ArrayIndexOutOfBoundsException
                    ("Coordinate out of bounds!");
        }
        int sample=data.getElem(bankIndices[b],
                y*scanlineStride+x*pixelStride+
                        bandOffsets[b]);
        return sample;
    }

    public float getSampleFloat(int x,int y,int b,DataBuffer data){
        // Bounds check for 'b' will be performed automatically
        if((x<0)||(y<0)||(x>=width)||(y>=height)){
            throw new ArrayIndexOutOfBoundsException
                    ("Coordinate out of bounds!");
        }
        float sample=data.getElemFloat(bankIndices[b],
                y*scanlineStride+x*pixelStride+
                        bandOffsets[b]);
        return sample;
    }

    public double getSampleDouble(int x,int y,int b,DataBuffer data){
        // Bounds check for 'b' will be performed automatically
        if((x<0)||(y<0)||(x>=width)||(y>=height)){
            throw new ArrayIndexOutOfBoundsException
                    ("Coordinate out of bounds!");
        }
        double sample=data.getElemDouble(bankIndices[b],
                y*scanlineStride+x*pixelStride+
                        bandOffsets[b]);
        return sample;
    }

    public int[] getSamples(int x,int y,int w,int h,int b,
                            int iArray[],DataBuffer data){
        // Bounds check for 'b' will be performed automatically
        if((x<0)||(y<0)||(x+w>width)||(y+h>height)){
            throw new ArrayIndexOutOfBoundsException
                    ("Coordinate out of bounds!");
        }
        int samples[];
        if(iArray!=null){
            samples=iArray;
        }else{
            samples=new int[w*h];
        }
        int lineOffset=y*scanlineStride+x*pixelStride+bandOffsets[b];
        int srcOffset=0;
        for(int i=0;i<h;i++){
            int sampleOffset=lineOffset;
            for(int j=0;j<w;j++){
                samples[srcOffset++]=data.getElem(bankIndices[b],
                        sampleOffset);
                sampleOffset+=pixelStride;
            }
            lineOffset+=scanlineStride;
        }
        return samples;
    }

    public void setDataElements(int x,int y,Object obj,DataBuffer data){
        if((x<0)||(y<0)||(x>=width)||(y>=height)){
            throw new ArrayIndexOutOfBoundsException
                    ("Coordinate out of bounds!");
        }
        int type=getTransferType();
        int numDataElems=getNumDataElements();
        int pixelOffset=y*scanlineStride+x*pixelStride;
        switch(type){
            case DataBuffer.TYPE_BYTE:
                byte[] barray=(byte[])obj;
                for(int i=0;i<numDataElems;i++){
                    data.setElem(bankIndices[i],pixelOffset+bandOffsets[i],
                            ((int)barray[i])&0xff);
                }
                break;
            case DataBuffer.TYPE_USHORT:
            case DataBuffer.TYPE_SHORT:
                short[] sarray=(short[])obj;
                for(int i=0;i<numDataElems;i++){
                    data.setElem(bankIndices[i],pixelOffset+bandOffsets[i],
                            ((int)sarray[i])&0xffff);
                }
                break;
            case DataBuffer.TYPE_INT:
                int[] iarray=(int[])obj;
                for(int i=0;i<numDataElems;i++){
                    data.setElem(bankIndices[i],
                            pixelOffset+bandOffsets[i],iarray[i]);
                }
                break;
            case DataBuffer.TYPE_FLOAT:
                float[] farray=(float[])obj;
                for(int i=0;i<numDataElems;i++){
                    data.setElemFloat(bankIndices[i],
                            pixelOffset+bandOffsets[i],farray[i]);
                }
                break;
            case DataBuffer.TYPE_DOUBLE:
                double[] darray=(double[])obj;
                for(int i=0;i<numDataElems;i++){
                    data.setElemDouble(bankIndices[i],
                            pixelOffset+bandOffsets[i],darray[i]);
                }
                break;
        }
    }

    public void setPixel(int x,int y,int iArray[],DataBuffer data){
        if((x<0)||(y<0)||(x>=width)||(y>=height)){
            throw new ArrayIndexOutOfBoundsException
                    ("Coordinate out of bounds!");
        }
        int pixelOffset=y*scanlineStride+x*pixelStride;
        for(int i=0;i<numBands;i++){
            data.setElem(bankIndices[i],
                    pixelOffset+bandOffsets[i],iArray[i]);
        }
    }

    public void setPixels(int x,int y,int w,int h,
                          int iArray[],DataBuffer data){
        int x1=x+w;
        int y1=y+h;
        if(x<0||x>=width||w>width||x1<0||x1>width||
                y<0||y>=height||h>height||y1<0||y1>height){
            throw new ArrayIndexOutOfBoundsException
                    ("Coordinate out of bounds!");
        }
        int lineOffset=y*scanlineStride+x*pixelStride;
        int srcOffset=0;
        for(int i=0;i<h;i++){
            int pixelOffset=lineOffset;
            for(int j=0;j<w;j++){
                for(int k=0;k<numBands;k++){
                    data.setElem(bankIndices[k],pixelOffset+bandOffsets[k],
                            iArray[srcOffset++]);
                }
                pixelOffset+=pixelStride;
            }
            lineOffset+=scanlineStride;
        }
    }

    public void setSample(int x,int y,int b,int s,
                          DataBuffer data){
        // Bounds check for 'b' will be performed automatically
        if((x<0)||(y<0)||(x>=width)||(y>=height)){
            throw new ArrayIndexOutOfBoundsException
                    ("Coordinate out of bounds!");
        }
        data.setElem(bankIndices[b],
                y*scanlineStride+x*pixelStride+bandOffsets[b],s);
    }

    public void setSample(int x,int y,int b,
                          float s,
                          DataBuffer data){
        // Bounds check for 'b' will be performed automatically
        if((x<0)||(y<0)||(x>=width)||(y>=height)){
            throw new ArrayIndexOutOfBoundsException
                    ("Coordinate out of bounds!");
        }
        data.setElemFloat(bankIndices[b],
                y*scanlineStride+x*pixelStride+bandOffsets[b],
                s);
    }

    public void setSample(int x,int y,int b,
                          double s,
                          DataBuffer data){
        // Bounds check for 'b' will be performed automatically
        if((x<0)||(y<0)||(x>=width)||(y>=height)){
            throw new ArrayIndexOutOfBoundsException
                    ("Coordinate out of bounds!");
        }
        data.setElemDouble(bankIndices[b],
                y*scanlineStride+x*pixelStride+bandOffsets[b],
                s);
    }

    public void setSamples(int x,int y,int w,int h,int b,
                           int iArray[],DataBuffer data){
        // Bounds check for 'b' will be performed automatically
        if((x<0)||(y<0)||(x+w>width)||(y+h>height)){
            throw new ArrayIndexOutOfBoundsException
                    ("Coordinate out of bounds!");
        }
        int lineOffset=y*scanlineStride+x*pixelStride+bandOffsets[b];
        int srcOffset=0;
        for(int i=0;i<h;i++){
            int sampleOffset=lineOffset;
            for(int j=0;j<w;j++){
                data.setElem(bankIndices[b],sampleOffset,iArray[srcOffset++]);
                sampleOffset+=pixelStride;
            }
            lineOffset+=scanlineStride;
        }
    }
}
