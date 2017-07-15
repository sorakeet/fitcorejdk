/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class LinkedHashMap<K,V>
        extends HashMap<K,V>
        implements Map<K,V>{
    private static final long serialVersionUID=3801124242820219131L;
    final boolean accessOrder;
    transient Entry<K,V> head;
    transient Entry<K,V> tail;

    public LinkedHashMap(int initialCapacity,float loadFactor){
        super(initialCapacity,loadFactor);
        accessOrder=false;
    }
    // internal utilities

    public LinkedHashMap(int initialCapacity){
        super(initialCapacity);
        accessOrder=false;
    }

    public LinkedHashMap(){
        super();
        accessOrder=false;
    }
    // overrides of HashMap hook methods

    public LinkedHashMap(Map<? extends K,? extends V> m){
        super();
        accessOrder=false;
        putMapEntries(m,false);
    }

    public LinkedHashMap(int initialCapacity,
                         float loadFactor,
                         boolean accessOrder){
        super(initialCapacity,loadFactor);
        this.accessOrder=accessOrder;
    }

    public V get(Object key){
        Node<K,V> e;
        if((e=getNode(hash(key),key))==null)
            return null;
        if(accessOrder)
            afterNodeAccess(e);
        return e.value;
    }

    public void clear(){
        super.clear();
        head=tail=null;
    }

    public boolean containsValue(Object value){
        for(Entry<K,V> e=head;e!=null;e=e.after){
            V v=e.value;
            if(v==value||(value!=null&&value.equals(v)))
                return true;
        }
        return false;
    }

    public Set<K> keySet(){
        Set<K> ks=keySet;
        if(ks==null){
            ks=new LinkedKeySet();
            keySet=ks;
        }
        return ks;
    }

    public Collection<V> values(){
        Collection<V> vs=values;
        if(vs==null){
            vs=new LinkedValues();
            values=vs;
        }
        return vs;
    }

    public Set<Map.Entry<K,V>> entrySet(){
        Set<Map.Entry<K,V>> es;
        return (es=entrySet)==null?(entrySet=new LinkedEntrySet()):es;
    }

    public V getOrDefault(Object key,V defaultValue){
        Node<K,V> e;
        if((e=getNode(hash(key),key))==null)
            return defaultValue;
        if(accessOrder)
            afterNodeAccess(e);
        return e.value;
    }

    public void forEach(BiConsumer<? super K,? super V> action){
        if(action==null)
            throw new NullPointerException();
        int mc=modCount;
        for(Entry<K,V> e=head;e!=null;e=e.after)
            action.accept(e.key,e.value);
        if(modCount!=mc)
            throw new ConcurrentModificationException();
    }

    public void replaceAll(BiFunction<? super K,? super V,? extends V> function){
        if(function==null)
            throw new NullPointerException();
        int mc=modCount;
        for(Entry<K,V> e=head;e!=null;e=e.after)
            e.value=function.apply(e.key,e.value);
        if(modCount!=mc)
            throw new ConcurrentModificationException();
    }

    Node<K,V> newNode(int hash,K key,V value,Node<K,V> e){
        Entry<K,V> p=
                new Entry<K,V>(hash,key,value,e);
        linkNodeLast(p);
        return p;
    }

    // link at the end of list
    private void linkNodeLast(Entry<K,V> p){
        Entry<K,V> last=tail;
        tail=p;
        if(last==null)
            head=p;
        else{
            p.before=last;
            last.after=p;
        }
    }

    Node<K,V> replacementNode(Node<K,V> p,Node<K,V> next){
        Entry<K,V> q=(Entry<K,V>)p;
        Entry<K,V> t=
                new Entry<K,V>(q.hash,q.key,q.value,next);
        transferLinks(q,t);
        return t;
    }

    // apply src's links to dst
    private void transferLinks(Entry<K,V> src,
                               Entry<K,V> dst){
        Entry<K,V> b=dst.before=src.before;
        Entry<K,V> a=dst.after=src.after;
        if(b==null)
            head=dst;
        else
            b.after=dst;
        if(a==null)
            tail=dst;
        else
            a.before=dst;
    }

    TreeNode<K,V> newTreeNode(int hash,K key,V value,Node<K,V> next){
        TreeNode<K,V> p=new TreeNode<K,V>(hash,key,value,next);
        linkNodeLast(p);
        return p;
    }

    TreeNode<K,V> replacementTreeNode(Node<K,V> p,Node<K,V> next){
        Entry<K,V> q=(Entry<K,V>)p;
        TreeNode<K,V> t=new TreeNode<K,V>(q.hash,q.key,q.value,next);
        transferLinks(q,t);
        return t;
    }

    void reinitialize(){
        super.reinitialize();
        head=tail=null;
    }

    void afterNodeAccess(Node<K,V> e){ // move node to last
        Entry<K,V> last;
        if(accessOrder&&(last=tail)!=e){
            Entry<K,V> p=
                    (Entry<K,V>)e, b=p.before, a=p.after;
            p.after=null;
            if(b==null)
                head=a;
            else
                b.after=a;
            if(a!=null)
                a.before=b;
            else
                last=b;
            if(last==null)
                head=p;
            else{
                p.before=last;
                last.after=p;
            }
            tail=p;
            ++modCount;
        }
    }

    void afterNodeInsertion(boolean evict){ // possibly remove eldest
        Entry<K,V> first;
        if(evict&&(first=head)!=null&&removeEldestEntry(first)){
            K key=first.key;
            removeNode(hash(key),key,null,false,true);
        }
    }

    void afterNodeRemoval(Node<K,V> e){ // unlink
        Entry<K,V> p=
                (Entry<K,V>)e, b=p.before, a=p.after;
        p.before=p.after=null;
        if(b==null)
            head=a;
        else
            b.after=a;
        if(a==null)
            tail=b;
        else
            a.before=b;
    }

    void internalWriteEntries(java.io.ObjectOutputStream s) throws IOException{
        for(Entry<K,V> e=head;e!=null;e=e.after){
            s.writeObject(e.key);
            s.writeObject(e.value);
        }
    }

    protected boolean removeEldestEntry(Map.Entry<K,V> eldest){
        return false;
    }

    static class Entry<K,V> extends Node<K,V>{
        Entry<K,V> before, after;

        Entry(int hash,K key,V value,Node<K,V> next){
            super(hash,key,value,next);
        }
    }

    final class LinkedKeySet extends AbstractSet<K>{
        public final Iterator<K> iterator(){
            return new LinkedKeyIterator();
        }

        public final Spliterator<K> spliterator(){
            return Spliterators.spliterator(this,Spliterator.SIZED|
                    Spliterator.ORDERED|
                    Spliterator.DISTINCT);
        }        public final int size(){
            return size;
        }

        public final void forEach(Consumer<? super K> action){
            if(action==null)
                throw new NullPointerException();
            int mc=modCount;
            for(Entry<K,V> e=head;e!=null;e=e.after)
                action.accept(e.key);
            if(modCount!=mc)
                throw new ConcurrentModificationException();
        }

        public final void clear(){
            LinkedHashMap.this.clear();
        }



        public final boolean contains(Object o){
            return containsKey(o);
        }

        public final boolean remove(Object key){
            return removeNode(hash(key),key,null,false,true)!=null;
        }
    }
    // Map overrides

    final class LinkedValues extends AbstractCollection<V>{
        public final Spliterator<V> spliterator(){
            return Spliterators.spliterator(this,Spliterator.SIZED|
                    Spliterator.ORDERED);
        }

        public final int size(){
            return size;
        }

        public final void clear(){
            LinkedHashMap.this.clear();
        }

        public final Iterator<V> iterator(){
            return new LinkedValueIterator();
        }

        public final boolean contains(Object o){
            return containsValue(o);
        }

        public final void forEach(Consumer<? super V> action){
            if(action==null)
                throw new NullPointerException();
            int mc=modCount;
            for(Entry<K,V> e=head;e!=null;e=e.after)
                action.accept(e.value);
            if(modCount!=mc)
                throw new ConcurrentModificationException();
        }
    }

    final class LinkedEntrySet extends AbstractSet<Map.Entry<K,V>>{
        public final int size(){
            return size;
        }

        public final void clear(){
            LinkedHashMap.this.clear();
        }

        public final Iterator<Map.Entry<K,V>> iterator(){
            return new LinkedEntryIterator();
        }

        public final boolean contains(Object o){
            if(!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> e=(Map.Entry<?,?>)o;
            Object key=e.getKey();
            Node<K,V> candidate=getNode(hash(key),key);
            return candidate!=null&&candidate.equals(e);
        }

        public final boolean remove(Object o){
            if(o instanceof Map.Entry){
                Map.Entry<?,?> e=(Map.Entry<?,?>)o;
                Object key=e.getKey();
                Object value=e.getValue();
                return removeNode(hash(key),key,value,true,true)!=null;
            }
            return false;
        }

        public final Spliterator<Map.Entry<K,V>> spliterator(){
            return Spliterators.spliterator(this,Spliterator.SIZED|
                    Spliterator.ORDERED|
                    Spliterator.DISTINCT);
        }

        public final void forEach(Consumer<? super Map.Entry<K,V>> action){
            if(action==null)
                throw new NullPointerException();
            int mc=modCount;
            for(Entry<K,V> e=head;e!=null;e=e.after)
                action.accept(e);
            if(modCount!=mc)
                throw new ConcurrentModificationException();
        }
    }
    // Iterators

    abstract class LinkedHashIterator{
        Entry<K,V> next;
        Entry<K,V> current;
        int expectedModCount;

        LinkedHashIterator(){
            next=head;
            expectedModCount=modCount;
            current=null;
        }

        public final boolean hasNext(){
            return next!=null;
        }

        final Entry<K,V> nextNode(){
            Entry<K,V> e=next;
            if(modCount!=expectedModCount)
                throw new ConcurrentModificationException();
            if(e==null)
                throw new NoSuchElementException();
            current=e;
            next=e.after;
            return e;
        }

        public final void remove(){
            Node<K,V> p=current;
            if(p==null)
                throw new IllegalStateException();
            if(modCount!=expectedModCount)
                throw new ConcurrentModificationException();
            current=null;
            K key=p.key;
            removeNode(hash(key),key,null,false,false);
            expectedModCount=modCount;
        }
    }

    final class LinkedKeyIterator extends LinkedHashIterator
            implements Iterator<K>{
        public final K next(){
            return nextNode().getKey();
        }
    }

    final class LinkedValueIterator extends LinkedHashIterator
            implements Iterator<V>{
        public final V next(){
            return nextNode().value;
        }
    }

    final class LinkedEntryIterator extends LinkedHashIterator
            implements Iterator<Map.Entry<K,V>>{
        public final Map.Entry<K,V> next(){
            return nextNode();
        }
    }
}
