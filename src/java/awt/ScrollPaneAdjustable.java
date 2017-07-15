/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import sun.awt.AWTAccessor;

import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.peer.ScrollPanePeer;
import java.io.Serializable;

public class ScrollPaneAdjustable implements Adjustable, Serializable{
    private static final String SCROLLPANE_ONLY=
            "Can be set by scrollpane only";
    private static final long serialVersionUID=-3359745691033257079L;

    static{
        Toolkit.loadLibraries();
        if(!GraphicsEnvironment.isHeadless()){
            initIDs();
        }
        AWTAccessor.setScrollPaneAdjustableAccessor(new AWTAccessor.ScrollPaneAdjustableAccessor(){
            public void setTypedValue(final ScrollPaneAdjustable adj,
                                      final int v,final int type){
                adj.setTypedValue(v,type);
            }
        });
    }

    private ScrollPane sp;
    private int orientation;
    private int value;
    private int minimum;
    private int maximum;
    private int visibleAmount;
    private transient boolean isAdjusting;
    private int unitIncrement=1;
    private int blockIncrement=1;
    private AdjustmentListener adjustmentListener;

    ScrollPaneAdjustable(ScrollPane sp,AdjustmentListener l,int orientation){
        this.sp=sp;
        this.orientation=orientation;
        addAdjustmentListener(l);
    }

    private static native void initIDs();

    void setSpan(int min,int max,int visible){
        // adjust the values to be reasonable
        minimum=min;
        maximum=Math.max(max,minimum+1);
        visibleAmount=Math.min(visible,maximum-minimum);
        visibleAmount=Math.max(visibleAmount,1);
        blockIncrement=Math.max((int)(visible*.90),1);
        setValue(value);
    }

    public int getOrientation(){
        return orientation;
    }

    public boolean getValueIsAdjusting(){
        return isAdjusting;
    }

    public void setValueIsAdjusting(boolean b){
        if(isAdjusting!=b){
            isAdjusting=b;
            AdjustmentEvent e=
                    new AdjustmentEvent(this,
                            AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED,
                            AdjustmentEvent.TRACK,value,b);
            adjustmentListener.adjustmentValueChanged(e);
        }
    }    public void setMinimum(int min){
        throw new AWTError(SCROLLPANE_ONLY);
    }

    public synchronized AdjustmentListener[] getAdjustmentListeners(){
        return (AdjustmentListener[])(AWTEventMulticaster.getListeners(
                adjustmentListener,
                AdjustmentListener.class));
    }

    public String toString(){
        return getClass().getName()+"["+paramString()+"]";
    }    public int getMinimum(){
        // XXX: This relies on setSpan always being called with 0 for
        // the minimum (which is currently true).
        return 0;
    }

    public String paramString(){
        return ((orientation==Adjustable.VERTICAL?"vertical,"
                :"horizontal,")
                +"[0.."+maximum+"]"
                +",val="+value
                +",vis="+visibleAmount
                +",unit="+unitIncrement
                +",block="+blockIncrement
                +",isAdjusting="+isAdjusting);
    }

    public void setMaximum(int max){
        throw new AWTError(SCROLLPANE_ONLY);
    }



    public int getMaximum(){
        return maximum;
    }



    public synchronized void setUnitIncrement(int u){
        if(u!=unitIncrement){
            unitIncrement=u;
            if(sp.peer!=null){
                ScrollPanePeer peer=(ScrollPanePeer)sp.peer;
                peer.setUnitIncrement(this,u);
            }
        }
    }

    public int getUnitIncrement(){
        return unitIncrement;
    }

    public synchronized void setBlockIncrement(int b){
        blockIncrement=b;
    }

    public int getBlockIncrement(){
        return blockIncrement;
    }

    public void setVisibleAmount(int v){
        throw new AWTError(SCROLLPANE_ONLY);
    }

    public int getVisibleAmount(){
        return visibleAmount;
    }

    public void setValue(int v){
        setTypedValue(v,AdjustmentEvent.TRACK);
    }

    private void setTypedValue(int v,int type){
        v=Math.max(v,minimum);
        v=Math.min(v,maximum-visibleAmount);
        if(v!=value){
            value=v;
            // Synchronously notify the listeners so that they are
            // guaranteed to be up-to-date with the Adjustable before
            // it is mutated again.
            AdjustmentEvent e=
                    new AdjustmentEvent(this,
                            AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED,
                            type,value,isAdjusting);
            adjustmentListener.adjustmentValueChanged(e);
        }
    }

    public int getValue(){
        return value;
    }

    public synchronized void addAdjustmentListener(AdjustmentListener l){
        if(l==null){
            return;
        }
        adjustmentListener=AWTEventMulticaster.add(adjustmentListener,l);
    }

    public synchronized void removeAdjustmentListener(AdjustmentListener l){
        if(l==null){
            return;
        }
        adjustmentListener=AWTEventMulticaster.remove(adjustmentListener,l);
    }
}
