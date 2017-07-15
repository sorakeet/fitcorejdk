/**
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.stream;

import java.util.*;
import java.util.function.*;

public interface DoubleStream extends BaseStream<Double,DoubleStream>{
    public static Builder builder(){
        return new Streams.DoubleStreamBuilderImpl();
    }

    public static DoubleStream empty(){
        return StreamSupport.doubleStream(Spliterators.emptyDoubleSpliterator(),false);
    }

    public static DoubleStream of(double t){
        return StreamSupport.doubleStream(new Streams.DoubleStreamBuilderImpl(t),false);
    }

    public static DoubleStream of(double... values){
        return Arrays.stream(values);
    }

    public static DoubleStream iterate(final double seed,final DoubleUnaryOperator f){
        Objects.requireNonNull(f);
        final PrimitiveIterator.OfDouble iterator=new PrimitiveIterator.OfDouble(){
            double t=seed;

            @Override
            public boolean hasNext(){
                return true;
            }

            @Override
            public double nextDouble(){
                double v=t;
                t=f.applyAsDouble(t);
                return v;
            }
        };
        return StreamSupport.doubleStream(Spliterators.spliteratorUnknownSize(
                iterator,
                Spliterator.ORDERED|Spliterator.IMMUTABLE|Spliterator.NONNULL),false);
    }

    public static DoubleStream generate(DoubleSupplier s){
        Objects.requireNonNull(s);
        return StreamSupport.doubleStream(
                new StreamSpliterators.InfiniteSupplyingSpliterator.OfDouble(Long.MAX_VALUE,s),false);
    }

    public static DoubleStream concat(DoubleStream a,DoubleStream b){
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);
        Spliterator.OfDouble split=new Streams.ConcatSpliterator.OfDouble(
                a.spliterator(),b.spliterator());
        DoubleStream stream=StreamSupport.doubleStream(split,a.isParallel()||b.isParallel());
        return stream.onClose(Streams.composedClose(a,b));
    }

    DoubleStream filter(DoublePredicate predicate);

    DoubleStream map(DoubleUnaryOperator mapper);

    <U> Stream<U> mapToObj(DoubleFunction<? extends U> mapper);

    IntStream mapToInt(DoubleToIntFunction mapper);

    LongStream mapToLong(DoubleToLongFunction mapper);

    DoubleStream flatMap(DoubleFunction<? extends DoubleStream> mapper);

    DoubleStream distinct();

    DoubleStream sorted();

    DoubleStream peek(DoubleConsumer action);

    DoubleStream limit(long maxSize);

    DoubleStream skip(long n);

    void forEach(DoubleConsumer action);

    void forEachOrdered(DoubleConsumer action);

    double[] toArray();

    double reduce(double identity,DoubleBinaryOperator op);

    OptionalDouble reduce(DoubleBinaryOperator op);

    <R> R collect(Supplier<R> supplier,
                  ObjDoubleConsumer<R> accumulator,
                  BiConsumer<R,R> combiner);

    double sum();

    OptionalDouble min();

    OptionalDouble max();

    long count();

    OptionalDouble average();

    DoubleSummaryStatistics summaryStatistics();

    boolean anyMatch(DoublePredicate predicate);

    boolean allMatch(DoublePredicate predicate);

    boolean noneMatch(DoublePredicate predicate);
    // Static factories

    OptionalDouble findFirst();

    OptionalDouble findAny();

    Stream<Double> boxed();

    @Override
    PrimitiveIterator.OfDouble iterator();

    @Override
    Spliterator.OfDouble spliterator();

    @Override
    DoubleStream sequential();

    @Override
    DoubleStream parallel();

    public interface Builder extends DoubleConsumer{
        default Builder add(double t){
            accept(t);
            return this;
        }

        @Override
        void accept(double t);

        DoubleStream build();
    }
}
