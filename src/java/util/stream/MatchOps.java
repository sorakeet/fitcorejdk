/**
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.stream;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.*;

final class MatchOps{
    private MatchOps(){
    }

    public static <T> TerminalOp<T,Boolean> makeRef(Predicate<? super T> predicate,
                                                    MatchKind matchKind){
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(matchKind);
        class MatchSink extends BooleanTerminalSink<T>{
            MatchSink(){
                super(matchKind);
            }

            @Override
            public void accept(T t){
                if(!stop&&predicate.test(t)==matchKind.stopOnPredicateMatches){
                    stop=true;
                    value=matchKind.shortCircuitResult;
                }
            }
        }
        return new MatchOp<>(StreamShape.REFERENCE,matchKind,MatchSink::new);
    }

    public static TerminalOp<Integer,Boolean> makeInt(IntPredicate predicate,
                                                      MatchKind matchKind){
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(matchKind);
        class MatchSink extends BooleanTerminalSink<Integer> implements Sink.OfInt{
            MatchSink(){
                super(matchKind);
            }

            @Override
            public void accept(int t){
                if(!stop&&predicate.test(t)==matchKind.stopOnPredicateMatches){
                    stop=true;
                    value=matchKind.shortCircuitResult;
                }
            }
        }
        return new MatchOp<>(StreamShape.INT_VALUE,matchKind,MatchSink::new);
    }

    public static TerminalOp<Long,Boolean> makeLong(LongPredicate predicate,
                                                    MatchKind matchKind){
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(matchKind);
        class MatchSink extends BooleanTerminalSink<Long> implements Sink.OfLong{
            MatchSink(){
                super(matchKind);
            }

            @Override
            public void accept(long t){
                if(!stop&&predicate.test(t)==matchKind.stopOnPredicateMatches){
                    stop=true;
                    value=matchKind.shortCircuitResult;
                }
            }
        }
        return new MatchOp<>(StreamShape.LONG_VALUE,matchKind,MatchSink::new);
    }

    public static TerminalOp<Double,Boolean> makeDouble(DoublePredicate predicate,
                                                        MatchKind matchKind){
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(matchKind);
        class MatchSink extends BooleanTerminalSink<Double> implements Sink.OfDouble{
            MatchSink(){
                super(matchKind);
            }

            @Override
            public void accept(double t){
                if(!stop&&predicate.test(t)==matchKind.stopOnPredicateMatches){
                    stop=true;
                    value=matchKind.shortCircuitResult;
                }
            }
        }
        return new MatchOp<>(StreamShape.DOUBLE_VALUE,matchKind,MatchSink::new);
    }

    enum MatchKind{
        ANY(true,true),
        ALL(false,false),
        NONE(true,false);
        private final boolean stopOnPredicateMatches;
        private final boolean shortCircuitResult;

        private MatchKind(boolean stopOnPredicateMatches,
                          boolean shortCircuitResult){
            this.stopOnPredicateMatches=stopOnPredicateMatches;
            this.shortCircuitResult=shortCircuitResult;
        }
    }

    private static final class MatchOp<T> implements TerminalOp<T,Boolean>{
        final MatchKind matchKind;
        final Supplier<BooleanTerminalSink<T>> sinkSupplier;
        private final StreamShape inputShape;

        MatchOp(StreamShape shape,
                MatchKind matchKind,
                Supplier<BooleanTerminalSink<T>> sinkSupplier){
            this.inputShape=shape;
            this.matchKind=matchKind;
            this.sinkSupplier=sinkSupplier;
        }

        @Override
        public StreamShape inputShape(){
            return inputShape;
        }

        @Override
        public int getOpFlags(){
            return StreamOpFlag.IS_SHORT_CIRCUIT|StreamOpFlag.NOT_ORDERED;
        }

        @Override
        public <S> Boolean evaluateParallel(PipelineHelper<T> helper,
                                            Spliterator<S> spliterator){
            // Approach for parallel implementation:
            // - Decompose as per usual
            // - run match on leaf chunks, call result "b"
            // - if b == matchKind.shortCircuitOn, complete early and return b
            // - else if we complete normally, return !shortCircuitOn
            return new MatchTask<>(this,helper,spliterator).invoke();
        }

        @Override
        public <S> Boolean evaluateSequential(PipelineHelper<T> helper,
                                              Spliterator<S> spliterator){
            return helper.wrapAndCopyInto(sinkSupplier.get(),spliterator).getAndClearState();
        }
    }

    private static abstract class BooleanTerminalSink<T> implements Sink<T>{
        boolean stop;
        boolean value;

        BooleanTerminalSink(MatchKind matchKind){
            value=!matchKind.shortCircuitResult;
        }

        public boolean getAndClearState(){
            return value;
        }

        @Override
        public boolean cancellationRequested(){
            return stop;
        }
    }

    @SuppressWarnings("serial")
    private static final class MatchTask<P_IN,P_OUT>
            extends AbstractShortCircuitTask<P_IN,P_OUT,Boolean,MatchTask<P_IN,P_OUT>>{
        private final MatchOp<P_OUT> op;

        MatchTask(MatchOp<P_OUT> op,PipelineHelper<P_OUT> helper,
                  Spliterator<P_IN> spliterator){
            super(helper,spliterator);
            this.op=op;
        }

        MatchTask(MatchTask<P_IN,P_OUT> parent,Spliterator<P_IN> spliterator){
            super(parent,spliterator);
            this.op=parent.op;
        }

        @Override
        protected MatchTask<P_IN,P_OUT> makeChild(Spliterator<P_IN> spliterator){
            return new MatchTask<>(this,spliterator);
        }

        @Override
        protected Boolean doLeaf(){
            boolean b=helper.wrapAndCopyInto(op.sinkSupplier.get(),spliterator).getAndClearState();
            if(b==op.matchKind.shortCircuitResult)
                shortCircuit(b);
            return null;
        }

        @Override
        protected Boolean getEmptyResult(){
            return !op.matchKind.shortCircuitResult;
        }
    }
}

