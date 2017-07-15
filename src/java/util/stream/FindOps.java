/**
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.stream;

import java.util.*;
import java.util.concurrent.CountedCompleter;
import java.util.function.Predicate;
import java.util.function.Supplier;

final class FindOps{
    private FindOps(){
    }

    public static <T> TerminalOp<T,Optional<T>> makeRef(boolean mustFindFirst){
        return new FindOp<>(mustFindFirst,StreamShape.REFERENCE,Optional.empty(),
                Optional::isPresent,FindSink.OfRef::new);
    }

    public static TerminalOp<Integer,OptionalInt> makeInt(boolean mustFindFirst){
        return new FindOp<>(mustFindFirst,StreamShape.INT_VALUE,OptionalInt.empty(),
                OptionalInt::isPresent,FindSink.OfInt::new);
    }

    public static TerminalOp<Long,OptionalLong> makeLong(boolean mustFindFirst){
        return new FindOp<>(mustFindFirst,StreamShape.LONG_VALUE,OptionalLong.empty(),
                OptionalLong::isPresent,FindSink.OfLong::new);
    }

    public static TerminalOp<Double,OptionalDouble> makeDouble(boolean mustFindFirst){
        return new FindOp<>(mustFindFirst,StreamShape.DOUBLE_VALUE,OptionalDouble.empty(),
                OptionalDouble::isPresent,FindSink.OfDouble::new);
    }

    private static final class FindOp<T,O> implements TerminalOp<T,O>{
        final boolean mustFindFirst;
        final O emptyValue;
        final Predicate<O> presentPredicate;
        final Supplier<TerminalSink<T,O>> sinkSupplier;
        private final StreamShape shape;

        FindOp(boolean mustFindFirst,
               StreamShape shape,
               O emptyValue,
               Predicate<O> presentPredicate,
               Supplier<TerminalSink<T,O>> sinkSupplier){
            this.mustFindFirst=mustFindFirst;
            this.shape=shape;
            this.emptyValue=emptyValue;
            this.presentPredicate=presentPredicate;
            this.sinkSupplier=sinkSupplier;
        }

        @Override
        public StreamShape inputShape(){
            return shape;
        }

        @Override
        public int getOpFlags(){
            return StreamOpFlag.IS_SHORT_CIRCUIT|(mustFindFirst?0:StreamOpFlag.NOT_ORDERED);
        }

        @Override
        public <P_IN> O evaluateParallel(PipelineHelper<T> helper,
                                         Spliterator<P_IN> spliterator){
            return new FindTask<>(this,helper,spliterator).invoke();
        }

        @Override
        public <S> O evaluateSequential(PipelineHelper<T> helper,
                                        Spliterator<S> spliterator){
            O result=helper.wrapAndCopyInto(sinkSupplier.get(),spliterator).get();
            return result!=null?result:emptyValue;
        }
    }

    private static abstract class FindSink<T,O> implements TerminalSink<T,O>{
        boolean hasValue;
        T value;

        FindSink(){
        } // Avoid creation of special accessor

        @Override
        public void accept(T value){
            if(!hasValue){
                hasValue=true;
                this.value=value;
            }
        }

        @Override
        public boolean cancellationRequested(){
            return hasValue;
        }

        static final class OfRef<T> extends FindSink<T,Optional<T>>{
            @Override
            public Optional<T> get(){
                return hasValue?Optional.of(value):null;
            }
        }

        static final class OfInt extends FindSink<Integer,OptionalInt>
                implements Sink.OfInt{
            @Override
            public void accept(int value){
                // Boxing is OK here, since few values will actually flow into the sink
                accept((Integer)value);
            }

            @Override
            public OptionalInt get(){
                return hasValue?OptionalInt.of(value):null;
            }
        }

        static final class OfLong extends FindSink<Long,OptionalLong>
                implements Sink.OfLong{
            @Override
            public void accept(long value){
                // Boxing is OK here, since few values will actually flow into the sink
                accept((Long)value);
            }

            @Override
            public OptionalLong get(){
                return hasValue?OptionalLong.of(value):null;
            }
        }

        static final class OfDouble extends FindSink<Double,OptionalDouble>
                implements Sink.OfDouble{
            @Override
            public void accept(double value){
                // Boxing is OK here, since few values will actually flow into the sink
                accept((Double)value);
            }

            @Override
            public OptionalDouble get(){
                return hasValue?OptionalDouble.of(value):null;
            }
        }
    }

    @SuppressWarnings("serial")
    private static final class FindTask<P_IN,P_OUT,O>
            extends AbstractShortCircuitTask<P_IN,P_OUT,O,FindTask<P_IN,P_OUT,O>>{
        private final FindOp<P_OUT,O> op;

        FindTask(FindOp<P_OUT,O> op,
                 PipelineHelper<P_OUT> helper,
                 Spliterator<P_IN> spliterator){
            super(helper,spliterator);
            this.op=op;
        }

        FindTask(FindTask<P_IN,P_OUT,O> parent,Spliterator<P_IN> spliterator){
            super(parent,spliterator);
            this.op=parent.op;
        }

        @Override
        protected FindTask<P_IN,P_OUT,O> makeChild(Spliterator<P_IN> spliterator){
            return new FindTask<>(this,spliterator);
        }

        @Override
        protected O doLeaf(){
            O result=helper.wrapAndCopyInto(op.sinkSupplier.get(),spliterator).get();
            if(!op.mustFindFirst){
                if(result!=null)
                    shortCircuit(result);
                return null;
            }else{
                if(result!=null){
                    foundResult(result);
                    return result;
                }else
                    return null;
            }
        }

        private void foundResult(O answer){
            if(isLeftmostNode())
                shortCircuit(answer);
            else
                cancelLaterNodes();
        }

        @Override
        public void onCompletion(CountedCompleter<?> caller){
            if(op.mustFindFirst){
                for(FindTask<P_IN,P_OUT,O> child=leftChild, p=null;child!=p;
                    p=child,child=rightChild){
                    O result=child.getLocalResult();
                    if(result!=null&&op.presentPredicate.test(result)){
                        setLocalResult(result);
                        foundResult(result);
                        break;
                    }
                }
            }
            super.onCompletion(caller);
        }

        @Override
        protected O getEmptyResult(){
            return op.emptyValue;
        }
    }
}

