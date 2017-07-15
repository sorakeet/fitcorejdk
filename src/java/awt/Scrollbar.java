/**
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import javax.accessibility.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.peer.ScrollbarPeer;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.EventListener;

public class Scrollbar extends Component implements Adjustable, Accessible{
    public static final int HORIZONTAL=0;
    public static final int VERTICAL=1;
    private static final String base="scrollbar";
    private static final long serialVersionUID=8451667562882310543L;
    private static int nameCounter=0;

    static{
        /** ensure that the necessary native libraries are loaded */
        Toolkit.loadLibraries();
        if(!GraphicsEnvironment.isHeadless()){
            initIDs();
        }
    }

    int value;
    int maximum;
    int minimum;
    int visibleAmount;
    int orientation;
    int lineIncrement=1;
    int pageIncrement=10;
    transient boolean isAdjusting;
    transient AdjustmentListener adjustmentListener;
    private int scrollbarSerializedDataVersion=1;

    public Scrollbar() throws HeadlessException{
        this(VERTICAL,0,10,0,100);
    }

    public Scrollbar(int orientation,int value,int visible,int minimum,
                     int maximum) throws HeadlessException{
        GraphicsEnvironment.checkHeadless();
        switch(orientation){
            case HORIZONTAL:
            case VERTICAL:
                this.orientation=orientation;
                break;
            default:
                throw new IllegalArgumentException("illegal scrollbar orientation");
        }
        setValues(value,visible,minimum,maximum);
    }

    public void setValues(int value,int visible,int minimum,int maximum){
        int oldValue;
        synchronized(this){
            if(minimum==Integer.MAX_VALUE){
                minimum=Integer.MAX_VALUE-1;
            }
            if(maximum<=minimum){
                maximum=minimum+1;
            }
            long maxMinusMin=(long)maximum-(long)minimum;
            if(maxMinusMin>Integer.MAX_VALUE){
                maxMinusMin=Integer.MAX_VALUE;
                maximum=minimum+(int)maxMinusMin;
            }
            if(visible>(int)maxMinusMin){
                visible=(int)maxMinusMin;
            }
            if(visible<1){
                visible=1;
            }
            if(value<minimum){
                value=minimum;
            }
            if(value>maximum-visible){
                value=maximum-visible;
            }
            oldValue=this.value;
            this.value=value;
            this.visibleAmount=visible;
            this.minimum=minimum;
            this.maximum=maximum;
            ScrollbarPeer peer=(ScrollbarPeer)this.peer;
            if(peer!=null){
                peer.setValues(value,visibleAmount,minimum,maximum);
            }
        }
        if((oldValue!=value)&&(accessibleContext!=null)){
            accessibleContext.firePropertyChange(
                    AccessibleContext.ACCESSIBLE_VALUE_PROPERTY,
                    Integer.valueOf(oldValue),
                    Integer.valueOf(value));
        }
    }

    public Scrollbar(int orientation) throws HeadlessException{
        this(orientation,0,10,0,100);
    }

    setPageIncrement(v);

    private static native void initIDs();

    String constructComponentName(){
        synchronized(Scrollbar.class){
            return base+nameCounter++;
        }
    }

    // REMIND: remove when filtering is done at lower level
    boolean eventEnabled(AWTEvent e){
        if(e.id==AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED){
            if((eventMask&AWTEvent.ADJUSTMENT_EVENT_MASK)!=0||
                    adjustmentListener!=null){
                return true;
            }
            return false;
        }
        return super.eventEnabled(e);
    }

    public <T extends EventListener> T[] getListeners(Class<T> listenerType){
        EventListener l=null;
        if(listenerType==AdjustmentListener.class){
            l=adjustmentListener;
        }else{
            return super.getListeners(listenerType);
        }
        return AWTEventMulticaster.getListeners(l,listenerType);
    }    public int getValue(){
        return value;
    }

    protected void processEvent(AWTEvent e){
        if(e instanceof AdjustmentEvent){
            processAdjustmentEvent((AdjustmentEvent)e);
            return;
        }
        super.processEvent(e);
    }

    public void addNotify(){
        synchronized(getTreeLock()){
            if(peer==null)
                peer=getToolkit().createScrollbar(this);
            super.addNotify();
        }
    }    public void setValue(int newValue){
        // Use setValues so that a consistent policy relating
        // minimum, maximum, visible amount, and value is enforced.
        setValues(newValue,visibleAmount,minimum,maximum);
    }

    protected String paramString(){
        return super.paramString()+
                ",val="+value+
                ",vis="+visibleAmount+
                ",min="+minimum+
                ",max="+maximum+
                ((orientation==VERTICAL)?",vert":",horz")+
                ",isAdjusting="+isAdjusting;
    }

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleAWTScrollBar();
        }
        return accessibleContext;
    }    public int getMinimum(){
        return minimum;
    }

    protected void processAdjustmentEvent(AdjustmentEvent e){
        AdjustmentListener listener=adjustmentListener;
        if(listener!=null){
            listener.adjustmentValueChanged(e);
        }
    }

    public int getOrientation(){
        return orientation;
    }    public void setMinimum(int newMinimum){
        // No checks are necessary in this method since minimum is
        // the first variable checked in the setValues function.
        // Use setValues so that a consistent policy relating
        // minimum, maximum, visible amount, and value is enforced.
        setValues(value,visibleAmount,newMinimum,maximum);
    }

    public void setOrientation(int orientation){
        synchronized(getTreeLock()){
            if(orientation==this.orientation){
                return;
            }
            switch(orientation){
                case HORIZONTAL:
                case VERTICAL:
                    this.orientation=orientation;
                    break;
                default:
                    throw new IllegalArgumentException("illegal scrollbar orientation");
            }
            /** Create a new peer with the specified orientation. */
            if(peer!=null){
                removeNotify();
                addNotify();
                invalidate();
            }
        }
        if(accessibleContext!=null){
            accessibleContext.firePropertyChange(
                    AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                    ((orientation==VERTICAL)
                            ?AccessibleState.HORIZONTAL:AccessibleState.VERTICAL),
                    ((orientation==VERTICAL)
                            ?AccessibleState.VERTICAL:AccessibleState.HORIZONTAL));
        }
    }

    public boolean getValueIsAdjusting(){
        return isAdjusting;
    }    public int getMaximum(){
        return maximum;
    }

    public void setValueIsAdjusting(boolean b){
        boolean oldValue;
        synchronized(this){
            oldValue=isAdjusting;
            isAdjusting=b;
        }
        if((oldValue!=b)&&(accessibleContext!=null)){
            accessibleContext.firePropertyChange(
                    AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                    ((oldValue)?AccessibleState.BUSY:null),
                    ((b)?AccessibleState.BUSY:null));
        }
    }

    public synchronized AdjustmentListener[] getAdjustmentListeners(){
        return getListeners(AdjustmentListener.class);
    }    public void setMaximum(int newMaximum){
        // minimum is checked first in setValues, so we need to
        // enforce minimum and maximum checks here.
        if(newMaximum==Integer.MIN_VALUE){
            newMaximum=Integer.MIN_VALUE+1;
        }
        if(minimum>=newMaximum){
            minimum=newMaximum-1;
        }
        // Use setValues so that a consistent policy relating
        // minimum, maximum, visible amount, and value is enforced.
        setValues(value,visibleAmount,minimum,newMaximum);
    }

    private void writeObject(ObjectOutputStream s)
            throws IOException{
        s.defaultWriteObject();
        AWTEventMulticaster.save(s,adjustmentListenerK,adjustmentListener);
        s.writeObject(null);
    }

    private void readObject(ObjectInputStream s)
            throws ClassNotFoundException, IOException, HeadlessException{
        GraphicsEnvironment.checkHeadless();
        s.defaultReadObject();
        Object keyOrNull;
        while(null!=(keyOrNull=s.readObject())){
            String key=((String)keyOrNull).intern();
            if(adjustmentListenerK==key)
                addAdjustmentListener((AdjustmentListener)(s.readObject()));
            else // skip value for unrecognized key
                s.readObject();
        }
    }    public int getVisibleAmount(){
        return getVisible();
    }

    protected class AccessibleAWTScrollBar extends AccessibleAWTComponent
            implements AccessibleValue{
        private static final long serialVersionUID=-344337268523697807L;

        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.SCROLL_BAR;
        }

        public AccessibleStateSet getAccessibleStateSet(){
            AccessibleStateSet states=super.getAccessibleStateSet();
            if(getValueIsAdjusting()){
                states.add(AccessibleState.BUSY);
            }
            if(getOrientation()==VERTICAL){
                states.add(AccessibleState.VERTICAL);
            }else{
                states.add(AccessibleState.HORIZONTAL);
            }
            return states;
        }

        public AccessibleValue getAccessibleValue(){
            return this;
        }

        public Number getCurrentAccessibleValue(){
            return Integer.valueOf(getValue());
        }

        public boolean setCurrentAccessibleValue(Number n){
            if(n instanceof Integer){
                setValue(n.intValue());
                return true;
            }else{
                return false;
            }
        }

        public Number getMinimumAccessibleValue(){
            return Integer.valueOf(getMinimum());
        }

        public Number getMaximumAccessibleValue(){
            return Integer.valueOf(getMaximum());
        }
    } // AccessibleAWTScrollBar    public void setBlockIncrement(int v){

    @Deprecated
    public int getVisible(){
        return visibleAmount;
    }



    public void setVisibleAmount(int newAmount){
        // Use setValues so that a consistent policy relating
        // minimum, maximum, visible amount, and value is enforced.
        setValues(value,newAmount,minimum,maximum);
    }



    public void setUnitIncrement(int v){
        setLineIncrement(v);
    }



    @Deprecated
    public synchronized void setLineIncrement(int v){
        int tmp=(v<1)?1:v;
        if(lineIncrement==tmp){
            return;
        }
        lineIncrement=tmp;
        ScrollbarPeer peer=(ScrollbarPeer)this.peer;
        if(peer!=null){
            peer.setLineIncrement(lineIncrement);
        }
    }



    public int getUnitIncrement(){
        return getLineIncrement();
    }



    @Deprecated
    public int getLineIncrement(){
        return lineIncrement;
    }





}

    @Deprecated
    public synchronized void setPageIncrement(int v){
        int tmp=(v<1)?1:v;
        if(pageIncrement==tmp){
            return;
        }
        pageIncrement=tmp;
        ScrollbarPeer peer=(ScrollbarPeer)this.peer;
        if(peer!=null){
            peer.setPageIncrement(pageIncrement);
        }
    }

    public int getBlockIncrement(){
        return getPageIncrement();
    }

    @Deprecated
    public int getPageIncrement(){
        return pageIncrement;
    }

    public synchronized void addAdjustmentListener(AdjustmentListener l){
        if(l==null){
            return;
        }
        adjustmentListener=AWTEventMulticaster.add(adjustmentListener,l);
        newEventsOnly=true;
    }

    public synchronized void removeAdjustmentListener(AdjustmentListener l){
        if(l==null){
            return;
        }
        adjustmentListener=AWTEventMulticaster.remove(adjustmentListener,l);
    }
/////////////////
// Accessibility support
////////////////
}
