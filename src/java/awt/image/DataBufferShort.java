/**
 * Copyright (c) 1997, 2008, Oracle and/or its affiliates. All rights reserved.
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

import static sun.java2d.StateTrackable.State.STABLE;
import static sun.java2d.StateTrackable.State.UNTRACKABLE;

public final class DataBufferShort extends DataBuffer{
    short data[];
    short bankdata[][];

    public DataBufferShort(int size){
        super(STABLE,TYPE_SHORT,size);
        data=new short[size];
        bankdata=new short[1][];
        bankdata[0]=data;
    }

    public DataBufferShort(int size,int numBanks){
        super(STABLE,TYPE_SHORT,size,numBanks);
        bankdata=new short[numBanks][];
        for(int i=0;i<numBanks;i++){
            bankdata[i]=new short[size];
        }
        data=bankdata[0];
    }

    public DataBufferShort(short dataArray[],int size){
        super(UNTRACKABLE,TYPE_SHORT,size);
        data=dataArray;
        bankdata=new short[1][];
        bankdata[0]=data;
    }

    public DataBufferShort(short dataArray[],int size,int offset){
        super(UNTRACKABLE,TYPE_SHORT,size,1,offset);
        data=dataArray;
        bankdata=new short[1][];
        bankdata[0]=data;
    }

    public DataBufferShort(short dataArray[][],int size){
        super(UNTRACKABLE,TYPE_SHORT,size,dataArray.length);
        bankdata=(short[][])dataArray.clone();
        data=bankdata[0];
    }

    public DataBufferShort(short dataArray[][],int size,int offsets[]){
        super(UNTRACKABLE,TYPE_SHORT,size,dataArray.length,offsets);
        bankdata=(short[][])dataArray.clone();
        data=bankdata[0];
    }

    public short[] getData(){
        theTrackable.setUntrackable();
        return data;
    }

    public short[] getData(int bank){
        theTrackable.setUntrackable();
        return bankdata[bank];
    }

    public short[][] getBankData(){
        theTrackable.setUntrackable();
        return (short[][])bankdata.clone();
    }

    public int getElem(int i){
        return (int)(data[i+offset]);
    }

    public int getElem(int bank,int i){
        return (int)(bankdata[bank][i+offsets[bank]]);
    }

    public void setElem(int i,int val){
        data[i+offset]=(short)val;
        theTrackable.markDirty();
    }

    public void setElem(int bank,int i,int val){
        bankdata[bank][i+offsets[bank]]=(short)val;
        theTrackable.markDirty();
    }
}
