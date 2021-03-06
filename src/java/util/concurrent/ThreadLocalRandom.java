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

import java.io.ObjectStreamField;
import java.util.Random;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.StreamSupport;

public class ThreadLocalRandom extends Random{
    static final ThreadLocalRandom instance=new ThreadLocalRandom();
    // IllegalArgumentException messages
    static final String BadBound="bound must be positive";
    static final String BadRange="bound must be greater than origin";
    static final String BadSize="size must be non-negative";
    private static final AtomicInteger probeGenerator=
            new AtomicInteger();
    private static final AtomicLong seeder=new AtomicLong(initialSeed());
    private static final long GAMMA=0x9e3779b97f4a7c15L;
    private static final int PROBE_INCREMENT=0x9e3779b9;
    private static final long SEEDER_INCREMENT=0xbb67ae8584caa73bL;
    // Constants from SplittableRandom
    private static final double DOUBLE_UNIT=0x1.0p-53;  // 1.0  / (1L << 53)
    private static final float FLOAT_UNIT=0x1.0p-24f; // 1.0f / (1 << 24)
    private static final ThreadLocal<Double> nextLocalGaussian=
            new ThreadLocal<Double>();
    // Serialization support
    private static final long serialVersionUID=-5851777807851030925L;
    private static final ObjectStreamField[] serialPersistentFields={
            new ObjectStreamField("rnd",long.class),
            new ObjectStreamField("initialized",boolean.class),
    };
    // Unsafe mechanics
    private static final sun.misc.Unsafe UNSAFE;
    private static final long SEED;
    private static final long PROBE;
    private static final long SECONDARY;

    static{
        try{
            UNSAFE=sun.misc.Unsafe.getUnsafe();
            Class<?> tk=Thread.class;
            SEED=UNSAFE.objectFieldOffset
                    (tk.getDeclaredField("threadLocalRandomSeed"));
            PROBE=UNSAFE.objectFieldOffset
                    (tk.getDeclaredField("threadLocalRandomProbe"));
            SECONDARY=UNSAFE.objectFieldOffset
                    (tk.getDeclaredField("threadLocalRandomSecondarySeed"));
        }catch(Exception e){
            throw new Error(e);
        }
    }

    boolean initialized;

    private ThreadLocalRandom(){
        initialized=true; // false during super() call
    }

    private static long initialSeed(){
        String pp=java.security.AccessController.doPrivileged(
                new sun.security.action.GetPropertyAction(
                        "java.util.secureRandomSeed"));
        if(pp!=null&&pp.equalsIgnoreCase("true")){
            byte[] seedBytes=java.security.SecureRandom.getSeed(8);
            long s=(long)(seedBytes[0])&0xffL;
            for(int i=1;i<8;++i)
                s=(s<<8)|((long)(seedBytes[i])&0xffL);
            return s;
        }
        return (mix64(System.currentTimeMillis())^
                mix64(System.nanoTime()));
    }

    private static long mix64(long z){
        z=(z^(z>>>33))*0xff51afd7ed558ccdL;
        z=(z^(z>>>33))*0xc4ceb9fe1a85ec53L;
        return z^(z>>>33);
    }

    static final int getProbe(){
        return UNSAFE.getInt(Thread.currentThread(),PROBE);
    }

    static final int advanceProbe(int probe){
        probe^=probe<<13;   // xorshift
        probe^=probe>>>17;
        probe^=probe<<5;
        UNSAFE.putInt(Thread.currentThread(),PROBE,probe);
        return probe;
    }

    static final int nextSecondarySeed(){
        int r;
        Thread t=Thread.currentThread();
        if((r=UNSAFE.getInt(t,SECONDARY))!=0){
            r^=r<<13;   // xorshift
            r^=r>>>17;
            r^=r<<5;
        }else{
            localInit();
            if((r=(int)UNSAFE.getLong(t,SEED))==0)
                r=1; // avoid zero
        }
        UNSAFE.putInt(t,SECONDARY,r);
        return r;
    }

    static final void localInit(){
        int p=probeGenerator.addAndGet(PROBE_INCREMENT);
        int probe=(p==0)?1:p; // skip 0
        long seed=mix64(seeder.getAndAdd(SEEDER_INCREMENT));
        Thread t=Thread.currentThread();
        UNSAFE.putLong(t,SEED,seed);
        UNSAFE.putInt(t,PROBE,probe);
    }

    public void setSeed(long seed){
        // only allow call from super() constructor
        if(initialized)
            throw new UnsupportedOperationException();
    }

    // We must define this, but never use it.
    protected int next(int bits){
        return (int)(mix64(nextSeed())>>>(64-bits));
    }

