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
    * ������һ������ѭ����ֱ���޷�������һ��ʣ�����ƽ�
    * Ҳ���� tryAdvance(action) Ϊ false
    * */
    default void forEachRemaining(Consumer<? super T> action){
        do{
            /*��������޾���ѯ*/
        }while(tryAdvance(action));
    }

    /*
    * ���Ǽ� forEachRemaining ��һ�����ѷ���
    * ��������������һ��ʣ�����ƽ� ���ʧ���������ѯ
    * ����ķ������ƶ�
    * */
    boolean tryAdvance(Consumer<? super T> action);

    /*
    * ���Էָ���طָ������
    * */
    Spliterator<T> trySplit();

    /*
    * �õ�ȷ�еĴ�С�������ȡ
    * */
    default long getExactSizeIfKnown(){
        return (characteristics()&SIZED)==0?-1L:estimateSize();
    }

    /*
    * Ԥ��������С
    * */
    long estimateSize();

    /*
    * ������˼��ȡ������
    * */
    int characteristics();

    /*
    * ������˼���ж��Ƿ����ĳ������
    * */
    default boolean hasCharacteristics(int characteristics){
        return (characteristics()&characteristics)==characteristics;
    }

    /*
    * ȡ�ñȽ�������Ȼ���ͻ��Ǹ��ݵ����߲��������ƶ�
    * */
    default Comparator<? super T> getComparator(){
        throw new IllegalStateException();
    }

    /*
    * �ڲ��ӿ� OfPrimitive
    * ԭʼ���͵���
    * ע��̳� �� Spliterator
    * */
    interface OfPrimitive<T,T_CONS,T_SPLITR extends OfPrimitive<T,T_CONS,T_SPLITR>> extends Spliterator<T>{

        /*
        * ������һ������ѭ����ֱ���޷�������һ��ʣ�����ƽ�
        * Ҳ���� tryAdvance(action) Ϊ false
        * */
        @SuppressWarnings("overloads")
        default void forEachRemaining(T_CONS action){
            do{
                /*��������޾���ѯ*/
            }while(tryAdvance(action));
        }

        /*
          * ���Ǽ� forEachRemaining ��һ�����ѷ���
          * ��������������һ��ʣ�����ƽ� ���ʧ���������ѯ
          * ������  extends Spliterator<T>
          * */
        @SuppressWarnings("overloads")
        boolean tryAdvance(T_CONS action);

        /*
          * ���Էָ���طָ������
          * ������  extends Spliterator<T>
          * */
        @Override
        T_SPLITR trySplit();
    }

    /*
    * �ڲ��ӿ�
    * ע��̳� �� OfPrimitive
    * ��������ҲҪע�� IntConsumer��@FunctionalInterface����OfInt
    * */
    interface OfInt extends OfPrimitive<Integer,IntConsumer,OfInt>{
        /*
        * �̳��� OfPrimitive
        * */
        @Override
        OfInt trySplit();

        /*
        �̳��� Spliterator
        * ���ط���������ʵ��
        * ����Ĳ��� ��һ�����ѽӿ� �̳з��� Integer
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
        * ����̳� OfPrimitive ���̳�
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
