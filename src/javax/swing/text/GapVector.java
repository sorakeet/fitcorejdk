/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

import java.io.Serializable;

abstract class GapVector implements Serializable{
    // ---- variables -----------------------------------
    private Object array;
    private int g0;
    private int g1;

    public GapVector(){
        this(10);
    }

    public GapVector(int initialLength){
        array=allocateArray(initialLength);
        g0=0;
        g1=initialLength;
    }

    protected abstract Object allocateArray(int len);

    protected final Object getArray(){
        return array;
    }

    protected final int getGapStart(){
        return g0;
    }

    protected final int getGapEnd(){
        return g1;
    }

    protected void replace(int position,int rmSize,Object addItems,int addSize){
        int addOffset=0;
        if(addSize==0){
            close(position,rmSize);
            return;
        }else if(rmSize>addSize){
            /** Shrink the end. */
            close(position+addSize,rmSize-addSize);
        }else{
            /** Grow the end, do two chunks. */
            int endSize=addSize-rmSize;
            int end=open(position+rmSize,endSize);
            System.arraycopy(addItems,rmSize,array,end,endSize);
            addSize=rmSize;
        }
        System.arraycopy(addItems,addOffset,array,position,addSize);
    }
    // --- gap management -------------------------------

    void close(int position,int nItems){
        if(nItems==0) return;
        int end=position+nItems;
        int new_gs=(g1-g0)+nItems;
        if(end<=g0){
            // Move gap to end of block.
            if(g0!=end){
                shiftGap(end);
            }
            // Adjust g0.
            shiftGapStartDown(g0-nItems);
        }else if(position>=g0){
            // Move gap to beginning of block.
            if(g0!=position){
                shiftGap(position);
            }
            // Adjust g1.
            shiftGapEndUp(g0+new_gs);
        }else{
            // The gap is properly inside the target block.
            // No data movement necessary, simply move both gap pointers.
            shiftGapStartDown(position);
            shiftGapEndUp(g0+new_gs);
        }
    }

    protected void shiftGap(int newGapStart){
        if(newGapStart==g0){
            return;
        }
        int oldGapStart=g0;
        int dg=newGapStart-oldGapStart;
        int oldGapEnd=g1;
        int newGapEnd=oldGapEnd+dg;
        int gapSize=oldGapEnd-oldGapStart;
        g0=newGapStart;
        g1=newGapEnd;
        if(dg>0){
            // Move gap up, move data down.
            System.arraycopy(array,oldGapEnd,array,oldGapStart,dg);
        }else if(dg<0){
            // Move gap down, move data up.
            System.arraycopy(array,newGapStart,array,newGapEnd,-dg);
        }
    }

    protected void shiftGapStartDown(int newGapStart){
        g0=newGapStart;
    }

    protected void shiftGapEndUp(int newGapEnd){
        g1=newGapEnd;
    }

    int open(int position,int nItems){
        int gapSize=g1-g0;
        if(nItems==0){
            if(position>g0)
                position+=gapSize;
            return position;
        }
        // Expand the array if the gap is too small.
        shiftGap(position);
        if(nItems>=gapSize){
            // Pre-shift the gap, to reduce total movement.
            shiftEnd(getArrayLength()-gapSize+nItems);
            gapSize=g1-g0;
        }
        g0=g0+nItems;
        return position;
    }

    protected void shiftEnd(int newSize){
        int oldSize=getArrayLength();
        int oldGapEnd=g1;
        int upperSize=oldSize-oldGapEnd;
        int arrayLength=getNewArraySize(newSize);
        int newGapEnd=arrayLength-upperSize;
        resize(arrayLength);
        g1=newGapEnd;
        if(upperSize!=0){
            // Copy array items to new end of array.
            System.arraycopy(array,oldGapEnd,array,newGapEnd,upperSize);
        }
    }

    protected abstract int getArrayLength();

    void resize(int nsize){
        Object narray=allocateArray(nsize);
        System.arraycopy(array,0,narray,0,Math.min(nsize,getArrayLength()));
        array=narray;
    }

    int getNewArraySize(int reqSize){
        return (reqSize+1)*2;
    }
}