    final long nextSeed(){
        Thread t;
        long r; // read and update per-thread seed
        UNSAFE.putLong(t=Thread.currentThread(),SEED,
                r=UNSAFE.getLong(t,SEED)+GAMMA);
        return r;
    }

    public int nextInt(){
        return mix32(nextSeed());
    }

    private static int mix32(long z){
        z=(z^(z>>>33))*0xff51afd7ed558ccdL;
        return (int)(((z^(z>>>33))*0xc4ceb9fe1a85ec53L)>>>32);
    }

    public int nextInt(int bound){
        if(bound<=0)
            throw new IllegalArgumentException(BadBound);
        int r=mix32(nextSeed());
        int m=bound-1;
        if((bound&m)==0) // power of two
            r&=m;
        else{ // reject over-represented candidates
            for(int u=r>>>1;
                u+m-(r=u%bound)<0;
                u=mix32(nextSeed())>>>1)
                ;
        }
        return r;
    }

    public long nextLong(){
        return mix64(nextSeed());
    }

    public boolean nextBoolean(){
        return mix32(nextSeed())<0;
    }

    public float nextFloat(){
        return (mix32(nextSeed())>>>8)*FLOAT_UNIT;
    }

    public double nextDouble(){
        return (mix64(nextSeed())>>>11)*DOUBLE_UNIT;
    }
    // stream methods, coded in a way intended to better isolate for
    // maintenance purposes the small differences across forms.

    public double nextGaussian(){
        // Use nextLocalGaussian instead of nextGaussian field
        Double d=nextLocalGaussian.get();
        if(d!=null){
            nextLocalGaussian.set(null);
            return d.doubleValue();
        }
        double v1, v2, s;
        do{
            v1=2*nextDouble()-1; // between -1 and 1
            v2=2*nextDouble()-1; // between -1 and 1
            s=v1*v1+v2*v2;
        }while(s>=1||s==0);
        double multiplier=StrictMath.sqrt(-2*StrictMath.log(s)/s);
        nextLocalGaussian.set(new Double(v2*multiplier));
        return v1*multiplier;
    }

    public IntStream ints(long streamSize){
        if(streamSize<0L)
            throw new IllegalArgumentException(BadSize);
        return StreamSupport.intStream
                (new RandomIntsSpliterator
                                (0L,streamSize,Integer.MAX_VALUE,0),
                        false);
    }

    public IntStream ints(){
        return StreamSupport.intStream
                (new RandomIntsSpliterator
                                (0L,Long.MAX_VALUE,Integer.MAX_VALUE,0),
                        false);
    }

    public IntStream ints(long streamSize,int randomNumberOrigin,
                          int randomNumberBound){
        if(streamSize<0L)
            throw new IllegalArgumentException(BadSize);
        if(randomNumberOrigin>=randomNumberBound)
            throw new IllegalArgumentException(BadRange);
        return StreamSupport.intStream
                (new RandomIntsSpliterator
                                (0L,streamSize,randomNumberOrigin,randomNumberBound),
                        false);
    }

    public IntStream ints(int randomNumberOrigin,int randomNumberBound){
        if(randomNumberOrigin>=randomNumberBound)
            throw new IllegalArgumentException(BadRange);
        return StreamSupport.intStream
                (new RandomIntsSpliterator
                                (0L,Long.MAX_VALUE,randomNumberOrigin,randomNumberBound),
                        false);
    }

    public LongStream longs(long streamSize){
        if(streamSize<0L)
            throw new IllegalArgumentException(BadSize);
        return StreamSupport.longStream
                (new RandomLongsSpliterator
                                (0L,streamSize,Long.MAX_VALUE,0L),
                        false);
    }

    public LongStream longs(){
        return StreamSupport.longStream
                (new RandomLongsSpliterator
                                (0L,Long.MAX_VALUE,Long.MAX_VALUE,0L),
                        false);
    }

    public LongStream longs(long streamSize,long randomNumberOrigin,
                            long randomNumberBound){
        if(streamSize<0L)
            throw new IllegalArgumentException(BadSize);
        if(randomNumberOrigin>=randomNumberBound)
            throw new IllegalArgumentException(BadRange);
        return StreamSupport.longStream
                (new RandomLongsSpliterator
                                (0L,streamSize,randomNumberOrigin,randomNumberBound),
                        false);
    }

    public LongStream longs(long randomNumberOrigin,long randomNumberBound){
        if(randomNumberOrigin>=randomNumberBound)
            throw new IllegalArgumentException(BadRange);
        return StreamSupport.longStream
                (new RandomLongsSpliterator
                                (0L,Long.MAX_VALUE,randomNumberOrigin,randomNumberBound),
                        false);
    }

