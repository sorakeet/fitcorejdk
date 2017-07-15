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
package java.util;

public abstract class AbstractQueue<E>
        extends AbstractCollection<E>
        implements Queue<E>{
    protected AbstractQueue(){
    }

    public E remove(){
        E x=poll();
        if(x!=null)
            return x;
        else
            throw new NoSuchElementException();
    }

    public E element(){
        E x=peek();
        if(x!=null)
            return x;
        else
            throw new NoSuchElementException();
    }    public boolean add(E e){
        if(offer(e))
            return true;
        else
            throw new IllegalStateException("Queue full");
    }



    public void clear(){
        while(poll()!=null)
            ;
    }

    public boolean addAll(Collection<? extends E> c){
        if(c==null)
            throw new NullPointerException();
        if(c==this)
            throw new IllegalArgumentException();
        boolean modified=false;
        for(E e : c)
            if(add(e))
                modified=true;
        return modified;
    }
}
