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

import sun.awt.image.SunWritableRaster;
import sun.java2d.StateTrackable.State;
import sun.java2d.StateTrackableDelegate;

import java.lang.annotation.Native;

import static sun.java2d.StateTrackable.State.UNTRACKABLE;

public abstract class DataBuffer{
    @Native
    public static final int TYPE_BYTE=0;
    @Native
    public static final int TYPE_USHORT=1;
    @Native
    public static final int TYPE_SHORT=2;
    @Native
    public static final int TYPE_INT=3;
    @Native
    public static final int TYPE_FLOAT=4;
    @Native
    public static final int TYPE_DOUBLE=5;
    @Native
    public static final int TYPE_UNDEFINED=32;
    private static final int dataTypeSize[]={8,16,16,32,32,64};

    static{
        SunWritableRaster.setDataStealer(new SunWritableRaster.DataStealer(){
            public byte[] getData(DataBufferByte dbb,int bank){
                return dbb.bankdata[bank];
            }

            public short[] getData(DataBufferUShort dbus,int bank){
                return dbus.bankdata[bank];
            }

            public int[] getData(DataBufferInt dbi,int bank){
                return dbi.bankdata[bank];
            }

            public StateTrackableDelegate getTrackable(DataBuffer db){
                return db.theTrackable;
            }

            public void setTrackable(DataBuffer db,
                                     StateTrackableDelegate trackable){
                db.theTrackable=trackable;
            }
        });
    }

    protected int dataType;
    protected int banks;
    protected int offset;
    protected int size;
    protected int offsets[];
    StateTrackableDelegate theTrackable;

    protected DataBuffer(int dataType,int size){
        this(UNTRACKABLE,dataType,size);
    }

    DataBuffer(State initialState,
               int dataType,int size){
        this.theTrackable=StateTrackableDelegate.createInstance(initialState);
        this.dataType=dataType;
        this.banks=1;
        this.size=size;
        this.offset=0;
        this.offsets=new int[1];  // init to 0 by new
    }

    protected DataBuffer(int dataType,int size,int numBanks){
        this(UNTRACKABLE,dataType,size,numBanks);
    }

    DataBuffer(State initialState,
               int dataType,int size,int numBanks){
        this.theTrackable=StateTrackableDelegate.createInstance(initialState);
        this.dataType=dataType;
        this.banks=numBanks;
        this.size=size;
        this.offset=0;
        this.offsets=new int[banks]; // init to 0 by new
    }

    protected DataBuffer(int dataType,int size,int numBanks,int offset){
        this(UNTRACKABLE,dataType,size,numBanks,offset);
    }

    DataBuffer(State initialState,
               int dataType,int size,int numBanks,int offset){
        this.theTrackable=StateTrackableDelegate.createInstance(initialState);
        this.dataType=dataType;
        this.banks=numBanks;
        this.size=size;
        this.offset=offset;
        this.offsets=new int[numBanks];
        for(int i=0;i<numBanks;i++){
            this.offsets[i]=offset;
        }
    }

    protected DataBuffer(int dataType,int size,int numBanks,int offsets[]){
        this(UNTRACKABLE,dataType,size,numBanks,offsets);
    }

    DataBuffer(State initialState,
               int dataType,int size,int numBanks,int offsets[]){
        if(numBanks!=offsets.length){
            throw new ArrayIndexOutOfBoundsException("Number of banks"+
                    " does not match number of bank offsets");
        }
        this.theTrackable=StateTrackableDelegate.createInstance(initialState);
        this.dataType=dataType;
        this.banks=numBanks;
        this.size=size;
        this.offset=offsets[0];
        this.offsets=(int[])offsets.clone();
    }

    public static int getDataTypeSize(int type){
        if(type<TYPE_BYTE||type>TYPE_DOUBLE){
            throw new IllegalArgumentException("Unknown data type "+type);
        }
        return dataTypeSize[type];
    }

    static int[] toIntArray(Object obj){
        if(obj instanceof int[]){
            return (int[])obj;
        }else if(obj==null){
            return null;
        }else if(obj instanceof short[]){
            short sdata[]=(short[])obj;
            int idata[]=new int[sdata.length];
            for(int i=0;i<sdata.length;i++){
                idata[i]=(int)sdata[i]&0xffff;
            }
            return idata;
        }else if(obj instanceof byte[]){
            byte bdata[]=(byte[])obj;
            int idata[]=new int[bdata.length];
            for(int i=0;i<bdata.length;i++){
                idata[i]=0xff&(int)bdata[i];
            }
            return idata;
        }
        return null;
    }

    public int getDataType(){
        return dataType;
    }

    public int getSize(){
        return size;
    }

    public int getOffset(){
        return offset;
    }

    public int[] getOffsets(){
        return (int[])offsets.clone();
    }

    public int getNumBanks(){
        return banks;
    }

    public float getElemFloat(int i){
        return (float)getElem(i);
    }

    public int getElem(int i){
        return getElem(0,i);
    }

    public abstract int getElem(int bank,int i);

    public float getElemFloat(int bank,int i){
        return (float)getElem(bank,i);
    }

    public void setElemFloat(int i,float val){
        setElem(i,(int)val);
    }

    public void setElem(int i,int val){
        setElem(0,i,val);
    }

    public abstract void setElem(int bank,int i,int val);

    public void setElemFloat(int bank,int i,float val){
        setElem(bank,i,(int)val);
    }

    public double getElemDouble(int i){
        return (double)getElem(i);
    }

    public double getElemDouble(int bank,int i){
        return (double)getElem(bank,i);
    }

    public void setElemDouble(int i,double val){
        setElem(i,(int)val);
    }

    public void setElemDouble(int bank,int i,double val){
        setElem(bank,i,(int)val);
    }
}
