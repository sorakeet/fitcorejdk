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

public class AtomicStampedReference<V>{
    // Unsafe mechanics
    private static final sun.misc.Unsafe UNSAFE=sun.misc.Unsafe.getUnsafe();
    private static final long pairOffset=
            objectFieldOffset(UNSAFE,"pair",AtomicStampedReference.class);
    private volatile Pair<V> pair;

    public AtomicStampedReference(V initialRef,int initialStamp){
        pair=Pair.of(initialRef,initialStamp);
    }

    static long objectFieldOffset(sun.misc.Unsafe UNSAFE,
                                  String field,Class<?> klazz){
        try{
            return UNSAFE.objectFieldOffset(klazz.getDeclaredField(field));
        }catch(NoSuchFieldException e){
            // Convert Exception to corresponding Error
            NoSuchFieldError error=new NoSuchFieldError(field);
            error.initCause(e);
            throw error;
        }
    }

    public V getReference(){
        return pair.reference;
    }

    public int getStamp(){
        return pair.stamp;
    }

    public V get(int[] stampHolder){
        Pair<V> pair=this.pair;
        stampHolder[0]=pair.stamp;
        return pair.reference;
    }

    public boolean weakCompareAndSet(V expectedReference,
                                     V newReference,
                                     int expectedStamp,
                                     int newStamp){
        return compareAndSet(expectedReference,newReference,
                expectedStamp,newStamp);
    }

    public boolean compareAndSet(V expectedReference,
                                 V newReference,
                                 int expectedStamp,
                                 int newStamp){
        Pair<V> current=pair;
        return
                expectedReference==current.reference&&
                        expectedStamp==current.stamp&&
                        ((newReference==current.reference&&
                                newStamp==current.stamp)||
                                casPair(current,Pair.of(newReference,newStamp)));
    }

    private boolean casPair(Pair<V> cmp,Pair<V> val){
        return UNSAFE.compareAndSwapObject(this,pairOffset,cmp,val);
    }

    public void set(V newReference,int newStamp){
        Pair<V> current=pair;
        if(newReference!=current.reference||newStamp!=current.stamp)
            this.pair=Pair.of(newReference,newStamp);
    }

    public boolean attemptStamp(V expectedReference,int newStamp){
        Pair<V> current=pair;
        return
                expectedReference==current.reference&&
                        (newStamp==current.stamp||
                                casPair(current,Pair.of(expectedReference,newStamp)));
    }

    private static class Pair<T>{
        final T reference;
        final int stamp;

        private Pair(T reference,int stamp){
            this.reference=reference;
            this.stamp=stamp;
        }

        static <T> Pair<T> of(T reference,int stamp){
            return new Pair<T>(reference,stamp);
        }
    }
}
