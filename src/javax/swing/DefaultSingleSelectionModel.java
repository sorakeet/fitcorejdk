/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import java.io.Serializable;
import java.util.EventListener;

public class DefaultSingleSelectionModel implements SingleSelectionModel,
        Serializable{
    protected transient ChangeEvent changeEvent=null;
    protected EventListenerList listenerList=new EventListenerList();
    private int index=-1;

    public ChangeListener[] getChangeListeners(){
        return listenerList.getListeners(ChangeListener.class);
    }    // implements javax.swing.SingleSelectionModel
    public int getSelectedIndex(){
        return index;
    }

    public <T extends EventListener> T[] getListeners(Class<T> listenerType){
        return listenerList.getListeners(listenerType);
    }    // implements javax.swing.SingleSelectionModel
    public void setSelectedIndex(int index){
        if(this.index!=index){
            this.index=index;
            fireStateChanged();
        }
    }

    // implements javax.swing.SingleSelectionModel
    public void clearSelection(){
        setSelectedIndex(-1);
    }

    // implements javax.swing.SingleSelectionModel
    public boolean isSelected(){
        boolean ret=false;
        if(getSelectedIndex()!=-1){
            ret=true;
        }
        return ret;
    }

    public void addChangeListener(ChangeListener l){
        listenerList.add(ChangeListener.class,l);
    }

    public void removeChangeListener(ChangeListener l){
        listenerList.remove(ChangeListener.class,l);
    }



    protected void fireStateChanged(){
        // Guaranteed to return a non-null array
        Object[] listeners=listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i=listeners.length-2;i>=0;i-=2){
            if(listeners[i]==ChangeListener.class){
                // Lazily create the event:
                if(changeEvent==null)
                    changeEvent=new ChangeEvent(this);
                ((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
            }
        }
    }


}
