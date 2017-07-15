/**
 * Copyright (c) 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.script;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SimpleBindings implements Bindings{
    private Map<String,Object> map;

    public SimpleBindings(){
        this(new HashMap<String,Object>());
    }

    public SimpleBindings(Map<String,Object> m){
        if(m==null){
            throw new NullPointerException();
        }
        this.map=m;
    }

    public int size(){
        return map.size();
    }    public Object put(String name,Object value){
        checkKey(name);
        return map.put(name,value);
    }

    public boolean isEmpty(){
        return map.isEmpty();
    }    public void putAll(Map<? extends String,? extends Object> toMerge){
        if(toMerge==null){
            throw new NullPointerException("toMerge map is null");
        }
        for(Entry<? extends String,? extends Object> entry : toMerge.entrySet()){
            String key=entry.getKey();
            checkKey(key);
            put(key,entry.getValue());
        }
    }

    public boolean containsValue(Object value){
        return map.containsValue(value);
    }

    public void clear(){
        map.clear();
    }    public boolean containsKey(Object key){
        checkKey(key);
        return map.containsKey(key);
    }

    public Set<String> keySet(){
        return map.keySet();
    }

    public Collection<Object> values(){
        return map.values();
    }

    public Set<Entry<String,Object>> entrySet(){
        return map.entrySet();
    }    public Object get(Object key){
        checkKey(key);
        return map.get(key);
    }





    public Object remove(Object key){
        checkKey(key);
        return map.remove(key);
    }





    private void checkKey(Object key){
        if(key==null){
            throw new NullPointerException("key can not be null");
        }
        if(!(key instanceof String)){
            throw new ClassCastException("key should be a String");
        }
        if(key.equals("")){
            throw new IllegalArgumentException("key can not be empty");
        }
    }
}
