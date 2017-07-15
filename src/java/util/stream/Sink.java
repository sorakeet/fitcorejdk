/**
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.stream;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

interface Sink<T> extends Consumer<T>{
    default void begin(long size){
    }

    default void end(){
    }

    default boolean cancellationRequested(){
        return false;
    }

    default void accept(int value){
        throw new IllegalStateException("called wrong accept method");
    }

    default void accept(long value){
        throw new IllegalStateException("called wrong accept method");
    }

    default void accept(double value){
        throw new IllegalStateException("called wrong accept method");
    }

    interface OfInt extends Sink<Integer>, IntConsumer{
        @Override
        default void accept(Integer i){
            if(Tripwire.ENABLED)
                Tripwire.trip(getClass(),"{0} calling Sink.OfInt.accept(Integer)");
            accept(i.intValue());
        }

        @Override
        void accept(int value);
    }

    interface OfLong extends Sink<Long>, LongConsumer{
        @Override
        void accept(long value);

        @Override
        default void accept(Long i){
            if(Tripwire.ENABLED)
                Tripwire.trip(getClass(),"{0} calling Sink.OfLong.accept(Long)");
            accept(i.longValue());
        }
    }

    interface OfDouble extends Sink<Double>, DoubleConsumer{
        @Override
        void accept(double value);

        @Override
        default void accept(Double i){
            if(Tripwire.ENABLED)
                Tripwire.trip(getClass(),"{0} calling Sink.OfDouble.accept(Double)");
            accept(i.doubleValue());
        }
    }

    static abstract class ChainedReference<T,E_OUT> implements Sink<T>{
        protected final Sink<? super E_OUT> downstream;

        public ChainedReference(Sink<? super E_OUT> downstream){
            this.downstream=Objects.requireNonNull(downstream);
        }

        @Override
        public void begin(long size){
            downstream.begin(size);
        }

        @Override
        public void end(){
            downstream.end();
        }

        @Override
        public boolean cancellationRequested(){
            return downstream.cancellationRequested();
        }
    }

    static abstract class ChainedInt<E_OUT> implements OfInt{
        protected final Sink<? super E_OUT> downstream;

        public ChainedInt(Sink<? super E_OUT> downstream){
            this.downstream=Objects.requireNonNull(downstream);
        }

        @Override
        public void begin(long size){
            downstream.begin(size);
        }

        @Override
        public void end(){
            downstream.end();
        }

        @Override
        public boolean cancellationRequested(){
            return downstream.cancellationRequested();
        }
    }

    static abstract class ChainedLong<E_OUT> implements OfLong{
        protected final Sink<? super E_OUT> downstream;

        public ChainedLong(Sink<? super E_OUT> downstream){
            this.downstream=Objects.requireNonNull(downstream);
        }

        @Override
        public void begin(long size){
            downstream.begin(size);
        }

        @Override
        public void end(){
            downstream.end();
        }

        @Override
        public boolean cancellationRequested(){
            return downstream.cancellationRequested();
        }
    }

    static abstract class ChainedDouble<E_OUT> implements OfDouble{
        protected final Sink<? super E_OUT> downstream;

        public ChainedDouble(Sink<? super E_OUT> downstream){
            this.downstream=Objects.requireNonNull(downstream);
        }

        @Override
        public void begin(long size){
            downstream.begin(size);
        }

        @Override
        public void end(){
            downstream.end();
        }

        @Override
        public boolean cancellationRequested(){
            return downstream.cancellationRequested();
        }
    }
}
