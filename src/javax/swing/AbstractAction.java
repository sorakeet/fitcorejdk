/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import sun.security.action.GetPropertyAction;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.AccessController;

public abstract class AbstractAction implements Action, Cloneable, Serializable{
    private static Boolean RECONFIGURE_ON_NULL;
    protected boolean enabled=true;
    protected SwingPropertyChangeSupport changeSupport;
    private transient ArrayTable arrayTable;

    public AbstractAction(){
    }

    public AbstractAction(String name){
        putValue(Action.NAME,name);
    }

    public AbstractAction(String name,Icon icon){
        this(name);
        putValue(Action.SMALL_ICON,icon);
    }

    static boolean shouldReconfigure(PropertyChangeEvent e){
        if(e.getPropertyName()==null){
            synchronized(AbstractAction.class){
                if(RECONFIGURE_ON_NULL==null){
                    RECONFIGURE_ON_NULL=Boolean.valueOf(
                            AccessController.doPrivileged(new GetPropertyAction(
                                    "swing.actions.reconfigureOnNull","false")));
                }
                return RECONFIGURE_ON_NULL;
            }
        }
        return false;
    }

    static void setEnabledFromAction(JComponent c,Action a){
        c.setEnabled((a!=null)?a.isEnabled():true);
    }

    static void setToolTipTextFromAction(JComponent c,Action a){
        c.setToolTipText(a!=null?
                (String)a.getValue(Action.SHORT_DESCRIPTION):null);
    }

    static boolean hasSelectedKey(Action a){
        return (a!=null&&a.getValue(Action.SELECTED_KEY)!=null);
    }

    static boolean isSelected(Action a){
        return Boolean.TRUE.equals(a.getValue(Action.SELECTED_KEY));
    }

    public Object getValue(String key){
        if(key=="enabled"){
            return enabled;
        }
        if(arrayTable==null){
            return null;
        }
        return arrayTable.get(key);
    }

    public void putValue(String key,Object newValue){
        Object oldValue=null;
        if(key=="enabled"){
            // Treat putValue("enabled") the same way as a call to setEnabled.
            // If we don't do this it means the two may get out of sync, and a
            // bogus property change notification would be sent.
            //
            // To avoid dependencies between putValue & setEnabled this
            // directly changes enabled. If we instead called setEnabled
            // to change enabled, it would be possible for stack
            // overflow in the case where a developer implemented setEnabled
            // in terms of putValue.
            if(newValue==null||!(newValue instanceof Boolean)){
                newValue=false;
            }
            oldValue=enabled;
            enabled=(Boolean)newValue;
        }else{
            if(arrayTable==null){
                arrayTable=new ArrayTable();
            }
            if(arrayTable.containsKey(key))
                oldValue=arrayTable.get(key);
            // Remove the entry for key if newValue is null
            // else put in the newValue for key.
            if(newValue==null){
                arrayTable.remove(key);
            }else{
                arrayTable.put(key,newValue);
            }
        }
        firePropertyChange(key,oldValue,newValue);
    }    public boolean isEnabled(){
        return enabled;
    }

    public Object[] getKeys(){
        if(arrayTable==null){
            return null;
        }
        Object[] keys=new Object[arrayTable.size()];
        arrayTable.getKeys(keys);
        return keys;
    }    public void setEnabled(boolean newValue){
        boolean oldValue=this.enabled;
        if(oldValue!=newValue){
            this.enabled=newValue;
            firePropertyChange("enabled",
                    Boolean.valueOf(oldValue),Boolean.valueOf(newValue));
        }
    }

    public synchronized PropertyChangeListener[] getPropertyChangeListeners(){
        if(changeSupport==null){
            return new PropertyChangeListener[0];
        }
        return changeSupport.getPropertyChangeListeners();
    }

    protected Object clone() throws CloneNotSupportedException{
        AbstractAction newAction=(AbstractAction)super.clone();
        synchronized(this){
            if(arrayTable!=null){
                newAction.arrayTable=(ArrayTable)arrayTable.clone();
            }
        }
        return newAction;
    }

    private void writeObject(ObjectOutputStream s) throws IOException{
        // Store the default fields
        s.defaultWriteObject();
        // And the keys
        ArrayTable.writeArrayTable(s,arrayTable);
    }    protected void firePropertyChange(String propertyName,Object oldValue,Object newValue){
        if(changeSupport==null||
                (oldValue!=null&&newValue!=null&&oldValue.equals(newValue))){
            return;
        }
        changeSupport.firePropertyChange(propertyName,oldValue,newValue);
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException,
            IOException{
        s.defaultReadObject();
        for(int counter=s.readInt()-1;counter>=0;counter--){
            putValue((String)s.readObject(),s.readObject());
        }
    }    public synchronized void addPropertyChangeListener(PropertyChangeListener listener){
        if(changeSupport==null){
            changeSupport=new SwingPropertyChangeSupport(this);
        }
        changeSupport.addPropertyChangeListener(listener);
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener listener){
        if(changeSupport==null){
            return;
        }
        changeSupport.removePropertyChangeListener(listener);
    }








}
