/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.image;

public class ShortLookupTable extends LookupTable{
    short data[][];

    public ShortLookupTable(int offset,short data[][]){
        super(offset,data.length);
        numComponents=data.length;
        numEntries=data[0].length;
        this.data=new short[numComponents][];
        // Allocate the array and copy the data reference
        for(int i=0;i<numComponents;i++){
            this.data[i]=data[i];
        }
    }

    public ShortLookupTable(int offset,short data[]){
        super(offset,data.length);
        numComponents=1;
        numEntries=data.length;
        this.data=new short[1][];
        this.data[0]=data;
    }

    public final short[][] getTable(){
        return data;
    }

    public int[] lookupPixel(int[] src,int[] dst){
        if(dst==null){
            // Need to alloc a new destination array
            dst=new int[src.length];
        }
        if(numComponents==1){
            // Apply one LUT to all channels
            for(int i=0;i<src.length;i++){
                int s=(src[i]&0xffff)-offset;
                if(s<0){
                    throw new ArrayIndexOutOfBoundsException("src["+i+
                            "]-offset is "+
                            "less than zero");
                }
                dst[i]=(int)data[0][s];
            }
        }else{
            for(int i=0;i<src.length;i++){
                int s=(src[i]&0xffff)-offset;
                if(s<0){
                    throw new ArrayIndexOutOfBoundsException("src["+i+
                            "]-offset is "+
                            "less than zero");
                }
                dst[i]=(int)data[i][s];
            }
        }
        return dst;
    }

    public short[] lookupPixel(short[] src,short[] dst){
        if(dst==null){
            // Need to alloc a new destination array
            dst=new short[src.length];
        }
        if(numComponents==1){
            // Apply one LUT to all channels
            for(int i=0;i<src.length;i++){
                int s=(src[i]&0xffff)-offset;
                if(s<0){
                    throw new ArrayIndexOutOfBoundsException("src["+i+
                            "]-offset is "+
                            "less than zero");
                }
                dst[i]=data[0][s];
            }
        }else{
            for(int i=0;i<src.length;i++){
                int s=(src[i]&0xffff)-offset;
                if(s<0){
                    throw new ArrayIndexOutOfBoundsException("src["+i+
                            "]-offset is "+
                            "less than zero");
                }
                dst[i]=data[i][s];
            }
        }
        return dst;
    }
}
