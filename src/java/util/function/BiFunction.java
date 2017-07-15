package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface BiFunction<T,U,R>{
    /*
    * ��Ϻ������������Ϻ����������Ĳ���Դ������ Function ���������ӿ�
    *  BiFunction<Integer, Integer, String> biFunction1 = (num1,num2) -> "BiFunction:" +(num1 + num2);
    *  Function<String, String> biFunction2 = (string) -> "Function:" +(string);
    *  System.out.println(biFunction1.apply(50,25));
    *  System.out.println(biFunction1.andThen(biFunction2).apply(50,25));
    *  ���
    *  BiFunction:75
    *  Function:BiFunction:75
    * */
    default <V> BiFunction<T,U,V> andThen(Function<? super R,? extends V> after){
        /*
        * ���м��
        * */
        Objects.requireNonNull(after);
        return (T t,U u)->after.apply(apply(t,u));
    }

    R apply(T t,U u);
}
