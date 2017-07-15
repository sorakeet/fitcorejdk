/**
 * Copyright (c) 1999, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

@SuppressWarnings("serial")
public class ActionMap implements Serializable{
    private transient ArrayTable arrayTable;
    private ActionMap parent;

    public ActionMap(){
    }

    public Action get(Object key){
        Action value=(arrayTable==null)?null:
                (Action)arrayTable.get(key);
        if(value==null){
            ActionMap parent=getParent();
            if(parent!=null){
                return parent.get(key);
            }
        }
        return value;
    }

    public ActionMap getParent(){
        return parent;
    }

    public void setParent(ActionMap map){
        this.parent=map;
    }

    public void clear(){
        if(arrayTable!=null){
            arrayTable.clear();
        }
    }

    public Object[] allKeys(){
        int count=size();
        ActionMap parent=getParent();
        if(count==0){
            if(parent!=null){
                return parent.allKeys();
            }
            return keys();
        }
        if(parent==null){
            return keys();
        }
        Object[] keys=keys();
        Object[] pKeys=parent.allKeys();
        if(pKeys==null){
            return keys;
        }
        if(keys==null){
            // Should only happen if size() != keys.length, which should only
            // happen if mutated from multiple threads (or a bogus subclass).
            return pKeys;
        }
        HashMap<Object,Object> keyMap=new HashMap<Object,Object>();
        int counter;
        for(counter=keys.length-1;counter>=0;counter--){
            keyMap.put(keys[counter],keys[counter]);
        }
        for(counter=pKeys.length-1;counter>=0;counter--){
            keyMap.put(pKeys[counter],pKeys[counter]);
        }
        return keyMap.keySet().toArray();
    }

    public Object[] keys(){
        if(arrayTable==null){
            return null;
        }
        return arrayTable.getKeys(null);
    }

    public int size(){
        if(arrayTable==null){
            return 0;
        }
        return arrayTable.size();
    }

    private void writeObject(ObjectOutputStream s) throws IOException{
        s.defaultWriteObject();
        ArrayTable.writeArrayTable(s,arrayTable);
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException,
            IOException{
        s.defaultReadObject();
        for(int counter=s.readInt()-1;counter>=0;counter--){
            put(s.readObject(),(Action)s.readObject());
        }
    }

    public void put(Object key,Action action){
        if(key==null){
            return;
        }
        if(action==null){
            remove(key);
        }else{
            if(arrayTable==null){
                arrayTable=new ArrayTable();
            }
            arrayTable.put(key,action);
        }
    }

    public void remove(Object key){
        if(arrayTable!=null){
            arrayTable.remove(key);
        }
    }
}