    public DoubleStream doubles(long streamSize){
        if(streamSize<0L)
            throw new IllegalArgumentException(BadSize);
        return StreamSupport.doubleStream
                (new RandomDoublesSpliterator
                                (0L,streamSize,Double.MAX_VALUE,0.0),
                        false);
    }

    public DoubleStream doubles(){
        return StreamSupport.doubleStream
                (new RandomDoublesSpliterator
                                (0L,Long.MAX_VALUE,Double.MAX_VALUE,0.0),
                        false);
    }

    public DoubleStream doubles(long streamSize,double randomNumberOrigin,
                                double randomNumberBound){
        if(streamSize<0L)
            throw new IllegalArgumentException(BadSize);
        if(!(randomNumberOrigin<randomNumberBound))
            throw new IllegalArgumentException(BadRange);
        return StreamSupport.doubleStream
                (new RandomDoublesSpliterator
                                (0L,streamSize,randomNumberOrigin,randomNumberBound),
                        false);
    }

    public DoubleStream doubles(double randomNumberOrigin,double randomNumberBound){
        if(!(randomNumberOrigin<randomNumberBound))
            throw new IllegalArgumentException(BadRange);
        return StreamSupport.doubleStream
                (new RandomDoublesSpliterator
                                (0L,Long.MAX_VALUE,randomNumberOrigin,randomNumberBound),
                        false);
    }

    public int nextInt(int origin,int bound){
        if(origin>=bound)
            throw new IllegalArgumentException(BadRange);
        return internalNextInt(origin,bound);
    }

    final int internalNextInt(int origin,int bound){
        int r=mix32(nextSeed());
        if(origin<bound){
            int n=bound-origin, m=n-1;
            if((n&m)==0)
                r=(r&m)+origin;
            else if(n>0){
                for(int u=r>>>1;
                    u+m-(r=u%n)<0;
                    u=mix32(nextSeed())>>>1)
                    ;
                r+=origin;
            }else{
                while(r<origin||r>=bound)
                    r=mix32(nextSeed());
            }
        }
        return r;
    }
    // Within-package utilities

    public long nextLong(long bound){
        if(bound<=0)
            throw new IllegalArgumentException(BadBound);
        long r=mix64(nextSeed());
        long m=bound-1;
        if((bound&m)==0L) // power of two
            r&=m;
        else{ // reject over-represented candidates
            for(long u=r>>>1;
                u+m-(r=u%bound)<0L;
                u=mix64(nextSeed())>>>1)
                ;
        }
        return r;
    }

    public long nextLong(long origin,long bound){
        if(origin>=bound)
            throw new IllegalArgumentException(BadRange);
        return internalNextLong(origin,bound);
    }

    final long internalNextLong(long origin,long bound){
        long r=mix64(nextSeed());
        if(origin<bound){
            long n=bound-origin, m=n-1;
            if((n&m)==0L)  // power of two
                r=(r&m)+origin;
            else if(n>0L){  // reject over-represented candidates
                for(long u=r>>>1;            // ensure nonnegative
                    u+m-(r=u%n)<0L;    // rejection check
                    u=mix64(nextSeed())>>>1) // retry
                    ;
                r+=origin;
            }else{              // range not representable as long
                while(r<origin||r>=bound)
                    r=mix64(nextSeed());
            }
        }
        return r;
    }

    public double nextDouble(double bound){
        if(!(bound>0.0))
            throw new IllegalArgumentException(BadBound);
        double result=(mix64(nextSeed())>>>11)*DOUBLE_UNIT*bound;
        return (result<bound)?result: // correct for rounding
                Double.longBitsToDouble(Double.doubleToLongBits(bound)-1);
    }

    public double nextDouble(double origin,double bound){
        if(!(origin<bound))
            throw new IllegalArgumentException(BadRange);
        return internalNextDouble(origin,bound);
    }

    final double internalNextDouble(double origin,double bound){
        double r=(nextLong()>>>11)*DOUBLE_UNIT;
        if(origin<bound){
            r=r*(bound-origin)+origin;
            if(r>=bound) // correct for rounding
                r=Double.longBitsToDouble(Double.doubleToLongBits(bound)-1);
        }
        return r;
    }

    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException{
        java.io.ObjectOutputStream.PutField fields=s.putFields();
        fields.put("rnd",UNSAFE.getLong(Thread.currentThread(),SEED));
        fields.put("initialized",true);
        s.writeFields();
    }

    private Object readResolve(){
        return current();
    }

