package java.lang;

import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

public interface Iterable<T>{
    /*
    * ���������Ҫ����һ�����Ѻ����ӿ�
    * */
    default void forEach(Consumer<? super T> action){
        /*
        * ���зǿ��쳣�о�
        * */
        Objects.requireNonNull(action);
        /*
        * ��������� T ������ ������� Iterable<T> �����ǽӿ��Զ��巺��
        * ���� interface Collection<E> extends Iterable<E> ��ôȡ���� E
        * */
        for(T t : this){
            /*
            * �����ǵ���ʵ���߼������Զ���ķ����߼�
            * ����
            * Integer[] temp={1,2,3,4,5,6,7};
            * Arrays.stream(temp).forEach(System.out::println);
            * */
            action.accept(t);
        }
    }

    /*
    * �����Ƿָ����������
    * */
    default Spliterator<T> spliterator(){
        /*
        * ����һ�������͵ķָ������
        * */
        return Spliterators.spliteratorUnknownSize(iterator(),0);
    }

    Iterator<T> iterator();
}
