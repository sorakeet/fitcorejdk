/**
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.stream;

import java.util.Objects;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountedCompleter;
import java.util.function.*;

final class ForEachOps{
    private ForEachOps(){
    }

    public static <T> TerminalOp<T,Void> makeRef(Consumer<? super T> action,
                                                 boolean ordered){
        Objects.requireNonNull(action);
        return new ForEachOp.OfRef<>(action,ordered);
    }

    public static TerminalOp<Integer,Void> makeInt(IntConsumer action,
                                                   boolean ordered){
        Objects.requireNonNull(action);
        return new ForEachOp.OfInt(action,ordered);
    }

    public static TerminalOp<Long,Void> makeLong(LongConsumer action,
                                                 boolean ordered){
        Objects.requireNonNull(action);
        return new ForEachOp.OfLong(action,ordered);
    }

    public static TerminalOp<Double,Void> makeDouble(DoubleConsumer action,
                                                     boolean ordered){
        Objects.requireNonNull(action);
        return new ForEachOp.OfDouble(action,ordered);
    }

    static abstract class ForEachOp<T>
            implements TerminalOp<T,Void>, TerminalSink<T,Void>{
        private final boolean ordered;

        protected ForEachOp(boolean ordered){
            this.ordered=ordered;
        }
        // TerminalOp

        @Override
        public Void get(){
            return null;
        }

        static final class OfRef<T> extends ForEachOp<T>{
            final Consumer<? super T> consumer;

            OfRef(Consumer<? super T> consumer,boolean ordered){
                super(ordered);
                this.consumer=consumer;
            }

            @Override
            public void accept(T t){
                consumer.accept(t);
            }
        }        @Override
        public int getOpFlags(){
            return ordered?0:StreamOpFlag.NOT_ORDERED;
        }

        static final class OfInt extends ForEachOp<Integer>
                implements Sink.OfInt{
            final IntConsumer consumer;

            OfInt(IntConsumer consumer,boolean ordered){
                super(ordered);
                this.consumer=consumer;
            }

            @Override
            public StreamShape inputShape(){
                return StreamShape.INT_VALUE;
            }

            @Override
            public void accept(int t){
                consumer.accept(t);
            }
        }

        static final class OfLong extends ForEachOp<Long>
                implements Sink.OfLong{
            final LongConsumer consumer;

            OfLong(LongConsumer consumer,boolean ordered){
                super(ordered);
                this.consumer=consumer;
            }

            @Override
            public StreamShape inputShape(){
                return StreamShape.LONG_VALUE;
            }

            @Override
            public void accept(long t){
                consumer.accept(t);
            }
        }        @Override
        public <S> Void evaluateSequential(PipelineHelper<T> helper,
                                           Spliterator<S> spliterator){
            return helper.wrapAndCopyInto(this,spliterator).get();
        }

        static final class OfDouble extends ForEachOp<Double>
                implements Sink.OfDouble{
            final DoubleConsumer consumer;

            OfDouble(DoubleConsumer consumer,boolean ordered){
                super(ordered);
                this.consumer=consumer;
            }

            @Override
            public StreamShape inputShape(){
                return StreamShape.DOUBLE_VALUE;
            }

            @Override
            public void accept(double t){
                consumer.accept(t);
            }
        }

        @Override
        public <S> Void evaluateParallel(PipelineHelper<T> helper,
                                         Spliterator<S> spliterator){
            if(ordered)
                new ForEachOrderedTask<>(helper,spliterator,this).invoke();
            else
                new ForEachTask<>(helper,spliterator,helper.wrapSink(this)).invoke();
            return null;
        }
        // TerminalSink


        // Implementations


    }

    @SuppressWarnings("serial")
    static final class ForEachTask<S,T> extends CountedCompleter<Void>{
        private final Sink<S> sink;
        private final PipelineHelper<T> helper;
        private Spliterator<S> spliterator;
        private long targetSize;

        ForEachTask(PipelineHelper<T> helper,
                    Spliterator<S> spliterator,
                    Sink<S> sink){
            super(null);
            this.sink=sink;
            this.helper=helper;
            this.spliterator=spliterator;
            this.targetSize=0L;
        }

        ForEachTask(ForEachTask<S,T> parent,Spliterator<S> spliterator){
            super(parent);
            this.spliterator=spliterator;
            this.sink=parent.sink;
            this.targetSize=parent.targetSize;
            this.helper=parent.helper;
        }

        // Similar to AbstractTask but doesn't need to track child tasks
        public void compute(){
            Spliterator<S> rightSplit=spliterator, leftSplit;
            long sizeEstimate=rightSplit.estimateSize(), sizeThreshold;
            if((sizeThreshold=targetSize)==0L)
                targetSize=sizeThreshold=AbstractTask.suggestTargetSize(sizeEstimate);
            boolean isShortCircuit=StreamOpFlag.SHORT_CIRCUIT.isKnown(helper.getStreamAndOpFlags());
            boolean forkRight=false;
            Sink<S> taskSink=sink;
            ForEachTask<S,T> task=this;
            while(!isShortCircuit||!taskSink.cancellationRequested()){
                if(sizeEstimate<=sizeThreshold||
                        (leftSplit=rightSplit.trySplit())==null){
                    task.helper.copyInto(taskSink,rightSplit);
                    break;
                }
                ForEachTask<S,T> leftTask=new ForEachTask<>(task,leftSplit);
                task.addToPendingCount(1);
                ForEachTask<S,T> taskToFork;
                if(forkRight){
                    forkRight=false;
                    rightSplit=leftSplit;
                    taskToFork=task;
                    task=leftTask;
                }else{
                    forkRight=true;
                    taskToFork=leftTask;
                }
                taskToFork.fork();
                sizeEstimate=rightSplit.estimateSize();
            }
            task.spliterator=null;
            task.propagateCompletion();
        }
    }

    @SuppressWarnings("serial")
    static final class ForEachOrderedTask<S,T> extends CountedCompleter<Void>{
        private final PipelineHelper<T> helper;
        private final long targetSize;
        private final ConcurrentHashMap<ForEachOrderedTask<S,T>,ForEachOrderedTask<S,T>> completionMap;
        private final Sink<T> action;
        private final ForEachOrderedTask<S,T> leftPredecessor;
        private Spliterator<S> spliterator;
        private Node<T> node;

        protected ForEachOrderedTask(PipelineHelper<T> helper,
                                     Spliterator<S> spliterator,
                                     Sink<T> action){
            super(null);
            this.helper=helper;
            this.spliterator=spliterator;
            this.targetSize=AbstractTask.suggestTargetSize(spliterator.estimateSize());
            // Size map to avoid concurrent re-sizes
            this.completionMap=new ConcurrentHashMap<>(Math.max(16,AbstractTask.LEAF_TARGET<<1));
            this.action=action;
            this.leftPredecessor=null;
        }

        ForEachOrderedTask(ForEachOrderedTask<S,T> parent,
                           Spliterator<S> spliterator,
                           ForEachOrderedTask<S,T> leftPredecessor){
            super(parent);
            this.helper=parent.helper;
            this.spliterator=spliterator;
            this.targetSize=parent.targetSize;
            this.completionMap=parent.completionMap;
            this.action=parent.action;
            this.leftPredecessor=leftPredecessor;
        }

        @Override
        public final void compute(){
            doCompute(this);
        }

        private static <S,T> void doCompute(ForEachOrderedTask<S,T> task){
            Spliterator<S> rightSplit=task.spliterator, leftSplit;
            long sizeThreshold=task.targetSize;
            boolean forkRight=false;
            while(rightSplit.estimateSize()>sizeThreshold&&
                    (leftSplit=rightSplit.trySplit())!=null){
                ForEachOrderedTask<S,T> leftChild=
                        new ForEachOrderedTask<>(task,leftSplit,task.leftPredecessor);
                ForEachOrderedTask<S,T> rightChild=
                        new ForEachOrderedTask<>(task,rightSplit,leftChild);
                // Fork the parent task
                // Completion of the left and right children "happens-before"
                // completion of the parent
                task.addToPendingCount(1);
                // Completion of the left child "happens-before" completion of
                // the right child
                rightChild.addToPendingCount(1);
                task.completionMap.put(leftChild,rightChild);
                // If task is not on the left spine
                if(task.leftPredecessor!=null){
                    /**
                     * Completion of left-predecessor, or left subtree,
                     * "happens-before" completion of left-most leaf node of
                     * right subtree.
                     * The left child's pending count needs to be updated before
                     * it is associated in the completion map, otherwise the
                     * left child can complete prematurely and violate the
                     * "happens-before" constraint.
                     */
                    leftChild.addToPendingCount(1);
                    // Update association of left-predecessor to left-most
                    // leaf node of right subtree
                    if(task.completionMap.replace(task.leftPredecessor,task,leftChild)){
                        // If replaced, adjust the pending count of the parent
                        // to complete when its children complete
                        task.addToPendingCount(-1);
                    }else{
                        // Left-predecessor has already completed, parent's
                        // pending count is adjusted by left-predecessor;
                        // left child is ready to complete
                        leftChild.addToPendingCount(-1);
                    }
                }
                ForEachOrderedTask<S,T> taskToFork;
                if(forkRight){
                    forkRight=false;
                    rightSplit=leftSplit;
                    task=leftChild;
                    taskToFork=rightChild;
                }else{
                    forkRight=true;
                    task=rightChild;
                    taskToFork=leftChild;
                }
                taskToFork.fork();
            }
            /**
             * Task's pending count is either 0 or 1.  If 1 then the completion
             * map will contain a value that is task, and two calls to
             * tryComplete are required for completion, one below and one
             * triggered by the completion of task's left-predecessor in
             * onCompletion.  Therefore there is no data race within the if
             * block.
             */
            if(task.getPendingCount()>0){
                // Cannot complete just yet so buffer elements into a Node
                // for use when completion occurs
                @SuppressWarnings("unchecked")
                IntFunction<T[]> generator=size->(T[])new Object[size];
                Node.Builder<T> nb=task.helper.makeNodeBuilder(
                        task.helper.exactOutputSizeIfKnown(rightSplit),
                        generator);
                task.node=task.helper.wrapAndCopyInto(nb,rightSplit).build();
                task.spliterator=null;
            }
            task.tryComplete();
        }

        @Override
        public void onCompletion(CountedCompleter<?> caller){
            if(node!=null){
                // Dump buffered elements from this leaf into the sink
                node.forEach(action);
                node=null;
            }else if(spliterator!=null){
                // Dump elements output from this leaf's pipeline into the sink
                helper.wrapAndCopyInto(action,spliterator);
                spliterator=null;
            }
            // The completion of this task *and* the dumping of elements
            // "happens-before" completion of the associated left-most leaf task
            // of right subtree (if any, which can be this task's right sibling)
            //
            ForEachOrderedTask<S,T> leftDescendant=completionMap.remove(this);
            if(leftDescendant!=null)
                leftDescendant.tryComplete();
        }
    }
}
