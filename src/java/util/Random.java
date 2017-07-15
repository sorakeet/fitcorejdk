/**
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util;

import sun.misc.Unsafe;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.StreamSupport;

public class Random implements java.io.Serializable{
    static final long serialVersionUID=3905348978240129619L;
    // IllegalArgumentException messages
    static final String BadBound="bound must be positive";
    static final String BadRange="bound must be greater than origin";
    static final String BadSize="size must be non-negative";
    private static final long multiplier=0x5DEECE66DL;
    private static final long addend=0xBL;
    private static final long mask=(1L<<48)-1;
    private static final double DOUBLE_UNIT=0x1.0p-53; // 1.0 / (1L << 53)
    private static final AtomicLong seedUniquifier
            =new AtomicLong(8682522807148012L);
    private static final ObjectStreamField[] serialPersistentFields={
            new ObjectStreamField("seed",Long.TYPE),
            new ObjectStreamField("nextNextGaussian",Double.TYPE),
            new ObjectStreamField("haveNextNextGaussian",Boolean.TYPE)
    };
    // Support for resetting seed while deserializing
    private static final Unsafe unsafe=Unsafe.getUnsafe();
    private static final long seedOffset;

    static{
        try{
            seedOffset=unsafe.objectFieldOffset
                    (Random.class.getDeclaredField("seed"));
        }catch(Exception ex){
            throw new Error(ex);
        }
    }

    private final AtomicLong seed;
    private double nextNextGaussian;
    private boolean haveNextNextGaussian=false;

    public Random(){
        this(seedUniquifier()^System.nanoTime());
    }

    private static long seedUniquifier(){
        // L'Ecuyer, "Tables of Linear Congruential Generators of
        // Different Sizes and Good Lattice Structure", 1999
        for(;;){
            long current=seedUniquifier.get();
            long next=current*181783497276652981L;
            if(seedUniquifier.compareAndSet(current,next))
                return next;
        }
    }

    public Random(long seed){
        if(getClass()==Random.class)
            this.seed=new AtomicLong(initialScramble(seed));
        else{
            // subclass might have overriden setSeed
            this.seed=new AtomicLong();
            setSeed(seed);
        }
    }

    private static long initialScramble(long seed){
        return (seed^multiplier)&mask;
    }

    synchronized public void setSeed(long seed){
        this.seed.set(initialScramble(seed));
        haveNextNextGaussian=false;
    }

    public void nextBytes(byte[] bytes){
        for(int i=0, len=bytes.length;i<len;)
            for(int rnd=nextInt(),
                n=Math.min(len-i,Integer.SIZE/Byte.SIZE);
                n-->0;rnd>>=Byte.SIZE)
                bytes[i++]=(byte)rnd;
    }

    public int nextInt(){
        return next(32);
    }

    protected int next(int bits){
        long oldseed, nextseed;
        AtomicLong seed=this.seed;
        do{
            oldseed=seed.get();
            nextseed=(oldseed*multiplier+addend)&mask;
        }while(!seed.compareAndSet(oldseed,nextseed));
        return (int)(nextseed>>>(48-bits));
    }

    final long internalNextLong(long origin,long bound){
        long r=nextLong();
        if(origin<bound){
            long n=bound-origin, m=n-1;
            if((n&m)==0L)  // power of two
                r=(r&m)+origin;
            else if(n>0L){  // reject over-represented candidates
                for(long u=r>>>1;            // ensure nonnegative
                    u+m-(r=u%n)<0L;    // rejection check
                    u=nextLong()>>>1) // retry
                    ;
                r+=origin;
            }else{              // range not representable as long
                while(r<origin||r>=bound)
                    r=nextLong();
            }
        }
        return r;
    }

    public long nextLong(){
        // it's okay that the bottom word remains signed.
        return ((long)(next(32))<<32)+next(32);
    }

    final int internalNextInt(int origin,int bound){
        if(origin<bound){
            int n=bound-origin;
            if(n>0){
                return nextInt(n)+origin;
            }else{  // range not representable as int
                int r;
                do{
                    r=nextInt();
                }while(r<origin||r>=bound);
                return r;
            }
        }else{
            return nextInt();
        }
    }

    public int nextInt(int bound){
        if(bound<=0)
            throw new IllegalArgumentException(BadBound);
        int r=next(31);
        int m=bound-1;
        if((bound&m)==0)  // i.e., bound is a power of 2
            r=(int)((bound*(long)r)>>31);
        else{
            for(int u=r;
                u-(r=u%bound)+m<0;
                u=next(31))
                ;
        }
        return r;
    }

    final double internalNextDouble(double origin,double bound){
        double r=nextDouble();
        if(origin<bound){
            r=r*(bound-origin)+origin;
            if(r>=bound) // correct for rounding
                r=Double.longBitsToDouble(Double.doubleToLongBits(bound)-1);
        }
        return r;
    }
    // stream methods, coded in a way intended to better isolate for
    // maintenance purposes the small differences across forms.

    public double nextDouble(){
        return (((long)(next(26))<<27)+next(27))*DOUBLE_UNIT;
    }

    public boolean nextBoolean(){
        return next(1)!=0;
    }

    public float nextFloat(){
        return next(24)/((float)(1<<24));
    }

    synchronized public double nextGaussian(){
        // See Knuth, ACP, Section 3.4.1 Algorithm C.
        if(haveNextNextGaussian){
            haveNextNextGaussian=false;
            return nextNextGaussian;
        }else{
            double v1, v2, s;
            do{
                v1=2*nextDouble()-1; // between -1 and 1
                v2=2*nextDouble()-1; // between -1 and 1
                s=v1*v1+v2*v2;
            }while(s>=1||s==0);
            double multiplier=StrictMath.sqrt(-2*StrictMath.log(s)/s);
            nextNextGaussian=v2*multiplier;
            haveNextNextGaussian=true;
            return v1*multiplier;
        }
    }

    public IntStream ints(long streamSize){
        if(streamSize<0L)
            throw new IllegalArgumentException(BadSize);
        return StreamSupport.intStream
                (new RandomIntsSpliterator
                                (this,0L,streamSize,Integer.MAX_VALUE,0),
                        false);
    }

    public IntStream ints(){
        return StreamSupport.intStream
                (new RandomIntsSpliterator
                                (this,0L,Long.MAX_VALUE,Integer.MAX_VALUE,0),
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
                                (this,0L,streamSize,randomNumberOrigin,randomNumberBound),
                        false);
    }

    public IntStream ints(int randomNumberOrigin,int randomNumberBound){
        if(randomNumberOrigin>=randomNumberBound)
            throw new IllegalArgumentException(BadRange);
        return StreamSupport.intStream
                (new RandomIntsSpliterator
                                (this,0L,Long.MAX_VALUE,randomNumberOrigin,randomNumberBound),
                        false);
    }

    public LongStream longs(long streamSize){
        if(streamSize<0L)
            throw new IllegalArgumentException(BadSize);
        return StreamSupport.longStream
                (new RandomLongsSpliterator
                                (this,0L,streamSize,Long.MAX_VALUE,0L),
                        false);
    }

    public LongStream longs(){
        return StreamSupport.longStream
                (new RandomLongsSpliterator
                                (this,0L,Long.MAX_VALUE,Long.MAX_VALUE,0L),
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
                                (this,0L,streamSize,randomNumberOrigin,randomNumberBound),
                        false);
    }

    public LongStream longs(long randomNumberOrigin,long randomNumberBound){
        if(randomNumberOrigin>=randomNumberBound)
            throw new IllegalArgumentException(BadRange);
        return StreamSupport.longStream
                (new RandomLongsSpliterator
                                (this,0L,Long.MAX_VALUE,randomNumberOrigin,randomNumberBound),
                        false);
    }

    public DoubleStream doubles(long streamSize){
        if(streamSize<0L)
            throw new IllegalArgumentException(BadSize);
        return StreamSupport.doubleStream
                (new RandomDoublesSpliterator
                                (this,0L,streamSize,Double.MAX_VALUE,0.0),
                        false);
    }

    public DoubleStream doubles(){
        return StreamSupport.doubleStream
                (new RandomDoublesSpliterator
                                (this,0L,Long.MAX_VALUE,Double.MAX_VALUE,0.0),
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
                                (this,0L,streamSize,randomNumberOrigin,randomNumberBound),
                        false);
    }

    public DoubleStream doubles(double randomNumberOrigin,double randomNumberBound){
        if(!(randomNumberOrigin<randomNumberBound))
            throw new IllegalArgumentException(BadRange);
        return StreamSupport.doubleStream
                (new RandomDoublesSpliterator
                                (this,0L,Long.MAX_VALUE,randomNumberOrigin,randomNumberBound),
                        false);
    }

    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException{
        ObjectInputStream.GetField fields=s.readFields();
        // The seed is read in as {@code long} for
        // historical reasons, but it is converted to an AtomicLong.
        long seedVal=fields.get("seed",-1L);
        if(seedVal<0)
            throw new java.io.StreamCorruptedException(
                    "Random: invalid seed");
        resetSeed(seedVal);
        nextNextGaussian=fields.get("nextNextGaussian",0.0);
        haveNextNextGaussian=fields.get("haveNextNextGaussian",false);
    }

    private void resetSeed(long seedVal){
        unsafe.putObjectVolatile(this,seedOffset,new AtomicLong(seedVal));
    }

    synchronized private void writeObject(ObjectOutputStream s)
            throws IOException{
        // set the values of the Serializable fields
        ObjectOutputStream.PutField fields=s.putFields();
        // The seed is serialized as a long for historical reasons.
        fields.put("seed",seed.get());
        fields.put("nextNextGaussian",nextNextGaussian);
        fields.put("haveNextNextGaussian",haveNextNextGaussian);
        // save them
        s.writeFields();
    }

    static final class RandomIntsSpliterator implements Spliterator.OfInt{
        final Random rng;
        final long fence;
        final int origin;
        final int bound;
        long index;

        RandomIntsSpliterator(Random rng,long index,long fence,
                              int origin,int bound){
            this.rng=rng;
            this.index=index;
            this.fence=fence;
            this.origin=origin;
            this.bound=bound;
        }

        public RandomIntsSpliterator trySplit(){
            long i=index, m=(i+fence)>>>1;
            return (m<=i)?null:
                    new RandomIntsSpliterator(rng,i,index=m,origin,bound);
        }

        public boolean tryAdvance(IntConsumer consumer){
            if(consumer==null) throw new NullPointerException();
            long i=index, f=fence;
            if(i<f){
                consumer.accept(rng.internalNextInt(origin,bound));
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
                Random r=rng;
                int o=origin, b=bound;
                do{
                    consumer.accept(r.internalNextInt(o,b));
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
        final Random rng;
        final long fence;
        final long origin;
        final long bound;
        long index;

        RandomLongsSpliterator(Random rng,long index,long fence,
                               long origin,long bound){
            this.rng=rng;
            this.index=index;
            this.fence=fence;
            this.origin=origin;
            this.bound=bound;
        }

        public RandomLongsSpliterator trySplit(){
            long i=index, m=(i+fence)>>>1;
            return (m<=i)?null:
                    new RandomLongsSpliterator(rng,i,index=m,origin,bound);
        }

        public boolean tryAdvance(LongConsumer consumer){
            if(consumer==null) throw new NullPointerException();
            long i=index, f=fence;
            if(i<f){
                consumer.accept(rng.internalNextLong(origin,bound));
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
                Random r=rng;
                long o=origin, b=bound;
                do{
                    consumer.accept(r.internalNextLong(o,b));
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
        final Random rng;
        final long fence;
        final double origin;
        final double bound;
        long index;

        RandomDoublesSpliterator(Random rng,long index,long fence,
                                 double origin,double bound){
            this.rng=rng;
            this.index=index;
            this.fence=fence;
            this.origin=origin;
            this.bound=bound;
        }

        public RandomDoublesSpliterator trySplit(){
            long i=index, m=(i+fence)>>>1;
            return (m<=i)?null:
                    new RandomDoublesSpliterator(rng,i,index=m,origin,bound);
        }

        public boolean tryAdvance(DoubleConsumer consumer){
            if(consumer==null) throw new NullPointerException();
            long i=index, f=fence;
            if(i<f){
                consumer.accept(rng.internalNextDouble(origin,bound));
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
                Random r=rng;
                double o=origin, b=bound;
                do{
                    consumer.accept(r.internalNextDouble(o,b));
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
