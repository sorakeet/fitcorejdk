/**
 *
 * Copyright (c) 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
/**********************************************************************
 **********************************************************************
 **********************************************************************
 *** COPYRIGHT (c) 1997-1998 Eastman Kodak Company.                 ***
 *** As  an unpublished  work pursuant to Title 17 of the United    ***
 *** States Code.  All rights reserved.                             ***
 **********************************************************************
 **********************************************************************
 **********************************************************************/
package com.sun.image.codec.jpeg;

public class JPEGQTable{
    public static final JPEGQTable StdLuminance=new JPEGQTable();
    public static final JPEGQTable StdChrominance=new JPEGQTable();
    private static final byte QTABLESIZE=64;

    static{
        int[] lumVals={
                16,11,12,14,12,10,16,14,
                13,14,18,17,16,19,24,40,
                26,24,22,22,24,49,35,37,
                29,40,58,51,61,60,57,51,
                56,55,64,72,92,78,64,68,
                87,69,55,56,80,109,81,87,
                95,98,103,104,103,62,77,113,
                121,112,100,120,92,101,103,99
        };
        StdLuminance.quantval=lumVals;
    }

    static{
        int[] chromVals={
                17,18,18,24,21,24,47,26,
                26,47,99,66,56,66,99,99,
                99,99,99,99,99,99,99,99,
                99,99,99,99,99,99,99,99,
                99,99,99,99,99,99,99,99,
                99,99,99,99,99,99,99,99,
                99,99,99,99,99,99,99,99,
                99,99,99,99,99,99,99,99
        };
        StdChrominance.quantval=chromVals;
    }

    private int quantval[];

    private JPEGQTable(){
        quantval=new int[QTABLESIZE];
    }

    public JPEGQTable(int table[]){
        if(table.length!=QTABLESIZE){
            throw new IllegalArgumentException
                    ("Quantization table is the wrong size.");
        }else{
            quantval=new int[QTABLESIZE];
            System.arraycopy(table,0,quantval,0,QTABLESIZE);
        }
    }

    public int[] getTable(){
        int[] table=new int[QTABLESIZE];
        System.arraycopy(quantval,0,table,0,QTABLESIZE);
        return table;
    }

    public JPEGQTable getScaledInstance(float scaleFactor,
                                        boolean forceBaseline){
        long max=(forceBaseline)?255L:32767L;
        int[] ret=new int[QTABLESIZE];
        for(int i=0;i<QTABLESIZE;i++){
            long holder=(long)((quantval[i]*scaleFactor)+0.5);
            // limit to valid range
            if(holder<=0L) holder=1L;
            // Max quantizer for 12 bits
            if(holder>max) holder=max;
            ret[i]=(int)holder;
        }
        return new JPEGQTable(ret);
    }
}
