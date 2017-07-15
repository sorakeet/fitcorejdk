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

public class MultiPixelPackedSampleModel extends SampleModel{
    int pixelBitStride;
    int bitMask;
    int pixelsPerDataElement;
    int dataElementSize;
    int dataBitOffset;
    int scanlineStride;

    public MultiPixelPackedSampleModel(int dataType,
                                       int w,
                                       int h,
                                       int numberOfBits){
        this(dataType,w,h,
                numberOfBits,
                (w*numberOfBits+DataBuffer.getDataTypeSize(dataType)-1)/
                        DataBuffer.getDataTypeSize(dataType),
                0);
        if(dataType!=DataBuffer.TYPE_BYTE&&
                dataType!=DataBuffer.TYPE_USHORT&&
                dataType!=DataBuffer.TYPE_INT){
            throw new IllegalArgumentException("Unsupported data type "+
                    dataType);
        }
    }

    public MultiPixelPackedSampleModel(int dataType,int w,int h,
                                       int numberOfBits,
                                       int scanlineStride,
                                       int dataBitOffset){
        super(dataType,w,h,1);
        if(dataType!=DataBuffer.TYPE_BYTE&&
                dataType!=DataBuffer.TYPE_USHORT&&
                dataType!=DataBuffer.TYPE_INT){
            throw new IllegalArgumentException("Unsupported data type "+
                    dataType);
        }
        this.dataType=dataType;
        this.pixelBitStride=numberOfBits;
        this.scanlineStride=scanlineStride;
        this.dataBitOffset=dataBitOffset;
        this.dataElementSize=DataBuffer.getDataTypeSize(dataType);
        this.pixelsPerDataElement=dataElementSize/numberOfBits;
        if(pixelsPerDataElement*numberOfBits!=dataElementSize){
            throw new RasterFormatException("MultiPixelPackedSampleModel "+
                    "does not allow pixels to "+
                    "span data element boundaries");
        }
        this.bitMask=(1<<numberOfBits)-1;
    }

    public int getNumDataElements(){
        return 1;
    }

    public int getTransferType(){
        if(pixelBitStride>16)
            return DataBuffer.TYPE_INT;
        else if(pixelBitStride>8)
            return DataBuffer.TYPE_USHORT;
        else
            return DataBuffer.TYPE_BYTE;
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
        int bitnum=dataBitOffset+x*pixelBitStride;
        int element=data.getElem(y*scanlineStride+bitnum/dataElementSize);
        int shift=dataElementSize-(bitnum&(dataElementSize-1))
                -pixelBitStride;
        pixels[0]=(element>>shift)&bitMask;
        return pixels;
    }

    public Object getDataElements(int x,int y,Object obj,DataBuffer data){
        if((x<0)||(y<0)||(x>=width)||(y>=height)){
            throw new ArrayIndexOutOfBoundsException
                    ("Coordinate out of bounds!");
        }
        int type=getTransferType();
        int bitnum=dataBitOffset+x*pixelBitStride;
        int shift=dataElementSize-(bitnum&(dataElementSize-1))
                -pixelBitStride;
        int element=0;
        switch(type){
            case DataBuffer.TYPE_BYTE:
                byte[] bdata;
                if(obj==null)
                    bdata=new byte[1];
                else
                    bdata=(byte[])obj;
                element=data.getElem(y*scanlineStride+
                        bitnum/dataElementSize);
                bdata[0]=(byte)((element>>shift)&bitMask);
                obj=(Object)bdata;
                break;
            case DataBuffer.TYPE_USHORT:
                short[] sdata;
                if(obj==null)
                    sdata=new short[1];
                else
                    sdata=(short[])obj;
                element=data.getElem(y*scanlineStride+
                        bitnum/dataElementSize);
                sdata[0]=(short)((element>>shift)&bitMask);
                obj=(Object)sdata;
                break;
            case DataBuffer.TYPE_INT:
                int[] idata;
                if(obj==null)
                    idata=new int[1];
                else
                    idata=(int[])obj;
                element=data.getElem(y*scanlineStride+
                        bitnum/dataElementSize);
                idata[0]=(element>>shift)&bitMask;
                obj=(Object)idata;
                break;
        }
        return obj;
    }

    public void setDataElements(int x,int y,Object obj,DataBuffer data){
        if((x<0)||(y<0)||(x>=width)||(y>=height)){
            throw new ArrayIndexOutOfBoundsException
                    ("Coordinate out of bounds!");
        }
        int type=getTransferType();
        int bitnum=dataBitOffset+x*pixelBitStride;
        int index=y*scanlineStride+(bitnum/dataElementSize);
        int shift=dataElementSize-(bitnum&(dataElementSize-1))
                -pixelBitStride;
        int element=data.getElem(index);
        element&=~(bitMask<<shift);
        switch(type){
            case DataBuffer.TYPE_BYTE:
                byte[] barray=(byte[])obj;
                element|=(((int)(barray[0])&0xff)&bitMask)<<shift;
                data.setElem(index,element);
                break;
            case DataBuffer.TYPE_USHORT:
                short[] sarray=(short[])obj;
                element|=(((int)(sarray[0])&0xffff)&bitMask)<<shift;
                data.setElem(index,element);
                break;
            case DataBuffer.TYPE_INT:
                int[] iarray=(int[])obj;
                element|=(iarray[0]&bitMask)<<shift;
                data.setElem(index,element);
                break;
        }
    }

