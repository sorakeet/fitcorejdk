/**
 * Copyright (c) 2005, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.mbeanserver;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Map;

import static com.sun.jmx.mbeanserver.Util.newMap;

class WeakIdentityHashMap<K,V>{
    private Map<WeakReference<K>,V> map=newMap();
    private ReferenceQueue<K> refQueue=new ReferenceQueue<K>();

    private WeakIdentityHashMap(){
    }

    static <K,V> WeakIdentityHashMap<K,V> make(){
        return new WeakIdentityHashMap<K,V>();
    }

    V get(K key){
        expunge();
        WeakReference<K> keyref=makeReference(key);
        return map.get(keyref);
    }

    private void expunge(){
        Reference<? extends K> ref;
        while((ref=refQueue.poll())!=null)
            map.remove(ref);
    }

    private WeakReference<K> makeReference(K referent){
        return new IdentityWeakReference<K>(referent);
    }

    public V put(K key,V value){
        expunge();
        if(key==null)
            throw new IllegalArgumentException("Null key");
        WeakReference<K> keyref=makeReference(key,refQueue);
        return map.put(keyref,value);
    }

    private WeakReference<K> makeReference(K referent,ReferenceQueue<K> q){
        return new IdentityWeakReference<K>(referent,q);
    }

    public V remove(K key){
        expunge();
        WeakReference<K> keyref=makeReference(key);
        return map.remove(keyref);
    }

    private static class IdentityWeakReference<T> extends WeakReference<T>{
        private final int hashCode;

        IdentityWeakReference(T o){
            this(o,null);
        }

        IdentityWeakReference(T o,ReferenceQueue<T> q){
            super(o,q);
            this.hashCode=(o==null)?0:System.identityHashCode(o);
        }

        public int hashCode(){
            return hashCode;
        }

        public boolean equals(Object o){
            if(this==o)
                return true;
            if(!(o instanceof IdentityWeakReference<?>))
                return false;
            IdentityWeakReference<?> wr=(IdentityWeakReference<?>)o;
            Object got=get();
            return (got!=null&&got==wr.get());
        }
    }
}
