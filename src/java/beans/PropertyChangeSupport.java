/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.beans;

import java.io.*;
import java.util.Hashtable;
import java.util.Map.Entry;

public class PropertyChangeSupport implements Serializable{
    static final long serialVersionUID=6401253773779951803L;
    private static final ObjectStreamField[] serialPersistentFields={
            new ObjectStreamField("children",Hashtable.class),
            new ObjectStreamField("source",Object.class),
            new ObjectStreamField("propertyChangeSupportSerializedDataVersion",Integer.TYPE)
    };
    private PropertyChangeListenerMap map=new PropertyChangeListenerMap();
    private Object source;

    public PropertyChangeSupport(Object sourceBean){
        if(sourceBean==null){
            throw new NullPointerException();
        }
        source=sourceBean;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener){
        if(listener==null){
            return;
        }
        if(listener instanceof PropertyChangeListenerProxy){
            PropertyChangeListenerProxy proxy=
                    (PropertyChangeListenerProxy)listener;
            // Call two argument add method.
            addPropertyChangeListener(proxy.getPropertyName(),
                    proxy.getListener());
        }else{
            this.map.add(null,listener);
        }
    }

    public void addPropertyChangeListener(
            String propertyName,
            PropertyChangeListener listener){
        if(listener==null||propertyName==null){
            return;
        }
        listener=this.map.extract(listener);
        if(listener!=null){
            this.map.add(propertyName,listener);
        }
    }

    public void removePropertyChangeListener(PropertyChangeListener listener){
        if(listener==null){
            return;
        }
        if(listener instanceof PropertyChangeListenerProxy){
            PropertyChangeListenerProxy proxy=
                    (PropertyChangeListenerProxy)listener;
            // Call two argument remove method.
            removePropertyChangeListener(proxy.getPropertyName(),
                    proxy.getListener());
        }else{
            this.map.remove(null,listener);
        }
    }

    public void removePropertyChangeListener(
            String propertyName,
            PropertyChangeListener listener){
        if(listener==null||propertyName==null){
            return;
        }
        listener=this.map.extract(listener);
        if(listener!=null){
            this.map.remove(propertyName,listener);
        }
    }

    public PropertyChangeListener[] getPropertyChangeListeners(){
        return this.map.getListeners();
    }

    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName){
        return this.map.getListeners(propertyName);
    }

    public void firePropertyChange(String propertyName,int oldValue,int newValue){
        if(oldValue!=newValue){
            firePropertyChange(propertyName,Integer.valueOf(oldValue),Integer.valueOf(newValue));
        }
    }

    public void firePropertyChange(String propertyName,Object oldValue,Object newValue){
        if(oldValue==null||newValue==null||!oldValue.equals(newValue)){
            firePropertyChange(new PropertyChangeEvent(this.source,propertyName,oldValue,newValue));
        }
    }

    public void firePropertyChange(PropertyChangeEvent event){
        Object oldValue=event.getOldValue();
        Object newValue=event.getNewValue();
        if(oldValue==null||newValue==null||!oldValue.equals(newValue)){
            String name=event.getPropertyName();
            PropertyChangeListener[] common=this.map.get(null);
            PropertyChangeListener[] named=(name!=null)
                    ?this.map.get(name)
                    :null;
            fire(common,event);
            fire(named,event);
        }
    }

    private static void fire(PropertyChangeListener[] listeners,PropertyChangeEvent event){
        if(listeners!=null){
            for(PropertyChangeListener listener : listeners){
                listener.propertyChange(event);
            }
        }
    }

    public void firePropertyChange(String propertyName,boolean oldValue,boolean newValue){
        if(oldValue!=newValue){
            firePropertyChange(propertyName,Boolean.valueOf(oldValue),Boolean.valueOf(newValue));
        }
    }

    public void fireIndexedPropertyChange(String propertyName,int index,int oldValue,int newValue){
        if(oldValue!=newValue){
            fireIndexedPropertyChange(propertyName,index,Integer.valueOf(oldValue),Integer.valueOf(newValue));
        }
    }

    public void fireIndexedPropertyChange(String propertyName,int index,Object oldValue,Object newValue){
        if(oldValue==null||newValue==null||!oldValue.equals(newValue)){
            firePropertyChange(new IndexedPropertyChangeEvent(source,propertyName,oldValue,newValue,index));
        }
    }

    public void fireIndexedPropertyChange(String propertyName,int index,boolean oldValue,boolean newValue){
        if(oldValue!=newValue){
            fireIndexedPropertyChange(propertyName,index,Boolean.valueOf(oldValue),Boolean.valueOf(newValue));
        }
    }

    public boolean hasListeners(String propertyName){
        return this.map.hasListeners(propertyName);
    }

    private void writeObject(ObjectOutputStream s) throws IOException{
        Hashtable<String,PropertyChangeSupport> children=null;
        PropertyChangeListener[] listeners=null;
        synchronized(this.map){
            for(Entry<String,PropertyChangeListener[]> entry : this.map.getEntries()){
                String property=entry.getKey();
                if(property==null){
                    listeners=entry.getValue();
                }else{
                    if(children==null){
                        children=new Hashtable<>();
                    }
                    PropertyChangeSupport pcs=new PropertyChangeSupport(this.source);
                    pcs.map.set(null,entry.getValue());
                    children.put(property,pcs);
                }
            }
        }
        ObjectOutputStream.PutField fields=s.putFields();
        fields.put("children",children);
        fields.put("source",this.source);
        fields.put("propertyChangeSupportSerializedDataVersion",2);
        s.writeFields();
        if(listeners!=null){
            for(PropertyChangeListener l : listeners){
                if(l instanceof Serializable){
                    s.writeObject(l);
                }
            }
        }
        s.writeObject(null);
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException{
        this.map=new PropertyChangeListenerMap();
        ObjectInputStream.GetField fields=s.readFields();
        @SuppressWarnings("unchecked")
        Hashtable<String,PropertyChangeSupport> children=(Hashtable<String,PropertyChangeSupport>)fields.get("children",null);
        this.source=fields.get("source",null);
        fields.get("propertyChangeSupportSerializedDataVersion",2);
        Object listenerOrNull;
        while(null!=(listenerOrNull=s.readObject())){
            this.map.add(null,(PropertyChangeListener)listenerOrNull);
        }
        if(children!=null){
            for(Entry<String,PropertyChangeSupport> entry : children.entrySet()){
                for(PropertyChangeListener listener : entry.getValue().getPropertyChangeListeners()){
                    this.map.add(entry.getKey(),listener);
                }
            }
        }
    }

    private static final class PropertyChangeListenerMap extends ChangeListenerMap<PropertyChangeListener>{
        private static final PropertyChangeListener[] EMPTY={};

        @Override
        protected PropertyChangeListener[] newArray(int length){
            return (0<length)
                    ?new PropertyChangeListener[length]
                    :EMPTY;
        }

        @Override
        protected PropertyChangeListener newProxy(String name,PropertyChangeListener listener){
            return new PropertyChangeListenerProxy(name,listener);
        }

        public final PropertyChangeListener extract(PropertyChangeListener listener){
            while(listener instanceof PropertyChangeListenerProxy){
                listener=((PropertyChangeListenerProxy)listener).getListener();
            }
            return listener;
        }
    }
}
