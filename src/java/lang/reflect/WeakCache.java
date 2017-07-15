/**
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang.reflect;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Supplier;

final class WeakCache<K,P,V>{
    private final ReferenceQueue<K> refQueue
            =new ReferenceQueue<>();
    // the key type is Object for supporting null key
    private final ConcurrentMap<Object,ConcurrentMap<Object,Supplier<V>>> map
            =new ConcurrentHashMap<>();
    private final ConcurrentMap<Supplier<V>,Boolean> reverseMap
            =new ConcurrentHashMap<>();
    private final BiFunction<K,P,?> subKeyFactory;
    private final BiFunction<K,P,V> valueFactory;

    public WeakCache(BiFunction<K,P,?> subKeyFactory,
                     BiFunction<K,P,V> valueFactory){
        this.subKeyFactory=Objects.requireNonNull(subKeyFactory);
        this.valueFactory=Objects.requireNonNull(valueFactory);
    }

    public V get(K key,P parameter){
        Objects.requireNonNull(parameter);
        expungeStaleEntries();
        Object cacheKey=CacheKey.valueOf(key,refQueue);
        // lazily install the 2nd level valuesMap for the particular cacheKey
        ConcurrentMap<Object,Supplier<V>> valuesMap=map.get(cacheKey);
        if(valuesMap==null){
            ConcurrentMap<Object,Supplier<V>> oldValuesMap
                    =map.putIfAbsent(cacheKey,
                    valuesMap=new ConcurrentHashMap<>());
            if(oldValuesMap!=null){
                valuesMap=oldValuesMap;
            }
        }
        // create subKey and retrieve the possible Supplier<V> stored by that
        // subKey from valuesMap
        Object subKey=Objects.requireNonNull(subKeyFactory.apply(key,parameter));
        Supplier<V> supplier=valuesMap.get(subKey);
        Factory factory=null;
        while(true){
            if(supplier!=null){
                // supplier might be a Factory or a CacheValue<V> instance
                V value=supplier.get();
                if(value!=null){
                    return value;
                }
            }
            // else no supplier in cache
            // or a supplier that returned null (could be a cleared CacheValue
            // or a Factory that wasn't successful in installing the CacheValue)
            // lazily construct a Factory
            if(factory==null){
                factory=new Factory(key,parameter,subKey,valuesMap);
            }
            if(supplier==null){
                supplier=valuesMap.putIfAbsent(subKey,factory);
                if(supplier==null){
                    // successfully installed Factory
                    supplier=factory;
                }
                // else retry with winning supplier
            }else{
                if(valuesMap.replace(subKey,supplier,factory)){
                    // successfully replaced
                    // cleared CacheEntry / unsuccessful Factory
                    // with our Factory
                    supplier=factory;
                }else{
                    // retry with current supplier
                    supplier=valuesMap.get(subKey);
                }
            }
        }
    }

    private void expungeStaleEntries(){
        CacheKey<K> cacheKey;
        while((cacheKey=(CacheKey<K>)refQueue.poll())!=null){
            cacheKey.expungeFrom(map,reverseMap);
        }
    }

    public boolean containsValue(V value){
        Objects.requireNonNull(value);
        expungeStaleEntries();
        return reverseMap.containsKey(new LookupValue<>(value));
    }

    public int size(){
        expungeStaleEntries();
        return reverseMap.size();
    }

    private interface Value<V> extends Supplier<V>{
    }

    private static final class LookupValue<V> implements Value<V>{
        private final V value;

        LookupValue(V value){
            this.value=value;
        }

        @Override
        public int hashCode(){
            return System.identityHashCode(value); // compare by identity
        }

        @Override
        public V get(){
            return value;
        }

        @Override
        public boolean equals(Object obj){
            return obj==this||
                    obj instanceof Value&&
                            this.value==((Value<?>)obj).get();  // compare by identity
        }
    }

    private static final class CacheValue<V>
            extends WeakReference<V> implements Value<V>{
        private final int hash;

        CacheValue(V value){
            super(value);
            this.hash=System.identityHashCode(value); // compare by identity
        }

        @Override
        public int hashCode(){
            return hash;
        }

        @Override
        public boolean equals(Object obj){
            V value;
            return obj==this||
                    obj instanceof Value&&
                            // cleared CacheValue is only equal to itself
                            (value=get())!=null&&
                            value==((Value<?>)obj).get(); // compare by identity
        }
    }

    private static final class CacheKey<K> extends WeakReference<K>{
        // a replacement for null keys
        private static final Object NULL_KEY=new Object();
        private final int hash;

        private CacheKey(K key,ReferenceQueue<K> refQueue){
            super(key,refQueue);
            this.hash=System.identityHashCode(key);  // compare by identity
        }

        static <K> Object valueOf(K key,ReferenceQueue<K> refQueue){
            return key==null
                    // null key means we can't weakly reference it,
                    // so we use a NULL_KEY singleton as cache key
                    ?NULL_KEY
                    // non-null key requires wrapping with a WeakReference
                    :new CacheKey<>(key,refQueue);
        }

        void expungeFrom(ConcurrentMap<?,? extends ConcurrentMap<?,?>> map,
                         ConcurrentMap<?,Boolean> reverseMap){
            // removing just by key is always safe here because after a CacheKey
            // is cleared and enqueue-ed it is only equal to itself
            // (see equals method)...
            ConcurrentMap<?,?> valuesMap=map.remove(this);
            // remove also from reverseMap if needed
            if(valuesMap!=null){
                for(Object cacheValue : valuesMap.values()){
                    reverseMap.remove(cacheValue);
                }
            }
        }

        @Override
        public int hashCode(){
            return hash;
        }

        @Override
        public boolean equals(Object obj){
            K key;
            return obj==this||
                    obj!=null&&
                            obj.getClass()==this.getClass()&&
                            // cleared CacheKey is only equal to itself
                            (key=this.get())!=null&&
                            // compare key by identity
                            key==((CacheKey<K>)obj).get();
        }
    }

    private final class Factory implements Supplier<V>{
        private final K key;
        private final P parameter;
        private final Object subKey;
        private final ConcurrentMap<Object,Supplier<V>> valuesMap;

        Factory(K key,P parameter,Object subKey,
                ConcurrentMap<Object,Supplier<V>> valuesMap){
            this.key=key;
            this.parameter=parameter;
            this.subKey=subKey;
            this.valuesMap=valuesMap;
        }

        @Override
        public synchronized V get(){ // serialize access
            // re-check
            Supplier<V> supplier=valuesMap.get(subKey);
            if(supplier!=this){
                // something changed while we were waiting:
                // might be that we were replaced by a CacheValue
                // or were removed because of failure ->
                // return null to signal WeakCache.get() to retry
                // the loop
                return null;
            }
            // else still us (supplier == this)
            // create new value
            V value=null;
            try{
                value=Objects.requireNonNull(valueFactory.apply(key,parameter));
            }finally{
                if(value==null){ // remove us on failure
                    valuesMap.remove(subKey,this);
                }
            }
            // the only path to reach here is with non-null value
            assert value!=null;
            // wrap value with CacheValue (WeakReference)
            CacheValue<V> cacheValue=new CacheValue<>(value);
            // try replacing us with CacheValue (this should always succeed)
            if(valuesMap.replace(subKey,this,cacheValue)){
                // put also in reverseMap
                reverseMap.put(cacheValue,Boolean.TRUE);
            }else{
                throw new AssertionError("Should not reach here");
            }
            // successfully replaced us with new CacheValue -> return the value
            // wrapped by it
            return value;
        }
    }
}