    public static ThreadLocalRandom current(){
        if(UNSAFE.getInt(Thread.currentThread(),PROBE)==0)
            localInit();
        return instance;
    }

    static final class RandomIntsSpliterator implements Spliterator.OfInt{
        final long fence;
        final int origin;
        final int bound;
        long index;

        RandomIntsSpliterator(long index,long fence,
                              int origin,int bound){
            this.index=index;
            this.fence=fence;
            this.origin=origin;
            this.bound=bound;
        }

        public RandomIntsSpliterator trySplit(){
            long i=index, m=(i+fence)>>>1;
            return (m<=i)?null:
                    new RandomIntsSpliterator(i,index=m,origin,bound);
        }

        public boolean tryAdvance(IntConsumer consumer){
            if(consumer==null) throw new NullPointerException();
            long i=index, f=fence;
            if(i<f){
                consumer.accept(ThreadLocalRandom.current().internalNextInt(origin,bound));
                index=i+1;
                return true;
            }
            return false;
        }

        public void forEachRemaining(IntConsumer consumer){
            if(consumer==null) throw new NullPointerException();
            long i=index, f=fence;
            if(i<f){
                index=f;
                int o=origin, b=bound;
                ThreadLocalRandom rng=ThreadLocalRandom.current();
                do{
                    consumer.accept(rng.internalNextInt(o,b));
                }while(++i<f);
            }
        }

        public long estimateSize(){
            return fence-index;
        }        public int characteristics(){
            return (Spliterator.SIZED|Spliterator.SUBSIZED|
                    Spliterator.NONNULL|Spliterator.IMMUTABLE);
        }


    }

    static final class RandomLongsSpliterator implements Spliterator.OfLong{
        final long fence;
        final long origin;
        final long bound;
        long index;

        RandomLongsSpliterator(long index,long fence,
                               long origin,long bound){
            this.index=index;
            this.fence=fence;
            this.origin=origin;
            this.bound=bound;
        }

        public RandomLongsSpliterator trySplit(){
            long i=index, m=(i+fence)>>>1;
            return (m<=i)?null:
                    new RandomLongsSpliterator(i,index=m,origin,bound);
        }

        public boolean tryAdvance(LongConsumer consumer){
            if(consumer==null) throw new NullPointerException();
            long i=index, f=fence;
            if(i<f){
                consumer.accept(ThreadLocalRandom.current().internalNextLong(origin,bound));
                index=i+1;
                return true;
            }
            return false;
        }

        public void forEachRemaining(LongConsumer consumer){
            if(consumer==null) throw new NullPointerException();
            long i=index, f=fence;
            if(i<f){
                index=f;
                long o=origin, b=bound;
                ThreadLocalRandom rng=ThreadLocalRandom.current();
                do{
                    consumer.accept(rng.internalNextLong(o,b));
                }while(++i<f);
            }
        }        public long estimateSize(){
            return fence-index;
        }



        public int characteristics(){
            return (Spliterator.SIZED|Spliterator.SUBSIZED|
                    Spliterator.NONNULL|Spliterator.IMMUTABLE);
        }
    }

    static final class RandomDoublesSpliterator implements Spliterator.OfDouble{
        final long fence;
        final double origin;
        final double bound;
        long index;

        RandomDoublesSpliterator(long index,long fence,
                                 double origin,double bound){
            this.index=index;
            this.fence=fence;
            this.origin=origin;
            this.bound=bound;
        }

        public RandomDoublesSpliterator trySplit(){
            long i=index, m=(i+fence)>>>1;
            return (m<=i)?null:
                    new RandomDoublesSpliterator(i,index=m,origin,bound);
        }

        public boolean tryAdvance(DoubleConsumer consumer){
            if(consumer==null) throw new NullPointerException();
            long i=index, f=fence;
            if(i<f){
                consumer.accept(ThreadLocalRandom.current().internalNextDouble(origin,bound));
                index=i+1;
                return true;
            }
            return false;
        }

        public void forEachRemaining(DoubleConsumer consumer){
            if(consumer==null) throw new NullPointerException();
            long i=index, f=fence;
            if(i<f){
                index=f;
                double o=origin, b=bound;
                ThreadLocalRandom rng=ThreadLocalRandom.current();
                do{
                    consumer.accept(rng.internalNextDouble(o,b));
                }while(++i<f);
            }
        }        public long estimateSize(){
            return fence-index;
        }



        public int characteristics(){
            return (Spliterator.SIZED|Spliterator.SUBSIZED|
                    Spliterator.NONNULL|Spliterator.IMMUTABLE);
        }
    }
}
