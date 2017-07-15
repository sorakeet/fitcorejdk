/**
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
/**
 *
 *
 *
 *
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package java.util.concurrent;

import java.util.*;

public class ConcurrentSkipListSet<E>
        extends AbstractSet<E>
        implements NavigableSet<E>, Cloneable, java.io.Serializable{
    private static final long serialVersionUID=-2479143111061671589L;
    private static final sun.misc.Unsafe UNSAFE;
    private static final long mapOffset;

    static{
        try{
            UNSAFE=sun.misc.Unsafe.getUnsafe();
            Class<?> k=ConcurrentSkipListSet.class;
            mapOffset=UNSAFE.objectFieldOffset
                    (k.getDeclaredField("m"));
        }catch(Exception e){
            throw new Error(e);
        }
    }

    private final ConcurrentNavigableMap<E,Object> m;

    public ConcurrentSkipListSet(){
        m=new ConcurrentSkipListMap<E,Object>();
    }

    public ConcurrentSkipListSet(Comparator<? super E> comparator){
        m=new ConcurrentSkipListMap<E,Object>(comparator);
    }

    public ConcurrentSkipListSet(Collection<? extends E> c){
        m=new ConcurrentSkipListMap<E,Object>();
        addAll(c);
    }

    public ConcurrentSkipListSet(SortedSet<E> s){
        m=new ConcurrentSkipListMap<E,Object>(s.comparator());
        addAll(s);
    }

    ConcurrentSkipListSet(ConcurrentNavigableMap<E,Object> m){
        this.m=m;
    }

    public ConcurrentSkipListSet<E> clone(){
        try{
            @SuppressWarnings("unchecked")
            ConcurrentSkipListSet<E> clone=
                    (ConcurrentSkipListSet<E>)super.clone();
            clone.setMap(new ConcurrentSkipListMap<E,Object>(m));
            return clone;
        }catch(CloneNotSupportedException e){
            throw new InternalError();
        }
    }

    public Iterator<E> iterator(){
        return m.navigableKeySet().iterator();
    }

    public int size(){
        return m.size();
    }

    public boolean isEmpty(){
        return m.isEmpty();
    }

    public boolean contains(Object o){
        return m.containsKey(o);
    }

    public boolean add(E e){
        return m.putIfAbsent(e,Boolean.TRUE)==null;
    }

    public boolean remove(Object o){
        return m.remove(o,Boolean.TRUE);
    }

    public void clear(){
        m.clear();
    }

    public boolean equals(Object o){
        // Override AbstractSet version to avoid calling size()
        if(o==this)
            return true;
        if(!(o instanceof Set))
            return false;
        Collection<?> c=(Collection<?>)o;
        try{
            return containsAll(c)&&c.containsAll(this);
        }catch(ClassCastException unused){
            return false;
        }catch(NullPointerException unused){
            return false;
        }
    }

    public boolean removeAll(Collection<?> c){
        // Override AbstractSet version to avoid unnecessary call to size()
        boolean modified=false;
        for(Object e : c)
            if(remove(e))
                modified=true;
        return modified;
    }

    public E lower(E e){
        return m.lowerKey(e);
    }

    public E floor(E e){
        return m.floorKey(e);
    }

    public E ceiling(E e){
        return m.ceilingKey(e);
    }

    public E higher(E e){
        return m.higherKey(e);
    }

    public E pollFirst(){
        Map.Entry<E,Object> e=m.pollFirstEntry();
        return (e==null)?null:e.getKey();
    }

    public E pollLast(){
        Map.Entry<E,Object> e=m.pollLastEntry();
        return (e==null)?null:e.getKey();
    }

    public NavigableSet<E> descendingSet(){
        return new ConcurrentSkipListSet<E>(m.descendingMap());
    }

    public Iterator<E> descendingIterator(){
        return m.descendingKeySet().iterator();
    }

    public NavigableSet<E> subSet(E fromElement,
                                  boolean fromInclusive,
                                  E toElement,
                                  boolean toInclusive){
        return new ConcurrentSkipListSet<E>
                (m.subMap(fromElement,fromInclusive,
                        toElement,toInclusive));
    }

    public NavigableSet<E> headSet(E toElement,boolean inclusive){
        return new ConcurrentSkipListSet<E>(m.headMap(toElement,inclusive));
    }

    public NavigableSet<E> tailSet(E fromElement,boolean inclusive){
        return new ConcurrentSkipListSet<E>(m.tailMap(fromElement,inclusive));
    }

    public NavigableSet<E> subSet(E fromElement,E toElement){
        return subSet(fromElement,true,toElement,false);
    }

    public NavigableSet<E> headSet(E toElement){
        return headSet(toElement,false);
    }

    public NavigableSet<E> tailSet(E fromElement){
        return tailSet(fromElement,true);
    }

    public Comparator<? super E> comparator(){
        return m.comparator();
    }

    public E first(){
        return m.firstKey();
    }

    public E last(){
        return m.lastKey();
    }

    @SuppressWarnings("unchecked")
    public Spliterator<E> spliterator(){
        if(m instanceof ConcurrentSkipListMap)
            return ((ConcurrentSkipListMap<E,?>)m).keySpliterator();
        else
            return (Spliterator<E>)((ConcurrentSkipListMap.SubMap<E,?>)m).keyIterator();
    }

    // Support for resetting map in clone
    private void setMap(ConcurrentNavigableMap<E,Object> map){
        UNSAFE.putObjectVolatile(this,mapOffset,map);
    }
}
