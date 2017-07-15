/**
 * Copyright (c) 1998, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.colorchooser;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.io.Serializable;

public class DefaultColorSelectionModel implements ColorSelectionModel, Serializable{
    protected transient ChangeEvent changeEvent=null;
    protected EventListenerList listenerList=new EventListenerList();
    private Color selectedColor;

    public DefaultColorSelectionModel(){
        selectedColor=Color.white;
    }

    public DefaultColorSelectionModel(Color color){
        selectedColor=color;
    }

    public Color getSelectedColor(){
        return selectedColor;
    }

    public void setSelectedColor(Color color){
        if(color!=null&&!selectedColor.equals(color)){
            selectedColor=color;
            fireStateChanged();
        }
    }

    public void addChangeListener(ChangeListener l){
        listenerList.add(ChangeListener.class,l);
    }

    public void removeChangeListener(ChangeListener l){
        listenerList.remove(ChangeListener.class,l);
    }

    protected void fireStateChanged(){
        Object[] listeners=listenerList.getListenerList();
        for(int i=listeners.length-2;i>=0;i-=2){
            if(listeners[i]==ChangeListener.class){
                if(changeEvent==null){
                    changeEvent=new ChangeEvent(this);
                }
                ((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
            }
        }
    }

    public ChangeListener[] getChangeListeners(){
        return listenerList.getListeners(ChangeListener.class);
    }
}
