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
package java.util.concurrent.atomic;

import sun.misc.Unsafe;

import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

public class AtomicReference<V> implements java.io.Serializable{
    private static final long serialVersionUID=-1848883965231344442L;
    private static final Unsafe unsafe=Unsafe.getUnsafe();
    private static final long valueOffset;

    static{
        try{
            valueOffset=unsafe.objectFieldOffset
                    (AtomicReference.class.getDeclaredField("value"));
        }catch(Exception ex){
            throw new Error(ex);
        }
    }

    private volatile V value;

    public AtomicReference(V initialValue){
        value=initialValue;
    }

    public AtomicReference(){
    }

    public final void set(V newValue){
        value=newValue;
    }

    public final void lazySet(V newValue){
        unsafe.putOrderedObject(this,valueOffset,newValue);
    }

    public final boolean weakCompareAndSet(V expect,V update){
        return unsafe.compareAndSwapObject(this,valueOffset,expect,update);
    }

    @SuppressWarnings("unchecked")
    public final V getAndSet(V newValue){
        return (V)unsafe.getAndSetObject(this,valueOffset,newValue);
    }

    public final V getAndUpdate(UnaryOperator<V> updateFunction){
        V prev, next;
        do{
            prev=get();
            next=updateFunction.apply(prev);
        }while(!compareAndSet(prev,next));
        return prev;
    }

    public final V get(){
        return value;
    }

    public final boolean compareAndSet(V expect,V update){
        return unsafe.compareAndSwapObject(this,valueOffset,expect,update);
    }

    public final V updateAndGet(UnaryOperator<V> updateFunction){
        V prev, next;
        do{
            prev=get();
            next=updateFunction.apply(prev);
        }while(!compareAndSet(prev,next));
        return next;
    }

    public final V getAndAccumulate(V x,
                                    BinaryOperator<V> accumulatorFunction){
        V prev, next;
        do{
            prev=get();
            next=accumulatorFunction.apply(prev,x);
        }while(!compareAndSet(prev,next));
        return prev;
    }

    public final V accumulateAndGet(V x,
                                    BinaryOperator<V> accumulatorFunction){
        V prev, next;
        do{
            prev=get();
            next=accumulatorFunction.apply(prev,x);
        }while(!compareAndSet(prev,next));
        return next;
    }

    public String toString(){
        return String.valueOf(get());
    }
}
