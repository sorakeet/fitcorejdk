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

import java.util.function.LongBinaryOperator;
import java.util.function.LongUnaryOperator;

public class AtomicLong extends Number implements java.io.Serializable{
    static final boolean VM_SUPPORTS_LONG_CAS=VMSupportsCS8();
    private static final long serialVersionUID=1927816293512124184L;
    // setup to use Unsafe.compareAndSwapLong for updates
    private static final Unsafe unsafe=Unsafe.getUnsafe();
    private static final long valueOffset;

    static{
        try{
            valueOffset=unsafe.objectFieldOffset
                    (AtomicLong.class.getDeclaredField("value"));
        }catch(Exception ex){
            throw new Error(ex);
        }
    }

    private volatile long value;

    public AtomicLong(long initialValue){
        value=initialValue;
    }

    public AtomicLong(){
    }

    private static native boolean VMSupportsCS8();

    public final void set(long newValue){
        value=newValue;
    }

    public final void lazySet(long newValue){
        unsafe.putOrderedLong(this,valueOffset,newValue);
    }

    public final long getAndSet(long newValue){
        return unsafe.getAndSetLong(this,valueOffset,newValue);
    }

    public final boolean weakCompareAndSet(long expect,long update){
        return unsafe.compareAndSwapLong(this,valueOffset,expect,update);
    }

    public final long getAndIncrement(){
        return unsafe.getAndAddLong(this,valueOffset,1L);
    }

    public final long getAndDecrement(){
        return unsafe.getAndAddLong(this,valueOffset,-1L);
    }

    public final long getAndAdd(long delta){
        return unsafe.getAndAddLong(this,valueOffset,delta);
    }

    public final long incrementAndGet(){
        return unsafe.getAndAddLong(this,valueOffset,1L)+1L;
    }

    public final long decrementAndGet(){
        return unsafe.getAndAddLong(this,valueOffset,-1L)-1L;
    }

    public final long addAndGet(long delta){
        return unsafe.getAndAddLong(this,valueOffset,delta)+delta;
    }

    public final long getAndUpdate(LongUnaryOperator updateFunction){
        long prev, next;
        do{
            prev=get();
            next=updateFunction.applyAsLong(prev);
        }while(!compareAndSet(prev,next));
        return prev;
    }

    public final long get(){
        return value;
    }

    public final boolean compareAndSet(long expect,long update){
        return unsafe.compareAndSwapLong(this,valueOffset,expect,update);
    }

    public final long updateAndGet(LongUnaryOperator updateFunction){
        long prev, next;
        do{
            prev=get();
            next=updateFunction.applyAsLong(prev);
        }while(!compareAndSet(prev,next));
        return next;
    }

    public final long getAndAccumulate(long x,
                                       LongBinaryOperator accumulatorFunction){
        long prev, next;
        do{
            prev=get();
            next=accumulatorFunction.applyAsLong(prev,x);
        }while(!compareAndSet(prev,next));
        return prev;
    }

    public final long accumulateAndGet(long x,
                                       LongBinaryOperator accumulatorFunction){
        long prev, next;
        do{
            prev=get();
            next=accumulatorFunction.applyAsLong(prev,x);
        }while(!compareAndSet(prev,next));
        return next;
    }

    public String toString(){
        return Long.toString(get());
    }

    public int intValue(){
        return (int)get();
    }

    public long longValue(){
        return get();
    }

    public float floatValue(){
        return (float)get();
    }

    public double doubleValue(){
        return (double)get();
    }
}
