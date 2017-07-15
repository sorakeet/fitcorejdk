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

public class AtomicBoolean implements java.io.Serializable{
    private static final long serialVersionUID=4654671469794556979L;
    // setup to use Unsafe.compareAndSwapInt for updates
    private static final Unsafe unsafe=Unsafe.getUnsafe();
    private static final long valueOffset;

    static{
        try{
            valueOffset=unsafe.objectFieldOffset
                    (AtomicBoolean.class.getDeclaredField("value"));
        }catch(Exception ex){
            throw new Error(ex);
        }
    }

    private volatile int value;

    public AtomicBoolean(boolean initialValue){
        value=initialValue?1:0;
    }

    public AtomicBoolean(){
    }

    public boolean weakCompareAndSet(boolean expect,boolean update){
        int e=expect?1:0;
        int u=update?1:0;
        return unsafe.compareAndSwapInt(this,valueOffset,e,u);
    }

    public final void set(boolean newValue){
        value=newValue?1:0;
    }

    public final void lazySet(boolean newValue){
        int v=newValue?1:0;
        unsafe.putOrderedInt(this,valueOffset,v);
    }

    public final boolean getAndSet(boolean newValue){
        boolean prev;
        do{
            prev=get();
        }while(!compareAndSet(prev,newValue));
        return prev;
    }

    public final boolean get(){
        return value!=0;
    }

    public final boolean compareAndSet(boolean expect,boolean update){
        int e=expect?1:0;
        int u=update?1:0;
        return unsafe.compareAndSwapInt(this,valueOffset,e,u);
    }

    public String toString(){
        return Boolean.toString(get());
    }
}
