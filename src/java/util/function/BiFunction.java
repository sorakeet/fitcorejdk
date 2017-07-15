package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface BiFunction<T,U,R>{
    /*
    * 组合函数，这里的组合函数所包含的参数源可以是 Function 顶级函数接口
    *  BiFunction<Integer, Integer, String> biFunction1 = (num1,num2) -> "BiFunction:" +(num1 + num2);
    *  Function<String, String> biFunction2 = (string) -> "Function:" +(string);
    *  System.out.println(biFunction1.apply(50,25));
    *  System.out.println(biFunction1.andThen(biFunction2).apply(50,25));
    *  结果
    *  BiFunction:75
    *  Function:BiFunction:75
    * */
    default <V> BiFunction<T,U,V> andThen(Function<? super R,? extends V> after){
        /*
        * 例行检查
        * */
        Objects.requireNonNull(after);
        return (T t,U u)->after.apply(apply(t,u));
    }

    R apply(T t,U u);
}
