/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.beans;

import java.io.*;
import java.util.Hashtable;
import java.util.Map.Entry;

public class VetoableChangeSupport implements Serializable{
    static final long serialVersionUID=-5090210921595982017L;
    private static final ObjectStreamField[] serialPersistentFields={
            new ObjectStreamField("children",Hashtable.class),
            new ObjectStreamField("source",Object.class),
            new ObjectStreamField("vetoableChangeSupportSerializedDataVersion",Integer.TYPE)
    };
    private VetoableChangeListenerMap map=new VetoableChangeListenerMap();
    private Object source;

    public VetoableChangeSupport(Object sourceBean){
        if(sourceBean==null){
            throw new NullPointerException();
        }
        source=sourceBean;
    }

    public void addVetoableChangeListener(VetoableChangeListener listener){
        if(listener==null){
            return;
        }
        if(listener instanceof VetoableChangeListenerProxy){
            VetoableChangeListenerProxy proxy=
                    (VetoableChangeListenerProxy)listener;
            // Call two argument add method.
            addVetoableChangeListener(proxy.getPropertyName(),
                    proxy.getListener());
        }else{
            this.map.add(null,listener);
        }
    }

    public void addVetoableChangeListener(
            String propertyName,
            VetoableChangeListener listener){
        if(listener==null||propertyName==null){
            return;
        }
        listener=this.map.extract(listener);
        if(listener!=null){
            this.map.add(propertyName,listener);
        }
    }

    public void removeVetoableChangeListener(VetoableChangeListener listener){
        if(listener==null){
            return;
        }
        if(listener instanceof VetoableChangeListenerProxy){
            VetoableChangeListenerProxy proxy=
                    (VetoableChangeListenerProxy)listener;
            // Call two argument remove method.
            removeVetoableChangeListener(proxy.getPropertyName(),
                    proxy.getListener());
        }else{
            this.map.remove(null,listener);
        }
    }

    public void removeVetoableChangeListener(
            String propertyName,
            VetoableChangeListener listener){
        if(listener==null||propertyName==null){
            return;
        }
        listener=this.map.extract(listener);
        if(listener!=null){
            this.map.remove(propertyName,listener);
        }
    }

    public VetoableChangeListener[] getVetoableChangeListeners(){
        return this.map.getListeners();
    }

    public VetoableChangeListener[] getVetoableChangeListeners(String propertyName){
        return this.map.getListeners(propertyName);
    }

    public void fireVetoableChange(String propertyName,int oldValue,int newValue)
            throws PropertyVetoException{
        if(oldValue!=newValue){
            fireVetoableChange(propertyName,Integer.valueOf(oldValue),Integer.valueOf(newValue));
        }
    }

    public void fireVetoableChange(String propertyName,Object oldValue,Object newValue)
            throws PropertyVetoException{
        if(oldValue==null||newValue==null||!oldValue.equals(newValue)){
            fireVetoableChange(new PropertyChangeEvent(this.source,propertyName,oldValue,newValue));
        }
    }

    public void fireVetoableChange(PropertyChangeEvent event)
            throws PropertyVetoException{
        Object oldValue=event.getOldValue();
        Object newValue=event.getNewValue();
        if(oldValue==null||newValue==null||!oldValue.equals(newValue)){
            String name=event.getPropertyName();
            VetoableChangeListener[] common=this.map.get(null);
            VetoableChangeListener[] named=(name!=null)
                    ?this.map.get(name)
                    :null;
            VetoableChangeListener[] listeners;
            if(common==null){
                listeners=named;
            }else if(named==null){
                listeners=common;
            }else{
                listeners=new VetoableChangeListener[common.length+named.length];
                System.arraycopy(common,0,listeners,0,common.length);
                System.arraycopy(named,0,listeners,common.length,named.length);
            }
            if(listeners!=null){
                int current=0;
                try{
                    while(current<listeners.length){
                        listeners[current].vetoableChange(event);
                        current++;
                    }
                }catch(PropertyVetoException veto){
                    event=new PropertyChangeEvent(this.source,name,newValue,oldValue);
                    for(int i=0;i<current;i++){
                        try{
                            listeners[i].vetoableChange(event);
                        }catch(PropertyVetoException exception){
                            // ignore exceptions that occur during rolling back
                        }
                    }
                    throw veto; // rethrow the veto exception
                }
            }
        }
    }

    public void fireVetoableChange(String propertyName,boolean oldValue,boolean newValue)
            throws PropertyVetoException{
        if(oldValue!=newValue){
            fireVetoableChange(propertyName,Boolean.valueOf(oldValue),Boolean.valueOf(newValue));
        }
    }

    public boolean hasListeners(String propertyName){
        return this.map.hasListeners(propertyName);
    }

    private void writeObject(ObjectOutputStream s) throws IOException{
        Hashtable<String,VetoableChangeSupport> children=null;
        VetoableChangeListener[] listeners=null;
        synchronized(this.map){
            for(Entry<String,VetoableChangeListener[]> entry : this.map.getEntries()){
                String property=entry.getKey();
                if(property==null){
                    listeners=entry.getValue();
                }else{
                    if(children==null){
                        children=new Hashtable<>();
                    }
                    VetoableChangeSupport vcs=new VetoableChangeSupport(this.source);
                    vcs.map.set(null,entry.getValue());
                    children.put(property,vcs);
                }
            }
        }
        ObjectOutputStream.PutField fields=s.putFields();
        fields.put("children",children);
        fields.put("source",this.source);
        fields.put("vetoableChangeSupportSerializedDataVersion",2);
        s.writeFields();
        if(listeners!=null){
            for(VetoableChangeListener l : listeners){
                if(l instanceof Serializable){
                    s.writeObject(l);
                }
            }
        }
        s.writeObject(null);
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException{
        this.map=new VetoableChangeListenerMap();
        ObjectInputStream.GetField fields=s.readFields();
        @SuppressWarnings("unchecked")
        Hashtable<String,VetoableChangeSupport> children=(Hashtable<String,VetoableChangeSupport>)fields.get("children",null);
        this.source=fields.get("source",null);
        fields.get("vetoableChangeSupportSerializedDataVersion",2);
        Object listenerOrNull;
        while(null!=(listenerOrNull=s.readObject())){
            this.map.add(null,(VetoableChangeListener)listenerOrNull);
        }
        if(children!=null){
            for(Entry<String,VetoableChangeSupport> entry : children.entrySet()){
                for(VetoableChangeListener listener : entry.getValue().getVetoableChangeListeners()){
                    this.map.add(entry.getKey(),listener);
                }
            }
        }
    }

    private static final class VetoableChangeListenerMap extends ChangeListenerMap<VetoableChangeListener>{
        private static final VetoableChangeListener[] EMPTY={};

        @Override
        protected VetoableChangeListener[] newArray(int length){
            return (0<length)
                    ?new VetoableChangeListener[length]
                    :EMPTY;
        }

        @Override
        protected VetoableChangeListener newProxy(String name,VetoableChangeListener listener){
            return new VetoableChangeListenerProxy(name,listener);
        }

        public final VetoableChangeListener extract(VetoableChangeListener listener){
            while(listener instanceof VetoableChangeListenerProxy){
                listener=((VetoableChangeListenerProxy)listener).getListener();
            }
            return listener;
        }
    }
}
