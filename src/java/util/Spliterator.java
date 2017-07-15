package java.util;

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

public interface Spliterator<T>{
    int ORDERED=0x00000010;
    int DISTINCT=0x00000001;
    int SORTED=0x00000004;
    int SIZED=0x00000040;
    int NONNULL=0x00000100;
    int IMMUTABLE=0x00000400;
    int CONCURRENT=0x00001000;
    int SUBSIZED=0x00004000;

    /*
    * 这里是一个无限循环，直到无法进行下一次剩余量推进
    * 也就是 tryAdvance(action) 为 false
    * */
    default void forEachRemaining(Consumer<? super T> action){
        do{
            /*这里进行无尽轮询*/
        }while(tryAdvance(action));
    }

    /*
    * 这是继 forEachRemaining 的一个消费方法
    * 他是用来进行下一次剩余量推进 如果失败则阻断轮询
    * 这里的泛型做推断
    * */
    boolean tryAdvance(Consumer<? super T> action);

    /*
    * 尝试分割，返回分割迭代器
    * */
    Spliterator<T> trySplit();

    /*
    * 得到确切的大小，如果可取
    * */
    default long getExactSizeIfKnown(){
        return (characteristics()&SIZED)==0?-1L:estimateSize();
    }

    /*
    * 预估容量大小
    * */
    long estimateSize();

    /*
    * 字面意思，取得特征
    * */
    int characteristics();

    /*
    * 字面意思，判断是否具有某种特征
    * */
    default boolean hasCharacteristics(int characteristics){
        return (characteristics()&characteristics)==characteristics;
    }

    /*
    * 取得比较器，当然泛型还是根据调用者参数类型推断
    * */
    default Comparator<? super T> getComparator(){
        throw new IllegalStateException();
    }

    /*
    * 内部接口 OfPrimitive
    * 原始类型调用
    * 注意继承 父 Spliterator
    * */
    interface OfPrimitive<T,T_CONS,T_SPLITR extends OfPrimitive<T,T_CONS,T_SPLITR>> extends Spliterator<T>{

        /*
        * 这里是一个无限循环，直到无法进行下一次剩余量推进
        * 也就是 tryAdvance(action) 为 false
        * */
        @SuppressWarnings("overloads")
        default void forEachRemaining(T_CONS action){
            do{
                /*这里进行无尽轮询*/
            }while(tryAdvance(action));
        }

        /*
          * 这是继 forEachRemaining 的一个消费方法
          * 他是用来进行下一次剩余量推进 如果失败则阻断轮询
          * 这里是  extends Spliterator<T>
          * */
        @SuppressWarnings("overloads")
        boolean tryAdvance(T_CONS action);

        /*
          * 尝试分割，返回分割迭代器
          * 这里是  extends Spliterator<T>
          * */
        @Override
        T_SPLITR trySplit();
    }

    /*
    * 内部接口
    * 注意继承 兄 OfPrimitive
    * 参数里面也要注意 IntConsumer（@FunctionalInterface），OfInt
    * */
    interface OfInt extends OfPrimitive<Integer,IntConsumer,OfInt>{
        /*
        * 继承自 OfPrimitive
        * */
        @Override
        OfInt trySplit();

        /*
        继承自 Spliterator
        * 重载方法，本地实现
        * 这里的参数 是一个消费接口 继承泛型 Integer
        * */
        @Override
        default boolean tryAdvance(Consumer<? super Integer> action){
            if(action instanceof IntConsumer){
                //return this.tryAdvance((IntConsumer)action);
                return tryAdvance((IntConsumer)action);
            }else{
                if(Tripwire.ENABLED)
                    Tripwire.trip(getClass(),
                            "{0} calling Spliterator.OfInt.tryAdvance((IntConsumer) action::accept)");
                return tryAdvance((IntConsumer)action::accept);
            }
        }

        /*
        * 这里继承 OfPrimitive 多层继承
        * */
        @Override
        boolean tryAdvance(IntConsumer action);

        @Override
        default void forEachRemaining(IntConsumer action){
            do{
            }while(tryAdvance(action));
        }

        @Override
        default void forEachRemaining(Consumer<? super Integer> action){
            if(action instanceof IntConsumer){
                forEachRemaining((IntConsumer)action);
            }else{
                if(Tripwire.ENABLED)
                    Tripwire.trip(getClass(),
                            "{0} calling Spliterator.OfInt.forEachRemaining((IntConsumer) action::accept)");
                forEachRemaining((IntConsumer)action::accept);
            }
        }
    }

    interface OfLong extends OfPrimitive<Long,LongConsumer,OfLong>{
        @Override
        OfLong trySplit();

        @Override
        boolean tryAdvance(LongConsumer action);

        @Override
        default void forEachRemaining(LongConsumer action){
            do{
            }while(tryAdvance(action));
        }

        @Override
        default boolean tryAdvance(Consumer<? super Long> action){
            if(action instanceof LongConsumer){
                return tryAdvance((LongConsumer)action);
            }else{
                if(Tripwire.ENABLED)
                    Tripwire.trip(getClass(),
                            "{0} calling Spliterator.OfLong.tryAdvance((LongConsumer) action::accept)");
                return tryAdvance((LongConsumer)action::accept);
            }
        }

        @Override
        default void forEachRemaining(Consumer<? super Long> action){
            if(action instanceof LongConsumer){
                forEachRemaining((LongConsumer)action);
            }else{
                if(Tripwire.ENABLED)
                    Tripwire.trip(getClass(),
                            "{0} calling Spliterator.OfLong.forEachRemaining((LongConsumer) action::accept)");
                forEachRemaining((LongConsumer)action::accept);
            }
        }
    }

    interface OfDouble extends OfPrimitive<Double,DoubleConsumer,OfDouble>{
        @Override
        OfDouble trySplit();

        @Override
        boolean tryAdvance(DoubleConsumer action);

        @Override
        default void forEachRemaining(DoubleConsumer action){
            do{
            }while(tryAdvance(action));
        }

        @Override
        default boolean tryAdvance(Consumer<? super Double> action){
            if(action instanceof DoubleConsumer){
                return tryAdvance((DoubleConsumer)action);
            }else{
                if(Tripwire.ENABLED)
                    Tripwire.trip(getClass(),
                            "{0} calling Spliterator.OfDouble.tryAdvance((DoubleConsumer) action::accept)");
                return tryAdvance((DoubleConsumer)action::accept);
            }
        }

        @Override
        default void forEachRemaining(Consumer<? super Double> action){
            if(action instanceof DoubleConsumer){
                forEachRemaining((DoubleConsumer)action);
            }else{
                if(Tripwire.ENABLED)
                    Tripwire.trip(getClass(),
                            "{0} calling Spliterator.OfDouble.forEachRemaining((DoubleConsumer) action::accept)");
                forEachRemaining((DoubleConsumer)action::accept);
            }
        }
    }
}