    public int getSample(int x,int y,int b,DataBuffer data){
        // 'b' must be 0
        if((x<0)||(y<0)||(x>=width)||(y>=height)||
                (b!=0)){
            throw new ArrayIndexOutOfBoundsException
                    ("Coordinate out of bounds!");
        }
        int bitnum=dataBitOffset+x*pixelBitStride;
        int element=data.getElem(y*scanlineStride+bitnum/dataElementSize);
        int shift=dataElementSize-(bitnum&(dataElementSize-1))
                -pixelBitStride;
        return (element>>shift)&bitMask;
    }

    public void setPixel(int x,int y,int[] iArray,DataBuffer data){
        if((x<0)||(y<0)||(x>=width)||(y>=height)){
            throw new ArrayIndexOutOfBoundsException
                    ("Coordinate out of bounds!");
        }
        int bitnum=dataBitOffset+x*pixelBitStride;
        int index=y*scanlineStride+(bitnum/dataElementSize);
        int shift=dataElementSize-(bitnum&(dataElementSize-1))
                -pixelBitStride;
        int element=data.getElem(index);
        element&=~(bitMask<<shift);
        element|=(iArray[0]&bitMask)<<shift;
        data.setElem(index,element);
    }

    public void setSample(int x,int y,int b,int s,
                          DataBuffer data){
        // 'b' must be 0
        if((x<0)||(y<0)||(x>=width)||(y>=height)||
                (b!=0)){
            throw new ArrayIndexOutOfBoundsException
                    ("Coordinate out of bounds!");
        }
        int bitnum=dataBitOffset+x*pixelBitStride;
        int index=y*scanlineStride+(bitnum/dataElementSize);
        int shift=dataElementSize-(bitnum&(dataElementSize-1))
                -pixelBitStride;
        int element=data.getElem(index);
        element&=~(bitMask<<shift);
        element|=(s&bitMask)<<shift;
        data.setElem(index,element);
    }

    public SampleModel createCompatibleSampleModel(int w,int h){
        SampleModel sampleModel=
                new MultiPixelPackedSampleModel(dataType,w,h,pixelBitStride);
        return sampleModel;
    }

    public SampleModel createSubsetSampleModel(int bands[]){
        if(bands!=null){
            if(bands.length!=1)
                throw new RasterFormatException("MultiPixelPackedSampleModel has "
                        +"only one band.");
        }
        SampleModel sm=createCompatibleSampleModel(width,height);
        return sm;
    }

    public DataBuffer createDataBuffer(){
        DataBuffer dataBuffer=null;
        int size=(int)scanlineStride*height;
        switch(dataType){
            case DataBuffer.TYPE_BYTE:
                dataBuffer=new DataBufferByte(size+(dataBitOffset+7)/8);
                break;
            case DataBuffer.TYPE_USHORT:
                dataBuffer=new DataBufferUShort(size+(dataBitOffset+15)/16);
                break;
            case DataBuffer.TYPE_INT:
                dataBuffer=new DataBufferInt(size+(dataBitOffset+31)/32);
                break;
        }
        return dataBuffer;
    }

    public int[] getSampleSize(){
        int sampleSize[]={pixelBitStride};
        return sampleSize;
    }

    public int getSampleSize(int band){
        return pixelBitStride;
    }

    public int getOffset(int x,int y){
        int offset=y*scanlineStride;
        offset+=(x*pixelBitStride+dataBitOffset)/dataElementSize;
        return offset;
    }

    public int getBitOffset(int x){
        return (x*pixelBitStride+dataBitOffset)%dataElementSize;
    }

    public int getScanlineStride(){
        return scanlineStride;
    }

    public int getPixelBitStride(){
        return pixelBitStride;
    }

    public int getDataBitOffset(){
        return dataBitOffset;
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
        hash^=pixelBitStride;
        hash<<=8;
        hash^=bitMask;
        hash<<=8;
        hash^=pixelsPerDataElement;
        hash<<=8;
        hash^=dataElementSize;
        hash<<=8;
        hash^=dataBitOffset;
        hash<<=8;
        hash^=scanlineStride;
        return hash;
    }

    public boolean equals(Object o){
        if((o==null)||!(o instanceof MultiPixelPackedSampleModel)){
            return false;
        }
        MultiPixelPackedSampleModel that=(MultiPixelPackedSampleModel)o;
        return this.width==that.width&&
                this.height==that.height&&
                this.numBands==that.numBands&&
                this.dataType==that.dataType&&
                this.pixelBitStride==that.pixelBitStride&&
                this.bitMask==that.bitMask&&
                this.pixelsPerDataElement==that.pixelsPerDataElement&&
                this.dataElementSize==that.dataElementSize&&
                this.dataBitOffset==that.dataBitOffset&&
                this.scanlineStride==that.scanlineStride;
    }
}
