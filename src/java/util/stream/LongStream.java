/**
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.stream;

import java.util.*;
import java.util.function.*;

public interface LongStream extends BaseStream<Long,LongStream>{
    public static Builder builder(){
        return new Streams.LongStreamBuilderImpl();
    }

    public static LongStream empty(){
        return StreamSupport.longStream(Spliterators.emptyLongSpliterator(),false);
    }

    public static LongStream of(long t){
        return StreamSupport.longStream(new Streams.LongStreamBuilderImpl(t),false);
    }

    public static LongStream of(long... values){
        return Arrays.stream(values);
    }

    public static LongStream iterate(final long seed,final LongUnaryOperator f){
        Objects.requireNonNull(f);
        final PrimitiveIterator.OfLong iterator=new PrimitiveIterator.OfLong(){
            long t=seed;

            @Override
            public boolean hasNext(){
                return true;
            }

            @Override
            public long nextLong(){
                long v=t;
                t=f.applyAsLong(t);
                return v;
            }
        };
        return StreamSupport.longStream(Spliterators.spliteratorUnknownSize(
                iterator,
                Spliterator.ORDERED|Spliterator.IMMUTABLE|Spliterator.NONNULL),false);
    }

    public static LongStream generate(LongSupplier s){
        Objects.requireNonNull(s);
        return StreamSupport.longStream(
                new StreamSpliterators.InfiniteSupplyingSpliterator.OfLong(Long.MAX_VALUE,s),false);
    }

    public static LongStream range(long startInclusive,final long endExclusive){
        if(startInclusive>=endExclusive){
            return empty();
        }else if(endExclusive-startInclusive<0){
            // Size of range > Long.MAX_VALUE
            // Split the range in two and concatenate
            // Note: if the range is [Long.MIN_VALUE, Long.MAX_VALUE) then
            // the lower range, [Long.MIN_VALUE, 0) will be further split in two
            long m=startInclusive+Long.divideUnsigned(endExclusive-startInclusive,2)+1;
            return concat(range(startInclusive,m),range(m,endExclusive));
        }else{
            return StreamSupport.longStream(
                    new Streams.RangeLongSpliterator(startInclusive,endExclusive,false),false);
        }
    }

    public static LongStream rangeClosed(long startInclusive,final long endInclusive){
        if(startInclusive>endInclusive){
            return empty();
        }else if(endInclusive-startInclusive+1<=0){
            // Size of range > Long.MAX_VALUE
            // Split the range in two and concatenate
            // Note: if the range is [Long.MIN_VALUE, Long.MAX_VALUE] then
            // the lower range, [Long.MIN_VALUE, 0), and upper range,
            // [0, Long.MAX_VALUE], will both be further split in two
            long m=startInclusive+Long.divideUnsigned(endInclusive-startInclusive,2)+1;
            return concat(range(startInclusive,m),rangeClosed(m,endInclusive));
        }else{
            return StreamSupport.longStream(
                    new Streams.RangeLongSpliterator(startInclusive,endInclusive,true),false);
        }
    }

    public static LongStream concat(LongStream a,LongStream b){
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);
        Spliterator.OfLong split=new Streams.ConcatSpliterator.OfLong(
                a.spliterator(),b.spliterator());
        LongStream stream=StreamSupport.longStream(split,a.isParallel()||b.isParallel());
        return stream.onClose(Streams.composedClose(a,b));
    }

    LongStream filter(LongPredicate predicate);

    LongStream map(LongUnaryOperator mapper);

    <U> Stream<U> mapToObj(LongFunction<? extends U> mapper);

    IntStream mapToInt(LongToIntFunction mapper);

    DoubleStream mapToDouble(LongToDoubleFunction mapper);

    LongStream flatMap(LongFunction<? extends LongStream> mapper);

    LongStream distinct();

    LongStream sorted();

    LongStream peek(LongConsumer action);

    LongStream limit(long maxSize);

    LongStream skip(long n);

    void forEach(LongConsumer action);

    void forEachOrdered(LongConsumer action);

    long[] toArray();

    long reduce(long identity,LongBinaryOperator op);

    OptionalLong reduce(LongBinaryOperator op);

    <R> R collect(Supplier<R> supplier,
                  ObjLongConsumer<R> accumulator,
                  BiConsumer<R,R> combiner);

    long sum();

    OptionalLong min();

    OptionalLong max();

    long count();

    OptionalDouble average();

    LongSummaryStatistics summaryStatistics();

    boolean anyMatch(LongPredicate predicate);

    boolean allMatch(LongPredicate predicate);
    // Static factories

    boolean noneMatch(LongPredicate predicate);

    OptionalLong findFirst();

    OptionalLong findAny();

    DoubleStream asDoubleStream();

    Stream<Long> boxed();

    @Override
    PrimitiveIterator.OfLong iterator();

    @Override
    Spliterator.OfLong spliterator();

    @Override
    LongStream sequential();

    @Override
    LongStream parallel();

    public interface Builder extends LongConsumer{
        default Builder add(long t){
            accept(t);
            return this;
        }

        @Override
        void accept(long t);

        LongStream build();
    }
}
