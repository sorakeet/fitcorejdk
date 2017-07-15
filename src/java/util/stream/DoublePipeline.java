/**
 * Copyright (c) 2013, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.stream;

import java.util.*;
import java.util.function.*;

abstract class DoublePipeline<E_IN>
        extends AbstractPipeline<E_IN,Double,DoubleStream>
        implements DoubleStream{
    DoublePipeline(Supplier<? extends Spliterator<Double>> source,
                   int sourceFlags,boolean parallel){
        super(source,sourceFlags,parallel);
    }

    DoublePipeline(Spliterator<Double> source,
                   int sourceFlags,boolean parallel){
        super(source,sourceFlags,parallel);
    }

    DoublePipeline(AbstractPipeline<?,E_IN,?> upstream,int opFlags){
        super(upstream,opFlags);
    }

    @Override
    public final PrimitiveIterator.OfDouble iterator(){
        return Spliterators.iterator(spliterator());
    }

    @Override
    public final Spliterator.OfDouble spliterator(){
        return adapt(super.spliterator());
    }
    // Shape-specific methods

    @Override
    final StreamShape getOutputShape(){
        return StreamShape.DOUBLE_VALUE;
    }

    @Override
    final <P_IN> Node<Double> evaluateToNode(PipelineHelper<Double> helper,
                                             Spliterator<P_IN> spliterator,
                                             boolean flattenTree,
                                             IntFunction<Double[]> generator){
        return Nodes.collectDouble(helper,spliterator,flattenTree);
    }

    @Override
    final <P_IN> Spliterator<Double> wrap(PipelineHelper<Double> ph,
                                          Supplier<Spliterator<P_IN>> supplier,
                                          boolean isParallel){
        return new StreamSpliterators.DoubleWrappingSpliterator<>(ph,supplier,isParallel);
    }

    @Override
    @SuppressWarnings("unchecked")
    final Spliterator.OfDouble lazySpliterator(Supplier<? extends Spliterator<Double>> supplier){
        return new StreamSpliterators.DelegatingSpliterator.OfDouble((Supplier<Spliterator.OfDouble>)supplier);
    }

    @Override
    final void forEachWithCancel(Spliterator<Double> spliterator,Sink<Double> sink){
        Spliterator.OfDouble spl=adapt(spliterator);
        DoubleConsumer adaptedSink=adapt(sink);
        do{
        }while(!sink.cancellationRequested()&&spl.tryAdvance(adaptedSink));
    }

    private static DoubleConsumer adapt(Sink<Double> sink){
        if(sink instanceof DoubleConsumer){
            return (DoubleConsumer)sink;
        }else{
            if(Tripwire.ENABLED)
                Tripwire.trip(AbstractPipeline.class,
                        "using DoubleStream.adapt(Sink<Double> s)");
            return sink::accept;
        }
    }
    // DoubleStream

    @Override
    final Node.Builder<Double> makeNodeBuilder(long exactSizeIfKnown,IntFunction<Double[]> generator){
        return Nodes.doubleBuilder(exactSizeIfKnown);
    }

    private static Spliterator.OfDouble adapt(Spliterator<Double> s){
        if(s instanceof Spliterator.OfDouble){
            return (Spliterator.OfDouble)s;
        }else{
            if(Tripwire.ENABLED)
                Tripwire.trip(AbstractPipeline.class,
                        "using DoubleStream.adapt(Spliterator<Double> s)");
            throw new UnsupportedOperationException("DoubleStream.adapt(Spliterator<Double> s)");
        }
    }
    // Stateless intermediate ops from DoubleStream

    @Override
    public DoubleStream unordered(){
        if(!isOrdered())
            return this;
        return new StatelessOp<Double>(this,StreamShape.DOUBLE_VALUE,StreamOpFlag.NOT_ORDERED){
            @Override
            Sink<Double> opWrapSink(int flags,Sink<Double> sink){
                return sink;
            }
        };
    }

    @Override
    public final DoubleStream filter(DoublePredicate predicate){
        Objects.requireNonNull(predicate);
        return new StatelessOp<Double>(this,StreamShape.DOUBLE_VALUE,
                StreamOpFlag.NOT_SIZED){
            @Override
            Sink<Double> opWrapSink(int flags,Sink<Double> sink){
                return new Sink.ChainedDouble<Double>(sink){
                    @Override
                    public void begin(long size){
                        downstream.begin(-1);
                    }

                    @Override
                    public void accept(double t){
                        if(predicate.test(t))
                            downstream.accept(t);
                    }
                };
            }
        };
    }

    @Override
    public final DoubleStream map(DoubleUnaryOperator mapper){
        Objects.requireNonNull(mapper);
        return new StatelessOp<Double>(this,StreamShape.DOUBLE_VALUE,
                StreamOpFlag.NOT_SORTED|StreamOpFlag.NOT_DISTINCT){
            @Override
            Sink<Double> opWrapSink(int flags,Sink<Double> sink){
                return new Sink.ChainedDouble<Double>(sink){
                    @Override
                    public void accept(double t){
                        downstream.accept(mapper.applyAsDouble(t));
                    }
                };
            }
        };
    }

    @Override
    public final <U> Stream<U> mapToObj(DoubleFunction<? extends U> mapper){
        Objects.requireNonNull(mapper);
        return new ReferencePipeline.StatelessOp<Double,U>(this,StreamShape.DOUBLE_VALUE,
                StreamOpFlag.NOT_SORTED|StreamOpFlag.NOT_DISTINCT){
            @Override
            Sink<Double> opWrapSink(int flags,Sink<U> sink){
                return new Sink.ChainedDouble<U>(sink){
                    @Override
                    public void accept(double t){
                        downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final IntStream mapToInt(DoubleToIntFunction mapper){
        Objects.requireNonNull(mapper);
        return new IntPipeline.StatelessOp<Double>(this,StreamShape.DOUBLE_VALUE,
                StreamOpFlag.NOT_SORTED|StreamOpFlag.NOT_DISTINCT){
            @Override
            Sink<Double> opWrapSink(int flags,Sink<Integer> sink){
                return new Sink.ChainedDouble<Integer>(sink){
                    @Override
                    public void accept(double t){
                        downstream.accept(mapper.applyAsInt(t));
                    }
                };
            }
        };
    }

    @Override
    public final LongStream mapToLong(DoubleToLongFunction mapper){
        Objects.requireNonNull(mapper);
        return new LongPipeline.StatelessOp<Double>(this,StreamShape.DOUBLE_VALUE,
                StreamOpFlag.NOT_SORTED|StreamOpFlag.NOT_DISTINCT){
            @Override
            Sink<Double> opWrapSink(int flags,Sink<Long> sink){
                return new Sink.ChainedDouble<Long>(sink){
                    @Override
                    public void accept(double t){
                        downstream.accept(mapper.applyAsLong(t));
                    }
                };
            }
        };
    }

    @Override
    public final DoubleStream flatMap(DoubleFunction<? extends DoubleStream> mapper){
        return new StatelessOp<Double>(this,StreamShape.DOUBLE_VALUE,
                StreamOpFlag.NOT_SORTED|StreamOpFlag.NOT_DISTINCT|StreamOpFlag.NOT_SIZED){
            @Override
            Sink<Double> opWrapSink(int flags,Sink<Double> sink){
                return new Sink.ChainedDouble<Double>(sink){
                    @Override
                    public void begin(long size){
                        downstream.begin(-1);
                    }

                    @Override
                    public void accept(double t){
                        try(DoubleStream result=mapper.apply(t)){
                            // We can do better that this too; optimize for depth=0 case and just grab spliterator and forEach it
                            if(result!=null)
                                result.sequential().forEach(i->downstream.accept(i));
                        }
                    }
                };
            }
        };
    }

    @Override
    public final DoubleStream distinct(){
        // While functional and quick to implement, this approach is not very efficient.
        // An efficient version requires a double-specific map/set implementation.
        return boxed().distinct().mapToDouble(i->(double)i);
    }

    @Override
    public final DoubleStream sorted(){
        return SortedOps.makeDouble(this);
    }
    // Stateful intermediate ops from DoubleStream

    @Override
    public final DoubleStream peek(DoubleConsumer action){
        Objects.requireNonNull(action);
        return new StatelessOp<Double>(this,StreamShape.DOUBLE_VALUE,
                0){
            @Override
            Sink<Double> opWrapSink(int flags,Sink<Double> sink){
                return new Sink.ChainedDouble<Double>(sink){
                    @Override
                    public void accept(double t){
                        action.accept(t);
                        downstream.accept(t);
                    }
                };
            }
        };
    }

    @Override
    public final DoubleStream limit(long maxSize){
        if(maxSize<0)
            throw new IllegalArgumentException(Long.toString(maxSize));
        return SliceOps.makeDouble(this,(long)0,maxSize);
    }

    @Override
    public final DoubleStream skip(long n){
        if(n<0)
            throw new IllegalArgumentException(Long.toString(n));
        if(n==0)
            return this;
        else{
            long limit=-1;
            return SliceOps.makeDouble(this,n,limit);
        }
    }

    @Override
    public void forEach(DoubleConsumer consumer){
        evaluate(ForEachOps.makeDouble(consumer,false));
    }
    // Terminal ops from DoubleStream

    @Override
    public void forEachOrdered(DoubleConsumer consumer){
        evaluate(ForEachOps.makeDouble(consumer,true));
    }

    @Override
    public final double[] toArray(){
        return Nodes.flattenDouble((Node.OfDouble)evaluateToArrayNode(Double[]::new))
                .asPrimitiveArray();
    }

    @Override
    public final double reduce(double identity,DoubleBinaryOperator op){
        return evaluate(ReduceOps.makeDouble(identity,op));
    }

    @Override
    public final OptionalDouble reduce(DoubleBinaryOperator op){
        return evaluate(ReduceOps.makeDouble(op));
    }

    @Override
    public final <R> R collect(Supplier<R> supplier,
                               ObjDoubleConsumer<R> accumulator,
                               BiConsumer<R,R> combiner){
        BinaryOperator<R> operator=(left,right)->{
            combiner.accept(left,right);
            return left;
        };
        return evaluate(ReduceOps.makeDouble(supplier,accumulator,operator));
    }

    @Override
    public final double sum(){
        /**
         * In the arrays allocated for the collect operation, index 0
         * holds the high-order bits of the running sum, index 1 holds
         * the low-order bits of the sum computed via compensated
         * summation, and index 2 holds the simple sum used to compute
         * the proper result if the stream contains infinite values of
         * the same sign.
         */
        double[] summation=collect(()->new double[3],
                (ll,d)->{
                    Collectors.sumWithCompensation(ll,d);
                    ll[2]+=d;
                },
                (ll,rr)->{
                    Collectors.sumWithCompensation(ll,rr[0]);
                    Collectors.sumWithCompensation(ll,rr[1]);
                    ll[2]+=rr[2];
                });
        return Collectors.computeFinalSum(summation);
    }

    @Override
    public final OptionalDouble min(){
        return reduce(Math::min);
    }

    @Override
    public final OptionalDouble max(){
        return reduce(Math::max);
    }

    @Override
    public final long count(){
        return mapToLong(e->1L).sum();
    }

    @Override
    public final OptionalDouble average(){
        /**
         * In the arrays allocated for the collect operation, index 0
         * holds the high-order bits of the running sum, index 1 holds
         * the low-order bits of the sum computed via compensated
         * summation, index 2 holds the number of values seen, index 3
         * holds the simple sum.
         */
        double[] avg=collect(()->new double[4],
                (ll,d)->{
                    ll[2]++;
                    Collectors.sumWithCompensation(ll,d);
                    ll[3]+=d;
                },
                (ll,rr)->{
                    Collectors.sumWithCompensation(ll,rr[0]);
                    Collectors.sumWithCompensation(ll,rr[1]);
                    ll[2]+=rr[2];
                    ll[3]+=rr[3];
                });
        return avg[2]>0
                ?OptionalDouble.of(Collectors.computeFinalSum(avg)/avg[2])
                :OptionalDouble.empty();
    }

    @Override
    public final DoubleSummaryStatistics summaryStatistics(){
        return collect(DoubleSummaryStatistics::new,DoubleSummaryStatistics::accept,
                DoubleSummaryStatistics::combine);
    }

    @Override
    public final boolean anyMatch(DoublePredicate predicate){
        return evaluate(MatchOps.makeDouble(predicate,MatchOps.MatchKind.ANY));
    }

    @Override
    public final boolean allMatch(DoublePredicate predicate){
        return evaluate(MatchOps.makeDouble(predicate,MatchOps.MatchKind.ALL));
    }

    @Override
    public final boolean noneMatch(DoublePredicate predicate){
        return evaluate(MatchOps.makeDouble(predicate,MatchOps.MatchKind.NONE));
    }

    @Override
    public final OptionalDouble findFirst(){
        return evaluate(FindOps.makeDouble(true));
    }

    @Override
    public final OptionalDouble findAny(){
        return evaluate(FindOps.makeDouble(false));
    }

    @Override
    public final Stream<Double> boxed(){
        return mapToObj(Double::valueOf);
    }
    //

    static class Head<E_IN> extends DoublePipeline<E_IN>{
        Head(Supplier<? extends Spliterator<Double>> source,
             int sourceFlags,boolean parallel){
            super(source,sourceFlags,parallel);
        }

        Head(Spliterator<Double> source,
             int sourceFlags,boolean parallel){
            super(source,sourceFlags,parallel);
        }

        @Override
        public void forEach(DoubleConsumer consumer){
            if(!isParallel()){
                adapt(sourceStageSpliterator()).forEachRemaining(consumer);
            }else{
                super.forEach(consumer);
            }
        }

        @Override
        public void forEachOrdered(DoubleConsumer consumer){
            if(!isParallel()){
                adapt(sourceStageSpliterator()).forEachRemaining(consumer);
            }else{
                super.forEachOrdered(consumer);
            }
        }        @Override
        final boolean opIsStateful(){
            throw new UnsupportedOperationException();
        }



        @Override
        final Sink<E_IN> opWrapSink(int flags,Sink<Double> sink){
            throw new UnsupportedOperationException();
        }
        // Optimized sequential terminal operations for the head of the pipeline
    }

    abstract static class StatelessOp<E_IN> extends DoublePipeline<E_IN>{
        StatelessOp(AbstractPipeline<?,E_IN,?> upstream,
                    StreamShape inputShape,
                    int opFlags){
            super(upstream,opFlags);
            assert upstream.getOutputShape()==inputShape;
        }

        @Override
        final boolean opIsStateful(){
            return false;
        }
    }

    abstract static class StatefulOp<E_IN> extends DoublePipeline<E_IN>{
        StatefulOp(AbstractPipeline<?,E_IN,?> upstream,
                   StreamShape inputShape,
                   int opFlags){
            super(upstream,opFlags);
            assert upstream.getOutputShape()==inputShape;
        }

        @Override
        final boolean opIsStateful(){
            return true;
        }

        @Override
        abstract <P_IN> Node<Double> opEvaluateParallel(PipelineHelper<Double> helper,
                                                        Spliterator<P_IN> spliterator,
                                                        IntFunction<Double[]> generator);
    }
}
