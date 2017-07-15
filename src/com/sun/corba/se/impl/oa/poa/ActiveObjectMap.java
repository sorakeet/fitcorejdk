/**
 * Copyright (c) 1997, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.oa.poa;

import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.omg.PortableServer.Servant;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class ActiveObjectMap{
    protected POAImpl poa;
    private Map keyToEntry=new HashMap();     // Map< Key, AOMEntry >
    private Map entryToServant=new HashMap(); // Map< AOMEntry, Servant >
    private Map servantToEntry=new HashMap(); // Map< Servant, AOMEntry >

    protected ActiveObjectMap(POAImpl poa){
        this.poa=poa;
    }

    public static ActiveObjectMap create(POAImpl poa,boolean multipleIDsAllowed){
        if(multipleIDsAllowed)
            return new MultipleObjectMap(poa);
        else
            return new SingleObjectMap(poa);
    }

    public final boolean contains(Servant value){
        return servantToEntry.containsKey(value);
    }

    public final boolean containsKey(Key key){
        return keyToEntry.containsKey(key);
    }

    public final AOMEntry get(Key key){
        AOMEntry result=(AOMEntry)keyToEntry.get(key);
        if(result==null){
            result=new AOMEntry(poa);
            putEntry(key,result);
        }
        return result;
    }

    protected void putEntry(Key key,AOMEntry value){
        keyToEntry.put(key,value);
    }

    public final Servant getServant(AOMEntry entry){
        return (Servant)entryToServant.get(entry);
    }

    public Key getKey(Servant value) throws WrongPolicy{
        AOMEntry entry=(AOMEntry)servantToEntry.get(value);
        return getKey(entry);
    }

    public abstract Key getKey(AOMEntry value) throws WrongPolicy;

    public final void putServant(Servant servant,AOMEntry value){
        entryToServant.put(value,servant);
        servantToEntry.put(servant,value);
    }

    public final void remove(Key key){
        AOMEntry entry=(AOMEntry)keyToEntry.remove(key);
        Servant servant=(Servant)entryToServant.remove(entry);
        if(servant!=null)
            servantToEntry.remove(servant);
        removeEntry(entry,key);
    }

    protected abstract void removeEntry(AOMEntry entry,Key key);

    public abstract boolean hasMultipleIDs(AOMEntry value);

    protected void clear(){
        keyToEntry.clear();
    }

    public final Set keySet(){
        return keyToEntry.keySet();
    }

    public static class Key{
        public byte[] id;

        Key(byte[] id){
            this.id=id;
        }

        // Use the same hash function as for String
        public int hashCode(){
            int h=0;
            for(int i=0;i<id.length;i++)
                h=31*h+id[i];
            return h;
        }

        public boolean equals(Object key){
            if(!(key instanceof Key))
                return false;
            Key k=(Key)key;
            if(k.id.length!=this.id.length)
                return false;
            for(int i=0;i<this.id.length;i++)
                if(this.id[i]!=k.id[i])
                    return false;
            return true;
        }

        public String toString(){
            StringBuffer buffer=new StringBuffer();
            for(int i=0;i<id.length;i++){
                buffer.append(Integer.toString((int)id[i],16));
                if(i!=id.length-1)
                    buffer.append(":");
            }
            return buffer.toString();
        }
    }
}

class SingleObjectMap extends ActiveObjectMap{
    private Map entryToKey=new HashMap();    // Map< AOMEntry, Key >

    public SingleObjectMap(POAImpl poa){
        super(poa);
    }

    public Key getKey(AOMEntry value) throws WrongPolicy{
        return (Key)entryToKey.get(value);
    }

    protected void putEntry(Key key,AOMEntry value){
        super.putEntry(key,value);
        entryToKey.put(value,key);
    }

    public boolean hasMultipleIDs(AOMEntry value){
        return false;
    }

    // This case does not need the key.
    protected void removeEntry(AOMEntry entry,Key key){
        entryToKey.remove(entry);
    }

    public void clear(){
        super.clear();
        entryToKey.clear();
    }
}

class MultipleObjectMap extends ActiveObjectMap{
    private Map entryToKeys=new HashMap();   // Map< AOMEntry, Set< Key > >

    public MultipleObjectMap(POAImpl poa){
        super(poa);
    }

    public Key getKey(AOMEntry value) throws WrongPolicy{
        throw new WrongPolicy();
    }

    protected void putEntry(Key key,AOMEntry value){
        super.putEntry(key,value);
        Set set=(Set)entryToKeys.get(value);
        if(set==null){
            set=new HashSet();
            entryToKeys.put(value,set);
        }
        set.add(key);
    }

    public boolean hasMultipleIDs(AOMEntry value){
        Set set=(Set)entryToKeys.get(value);
        if(set==null)
            return false;
        return set.size()>1;
    }

    protected void removeEntry(AOMEntry entry,Key key){
        Set keys=(Set)entryToKeys.get(entry);
        if(keys!=null){
            keys.remove(key);
            if(keys.isEmpty())
                entryToKeys.remove(entry);
        }
    }

    public void clear(){
        super.clear();
        entryToKeys.clear();
    }
}
