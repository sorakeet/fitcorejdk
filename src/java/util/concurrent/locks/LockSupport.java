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
package java.util.concurrent.locks;

public class LockSupport{
    // Hotspot implementation via intrinsics API
    private static final sun.misc.Unsafe UNSAFE;
    private static final long parkBlockerOffset;
    private static final long SEED;
    private static final long PROBE;
    private static final long SECONDARY;

    static{
        try{
            UNSAFE=sun.misc.Unsafe.getUnsafe();
            Class<?> tk=Thread.class;
            parkBlockerOffset=UNSAFE.objectFieldOffset
                    (tk.getDeclaredField("parkBlocker"));
            SEED=UNSAFE.objectFieldOffset
                    (tk.getDeclaredField("threadLocalRandomSeed"));
            PROBE=UNSAFE.objectFieldOffset
                    (tk.getDeclaredField("threadLocalRandomProbe"));
            SECONDARY=UNSAFE.objectFieldOffset
                    (tk.getDeclaredField("threadLocalRandomSecondarySeed"));
        }catch(Exception ex){
            throw new Error(ex);
        }
    }

    private LockSupport(){
    } // Cannot be instantiated.

    public static void unpark(Thread thread){
        if(thread!=null)
            UNSAFE.unpark(thread);
    }

    public static void park(Object blocker){
        Thread t=Thread.currentThread();
        setBlocker(t,blocker);
        UNSAFE.park(false,0L);
        setBlocker(t,null);
    }

    private static void setBlocker(Thread t,Object arg){
        // Even though volatile, hotspot doesn't need a write barrier here.
        UNSAFE.putObject(t,parkBlockerOffset,arg);
    }

    public static void parkNanos(Object blocker,long nanos){
        if(nanos>0){
            Thread t=Thread.currentThread();
            setBlocker(t,blocker);
            UNSAFE.park(false,nanos);
            setBlocker(t,null);
        }
    }

    public static void parkUntil(Object blocker,long deadline){
        Thread t=Thread.currentThread();
        setBlocker(t,blocker);
        UNSAFE.park(true,deadline);
        setBlocker(t,null);
    }

    public static Object getBlocker(Thread t){
        if(t==null)
            throw new NullPointerException();
        return UNSAFE.getObjectVolatile(t,parkBlockerOffset);
    }

    public static void park(){
        UNSAFE.park(false,0L);
    }

    public static void parkNanos(long nanos){
        if(nanos>0)
            UNSAFE.park(false,nanos);
    }

    public static void parkUntil(long deadline){
        UNSAFE.park(true,deadline);
    }

    static final int nextSecondarySeed(){
        int r;
        Thread t=Thread.currentThread();
        if((r=UNSAFE.getInt(t,SECONDARY))!=0){
            r^=r<<13;   // xorshift
            r^=r>>>17;
            r^=r<<5;
        }else if((r=java.util.concurrent.ThreadLocalRandom.current().nextInt())==0)
            r=1; // avoid zero
        UNSAFE.putInt(t,SECONDARY,r);
        return r;
    }
}
