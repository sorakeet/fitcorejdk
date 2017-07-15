/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import java.io.Serializable;
import java.util.EventObject;

public abstract class AbstractCellEditor implements CellEditor, Serializable{
    protected EventListenerList listenerList=new EventListenerList();
    transient protected ChangeEvent changeEvent=null;
    // Force this to be implemented.
    // public Object  getCellEditorValue()

    public boolean isCellEditable(EventObject e){
        return true;
    }

    public boolean shouldSelectCell(EventObject anEvent){
        return true;
    }

    public boolean stopCellEditing(){
        fireEditingStopped();
        return true;
    }

    public void cancelCellEditing(){
        fireEditingCanceled();
    }

    public void addCellEditorListener(CellEditorListener l){
        listenerList.add(CellEditorListener.class,l);
    }

    public void removeCellEditorListener(CellEditorListener l){
        listenerList.remove(CellEditorListener.class,l);
    }

    protected void fireEditingCanceled(){
        // Guaranteed to return a non-null array
        Object[] listeners=listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i=listeners.length-2;i>=0;i-=2){
            if(listeners[i]==CellEditorListener.class){
                // Lazily create the event:
                if(changeEvent==null)
                    changeEvent=new ChangeEvent(this);
                ((CellEditorListener)listeners[i+1]).editingCanceled(changeEvent);
            }
        }
    }

    protected void fireEditingStopped(){
        // Guaranteed to return a non-null array
        Object[] listeners=listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i=listeners.length-2;i>=0;i-=2){
            if(listeners[i]==CellEditorListener.class){
                // Lazily create the event:
                if(changeEvent==null)
                    changeEvent=new ChangeEvent(this);
                ((CellEditorListener)listeners[i+1]).editingStopped(changeEvent);
            }
        }
    }

    public CellEditorListener[] getCellEditorListeners(){
        return listenerList.getListeners(CellEditorListener.class);
    }
}
