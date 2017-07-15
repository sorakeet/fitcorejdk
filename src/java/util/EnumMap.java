/**
 * Copyright (c) 2003, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util;

import sun.misc.SharedSecrets;

public class EnumMap<K extends Enum<K>,V> extends AbstractMap<K,V>
        implements java.io.Serializable, Cloneable{
    private static final Object NULL=new Object(){
        public int hashCode(){
            return 0;
        }

        public String toString(){
            return "java.util.EnumMap.NULL";
        }
    };
    private static final Enum<?>[] ZERO_LENGTH_ENUM_ARRAY=new Enum<?>[0];
    private static final long serialVersionUID=458661240069192865L;
    private final Class<K> keyType;
    private transient K[] keyUniverse;
    private transient Object[] vals;
    private transient int size=0;
    // Views
    private transient Set<Entry<K,V>> entrySet;

    public EnumMap(Class<K> keyType){
        this.keyType=keyType;
        keyUniverse=getKeyUniverse(keyType);
        vals=new Object[keyUniverse.length];
    }

    private static <K extends Enum<K>> K[] getKeyUniverse(Class<K> keyType){
        return SharedSecrets.getJavaLangAccess()
                .getEnumConstantsShared(keyType);
    }

    public EnumMap(EnumMap<K,? extends V> m){
        keyType=m.keyType;
        keyUniverse=m.keyUniverse;
        vals=m.vals.clone();
        size=m.size;
    }
    // Query Operations

    public EnumMap(Map<K,? extends V> m){
        if(m instanceof EnumMap){
            EnumMap<K,? extends V> em=(EnumMap<K,? extends V>)m;
            keyType=em.keyType;
            keyUniverse=em.keyUniverse;
            vals=em.vals.clone();
            size=em.size;
        }else{
            if(m.isEmpty())
                throw new IllegalArgumentException("Specified map is empty");
            keyType=m.keySet().iterator().next().getDeclaringClass();
            keyUniverse=getKeyUniverse(keyType);
            vals=new Object[keyUniverse.length];
            putAll(m);
        }
    }

    public int size(){
        return size;
    }

    public boolean containsValue(Object value){
        value=maskNull(value);
        for(Object val : vals)
            if(value.equals(val))
                return true;
        return false;
    }

    private Object maskNull(Object value){
        return (value==null?NULL:value);
    }

    public boolean containsKey(Object key){
        return isValidKey(key)&&vals[((Enum<?>)key).ordinal()]!=null;
    }
    // Modification Operations

    public V get(Object key){
        return (isValidKey(key)?
                unmaskNull(vals[((Enum<?>)key).ordinal()]):null);
    }

    @SuppressWarnings("unchecked")
    private V unmaskNull(Object value){
        return (V)(value==NULL?null:value);
    }

    public V put(K key,V value){
        typeCheck(key);
        int index=key.ordinal();
        Object oldValue=vals[index];
        vals[index]=maskNull(value);
        if(oldValue==null)
            size++;
        return unmaskNull(oldValue);
    }

    public V remove(Object key){
        if(!isValidKey(key))
            return null;
        int index=((Enum<?>)key).ordinal();
        Object oldValue=vals[index];
        vals[index]=null;
        if(oldValue!=null)
            size--;
        return unmaskNull(oldValue);
    }
    // Bulk Operations

    public void putAll(Map<? extends K,? extends V> m){
        if(m instanceof EnumMap){
            EnumMap<?,?> em=(EnumMap<?,?>)m;
            if(em.keyType!=keyType){
                if(em.isEmpty())
                    return;
                throw new ClassCastException(em.keyType+" != "+keyType);
            }
            for(int i=0;i<keyUniverse.length;i++){
                Object emValue=em.vals[i];
                if(emValue!=null){
                    if(vals[i]==null)
                        size++;
                    vals[i]=emValue;
                }
            }
        }else{
            super.putAll(m);
        }
    }

    public void clear(){
        Arrays.fill(vals,null);
        size=0;
    }

    public Set<K> keySet(){
        Set<K> ks=keySet;
        if(ks==null){
            ks=new KeySet();
            keySet=ks;
        }
        return ks;
    }

    public Collection<V> values(){
        Collection<V> vs=values;
        if(vs==null){
            vs=new Values();
            values=vs;
        }
        return vs;
    }

    public Set<Entry<K,V>> entrySet(){
        Set<Entry<K,V>> es=entrySet;
        if(es!=null)
            return es;
        else
            return entrySet=new EntrySet();
    }

    public boolean equals(Object o){
        if(this==o)
            return true;
        if(o instanceof EnumMap)
            return equals((EnumMap<?,?>)o);
        if(!(o instanceof Map))
            return false;
        Map<?,?> m=(Map<?,?>)o;
        if(size!=m.size())
            return false;
        for(int i=0;i<keyUniverse.length;i++){
            if(null!=vals[i]){
                K key=keyUniverse[i];
                V value=unmaskNull(vals[i]);
                if(null==value){
                    if(!((null==m.get(key))&&m.containsKey(key)))
                        return false;
                }else{
                    if(!value.equals(m.get(key)))
                        return false;
                }
            }
        }
        return true;
    }

    private boolean equals(EnumMap<?,?> em){
        if(em.keyType!=keyType)
            return size==0&&em.size==0;
        // Key types match, compare each value
        for(int i=0;i<keyUniverse.length;i++){
            Object ourValue=vals[i];
            Object hisValue=em.vals[i];
            if(hisValue!=ourValue&&
                    (hisValue==null||!hisValue.equals(ourValue)))
                return false;
        }
        return true;
    }

    public int hashCode(){
        int h=0;
        for(int i=0;i<keyUniverse.length;i++){
            if(null!=vals[i]){
                h+=entryHashCode(i);
            }
        }
        return h;
    }

    private int entryHashCode(int index){
        return (keyUniverse[index].hashCode()^vals[index].hashCode());
    }

    @SuppressWarnings("unchecked")
    public EnumMap<K,V> clone(){
        EnumMap<K,V> result=null;
        try{
            result=(EnumMap<K,V>)super.clone();
        }catch(CloneNotSupportedException e){
            throw new AssertionError();
        }
        result.vals=result.vals.clone();
        result.entrySet=null;
        return result;
    }

    private void typeCheck(K key){
        Class<?> keyClass=key.getClass();
        if(keyClass!=keyType&&keyClass.getSuperclass()!=keyType)
            throw new ClassCastException(keyClass+" != "+keyType);
    }

    private boolean isValidKey(Object key){
        if(key==null)
            return false;
        // Cheaper than instanceof Enum followed by getDeclaringClass
        Class<?> keyClass=key.getClass();
        return keyClass==keyType||keyClass.getSuperclass()==keyType;
    }

    private boolean containsMapping(Object key,Object value){
        return isValidKey(key)&&
                maskNull(value).equals(vals[((Enum<?>)key).ordinal()]);
    }
    // Comparison and hashing

    private boolean removeMapping(Object key,Object value){
        if(!isValidKey(key))
            return false;
        int index=((Enum<?>)key).ordinal();
        if(maskNull(value).equals(vals[index])){
            vals[index]=null;
            size--;
            return true;
        }
        return false;
    }

    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException{
        // Write out the key type and any hidden stuff
        s.defaultWriteObject();
        // Write out size (number of Mappings)
        s.writeInt(size);
        // Write out keys and values (alternating)
        int entriesToBeWritten=size;
        for(int i=0;entriesToBeWritten>0;i++){
            if(null!=vals[i]){
                s.writeObject(keyUniverse[i]);
                s.writeObject(unmaskNull(vals[i]));
                entriesToBeWritten--;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException{
        // Read in the key type and any hidden stuff
        s.defaultReadObject();
        keyUniverse=getKeyUniverse(keyType);
        vals=new Object[keyUniverse.length];
        // Read in size (number of Mappings)
        int size=s.readInt();
        // Read the keys and values, and put the mappings in the HashMap
        for(int i=0;i<size;i++){
            K key=(K)s.readObject();
            V value=(V)s.readObject();
            put(key,value);
        }
    }

    private class KeySet extends AbstractSet<K>{
        public Iterator<K> iterator(){
            return new KeyIterator();
        }

        public int size(){
            return size;
        }

        public boolean contains(Object o){
            return containsKey(o);
        }

        public boolean remove(Object o){
            int oldSize=size;
            EnumMap.this.remove(o);
            return size!=oldSize;
        }

        public void clear(){
            EnumMap.this.clear();
        }
    }

    private class Values extends AbstractCollection<V>{
        public Iterator<V> iterator(){
            return new ValueIterator();
        }

        public int size(){
            return size;
        }

        public boolean contains(Object o){
            return containsValue(o);
        }

        public boolean remove(Object o){
            o=maskNull(o);
            for(int i=0;i<vals.length;i++){
                if(o.equals(vals[i])){
                    vals[i]=null;
                    size--;
                    return true;
                }
            }
            return false;
        }

        public void clear(){
            EnumMap.this.clear();
        }
    }

    private class EntrySet extends AbstractSet<Entry<K,V>>{
        public Iterator<Entry<K,V>> iterator(){
            return new EntryIterator();
        }

        public boolean contains(Object o){
            if(!(o instanceof Map.Entry))
                return false;
            Entry<?,?> entry=(Entry<?,?>)o;
            return containsMapping(entry.getKey(),entry.getValue());
        }

        public boolean remove(Object o){
            if(!(o instanceof Map.Entry))
                return false;
            Entry<?,?> entry=(Entry<?,?>)o;
            return removeMapping(entry.getKey(),entry.getValue());
        }

        public int size(){
            return size;
        }

        public void clear(){
            EnumMap.this.clear();
        }

        public Object[] toArray(){
            return fillEntryArray(new Object[size]);
        }

        @SuppressWarnings("unchecked")
        public <T> T[] toArray(T[] a){
            int size=size();
            if(a.length<size)
                a=(T[])java.lang.reflect.Array
                        .newInstance(a.getClass().getComponentType(),size);
            if(a.length>size)
                a[size]=null;
            return (T[])fillEntryArray(a);
        }

        private Object[] fillEntryArray(Object[] a){
            int j=0;
            for(int i=0;i<vals.length;i++)
                if(vals[i]!=null)
                    a[j++]=new SimpleEntry<>(
                            keyUniverse[i],unmaskNull(vals[i]));
            return a;
        }
    }

    private abstract class EnumMapIterator<T> implements Iterator<T>{
        // Lower bound on index of next element to return
        int index=0;
        // Index of last returned element, or -1 if none
        int lastReturnedIndex=-1;

        public boolean hasNext(){
            while(index<vals.length&&vals[index]==null)
                index++;
            return index!=vals.length;
        }

        public void remove(){
            checkLastReturnedIndex();
            if(vals[lastReturnedIndex]!=null){
                vals[lastReturnedIndex]=null;
                size--;
            }
            lastReturnedIndex=-1;
        }

        private void checkLastReturnedIndex(){
            if(lastReturnedIndex<0)
                throw new IllegalStateException();
        }
    }

    private class KeyIterator extends EnumMapIterator<K>{
        public K next(){
            if(!hasNext())
                throw new NoSuchElementException();
            lastReturnedIndex=index++;
            return keyUniverse[lastReturnedIndex];
        }
    }

    private class ValueIterator extends EnumMapIterator<V>{
        public V next(){
            if(!hasNext())
                throw new NoSuchElementException();
            lastReturnedIndex=index++;
            return unmaskNull(vals[lastReturnedIndex]);
        }
    }

    private class EntryIterator extends EnumMapIterator<Entry<K,V>>{
        private Entry lastReturnedEntry;

        public void remove(){
            lastReturnedIndex=
                    ((null==lastReturnedEntry)?-1:lastReturnedEntry.index);
            super.remove();
            lastReturnedEntry.index=lastReturnedIndex;
            lastReturnedEntry=null;
        }

        private class Entry implements Map.Entry<K,V>{
            private int index;

            private Entry(int index){
                this.index=index;
            }

            public K getKey(){
                checkIndexForEntryUse();
                return keyUniverse[index];
            }

            public V getValue(){
                checkIndexForEntryUse();
                return unmaskNull(vals[index]);
            }

            public V setValue(V value){
                checkIndexForEntryUse();
                V oldValue=unmaskNull(vals[index]);
                vals[index]=maskNull(value);
                return oldValue;
            }

            private void checkIndexForEntryUse(){
                if(index<0)
                    throw new IllegalStateException("Entry was removed");
            }

            public boolean equals(Object o){
                if(index<0)
                    return o==this;
                if(!(o instanceof Map.Entry))
                    return false;
                Map.Entry<?,?> e=(Map.Entry<?,?>)o;
                V ourValue=unmaskNull(vals[index]);
                Object hisValue=e.getValue();
                return (e.getKey()==keyUniverse[index]&&
                        (ourValue==hisValue||
                                (ourValue!=null&&ourValue.equals(hisValue))));
            }

            public int hashCode(){
                if(index<0)
                    return super.hashCode();
                return entryHashCode(index);
            }

            public String toString(){
                if(index<0)
                    return super.toString();
                return keyUniverse[index]+"="
                        +unmaskNull(vals[index]);
            }
        }        public Map.Entry<K,V> next(){
            if(!hasNext())
                throw new NoSuchElementException();
            lastReturnedEntry=new Entry(index++);
            return lastReturnedEntry;
        }


    }
}
