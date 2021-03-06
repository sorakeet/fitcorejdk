/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util;

public class TreeSet<E> extends AbstractSet<E>
        implements NavigableSet<E>, Cloneable, java.io.Serializable{
    // Dummy value to associate with an Object in the backing Map
    private static final Object PRESENT=new Object();
    private static final long serialVersionUID=-2479143000061671589L;
    private transient NavigableMap<E,Object> m;

    public TreeSet(Collection<? extends E> c){
        this();
        addAll(c);
    }

    public TreeSet(){
        this(new TreeMap<E,Object>());
    }

    TreeSet(NavigableMap<E,Object> m){
        this.m=m;
    }

    public TreeSet(SortedSet<E> s){
        this(s.comparator());
        addAll(s);
    }

    public TreeSet(Comparator<? super E> comparator){
        this(new TreeMap<>(comparator));
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
        return m.put(e,PRESENT)==null;
    }

    public boolean remove(Object o){
        return m.remove(o)==PRESENT;
    }

    public boolean addAll(Collection<? extends E> c){
        // Use linear-time version if applicable
        if(m.size()==0&&c.size()>0&&
                c instanceof SortedSet&&
                m instanceof TreeMap){
            SortedSet<? extends E> set=(SortedSet<? extends E>)c;
            TreeMap<E,Object> map=(TreeMap<E,Object>)m;
            Comparator<?> cc=set.comparator();
            Comparator<? super E> mc=map.comparator();
            if(cc==mc||(cc!=null&&cc.equals(mc))){
                map.addAllForTreeSet(set,PRESENT);
                return true;
            }
        }
        return super.addAll(c);
    }

    public void clear(){
        m.clear();
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
        Map.Entry<E,?> e=m.pollFirstEntry();
        return (e==null)?null:e.getKey();
    }

    public E pollLast(){
        Map.Entry<E,?> e=m.pollLastEntry();
        return (e==null)?null:e.getKey();
    }

    public NavigableSet<E> descendingSet(){
        return new TreeSet<>(m.descendingMap());
    }
    // NavigableSet API methods

    public Iterator<E> descendingIterator(){
        return m.descendingKeySet().iterator();
    }

    public NavigableSet<E> subSet(E fromElement,boolean fromInclusive,
                                  E toElement,boolean toInclusive){
        return new TreeSet<>(m.subMap(fromElement,fromInclusive,
                toElement,toInclusive));
    }

    public NavigableSet<E> headSet(E toElement,boolean inclusive){
        return new TreeSet<>(m.headMap(toElement,inclusive));
    }

    public NavigableSet<E> tailSet(E fromElement,boolean inclusive){
        return new TreeSet<>(m.tailMap(fromElement,inclusive));
    }

    public SortedSet<E> subSet(E fromElement,E toElement){
        return subSet(fromElement,true,toElement,false);
    }

    public SortedSet<E> headSet(E toElement){
        return headSet(toElement,false);
    }

    public SortedSet<E> tailSet(E fromElement){
        return tailSet(fromElement,true);
    }

    @SuppressWarnings("unchecked")
    public Object clone(){
        TreeSet<E> clone;
        try{
            clone=(TreeSet<E>)super.clone();
        }catch(CloneNotSupportedException e){
            throw new InternalError(e);
        }
        clone.m=new TreeMap<>(m);
        return clone;
    }

    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException{
        // Write out any hidden stuff
        s.defaultWriteObject();
        // Write out Comparator
        s.writeObject(m.comparator());
        // Write out size
        s.writeInt(m.size());
        // Write out all elements in the proper order.
        for(E e : m.keySet())
            s.writeObject(e);
    }

    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException{
        // Read in any hidden stuff
        s.defaultReadObject();
        // Read in Comparator
        @SuppressWarnings("unchecked")
        Comparator<? super E> c=(Comparator<? super E>)s.readObject();
        // Create backing TreeMap
        TreeMap<E,Object> tm=new TreeMap<>(c);
        m=tm;
        // Read in size
        int size=s.readInt();
        tm.readTreeSet(size,s,PRESENT);
    }

    public Spliterator<E> spliterator(){
        return TreeMap.keySpliteratorFor(m);
    }
}
