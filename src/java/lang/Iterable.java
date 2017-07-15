package java.lang;

import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

public interface Iterable<T>{
    /*
    * 这里参数需要的是一个消费函数接口
    * */
    default void forEach(Consumer<? super T> action){
        /*
        * 例行非空异常判决
        * */
        Objects.requireNonNull(action);
        /*
        * 这里的类型 T 受限于 传入参数 Iterable<T> 这里是接口自定义泛型
        * 例如 interface Collection<E> extends Iterable<E> 那么取决于 E
        * */
        for(T t : this){
            /*
            * 这里是调用实例逻辑，你自定义的方法逻辑
            * 例如
            * Integer[] temp={1,2,3,4,5,6,7};
            * Arrays.stream(temp).forEach(System.out::println);
            * */
            action.accept(t);
        }
    }

    /*
    * 这里是分割迭代器方法
    * */
    default Spliterator<T> spliterator(){
        /*
        * 返回一个带泛型的分割迭代器
        * */
        return Spliterators.spliteratorUnknownSize(iterator(),0);
    }

    Iterator<T> iterator();
}
