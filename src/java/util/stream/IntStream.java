/**
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.stream;

import java.util.*;
import java.util.function.*;

public interface IntStream extends BaseStream<Integer,IntStream>{
    public static Builder builder(){
        return new Streams.IntStreamBuilderImpl();
    }

    public static IntStream of(int t){
        return StreamSupport.intStream(new Streams.IntStreamBuilderImpl(t),false);
    }

    public static IntStream of(int... values){
        return Arrays.stream(values);
    }

    public static IntStream iterate(final int seed,final IntUnaryOperator f){
        Objects.requireNonNull(f);
        final PrimitiveIterator.OfInt iterator=new PrimitiveIterator.OfInt(){
            int t=seed;

            @Override
            public boolean hasNext(){
                return true;
            }

            @Override
            public int nextInt(){
                int v=t;
                t=f.applyAsInt(t);
                return v;
            }
        };
        return StreamSupport.intStream(Spliterators.spliteratorUnknownSize(
                iterator,
                Spliterator.ORDERED|Spliterator.IMMUTABLE|Spliterator.NONNULL),false);
    }

    public static IntStream generate(IntSupplier s){
        Objects.requireNonNull(s);
        return StreamSupport.intStream(
                new StreamSpliterators.InfiniteSupplyingSpliterator.OfInt(Long.MAX_VALUE,s),false);
    }

    public static IntStream range(int startInclusive,int endExclusive){
        if(startInclusive>=endExclusive){
            return empty();
        }else{
            return StreamSupport.intStream(
                    new Streams.RangeIntSpliterator(startInclusive,endExclusive,false),false);
        }
    }

    public static IntStream empty(){
        return StreamSupport.intStream(Spliterators.emptyIntSpliterator(),false);
    }

    public static IntStream rangeClosed(int startInclusive,int endInclusive){
        if(startInclusive>endInclusive){
            return empty();
        }else{
            return StreamSupport.intStream(
                    new Streams.RangeIntSpliterator(startInclusive,endInclusive,true),false);
        }
    }

    public static IntStream concat(IntStream a,IntStream b){
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);
        Spliterator.OfInt split=new Streams.ConcatSpliterator.OfInt(
                a.spliterator(),b.spliterator());
        IntStream stream=StreamSupport.intStream(split,a.isParallel()||b.isParallel());
        return stream.onClose(Streams.composedClose(a,b));
    }

    IntStream filter(IntPredicate predicate);

    IntStream map(IntUnaryOperator mapper);

    <U> Stream<U> mapToObj(IntFunction<? extends U> mapper);

    LongStream mapToLong(IntToLongFunction mapper);

    DoubleStream mapToDouble(IntToDoubleFunction mapper);

    IntStream flatMap(IntFunction<? extends IntStream> mapper);

    IntStream distinct();

    IntStream sorted();

    IntStream peek(IntConsumer action);

    IntStream limit(long maxSize);

    IntStream skip(long n);

    void forEach(IntConsumer action);

    void forEachOrdered(IntConsumer action);

    int[] toArray();

    int reduce(int identity,IntBinaryOperator op);

    OptionalInt reduce(IntBinaryOperator op);

    <R> R collect(Supplier<R> supplier,
                  ObjIntConsumer<R> accumulator,
                  BiConsumer<R,R> combiner);

    int sum();

    OptionalInt min();

    OptionalInt max();

    long count();

    OptionalDouble average();

    IntSummaryStatistics summaryStatistics();

    boolean anyMatch(IntPredicate predicate);

    boolean allMatch(IntPredicate predicate);

    boolean noneMatch(IntPredicate predicate);
    // Static factories

    OptionalInt findFirst();

    OptionalInt findAny();

    LongStream asLongStream();

    DoubleStream asDoubleStream();

    Stream<Integer> boxed();

    @Override
    PrimitiveIterator.OfInt iterator();

    @Override
    Spliterator.OfInt spliterator();

    @Override
    IntStream sequential();

    @Override
    IntStream parallel();

    public interface Builder extends IntConsumer{
        default Builder add(int t){
            accept(t);
            return this;
        }

        @Override
        void accept(int t);

        IntStream build();
    }
}
